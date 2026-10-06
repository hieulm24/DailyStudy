package com.englishlearning.entity.nutrition;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "NUTRITION_FOOD_UNIT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionFoodUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOOD_ID", nullable = false)
    private NutritionFood food;

    @Column(name = "UNIT", nullable = false, length = 30)
    private String unit; // e.g. "bát", "quả", "thìa", "cái", "miếng", "hộp", "chai", "lon", "khẩu phần"

    @Column(name = "GRAM_VALUE", precision = 10, scale = 2)
    private BigDecimal gramValue;

    @Column(name = "ML_VALUE", precision = 10, scale = 2)
    private BigDecimal mlValue;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

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
