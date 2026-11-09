package com.myproject.tuition;

import com.myproject.tuition.exception.GlobalExceptionHandler;
import com.myproject.tuition.entity.Tuition;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.*;

class StudentUniquenessTest {
    @Test void studentConflictDoesNotExposeSql() {
        var handler = new GlobalExceptionHandler();
        var error = new DataIntegrityViolationException("secret SQL",
            new java.sql.SQLException("UQ_tuition_student_id private details", "23000", 2627));
        var response = handler.handleIntegrity(error);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("Student ID already exists", response.getBody().get("message"));
        assertEquals(500, handler.handleIntegrity(new DataIntegrityViolationException("secret SQL")).getStatusCode().value());
    }
    @Test void trimPreservesLeadingZeros() {
        var tuition = new Tuition();
        tuition.setStudentId(" 001 ");
        assertEquals("001", tuition.getStudentId());
    }
}
