
package com.myproject.tuition.controller;

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
public class TuitionController {

    private final TuitionService tuitionService;

    public TuitionController(TuitionService tuitionService) {
        this.tuitionService = tuitionService;
    }

    // Lấy thông tin học phí
    @GetMapping("/{studentId}")
    public TuitionResponse getTuitionByStudentId(
            @PathVariable Long studentId) {

        return tuitionService.getTuitionByStudentId(studentId);
    }

    // Claim học phí trước khi trừ tiền
    @PutMapping("/{studentId}/claim")
    public ResponseEntity<Void> claimTuition(
            @PathVariable Long studentId,
            @RequestParam String paymentId) {

        tuitionService.claimTuition(studentId, paymentId);
        return ResponseEntity.ok().build();
    }

    // Đánh dấu học phí đã thanh toán
    @PutMapping("/{studentId}/paid")
    public ResponseEntity<Void> markTuitionAsPaid(
            @PathVariable Long studentId,
            @RequestParam String paymentId) {

        tuitionService.markTuitionAsPaid(studentId, paymentId);
        return ResponseEntity.ok().build();
    }

    // Giải phóng claim khi xác nhận giao dịch chưa trừ tiền
    @PutMapping("/{studentId}/release")
    public ResponseEntity<Void> releaseTuition(
            @PathVariable Long studentId,
            @RequestParam String paymentId) {

        tuitionService.releaseTuition(studentId, paymentId);
        return ResponseEntity.ok().build();
    }
}
