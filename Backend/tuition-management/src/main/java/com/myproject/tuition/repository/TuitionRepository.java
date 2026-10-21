package com.myproject.tuition.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.myproject.tuition.entity.Tuition;

import jakarta.persistence.LockModeType;

public interface TuitionRepository extends JpaRepository<Tuition, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tuition t WHERE t.studentId = :studentId")
    Optional<Tuition> findByStudentIdForUpdate(Long studentId);

        @Modifying
    @Query("""
        UPDATE Tuition t
        SET t.status = 'PROCESSING',
            t.processingPaymentId = :paymentId
        WHERE t.studentId = :studentId
          AND t.status = 'UNPAID'
    """)
    int claimTuition(
        @Param("studentId") Long studentId,
        @Param("paymentId") String paymentId
    );

    // Chỉ giao dịch đang giữ claim mới được hoàn tất.
    @Modifying
    @Query("""
        UPDATE Tuition t
        SET t.status = 'PAID',
            t.processingPaymentId = NULL
        WHERE t.studentId = :studentId
          AND t.status = 'PROCESSING'
          AND t.processingPaymentId = :paymentId
    """)
    int completeTuition(
        @Param("studentId") Long studentId,
        @Param("paymentId") String paymentId
    );

    // Chỉ giao dịch đang giữ claim mới được giải phóng.
    @Modifying
    @Query("""
        UPDATE Tuition t
        SET t.status = 'UNPAID',
            t.processingPaymentId = NULL
        WHERE t.studentId = :studentId
          AND t.status = 'PROCESSING'
          AND t.processingPaymentId = :paymentId
    """)
    int releaseTuition(
        @Param("studentId") Long studentId,
        @Param("paymentId") String paymentId
    );
}
