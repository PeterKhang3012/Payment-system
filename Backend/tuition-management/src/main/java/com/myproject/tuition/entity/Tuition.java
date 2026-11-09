package com.myproject.tuition.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tuition", uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        name = "UQ_tuition_student_id", columnNames = "student_id"))
public class Tuition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, length = 20)
    @org.hibernate.annotations.Nationalized
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 20)
    @jakarta.validation.constraints.Pattern(regexp = "[0-9]*[1-9][0-9]*")
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "processing_payment_id")
    private String processingPaymentId;

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    private void normalizeStudentId() {
        if (studentId != null) studentId = studentId.trim();
    }

    public Tuition() {
    }

    public Long getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId == null ? null : studentId.trim();
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessingPaymentId() {
        return processingPaymentId;
    }

    public void setProcessingPaymentId(String processingPaymentId) {
        this.processingPaymentId = processingPaymentId;
    }
}
