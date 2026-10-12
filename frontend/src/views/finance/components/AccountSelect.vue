<template>
  <div ref="containerRef" class="relative w-full">
    <label v-if="label" class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
      {{ label }}
      <span v-if="required" class="text-rose-500">*</span>
    </label>

    <!-- Trigger Button -->
    <button
      type="button"
      :disabled="disabled"
      class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border rounded-md text-left flex items-center justify-between gap-2 transition-all shadow-2xs cursor-pointer focus:outline-none focus:ring-2 focus:ring-brand-500/20"
      :class="[
        isOpen ? 'border-brand-500 ring-2 ring-brand-500/20' : 'border-slate-200 hover:border-slate-300',
        disabled ? 'bg-slate-50 opacity-60 cursor-not-allowed' : '',
        error ? 'border-rose-400' : '',
      ]"
      @click="toggleDropdown"
    >
      <!-- Selected State -->
      <div v-if="selectedAccount" class="flex items-center gap-2.5 min-w-0">
        <div
          class="w-6 h-6 rounded flex items-center justify-center shrink-0"
          :class="getIconBgClass(selectedAccount.accountType)"
        >
          <Banknote v-if="selectedAccount.accountType === 'CASH'" class="w-3.5 h-3.5 text-emerald-600" />
          <Landmark v-else-if="selectedAccount.accountType === 'BANK'" class="w-3.5 h-3.5 text-blue-600" />
          <Smartphone v-else-if="selectedAccount.accountType === 'EWALLET'" class="w-3.5 h-3.5 text-pink-600" />
          <CreditCard v-else-if="selectedAccount.accountType === 'CREDIT'" class="w-3.5 h-3.5 text-purple-600" />
          <Wallet v-else class="w-3.5 h-3.5 text-slate-500" />
        </div>
        <div class="flex items-center gap-2 truncate">
          <span class="font-bold text-slate-900 truncate">{{ selectedAccount.name }}</span>
          <span class="text-[11px] text-slate-400 font-medium hidden sm:inline">
            ({{ getAccountTypeLabel(selectedAccount.accountType) }})
          </span>
        </div>
      </div>

      <!-- Placeholder State -->
      <span v-else class="text-slate-400">
        {{ placeholder || '-- Chọn nguồn tiền --' }}
      </span>

      <!-- Right Indicator & Balance -->
      <div class="flex items-center gap-2 shrink-0">
        <span v-if="selectedAccount" class="text-xs font-semibold text-slate-600 hidden sm:inline">
          {{ formatBalance(selectedAccount) }}
        </span>
        <ChevronDown
          class="w-4 h-4 text-slate-400 transition-transform duration-200"
          :class="isOpen ? 'rotate-180' : ''"
        />
      </div>
    </button>

    <!-- Dropdown Menu -->
    <div
      v-if="isOpen"
      class="absolute z-50 mt-1 w-full bg-white border border-slate-200 rounded-lg shadow-lg max-h-60 overflow-y-auto divide-y divide-slate-100 py-1 focus:outline-none"
    >
      <div
        v-if="accounts.length === 0"
        class="px-4 py-3 text-xs text-slate-400 text-center italic"
      >
        Không có nguồn tiền nào khả dụng
      </div>
      <div
        v-for="acc in accounts"
        :key="acc.id"
        class="px-3.5 py-2.5 flex items-center justify-between gap-2.5 hover:bg-slate-50 cursor-pointer transition-colors"
        :class="modelValue === acc.id ? 'bg-brand-50/60' : ''"
        @click="selectAccount(acc)"
      >
        <div class="flex items-center gap-2.5 min-w-0">
          <div
            class="w-7 h-7 rounded-md flex items-center justify-center shrink-0"
            :class="getIconBgClass(acc.accountType)"
          >
            <Banknote v-if="acc.accountType === 'CASH'" class="w-4 h-4 text-emerald-600" />
            <Landmark v-else-if="acc.accountType === 'BANK'" class="w-4 h-4 text-blue-600" />
            <Smartphone v-else-if="acc.accountType === 'EWALLET'" class="w-4 h-4 text-pink-600" />
            <CreditCard v-else-if="acc.accountType === 'CREDIT'" class="w-4 h-4 text-purple-600" />
            <Wallet v-else class="w-4 h-4 text-slate-500" />
          </div>
          <div class="min-w-0">
            <p
              class="text-xs sm:text-sm font-bold text-slate-800 truncate"
              :class="modelValue === acc.id ? 'text-brand-700' : ''"
            >
              {{ acc.name }}
            </p>
            <p class="text-[11px] text-slate-400">
              {{ getAccountTypeLabel(acc.accountType) }}
            </p>
          </div>
        </div>

        <div class="flex items-center gap-2 shrink-0">
          <span class="text-xs font-bold text-slate-700">
            {{ formatBalance(acc) }}
          </span>
          <Check v-if="modelValue === acc.id" class="w-4 h-4 text-brand-600 shrink-0" />
        </div>
      </div>
    </div>

    <!-- Error message -->
    <p v-if="error" class="mt-1 text-xs text-rose-500">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import {
  Banknote,
  Landmark,
  Smartphone,
  CreditCard,
  Wallet,
  ChevronDown,
  Check,
} from 'lucide-vue-next';
import type { FinancialAccount } from '../../../types';
import { formatCurrency, getAccountTypeLabel } from '../../../utils/finance';

const props = withDefaults(
  defineProps<{
    modelValue?: number | null;
    accounts: FinancialAccount[];
    label?: string;
    placeholder?: string;
    required?: boolean;
    disabled?: boolean;
    error?: string;
    balanceField?: 'currentBalance' | 'availableBalance';
  }>(),
  {
    modelValue: null,
    required: false,
    disabled: false,
    balanceField: 'currentBalance',
  }
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: number | null): void;
  (e: 'change', account: FinancialAccount | null): void;
}>();

const isOpen = ref(false);
const containerRef = ref<HTMLElement | null>(null);

const selectedAccount = computed(() => {
  if (!props.modelValue) return null;
  return props.accounts.find((a) => a.id === props.modelValue) || null;
});

function toggleDropdown() {
  if (props.disabled) return;
  isOpen.value = !isOpen.value;
}

function selectAccount(account: FinancialAccount) {
  emit('update:modelValue', account.id);
  emit('change', account);
  isOpen.value = false;
}

function formatBalance(acc: FinancialAccount): string {
  if (props.balanceField === 'availableBalance') {
    return formatCurrency(acc.availableBalance ?? acc.currentBalance);
  }
  return formatCurrency(acc.currentBalance);
}

function getIconBgClass(type?: string): string {
  switch (type?.toUpperCase()) {
    case 'CASH':
      return 'bg-emerald-50 text-emerald-600';
    case 'BANK':
      return 'bg-blue-50 text-blue-600';
    case 'EWALLET':
      return 'bg-pink-50 text-pink-600';
    case 'CREDIT':
      return 'bg-purple-50 text-purple-600';
    default:
      return 'bg-slate-100 text-slate-600';
  }
}

function handleClickOutside(event: MouseEvent) {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    isOpen.value = false;
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>
