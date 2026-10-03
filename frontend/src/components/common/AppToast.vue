<template>
  <div class="fixed bottom-5 right-5 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none">
    <TransitionGroup
      enter-active-class="transform ease-out duration-200 transition"
      enter-from-class="translate-y-2 opacity-0 sm:translate-y-0 sm:translate-x-2"
      enter-to-class="translate-y-0 opacity-100 sm:translate-x-0"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-for="toast in toastStore.toasts"
        :key="toast.id"
        class="pointer-events-auto flex items-start p-3 rounded-md bg-white border shadow-lg text-xs"
        :class="getToastBorder(toast.type)"
      >
        <component :is="getToastIcon(toast.type)" class="w-4 h-4 mr-2.5 mt-0.5 flex-shrink-0" :class="getToastIconColor(toast.type)" />
        <div class="flex-1 font-medium text-slate-800 leading-snug">{{ toast.message }}</div>
        <button
          type="button"
          class="ml-2 text-slate-400 hover:text-slate-600 focus:outline-none"
          @click="toastStore.remove(toast.id)"
        >
          <X class="w-3.5 h-3.5" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import { useToastStore } from '../../stores/toast.store';
import { CheckCircle2, AlertCircle, Info, AlertTriangle, X } from 'lucide-vue-next';

const toastStore = useToastStore();

function getToastIcon(type: string) {
  switch (type) {
    case 'success':
      return CheckCircle2;
    case 'error':
      return AlertCircle;
    case 'warning':
      return AlertTriangle;
    case 'info':
    default:
      return Info;
  }
}

function getToastIconColor(type: string) {
  switch (type) {
    case 'success':
      return 'text-emerald-500';
    case 'error':
      return 'text-rose-500';
    case 'warning':
      return 'text-amber-500';
    case 'info':
    default:
      return 'text-brand-500';
  }
}

function getToastBorder(type: string) {
  switch (type) {
    case 'success':
      return 'border-emerald-200';
    case 'error':
      return 'border-rose-200';
    case 'warning':
      return 'border-amber-200';
    case 'info':
    default:
      return 'border-slate-200';
  }
}
</script>
