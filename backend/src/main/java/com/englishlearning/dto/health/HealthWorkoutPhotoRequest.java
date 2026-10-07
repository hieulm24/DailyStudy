package com.englishlearning.dto.health;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthWorkoutPhotoRequest {

    @NotNull(message = "Ngày ghi nhận không được để trống")
    private LocalDate logDate;

    @NotBlank(message = "Đường dẫn ảnh không được để trống")
    private String imageUrl;

    private String caption;

    private BigDecimal weightKg;
}
