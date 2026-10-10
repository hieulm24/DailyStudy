<template>
  <header class="sticky top-0 z-30 flex items-center justify-between h-28 px-6 sm:px-8 bg-white border-b border-slate-200 shadow-2xs">
    <!-- Mobile Menu Toggle -->
    <div class="flex items-center gap-4">
      <button
        type="button"
        class="p-2 text-slate-600 hover:text-slate-900 rounded-md hover:bg-slate-100 lg:hidden focus:outline-none"
        @click="$emit('toggle-sidebar')"
      >
        <Menu class="w-6 h-6" />
      </button>
    </div>

    <!-- Header Actions -->
    <div class="flex items-center gap-3 sm:gap-4">
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

      <!-- User Profile Header Button -->
      <router-link
        to="/profile"
        title="Quản lý thông tin cá nhân"
        class="flex items-center gap-2.5 pl-2 sm:pl-3 border-l border-slate-200 group hover:opacity-90 transition-opacity cursor-pointer"
      >
        <img
          v-if="authStore.user?.avatarUrl"
          :src="authStore.user.avatarUrl"
          class="w-10 h-10 rounded-full object-cover border border-slate-200 group-hover:ring-2 group-hover:ring-brand-500 transition-all shrink-0"
        />
        <div
          v-else
          class="w-10 h-10 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center font-bold text-sm group-hover:ring-2 group-hover:ring-brand-500 transition-all shrink-0"
        >
          {{ userInitial }}
        </div>
        <div class="hidden md:flex flex-col text-left">
          <span class="text-sm font-bold text-slate-800 group-hover:text-brand-600 transition-colors">
            {{ authStore.user?.displayName || 'Minh Hiếu' }}
          </span>
          <span class="text-[11px] text-slate-400 font-medium">
            Mục tiêu: {{ authStore.user?.targetScore || 650 }}+ TOEIC
          </span>
        </div>
      </router-link>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useAuthStore } from '../../stores/auth.store';
import { useReviewStore } from '../../stores/review.store';
import { Menu, Bell, CheckCircle, Plus } from 'lucide-vue-next';
import AppButton from '../common/AppButton.vue';

defineEmits<{
  (e: 'toggle-sidebar'): void;
  (e: 'quick-add'): void;
}>();

const authStore = useAuthStore();
const reviewStore = useReviewStore();

const userInitial = computed(() => {
  const name = authStore.user?.displayName || 'H';
  return name.trim().charAt(0).toUpperCase();
});
</script>
