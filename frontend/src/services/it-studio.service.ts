import api from './api';
import type {
  ApiResponse,
  PageResponse,
  ItNote,
  CreateOrUpdateItNoteRequest,
  ItChatSession,
  SendItChatRequest,
  ItCodeSnippet,
  CreateOrUpdateSnippetRequest,
  ItPlaygroundExecutionRequest,
  ItPlaygroundExecutionResponse,
} from '../types';

export const itStudioService = {
  // -------------------------------------------------------------
  // NOTES & KNOWLEDGE BASE
  // -------------------------------------------------------------
  async getNotes(params: { category?: string; search?: string; page?: number; size?: number }): Promise<PageResponse<ItNote>> {
    const res = await api.get<ApiResponse<PageResponse<ItNote>>>('/it/notes', { params });
    return res.data.data;
  },

  async getNoteById(id: number): Promise<ItNote> {
    const res = await api.get<ApiResponse<ItNote>>(`/it/notes/${id}`);
    return res.data.data;
  },

  async createNote(data: CreateOrUpdateItNoteRequest): Promise<ItNote> {
    const res = await api.post<ApiResponse<ItNote>>('/it/notes', data);
    return res.data.data;
  },

  async updateNote(id: number, data: CreateOrUpdateItNoteRequest): Promise<ItNote> {
    const res = await api.put<ApiResponse<ItNote>>(`/it/notes/${id}`, data);
    return res.data.data;
  },

  async toggleFavorite(id: number): Promise<ItNote> {
    const res = await api.patch<ApiResponse<ItNote>>(`/it/notes/${id}/favorite`);
    return res.data.data;
  },

  async deleteNote(id: number): Promise<void> {
    await api.delete(`/it/notes/${id}`);
  },

  async uploadNoteImage(file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);
    const res = await api.post<ApiResponse<{ url: string }>>('/it/notes/upload-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data.data.url;
  },

  // -------------------------------------------------------------
  // AI ARCHITECT & CODING MENTOR (CHAT)
  // -------------------------------------------------------------
  async getSessions(): Promise<ItChatSession[]> {
    const res = await api.get<ApiResponse<ItChatSession[]>>('/it/chat/sessions');
    return res.data.data;
  },

  async getSessionById(id: number): Promise<ItChatSession> {
    const res = await api.get<ApiResponse<ItChatSession>>(`/it/chat/sessions/${id}`);
    return res.data.data;
  },

  async sendMessage(data: SendItChatRequest): Promise<ItChatSession> {
    const geminiKey = localStorage.getItem('gemini_api_key')?.trim();
    const payload: SendItChatRequest = {
      ...data,
      apiKey: data.apiKey || (geminiKey ? geminiKey : undefined),
    };
    const res = await api.post<ApiResponse<ItChatSession>>('/it/chat/send', payload);
    return res.data.data;
  },

  async deleteSession(id: number): Promise<void> {
    await api.delete(`/it/chat/sessions/${id}`);
  },

  // -------------------------------------------------------------
  // CODE SNIPPETS & SQL QUERIES
  // -------------------------------------------------------------
  async getSnippets(params: { language?: string; search?: string; page?: number; size?: number }): Promise<PageResponse<ItCodeSnippet>> {
    const res = await api.get<ApiResponse<PageResponse<ItCodeSnippet>>>('/it/snippets', { params });
    return res.data.data;
  },

  async getSnippetById(id: number): Promise<ItCodeSnippet> {
    const res = await api.get<ApiResponse<ItCodeSnippet>>(`/it/snippets/${id}`);
    return res.data.data;
  },

  async createSnippet(data: CreateOrUpdateSnippetRequest): Promise<ItCodeSnippet> {
    const res = await api.post<ApiResponse<ItCodeSnippet>>('/it/snippets', data);
    return res.data.data;
  },

  async updateSnippet(id: number, data: CreateOrUpdateSnippetRequest): Promise<ItCodeSnippet> {
    const res = await api.put<ApiResponse<ItCodeSnippet>>(`/it/snippets/${id}`, data);
    return res.data.data;
  },

  async deleteSnippet(id: number): Promise<void> {
    await api.delete(`/it/snippets/${id}`);
  },

  // -------------------------------------------------------------
  // CODE PLAYGROUND & AI ANALYZER
  // -------------------------------------------------------------
  async executeOrAnalyze(data: ItPlaygroundExecutionRequest): Promise<ItPlaygroundExecutionResponse> {
    const geminiKey = localStorage.getItem('gemini_api_key')?.trim();
    const payload: ItPlaygroundExecutionRequest = {
      ...data,
      apiKey: data.apiKey || (geminiKey ? geminiKey : undefined),
    };
    const res = await api.post<ApiResponse<ItPlaygroundExecutionResponse>>('/it/playground/execute', payload);
    return res.data.data;
  },
};
