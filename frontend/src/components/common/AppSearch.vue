<template>
  <div class="relative w-full max-w-xs">
    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
      <Search class="w-4 h-4" />
    </div>
    <input
      type="text"
      :value="modelValue"
      :placeholder="placeholder"
      class="block w-full text-xs sm:text-sm pl-9 pr-8 py-2 rounded-md border border-slate-300 bg-white placeholder:text-slate-400 focus:outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 transition-colors"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      @keyup.enter="$emit('search')"
    />
    <button
      v-if="modelValue"
      type="button"
      class="absolute inset-y-0 right-0 pr-2.5 flex items-center text-slate-400 hover:text-slate-600"
      @click="$emit('update:modelValue', ''); $emit('search')"
    >
      <X class="w-3.5 h-3.5" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { Search, X } from 'lucide-vue-next';

withDefaults(
  defineProps<{
    modelValue: string;
    placeholder?: string;
  }>(),
  {
    placeholder: 'Tìm kiếm nhanh...',
  }
);

defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'search'): void;
}>();
</script>
