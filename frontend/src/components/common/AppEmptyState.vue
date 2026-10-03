<template>
  <div class="flex flex-col items-center justify-center py-12 px-4 text-center">
    <div class="w-12 h-12 rounded-md bg-slate-100 flex items-center justify-center text-slate-400 mb-3">
      <component :is="icon" v-if="icon" class="w-6 h-6" />
      <Inbox v-else class="w-6 h-6" />
    </div>
    <h3 class="text-sm font-semibold text-slate-800">{{ title }}</h3>
    <p v-if="description" class="text-xs text-slate-500 max-w-sm mt-1 mb-4">{{ description }}</p>
    <slot name="action">
      <AppButton v-if="actionText" variant="primary" size="sm" @click="$emit('action')">
        {{ actionText }}
      </AppButton>
    </slot>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue';
import { Inbox } from 'lucide-vue-next';
import AppButton from './AppButton.vue';

withDefaults(
  defineProps<{
    icon?: Component;
    title?: string;
    description?: string;
    actionText?: string;
  }>(),
  {
    title: 'Không có dữ liệu',
  }
);

defineEmits<{
  (e: 'action'): void;
}>();
</script>
