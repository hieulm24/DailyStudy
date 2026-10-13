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
import java.util.stream.Collectors;

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

        List<Object[]> catAndAccBreakdown = transactionRepository.getCategoryAndAccountExpenseBreakdown(startOfMonth,
                endOfMonth);
        Map<String, BigDecimal> actualByCatAndAcc = new HashMap<>();
        for (Object[] row : catAndAccBreakdown) {
            Long catId = (Long) row[0];
            Long accId = (Long) row[1];
            BigDecimal spent = (BigDecimal) row[2];
            if (catId != null && spent != null) {
                actualByCatAndAcc.put(catId + "_" + (accId != null ? accId : 0), spent);
            }
        }

        // Lấy danh sách tất cả tài khoản để tra cứu tên và loại ví
        Map<Long, FinancialAccount> allAccountMap = accountRepository.findAll().stream()
                .collect(Collectors.toMap(FinancialAccount::getId, a -> a));

        // Gom các khoản dự chi theo danh mục
        Map<Long, List<FinancialBudget>> budgetsByCategory = new LinkedHashMap<>();
        for (FinancialBudget b : budgets) {
            budgetsByCategory.computeIfAbsent(b.getCategoryId(), k -> new ArrayList<>()).add(b);
        }

        // 4. Build items response kèm bóc tách chi tiêu theo từng ví (spentBreakdown)
        BigDecimal totalPlannedExpense = BigDecimal.ZERO;
        Map<Long, BigDecimal> plannedExpensePerAccount = new HashMap<>();
        List<BudgetItemResponse> items = new ArrayList<>();

        for (FinancialBudget b : budgets) {
            BigDecimal planned = (b.getPlannedAmount() != null) ? b.getPlannedAmount() : BigDecimal.ZERO;
            totalPlannedExpense = totalPlannedExpense.add(planned);

            // Phân tích danh sách accountIds của khoản dự chi này
            List<Long> accIdList = new ArrayList<>();
            if (b.getAccountIds() != null && !b.getAccountIds().trim().isEmpty()) {
                Arrays.stream(b.getAccountIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .forEach(accIdList::add);
            } else if (b.getAccountId() != null) {
                accIdList.add(b.getAccountId());
            }

            // Ghi nhận dự chi cho từng ví (phân bổ đều nếu chọn kết hợp nhiều ví)
            if (accIdList.size() == 1) {
                plannedExpensePerAccount.merge(accIdList.get(0), planned, BigDecimal::add);
            } else if (accIdList.size() > 1) {
                BigDecimal share = planned.divide(BigDecimal.valueOf(accIdList.size()), 2, RoundingMode.HALF_UP);
                for (Long aId : accIdList) {
                    plannedExpensePerAccount.merge(aId, share, BigDecimal::add);
                }
            }

            // Đặt tên hiển thị nguồn tiền: ví dụ "Vietcombank + Tiền mặt"
            String accountName;
            String accountType;
            if (accIdList.isEmpty()) {
                accountName = "Linh hoạt mọi ví";
                accountType = "ALL";
            } else if (accIdList.size() == 1) {
                FinancialAccount a = allAccountMap.get(accIdList.get(0));
                accountName = (a != null) ? a.getName() : "Ví #" + accIdList.get(0);
                accountType = (a != null) ? a.getAccountType() : null;
            } else {
                List<String> names = accIdList.stream()
                        .map(allAccountMap::get)
                        .filter(Objects::nonNull)
                        .map(FinancialAccount::getName)
                        .collect(Collectors.toList());
                accountName = String.join(" + ", names);
                accountType = "COMBINED";
            }

            // Bóc tách chi tiêu thực tế cho khoản này theo từng ví (spentBreakdown)
            List<AccountSpentBreakdown> breakdownList = new ArrayList<>();
            BigDecimal actual = BigDecimal.ZERO;

            List<FinancialBudget> catBudgets = budgetsByCategory.getOrDefault(b.getCategoryId(), Collections.emptyList());

            if (!accIdList.isEmpty()) {
                for (Long aId : accIdList) {
                    BigDecimal sp = actualByCatAndAcc.getOrDefault(b.getCategoryId() + "_" + aId, BigDecimal.ZERO);
                    if (sp != null && sp.compareTo(BigDecimal.ZERO) > 0) {
                        FinancialAccount a = allAccountMap.get(aId);
                        breakdownList.add(AccountSpentBreakdown.builder()
                                .accountId(aId)
                                .accountName(a != null ? a.getName() : "Ví #" + aId)
                                .accountType(a != null ? a.getAccountType() : null)
                                .amount(sp)
                                .build());
                        actual = actual.add(sp);
                    }
                }
            } else {
                // Trường hợp linh hoạt: lấy tất cả các ví đã chi tiêu cho danh mục này
                for (Object[] row : catAndAccBreakdown) {
                    Long cId = (Long) row[0];
                    Long aId = (Long) row[1];
                    BigDecimal sp = (BigDecimal) row[2];
                    if (b.getCategoryId().equals(cId) && sp != null && sp.compareTo(BigDecimal.ZERO) > 0) {
                        FinancialAccount a = allAccountMap.get(aId);
                        breakdownList.add(AccountSpentBreakdown.builder()
                                .accountId(aId)
                                .accountName(a != null ? a.getName() : "Ví #" + aId)
                                .accountType(a != null ? a.getAccountType() : null)
                                .amount(sp)
                                .build());
                        actual = actual.add(sp);
                    }
                }
            }

            // Nếu đây là khoản dự chi duy nhất của danh mục này, nhưng người dùng có chi từ ví khác chưa liệt kê:
            // gom thêm vào để không bị sót chi tiêu thực tế của danh mục!
            if (catBudgets.size() == 1) {
                BigDecimal totalCatSpent = actualExpenseMap.getOrDefault(b.getCategoryId(), BigDecimal.ZERO);
                if (totalCatSpent.compareTo(actual) > 0) {
                    for (Object[] row : catAndAccBreakdown) {
                        Long cId = (Long) row[0];
                        Long aId = (Long) row[1];
                        BigDecimal sp = (BigDecimal) row[2];
                        if (b.getCategoryId().equals(cId) && !accIdList.contains(aId) && sp != null && sp.compareTo(BigDecimal.ZERO) > 0) {
                            FinancialAccount a = allAccountMap.get(aId);
                            breakdownList.add(AccountSpentBreakdown.builder()
                                    .accountId(aId)
                                    .accountName(a != null ? a.getName() : "Ví #" + aId)
                                    .accountType(a != null ? a.getAccountType() : null)
                                    .amount(sp)
                                    .build());
                            actual = actual.add(sp);
                        }
                    }
                }
            }

            BigDecimal remaining = planned.subtract(actual);

            double percentage = 0.0;
            if (planned.compareTo(BigDecimal.ZERO) > 0) {
                percentage = actual.divide(planned, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            boolean isExceeded = actual.compareTo(planned) > 0;

            FinancialCategory cat = b.getCategory();

            items.add(BudgetItemResponse.builder()
                    .id(b.getId())
                    .month(b.getMonth())
                    .year(b.getYear())
                    .categoryId(b.getCategoryId())
                    .categoryName(cat != null ? cat.getName() : "Chưa phân loại")
                    .categoryIcon(cat != null ? cat.getIcon() : "Tag")
                    .categoryColor(cat != null ? cat.getColor() : "#64748b")
                    .accountId(b.getAccountId())
                    .accountIds(accIdList)
                    .accountName(accountName)
                    .accountType(accountType)
                    .plannedAmount(planned)
                    .actualSpent(actual)
                    .remainingAmount(remaining)
                    .percentageSpent(percentage)
                    .isExceeded(isExceeded)
                    .spentBreakdown(breakdownList)
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
        // - If expectedIncome == 0 (current month with real transactions), smartly fall
        // back to totalActualIncome!
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

            // Actual spent and actual income from this account in the current month
            BigDecimal actualSpentForAcc = transactionRepository.sumExpenseForAccountBetween(acc.getId(), startOfMonth,
                    endOfMonth);
            if (actualSpentForAcc == null) {
                actualSpentForAcc = BigDecimal.ZERO;
            }

            BigDecimal actualIncomeForAcc = transactionRepository.sumIncomeForAccountBetween(acc.getId(), startOfMonth,
                    endOfMonth);
            if (actualIncomeForAcc == null) {
                actualIncomeForAcc = BigDecimal.ZERO;
            }

            BigDecimal incomingTransfers = transactionRepository.sumIncomingTransferForAccountBetween(acc.getId(), startOfMonth,
                    endOfMonth);
            if (incomingTransfers == null) {
                incomingTransfers = BigDecimal.ZERO;
            }

            BigDecimal outgoingTransfers = transactionRepository.sumOutgoingTransferForAccountBetween(acc.getId(), startOfMonth,
                    endOfMonth);
            if (outgoingTransfers == null) {
                outgoingTransfers = BigDecimal.ZERO;
            }

            // Remaining planned expense: only what hasn't been spent yet from this wallet
            BigDecimal remainingPlannedExpense = plannedExpense.compareTo(actualSpentForAcc) > 0
                    ? plannedExpense.subtract(actualSpentForAcc)
                    : BigDecimal.ZERO;

            // Remaining planned income: only what hasn't been received yet into this wallet
            BigDecimal remainingPlannedIncome = plannedIncome.compareTo(actualIncomeForAcc) > 0
                    ? plannedIncome.subtract(actualIncomeForAcc)
                    : BigDecimal.ZERO;

            // Projected balance at end of month = currentBalance + remainingPlannedIncome -
            // remainingPlannedExpense
            BigDecimal projectedBal = currentBal.add(remainingPlannedIncome).subtract(remainingPlannedExpense);
            boolean isDeficit = projectedBal.compareTo(BigDecimal.ZERO) < 0;

            accountForecasts.add(AccountBudgetForecast.builder()
                    .accountId(acc.getId())
                    .accountName(acc.getName())
                    .accountType(acc.getAccountType())
                    .currentBalance(currentBal)
                    .plannedExpense(plannedExpense)
                    .plannedIncome(plannedIncome)
                    .actualSpent(actualSpentForAcc)
                    .remainingPlannedExpense(remainingPlannedExpense)
                    .actualIncome(actualIncomeForAcc)
                    .remainingPlannedIncome(remainingPlannedIncome)
                    .incomingTransfers(incomingTransfers)
                    .outgoingTransfers(outgoingTransfers)
                    .projectedBalance(projectedBal)
                    .isDeficit(isDeficit)
                    .build());
        }

        BigDecimal projectedTotalWalletBalance = accountForecasts.stream()
                .map(AccountBudgetForecast::getProjectedBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (expectedIncomeAccountId == null && expectedIncome != null
                && expectedIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remainingUnassignedIncome = expectedIncome.compareTo(totalActualIncome) > 0
                    ? expectedIncome.subtract(totalActualIncome)
                    : BigDecimal.ZERO;
            projectedTotalWalletBalance = projectedTotalWalletBalance.add(remainingUnassignedIncome);
        }

        // 7. Goal Feasibility Assessment (Đối soát với Mục tiêu tài chính)
        List<GoalResponse> activeGoals = goalService.getAllGoals("ACTIVE");
        BigDecimal totalGoalsRequiredMonthly = BigDecimal.ZERO;
        List<GoalBudgetAssessment> goalAssessments = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("#,###");

        for (GoalResponse g : activeGoals) {
            BigDecimal targetAmount = g.getTargetAmount() != null ? g.getTargetAmount() : BigDecimal.ZERO;
            BigDecimal currentAmount = g.getCurrentAmount() != null ? g.getCurrentAmount() : BigDecimal.ZERO;
            BigDecimal remainingAmount = g.getRemainingAmount() != null ? g.getRemainingAmount()
                    : targetAmount.subtract(currentAmount);

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
                    assessmentNote = "Dự kiến dư +" + df.format(expectedSurplus)
                            + " ₫, đủ tiền tích lũy và còn dư thêm +" + df.format(diff) + " ₫.";
                } else {
                    assessmentNote = "Vừa vặn đủ chỉ tiêu tích lũy (" + df.format(reqSavings) + " ₫) cho mục tiêu này.";
                }
            } else {
                if (expectedSurplus.compareTo(BigDecimal.ZERO) > 0) {
                    assessmentNote = "Chỉ dư +" + df.format(expectedSurplus) + " ₫, bị thiếu hụt "
                            + df.format(diff.abs()) + " ₫ so với tiến độ chuẩn (" + df.format(reqSavings)
                            + " ₫/tháng).";
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
        List<Long> accIds = new ArrayList<>();
        if (request.getAccountIds() != null && !request.getAccountIds().isEmpty()) {
            accIds.addAll(request.getAccountIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList()));
        } else if (request.getAccountId() != null) {
            accIds.add(request.getAccountId());
        }

        if (accIds.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ví thanh toán cho khoản dự chi");
        }

        BigDecimal totalCurrentBalance = BigDecimal.ZERO;
        List<String> accountNames = new ArrayList<>();
        for (Long aId : accIds) {
            AccountResponse accountResp = accountService.getAccountById(aId);
            if (accountResp != null) {
                accountNames.add(accountResp.getName());
                if (accountResp.getCurrentBalance() != null) {
                    totalCurrentBalance = totalCurrentBalance.add(accountResp.getCurrentBalance());
                }
            }
        }
        String combinedName = String.join(" + ", accountNames);

        // Tính tổng dự chi đã lên kế hoạch cho các ví này trong tháng (loại trừ item hiện tại nếu đang chỉnh sửa)
        List<FinancialBudget> monthBudgets = budgetRepository.findByMonthAndYear(request.getMonth(), request.getYear());
        BigDecimal otherPlannedForAccount = monthBudgets.stream()
                .filter(b -> request.getId() == null || !request.getId().equals(b.getId()))
                .filter(b -> {
                    List<Long> bAccIds = new ArrayList<>();
                    if (b.getAccountIds() != null && !b.getAccountIds().trim().isEmpty()) {
                        Arrays.stream(b.getAccountIds().split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(Long::parseLong)
                                .forEach(bAccIds::add);
                    } else if (b.getAccountId() != null) {
                        bAccIds.add(b.getAccountId());
                    }
                    return bAccIds.stream().anyMatch(accIds::contains);
                })
                .map(b -> b.getPlannedAmount() != null ? b.getPlannedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal plannedAmount = request.getPlannedAmount() != null ? request.getPlannedAmount() : BigDecimal.ZERO;
        BigDecimal newTotalPlanned = otherPlannedForAccount.add(plannedAmount);

        if (newTotalPlanned.compareTo(totalCurrentBalance) > 0) {
            DecimalFormat df = new DecimalFormat("#,###");
            String curBalStr = df.format(totalCurrentBalance) + " ₫";
            String newTotalStr = df.format(newTotalPlanned) + " ₫";

            if (totalCurrentBalance.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(String.format(
                        "Ví '%s' hiện không có tiền (Số dư: %s), không thể lập khoản dự chi!",
                        combinedName, curBalStr));
            } else {
                throw new IllegalArgumentException(String.format(
                        "Số dư ví '%s' không đủ! Hiện có %s, nhưng tổng dự kiến chi từ nguồn này sẽ là %s.",
                        combinedName, curBalStr, newTotalStr));
            }
        }

        FinancialCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        FinancialBudget budget;
        if (request.getId() != null) {
            // Trường hợp cập nhật khoản dự chi hiện có theo ID
            budget = budgetRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy khoản dự chi với ID: " + request.getId()));
            budget.setCategoryId(request.getCategoryId());
        } else {
            // Trường hợp thêm mới khoản dự chi: Tạo mới độc lập (cho phép tạo nhiều khoản cùng 1 danh mục)
            budget = FinancialBudget.builder()
                    .month(request.getMonth())
                    .year(request.getYear())
                    .categoryId(request.getCategoryId())
                    .build();
        }

        budget.setAccountId(accIds.get(0));
        budget.setAccountIds(accIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
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
