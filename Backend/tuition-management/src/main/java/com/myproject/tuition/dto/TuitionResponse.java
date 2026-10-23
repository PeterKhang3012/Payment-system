package com.myproject.tuition.dto;

import java.math.BigDecimal;

public class TuitionResponse {
    private Long tuitionId;
    private Long studentId;
    private String studentName;
    private String status;
    private BigDecimal amount;

    public TuitionResponse(Long tuitionId, Long studentId, String studentName, String status, BigDecimal amount) {
        this.tuitionId = tuitionId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.status = status;
        this.amount = amount;
    }

    public Long getTuitionId() {
        return tuitionId;
    }

    public void setTuitionId(Long tuitionId) {
        this.tuitionId = tuitionId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
