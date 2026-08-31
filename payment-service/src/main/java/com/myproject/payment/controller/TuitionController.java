package com.myproject.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.payment.entity.Tuition;
import com.myproject.payment.service.TuitionService;

@RestController
@RequestMapping("/api/tuitions")
public class TuitionController {
    private final TuitionService tuitionService;

    public TuitionController(TuitionService tuitionService) {
        this.tuitionService = tuitionService;
    }

    @GetMapping("/{studentId}")
    public Tuition getTuitionByStudentId(@PathVariable String studentId) {
        return tuitionService.getTuitionByStudentId(studentId);
    }
}
