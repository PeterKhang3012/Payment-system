package com.paymentsystem.otpservice.scheduler;

import com.paymentsystem.otpservice.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Xóa OTP hết hạn mỗi giờ. @EnableScheduling đặt tại OtpServiceApplication. */
@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(OtpCleanupScheduler.class);

    private final OtpRepository otpRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        log.info("cleanup: xoa OTP het han truoc {}", threshold);

        int deleted = otpRepository.deleteByExpiresAtBefore(threshold);
        if (deleted > 0) {
            log.info("cleanup: da xoa {} ban ghi", deleted);
        } else {
            log.debug("cleanup: khong co ban ghi can xoa");
        }
    }
}
