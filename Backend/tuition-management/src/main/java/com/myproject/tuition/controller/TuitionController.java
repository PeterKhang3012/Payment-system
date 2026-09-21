package com.myproject.tuition.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/{studentId}")
    public TuitionResponse getTuitionByStudentId(@PathVariable Long studentId) {
        return tuitionService.getTuitionByStudentId(studentId);
    }

    @PutMapping("/{studentId}/paid")
    public void markTuitionAsPaid(@PathVariable Long studentId) {
        tuitionService.markTuitionAsPaid(studentId);
    }
}
