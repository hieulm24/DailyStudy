package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.statistics.ChartDataDto;
import com.englishlearning.dto.statistics.StreakHeatmapDto;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/chart")
    public ResponseEntity<ApiResponse<ChartDataDto>> getChartData(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "LAST_7_DAYS") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        ChartDataDto chartData = statisticsService.getChartData(userPrincipal.getId(), range, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.ok(chartData));
    }

    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<StreakHeatmapDto>> getStreakAndHeatmap(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        StreakHeatmapDto heatmap = statisticsService.getStreakAndHeatmap(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(heatmap));
    }
}
