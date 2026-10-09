package com.myproject.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentTransactionResponse {
    final Long transactionId;
    final Long tuitionId;
    final BigDecimal amount;
    final String status;
    final LocalDateTime createdAt;

    public PaymentTransactionResponse(Long transactionId, Long tuitionId, BigDecimal amount, String status, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.tuitionId = tuitionId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Long getTuitionId() {
        return tuitionId;
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
