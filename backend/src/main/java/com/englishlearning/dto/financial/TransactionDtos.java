package com.englishlearning.dto.financial;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionRequest {
        @NotNull(message = "Tài khoản không được để trống")
        private Long accountId;

        private Long categoryId; // Bắt buộc đối với INCOME/EXPENSE, null đối với TRANSFER

        @NotBlank(message = "Loại giao dịch không được để trống")
        private String type; // INCOME, EXPENSE, TRANSFER

        @NotNull(message = "Số tiền không được để trống")
        @DecimalMin(value = "0.01", message = "Số tiền phải lớn hơn 0")
        private BigDecimal amount;

        @NotNull(message = "Ngày giao dịch không được để trống")
        private LocalDateTime transactionDate;

        private String description;
        private String note;

        private Long toAccountId; // Bắt buộc đối với TRANSFER
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionResponse {
        private Long id;
        private Long accountId;
        private String accountName;
        private String accountType;

        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private String categoryColor;

        private String type; // INCOME, EXPENSE, TRANSFER
        private BigDecimal amount;
        private LocalDateTime transactionDate;
        private String description;
        private String note;

        private Long toAccountId;
        private String toAccountName;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionFilterRequest {
        private String search;
        private String type;
        private Long accountId;
        private Long categoryId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime endDate;

        @Builder.Default
        private int page = 0;

        @Builder.Default
        private int size = 20;

        @Builder.Default
        private String sortBy = "transactionDate";

        @Builder.Default
        private String sortDir = "desc";
    }
}
