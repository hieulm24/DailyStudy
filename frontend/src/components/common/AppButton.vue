<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="[
      'inline-flex items-center justify-center font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 text-sm select-none',
      sizeClasses,
      variantClasses,
      'rounded-md',
      disabled || loading ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer',
      fullWidth ? 'w-full' : '',
    ]"
    @click="$emit('click', $event)"
  >
    <svg
      v-if="loading"
      class="animate-spin -ml-1 mr-2 h-4 w-4 text-current"
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
    </svg>
    <component :is="icon" v-else-if="icon" :class="['w-4 h-4', $slots.default ? 'mr-1.5' : '']" />
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Component } from 'vue';

const props = withDefaults(
  defineProps<{
    type?: 'button' | 'submit' | 'reset';
    variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger' | 'success';
    size?: 'sm' | 'md' | 'lg' | 'icon';
    disabled?: boolean;
    loading?: boolean;
    fullWidth?: boolean;
    icon?: Component;
  }>(),
  {
    type: 'button',
    variant: 'primary',
    size: 'md',
    disabled: false,
    loading: false,
    fullWidth: false,
  }
);

defineEmits<{
  (e: 'click', event: MouseEvent): void;
}>();

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'primary':
      return 'bg-brand-600 hover:bg-brand-700 text-white shadow-sm focus:ring-brand-500';
    case 'secondary':
      return 'bg-slate-100 hover:bg-slate-200 text-slate-800 focus:ring-slate-400';
    case 'outline':
      return 'border border-slate-300 hover:bg-slate-50 text-slate-700 focus:ring-brand-500';
    case 'ghost':
      return 'hover:bg-slate-100 text-slate-700 focus:ring-slate-300';
    case 'danger':
      return 'bg-rose-600 hover:bg-rose-700 text-white shadow-sm focus:ring-rose-500';
    case 'success':
      return 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm focus:ring-emerald-500';
    default:
      return 'bg-brand-600 hover:bg-brand-700 text-white shadow-sm focus:ring-brand-500';
  }
});

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm':
      return 'h-8 px-2.5 text-xs';
    case 'lg':
      return 'h-11 px-5 text-base';
    case 'icon':
      return 'h-9 w-9 p-0';
    case 'md':
    default:
      return 'h-9 px-3.5 text-sm';
  }
});
</script>
