package com.myproject.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PaymentRequest {

    // API validation: reject missing or nonpositive IDs before side effects.
    @NotBlank(message = "studentId is required")
    @Size(max = 20, message = "studentId must not exceed 20 characters")
    @Pattern(regexp = "[0-9]*[1-9][0-9]*", message = "studentId must contain digits and be positive")
    private String studentId;

    // API validation: missing/null terms resolve to false and are rejected by the existing controller check.
    private boolean acceptedTerms;

    // API parsing: allow normal JSON property binding without changing valid request fields.
    public PaymentRequest() {}

    public PaymentRequest(String studentId, boolean acceptedTerms) {
        this.studentId = studentId == null ? null : studentId.trim();
        this.acceptedTerms = acceptedTerms;
    }

    public String getStudentId() {
        return studentId;
    }

    public boolean isAcceptedTerms() {
        return acceptedTerms;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId == null ? null : studentId.trim();
    }

    public void setAcceptedTerms(boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }
}
