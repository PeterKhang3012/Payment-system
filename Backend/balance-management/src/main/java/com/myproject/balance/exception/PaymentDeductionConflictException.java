package com.myproject.balance.exception;

public class PaymentDeductionConflictException extends RuntimeException {
    public PaymentDeductionConflictException(String message) {
        super(message);
    }
}
