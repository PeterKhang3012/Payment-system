package com.paymentsystem.userservice.service;

import com.paymentsystem.userservice.dto.UserDtos.TuitionResponse;
import com.paymentsystem.userservice.dto.UserDtos.UpdateTuitionStatusRequest;
import com.paymentsystem.userservice.entity.TuitionFee;
import com.paymentsystem.userservice.entity.TuitionStatus;
import com.paymentsystem.userservice.exception.GlobalException;
import com.paymentsystem.userservice.repository.TuitionFeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Quản lý học phí sinh viên.
 *
 * Luồng cập nhật trạng thái (atomic — ngăn double-payment):
 * 1. UPDATE ... WHERE id=? AND status='UNPAID'
 * 2. rowsAffected=1 → thành công
 * 3. rowsAffected=0 → fetch record: không tồn tại (404) hoặc đã PAID (409)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TuitionService {

    private final TuitionFeeRepository tuitionFeeRepository;

    @Value("${internal.service-token}")
    private String validServiceToken;

    // ── Read ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TuitionResponse> getTuitionsByCurrentUser(UserDetails userDetails) {
        return tuitionFeeRepository.findAll().stream()
                .map(this::toTuitionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TuitionResponse> getTuitionsByStudentId(String studentId) {
        return tuitionFeeRepository.findByStudentId(studentId).stream()
                .map(this::toTuitionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TuitionResponse getTuitionById(Long id) {
        return tuitionFeeRepository.findById(id)
                .map(this::toTuitionResponse)
                .orElseThrow(() -> new GlobalException.ResourceNotFoundException("TuitionFee", "id", id));
    }

    // ── Write ───────────────────────────────────────────

    /**
     * Cập nhật học phí sang PAID.
     * Chỉ payment-service được gọi endpoint này (xác thực bằng service token).
     */
    @Transactional
    public TuitionResponse updateTuitionStatus(Long id, UpdateTuitionStatusRequest request) {
        if (request.status() != TuitionStatus.PAID) {
            throw new IllegalArgumentException("Chi cho phep cap nhat sang trang thai PAID. Nhan duoc: " + request.status());
        }

        log.info("updateTuitionStatus: id={} paymentId={}", id, request.paymentId());

        int rowsAffected = tuitionFeeRepository.updateStatusConditionally(
                id, TuitionStatus.PAID, TuitionStatus.UNPAID, request.paymentId());

        if (rowsAffected == 1) {
            log.info("updateTuitionStatus: thanh cong id={}", id);
            return tuitionFeeRepository.findById(id)
                    .map(this::toTuitionResponse)
                    .orElseThrow(() -> new GlobalException.ResourceNotFoundException("TuitionFee", "id", id));
        }

        // rowsAffected = 0 → phân tích nguyên nhân
        TuitionFee existing = tuitionFeeRepository.findById(id)
                .orElseThrow(() -> new GlobalException.ResourceNotFoundException("TuitionFee", "id", id));

        if (existing.getStatus() == TuitionStatus.PAID) {
            log.warn("updateTuitionStatus: double-payment bi chan id={} existingPaymentId={}", id, existing.getPaymentId());
            throw new GlobalException.TuitionAlreadyPaidException(id, existing.getPaymentId());
        }

        throw new IllegalStateException("Cap nhat trang thai that bai. Vui long thu lai.");
    }

    // ── Internal token validation ────────────────────────

    /** Ném InvalidServiceTokenException nếu token sai hoặc thiếu. */
    public void validateServiceToken(String receivedToken) {
        if (!validServiceToken.equals(receivedToken)) {
            throw new GlobalException.InvalidServiceTokenException();
        }
    }

    // ── Helper ──────────────────────────────────────────

    private TuitionResponse toTuitionResponse(TuitionFee t) {
        return new TuitionResponse(
                t.getId(),
                t.getStudentId(),
                t.getSemester(),
                t.getAmount(),
                t.getStatus() != null ? t.getStatus().name() : null,
                t.getDescription(),
                t.getDueDate() != null ? t.getDueDate().toString() : null,
                t.getPaymentId(),
                t.getCreatedAt() != null ? t.getCreatedAt().toString() : null
        );
    }
}
