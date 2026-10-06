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
public class UsdaFoodDto {
    private Long fdcId;
    private String description;
    private String dataType;
    private String brandOwner;
    private BigDecimal servingSize;
    private String servingSizeUnit;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbohydrate;
    private BigDecimal fat;
    private BigDecimal fiber;
    @Builder.Default
    private List<NutritionMicronutrientDto> micronutrients = new ArrayList<>();
}
