<template>
  <Teleport to="body">
    <!-- Floating Trigger Icon (Shown immediately near selection) -->
    <Transition
      enter-active-class="transition ease-out duration-150 transform"
      enter-from-class="opacity-0 scale-75 translate-y-1"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition ease-in duration-100 transform"
      leave-from-class="opacity-100 scale-100 translate-y-0"
      leave-to-class="opacity-0 scale-75 translate-y-1"
    >
      <div
        v-if="showTrigger && !showPopup && isEnabled"
        ref="triggerRef"
        class="fixed z-[9998] flex items-center gap-1 p-1 bg-slate-900/90 text-white rounded-lg shadow-xl backdrop-blur-md border border-slate-700/60 pointer-events-auto cursor-pointer select-none hover:bg-slate-900 transition-all group"
        :style="triggerStyle"
        @mousedown.prevent.stop
        @click.stop="openPopup"
      >
        <button
          type="button"
          class="flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold hover:text-brand-300 transition-colors"
          title="Tra từ & Dịch nghĩa nhanh"
        >
          <Sparkles class="w-3.5 h-3.5 text-amber-400 animate-pulse" />
          <span>Dịch / Tra từ</span>
        </button>
        <button
          type="button"
          class="p-1 text-slate-400 hover:text-white rounded hover:bg-slate-800 transition-colors"
          title="Phát âm"
          @click.stop="speakSelectedText"
        >
          <Volume2 class="w-3.5 h-3.5" />
        </button>
      </div>
    </Transition>

    <!-- Floating Translation & AI Popup Card -->
    <Transition
      enter-active-class="transition ease-out duration-200 transform"
      enter-from-class="opacity-0 scale-95 -translate-y-2"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition ease-in duration-150 transform"
      leave-from-class="opacity-100 scale-100 translate-y-0"
      leave-to-class="opacity-0 scale-95 -translate-y-2"
    >
      <div
        v-if="showPopup && isEnabled"
        ref="popupRef"
        class="fixed z-[9999] w-[360px] sm:w-[420px] max-w-[calc(100vw-32px)] max-h-[85vh] flex flex-col bg-white rounded-xl shadow-2xl border border-slate-200/90 overflow-hidden text-slate-800 backdrop-blur-lg"
        :style="popupStyle"
        @mousedown.stop
      >
        <!-- Header -->
        <div class="px-4 py-3 bg-gradient-to-r from-brand-600 via-brand-700 to-indigo-700 text-white flex items-center justify-between shrink-0">
          <div class="flex items-center gap-2 min-w-0 pr-2">
            <Sparkles class="w-4 h-4 text-amber-300 shrink-0" />
            <span class="font-bold text-sm truncate tracking-wide">Tra cứu AI & Từ điển</span>
          </div>
          <div class="flex items-center gap-1 shrink-0">
            <button
              type="button"
              class="p-1 rounded-md text-white/80 hover:text-white hover:bg-white/20 transition-colors"
              title="Phát âm chuẩn (Audio)"
              @click="speakSelectedText"
            >
              <Volume2 class="w-4 h-4" />
            </button>
            <button
              type="button"
              class="p-1 rounded-md text-white/80 hover:text-white hover:bg-white/20 transition-colors"
              title="Sao chép từ"
              @click="copyText"
            >
              <Check v-if="isCopied" class="w-4 h-4 text-emerald-300" />
              <Copy v-else class="w-4 h-4" />
            </button>
            <button
              type="button"
              class="p-1 rounded-md text-white/80 hover:text-white hover:bg-white/20 transition-colors"
              title="Đóng popup (Esc)"
              @click="closePopup"
            >
              <X class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Body Content (Scrollable) -->
        <div class="p-4 space-y-3.5 overflow-y-auto max-h-[calc(85vh-120px)] custom-scrollbar text-xs sm:text-sm">
          <!-- Selected Word / Text & IPA -->
          <div class="space-y-1 pb-2 border-b border-slate-100">
            <div class="flex items-baseline justify-between gap-2">
              <span class="text-base sm:text-lg font-extrabold text-slate-900 tracking-tight break-words">
                {{ selectedText }}
              </span>
              <span v-if="translation?.phonetic" class="text-xs font-mono text-brand-600 bg-brand-50 px-2 py-0.5 rounded border border-brand-200 shrink-0">
                {{ translation.phonetic }}
              </span>
            </div>
            <div v-if="surroundingSentence && surroundingSentence !== selectedText" class="text-[11px] text-slate-400 italic line-clamp-2">
              "{{ surroundingSentence }}"
            </div>
          </div>

          <!-- Loading State -->
          <div v-if="loading" class="py-6 flex flex-col items-center justify-center gap-2 text-slate-500">
            <Loader2 class="w-6 h-6 text-brand-600 animate-spin" />
            <span class="text-xs font-medium">Đang dịch & phân tích ngôn ngữ...</span>
          </div>

          <!-- Translation Result -->
          <div v-else class="space-y-3">
            <!-- Vietnamese Meaning -->
            <div class="p-3 rounded-lg bg-slate-50 border border-slate-200/80 space-y-1">
              <div class="text-[10px] uppercase font-bold text-slate-500 tracking-wider flex items-center gap-1.5">
                <Languages class="w-3.5 h-3.5 text-brand-600" />
                <span>Nghĩa tiếng Việt</span>
                <span class="text-[9px] font-normal text-slate-400 lowercase" v-if="translation?.source">({{ translation.source }})</span>
              </div>
              <div class="text-sm sm:text-base font-semibold text-slate-800 leading-snug">
                {{ translation?.translatedText || 'Không có kết quả dịch' }}
              </div>
            </div>

            <!-- Dictionary Definitions (if available) -->
            <div v-if="translation?.dictionary?.meanings?.length" class="space-y-2">
              <div class="text-[11px] font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
                <BookOpen class="w-3.5 h-3.5 text-indigo-600" />
                <span>Định nghĩa từ điển</span>
              </div>
              <div
                v-for="(meaning, idx) in translation.dictionary.meanings.slice(0, 2)"
                :key="idx"
                class="p-2.5 rounded-md bg-white border border-slate-100 shadow-2xs space-y-1.5"
              >
                <div class="flex items-center gap-2">
                  <span class="text-[10px] font-bold uppercase px-1.5 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200">
                    {{ meaning.partOfSpeech }}
                  </span>
                </div>
                <ul class="space-y-1 pl-3 list-disc text-slate-600 text-xs">
                  <li v-for="(def, dIdx) in meaning.definitions.slice(0, 2)" :key="dIdx">
                    <span class="font-medium text-slate-800">{{ def.definition }}</span>
                    <p v-if="def.example" class="text-[11px] text-slate-400 italic mt-0.5">
                      Ex: "{{ def.example }}"
                    </p>
                  </li>
                </ul>
              </div>
            </div>

            <!-- AI Explainer Accordion -->
            <div class="border border-purple-200 rounded-lg bg-gradient-to-br from-purple-50/60 to-indigo-50/40 p-3 space-y-2">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-1.5 text-purple-900 font-bold text-xs">
                  <Bot class="w-4 h-4 text-purple-600" />
                  <span>Phân tích ngữ cảnh AI</span>
                </div>
                <button
                  v-if="!aiExplaining && !aiResult"
                  type="button"
                  class="px-2 py-0.5 text-[11px] font-semibold bg-purple-600 text-white rounded hover:bg-purple-700 transition-colors shadow-2xs"
                  @click="runAiExplanation"
                >
                  Phân tích ngay
                </button>
              </div>

              <!-- AI Loading -->
              <div v-if="aiExplaining" class="py-3 flex items-center justify-center gap-2 text-purple-700 text-xs">
                <Loader2 class="w-4 h-4 animate-spin text-purple-600" />
                <span>AI đang phân tích ngữ pháp & sắc thái...</span>
              </div>

              <!-- AI Result Content -->
              <div v-else-if="aiResult" class="space-y-2 text-xs text-slate-700 pt-1">
                <p class="font-medium text-slate-800 leading-relaxed">{{ aiResult.summary }}</p>

                <div v-if="aiResult.grammarPoint" class="p-2 rounded bg-white/80 border border-purple-100 flex items-start gap-1.5">
                  <Sparkles class="w-3.5 h-3.5 text-purple-600 shrink-0 mt-0.5" />
                  <div>
                    <span class="font-bold text-purple-800">Điểm ngữ pháp: </span>
                    <span>{{ aiResult.grammarPoint }}</span>
                  </div>
                </div>

                <div v-if="aiResult.collocations?.length" class="space-y-1">
                  <div class="flex items-center gap-1 text-slate-700 font-bold text-[11px]">
                    <Layers class="w-3.5 h-3.5 text-indigo-600" />
                    <span>Cụm từ hay gặp (Collocations):</span>
                  </div>
                  <div class="flex flex-wrap gap-1">
                    <span
                      v-for="(c, cIdx) in aiResult.collocations"
                      :key="cIdx"
                      class="px-1.5 py-0.5 rounded bg-white border border-slate-200 text-[10px] text-slate-700"
                    >
                      {{ c }}
                    </span>
                  </div>
                </div>

                <div v-if="aiResult.contextUsage" class="text-[11px] text-slate-500 italic">
                  {{ aiResult.contextUsage }}
                </div>
              </div>
            </div>

            <!-- Quick Add to Vocabulary Form -->
            <div v-if="showQuickSaveForm" class="p-3 rounded-lg bg-emerald-50/80 border border-emerald-200 space-y-2.5">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold text-emerald-900 flex items-center gap-1.5">
                  <BookmarkPlus class="w-4 h-4 text-emerald-600" />
                  <span>Thêm nhanh vào Sổ Từ Vựng</span>
                </span>
                <button
                  type="button"
                  class="text-xs text-slate-400 hover:text-slate-600"
                  @click="showQuickSaveForm = false"
                >
                  <X class="w-3.5 h-3.5" />
                </button>
              </div>

              <div class="space-y-2">
                <div>
                  <label class="text-[10px] font-semibold text-slate-600">Từ vựng (Word)</label>
                  <input
                    v-model="saveForm.word"
                    class="w-full text-xs px-2.5 py-1.5 rounded border border-slate-300 bg-white focus:ring-1 focus:ring-emerald-500 focus:outline-none"
                    placeholder="Word"
                  />
                </div>
                <div class="grid grid-cols-2 gap-2">
                  <div>
                    <label class="text-[10px] font-semibold text-slate-600">Nghĩa tiếng Việt</label>
                    <input
                      v-model="saveForm.meaning"
                      class="w-full text-xs px-2.5 py-1.5 rounded border border-slate-300 bg-white focus:ring-1 focus:ring-emerald-500 focus:outline-none"
                      placeholder="Nghĩa"
                    />
                  </div>
                  <div>
                    <label class="text-[10px] font-semibold text-slate-600">Phiên âm</label>
                    <input
                      v-model="saveForm.pronunciation"
                      class="w-full text-xs px-2.5 py-1.5 rounded border border-slate-300 bg-white focus:ring-1 focus:ring-emerald-500 focus:outline-none"
                      placeholder="/.../"
                    />
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-2">
                  <div>
                    <label class="text-[10px] font-semibold text-slate-600">Từ loại</label>
                    <select
                      v-model="saveForm.partOfSpeech"
                      class="w-full text-xs px-2.5 py-1.5 rounded border border-slate-300 bg-white focus:ring-1 focus:ring-emerald-500 focus:outline-none"
                    >
                      <option value="noun">Danh từ (Noun)</option>
                      <option value="verb">Động từ (Verb)</option>
                      <option value="adjective">Tính từ (Adjective)</option>
                      <option value="adverb">Trạng từ (Adverb)</option>
                      <option value="phrase">Cụm từ (Phrase)</option>
                      <option value="idiom">Thành ngữ (Idiom)</option>
                      <option value="other">Khác</option>
                    </select>
                  </div>
                  <div>
                    <label class="text-[10px] font-semibold text-slate-600">Cấp độ</label>
                    <select
                      v-model="saveForm.level"
                      class="w-full text-xs px-2.5 py-1.5 rounded border border-slate-300 bg-white focus:ring-1 focus:ring-emerald-500 focus:outline-none"
                    >
                      <option value="A1">A1 - Cơ bản</option>
                      <option value="A2">A2 - Sơ cấp</option>
                      <option value="B1">B1 - Trung cấp</option>
                      <option value="B2">B2 - Khá</option>
                      <option value="C1">C1 - Nâng cao</option>
                      <option value="C2">C2 - Thành thạo</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label class="text-[10px] font-semibold text-slate-600">Câu ví dụ thực tế</label>
                  <input
                    v-model="saveForm.exampleSentence"
                    class="w-full text-xs px-2.5 py-1.5 rounded border border-slate-300 bg-white focus:ring-1 focus:ring-emerald-500 focus:outline-none"
                    placeholder="Example sentence"
                  />
                </div>
              </div>

              <div class="flex justify-end gap-2 pt-1">
                <button
                  type="button"
                  class="px-2.5 py-1 text-xs font-medium text-slate-600 hover:text-slate-800"
                  @click="showQuickSaveForm = false"
                >
                  Hủy
                </button>
                <button
                  type="button"
                  class="px-3 py-1 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white rounded shadow-xs flex items-center gap-1 transition-colors"
                  :disabled="savingVocabulary"
                  @click="submitSaveVocabulary"
                >
                  <Loader2 v-if="savingVocabulary" class="w-3.5 h-3.5 animate-spin" />
                  <span>{{ savingVocabulary ? 'Đang lưu...' : 'Lưu từ vựng' }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer Actions -->
        <div class="px-4 py-2.5 bg-slate-50 border-t border-slate-200/90 flex items-center justify-between shrink-0">
          <div class="text-[11px] text-slate-400">
            Bấm <kbd class="px-1 py-0.5 bg-slate-200 rounded text-[10px]">Esc</kbd> để đóng
          </div>
          <div class="flex items-center gap-2">
            <button
              v-if="!showQuickSaveForm"
              type="button"
              class="px-3 py-1.5 text-xs font-bold bg-emerald-600 hover:bg-emerald-700 text-white rounded-md flex items-center gap-1.5 transition-colors shadow-xs"
              @click="prepareQuickSave"
            >
              <BookmarkPlus class="w-3.5 h-3.5" />
              <span>+ Lưu vào Từ Vựng</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue';
import {
  aiTranslationService,
  type TranslationResult,
  type AIExplanationResult,
} from '../../services/ai-translation.service';
import { vocabularyService } from '../../services/vocabulary.service';
import { useToastStore } from '../../stores/toast.store';
import {
  Sparkles,
  Volume2,
  Copy,
  Check,
  X,
  Loader2,
  Bot,
  BookmarkPlus,
  Languages,
  BookOpen,
  Layers,
} from 'lucide-vue-next';

const toastStore = useToastStore();

// Trạng thái hiển thị
const isEnabled = ref(true);
const showTrigger = ref(false);
const showPopup = ref(false);
const loading = ref(false);
const aiExplaining = ref(false);
const savingVocabulary = ref(false);
const isCopied = ref(false);
const showQuickSaveForm = ref(false);

// Dữ liệu lựa chọn
const selectedText = ref('');
const surroundingSentence = ref('');
const translation = ref<TranslationResult | null>(null);
const aiResult = ref<AIExplanationResult | null>(null);

// Vị trí Popup & Trigger
const coords = reactive({
  x: 0,
  y: 0,
  top: 0,
  bottom: 0,
  left: 0,
  right: 0,
});

// Form lưu từ vựng nhanh
const saveForm = reactive({
  word: '',
  meaning: '',
  pronunciation: '',
  partOfSpeech: 'noun',
  level: 'B1',
  exampleSentence: '',
  exampleMeaning: '',
});

const triggerRef = ref<HTMLElement | null>(null);
const popupRef = ref<HTMLElement | null>(null);

// Tọa độ vị trí trigger icon
const triggerStyle = computed(() => {
  const x = Math.min(Math.max(16, coords.x - 40), window.innerWidth - 130);
  const y = coords.top > 45 ? coords.top - 38 : coords.bottom + 8;
  return {
    left: `${x}px`,
    top: `${y}px`,
  };
});

// Tọa độ vị trí popup card (tự động điều chỉnh để không bị tràn màn hình)
const popupStyle = computed(() => {
  const cardWidth = Math.min(420, window.innerWidth - 32);
  let left = coords.x - cardWidth / 2;
  if (left < 16) left = 16;
  if (left + cardWidth > window.innerWidth - 16) {
    left = window.innerWidth - cardWidth - 16;
  }

  let top = coords.bottom + 10;
  // Nếu bên dưới không đủ chỗ (cách mép dưới < 350px) thì lật lên trên
  if (window.innerHeight - coords.bottom < 360 && coords.top > 360) {
    top = Math.max(16, coords.top - 440);
  }

  return {
    left: `${left}px`,
    top: `${top}px`,
  };
});

/**
 * Xử lý sự kiện bôi đen chuột
 */
function handleMouseUp(e: MouseEvent) {
  isEnabled.value = localStorage.getItem('selection_translator_enabled') !== 'false';
  if (!isEnabled.value) {
    showTrigger.value = false;
    showPopup.value = false;
    return;
  }

  // Bỏ qua nếu click bên trong popup
  if (popupRef.value && popupRef.value.contains(e.target as Node)) {
    return;
  }
  if (triggerRef.value && triggerRef.value.contains(e.target as Node)) {
    return;
  }

  // Lấy text được bôi đen
  const selection = window.getSelection();
  if (!selection || selection.isCollapsed || selection.rangeCount === 0) {
    if (!showPopup.value) {
      showTrigger.value = false;
    }
    return;
  }

  const text = selection.toString().trim();
  // Kiểm tra độ dài hợp lệ (từ 1 đến 500 ký tự)
  if (!text || text.length < 1 || text.length > 500) {
    showTrigger.value = false;
    return;
  }

  const range = selection.getRangeAt(0);
  const rect = range.getBoundingClientRect();

  // Bỏ qua nếu kích thước vùng chọn quá bé
  if (rect.width === 0 && rect.height === 0) {
    showTrigger.value = false;
    return;
  }

  selectedText.value = text;
  coords.x = rect.left + rect.width / 2;
  coords.y = rect.top;
  coords.top = rect.top;
  coords.bottom = rect.bottom;
  coords.left = rect.left;
  coords.right = rect.right;

  // Trích xuất cả câu chứa từ được bôi đen
  extractSurroundingSentence(range, text);

  // Kiểm tra setting auto-popup
  const autoOpen = localStorage.getItem('auto_open_translator') === 'true';
  if (autoOpen) {
    openPopup();
  } else {
    showTrigger.value = true;
  }
}

/**
 * Lấy câu văn bao quanh từ bôi đen để làm câu ví dụ ngữ cảnh
 */
function extractSurroundingSentence(range: Range, text: string) {
  try {
    const containerText = range.startContainer.textContent || '';
    if (containerText && containerText.length > text.length) {
      const sentenceRegex = /[^.!?\n]+[.!?\n]+/g;
      let match: RegExpExecArray | null;
      while ((match = sentenceRegex.exec(containerText)) !== null) {
        if (match[0].includes(text)) {
          surroundingSentence.value = match[0].trim();
          return;
        }
      }
      surroundingSentence.value = containerText.trim().slice(0, 150);
      return;
    }
  } catch (err) {
    // Ignore
  }
  surroundingSentence.value = '';
}

/**
 * Mở popup và thực hiện dịch
 */
async function openPopup() {
  showTrigger.value = false;
  showPopup.value = true;
  showQuickSaveForm.value = false;
  aiResult.value = null;
  loading.value = true;

  try {
    const res = await aiTranslationService.translate(selectedText.value);
    translation.value = res;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

/**
 * Chạy phân tích chuyên sâu AI
 */
async function runAiExplanation() {
  if (!selectedText.value) return;
  aiExplaining.value = true;
  try {
    aiResult.value = await aiTranslationService.explainWithAI(selectedText.value, surroundingSentence.value);
  } catch (err) {
    console.error('AI error:', err);
  } finally {
    aiExplaining.value = false;
  }
}

/**
 * Chuẩn bị form lưu từ vựng nhanh
 */
function prepareQuickSave() {
  const dict = translation.value?.dictionary;
  const partOfSpeech = dict?.meanings?.[0]?.partOfSpeech || (selectedText.value.includes(' ') ? 'phrase' : 'noun');

  saveForm.word = selectedText.value;
  saveForm.meaning = translation.value?.translatedText || '';
  saveForm.pronunciation = translation.value?.phonetic || '';
  saveForm.partOfSpeech = partOfSpeech;
  saveForm.level = 'B1';
  saveForm.exampleSentence = surroundingSentence.value || (dict?.meanings?.[0]?.definitions?.[0]?.example || '');
  saveForm.exampleMeaning = '';
  showQuickSaveForm.value = true;
}

/**
 * Gửi lưu từ vựng vào backend
 */
async function submitSaveVocabulary() {
  if (!saveForm.word || !saveForm.meaning) {
    toastStore.warning('Vui lòng nhập từ và nghĩa');
    return;
  }

  savingVocabulary.value = true;
  try {
    await vocabularyService.createVocabulary({
      word: saveForm.word.trim(),
      meaning: saveForm.meaning.trim(),
      pronunciation: saveForm.pronunciation.trim() || undefined,
      partOfSpeech: saveForm.partOfSpeech,
      level: saveForm.level,
      exampleSentence: saveForm.exampleSentence?.trim() || undefined,
      exampleMeaning: saveForm.exampleMeaning?.trim() || undefined,
      status: 'LEARNING',
    });

    toastStore.success(`Đã thêm từ "${saveForm.word}" vào Sổ Từ Vựng!`);
    showQuickSaveForm.value = false;
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể lưu từ vựng');
  } finally {
    savingVocabulary.value = false;
  }
}

/**
 * Phát âm
 */
function speakSelectedText() {
  if (selectedText.value) {
    aiTranslationService.speak(selectedText.value);
  }
}

/**
 * Copy text
 */
async function copyText() {
  if (!selectedText.value) return;
  try {
    await navigator.clipboard.writeText(selectedText.value);
    isCopied.value = true;
    setTimeout(() => {
      isCopied.value = false;
    }, 2000);
  } catch (err) {
    console.error(err);
  }
}

/**
 * Đóng popup
 */
function closePopup() {
  showPopup.value = false;
  showTrigger.value = false;
  showQuickSaveForm.value = false;
}

/**
 * Lắng nghe phím tắt Esc & Click outside
 */
function handleKeyDown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    closePopup();
  }
}

function handleDocumentClick(e: MouseEvent) {
  if (popupRef.value && !popupRef.value.contains(e.target as Node) && triggerRef.value && !triggerRef.value.contains(e.target as Node)) {
    closePopup();
  }
}

onMounted(() => {
  document.addEventListener('mouseup', handleMouseUp);
  document.addEventListener('keydown', handleKeyDown);
  document.addEventListener('click', handleDocumentClick);
});

onBeforeUnmount(() => {
  document.removeEventListener('mouseup', handleMouseUp);
  document.removeEventListener('keydown', handleKeyDown);
  document.removeEventListener('click', handleDocumentClick);
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
