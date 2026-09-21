package com.myproject.payment.exception;

public class BalanceNotFoundException extends RuntimeException{
    public BalanceNotFoundException (String message) {
        super(message);
    }
}
