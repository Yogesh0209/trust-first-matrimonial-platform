package com.tfmsp.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.HostAddMemberRequest;
import com.tfmsp.backend.dto.RegisterRequest;
import com.tfmsp.backend.dto.UserResponse;
import com.tfmsp.backend.entity.AuditLog;
import com.tfmsp.backend.entity.Bureau;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.repository.AuditLogRepository;
import com.tfmsp.backend.repository.BureauRepository;
import com.tfmsp.backend.repository.UserRepository;

@Service
public class HostMemberService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;
    private final AuditLogRepository auditLogRepository;

    public HostMemberService(AuthService authService,
            UserRepository userRepository,
            BureauRepository bureauRepository,
            AuditLogRepository auditLogRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public UserResponse addMember(Long hostUserId, HostAddMemberRequest req) {
        User host = userRepository.findById(hostUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (host.getBureauId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with any bureau");
        }
        Bureau bureau = bureauRepository.findById(host.getBureauId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bureau not found"));

        // reuse the normal registration rules (age, unique email/phone, plan limit)
        RegisterRequest reg = new RegisterRequest();
        reg.setFullName(req.getFullName());
        reg.setEmail(req.getEmail());
        reg.setPhone(req.getPhone());
        reg.setPassword(req.getPassword());
        reg.setGender(req.getGender());
        reg.setDateOfBirth(req.getDateOfBirth());
        reg.setBureauCode(bureau.getBureauCode());

        UserResponse created = authService.register(reg);

        // the Host vouches for this member, so no approval step
        User member = userRepository.findById(created.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        member.setStatus(UserStatus.ACTIVE);
        userRepository.save(member);

        auditLogRepository.save(new AuditLog(hostUserId, "HOST_ADD_MEMBER", "USER", member.getId(),
                "Host added member " + member.getEmail()));

        return created;
    }
}