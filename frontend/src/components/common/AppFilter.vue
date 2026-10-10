<template>
  <div class="flex flex-wrap items-center gap-2 text-xs">
    <!-- Date Quick Filters -->
    <div class="inline-flex rounded-md border border-slate-200 bg-white p-0.5 shadow-sm">
      <button
        type="button"
        :class="[
          'px-2.5 py-1 font-medium rounded-sm transition-colors',
          dateRange === '' || dateRange === 'ALL'
            ? 'bg-slate-800 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
        ]"
        @click="setDateRange('')"
      >
        Tất cả
      </button>
      <button
        type="button"
        :class="[
          'px-2.5 py-1 font-medium rounded-sm transition-colors',
          dateRange === 'TODAY'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
        ]"
        @click="setDateRange('TODAY')"
      >
        Hôm nay
      </button>
      <button
        type="button"
        :class="[
          'px-2.5 py-1 font-medium rounded-sm transition-colors',
          dateRange === 'YESTERDAY'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
        ]"
        @click="setDateRange('YESTERDAY')"
      >
        Hôm qua
      </button>
      <button
        type="button"
        :class="[
          'px-2.5 py-1 font-medium rounded-sm transition-colors',
          dateRange === 'LAST_7_DAYS'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
        ]"
        @click="setDateRange('LAST_7_DAYS')"
      >
        7 ngày
      </button>
      <button
        type="button"
        :class="[
          'px-2.5 py-1 font-medium rounded-sm transition-colors',
          dateRange === 'LAST_30_DAYS'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
        ]"
        @click="setDateRange('LAST_30_DAYS')"
      >
        30 ngày
      </button>
      <button
        type="button"
        :class="[
          'px-2.5 py-1 font-medium rounded-sm transition-colors',
          dateRange === 'CUSTOM'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
        ]"
        @click="setDateRange('CUSTOM')"
      >
        Tùy chọn
      </button>
    </div>

    <!-- Custom Date Inputs -->
    <div v-if="dateRange === 'CUSTOM'" class="flex items-center gap-1.5 bg-white border border-slate-200 px-2 py-0.5 rounded-md shadow-sm">
      <input
        type="date"
        :value="fromDate"
        class="text-xs text-slate-700 bg-transparent border-0 focus:ring-0 p-0"
        @input="$emit('update:fromDate', ($event.target as HTMLInputElement).value); $emit('change')"
      />
      <span class="text-slate-400">-</span>
      <input
        type="date"
        :value="toDate"
        class="text-xs text-slate-700 bg-transparent border-0 focus:ring-0 p-0"
        @input="$emit('update:toDate', ($event.target as HTMLInputElement).value); $emit('change')"
      />
    </div>

    <!-- Optional Level Filter -->
    <select
      v-if="showLevel"
      :value="level"
      class="text-xs bg-white border border-slate-200 text-slate-700 rounded-md px-2.5 py-1.5 shadow-sm focus:outline-none focus:border-brand-500"
      @change="$emit('update:level', ($event.target as HTMLSelectElement).value); $emit('change')"
    >
      <option value="">Mọi cấp độ (Level)</option>
      <option value="A0">A0 - Mất gốc</option>
      <option value="A1">A1 - Nhập môn</option>
      <option value="A2">A2 - Sơ cấp</option>
      <option value="B1">B1 - Trung cấp</option>
      <option value="B2">B2 - Nâng cao</option>
      <option value="C1">C1 - Thành thạo</option>
      <option value="C2">C2 - Bản ngữ</option>
    </select>

    <!-- Optional Status Filter -->
    <select
      v-if="showStatus"
      :value="status"
      class="text-xs bg-white border border-slate-200 text-slate-700 rounded-md px-2.5 py-1.5 shadow-sm focus:outline-none focus:border-brand-500"
      @change="$emit('update:status', ($event.target as HTMLSelectElement).value); $emit('change')"
    >
      <option value="">Mọi trạng thái</option>
      <option value="NEW">Mới (New)</option>
      <option value="LEARNING">Đang học (Learning)</option>
      <option value="REVIEW">Cần ôn (Review)</option>
      <option value="MASTERED">Thuộc lòng (Mastered)</option>
    </select>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    dateRange?: string;
    fromDate?: string;
    toDate?: string;
    level?: string;
    status?: string;
    showLevel?: boolean;
    showStatus?: boolean;
  }>(),
  {
    dateRange: '',
    showLevel: true,
    showStatus: true,
  }
);

const emit = defineEmits<{
  (e: 'update:dateRange', value: string): void;
  (e: 'update:fromDate', value: string): void;
  (e: 'update:toDate', value: string): void;
  (e: 'update:level', value: string): void;
  (e: 'update:status', value: string): void;
  (e: 'change'): void;
}>();

function setDateRange(range: string) {
  emit('update:dateRange', range);
  emit('change');
}
</script>
