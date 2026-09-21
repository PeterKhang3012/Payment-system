package com.myproject.payment.client;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.myproject.payment.dto.BalanceResponse;
import com.myproject.payment.dto.DeductBalanceResponse;
import com.myproject.payment.exception.BalanceNotFoundException;

@Component
public class BalanceClient {
    private final RestClient restClient;

    public BalanceClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:3001/api/balances")
                .build();
    }

    public BalanceResponse getBalance(String authorization) {
        try{
            return restClient.get()
                    .uri("/me")
                    .header("Authorization", authorization)
                    .retrieve()
                    .body(BalanceResponse.class);
        }catch(HttpClientErrorException.NotFound e){
            throw new BalanceNotFoundException("Balance not found for current user");
        }
    }

    public DeductBalanceResponse deductBalance(
            BigDecimal amount,
            String authorization) {
        return restClient.post()
                .uri("/deduct")
                .header("Authorization", authorization)
                .body(amount)
                .retrieve()
                .body(DeductBalanceResponse.class);
    }
}
