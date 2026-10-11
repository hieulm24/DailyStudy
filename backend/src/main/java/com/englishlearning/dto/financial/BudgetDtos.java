package com.englishlearning.dto.financial;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudgetDtos {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveMonthlyPlanRequest {
        @NotNull(message = "Tháng không được để trống")
        @Min(value = 1, message = "Tháng phải từ 1 đến 12")
        @Max(value = 12, message = "Tháng phải từ 1 đến 12")
        private Integer month;

        @NotNull(message = "Năm không được để trống")
        @Min(value = 2000, message = "Năm không hợp lệ")
        private Integer year;

        @NotNull(message = "Thu nhập dự kiến không được để trống")
        @DecimalMin(value = "0.0", message = "Thu nhập dự kiến không được âm")
        private BigDecimal expectedIncome;

        private Long accountId; // Ví dự kiến nhận tiền (optional)
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveBudgetItemRequest {
        private Long id;

        @NotNull(message = "Tháng không được để trống")
        @Min(1)
        @Max(12)
        private Integer month;

        @NotNull(message = "Năm không được để trống")
        private Integer year;

        @NotNull(message = "Danh mục không được để trống")
        private Long categoryId;

        private Long accountId; // Ví dự kiến chi tiền (optional)

        @NotNull(message = "Số tiền dự kiến không được để trống")
        @DecimalMin(value = "0.0", message = "Số tiền dự kiến không được âm")
        private BigDecimal plannedAmount;

        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchSaveBudgetRequest {
        @NotNull(message = "Tháng không được để trống")
        @Min(1)
        @Max(12)
        private Integer month;

        @NotNull(message = "Năm không được để trống")
        private Integer year;

        private BigDecimal expectedIncome;
        private Long accountId;
        private String note;
        private List<SaveBudgetItemRequest> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BudgetItemResponse {
        private Long id;
        private Integer month;
        private Integer year;
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private String categoryColor;
        private Long accountId;
        private String accountName;
        private String accountType;
        private BigDecimal plannedAmount;
        private BigDecimal actualSpent;
        private BigDecimal remainingAmount;
        private Double percentageSpent;
        private Boolean isExceeded;
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountBudgetForecast {
        private Long accountId;
        private String accountName;
        private String accountType;
        private BigDecimal currentBalance;      // Số dư thực tế hiện tại
        private BigDecimal plannedExpense;       // Dự kiến chi từ ví này
        private BigDecimal plannedIncome;        // Dự kiến thu về ví này
        private BigDecimal projectedBalance;     // Số dư dự kiến = currentBalance + plannedIncome - plannedExpense
        private Boolean isDeficit;               // projectedBalance < 0
        private BigDecimal actualSpent;          // Thực tế đã chi từ ví này
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoalBudgetAssessment {
        private Long goalId;
        private String goalName;
        private String goalIcon;
        private String goalColor;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private BigDecimal remainingAmount;
        private LocalDate targetDate;
        private Integer monthsRemaining;
        private BigDecimal requiredMonthlySavings;    // Cần tiết kiệm mỗi tháng để đạt mục tiêu
        private BigDecimal projectedMonthlySurplus;    // Dự kiến dư tháng này (sau thu chi)
        private Boolean isSufficient;                  // Có đủ khả năng tích lũy (surplus >= required)
        private BigDecimal surplusOrDeficit;           // Chênh lệch (+ dư thêm, - thiếu hụt)
        private Double estimatedMonthsToComplete;      // Số tháng ước tính hoàn thành với mức dư này
        private String assessmentNote;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyBudgetPlanResponse {
        private Integer month;
        private Integer year;
        private BigDecimal expectedIncome;
        private Long expectedIncomeAccountId;
        private String expectedIncomeAccountName;
        private BigDecimal totalActualIncome;
        private BigDecimal effectiveIncome;
        private Boolean isIncomeFromActual;
        private BigDecimal totalPlannedExpense;
        private BigDecimal expectedSurplus;
        private BigDecimal totalActualExpense;
        private BigDecimal actualSurplus;
        private Double overallSpentPercentage;
        private String planNote;

        // Current wallet balance baseline
        private BigDecimal totalCurrentWalletBalance;

        // Projected wallet balance after budget execution
        private BigDecimal projectedTotalWalletBalance;

        // Breakdown per wallet
        private List<AccountBudgetForecast> accountForecasts;

        // Budget items per category
        private List<BudgetItemResponse> items;

        // Assessment against financial goals
        private BigDecimal totalGoalsRequiredMonthly;
        private String goalsOverallStatus; // SUFFICIENT, DEFICIT, NO_GOALS
        private List<GoalBudgetAssessment> goalAssessments;
    }
}
