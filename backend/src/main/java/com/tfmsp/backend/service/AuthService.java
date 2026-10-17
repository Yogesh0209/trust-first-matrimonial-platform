package com.tfmsp.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.AuthResponse;
import com.tfmsp.backend.dto.ChangePasswordRequest;
import com.tfmsp.backend.dto.LoginRequest;
import com.tfmsp.backend.dto.RegisterRequest;
import com.tfmsp.backend.dto.UserResponse;
import com.tfmsp.backend.entity.AuditLog;
import com.tfmsp.backend.entity.Bureau;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Gender;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.UserStatus;
import com.tfmsp.backend.repository.AuditLogRepository;
import com.tfmsp.backend.repository.BureauRepository;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.security.JwtService;

@Service
public class AuthService {

    // one message for every login failure, so nobody can tell if an email exists
    private static final String LOGIN_FAILED_MESSAGE =
            "Invalid email or password, or the account is temporarily locked. Please try again later.";

    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PlanLimitService planLimitService;
    private final AuditLogRepository auditLogRepository;

    public AuthService(UserRepository userRepository,
            BureauRepository bureauRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            PlanLimitService planLimitService,
            AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.planLimitService = planLimitService;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. Legal Marriageable Age Validation
        validateLegalAge(request.getGender(), request.getDateOfBirth());

        // 2. Validate Bureau Code
        Bureau bureau = bureauRepository.findByBureauCodeForUpdate(request.getBureauCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bureau code"));

        if (!"ACTIVE".equalsIgnoreCase(bureau.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bureau is currently not active for registration");
        }

        // 3. Tenant Capacity Check (FRD TEN-09, UC-01)
        Integer limit = planLimitService.getMemberLimit(bureau.getId());
        long currentMembers = userRepository.countByBureauIdAndRole(bureau.getId(), Role.END_USER);
        if (limit != null && currentMembers >= limit) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bureau member limit reached for its current plan");
        }

        // 4. Unique Email and Phone Validation
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
        }

        // 5. Create User under Bureau with initial PENDING status (FRD UC-01, Section 1.4)
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPhone(request.getPhone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.END_USER);
        user.setStatus(UserStatus.PENDING);
        user.setBureauId(bureau.getId());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getFullName(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, LOGIN_FAILED_MESSAGE));

        // 1. Account locked (FRD AUTH-03: 15 min lock after 5 failed attempts)
        if (user.isAccountLocked()) {
            audit(user.getId(), "LOGIN_FAILED", "Login attempt while account is locked");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, LOGIN_FAILED_MESSAGE);
        }

        // 2. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                audit(user.getId(), "ACCOUNT_LOCKED", "Account locked for 15 minutes after 5 failed logins");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, LOGIN_FAILED_MESSAGE);
            }
            userRepository.save(user);
            audit(user.getId(), "LOGIN_FAILED", "Failed login attempt " + attempts + " of 5");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, LOGIN_FAILED_MESSAGE);
        }

        // 3. Blocked, suspended or deleted (FRD Section 1.3, 1.5)
        if (user.isBlocked() || user.getStatus() == UserStatus.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account has been suspended");
        }
        if (user.getStatus() == UserStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your account has been deleted");
        }

        // Home bureau suspended (FRD Section 1.3)
        if (user.getBureauId() != null) {
            Bureau bureau = bureauRepository.findById(user.getBureauId()).orElse(null);
            if (bureau != null && "SUSPENDED".equalsIgnoreCase(bureau.getStatus())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your bureau is currently suspended");
            }
        }

        // 4. Successful login: reset failed attempts and update last active time (FRD FRE-01)
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);
        audit(user.getId(), "LOGIN_SUCCESS", "User logged in");

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getRole(), user.getBureauId(), jwtService.getExpirationSeconds());
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // never put passwords, tokens or OTPs in the details text (FRD SYS-04)
    private void audit(Long userId, String action, String details) {
        auditLogRepository.save(new AuditLog(userId, action, "USER", userId, details));
    }

    private void validateLegalAge(Gender gender, LocalDate dateOfBirth) {
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (gender == Gender.FEMALE && age < 18) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registration rejected: minimum legal marriageable age for females is 18 years");
        }
        if (gender == Gender.MALE && age < 21) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registration rejected: minimum legal marriageable age for males is 21 years");
        }
        if (gender == Gender.OTHER && age < 18) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Registration rejected: minimum legal age is 18 years");
        }
    }
}