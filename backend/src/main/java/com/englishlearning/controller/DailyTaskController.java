package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.common.PageResponse;
import com.englishlearning.dto.task.*;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.DailyTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class DailyTaskController {

    private final DailyTaskService dailyTaskService;

    @PostMapping
    public ResponseEntity<ApiResponse<DailyTaskResponse>> createTask(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DailyTaskRequest request) {
        DailyTaskResponse response = dailyTaskService.createTask(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm công việc thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DailyTaskResponse>>> getTasks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @ModelAttribute DailyTaskFilterRequest filter) {
        PageResponse<DailyTaskResponse> response = dailyTaskService.getTasks(userPrincipal.getId(), filter);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/by-date")
    public ResponseEntity<ApiResponse<List<DailyTaskResponse>>> getTasksByDate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailyTaskResponse> response = dailyTaskService.getTasksByDate(userPrincipal.getId(), date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyTaskResponse>> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DailyTaskResponse response = dailyTaskService.getTaskById(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DailyTaskResponse>> updateTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DailyTaskRequest request) {
        DailyTaskResponse response = dailyTaskService.updateTask(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật công việc thành công", response));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<DailyTaskResponse>> toggleTaskCompletion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DailyTaskResponse response = dailyTaskService.toggleTaskCompletion(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DailyTaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DailyTaskResponse response = dailyTaskService.updateTaskStatus(id, userPrincipal.getId(), status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderTasks(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody List<DailyTaskReorderItem> items) {
        dailyTaskService.reorderTasks(userPrincipal.getId(), date, items);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thứ tự thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        dailyTaskService.deleteTask(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa công việc thành công", null));
    }

    @GetMapping("/daily-summaries")
    public ResponseEntity<ApiResponse<PageResponse<DailyTaskSummaryResponse>>> getDailySummaries(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size) {
        PageResponse<DailyTaskSummaryResponse> response = dailyTaskService.getDailySummaries(userPrincipal.getId(), fromDate, toDate, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DailyTaskStatsResponse>> getTaskStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DailyTaskStatsResponse response = dailyTaskService.getTaskStats(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
