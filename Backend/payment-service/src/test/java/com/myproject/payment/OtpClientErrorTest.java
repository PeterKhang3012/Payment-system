package com.myproject.payment;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.test.web.client.MockRestServiceServer;
import com.myproject.payment.client.OtpClient;
import com.myproject.payment.dto.OtpDtos.GenerateOtpRequest;
import com.myproject.payment.exception.GlobalExceptionHandler;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;

// API error regression: internal token rejection must not become a misleading user-authentication failure.
class OtpClientErrorTest {
    @Test void internalCredentialRejectionIs502() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new OtpClient(builder);
        server.expect(requestTo("http://otp-service:8083/api/otp/generate"))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("internal private details"));
        var exception = assertThrows(RestClientException.class,
            () -> client.generateOtp(new GenerateOtpRequest("payment", "test@example.invalid")));
        var response = new GlobalExceptionHandler().handleDownstream(exception);
        assertEquals(502,response.getStatusCode().value());
        assertEquals("Dependent service failed",response.getBody().get("message"));
        server.verify();
    }
}
