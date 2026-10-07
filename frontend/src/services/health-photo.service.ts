import api from './api';
import type { ApiResponse, PageResponse } from '../types';
import type { HealthWorkoutPhoto, HealthWorkoutPhotoRequest } from '../types/health-photo.types';

export const healthPhotoService = {
  async getPhotosByDate(date: string): Promise<HealthWorkoutPhoto[]> {
    const res = await api.get<ApiResponse<HealthWorkoutPhoto[]>>('/health/photos/by-date', {
      params: { date },
    });
    return res.data.data;
  },

  async getAllPhotos(page = 0, size = 24): Promise<PageResponse<HealthWorkoutPhoto>> {
    const res = await api.get<ApiResponse<PageResponse<HealthWorkoutPhoto>>>('/health/photos', {
      params: { page, size },
    });
    return res.data.data;
  },

  async savePhoto(data: HealthWorkoutPhotoRequest): Promise<HealthWorkoutPhoto> {
    const res = await api.post<ApiResponse<HealthWorkoutPhoto>>('/health/photos', data);
    return res.data.data;
  },

  async uploadPhoto(
    file: File,
    logDate?: string,
    caption?: string,
    weightKg?: number
  ): Promise<HealthWorkoutPhoto> {
    const formData = new FormData();
    formData.append('file', file);
    if (logDate) formData.append('logDate', logDate);
    if (caption) formData.append('caption', caption);
    if (weightKg !== undefined && weightKg !== null) formData.append('weightKg', weightKg.toString());

    const res = await api.post<ApiResponse<HealthWorkoutPhoto>>('/health/photos/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data.data;
  },

  async deletePhoto(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/health/photos/${id}`);
  },
};
