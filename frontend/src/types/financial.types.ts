export interface FinancialAccount {
  id: number;
  name: string;
  accountType: 'CASH' | 'BANK' | 'EWALLET' | 'CREDIT' | 'OTHER' | string;
  initialBalance: number;
  isActive: boolean;
  createdAt: string;
  updatedAt?: string;
  currentBalance: number;
  allocatedToGoal: number;
  availableBalance: number;
}

export interface FinancialAccountRequest {
  name: string;
  accountType: string;
  initialBalance: number;
  isActive?: boolean;
}

export interface FinancialCategory {
  id: number;
  name: string;
  type: 'INCOME' | 'EXPENSE';
  icon?: string;
  color?: string;
  isDefault: boolean;
  isActive: boolean;
  createdAt: string;
  updatedAt?: string;
  transactionCount?: number;
  totalAmount?: number;
}

export interface FinancialCategoryRequest {
  name: string;
  type: 'INCOME' | 'EXPENSE';
  icon?: string;
  color?: string;
  isDefault?: boolean;
  isActive?: boolean;
}

export interface CategorySpendingStat {
  categoryId: number;
  categoryName: string;
  icon?: string;
  color?: string;
  totalAmount: number;
  percentage: number;
  count: number;
}

export interface FinancialTransaction {
  id: number;
  accountId: number;
  accountName?: string;
  accountType?: string;
  categoryId?: number;
  categoryName?: string;
  categoryIcon?: string;
  categoryColor?: string;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  amount: number;
  transactionDate: string;
  description?: string;
  note?: string;
  toAccountId?: number;
  toAccountName?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface FinancialTransactionRequest {
  accountId: number;
  categoryId?: number | null;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  amount: number;
  transactionDate: string;
  description?: string;
  note?: string;
  toAccountId?: number | null;
}

export interface TransactionFilter {
  search?: string;
  type?: string;
  accountId?: number;
  categoryId?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}

export interface FinancialGoal {
  id: number;
  name: string;
  description?: string;
  targetAmount: number;
  startDate: string;
  targetDate?: string;
  status: 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'CANCELLED';
  icon?: string;
  color?: string;
  createdAt: string;
  updatedAt?: string;
  currentAmount: number;
  remainingAmount: number;
  progressPercentage: number;
  monthlySavingsNeeded?: number;
  estimatedCompletionDate?: string;
  planStatusNote?: string;
  contributionCount: number;
}

export interface FinancialGoalRequest {
  name: string;
  description?: string;
  targetAmount: number;
  startDate: string;
  targetDate?: string | null;
  status?: string;
  icon?: string;
  color?: string;
}

export interface FinancialGoalContribution {
  id: number;
  goalId: number;
  goalName?: string;
  accountId: number;
  accountName?: string;
  transactionId?: number;
  type: 'DEPOSIT' | 'WITHDRAW';
  amount: number;
  contributionDate: string;
  note?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface FinancialGoalContributionRequest {
  goalId: number;
  accountId: number;
  transactionId?: number | null;
  type: 'DEPOSIT' | 'WITHDRAW';
  amount: number;
  contributionDate: string;
  note?: string;
}

export interface MonthlyTrendPoint {
  period: string;
  label: string;
  income: number;
  expense: number;
  net: number;
}

export interface BalanceTrendPoint {
  date: string;
  balance: number;
  income: number;
  expense: number;
}

export interface FinancialDashboardData {
  monthlyIncome: number;
  monthlyExpense: number;
  monthlyNetBalance: number;
  totalAccountBalance: number;
  totalAllocatedToGoals: number;
  categoryExpenses: CategorySpendingStat[];
  monthlyTrends: MonthlyTrendPoint[];
  recentTransactions: FinancialTransaction[];
  goalsSummary: FinancialGoal[];
}

export interface FinancialReportData {
  totalIncome: number;
  totalExpense: number;
  netSavings: number;
  expenseByCategory: CategorySpendingStat[];
  incomeByCategory: CategorySpendingStat[];
  monthlyComparison: MonthlyTrendPoint[];
  balanceTrend: BalanceTrendPoint[];
  goalsReport: FinancialGoal[];
}

export interface FinancialBudgetItem {
  id: number;
  month: number;
  year: number;
  categoryId: number;
  categoryName: string;
  categoryIcon?: string;
  categoryColor?: string;
  accountId?: number;
  accountName?: string;
  accountType?: string;
  plannedAmount: number;
  actualSpent: number;
  remainingAmount: number;
  percentageSpent: number;
  isExceeded: boolean;
  note?: string;
}

export interface AccountBudgetForecast {
  accountId: number;
  accountName: string;
  accountType?: string;
  currentBalance: number;
  plannedExpense: number;
  plannedIncome: number;
  projectedBalance: number;
  isDeficit: boolean;
  actualSpent: number;
}

export interface GoalBudgetAssessment {
  goalId: number;
  goalName: string;
  goalIcon?: string;
  goalColor?: string;
  targetAmount: number;
  currentAmount: number;
  remainingAmount: number;
  targetDate?: string;
  monthsRemaining?: number;
  requiredMonthlySavings: number;
  projectedMonthlySurplus: number;
  isSufficient: boolean;
  surplusOrDeficit: number;
  estimatedMonthsToComplete?: number;
  assessmentNote: string;
}

export interface MonthlyBudgetPlan {
  month: number;
  year: number;
  expectedIncome: number;
  expectedIncomeAccountId?: number;
  expectedIncomeAccountName?: string;
  totalActualIncome?: number;
  effectiveIncome?: number;
  isIncomeFromActual?: boolean;
  totalPlannedExpense: number;
  expectedSurplus: number;
  totalActualExpense: number;
  actualSurplus: number;
  overallSpentPercentage: number;
  planNote?: string;
  totalCurrentWalletBalance?: number;
  projectedTotalWalletBalance?: number;
  accountForecasts?: AccountBudgetForecast[];
  items: FinancialBudgetItem[];
  totalGoalsRequiredMonthly?: number;
  goalsOverallStatus?: 'SUFFICIENT' | 'DEFICIT' | 'NO_GOALS' | string;
  goalAssessments?: GoalBudgetAssessment[];
}

export interface SaveMonthlyPlanRequest {
  month: number;
  year: number;
  expectedIncome: number;
  accountId?: number | null;
  note?: string;
}

export interface SaveBudgetItemRequest {
  id?: number;
  month: number;
  year: number;
  categoryId: number;
  accountId?: number | null;
  plannedAmount: number;
  note?: string;
}

