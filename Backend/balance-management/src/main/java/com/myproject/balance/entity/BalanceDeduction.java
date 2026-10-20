package com.myproject.balance.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "Balance_Deduction",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_balance_deduction_payment_id",
            columnNames = {"payment_id"}
        )
    }
)
public class BalanceDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, length = 36)
    private String paymentId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    // Số tiền đã trừ
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Số dư sau khi trừ tiền
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfterDeduction;

    public BalanceDeduction() {
    }

    public BalanceDeduction(
            String paymentId,
            String userId,
            BigDecimal amount,
            BigDecimal balanceAfterDeduction) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.amount = amount;
        this.balanceAfterDeduction = balanceAfterDeduction;
    }

    public Long getId() {
        return id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfterDeduction() {
        return balanceAfterDeduction;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setBalanceAfterDeduction(BigDecimal balanceAfterDeduction) {
        this.balanceAfterDeduction = balanceAfterDeduction;
    }
}