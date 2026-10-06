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
public class CalorieDeficitCalculationRequest {
    private BigDecimal weightKg;
    private BigDecimal tdee;
    private BigDecimal foodCalories;
    private LocalDate logDate;
    private List<Item> activities;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long activityId;
        private BigDecimal durationMinutes;
    }
}
