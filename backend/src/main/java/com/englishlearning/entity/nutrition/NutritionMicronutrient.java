package com.englishlearning.entity.nutrition;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "NUTRITION_MICRONUTRIENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionMicronutrient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOOD_VARIANT_ID", nullable = false)
    private NutritionFoodVariant variant;

    @Column(name = "NUTRIENT_NAME", nullable = false, length = 100)
    private String nutrientName; // e.g. "Vitamin C", "Kali", "Vitamin B6", "Canxi", "Sắt", "Magie"

    @Column(name = "AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "UNIT", nullable = false, length = 20)
    private String unit; // e.g. "mg", "mcg", "IU", "g"

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
