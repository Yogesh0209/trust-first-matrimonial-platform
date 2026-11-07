package com.tfmsp.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tfmsp.backend.enums.PaymentStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record CreateOrderRequest(
            @NotNull(message = "Plan ID is required") Long planId,
            @NotNull(message = "Months is required")
            @Min(value = 1, message = "Minimum 1 month")
            @Max(value = 12, message = "Maximum 12 months") Integer months) {
    }

    public record VerifyRequest(
            @NotBlank(message = "razorpayOrderId is required") String razorpayOrderId,
            @NotBlank(message = "razorpayPaymentId is required") String razorpayPaymentId,
            @NotBlank(message = "razorpaySignature is required") String razorpaySignature) {
    }

    public record FailRequest(
            @NotBlank(message = "razorpayOrderId is required") String razorpayOrderId) {
    }

    // amount is in paise (Rs 1 = 100 paise), as Razorpay expects
    public record OrderResponse(String orderId, long amount, String currency, String keyId,
            String planName, int months) {
    }

    public record HistoryItem(Long id, String orderId, String paymentId, String planName,
            Integer months, BigDecimal amount, PaymentStatus status,
            LocalDateTime createdAt, LocalDateTime paidAt) {
    }
}