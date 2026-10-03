<template>
  <span
    :class="[
      'inline-flex items-center px-2 py-0.5 text-xs font-medium rounded-sm transition-colors',
      variantClasses,
    ]"
  >
    <span v-if="dot" :class="['w-1.5 h-1.5 rounded-full mr-1.5', dotClasses]" />
    <slot />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'slate' | 'level';
    dot?: boolean;
    level?: string;
  }>(),
  {
    variant: 'slate',
    dot: false,
  }
);

const variantClasses = computed(() => {
  if (props.level) {
    const lvl = props.level.toUpperCase();
    if (lvl.includes('A1') || lvl.includes('A2')) return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    if (lvl.includes('B1') || lvl.includes('B2')) return 'bg-brand-50 text-brand-700 border border-brand-200';
    if (lvl.includes('C1') || lvl.includes('C2')) return 'bg-purple-50 text-purple-700 border border-purple-200';
    return 'bg-slate-100 text-slate-700 border border-slate-200';
  }

  switch (props.variant) {
    case 'primary':
      return 'bg-brand-50 text-brand-700 border border-brand-200';
    case 'success':
      return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    case 'warning':
      return 'bg-amber-50 text-amber-700 border border-amber-200';
    case 'danger':
      return 'bg-rose-50 text-rose-700 border border-rose-200';
    case 'info':
      return 'bg-cyan-50 text-cyan-700 border border-cyan-200';
    case 'slate':
    default:
      return 'bg-slate-100 text-slate-700 border border-slate-200';
  }
});

const dotClasses = computed(() => {
  switch (props.variant) {
    case 'primary':
      return 'bg-brand-500';
    case 'success':
      return 'bg-emerald-500';
    case 'warning':
      return 'bg-amber-500';
    case 'danger':
      return 'bg-rose-500';
    case 'info':
      return 'bg-cyan-500';
    case 'slate':
    default:
      return 'bg-slate-500';
  }
});
</script>
