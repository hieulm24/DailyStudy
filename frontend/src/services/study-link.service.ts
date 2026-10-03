import api from './api';
import type { ApiResponse, PageResponse, StudyLink, StudyLinkFilter } from '../types';

export const studyLinkService = {
  async getLinks(params?: StudyLinkFilter): Promise<PageResponse<StudyLink>> {
    const res = await api.get<ApiResponse<PageResponse<StudyLink>>>('/study-links', { params });
    return res.data.data;
  },

  async getLinkById(id: number): Promise<StudyLink> {
    const res = await api.get<ApiResponse<StudyLink>>(`/study-links/${id}`);
    return res.data.data;
  },

  async createLink(data: { title: string; url: string; category?: string; description?: string; isFavorite?: boolean }): Promise<StudyLink> {
    const res = await api.post<ApiResponse<StudyLink>>('/study-links', data);
    return res.data.data;
  },

  async updateLink(id: number, data: { title: string; url: string; category?: string; description?: string; isFavorite?: boolean }): Promise<StudyLink> {
    const res = await api.put<ApiResponse<StudyLink>>(`/study-links/${id}`, data);
    return res.data.data;
  },

  async recordClick(id: number): Promise<StudyLink> {
    const res = await api.patch<ApiResponse<StudyLink>>(`/study-links/${id}/click`);
    return res.data.data;
  },

  async toggleFavorite(id: number): Promise<StudyLink> {
    const res = await api.patch<ApiResponse<StudyLink>>(`/study-links/${id}/favorite`);
    return res.data.data;
  },

  async deleteLink(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/study-links/${id}`);
  },
};
