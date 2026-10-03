import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface ToastMessage {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<ToastMessage[]>([]);
  let counter = 0;

  function show(type: 'success' | 'error' | 'warning' | 'info', message: string, duration = 3500) {
    const id = ++counter;
    toasts.value.push({ id, type, message });

    setTimeout(() => {
      remove(id);
    }, duration);
  }

  function success(message: string, duration = 3500) {
    show('success', message, duration);
  }

  function error(message: string, duration = 4000) {
    show('error', message, duration);
  }

  function info(message: string, duration = 3500) {
    show('info', message, duration);
  }

  function warning(message: string, duration = 3500) {
    show('warning', message, duration);
  }

  function remove(id: number) {
    toasts.value = toasts.value.filter((t) => t.id !== id);
  }

  return {
    toasts,
    show,
    success,
    error,
    info,
    warning,
    remove,
  };
});
