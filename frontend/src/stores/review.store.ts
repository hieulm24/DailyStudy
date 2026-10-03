import { defineStore } from 'pinia';
import { ref } from 'vue';
import { reviewService } from '../services/review.service';
import type { ReviewDueSummary } from '../types';

export const useReviewStore = defineStore('review', () => {
  const summary = ref<ReviewDueSummary>({
    totalDue: 0,
    vocabularyDue: 0,
    grammarDue: 0,
    totalItems: 0,
    totalMastered: 0,
  });

  async function fetchSummary() {
    try {
      summary.value = await reviewService.getReviewSummary();
    } catch (e) {
      console.error('Failed to load review summary', e);
    }
  }

  return {
    summary,
    fetchSummary,
  };
});
