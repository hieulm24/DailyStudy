import api from './api';
import type {
  ApiResponse,
  PageResponse,
  Grammar,
  GrammarExerciseQuestion,
  GenerateGrammarExerciseRequest,
  SubmitGrammarExerciseRequest,
  GrammarExerciseHistory,
} from '../types';

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

  // --- AI Exercise Methods ---
  async generateExercises(data: GenerateGrammarExerciseRequest): Promise<GrammarExerciseQuestion[]> {
    const res = await api.post<ApiResponse<GrammarExerciseQuestion[]>>('/grammar/exercises/generate', data);
    return res.data.data;
  },

  async submitExercise(data: SubmitGrammarExerciseRequest): Promise<GrammarExerciseHistory> {
    const res = await api.post<ApiResponse<GrammarExerciseHistory>>('/grammar/exercises/submit', data);
    return res.data.data;
  },

  async getTopicHistory(topicId: number): Promise<GrammarExerciseHistory[]> {
    const res = await api.get<ApiResponse<GrammarExerciseHistory[]>>(`/grammar/exercises/history/topic/${topicId}`);
    return res.data.data;
  },

  async getHistoryDetail(historyId: number): Promise<GrammarExerciseHistory> {
    const res = await api.get<ApiResponse<GrammarExerciseHistory>>(`/grammar/exercises/history/${historyId}`);
    return res.data.data;
  },
};

