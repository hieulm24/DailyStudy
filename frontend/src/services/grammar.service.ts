import api from './api';
import type { ApiResponse, PageResponse, Grammar } from '../types';

export interface GrammarFilterParams {
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

export const grammarService = {
  async getGrammars(params?: GrammarFilterParams): Promise<PageResponse<Grammar>> {
    const res = await api.get<ApiResponse<PageResponse<Grammar>>>('/grammar', { params });
    return res.data.data;
  },

  async getGrammarById(id: number): Promise<Grammar> {
    const res = await api.get<ApiResponse<Grammar>>(`/grammar/${id}`);
    return res.data.data;
  },

  async createGrammar(data: Partial<Grammar> & { exampleSentence?: string; exampleMeaning?: string }): Promise<Grammar> {
    const res = await api.post<ApiResponse<Grammar>>('/grammar', data);
    return res.data.data;
  },

  async updateGrammar(id: number, data: Partial<Grammar> & { exampleSentence?: string; exampleMeaning?: string }): Promise<Grammar> {
    const res = await api.put<ApiResponse<Grammar>>(`/grammar/${id}`, data);
    return res.data.data;
  },

  async deleteGrammar(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/grammar/${id}`);
  },
};
