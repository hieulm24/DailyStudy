import api from './api';
import type { ApiResponse, ReviewDueSummary, ReviewItem, TopicReviewSummary } from '../types';

export const reviewService = {
  async getReviewSummary(topicId?: number): Promise<ReviewDueSummary> {
    const res = await api.get<ApiResponse<ReviewDueSummary>>('/review/summary', {
      params: topicId ? { topicId } : undefined,
    });
    return res.data.data;
  },

  async getTopicsReviewSummary(): Promise<TopicReviewSummary[]> {
    const res = await api.get<ApiResponse<TopicReviewSummary[]>>('/review/topics-summary');
    return res.data.data;
  },

  async getDueReviewItems(topicId?: number): Promise<ReviewItem[]> {
    const res = await api.get<ApiResponse<ReviewItem[]>>('/review/due', {
      params: topicId ? { topicId } : undefined,
    });
    return res.data.data;
  },

  async submitReview(reviewItemId: number, result: 'FORGOT' | 'HARD' | 'GOOD' | 'EASY', difficulty?: string): Promise<ReviewItem> {
    const res = await api.post<ApiResponse<ReviewItem>>('/review/submit', {
      reviewItemId,
      result,
      difficulty,
    });
    return res.data.data;
  },
};

