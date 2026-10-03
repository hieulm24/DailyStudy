<template>
  <div class="w-full">
    <label v-if="label" :for="id" class="block text-xs font-semibold text-slate-700 mb-1.5">
      {{ label }}
      <span v-if="required" class="text-rose-500">*</span>
    </label>
    <div class="relative rounded-md">
      <div v-if="icon" class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
        <component :is="icon" class="w-4 h-4" />
      </div>
      <input
        :id="id"
        :type="inputType"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :required="required"
        :class="[
          'block w-full text-sm rounded-md border border-slate-300 bg-white shadow-sm transition-colors',
          'focus:border-brand-500 focus:ring-1 focus:ring-brand-500 focus:outline-none',
          icon ? 'pl-9' : 'pl-3',
          type === 'password' ? 'pr-9' : 'pr-3',
          'py-2 text-slate-900 placeholder:text-slate-400',
          disabled ? 'bg-slate-50 text-slate-500 cursor-not-allowed' : '',
          error ? 'border-rose-400 focus:border-rose-500 focus:ring-rose-500' : '',
        ]"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        @keyup.enter="$emit('enter')"
      />
      <button
        v-if="type === 'password'"
        type="button"
        tabindex="-1"
        class="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-600 focus:outline-none"
        @click="showPassword = !showPassword"
      >
        <Eye v-if="showPassword" class="w-4 h-4" />
        <EyeOff v-else class="w-4 h-4" />
      </button>
    </div>
    <p v-if="error" class="mt-1 text-xs text-rose-500">{{ error }}</p>
    <p v-else-if="hint" class="mt-1 text-xs text-slate-500">{{ hint }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { Component } from 'vue';
import { Eye, EyeOff } from 'lucide-vue-next';

const props = withDefaults(
  defineProps<{
    modelValue?: string | number;
    label?: string;
    id?: string;
    type?: string;
    placeholder?: string;
    disabled?: boolean;
    required?: boolean;
    error?: string;
    hint?: string;
    icon?: Component;
  }>(),
  {
    modelValue: '',
    type: 'text',
    disabled: false,
    required: false,
  }
);

defineEmits<{
  (e: 'update:modelValue', value: any): void;
  (e: 'enter'): void;
}>();

const showPassword = ref(false);

const inputType = computed(() => {
  if (props.type === 'password') {
    return showPassword.value ? 'text' : 'password';
  }
  return props.type;
});
</script>
