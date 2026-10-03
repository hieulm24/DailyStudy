import api from './api';
import type { Activity, ApiResponse, ChartData, DashboardSummary, StreakHeatmap } from '../types';

export const statisticsService = {
  async getDashboardSummary(): Promise<DashboardSummary> {
    const res = await api.get<ApiResponse<DashboardSummary>>('/dashboard');
    return res.data.data;
  },

  async getChartData(range: string = 'LAST_7_DAYS', fromDate?: string, toDate?: string): Promise<ChartData> {
    const res = await api.get<ApiResponse<ChartData>>('/statistics/chart', {
      params: { range, fromDate, toDate },
    });
    return res.data.data;
  },

  async getStreakAndHeatmap(): Promise<StreakHeatmap> {
    const res = await api.get<ApiResponse<StreakHeatmap>>('/statistics/streak');
    return res.data.data;
  },

  async getRecentActivities(limit: number = 10): Promise<Activity[]> {
    const res = await api.get<ApiResponse<Activity[]>>('/activities/recent', { params: { limit } });
    return res.data.data;
  },

  async getTodayActivities(): Promise<Activity[]> {
    const res = await api.get<ApiResponse<Activity[]>>('/activities/today');
    return res.data.data;
  },
};
