<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div>
        <h3 class="text-base font-bold text-slate-900">Quản lý danh mục thu & chi</h3>
        <p class="text-xs text-slate-500 mt-0.5">
          Tùy chỉnh danh mục chi tiêu và thu nhập phù hợp với thói quen sinh hoạt cá nhân
        </p>
      </div>

      <div class="flex items-center gap-3 w-full md:w-auto">
        <button
          type="button"
          @click="openAddCategoryModal"
          class="inline-flex items-center justify-center gap-1.5 px-4 py-2 text-xs sm:text-sm font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-md transition-colors shadow-xs"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm danh mục mới</span>
        </button>

        <button
          type="button"
          @click="loadCategories"
          :disabled="loading"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs sm:text-sm font-medium text-slate-700 bg-white border border-slate-200 rounded-md hover:bg-slate-50 transition-colors shadow-xs"
          title="Làm mới"
        >
          <RefreshCw :class="['w-4 h-4 text-slate-500', loading ? 'animate-spin' : '']" />
          <span>Làm mới</span>
        </button>
      </div>
    </div>

    <!-- Category Type Tabs Switcher -->
    <div class="flex items-center gap-2 border-b border-slate-200 pb-2">
      <button
        type="button"
        @click="activeCategoryType = 'EXPENSE'"
        :class="[
          'px-4 py-2 rounded-md font-bold text-xs sm:text-sm flex items-center gap-2 transition-all',
          activeCategoryType === 'EXPENSE'
            ? 'bg-rose-50 text-rose-700 border border-rose-200 shadow-2xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
        ]"
      >
        <ArrowUpRight class="w-4 h-4" />
        <span>Danh mục chi tiêu ({{ expenseCategories.length }})</span>
      </button>

      <button
        type="button"
        @click="activeCategoryType = 'INCOME'"
        :class="[
          'px-4 py-2 rounded-md font-bold text-xs sm:text-sm flex items-center gap-2 transition-all',
          activeCategoryType === 'INCOME'
            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200 shadow-2xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100',
        ]"
      >
        <ArrowDownLeft class="w-4 h-4" />
        <span>Danh mục thu nhập ({{ incomeCategories.length }})</span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-16 bg-white rounded-md border border-slate-200">
      <div class="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      <span class="mt-3 text-xs font-medium text-slate-500">Đang tải danh mục...</span>
    </div>

    <!-- Categories Grid -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div
        v-for="cat in displayedCategories"
        :key="cat.id"
        :class="[
          'bg-white p-4 rounded-md border transition-all flex flex-col justify-between hover:shadow-xs',
          cat.isActive ? 'border-slate-200' : 'border-slate-200/60 bg-slate-50 opacity-60',
        ]"
      >
        <div class="flex items-start justify-between gap-2">
          <div class="flex items-center gap-3 truncate">
            <div
              class="w-10 h-10 rounded-md flex items-center justify-center text-white shrink-0 font-bold"
              :style="{ backgroundColor: cat.color || '#64748b' }"
            >
              <Folder class="w-5 h-5" />
            </div>
            <div class="truncate">
              <h4 class="text-sm font-bold text-slate-900 truncate">{{ cat.name }}</h4>
              <span class="text-[11px] text-slate-400">
                {{ cat.type === 'EXPENSE' ? 'Khoản chi' : 'Khoản thu' }}
                <span v-if="cat.isDefault" class="text-brand-600 font-semibold">• Mặc định</span>
              </span>
            </div>
          </div>

          <span
            :class="[
              'px-2 py-0.5 text-[10px] font-bold rounded-full border shrink-0',
              cat.isActive ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-slate-100 text-slate-500 border-slate-200',
            ]"
          >
            {{ cat.isActive ? 'Dùng' : 'Khóa' }}
          </span>
        </div>

        <!-- Card Actions -->
        <div class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-end gap-1">
          <button
            type="button"
            @click="openEditCategoryModal(cat)"
            class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded transition-colors"
            title="Chỉnh sửa danh mục"
          >
            <Edit2 class="w-3.5 h-3.5" />
          </button>
          <button
            type="button"
            @click="toggleCategoryActive(cat)"
            class="p-1.5 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded transition-colors"
            :title="cat.isActive ? 'Ngừng sử dụng danh mục' : 'Kích hoạt lại'"
          >
            <Power class="w-3.5 h-3.5" />
          </button>
          <button
            type="button"
            @click="confirmDeleteCategory(cat)"
            class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors"
            title="Xóa danh mục"
          >
            <Trash2 class="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>

    <!-- ==========================================
         MODAL: ADD / EDIT CATEGORY
    ========================================== -->
    <AppModal
      v-model="isModalOpen"
      :title="isEditing ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới'"
      size="md"
    >
      <form @submit.prevent="saveCategory" class="space-y-4 sm:space-y-5 py-1 text-xs sm:text-sm">
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Loại danh mục <span class="text-rose-500">*</span></label>
          <select
            v-model="categoryForm.type"
            required
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          >
            <option value="EXPENSE">Chi tiêu (Khoản tiền ra)</option>
            <option value="INCOME">Thu nhập (Khoản tiền vào)</option>
          </select>
        </div>

        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Tên danh mục <span class="text-rose-500">*</span></label>
          <input
            v-model="categoryForm.name"
            type="text"
            required
            placeholder="Ví dụ: Ăn uống, Tiền nhà, Mua sắm, Lương..."
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-900 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          />
        </div>

        <!-- Color Palette Picker -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-2">Màu sắc danh mục</label>
          <div class="flex items-center gap-2.5">
            <button
              v-for="c in colorOptions"
              :key="c"
              type="button"
              @click="categoryForm.color = c"
              :style="{ backgroundColor: c }"
              :class="[
                'w-7 h-7 rounded-full transition-all border-2 cursor-pointer',
                categoryForm.color === c ? 'scale-110 border-slate-800 ring-2 ring-slate-400/40' : 'border-transparent hover:scale-105',
              ]"
            ></button>
          </div>
        </div>

        <div class="flex items-center gap-2.5 pt-1">
          <input
            id="catActiveCheck"
            v-model="categoryForm.isActive"
            type="checkbox"
            class="w-4.5 h-4.5 rounded text-brand-600 focus:ring-brand-500 cursor-pointer"
          />
          <label for="catActiveCheck" class="text-xs sm:text-sm font-medium text-slate-700 cursor-pointer select-none">
            Kích hoạt sử dụng danh mục này
          </label>
        </div>

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
            <span>{{ isEditing ? 'Lưu thay đổi' : 'Thêm danh mục' }}</span>
          </button>
        </div>
      </form>
    </AppModal>

    <!-- Confirm Delete Category Dialog -->
    <AppConfirmDialog
      v-model="isDeleteOpen"
      title="Xác nhận xóa danh mục"
      :message="`Bạn có chắc chắn muốn xóa danh mục '${categoryToDelete?.name}' không? Thao tác này chỉ thực hiện được nếu chưa có giao dịch nào sử dụng danh mục này.`"
      confirm-text="Xóa danh mục"
      confirm-variant="danger"
      :loading="deleting"
      @confirm="executeDeleteCategory"
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
  Plus,
  RefreshCw,
  Folder,
  ArrowUpRight,
  ArrowDownLeft,
  Edit2,
  Power,
  Trash2,
  Save,
} from 'lucide-vue-next';
import AppModal from '../../../components/common/AppModal.vue';
import AppConfirmDialog from '../../../components/common/AppConfirmDialog.vue';
import { useToastStore } from '../../../stores/toast.store';
import { financialService } from '../../../services/financial.service';
import type { FinancialCategory, FinancialCategoryRequest } from '../../../types';

const emit = defineEmits<{
  (e: 'dataChanged'): void;
}>();

const loading = ref(false);
const submitting = ref(false);
const deleting = ref(false);
const categories = ref<FinancialCategory[]>([]);
const activeCategoryType = ref<'EXPENSE' | 'INCOME'>('EXPENSE');

const colorOptions = [
  '#ef4444', // red
  '#f97316', // orange
  '#f59e0b', // amber
  '#10b981', // emerald
  '#06b6d4', // cyan
  '#3b82f6', // blue
  '#6366f1', // indigo
  '#8b5cf6', // purple
  '#ec4899', // pink
  '#64748b', // slate
];

const expenseCategories = computed(() => categories.value.filter((c) => c.type === 'EXPENSE'));
const incomeCategories = computed(() => categories.value.filter((c) => c.type === 'INCOME'));

const displayedCategories = computed(() => {
  return activeCategoryType.value === 'EXPENSE' ? expenseCategories.value : incomeCategories.value;
});

// Modal state
const isModalOpen = ref(false);
const isEditing = ref(false);
const editingId = ref<number | null>(null);

const categoryForm = ref<FinancialCategoryRequest>({
  name: '',
  type: 'EXPENSE',
  color: '#ef4444',
  isActive: true,
});

// Delete state
const isDeleteOpen = ref(false);
const categoryToDelete = ref<FinancialCategory | null>(null);

async function loadCategories() {
  loading.value = true;
  try {
    categories.value = await financialService.getCategories(undefined, false);
  } catch (err) {
    console.error('Failed to load categories', err);
  } finally {
    loading.value = false;
  }
}

function openAddCategoryModal() {
  isEditing.value = false;
  editingId.value = null;
  categoryForm.value = {
    name: '',
    type: activeCategoryType.value,
    color: activeCategoryType.value === 'EXPENSE' ? '#ef4444' : '#10b981',
    isActive: true,
  };
  isModalOpen.value = true;
}

function openEditCategoryModal(cat: FinancialCategory) {
  isEditing.value = true;
  editingId.value = cat.id;
  categoryForm.value = {
    name: cat.name,
    type: cat.type,
    color: cat.color || '#64748b',
    isActive: cat.isActive,
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

async function saveCategory() {
  if (!categoryForm.value.name.trim()) return;
  submitting.value = true;
  try {
    if (isEditing.value && editingId.value) {
      await financialService.updateCategory(editingId.value, categoryForm.value);
      toastStore.success('Cập nhật danh mục thành công');
    } else {
      await financialService.createCategory(categoryForm.value);
      toastStore.success('Thêm danh mục mới thành công');
    }
    isModalOpen.value = false;
    await loadCategories();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi khi lưu danh mục', 'Lỗi lưu danh mục');
  } finally {
    submitting.value = false;
  }
}

async function toggleCategoryActive(cat: FinancialCategory) {
  try {
    await financialService.toggleCategory(cat.id);
    toastStore.success('Đã cập nhật trạng thái danh mục');
    await loadCategories();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi khi cập nhật trạng thái', 'Lỗi cập nhật');
  }
}

function confirmDeleteCategory(cat: FinancialCategory) {
  categoryToDelete.value = cat;
  isDeleteOpen.value = true;
}

async function executeDeleteCategory() {
  if (!categoryToDelete.value) return;
  deleting.value = true;
  try {
    await financialService.deleteCategory(categoryToDelete.value.id);
    toastStore.success('Đã xóa danh mục thành công');
    isDeleteOpen.value = false;
    categoryToDelete.value = null;
    await loadCategories();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Không thể xóa danh mục', 'Lỗi xóa danh mục');
  } finally {
    deleting.value = false;
  }
}

onMounted(() => {
  loadCategories();
});

defineExpose({
  openAddModal: openAddCategoryModal,
  refresh: loadCategories,
});
</script>
