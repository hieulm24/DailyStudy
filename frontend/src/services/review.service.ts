import api from './api';
import type { ApiResponse, ReviewDueSummary, ReviewItem } from '../types';

export const reviewService = {
  async getReviewSummary(): Promise<ReviewDueSummary> {
    const res = await api.get<ApiResponse<ReviewDueSummary>>('/review/summary');
    return res.data.data;
  },

  async getDueReviewItems(): Promise<ReviewItem[]> {
    const res = await api.get<ApiResponse<ReviewItem[]>>('/review/due');
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
