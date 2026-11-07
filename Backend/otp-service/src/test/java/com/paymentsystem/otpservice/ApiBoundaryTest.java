package com.paymentsystem.otpservice;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import com.paymentsystem.otpservice.controller.OtpController;
import com.paymentsystem.otpservice.service.OtpService;
import com.paymentsystem.otpservice.exception.GlobalException;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// API boundary regression: reject bad input before OTP persistence or email delivery.
class ApiBoundaryTest {
    @Test void malformedAndInvalidBodiesHaveNoSideEffects() throws Exception {
        var service = mock(OtpService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new OtpController(service))
            .setControllerAdvice(new GlobalException.Handler()).build();
        for (String body : new String[]{"", "null", "{}", "[]", "{", "{\"paymentId\":\"p\",\"code\":\"12\"}"}) {
            mvc.perform(post("/api/otp/verify").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        }
        verifyNoInteractions(service);
    }
    @Test void unsupportedMediaTypePreserves415() throws Exception {
        var service = mock(OtpService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new OtpController(service))
            .setControllerAdvice(new GlobalException.Handler()).build();
        mvc.perform(post("/api/otp/verify").contentType(MediaType.TEXT_PLAIN).content("private details"))
            .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.message").value("Invalid request"));
        verifyNoInteractions(service);
    }
    @Test void emailFailureDoesNotExposeInfrastructure() {
        var req = new org.springframework.mock.web.MockHttpServletRequest();
        var response = new GlobalException.Handler().handleEmailFail(
            new GlobalException.EmailSendException("private@example.com", new RuntimeException("SMTP password")),req);
        org.junit.jupiter.api.Assertions.assertEquals(503,response.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("Unable to send OTP email",response.getBody().message());
    }
}
