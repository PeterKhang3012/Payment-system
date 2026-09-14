package com.myproject.payment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.payment.dto.PaymentRequest;
import com.myproject.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay")
    public void payTuition(@RequestBody PaymentRequest paymentRequest) {
        paymentService.payTuition(paymentRequest.getStudentId(), paymentRequest.getUserId(), paymentRequest.isAcceptedTerms());
    }
}
