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
                .baseUrl("http://tuition-management:3002/api/tuitions")
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

    public void claimTuition(
            Long studentId, String paymentId, String authorization) {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/{studentId}/claim")
                        .queryParam("paymentId", paymentId)
                        .build(studentId))
                .header("Authorization", authorization)
                .retrieve()
                .toBodilessEntity();
    }

    public void markTuitionAsPaid(
            Long studentId, String paymentId, String authorization) {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/{studentId}/paid")
                        .queryParam("paymentId", paymentId)
                        .build(studentId))
                .header("Authorization", authorization)
                .retrieve()
                .toBodilessEntity();
    }

    public void releaseTuition(
            Long studentId, String paymentId, String authorization) {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/{studentId}/release")
                        .queryParam("paymentId", paymentId)
                        .build(studentId))
                .header("Authorization", authorization)
                .retrieve()
                .toBodilessEntity();
    }
}
