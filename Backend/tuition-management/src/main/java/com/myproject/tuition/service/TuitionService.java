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

    public void markTuitionAsPaid(Long studentId) {
        Tuition tuition = tuitionRepository.findByStudentIdForUpdate(studentId)
            .orElseThrow(() -> new TuitionNotFoundException("Tuition not found for student ID: " + studentId));
        tuition.setStatus("PAID");
        tuitionRepository.save(tuition);
    }
}
