package com.paymentsystem.otpservice;

import com.paymentsystem.otpservice.dto.OtpDtos.*;
import com.paymentsystem.otpservice.entity.Otp;
import com.paymentsystem.otpservice.exception.GlobalException;
import com.paymentsystem.otpservice.repository.OtpRepository;
import com.paymentsystem.otpservice.service.EmailService;
import com.paymentsystem.otpservice.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class OtpLifecycleTest {
    OtpRepository repository;
    EmailService email;
    OtpService service;
    @BeforeEach void setup() {
        repository = mock(OtpRepository.class);
        email = mock(EmailService.class);
        service = new OtpService(repository, email);
        ReflectionTestUtils.setField(service, "expiryMinutes", 5);
        ReflectionTestUtils.setField(service, "maxAttempts", 5);
    }
    @Test void newOtpHasFiveMinuteLifetime() {
        when(repository.findByPaymentId("test")).thenReturn(Optional.empty());
        var response = service.generateOtp(new GenerateOtpRequest("test", "test@example.invalid"));
        assertEquals(300, response.expiresInSeconds());
        assertTrue(LocalDateTime.parse(response.expiresAt()).isAfter(LocalDateTime.now().plusMinutes(4)));
        verify(repository).save(any(Otp.class));
    }
    @Test void validOtpIsAccepted() {
        when(repository.verifyAndMarkUsed(eq("test"), eq("123456"), any(), eq(5))).thenReturn(1);
        assertTrue(service.verifyOtp(new VerifyOtpRequest("test", "123456")).success());
    }
    @Test void expiredOtpIsRejected() {
        when(repository.findByPaymentId("test")).thenReturn(Optional.of(Otp.builder().expiresAt(LocalDateTime.now().minusMinutes(1)).build()));
        assertThrows(GlobalException.OtpExpiredException.class, () -> service.verifyOtp(new VerifyOtpRequest("test", "123456")));
    }
    @Test void wrongOtpIsRejectedAndCountsAttempt() {
        when(repository.findByPaymentId("test")).thenReturn(Optional.of(Otp.builder().expiresAt(LocalDateTime.now().plusMinutes(5)).build()));
        var response = service.verifyOtp(new VerifyOtpRequest("test", "000000"));
        assertFalse(response.success());
        assertEquals(4, response.remainingAttempts());
        verify(repository).incrementAttempts(eq("test"), any(), eq(5));
    }
    @Test void usedOtpCannotBeReused() {
        when(repository.findByPaymentId("test")).thenReturn(Optional.of(Otp.builder().used(true).expiresAt(LocalDateTime.now().plusMinutes(5)).build()));
        assertThrows(GlobalException.OtpAlreadyUsedException.class, () -> service.verifyOtp(new VerifyOtpRequest("test", "123456")));
    }
    @Test void regenerateUpdatesSameOtpAndResetsAttempts() {
        Otp previous = Otp.builder().used(true).attempts(5).expiresAt(LocalDateTime.now().minusMinutes(1)).build();
        when(repository.findByPaymentId("test")).thenReturn(Optional.of(previous));
        service.generateOtp(new GenerateOtpRequest("test", "test@example.invalid"));
        assertFalse(previous.isUsed());
        assertEquals(0, previous.getAttempts());
        assertTrue(previous.getExpiresAt().isAfter(LocalDateTime.now()));
        verify(repository).save(previous);
    }
}
