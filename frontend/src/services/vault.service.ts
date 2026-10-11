import api from './api';
import type { ApiResponse, PageResponse } from '../types';
import type {
  AccountCategory,
  AccountCredential,
  AccountCredentialDetail,
  CreateAccountCredentialRequest,
  UpdateAccountCredentialRequest,
  VaultSummary,
  AccountAttachment,
  PasswordGeneratorRequest,
  PasswordGeneratorResponse,
} from '../types/vault.types';

export const vaultService = {
  async getCategories(): Promise<AccountCategory[]> {
    const res = await api.get<ApiResponse<AccountCategory[]>>('/vault/categories');
    return res.data.data;
  },

  async getCredentials(params?: {
    categoryId?: number;
    isFavorite?: boolean;
    isDeleted?: boolean;
    search?: string;
    page?: number;
    size?: number;
  }): Promise<PageResponse<AccountCredential>> {
    const res = await api.get<ApiResponse<PageResponse<AccountCredential>>>('/vault/credentials', {
      params,
    });
    return res.data.data;
  },

  async getAllActiveCredentials(): Promise<AccountCredential[]> {
    const res = await api.get<ApiResponse<AccountCredential[]>>('/vault/credentials/active-all');
    return res.data.data;
  },

  async getCredentialDetail(id: number): Promise<AccountCredentialDetail> {
    const res = await api.get<ApiResponse<AccountCredentialDetail>>(`/vault/credentials/${id}`);
    return res.data.data;
  },

  async createCredential(data: CreateAccountCredentialRequest): Promise<AccountCredentialDetail> {
    const res = await api.post<ApiResponse<AccountCredentialDetail>>('/vault/credentials', data);
    return res.data.data;
  },

  async updateCredential(id: number, data: UpdateAccountCredentialRequest): Promise<AccountCredentialDetail> {
    const res = await api.put<ApiResponse<AccountCredentialDetail>>(`/vault/credentials/${id}`, data);
    return res.data.data;
  },

  async toggleFavorite(id: number): Promise<void> {
    await api.patch<ApiResponse<void>>(`/vault/credentials/${id}/favorite`);
  },

  async moveToTrash(id: number): Promise<void> {
    await api.patch<ApiResponse<void>>(`/vault/credentials/${id}/trash`);
  },

  async restoreFromTrash(id: number): Promise<void> {
    await api.patch<ApiResponse<void>>(`/vault/credentials/${id}/restore`);
  },

  async deletePermanently(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/vault/credentials/${id}`);
  },

  async getSummary(): Promise<VaultSummary> {
    const res = await api.get<ApiResponse<VaultSummary>>('/vault/summary');
    return res.data.data;
  },

  async generatePassword(options: PasswordGeneratorRequest): Promise<PasswordGeneratorResponse> {
    const res = await api.post<ApiResponse<PasswordGeneratorResponse>>('/vault/generator', options);
    return res.data.data;
  },

  async uploadAttachment(credentialId: number, file: File, description?: string): Promise<AccountAttachment> {
    const formData = new FormData();
    formData.append('file', file);
    if (description) {
      formData.append('description', description);
    }
    const res = await api.post<ApiResponse<AccountAttachment>>(
      `/vault/credentials/${credentialId}/attachments`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return res.data.data;
  },

  async deleteAttachment(attachmentId: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/vault/attachments/${attachmentId}`);
  },

  getAttachmentDownloadUrl(attachmentId: number): string {
    return `/api/vault/attachments/${attachmentId}/download`;
  },

  getAttachmentPreviewUrl(attachmentId: number): string {
    return `/api/vault/attachments/${attachmentId}/preview`;
  },

  async downloadAttachmentFile(attachmentId: number, fileName: string): Promise<void> {
    const res = await api.get(`/vault/attachments/${attachmentId}/download`, { responseType: 'blob' });
    const blob = new Blob([res.data]);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  async fetchAttachmentBlob(attachmentId: number): Promise<{ url: string; mimeType: string; textContent?: string }> {
    const res = await api.get(`/vault/attachments/${attachmentId}/preview`, { responseType: 'blob' });
    const mimeType = (res.headers['content-type'] as string) || 'application/octet-stream';
    const blob = new Blob([res.data], { type: mimeType });
    const url = window.URL.createObjectURL(blob);
    return { url, mimeType };
  },
};
