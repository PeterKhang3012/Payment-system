package com.myproject.payment.dto;
import java.math.BigDecimal;

public class BalanceResponse {
    private BigDecimal balance;

    public BalanceResponse(BigDecimal balance){
        this.balance = balance;
    }

    public BigDecimal getBalance(){
        return this.balance;
    }

    public void setBalance(BigDecimal balance){
        this.balance = balance;
    }
}
