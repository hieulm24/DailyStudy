package com.englishlearning.dto.financial;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountRequest {
        @NotBlank(message = "Tên tài khoản không được để trống")
        private String name;

        @NotBlank(message = "Loại tài khoản không được để trống")
        private String accountType; // CASH, BANK, EWALLET, CREDIT, OTHER

        @NotNull(message = "Số dư ban đầu không được để trống")
        @DecimalMin(value = "0.0", message = "Số dư ban đầu không được âm")
        private BigDecimal initialBalance;

        @Builder.Default
        private Boolean isActive = true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountResponse {
        private Long id;
        private String name;
        private String accountType;
        private BigDecimal initialBalance;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Computed real-time metrics
        private BigDecimal currentBalance;
        private BigDecimal allocatedToGoal;
        private BigDecimal availableBalance;
        private Long transactionCount;
    }
}
