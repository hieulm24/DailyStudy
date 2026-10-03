package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.statistics.DashboardSummaryDto;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DashboardSummaryDto summary = statisticsService.getDashboardSummary(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
