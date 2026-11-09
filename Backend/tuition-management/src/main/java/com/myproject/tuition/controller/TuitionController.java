package com.myproject.tuition.controller;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.tuition.dto.TuitionResponse;
import com.myproject.tuition.service.TuitionService;

@RestController
@RequestMapping("/api/tuitions")
// API validation: Spring MVC validates path/query constraints before any tuition update.
public class TuitionController {

    @InitBinder("studentId")
    public void trimStudentId(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    private final TuitionService tuitionService;

    public TuitionController(TuitionService tuitionService) {
        this.tuitionService = tuitionService;
    }

    // Lấy thông tin học phí
    @GetMapping("/{studentId}")
    public TuitionResponse getTuitionByStudentId(
            @PathVariable @NotBlank(message = "Student ID is required")
            @jakarta.validation.constraints.Size(max = 20)
            @jakarta.validation.constraints.Pattern(regexp = "[0-9]*[1-9][0-9]*", message = "Student ID must contain digits and be positive")
            String studentId) {

        return tuitionService.getTuitionByStudentId(studentId);
    }

    // Claim học phí trước khi trừ tiền
    @PutMapping("/{studentId}/claim")
    public ResponseEntity<Void> claimTuition(
            @PathVariable @NotBlank(message = "Student ID is required")
            @jakarta.validation.constraints.Size(max = 20)
            @jakarta.validation.constraints.Pattern(regexp = "[0-9]*[1-9][0-9]*", message = "Student ID must contain digits and be positive")
            String studentId,
            @RequestParam @NotBlank(message = "Payment ID is required")
            // API validation: match the existing database string capacity.
            @jakarta.validation.constraints.Size(max = 255) String paymentId) {

        tuitionService.claimTuition(studentId, paymentId);
        return ResponseEntity.ok().build();
    }

    // Đánh dấu học phí đã thanh toán
    @PutMapping("/{studentId}/paid")
    public ResponseEntity<Void> markTuitionAsPaid(
            @PathVariable @NotBlank(message = "Student ID is required")
            @jakarta.validation.constraints.Size(max = 20)
            @jakarta.validation.constraints.Pattern(regexp = "[0-9]*[1-9][0-9]*", message = "Student ID must contain digits and be positive")
            String studentId,
            @RequestParam  @NotBlank(message = "Payment ID is required")
            // API validation: match the existing database string capacity.
            @jakarta.validation.constraints.Size(max = 255) String paymentId) {

        tuitionService.markTuitionAsPaid(studentId, paymentId);
        return ResponseEntity.ok().build();
    }

    // Giải phóng claim khi xác nhận giao dịch chưa trừ tiền
    @PutMapping("/{studentId}/release")
    public ResponseEntity<Void> releaseTuition(
            @PathVariable @NotBlank(message = "Student ID is required")
            @jakarta.validation.constraints.Size(max = 20)
            @jakarta.validation.constraints.Pattern(regexp = "[0-9]*[1-9][0-9]*", message = "Student ID must contain digits and be positive")
            String studentId,
            @RequestParam @NotBlank(message = "Payment ID is required")
            // API validation: match the existing database string capacity.
            @jakarta.validation.constraints.Size(max = 255) String paymentId) {

        tuitionService.releaseTuition(studentId, paymentId);
        return ResponseEntity.ok().build();
    }
}
