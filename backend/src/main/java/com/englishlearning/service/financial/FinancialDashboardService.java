package com.englishlearning.service.financial;

import com.englishlearning.dto.financial.CategoryDtos.CategorySpendingStat;
import com.englishlearning.dto.financial.FinancialDashboardDto.*;
import com.englishlearning.dto.financial.GoalDtos.GoalResponse;
import com.englishlearning.dto.financial.TransactionDtos.TransactionResponse;
import com.englishlearning.entity.financial.FinancialAccount;
import com.englishlearning.entity.financial.FinancialGoal;
import com.englishlearning.entity.financial.FinancialTransaction;
import com.englishlearning.repository.financial.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialDashboardService {

    private final FinancialTransactionRepository transactionRepository;
    private final FinancialAccountRepository accountRepository;
    private final FinancialGoalRepository goalRepository;
    private final FinancialGoalContributionRepository goalContributionRepository;
    private final FinancialAccountService accountService;
    private final FinancialGoalService goalService;
    private final FinancialTransactionService transactionService;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(Integer year, Integer month, LocalDateTime startDate, LocalDateTime endDate) {
        // Resolve date range (defaults to current month if not provided)
        LocalDateTime from;
        LocalDateTime to;

        if (startDate != null && endDate != null) {
            from = startDate;
            to = endDate;
        } else {
            YearMonth targetMonth = (year != null && month != null)
                    ? YearMonth.of(year, month)
                    : YearMonth.now();

            from = targetMonth.atDay(1).atStartOfDay();
            to = targetMonth.atEndOfMonth().atTime(LocalTime.MAX);
        }

        BigDecimal monthlyIncome = transactionRepository.sumIncomeBetween(from, to);
        if (monthlyIncome == null) monthlyIncome = BigDecimal.ZERO;

        BigDecimal monthlyExpense = transactionRepository.sumExpenseBetween(from, to);
        if (monthlyExpense == null) monthlyExpense = BigDecimal.ZERO;

        BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpense);

        // Tổng số dư các tài khoản
        List<FinancialAccount> activeAccounts = accountRepository.findByIsActiveTrueOrderByNameAsc();
        BigDecimal totalAccountBalance = activeAccounts.stream()
                .map(acc -> accountService.mapToResponse(acc).getCurrentBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng tiền đã phân bổ cho các mục tiêu (active)
        BigDecimal totalAllocatedToGoals = goalContributionRepository.sumTotalAllocatedActiveGoals();
        if (totalAllocatedToGoals == null || totalAllocatedToGoals.compareTo(BigDecimal.ZERO) < 0) {
            totalAllocatedToGoals = BigDecimal.ZERO;
        }

        // Cơ cấu chi tiêu theo danh mục
        List<CategorySpendingStat> categoryExpenses = buildCategoryBreakdown(from, to, monthlyExpense, true);

        // Biểu đồ thu nhập & chi tiêu theo tháng (6 tháng gần nhất tính tới thời điểm chọn)
        List<MonthlyTrendPoint> monthlyTrends = buildMonthlyTrends(to.toLocalDate());

        // Giao dịch gần nhất
        List<FinancialTransaction> recents = transactionRepository.findTop10ByOrderByTransactionDateDescCreatedAtDesc();
        List<TransactionResponse> recentResponses = recents.stream()
                .map(transactionService::mapToResponse)
                .collect(Collectors.toList());

        // Tiến độ các mục tiêu tài chính
        List<FinancialGoal> goals = goalRepository.findAllByOrderByCreatedAtDesc();
        List<GoalResponse> goalResponses = goals.stream()
                .map(goalService::mapToResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .monthlyIncome(monthlyIncome)
                .monthlyExpense(monthlyExpense)
                .monthlyNetBalance(monthlyNet)
                .totalAccountBalance(totalAccountBalance)
                .totalAllocatedToGoals(totalAllocatedToGoals)
                .categoryExpenses(categoryExpenses)
                .monthlyTrends(monthlyTrends)
                .recentTransactions(recentResponses)
                .goalsSummary(goalResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportData(Integer year, Integer month, LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime from;
        LocalDateTime to;

        if (startDate != null && endDate != null) {
            from = startDate;
            to = endDate;
        } else {
            YearMonth targetMonth = (year != null && month != null)
                    ? YearMonth.of(year, month)
                    : YearMonth.now();

            from = targetMonth.atDay(1).atStartOfDay();
            to = targetMonth.atEndOfMonth().atTime(LocalTime.MAX);
        }

        BigDecimal totalIncome = transactionRepository.sumIncomeBetween(from, to);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpense = transactionRepository.sumExpenseBetween(from, to);
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        List<CategorySpendingStat> expenseByCategory = buildCategoryBreakdown(from, to, totalExpense, true);
        List<CategorySpendingStat> incomeByCategory = buildCategoryBreakdown(from, to, totalIncome, false);

        List<MonthlyTrendPoint> monthlyComparison = buildMonthlyTrends(to.toLocalDate());

        // Goals progress report
        List<FinancialGoal> allGoals = goalRepository.findAllByOrderByCreatedAtDesc();
        List<GoalResponse> goalsReport = allGoals.stream()
                .map(goalService::mapToResponse)
                .collect(Collectors.toList());

        // Balance trend
        List<BalanceTrendPoint> balanceTrend = buildBalanceTrend(to.toLocalDate());

        return ReportResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .expenseByCategory(expenseByCategory)
                .incomeByCategory(incomeByCategory)
                .monthlyComparison(monthlyComparison)
                .balanceTrend(balanceTrend)
                .goalsReport(goalsReport)
                .build();
    }

    private List<CategorySpendingStat> buildCategoryBreakdown(LocalDateTime from, LocalDateTime to, BigDecimal total, boolean isExpense) {
        List<Object[]> rawList = isExpense
                ? transactionRepository.getCategoryExpenseBreakdown(from, to)
                : transactionRepository.getCategoryIncomeBreakdown(from, to);

        List<CategorySpendingStat> result = new ArrayList<>();
        for (Object[] row : rawList) {
            Long catId = (Long) row[0];
            String name = (String) row[1];
            String icon = (String) row[2];
            String color = (String) row[3];
            BigDecimal amount = (BigDecimal) row[4];
            Long count = (Long) row[5];

            double pct = 0.0;
            if (total.compareTo(BigDecimal.ZERO) > 0 && amount != null) {
                pct = amount.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            result.add(CategorySpendingStat.builder()
                    .categoryId(catId)
                    .categoryName(name != null ? name : "Chưa phân loại")
                    .icon(icon != null ? icon : "Tag")
                    .color(color != null ? color : "#64748b")
                    .totalAmount(amount != null ? amount : BigDecimal.ZERO)
                    .percentage(pct)
                    .count(count)
                    .build());
        }

        return result;
    }

    private List<MonthlyTrendPoint> buildMonthlyTrends(LocalDate baseDate) {
        List<MonthlyTrendPoint> points = new ArrayList<>();
        YearMonth current = YearMonth.from(baseDate);

        // Last 6 months
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

            BigDecimal inc = transactionRepository.sumIncomeBetween(start, end);
            if (inc == null) inc = BigDecimal.ZERO;

            BigDecimal exp = transactionRepository.sumExpenseBetween(start, end);
            if (exp == null) exp = BigDecimal.ZERO;

            BigDecimal net = inc.subtract(exp);

            points.add(MonthlyTrendPoint.builder()
                    .period(ym.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .label("Tháng " + ym.getMonthValue())
                    .income(inc)
                    .expense(exp)
                    .net(net)
                    .build());
        }

        return points;
    }

    private List<BalanceTrendPoint> buildBalanceTrend(LocalDate baseDate) {
        List<BalanceTrendPoint> points = new ArrayList<>();
        YearMonth current = YearMonth.from(baseDate);

        // Sum initial balances of all accounts
        BigDecimal totalInitial = accountRepository.findAll().stream()
                .map(a -> a.getInitialBalance() != null ? a.getInitialBalance() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Start of the 6-month window
        YearMonth startWindowMonth = current.minusMonths(5);
        LocalDateTime windowStartTime = startWindowMonth.atDay(1).atStartOfDay();

        // Calculate cumulative balance before the 6-month window starts
        BigDecimal preWindowIncome = transactionRepository.sumIncomeBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0), windowStartTime.minusSeconds(1));
        if (preWindowIncome == null) preWindowIncome = BigDecimal.ZERO;

        BigDecimal preWindowExpense = transactionRepository.sumExpenseBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0), windowStartTime.minusSeconds(1));
        if (preWindowExpense == null) preWindowExpense = BigDecimal.ZERO;

        BigDecimal runningBalance = totalInitial.add(preWindowIncome).subtract(preWindowExpense);

        // 6 months trend
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

            BigDecimal inc = transactionRepository.sumIncomeBetween(start, end);
            if (inc == null) inc = BigDecimal.ZERO;

            BigDecimal exp = transactionRepository.sumExpenseBetween(start, end);
            if (exp == null) exp = BigDecimal.ZERO;

            runningBalance = runningBalance.add(inc).subtract(exp);

            points.add(BalanceTrendPoint.builder()
                    .date(ym.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                    .income(inc)
                    .expense(exp)
                    .balance(runningBalance)
                    .build());
        }

        return points;
    }
}
