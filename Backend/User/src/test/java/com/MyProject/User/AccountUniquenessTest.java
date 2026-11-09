package com.MyProject.User;

import com.MyProject.User.entity.User;
import com.MyProject.User.exception.GlobalException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

class AccountUniquenessTest {
    @Test void duplicateFieldsHaveSafeSeparateMessages() {
        var handler = new GlobalException.Handler();
        for (String[] pair : new String[][]{{"UQ_users_email", "Email"}}) {
            var error = new DataIntegrityViolationException("private SQL", new java.sql.SQLException(pair[0], "23000", 2627));
            var response = handler.handleIntegrity(error, new MockHttpServletRequest());
            assertEquals(409, response.getStatusCode().value());
            assertEquals(pair[1] + " already exists", response.getBody().message());
        }
        assertEquals(500, handler.handleIntegrity(new DataIntegrityViolationException("secret"),
            new MockHttpServletRequest()).getStatusCode().value());
    }
    @Test void trimmingPreservesEmailCase() {
        var user = new User();
        user.setEmail("  Student@Example.com  ");
        assertEquals("Student@Example.com", user.getEmail());
    }
}
