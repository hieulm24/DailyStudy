<template>
  <div class="w-full">
    <label v-if="label" :for="id" class="block text-xs font-semibold text-slate-700 mb-1.5">
      {{ label }}
      <span v-if="required" class="text-rose-500">*</span>
    </label>
    <textarea
      :id="id"
      :value="modelValue"
      :placeholder="placeholder"
      :rows="rows"
      :disabled="disabled"
      :required="required"
      :class="[
        'block w-full text-sm rounded-md border border-slate-300 bg-white shadow-sm transition-colors',
        'focus:border-brand-500 focus:ring-1 focus:ring-brand-500 focus:outline-none',
        'px-3 py-2 text-slate-900 placeholder:text-slate-400',
        disabled ? 'bg-slate-50 text-slate-500 cursor-not-allowed' : '',
        error ? 'border-rose-400 focus:border-rose-500 focus:ring-rose-500' : '',
      ]"
      @input="$emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
    ></textarea>
    <p v-if="error" class="mt-1 text-xs text-rose-500">{{ error }}</p>
    <p v-else-if="hint" class="mt-1 text-xs text-slate-500">{{ hint }}</p>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue?: string;
    label?: string;
    id?: string;
    placeholder?: string;
    rows?: number | string;
    disabled?: boolean;
    required?: boolean;
    error?: string;
    hint?: string;
  }>(),
  {
    modelValue: '',
    rows: 3,
    disabled: false,
    required: false,
  }
);

defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();
</script>
