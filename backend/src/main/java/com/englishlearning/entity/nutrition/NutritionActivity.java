package com.englishlearning.entity.nutrition;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "NUTRITION_ACTIVITY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "CATEGORY", nullable = false, length = 50)
    private String category;

    @Column(name = "INTENSITY", nullable = false, length = 50)
    private String intensity;

    @Column(name = "MET_VALUE", nullable = false, precision = 6, scale = 2)
    private BigDecimal metValue;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
}
