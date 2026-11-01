package com.myproject.emailnotification.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.emailnotification.dto.PaymentSuccessfulEvent;
import com.myproject.emailnotification.service.EmailNotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailNotificationService emailService;

    @Value("${app.internal.token}")
    private String internalToken;

    public EmailController(EmailNotificationService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/payment-success")
    public ResponseEntity<?> sendPaymentSuccessEmail(
            @RequestHeader("X-Internal-Service-Token")
            String token,

            @Valid @RequestBody
            PaymentSuccessfulEvent event) {

        if (!MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                internalToken.getBytes(StandardCharsets.UTF_8))) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Unauthorized"));
        }

        try {
            emailService.sendPaymentSuccessEmail(event);

            return ResponseEntity.ok(
                    Map.of("message", "Email sent successfully")
            );

        } catch (Exception e) {

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of("message", "Failed to send email")
            );
        }
    }
}