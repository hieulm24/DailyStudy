package com.englishlearning.controller;

import com.englishlearning.common.ApiResponse;
import com.englishlearning.dto.nutrition.*;
import com.englishlearning.security.UserPrincipal;
import com.englishlearning.service.NutritionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;
    private final com.englishlearning.service.UsdaFoodDataService usdaFoodDataService;
    private final com.englishlearning.service.CalorieCalculationService calorieCalculationService;

    @GetMapping("/usda/search")
    public ResponseEntity<ApiResponse<List<UsdaFoodDto>>> searchUsdaFoods(
            @RequestParam("query") String query,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        List<UsdaFoodDto> results = usdaFoodDataService.searchFoods(query, pageSize);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @PostMapping("/usda/import/{fdcId}")
    public ResponseEntity<ApiResponse<NutritionFoodSearchDto>> importUsdaFood(@PathVariable Long fdcId) {
        var savedFood = usdaFoodDataService.importUsdaFoodToSystem(fdcId);
        NutritionFoodSearchDto detail = nutritionService.getFoodDetail(savedFood.getId());
        return ResponseEntity.ok(ApiResponse.ok("Đã nhập thực phẩm từ USDA thành công", detail));
    }

    @GetMapping("/foods/search")
    public ResponseEntity<ApiResponse<List<NutritionFoodSearchDto>>> searchFoods(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Long userId = userPrincipal != null ? userPrincipal.getId() : null;
        List<NutritionFoodSearchDto> results = nutritionService.searchFoods(userId, keyword);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/foods/{id}")
    public ResponseEntity<ApiResponse<NutritionFoodSearchDto>> getFoodDetail(@PathVariable Long id) {
        NutritionFoodSearchDto detail = nutritionService.getFoodDetail(id);
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<NutritionDailySummaryResponse>> getDailySummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        NutritionDailySummaryResponse response = nutritionService.getDailySummary(userPrincipal.getId(), date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/daily")
    public ResponseEntity<ApiResponse<NutritionDailyLogResponse>> addDailyLog(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NutritionDailyLogRequest request) {
        NutritionDailyLogResponse response = nutritionService.addDailyLog(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đã thêm thực phẩm vào nhật ký", response));
    }

    @PutMapping("/daily/{id}")
    public ResponseEntity<ApiResponse<NutritionDailyLogResponse>> updateDailyLog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NutritionDailyLogRequest request) {
        NutritionDailyLogResponse response = nutritionService.updateDailyLog(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thực phẩm thành công", response));
    }

    @DeleteMapping("/daily/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDailyLog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        nutritionService.deleteDailyLog(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa thực phẩm khỏi nhật ký", null));
    }

    @PostMapping("/daily/copy")
    public ResponseEntity<ApiResponse<NutritionDailySummaryResponse>> copyDayLogs(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        NutritionDailySummaryResponse response = nutritionService.copyDayLogs(userPrincipal.getId(), fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.ok("Sao chép thực phẩm thành công", response));
    }

    @GetMapping("/recent-foods")
    public ResponseEntity<ApiResponse<List<NutritionRecentFoodDto>>> getRecentFoods(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<NutritionRecentFoodDto> list = nutritionService.getRecentFoods(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/user-foods")
    public ResponseEntity<ApiResponse<List<NutritionUserFoodDto>>> getUserFoods(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<NutritionUserFoodDto> list = nutritionService.getUserFoods(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/user-foods")
    public ResponseEntity<ApiResponse<NutritionUserFoodDto>> createUserFood(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NutritionUserFoodRequest request) {
        NutritionUserFoodDto dto = nutritionService.createUserFood(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tạo thực phẩm cá nhân thành công", dto));
    }

    @DeleteMapping("/user-foods/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserFood(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        nutritionService.deleteUserFood(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Xóa thực phẩm cá nhân thành công", null));
    }

    // ==========================================
    // PHYSICAL ACTIVITIES & CALORIE DEFICIT APIS
    // ==========================================

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<NutritionActivityDto>>> getActiveActivities() {
        List<NutritionActivityDto> list = calorieCalculationService.getActiveActivities();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/activity-logs")
    public ResponseEntity<ApiResponse<List<NutritionActivityLogResponse>>> getActivityLogs(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<NutritionActivityLogResponse> list = calorieCalculationService.getActivityLogs(userPrincipal.getId(), date);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/activity-logs")
    public ResponseEntity<ApiResponse<NutritionActivityLogResponse>> addActivityLog(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NutritionActivityLogRequest request) {
        NutritionActivityLogResponse response = calorieCalculationService.addActivityLog(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Ghi nhận hoạt động thể chất thành công", response));
    }

    @PutMapping("/activity-logs/{id}")
    public ResponseEntity<ApiResponse<NutritionActivityLogResponse>> updateActivityLog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody NutritionActivityLogRequest request) {
        NutritionActivityLogResponse response = calorieCalculationService.updateActivityLog(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hoạt động thành công", response));
    }

    @DeleteMapping("/activity-logs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteActivityLog(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        calorieCalculationService.deleteActivityLog(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa hoạt động", null));
    }

    @GetMapping("/deficit-summary")
    public ResponseEntity<ApiResponse<CalorieDeficitSummaryResponse>> getCalorieDeficitSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "tdee", required = false) java.math.BigDecimal tdee,
            @RequestParam(value = "weight", required = false) java.math.BigDecimal weight) {
        CalorieDeficitSummaryResponse summary = calorieCalculationService.getCalorieDeficitSummary(userPrincipal.getId(), date, tdee, weight);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/calories/calculate")
    public ResponseEntity<ApiResponse<CalorieDeficitSummaryResponse>> calculateDeficit(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CalorieDeficitCalculationRequest request) {
        CalorieDeficitSummaryResponse response = calorieCalculationService.calculateDeficitPreview(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<NutritionStatisticsResponse>> getNutritionStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(value = "range", defaultValue = "7_DAYS") String range,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "tdee", required = false) java.math.BigDecimal tdee,
            @RequestParam(value = "weight", required = false) java.math.BigDecimal weight) {
        NutritionStatisticsResponse response = calorieCalculationService.getNutritionStatistics(
                userPrincipal.getId(), range, fromDate, toDate, tdee, weight);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

