package com.myproject.payment.exception;

// API error handling: classify an existing payment rejection without changing business rules.
public class PaymentForbiddenException extends RuntimeException {
    public PaymentForbiddenException(String message) { super(message); }
}
