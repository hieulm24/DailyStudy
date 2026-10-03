<template>
  <div class="w-full">
    <label v-if="label" :for="id" class="block text-xs font-semibold text-slate-700 mb-1.5">
      {{ label }}
      <span v-if="required" class="text-rose-500">*</span>
    </label>
    <select
      :id="id"
      :value="modelValue"
      :disabled="disabled"
      :required="required"
      :class="[
        'block w-full text-sm rounded-md border border-slate-300 bg-white shadow-sm transition-colors',
        'focus:border-brand-500 focus:ring-1 focus:ring-brand-500 focus:outline-none',
        'px-3 py-2 text-slate-900',
        disabled ? 'bg-slate-50 text-slate-500 cursor-not-allowed' : '',
        error ? 'border-rose-400 focus:border-rose-500 focus:ring-rose-500' : '',
      ]"
      @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
    >
      <option v-if="placeholder" value="">{{ placeholder }}</option>
      <option
        v-for="opt in options"
        :key="opt.value"
        :value="opt.value"
      >
        {{ opt.label }}
      </option>
    </select>
    <p v-if="error" class="mt-1 text-xs text-rose-500">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
export interface SelectOption {
  label: string;
  value: string | number;
}

withDefaults(
  defineProps<{
    modelValue?: string | number;
    label?: string;
    id?: string;
    placeholder?: string;
    options: SelectOption[];
    disabled?: boolean;
    required?: boolean;
    error?: string;
  }>(),
  {
    modelValue: '',
    disabled: false,
    required: false,
  }
);

defineEmits<{
  (e: 'update:modelValue', value: any): void;
}>();
</script>
