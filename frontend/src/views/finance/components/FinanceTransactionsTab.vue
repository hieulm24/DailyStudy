<template>
  <div class="space-y-5">
    <!-- Filter Bar & Search -->
    <div class="bg-white p-4 sm:p-5 rounded-md border border-slate-200 shadow-xs space-y-4">
      <div class="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <!-- Search Input -->
        <div class="relative flex-1">
          <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            v-model="searchQuery"
            @keyup.enter="applyFilters"
            type="text"
            placeholder="Tìm theo mô tả, ghi chú giao dịch..."
            class="w-full pl-9 pr-4 py-2 text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-md text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <!-- Add Transaction Button -->
        <button
          type="button"
          @click="openAddModal"
          class="inline-flex items-center justify-center gap-1.5 px-4 py-2 text-xs sm:text-sm font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-md transition-colors shadow-xs shrink-0"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm giao dịch</span>
        </button>
      </div>

      <!-- Filter Controls Grid -->
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3 pt-2 border-t border-slate-100">
        <!-- Type Filter -->
        <div>
          <label class="block text-[11px] font-semibold text-slate-500 mb-1">Loại giao dịch</label>
          <select
            v-model="selectedType"
            @change="applyFilters"
            class="w-full px-3 py-1.5 text-xs font-medium bg-slate-50 border border-slate-200 rounded-md text-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option value="">Tất cả loại</option>
            <option value="INCOME">Thu nhập (+)</option>
            <option value="EXPENSE">Chi tiêu (-)</option>
            <option value="TRANSFER">Chuyển tiền nội bộ (➔)</option>
          </select>
        </div>

        <!-- Account Filter -->
        <div>
          <label class="block text-[11px] font-semibold text-slate-500 mb-1">Tài khoản / Ví</label>
          <select
            v-model="selectedAccountId"
            @change="applyFilters"
            class="w-full px-3 py-1.5 text-xs font-medium bg-slate-50 border border-slate-200 rounded-md text-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option :value="undefined">Tất cả tài khoản</option>
            <option v-for="acc in accounts" :key="acc.id" :value="acc.id">
              {{ acc.name }} ({{ formatCurrency(acc.currentBalance) }})
            </option>
          </select>
        </div>

        <!-- Category Filter -->
        <div>
          <label class="block text-[11px] font-semibold text-slate-500 mb-1">Danh mục</label>
          <select
            v-model="selectedCategoryId"
            @change="applyFilters"
            class="w-full px-3 py-1.5 text-xs font-medium bg-slate-50 border border-slate-200 rounded-md text-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option :value="undefined">Tất cả danh mục</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              [{{ cat.type === 'INCOME' ? 'Thu' : 'Chi' }}] {{ cat.name }}
            </option>
          </select>
        </div>

        <!-- Quick Reset Filter -->
        <div class="flex items-end gap-2">
          <button
            type="button"
            @click="resetFilters"
            class="w-full px-3 py-1.5 text-xs font-medium text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-md transition-colors"
          >
            Đặt lại bộ lọc
          </button>
        </div>
      </div>
    </div>

    <!-- Transactions List Table -->
    <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
      <!-- Loading State -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-16">
        <div class="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
        <span class="mt-3 text-xs font-medium text-slate-500">Đang tải danh sách giao dịch...</span>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="!transactions || transactions.length === 0"
        class="py-16 flex flex-col items-center justify-center text-center p-6"
      >
        <Receipt class="w-12 h-12 text-slate-300 mb-3" />
        <h3 class="text-sm font-bold text-slate-800">Không tìm thấy giao dịch nào</h3>
        <p class="text-xs text-slate-500 max-w-sm mt-1">
          Chưa có giao dịch phù hợp với điều kiện tìm kiếm hiện tại. Hãy thử chọn khoảng thời gian khác hoặc thêm giao dịch mới.
        </p>
        <button
          type="button"
          @click="openAddModal"
          class="mt-4 inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-md transition-colors"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm giao dịch ngay</span>
        </button>
      </div>

      <!-- Table Content -->
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-slate-50/80 border-b border-slate-200 text-[11.5px] font-bold text-slate-500 uppercase tracking-wider">
              <th class="py-3 px-4">Thời gian</th>
              <th class="py-3 px-4">Loại</th>
              <th class="py-3 px-4">Danh mục</th>
              <th class="py-3 px-4">Tài khoản</th>
              <th class="py-3 px-4">Mô tả / Ghi chú</th>
              <th class="py-3 px-4 text-right">Số tiền</th>
              <th class="py-3 px-4 text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100 text-xs">
            <tr
              v-for="tx in transactions"
              :key="tx.id"
              class="hover:bg-slate-50/70 transition-colors group"
            >
              <!-- Ngày giao dịch -->
              <td class="py-3 px-4 font-medium text-slate-700 whitespace-nowrap">
                {{ formatDateTime(tx.transactionDate) }}
              </td>

              <!-- Loại giao dịch -->
              <td class="py-3 px-4 whitespace-nowrap">
                <span
                  v-if="tx.type === 'INCOME'"
                  class="inline-flex items-center gap-1 px-2 py-0.5 font-bold rounded bg-emerald-50 text-emerald-700 border border-emerald-200/60"
                >
                  <ArrowDownLeft class="w-3.5 h-3.5" />
                  <span>Thu nhập</span>
                </span>
                <span
                  v-else-if="tx.type === 'EXPENSE'"
                  class="inline-flex items-center gap-1 px-2 py-0.5 font-bold rounded bg-rose-50 text-rose-700 border border-rose-200/60"
                >
                  <ArrowUpRight class="w-3.5 h-3.5" />
                  <span>Chi tiêu</span>
                </span>
                <span
                  v-else
                  class="inline-flex items-center gap-1 px-2 py-0.5 font-bold rounded bg-indigo-50 text-indigo-700 border border-indigo-200/60"
                >
                  <ArrowRightLeft class="w-3.5 h-3.5" />
                  <span>Chuyển tiền</span>
                </span>
              </td>

              <!-- Danh mục -->
              <td class="py-3 px-4 font-medium text-slate-800">
                <div v-if="tx.categoryName" class="flex items-center gap-1.5">
                  <span
                    class="w-2.5 h-2.5 rounded-full shrink-0"
                    :style="{ backgroundColor: tx.categoryColor || '#64748b' }"
                  ></span>
                  <span class="truncate max-w-[140px]">{{ tx.categoryName }}</span>
                </div>
                <span v-else class="text-slate-400 italic">-</span>
              </td>

              <!-- Tài khoản -->
              <td class="py-3 px-4 text-slate-700 whitespace-nowrap">
                <div class="flex items-center gap-1">
                  <span class="font-semibold text-slate-800">{{ tx.accountName }}</span>
                  <span v-if="tx.toAccountName" class="text-indigo-600 font-bold flex items-center gap-1">
                    ➔ {{ tx.toAccountName }}
                  </span>
                </div>
              </td>

              <!-- Mô tả / Ghi chú -->
              <td class="py-3 px-4 text-slate-600 max-w-xs truncate">
                <span class="font-medium text-slate-800">{{ tx.description || '-' }}</span>
                <span v-if="tx.note" class="text-slate-400 text-[11px] block truncate">{{ tx.note }}</span>
              </td>

              <!-- Số tiền -->
              <td class="py-3 px-4 text-right whitespace-nowrap">
                <span
                  :class="[
                    'text-sm font-bold',
                    tx.type === 'INCOME'
                      ? 'text-emerald-600'
                      : tx.type === 'EXPENSE'
                      ? 'text-rose-600'
                      : 'text-indigo-600',
                  ]"
                >
                  {{ tx.type === 'INCOME' ? '+' : tx.type === 'EXPENSE' ? '-' : '' }}{{ formatCurrency(tx.amount) }}
                </span>
              </td>

              <!-- Thao tác -->
              <td class="py-3 px-4 text-center whitespace-nowrap">
                <div class="flex items-center justify-center gap-1">
                  <button
                    type="button"
                    @click="openEditModal(tx)"
                    class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded transition-colors"
                    title="Chỉnh sửa giao dịch"
                  >
                    <Edit2 class="w-4 h-4" />
                  </button>
                  <button
                    type="button"
                    @click="confirmDelete(tx)"
                    class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors"
                    title="Xóa giao dịch"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Standard Pagination Footer -->
      <AppPagination
        v-if="totalElements > 0"
        :current-page="currentPage"
        :total-pages="totalPages"
        :page-size="pageSize"
        :total-elements="totalElements"
        :show-page-size="true"
        @update:page="changePage"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <!-- ==========================================
         MODAL: ADD / EDIT TRANSACTION
    ========================================== -->
    <AppModal
      v-model="isModalOpen"
      :title="isEditing ? 'Chỉnh sửa giao dịch' : 'Thêm giao dịch mới'"
      size="lg"
    >
      <form @submit.prevent="saveTransaction" class="space-y-4 sm:space-y-5 py-2">
        <!-- Loại giao dịch Selector Buttons -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-2">Loại giao dịch <span class="text-rose-500">*</span></label>
          <div class="grid grid-cols-3 gap-2.5">
            <button
              type="button"
              @click="formData.type = 'EXPENSE'"
              :class="[
                'py-2.5 px-3 rounded-md font-bold text-xs sm:text-sm flex items-center justify-center gap-1.5 border transition-all cursor-pointer',
                formData.type === 'EXPENSE'
                  ? 'bg-rose-50 border-rose-300 text-rose-700 shadow-2xs'
                  : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50',
              ]"
            >
              <ArrowUpRight class="w-4 h-4 text-rose-600" />
              <span>Chi tiêu</span>
            </button>

            <button
              type="button"
              @click="formData.type = 'INCOME'"
              :class="[
                'py-2.5 px-3 rounded-md font-bold text-xs sm:text-sm flex items-center justify-center gap-1.5 border transition-all cursor-pointer',
                formData.type === 'INCOME'
                  ? 'bg-emerald-50 border-emerald-300 text-emerald-700 shadow-2xs'
                  : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50',
              ]"
            >
              <ArrowDownLeft class="w-4 h-4 text-emerald-600" />
              <span>Thu nhập</span>
            </button>

            <button
              type="button"
              @click="formData.type = 'TRANSFER'"
              :class="[
                'py-2.5 px-3 rounded-md font-bold text-xs sm:text-sm flex items-center justify-center gap-1.5 border transition-all cursor-pointer',
                formData.type === 'TRANSFER'
                  ? 'bg-indigo-50 border-indigo-300 text-indigo-700 shadow-2xs'
                  : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50',
              ]"
            >
              <ArrowRightLeft class="w-4 h-4 text-indigo-600" />
              <span>Chuyển tiền</span>
            </button>
          </div>
        </div>

        <!-- Số tiền -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Số tiền (VNĐ) <span class="text-rose-500">*</span></label>
          <CurrencyInput
            v-model="formData.amount"
            placeholder="Ví dụ: 50.000"
            :required="true"
          />
        </div>

        <!-- Tài khoản nguồn -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
            {{ formData.type === 'TRANSFER' ? 'Tài khoản chuyển đi *' : 'Tài khoản thanh toán *' }}
          </label>
          <select
            v-model="formData.accountId"
            required
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          >
            <option :value="undefined" disabled>-- Chọn tài khoản --</option>
            <option v-for="acc in accounts" :key="acc.id" :value="acc.id">
              {{ acc.name }} (Số dư: {{ formatCurrency(acc.currentBalance) }})
            </option>
          </select>
          <div
            v-if="selectedSourceAccount"
            :class="[
              'mt-1.5 p-2 rounded-md border text-xs flex items-center justify-between transition-colors',
              (selectedSourceAccount.currentBalance || 0) <= 0 && selectedSourceAccount.accountType !== 'CREDIT'
                ? 'bg-rose-50 border-rose-200 text-rose-700'
                : 'bg-slate-50 border-slate-200/80 text-slate-700'
            ]"
          >
            <div class="flex items-center gap-1.5">
              <Wallet class="w-3.5 h-3.5" :class="(selectedSourceAccount.currentBalance || 0) <= 0 && selectedSourceAccount.accountType !== 'CREDIT' ? 'text-rose-500' : 'text-slate-500'" />
              <span>Số dư hiện tại của ví:</span>
            </div>
            <span class="font-bold">
              {{ formatCurrency(selectedSourceAccount.currentBalance) }}
            </span>
          </div>
        </div>

        <!-- Tài khoản đích (Chỉ hiện khi TRANSFER) -->
        <div v-if="formData.type === 'TRANSFER'">
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Tài khoản nhận tiền <span class="text-rose-500">*</span></label>
          <select
            v-model="formData.toAccountId"
            required
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          >
            <option :value="undefined" disabled>-- Chọn tài khoản đích --</option>
            <option
              v-for="acc in accounts.filter((a) => a.id !== formData.accountId)"
              :key="acc.id"
              :value="acc.id"
            >
              {{ acc.name }} (Số dư: {{ formatCurrency(acc.currentBalance) }})
            </option>
          </select>
        </div>

        <!-- Danh mục (Chỉ hiện khi INCOME hoặc EXPENSE) -->
        <div v-if="formData.type !== 'TRANSFER'">
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Danh mục thu/chi</label>
          <select
            v-model="formData.categoryId"
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          >
            <option :value="undefined">-- Chọn danh mục phù hợp --</option>
            <option
              v-for="cat in filteredCategories"
              :key="cat.id"
              :value="cat.id"
            >
              {{ cat.name }}
            </option>
          </select>
        </div>

        <!-- Ngày giờ giao dịch -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Ngày giờ giao dịch <span class="text-rose-500">*</span></label>
          <input
            v-model="formData.transactionDate"
            type="datetime-local"
            required
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          />
        </div>

        <!-- Mô tả ngắn -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Mô tả giao dịch</label>
          <input
            v-model="formData.description"
            type="text"
            maxlength="255"
            placeholder="Ví dụ: Ăn trưa bún bò, Tiền lương tháng 10..."
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          />
        </div>

        <!-- Ghi chú chi tiết -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Ghi chú thêm</label>
          <textarea
            v-model="formData.note"
            rows="3"
            placeholder="Ghi chú chi tiết hoặc lưu ý bổ sung..."
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs resize-none"
          ></textarea>
        </div>

        <!-- Modal Footer Actions -->
        <div class="flex items-center justify-end gap-3 pt-4 border-t border-slate-200">
          <button
            type="button"
            @click="isModalOpen = false"
            class="px-4 py-2.5 text-xs sm:text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
          >
            Hủy
          </button>
          <button
            type="submit"
            :disabled="submitting"
            class="inline-flex items-center gap-1.5 px-5 py-2.5 text-xs sm:text-sm font-bold text-white bg-brand-600 hover:bg-brand-700 disabled:opacity-50 rounded-md transition-colors shadow-xs cursor-pointer"
          >
            <Save class="w-4 h-4" />
            <span>{{ isEditing ? 'Lưu thay đổi' : 'Thêm giao dịch' }}</span>
          </button>
        </div>
      </form>
    </AppModal>

    <!-- Confirm Delete Dialog -->
    <AppConfirmDialog
      v-model="isDeleteOpen"
      title="Xác nhận xóa giao dịch"
      :message="`Bạn có chắc chắn muốn xóa giao dịch '${txToDelete?.description || formatCurrency(txToDelete?.amount)}' không? Số dư các tài khoản liên quan sẽ được tự động cập nhật lại.`"
      confirm-text="Xóa giao dịch"
      confirm-variant="danger"
      :loading="deleting"
      @confirm="executeDelete"
    />

    <!-- Base Error Alert Dialog (Confirm err base) -->
    <AppConfirmDialog
      v-model="isErrorDialogOpen"
      type="error"
      :title="errorDialogTitle"
      :message="errorDialogMessage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import {
  Search,
  Plus,
  Receipt,
  Edit2,
  Trash2,
  Save,
  ArrowDownLeft,
  ArrowUpRight,
  ArrowRightLeft,
  Wallet,
} from 'lucide-vue-next';
import AppModal from '../../../components/common/AppModal.vue';
import AppConfirmDialog from '../../../components/common/AppConfirmDialog.vue';
import AppPagination from '../../../components/common/AppPagination.vue';
import CurrencyInput from '../../../components/common/CurrencyInput.vue';
import { useToastStore } from '../../../stores/toast.store';
import { financialService } from '../../../services/financial.service';
import type {
  FinancialTransaction,
  FinancialTransactionRequest,
  FinancialAccount,
  FinancialCategory,
} from '../../../types';
import { formatCurrency, formatDateTime } from '../../../utils/finance';

const emit = defineEmits<{
  (e: 'dataChanged'): void;
}>();

const loading = ref(false);
const submitting = ref(false);
const deleting = ref(false);

const transactions = ref<FinancialTransaction[]>([]);
const accounts = ref<FinancialAccount[]>([]);
const categories = ref<FinancialCategory[]>([]);

// Filter states
const searchQuery = ref('');
const selectedType = ref('');
const selectedAccountId = ref<number | undefined>(undefined);
const selectedCategoryId = ref<number | undefined>(undefined);
const currentPage = ref(0);
const pageSize = ref(20);
const totalPages = ref(1);
const totalElements = ref(0);

// Modal states
const isModalOpen = ref(false);
const isEditing = ref(false);
const editingId = ref<number | null>(null);

const formData = ref<FinancialTransactionRequest>({
  accountId: 0,
  categoryId: null,
  type: 'EXPENSE',
  amount: 0,
  transactionDate: new Date().toISOString().slice(0, 16),
  description: '',
  note: '',
  toAccountId: null,
});

// Delete states
const isDeleteOpen = ref(false);
const txToDelete = ref<FinancialTransaction | null>(null);

const filteredCategories = computed(() => {
  return categories.value.filter((c) => c.type === formData.value.type);
});

async function loadMetadata() {
  try {
    const [accs, cats] = await Promise.all([
      financialService.getAccounts(true),
      financialService.getCategories(undefined, true),
    ]);
    accounts.value = accs;
    categories.value = cats;
  } catch (err) {
    console.error('Failed to load accounts or categories', err);
  }
}

async function loadTransactions() {
  loading.value = true;
  try {
    const res = await financialService.getTransactions({
      search: searchQuery.value.trim() || undefined,
      type: selectedType.value || undefined,
      accountId: selectedAccountId.value,
      categoryId: selectedCategoryId.value,
      page: currentPage.value,
      size: pageSize.value,
    });
    transactions.value = res.items;
    totalPages.value = res.totalPages;
    totalElements.value = res.totalElements;
  } catch (err) {
    console.error('Failed to fetch transactions', err);
  } finally {
    loading.value = false;
  }
}

function applyFilters() {
  currentPage.value = 0;
  loadTransactions();
}

function resetFilters() {
  searchQuery.value = '';
  selectedType.value = '';
  selectedAccountId.value = undefined;
  selectedCategoryId.value = undefined;
  currentPage.value = 0;
  loadTransactions();
}

function changePage(page: number) {
  currentPage.value = page;
  loadTransactions();
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize;
  currentPage.value = 0;
  loadTransactions();
}

const editingTransaction = ref<FinancialTransaction | null>(null);

const selectedSourceAccount = computed(() => {
  return accounts.value.find((a) => a.id === formData.value.accountId);
});

function openAddModal() {
  isEditing.value = false;
  editingId.value = null;
  editingTransaction.value = null;
  const now = new Date();
  const localIso = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);

  formData.value = {
    accountId: accounts.value[0]?.id || 0,
    categoryId: null,
    type: 'EXPENSE',
    amount: 0,
    transactionDate: localIso,
    description: '',
    note: '',
    toAccountId: null,
  };
  isModalOpen.value = true;
}

function openEditModal(tx: FinancialTransaction) {
  isEditing.value = true;
  editingId.value = tx.id;
  editingTransaction.value = tx;

  let formattedDate = tx.transactionDate;
  try {
    formattedDate = new Date(tx.transactionDate).toISOString().slice(0, 16);
  } catch {}

  formData.value = {
    accountId: tx.accountId,
    categoryId: tx.categoryId || null,
    type: tx.type,
    amount: tx.amount,
    transactionDate: formattedDate,
    description: tx.description || '',
    note: tx.note || '',
    toAccountId: tx.toAccountId || null,
  };
  isModalOpen.value = true;
}

const toastStore = useToastStore();

// Error Dialog state (Base confirm err)
const isErrorDialogOpen = ref(false);
const errorDialogTitle = ref('Thông báo lỗi');
const errorDialogMessage = ref('');

function showError(msg: string, title = 'Thông báo lỗi') {
  errorDialogTitle.value = title;
  errorDialogMessage.value = msg;
  isErrorDialogOpen.value = true;
  toastStore.error(msg);
}

async function saveTransaction() {
  if (!formData.value.accountId) {
    showError('Vui lòng chọn tài khoản / ví thanh toán');
    return;
  }
  if (!formData.value.amount || formData.value.amount <= 0) {
    showError('Vui lòng nhập số tiền giao dịch lớn hơn 0');
    return;
  }
  if (formData.value.type === 'TRANSFER') {
    if (!formData.value.toAccountId) {
      showError('Vui lòng chọn tài khoản nhận tiền');
      return;
    }
    if (formData.value.accountId === formData.value.toAccountId) {
      showError('Tài khoản nhận tiền phải khác tài khoản chuyển đi');
      return;
    }
  }

  // Validate số dư ví chi tiền / chuyển đi
  if (formData.value.type === 'EXPENSE' || formData.value.type === 'TRANSFER') {
    const selectedAcc = accounts.value.find((a) => a.id === formData.value.accountId);
    if (selectedAcc && selectedAcc.accountType !== 'CREDIT') {
      const currentBal = selectedAcc.currentBalance ?? 0;
      let effectiveBal = currentBal;
      if (
        isEditing.value &&
        editingTransaction.value &&
        editingTransaction.value.accountId === selectedAcc.id &&
        (editingTransaction.value.type === 'EXPENSE' || editingTransaction.value.type === 'TRANSFER')
      ) {
        effectiveBal += editingTransaction.value.amount || 0;
      }

      if (formData.value.amount > effectiveBal) {
        const action = formData.value.type === 'TRANSFER' ? 'chuyển tiền' : 'chi tiêu';
        showError(
          `Số dư ví "${selectedAcc.name}" không đủ để ${action}! (Hiện có: ${formatCurrency(effectiveBal)}, Cần: ${formatCurrency(formData.value.amount)})`
        );
        return;
      }
    }
  }

  submitting.value = true;
  try {
    if (isEditing.value && editingId.value) {
      await financialService.updateTransaction(editingId.value, formData.value);
      toastStore.success('Cập nhật giao dịch thành công');
    } else {
      await financialService.createTransaction(formData.value);
      toastStore.success('Thêm giao dịch mới thành công');
    }
    isModalOpen.value = false;
    await Promise.all([loadTransactions(), loadMetadata()]);
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi xảy ra khi lưu giao dịch', 'Lỗi lưu giao dịch');
  } finally {
    submitting.value = false;
  }
}

function confirmDelete(tx: FinancialTransaction) {
  txToDelete.value = tx;
  isDeleteOpen.value = true;
}

async function executeDelete() {
  if (!txToDelete.value) return;
  deleting.value = true;
  try {
    await financialService.deleteTransaction(txToDelete.value.id);
    toastStore.success('Đã xóa giao dịch thành công');
    isDeleteOpen.value = false;
    txToDelete.value = null;
    await Promise.all([loadTransactions(), loadMetadata()]);
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi xảy ra khi xóa giao dịch', 'Lỗi xóa giao dịch');
  } finally {
    deleting.value = false;
  }
}

onMounted(() => {
  loadMetadata();
  loadTransactions();
});

defineExpose({
  openAddModal,
  refresh: () => {
    loadMetadata();
    loadTransactions();
  },
});
</script>
