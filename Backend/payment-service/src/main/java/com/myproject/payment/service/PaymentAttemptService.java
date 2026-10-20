package com.myproject.payment.service;

import java.math.BigDecimal;
import java.time.Duration;

import java.util.Collections;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.myproject.payment.dto.PaymentAttempt;
import com.myproject.payment.exception.PaymentFailedException;

@Service
public class PaymentAttemptService {

    private static final String KEY_PREFIX = "payment-attempt:";
    private static final String ACTIVE_PREFIX = "payment-active:";
    private static final String PROCESSING_PREFIX = "payment-processing:";
    // Payment attempt tồn tại 5 phút
    private static final long ATTEMPT_EXPIRATION = 5;
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('GET', KEYS[1]) == ARGV[1] " +
                    "then return redis.call('DEL', KEYS[1]) " +
                    "else return 0 end",
                    Long.class
            );

    private final RedisTemplate<String, PaymentAttempt> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    
    public PaymentAttemptService(
            RedisTemplate<String, PaymentAttempt> redisTemplate,
            StringRedisTemplate stringRedisTemplate) {

        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * Lưu một payment attempt vào Redis.
     */
    public void saveAttempt(
            String paymentId,
            String userId,
            Long studentId,
            BigDecimal amount) {

        String activeKey = ACTIVE_PREFIX + userId;
        String attemptKey = KEY_PREFIX + paymentId;
        
        // Thử đặt khóa cho userId để đảm bảo chỉ có một payment attempt đang hoạt động
        Boolean reserved = stringRedisTemplate.opsForValue()
                .setIfAbsent(
                        activeKey,
                        paymentId,
                        Duration.ofMinutes(ATTEMPT_EXPIRATION)
                );
        // Nếu không đặt được khóa, nghĩa là đã có một payment attempt đang hoạt động
        if (!Boolean.TRUE.equals(reserved)) {
            throw new PaymentFailedException(
                    "You already have an active payment attempt."
            );
        }

        try {
            PaymentAttempt attempt = new PaymentAttempt(
                    paymentId,
                    userId,
                    studentId,
                    amount
            );

            redisTemplate.opsForValue().set(
                    attemptKey,
                    attempt,
                    Duration.ofMinutes(ATTEMPT_EXPIRATION)
            );
        } catch (RuntimeException ex) {
            releaseReservation(userId, paymentId);
            throw ex;
        }
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
        PaymentAttempt attempt = getAttempt(paymentId);

        redisTemplate.delete(key);
        releaseReservation(attempt.getUserId(), paymentId);
    }

    private void releaseReservation(
        String userId,
        String paymentId) {

        stringRedisTemplate.execute(
                RELEASE_SCRIPT,
                Collections.singletonList(ACTIVE_PREFIX + userId),
                paymentId
        );
    }

    public boolean acquireProcessingLock(String userId, String paymentId) {
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(
            PROCESSING_PREFIX + userId,
            paymentId,
            Duration.ofMinutes(5)
        );

        return Boolean.TRUE.equals(acquired);
    }

    public void releaseProcessingLock(String userId, String paymentId) {
        stringRedisTemplate.execute(
            RELEASE_SCRIPT,
            Collections.singletonList(PROCESSING_PREFIX + userId),
            paymentId
        );
    }
}
