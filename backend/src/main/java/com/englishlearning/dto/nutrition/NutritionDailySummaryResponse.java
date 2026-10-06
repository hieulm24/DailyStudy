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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionDailySummaryResponse {
    private LocalDate logDate;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbohydrate;
    private BigDecimal totalFat;
    private BigDecimal totalFiber;
    @Builder.Default
    private List<NutritionDailyLogResponse> items = new ArrayList<>();
    @Builder.Default
    private List<TopMicronutrientDto> topMicronutrients = new ArrayList<>();
}
