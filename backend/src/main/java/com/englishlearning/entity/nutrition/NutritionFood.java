package com.englishlearning.entity.nutrition;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "NUTRITION_FOOD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 255)
    private String name;

    @Column(name = "CATEGORY", length = 100)
    private String category;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "DATA_SOURCE", length = 100)
    private String dataSource;

    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NutritionFoodVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NutritionFoodUnit> units = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
