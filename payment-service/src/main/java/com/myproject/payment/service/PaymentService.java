package com.myproject.payment.service;

import org.springframework.stereotype.Service;

import com.myproject.payment.entity.Tuition;
import com.myproject.payment.entity.Wallet;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {
    private final TuitionService tuitionService;
    private final WalletService walletService;
    public PaymentService(TuitionService tuitionService, WalletService walletService) {
        this.tuitionService = tuitionService;
        this.walletService = walletService;
    }

    @Transactional
    public void payTuition(String studentId, String userId, boolean acceptedTerms) {
        Wallet wallet = walletService.getWalletByUserId(userId);
        Tuition tuition = tuitionService.getTuitionByStudentId(studentId);

        //Checking that the user has accepted the terms and conditions before proceeding with the payment
        if (!acceptedTerms) {
            throw new RuntimeException("You must accept the terms and conditions to proceed with the payment.");
        }

        //checking if the tuition has already been paid
        if ("PAID".equalsIgnoreCase(tuition.getStatus())) {
            throw new RuntimeException("Tuition has already been paid for student ID: " + studentId);
        }

        //checking if the wallet has sufficient balance to cover the tuition amount
        if (wallet.getBalance().compareTo(tuition.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance in wallet for user ID: " + userId);
        }

        //Handling the payment process by deducting the tuition amount from the wallet balance and updating the tuition status to "PAID"
        wallet.setBalance(wallet.getBalance().subtract(tuition.getAmount()));
        tuition.setStatus("PAID");
    }
}
