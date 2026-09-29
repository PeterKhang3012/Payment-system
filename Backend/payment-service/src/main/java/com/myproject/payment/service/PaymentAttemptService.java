package com.myproject.payment.service;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.myproject.payment.dto.PaymentAttempt;

@Service
public class PaymentAttemptService {

    private static final String KEY_PREFIX = "payment-attempt:";

    // Payment attempt tồn tại 5 phút
    private static final long ATTEMPT_EXPIRATION = 5;

    private final RedisTemplate<String, PaymentAttempt> redisTemplate;

    public PaymentAttemptService(
            RedisTemplate<String, PaymentAttempt> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    /**
     * Lưu một payment attempt vào Redis.
     */
    public void saveAttempt(
            String paymentId,
            String userId,
            Long studentId,
            BigDecimal amount) {

        PaymentAttempt attempt =
                new PaymentAttempt(
                        paymentId,
                        userId,
                        studentId,
                        amount);

        String key = KEY_PREFIX + paymentId;

        redisTemplate.opsForValue().set(
            key,
            attempt,
            Duration.ofMinutes(ATTEMPT_EXPIRATION)
        );
    }

    /**
     * Lấy payment attempt từ Redis.
     */
    public PaymentAttempt getAttempt(String paymentId) {

        String key = KEY_PREFIX + paymentId;

        PaymentAttempt attempt =
                redisTemplate.opsForValue().get(key);

        if (attempt == null) {
            throw new IllegalArgumentException(
                    "Payment attempt not found or expired.");
        }

        return attempt;
    }

    /**
     * Xóa payment attempt sau khi thanh toán thành công.
     */
    public void deleteAttempt(String paymentId) {

        String key = KEY_PREFIX + paymentId;

        redisTemplate.delete(key);
    }
}
