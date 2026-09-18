package com.myproject.balance.dto;

import java.math.BigDecimal;

public class BalanceResponse {
    private boolean success;
    private String message;
    private BigDecimal balance;

    public BalanceResponse(boolean success, String message, BigDecimal balance) {
        this.success = success;
        this.message = message;
        this.balance = balance;
    }
    
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
