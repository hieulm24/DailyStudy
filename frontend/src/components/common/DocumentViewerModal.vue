<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-center justify-center p-2 sm:p-4 bg-slate-950/75 backdrop-blur-sm transition-all"
      @click.self="closeViewer"
    >
      <div
        :class="[
          'relative w-full bg-white rounded-lg shadow-2xl border border-slate-200 flex flex-col overflow-hidden transition-all duration-200',
          isFullscreen
            ? 'fixed inset-0 h-full w-full rounded-none border-0'
            : 'max-w-6xl h-[92vh] max-h-[95vh]',
        ]"
      >
        <!-- Modal Top Bar Header -->
        <div class="px-4 sm:px-6 py-3 bg-slate-900 text-white flex items-center justify-between gap-3 shrink-0 border-b border-slate-800">
          <div class="flex items-center gap-3 min-w-0 flex-1">
            <div :class="['w-9 h-9 rounded-md flex items-center justify-center shrink-0 border border-white/10', headerIconStyle.bg]">
              <component :is="headerIconStyle.icon" :class="['w-5 h-5', headerIconStyle.color]" />
            </div>
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <h3 class="text-sm sm:text-base font-bold text-white truncate" :title="document?.title || document?.fileName">
                  {{ document?.title || document?.fileName }}
                </h3>
                <span :class="['px-2 py-0.5 text-xs font-bold rounded-md uppercase shrink-0 border', headerIconStyle.badge]">
                  {{ detectedFormat }}
                </span>
              </div>
              <div class="flex items-center gap-3 text-xs text-slate-400 mt-0.5 truncate">
                <span class="font-mono truncate">{{ document?.fileName }}</span>
                <span class="hidden sm:inline">•</span>
                <span class="hidden sm:inline font-mono">{{ document?.formattedFileSize }}</span>
                <span v-if="document?.category" class="hidden md:inline">•</span>
                <span v-if="document?.category" class="hidden md:inline">{{ getCategoryLabel(document?.category) }}</span>
              </div>
            </div>
          </div>

          <!-- Header Action Buttons -->
          <div class="flex items-center gap-1.5 shrink-0">
            <!-- Download Button -->
            <button
              type="button"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-md bg-brand-600 hover:bg-brand-500 text-white transition-colors shadow-xs"
              title="Tải tài liệu về máy tính"
              @click="downloadCurrentDoc"
            >
              <Download class="w-4 h-4" />
              <span class="hidden sm:inline">Tải về</span>
            </button>

            <!-- Open in new tab (for PDF / Image) -->
            <a
              v-if="blobUrl && (detectedFormat === 'PDF' || detectedFormat === 'IMAGE')"
              :href="blobUrl"
              target="_blank"
              class="p-2 rounded-md text-slate-300 hover:text-white hover:bg-white/10 transition-colors"
              title="Mở trong tab trình duyệt mới"
            >
              <ExternalLink class="w-4.5 h-4.5" />
            </a>

            <!-- Fullscreen Toggle -->
            <button
              type="button"
              class="p-2 rounded-md text-slate-300 hover:text-white hover:bg-white/10 transition-colors"
              :title="isFullscreen ? 'Thu nhỏ cửa sổ' : 'Xem toàn màn hình'"
              @click="toggleFullscreen"
            >
              <Minimize2 v-if="isFullscreen" class="w-4.5 h-4.5" />
              <Maximize2 v-else class="w-4.5 h-4.5" />
            </button>

            <!-- Close Button -->
            <button
              type="button"
              class="p-2 rounded-md text-slate-400 hover:text-rose-400 hover:bg-white/10 transition-colors ml-1"
              title="Đóng (Esc)"
              @click="closeViewer"
            >
              <X class="w-5 h-5" />
            </button>
          </div>
        </div>

        <!-- Format-Specific Toolbar / Controls -->
        <!-- 1. Excel Toolbar (Sheet tabs + In-sheet search) -->
        <div
          v-if="!isLoading && !loadError && detectedFormat === 'EXCEL'"
          class="px-4 py-2 bg-slate-100 border-b border-slate-200 flex flex-wrap items-center justify-between gap-3 shrink-0"
        >
          <!-- Sheet Tabs -->
          <div class="flex items-center gap-1.5 overflow-x-auto max-w-full py-0.5">
            <span class="text-xs font-semibold text-slate-500 mr-1 shrink-0 flex items-center gap-1">
              <FileSpreadsheet class="w-3.5 h-3.5 text-emerald-600" />
              Trang tính:
            </span>
            <button
              v-for="sheet in excelState.sheetNames"
              :key="sheet"
              type="button"
              :class="[
                'px-3 py-1 text-xs font-semibold rounded-md transition-all shrink-0 flex items-center gap-1.5 border',
                excelState.activeSheet === sheet
                  ? 'bg-emerald-600 text-white border-emerald-600 shadow-xs'
                  : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50',
              ]"
              @click="selectExcelSheet(sheet)"
            >
              <span>{{ sheet }}</span>
              <span
                v-if="excelState.activeSheet === sheet && excelState.rawRows.length > 0"
                class="bg-white/20 text-white px-1.5 py-0.2 rounded-full text-[10px]"
              >
                {{ excelState.rawRows.length }} dòng
              </span>
            </button>
          </div>

          <!-- In-sheet Search & Row Counter -->
          <div class="flex items-center gap-3 shrink-0">
            <div class="relative w-48 sm:w-64">
              <Search class="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
              <input
                v-model="excelState.searchQuery"
                type="text"
                placeholder="Lọc dữ liệu trong bảng..."
                class="w-full text-xs pl-8 pr-3 py-1.5 bg-white border border-slate-200 rounded-md focus:outline-none focus:border-emerald-500 shadow-xs"
              />
              <button
                v-if="excelState.searchQuery"
                type="button"
                class="absolute right-2 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                @click="excelState.searchQuery = ''"
              >
                <X class="w-3.5 h-3.5" />
              </button>
            </div>
            <span class="text-xs text-slate-500 font-medium hidden sm:inline">
              Hiển thị <strong class="text-slate-800">{{ excelFilteredRows.length }}</strong> / {{ excelState.rawRows.length }} dòng
            </span>
          </div>
        </div>

        <!-- 2. Image Toolbar (Zoom & Rotate) -->
        <div
          v-if="!isLoading && !loadError && detectedFormat === 'IMAGE'"
          class="px-4 py-2 bg-slate-100 border-b border-slate-200 flex items-center justify-between gap-3 shrink-0"
        >
          <div class="flex items-center gap-1.5">
            <button
              type="button"
              class="p-1.5 bg-white rounded-md border border-slate-200 text-slate-700 hover:bg-slate-50 shadow-xs"
              title="Phóng to"
              @click="zoomIn"
            >
              <ZoomIn class="w-4 h-4" />
            </button>
            <button
              type="button"
              class="p-1.5 bg-white rounded-md border border-slate-200 text-slate-700 hover:bg-slate-50 shadow-xs"
              title="Thu nhỏ"
              @click="zoomOut"
            >
              <ZoomOut class="w-4 h-4" />
            </button>
            <button
              type="button"
              class="px-2.5 py-1 bg-white rounded-md border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-50 shadow-xs"
              title="Đặt lại kích thước 100%"
              @click="resetZoom"
            >
              {{ imageState.zoom }}%
            </button>
            <button
              type="button"
              class="p-1.5 bg-white rounded-md border border-slate-200 text-slate-700 hover:bg-slate-50 shadow-xs ml-2"
              title="Xoay 90 độ"
              @click="rotateImage"
            >
              <RotateCw class="w-4 h-4" />
            </button>
          </div>
          <span class="text-xs text-slate-500">
            Cuộn chuột hoặc dùng phím điều khiển để xem ảnh rõ nét
          </span>
        </div>

        <!-- 3. Text Toolbar (Copy all text, line counter) -->
        <div
          v-if="!isLoading && !loadError && detectedFormat === 'TEXT'"
          class="px-4 py-2 bg-slate-100 border-b border-slate-200 flex items-center justify-between gap-3 shrink-0"
        >
          <div class="flex items-center gap-2 text-xs text-slate-600">
            <FileText class="w-4 h-4 text-slate-500" />
            <span>Tổng cộng: <strong>{{ textLinesCount }}</strong> dòng văn bản</span>
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-1.5 px-3 py-1 bg-white border border-slate-200 rounded-md text-xs font-semibold text-slate-700 hover:bg-slate-50 shadow-xs"
            @click="copyTextContent"
          >
            <Check v-if="textState.isCopied" class="w-3.5 h-3.5 text-emerald-600" />
            <Copy v-else class="w-3.5 h-3.5 text-slate-500" />
            <span>{{ textState.isCopied ? 'Đã sao chép!' : 'Sao chép nội dung' }}</span>
          </button>
        </div>

        <!-- Modal Viewer Body Container -->
        <div class="flex-1 bg-slate-100 overflow-hidden relative min-h-0 flex flex-col">
          <!-- Error State / Unsupported Binary Fallback -->
          <div v-if="loadError" class="flex-1 flex items-center justify-center p-6 sm:p-10">
            <div class="bg-white p-8 rounded-lg border border-slate-200 shadow-sm max-w-lg text-center space-y-4">
              <div class="w-14 h-14 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center mx-auto border border-rose-100">
                <AlertCircle class="w-7 h-7" />
              </div>
              <div>
                <h4 class="text-base font-bold text-slate-900">Không thể xem trực tiếp tệp này</h4>
                <p class="text-xs sm:text-sm text-slate-500 mt-1.5 leading-relaxed">
                  {{ loadError }}
                </p>
              </div>
              <div class="pt-2 flex flex-col sm:flex-row items-center justify-center gap-2">
                <button
                  type="button"
                  class="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-4 py-2 rounded-md bg-brand-600 hover:bg-brand-700 text-white text-xs font-bold transition-all shadow-xs"
                  @click="downloadCurrentDoc"
                >
                  <Download class="w-4 h-4" />
                  <span>Tải tệp tin về máy tính</span>
                </button>
                <button
                  type="button"
                  class="w-full sm:w-auto px-4 py-2 rounded-md border border-slate-200 hover:bg-slate-50 text-slate-700 text-xs font-semibold transition-colors"
                  @click="closeViewer"
                >
                  Đóng
                </button>
              </div>
            </div>
          </div>

          <!-- Active Document Viewers (Always mounted while no fatal error) -->
          <div v-else class="flex-1 relative overflow-hidden flex flex-col min-h-0">
            <!-- Loading Overlay (Independent from viewer DOM) -->
            <div v-if="isLoading" class="absolute inset-0 z-20 flex flex-col items-center justify-center bg-white/90 backdrop-blur-xs">
              <Loader2 class="w-10 h-10 animate-spin text-brand-600 mb-3" />
              <p class="text-sm font-semibold text-slate-800">Đang tải và chuẩn bị nội dung tài liệu...</p>
              <p class="text-xs text-slate-400 mt-1">Đang render dữ liệu trực tiếp trong trình duyệt</p>
            </div>

            <!-- 1. PDF Viewer -->
            <div v-if="detectedFormat === 'PDF'" class="w-full h-full min-h-0 flex-1 bg-slate-900">
              <iframe
                v-if="blobUrl"
                :src="blobUrl"
                class="w-full h-full border-0"
                title="PDF Preview"
              ></iframe>
            </div>

            <!-- 2. Word (.docx) Viewer -->
            <div
              v-else-if="detectedFormat === 'WORD'"
              class="w-full h-full overflow-y-auto p-4 sm:p-8 flex justify-center bg-slate-200/80 custom-scrollbar"
            >
              <div
                ref="docxContainerRef"
                class="docx-render-host bg-white shadow-md rounded-md max-w-4xl w-full min-h-[500px]"
              ></div>
            </div>

            <!-- 3. Excel (.xlsx, .xls, .csv) Viewer -->
            <div v-else-if="detectedFormat === 'EXCEL'" class="w-full h-full overflow-auto bg-white flex flex-col">
              <div v-if="excelFilteredRows.length === 0" class="p-12 text-center text-slate-400">
                <FileSpreadsheet class="w-10 h-10 mx-auto text-slate-300 mb-2" />
                <p class="text-sm font-semibold text-slate-600">Không có dữ liệu phù hợp trong trang tính này</p>
                <p v-if="excelState.searchQuery" class="text-xs text-slate-400 mt-1">
                  Thử xóa từ khóa tìm kiếm "{{ excelState.searchQuery }}"
                </p>
              </div>

              <div v-else class="flex-1 overflow-auto">
                <table class="w-full border-collapse text-left text-xs font-mono select-text">
                  <thead class="bg-slate-100/90 sticky top-0 z-10 border-b border-slate-300 shadow-xs">
                    <tr>
                      <!-- Row number header -->
                      <th class="py-2.5 px-3 w-14 text-center bg-slate-200/90 text-slate-600 font-bold border-r border-slate-300 select-none">
                        #
                      </th>
                      <!-- Data columns headers -->
                      <th
                        v-for="(header, colIdx) in excelHeaders"
                        :key="colIdx"
                        class="py-2.5 px-3 font-bold text-slate-800 border-r border-slate-200 last:border-r-0 whitespace-nowrap bg-slate-100"
                      >
                        <div class="flex items-center justify-between gap-2">
                          <span>{{ header || getExcelColumnLetter(colIdx) }}</span>
                          <span class="text-[10px] text-slate-400 font-normal">({{ getExcelColumnLetter(colIdx) }})</span>
                        </div>
                      </th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100 bg-white">
                    <tr
                      v-for="(row, rIdx) in excelFilteredDataRows"
                      :key="rIdx"
                      class="hover:bg-brand-50/40 transition-colors even:bg-slate-50/40"
                    >
                      <!-- Row number -->
                      <td class="py-2 px-3 text-center bg-slate-50 text-slate-400 font-semibold border-r border-slate-200 select-none text-[11px]">
                        {{ rIdx + 1 }}
                      </td>
                      <!-- Cells -->
                      <td
                        v-for="(_, cIdx) in excelHeaders.length"
                        :key="cIdx"
                        class="py-2 px-3 text-slate-800 border-r border-slate-100 last:border-r-0 whitespace-pre-wrap break-words max-w-sm"
                      >
                        {{ formatCellValue(row[cIdx]) }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- 4. Image Viewer -->
            <div
              v-else-if="detectedFormat === 'IMAGE'"
              class="w-full h-full overflow-auto flex items-center justify-center p-6 bg-slate-900/95"
            >
              <div
                class="transition-transform duration-150 flex items-center justify-center"
                :style="{
                  transform: `scale(${imageState.zoom / 100}) rotate(${imageState.rotation}deg)`,
                }"
              >
                <img
                  v-if="blobUrl"
                  :src="blobUrl"
                  :alt="document?.title || 'Document Image'"
                  class="max-w-full max-h-[80vh] object-contain rounded shadow-2xl select-none"
                />
              </div>
            </div>

            <!-- 5. Text / Code Viewer -->
            <div v-else-if="detectedFormat === 'TEXT'" class="w-full h-full overflow-auto bg-slate-950 text-slate-200 p-4 sm:p-6 font-mono text-xs sm:text-sm">
              <div class="flex gap-4">
                <!-- Line Numbers -->
                <div class="text-right text-slate-600 select-none pr-3 border-r border-slate-800 shrink-0">
                  <div v-for="n in textLinesCount" :key="n">{{ n }}</div>
                </div>
                <!-- Text Content -->
                <pre class="flex-1 whitespace-pre-wrap break-words overflow-x-auto selection:bg-brand-600 selection:text-white">{{ textState.content }}</pre>
              </div>
            </div>

            <!-- 6. Generic / PowerPoint Fallback -->
            <div v-else class="flex-1 flex items-center justify-center p-8">
              <div class="bg-white p-8 rounded-lg border border-slate-200 shadow-sm max-w-md text-center space-y-4">
                <div class="w-16 h-16 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center mx-auto border border-amber-200">
                  <Presentation class="w-8 h-8" />
                </div>
                <div>
                  <h4 class="text-base font-bold text-slate-900">{{ document?.title }}</h4>
                  <p class="text-xs text-slate-500 mt-1">
                    Định dạng <strong>{{ detectedFormat }}</strong> ({{ document?.fileName }}). Tải về để mở trong Microsoft Office hoặc ứng dụng chuyên dụng.
                  </p>
                </div>
                <AppButton variant="primary" size="md" :icon="Download" class="w-full justify-center" @click="downloadCurrentDoc">
                  Tải tệp tin về máy
                </AppButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick, onMounted, onUnmounted } from 'vue';
import type { LearningDocument } from '../../types';
import { documentService } from '../../services/document.service';
import * as docx from 'docx-preview';
import * as XLSX from 'xlsx';
import AppButton from './AppButton.vue';
import {
  X,
  Maximize2,
  Minimize2,
  Download,
  ExternalLink,
  FileSpreadsheet,
  FileText,
  FileCode,
  File,
  Presentation,
  Image as ImageIcon,
  Loader2,
  AlertCircle,
  Search,
  ZoomIn,
  ZoomOut,
  RotateCw,
  Copy,
  Check,
} from 'lucide-vue-next';

const props = defineProps<{
  modelValue: boolean;
  document: LearningDocument | null;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void;
}>();

const isFullscreen = ref(false);
const isLoading = ref(false);
const loadError = ref<string | null>(null);
const blobUrl = ref<string | null>(null);
const docxContainerRef = ref<HTMLDivElement | null>(null);

// Excel State
const excelState = reactive({
  workbook: null as XLSX.WorkBook | null,
  sheetNames: [] as string[],
  activeSheet: '',
  rawRows: [] as any[][],
  searchQuery: '',
});

// Image State
const imageState = reactive({
  zoom: 100,
  rotation: 0,
});

// Text State
const textState = reactive({
  content: '',
  isCopied: false,
});

// Detect document format accurately from extension + type
const detectedFormat = computed<'PDF' | 'WORD' | 'EXCEL' | 'IMAGE' | 'TEXT' | 'POWERPOINT' | 'OTHER'>(() => {
  if (!props.document) return 'OTHER';
  const fileName = (props.document.fileName || '').toLowerCase();
  const fileType = (props.document.fileType || '').toUpperCase();

  if (fileType === 'PDF' || fileName.endsWith('.pdf')) return 'PDF';
  if (fileType === 'WORD' || fileName.endsWith('.docx') || fileName.endsWith('.doc')) return 'WORD';
  if (fileType === 'EXCEL' || fileName.endsWith('.xlsx') || fileName.endsWith('.xls') || fileName.endsWith('.csv')) return 'EXCEL';
  if (
    fileType === 'IMAGE' ||
    fileName.endsWith('.png') ||
    fileName.endsWith('.jpg') ||
    fileName.endsWith('.jpeg') ||
    fileName.endsWith('.gif') ||
    fileName.endsWith('.webp') ||
    fileName.endsWith('.svg')
  ) {
    return 'IMAGE';
  }
  if (fileType === 'POWERPOINT' || fileName.endsWith('.pptx') || fileName.endsWith('.ppt')) return 'POWERPOINT';
  if (
    fileType === 'TEXT' ||
    fileName.endsWith('.txt') ||
    fileName.endsWith('.json') ||
    fileName.endsWith('.md') ||
    fileName.endsWith('.sql') ||
    fileName.endsWith('.java') ||
    fileName.endsWith('.ts') ||
    fileName.endsWith('.js')
  ) {
    return 'TEXT';
  }
  return 'OTHER';
});

// Header styling based on file format
const headerIconStyle = computed(() => {
  switch (detectedFormat.value) {
    case 'PDF':
      return { icon: FileCode, bg: 'bg-rose-500/20 text-rose-400', color: 'text-rose-400', badge: 'bg-rose-500/20 text-rose-300 border-rose-500/30' };
    case 'WORD':
      return { icon: FileText, bg: 'bg-blue-500/20 text-blue-400', color: 'text-blue-400', badge: 'bg-blue-500/20 text-blue-300 border-blue-500/30' };
    case 'EXCEL':
      return { icon: FileSpreadsheet, bg: 'bg-emerald-500/20 text-emerald-400', color: 'text-emerald-400', badge: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' };
    case 'IMAGE':
      return { icon: ImageIcon, bg: 'bg-purple-500/20 text-purple-400', color: 'text-purple-400', badge: 'bg-purple-500/20 text-purple-300 border-purple-500/30' };
    case 'POWERPOINT':
      return { icon: Presentation, bg: 'bg-amber-500/20 text-amber-400', color: 'text-amber-400', badge: 'bg-amber-500/20 text-amber-300 border-amber-500/30' };
    case 'TEXT':
      return { icon: FileText, bg: 'bg-slate-500/20 text-slate-300', color: 'text-slate-300', badge: 'bg-slate-500/20 text-slate-300 border-slate-500/30' };
    default:
      return { icon: File, bg: 'bg-slate-500/20 text-slate-400', color: 'text-slate-400', badge: 'bg-slate-500/20 text-slate-400 border-slate-500/30' };
  }
});

// Excel Computed Helpers
const excelHeaders = computed<any[]>(() => {
  if (excelState.rawRows.length === 0) return [];
  const firstRow = excelState.rawRows[0];
  if (Array.isArray(firstRow)) {
    // Find max column length in active sheet
    let maxCols = firstRow.length;
    for (let i = 0; i < Math.min(excelState.rawRows.length, 20); i++) {
      if (Array.isArray(excelState.rawRows[i])) {
        maxCols = Math.max(maxCols, excelState.rawRows[i].length);
      }
    }
    const headers = [];
    for (let i = 0; i < maxCols; i++) {
      headers.push(firstRow[i] !== undefined && firstRow[i] !== null && String(firstRow[i]).trim() !== '' ? String(firstRow[i]) : getExcelColumnLetter(i));
    }
    return headers;
  }
  return [];
});

const excelFilteredRows = computed<any[][]>(() => {
  if (excelState.rawRows.length === 0) return [];
  if (!excelState.searchQuery.trim()) {
    return excelState.rawRows;
  }
  const q = excelState.searchQuery.toLowerCase().trim();
  return excelState.rawRows.filter((row, idx) => {
    if (idx === 0) return true; // keep header
    return row.some((cell) => cell !== undefined && cell !== null && String(cell).toLowerCase().includes(q));
  });
});

const excelFilteredDataRows = computed<any[][]>(() => {
  const filtered = excelFilteredRows.value;
  if (filtered.length <= 1) return [];
  return filtered.slice(1);
});

// Text Computed Helpers
const textLinesCount = computed(() => {
  if (!textState.content) return 1;
  return textState.content.split('\n').length;
});

// Watch modal state & load file
watch(
  () => [props.modelValue, props.document],
  async ([isOpen, doc]) => {
    if (isOpen && doc) {
      await loadDocumentContent(doc as LearningDocument);
    } else {
      cleanupViewer();
    }
  },
  { immediate: true }
);

async function loadDocumentContent(doc: LearningDocument) {
  isLoading.value = true;
  loadError.value = null;
  cleanupViewer();

  try {
    const format = detectedFormat.value;
    const fileName = (doc.fileName || '').toLowerCase();

    if (format === 'PDF') {
      const blob = await documentService.getDocumentBlob(doc.id);
      const pdfBlob = new Blob([blob], { type: 'application/pdf' });
      blobUrl.value = URL.createObjectURL(pdfBlob);
    } else if (format === 'WORD') {
      if (fileName.endsWith('.doc')) {
        loadError.value = 'Tệp định dạng Word cũ (.doc). Trình duyệt chỉ hỗ trợ xem trực tiếp định dạng Word mới (.docx). Vui lòng tải về máy tính để mở bằng Microsoft Word hoặc chuyển đổi sang .docx.';
        return;
      }
      const arrayBuffer = await documentService.getDocumentArrayBuffer(doc.id);
      await nextTick();

      // Ensure container DOM is mounted and accessible
      let retries = 0;
      while (!docxContainerRef.value && retries < 15) {
        await new Promise((resolve) => setTimeout(resolve, 50));
        retries++;
      }

      if (docxContainerRef.value) {
        docxContainerRef.value.innerHTML = '';
        await docx.renderAsync(arrayBuffer, docxContainerRef.value, undefined, {
          className: 'docx-document-rendered',
          inWrapper: true,
          ignoreWidth: false,
          ignoreHeight: false,
          ignoreFonts: false,
          breakPages: true,
          experimental: true,
          trimXmlDeclaration: true,
          useBase64URL: true,
          renderHeaders: true,
          renderFooters: true,
          renderFootnotes: true,
          renderEndnotes: true,
        });
      } else {
        throw new Error('Không thể khởi tạo khung hiển thị tài liệu Word.');
      }
    } else if (format === 'EXCEL') {
      const arrayBuffer = await documentService.getDocumentArrayBuffer(doc.id);
      const workbook = XLSX.read(arrayBuffer, { type: 'array' });
      excelState.workbook = workbook;
      excelState.sheetNames = workbook.SheetNames || [];
      if (excelState.sheetNames.length > 0) {
        selectExcelSheet(excelState.sheetNames[0]);
      } else {
        loadError.value = 'Tệp Excel này không có trang tính (Sheet) nào hợp lệ.';
      }
    } else if (format === 'IMAGE') {
      const blob = await documentService.getDocumentBlob(doc.id);
      blobUrl.value = URL.createObjectURL(blob);
      imageState.zoom = 100;
      imageState.rotation = 0;
    } else if (format === 'TEXT') {
      const blob = await documentService.getDocumentBlob(doc.id);
      textState.content = await blob.text();
    } else {
      // POWERPOINT / OTHER
    }
  } catch (err: any) {
    console.error('Lỗi khi tải tài liệu xem trước:', err);
    loadError.value = err?.response?.data?.message || 'Không thể tải nội dung xem trước từ máy chủ. Vui lòng tải tệp về để mở trực tiếp.';
  } finally {
    isLoading.value = false;
  }
}

function selectExcelSheet(sheetName: string) {
  if (!excelState.workbook) return;
  excelState.activeSheet = sheetName;
  const worksheet = excelState.workbook.Sheets[sheetName];
  if (!worksheet) {
    excelState.rawRows = [];
    return;
  }
  const rows = XLSX.utils.sheet_to_json<any[]>(worksheet, {
    header: 1,
    defval: '',
    blankrows: false,
  });
  excelState.rawRows = rows || [];
}

function getExcelColumnLetter(index: number): string {
  let letter = '';
  let temp = index;
  while (temp >= 0) {
    letter = String.fromCharCode((temp % 26) + 65) + letter;
    temp = Math.floor(temp / 26) - 1;
  }
  return letter;
}

function formatCellValue(val: any): string {
  if (val === undefined || val === null) return '';
  if (typeof val === 'number') {
    return val.toLocaleString('vi-VN');
  }
  return String(val);
}

// Image Zoom & Rotate
function zoomIn() {
  if (imageState.zoom < 300) imageState.zoom += 25;
}

function zoomOut() {
  if (imageState.zoom > 25) imageState.zoom -= 25;
}

function resetZoom() {
  imageState.zoom = 100;
  imageState.rotation = 0;
}

function rotateImage() {
  imageState.rotation = (imageState.rotation + 90) % 360;
}

// Text Copy
async function copyTextContent() {
  if (!textState.content) return;
  try {
    await navigator.clipboard.writeText(textState.content);
    textState.isCopied = true;
    setTimeout(() => {
      textState.isCopied = false;
    }, 2000);
  } catch (e) {
    console.error(e);
  }
}

// Fullscreen
function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value;
}

// Download
async function downloadCurrentDoc() {
  if (!props.document) return;
  try {
    await documentService.downloadFile(props.document.id, props.document.fileName);
  } catch (e) {
    console.error(e);
  }
}

// Close & Cleanup
function closeViewer() {
  emit('update:modelValue', false);
}

function cleanupViewer() {
  if (blobUrl.value) {
    URL.revokeObjectURL(blobUrl.value);
    blobUrl.value = null;
  }
  if (docxContainerRef.value) {
    docxContainerRef.value.innerHTML = '';
  }
  excelState.workbook = null;
  excelState.sheetNames = [];
  excelState.activeSheet = '';
  excelState.rawRows = [];
  excelState.searchQuery = '';
  textState.content = '';
  textState.isCopied = false;
  imageState.zoom = 100;
  imageState.rotation = 0;
}

function getCategoryLabel(category?: string) {
  switch (category) {
    case 'TOEIC': return 'Đề thi TOEIC';
    case 'VOCABULARY': return 'Từ vựng';
    case 'GRAMMAR': return 'Ngữ pháp';
    case 'LISTENING': return 'Luyện nghe';
    case 'SPEAKING': return 'Luyện nói';
    case 'TEST_EXAM': return 'Đề thi thử';
    case 'IT_DEV': return 'Lập trình / Dev';
    case 'IT_BACKEND': return 'Backend';
    case 'IT_FRONTEND': return 'Frontend';
    case 'IT_DATABASE': return 'Database';
    default: return 'Tài liệu học tập';
  }
}

// Keydown Escape handler
function handleKeydown(e: KeyboardEvent) {
  if (props.modelValue && e.key === 'Escape') {
    closeViewer();
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
  cleanupViewer();
});
</script>

<style scoped>
/* Word docx container styling */
:deep(.docx-render-host) {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

:deep(.docx-wrapper) {
  background: transparent !important;
  padding: 0 !important;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

:deep(.docx-wrapper > section.docx) {
  background: white !important;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1) !important;
  padding: 40px 48px !important;
  border-radius: 6px !important;
  margin-bottom: 20px !important;
  max-width: 100% !important;
  box-sizing: border-box !important;
}

:deep(.docx table) {
  border-collapse: collapse !important;
  width: 100% !important;
  margin: 12px 0 !important;
}

:deep(.docx td),
:deep(.docx th) {
  border: 1px solid #cbd5e1 !important;
  padding: 6px 10px !important;
}

:deep(.docx p) {
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f5f9;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
