package com.myproject.payment.dto;

import java.math.BigDecimal;

public class PaymentAttempt {

    private String paymentId;
    private String userId;
    private String studentId;
    private BigDecimal amount;

    public PaymentAttempt() {
    }

    public PaymentAttempt(
            String paymentId,
            String userId,
            String studentId,
            BigDecimal amount) {

        this.paymentId = paymentId;
        this.userId = userId;
        this.studentId = studentId == null ? null : studentId.trim();
        this.amount = amount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId == null ? null : studentId.trim();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
