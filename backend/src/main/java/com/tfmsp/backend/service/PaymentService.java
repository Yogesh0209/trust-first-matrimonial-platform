package com.tfmsp.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.tfmsp.backend.dto.PaymentDtos.CreateOrderRequest;
import com.tfmsp.backend.dto.PaymentDtos.HistoryItem;
import com.tfmsp.backend.dto.PaymentDtos.OrderResponse;
import com.tfmsp.backend.dto.PaymentDtos.VerifyRequest;
import com.tfmsp.backend.dto.SubscriptionResponse;
import com.tfmsp.backend.entity.Payment;
import com.tfmsp.backend.entity.Plan;
import com.tfmsp.backend.enums.PaymentStatus;
import com.tfmsp.backend.repository.PaymentRepository;
import com.tfmsp.backend.repository.PlanRepository;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PlanRepository planRepository;
    private final TenantService tenantService;
    private final RestClient http;
    private final String keyId;
    private final String keySecret;

    public PaymentService(PaymentRepository paymentRepository,
            PlanRepository planRepository,
            TenantService tenantService,
            @Value("${razorpay.key-id:}") String keyId,
            @Value("${razorpay.key-secret:}") String keySecret) {
        this.paymentRepository = paymentRepository;
        this.planRepository = planRepository;
        this.tenantService = tenantService;
        this.http = RestClient.builder().baseUrl("https://api.razorpay.com/v1").build();
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    // Step 1: ask Razorpay for an order and remember it as CREATED
    public OrderResponse createOrder(Long bureauId, CreateOrderRequest req) {
        requireConfigured();

        Plan plan = planRepository.findById(req.planId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        if (!"ACTIVE".equalsIgnoreCase(plan.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This plan is not available");
        }

        BigDecimal total = plan.getPricePerMonth().multiply(BigDecimal.valueOf(req.months()));
        long paise = total.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        if (paise < 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This plan has no payable price. Ask the Super Admin to assign it.");
        }

        Map<String, Object> body = Map.of(
                "amount", paise,
                "currency", "INR",
                "receipt", "b" + bureauId + "-" + System.currentTimeMillis(),
                "notes", Map.of("bureauId", String.valueOf(bureauId), "planId", String.valueOf(plan.getId())));

        Map<String, Object> res;
        try {
            res = http.post().uri("/orders")
                    .headers(h -> h.setBasicAuth(keyId, keySecret))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
        } catch (RestClientException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment gateway error. Please try again.");
        }
        if (res == null || res.get("id") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment gateway returned no order");
        }

        String orderId = String.valueOf(res.get("id"));
        paymentRepository.save(new Payment(bureauId, plan.getId(), req.months(), total, orderId));

        return new OrderResponse(orderId, paise, "INR", keyId, plan.getName(), req.months());
    }

    // Step 2: check Razorpay's signature, then activate the plan
    @Transactional
    public SubscriptionResponse verify(Long bureauId, Long hostUserId, VerifyRequest req) {
        requireConfigured();

        Payment payment = ownPayment(bureauId, req.razorpayOrderId());

        if (payment.getStatus() == PaymentStatus.PAID) {
            // verify was already done (for example a double click): just return the current plan
            return tenantService.getBureauSubscription(bureauId);
        }

        String expected = sign(req.razorpayOrderId() + "|" + req.razorpayPaymentId());
        boolean valid = MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                req.razorpaySignature().getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verification failed");
        }

        SubscriptionResponse sub = tenantService.assignSubscription(
                bureauId, payment.getPlanId(), payment.getMonths(), hostUserId);

        payment.setStatus(PaymentStatus.PAID);
        payment.setGatewayPaymentId(req.razorpayPaymentId());
        payment.setPaidAt(LocalDateTime.now());
        payment.setSubscriptionId(sub.getSubscriptionId());
        paymentRepository.save(payment);

        return sub;
    }

    // Called when the popup reports a failure or the host closes it
    @Transactional
    public void markFailed(Long bureauId, String orderId) {
        Payment payment = ownPayment(bureauId, orderId);
        if (payment.getStatus() == PaymentStatus.CREATED) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    @Transactional(readOnly = true)
    public List<HistoryItem> history(Long bureauId) {
        List<Payment> payments = paymentRepository.findByBureauIdOrderByCreatedAtDesc(bureauId);
        Map<Long, String> planNames = planRepository.findAll().stream()
                .collect(Collectors.toMap(Plan::getId, Plan::getName, (a, b) -> a));
        return payments.stream()
                .map(p -> new HistoryItem(p.getId(), p.getGatewayOrderId(), p.getGatewayPaymentId(),
                        planNames.getOrDefault(p.getPlanId(), "Plan"), p.getMonths(), p.getAmount(),
                        p.getStatus(), p.getCreatedAt(), p.getPaidAt()))
                .toList();
    }

    private Payment ownPayment(Long bureauId, String orderId) {
        Payment payment = paymentRepository.findByGatewayOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        if (!bureauId.equals(payment.getBureauId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        return payment;
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not verify payment");
        }
    }

    private void requireConfigured() {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payments are not configured");
        }
    }
}