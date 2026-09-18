package com.myproject.balance.entity;


import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Balance")
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column( nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    public Balance() {
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
