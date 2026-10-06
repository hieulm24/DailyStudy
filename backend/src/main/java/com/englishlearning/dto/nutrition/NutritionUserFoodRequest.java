package com.englishlearning.dto.nutrition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class NutritionUserFoodRequest {

    @NotBlank(message = "Tên thực phẩm không được để trống")
    private String name;

    private String state;

    @Builder.Default
    private BigDecimal servingAmount = new BigDecimal("100");

    @Builder.Default
    private String servingUnit = "g";

    @NotNull(message = "Calories không được để trống")
    @DecimalMin(value = "0.00", message = "Calories không được là số âm")
    private BigDecimal calories;

    @NotNull(message = "Protein không được để trống")
    @DecimalMin(value = "0.00", message = "Protein không được là số âm")
    private BigDecimal protein;

    @NotNull(message = "Carbohydrate không được để trống")
    @DecimalMin(value = "0.00", message = "Carbohydrate không được là số âm")
    private BigDecimal carbohydrate;

    @NotNull(message = "Fat không được để trống")
    @DecimalMin(value = "0.00", message = "Fat không được là số âm")
    private BigDecimal fat;

    @Builder.Default
    private BigDecimal fiber = BigDecimal.ZERO;

    private String dataSource;

    private String description;

    @Builder.Default
    private List<NutritionMicronutrientDto> micronutrients = new ArrayList<>();
}
