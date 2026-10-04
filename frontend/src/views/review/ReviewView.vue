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
              Thuật toán nhắc nhở đúng thời điểm vàng trước khi kiến thức bị lãng quên
            </p>
          </div>
        </div>
      </div>

      <!-- Quick Stats Counters -->
      <div class="flex items-center gap-4 bg-slate-50 px-4.5 py-2.5 rounded-md border border-slate-200 text-sm">
        <div class="text-center">
          <span class="text-slate-500 text-xs block">Từ vựng</span>
          <span class="font-bold text-slate-900 text-base">{{ summary.vocabularyDue }}</span>
        </div>
        <div class="w-px h-7 bg-slate-200"></div>
        <div class="text-center">
          <span class="text-slate-500 text-xs block">Ngữ pháp</span>
          <span class="font-bold text-slate-900 text-base">{{ summary.grammarDue }}</span>
        </div>
        <div class="w-px h-7 bg-slate-200"></div>
        <div class="text-center">
          <span class="text-slate-500 text-xs block">Tổng cần ôn</span>
          <span class="font-bold text-brand-600 text-base">{{ summary.totalDue }}</span>
        </div>
      </div>
    </div>

    <!-- Active Review Session Mode -->
    <div v-if="loading" class="bg-white p-16 rounded-md border border-slate-200 text-center text-sm text-slate-400">
      Đang chuẩn bị phiên ôn tập...
    </div>

    <!-- Finished / Caught up Screen -->
    <div
      v-else-if="dueItems.length === 0"
      class="bg-white p-12 rounded-md border border-slate-200 shadow-xs text-center space-y-4"
    >
      <div class="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
        <CheckCircle class="w-8 h-8" />
      </div>
      <div>
        <h3 class="text-xl font-bold text-slate-900">Tuyệt vời! Bạn đã hoàn thành toàn bộ mục ôn tập hôm nay!</h3>
        <p class="text-sm text-slate-500 max-w-md mx-auto mt-1.5 leading-relaxed">
          Tất cả từ vựng và ngữ pháp đã được ghi nhớ. Hãy quay lại vào ngày mai hoặc thử sức với các mini games để củng cố phản xạ.
        </p>
      </div>
      <div class="flex justify-center gap-3 pt-2">
        <router-link to="/games">
          <AppButton variant="primary" size="md" :icon="Gamepad2">Chơi Mini Games</AppButton>
        </router-link>
        <router-link to="/dashboard">
          <AppButton variant="secondary" size="md">Về Dashboard</AppButton>
        </router-link>
      </div>
    </div>

    <!-- Interactive Flashcard Review Container -->
    <div v-else class="space-y-4">
      <!-- Session Progress Bar -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center justify-between text-sm">
        <div class="flex items-center gap-2">
          <span class="font-semibold text-slate-700">Tiến độ phiên ôn tập:</span>
          <span class="font-bold text-brand-600">{{ currentIndex + 1 }} / {{ totalSessionItems }}</span>
        </div>
        <div class="w-48 bg-slate-100 rounded-full h-2.5 overflow-hidden">
          <div
            class="bg-brand-600 h-2.5 rounded-full transition-all duration-300"
            :style="{ width: `${((currentIndex) / totalSessionItems) * 100}%` }"
          ></div>
        </div>
      </div>

      <!-- Flashcard Box -->
      <div class="bg-white rounded-md border border-slate-200 shadow-sm overflow-hidden flex flex-col min-h-[400px] justify-between">
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
            <div class="text-3xl sm:text-4xl font-extrabold text-slate-900 tracking-tight">
              {{ currentItem?.title }}
            </div>
            <p v-if="currentItem?.subtitle" class="text-base font-mono text-slate-500 font-medium">
              {{ currentItem?.subtitle }}
            </p>
          </div>

          <!-- Answer Section (Revealed on click) -->
          <div v-if="isAnswerRevealed" class="space-y-4 pt-4 border-t border-slate-100 animate-fadeIn">
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

            <!-- Example sentence -->
            <div v-if="currentItem?.exampleSentence" class="p-3.5 bg-slate-50 rounded-md border border-slate-100 text-sm space-y-1.5">
              <span class="font-semibold text-slate-900 block">"{{ currentItem?.exampleSentence }}"</span>
              <span v-if="currentItem?.exampleMeaning" class="text-slate-600 block">{{ currentItem?.exampleMeaning }}</span>
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
import type { ReviewItem, ReviewDueSummary } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppBadge from '../../components/common/AppBadge.vue';
import { RefreshCw, CheckCircle, Gamepad2, Lightbulb } from 'lucide-vue-next';

const reviewStore = useReviewStore();
const toastStore = useToastStore();

const loading = ref(true);
const submitting = ref(false);
const isAnswerRevealed = ref(false);
const currentIndex = ref(0);
const dueItems = ref<ReviewItem[]>([]);
const totalSessionItems = ref(0);

const summary = ref<ReviewDueSummary>({
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
  await loadReviewQueue();
});

async function loadReviewQueue() {
  loading.value = true;
  try {
    const [sum, items] = await Promise.all([
      reviewService.getReviewSummary(),
      reviewService.getDueReviewItems(),
    ]);
    summary.value = sum;
    dueItems.value = items;
    totalSessionItems.value = items.length;
    currentIndex.value = 0;
    isAnswerRevealed.value = false;
  } catch (err) {
    toastStore.error('Không thể tải dữ liệu ôn tập');
  } finally {
    loading.value = false;
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
      toastStore.success('Chúc mừng! Bạn đã hoàn thành phiên ôn tập hôm nay.');
    }
  } catch (err) {
    toastStore.error('Không thể lưu kết quả ôn tập');
  } finally {
    submitting.value = false;
  }
}
</script>
