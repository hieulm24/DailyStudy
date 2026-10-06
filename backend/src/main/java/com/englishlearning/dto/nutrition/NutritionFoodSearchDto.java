package com.englishlearning.dto.nutrition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionFoodSearchDto {
    private Long foodId;
    private Long userFoodId;
    private String name;
    private String category;
    private String description;
    private String dataSource;
    private Boolean isUserCustom;
    @Builder.Default
    private List<NutritionVariantDto> variants = new ArrayList<>();
    @Builder.Default
    private List<NutritionUnitDto> units = new ArrayList<>();
}
