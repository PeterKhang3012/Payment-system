package com.myproject.payment;

import com.myproject.payment.client.*;
import com.myproject.payment.dto.*;
import com.myproject.payment.dto.OtpDtos.*;
import com.myproject.payment.service.*;
import com.myproject.payment.exception.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StringStudentPaymentFlowTest {
    final PaymentTransactionService transactions = mock(PaymentTransactionService.class);
    final TuitionClient tuition = mock(TuitionClient.class);
    final BalanceClient balance = mock(BalanceClient.class);
    final OtpClient otp = mock(OtpClient.class);
    final PaymentAttemptService attempts = mock(PaymentAttemptService.class);
    final EmailClient email = mock(EmailClient.class);
    final PaymentService service = new PaymentService(transactions, tuition, balance, otp, attempts, email);
    final BigDecimal amount = new BigDecimal("5000000");

    @Test void requestOtpPreservesStringIdAndBalanceCheck() {
        when(tuition.getTuitionByStudentId("001", "Bearer test"))
            .thenReturn(new TuitionResponse(1L, "001", "Student", "UNPAID", amount));
        when(balance.getBalance("Bearer test")).thenReturn(new BalanceResponse(amount));
        service.requestOtp("user", "001", true, "Bearer test", "student@example.com");
        verify(attempts).saveAttempt(anyString(), eq("user"), eq("001"), eq(amount));
        verify(otp).generateOtp(argThat(request -> request.email().equals("student@example.com")));
        verify(balance, never()).deductBalance(anyString(), any(), anyString());
    }
    @Test void successfulPaymentPreservesClaimDeductionAndCompletionOrder() {
        when(attempts.getAttempt("payment")).thenReturn(new PaymentAttempt("payment", "user", "001", amount));
        when(attempts.acquireProcessingLock("user", "payment")).thenReturn(true);
        when(otp.verifyOtp(any())).thenReturn(new VerifyOtpResponse(true, "payment", "OK", null));
        when(tuition.getTuitionByStudentId("001", "Bearer test"))
            .thenReturn(new TuitionResponse(1L, "001", "Student", "UNPAID", amount));
        when(balance.getBalance("Bearer test")).thenReturn(new BalanceResponse(amount));
        when(balance.deductBalance("payment", amount, "Bearer test"))
            .thenReturn(new DeductBalanceResponse(true, "OK", BigDecimal.ZERO));
        service.verifyOtpAndPay("user", "Bearer test", "payment", "123456", "student@example.com", "Payer");
        var order = inOrder(otp, tuition, balance, transactions, email, attempts);
        order.verify(otp).verifyOtp(any());
        order.verify(tuition).getTuitionByStudentId("001", "Bearer test");
        order.verify(tuition).claimTuition("001", "payment", "Bearer test");
        order.verify(balance).getBalance("Bearer test");
        order.verify(balance).deductBalance("payment", amount, "Bearer test");
        order.verify(tuition).markTuitionAsPaid("001", "payment", "Bearer test");
        order.verify(transactions).createPaymentTransaction("user", 1L, amount);
        order.verify(email).sendPaymentSuccessEmail(argThat(event -> event.studentId().equals("001")));
        order.verify(attempts).deleteAttempt("payment");
        order.verify(attempts).releaseProcessingLock("user", "payment");
        verify(tuition, never()).releaseTuition(anyString(), anyString(), anyString());
    }
    @Test void rejectedOtpCannotClaimOrDeduct() {
        when(attempts.getAttempt("payment")).thenReturn(new PaymentAttempt("payment", "user", "001", amount));
        when(attempts.acquireProcessingLock("user", "payment")).thenReturn(true);
        when(otp.verifyOtp(any())).thenReturn(new VerifyOtpResponse(false, "payment", "Invalid OTP", 2));
        assertThrows(PaymentFailedException.class,
            () -> service.verifyOtpAndPay("user", "Bearer test", "payment", "000000", "student@example.com", "Payer"));
        verifyNoInteractions(tuition, balance, transactions, email);
        verify(attempts).releaseProcessingLock("user", "payment");
    }
}
