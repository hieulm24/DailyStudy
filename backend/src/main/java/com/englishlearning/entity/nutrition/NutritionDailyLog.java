package com.englishlearning.entity.nutrition;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NUTRITION_DAILY_LOG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionDailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "LOG_DATE", nullable = false)
    private LocalDate logDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOOD_VARIANT_ID")
    private NutritionFoodVariant foodVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_FOOD_ID")
    private NutritionUserFood userFood;

    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "UNIT", nullable = false, length = 30)
    private String unit;

    @Column(name = "CALCULATED_GRAMS", precision = 10, scale = 2)
    private BigDecimal calculatedGrams;

    @Column(name = "CALCULATED_ML", precision = 10, scale = 2)
    private BigDecimal calculatedMl;

    @Column(name = "CALORIES", nullable = false, precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(name = "PROTEIN", nullable = false, precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(name = "CARBOHYDRATE", nullable = false, precision = 10, scale = 2)
    private BigDecimal carbohydrate;

    @Column(name = "FAT", nullable = false, precision = 10, scale = 2)
    private BigDecimal fat;

    @Column(name = "FIBER", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fiber = BigDecimal.ZERO;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.fiber == null) {
            this.fiber = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
