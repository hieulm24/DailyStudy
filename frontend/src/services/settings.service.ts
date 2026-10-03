import api from './api';
import type { ApiResponse, UserSettings } from '../types';

export const settingsService = {
  async getSettings(): Promise<UserSettings> {
    const res = await api.get<ApiResponse<UserSettings>>('/settings');
    return res.data.data;
  },

  async updateSettings(data: Partial<UserSettings>): Promise<UserSettings> {
    const res = await api.put<ApiResponse<UserSettings>>('/settings', data);
    return res.data.data;
  },

  async exportData(): Promise<any> {
    const res = await api.get<ApiResponse<any>>('/settings/export');
    return res.data.data;
  },

  async importData(backupData: any): Promise<void> {
    await api.post<ApiResponse<void>>('/settings/import', backupData);
  },
};
