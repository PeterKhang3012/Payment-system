package com.myproject.payment.exception;

public class TuitionAlreadyPaidException extends RuntimeException {

    public TuitionAlreadyPaidException(String message) {
        super(message);
    }
}