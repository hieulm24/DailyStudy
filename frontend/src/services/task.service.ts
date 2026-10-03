import api from './api';
import type {
  ApiResponse,
  PageResponse,
  DailyTask,
  DailyTaskRequest,
  DailyTaskFilter,
  DailyTaskSummary,
  DailyTaskStats,
  DailyTaskReorderItem,
} from '../types';

export const taskService = {
  async getTasks(params?: DailyTaskFilter): Promise<PageResponse<DailyTask>> {
    const res = await api.get<ApiResponse<PageResponse<DailyTask>>>('/tasks', { params });
    return res.data.data;
  },

  async getTasksByDate(date: string): Promise<DailyTask[]> {
    const res = await api.get<ApiResponse<DailyTask[]>>('/tasks/by-date', { params: { date } });
    return res.data.data;
  },

  async getTaskById(id: number): Promise<DailyTask> {
    const res = await api.get<ApiResponse<DailyTask>>(`/tasks/${id}`);
    return res.data.data;
  },

  async createTask(data: DailyTaskRequest): Promise<DailyTask> {
    const res = await api.post<ApiResponse<DailyTask>>('/tasks', data);
    return res.data.data;
  },

  async updateTask(id: number, data: DailyTaskRequest): Promise<DailyTask> {
    const res = await api.put<ApiResponse<DailyTask>>(`/tasks/${id}`, data);
    return res.data.data;
  },

  async toggleTask(id: number): Promise<DailyTask> {
    const res = await api.patch<ApiResponse<DailyTask>>(`/tasks/${id}/toggle`);
    return res.data.data;
  },

  async updateStatus(id: number, status: string): Promise<DailyTask> {
    const res = await api.patch<ApiResponse<DailyTask>>(`/tasks/${id}/status`, null, { params: { status } });
    return res.data.data;
  },

  async reorderTasks(items: DailyTaskReorderItem[]): Promise<void> {
    await api.put<ApiResponse<void>>('/tasks/reorder', items);
  },

  async deleteTask(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/tasks/${id}`);
  },

  async getDailySummaries(fromDate?: string, toDate?: string, page = 0, size = 30): Promise<PageResponse<DailyTaskSummary>> {
    const res = await api.get<ApiResponse<PageResponse<DailyTaskSummary>>>('/tasks/daily-summaries', {
      params: { fromDate, toDate, page, size },
    });
    return res.data.data;
  },

  async getTaskStats(fromDate?: string, toDate?: string): Promise<DailyTaskStats> {
    const res = await api.get<ApiResponse<DailyTaskStats>>('/tasks/stats', {
      params: { fromDate, toDate },
    });
    return res.data.data;
  },
};
