package com.myproject.tuition.service;

import org.springframework.stereotype.Service;

import com.myproject.tuition.entity.Tuition;
import com.myproject.tuition.repository.TuitionRepository;

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
