<template>
  <div class="flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 border-t border-slate-100 text-xs text-slate-500 bg-white">
    <!-- Item counter & Page Size -->
    <div class="flex items-center gap-3">
      <div>
        Hiển thị
        <span class="font-semibold text-slate-800">{{ totalElements === 0 ? 0 : currentPage * pageSize + 1 }}</span>
        -
        <span class="font-semibold text-slate-800">{{ Math.min((currentPage + 1) * pageSize, totalElements) }}</span>
        trên tổng số <span class="font-bold text-slate-900">{{ totalElements }}</span> mục
      </div>

      <div v-if="showPageSize && totalElements > 10" class="hidden sm:flex items-center gap-1.5 pl-3 border-l border-slate-200">
        <span class="text-slate-400 text-[11px]">Xem:</span>
        <select
          :value="pageSize"
          class="text-xs bg-slate-50 border border-slate-200 rounded px-1.5 py-0.5 text-slate-700 focus:outline-none focus:border-brand-500 cursor-pointer"
          @change="handlePageSizeChange"
        >
          <option :value="10">10 / trang</option>
          <option :value="20">20 / trang</option>
          <option :value="50">50 / trang</option>
          <option :value="100">100 / trang</option>
        </select>
      </div>
    </div>

    <!-- Pagination Controls -->
    <div v-if="totalPages > 1" class="flex items-center space-x-1">
      <!-- First Page -->
      <button
        type="button"
        title="Trang đầu"
        :disabled="currentPage === 0"
        :class="[
          'p-1.5 rounded-md border text-slate-600 transition-colors',
          currentPage === 0
            ? 'opacity-40 cursor-not-allowed bg-slate-50 border-slate-200'
            : 'hover:bg-slate-50 hover:text-slate-900 border-slate-200 cursor-pointer',
        ]"
        @click="$emit('update:page', 0)"
      >
        <ChevronsLeft class="w-3.5 h-3.5" />
      </button>

      <!-- Previous Page -->
      <button
        type="button"
        title="Trang trước"
        :disabled="currentPage === 0"
        :class="[
          'p-1.5 rounded-md border text-slate-600 transition-colors',
          currentPage === 0
            ? 'opacity-40 cursor-not-allowed bg-slate-50 border-slate-200'
            : 'hover:bg-slate-50 hover:text-slate-900 border-slate-200 cursor-pointer',
        ]"
        @click="$emit('update:page', currentPage - 1)"
      >
        <ChevronLeft class="w-3.5 h-3.5" />
      </button>

      <!-- Page Numbers with Ellipsis -->
      <template v-for="(p, idx) in visiblePages" :key="idx">
        <span v-if="p === '...'" class="px-1.5 py-1 text-slate-400 font-medium select-none">...</span>
        <button
          v-else
          type="button"
          :class="[
            'min-w-[28px] h-7 px-2 text-xs font-semibold rounded-md border transition-all cursor-pointer',
            currentPage === (p as number) - 1
              ? 'bg-brand-600 text-white border-brand-600 shadow-xs'
              : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900 border-slate-200',
          ]"
          @click="$emit('update:page', (p as number) - 1)"
        >
          {{ p }}
        </button>
      </template>

      <!-- Next Page -->
      <button
        type="button"
        title="Trang sau"
        :disabled="currentPage >= totalPages - 1"
        :class="[
          'p-1.5 rounded-md border text-slate-600 transition-colors',
          currentPage >= totalPages - 1
            ? 'opacity-40 cursor-not-allowed bg-slate-50 border-slate-200'
            : 'hover:bg-slate-50 hover:text-slate-900 border-slate-200 cursor-pointer',
        ]"
        @click="$emit('update:page', currentPage + 1)"
      >
        <ChevronRight class="w-3.5 h-3.5" />
      </button>

      <!-- Last Page -->
      <button
        type="button"
        title="Trang cuối"
        :disabled="currentPage >= totalPages - 1"
        :class="[
          'p-1.5 rounded-md border text-slate-600 transition-colors',
          currentPage >= totalPages - 1
            ? 'opacity-40 cursor-not-allowed bg-slate-50 border-slate-200'
            : 'hover:bg-slate-50 hover:text-slate-900 border-slate-200 cursor-pointer',
        ]"
        @click="$emit('update:page', totalPages - 1)"
      >
        <ChevronsRight class="w-3.5 h-3.5" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from 'lucide-vue-next';

const props = withDefaults(
  defineProps<{
    currentPage: number;
    totalPages: number;
    pageSize: number;
    totalElements: number;
    showPageSize?: boolean;
  }>(),
  {
    showPageSize: true,
  }
);

const emit = defineEmits<{
  (e: 'update:page', page: number): void;
  (e: 'update:pageSize', size: number): void;
}>();

const visiblePages = computed<(number | string)[]>(() => {
  const total = props.totalPages;
  const current = props.currentPage + 1;

  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  if (current <= 4) {
    return [1, 2, 3, 4, 5, '...', total];
  }

  if (current >= total - 3) {
    return [1, '...', total - 4, total - 3, total - 2, total - 1, total];
  }

  return [1, '...', current - 1, current, current + 1, '...', total];
});

function handlePageSizeChange(e: Event) {
  const target = e.target as HTMLSelectElement;
  const newSize = Number(target.value) || 20;
  emit('update:pageSize', newSize);
}
</script>
