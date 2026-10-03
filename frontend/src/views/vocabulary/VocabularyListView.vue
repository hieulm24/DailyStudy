<template>
  <div class="space-y-6">
    <!-- Header & Action Row -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs">
      <div>
        <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Quản lý từ vựng (Vocabulary)</h2>
        <p class="text-sm sm:text-base text-slate-500 mt-1">Tra cứu, thêm mới và quản lý kho từ vựng cá nhân</p>
      </div>
      <AppButton variant="primary" size="md" :icon="Plus" @click="openAddModal">
        + Thêm từ vựng mới
      </AppButton>
    </div>

    <!-- Search & Filters Row -->
    <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs space-y-3">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
        <AppSearch v-model="filter.search" placeholder="Tìm theo từ, nghĩa..." @search="loadData" />

        <div class="flex items-center gap-2">
          <select
            v-model="filter.partOfSpeech"
            class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
            @change="loadData"
          >
            <option value="">Mọi loại từ (Part of Speech)</option>
            <option value="noun">Danh từ (Noun)</option>
            <option value="verb">Động từ (Verb)</option>
            <option value="adjective">Tính từ (Adjective)</option>
            <option value="adverb">Trạng từ (Adverb)</option>
            <option value="preposition">Giới từ (Preposition)</option>
            <option value="conjunction">Liên từ (Conjunction)</option>
            <option value="idiom">Thành ngữ (Idiom / Phrasal Verb)</option>
          </select>

          <select
            v-model="filter.sortBy"
            class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
            @change="loadData"
          >
            <option value="createdAt">Mới thêm nhất</option>
            <option value="word">Theo bảng chữ cái (A-Z)</option>
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

    <!-- Vocabulary List Table -->
    <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
      <div v-if="loading" class="py-12 text-center text-sm text-slate-400">
        Đang tải danh sách từ vựng...
      </div>

      <div v-else-if="pageData.items.length === 0">
        <AppEmptyState
          :icon="BookOpen"
          title="Chưa có từ vựng nào phù hợp"
          description="Hãy thêm từ vựng mới hoặc thay đổi bộ lọc tìm kiếm."
          action-text="+ Thêm từ vựng"
          @action="openAddModal"
        />
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-sm text-slate-800">
          <thead class="bg-slate-50 border-b border-slate-200 text-xs font-bold uppercase text-slate-600 tracking-wider">
            <tr>
              <th class="py-3.5 px-4">Từ vựng (Word)</th>
              <th class="py-3.5 px-4">Nghĩa tiếng Việt (Meaning)</th>
              <th class="py-3.5 px-4">Loại / Level</th>
              <th class="py-3.5 px-4">Ví dụ thực tế</th>
              <th class="py-3.5 px-4">Trạng thái</th>
              <th class="py-3.5 px-4 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="item in pageData.items"
              :key="item.id"
              class="hover:bg-slate-50/80 transition-colors cursor-pointer group"
              @click="openDetailModal(item)"
            >
              <td class="py-3.5 px-4 font-semibold text-slate-900">
                <div class="flex items-baseline gap-2">
                  <span class="text-base font-bold text-brand-600 group-hover:text-brand-700 transition-colors">{{ item.word }}</span>
                  <span v-if="item.pronunciation" class="text-slate-400 font-normal text-xs">{{ item.pronunciation }}</span>
                </div>
              </td>
              <td class="py-3.5 px-4 text-slate-900 font-medium text-sm sm:text-base max-w-xs">
                {{ item.meaning }}
              </td>
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-1.5">
                  <span v-if="item.partOfSpeech" class="px-2 py-0.5 rounded-sm bg-slate-100 text-slate-700 text-xs font-semibold">
                    {{ item.partOfSpeech }}
                  </span>
                  <AppBadge v-if="item.level" :level="item.level">
                    {{ item.level }}
                  </AppBadge>
                </div>
              </td>
              <td class="py-3.5 px-4 text-slate-600 text-sm max-w-sm truncate">
                <span v-if="item.examples && item.examples.length > 0" class="italic">
                  "{{ item.examples[0].exampleSentence }}"
                </span>
                <span v-else class="text-slate-300">-</span>
              </td>
              <td class="py-3.5 px-4">
                <AppBadge :variant="getStatusVariant(item.status)" dot>
                  {{ getStatusLabel(item.status) }}
                </AppBadge>
              </td>
              <td class="py-3.5 px-4 text-right" @click.stop>
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
                    title="Xóa từ vựng"
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
      :title="isEditing ? 'Chỉnh sửa từ vựng' : 'Thêm từ vựng mới'"
      size="lg"
    >
      <form class="space-y-4" @submit.prevent="saveVocabulary">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            v-model="form.word"
            label="Từ vựng (Word)"
            placeholder="e.g. abandon"
            required
          />
          <AppInput
            v-model="form.pronunciation"
            label="Phát âm (Pronunciation)"
            placeholder="e.g. /əˈbæn.dən/"
          />
        </div>

        <AppTextarea
          v-model="form.meaning"
          label="Nghĩa tiếng Việt (Meaning)"
          placeholder="e.g. từ bỏ, ruồng bỏ"
          rows="2"
          required
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppSelect
            v-model="form.partOfSpeech"
            label="Từ loại (Part of Speech)"
            placeholder="Chọn loại từ"
            :options="partOfSpeechOptions"
          />
          <AppSelect
            v-model="form.level"
            label="Cấp độ (Level)"
            placeholder="Chọn cấp độ"
            :options="levelOptions"
          />
        </div>

        <!-- Example Section -->
        <div class="p-3.5 bg-slate-50 rounded-md border border-slate-200 space-y-3">
          <span class="text-xs font-semibold text-slate-700 block">Câu ví dụ thực tế (Example Sentence)</span>
          <AppInput
            v-model="form.exampleSentence"
            placeholder="e.g. He decided to abandon the plan."
          />
          <AppInput
            v-model="form.exampleMeaning"
            placeholder="e.g. Anh ấy quyết định từ bỏ kế hoạch."
          />
        </div>

        <AppTextarea
          v-model="form.note"
          label="Ghi chú cá nhân (Note)"
          placeholder="Mẹo nhớ từ, collocation, ngữ cảnh sử dụng..."
          rows="2"
        />

        <div class="flex justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton variant="secondary" size="sm" @click="showFormModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="sm" :loading="saving">
            {{ isEditing ? 'Lưu thay đổi' : 'Thêm từ vựng' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- Detail Modal -->
    <AppModal
      v-model="showDetailModal"
      title="Chi tiết từ vựng"
      size="md"
    >
      <div v-if="selectedItem" class="space-y-4">
        <div class="flex items-start justify-between pb-3 border-b border-slate-100">
          <div>
            <div class="flex items-center gap-2.5">
              <h3 class="text-2xl font-bold text-brand-600">{{ selectedItem.word }}</h3>
              <span v-if="selectedItem.pronunciation" class="text-sm text-slate-500 font-mono">{{ selectedItem.pronunciation }}</span>
            </div>
            <div class="flex items-center gap-2 mt-1.5">
              <span v-if="selectedItem.partOfSpeech" class="text-sm text-slate-600 font-medium">({{ selectedItem.partOfSpeech }})</span>
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

        <div>
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Nghĩa tiếng Việt</h4>
          <p class="text-base font-semibold text-slate-900 bg-slate-50 p-3.5 rounded-md border border-slate-100">{{ selectedItem.meaning }}</p>
        </div>

        <div v-if="selectedItem.examples && selectedItem.examples.length > 0">
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Câu ví dụ thực tế</h4>
          <div class="p-3.5 bg-brand-50/40 rounded-md border border-brand-100 space-y-1.5">
            <p class="text-sm font-semibold text-slate-900">"{{ selectedItem.examples[0].exampleSentence }}"</p>
            <p v-if="selectedItem.examples[0].meaning" class="text-sm text-slate-600">{{ selectedItem.examples[0].meaning }}</p>
          </div>
        </div>

        <div v-if="selectedItem.note">
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Ghi chú cá nhân</h4>
          <p class="text-sm text-slate-700 bg-slate-50 p-3 rounded-md border border-slate-100">{{ selectedItem.note }}</p>
        </div>

        <div class="flex items-center justify-between pt-3 border-t border-slate-100 text-xs">
          <div class="text-slate-400">
            Thêm ngày: {{ formatDate(selectedItem.createdAt) }}
          </div>
          <div class="flex items-center gap-2">
            <AppButton variant="outline" size="sm" @click="markMastered(selectedItem)">
              Thuộc lòng
            </AppButton>
            <AppButton variant="primary" size="sm" :icon="Edit" @click="openEditModal(selectedItem)">
              Sửa
            </AppButton>
          </div>
        </div>
      </div>
    </AppModal>

    <!-- Delete Confirm Dialog -->
    <AppConfirmDialog
      v-model="showDeleteDialog"
      title="Xác nhận xóa từ vựng"
      :message="`Bạn có chắc chắn muốn xóa từ vựng '${itemToDelete?.word}' không? Hành động này sẽ xóa cả lịch sử ôn tập liên quan.`"
      :loading="deleting"
      @confirm="handleDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { vocabularyService } from '../../services/vocabulary.service';
import { useToastStore } from '../../stores/toast.store';
import { useReviewStore } from '../../stores/review.store';
import type { Vocabulary, PageResponse } from '../../types';
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
import { Plus, BookOpen, Edit, Trash2, Star } from 'lucide-vue-next';

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
const selectedItem = ref<Vocabulary | null>(null);
const itemToDelete = ref<Vocabulary | null>(null);

const pageData = ref<PageResponse<Vocabulary>>({
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
  partOfSpeech: '',
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
  word: '',
  meaning: '',
  pronunciation: '',
  partOfSpeech: '',
  level: 'B1',
  note: '',
  exampleSentence: '',
  exampleMeaning: '',
});

const partOfSpeechOptions = [
  { label: 'Danh từ (Noun)', value: 'noun' },
  { label: 'Động từ (Verb)', value: 'verb' },
  { label: 'Tính từ (Adjective)', value: 'adjective' },
  { label: 'Trạng từ (Adverb)', value: 'adverb' },
  { label: 'Giới từ (Preposition)', value: 'preposition' },
  { label: 'Thành ngữ (Idiom / Phrasal)', value: 'idiom' },
];

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
    const res = await vocabularyService.getVocabularies({
      ...filter,
      fromDate: filter.fromDate || undefined,
      toDate: filter.toDate || undefined,
    });
    pageData.value = res;
  } catch (err) {
    toastStore.error('Không thể tải danh sách từ vựng');
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
  form.word = '';
  form.meaning = '';
  form.pronunciation = '';
  form.partOfSpeech = 'noun';
  form.level = 'B1';
  form.note = '';
  form.exampleSentence = '';
  form.exampleMeaning = '';
  showFormModal.value = true;
}

function openEditModal(item: Vocabulary) {
  isEditing.value = true;
  form.id = item.id;
  form.word = item.word;
  form.meaning = item.meaning;
  form.pronunciation = item.pronunciation || '';
  form.partOfSpeech = item.partOfSpeech || 'noun';
  form.level = item.level || 'B1';
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

function openDetailModal(item: Vocabulary) {
  selectedItem.value = item;
  showDetailModal.value = true;
}

async function saveVocabulary() {
  if (!form.word.trim() || !form.meaning.trim()) {
    toastStore.warning('Vui lòng nhập từ vựng và nghĩa tiếng Việt');
    return;
  }

  saving.value = true;
  try {
    if (isEditing.value) {
      await vocabularyService.updateVocabulary(form.id, {
        word: form.word,
        meaning: form.meaning,
        pronunciation: form.pronunciation,
        partOfSpeech: form.partOfSpeech,
        level: form.level,
        note: form.note,
        exampleSentence: form.exampleSentence,
        exampleMeaning: form.exampleMeaning,
      });
      toastStore.success('Cập nhật từ vựng thành công');
    } else {
      await vocabularyService.createVocabulary({
        word: form.word,
        meaning: form.meaning,
        pronunciation: form.pronunciation,
        partOfSpeech: form.partOfSpeech,
        level: form.level,
        note: form.note,
        exampleSentence: form.exampleSentence,
        exampleMeaning: form.exampleMeaning,
      });
      toastStore.success('Thêm từ vựng mới thành công');
    }
    showFormModal.value = false;
    loadData();
    reviewStore.fetchSummary();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu từ vựng');
  } finally {
    saving.value = false;
  }
}

function confirmDelete(item: Vocabulary) {
  itemToDelete.value = item;
  showDeleteDialog.value = true;
}

async function handleDelete() {
  if (!itemToDelete.value) return;
  deleting.value = true;
  try {
    await vocabularyService.deleteVocabulary(itemToDelete.value.id);
    toastStore.success('Đã xóa từ vựng thành công');
    showDeleteDialog.value = false;
    showDetailModal.value = false;
    loadData();
    reviewStore.fetchSummary();
  } catch (err) {
    toastStore.error('Không thể xóa từ vựng');
  } finally {
    deleting.value = false;
  }
}

async function markMastered(item: Vocabulary) {
  try {
    const updated = await vocabularyService.markAsMastered(item.id);
    selectedItem.value = updated;
    toastStore.success('Đã đánh dấu từ vựng là Thuộc Lòng!');
    loadData();
    reviewStore.fetchSummary();
  } catch (err) {
    toastStore.error('Không thể cập nhật trạng thái');
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
