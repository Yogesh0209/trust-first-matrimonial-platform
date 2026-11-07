package com.tfmsp.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.PaymentDtos.CreateOrderRequest;
import com.tfmsp.backend.dto.PaymentDtos.FailRequest;
import com.tfmsp.backend.dto.PaymentDtos.HistoryItem;
import com.tfmsp.backend.dto.PaymentDtos.OrderResponse;
import com.tfmsp.backend.dto.PaymentDtos.VerifyRequest;
import com.tfmsp.backend.dto.SubscriptionResponse;
import com.tfmsp.backend.entity.User;
import com.tfmsp.backend.enums.Role;
import com.tfmsp.backend.repository.UserRepository;
import com.tfmsp.backend.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/host/payments")
public class HostPaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public HostPaymentController(PaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    private User verifyHost(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userRepository.findById(Long.valueOf(auth.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (user.getRole() != Role.HOST) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Host role required");
        }
        if (user.getBureauId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not associated with any bureau");
        }
        return user;
    }

    @PostMapping("/create-order")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest req, Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(paymentService.createOrder(host.getBureauId(), req));
    }

    @PostMapping("/verify")
    public ResponseEntity<SubscriptionResponse> verify(
            @Valid @RequestBody VerifyRequest req, Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(paymentService.verify(host.getBureauId(), host.getId(), req));
    }

    @PostMapping("/fail")
    public ResponseEntity<Map<String, String>> fail(
            @Valid @RequestBody FailRequest req, Authentication auth) {
        User host = verifyHost(auth);
        paymentService.markFailed(host.getBureauId(), req.razorpayOrderId());
        return ResponseEntity.ok(Map.of("message", "Payment marked as failed"));
    }

    @GetMapping
    public ResponseEntity<List<HistoryItem>> history(Authentication auth) {
        User host = verifyHost(auth);
        return ResponseEntity.ok(paymentService.history(host.getBureauId()));
    }
}