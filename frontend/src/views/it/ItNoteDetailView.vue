<template>
  <div class="space-y-6 w-full">
    <!-- Breadcrumb & Back Button -->
    <div class="flex items-center justify-between gap-4">
      <div class="flex items-center gap-2 text-xs text-slate-500">
        <router-link to="/it/notes" class="hover:text-indigo-600 flex items-center gap-1 font-medium transition-colors">
          <ArrowLeft class="w-4 h-4" />
          <span>Quay lại Sổ tay</span>
        </router-link>
        <span>/</span>
        <span class="text-slate-400 truncate max-w-xs">{{ note?.title || 'Chi tiết bài ghi chép' }}</span>
      </div>

      <div class="flex items-center gap-2">
        <button
          type="button"
          :class="[
            'p-2 rounded-md text-xs font-semibold flex items-center gap-1.5 transition-all border',
            note?.isFavorite
              ? 'bg-amber-50 text-amber-700 border-amber-300 font-bold'
              : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
          ]"
          @click="toggleFavorite"
        >
          <Bookmark class="w-4 h-4" :class="note?.isFavorite ? 'fill-amber-500 text-amber-500' : ''" />
          <span class="hidden sm:inline">{{ note?.isFavorite ? 'Đã yêu thích' : 'Yêu thích' }}</span>
        </button>

        <AppButton
          variant="secondary"
          size="sm"
          :icon="Edit"
          @click="openEditModal"
        >
          <span class="hidden sm:inline">Chỉnh sửa</span>
        </AppButton>

        <button
          type="button"
          title="Xóa bài"
          class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 border border-slate-200 rounded-md transition-colors"
          @click="deleteNote"
        >
          <Trash2 class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="bg-white p-16 rounded-md border border-slate-200 text-center text-xs text-slate-400">
      Đang tải nội dung bài học kiến trúc...
    </div>

    <!-- Error State -->
    <div v-else-if="!note" class="bg-white p-16 rounded-md border border-slate-200 text-center space-y-3">
      <AlertCircle class="w-12 h-12 text-rose-500 mx-auto" />
      <h3 class="text-base font-bold text-slate-800">Không tìm thấy bài ghi chép</h3>
      <p class="text-xs text-slate-500">Bài học này có thể đã bị xóa hoặc bạn không có quyền truy cập.</p>
      <AppButton variant="primary" size="sm" @click="$router.push('/it/notes')">
        Trở về danh sách
      </AppButton>
    </div>

    <!-- Main Content View -->
    <template v-else>
      <!-- Title & Meta Header Card -->
      <div class="bg-white rounded-md border border-slate-200 p-6 sm:p-7 shadow-xs space-y-3.5">
        <div class="flex items-center gap-2 flex-wrap">
          <span
            class="px-2.5 py-0.5 rounded-sm text-xs font-bold uppercase tracking-wider"
            :class="getCategoryBadgeClass(note.category)"
          >
            {{ formatCategoryName(note.category) }}
          </span>

          <span class="text-xs text-slate-400 flex items-center gap-1">
            <Calendar class="w-3.5 h-3.5" />
            <span>Cập nhật: {{ formatDate(note.updatedAt || note.createdAt) }}</span>
          </span>
        </div>

        <h1 class="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight leading-snug">
          {{ note.title }}
        </h1>

        <!-- Tags List -->
        <div v-if="note.tags" class="flex items-center gap-2 flex-wrap pt-1">
          <span
            v-for="t in splitTags(note.tags)"
            :key="t"
            class="text-xs text-indigo-600 bg-indigo-50 px-2.5 py-0.5 rounded-sm font-medium border border-indigo-100"
          >
            #{{ t }}
          </span>
        </div>
      </div>

      <!-- Image Gallery Section (If Any) -->
      <div v-if="imagesList.length > 0" class="bg-white rounded-md border border-slate-200 p-6 shadow-xs space-y-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2 text-slate-800 font-bold text-sm">
            <ImageIcon class="w-4 h-4 text-emerald-600" />
            <span>Hình ảnh Minh họa & Sơ đồ Hệ thống ({{ imagesList.length }})</span>
          </div>
          <span class="text-[11px] text-slate-400">Click vào ảnh để xem phóng to</span>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          <div
            v-for="(imgUrl, idx) in imagesList"
            :key="idx"
            class="rounded-md overflow-hidden border border-slate-200 bg-slate-900 group relative cursor-pointer shadow-2xs hover:border-indigo-400 transition-all"
            @click="previewImage(imgUrl)"
          >
            <img
              :src="imgUrl"
              :alt="note.title + ' - Hình ' + (idx + 1)"
              class="w-full h-44 object-contain p-2 group-hover:scale-105 transition-transform duration-300"
            />
            <div class="absolute bottom-2 right-2 bg-slate-900/80 text-white text-[10px] px-2 py-0.5 rounded-sm backdrop-blur-xs flex items-center gap-1">
              <ExternalLink class="w-3 h-3" />
              <span>Phóng to</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Architecture Diagram Section (Mermaid) -->
      <div v-if="note.diagramMermaid" class="bg-slate-950 rounded-md border border-slate-800 p-5 text-white shadow-xs space-y-3">
        <div class="flex items-center justify-between border-b border-slate-800 pb-3">
          <div class="flex items-center gap-2 text-indigo-400 font-bold text-sm">
            <Network class="w-4 h-4" />
            <span>Sơ đồ Kiến trúc Hệ thống (Mermaid)</span>
          </div>

          <button
            type="button"
            class="text-xs text-slate-400 hover:text-white flex items-center gap-1.5 transition-colors bg-slate-900 px-3 py-1.5 rounded-md border border-slate-800"
            @click="copyDiagram"
          >
            <Copy class="w-3.5 h-3.5" />
            <span>Sao chép mã Sơ đồ</span>
          </button>
        </div>

        <!-- Mermaid Code Display -->
        <pre class="overflow-x-auto p-4 bg-slate-900/90 rounded-md text-xs font-mono text-emerald-400 leading-relaxed border border-slate-800">{{ note.diagramMermaid }}</pre>
      </div>

      <!-- Main Detailed Markdown Content -->
      <div class="bg-white rounded-md border border-slate-200 p-6 sm:p-8 shadow-xs space-y-5">
        <div class="flex items-center justify-between border-b border-slate-100 pb-3.5">
          <h2 class="font-bold text-slate-800 text-base flex items-center gap-2">
            <BookOpen class="w-4 h-4 text-indigo-600" />
            <span>Nội dung Chi tiết & Giải pháp Kỹ thuật</span>
          </h2>
          <button
            type="button"
            class="text-xs text-slate-500 hover:text-indigo-600 flex items-center gap-1 transition-colors"
            @click="copyMarkdown"
          >
            <Copy class="w-3.5 h-3.5" />
            <span>Sao chép Markdown</span>
          </button>
        </div>

        <!-- Rendered Text Body -->
        <div class="prose prose-slate max-w-none text-slate-800 leading-relaxed font-sans whitespace-pre-wrap text-sm sm:text-[15px]">
          {{ note.contentMarkdown }}
        </div>
      </div>
    </template>

    <!-- MODAL: EDIT NOTE -->
    <AppModal
      v-model="showEditModal"
      title="Chỉnh sửa Bài ghi chép Kiến thức"
      size="xl"
    >
      <form class="space-y-4" @submit.prevent="saveEdit" @paste="handleEditPaste">
        <AppInput
          v-model="editForm.title"
          label="Tiêu đề bài học / kiến trúc"
          required
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Danh mục chuyên ngành
            </label>
            <select
              v-model="editForm.category"
              class="w-full text-xs bg-white border border-slate-300 rounded-md px-3 py-2 focus:outline-none focus:border-indigo-500"
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
            v-model="editForm.tags"
            label="Thẻ Tags (cách nhau dấu phẩy)"
          />
        </div>

        <!-- IMAGE MANAGEMENT SECTION IN EDIT MODAL -->
        <div class="space-y-2.5 bg-slate-50 p-3.5 rounded-md border border-slate-200">
          <div class="flex items-center justify-between">
            <label class="text-xs font-bold text-slate-800 flex items-center gap-1.5">
              <ImageIcon class="w-4 h-4 text-indigo-600" />
              <span>Hình ảnh Minh họa & Sơ đồ Hệ thống</span>
            </label>
            <span class="text-[11px] text-slate-500">
              Dán ảnh (Ctrl+V) hoặc chọn file từ máy
            </span>
          </div>

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
                @change="handleEditFileUpload"
              />
            </label>

            <div class="flex items-center gap-1 flex-1 min-w-[200px]">
              <input
                v-model="newImageUrl"
                type="text"
                placeholder="Hoặc dán URL ảnh web (https://...)"
                class="w-full text-xs bg-white border border-slate-300 rounded-md px-2.5 py-1.5 focus:outline-none focus:border-indigo-500"
                @keydown.enter.prevent="addEditImageUrl"
              />
              <button
                type="button"
                class="px-2.5 py-1.5 bg-slate-200 hover:bg-slate-300 text-slate-700 rounded-md text-xs font-semibold shrink-0"
                @click="addEditImageUrl"
              >
                Thêm URL
              </button>
            </div>
          </div>

          <div v-if="editImagesList.length > 0" class="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-2">
            <div
              v-for="(imgUrl, idx) in editImagesList"
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
                  @click="insertImageToEditMarkdown(imgUrl)"
                >
                  <Plus class="w-3 h-3" />
                  <span>Chèn</span>
                </button>
                <button
                  type="button"
                  title="Xóa ảnh"
                  class="p-1 bg-rose-600 text-white rounded-sm hover:bg-rose-700"
                  @click="removeEditImage(idx)"
                >
                  <Trash2 class="w-3 h-3" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">
            Nội dung chi tiết (Markdown)
          </label>
          <textarea
            v-model="editForm.contentMarkdown"
            rows="10"
            required
            class="w-full text-xs font-mono bg-white border border-slate-300 rounded-md p-3 focus:outline-none focus:border-indigo-500"
          ></textarea>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">
            Mã sơ đồ Mermaid
          </label>
          <textarea
            v-model="editForm.diagramMermaid"
            rows="4"
            class="w-full text-xs font-mono bg-slate-950 text-emerald-400 border border-slate-800 rounded-md p-3"
          ></textarea>
        </div>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" @click="showEditModal = false">
            Hủy
          </AppButton>
          <AppButton
            type="submit"
            variant="primary"
            size="md"
            class="bg-indigo-600 hover:bg-indigo-700 text-white"
            :loading="saving"
          >
            Lưu thay đổi
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- IMAGE LIGHTBOX MODAL -->
    <div
      v-if="previewImageUrl"
      class="fixed inset-0 z-50 bg-black/90 flex items-center justify-center p-4 backdrop-blur-xs"
      @click="previewImageUrl = null"
    >
      <div class="relative max-w-5xl max-h-[90vh] flex flex-col items-center" @click.stop>
        <button
          type="button"
          class="absolute -top-10 right-0 text-white hover:text-slate-300 p-1"
          @click="previewImageUrl = null"
        >
          <X class="w-6 h-6" />
        </button>
        <img
          :src="previewImageUrl"
          alt="Preview"
          class="max-w-full max-h-[85vh] object-contain rounded-md border border-slate-800 shadow-2xl"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { itStudioService } from '../../services/it-studio.service';
import { useToastStore } from '../../stores/toast.store';
import type { ItNote } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  ArrowLeft,
  Calendar,
  Bookmark,
  Edit,
  Trash2,
  AlertCircle,
  Network,
  Copy,
  BookOpen,
  Image as ImageIcon,
  ExternalLink,
  Upload,
  Plus,
  X,
} from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const toastStore = useToastStore();

const noteId = Number(route.params.id);
const note = ref<ItNote | null>(null);
const loading = ref(false);
const previewImageUrl = ref<string | null>(null);

onMounted(async () => {
  await loadNoteDetail();
});

async function loadNoteDetail() {
  if (!noteId) return;
  loading.value = true;
  try {
    note.value = await itStudioService.getNoteById(noteId);
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể tải bài ghi chép');
  } finally {
    loading.value = false;
  }
}

const imagesList = computed(() => {
  if (!note.value?.imageUrlsJson) return [];
  try {
    const parsed = JSON.parse(note.value.imageUrlsJson);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [note.value.imageUrlsJson];
  }
});

async function toggleFavorite() {
  if (!note.value) return;
  try {
    const updated = await itStudioService.toggleFavorite(note.value.id);
    note.value.isFavorite = updated.isFavorite;
    toastStore.success(note.value.isFavorite ? 'Đã thêm vào yêu thích' : 'Đã bỏ yêu thích');
  } catch (err) {
    toastStore.error('Không thể cập nhật yêu thích');
  }
}

async function deleteNote() {
  if (!note.value) return;
  if (!confirm('Bạn có chắc chắn muốn xóa bài ghi chú này?')) return;
  try {
    await itStudioService.deleteNote(note.value.id);
    toastStore.success('Đã xóa bài ghi chú');
    router.push('/it/notes');
  } catch (err) {
    toastStore.error('Không thể xóa bài ghi chú');
  }
}

function copyMarkdown() {
  if (!note.value?.contentMarkdown) return;
  navigator.clipboard.writeText(note.value.contentMarkdown);
  toastStore.success('Đã sao chép nội dung Markdown');
}

function copyDiagram() {
  if (!note.value?.diagramMermaid) return;
  navigator.clipboard.writeText(note.value.diagramMermaid);
  toastStore.success('Đã sao chép mã Mermaid');
}

function previewImage(url: string) {
  previewImageUrl.value = url;
}

// -------------------------------------------------------------
// EDIT MODAL & IMAGE UPLOAD
// -------------------------------------------------------------
const showEditModal = ref(false);
const saving = ref(false);
const uploadingImage = ref(false);
const newImageUrl = ref('');
const editImagesList = ref<string[]>([]);

const editForm = reactive({
  title: '',
  category: 'SYSTEM_DESIGN',
  tags: '',
  contentMarkdown: '',
  diagramMermaid: '',
  imageUrlsJson: '',
});

function openEditModal() {
  if (!note.value) return;
  editForm.title = note.value.title;
  editForm.category = note.value.category;
  editForm.tags = note.value.tags || '';
  editForm.contentMarkdown = note.value.contentMarkdown;
  editForm.diagramMermaid = note.value.diagramMermaid || '';
  editImagesList.value = [...imagesList.value];
  newImageUrl.value = '';
  showEditModal.value = true;
}

async function handleEditFileUpload(e: Event) {
  const target = e.target as HTMLInputElement;
  if (!target.files || target.files.length === 0) return;

  uploadingImage.value = true;
  try {
    for (let i = 0; i < target.files.length; i++) {
      const file = target.files[i];
      const url = await itStudioService.uploadNoteImage(file);
      editImagesList.value.push(url);
    }
    toastStore.success('Đã tải ảnh lên thành công!');
    target.value = '';
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Tải ảnh thất bại');
  } finally {
    uploadingImage.value = false;
  }
}

async function handleEditPaste(e: ClipboardEvent) {
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
          editImagesList.value.push(url);
          insertImageToEditMarkdown(url);
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

function addEditImageUrl() {
  const url = newImageUrl.value.trim();
  if (!url) return;
  editImagesList.value.push(url);
  newImageUrl.value = '';
  toastStore.success('Đã thêm URL ảnh');
}

function removeEditImage(idx: number) {
  editImagesList.value.splice(idx, 1);
}

function insertImageToEditMarkdown(url: string) {
  const imgMarkdown = `\n![Hình minh họa](${url})\n`;
  editForm.contentMarkdown += imgMarkdown;
  toastStore.success('Đã chèn ảnh vào nội dung bài');
}

async function saveEdit() {
  if (!note.value || !editForm.title.trim() || !editForm.contentMarkdown.trim()) {
    toastStore.error('Tiêu đề và nội dung không được để trống');
    return;
  }

  editForm.imageUrlsJson = editImagesList.value.length > 0 ? JSON.stringify(editImagesList.value) : '';

  saving.value = true;
  try {
    const updated = await itStudioService.updateNote(note.value.id, editForm);
    note.value = updated;
    toastStore.success('Cập nhật bài ghi chép thành công!');
    showEditModal.value = false;
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể lưu ghi chú');
  } finally {
    saving.value = false;
  }
}

// Helpers
function formatCategoryName(cat: string) {
  const map: Record<string, string> = {
    MICROSERVICES: 'Microservices & Phân tán',
    KAFKA_DISTRIBUTED: 'Apache Kafka & Event-Driven',
    DATABASE_OPTIMIZATION: 'SQL & Database Engine',
    JAVA_SPRING: 'Java Spring Boot Enterprise',
    SYSTEM_DESIGN: 'System Design & Kiến trúc',
    DEVOPS_CLOUD: 'DevOps & Cloud',
    DSA_LEETCODE: 'Cấu trúc Dữ liệu & Giải thuật',
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

function splitTags(tags?: string) {
  if (!tags) return [];
  return tags.split(',').map((t) => t.trim()).filter((t) => t.length > 0);
}

function formatDate(dateStr?: string) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
}
</script>
