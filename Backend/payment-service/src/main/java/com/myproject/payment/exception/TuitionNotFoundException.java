package com.myproject.payment.exception;

public class TuitionNotFoundException extends RuntimeException {

    public TuitionNotFoundException(String message) {
        super(message);
    }
}