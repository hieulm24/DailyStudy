package com.englishlearning.dto.financial;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class FinancialDashboardDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardResponse {
        private BigDecimal monthlyIncome;
        private BigDecimal monthlyExpense;
        private BigDecimal monthlyNetBalance; // Income - Expense
        private BigDecimal totalAccountBalance; // Tổng số dư các tài khoản
        private BigDecimal totalAllocatedToGoals; // Tổng tiền đã phân bổ cho các mục tiêu
        private List<CategoryDtos.CategorySpendingStat> categoryExpenses;
        private List<MonthlyTrendPoint> monthlyTrends;
        private List<TransactionDtos.TransactionResponse> recentTransactions;
        private List<GoalDtos.GoalResponse> goalsSummary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyTrendPoint {
        private String period; // e.g., "2026-10" or "11/10"
        private String label;  // e.g., "Tháng 10"
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportResponse {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netSavings;
        private List<CategoryDtos.CategorySpendingStat> expenseByCategory;
        private List<CategoryDtos.CategorySpendingStat> incomeByCategory;
        private List<MonthlyTrendPoint> monthlyComparison;
        private List<BalanceTrendPoint> balanceTrend;
        private List<GoalDtos.GoalResponse> goalsReport;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BalanceTrendPoint {
        private String date; // YYYY-MM-DD
        private BigDecimal balance;
        private BigDecimal income;
        private BigDecimal expense;
    }
}
