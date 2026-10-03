<template>
  <div v-if="totalPages > 1" class="flex items-center justify-between px-2 py-3 border-t border-slate-100 text-xs text-slate-500">
    <div>
      Hiển thị <span class="font-medium text-slate-700">{{ currentPage * pageSize + 1 }}</span> đến
      <span class="font-medium text-slate-700">{{ Math.min((currentPage + 1) * pageSize, totalElements) }}</span>
      trong tổng số <span class="font-medium text-slate-700">{{ totalElements }}</span> mục
    </div>
    <div class="flex items-center space-x-1">
      <button
        type="button"
        :disabled="currentPage === 0"
        :class="[
          'px-2.5 py-1.5 rounded-md border text-slate-600 transition-colors',
          currentPage === 0
            ? 'opacity-40 cursor-not-allowed bg-slate-50 border-slate-200'
            : 'hover:bg-slate-50 hover:text-slate-900 border-slate-200',
        ]"
        @click="$emit('update:page', currentPage - 1)"
      >
        <ChevronLeft class="w-3.5 h-3.5" />
      </button>

      <span class="px-2 py-1 font-medium text-slate-700">
        Trang {{ currentPage + 1 }} / {{ totalPages }}
      </span>

      <button
        type="button"
        :disabled="currentPage >= totalPages - 1"
        :class="[
          'px-2.5 py-1.5 rounded-md border text-slate-600 transition-colors',
          currentPage >= totalPages - 1
            ? 'opacity-40 cursor-not-allowed bg-slate-50 border-slate-200'
            : 'hover:bg-slate-50 hover:text-slate-900 border-slate-200',
        ]"
        @click="$emit('update:page', currentPage + 1)"
      >
        <ChevronRight class="w-3.5 h-3.5" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChevronLeft, ChevronRight } from 'lucide-vue-next';

defineProps<{
  currentPage: number;
  totalPages: number;
  pageSize: number;
  totalElements: number;
}>();

defineEmits<{
  (e: 'update:page', page: number): void;
}>();
</script>
