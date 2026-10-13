<template>
  <div ref="containerRef" class="relative w-full">
    <label
      v-if="label"
      class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5"
    >
      {{ label }}
      <span v-if="required" class="text-rose-500">*</span>
    </label>

    <!-- Trigger Button -->
    <button
      type="button"
      :disabled="disabled"
      class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border rounded-md text-left flex items-center justify-between gap-2 transition-all shadow-2xs cursor-pointer focus:outline-none focus:ring-2 focus:ring-brand-500/20"
      :class="[
        isOpen
          ? 'border-brand-500 ring-2 ring-brand-500/20'
          : 'border-slate-200 hover:border-slate-300',
        disabled ? 'bg-slate-50 opacity-60 cursor-not-allowed' : '',
        error ? 'border-rose-400' : '',
      ]"
      @click="toggleDropdown"
    >
      <!-- Selected State -->
      <div v-if="hasSelection" class="flex items-center gap-2.5 min-w-0">
        <div
          class="w-6 h-6 rounded flex items-center justify-center shrink-0"
          :class="multiple && selectedAccounts.length > 1 ? 'bg-indigo-50 text-indigo-600' : getIconBgClass(primaryAccount?.accountType)"
        >
          <Layers
            v-if="multiple && selectedAccounts.length > 1"
            class="w-3.5 h-3.5 text-indigo-600"
          />
          <Banknote
            v-else-if="primaryAccount?.accountType === 'CASH'"
            class="w-3.5 h-3.5 text-emerald-600"
          />
          <Landmark
            v-else-if="primaryAccount?.accountType === 'BANK'"
            class="w-3.5 h-3.5 text-blue-600"
          />
          <Smartphone
            v-else-if="primaryAccount?.accountType === 'EWALLET'"
            class="w-3.5 h-3.5 text-pink-600"
          />
          <CreditCard
            v-else-if="primaryAccount?.accountType === 'CREDIT'"
            class="w-3.5 h-3.5 text-purple-600"
          />
          <Wallet v-else class="w-3.5 h-3.5 text-slate-500" />
        </div>
        <div class="flex items-center gap-2 truncate">
          <span class="font-bold text-slate-900 truncate">
            {{ selectedLabel }}
          </span>
          <span
            v-if="multiple && selectedAccounts.length > 1"
            class="text-[10px] text-indigo-700 bg-indigo-50 px-1.5 py-0.5 rounded font-semibold shrink-0"
          >
            {{ selectedAccounts.length }} ví
          </span>
          <span
            v-else-if="primaryAccount"
            class="text-[11px] text-slate-400 font-medium hidden sm:inline"
          >
            ({{ getAccountTypeLabel(primaryAccount.accountType) }})
          </span>
        </div>
      </div>

      <!-- Placeholder State -->
      <span v-else class="text-slate-400">
        {{ placeholder || (multiple ? "-- Chọn 1 hoặc nhiều nguồn tiền --" : "-- Chọn nguồn tiền --") }}
      </span>

      <!-- Right Indicator & Balance -->
      <div class="flex items-center gap-2 shrink-0">
        <span
          v-if="hasSelection"
          class="text-xs font-semibold text-slate-600 hidden sm:inline"
        >
          {{ formatCurrency(totalSelectedBalance) }}
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
      class="absolute z-50 mt-1 w-full bg-white border border-slate-200 rounded-lg shadow-lg max-h-64 overflow-y-auto divide-y divide-slate-100 py-1 focus:outline-none"
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
        :class="isAccountSelected(acc.id) ? 'bg-brand-50/60' : ''"
        @click="selectAccount(acc)"
      >
        <div class="flex items-center gap-2.5 min-w-0">
          <!-- Checkbox for multiple mode -->
          <div
            v-if="multiple"
            class="w-4 h-4 rounded border flex items-center justify-center shrink-0 transition-colors"
            :class="isAccountSelected(acc.id) ? 'border-brand-600 bg-brand-600 text-white' : 'border-slate-300 bg-white'"
          >
            <Check v-if="isAccountSelected(acc.id)" class="w-3 h-3 stroke-[3]" />
          </div>

          <div
            class="w-7 h-7 rounded-md flex items-center justify-center shrink-0"
            :class="getIconBgClass(acc.accountType)"
          >
            <Banknote
              v-if="acc.accountType === 'CASH'"
              class="w-4 h-4 text-emerald-600"
            />
            <Landmark
              v-else-if="acc.accountType === 'BANK'"
              class="w-4 h-4 text-blue-600"
            />
            <Smartphone
              v-else-if="acc.accountType === 'EWALLET'"
              class="w-4 h-4 text-pink-600"
            />
            <CreditCard
              v-else-if="acc.accountType === 'CREDIT'"
              class="w-4 h-4 text-purple-600"
            />
            <Wallet v-else class="w-4 h-4 text-slate-500" />
          </div>
          <div class="min-w-0">
            <p
              class="text-xs sm:text-sm font-bold text-slate-800 truncate"
              :class="isAccountSelected(acc.id) ? 'text-brand-700' : ''"
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
          <Check
            v-if="!multiple && isAccountSelected(acc.id)"
            class="w-4 h-4 text-brand-600 shrink-0"
          />
        </div>
      </div>

      <!-- Multiple mode footer -->
      <div
        v-if="multiple && accounts.length > 0"
        class="p-2.5 bg-slate-50/90 border-t border-slate-200 flex items-center justify-between gap-2"
      >
        <span class="text-[11px] text-slate-500 truncate">
          {{ selectedAccounts.length > 0 ? `Đã chọn: ${selectedLabel}` : 'Có thể chọn nhiều ví kết hợp' }}
        </span>
        <button
          type="button"
          class="px-3 py-1 text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 rounded transition-colors shrink-0"
          @click.stop="isOpen = false"
        >
          Xong
        </button>
      </div>
    </div>

    <!-- Error message -->
    <p v-if="error" class="mt-1 text-xs text-rose-500">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import {
  Banknote,
  Landmark,
  Smartphone,
  CreditCard,
  Wallet,
  ChevronDown,
  Check,
  Layers,
} from "lucide-vue-next";
import type { FinancialAccount } from "../../../types";
import { formatCurrency, getAccountTypeLabel } from "../../../utils/finance";

const props = withDefaults(
  defineProps<{
    modelValue?: number | number[] | null;
    accounts: FinancialAccount[];
    label?: string;
    placeholder?: string;
    required?: boolean;
    disabled?: boolean;
    error?: string;
    balanceField?: "currentBalance" | "availableBalance";
    multiple?: boolean;
  }>(),
  {
    modelValue: null,
    required: false,
    disabled: false,
    balanceField: "currentBalance",
    multiple: false,
  },
);

const emit = defineEmits<{
  (e: "update:modelValue", value: any): void;
  (e: "change", account: any): void;
}>();

const isOpen = ref(false);
const containerRef = ref<HTMLElement | null>(null);

const selectedIds = computed<number[]>(() => {
  if (Array.isArray(props.modelValue)) {
    return props.modelValue;
  }
  if (typeof props.modelValue === "number") {
    return [props.modelValue];
  }
  return [];
});

const selectedAccounts = computed(() => {
  return props.accounts.filter((a) => selectedIds.value.includes(a.id));
});

const primaryAccount = computed(() => {
  return selectedAccounts.value[0] || null;
});

const hasSelection = computed(() => {
  return selectedAccounts.value.length > 0;
});

const selectedLabel = computed(() => {
  if (selectedAccounts.value.length === 0) return "";
  return selectedAccounts.value.map((a) => a.name).join(" + ");
});

const totalSelectedBalance = computed(() => {
  return selectedAccounts.value.reduce((sum, a) => {
    const val =
      props.balanceField === "availableBalance"
        ? (a.availableBalance ?? a.currentBalance ?? 0)
        : (a.currentBalance ?? 0);
    return sum + val;
  }, 0);
});

function isAccountSelected(accId: number): boolean {
  return selectedIds.value.includes(accId);
}

function toggleDropdown() {
  if (props.disabled) return;
  isOpen.value = !isOpen.value;
}

function selectAccount(account: FinancialAccount) {
  if (props.multiple) {
    let newIds: number[];
    if (selectedIds.value.includes(account.id)) {
      newIds = selectedIds.value.filter((id) => id !== account.id);
    } else {
      newIds = [...selectedIds.value, account.id];
    }
    emit("update:modelValue", newIds);
    const newAccounts = props.accounts.filter((a) => newIds.includes(a.id));
    emit("change", newAccounts);
  } else {
    emit("update:modelValue", account.id);
    emit("change", account);
    isOpen.value = false;
  }
}

function formatBalance(acc: FinancialAccount): string {
  if (props.balanceField === "availableBalance") {
    return formatCurrency(acc.availableBalance ?? acc.currentBalance);
  }
  return formatCurrency(acc.currentBalance);
}

function getIconBgClass(type?: string): string {
  switch (type?.toUpperCase()) {
    case "CASH":
      return "bg-emerald-50 text-emerald-600";
    case "BANK":
      return "bg-blue-50 text-blue-600";
    case "EWALLET":
      return "bg-pink-50 text-pink-600";
    case "CREDIT":
      return "bg-purple-50 text-purple-600";
    default:
      return "bg-slate-100 text-slate-600";
  }
}

function handleClickOutside(event: MouseEvent) {
  if (
    containerRef.value &&
    !containerRef.value.contains(event.target as Node)
  ) {
    isOpen.value = false;
  }
}

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>
