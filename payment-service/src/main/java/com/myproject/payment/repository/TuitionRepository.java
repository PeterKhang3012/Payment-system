package com.myproject.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.myproject.payment.entity.Tuition;

public interface TuitionRepository extends JpaRepository<Tuition, Long> {
    Optional<Tuition> findByStudentId(String studentId);
}
