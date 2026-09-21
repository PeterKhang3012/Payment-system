package com.myproject.payment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.myproject.payment.dto.TuitionResponse;
import com.myproject.payment.exception.TuitionNotFoundException;

@Component
public class TuitionClient {
    private final RestClient restClient;

    public TuitionClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:3002/api/tuitions")
                .build();
    }

    public TuitionResponse getTuitionByStudentId(Long studentId, String authorization) {
        try{
            return restClient.get()
                    .uri("/{studentId}", studentId)
                    .header("Authorization", authorization)
                    .retrieve()
                    .body(TuitionResponse.class);
        }catch(HttpClientErrorException.NotFound e){
             throw new TuitionNotFoundException(
                    "Tuition not found for student ID: " + studentId
            );
        }
    }

    public void markTuitionAsPaid(Long studentId, String authorization) {
        restClient.put()
                .uri("/{studentId}/paid", studentId)
                .header("Authorization", authorization)
                .retrieve()
                .body(Void.class);
    }
}
