package com.englishlearning.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalorieDeficitSummaryResponse {
    private LocalDate logDate;
    private BigDecimal weightKg;
    private BigDecimal tdee;
    private BigDecimal foodCalories;
    private BigDecimal activityCalories;
    private BigDecimal totalCaloriesBurned;
    private BigDecimal calorieBalance;
    private String status; // DEFICIT, SURPLUS, MAINTENANCE
    @Builder.Default
    private List<NutritionActivityLogResponse> activityLogs = new ArrayList<>();
    private Integer loggedFoodsCount;
}
