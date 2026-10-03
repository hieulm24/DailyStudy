<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-y-auto bg-slate-900/50 backdrop-blur-sm"
      @click.self="closeOnBackdrop && $emit('update:modelValue', false)"
    >
      <div
        :class="[
          'relative w-full bg-white rounded-md shadow-xl border border-slate-200 overflow-hidden my-8 transform transition-all',
          sizeClasses,
        ]"
      >
        <!-- Modal Header -->
        <div v-if="title || $slots.header" class="flex items-center justify-between px-6 py-4 border-b border-slate-100">
          <slot name="header">
            <h3 class="text-base font-semibold text-slate-900">{{ title }}</h3>
          </slot>
          <button
            type="button"
            class="text-slate-400 hover:text-slate-600 focus:outline-none p-1 rounded-md hover:bg-slate-100 transition-colors"
            @click="$emit('update:modelValue', false)"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <!-- Modal Body -->
        <div class="px-6 py-5 max-h-[calc(100vh-6rem)] sm:max-h-[calc(100vh-7rem)] overflow-y-auto">
          <slot />
        </div>

        <!-- Modal Footer -->
        <div v-if="$slots.footer" class="flex items-center justify-end gap-3 px-6 py-4 border-t border-slate-100 bg-slate-50/50">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { X } from 'lucide-vue-next';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    title?: string;
    size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';
    closeOnBackdrop?: boolean;
  }>(),
  {
    size: 'md',
    closeOnBackdrop: true,
  }
);

defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm':
      return 'max-w-md';
    case 'lg':
      return 'max-w-3xl';
    case 'xl':
      return 'max-w-5xl';
    case '2xl':
      return 'max-w-6xl';
    case '3xl':
      return 'max-w-7xl';
    case 'full':
      return 'max-w-[95vw] sm:max-w-[92vw] lg:max-w-7xl';
    case 'md':
    default:
      return 'max-w-xl';
  }
});
</script>
