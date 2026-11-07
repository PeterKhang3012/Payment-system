package com.myproject.payment.exception;

// API error handling: classify an existing payment rejection without changing business rules.
public class PaymentConflictException extends RuntimeException {
    public PaymentConflictException(String message) { super(message); }
}
