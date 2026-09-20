package com.paymentsystem.userservice;

import com.paymentsystem.userservice.entity.TuitionFee;
import com.paymentsystem.userservice.entity.TuitionStatus;
import com.paymentsystem.userservice.entity.User;
import com.paymentsystem.userservice.repository.TuitionFeeRepository;
import com.paymentsystem.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seed dữ liệu mẫu khi khởi động — chỉ chạy khi DB rỗng (idempotent).
 *
 * Tài khoản: nguyenvana | tranthib | lequanghung — password: Student@123
 * Lưu ý: thay email của lequanghung bằng email thật để test gửi OTP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository       userRepository;
    private final TuitionFeeRepository tuitionFeeRepository;
    private final PasswordEncoder      passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("DataInitializer: DB da co du lieu — bo qua seed.");
            return;
        }

        log.info("DataInitializer: dang seed du lieu mau...");

        User sv1 = userRepository.save(User.builder()
                .studentId("SV001")
                .username("nguyenvana")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .fullName("Nguyen Van A")
                .email("nguyenvana@student.edu.vn")
                .build());

        User sv2 = userRepository.save(User.builder()
                .studentId("SV002")
                .username("tranthib")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .fullName("Tran Thi B")
                .email("tranthib@student.edu.vn")
                .build());

        // Thay email thật để test nhận OTP
        User sv3 = userRepository.save(User.builder()
                .studentId("SV003")
                .username("lequanghung")
                .passwordHash(passwordEncoder.encode("Student@123"))
                .fullName("Le Quang Hung")
                .email("your-real-email@gmail.com")
                .build());

        // Hoc phi SV001
        tuitionFeeRepository.save(TuitionFee.builder()
                .studentId(sv1.getStudentId())
                .semester("HK1-2025-2026")
                .amount(new BigDecimal("12500000.00"))
                .status(TuitionStatus.UNPAID)
                .description("Hoc phi HK1 nam hoc 2025-2026")
                .dueDate(LocalDate.of(2025, 10, 31))
                .build());

        tuitionFeeRepository.save(TuitionFee.builder()
                .studentId(sv1.getStudentId())
                .semester("HK2-2024-2025")
                .amount(new BigDecimal("12000000.00"))
                .status(TuitionStatus.PAID)
                .description("Hoc phi HK2 nam hoc 2024-2025")
                .dueDate(LocalDate.of(2025, 3, 31))
                .paymentId("PAY-2025-SV001-001")
                .build());

        // Hoc phi SV002
        tuitionFeeRepository.save(TuitionFee.builder()
                .studentId(sv2.getStudentId())
                .semester("HK1-2025-2026")
                .amount(new BigDecimal("11800000.00"))
                .status(TuitionStatus.UNPAID)
                .description("Hoc phi HK1 nam hoc 2025-2026")
                .dueDate(LocalDate.of(2025, 10, 31))
                .build());

        // Hoc phi SV003
        tuitionFeeRepository.save(TuitionFee.builder()
                .studentId(sv3.getStudentId())
                .semester("HK1-2025-2026")
                .amount(new BigDecimal("13200000.00"))
                .status(TuitionStatus.UNPAID)
                .description("Hoc phi HK1 nam hoc 2025-2026")
                .dueDate(LocalDate.of(2025, 10, 31))
                .build());

        log.info("DataInitializer: seed thanh cong — 3 users, 4 hoc phi.");
    }
}
