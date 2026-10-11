package com.englishlearning.service.financial;

import com.englishlearning.dto.financial.AccountDtos.AccountResponse;
import com.englishlearning.dto.financial.BudgetDtos.*;
import com.englishlearning.dto.financial.GoalDtos.GoalResponse;
import com.englishlearning.entity.financial.FinancialAccount;
import com.englishlearning.entity.financial.FinancialBudget;
import com.englishlearning.entity.financial.FinancialCategory;
import com.englishlearning.entity.financial.FinancialMonthlyPlan;
import com.englishlearning.repository.financial.FinancialAccountRepository;
import com.englishlearning.repository.financial.FinancialBudgetRepository;
import com.englishlearning.repository.financial.FinancialCategoryRepository;
import com.englishlearning.repository.financial.FinancialMonthlyPlanRepository;
import com.englishlearning.repository.financial.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialBudgetService {

    private final FinancialMonthlyPlanRepository planRepository;
    private final FinancialBudgetRepository budgetRepository;
    private final FinancialCategoryRepository categoryRepository;
    private final FinancialAccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final FinancialAccountService accountService;
    private final FinancialGoalService goalService;

    @Transactional(readOnly = true)
    public MonthlyBudgetPlanResponse getMonthlyBudgetPlan(Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int targetMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();
        int targetYear = (year != null && year >= 2000) ? year : now.getYear();

        // 1. Get Monthly Plan (Expected Income & Associated Account)
        FinancialMonthlyPlan plan = planRepository.findByMonthAndYear(targetMonth, targetYear)
                .orElse(null);
        BigDecimal expectedIncome = (plan != null && plan.getExpectedIncome() != null)
                ? plan.getExpectedIncome()
                : BigDecimal.ZERO;
        Long expectedIncomeAccountId = (plan != null) ? plan.getAccountId() : null;
        String expectedIncomeAccountName = null;
        if (expectedIncomeAccountId != null) {
            expectedIncomeAccountName = accountRepository.findById(expectedIncomeAccountId)
                    .map(FinancialAccount::getName)
                    .orElse(null);
        }
        String planNote = (plan != null) ? plan.getNote() : null;

        // 2. Query budget items with category and account
        List<FinancialBudget> budgets = budgetRepository.findByMonthAndYearWithCategory(targetMonth, targetYear);

        // 3. Query actual expenses per category in this month
        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = ym.atEndOfMonth().atTime(LocalTime.MAX);

        List<Object[]> breakdown = transactionRepository.getCategoryExpenseBreakdown(startOfMonth, endOfMonth);
        Map<Long, BigDecimal> actualExpenseMap = new HashMap<>();
        BigDecimal totalActualExpense = BigDecimal.ZERO;

        for (Object[] row : breakdown) {
            Long catId = (Long) row[0];
            BigDecimal spent = (BigDecimal) row[4];
            if (catId != null && spent != null) {
                actualExpenseMap.put(catId, spent);
                totalActualExpense = totalActualExpense.add(spent);
            }
        }

        // 4. Build items response
        BigDecimal totalPlannedExpense = BigDecimal.ZERO;
        Map<Long, BigDecimal> plannedExpensePerAccount = new HashMap<>();
        List<BudgetItemResponse> items = new ArrayList<>();

        for (FinancialBudget b : budgets) {
            BigDecimal planned = (b.getPlannedAmount() != null) ? b.getPlannedAmount() : BigDecimal.ZERO;
            totalPlannedExpense = totalPlannedExpense.add(planned);

            if (b.getAccountId() != null) {
                plannedExpensePerAccount.merge(b.getAccountId(), planned, BigDecimal::add);
            }

            BigDecimal actual = actualExpenseMap.getOrDefault(b.getCategoryId(), BigDecimal.ZERO);
            BigDecimal remaining = planned.subtract(actual);

            double percentage = 0.0;
            if (planned.compareTo(BigDecimal.ZERO) > 0) {
                percentage = actual.divide(planned, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            boolean isExceeded = actual.compareTo(planned) > 0;

            FinancialCategory cat = b.getCategory();
            FinancialAccount acc = b.getAccount();

            items.add(BudgetItemResponse.builder()
                    .id(b.getId())
                    .month(b.getMonth())
                    .year(b.getYear())
                    .categoryId(b.getCategoryId())
                    .categoryName(cat != null ? cat.getName() : "Chưa phân loại")
                    .categoryIcon(cat != null ? cat.getIcon() : "Tag")
                    .categoryColor(cat != null ? cat.getColor() : "#64748b")
                    .accountId(b.getAccountId())
                    .accountName(acc != null ? acc.getName() : "Tất cả ví")
                    .accountType(acc != null ? acc.getAccountType() : null)
                    .plannedAmount(planned)
                    .actualSpent(actual)
                    .remainingAmount(remaining)
                    .percentageSpent(percentage)
                    .isExceeded(isExceeded)
                    .note(b.getNote())
                    .build());
        }

        // 4.5 Query actual income in this month from transactions
        BigDecimal totalActualIncome = transactionRepository.sumIncomeBetween(startOfMonth, endOfMonth);
        if (totalActualIncome == null) {
            totalActualIncome = BigDecimal.ZERO;
        }

        // Effective income used for planning:
        // - If user set expectedIncome > 0 (e.g. planning for next month), use it!
        // - If expectedIncome == 0 (current month with real transactions), smartly fall back to totalActualIncome!
        boolean isIncomeFromActual = false;
        BigDecimal effectiveIncome = (expectedIncome != null) ? expectedIncome : BigDecimal.ZERO;
        if (effectiveIncome.compareTo(BigDecimal.ZERO) == 0 && totalActualIncome.compareTo(BigDecimal.ZERO) > 0) {
            effectiveIncome = totalActualIncome;
            isIncomeFromActual = true;
        }

        // 5. Calculate Surplus
        BigDecimal expectedSurplus = effectiveIncome.subtract(totalPlannedExpense);
        BigDecimal actualSurplus = totalActualIncome.subtract(totalActualExpense);

        double overallSpentPercentage = 0.0;
        if (totalPlannedExpense.compareTo(BigDecimal.ZERO) > 0) {
            overallSpentPercentage = totalActualExpense.divide(totalPlannedExpense, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        // 6. Account Cashflow Forecasts (Dự báo dòng tiền từng ví)
        List<AccountResponse> activeAccounts = accountService.getAllAccounts(true);
        BigDecimal totalCurrentWalletBalance = BigDecimal.ZERO;
        List<AccountBudgetForecast> accountForecasts = new ArrayList<>();

        for (AccountResponse acc : activeAccounts) {
            BigDecimal currentBal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;
            totalCurrentWalletBalance = totalCurrentWalletBalance.add(currentBal);

            BigDecimal plannedExpense = plannedExpensePerAccount.getOrDefault(acc.getId(), BigDecimal.ZERO);
            BigDecimal plannedIncome = (expectedIncomeAccountId != null && expectedIncomeAccountId.equals(acc.getId()))
                    ? expectedIncome
                    : BigDecimal.ZERO;

            BigDecimal projectedBal = currentBal.add(plannedIncome).subtract(plannedExpense);
            boolean isDeficit = projectedBal.compareTo(BigDecimal.ZERO) < 0;

            // Actual spent from this account in the month
            BigDecimal actualSpent = transactionRepository.sumExpenseBetween(startOfMonth, endOfMonth);
            // specific to account:
            BigDecimal actualSpentForAcc = transactionRepository.sumExpenseForAccount(acc.getId());

            accountForecasts.add(AccountBudgetForecast.builder()
                    .accountId(acc.getId())
                    .accountName(acc.getName())
                    .accountType(acc.getAccountType())
                    .currentBalance(currentBal)
                    .plannedExpense(plannedExpense)
                    .plannedIncome(plannedIncome)
                    .projectedBalance(projectedBal)
                    .isDeficit(isDeficit)
                    .actualSpent(actualSpentForAcc != null ? actualSpentForAcc : BigDecimal.ZERO)
                    .build());
        }

        BigDecimal projectedTotalWalletBalance = totalCurrentWalletBalance.subtract(totalPlannedExpense);
        if (expectedIncome != null && expectedIncome.compareTo(BigDecimal.ZERO) > 0) {
            projectedTotalWalletBalance = projectedTotalWalletBalance.add(expectedIncome);
        }

        // 7. Goal Feasibility Assessment (Đối soát với Mục tiêu tài chính)
        List<GoalResponse> activeGoals = goalService.getAllGoals("ACTIVE");
        BigDecimal totalGoalsRequiredMonthly = BigDecimal.ZERO;
        List<GoalBudgetAssessment> goalAssessments = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("#,###");

        for (GoalResponse g : activeGoals) {
            BigDecimal targetAmount = g.getTargetAmount() != null ? g.getTargetAmount() : BigDecimal.ZERO;
            BigDecimal currentAmount = g.getCurrentAmount() != null ? g.getCurrentAmount() : BigDecimal.ZERO;
            BigDecimal remainingAmount = g.getRemainingAmount() != null ? g.getRemainingAmount() : targetAmount.subtract(currentAmount);

            LocalDate targetDate = g.getTargetDate();
            int monthsRemaining = 1;
            if (targetDate != null) {
                long months = ChronoUnit.MONTHS.between(YearMonth.from(now), YearMonth.from(targetDate));
                monthsRemaining = (int) Math.max(1, months);
            }

            BigDecimal reqSavings = g.getMonthlySavingsNeeded();
            if (reqSavings == null || reqSavings.compareTo(BigDecimal.ZERO) <= 0) {
                if (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && monthsRemaining > 0) {
                    reqSavings = remainingAmount.divide(BigDecimal.valueOf(monthsRemaining), 0, RoundingMode.CEILING);
                } else {
                    reqSavings = BigDecimal.ZERO;
                }
            }

            totalGoalsRequiredMonthly = totalGoalsRequiredMonthly.add(reqSavings);

            boolean isSufficient = expectedSurplus.compareTo(reqSavings) >= 0;
            BigDecimal diff = expectedSurplus.subtract(reqSavings);

            Double estMonths = null;
            if (expectedSurplus.compareTo(BigDecimal.ZERO) > 0 && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                estMonths = remainingAmount.divide(expectedSurplus, 1, RoundingMode.HALF_UP).doubleValue();
            }

            String assessmentNote;
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                assessmentNote = "Mục tiêu đã hoàn thành 100%!";
            } else if (isSufficient) {
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    assessmentNote = "Dự kiến dư +" + df.format(expectedSurplus) + " ₫, đủ tiền tích lũy và còn dư thêm +" + df.format(diff) + " ₫.";
                } else {
                    assessmentNote = "Vừa vặn đủ chỉ tiêu tích lũy (" + df.format(reqSavings) + " ₫) cho mục tiêu này.";
                }
            } else {
                if (expectedSurplus.compareTo(BigDecimal.ZERO) > 0) {
                    assessmentNote = "Chỉ dư +" + df.format(expectedSurplus) + " ₫, bị thiếu hụt " + df.format(diff.abs()) + " ₫ so với tiến độ chuẩn (" + df.format(reqSavings) + " ₫/tháng).";
                } else {
                    assessmentNote = "Kế hoạch chi tiêu bị âm, không có số dư để tích lũy cho mục tiêu này!";
                }
            }

            goalAssessments.add(GoalBudgetAssessment.builder()
                    .goalId(g.getId())
                    .goalName(g.getName())
                    .goalIcon(g.getIcon())
                    .goalColor(g.getColor())
                    .targetAmount(targetAmount)
                    .currentAmount(currentAmount)
                    .remainingAmount(remainingAmount)
                    .targetDate(targetDate)
                    .monthsRemaining(monthsRemaining)
                    .requiredMonthlySavings(reqSavings)
                    .projectedMonthlySurplus(expectedSurplus)
                    .isSufficient(isSufficient)
                    .surplusOrDeficit(diff)
                    .estimatedMonthsToComplete(estMonths)
                    .assessmentNote(assessmentNote)
                    .build());
        }

        String goalsOverallStatus = "NO_GOALS";
        if (!activeGoals.isEmpty()) {
            if (expectedSurplus.compareTo(totalGoalsRequiredMonthly) >= 0) {
                goalsOverallStatus = "SUFFICIENT";
            } else {
                goalsOverallStatus = "DEFICIT";
            }
        }

        return MonthlyBudgetPlanResponse.builder()
                .month(targetMonth)
                .year(targetYear)
                .expectedIncome(expectedIncome)
                .expectedIncomeAccountId(expectedIncomeAccountId)
                .expectedIncomeAccountName(expectedIncomeAccountName)
                .totalActualIncome(totalActualIncome)
                .effectiveIncome(effectiveIncome)
                .isIncomeFromActual(isIncomeFromActual)
                .totalPlannedExpense(totalPlannedExpense)
                .expectedSurplus(expectedSurplus)
                .totalActualExpense(totalActualExpense)
                .actualSurplus(actualSurplus)
                .overallSpentPercentage(overallSpentPercentage)
                .planNote(planNote)
                .totalCurrentWalletBalance(totalCurrentWalletBalance)
                .projectedTotalWalletBalance(projectedTotalWalletBalance)
                .accountForecasts(accountForecasts)
                .items(items)
                .totalGoalsRequiredMonthly(totalGoalsRequiredMonthly)
                .goalsOverallStatus(goalsOverallStatus)
                .goalAssessments(goalAssessments)
                .build();
    }

    @Transactional
    public FinancialMonthlyPlan saveMonthlyPlan(SaveMonthlyPlanRequest request) {
        FinancialMonthlyPlan plan = planRepository.findByMonthAndYear(request.getMonth(), request.getYear())
                .orElse(FinancialMonthlyPlan.builder()
                        .month(request.getMonth())
                        .year(request.getYear())
                        .build());

        plan.setExpectedIncome(request.getExpectedIncome() != null ? request.getExpectedIncome() : BigDecimal.ZERO);
        plan.setAccountId(request.getAccountId());
        plan.setNote(request.getNote());
        return planRepository.save(plan);
    }

    @Transactional
    public void deleteMonthlyPlan(Integer month, Integer year) {
        planRepository.findByMonthAndYear(month, year).ifPresent(planRepository::delete);
    }

    @Transactional
    public FinancialBudget saveBudgetItem(SaveBudgetItemRequest request) {
        FinancialCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        FinancialBudget budget = budgetRepository.findByMonthAndYearAndCategoryId(
                request.getMonth(), request.getYear(), request.getCategoryId())
                .orElse(null);

        if (budget == null) {
            if (request.getId() != null) {
                budget = budgetRepository.findById(request.getId())
                        .orElse(null);
            }
            if (budget == null) {
                budget = FinancialBudget.builder()
                        .month(request.getMonth())
                        .year(request.getYear())
                        .categoryId(request.getCategoryId())
                        .build();
            }
        }

        budget.setAccountId(request.getAccountId());
        budget.setPlannedAmount(request.getPlannedAmount() != null ? request.getPlannedAmount() : BigDecimal.ZERO);
        budget.setNote(request.getNote());
        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudgetItem(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy khoản dự toán với ID: " + id);
        }
        budgetRepository.deleteById(id);
    }

    @Transactional
    public MonthlyBudgetPlanResponse batchSaveBudget(BatchSaveBudgetRequest request) {
        // 1. Save or update monthly plan
        FinancialMonthlyPlan plan = planRepository.findByMonthAndYear(request.getMonth(), request.getYear())
                .orElse(FinancialMonthlyPlan.builder()
                        .month(request.getMonth())
                        .year(request.getYear())
                        .build());
        if (request.getExpectedIncome() != null) {
            plan.setExpectedIncome(request.getExpectedIncome());
        }
        if (request.getAccountId() != null) {
            plan.setAccountId(request.getAccountId());
        }
        if (request.getNote() != null) {
            plan.setNote(request.getNote());
        }
        planRepository.save(plan);

        // 2. Save items if provided
        if (request.getItems() != null) {
            for (SaveBudgetItemRequest item : request.getItems()) {
                item.setMonth(request.getMonth());
                item.setYear(request.getYear());
                saveBudgetItem(item);
            }
        }

        return getMonthlyBudgetPlan(request.getMonth(), request.getYear());
    }
}
