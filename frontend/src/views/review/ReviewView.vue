<template>
  <div class="space-y-6">
    <!-- Header Summary Card -->
    <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <div class="flex items-center gap-3">
          <div class="p-2.5 rounded-md bg-brand-50 text-brand-600">
            <RefreshCw class="w-6 h-6" />
          </div>
          <div>
            <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Không gian ôn tập (Spaced Repetition)</h2>
            <p class="text-sm sm:text-base text-slate-500 mt-1">
              Ôn tập từ vựng & ngữ pháp theo chủ đề dựa trên thuật toán lặp lại ngắt quãng
            </p>
          </div>
        </div>
      </div>

      <!-- Quick Stats Counters -->
      <div class="flex items-center gap-4 bg-slate-50 px-4.5 py-2.5 rounded-md border border-slate-200 text-sm">
        <div class="text-center">
          <span class="text-slate-500 text-xs block">Từ vựng</span>
          <span class="font-bold text-slate-900 text-base">{{ globalSummary.vocabularyDue }}</span>
        </div>
        <div class="w-px h-7 bg-slate-200"></div>
        <div class="text-center">
          <span class="text-slate-500 text-xs block">Ngữ pháp</span>
          <span class="font-bold text-slate-900 text-base">{{ globalSummary.grammarDue }}</span>
        </div>
        <div class="w-px h-7 bg-slate-200"></div>
        <div class="text-center">
          <span class="text-slate-500 text-xs block">Tổng cần ôn</span>
          <span class="font-bold text-brand-600 text-base">{{ globalSummary.totalDue }}</span>
        </div>
      </div>
    </div>

    <!-- ============================================================= -->
    <!-- VIEW 1: TOPIC SELECTION BOXES (Chọn chủ đề để ôn tập)         -->
    <!-- ============================================================= -->
    <div v-if="!isSessionActive" class="space-y-5">
      <div class="flex items-center justify-between">
        <div>
          <h3 class="text-lg font-bold text-slate-900">Chọn chủ đề muốn ôn tập</h3>
          <p class="text-xs sm:text-sm text-slate-500">
            Chọn một chủ đề cụ thể hoặc ôn tập tổng hợp toàn bộ từ vựng
          </p>
        </div>
      </div>

      <div v-if="loadingTopics" class="bg-white p-12 rounded-md border border-slate-200 text-center text-sm text-slate-400">
        Đang tải danh sách chủ đề ôn tập...
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4.5">
        <!-- Box: Tất cả chủ đề (Toàn bộ kho từ & ngữ pháp) -->
        <div
          class="group bg-white p-5 rounded-xl border-2 border-slate-200/80 hover:border-brand-500 hover:shadow-lg transition-all duration-300 flex flex-col justify-between cursor-pointer relative overflow-hidden"
          @click="startReviewSession(null, 'Tất cả chủ đề')"
        >
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <div class="p-2.5 rounded-lg bg-gradient-to-tr from-brand-600 to-indigo-600 text-white shadow-sm">
                <Sparkles class="w-5 h-5" />
              </div>
              <span
                class="text-xs font-bold px-2.5 py-1 rounded-full flex items-center gap-1.5"
                :class="globalSummary.totalDue > 0 ? 'bg-rose-50 text-rose-700 border border-rose-200' : 'bg-emerald-50 text-emerald-700 border border-emerald-200'"
              >
                <Clock class="w-3.5 h-3.5" />
                <span>{{ globalSummary.totalDue > 0 ? `${globalSummary.totalDue} mục cần ôn` : 'Đã ôn hết' }}</span>
              </span>
            </div>

            <div>
              <h4 class="text-base font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                Tất cả chủ đề (Toàn bộ kho)
              </h4>
              <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                Ôn tập tổng hợp toàn bộ từ vựng và ngữ pháp đã đến hạn ôn tập hôm nay.
              </p>
            </div>

            <div class="text-xs text-slate-500 font-medium">
              Tổng kho: <strong>{{ globalSummary.totalItems }}</strong> mục (<strong>{{ globalSummary.totalMastered }}</strong> đã thuộc lòng)
            </div>
          </div>

          <div class="pt-4 mt-3 border-t border-slate-100 flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-400">Toàn bộ</span>
            <AppButton variant="primary" size="sm" class="group-hover:shadow-xs">
              Bắt đầu ôn tập →
            </AppButton>
          </div>
        </div>

        <!-- Boxes for each individual topic -->
        <div
          v-for="t in topicSummaries"
          :key="t.topicId"
          class="group bg-white p-5 rounded-xl border-2 border-slate-200/80 hover:border-brand-500 hover:shadow-lg transition-all duration-300 flex flex-col justify-between cursor-pointer relative overflow-hidden"
          @click="startReviewSession(t.topicId, t.topicName)"
        >
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <div class="p-2.5 rounded-lg bg-brand-50 text-brand-600 group-hover:bg-brand-600 group-hover:text-white transition-colors">
                <Folder class="w-5 h-5" />
              </div>
              <div class="flex items-center gap-1.5">
                <AppBadge v-if="t.level" :level="t.level">{{ t.level }}</AppBadge>
                <span
                  class="text-xs font-bold px-2 py-0.5 rounded-full flex items-center gap-1"
                  :class="t.dueWords > 0 ? 'bg-amber-50 text-amber-700 border border-amber-200' : 'bg-slate-100 text-slate-600'"
                >
                  <Clock class="w-3 h-3" />
                  <span>{{ t.dueWords > 0 ? `${t.dueWords} cần ôn` : 'Đã ôn đủ' }}</span>
                </span>
              </div>
            </div>

            <div>
              <h4 class="text-base font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                {{ t.topicName }}
              </h4>
              <p class="text-xs text-slate-500 mt-1">
                Tổng <strong>{{ t.totalWords }}</strong> từ vựng ({{ t.masteredWords }} đã thuộc, {{ t.learningWords }} đang học)
              </p>
            </div>

            <!-- Mini Progress Bar -->
            <div v-if="t.totalWords > 0" class="space-y-1">
              <div class="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden flex">
                <div
                  class="bg-emerald-500 h-full"
                  :style="{ width: `${(t.masteredWords / t.totalWords) * 100}%` }"
                  title="Đã thuộc"
                ></div>
                <div
                  class="bg-amber-500 h-full"
                  :style="{ width: `${(t.learningWords / t.totalWords) * 100}%` }"
                  title="Đang học"
                ></div>
              </div>
              <div class="flex justify-between text-[10px] text-slate-400">
                <span>{{ Math.round((t.masteredWords / t.totalWords) * 100) }}% hoàn thành</span>
                <span>{{ t.masteredWords }}/{{ t.totalWords }} từ</span>
              </div>
            </div>
            <div v-else class="text-xs text-slate-400 italic">
              Chưa có từ vựng
            </div>
          </div>

          <div class="pt-4 mt-3 border-t border-slate-100 flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-400">Chủ đề</span>
            <AppButton variant="secondary" size="sm" class="group-hover:bg-brand-50 group-hover:text-brand-700 group-hover:border-brand-200">
              Ôn chủ đề này →
            </AppButton>
          </div>
        </div>
      </div>
    </div>

    <!-- ============================================================= -->
    <!-- VIEW 2: ACTIVE FLASHCARD REVIEW SESSION                       -->
    <!-- ============================================================= -->
    <div v-else class="space-y-4">
      <!-- Session Navigation & Progress Bar -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-sm">
        <div class="flex items-center gap-3">
          <button
            type="button"
            class="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-brand-600 bg-slate-100 hover:bg-slate-200 px-3 py-1.5 rounded-md transition-colors"
            @click="exitSession"
          >
            <ArrowLeft class="w-3.5 h-3.5" />
            <span>Đổi chủ đề khác</span>
          </button>
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold text-slate-500">Đang ôn:</span>
            <span class="text-xs font-bold text-brand-700 bg-brand-50 px-2.5 py-1 rounded-md border border-brand-200">
              {{ activeTopicName }}
            </span>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <span class="font-semibold text-slate-700 text-xs sm:text-sm">Tiến độ:</span>
          <span class="font-bold text-brand-600 text-xs sm:text-sm">{{ currentIndex + 1 }} / {{ totalSessionItems }}</span>
          <div class="w-36 sm:w-48 bg-slate-100 rounded-full h-2.5 overflow-hidden">
            <div
              class="bg-brand-600 h-2.5 rounded-full transition-all duration-300"
              :style="{ width: `${((currentIndex) / totalSessionItems) * 100}%` }"
            ></div>
          </div>
        </div>
      </div>

      <!-- Finished / Caught up Screen -->
      <div
        v-if="dueItems.length === 0"
        class="bg-white p-12 rounded-md border border-slate-200 shadow-xs text-center space-y-4"
      >
        <div class="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
          <CheckCircle class="w-8 h-8" />
        </div>
        <div>
          <h3 class="text-xl font-bold text-slate-900">
            Tuyệt vời! Bạn đã hoàn thành phiên ôn tập cho "{{ activeTopicName }}"!
          </h3>
          <p class="text-sm text-slate-500 max-w-md mx-auto mt-1.5 leading-relaxed">
            Tất cả từ vựng đã được ôn tập và cập nhật chu kỳ ghi nhớ mới. Bạn có thể chọn chủ đề khác để ôn tiếp hoặc thử sức với Mini Games.
          </p>
        </div>
        <div class="flex justify-center gap-3 pt-2 flex-wrap">
          <AppButton variant="primary" size="md" :icon="RotateCcw" @click="exitSession">
            Ôn chủ đề khác
          </AppButton>
          <router-link to="/games">
            <AppButton variant="secondary" size="md" :icon="Gamepad2">Chơi Mini Games</AppButton>
          </router-link>
        </div>
      </div>

      <!-- Flashcard Box -->
      <div v-else class="bg-white rounded-md border border-slate-200 shadow-sm overflow-hidden flex flex-col min-h-[420px] justify-between">
        <!-- Card Content Top -->
        <div class="p-6 sm:p-8 space-y-6">
          <div class="flex items-center justify-between pb-3 border-b border-slate-100">
            <AppBadge :variant="currentItem?.contentType === 'VOCABULARY' ? 'primary' : 'warning'">
              {{ currentItem?.contentType === 'VOCABULARY' ? 'Từ vựng (Vocabulary)' : 'Ngữ pháp (Grammar)' }}
            </AppBadge>
            <div class="text-xs text-slate-500 font-medium">
              Đã ôn: <strong>{{ currentItem?.reviewCount || 0 }}</strong> lần | Cách quãng: <strong>{{ currentItem?.currentIntervalDays || 1 }}</strong> ngày
            </div>
          </div>

          <!-- Question Prompt (Front) -->
          <div class="text-center py-5 space-y-2.5">
            <div class="flex items-center justify-center gap-2">
              <div class="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight">
                {{ currentItem?.title }}
              </div>
              <button
                v-if="currentItem?.title"
                type="button"
                class="p-2 rounded-full text-slate-400 hover:text-brand-600 hover:bg-brand-50 transition-colors"
                title="Phát âm từ vựng"
                @click="speakWord(currentItem.title)"
              >
                <Volume2 class="w-5 h-5" />
              </button>
            </div>
            <p v-if="currentItem?.subtitle" class="text-base font-mono text-slate-500 font-medium">
              {{ currentItem?.subtitle }}
            </p>
          </div>

          <!-- Answer Section (Revealed on click) -->
          <div v-if="isAnswerRevealed" class="space-y-4 pt-4 border-t border-slate-100">
            <!-- Meaning / Usage -->
            <div>
              <span class="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-1.5">
                {{ currentItem?.contentType === 'VOCABULARY' ? 'Nghĩa tiếng Việt' : 'Cách dùng & Công thức' }}
              </span>
              <p class="text-lg font-bold text-brand-700 bg-brand-50/60 p-4 rounded-md border border-brand-100">
                {{ currentItem?.primaryMeaning }}
              </p>
            </div>

            <!-- Structure Formula if grammar -->
            <div v-if="currentItem?.structure" class="p-3.5 bg-slate-50 rounded-md border border-slate-200 font-mono text-base text-purple-900 font-bold">
              Công thức: {{ currentItem?.structure }}
            </div>

            <!-- Short Example sentence -->
            <div v-if="currentItem?.exampleSentence" class="p-3.5 bg-slate-50 rounded-md border border-slate-100 text-sm space-y-1.5">
              <span class="font-semibold text-slate-900 block">"{{ currentItem?.exampleSentence }}"</span>
              <span v-if="currentItem?.exampleMeaning" class="text-slate-600 block">{{ currentItem?.exampleMeaning }}</span>
            </div>

            <!-- Extended Long Context Sentence -->
            <div v-if="currentItem?.contextSentence" class="p-3.5 bg-purple-50/70 rounded-md border border-purple-200 text-sm space-y-1.5">
              <div class="flex items-center justify-between">
                <span class="text-[11px] font-bold text-purple-800 uppercase tracking-wider">Ngữ cảnh / Câu ví dụ mở rộng:</span>
                <span class="text-[10px] font-semibold text-purple-600 bg-purple-100 px-1.5 py-0.2 rounded">TOEIC Context</span>
              </div>
              <p class="font-medium text-slate-900 leading-relaxed">"{{ currentItem?.contextSentence }}"</p>
              <p v-if="currentItem?.contextMeaning" class="text-xs text-purple-950/80 leading-relaxed">{{ currentItem?.contextMeaning }}</p>
            </div>

            <!-- Note -->
            <p v-if="currentItem?.note" class="text-sm text-slate-700 bg-amber-50/50 p-3 rounded-md border border-amber-100 flex items-center gap-1.5">
              <Lightbulb class="w-4 h-4 text-amber-500 shrink-0" />
              <span>Ghi chú: {{ currentItem?.note }}</span>
            </p>
          </div>
        </div>

        <!-- Card Action Bottom Footer -->
        <div class="p-4 sm:p-6 bg-slate-50 border-t border-slate-200">
          <!-- Button: Show Answer -->
          <div v-if="!isAnswerRevealed" class="text-center">
            <AppButton
              variant="primary"
              size="lg"
              class="w-full sm:w-auto px-8"
              @click="isAnswerRevealed = true"
            >
              Hiển thị câu trả lời (Show Answer)
            </AppButton>
          </div>

          <!-- Rating Buttons: Forgot / Hard / Good / Easy -->
          <div v-else class="space-y-2">
            <span class="text-sm font-semibold text-slate-700 block text-center mb-2.5">
              Đánh giá mức độ ghi nhớ của bạn:
            </span>
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
              <button
                type="button"
                :disabled="submitting"
                class="flex flex-col items-center p-3 rounded-md bg-rose-50 text-rose-800 border border-rose-200 hover:bg-rose-100 transition-all font-medium text-xs shadow-xs focus:ring-2 focus:ring-rose-400 cursor-pointer"
                @click="rateItem('FORGOT')"
              >
                <span class="font-bold text-base">Quên (Forgot)</span>
                <span class="text-xs text-rose-600 mt-1">Ôn lại (+1 ngày)</span>
              </button>

              <button
                type="button"
                :disabled="submitting"
                class="flex flex-col items-center p-3 rounded-md bg-amber-50 text-amber-800 border border-amber-200 hover:bg-amber-100 transition-all font-medium text-xs shadow-xs focus:ring-2 focus:ring-amber-400 cursor-pointer"
                @click="rateItem('HARD')"
              >
                <span class="font-bold text-base">Khó nhớ (Hard)</span>
                <span class="text-xs text-amber-600 mt-1">Khoảng cách ngắn</span>
              </button>

              <button
                type="button"
                :disabled="submitting"
                class="flex flex-col items-center p-3 rounded-md bg-brand-50 text-brand-800 border border-brand-200 hover:bg-brand-100 transition-all font-medium text-xs shadow-xs focus:ring-2 focus:ring-brand-400 cursor-pointer"
                @click="rateItem('GOOD')"
              >
                <span class="font-bold text-base">Nhớ được (Good)</span>
                <span class="text-xs text-brand-600 mt-1">Tăng khoảng cách (x2)</span>
              </button>

              <button
                type="button"
                :disabled="submitting"
                class="flex flex-col items-center p-3 rounded-md bg-emerald-50 text-emerald-800 border border-emerald-200 hover:bg-emerald-100 transition-all font-medium text-xs shadow-xs focus:ring-2 focus:ring-emerald-400 cursor-pointer"
                @click="rateItem('EASY')"
              >
                <span class="font-bold text-base">Rất dễ (Easy)</span>
                <span class="text-xs text-emerald-600 mt-1">Tăng mạnh (+2.5x)</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { reviewService } from '../../services/review.service';
import { useReviewStore } from '../../stores/review.store';
import { useToastStore } from '../../stores/toast.store';
import type { ReviewItem, ReviewDueSummary, TopicReviewSummary } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppBadge from '../../components/common/AppBadge.vue';
import {
  RefreshCw,
  CheckCircle,
  Gamepad2,
  Lightbulb,
  Folder,
  Sparkles,
  Clock,
  ArrowLeft,
  RotateCcw,
  Volume2,
} from 'lucide-vue-next';

const reviewStore = useReviewStore();
const toastStore = useToastStore();

// View Mode State
const isSessionActive = ref(false);
const selectedTopicId = ref<number | null>(null);
const activeTopicName = ref('Tất cả chủ đề');

// Topic Summaries State
const loadingTopics = ref(true);
const topicSummaries = ref<TopicReviewSummary[]>([]);

// Overall Queue State
const loadingSession = ref(false);
const submitting = ref(false);
const isAnswerRevealed = ref(false);
const currentIndex = ref(0);
const dueItems = ref<ReviewItem[]>([]);
const totalSessionItems = ref(0);

const globalSummary = ref<ReviewDueSummary>({
  totalDue: 0,
  vocabularyDue: 0,
  grammarDue: 0,
  totalItems: 0,
  totalMastered: 0,
});

const currentItem = computed(() => {
  if (dueItems.value.length === 0) return null;
  return dueItems.value[currentIndex.value];
});

onMounted(async () => {
  await loadTopicSummaries();
});

async function loadTopicSummaries() {
  loadingTopics.value = true;
  try {
    const [sum, topics] = await Promise.all([
      reviewService.getReviewSummary(),
      reviewService.getTopicsReviewSummary(),
    ]);
    globalSummary.value = sum;
    topicSummaries.value = topics;
  } catch (err) {
    toastStore.error('Không thể tải dữ liệu chủ đề ôn tập');
  } finally {
    loadingTopics.value = false;
  }
}

async function startReviewSession(topicId: number | null, topicName: string) {
  selectedTopicId.value = topicId;
  activeTopicName.value = topicName;
  loadingSession.value = true;
  isSessionActive.value = true;

  try {
    const items = await reviewService.getDueReviewItems(topicId || undefined);
    dueItems.value = items;
    totalSessionItems.value = items.length;
    currentIndex.value = 0;
    isAnswerRevealed.value = false;
  } catch (err) {
    toastStore.error('Không thể tải câu hỏi ôn tập');
    isSessionActive.value = false;
  } finally {
    loadingSession.value = false;
  }
}

function exitSession() {
  isSessionActive.value = false;
  selectedTopicId.value = null;
  loadTopicSummaries();
}

function speakWord(text: string) {
  if ('speechSynthesis' in window && text) {
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 0.9;
    window.speechSynthesis.speak(utterance);
  }
}

async function rateItem(result: 'FORGOT' | 'HARD' | 'GOOD' | 'EASY') {
  if (!currentItem.value) return;

  submitting.value = true;
  try {
    await reviewService.submitReview(currentItem.value.id, result);

    // Advance to next item
    if (currentIndex.value < dueItems.value.length - 1) {
      currentIndex.value++;
      isAnswerRevealed.value = false;
    } else {
      // Completed all items
      dueItems.value = [];
      reviewStore.fetchSummary();
      toastStore.success(`Chúc mừng! Bạn đã hoàn thành phiên ôn tập cho "${activeTopicName.value}".`);
    }
  } catch (err) {
    toastStore.error('Không thể lưu kết quả ôn tập');
  } finally {
    submitting.value = false;
  }
}
</script>
