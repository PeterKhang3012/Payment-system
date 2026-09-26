package com.paymentsystem.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Khoản học phí của sinh viên — bảng tuition_fees.
 *
 * Thiết kế loose-coupling: liên kết với users qua student_id (application-level)
 * thay vì FK cứng → tránh cascade delete, dễ migrate microservice sau này.
 */
@Entity
@Table(name = "tuition_fees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuitionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã sinh viên — khóa kết nối với bảng users (không dùng FK). */
    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    //thiếu tên sinh viên

    /** Học kỳ — ví dụ: "HK1-2025-2026". */
    @Column(nullable = false, length = 50)
    private String semester;

    /** Số tiền học phí (VND) — CHECK amount > 0 ở DB level. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private TuitionStatus status = TuitionStatus.UNPAID;

    @Column(length = 500)
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * Mã giao dịch từ payment-service — null khi chưa thanh toán.
     * Điền vào sau khi payment-service gọi PATCH /api/tuitions/{id}/status.
     */
    @Column(name = "payment_id", length = 100)
    private String paymentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
