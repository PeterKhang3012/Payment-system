package com.myproject.payment.dto;

public class PaymentRequest {
    private Long studentId;
    private boolean acceptedTerms;

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
