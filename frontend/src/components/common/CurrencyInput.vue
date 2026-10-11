<template>
  <div class="space-y-1.5 w-full">
    <div class="relative flex items-center">
      <!-- Formatted Text Input -->
      <input
        ref="inputRef"
        type="text"
        inputmode="numeric"
        :value="displayValue"
        :placeholder="placeholder || '0'"
        :disabled="disabled"
        :required="required"
        @input="onInput"
        @focus="onFocus"
        @blur="onBlur"
        class="w-full pl-3 pr-9 py-2 text-sm font-bold bg-slate-50 border border-slate-200 rounded-md text-slate-900 focus:outline-none focus:ring-2 focus:ring-brand-500 disabled:opacity-50 disabled:cursor-not-allowed tracking-tight"
      />

      <!-- Currency suffix -->
      <span class="absolute right-3 text-xs font-bold text-slate-400 pointer-events-none select-none">
        ₫
      </span>
    </div>

    <!-- Quick Increment Buttons & Live Preview -->
    <div v-if="showQuickButtons" class="flex flex-wrap items-center gap-1.5 pt-0.5">
      <button
        v-for="btn in quickOptions"
        :key="btn.value"
        type="button"
        @click="addAmount(btn.value)"
        class="px-2 py-0.5 text-[10.5px] font-bold text-slate-600 bg-slate-100 hover:bg-brand-50 hover:text-brand-700 hover:border-brand-200 rounded border border-slate-200/60 transition-colors"
      >
        +{{ btn.label }}
      </button>

      <button
        v-if="modelValue && modelValue > 0"
        type="button"
        @click="clearAmount"
        class="px-2 py-0.5 text-[10.5px] font-semibold text-rose-600 hover:bg-rose-50 rounded transition-colors ml-auto"
      >
        Xóa (0 ₫)
      </button>
    </div>

    <!-- Formatted Readout -->
    <div v-if="showPreview && modelValue && modelValue > 0" class="text-[11px] text-brand-600 font-semibold flex items-center gap-1">
      <span>Định dạng:</span>
      <span class="font-bold underline">{{ formatCurrency(modelValue) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { formatCurrency, formatNumber, parseCurrencyInput } from '../../utils/finance';

const props = withDefaults(
  defineProps<{
    modelValue: number | null | undefined;
    placeholder?: string;
    required?: boolean;
    disabled?: boolean;
    showQuickButtons?: boolean;
    showPreview?: boolean;
    allowZero?: boolean;
  }>(),
  {
    placeholder: 'Ví dụ: 1.000.000',
    required: false,
    disabled: false,
    showQuickButtons: true,
    showPreview: true,
    allowZero: false,
  }
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void;
}>();

const inputRef = ref<HTMLInputElement | null>(null);
const isFocused = ref(false);

const quickOptions = [
  { label: '50K', value: 50_000 },
  { label: '100K', value: 100_000 },
  { label: '500K', value: 500_000 },
  { label: '1Tr', value: 1_000_000 },
  { label: '5Tr', value: 5_000_000 },
  { label: '10Tr', value: 10_000_000 },
];

const displayValue = computed(() => {
  if (props.modelValue === null || props.modelValue === undefined) {
    return '';
  }
  if (props.modelValue === 0) {
    return props.allowZero ? '0' : '';
  }
  return formatNumber(props.modelValue);
});

function onInput(event: Event) {
  const target = event.target as HTMLInputElement;
  const rawValue = target.value;
  const cursorPos = target.selectionStart || 0;

  // Count digits before cursor
  const digitsBeforeCursor = rawValue.slice(0, cursorPos).replace(/[^\d]/g, '').length;

  const numeric = parseCurrencyInput(rawValue);
  emit('update:modelValue', numeric);

  if (numeric > 0 || (props.allowZero && rawValue.trim() === '0')) {
    const formatted = formatNumber(numeric);
    target.value = formatted;

    // Reposition cursor
    let newPos = 0;
    let countedDigits = 0;
    for (let i = 0; i < formatted.length; i++) {
      if (/\d/.test(formatted[i])) {
        countedDigits++;
      }
      if (countedDigits >= digitsBeforeCursor) {
        newPos = i + 1;
        break;
      }
    }
    if (digitsBeforeCursor === 0) newPos = 0;
    target.setSelectionRange(newPos, newPos);
  } else {
    target.value = '';
  }
}

function onFocus() {
  isFocused.value = true;
}

function onBlur() {
  isFocused.value = false;
}

function addAmount(amountToAdd: number) {
  const current = props.modelValue || 0;
  const next = current + amountToAdd;
  emit('update:modelValue', next);
}

function clearAmount() {
  emit('update:modelValue', 0);
}
</script>
