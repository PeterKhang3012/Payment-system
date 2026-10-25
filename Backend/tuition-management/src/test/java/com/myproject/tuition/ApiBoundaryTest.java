package com.myproject.tuition;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import com.myproject.tuition.controller.TuitionController;
import com.myproject.tuition.service.TuitionService;
import com.myproject.tuition.exception.GlobalExceptionHandler;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// API boundary regression: invalid requests must never reach business processing.
class ApiBoundaryTest {
    final TuitionService service = mock(TuitionService.class);
    final MockMvc mvc = MockMvcBuilders.standaloneSetup(new TuitionController(service))
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
        mvc.perform(put("/api/tuitions/0/claim").header("Authorization", "Bearer test")
            .header("Idempotency-Key", "test").param("paymentId", "test")
            .contentType(MediaType.APPLICATION_JSON).content(""))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(service);
    }
    @Test void malformedOrMissingInputHasNoSideEffects() throws Exception {
        mvc.perform(put("/api/tuitions/not-a-number/claim"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        verifyNoInteractions(service);
    }
    @Test void validRequestPreservesDelegation() throws Exception {
        mvc.perform(put("/api/tuitions/1/claim").param("paymentId", "test"))
            .andExpect(status().isOk());
        verify(service).claimTuition(1L,"test");
    }
    @Test void conflictIs409() throws Exception {
        doThrow(new com.myproject.tuition.exception.TuitionConflictException("State conflict"))
            .when(service).claimTuition(1L,"test");
        mvc.perform(put("/api/tuitions/1/claim").param("paymentId", "test"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("State conflict"));
    }
    @Test void unexpectedFailureIsSanitized() {
        var response = new GlobalExceptionHandler().handleUnexpected(new IllegalStateException("SQL secret /private/key"));
        org.junit.jupiter.api.Assertions.assertEquals(500, response.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("Internal server error", response.getBody().get("message"));
    }
}
