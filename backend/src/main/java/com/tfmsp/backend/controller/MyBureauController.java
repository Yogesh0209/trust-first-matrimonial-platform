package com.tfmsp.backend.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.entity.Bureau;
import com.tfmsp.backend.entity.Plan;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.enums.SubscriptionStatus;
import com.tfmsp.backend.repository.BureauRepository;
import com.tfmsp.backend.repository.PlanRepository;
import com.tfmsp.backend.repository.SubscriptionRepository;
import com.tfmsp.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/me")
public class MyBureauController {

    private final UserRepository userRepository;
    private final BureauRepository bureauRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    public MyBureauController(UserRepository userRepository, BureauRepository bureauRepository,
            SubscriptionRepository subscriptionRepository, PlanRepository planRepository) {
        this.userRepository = userRepository;
        this.bureauRepository = bureauRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    @GetMapping("/bureau")
    public ResponseEntity<Map<String, Object>> myBureau(Authentication auth) {
        User user = userRepository.findById(Long.valueOf(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getBureauId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "You are not linked to a bureau");
        }
        Bureau bureau = bureauRepository.findById(user.getBureauId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bureau not found"));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", bureau.getName());
        out.put("bureauCode", bureau.getBureauCode());
        out.put("city", bureau.getCity());

        // current plan of the bureau (null if no active subscription)
        subscriptionRepository
                .findFirstByBureauIdAndStatusOrderByEndDateDesc(bureau.getId(), SubscriptionStatus.ACTIVE)
                .flatMap(s -> planRepository.findById(s.getPlanId()))
                .ifPresentOrElse((Plan p) -> {
                    out.put("planName", p.getName());
                    out.put("govIdVerification", p.isGovIdVerification());
                    out.put("crossBureauSearch", p.isCrossBureauSearch());
                }, () -> out.put("planName", null));

        // contact = the bureau's host account(s)
        List<User> hosts = userRepository.findByBureauIdAndRole(bureau.getId(), Role.HOST);
        if (!hosts.isEmpty()) {
            User host = hosts.get(0);
            out.put("contactName", host.getFullName());
            out.put("contactEmail", host.getEmail());
            out.put("contactPhone", host.getPhone());
        }
        return ResponseEntity.ok(out);
    }
}