package com.myproject.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransactionResponse {
    final Long transactionId;
    final Long studentId;
    final BigDecimal amount;
    final String status;
    final LocalDateTime createdAt;

    public PaymentTransactionResponse(Long transactionId, Long studentId, BigDecimal amount, String status, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.studentId = studentId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
