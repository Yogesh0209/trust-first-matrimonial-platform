package com.tfmsp.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.tfmsp.backend.enums.OtpPurpose;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender, @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    /** Sends the OTP by email. Never throws: a mail problem must not break registration. */
    public void sendOtp(String to, OtpPurpose purpose, String otp) {
        if (from == null || from.isBlank()) {
            return; // mail not configured, the console log is used instead
        }
        String subject = purpose == OtpPurpose.PASSWORD_RESET
                ? "Milan password reset code"
                : "Your Milan verification code";
        String body = "Your Milan code is: " + otp
                + "\n\nIt is valid for 5 minutes. If you did not request it, ignore this email.";
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Could not send OTP email to {}: {}", to, e.getMessage());
        }
    }
}