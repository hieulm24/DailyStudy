import api from './api';
import type { ApiResponse } from '../types';
import type {
  NutritionFoodSearchItem,
  NutritionDailySummary,
  NutritionDailyLogItem,
  NutritionDailyLogRequest,
  NutritionRecentFood,
  NutritionUserFoodRequest,
  NutritionTargetSettings,
  UsdaFoodItem,
} from '../types/nutrition';

const TARGET_STORAGE_KEY = 'daily_study_nutrition_target';

export const nutritionService = {
  async searchFoods(keyword?: string): Promise<NutritionFoodSearchItem[]> {
    const res = await api.get<ApiResponse<NutritionFoodSearchItem[]>>('/nutrition/foods/search', {
      params: { keyword: keyword || '' },
    });
    return res.data.data;
  },

  async searchUsdaFoods(query: string, pageSize = 10): Promise<UsdaFoodItem[]> {
    const res = await api.get<ApiResponse<UsdaFoodItem[]>>('/nutrition/usda/search', {
      params: { query, pageSize },
    });
    return res.data.data;
  },

  async importUsdaFood(fdcId: number): Promise<NutritionFoodSearchItem> {
    const res = await api.post<ApiResponse<NutritionFoodSearchItem>>(`/nutrition/usda/import/${fdcId}`);
    return res.data.data;
  },

  async getFoodDetail(id: number): Promise<NutritionFoodSearchItem> {
    const res = await api.get<ApiResponse<NutritionFoodSearchItem>>(`/nutrition/foods/${id}`);
    return res.data.data;
  },

  async getDailySummary(date?: string): Promise<NutritionDailySummary> {
    const res = await api.get<ApiResponse<NutritionDailySummary>>('/nutrition/daily', {
      params: { date },
    });
    return res.data.data;
  },

  async addDailyLog(data: NutritionDailyLogRequest): Promise<NutritionDailyLogItem> {
    const res = await api.post<ApiResponse<NutritionDailyLogItem>>('/nutrition/daily', data);
    return res.data.data;
  },

  async updateDailyLog(id: number, data: NutritionDailyLogRequest): Promise<NutritionDailyLogItem> {
    const res = await api.put<ApiResponse<NutritionDailyLogItem>>(`/nutrition/daily/${id}`, data);
    return res.data.data;
  },

  async deleteDailyLog(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/nutrition/daily/${id}`);
  },

  async copyDayLogs(fromDate: string, toDate: string): Promise<NutritionDailySummary> {
    const res = await api.post<ApiResponse<NutritionDailySummary>>('/nutrition/daily/copy', null, {
      params: { fromDate, toDate },
    });
    return res.data.data;
  },

  async getRecentFoods(): Promise<NutritionRecentFood[]> {
    const res = await api.get<ApiResponse<NutritionRecentFood[]>>('/nutrition/recent-foods');
    return res.data.data;
  },

  async getUserFoods(): Promise<NutritionFoodSearchItem[]> {
    const res = await api.get<ApiResponse<NutritionFoodSearchItem[]>>('/nutrition/user-foods');
    return res.data.data;
  },

  async createUserFood(data: NutritionUserFoodRequest): Promise<void> {
    await api.post<ApiResponse<any>>('/nutrition/user-foods', data);
  },

  async deleteUserFood(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/nutrition/user-foods/${id}`);
  },

  // Activities & Calorie Deficit
  async getActivities(): Promise<import('../types/nutrition').NutritionActivity[]> {
    const res = await api.get<ApiResponse<import('../types/nutrition').NutritionActivity[]>>('/nutrition/activities');
    return res.data.data;
  },

  async getActivityLogs(date?: string): Promise<import('../types/nutrition').NutritionActivityLog[]> {
    const res = await api.get<ApiResponse<import('../types/nutrition').NutritionActivityLog[]>>('/nutrition/activity-logs', {
      params: { date },
    });
    return res.data.data;
  },

  async addActivityLog(data: import('../types/nutrition').NutritionActivityLogRequest): Promise<import('../types/nutrition').NutritionActivityLog> {
    const res = await api.post<ApiResponse<import('../types/nutrition').NutritionActivityLog>>('/nutrition/activity-logs', data);
    return res.data.data;
  },

  async updateActivityLog(id: number, data: import('../types/nutrition').NutritionActivityLogRequest): Promise<import('../types/nutrition').NutritionActivityLog> {
    const res = await api.put<ApiResponse<import('../types/nutrition').NutritionActivityLog>>(`/nutrition/activity-logs/${id}`, data);
    return res.data.data;
  },

  async deleteActivityLog(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/nutrition/activity-logs/${id}`);
  },

  async getDeficitSummary(date?: string, tdee?: number, weight?: number): Promise<import('../types/nutrition').CalorieDeficitSummary> {
    const res = await api.get<ApiResponse<import('../types/nutrition').CalorieDeficitSummary>>('/nutrition/deficit-summary', {
      params: { date, tdee, weight },
    });
    return res.data.data;
  },

  async calculateDeficit(data: import('../types/nutrition').CalorieDeficitCalculationRequest): Promise<import('../types/nutrition').CalorieDeficitSummary> {
    const res = await api.post<ApiResponse<import('../types/nutrition').CalorieDeficitSummary>>('/nutrition/calories/calculate', data);
    return res.data.data;
  },

  async getStatistics(params?: {
    range?: string;
    fromDate?: string;
    toDate?: string;
    tdee?: number;
    weight?: number;
  }): Promise<import('../types/nutrition').NutritionStatisticsResponse> {
    const res = await api.get<ApiResponse<import('../types/nutrition').NutritionStatisticsResponse>>('/nutrition/statistics', {
      params,
    });
    return res.data.data;
  },

  // Target Settings (persisted in localStorage per requirement 15)

  getTargetSettings(): NutritionTargetSettings {
    const saved = localStorage.getItem(TARGET_STORAGE_KEY);
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        console.error('Failed to parse nutrition targets:', e);
      }
    }
    return {
      weight: 68,
      height: 172,
      targetCalories: 2100,
      targetProtein: 130,
      targetCarb: 250,
      targetFat: 60,
    };
  },

  saveTargetSettings(settings: NutritionTargetSettings): void {
    localStorage.setItem(TARGET_STORAGE_KEY, JSON.stringify(settings));
  },
};

