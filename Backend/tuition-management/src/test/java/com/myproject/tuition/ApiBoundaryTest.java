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
        mvc.perform(put("/api/tuitions/invalid!/claim").header("Authorization", "Bearer test")
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
        verify(service).claimTuition("1","test");
    }
    @Test void stringIdsAreTrimmedAndLeadingZerosPreserved() throws Exception {
        mvc.perform(get("/api/tuitions/{studentId}", " 001 ")).andExpect(status().isOk());
        verify(service).getTuitionByStudentId("001");
        mvc.perform(put("/api/tuitions/{studentId}/claim", " 001 ").param("paymentId", "test"))
            .andExpect(status().isOk());
        verify(service).claimTuition("001", "test");
    }
    @Test void alphanumericIdsPreserveAllOperations() throws Exception {
        for (String id : new String[]{"SV001", "aB01", "000", "0", "ABC", "AB123456789012345678"}) {
            mvc.perform(get("/api/tuitions/{studentId}", " " + id + " ")).andExpect(status().isOk());
            verify(service).getTuitionByStudentId(id);
            for (String operation : new String[]{"claim", "paid", "release"}) {
                mvc.perform(put("/api/tuitions/{studentId}/" + operation, id).param("paymentId", "test"))
                    .andExpect(status().isOk());
            }
            verify(service).claimTuition(id, "test");
            verify(service).markTuitionAsPaid(id, "test");
            verify(service).releaseTuition(id, "test");
        }
    }
    @Test void invalidStringIdsDoNotReachService() throws Exception {
        for (String id : new String[]{" ", "-1", "1.5", "SV!01", "SV 01", "123456789012345678901"}) {
            mvc.perform(get("/api/tuitions/{studentId}", id)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
    @Test void conflictIs409() throws Exception {
        doThrow(new com.myproject.tuition.exception.TuitionConflictException("State conflict"))
            .when(service).claimTuition("1","test");
        mvc.perform(put("/api/tuitions/1/claim").param("paymentId", "test"))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("State conflict"));
    }
    @Test void unexpectedFailureIsSanitized() {
        var response = new GlobalExceptionHandler().handleUnexpected(new IllegalStateException("SQL secret /private/key"));
        org.junit.jupiter.api.Assertions.assertEquals(500, response.getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals("Internal server error", response.getBody().get("message"));
    }
}
