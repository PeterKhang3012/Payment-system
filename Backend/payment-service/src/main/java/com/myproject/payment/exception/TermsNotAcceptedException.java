package com.myproject.payment.exception;

public class TermsNotAcceptedException extends RuntimeException {

    public TermsNotAcceptedException(String message) {
        super(message);
    }
}