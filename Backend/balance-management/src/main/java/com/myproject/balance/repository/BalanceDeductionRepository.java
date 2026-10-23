package com.myproject.balance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.balance.entity.BalanceDeduction;

public interface BalanceDeductionRepository extends JpaRepository<BalanceDeduction, Long> {
    Optional<BalanceDeduction> findByPaymentId(String paymentId);
    
}

