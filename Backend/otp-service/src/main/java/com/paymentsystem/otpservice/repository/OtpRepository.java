package com.paymentsystem.otpservice.repository;

import com.paymentsystem.otpservice.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByPaymentId(String paymentId);

    /**
     * Atomic verify: UPDATE chỉ khi code đúng + chưa dùng + chưa hết hạn + chưa vượt max attempts.
     * Trả về 1 nếu thành công, 0 nếu thất bại.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Otp o SET o.used = true " +
           "WHERE o.paymentId = :paymentId " +
           "  AND o.code = :code " +
           "  AND o.used = false " +
           "  AND o.expiresAt > :now " +
           "  AND o.attempts < :maxAttempts")
    int verifyAndMarkUsed(@Param("paymentId")   String paymentId,
                           @Param("code")        String code,
                           @Param("now")         LocalDateTime now,
                           @Param("maxAttempts") int maxAttempts);

    /**
     * Tăng attempts atomically — chỉ khi OTP chưa hết hạn và chưa vượt max.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Otp o SET o.attempts = o.attempts + 1 " +
           "WHERE o.paymentId = :paymentId " +
           "  AND o.expiresAt > :now " +
           "  AND o.attempts < :maxAttempts")
    int incrementAttempts(@Param("paymentId")   String paymentId,
                           @Param("now")         LocalDateTime now,
                           @Param("maxAttempts") int maxAttempts);

    /** Xóa OTP hết hạn — dùng bởi OtpCleanupScheduler. Trả về số bản ghi đã xóa. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :threshold")
    int deleteByExpiresAtBefore(@Param("threshold") LocalDateTime threshold);
}
