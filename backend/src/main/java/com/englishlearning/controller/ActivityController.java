package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.statistics.ActivityDto;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ActivityDto>>> getRecentActivities(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit) {
        List<ActivityDto> list = activityService.getRecentActivities(userPrincipal.getId(), limit);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<ActivityDto>>> getTodayActivities(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ActivityDto> list = activityService.getTodayActivities(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
