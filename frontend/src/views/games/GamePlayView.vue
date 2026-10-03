<template>
  <div class="max-w-3xl mx-auto space-y-4">
    <!-- Header -->
    <div class="flex items-center justify-between bg-white p-4 rounded-md border border-slate-200 shadow-xs">
      <div class="flex items-center gap-2">
        <router-link to="/games" class="p-1 text-slate-400 hover:text-slate-600 rounded-md">
          <ArrowLeft class="w-4 h-4" />
        </router-link>
        <div>
          <h2 class="text-sm font-bold text-slate-900">{{ sessionData?.gameName || 'Mini Game' }}</h2>
          <span class="text-[10px] text-slate-500">Phiên chơi #{{ sessionData?.sessionId }}</span>
        </div>
      </div>

      <!-- Score ticker -->
      <div v-if="!isGameOver && sessionData" class="flex items-center gap-3 text-xs font-semibold">
        <span class="text-emerald-600">Đúng: {{ correctCount }}</span>
        <span class="text-rose-600">Sai: {{ wrongCount }}</span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="bg-white p-16 rounded-md border border-slate-200 text-center text-xs text-slate-400">
      Đang tải bộ câu hỏi...
    </div>

    <!-- Error State -->
    <div v-else-if="errorMessage" class="bg-white p-12 rounded-md border border-slate-200 text-center space-y-3">
      <AlertCircle class="w-10 h-10 text-rose-500 mx-auto" />
      <p class="text-sm text-slate-700">{{ errorMessage }}</p>
      <div class="pt-2">
        <router-link to="/games">
          <AppButton variant="secondary" size="sm">Quay lại danh sách Game</AppButton>
        </router-link>
      </div>
    </div>

    <!-- Game Over / Result Screen -->
    <div v-else-if="isGameOver && gameResult" class="bg-white p-8 rounded-md border border-slate-200 shadow-sm text-center space-y-6">
      <div class="w-16 h-16 rounded-full bg-brand-50 text-brand-600 flex items-center justify-center mx-auto">
        <Trophy class="w-8 h-8" />
      </div>

      <div>
        <h3 class="text-xl font-bold text-slate-900">Hoàn thành trò chơi!</h3>
        <p class="text-xs text-slate-500 mt-1">{{ gameResult.gameName }}</p>
      </div>

      <!-- Score Summary -->
      <div class="grid grid-cols-3 gap-3 max-w-sm mx-auto bg-slate-50 p-4 rounded-md border border-slate-200 text-xs">
        <div>
          <span class="text-slate-400 text-[10px] block">Điểm số</span>
          <span class="text-xl font-bold text-brand-600">{{ gameResult.score }}%</span>
        </div>
        <div class="border-x border-slate-200">
          <span class="text-slate-400 text-[10px] block">Câu đúng</span>
          <span class="text-xl font-bold text-emerald-600">{{ gameResult.correctAnswers }}</span>
        </div>
        <div>
          <span class="text-slate-400 text-[10px] block">Câu sai</span>
          <span class="text-xl font-bold text-rose-600">{{ gameResult.wrongAnswers }}</span>
        </div>
      </div>

      <div class="flex justify-center gap-3 pt-4">
        <AppButton variant="primary" size="md" :icon="RotateCcw" @click="startNewGame">
          Chơi lại (Play again)
        </AppButton>
        <router-link to="/games">
          <AppButton variant="secondary" size="md">
            Quay lại kho game
          </AppButton>
        </router-link>
      </div>
    </div>

    <!-- Active Question Arena -->
    <div v-else-if="currentQuestion" class="bg-white rounded-md border border-slate-200 shadow-sm overflow-hidden flex flex-col justify-between min-h-[400px]">
      <!-- Top Progress -->
      <div class="px-6 py-3 bg-slate-50 border-b border-slate-200 flex items-center justify-between text-xs">
        <span class="font-medium text-slate-600">Câu hỏi {{ currentQuestionIndex + 1 }} / {{ totalQuestions }}</span>
        <div class="w-32 bg-slate-200 rounded-full h-1.5 overflow-hidden">
          <div
            class="bg-brand-600 h-1.5 rounded-full transition-all duration-300"
            :style="{ width: `${((currentQuestionIndex + 1) / totalQuestions) * 100}%` }"
          ></div>
        </div>
      </div>

      <!-- Question Content -->
      <div class="p-6 sm:p-8 space-y-6">
        <div class="text-center space-y-2">
          <span v-if="currentQuestion.prompt" class="text-xs font-medium text-slate-500 block">
            {{ currentQuestion.prompt }}
          </span>
          <h3 class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight leading-snug">
            {{ currentQuestion.questionText }}
          </h3>
        </div>

        <!-- Mode 1: Multiple Choice Options (4 options) -->
        <div v-if="currentQuestion.options && currentQuestion.options.length > 0" class="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
          <button
            v-for="opt in currentQuestion.options"
            :key="opt.id"
            type="button"
            :disabled="hasAnsweredCurrent"
            :class="[
              'p-4 text-left text-sm sm:text-base font-semibold rounded-md border transition-all flex items-center justify-between cursor-pointer',
              getOptionClass(opt),
            ]"
            @click="handleSelectOption(opt)"
          >
            <span>{{ opt.optionText }}</span>
            <Check v-if="hasAnsweredCurrent && opt.isCorrect" class="w-5 h-5 text-emerald-600" />
            <X v-else-if="hasAnsweredCurrent && selectedOptionId === opt.id && !opt.isCorrect" class="w-5 h-5 text-rose-600" />
          </button>
        </div>

        <!-- Mode 2: Word Meaning Input Type -->
        <div v-else-if="gameCode === 'WORD_MEANING'" class="max-w-md mx-auto space-y-3 pt-2">
          <AppInput
            v-model="userTypedAnswer"
            placeholder="Gõ từ tiếng Anh..."
            :disabled="hasAnsweredCurrent"
            @enter="submitTypedAnswer"
          />
          <AppButton
            v-if="!hasAnsweredCurrent"
            variant="primary"
            size="md"
            full-width
            @click="submitTypedAnswer"
          >
            Kiểm tra đáp án
          </AppButton>
        </div>

        <!-- Mode 3: Flashcard Show Meaning & Rating -->
        <div v-else-if="gameCode === 'FLASHCARD'" class="max-w-md mx-auto text-center space-y-4 pt-2">
          <div v-if="isFlashcardMeaningShown" class="p-4 bg-brand-50 rounded-md border border-brand-200">
            <span class="text-xs font-semibold text-slate-500 block mb-1">Nghĩa tiếng Việt</span>
            <span class="text-lg font-bold text-brand-800">{{ currentQuestion.targetAnswer }}</span>
            <p v-if="currentQuestion.explanation" class="text-sm text-slate-600 italic mt-2">
              "{{ currentQuestion.explanation }}"
            </p>
          </div>
          <AppButton
            v-if="!isFlashcardMeaningShown"
            variant="primary"
            size="md"
            @click="isFlashcardMeaningShown = true"
          >
            Hiện nghĩa (Show Meaning)
          </AppButton>
        </div>

        <!-- Explanation & Feedback banner -->
        <div
          v-if="hasAnsweredCurrent && currentQuestion.explanation"
          :class="[
            'p-3.5 rounded-md text-sm border animate-fadeIn',
            lastAnswerCorrect ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-rose-50 border-rose-200 text-rose-800',
          ]"
        >
          <div class="font-bold mb-0.5">
            {{ lastAnswerCorrect ? '✓ Chính xác!' : '✗ Chưa chính xác!' }}
          </div>
          <div>{{ currentQuestion.explanation }}</div>
        </div>
      </div>

      <!-- Bottom Navigation Footer -->
      <div class="px-6 py-4 bg-slate-50 border-t border-slate-200 flex items-center justify-between">
        <div class="text-xs text-slate-500">
          <span v-if="hasAnsweredCurrent">Nhấn <strong>Tiếp tục</strong> để chuyển câu tiếp theo</span>
        </div>

        <div v-if="gameCode === 'FLASHCARD' && isFlashcardMeaningShown && !hasAnsweredCurrent" class="flex gap-2">
          <AppButton variant="danger" size="sm" @click="handleFlashcardRating(false)">Chưa nhớ</AppButton>
          <AppButton variant="success" size="sm" @click="handleFlashcardRating(true)">Đã nhớ tốt</AppButton>
        </div>

        <AppButton
          v-else-if="hasAnsweredCurrent"
          variant="primary"
          size="sm"
          :icon="ChevronRight"
          @click="nextQuestion"
        >
          {{ currentQuestionIndex < totalQuestions - 1 ? 'Câu tiếp theo' : 'Xem kết quả' }}
        </AppButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { gameService, type AnswerSubmission } from '../../services/game.service';
import { useToastStore } from '../../stores/toast.store';
import type { GameSessionStart, GameQuestion, GameOption, GameResult } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import { ArrowLeft, Trophy, RotateCcw, ChevronRight, Check, X, AlertCircle } from 'lucide-vue-next';

const route = useRoute();
const toastStore = useToastStore();

const gameCode = computed(() => (route.params.code as string) || 'FLASHCARD');

const loading = ref(true);
const errorMessage = ref('');
const sessionData = ref<GameSessionStart | null>(null);
const currentQuestionIndex = ref(0);
const userTypedAnswer = ref('');
const selectedOptionId = ref<number | null>(null);
const isFlashcardMeaningShown = ref(false);

const hasAnsweredCurrent = ref(false);
const lastAnswerCorrect = ref(false);

const recordedAnswers = ref<AnswerSubmission[]>([]);
const correctCount = ref(0);
const wrongCount = ref(0);

const isGameOver = ref(false);
const gameResult = ref<GameResult | null>(null);

const currentQuestion = computed<GameQuestion | null>(() => {
  if (!sessionData.value || !sessionData.value.questions) return null;
  return sessionData.value.questions[currentQuestionIndex.value] || null;
});

const totalQuestions = computed(() => sessionData.value?.questions?.length || 0);

onMounted(() => {
  startNewGame();
});

async function startNewGame() {
  loading.value = true;
  errorMessage.value = '';
  isGameOver.value = false;
  gameResult.value = null;
  currentQuestionIndex.value = 0;
  recordedAnswers.value = [];
  correctCount.value = 0;
  wrongCount.value = 0;
  hasAnsweredCurrent.value = false;
  selectedOptionId.value = null;
  userTypedAnswer.value = '';
  isFlashcardMeaningShown.value = false;

  try {
    const res = await gameService.startGame(gameCode.value);
    sessionData.value = res;
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Không thể bắt đầu phiên chơi';
  } finally {
    loading.value = false;
  }
}

function handleSelectOption(option: GameOption) {
  if (hasAnsweredCurrent.value) return;

  selectedOptionId.value = option.id;
  hasAnsweredCurrent.value = true;
  const isCorrect = !!option.isCorrect;
  lastAnswerCorrect.value = isCorrect;

  if (isCorrect) correctCount.value++;
  else wrongCount.value++;

  recordedAnswers.value.push({
    questionId: currentQuestion.value?.id,
    selectedOptionId: option.id,
    answerText: option.optionText,
    isCorrect,
  });
}

function submitTypedAnswer() {
  if (hasAnsweredCurrent.value || !userTypedAnswer.value.trim()) return;

  const typed = userTypedAnswer.value.trim().toLowerCase();
  const target = (currentQuestion.value?.targetAnswer || '').trim().toLowerCase();
  const isCorrect = typed === target;

  hasAnsweredCurrent.value = true;
  lastAnswerCorrect.value = isCorrect;

  if (isCorrect) correctCount.value++;
  else wrongCount.value++;

  recordedAnswers.value.push({
    questionId: currentQuestion.value?.id,
    answerText: userTypedAnswer.value.trim(),
    isCorrect,
  });
}

function handleFlashcardRating(isCorrect: boolean) {
  hasAnsweredCurrent.value = true;
  lastAnswerCorrect.value = isCorrect;

  if (isCorrect) correctCount.value++;
  else wrongCount.value++;

  recordedAnswers.value.push({
    questionId: currentQuestion.value?.id,
    answerText: isCorrect ? 'MASTERED' : 'FORGOT',
    isCorrect,
  });

  nextQuestion();
}

function getOptionClass(option: GameOption) {
  if (!hasAnsweredCurrent.value) {
    return 'bg-white hover:bg-slate-50 hover:border-brand-300 border-slate-200 text-slate-800';
  }

  if (option.isCorrect) {
    return 'bg-emerald-50 border-emerald-300 text-emerald-900 font-bold';
  }

  if (selectedOptionId.value === option.id && !option.isCorrect) {
    return 'bg-rose-50 border-rose-300 text-rose-900 font-bold';
  }

  return 'bg-slate-50 border-slate-200 text-slate-400 opacity-60';
}

async function nextQuestion() {
  if (currentQuestionIndex.value < totalQuestions.value - 1) {
    currentQuestionIndex.value++;
    hasAnsweredCurrent.value = false;
    selectedOptionId.value = null;
    userTypedAnswer.value = '';
    isFlashcardMeaningShown.value = false;
  } else {
    await finishGame();
  }
}

async function finishGame() {
  if (!sessionData.value) return;

  try {
    const res = await gameService.submitAnswers(sessionData.value.sessionId, recordedAnswers.value);
    gameResult.value = res;
    isGameOver.value = true;
    toastStore.success('Hoàn thành trò chơi! Đã ghi nhận điểm số.');
  } catch (err) {
    toastStore.error('Không thể lưu kết quả trò chơi');
  }
}
</script>
