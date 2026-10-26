package com.myproject.payment.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.myproject.payment.dto.OtpDtos.GenerateOtpRequest;
import com.myproject.payment.dto.OtpDtos.GenerateOtpResponse;
import com.myproject.payment.dto.OtpDtos.VerifyOtpRequest;
import com.myproject.payment.dto.OtpDtos.VerifyOtpResponse;

@Component
public class OtpClient {

    private final RestClient restClient;

    private static final String OTP_SERVICE_URL = "http://otp-service:8083";
    private static final String INTERNAL_SERVICE_TOKEN =
            "ps-internal-service-token-2025";

    public OtpClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl(OTP_SERVICE_URL)
                .build();
    }

    /**
     * POST /api/otp/generate
     */
    public GenerateOtpResponse generateOtp(
            GenerateOtpRequest request) {

        return restClient.post()
                .uri("/api/otp/generate")
                .header(
                    "X-Internal-Service-Token",
                    INTERNAL_SERVICE_TOKEN
                )
                .body(request)
                .retrieve()
                // API error handling: internal service credentials failing are a dependency error, not user authentication.
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        (httpRequest, response) -> {
                            throw new org.springframework.web.client.RestClientException("OTP service authorization failed");
                        })
                .body(GenerateOtpResponse.class);
    }

    /**
     * POST /api/otp/verify
     */
    public VerifyOtpResponse verifyOtp(
            VerifyOtpRequest request) {

        return restClient.post()
                .uri("/api/otp/verify")
                .header(
                    "X-Internal-Service-Token",
                    INTERNAL_SERVICE_TOKEN
                )
                .body(request)
                .retrieve()
                // API error handling: do not misreport internal token/configuration failures as a user login failure.
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        (httpRequest, response) -> {
                            throw new org.springframework.web.client.RestClientException("OTP service authorization failed");
                        })
                .body(VerifyOtpResponse.class);
    }
}
