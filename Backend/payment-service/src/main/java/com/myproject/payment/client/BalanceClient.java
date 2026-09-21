package com.myproject.payment.client;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.myproject.payment.dto.BalanceResponse;
import com.myproject.payment.dto.DeductBalanceResponse;

@Component
public class BalanceClient {
    private final RestClient restClient;

    public BalanceClient(RestClient.Builder builder) {
        this.restClient = builder
        .baseUrl("http://localhost:3001/api/balances")
        .build();
    }

    public BalanceResponse getBalance(String authorization) {
        return restClient.get()
                .uri("/me")
                .header("Authorization", authorization)
                .retrieve()
                .body(BalanceResponse.class);
    }

    public DeductBalanceResponse deductBalance(
        BigDecimal amount,
        String authorization
    ) {
        return restClient.post()
                .uri("/deduct")
                .header("Authorization", authorization)
                .body(amount)
                .retrieve()
                .body(DeductBalanceResponse.class);
    }
}
