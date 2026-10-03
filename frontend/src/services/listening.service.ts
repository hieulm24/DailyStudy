import api from './api';
import type { ApiResponse, PageResponse, ListeningLesson } from '../types';

export interface ListeningFilterParams {
  search?: string;
  level?: string;
  status?: string;
  dateRange?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

export const listeningService = {
  async getListeningLessons(params?: ListeningFilterParams): Promise<PageResponse<ListeningLesson>> {
    const res = await api.get<ApiResponse<PageResponse<ListeningLesson>>>('/listening', { params });
    return res.data.data;
  },

  async getListeningLessonById(id: number): Promise<ListeningLesson> {
    const res = await api.get<ApiResponse<ListeningLesson>>(`/listening/${id}`);
    return res.data.data;
  },

  async createListeningLesson(data: Partial<ListeningLesson>): Promise<ListeningLesson> {
    const res = await api.post<ApiResponse<ListeningLesson>>('/listening', data);
    return res.data.data;
  },

  async updateListeningLesson(id: number, data: Partial<ListeningLesson>): Promise<ListeningLesson> {
    const res = await api.put<ApiResponse<ListeningLesson>>(`/listening/${id}`, data);
    return res.data.data;
  },

  async deleteListeningLesson(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/listening/${id}`);
  },
};
