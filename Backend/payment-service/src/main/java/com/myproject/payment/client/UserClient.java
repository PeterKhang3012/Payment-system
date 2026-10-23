package com.myproject.payment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.myproject.payment.dto.UserDtos;
import com.myproject.payment.exception.TuitionNotFoundException;

@Component
public class UserClient {
    private final RestClient restClient;

    public UserClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://user-service:3006/api/users")
                .build();
    }

    public UserDtos.UserResponse getEmailByUserId(String authorization) {
        try{
            return restClient.get()
                    .uri("/me")
                    .header("Authorization", authorization)
                    .retrieve()
                    .body(UserDtos.UserResponse.class);
        }catch(HttpClientErrorException.NotFound e){
             throw new TuitionNotFoundException(
                    "User not found for the given authorization token"
            );
        }
    }
}
