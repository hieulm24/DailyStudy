<template>
  <div class="space-y-6 w-full">
    <!-- Header Banner -->
    <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-5">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 shrink-0 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-100/60 shadow-xs">
          <WalletCards class="w-7 h-7" />
        </div>
        <div>
          <h2 class="text-xl sm:text-2xl lg:text-3xl font-bold text-slate-900 tracking-tight">
            Tài chính cá nhân (Finance)
          </h2>
          <p class="text-xs sm:text-sm text-slate-500 mt-1">
            Kiểm soát tiền vào, tiền ra, số dư các ví, hoạch định mục tiêu tích lũy và báo cáo tài chính thông minh
          </p>
        </div>
      </div>

      <!-- Quick Action Buttons -->
      <div class="flex flex-wrap items-center gap-2.5 shrink-0 self-start md:self-auto">
        <button
          type="button"
          @click="openAddTransactionModal"
          class="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs sm:text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-md transition-colors shadow-xs"
        >
          <Plus class="w-4 h-4" />
          <span>Ghi chép thu chi</span>
        </button>

        <button
          type="button"
          @click="openAddGoalModal"
          class="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs sm:text-sm font-bold text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-md transition-colors shadow-xs"
        >
          <Target class="w-4 h-4" />
          <span>Mục tiêu mới</span>
        </button>
      </div>
    </div>

    <!-- Tab Navigation Switcher -->
    <div class="bg-white p-1.5 rounded-md border border-slate-200 shadow-xs grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-1.5">
      <button
        type="button"
        @click="setTab('dashboard')"
        :class="[
          'flex items-center justify-center gap-2 py-2.5 px-3 rounded-md text-xs sm:text-sm font-bold transition-all cursor-pointer',
          activeTab === 'dashboard'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50',
        ]"
      >
        <LayoutDashboard class="w-4 h-4" />
        <span>Tổng quan</span>
      </button>

      <button
        type="button"
        @click="setTab('transactions')"
        :class="[
          'flex items-center justify-center gap-2 py-2.5 px-3 rounded-md text-xs sm:text-sm font-bold transition-all cursor-pointer',
          activeTab === 'transactions'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50',
        ]"
      >
        <Receipt class="w-4 h-4" />
        <span>Giao dịch thu & chi</span>
      </button>

      <button
        type="button"
        @click="setTab('accounts')"
        :class="[
          'flex items-center justify-center gap-2 py-2.5 px-3 rounded-md text-xs sm:text-sm font-bold transition-all cursor-pointer',
          activeTab === 'accounts'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50',
        ]"
      >
        <Wallet class="w-4 h-4" />
        <span>Ví & Tài khoản</span>
      </button>

      <button
        type="button"
        @click="setTab('goals')"
        :class="[
          'flex items-center justify-center gap-2 py-2.5 px-3 rounded-md text-xs sm:text-sm font-bold transition-all cursor-pointer',
          activeTab === 'goals'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50',
        ]"
      >
        <Target class="w-4 h-4" />
        <span>Mục tiêu tài chính</span>
      </button>

      <button
        type="button"
        @click="setTab('categories')"
        :class="[
          'flex items-center justify-center gap-2 py-2.5 px-3 rounded-md text-xs sm:text-sm font-bold transition-all cursor-pointer',
          activeTab === 'categories'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50',
        ]"
      >
        <Tags class="w-4 h-4" />
        <span>Danh mục thu chi</span>
      </button>
    </div>

    <!-- Active Tab Component Rendering -->
    <div class="transition-opacity duration-150">
      <FinanceDashboardTab
        v-if="activeTab === 'dashboard'"
        ref="dashboardTabRef"
        @switchTab="setTab"
        @openAddTransaction="openAddTransactionModal"
        @openAddGoal="openAddGoalModal"
      />

      <FinanceTransactionsTab
        v-else-if="activeTab === 'transactions'"
        ref="transactionsTabRef"
        @dataChanged="onAnyDataChanged"
      />

      <FinanceAccountsTab
        v-else-if="activeTab === 'accounts'"
        ref="accountsTabRef"
        @dataChanged="onAnyDataChanged"
      />

      <FinanceGoalsTab
        v-else-if="activeTab === 'goals'"
        ref="goalsTabRef"
        @dataChanged="onAnyDataChanged"
      />

      <FinanceCategoriesTab
        v-else-if="activeTab === 'categories'"
        ref="categoriesTabRef"
        @dataChanged="onAnyDataChanged"
      />

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  WalletCards,
  Plus,
  Target,
  LayoutDashboard,
  Receipt,
  Wallet,
  Tags,
} from 'lucide-vue-next';
import FinanceDashboardTab from './components/FinanceDashboardTab.vue';
import FinanceTransactionsTab from './components/FinanceTransactionsTab.vue';
import FinanceAccountsTab from './components/FinanceAccountsTab.vue';
import FinanceGoalsTab from './components/FinanceGoalsTab.vue';
import FinanceCategoriesTab from './components/FinanceCategoriesTab.vue';

const route = useRoute();
const router = useRouter();

type TabType = 'dashboard' | 'transactions' | 'accounts' | 'goals' | 'categories';

const activeTab = ref<TabType>('dashboard');

// Tab Component Refs
const dashboardTabRef = ref<any>(null);
const transactionsTabRef = ref<any>(null);
const accountsTabRef = ref<any>(null);
const goalsTabRef = ref<any>(null);
const categoriesTabRef = ref<any>(null);

function setTab(tab: string) {
  const validTabs: TabType[] = ['dashboard', 'transactions', 'accounts', 'goals', 'categories'];
  if (validTabs.includes(tab as TabType)) {
    activeTab.value = tab as TabType;
    router.replace({ query: { ...route.query, tab } });
  }
}

watch(
  () => route.query.tab,
  (newTab) => {
    if (newTab && typeof newTab === 'string') {
      const validTabs: TabType[] = ['dashboard', 'transactions', 'accounts', 'goals', 'categories'];
      if (validTabs.includes(newTab as TabType) && activeTab.value !== newTab) {
        activeTab.value = newTab as TabType;
      }
    }
  },
  { immediate: true }
);

function openAddTransactionModal() {
  if (activeTab.value !== 'transactions') {
    setTab('transactions');
    nextTick(() => {
      transactionsTabRef.value?.openAddModal?.();
    });
  } else {
    transactionsTabRef.value?.openAddModal?.();
  }
}

function openAddGoalModal() {
  if (activeTab.value !== 'goals') {
    setTab('goals');
    nextTick(() => {
      goalsTabRef.value?.openAddModal?.();
    });
  } else {
    goalsTabRef.value?.openAddModal?.();
  }
}

function onAnyDataChanged() {
  dashboardTabRef.value?.refresh?.();
  transactionsTabRef.value?.refresh?.();
  accountsTabRef.value?.refresh?.();
  goalsTabRef.value?.refresh?.();
  categoriesTabRef.value?.refresh?.();
}

onMounted(() => {
  const initial = route.query.tab as string;
  if (initial) {
    setTab(initial);
  }
});
</script>
