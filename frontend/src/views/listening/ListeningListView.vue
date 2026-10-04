<template>
  <div class="space-y-6">
    <!-- Header & Action Row -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs">
      <div>
        <div class="flex items-center gap-2.5">
          <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Nhật ký luyện nghe (Listening Log)</h2>
          <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
            Tổng {{ totalAllCount }} bài
          </span>
        </div>
        <p class="text-sm sm:text-base text-slate-500 mt-1">Ghi chép bài nghe podcast, tin tức, audio đã hoàn thành</p>
      </div>
      <AppButton variant="primary" size="md" :icon="Plus" @click="openAddModal">
        Ghi bài nghe mới
      </AppButton>
    </div>

    <!-- Quick Stats Metric Cards -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
      <!-- Total All -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-emerald-50 text-emerald-600 shrink-0">
          <Headphones class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Tổng bài nghe</div>
          <div class="text-lg sm:text-xl font-bold text-slate-900 leading-tight">
            {{ totalAllCount }} <span class="text-xs font-normal text-slate-400">bài</span>
          </div>
        </div>
      </div>

      <!-- Filtered Count -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3" :class="{ 'border-emerald-300 bg-emerald-50/20': isFilterActive }">
        <div class="p-2.5 rounded-md bg-indigo-50 text-indigo-600 shrink-0">
          <Filter class="w-5 h-5" />
        </div>
        <div class="min-w-0">
          <div class="text-[11px] font-semibold text-slate-500 truncate" :title="currentFilterLabel">{{ currentFilterLabel }}</div>
          <div class="text-lg sm:text-xl font-bold text-indigo-600 leading-tight">
            {{ pageData.totalElements }} <span class="text-xs font-normal text-slate-400">bài</span>
          </div>
        </div>
      </div>

      <!-- Completed -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-teal-50 text-teal-600 shrink-0">
          <CheckCircle2 class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Đã hoàn thành</div>
          <div class="text-lg sm:text-xl font-bold text-teal-600 leading-tight">
            {{ completedCount }} <span class="text-xs font-normal text-slate-400">bài</span>
          </div>
        </div>
      </div>

      <!-- In Progress / Listening -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-amber-50 text-amber-600 shrink-0">
          <Flame class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Đang luyện nghe</div>
          <div class="text-lg sm:text-xl font-bold text-amber-600 leading-tight">
            {{ inProgressCount }} <span class="text-xs font-normal text-slate-400">bài</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters Row -->
    <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs space-y-3">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
        <AppSearch v-model="filter.search" placeholder="Tìm theo tiêu đề, mô tả..." @search="loadData" />

        <div class="flex items-center gap-2">
          <select
            v-model="filter.sortBy"
            class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
            @change="loadData"
          >
            <option value="createdAt">Mới nghe nhất</option>
            <option value="title">Theo tiêu đề (A-Z)</option>
            <option value="durationSeconds">Thời lượng</option>
          </select>
        </div>
      </div>

      <AppFilter
        v-model:date-range="filter.dateRange"
        v-model:from-date="filter.fromDate"
        v-model:to-date="filter.toDate"
        v-model:level="filter.level"
        v-model:status="filter.status"
        @change="loadData"
      />

      <!-- Filter Result Indicator Bar -->
      <div
        class="flex flex-wrap items-center justify-between gap-2 px-3 py-2 rounded-md bg-slate-50 border border-slate-200/80 text-xs"
      >
        <div class="flex items-center gap-2 text-slate-700">
          <BarChart3 class="w-4 h-4 text-emerald-600 shrink-0" />
          <span class="font-semibold text-slate-900">
            Kết quả: Tìm thấy <span class="text-emerald-600 font-bold text-sm">{{ pageData.totalElements }}</span> / {{ totalAllCount }} bài nghe
          </span>
          <span v-if="isFilterActive" class="px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 font-medium">
            (Đang lọc: {{ currentFilterLabel }})
          </span>
        </div>

        <button
          v-if="isFilterActive"
          type="button"
          class="flex items-center gap-1 text-slate-500 hover:text-rose-600 font-medium transition-colors"
          @click="resetFilters"
        >
          <RotateCcw class="w-3.5 h-3.5" />
          <span>Đặt lại bộ lọc</span>
        </button>
      </div>
    </div>

    <!-- Lessons List Table -->
    <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
      <div v-if="loading" class="py-12 text-center text-sm text-slate-400">
        Đang tải danh sách bài nghe...
      </div>

      <div v-else-if="pageData.items.length === 0">
        <AppEmptyState
          :icon="Headphones"
          title="Chưa có bài nghe nào"
          description="Hãy ghi lại bài nghe podcast hoặc video tiếng Anh đầu tiên của bạn hôm nay!"
          action-text="Ghi bài nghe mới"
          @action="openAddModal"
        />
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-sm text-slate-800">
          <thead class="bg-slate-50 border-b border-slate-200 text-xs font-bold uppercase text-slate-600 tracking-wider">
            <tr>
              <th class="py-3.5 px-4">Tên bài nghe</th>
              <th class="py-3.5 px-4">Level / Thời lượng</th>
              <th class="py-3.5 px-4">Ghi chú & Đánh giá</th>
              <th class="py-3.5 px-4">Ngày nghe</th>
              <th class="py-3.5 px-4">Link nguồn</th>
              <th class="py-3.5 px-4 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="item in pageData.items"
              :key="item.id"
              class="hover:bg-slate-50/80 transition-colors"
            >
              <td class="py-3.5 px-4 font-semibold text-slate-900 max-w-xs">
                <div class="font-bold text-slate-900 text-sm sm:text-base">{{ item.title }}</div>
                <div v-if="item.description" class="text-slate-500 text-xs font-normal truncate mt-0.5">
                  {{ item.description }}
                </div>
              </td>
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-2">
                  <AppBadge v-if="item.level" :level="item.level">{{ item.level }}</AppBadge>
                  <span class="text-slate-600 font-medium text-xs">
                    {{ formatDuration(item.durationSeconds) }}
                  </span>
                </div>
              </td>
              <td class="py-3.5 px-4 text-slate-600 max-w-xs truncate text-sm">
                {{ item.note || '-' }}
              </td>
              <td class="py-3.5 px-4 text-slate-500 text-xs">
                {{ formatDate(item.learnedAt || item.createdAt) }}
              </td>
              <td class="py-3.5 px-4">
                <a
                  v-if="item.url"
                  :href="item.url"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="inline-flex items-center gap-1 text-brand-600 hover:text-brand-800 hover:underline font-medium text-sm"
                >
                  <ExternalLink class="w-4 h-4" />
                  Mở bài nghe
                </a>
                <span v-else class="text-slate-300">-</span>
              </td>
              <td class="py-3.5 px-4 text-right">
                <div class="flex items-center justify-end gap-1">
                  <button
                    type="button"
                    title="Chỉnh sửa"
                    class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded-md transition-colors"
                    @click="openEditModal(item)"
                  >
                    <Edit class="w-4 h-4" />
                  </button>
                  <button
                    type="button"
                    title="Xóa bài nghe"
                    class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
                    @click="confirmDelete(item)"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <AppPagination
        :current-page="pageData.page"
        :total-pages="pageData.totalPages"
        :page-size="pageData.size"
        :total-elements="pageData.totalElements"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <!-- Add / Edit Modal -->
    <AppModal
      v-model="showFormModal"
      :title="isEditing ? 'Chỉnh sửa bài nghe' : 'Ghi bài luyện nghe mới'"
      size="md"
    >
      <form class="space-y-4" @submit.prevent="saveListening">
        <AppInput
          v-model="form.title"
          label="Tên / Tiêu đề bài nghe (Title)"
          placeholder="e.g. BBC 6 Minute English - The Power of Reading"
          required
        />

        <AppInput
          v-model="form.url"
          label="Link bài nghe / URL"
          placeholder="https://..."
          type="url"
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppSelect
            v-model="form.level"
            label="Cấp độ (Level)"
            placeholder="Chọn cấp độ"
            :options="levelOptions"
          />
          <AppInput
            v-model="form.durationMinutes"
            label="Thời lượng (Phút)"
            type="number"
            placeholder="e.g. 15"
          />
        </div>

        <AppTextarea
          v-model="form.description"
          label="Tóm tắt nội dung (Description)"
          placeholder="Chủ đề chính, ý nghĩa của bài nghe..."
          rows="2"
        />

        <AppTextarea
          v-model="form.note"
          label="Ghi chú & Tự đánh giá (Note)"
          placeholder="e.g. Nghe được 70%, từ vựng mới: ..., giọng Anh-Mỹ..."
          rows="2"
        />

        <div class="flex justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton variant="secondary" size="sm" @click="showFormModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="sm" :loading="saving">
            {{ isEditing ? 'Lưu thay đổi' : 'Ghi nhận bài nghe' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- Delete Confirm Dialog -->
    <AppConfirmDialog
      v-model="showDeleteDialog"
      title="Xác nhận xóa bài nghe"
      :message="`Bạn có chắc chắn muốn xóa bài nghe '${itemToDelete?.title}' không?`"
      :loading="deleting"
      @confirm="handleDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { listeningService } from '../../services/listening.service';
import { useToastStore } from '../../stores/toast.store';
import type { ListeningLesson, PageResponse } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppTextarea from '../../components/common/AppTextarea.vue';
import AppSelect from '../../components/common/AppSelect.vue';
import AppModal from '../../components/common/AppModal.vue';
import AppBadge from '../../components/common/AppBadge.vue';
import AppPagination from '../../components/common/AppPagination.vue';
import AppEmptyState from '../../components/common/AppEmptyState.vue';
import AppConfirmDialog from '../../components/common/AppConfirmDialog.vue';
import AppSearch from '../../components/common/AppSearch.vue';
import AppFilter from '../../components/common/AppFilter.vue';
import { Plus, Headphones, ExternalLink, Edit, Trash2, CheckCircle2, Flame, RotateCcw, Filter, BarChart3 } from 'lucide-vue-next';
import { computed } from 'vue';

const route = useRoute();
const toastStore = useToastStore();

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);

const totalAllCount = ref(0);
const completedCount = ref(0);
const inProgressCount = ref(0);

const showFormModal = ref(false);
const showDeleteDialog = ref(false);
const isEditing = ref(false);
const itemToDelete = ref<ListeningLesson | null>(null);

const pageData = ref<PageResponse<ListeningLesson>>({
  items: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  isFirst: true,
  isLast: true,
});

const filter = reactive({
  search: '',
  level: '',
  status: '',
  dateRange: '',
  fromDate: '',
  toDate: '',
  page: 0,
  size: 20,
  sortBy: 'createdAt',
  sortDirection: 'DESC',
});

const currentFilterLabel = computed(() => {
  if (filter.dateRange === 'TODAY') return 'Hôm nay';
  if (filter.dateRange === 'YESTERDAY') return 'Hôm qua';
  if (filter.dateRange === 'LAST_7_DAYS') return '7 ngày qua';
  if (filter.dateRange === 'LAST_30_DAYS') return '30 ngày qua';
  if (filter.dateRange === 'CUSTOM' && (filter.fromDate || filter.toDate)) {
    return `Từ ${filter.fromDate || '...'} đến ${filter.toDate || '...'}`;
  }
  if (filter.search) return `Tìm: "${filter.search}"`;
  if (filter.level) return `Cấp độ ${filter.level}`;
  if (filter.status) return `Trạng thái ${getStatusLabel(filter.status)}`;
  return 'Tất cả bài nghe';
});

const isFilterActive = computed(() => {
  return (
    !!filter.search ||
    !!filter.level ||
    !!filter.status ||
    (!!filter.dateRange && filter.dateRange !== 'ALL') ||
    !!filter.fromDate ||
    !!filter.toDate
  );
});

function resetFilters() {
  filter.search = '';
  filter.level = '';
  filter.status = '';
  filter.dateRange = '';
  filter.fromDate = '';
  filter.toDate = '';
  filter.page = 0;
  loadData();
}

async function fetchOverallStats() {
  try {
    const resAll = await listeningService.getListeningLessons({ page: 0, size: 1 });
    totalAllCount.value = resAll.totalElements;

    const resCompleted = await listeningService.getListeningLessons({ status: 'COMPLETED', page: 0, size: 1 });
    completedCount.value = resCompleted.totalElements;
    inProgressCount.value = Math.max(0, totalAllCount.value - completedCount.value);
  } catch (e) {
    // fallback
  }
}

const form = reactive({
  id: 0,
  title: '',
  url: '',
  level: 'B1',
  durationMinutes: 10,
  description: '',
  note: '',
});

const levelOptions = [
  { label: 'A1 - Beginner', value: 'A1' },
  { label: 'A2 - Elementary', value: 'A2' },
  { label: 'B1 - Intermediate', value: 'B1' },
  { label: 'B2 - Upper Intermediate', value: 'B2' },
  { label: 'C1 - Advanced', value: 'C1' },
  { label: 'C2 - Mastery', value: 'C2' },
];

onMounted(() => {
  fetchOverallStats();
  loadData();
  if (route.query.action === 'add') {
    openAddModal();
  }
});

async function loadData() {
  loading.value = true;
  try {
    const res = await listeningService.getListeningLessons({
      ...filter,
      fromDate: filter.fromDate || undefined,
      toDate: filter.toDate || undefined,
    });
    pageData.value = res;
    if (!isFilterActive.value && res.totalElements) {
      totalAllCount.value = res.totalElements;
    }
  } catch (err) {
    toastStore.error('Không thể tải danh sách bài nghe');
  } finally {
    loading.value = false;
  }
}

function handlePageChange(newPage: number) {
  filter.page = newPage;
  loadData();
}

function handlePageSizeChange(newSize: number) {
  filter.size = newSize;
  filter.page = 0;
  loadData();
}

function openAddModal() {
  isEditing.value = false;
  form.id = 0;
  form.title = '';
  form.url = '';
  form.level = 'B1';
  form.durationMinutes = 15;
  form.description = '';
  form.note = '';
  showFormModal.value = true;
}

function openEditModal(item: ListeningLesson) {
  isEditing.value = true;
  form.id = item.id;
  form.title = item.title;
  form.url = item.url || '';
  form.level = item.level || 'B1';
  form.durationMinutes = item.durationSeconds ? Math.round(item.durationSeconds / 60) : 10;
  form.description = item.description || '';
  form.note = item.note || '';
  showFormModal.value = true;
}

async function saveListening() {
  if (!form.title.trim()) {
    toastStore.warning('Vui lòng nhập tiêu đề bài nghe');
    return;
  }

  saving.value = true;
  const durationSeconds = (Number(form.durationMinutes) || 0) * 60;

  try {
    if (isEditing.value) {
      await listeningService.updateListeningLesson(form.id, {
        title: form.title,
        url: form.url,
        level: form.level,
        durationSeconds,
        description: form.description,
        note: form.note,
      });
      toastStore.success('Cập nhật bài nghe thành công');
    } else {
      await listeningService.createListeningLesson({
        title: form.title,
        url: form.url,
        level: form.level,
        durationSeconds,
        description: form.description,
        note: form.note,
        status: 'COMPLETED',
      });
      toastStore.success('Ghi nhận bài nghe mới thành công');
    }
    showFormModal.value = false;
    loadData();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu bài nghe');
  } finally {
    saving.value = false;
  }
}

function confirmDelete(item: ListeningLesson) {
  itemToDelete.value = item;
  showDeleteDialog.value = true;
}

async function handleDelete() {
  if (!itemToDelete.value) return;
  deleting.value = true;
  try {
    await listeningService.deleteListeningLesson(itemToDelete.value.id);
    toastStore.success('Đã xóa bài nghe thành công');
    showDeleteDialog.value = false;
    loadData();
  } catch (err) {
    toastStore.error('Không thể xóa bài nghe');
  } finally {
    deleting.value = false;
  }
}

function getStatusLabel(status: string): string {
  switch (status?.toUpperCase()) {
    case 'NOT_STARTED':
      return 'Chưa học';
    case 'IN_PROGRESS':
      return 'Đang học';
    case 'COMPLETED':
      return 'Hoàn thành';
    default:
      return status;
  }
}

function formatDuration(seconds?: number): string {
  if (!seconds || seconds <= 0) return '0 phút';
  const mins = Math.round(seconds / 60);
  return `${mins} phút`;
}

function formatDate(isoStr?: string): string {
  if (!isoStr) return '';
  return new Date(isoStr).toLocaleDateString('vi-VN');
}
</script>
