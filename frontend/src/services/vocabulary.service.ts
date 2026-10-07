import api from './api';
import type { ApiResponse, PageResponse, Vocabulary, VocabularyTopic, VocabularyTopicFilter, VocabularyTopicRequest } from '../types';

export interface VocabularyFilterParams {
  topicId?: number;
  search?: string;
  level?: string;
  partOfSpeech?: string;
  status?: string;
  dateRange?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: string;
}

export const vocabularyService = {
  // Topics API
  async getTopics(params?: VocabularyTopicFilter): Promise<PageResponse<VocabularyTopic>> {
    const res = await api.get<ApiResponse<PageResponse<VocabularyTopic>>>('/vocabulary-topics', { params });
    return res.data.data;
  },

  async getAllTopics(): Promise<VocabularyTopic[]> {
    const res = await api.get<ApiResponse<VocabularyTopic[]>>('/vocabulary-topics/all');
    return res.data.data;
  },

  async getTopicById(id: number): Promise<VocabularyTopic> {
    const res = await api.get<ApiResponse<VocabularyTopic>>(`/vocabulary-topics/${id}`);
    return res.data.data;
  },

  async createTopic(data: VocabularyTopicRequest): Promise<VocabularyTopic> {
    const res = await api.post<ApiResponse<VocabularyTopic>>('/vocabulary-topics', data);
    return res.data.data;
  },

  async updateTopic(id: number, data: Partial<VocabularyTopicRequest>): Promise<VocabularyTopic> {
    const res = await api.put<ApiResponse<VocabularyTopic>>(`/vocabulary-topics/${id}`, data);
    return res.data.data;
  },

  async deleteTopic(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/vocabulary-topics/${id}`);
  },

  // Vocabularies API
  async getVocabularies(params?: VocabularyFilterParams): Promise<PageResponse<Vocabulary>> {
    const res = await api.get<ApiResponse<PageResponse<Vocabulary>>>('/vocabularies', { params });
    return res.data.data;
  },

  async getVocabularyById(id: number): Promise<Vocabulary> {
    const res = await api.get<ApiResponse<Vocabulary>>(`/vocabularies/${id}`);
    return res.data.data;
  },

  async createVocabulary(data: Partial<Vocabulary> & { exampleSentence?: string; exampleMeaning?: string }): Promise<Vocabulary> {
    const res = await api.post<ApiResponse<Vocabulary>>('/vocabularies', data);
    return res.data.data;
  },

  async updateVocabulary(id: number, data: Partial<Vocabulary> & { exampleSentence?: string; exampleMeaning?: string }): Promise<Vocabulary> {
    const res = await api.put<ApiResponse<Vocabulary>>(`/vocabularies/${id}`, data);
    return res.data.data;
  },

  async deleteVocabulary(id: number): Promise<void> {
    await api.delete<ApiResponse<void>>(`/vocabularies/${id}`);
  },

  async markAsMastered(id: number): Promise<Vocabulary> {
    const res = await api.post<ApiResponse<Vocabulary>>(`/vocabularies/${id}/mastered`);
    return res.data.data;
  },
};

