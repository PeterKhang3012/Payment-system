package com.myproject.payment.service;

import org.springframework.stereotype.Service;

import com.myproject.payment.client.BalanceClient;
import com.myproject.payment.client.TuitionClient;
import com.myproject.payment.dto.BalanceResponse;
import com.myproject.payment.dto.DeductBalanceResponse;
import com.myproject.payment.dto.TuitionResponse;
import com.myproject.payment.exception.*;

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
            throw new TermsNotAcceptedException("Terms and conditions must be accepted.");
        }

        TuitionResponse tuitionResponse = tuitionClient.getTuitionByStudentId(studentId, authorization);

        if (tuitionResponse.getStatus().equalsIgnoreCase("PAID")) {
            throw new TuitionAlreadyPaidException("Tuition has aldready paid");
        }

        BalanceResponse balanceResponse = balanceClient.getBalance(authorization);

        if (balanceResponse.getBalance().compareTo(tuitionResponse.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance to pay tuition.");
        }

        DeductBalanceResponse deductBalance = balanceClient.deductBalance(tuitionResponse.getAmount(), authorization);

        // Dừng giao dịch nếu trừ tiền thất bại
        if (!deductBalance.isSuccess()) {
            throw new PaymentFailedException("Payment failed");
        }

        tuitionClient.markTuitionAsPaid(studentId, authorization);

        System.out.println("Tuition ID = " + tuitionResponse.getTuitionId());
        System.out.println("Tuition amount = " + tuitionResponse.getAmount());
        paymentTransactionService.createPaymentTransaction(userId, tuitionResponse.getTuitionId(),
                tuitionResponse.getAmount());
    }
}
