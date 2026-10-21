package com.myproject.tuition.service;

import org.springframework.stereotype.Service;

import com.myproject.tuition.dto.TuitionResponse;
import com.myproject.tuition.entity.Tuition;
import com.myproject.tuition.exception.TuitionNotFoundException;
import com.myproject.tuition.repository.TuitionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TuitionService {
    private final TuitionRepository tuitionRepository;

    public TuitionService(TuitionRepository tuitionRepository) {
        this.tuitionRepository = tuitionRepository;
    }

    //search for tuition by studentID
    public TuitionResponse getTuitionByStudentId(Long studentId) {
        Tuition tuition = tuitionRepository.findByStudentIdForUpdate(studentId)
        .orElseThrow(() -> new TuitionNotFoundException("Tuition not found for student ID: " + studentId));
        return new TuitionResponse(
            tuition.getId(), 
            tuition.getStudentId(), 
            tuition.getStudentName(), 
            tuition.getStatus(),
            tuition.getAmount()
        );
    }

    public void claimTuition(Long studentId, String paymentId) {
        validatePaymentId(paymentId);

        int updated = tuitionRepository.claimTuition(
            studentId, paymentId
        );

        if (updated == 0) {
            throw new IllegalStateException(
                "Tuition is already paid, being processed, or not found"
            );
        }
    }

    public void markTuitionAsPaid(Long studentId, String paymentId) {
        validatePaymentId(paymentId);

        int updated = tuitionRepository.completeTuition(
            studentId, paymentId
        );

        if (updated == 0) {
            throw new IllegalStateException(
                "Cannot complete tuition: payment does not own the claim"
            );
        }
    }

    public void releaseTuition(Long studentId, String paymentId) {
        validatePaymentId(paymentId);

        int updated = tuitionRepository.releaseTuition(
            studentId, paymentId
        );

        if (updated == 0) {
            throw new IllegalStateException(
                "Cannot release tuition: payment does not own the claim"
            );
        }
    }

    private void validatePaymentId(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalArgumentException(
                "paymentId must not be blank"
            );
        }
    }
}
