package com.myproject.balance.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException (String message) {
        super(message);
    }
}