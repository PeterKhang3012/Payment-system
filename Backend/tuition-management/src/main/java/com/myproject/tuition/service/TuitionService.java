package com.myproject.tuition.service;

import org.springframework.stereotype.Service;

import com.myproject.tuition.entity.Tuition;
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
    public Tuition getTuitionByStudentId(Long studentId) {
        return tuitionRepository.findByStudentIdForUpdate(studentId)
        .orElseThrow(() -> new RuntimeException("Tuition not found for student ID: " + studentId));
    }

    public void markTuitionAsPaid(Long studentId) {
        Tuition tuition = getTuitionByStudentId(studentId);
        tuition.setStatus("PAID");
        tuitionRepository.save(tuition);
    }
}
