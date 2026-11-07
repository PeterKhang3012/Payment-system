package com.myproject.emailnotification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.myproject.emailnotification.dto.PaymentSuccessfulEvent;
import com.myproject.emailnotification.service.EmailNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EmailGreetingTest {
    @Test
    void greetingUsesPayerNameAndEscapesHtml() {
        var event = new PaymentSuccessfulEvent("payment-1", "user-1", "payer@example.com",
                "Nguyen Van B <&>", "Nguyen Van A", "student-1",
                new BigDecimal("100000"), LocalDateTime.of(2026, 11, 1, 12, 0));
        String html = ReflectionTestUtils.invokeMethod(
                new EmailNotificationService(null), "loadEmailTemplate", event);

        assertNotNull(html);
        assertTrue(html.contains("<strong>Nguyen Van B &lt;&amp;&gt;</strong>"));
        assertFalse(html.contains("<strong>Nguyen Van A</strong>"));
        assertTrue(html.contains("student-1"));
        assertEquals("Nguyen Van A", event.studentName());
        assertFalse(html.contains("{{fullName}}"));
    }
}
