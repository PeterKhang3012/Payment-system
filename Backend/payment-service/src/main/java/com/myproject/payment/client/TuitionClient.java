package com.myproject.payment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.myproject.payment.dto.TuitionResponse;

@Component
public class TuitionClient {
    private final RestClient restClient;

    public TuitionClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:3002/api/tuitions")
                .build();
    }

    public TuitionResponse getTuitionByStudentId(Long studentId, String authorization) {
        return restClient.get()
                .uri("/{studentId}", studentId)
                .header("Authorization", authorization)
                .retrieve()
                .body(TuitionResponse.class);
    }

    public void markTuitionAsPaid(Long studentId, String authorization) {
        restClient.put()
                .uri("/{studentId}/paid", studentId)
                .header("Authorization", authorization)
                .retrieve()
                .body(Void.class);
    }
}
