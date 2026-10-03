import api from './api';
import type { ApiResponse, PageResponse, LearningDocument, DocumentFilter, DocumentStatistics } from '../types';

export const documentService = {
  async getDocuments(params?: DocumentFilter): Promise<PageResponse<LearningDocument>> {
    const res = await api.get<ApiResponse<PageResponse<LearningDocument>>>('/documents', { params });
    return res.data.data;
  },

  async getDocumentById(id: number): Promise<LearningDocument> {
    const res = await api.get<ApiResponse<LearningDocument>>(`/documents/${id}`);
    return res.data.data;
  },

  async uploadDocument(
    file: File,
    title?: string,
    category?: string,
    description?: string,
    onUploadProgress?: (progressEvent: any) => void
  ): Promise<LearningDocument> {
    const formData = new FormData();
    formData.append('file', file);
    if (title) formData.append('title', title);
    if (category) formData.append('category', category);
    if (description) formData.append('description', description);

    const res = await api.post<ApiResponse<LearningDocument>>('/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    });
    return res.data.data;
  },

  async updateDocument(
    id: number,
    data: { title: string; category?: string; description?: string; isFavorite?: boolean }
  ): Promise<LearningDocument> {
    const res = await api.put<ApiResponse<LearningDocument>>(`/documents/${id}`, data);
    return res.data.data;
  },

  async toggleFavorite(id: number): Promise<LearningDocument> {
    const res = await api.patch<ApiResponse<LearningDocument>>(`/documents/${id}/favorite`);
    return res.data.data;
  },

  async deleteDocument(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/documents/${id}`);
  },

  async getStatistics(): Promise<DocumentStatistics> {
    const res = await api.get<ApiResponse<DocumentStatistics>>('/documents/statistics');
    return res.data.data;
  },

  getDownloadUrl(id: number): string {
    const baseUrl = api.defaults.baseURL || '/api';
    return `${baseUrl}/documents/${id}/download`;
  },

  getPreviewUrl(id: number): string {
    const baseUrl = api.defaults.baseURL || '/api';
    return `${baseUrl}/documents/${id}/preview`;
  },

  async getDocumentBlob(id: number): Promise<Blob> {
    const res = await api.get(`/documents/${id}/preview`, {
      responseType: 'blob',
    });
    return res.data;
  },

  async getDocumentArrayBuffer(id: number): Promise<ArrayBuffer> {
    const res = await api.get(`/documents/${id}/preview`, {
      responseType: 'arraybuffer',
    });
    return res.data;
  },

  async downloadFile(id: number, fileName: string): Promise<void> {
    const res = await api.get(`/documents/${id}/download`, {
      responseType: 'blob',
    });
    const blob = new Blob([res.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(downloadUrl);
  },
};
