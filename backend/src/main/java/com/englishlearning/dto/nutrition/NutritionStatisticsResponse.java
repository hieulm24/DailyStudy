package com.englishlearning.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionStatisticsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private int daysCount;
    private int loggedDaysCount;
    private BigDecimal tdee;
    private BigDecimal weightKg;

    // Calorie totals & averages
    private BigDecimal totalFoodCalories;
    private BigDecimal avgDailyFoodCalories;

    private BigDecimal totalActivityCalories;
    private BigDecimal avgDailyActivityCalories;

    private BigDecimal totalBurnedCalories;
    private BigDecimal avgDailyBurnedCalories;

    private BigDecimal netCalorieBalance;
    private BigDecimal avgDailyCalorieBalance;
    private BigDecimal estimatedFatKgChange; // netCalorieBalance / 7700

    // Workout & compliance metrics
    private BigDecimal totalWorkoutMinutes;
    private BigDecimal avgDailyWorkoutMinutes;
    private int workoutSessionsCount;

    private int deficitDaysCount;
    private int surplusDaysCount;
    private int maintenanceDaysCount;
    private BigDecimal deficitRatePercent;

    private int gymDaysCount;
    private int nonGymWorkoutDaysCount;
    private int restDaysCount;

    // Macros
    private BigDecimal totalProtein;
    private BigDecimal totalCarbohydrate;
    private BigDecimal totalFat;
    private BigDecimal totalFiber;
    private BigDecimal avgDailyProtein;
    private BigDecimal avgDailyCarbohydrate;
    private BigDecimal avgDailyFat;

    // Breakdowns
    private List<CategorySummary> categoryBreakdown;
    private List<DailyTrendItem> dailyTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String category;
        private String categoryName;
        private BigDecimal totalMinutes;
        private BigDecimal totalCalories;
        private int sessionsCount;
        private BigDecimal percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendItem {
        private LocalDate date;
        private String dayOfWeek;
        private BigDecimal foodCalories;
        private BigDecimal activityCalories;
        private BigDecimal totalBurned;
        private BigDecimal tdee;
        private BigDecimal calorieBalance;
        private String status; // DEFICIT, SURPLUS, MAINTENANCE
        private BigDecimal protein;
        private BigDecimal carbohydrate;
        private BigDecimal fat;
        private BigDecimal workoutMinutes;
        private int foodCount;
        private int activityCount;
        private boolean hasGym;
        private String activitySummary;
    }
}
