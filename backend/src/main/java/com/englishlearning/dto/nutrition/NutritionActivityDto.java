package com.englishlearning.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionActivityDto {
    private Long id;
    private String name;
    private String category;
    private String intensity;
    private BigDecimal metValue;
    private String description;
}
