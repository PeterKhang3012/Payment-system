package com.myproject.payment;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import com.myproject.payment.controller.PaymentController;
import com.myproject.payment.service.PaymentService;
import com.myproject.payment.exception.GlobalExceptionHandler;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// API boundary regression: invalid requests must never reach business processing.
class ApiBoundaryTest {
    final PaymentService service = mock(PaymentService.class);
    final com.myproject.payment.client.UserClient userClient = mock(com.myproject.payment.client.UserClient.class);
    final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PaymentController(service, userClient))
        // Supply the authenticated principal without starting JWT infrastructure in this boundary test.
        .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
        .setControllerAdvice(new GlobalExceptionHandler()).build();

    @org.junit.jupiter.api.BeforeEach void authenticate() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test").header("alg", "RS256").claim("userId", "user-1").build();
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt));
    }
    @org.junit.jupiter.api.AfterEach void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test void invalidInputHasNoSideEffects() throws Exception {
        mvc.perform(post("/api/payments/request-otp").header("Authorization", "Bearer test")
            .header("Idempotency-Key", "test").param("paymentId", "test")
            .contentType(MediaType.APPLICATION_JSON).content("{\"studentId\":\"SV!01\",\"acceptedTerms\":true}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(service);
    }
    @Test void malformedOrMissingInputHasNoSideEffects() throws Exception {
        mvc.perform(post("/api/payments/request-otp").header("Authorization", "Bearer test").header("Idempotency-Key", "test").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(service);
    }
    @Test void invalidStringStudentIdsHaveNoSideEffects() throws Exception {
        for (String id : new String[]{"", " ", "-1", "1.5", "SV!01", "SV 01", "123456789012345678901"}) {
            mvc.perform(post("/api/payments/request-otp").header("Authorization", "Bearer test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":\"" + id + "\",\"acceptedTerms\":true}"))
                .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service, userClient);
    }
    @Test void stringStudentIdIsTrimmedWithoutLosingLeadingZeros() throws Exception {
        when(userClient.getCurrentUser("Bearer test")).thenReturn(
            new com.myproject.payment.dto.UserDtos.UserResponse(null, "user-1", "Student", "student@example.com", null, null));
        mvc.perform(post("/api/payments/request-otp").header("Authorization", "Bearer test")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"studentId\":\" 001 \",\"acceptedTerms\":true}"))
            .andExpect(status().isOk());
        verify(service).requestOtp("user-1", "001", true, "Bearer test", "student@example.com");
    }
    @Test void alphanumericStudentIdsReachOtpRequestUnchanged() throws Exception {
        when(userClient.getCurrentUser("Bearer test")).thenReturn(
            new com.myproject.payment.dto.UserDtos.UserResponse(null, "user-1", "Student", "student@example.com", null, null));
        for (String id : new String[]{"SV001", "aB01", "000", "0", "ABC", "AB123456789012345678"}) {
            mvc.perform(post("/api/payments/request-otp").header("Authorization", "Bearer test")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":\" " + id + " \",\"acceptedTerms\":true}"))
                .andExpect(status().isOk());
            verify(service).requestOtp("user-1", id, true, "Bearer test", "student@example.com");
        }
    }
    @Test void invalidOtpHasNoSideEffects() throws Exception {
        for (String body : new String[]{"{}","null","[]","{\"paymentId\":\"bad\",\"code\":\"123456\"}",
            "{\"paymentId\":\"12345678-1234-1234-1234-123456789012\",\"code\":\"123\"}"}) {
            mvc.perform(post("/api/payments/verify").header("Authorization","Bearer test")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
    @Test void validVerifyPreservesDelegation() throws Exception {
        when(userClient.getCurrentUser("Bearer test")).thenReturn(
            new com.myproject.payment.dto.UserDtos.UserResponse(null, "user-1", "Nguyen Van B", "user@gmail.com", null, null));
        mvc.perform(post("/api/payments/verify").header("Authorization","Bearer test")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"paymentId\":\"12345678-1234-1234-1234-123456789012\",\"code\":\"123456\"}"))
            .andExpect(status().isOk()).andExpect(content().string(""));
        verify(service).verifyOtpAndPay("user-1","Bearer test","12345678-1234-1234-1234-123456789012","123456", "user@gmail.com", "Nguyen Van B");
    }
    @Test void businessAndDownstreamErrorsAreSafe() {
        var handler = new GlobalExceptionHandler();
        org.junit.jupiter.api.Assertions.assertEquals(404,handler.PaymentNotFound(new com.myproject.payment.exception.PaymentNotFoundException("Unavailable")).getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals(409,handler.handleConflict(new com.myproject.payment.exception.PaymentConflictException("Conflict")).getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals(403,handler.handleForbidden(new com.myproject.payment.exception.PaymentForbiddenException("Forbidden")).getStatusCode().value());
        var error = new org.springframework.web.client.ResourceAccessException("internal host password");
        org.junit.jupiter.api.Assertions.assertEquals(502,handler.handleDownstream(error).getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("Payment could not be completed",handler.handlePaymentFailed(new com.myproject.payment.exception.PaymentFailedException("private details")).getBody().get("message"));
    }
    @Test void unexpectedFailureIsSanitized() {
        var response = new GlobalExceptionHandler().handleUnexpected(new IllegalStateException("SQL secret /private/key"));
        org.junit.jupiter.api.Assertions.assertEquals(500, response.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("Internal server error", response.getBody().get("message"));
    }
}
