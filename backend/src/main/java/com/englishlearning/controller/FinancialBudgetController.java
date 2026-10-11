package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.financial.BudgetDtos.*;
import com.englishlearning.entity.financial.FinancialBudget;
import com.englishlearning.entity.financial.FinancialMonthlyPlan;
import com.englishlearning.service.financial.FinancialBudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financial/budget-plan")
@RequiredArgsConstructor
public class FinancialBudgetController {

    private final FinancialBudgetService budgetService;

    @GetMapping
    public ResponseEntity<ApiResponse<MonthlyBudgetPlanResponse>> getBudgetPlan(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
        MonthlyBudgetPlanResponse response = budgetService.getMonthlyBudgetPlan(month, year);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/plan")
    public ResponseEntity<ApiResponse<FinancialMonthlyPlan>> saveMonthlyPlan(
            @Valid @RequestBody SaveMonthlyPlanRequest request) {
        FinancialMonthlyPlan plan = budgetService.saveMonthlyPlan(request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thu nhập dự kiến thành công", plan));
    }

    @DeleteMapping("/plan")
    public ResponseEntity<ApiResponse<Void>> deleteMonthlyPlan(
            @RequestParam("month") Integer month,
            @RequestParam("year") Integer year) {
        budgetService.deleteMonthlyPlan(month, year);
        return ResponseEntity.ok(ApiResponse.ok("Xóa thu nhập dự kiến thành công", null));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<FinancialBudget>> saveBudgetItem(
            @Valid @RequestBody SaveBudgetItemRequest request) {
        FinancialBudget budget = budgetService.saveBudgetItem(request);
        return ResponseEntity.ok(ApiResponse.ok("Lưu khoản dự toán chi tiêu thành công", budget));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudgetItem(@PathVariable("id") Long id) {
        budgetService.deleteBudgetItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa khoản dự toán chi tiêu thành công", null));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<MonthlyBudgetPlanResponse>> batchSaveBudget(
            @Valid @RequestBody BatchSaveBudgetRequest request) {
        MonthlyBudgetPlanResponse response = budgetService.batchSaveBudget(request);
        return ResponseEntity.ok(ApiResponse.ok("Lưu toàn bộ kế hoạch ngân sách thành công", response));
    }
}
