<template>
  <div class="space-y-6">
    <!-- Header & Action Row -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs">
      <div>
        <div class="flex items-center gap-2.5">
          <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Nhật ký luyện nói (Speaking Log)</h2>
          <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-50 text-rose-700 border border-rose-200">
            Tổng {{ totalAllCount }} bài
          </span>
        </div>
        <p class="text-sm sm:text-base text-slate-500 mt-1">Ghi lại các chủ đề nói, bài thu âm và nhận xét phát âm cá nhân</p>
      </div>
      <AppButton variant="primary" size="md" :icon="Plus" @click="openAddModal">
        Ghi bài luyện nói mới
      </AppButton>
    </div>

    <!-- Quick Stats Metric Cards -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
      <!-- Total All -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-rose-50 text-rose-600 shrink-0">
          <Mic class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Tổng bài nói</div>
          <div class="text-lg sm:text-xl font-bold text-slate-900 leading-tight">
            {{ totalAllCount }} <span class="text-xs font-normal text-slate-400">bài</span>
          </div>
        </div>
      </div>

      <!-- Filtered Count -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3" :class="{ 'border-rose-300 bg-rose-50/20': isFilterActive }">
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
        <div class="p-2.5 rounded-md bg-emerald-50 text-emerald-600 shrink-0">
          <CheckCircle2 class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Đã hoàn thành</div>
          <div class="text-lg sm:text-xl font-bold text-emerald-600 leading-tight">
            {{ completedCount }} <span class="text-xs font-normal text-slate-400">bài</span>
          </div>
        </div>
      </div>

      <!-- In Progress / Practicing -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-amber-50 text-amber-600 shrink-0">
          <Flame class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Đang luyện nói</div>
          <div class="text-lg sm:text-xl font-bold text-amber-600 leading-tight">
            {{ inProgressCount }} <span class="text-xs font-normal text-slate-400">bài</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters Row -->
    <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs space-y-3">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
        <AppSearch v-model="filter.search" placeholder="Tìm theo tiêu đề, chủ đề nói..." @search="loadData" />

        <div class="flex items-center gap-2">
          <select
            v-model="filter.sortBy"
            class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
            @change="loadData"
          >
            <option value="createdAt">Mới luyện nói nhất</option>
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
          <BarChart3 class="w-4 h-4 text-rose-600 shrink-0" />
          <span class="font-semibold text-slate-900">
            Kết quả: Tìm thấy <span class="text-rose-600 font-bold text-sm">{{ pageData.totalElements }}</span> / {{ totalAllCount }} bài nói
          </span>
          <span v-if="isFilterActive" class="px-2 py-0.5 rounded bg-rose-100 text-rose-800 font-medium">
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

    <!-- Speaking List Table -->
    <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
      <div v-if="loading" class="py-12 text-center text-sm text-slate-400">
        Đang tải danh sách bài luyện nói...
      </div>

      <div v-else-if="pageData.items.length === 0">
        <AppEmptyState
          :icon="Mic"
          title="Chưa có bài luyện nói nào"
          description="Bắt đầu ghi lại chủ đề luyện nói hôm nay để theo dõi sự tiến bộ về phát âm và độ trôi chảy!"
          action-text="Ghi bài nói mới"
          @action="openAddModal"
        />
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-sm text-slate-800">
          <thead class="bg-slate-50 border-b border-slate-200 text-xs font-bold uppercase text-slate-600 tracking-wider">
            <tr>
              <th class="py-3.5 px-4">Tiêu đề bài nói</th>
              <th class="py-3.5 px-4">Chủ đề (Topic)</th>
              <th class="py-3.5 px-4">Level / Thời lượng</th>
              <th class="py-3.5 px-4">Nhận xét & Ghi chú</th>
              <th class="py-3.5 px-4">Ngày nói</th>
              <th class="py-3.5 px-4">Bản thu / Link</th>
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
              <td class="py-3.5 px-4 text-slate-700 font-medium">
                <span class="px-2.5 py-1 rounded-sm bg-slate-100 text-slate-700 border border-slate-200 text-xs font-semibold">
                  {{ item.topic || 'General' }}
                </span>
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
                {{ formatDate(item.practicedAt || item.createdAt) }}
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
                  Mở bản thu
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
                    title="Xóa bài nói"
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
      :title="isEditing ? 'Chỉnh sửa bài luyện nói' : 'Ghi nhận bài luyện nói mới'"
      size="md"
    >
      <form class="space-y-4" @submit.prevent="saveSpeaking">
        <AppInput
          v-model="form.title"
          label="Tiêu đề bài nói (Title)"
          placeholder="e.g. My self introduction / Describe a memorable journey"
          required
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            v-model="form.topic"
            label="Chủ đề (Topic)"
            placeholder="e.g. IELTS Part 2 / Daily Routine"
          />
          <AppSelect
            v-model="form.level"
            label="Cấp độ (Level)"
            placeholder="Chọn cấp độ"
            :options="levelOptions"
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            v-model="form.durationMinutes"
            label="Thời lượng nói (Phút)"
            type="number"
            placeholder="e.g. 5"
          />
          <AppInput
            v-model="form.url"
            label="Link bản thu âm / Video (Nếu có)"
            placeholder="https://..."
            type="url"
          />
        </div>

        <AppTextarea
          v-model="form.description"
          label="Dàn ý bài nói (Description)"
          placeholder="Các ý chính đã trình bày..."
          rows="2"
        />

        <AppTextarea
          v-model="form.note"
          label="Nhận xét & Điểm cần cải thiện (Note)"
          placeholder="e.g. Cần sửa ngữ điệu, phát âm âm cuối /s/, dùng từ vựng tốt..."
          rows="2"
        />

        <div class="flex justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton variant="secondary" size="sm" @click="showFormModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="sm" :loading="saving">
            {{ isEditing ? 'Lưu thay đổi' : 'Ghi nhận bài nói' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- Delete Confirm Dialog -->
    <AppConfirmDialog
      v-model="showDeleteDialog"
      title="Xác nhận xóa bài luyện nói"
      :message="`Bạn có chắc chắn muốn xóa bài nói '${itemToDelete?.title}' không?`"
      :loading="deleting"
      @confirm="handleDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { speakingService } from '../../services/speaking.service';
import { useToastStore } from '../../stores/toast.store';
import type { SpeakingLesson, PageResponse } from '../../types';
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
import { Plus, Mic, ExternalLink, Edit, Trash2, CheckCircle2, Flame, RotateCcw, Filter, BarChart3 } from 'lucide-vue-next';
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
const itemToDelete = ref<SpeakingLesson | null>(null);

const pageData = ref<PageResponse<SpeakingLesson>>({
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
  return 'Tất cả bài nói';
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
    const resAll = await speakingService.getSpeakingLessons({ page: 0, size: 1 });
    totalAllCount.value = resAll.totalElements;

    const resCompleted = await speakingService.getSpeakingLessons({ status: 'COMPLETED', page: 0, size: 1 });
    completedCount.value = resCompleted.totalElements;
    inProgressCount.value = Math.max(0, totalAllCount.value - completedCount.value);
  } catch (e) {
    // fallback
  }
}

const form = reactive({
  id: 0,
  title: '',
  topic: '',
  level: 'B1',
  durationMinutes: 5,
  url: '',
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
    const res = await speakingService.getSpeakingLessons({
      ...filter,
      fromDate: filter.fromDate || undefined,
      toDate: filter.toDate || undefined,
    });
    pageData.value = res;
    if (!isFilterActive.value && res.totalElements) {
      totalAllCount.value = res.totalElements;
    }
  } catch (err) {
    toastStore.error('Không thể tải danh sách bài luyện nói');
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
  form.topic = 'Daily Conversation';
  form.level = 'B1';
  form.durationMinutes = 5;
  form.url = '';
  form.description = '';
  form.note = '';
  showFormModal.value = true;
}

function openEditModal(item: SpeakingLesson) {
  isEditing.value = true;
  form.id = item.id;
  form.title = item.title;
  form.topic = item.topic || '';
  form.level = item.level || 'B1';
  form.durationMinutes = item.durationSeconds ? Math.round(item.durationSeconds / 60) : 5;
  form.url = item.url || '';
  form.description = item.description || '';
  form.note = item.note || '';
  showFormModal.value = true;
}

async function saveSpeaking() {
  if (!form.title.trim()) {
    toastStore.warning('Vui lòng nhập tiêu đề bài nói');
    return;
  }

  saving.value = true;
  const durationSeconds = (Number(form.durationMinutes) || 0) * 60;

  try {
    if (isEditing.value) {
      await speakingService.updateSpeakingLesson(form.id, {
        title: form.title,
        topic: form.topic,
        level: form.level,
        durationSeconds,
        url: form.url,
        description: form.description,
        note: form.note,
      });
      toastStore.success('Cập nhật bài luyện nói thành công');
    } else {
      await speakingService.createSpeakingLesson({
        title: form.title,
        topic: form.topic,
        level: form.level,
        durationSeconds,
        url: form.url,
        description: form.description,
        note: form.note,
        status: 'COMPLETED',
      });
      toastStore.success('Ghi nhận bài luyện nói mới thành công');
    }
    showFormModal.value = false;
    loadData();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu bài nói');
  } finally {
    saving.value = false;
  }
}

function confirmDelete(item: SpeakingLesson) {
  itemToDelete.value = item;
  showDeleteDialog.value = true;
}

async function handleDelete() {
  if (!itemToDelete.value) return;
  deleting.value = true;
  try {
    await speakingService.deleteSpeakingLesson(itemToDelete.value.id);
    toastStore.success('Đã xóa bài luyện nói thành công');
    showDeleteDialog.value = false;
    loadData();
  } catch (err) {
    toastStore.error('Không thể xóa bài luyện nói');
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
