package com.myproject.tuition.exception;

public class TuitionNotFoundException extends RuntimeException {

    public TuitionNotFoundException(String message) {
        super(message);
    }
}