package com.myproject.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.myproject.payment.client.BalanceClient;
import com.myproject.payment.client.OtpClient;
import com.myproject.payment.client.TuitionClient;
import com.myproject.payment.dto.BalanceResponse;
import com.myproject.payment.dto.DeductBalanceResponse;
import com.myproject.payment.dto.OtpDtos.GenerateOtpRequest;
import com.myproject.payment.dto.OtpDtos.GenerateOtpResponse;
import com.myproject.payment.dto.OtpDtos.VerifyOtpRequest;
import com.myproject.payment.dto.OtpDtos.VerifyOtpResponse;
import com.myproject.payment.dto.PaymentAttempt;
import com.myproject.payment.dto.TuitionResponse;
import com.myproject.payment.exception.InsufficientBalanceException;
import com.myproject.payment.exception.PaymentFailedException;
import com.myproject.payment.exception.TermsNotAcceptedException;
import com.myproject.payment.exception.TuitionAlreadyPaidException;

@Service
public class PaymentService {

    private final PaymentTransactionService paymentTransactionService;
    private final TuitionClient tuitionClient;
    private final BalanceClient balanceClient;
    private final OtpClient otpClient;
    private final PaymentAttemptService paymentAttemptService;

    public PaymentService(
            PaymentTransactionService paymentTransactionService,
            TuitionClient tuitionClient,
            BalanceClient balanceClient,
            OtpClient otpClient,
            PaymentAttemptService paymentAttemptService) {

        this.paymentTransactionService = paymentTransactionService;
        this.tuitionClient = tuitionClient;
        this.balanceClient = balanceClient;
        this.otpClient = otpClient;
        this.paymentAttemptService = paymentAttemptService;
    }

    //yêu cầu otp
    public GenerateOtpResponse requestOtp(
            String userId,
            Long studentId,
            boolean acceptedTerms,
            String authorization,
            String email) {

        // 1. Kiểm tra người dùng đã đồng ý điều khoản chưa
        if (!acceptedTerms) {
            throw new TermsNotAcceptedException(
                    "Terms and conditions must be accepted.");
        }

        // 2. Lấy thông tin học phí
        TuitionResponse tuitionResponse =
                tuitionClient.getTuitionByStudentId(
                        studentId,
                        authorization);

        // 3. Kiểm tra học phí đã thanh toán chưa
        if (tuitionResponse.getStatus().equalsIgnoreCase("PAID")) {
            throw new TuitionAlreadyPaidException(
                    "Tuition has already been paid.");
        }

        if (!"UNPAID".equalsIgnoreCase(tuitionResponse.getStatus())) {
            throw new PaymentFailedException(
                "Tuition is currently being processed."
            );
        }       

        // 4. Lấy số dư hiện tại
        BalanceResponse balanceResponse =
                balanceClient.getBalance(authorization);

        // 5. Kiểm tra số dư có đủ không
        if (balanceResponse.getBalance()
                .compareTo(tuitionResponse.getAmount()) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance to pay tuition.");
        }


        // 6. Tạo paymentId để OTP Service xác định
        //    OTP này thuộc giao dịch thanh toán nào
        String paymentId = UUID.randomUUID().toString();


        //Tạo 1 payment attempt để lưu trữ
        paymentAttemptService.saveAttempt(
                paymentId,
                userId,
                studentId,
                tuitionResponse.getAmount()
        );

        // 7. Tạo request gửi sang OTP Service
        GenerateOtpRequest request =
                new GenerateOtpRequest(
                        paymentId,
                        email);

        // 8. Gửi request sang OTP Service
        return otpClient.generateOtp(request);
    }

    //thanh toán
    public void verifyOtpAndPay(
        String userId, 
        String authorization, 
        String paymentId,
        String code) {

        PaymentAttempt attempt = paymentAttemptService.getAttempt(paymentId);

        if (!attempt.getUserId().equals(userId)) {
        throw new PaymentFailedException(
                "Payment attempt does not belong to this user.");
        }

        boolean acquired = paymentAttemptService.acquireProcessingLock(userId, paymentId);

        if (!acquired) {
                throw new PaymentFailedException(
                        "Another payment is currently being processed.");
        }

        boolean tuitionClaimed = false;
       
        // Chỉ true khi biết chắc tiền chưa bị trừ,
        // nên có thể giải phóng claim an toàn.
        boolean safeToReleaseClaim = false;
        
        try{  
                Long studentId = attempt.getStudentId();
                // 1. Tạo request verify OTP
                VerifyOtpRequest request =
                        new VerifyOtpRequest(
                                paymentId,
                                code);

                // 2. Gửi sang OTP Service để xác thực
                VerifyOtpResponse otpResponse =
                        otpClient.verifyOtp(request);

                // 3. OTP không hợp lệ
                if (!otpResponse.success()) {
                throw new PaymentFailedException(
                        otpResponse.message());
                }

                //otp hợp lệ thì bắt đầu thanh toán

                // 4. Lấy lại thông tin học phí
                TuitionResponse tuitionResponse =
                        tuitionClient.getTuitionByStudentId(
                                studentId,
                                authorization);

                // 5. Kiểm tra lại học phí
                if (tuitionResponse.getStatus().equalsIgnoreCase("PAID")) {
                throw new TuitionAlreadyPaidException(
                        "Tuition has already been paid.");
                }

                if (!"UNPAID".equalsIgnoreCase(
                        tuitionResponse.getStatus())) {
                        throw new PaymentFailedException(
                        "Tuition is currently being processed."
                        );
                }
                // 3. CLAIM HỌC PHÍ TRƯỚC KHI TRỪ TIỀN
                tuitionClient.claimTuition(
                        studentId, paymentId, authorization
                );

                tuitionClaimed = true;
                safeToReleaseClaim = true;     
                // 6. Lấy lại số dư
                BalanceResponse balanceResponse =
                        balanceClient.getBalance(authorization);

                // 7. Kiểm tra lại số dư
                if (balanceResponse.getBalance()
                        .compareTo(tuitionResponse.getAmount()) < 0) {

                throw new InsufficientBalanceException(
                        "Insufficient balance to pay tuition.");
                }

                // Bắt đầu gọi API trừ tiền.
                // Nếu request lỗi hoặc timeout, không thể mặc định
                // rằng tiền chưa bị trừ.
                safeToReleaseClaim = false;

                // 8. Trừ tiền
                DeductBalanceResponse deductBalance =
                        balanceClient.deductBalance(
                                paymentId,
                                tuitionResponse.getAmount(),
                                authorization);

                // 9. Kiểm tra kết quả trừ tiền
                if (!deductBalance.isSuccess()) {
                throw new PaymentFailedException(
                        "Payment failed.");
                }

                // 10. Cập nhật trạng thái học phí thành PAID
                tuitionClient.markTuitionAsPaid(
                        studentId,
                        paymentId,
                        authorization);

                // 11. Lưu lịch sử giao dịch
                paymentTransactionService.createPaymentTransaction(
                        userId,
                        tuitionResponse.getTuitionId(),
                        tuitionResponse.getAmount());

                // 13. Thanh toán thành công -> xóa attempt 
                paymentAttemptService.deleteAttempt(paymentId);
        } catch (RuntimeException ex) {

            // Chỉ release nếu đã claim và biết chắc chưa trừ tiền.
            // Nếu không rõ kết quả trừ tiền, giữ claim để đối soát.
            if (tuitionClaimed && safeToReleaseClaim) {
                try {
                    tuitionClient.releaseTuition(
                        attempt.getStudentId(),
                        paymentId,
                        authorization
                    );
                } catch (RuntimeException releaseException) {
                    // Giữ lại lỗi release để phục vụ chẩn đoán.
                    ex.addSuppressed(releaseException);
                }
            }

            throw ex;

        }finally{
                paymentAttemptService.releaseProcessingLock(userId, paymentId);
        }
    }
}