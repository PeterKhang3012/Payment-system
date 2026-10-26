package com.myproject.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PaymentRequest {

    // API validation: reject missing or nonpositive IDs before side effects.
    @NotNull(message = "studentId is required")
    @Positive(message = "studentId must be positive")
    private Long studentId;

    // API validation: missing/null terms resolve to false and are rejected by the existing controller check.
    private boolean acceptedTerms;

    // API parsing: allow normal JSON property binding without changing valid request fields.
    public PaymentRequest() {}

    public PaymentRequest(Long studentId, boolean acceptedTerms) {
        this.studentId = studentId;
        this.acceptedTerms = acceptedTerms;
    }

    public Long getStudentId() {
        return studentId;
    }

    public boolean isAcceptedTerms() {
        return acceptedTerms;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setAcceptedTerms(boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }
}
