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
public class TopMicronutrientDto {
    private String nutrientName;
    private BigDecimal totalAmount;
    private String unit;
}
