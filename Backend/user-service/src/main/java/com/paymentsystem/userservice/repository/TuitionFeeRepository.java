package com.paymentsystem.userservice.repository;

import com.paymentsystem.userservice.entity.TuitionFee;
import com.paymentsystem.userservice.entity.TuitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TuitionFeeRepository extends JpaRepository<TuitionFee, Long> {

    List<TuitionFee> findByStudentId(String studentId);

    /**
     * ATOMIC UPDATE: chỉ cập nhật khi status = expectedStatus (UNPAID).
     * Ngăn race condition và double-payment.
     *
     * Trả về: 1 = cập nhật thành công | 0 = thất bại (đã PAID hoặc không tìm thấy)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TuitionFee t " +
           "SET t.status = :newStatus, t.paymentId = :paymentId " +
           "WHERE t.id = :id AND t.status = :expectedStatus")
    int updateStatusConditionally(@Param("id")             Long id,
                                   @Param("newStatus")      TuitionStatus newStatus,
                                   @Param("expectedStatus") TuitionStatus expectedStatus,
                                   @Param("paymentId")      String paymentId);
}
