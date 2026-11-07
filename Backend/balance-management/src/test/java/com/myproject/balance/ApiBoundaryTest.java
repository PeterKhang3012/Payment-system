package com.myproject.balance;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import com.myproject.balance.controller.BalanceController;
import com.myproject.balance.service.BalanceService;
import com.myproject.balance.exception.GlobalExceptionHandler;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// API boundary regression: invalid requests must never reach business processing.
class ApiBoundaryTest {
    final BalanceService service = mock(BalanceService.class);
    final MockMvc mvc = MockMvcBuilders.standaloneSetup(new BalanceController(service))
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
        mvc.perform(post("/api/balances/deduct").header("Authorization", "Bearer test")
            .header("Idempotency-Key", "test").param("paymentId", "test")
            .contentType(MediaType.APPLICATION_JSON).content("-1"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(service);
    }
    @Test void malformedOrMissingInputHasNoSideEffects() throws Exception {
        mvc.perform(post("/api/balances/deduct").header("Authorization", "Bearer test").header("Idempotency-Key", "test").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(service);
    }
    @Test void validRequestPreservesDelegation() throws Exception {
        mvc.perform(post("/api/balances/deduct").header("Idempotency-Key", "test")
            .contentType(MediaType.APPLICATION_JSON).content("10.25")).andExpect(status().isOk());
        verify(service).deductBalance("user-1","test",new java.math.BigDecimal("10.25"));
    }
    @Test void invalidAmountRangeAndKeyHaveNoSideEffects() throws Exception {
        for (String value : new String[]{"0","null","{}","[]","100000000000000000","1.001"}) {
            mvc.perform(post("/api/balances/deduct").header("Idempotency-Key", "test")
                .contentType(MediaType.APPLICATION_JSON).content(value)).andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/balances/deduct").header("Idempotency-Key", "x".repeat(37))
            .contentType(MediaType.APPLICATION_JSON).content("1")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void unexpectedFailureIsSanitized() {
        var response = new GlobalExceptionHandler().handleUnexpected(new IllegalStateException("SQL secret /private/key"));
        org.junit.jupiter.api.Assertions.assertEquals(500, response.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("Internal server error", response.getBody().get("message"));
    }
}
