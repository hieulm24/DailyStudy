import api from './api';
import type { ApiResponse, PageResponse, SpeakingLesson } from '../types';

export interface SpeakingFilterParams {
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

export const speakingService = {
  async getSpeakingLessons(params?: SpeakingFilterParams): Promise<PageResponse<SpeakingLesson>> {
    const res = await api.get<ApiResponse<PageResponse<SpeakingLesson>>>('/speaking', { params });
    return res.data.data;
  },

  async getSpeakingLessonById(id: number): Promise<SpeakingLesson> {
    const res = await api.get<ApiResponse<SpeakingLesson>>(`/speaking/${id}`);
    return res.data.data;
  },

  async createSpeakingLesson(data: Partial<SpeakingLesson>): Promise<SpeakingLesson> {
    const res = await api.post<ApiResponse<SpeakingLesson>>('/speaking', data);
    return res.data.data;
  },

  async updateSpeakingLesson(id: number, data: Partial<SpeakingLesson>): Promise<SpeakingLesson> {
    const res = await api.put<ApiResponse<SpeakingLesson>>(`/speaking/${id}`, data);
    return res.data.data;
  },

  async deleteSpeakingLesson(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/speaking/${id}`);
  },
};
