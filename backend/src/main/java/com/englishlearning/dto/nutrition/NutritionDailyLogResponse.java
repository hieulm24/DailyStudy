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
public class NutritionDailyLogResponse {
    private Long id;
    private LocalDate logDate;
    private Long foodVariantId;
    private Long userFoodId;
    private String foodName;
    private String state;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal calculatedGrams;
    private BigDecimal calculatedMl;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbohydrate;
    private BigDecimal fat;
    private BigDecimal fiber;
    private Boolean isUserCustom;
    private String dataSource;
    @Builder.Default
    private List<NutritionMicronutrientDto> micronutrients = new ArrayList<>();
}
