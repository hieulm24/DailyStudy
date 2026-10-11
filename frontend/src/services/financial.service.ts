import api from './api';
import type {
  ApiResponse,
  PageResponse,
  FinancialAccount,
  FinancialAccountRequest,
  FinancialCategory,
  FinancialCategoryRequest,
  FinancialTransaction,
  FinancialTransactionRequest,
  TransactionFilter,
  FinancialGoal,
  FinancialGoalRequest,
  FinancialGoalContribution,
  FinancialGoalContributionRequest,
  FinancialDashboardData,
  FinancialReportData,
  MonthlyBudgetPlan,
  FinancialBudgetItem,
  SaveMonthlyPlanRequest,
  SaveBudgetItemRequest,
} from '../types';

export const financialService = {
  // ==========================================
  // ACCOUNTS
  // ==========================================
  async getAccounts(activeOnly = false): Promise<FinancialAccount[]> {
    const res = await api.get<ApiResponse<FinancialAccount[]>>('/financial/accounts', {
      params: { activeOnly },
    });
    return res.data.data;
  },

  async getAccountById(id: number): Promise<FinancialAccount> {
    const res = await api.get<ApiResponse<FinancialAccount>>(`/financial/accounts/${id}`);
    return res.data.data;
  },

  async createAccount(data: FinancialAccountRequest): Promise<FinancialAccount> {
    const res = await api.post<ApiResponse<FinancialAccount>>('/financial/accounts', data);
    return res.data.data;
  },

  async updateAccount(id: number, data: FinancialAccountRequest): Promise<FinancialAccount> {
    const res = await api.put<ApiResponse<FinancialAccount>>(`/financial/accounts/${id}`, data);
    return res.data.data;
  },

  async toggleAccount(id: number): Promise<FinancialAccount> {
    const res = await api.patch<ApiResponse<FinancialAccount>>(`/financial/accounts/${id}/toggle`);
    return res.data.data;
  },

  async deleteAccount(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/financial/accounts/${id}`);
  },

  // ==========================================
  // CATEGORIES
  // ==========================================
  async getCategories(type?: string, activeOnly = false): Promise<FinancialCategory[]> {
    const res = await api.get<ApiResponse<FinancialCategory[]>>('/financial/categories', {
      params: { type, activeOnly },
    });
    return res.data.data;
  },

  async getCategoryById(id: number): Promise<FinancialCategory> {
    const res = await api.get<ApiResponse<FinancialCategory>>(`/financial/categories/${id}`);
    return res.data.data;
  },

  async createCategory(data: FinancialCategoryRequest): Promise<FinancialCategory> {
    const res = await api.post<ApiResponse<FinancialCategory>>('/financial/categories', data);
    return res.data.data;
  },

  async updateCategory(id: number, data: FinancialCategoryRequest): Promise<FinancialCategory> {
    const res = await api.put<ApiResponse<FinancialCategory>>(`/financial/categories/${id}`, data);
    return res.data.data;
  },

  async toggleCategory(id: number): Promise<FinancialCategory> {
    const res = await api.patch<ApiResponse<FinancialCategory>>(`/financial/categories/${id}/toggle`);
    return res.data.data;
  },

  async deleteCategory(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/financial/categories/${id}`);
  },

  // ==========================================
  // TRANSACTIONS
  // ==========================================
  async getTransactions(filter?: TransactionFilter): Promise<PageResponse<FinancialTransaction>> {
    const res = await api.get<ApiResponse<PageResponse<FinancialTransaction>>>('/financial/transactions', {
      params: filter,
    });
    return res.data.data;
  },

  async getTransactionById(id: number): Promise<FinancialTransaction> {
    const res = await api.get<ApiResponse<FinancialTransaction>>(`/financial/transactions/${id}`);
    return res.data.data;
  },

  async createTransaction(data: FinancialTransactionRequest): Promise<FinancialTransaction> {
    const res = await api.post<ApiResponse<FinancialTransaction>>('/financial/transactions', data);
    return res.data.data;
  },

  async updateTransaction(id: number, data: FinancialTransactionRequest): Promise<FinancialTransaction> {
    const res = await api.put<ApiResponse<FinancialTransaction>>(`/financial/transactions/${id}`, data);
    return res.data.data;
  },

  async deleteTransaction(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/financial/transactions/${id}`);
  },

  // ==========================================
  // GOALS & CONTRIBUTIONS
  // ==========================================
  async getGoals(status?: string): Promise<FinancialGoal[]> {
    const res = await api.get<ApiResponse<FinancialGoal[]>>('/financial/goals', {
      params: { status },
    });
    return res.data.data;
  },

  async getGoalById(id: number): Promise<FinancialGoal> {
    const res = await api.get<ApiResponse<FinancialGoal>>(`/financial/goals/${id}`);
    return res.data.data;
  },

  async createGoal(data: FinancialGoalRequest): Promise<FinancialGoal> {
    const res = await api.post<ApiResponse<FinancialGoal>>('/financial/goals', data);
    return res.data.data;
  },

  async updateGoal(id: number, data: FinancialGoalRequest): Promise<FinancialGoal> {
    const res = await api.put<ApiResponse<FinancialGoal>>(`/financial/goals/${id}`, data);
    return res.data.data;
  },

  async updateGoalStatus(id: number, status: string): Promise<FinancialGoal> {
    const res = await api.patch<ApiResponse<FinancialGoal>>(`/financial/goals/${id}/status`, null, {
      params: { status },
    });
    return res.data.data;
  },

  async deleteGoal(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/financial/goals/${id}`);
  },

  async getGoalContributions(goalId: number): Promise<FinancialGoalContribution[]> {
    const res = await api.get<ApiResponse<FinancialGoalContribution[]>>(`/financial/goals/${goalId}/contributions`);
    return res.data.data;
  },

  async addGoalContribution(data: FinancialGoalContributionRequest): Promise<FinancialGoalContribution> {
    const res = await api.post<ApiResponse<FinancialGoalContribution>>('/financial/goals/contributions', data);
    return res.data.data;
  },

  async updateGoalContribution(contributionId: number, data: FinancialGoalContributionRequest): Promise<FinancialGoalContribution> {
    const res = await api.put<ApiResponse<FinancialGoalContribution>>(`/financial/goals/contributions/${contributionId}`, data);
    return res.data.data;
  },

  async deleteGoalContribution(contributionId: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/financial/goals/contributions/${contributionId}`);
  },

  // ==========================================
  // DASHBOARD & REPORTS
  // ==========================================
  async getDashboard(params?: { year?: number; month?: number; startDate?: string; endDate?: string }): Promise<FinancialDashboardData> {
    const res = await api.get<ApiResponse<FinancialDashboardData>>('/financial/dashboard', { params });
    return res.data.data;
  },

  async getReports(params?: { year?: number; month?: number; startDate?: string; endDate?: string }): Promise<FinancialReportData> {
    const res = await api.get<ApiResponse<FinancialReportData>>('/financial/reports', { params });
    return res.data.data;
  },

  // ==========================================
  // BUDGET & MONTHLY PLANNING
  // ==========================================
  async getBudgetPlan(month?: number, year?: number): Promise<MonthlyBudgetPlan> {
    const res = await api.get<ApiResponse<MonthlyBudgetPlan>>('/financial/budget-plan', {
      params: { month, year },
    });
    return res.data.data;
  },

  async saveMonthlyPlan(data: SaveMonthlyPlanRequest): Promise<any> {
    const res = await api.post<ApiResponse<any>>('/financial/budget-plan/plan', data);
    return res.data.data;
  },

  async saveBudgetItem(data: SaveBudgetItemRequest): Promise<FinancialBudgetItem> {
    const res = await api.post<ApiResponse<FinancialBudgetItem>>('/financial/budget-plan/items', data);
    return res.data.data;
  },

  async deleteBudgetItem(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/financial/budget-plan/items/${id}`);
  },

  async deleteMonthlyPlan(month: number, year: number): Promise<void> {
    await api.delete<ApiResponse<void>>('/financial/budget-plan/plan', {
      params: { month, year },
    });
  },
};

