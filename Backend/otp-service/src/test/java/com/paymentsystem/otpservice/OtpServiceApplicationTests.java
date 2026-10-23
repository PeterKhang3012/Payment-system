package com.paymentsystem.otpservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

/**
 * Kiểm tra Spring ApplicationContext khởi động không lỗi.
 *
 * Profile "test" → dùng H2 in-memory thay MySQL.
 * @MockBean JavaMailSender → không cần SMTP server thật khi test.
 *
 * Chạy: mvn test
 */
@SpringBootTest
@ActiveProfiles("test")
class OtpServiceApplicationTests {

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void contextLoads() {
        // ApplicationContext (OtpService, EmailService, InternalTokenFilter,
        // OtpCleanupScheduler) phải khởi động thành công với profile test
    }
}
