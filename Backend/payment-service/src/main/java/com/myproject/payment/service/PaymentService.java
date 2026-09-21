package com.myproject.payment.service;

import org.springframework.stereotype.Service;

import com.myproject.payment.client.BalanceClient;
import com.myproject.payment.client.TuitionClient;
import com.myproject.payment.dto.BalanceResponse;
import com.myproject.payment.dto.TuitionResponse;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {
    private final PaymentTransactionService paymentTransactionService;
    private final TuitionClient tuitionClient;
    private final BalanceClient balanceClient;

    public PaymentService(PaymentTransactionService paymentTransactionService, TuitionClient tuitionClient,
            BalanceClient balanceClient) {
        this.paymentTransactionService = paymentTransactionService;
        this.tuitionClient = tuitionClient;
        this.balanceClient = balanceClient;
    }

    @Transactional
    public void payTuition(String userId, Long studentId, boolean acceptedTerms, String authorization) {
        if (!acceptedTerms) {
            throw new IllegalArgumentException("Terms and conditions must be accepted.");
        }

        TuitionResponse tuitionResponse = tuitionClient.getTuitionByStudentId(studentId, authorization);
        BalanceResponse balanceResponse = balanceClient.getBalance(authorization);
        
        if(balanceResponse.getBalance().compareTo(tuitionResponse.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance to pay tuition.");
        }

        balanceClient.deductBalance(tuitionResponse.getAmount(), authorization);

        tuitionClient.markTuitionAsPaid(studentId, authorization);

        
        System.out.println("Tuition ID = " + tuitionResponse.getTuitionId());
        System.out.println("Tuition amount = " + tuitionResponse.getAmount());
        paymentTransactionService.createPaymentTransaction(userId, tuitionResponse.getTuitionId(), tuitionResponse.getAmount());
    }
}
