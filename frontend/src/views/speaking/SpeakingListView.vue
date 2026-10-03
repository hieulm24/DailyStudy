<template>
  <div class="space-y-6">
    <!-- Header & Action Row -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs">
      <div>
        <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Nhật ký luyện nói (Speaking Log)</h2>
        <p class="text-sm sm:text-base text-slate-500 mt-1">Ghi lại các chủ đề nói, bài thu âm và nhận xét phát âm cá nhân</p>
      </div>
      <AppButton variant="primary" size="md" :icon="Plus" @click="openAddModal">
        + Ghi bài luyện nói mới
      </AppButton>
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
          action-text="+ Ghi bài nói"
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
import { Plus, Mic, ExternalLink, Edit, Trash2 } from 'lucide-vue-next';

const route = useRoute();
const toastStore = useToastStore();

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);

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
