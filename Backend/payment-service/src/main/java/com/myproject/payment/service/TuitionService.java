package com.myproject.payment.service;

import org.springframework.stereotype.Service;

import com.myproject.payment.entity.Tuition;
import com.myproject.payment.repository.TuitionRepository;

import jakarta.transaction.Transactional;

@Service
public class TuitionService {
    private final TuitionRepository tuitionRepository;

    public TuitionService(TuitionRepository tuitionRepository) {
        this.tuitionRepository = tuitionRepository;
    }

    //search for tuition by studentID
    @Transactional
    public Tuition getTuitionByStudentId(String studentId) {
        return tuitionRepository.findByStudentIdForUpdate(studentId)
        .orElseThrow(() -> new RuntimeException("Tuition not found for student ID: " + studentId));
    }
}
