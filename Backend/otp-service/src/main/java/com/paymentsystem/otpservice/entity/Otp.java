package com.paymentsystem.otpservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

/** Bảng otps — lưu OTP của mỗi giao dịch. */
@Entity
@Table(name = "otps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã giao dịch từ payment-service — UNIQUE. */
    @Column(name = "payment_id", nullable = false, unique = true, length = 100)
    private String paymentId;

    /** Mã 6 chữ số sinh bởi SecureRandom. */
    @Column(nullable = false, length = 6)
    private String code;

    /** Email nhận OTP. */
    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** true sau khi verify thành công — mỗi OTP chỉ dùng 1 lần. */
    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    /** Số lần nhập sai — max 5 lần. */
    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
