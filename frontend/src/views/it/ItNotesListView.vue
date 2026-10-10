<template>
  <div class="space-y-6 w-full">
    <!-- Header Banner -->
    <div class="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 rounded-md p-6 sm:p-7 text-white relative overflow-hidden border border-slate-800 shadow-sm">
      <div class="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:20px_20px]"></div>
      <div class="relative z-10 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div class="space-y-1.5">
          <div class="flex items-center gap-2">
            <span class="px-2.5 py-0.5 rounded-sm text-xs font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              IT Knowledge Base
            </span>
            <span class="px-2.5 py-0.5 rounded-sm text-xs font-bold bg-purple-500/20 text-purple-300 border border-purple-500/30">
              Sổ tay Kiến trúc
            </span>
          </div>
          <h1 class="text-xl sm:text-2xl font-bold tracking-tight">
            Kho Lưu trữ & Ghi chép Kiến thức IT
          </h1>
          <p class="text-xs sm:text-sm text-slate-300 max-w-2xl leading-relaxed">
            Tổng hợp bài học kiến trúc hệ thống, sơ đồ Mermaid, mã nguồn và hình ảnh minh họa thực tế.
          </p>
        </div>

        <AppButton
          variant="primary"
          size="md"
          :icon="Plus"
          class="bg-indigo-600 hover:bg-indigo-700 text-white shrink-0 shadow-sm"
          @click="openNewNoteModal"
        >
          Tạo Ghi chú Mới
        </AppButton>
      </div>
    </div>

    <!-- Category Tabs Filter -->
    <div class="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
      <button
        v-for="cat in categories"
        :key="cat.id"
        type="button"
        :class="[
          'px-3.5 py-1.5 rounded-md text-xs font-bold transition-all shrink-0 flex items-center gap-2 border',
          selectedCategory === cat.id
            ? 'bg-indigo-600 text-white border-indigo-600 shadow-xs'
            : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50 hover:text-slate-900'
        ]"
        @click="selectCategory(cat.id)"
      >
        <span>{{ cat.label }}</span>
      </button>
    </div>

    <!-- Filters & Search Toolbar -->
    <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex flex-col sm:flex-row items-center justify-between gap-3">
      <div class="relative w-full sm:w-80">
        <Search class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Tìm theo tiêu đề, nội dung, tags..."
          class="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-md focus:outline-none focus:border-indigo-500 focus:bg-white text-slate-800 transition-colors"
          @input="debounceSearch"
        />
      </div>

      <div class="flex items-center gap-2 w-full sm:w-auto justify-end">
        <button
          type="button"
          :class="[
            'px-3 py-1.5 rounded-md text-xs font-semibold flex items-center gap-1.5 transition-all border',
            onlyFavorites
              ? 'bg-amber-50 text-amber-700 border-amber-300 font-bold shadow-2xs'
              : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
          ]"
          @click="toggleFavoriteFilter"
        >
          <Bookmark class="w-3.5 h-3.5" :class="onlyFavorites ? 'fill-amber-500 text-amber-500' : ''" />
          <span>Yêu thích</span>
        </button>

        <span class="text-xs text-slate-400 hidden sm:inline">
          Tổng cộng: <strong class="text-slate-700">{{ filteredNotes.length }}</strong> bài ghi chép
        </span>
      </div>
    </div>

    <!-- Notes Grid -->
    <div v-if="loadingNotes" class="bg-white p-12 rounded-md border border-slate-200 text-center text-xs text-slate-400">
      Đang tải danh sách ghi chú kiến thức...
    </div>

    <div v-else-if="filteredNotes.length === 0" class="bg-white p-12 rounded-md border border-slate-200 text-center space-y-3">
      <BookOpen class="w-12 h-12 text-slate-300 mx-auto" />
      <h3 class="text-base font-bold text-slate-800">Chưa có bài ghi chú nào trong mục này</h3>
      <p class="text-xs text-slate-500 max-w-sm mx-auto">
        Tạo bài ghi chú đầu tiên kèm hình ảnh minh họa, sơ đồ hoặc lưu trực tiếp từ các cuộc trò chuyện với AI.
      </p>
      <AppButton variant="primary" size="sm" :icon="Plus" class="bg-indigo-600 text-white" @click="openNewNoteModal">
        Tạo ghi chú ngay
      </AppButton>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="note in filteredNotes"
        :key="note.id"
        class="bg-white rounded-md border border-slate-200 p-4 shadow-xs hover:border-indigo-300 hover:shadow-md transition-all flex flex-col justify-between group cursor-pointer space-y-3 overflow-hidden"
        @click="goToNoteDetail(note.id)"
      >
        <div class="space-y-2.5">
          <!-- Thumbnail Image (If Note has images) -->
          <div
            v-if="getFirstImage(note)"
            class="w-full h-36 rounded-md overflow-hidden bg-slate-900 border border-slate-100 relative group-hover:opacity-95 transition-opacity"
          >
            <img
              :src="getFirstImage(note)"
              :alt="note.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
            <span
              v-if="getImageCount(note) > 1"
              class="absolute bottom-2 right-2 bg-slate-900/80 text-white text-[10px] font-bold px-2 py-0.5 rounded-sm backdrop-blur-xs flex items-center gap-1"
            >
              <ImageIcon class="w-3 h-3" />
              <span>+{{ getImageCount(note) - 1 }} ảnh</span>
            </span>
          </div>

          <!-- Top Row: Category Badge & Favorite -->
          <div class="flex items-start justify-between gap-2">
            <span
              class="px-2 py-0.5 rounded-sm text-[10px] font-bold uppercase tracking-wider"
              :class="getCategoryBadgeClass(note.category)"
            >
              {{ formatCategoryName(note.category) }}
            </span>
            <button
              type="button"
              title="Đánh dấu yêu thích"
              class="p-1 rounded-sm text-slate-300 hover:text-amber-500 hover:bg-amber-50 transition-colors"
              :class="note.isFavorite ? 'text-amber-500' : ''"
              @click.stop="toggleFavorite(note)"
            >
              <Bookmark class="w-4 h-4" :class="note.isFavorite ? 'fill-current' : ''" />
            </button>
          </div>

          <!-- Title -->
          <h2 class="font-bold text-slate-900 group-hover:text-indigo-600 transition-colors text-sm sm:text-base line-clamp-2 leading-snug">
            {{ note.title }}
          </h2>

          <!-- Content Excerpt -->
          <p class="text-xs text-slate-600 line-clamp-3 leading-relaxed">
            {{ cleanMarkdownPreview(note.contentMarkdown) }}
          </p>

          <!-- Badges for Diagram and Images -->
          <div class="flex items-center gap-2 flex-wrap">
            <span
              v-if="note.diagramMermaid"
              class="inline-flex items-center gap-1 px-2 py-0.5 rounded-sm bg-indigo-50 text-indigo-700 text-[10px] font-semibold border border-indigo-100"
            >
              <Network class="w-3 h-3" />
              <span>Sơ đồ Mermaid</span>
            </span>

            <span
              v-if="getImageCount(note) > 0"
              class="inline-flex items-center gap-1 px-2 py-0.5 rounded-sm bg-emerald-50 text-emerald-700 text-[10px] font-semibold border border-emerald-100"
            >
              <ImageIcon class="w-3 h-3" />
              <span>{{ getImageCount(note) }} Hình ảnh</span>
            </span>
          </div>

          <!-- Tags -->
          <div v-if="note.tags" class="flex items-center gap-1.5 flex-wrap pt-1">
            <span
              v-for="t in splitTags(note.tags)"
              :key="t"
              class="text-[10px] text-slate-500 bg-slate-100 px-2 py-0.5 rounded-sm"
            >
              #{{ t }}
            </span>
          </div>
        </div>

        <!-- Card Footer -->
        <div class="flex items-center justify-between pt-2.5 border-t border-slate-100 text-[11px] text-slate-400" @click.stop>
          <div class="flex items-center gap-1.5">
            <Calendar class="w-3.5 h-3.5" />
            <span>{{ formatDate(note.updatedAt || note.createdAt) }}</span>
          </div>

          <div class="flex items-center gap-1">
            <button
              type="button"
              title="Xem chi tiết"
              class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-sm transition-colors"
              @click="goToNoteDetail(note.id)"
            >
              <ArrowRight class="w-4 h-4" />
            </button>
            <button
              type="button"
              title="Chỉnh sửa"
              class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-sm transition-colors"
              @click="openEditNoteModal(note)"
            >
              <Edit class="w-3.5 h-3.5" />
            </button>
            <button
              type="button"
              title="Xóa bài"
              class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-sm transition-colors"
              @click="deleteNote(note.id)"
            >
              <Trash2 class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL: TẠO / CHỈNH SỬA GHI CHÚ IT -->
    <AppModal
      v-model="showNoteModal"
      :title="editingNoteId ? 'Chỉnh sửa Ghi chú Kiến thức' : 'Tạo Ghi chú Kiến thức Mới'"
      size="xl"
    >
      <form class="space-y-4" @submit.prevent="saveNote" @paste="handlePaste">
        <AppInput
          v-model="noteForm.title"
          label="Tiêu đề bài học / kiến trúc"
          placeholder="e.g. Tổng quan Apache Kafka Partitioning & Consumer Rebalance"
          required
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Danh mục chuyên ngành
            </label>
            <select
              v-model="noteForm.category"
              class="w-full text-xs bg-white border border-slate-300 rounded-md px-3 py-2 focus:outline-none focus:border-indigo-500 text-slate-800"
            >
              <option value="MICROSERVICES">Microservices & Hệ thống Phân tán</option>
              <option value="KAFKA_DISTRIBUTED">Apache Kafka & Event-Driven</option>
              <option value="DATABASE_OPTIMIZATION">SQL & Database Engine Tuning</option>
              <option value="JAVA_SPRING">Java Spring Boot Enterprise</option>
              <option value="SYSTEM_DESIGN">System Design & High Availability</option>
              <option value="DEVOPS_CLOUD">DevOps & Cloud Architecture</option>
              <option value="DSA_LEETCODE">Cấu trúc Dữ liệu & Giải thuật</option>
            </select>
          </div>

          <AppInput
            v-model="noteForm.tags"
            label="Thẻ Tags (cách nhau dấu phẩy)"
            placeholder="kafka, partition, rebalance, offset"
          />
        </div>

        <!-- IMAGE UPLOAD & MANAGEMENT SECTION -->
        <div class="space-y-2.5 bg-slate-50 p-3.5 rounded-md border border-slate-200">
          <div class="flex items-center justify-between">
            <label class="text-xs font-bold text-slate-800 flex items-center gap-1.5">
              <ImageIcon class="w-4 h-4 text-indigo-600" />
              <span>Hình ảnh Minh họa & Sơ đồ Hệ thống</span>
            </label>
            <span class="text-[11px] text-slate-500">
              Có thể kéo thả, dán (Ctrl+V) hoặc chọn file từ máy
            </span>
          </div>

          <!-- File Upload Button & URL input -->
          <div class="flex flex-wrap items-center gap-2">
            <label class="cursor-pointer inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md text-xs font-semibold shadow-2xs transition-all">
              <Upload class="w-3.5 h-3.5" />
              <span>{{ uploadingImage ? 'Đang tải ảnh...' : 'Chọn ảnh từ máy tính' }}</span>
              <input
                type="file"
                multiple
                accept="image/*"
                class="hidden"
                :disabled="uploadingImage"
                @change="handleFileUpload"
              />
            </label>

            <!-- URL input quick add -->
            <div class="flex items-center gap-1 flex-1 min-w-[200px]">
              <input
                v-model="newImageUrl"
                type="text"
                placeholder="Hoặc dán URL ảnh web (https://...)"
                class="w-full text-xs bg-white border border-slate-300 rounded-md px-2.5 py-1.5 focus:outline-none focus:border-indigo-500"
                @keydown.enter.prevent="addImageUrl"
              />
              <button
                type="button"
                class="px-2.5 py-1.5 bg-slate-200 hover:bg-slate-300 text-slate-700 rounded-md text-xs font-semibold shrink-0"
                @click="addImageUrl"
              >
                Thêm URL
              </button>
            </div>
          </div>

          <!-- Attached Images Grid Preview -->
          <div v-if="attachedImages.length > 0" class="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-2">
            <div
              v-for="(imgUrl, idx) in attachedImages"
              :key="idx"
              class="relative rounded-md overflow-hidden border border-slate-200 bg-white group shadow-2xs"
            >
              <img
                :src="imgUrl"
                alt="Uploaded"
                class="w-full h-20 object-cover"
              />
              <div class="absolute inset-0 bg-slate-900/60 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-1.5">
                <button
                  type="button"
                  title="Chèn ảnh vào bài viết"
                  class="p-1 bg-indigo-600 text-white rounded-sm text-[10px] hover:bg-indigo-700 flex items-center gap-0.5"
                  @click="insertImageToMarkdown(imgUrl)"
                >
                  <Plus class="w-3 h-3" />
                  <span>Chèn</span>
                </button>
                <button
                  type="button"
                  title="Xóa ảnh"
                  class="p-1 bg-rose-600 text-white rounded-sm hover:bg-rose-700"
                  @click="removeAttachedImage(idx)"
                >
                  <Trash2 class="w-3 h-3" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <div>
          <div class="flex items-center justify-between mb-1.5">
            <label class="text-xs font-semibold text-slate-700">
              Nội dung chi tiết (Hỗ trợ Markdown)
            </label>
            <span class="text-[11px] text-slate-400">Dán ảnh (Ctrl+V) để tải ảnh nhanh</span>
          </div>
          <textarea
            ref="contentEditorRef"
            v-model="noteForm.contentMarkdown"
            rows="10"
            required
            class="w-full text-xs font-mono bg-white border border-slate-300 rounded-md p-3 shadow-xs focus:outline-none focus:border-indigo-500"
            placeholder="# 1. Bản chất vấn đề&#10;&#10;**Khái niệm cốt lõi:**&#10;- Ý chính 1...&#10;- Ý chính 2...&#10;&#10;```java&#10;// Code mẫu&#10;```"
          ></textarea>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">
            Mã sơ đồ Mermaid (Tùy chọn)
          </label>
          <textarea
            v-model="noteForm.diagramMermaid"
            rows="4"
            class="w-full text-xs font-mono bg-slate-950 text-emerald-400 border border-slate-800 rounded-md p-3 focus:outline-none"
            placeholder="sequenceDiagram&#10;    Client->>Kafka: Send Event (OrderCreated)&#10;    Kafka-->>Worker: Consume Event"
          ></textarea>
        </div>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" @click="showNoteModal = false">
            Hủy
          </AppButton>
          <AppButton
            type="submit"
            variant="primary"
            size="md"
            class="bg-indigo-600 hover:bg-indigo-700 text-white"
            :loading="savingNote"
          >
            Lưu bài ghi chép
          </AppButton>
        </div>
      </form>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { itStudioService } from '../../services/it-studio.service';
import { useToastStore } from '../../stores/toast.store';
import type { ItNote } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  Plus,
  Search,
  BookOpen,
  Bookmark,
  Edit,
  Trash2,
  Calendar,
  Network,
  Image as ImageIcon,
  ArrowRight,
  Upload,
} from 'lucide-vue-next';

const router = useRouter();
const toastStore = useToastStore();

const notes = ref<ItNote[]>([]);
const loadingNotes = ref(false);
const selectedCategory = ref<string>('ALL');
const searchQuery = ref('');
const onlyFavorites = ref(false);

const categories = [
  { id: 'ALL', label: 'Tất cả' },
  { id: 'MICROSERVICES', label: 'Microservices & Phân tán' },
  { id: 'KAFKA_DISTRIBUTED', label: 'Apache Kafka' },
  { id: 'DATABASE_OPTIMIZATION', label: 'SQL & Database Engine' },
  { id: 'JAVA_SPRING', label: 'Java Spring Boot' },
  { id: 'SYSTEM_DESIGN', label: 'System Design' },
  { id: 'DEVOPS_CLOUD', label: 'DevOps & Cloud' },
  { id: 'DSA_LEETCODE', label: 'Cấu trúc Dữ liệu & Giải thuật' },
];

onMounted(async () => {
  await loadNotes();
});

async function loadNotes() {
  loadingNotes.value = true;
  try {
    const res = await itStudioService.getNotes({
      category: selectedCategory.value === 'ALL' ? undefined : selectedCategory.value,
      search: searchQuery.value.trim() || undefined,
      page: 0,
      size: 50,
    });
    notes.value = res.items;
  } catch (err) {
    console.warn('Failed to load notes:', err);
  } finally {
    loadingNotes.value = false;
  }
}

function selectCategory(catId: string) {
  selectedCategory.value = catId;
  loadNotes();
}

let debounceTimer: any = null;
function debounceSearch() {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    loadNotes();
  }, 350);
}

function toggleFavoriteFilter() {
  onlyFavorites.value = !onlyFavorites.value;
}

const filteredNotes = computed(() => {
  if (onlyFavorites.value) {
    return notes.value.filter((n) => n.isFavorite);
  }
  return notes.value;
});

function goToNoteDetail(id: number) {
  router.push(`/it/notes/${id}`);
}

async function toggleFavorite(note: ItNote) {
  try {
    const updated = await itStudioService.toggleFavorite(note.id);
    note.isFavorite = updated.isFavorite;
    toastStore.success(note.isFavorite ? 'Đã thêm vào yêu thích' : 'Đã bỏ yêu thích');
  } catch (err) {
    toastStore.error('Không thể cập nhật yêu thích');
  }
}

async function deleteNote(id: number) {
  if (!confirm('Bạn có chắc chắn muốn xóa bài ghi chú này?')) return;
  try {
    await itStudioService.deleteNote(id);
    toastStore.success('Đã xóa bài ghi chú');
    notes.value = notes.value.filter((n) => n.id !== id);
  } catch (err) {
    toastStore.error('Không thể xóa bài ghi chú');
  }
}

// -------------------------------------------------------------
// NOTE FORM MODAL WITH IMAGE UPLOADER
// -------------------------------------------------------------
const showNoteModal = ref(false);
const savingNote = ref(false);
const editingNoteId = ref<number | null>(null);
const uploadingImage = ref(false);
const newImageUrl = ref('');
const attachedImages = ref<string[]>([]);
const contentEditorRef = ref<HTMLTextAreaElement | null>(null);

const noteForm = reactive({
  title: '',
  category: 'SYSTEM_DESIGN',
  tags: '',
  contentMarkdown: '',
  diagramMermaid: '',
  imageUrlsJson: '',
});

function openNewNoteModal() {
  editingNoteId.value = null;
  noteForm.title = '';
  noteForm.category = selectedCategory.value === 'ALL' ? 'SYSTEM_DESIGN' : selectedCategory.value;
  noteForm.tags = '';
  noteForm.contentMarkdown = '';
  noteForm.diagramMermaid = '';
  attachedImages.value = [];
  newImageUrl.value = '';
  showNoteModal.value = true;
}

function openEditNoteModal(note: ItNote) {
  editingNoteId.value = note.id;
  noteForm.title = note.title;
  noteForm.category = note.category;
  noteForm.tags = note.tags || '';
  noteForm.contentMarkdown = note.contentMarkdown;
  noteForm.diagramMermaid = note.diagramMermaid || '';
  
  if (note.imageUrlsJson) {
    try {
      const parsed = JSON.parse(note.imageUrlsJson);
      attachedImages.value = Array.isArray(parsed) ? parsed : [];
    } catch {
      attachedImages.value = [note.imageUrlsJson];
    }
  } else {
    attachedImages.value = [];
  }
  newImageUrl.value = '';
  showNoteModal.value = true;
}

async function handleFileUpload(e: Event) {
  const target = e.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) return;

  uploadingImage.value = true;
  try {
    for (let i = 0; i < target.files.length; i++) {
      const file = target.files[i];
      const url = await itStudioService.uploadNoteImage(file);
      attachedImages.value.push(url);
    }
    toastStore.success('Đã tải ảnh lên thành công!');
    target.value = '';
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Tải ảnh thất bại');
  } finally {
    uploadingImage.value = false;
  }
}

async function handlePaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items;
  if (!items) return;

  for (let i = 0; i < items.length; i++) {
    const item = items[i];
    if (item.type.indexOf('image') !== -1) {
      const blob = item.getAsFile();
      if (blob) {
        uploadingImage.value = true;
        try {
          const url = await itStudioService.uploadNoteImage(blob);
          attachedImages.value.push(url);
          insertImageToMarkdown(url);
          toastStore.success('Đã dán và tải ảnh lên tự động!');
        } catch (err) {
          toastStore.error('Không thể tải ảnh dán');
        } finally {
          uploadingImage.value = false;
        }
      }
    }
  }
}

function addImageUrl() {
  const url = newImageUrl.value.trim();
  if (!url) return;
  attachedImages.value.push(url);
  newImageUrl.value = '';
  toastStore.success('Đã thêm URL ảnh');
}

function removeAttachedImage(idx: number) {
  attachedImages.value.splice(idx, 1);
}

function insertImageToMarkdown(url: string) {
  const imgMarkdown = `\n![Hình minh họa](${url})\n`;
  noteForm.contentMarkdown += imgMarkdown;
  toastStore.success('Đã chèn ảnh vào nội dung bài');
}

async function saveNote() {
  if (!noteForm.title.trim() || !noteForm.contentMarkdown.trim()) {
    toastStore.error('Tiêu đề và nội dung không được để trống');
    return;
  }

  noteForm.imageUrlsJson = attachedImages.value.length > 0 ? JSON.stringify(attachedImages.value) : '';

  savingNote.value = true;
  try {
    if (editingNoteId.value) {
      await itStudioService.updateNote(editingNoteId.value, noteForm);
      toastStore.success('Cập nhật bài ghi chép thành công!');
    } else {
      await itStudioService.createNote(noteForm);
      toastStore.success('Tạo bài ghi chép thành công!');
    }
    showNoteModal.value = false;
    await loadNotes();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể lưu ghi chú');
  } finally {
    savingNote.value = false;
  }
}

// Helpers
function formatCategoryName(cat: string) {
  const map: Record<string, string> = {
    MICROSERVICES: 'Microservices',
    KAFKA_DISTRIBUTED: 'Apache Kafka',
    DATABASE_OPTIMIZATION: 'SQL & Database',
    JAVA_SPRING: 'Spring Boot',
    SYSTEM_DESIGN: 'System Design',
    DEVOPS_CLOUD: 'DevOps & Cloud',
    DSA_LEETCODE: 'DSA',
  };
  return map[cat] || cat;
}

function getCategoryBadgeClass(cat: string) {
  const map: Record<string, string> = {
    MICROSERVICES: 'bg-purple-50 text-purple-700 border border-purple-200',
    KAFKA_DISTRIBUTED: 'bg-indigo-50 text-indigo-700 border border-indigo-200',
    DATABASE_OPTIMIZATION: 'bg-teal-50 text-teal-700 border border-teal-200',
    JAVA_SPRING: 'bg-emerald-50 text-emerald-700 border border-emerald-200',
    SYSTEM_DESIGN: 'bg-blue-50 text-blue-700 border border-blue-200',
    DEVOPS_CLOUD: 'bg-orange-50 text-orange-700 border border-orange-200',
    DSA_LEETCODE: 'bg-rose-50 text-rose-700 border border-rose-200',
  };
  return map[cat] || 'bg-slate-100 text-slate-700';
}

function cleanMarkdownPreview(md?: string) {
  if (!md) return '';
  return md.replace(/[#*`_~>[\]]/g, '').replace(/!\[.*?\]\(.*?\)/g, '').trim();
}

function getImageCount(note: ItNote) {
  if (!note.imageUrlsJson) return 0;
  try {
    const parsed = JSON.parse(note.imageUrlsJson);
    return Array.isArray(parsed) ? parsed.length : 0;
  } catch {
    return 0;
  }
}

function getFirstImage(note: ItNote): string | undefined {
  if (!note.imageUrlsJson) return undefined;
  try {
    const parsed = JSON.parse(note.imageUrlsJson);
    return Array.isArray(parsed) && parsed.length > 0 ? parsed[0] : undefined;
  } catch {
    return undefined;
  }
}

function splitTags(tags?: string) {
  if (!tags) return [];
  return tags.split(',').map((t) => t.trim()).filter((t) => t.length > 0);
}

function formatDate(dateStr?: string) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
}
</script>
