package com.myproject.payment.dto;

public class PaymentRequest {
    private String studentId;
    private String userId;
    private boolean acceptedTerms;

    public PaymentRequest(String studentId, String userId, boolean acceptedTerms) {
        this.studentId = studentId;
        this.userId = userId;
        this.acceptedTerms = acceptedTerms;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isAcceptedTerms() {
        return acceptedTerms;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAcceptedTerms(boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }
}
