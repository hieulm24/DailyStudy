package com.englishlearning.dto.health;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthWorkoutPhotoResponse {

    private Long id;
    private Long userId;
    private LocalDate logDate;
    private String imageUrl;
    private String caption;
    private BigDecimal weightKg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
