package com.englishlearning.entity.nutrition;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NUTRITION_ACTIVITY_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "LOG_DATE", nullable = false)
    private LocalDate logDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIVITY_ID", nullable = false)
    private NutritionActivity activity;

    @Column(name = "DURATION_MINUTES", nullable = false, precision = 8, scale = 2)
    private BigDecimal durationMinutes;

    @Column(name = "WEIGHT_KG", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "MET_VALUE", nullable = false, precision = 6, scale = 2)
    private BigDecimal metValue;

    @Column(name = "CALORIES_BURNED", nullable = false, precision = 10, scale = 2)
    private BigDecimal caloriesBurned;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
}
