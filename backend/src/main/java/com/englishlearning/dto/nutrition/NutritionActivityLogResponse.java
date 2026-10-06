package com.englishlearning.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionActivityLogResponse {
    private Long id;
    private Long userId;
    private LocalDate logDate;
    private Long activityId;
    private String activityName;
    private String category;
    private String intensity;
    private BigDecimal durationMinutes;
    private BigDecimal weightKg;
    private BigDecimal metValue;
    private BigDecimal caloriesBurned;
    private LocalDateTime createdAt;
}
