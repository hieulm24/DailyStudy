package com.englishlearning.dto.nutrition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionActivityLogRequest {

    @NotNull(message = "Ngày ghi nhận không được để trống")
    private LocalDate logDate;

    @NotNull(message = "Hoạt động không được để trống")
    private Long activityId;

    @NotNull(message = "Thời gian tập không được để trống")
    @DecimalMin(value = "0.1", message = "Thời gian tập phải lớn hơn 0")
    private BigDecimal durationMinutes;

    @DecimalMin(value = "1.0", message = "Cân nặng phải lớn hơn 0")
    private BigDecimal weightKg;
}
