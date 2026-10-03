<template>
  <div class="space-y-6">
    <!-- Header & Action Row -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs">
      <div>
        <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Chủ đề ngữ pháp (Grammar Topics)</h2>
        <p class="text-sm sm:text-base text-slate-500 mt-1">Hệ thống hóa cấu trúc, cách dùng, dấu hiệu nhận biết và câu ví dụ</p>
      </div>
      <AppButton variant="primary" size="md" :icon="Plus" @click="openAddModal">
        + Thêm chủ đề ngữ pháp
      </AppButton>
    </div>

    <!-- Search & Filters Row -->
    <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs space-y-3">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
        <AppSearch v-model="filter.search" placeholder="Tìm theo chủ đề, cấu trúc, cách dùng..." @search="loadData" />

        <div class="flex items-center gap-2">
          <select
            v-model="filter.sortBy"
            class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
            @change="loadData"
          >
            <option value="createdAt">Mới thêm nhất</option>
            <option value="topic">Theo tên chủ đề (A-Z)</option>
            <option value="reviewCount">Ôn tập nhiều nhất</option>
            <option value="masteryLevel">Mức độ thông thạo</option>
          </select>
        </div>
      </div>

      <!-- Date & Level Filters -->
      <AppFilter
        v-model:date-range="filter.dateRange"
        v-model:from-date="filter.fromDate"
        v-model:to-date="filter.toDate"
        v-model:level="filter.level"
        v-model:status="filter.status"
        @change="loadData"
      />
    </div>

    <!-- Grammar Grid Cards -->
    <div v-if="loading" class="bg-white p-12 rounded-md border border-slate-200 text-center text-sm text-slate-400">
      Đang tải danh sách ngữ pháp...
    </div>

    <div v-else-if="pageData.items.length === 0" class="bg-white rounded-md border border-slate-200">
      <AppEmptyState
        :icon="Sparkles"
        title="Chưa có chủ đề ngữ pháp nào phù hợp"
        description="Hãy thêm ngữ pháp mới để củng cố nền tảng tiếng Anh của bạn."
        action-text="+ Thêm chủ đề"
        @action="openAddModal"
      />
    </div>

    <div v-else class="space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="item in pageData.items"
          :key="item.id"
          class="bg-white p-5 rounded-md border border-slate-200 shadow-xs hover:border-slate-300 transition-all cursor-pointer flex flex-col justify-between group"
          @click="openDetailModal(item)"
        >
          <div>
            <div class="flex items-start justify-between gap-2 mb-2.5">
              <div class="flex items-center gap-2">
                <h3 class="font-bold text-slate-900 group-hover:text-brand-600 transition-colors text-base sm:text-lg">
                  {{ item.topic }}
                </h3>
                <AppBadge v-if="item.level" :level="item.level">{{ item.level }}</AppBadge>
              </div>
              <AppBadge :variant="getStatusVariant(item.status)" dot>
                {{ getStatusLabel(item.status) }}
              </AppBadge>
            </div>

            <!-- Main Structure Formula -->
            <div v-if="item.structure" class="bg-slate-50 p-2.5 rounded-md border border-slate-100 font-mono text-sm text-brand-700 font-bold mb-2.5">
              {{ item.structure }}
            </div>

            <!-- Usage Preview -->
            <p v-if="item.usage" class="text-sm text-slate-700 line-clamp-2 mb-2.5 leading-relaxed">
              {{ item.usage }}
            </p>

            <!-- Examples Preview -->
            <div v-if="item.examples && item.examples.length > 0" class="text-sm text-slate-600 italic truncate bg-brand-50/30 p-2.5 rounded-sm border border-brand-50">
              "{{ item.examples[0].exampleSentence }}"
            </div>
          </div>

          <!-- Card Footer Actions -->
          <div class="flex items-center justify-between pt-3 mt-3 border-t border-slate-100 text-xs text-slate-500" @click.stop>
            <div class="flex items-center gap-2 text-xs">
              <span>Đã ôn: <strong>{{ item.reviewCount }}</strong> lần</span>
              <span class="inline-flex items-center gap-1">
                <Star class="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
                <strong>{{ item.masteryLevel }}/5</strong>
              </span>
            </div>
            <div class="flex items-center gap-1">
              <button
                type="button"
                title="Sửa"
                class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded-md transition-colors"
                @click="openEditModal(item)"
              >
                <Edit class="w-4 h-4" />
              </button>
              <button
                type="button"
                title="Xóa"
                class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
                @click="confirmDelete(item)"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="bg-white rounded-md border border-slate-200 shadow-xs">
        <AppPagination
          :current-page="pageData.page"
          :total-pages="pageData.totalPages"
          :page-size="pageData.size"
          :total-elements="pageData.totalElements"
          @update:page="handlePageChange"
        />
      </div>
    </div>

    <!-- Add / Edit Modal -->
    <AppModal
      v-model="showFormModal"
      :title="isEditing ? 'Chỉnh sửa chủ đề ngữ pháp' : 'Thêm chủ đề ngữ pháp mới'"
      size="lg"
    >
      <form class="space-y-4" @submit.prevent="saveGrammar">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            v-model="form.topic"
            label="Chủ đề ngữ pháp (Topic)"
            placeholder="e.g. Present Perfect"
            required
          />
          <AppSelect
            v-model="form.level"
            label="Cấp độ (Level)"
            placeholder="Chọn cấp độ"
            :options="levelOptions"
          />
        </div>

        <AppInput
          v-model="form.structure"
          label="Cấu trúc tổng quát (Structure)"
          placeholder="e.g. S + have/has + V3/ed"
        />

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 p-3 bg-slate-50 rounded-md border border-slate-200">
          <AppInput
            v-model="form.positiveStructure"
            label="Khẳng định (+)"
            placeholder="S + have/has + V3"
          />
          <AppInput
            v-model="form.negativeStructure"
            label="Phủ định (-)"
            placeholder="S + have/has + not + V3"
          />
          <AppInput
            v-model="form.questionStructure"
            label="Nghi vấn (?)"
            placeholder="Have/Has + S + V3?"
          />
        </div>

        <AppTextarea
          v-model="form.usage"
          label="Cách dùng (Usage)"
          placeholder="Khi nào sử dụng, ngữ cảnh..."
          rows="2"
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            v-model="form.signalWords"
            label="Dấu hiệu nhận biết (Signal words)"
            placeholder="e.g. already, yet, just, since, for..."
          />
          <AppInput
            v-model="form.commonMistakes"
            label="Lỗi thường gặp (Common mistakes)"
            placeholder="e.g. Nhầm lẫn với Past Simple..."
          />
        </div>

        <!-- Examples section -->
        <div class="p-3 bg-slate-50 rounded-md border border-slate-200 space-y-2">
          <span class="text-xs font-semibold text-slate-700 block">Câu ví dụ minh họa</span>
          <AppInput
            v-model="form.exampleSentence"
            placeholder="e.g. I have lived here for 5 years."
          />
          <AppInput
            v-model="form.exampleMeaning"
            placeholder="e.g. Tôi đã sống ở đây được 5 năm."
          />
        </div>

        <AppTextarea
          v-model="form.note"
          label="Ghi chú cá nhân (Note)"
          placeholder="Quy tắc đặc biệt hoặc ví dụ mở rộng..."
          rows="2"
        />

        <div class="flex justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton variant="secondary" size="sm" @click="showFormModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="sm" :loading="saving">
            {{ isEditing ? 'Lưu thay đổi' : 'Thêm ngữ pháp' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- Detail Modal -->
    <AppModal
      v-model="showDetailModal"
      title="Chi tiết chủ đề ngữ pháp"
      size="lg"
    >
      <div v-if="selectedItem" class="space-y-4">
        <div class="flex items-start justify-between pb-3 border-b border-slate-100">
          <div>
            <div class="flex items-center gap-2.5">
              <h3 class="text-2xl font-bold text-slate-900">{{ selectedItem.topic }}</h3>
              <AppBadge v-if="selectedItem.level" :level="selectedItem.level">{{ selectedItem.level }}</AppBadge>
              <AppBadge :variant="getStatusVariant(selectedItem.status)">{{ getStatusLabel(selectedItem.status) }}</AppBadge>
            </div>
          </div>
          <div class="text-right text-xs text-slate-500">
            <div>Đã ôn: <strong>{{ selectedItem.reviewCount }}</strong> lần</div>
            <div class="mt-0.5 inline-flex items-center gap-1">
              <span>Thông thạo: <strong>{{ selectedItem.masteryLevel }}/5</strong></span>
              <Star class="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
            </div>
          </div>
        </div>

        <!-- Structure Display -->
        <div v-if="selectedItem.structure">
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Cấu trúc công thức</h4>
          <div class="bg-brand-50/50 p-3.5 rounded-md border border-brand-200 font-mono text-base text-brand-800 font-bold">
            {{ selectedItem.structure }}
          </div>
        </div>

        <!-- Sub Structures -->
        <div v-if="selectedItem.positiveStructure || selectedItem.negativeStructure || selectedItem.questionStructure" class="grid grid-cols-1 sm:grid-cols-3 gap-2.5 text-xs">
          <div v-if="selectedItem.positiveStructure" class="p-3 rounded-md bg-slate-50 border border-slate-100">
            <span class="font-bold text-emerald-700 block mb-1 text-xs">Khẳng định (+)</span>
            <span class="font-mono text-slate-800 text-sm font-semibold">{{ selectedItem.positiveStructure }}</span>
          </div>
          <div v-if="selectedItem.negativeStructure" class="p-3 rounded-md bg-slate-50 border border-slate-100">
            <span class="font-bold text-rose-700 block mb-1 text-xs">Phủ định (-)</span>
            <span class="font-mono text-slate-800 text-sm font-semibold">{{ selectedItem.negativeStructure }}</span>
          </div>
          <div v-if="selectedItem.questionStructure" class="p-3 rounded-md bg-slate-50 border border-slate-100">
            <span class="font-bold text-indigo-700 block mb-1 text-xs">Nghi vấn (?)</span>
            <span class="font-mono text-slate-800 text-sm font-semibold">{{ selectedItem.questionStructure }}</span>
          </div>
        </div>

        <!-- Usage -->
        <div v-if="selectedItem.usage">
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Cách dùng & Ngữ cảnh</h4>
          <p class="text-sm sm:text-base text-slate-800 leading-relaxed bg-slate-50 p-3.5 rounded-md border border-slate-100 whitespace-pre-line">{{ selectedItem.usage }}</p>
        </div>

        <!-- Signals and Mistakes -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
          <div v-if="selectedItem.signalWords" class="p-3.5 bg-amber-50/40 rounded-md border border-amber-100">
            <h5 class="font-bold text-amber-800 mb-1 text-xs uppercase tracking-wider">Dấu hiệu nhận biết</h5>
            <p class="text-slate-700 font-medium">{{ selectedItem.signalWords }}</p>
          </div>
          <div v-if="selectedItem.commonMistakes" class="p-3.5 bg-rose-50/40 rounded-md border border-rose-100">
            <h5 class="font-bold text-rose-800 mb-1 text-xs uppercase tracking-wider">Lỗi thường gặp</h5>
            <p class="text-slate-700 font-medium">{{ selectedItem.commonMistakes }}</p>
          </div>
        </div>

        <!-- Examples -->
        <div v-if="selectedItem.examples && selectedItem.examples.length > 0">
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Ví dụ thực tế</h4>
          <div class="p-3.5 bg-slate-50 rounded-md border border-slate-100 space-y-1 text-sm">
            <p class="font-semibold text-slate-900">"{{ selectedItem.examples[0].exampleSentence }}"</p>
            <p v-if="selectedItem.examples[0].meaning" class="text-slate-600">{{ selectedItem.examples[0].meaning }}</p>
          </div>
        </div>

        <!-- Notes -->
        <div v-if="selectedItem.note">
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Ghi chú</h4>
          <p class="text-sm text-slate-700 bg-slate-50 p-3 rounded-md border border-slate-100">{{ selectedItem.note }}</p>
        </div>

        <div class="flex items-center justify-between pt-3 border-t border-slate-100 text-xs">
          <span class="text-slate-400">Ngày thêm: {{ formatDate(selectedItem.createdAt) }}</span>
          <AppButton variant="primary" size="sm" :icon="Edit" @click="openEditModal(selectedItem)">
            Chỉnh sửa
          </AppButton>
        </div>
      </div>
    </AppModal>

    <!-- Delete Confirm Dialog -->
    <AppConfirmDialog
      v-model="showDeleteDialog"
      title="Xác nhận xóa chủ đề ngữ pháp"
      :message="`Bạn có chắc chắn muốn xóa chủ đề '${itemToDelete?.topic}' không?`"
      :loading="deleting"
      @confirm="handleDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { grammarService } from '../../services/grammar.service';
import { useToastStore } from '../../stores/toast.store';
import { useReviewStore } from '../../stores/review.store';
import type { Grammar, PageResponse } from '../../types';
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
import { Plus, Sparkles, Edit, Trash2, Star } from 'lucide-vue-next';

const route = useRoute();
const toastStore = useToastStore();
const reviewStore = useReviewStore();

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);

const showFormModal = ref(false);
const showDetailModal = ref(false);
const showDeleteDialog = ref(false);
const isEditing = ref(false);
const selectedItem = ref<Grammar | null>(null);
const itemToDelete = ref<Grammar | null>(null);

const pageData = ref<PageResponse<Grammar>>({
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
  topic: '',
  level: 'B1',
  structure: '',
  positiveStructure: '',
  negativeStructure: '',
  questionStructure: '',
  usage: '',
  signalWords: '',
  commonMistakes: '',
  note: '',
  exampleSentence: '',
  exampleMeaning: '',
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
    const res = await grammarService.getGrammars({
      ...filter,
      fromDate: filter.fromDate || undefined,
      toDate: filter.toDate || undefined,
    });
    pageData.value = res;
  } catch (err) {
    toastStore.error('Không thể tải danh sách ngữ pháp');
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
  form.topic = '';
  form.level = 'B1';
  form.structure = '';
  form.positiveStructure = '';
  form.negativeStructure = '';
  form.questionStructure = '';
  form.usage = '';
  form.signalWords = '';
  form.commonMistakes = '';
  form.note = '';
  form.exampleSentence = '';
  form.exampleMeaning = '';
  showFormModal.value = true;
}

function openEditModal(item: Grammar) {
  isEditing.value = true;
  form.id = item.id;
  form.topic = item.topic;
  form.level = item.level || 'B1';
  form.structure = item.structure || '';
  form.positiveStructure = item.positiveStructure || '';
  form.negativeStructure = item.negativeStructure || '';
  form.questionStructure = item.questionStructure || '';
  form.usage = item.usage || '';
  form.signalWords = item.signalWords || '';
  form.commonMistakes = item.commonMistakes || '';
  form.note = item.note || '';

  if (item.examples && item.examples.length > 0) {
    form.exampleSentence = item.examples[0].exampleSentence || '';
    form.exampleMeaning = item.examples[0].meaning || '';
  } else {
    form.exampleSentence = '';
    form.exampleMeaning = '';
  }

  showDetailModal.value = false;
  showFormModal.value = true;
}

function openDetailModal(item: Grammar) {
  selectedItem.value = item;
  showDetailModal.value = true;
}

async function saveGrammar() {
  if (!form.topic.trim()) {
    toastStore.warning('Vui lòng nhập tên chủ đề ngữ pháp');
    return;
  }

  saving.value = true;
  try {
    if (isEditing.value) {
      await grammarService.updateGrammar(form.id, {
        topic: form.topic,
        level: form.level,
        structure: form.structure,
        positiveStructure: form.positiveStructure,
        negativeStructure: form.negativeStructure,
        questionStructure: form.questionStructure,
        usage: form.usage,
        signalWords: form.signalWords,
        commonMistakes: form.commonMistakes,
        note: form.note,
        exampleSentence: form.exampleSentence,
        exampleMeaning: form.exampleMeaning,
      });
      toastStore.success('Cập nhật ngữ pháp thành công');
    } else {
      await grammarService.createGrammar({
        topic: form.topic,
        level: form.level,
        structure: form.structure,
        positiveStructure: form.positiveStructure,
        negativeStructure: form.negativeStructure,
        questionStructure: form.questionStructure,
        usage: form.usage,
        signalWords: form.signalWords,
        commonMistakes: form.commonMistakes,
        note: form.note,
        exampleSentence: form.exampleSentence,
        exampleMeaning: form.exampleMeaning,
      });
      toastStore.success('Thêm chủ đề ngữ pháp mới thành công');
    }
    showFormModal.value = false;
    loadData();
    reviewStore.fetchSummary();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu ngữ pháp');
  } finally {
    saving.value = false;
  }
}

function confirmDelete(item: Grammar) {
  itemToDelete.value = item;
  showDeleteDialog.value = true;
}

async function handleDelete() {
  if (!itemToDelete.value) return;
  deleting.value = true;
  try {
    await grammarService.deleteGrammar(itemToDelete.value.id);
    toastStore.success('Đã xóa chủ đề ngữ pháp thành công');
    showDeleteDialog.value = false;
    showDetailModal.value = false;
    loadData();
    reviewStore.fetchSummary();
  } catch (err) {
    toastStore.error('Không thể xóa ngữ pháp');
  } finally {
    deleting.value = false;
  }
}

function getStatusVariant(status: string): 'slate' | 'primary' | 'warning' | 'success' {
  switch (status?.toUpperCase()) {
    case 'NEW':
      return 'primary';
    case 'LEARNING':
      return 'warning';
    case 'REVIEW':
      return 'warning';
    case 'MASTERED':
      return 'success';
    default:
      return 'slate';
  }
}

function getStatusLabel(status: string): string {
  switch (status?.toUpperCase()) {
    case 'NEW':
      return 'Mới';
    case 'LEARNING':
      return 'Đang học';
    case 'REVIEW':
      return 'Cần ôn';
    case 'MASTERED':
      return 'Thuộc lòng';
    default:
      return status;
  }
}

function formatDate(isoStr?: string): string {
  if (!isoStr) return '';
  return new Date(isoStr).toLocaleDateString('vi-VN');
}
</script>
