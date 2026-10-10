<template>
  <AppModal
    :model-value="modelValue"
    size="sm"
    :z-index-class="zIndexClass || 'z-[80]'"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <template #header>
      <div class="flex items-center gap-2">
        <AlertTriangle class="w-5 h-5 text-amber-500" />
        <h3 class="text-base font-semibold text-slate-900">{{ title }}</h3>
      </div>
    </template>

    <p class="text-sm text-slate-600 leading-relaxed">{{ message }}</p>

    <template #footer>
      <AppButton variant="secondary" size="sm" @click="$emit('update:modelValue', false)">
        {{ cancelText }}
      </AppButton>
      <AppButton :variant="confirmVariant" size="sm" :loading="loading" @click="$emit('confirm')">
        {{ confirmText }}
      </AppButton>
    </template>
  </AppModal>
</template>

<script setup lang="ts">
import { AlertTriangle } from 'lucide-vue-next';
import AppModal from './AppModal.vue';
import AppButton from './AppButton.vue';

withDefaults(
  defineProps<{
    modelValue: boolean;
    title?: string;
    message?: string;
    confirmText?: string;
    cancelText?: string;
    confirmVariant?: 'danger' | 'primary';
    loading?: boolean;
    zIndexClass?: string;
  }>(),
  {
    title: 'Xác nhận hành động',
    message: 'Bạn có chắc chắn muốn thực hiện hành động này không?',
    confirmText: 'Xác nhận',
    cancelText: 'Hủy bỏ',
    confirmVariant: 'danger',
    loading: false,
    zIndexClass: 'z-[80]',
  }
);

defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirm'): void;
}>();
</script>
