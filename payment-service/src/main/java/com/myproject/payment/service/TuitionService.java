package com.myproject.payment.service;

import org.springframework.stereotype.Service;

import com.myproject.payment.entity.Tuition;
import com.myproject.payment.repository.TuitionRepository;

@Service
public class TuitionService {
    private final TuitionRepository tuitionRepository;

    public TuitionService(TuitionRepository tuitionRepository) {
        this.tuitionRepository = tuitionRepository;
    }

    //search for tuition by studentID
    public Tuition getTuitionByStudentId(String studentId) {
        return tuitionRepository.findByStudentId(studentId)
        .orElseThrow(() -> new RuntimeException("Tuition not found for student ID: " + studentId));
    }
}
