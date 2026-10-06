package com.englishlearning.dto.nutrition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionDailyLogRequest {

    @NotNull(message = "Ngày ghi nhận không được để trống")
    private LocalDate logDate;

    private Long foodVariantId;

    private Long userFoodId;

    @NotNull(message = "Số lượng không được để trống")
    @DecimalMin(value = "0.01", message = "Số lượng phải lớn hơn 0")
    private BigDecimal quantity;

    @NotBlank(message = "Đơn vị không được để trống")
    private String unit;
}
