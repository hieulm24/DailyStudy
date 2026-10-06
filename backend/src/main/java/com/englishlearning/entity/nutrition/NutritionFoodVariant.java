package com.englishlearning.entity.nutrition;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "NUTRITION_FOOD_VARIANT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionFoodVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOOD_ID", nullable = false)
    private NutritionFood food;

    @Column(name = "STATE", nullable = false, length = 50)
    private String state; // e.g. "Sống", "Chín", "Luộc", "Hấp", "Chiên", "Nướng", "Khô", "Nước", "Tươi", "Khác"

    @Column(name = "SERVING_AMOUNT", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal servingAmount = new BigDecimal("100");

    @Column(name = "SERVING_UNIT", nullable = false, length = 20)
    @Builder.Default
    private String servingUnit = "g";

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

    @Column(name = "DATA_SOURCE", length = 100)
    private String dataSource;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NutritionMicronutrient> micronutrients = new ArrayList<>();

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
