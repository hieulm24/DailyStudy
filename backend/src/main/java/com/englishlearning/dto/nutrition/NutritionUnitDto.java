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
public class NutritionUnitDto {
    private Long id;
    private String unit;
    private BigDecimal gramValue;
    private BigDecimal mlValue;
    private String description;
}
