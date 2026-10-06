package com.englishlearning.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionUserFoodDto {
    private Long id;
    private String name;
    private String state;
    private BigDecimal servingAmount;
    private String servingUnit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbohydrate;
    private BigDecimal fat;
    private BigDecimal fiber;
    private String dataSource;
    private String description;
    @Builder.Default
    private List<NutritionMicronutrientDto> micronutrients = new ArrayList<>();
}
