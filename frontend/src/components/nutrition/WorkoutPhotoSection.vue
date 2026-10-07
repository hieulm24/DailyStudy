<template>
  <div class="bg-white rounded-md border border-slate-200 overflow-hidden shadow-2xs space-y-4 p-5">
    <!-- Section Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pb-4 border-b border-slate-100">
      <div class="flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-rose-50 text-rose-600 border border-rose-100">
          <Camera class="w-5 h-5" />
        </div>
        <div>
          <h2 class="text-base sm:text-lg font-bold text-slate-900">
            Ảnh tập luyện & Check-in vóc dáng
          </h2>
          <p class="text-xs text-slate-500 mt-0.5">
            Lưu giữ hình ảnh buổi tập ngày <strong class="text-slate-700">{{ formattedDate }}</strong> để theo dõi sự thay đổi
          </p>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <button
          type="button"
          :class="[
            'px-3 py-1.5 text-xs font-semibold rounded-md border transition-colors',
            viewMode === 'today'
              ? 'bg-rose-50 text-rose-700 border-rose-200'
              : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
          ]"
          @click="viewMode = 'today'"
        >
          Ngày đang chọn ({{ todayPhotos.length }})
        </button>

        <button
          type="button"
          :class="[
            'px-3 py-1.5 text-xs font-semibold rounded-md border transition-colors',
            viewMode === 'gallery'
              ? 'bg-rose-50 text-rose-700 border-rose-200'
              : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
          ]"
          @click="openGalleryMode"
        >
          <Images class="w-3.5 h-3.5 inline mr-1" />
          <span>Tất cả ảnh (Album)</span>
        </button>

        <AppButton variant="primary" size="sm" :icon="Plus" @click="openUploadModal">
          Thêm ảnh
        </AppButton>
      </div>
    </div>

    <!-- VIEW 1: TODAY'S PHOTOS -->
    <div v-if="viewMode === 'today'">
      <div v-if="loadingToday" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
        <div v-for="i in 3" :key="i" class="h-44 bg-slate-100 rounded-lg animate-pulse" />
      </div>

      <div v-else-if="todayPhotos.length === 0" class="py-8 text-center bg-slate-50/70 rounded-lg border border-dashed border-slate-200 space-y-2.5">
        <div class="w-10 h-10 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto">
          <Camera class="w-5 h-5" />
        </div>
        <div class="space-y-0.5">
          <p class="text-xs font-bold text-slate-700">Chưa có ảnh tập luyện nào cho ngày này</p>
          <p class="text-[11px] text-slate-500">Chụp lại ảnh phòng gym, vóc dáng hoặc bữa ăn để xem lại sau này!</p>
        </div>
        <AppButton variant="outline" size="sm" :icon="Plus" @click="openUploadModal">
          Thêm ảnh ngày hôm nay
        </AppButton>
      </div>

      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3.5">
        <div
          v-for="photo in todayPhotos"
          :key="photo.id"
          class="relative bg-white rounded-lg border border-slate-200 overflow-hidden shadow-2xs group hover:shadow-md transition-all"
        >
          <div class="aspect-square bg-slate-100 relative overflow-hidden cursor-pointer" @click="openPreview(photo)">
            <img
              :src="photo.imageUrl"
              :alt="photo.caption || 'Ảnh tập luyện'"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
            />
            <div class="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white">
              <Maximize2 class="w-5 h-5" />
            </div>
            <span v-if="photo.weightKg" class="absolute top-2 left-2 px-2 py-0.5 text-[10px] font-bold rounded bg-black/60 text-white backdrop-blur-xs inline-flex items-center gap-1">
              <Scale class="w-3 h-3 text-rose-300" />
              <span>{{ photo.weightKg }} kg</span>
            </span>
          </div>

          <div class="p-2.5 space-y-1 bg-white">
            <p v-if="photo.caption" class="text-xs font-semibold text-slate-800 line-clamp-1">
              {{ photo.caption }}
            </p>
            <p v-else class="text-xs text-slate-400 italic">Không có chú thích</p>
            <div class="flex items-center justify-between text-[11px] text-slate-400 pt-1">
              <span>{{ formatTime(photo.createdAt) }}</span>
              <button
                type="button"
                title="Xóa ảnh này"
                class="text-slate-400 hover:text-rose-600 transition-colors p-1"
                @click="confirmDeletePhoto(photo.id)"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- VIEW 2: ALBUM GALLERY -->
    <div v-else-if="viewMode === 'gallery'" class="space-y-4">
      <div v-if="loadingGallery" class="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-3">
        <div v-for="i in 6" :key="i" class="h-36 bg-slate-100 rounded-lg animate-pulse" />
      </div>

      <div v-else-if="allPhotos.length === 0" class="py-8 text-center bg-slate-50/70 rounded-lg border border-dashed border-slate-200 text-xs text-slate-500">
        Chưa có ảnh tập luyện nào trong toàn bộ lịch sử.
      </div>

      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
        <div
          v-for="photo in allPhotos"
          :key="photo.id"
          class="relative bg-white rounded-lg border border-slate-200 overflow-hidden shadow-2xs group hover:shadow-md transition-all"
        >
          <div class="aspect-square bg-slate-100 relative overflow-hidden cursor-pointer" @click="openPreview(photo)">
            <img
              :src="photo.imageUrl"
              :alt="photo.caption || 'Ảnh tập luyện'"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
            />
            <div class="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white">
              <Maximize2 class="w-5 h-5" />
            </div>
            <span class="absolute top-2 left-2 px-1.5 py-0.5 text-[10px] font-bold rounded bg-black/60 text-white backdrop-blur-xs">
              {{ formatDate(photo.logDate) }}
            </span>
          </div>

          <div class="p-2 bg-white flex items-center justify-between text-xs">
            <span class="font-semibold text-slate-700 truncate mr-2">{{ photo.caption || `${photo.weightKg ? photo.weightKg + ' kg' : 'Ảnh tập'}` }}</span>
            <button
              type="button"
              class="text-slate-400 hover:text-rose-600 transition-colors p-1"
              @click="confirmDeletePhoto(photo.id)"
            >
              <Trash2 class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL: TẢI ẢNH TẬP LUYỆN -->
    <AppModal v-model="showUploadModal" title="Thêm ảnh tập luyện / Check-in" size="md">
      <form class="space-y-4" @submit.prevent="submitPhoto">
        <!-- Date Selector -->
        <div>
          <label class="block text-xs font-bold text-slate-700 mb-1">Ngày ghi nhận</label>
          <input
            v-model="uploadForm.logDate"
            type="date"
            class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500"
            required
          />
        </div>

        <!-- File Upload Area -->
        <div>
          <label class="block text-xs font-bold text-slate-700 mb-1">Chọn ảnh từ máy</label>
          <div
            class="border-2 border-dashed border-slate-200 hover:border-rose-400 rounded-lg p-4 text-center cursor-pointer bg-slate-50/50 hover:bg-rose-50/20 transition-all relative"
            @click="triggerFileInput"
          >
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleFileSelected"
            />
            <div v-if="previewUrl" class="space-y-2">
              <img :src="previewUrl" alt="Preview" class="max-h-48 mx-auto rounded-md object-contain shadow-2xs" />
              <p class="text-xs font-semibold text-rose-600">Nhấn để chọn ảnh khác</p>
            </div>
            <div v-else class="space-y-1.5 py-4">
              <UploadCloud class="w-8 h-8 text-slate-400 mx-auto" />
              <p class="text-xs font-bold text-slate-700">Nhấn để chọn ảnh tập luyện</p>
              <p class="text-[11px] text-slate-400">Hỗ trợ JPG, PNG, WEBP</p>
            </div>
          </div>
        </div>

        <!-- Or Image URL input -->
        <div v-if="!selectedFile">
          <label class="block text-xs font-bold text-slate-700 mb-1">Hoặc dán đường dẫn ảnh (URL)</label>
          <AppInput
            id="photo-url"
            v-model="uploadForm.imageUrl"
            placeholder="https://..."
            @input="onUrlInput"
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            id="photo-weight"
            v-model.number="uploadForm.weightKg"
            label="Cân nặng hôm nay (kg) (tùy chọn)"
            type="number"
            step="0.1"
            placeholder="68.5"
          />

          <AppInput
            id="photo-caption"
            v-model="uploadForm.caption"
            label="Ghi chú buổi tập"
            placeholder="Ví dụ: Tập ngực + bụng..."
          />
        </div>

        <div class="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showUploadModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="isUploading" :disabled="!selectedFile && !uploadForm.imageUrl">
            Lưu ảnh tập
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- MODAL: PHÓNG TO XEM ẢNH -->
    <AppModal v-model="showPreviewModal" :title="previewPhoto?.caption || 'Chi tiết ảnh tập luyện'" size="lg">
      <div v-if="previewPhoto" class="space-y-3">
        <div class="bg-black/90 rounded-lg overflow-hidden flex items-center justify-center p-2 max-h-[70vh]">
          <img
            :src="previewPhoto.imageUrl"
            :alt="previewPhoto.caption || 'Ảnh tập luyện'"
            class="max-h-[65vh] w-auto object-contain rounded"
          />
        </div>
        <div class="flex items-center justify-between text-xs text-slate-600 bg-slate-50 p-3 rounded-lg">
          <div>
            <span class="font-bold text-slate-800">Ngày: {{ formatDate(previewPhoto.logDate) }}</span>
            <span v-if="previewPhoto.weightKg" class="ml-3 font-semibold text-rose-600">Cân nặng: {{ previewPhoto.weightKg }} kg</span>
          </div>
          <span class="text-slate-400">{{ formatTime(previewPhoto.createdAt) }}</span>
        </div>
      </div>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import { healthPhotoService } from '../../services/health-photo.service';
import type { HealthWorkoutPhoto } from '../../types/health-photo.types';
import AppButton from '../common/AppButton.vue';
import AppInput from '../common/AppInput.vue';
import AppModal from '../common/AppModal.vue';
import { Camera, Plus, Trash2, Maximize2, Images, UploadCloud, Scale } from 'lucide-vue-next';

const props = defineProps<{
  logDate: string;
}>();

const viewMode = ref<'today' | 'gallery'>('today');
const todayPhotos = ref<HealthWorkoutPhoto[]>([]);
const allPhotos = ref<HealthWorkoutPhoto[]>([]);
const loadingToday = ref(false);
const loadingGallery = ref(false);

const showUploadModal = ref(false);
const showPreviewModal = ref(false);
const previewPhoto = ref<HealthWorkoutPhoto | null>(null);

const fileInputRef = ref<HTMLInputElement | null>(null);
const selectedFile = ref<File | null>(null);
const previewUrl = ref<string | null>(null);
const isUploading = ref(false);

const uploadForm = reactive({
  logDate: props.logDate || new Date().toISOString().split('T')[0],
  imageUrl: '',
  caption: '',
  weightKg: undefined as number | undefined,
});

const formattedDate = computed(() => {
  if (!props.logDate) return '';
  const dt = new Date(props.logDate);
  return dt.toLocaleDateString('vi-VN', { weekday: 'short', day: '2-digit', month: '2-digit', year: 'numeric' });
});

watch(
  () => props.logDate,
  (newDate) => {
    uploadForm.logDate = newDate;
    if (viewMode.value === 'today') {
      loadTodayPhotos();
    }
  },
  { immediate: true }
);

async function loadTodayPhotos() {
  loadingToday.value = true;
  try {
    const res = await healthPhotoService.getPhotosByDate(props.logDate);
    todayPhotos.value = res;
  } catch (err) {
    console.error('Failed to load today photos', err);
  } finally {
    loadingToday.value = false;
  }
}

async function openGalleryMode() {
  viewMode.value = 'gallery';
  loadingGallery.value = true;
  try {
    const res = await healthPhotoService.getAllPhotos(0, 50);
    allPhotos.value = res.items;
  } catch (err) {
    console.error('Failed to load all photos', err);
  } finally {
    loadingGallery.value = false;
  }
}

function openUploadModal() {
  uploadForm.logDate = props.logDate || new Date().toISOString().split('T')[0];
  uploadForm.imageUrl = '';
  uploadForm.caption = '';
  uploadForm.weightKg = undefined;
  selectedFile.value = null;
  previewUrl.value = null;
  showUploadModal.value = true;
}

function triggerFileInput() {
  fileInputRef.value?.click();
}

function handleFileSelected(e: Event) {
  const target = e.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    const file = target.files[0];
    selectedFile.value = file;
    previewUrl.value = URL.createObjectURL(file);
  }
}

function onUrlInput() {
  if (uploadForm.imageUrl) {
    previewUrl.value = uploadForm.imageUrl;
  }
}

async function submitPhoto() {
  isUploading.value = true;
  try {
    if (selectedFile.value) {
      await healthPhotoService.uploadPhoto(
        selectedFile.value,
        uploadForm.logDate,
        uploadForm.caption || undefined,
        uploadForm.weightKg || undefined
      );
    } else if (uploadForm.imageUrl) {
      await healthPhotoService.savePhoto({
        logDate: uploadForm.logDate,
        imageUrl: uploadForm.imageUrl,
        caption: uploadForm.caption || undefined,
        weightKg: uploadForm.weightKg || undefined,
      });
    }
    showUploadModal.value = false;
    await loadTodayPhotos();
    if (viewMode.value === 'gallery') {
      await openGalleryMode();
    }
  } catch (err) {
    console.error('Failed to submit photo', err);
  } finally {
    isUploading.value = false;
  }
}

function openPreview(photo: HealthWorkoutPhoto) {
  previewPhoto.value = photo;
  showPreviewModal.value = true;
}

async function confirmDeletePhoto(id: number) {
  if (!confirm('Bạn có chắc chắn muốn xóa ảnh này?')) return;
  try {
    await healthPhotoService.deletePhoto(id);
    await loadTodayPhotos();
    if (viewMode.value === 'gallery') {
      await openGalleryMode();
    }
  } catch (err) {
    console.error('Failed to delete photo', err);
  }
}

function formatDate(d: string) {
  if (!d) return '';
  const dt = new Date(d);
  return dt.toLocaleDateString('vi-VN');
}

function formatTime(d: string) {
  if (!d) return '';
  const dt = new Date(d);
  return dt.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}
</script>
