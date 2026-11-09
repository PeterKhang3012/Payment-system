package com.MyProject.User.entity;

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

/**
 * Tài khoản sinh viên — bảng users.
 * Không implement UserDetails trực tiếp (giữ entity sạch).
 * CustomUserDetailsService chịu trách nhiệm convert sang Spring Security principal.
 */
@Entity
@Table(name = "users", uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        name = "UQ_users_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String email;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    private void normalizeAccountFields() {
        if (email != null) email = email.trim();
    }

    @Column(name = "phone_number", nullable = true, length = 15)
    private String phoneNumber;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
