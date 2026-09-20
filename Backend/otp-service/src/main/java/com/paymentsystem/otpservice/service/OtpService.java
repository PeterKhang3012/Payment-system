package com.paymentsystem.otpservice.service;

import com.paymentsystem.otpservice.dto.OtpDtos.GenerateOtpRequest;
import com.paymentsystem.otpservice.dto.OtpDtos.GenerateOtpResponse;
import com.paymentsystem.otpservice.dto.OtpDtos.VerifyOtpRequest;
import com.paymentsystem.otpservice.dto.OtpDtos.VerifyOtpResponse;
import com.paymentsystem.otpservice.entity.Otp;
import com.paymentsystem.otpservice.exception.GlobalException;
import com.paymentsystem.otpservice.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Sinh OTP và verify OTP.
 *
 * Luồng verify (atomic):
 * 1. UPDATE ... WHERE code=? AND used=false AND expires_at>now AND attempts<max
 * 2. rowsAffected=1 → thành công
 * 3. rowsAffected=0 → fetch record, phân loại lỗi: notFound/used/expired/maxAttempts/wrongCode
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpRepository otpRepository;
    private final EmailService  emailService;

    @Value("${otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Transactional
    public GenerateOtpResponse generateOtp(GenerateOtpRequest request) {
        String paymentId = request.paymentId();
        String email     = request.email();

        log.info("generateOtp: paymentId={} email={}", paymentId, EmailService.maskEmail(email));

        LocalDateTime now       = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(expiryMinutes);
        String        code      = generateCode();

        Optional<Otp> existing = otpRepository.findByPaymentId(paymentId);
        if (existing.isPresent()) {
            // Regenerate: cập nhật bản ghi cũ, tránh vi phạm UNIQUE constraint
            Otp otp = existing.get();
            otp.setCode(code);
            otp.setEmail(email);
            otp.setExpiresAt(expiresAt);
            otp.setUsed(false);
            otp.setAttempts(0);
            otpRepository.save(otp);
        } else {
            otpRepository.save(Otp.builder()
                    .paymentId(paymentId)
                    .code(code)
                    .email(email)
                    .expiresAt(expiresAt)
                    .build());
        }

        // Gửi email sau khi lưu DB; nếu fail thì transaction rollback
        emailService.sendOtpEmail(email, code, paymentId, expiryMinutes);
        log.info("generateOtp: hoan tat paymentId={}", paymentId);

        return new GenerateOtpResponse(
                paymentId,
                EmailService.maskEmail(email),
                expiresAt.toString(),
                (long) expiryMinutes * 60,
                "OTP da duoc gui den email cua ban. Hieu luc " + expiryMinutes + " phut."
        );
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String        paymentId = request.paymentId();
        String        inputCode = request.code();
        LocalDateTime now       = LocalDateTime.now();

        log.info("verifyOtp: paymentId={}", paymentId);

        // Bước 1: atomic UPDATE
        int rowsAffected = otpRepository.verifyAndMarkUsed(paymentId, inputCode, now, maxAttempts);

        if (rowsAffected == 1) {
            log.info("verifyOtp: xac thuc thanh cong paymentId={}", paymentId);
            return new VerifyOtpResponse(true, paymentId,
                    "Xac thuc OTP thanh cong. Giao dich co the tiep tuc.", null);
        }

        // Bước 2: phân tích nguyên nhân thất bại
        Otp otp = otpRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> {
                    log.warn("verifyOtp: khong tim thay OTP paymentId={}", paymentId);
                    return new GlobalException.OtpNotFoundException(paymentId);
                });

        if (otp.isUsed()) {
            log.warn("verifyOtp: OTP da dung paymentId={}", paymentId);
            throw new GlobalException.OtpAlreadyUsedException(paymentId);
        }

        if (otp.getExpiresAt().isBefore(now)) {
            log.warn("verifyOtp: OTP het han paymentId={}", paymentId);
            throw new GlobalException.OtpExpiredException(paymentId);
        }

        if (otp.getAttempts() >= maxAttempts) {
            log.warn("verifyOtp: OTP bi khoa paymentId={}", paymentId);
            throw new GlobalException.OtpMaxAttemptsExceededException(paymentId, maxAttempts);
        }

        // Sai code — tăng đếm sai atomically
        otpRepository.incrementAttempts(paymentId, now, maxAttempts);

        int attemptsAfter = otp.getAttempts() + 1;
        int remaining     = maxAttempts - attemptsAfter;

        if (remaining <= 0) {
            log.warn("verifyOtp: OTP bi khoa sau lan nay paymentId={}", paymentId);
            throw new GlobalException.OtpMaxAttemptsExceededException(paymentId, maxAttempts);
        }

        log.warn("verifyOtp: sai code paymentId={} remaining={}", paymentId, remaining);
        return new VerifyOtpResponse(false, paymentId,
                "Ma OTP khong dung. Con " + remaining + " lan thu.", remaining);
    }

    /** Sinh mã 6 chữ số bằng SecureRandom, format %06d đảm bảo đủ 6 chữ số. */
    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
