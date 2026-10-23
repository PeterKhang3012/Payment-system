package com.myproject.balance.exception;

public class BalanceNotFoundException extends RuntimeException{
    public BalanceNotFoundException (String message) {
        super(message);
    }
}
