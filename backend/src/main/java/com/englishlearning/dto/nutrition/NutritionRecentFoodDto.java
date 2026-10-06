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
public class NutritionRecentFoodDto {
    private Long foodVariantId;
    private Long userFoodId;
    private String name;
    private String state;
    private BigDecimal defaultQuantity;
    private String defaultUnit;
    private Boolean isUserCustom;
}
