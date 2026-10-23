package com.myproject.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.payment.entity.PaymentTransaction;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByUserId(String userId);
}
