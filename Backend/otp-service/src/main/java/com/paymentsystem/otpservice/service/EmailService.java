package com.paymentsystem.otpservice.service;

import com.paymentsystem.otpservice.exception.GlobalException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

/** Gửi email OTP qua SMTP. Cấu hình tại application.yml (spring.mail.*). */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${otp.email.from-name:He Thong Thanh Toan Hoc Phi}")
    private String fromName;

    @Value("${otp.email.subject-prefix:[Thanh Toan Hoc Phi] Ma OTP xac thuc}")
    private String subjectPrefix;

    public void sendOtpEmail(String toEmail, String otpCode, String paymentId, int expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, fromName, "UTF-8"));
            helper.setTo(toEmail);
            helper.setSubject(subjectPrefix + " - " + paymentId);
            helper.setText(buildHtml(otpCode, paymentId, expiryMinutes), true);
            mailSender.send(message);
            log.info("sendOtpEmail: gui thanh cong -> {} paymentId={}", maskEmail(toEmail), paymentId);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("sendOtpEmail: gui that bai -> {} paymentId={} error={}", maskEmail(toEmail), paymentId, e.getMessage());
            throw new GlobalException.EmailSendException(toEmail, e);
        }
    }

    private String buildHtml(String otpCode, String paymentId, int expiryMinutes) {
        return "<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"UTF-8\"></head>"
             + "<body style=\"margin:0;padding:0;background:#f4f6f9;font-family:Arial,sans-serif;\">"
             + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding:40px 0;\">"
             + "<tr><td align=\"center\">"
             + "<table width=\"580\" cellpadding=\"0\" cellspacing=\"0\" "
             + "style=\"background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);\">"
             + "<tr><td style=\"background:linear-gradient(135deg,#1565C0,#0D47A1);padding:28px;text-align:center;\">"
             + "<h2 style=\"color:#fff;margin:0;font-size:20px;\">Xac Thuc Thanh Toan Hoc Phi</h2></td></tr>"
             + "<tr><td style=\"padding:36px 48px;\">"
             + "<p style=\"color:#555;font-size:14px;line-height:1.8;\">Xin chao,<br>"
             + "Ban da yeu cau xac thuc giao dich thanh toan hoc phi.</p>"
             + "<div style=\"background:#f0f4ff;border-left:4px solid #1565C0;border-radius:4px;"
             + "padding:10px 16px;margin:20px 0;font-size:13px;color:#333;\">"
             + "Ma giao dich: <strong style=\"font-family:monospace;color:#1565C0;\">"
             + escapeHtml(paymentId) + "</strong></div>"
             + "<div style=\"text-align:center;margin:32px 0;\">"
             + "<p style=\"font-size:12px;color:#999;text-transform:uppercase;letter-spacing:1px;margin-bottom:8px;\">Ma OTP cua ban</p>"
             + "<div style=\"display:inline-block;border:2px solid #1565C0;border-radius:10px;padding:18px 36px;\">"
             + "<span style=\"font-size:40px;font-weight:700;color:#1565C0;letter-spacing:12px;"
             + "font-family:'Courier New',monospace;\">" + otpCode + "</span></div>"
             + "<p style=\"font-size:13px;color:#e53935;margin-top:10px;\">"
             + "Hieu luc: <strong>" + expiryMinutes + " phut</strong></p></div>"
             + "<div style=\"background:#fff8e1;border:1px solid #ffe082;border-radius:8px;"
             + "padding:14px 16px;font-size:13px;color:#f57f17;line-height:1.7;\">"
             + "<strong>Luu y:</strong><br>"
             + "- OTP chi dung duoc <strong>1 lan</strong> trong vong <strong>" + expiryMinutes + " phut</strong>.<br>"
             + "- Sau <strong>5 lan nhap sai</strong> OTP se bi khoa.<br>"
             + "- Tuyet doi khong chia se ma nay voi bat ky ai.</div>"
             + "</td></tr>"
             + "<tr><td style=\"background:#f8f9fa;padding:16px;text-align:center;font-size:11px;color:#aaa;border-top:1px solid #eee;\">"
             + "Email tu dong — vui long khong phan hoi.<br>"
             + "He Thong Thanh Toan Hoc Phi | Kien Truc Huong Dich Vu (504070)"
             + "</td></tr></table></td></tr></table></body></html>";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;")
                   .replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Mask email trong log: nguyenvana@gmail.com → ng****@gmail.com */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) return "[unknown]";
        int at = email.indexOf('@');
        return (at <= 2) ? email : email.substring(0, 2) + "****" + email.substring(at);
    }
}
