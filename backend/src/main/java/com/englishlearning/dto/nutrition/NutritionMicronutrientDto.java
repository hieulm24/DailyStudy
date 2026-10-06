package com.englishlearning.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionMicronutrientDto {
    private Long id;
    private String nutrientName;
    private BigDecimal amount;
    private String unit;
}
