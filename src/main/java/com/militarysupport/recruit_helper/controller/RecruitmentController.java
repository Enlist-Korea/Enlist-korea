package com.militarysupport.recruit_helper.controller;

import com.militarysupport.recruit_helper.domain.Recruitment;
import com.militarysupport.recruit_helper.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @GetMapping("/available")
    public ResponseEntity<List<Recruitment>> getAvailableRecruitments() {
        List<Recruitment> result = recruitmentService.getCurrentlyOpenRecruitments();
        return ResponseEntity.ok(result);
    }

}
