<template>
  <header class="sticky top-0 z-30 flex items-center justify-between h-20 px-6 sm:px-8 bg-white border-b border-slate-200 shadow-2xs">
    <!-- Mobile Menu Toggle & Title -->
    <div class="flex items-center gap-4">
      <button
        type="button"
        class="p-2 text-slate-600 hover:text-slate-900 rounded-md hover:bg-slate-100 lg:hidden focus:outline-none"
        @click="$emit('toggle-sidebar')"
      >
        <Menu class="w-6 h-6" />
      </button>

      <div class="flex items-center gap-2">
        <h1 class="text-lg sm:text-xl font-bold text-slate-900">{{ pageTitle }}</h1>
      </div>
    </div>

    <!-- Header Actions -->
    <div class="flex items-center gap-3.5">
      <!-- Review Notification Alert Badge -->
      <router-link
        to="/review"
        v-if="reviewStore.summary.totalDue > 0"
        class="hidden sm:inline-flex items-center gap-2 px-3.5 py-1.5 text-sm font-medium rounded-md bg-amber-50 text-amber-800 border border-amber-200 hover:bg-amber-100 transition-colors shadow-2xs"
      >
        <Bell class="w-4 h-4 text-amber-600 animate-bounce" />
        <span>Cần ôn <strong>{{ reviewStore.summary.totalDue }}</strong> mục</span>
      </router-link>

      <router-link
        to="/review"
        v-else
        class="hidden sm:inline-flex items-center gap-2 px-3.5 py-1.5 text-sm font-medium rounded-md bg-emerald-50 text-emerald-800 border border-emerald-200"
      >
        <CheckCircle class="w-4 h-4 text-emerald-600" />
        <span>Đã hoàn thành ôn tập</span>
      </router-link>

      <!-- Quick Add Buttons -->
      <div class="relative">
        <AppButton variant="primary" size="md" :icon="Plus" @click="$emit('quick-add')">
          <span class="hidden sm:inline">Thêm nhanh</span>
        </AppButton>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { useReviewStore } from '../../stores/review.store';
import { Menu, Bell, CheckCircle, Plus } from 'lucide-vue-next';
import AppButton from '../common/AppButton.vue';

defineEmits<{
  (e: 'toggle-sidebar'): void;
  (e: 'quick-add'): void;
}>();

const route = useRoute();
const reviewStore = useReviewStore();

const pageTitle = computed(() => {
  switch (route.path) {
    case '/dashboard':
      return 'Tổng quan tiến độ (Dashboard)';
    case '/vocabulary':
      return 'Từ vựng tiếng Anh (Vocabulary)';
    case '/grammar':
      return 'Ngữ pháp & Cấu trúc (Grammar)';
    case '/listening':
      return 'Nhật ký luyện nghe (Listening)';
    case '/speaking':
      return 'Nhật ký luyện nói (Speaking)';
    case '/review':
      return 'Hệ thống ôn tập (Spaced Repetition)';
    case '/games':
      return 'Mini Games Ôn Luyện';
    case '/documents':
      return 'Kho tài liệu & Liên kết (Resources)';
    case '/tasks':
      return 'Kế hoạch & Việc cần làm hàng ngày (Daily Tasks)';
    case '/statistics':
      return 'Thống kê & Biểu đồ học tập';
    case '/settings':
      return 'Cài đặt hệ thống (Settings)';
    default:
      return 'English Learning Hub';
  }
});
</script>
