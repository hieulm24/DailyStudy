<template>
  <AppModal
    :model-value="modelValue"
    size="sm"
    :z-index-class="zIndexClass || 'z-[80]'"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <template #header>
      <div class="flex items-center gap-2">
        <component :is="iconComponent" class="w-5 h-5 flex-shrink-0" :class="iconColorClass" />
        <h3 class="text-base font-semibold text-slate-900">{{ dialogTitle }}</h3>
      </div>
    </template>

    <div class="text-sm text-slate-600 leading-relaxed whitespace-pre-line">{{ message }}</div>

    <template #footer>
      <AppButton
        v-if="effectiveShowCancel"
        variant="secondary"
        size="sm"
        @click="$emit('update:modelValue', false)"
      >
        {{ cancelText }}
      </AppButton>
      <AppButton
        :variant="effectiveConfirmVariant"
        size="sm"
        :loading="loading"
        @click="handleConfirm"
      >
        {{ effectiveConfirmText }}
      </AppButton>
    </template>
  </AppModal>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { AlertTriangle, AlertCircle, CheckCircle2, Info } from 'lucide-vue-next';
import AppModal from './AppModal.vue';
import AppButton from './AppButton.vue';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    title?: string;
    message?: string;
    type?: 'confirm' | 'error' | 'warning' | 'info' | 'success';
    confirmText?: string;
    cancelText?: string;
    confirmVariant?: 'danger' | 'primary' | 'secondary' | 'outline' | 'ghost' | 'success';
    loading?: boolean;
    zIndexClass?: string;
    showCancel?: boolean;
  }>(),
  {
    title: '',
    message: 'Bạn có chắc chắn muốn thực hiện hành động này không?',
    type: 'confirm',
    confirmText: '',
    cancelText: 'Hủy bỏ',
    loading: false,
    zIndexClass: 'z-[80]',
  }
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirm'): void;
}>();

const iconComponent = computed(() => {
  switch (props.type) {
    case 'error':
      return AlertCircle;
    case 'success':
      return CheckCircle2;
    case 'info':
      return Info;
    case 'warning':
    case 'confirm':
    default:
      return AlertTriangle;
  }
});

const iconColorClass = computed(() => {
  switch (props.type) {
    case 'error':
      return 'text-rose-500';
    case 'success':
      return 'text-emerald-500';
    case 'info':
      return 'text-brand-500';
    case 'warning':
    case 'confirm':
    default:
      return 'text-amber-500';
  }
});

const dialogTitle = computed(() => {
  if (props.title) return props.title;
  switch (props.type) {
    case 'error':
      return 'Thông báo lỗi';
    case 'success':
      return 'Thành công';
    case 'info':
      return 'Thông báo';
    case 'warning':
      return 'Cảnh báo';
    case 'confirm':
    default:
      return 'Xác nhận hành động';
  }
});

const effectiveShowCancel = computed(() => {
  if (typeof props.showCancel === 'boolean') return props.showCancel;
  // By default, error and success alerts don't show a cancel button
  return props.type !== 'error' && props.type !== 'success';
});

const effectiveConfirmText = computed(() => {
  if (props.confirmText) return props.confirmText;
  if (props.type === 'error' || props.type === 'success') return 'Đã hiểu';
  return 'Xác nhận';
});

const effectiveConfirmVariant = computed(() => {
  if (props.confirmVariant) return props.confirmVariant;
  if (props.type === 'error') return 'danger';
  if (props.type === 'success') return 'success';
  return 'danger';
});

function handleConfirm() {
  emit('confirm');
  if (props.type === 'error' || props.type === 'success') {
    emit('update:modelValue', false);
  }
}
</script>
