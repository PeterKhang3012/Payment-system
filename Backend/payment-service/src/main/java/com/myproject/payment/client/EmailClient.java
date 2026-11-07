package com.myproject.payment.client;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.myproject.payment.dto.PaymentSuccessfulEvent;
@Service
public class EmailClient {

    private final RestClient restClient;

    @Value("${INTERNAL_SERVICE_TOKEN}")
    private String internalToken;

    public EmailClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://email-notification-service:3007/api/emails")
                .build();
    }

    public void sendPaymentSuccessEmail(
            PaymentSuccessfulEvent event) {

        restClient.post()
                .uri("/payment-success")
                .header(
                    "X-Internal-Service-Token",
                    internalToken
                )
                .body(event)
                .retrieve()
                .toBodilessEntity();
    }
}