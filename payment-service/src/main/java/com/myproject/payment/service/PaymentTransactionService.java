package com.myproject.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.myproject.payment.entity.PaymentTransaction;
import  com.myproject.payment.entity.Tuition;
import com.myproject.payment.repository.PaymentTransactionRepository;

@Service
public class PaymentTransactionService {
    private final PaymentTransactionRepository paymentTransactionRepository;

    public PaymentTransactionService(PaymentTransactionRepository paymentTransactionRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    public PaymentTransaction createPaymentTransaction(String userId, Tuition tuition, BigDecimal amount) {
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setUserId(userId);
        paymentTransaction.setTuition(tuition);
        paymentTransaction.setAmount(amount);
        paymentTransaction.setStatus("PAID");
        paymentTransaction.setCreatedAt(LocalDateTime.now());
        return paymentTransactionRepository.save(paymentTransaction);
    }

    public List<PaymentTransaction> getPaymentTransactionsByUserId(String userId) {
        return paymentTransactionRepository.findByUserId(userId);
    }
}
