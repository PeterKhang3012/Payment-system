package com.myproject.emailnotification.service;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.myproject.emailnotification.dto.PaymentSuccessfulEvent;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {
    private final JavaMailSender mailSender;
    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPaymentSuccessEmail(
            PaymentSuccessfulEvent event) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                StandardCharsets.UTF_8.name()
        );

        helper.setFrom(fromEmail);
        helper.setTo(event.recipientEmail());

        helper.setSubject(
                "KDNPay - Xac nhan thanh toan hoc phi thanh cong"
        );

        String html = loadEmailTemplate(event);

        helper.setText(html, true);

        mailSender.send(message);
    }

    private String loadEmailTemplate(
            PaymentSuccessfulEvent event) throws Exception {

        ClassPathResource resource =
                new ClassPathResource("templates/payment-success.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        NumberFormat formatter =
                NumberFormat.getInstance(Locale.of("vi", "VN"));

        html = html.replace(
                "{{fullName}}",
                escapeHtml(event.fullName())
        );

        html = html.replace(
                "{{studentName}}",
                escapeHtml(event.studentName())
        );

        html = html.replace(
                "{{studentId}}",
                escapeHtml(event.studentId())
        );

        html = html.replace(
                "{{paymentId}}",
                escapeHtml(event.paymentId())
        );

        html = html.replace(
                "{{amount}}",
                formatter.format(event.amount())
        );

        html = html.replace(
                "{{paidAt}}",
                escapeHtml(event.paidAt().toString())
        );

        return html;
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
