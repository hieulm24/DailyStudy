<template>
  <div
    :class="[
      'bg-white rounded-md border border-slate-200 shadow-sm transition-all',
      padding ? 'p-5' : '',
      hover ? 'hover:border-slate-300 hover:shadow' : '',
    ]"
  >
    <div v-if="$slots.header || title" class="flex items-center justify-between pb-3 mb-4 border-b border-slate-100">
      <slot name="header">
        <div>
          <h3 class="font-semibold text-slate-800 text-sm md:text-base">{{ title }}</h3>
          <p v-if="subtitle" class="text-xs text-slate-500 mt-0.5">{{ subtitle }}</p>
        </div>
      </slot>
      <div v-if="$slots.actions">
        <slot name="actions" />
      </div>
    </div>
    <slot />
    <div v-if="$slots.footer" class="mt-4 pt-3 border-t border-slate-100">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string;
    subtitle?: string;
    padding?: boolean;
    hover?: boolean;
  }>(),
  {
    padding: true,
    hover: false,
  }
);
</script>
