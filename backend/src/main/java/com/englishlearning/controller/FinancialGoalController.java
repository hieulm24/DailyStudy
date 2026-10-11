package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.financial.GoalDtos.*;
import com.englishlearning.service.financial.FinancialGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financial/goals")
@RequiredArgsConstructor
public class FinancialGoalController {

    private final FinancialGoalService goalService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getAllGoals(
            @RequestParam(value = "status", required = false) String status) {
        List<GoalResponse> response = goalService.getAllGoals(status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(@PathVariable Long id) {
        GoalResponse response = goalService.getGoalById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm mục tiêu tài chính thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.updateGoal(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật mục tiêu thành công", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoalStatus(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        GoalResponse response = goalService.updateGoalStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái mục tiêu thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa mục tiêu thành công", null));
    }

    // ==========================================
    // CONTRIBUTIONS
    // ==========================================

    @GetMapping("/{id}/contributions")
    public ResponseEntity<ApiResponse<List<GoalContributionResponse>>> getContributions(@PathVariable Long id) {
        List<GoalContributionResponse> response = goalService.getContributions(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/contributions")
    public ResponseEntity<ApiResponse<GoalContributionResponse>> addContribution(
            @Valid @RequestBody GoalContributionRequest request) {
        GoalContributionResponse response = goalService.addContribution(request);
        String msg = "DEPOSIT".equalsIgnoreCase(request.getType()) ? "Tích lũy tiền vào mục tiêu thành công" : "Rút tiền khỏi mục tiêu thành công";
        return ResponseEntity.ok(ApiResponse.ok(msg, response));
    }

    @PutMapping("/contributions/{contributionId}")
    public ResponseEntity<ApiResponse<GoalContributionResponse>> updateContribution(
            @PathVariable Long contributionId,
            @Valid @RequestBody GoalContributionRequest request) {
        GoalContributionResponse response = goalService.updateContribution(contributionId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật khoản tích lũy thành công", response));
    }

    @DeleteMapping("/contributions/{contributionId}")
    public ResponseEntity<ApiResponse<Void>> deleteContribution(@PathVariable Long contributionId) {
        goalService.deleteContribution(contributionId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa khoản tích lũy thành công", null));
    }
}
