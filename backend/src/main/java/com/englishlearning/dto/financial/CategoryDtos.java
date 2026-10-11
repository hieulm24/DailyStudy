package com.englishlearning.dto.financial;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CategoryDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryRequest {
        @NotBlank(message = "Tên danh mục không được để trống")
        private String name;

        @NotBlank(message = "Loại danh mục không được để trống")
        private String type; // INCOME, EXPENSE

        private String icon;
        private String color;

        @Builder.Default
        private Boolean isDefault = false;

        @Builder.Default
        private Boolean isActive = true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String type;
        private String icon;
        private String color;
        private Boolean isDefault;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private Long transactionCount;
        private BigDecimal totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySpendingStat {
        private Long categoryId;
        private String categoryName;
        private String icon;
        private String color;
        private BigDecimal totalAmount;
        private Double percentage;
        private Long count;
    }
}
