package com.myproject.tuition.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.myproject.tuition.entity.Tuition;

import jakarta.persistence.LockModeType;

public interface TuitionRepository extends JpaRepository<Tuition, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tuition t WHERE t.studentId = :studentId")
    Optional<Tuition> findByStudentIdForUpdate(Long studentId);
}
