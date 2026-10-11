package com.englishlearning.dto.financial;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoalRequest {
        @NotBlank(message = "Tên mục tiêu không được để trống")
        private String name;

        private String description;

        @NotNull(message = "Số tiền mục tiêu không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền mục tiêu phải lớn hơn 0")
        private BigDecimal targetAmount;

        @NotNull(message = "Ngày bắt đầu không được để trống")
        private LocalDate startDate;

        private LocalDate targetDate;

        @Builder.Default
        private String status = "ACTIVE"; // ACTIVE, PAUSED, COMPLETED, CANCELLED

        private String icon;
        private String color;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoalResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal targetAmount;
        private LocalDate startDate;
        private LocalDate targetDate;
        private String status; // ACTIVE, PAUSED, COMPLETED, CANCELLED
        private String icon;
        private String color;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Computed metrics
        private BigDecimal currentAmount;
        private BigDecimal remainingAmount;
        private Double progressPercentage;
        private BigDecimal monthlySavingsNeeded;
        private LocalDate estimatedCompletionDate;
        private String planStatusNote;
        private Long contributionCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoalContributionRequest {
        @NotNull(message = "Mục tiêu không được để trống")
        private Long goalId;

        @NotNull(message = "Tài khoản nguồn không được để trống")
        private Long accountId;

        private Long transactionId;

        @NotBlank(message = "Loại tích lũy không được để trống")
        private String type; // DEPOSIT, WITHDRAW

        @NotNull(message = "Số tiền không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
        private BigDecimal amount;

        @NotNull(message = "Ngày tích lũy không được để trống")
        private LocalDateTime contributionDate;

        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoalContributionResponse {
        private Long id;
        private Long goalId;
        private String goalName;
        private Long accountId;
        private String accountName;
        private Long transactionId;
        private String type; // DEPOSIT, WITHDRAW
        private BigDecimal amount;
        private LocalDateTime contributionDate;
        private String note;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
