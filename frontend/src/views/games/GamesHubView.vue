<template>
  <div class="space-y-6 w-full">
    <!-- Header -->
    <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <div class="flex items-center gap-3">
          <div class="p-2.5 rounded-md bg-brand-50 text-brand-600">
            <Gamepad2 class="w-6 h-6" />
          </div>
          <div>
            <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Mini Games Ôn Luyện</h2>
            <p class="text-sm sm:text-base text-slate-500 mt-1">
              Củng cố từ vựng và ngữ pháp qua các trò chơi tương tác nhanh
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Games Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <!-- Game 1: Flashcard -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs hover:border-brand-300 hover:shadow-sm transition-all flex flex-col justify-between">
        <div>
          <div class="w-10 h-10 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center mb-3">
            <Layers class="w-5 h-5" />
          </div>
          <h3 class="font-bold text-slate-900 text-base">1. Flashcard Ôn Tập</h3>
          <p class="text-sm text-slate-600 mt-1.5 leading-relaxed">
            Lật thẻ từ vựng kiểm tra trí nhớ và đánh giá theo Spaced Repetition (Forgot / Hard / Good / Easy).
          </p>
        </div>
        <div class="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between">
          <span class="text-xs font-semibold text-brand-600 bg-brand-50 px-2.5 py-1 rounded-sm">Từ vựng</span>
          <router-link to="/games/play/FLASHCARD">
            <AppButton variant="primary" size="sm">Bắt đầu chơi</AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 2: Multiple Choice -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs hover:border-brand-300 hover:shadow-sm transition-all flex flex-col justify-between">
        <div>
          <div class="w-10 h-10 rounded-md bg-purple-50 text-purple-600 flex items-center justify-center mb-3">
            <HelpCircle class="w-5 h-5" />
          </div>
          <h3 class="font-bold text-slate-900 text-base">2. Trắc Nghiệm 4 Lựa Chọn</h3>
          <p class="text-sm text-slate-600 mt-1.5 leading-relaxed">
            Chọn nghĩa tiếng Việt chính xác nhất cho từ vựng trong 4 đáp án cho trước.
          </p>
        </div>
        <div class="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between">
          <span class="text-xs font-semibold text-purple-600 bg-purple-50 px-2.5 py-1 rounded-sm">Trắc nghiệm</span>
          <router-link to="/games/play/MULTIPLE_CHOICE">
            <AppButton variant="primary" size="sm">Bắt đầu chơi</AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 3: Word Meaning -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs hover:border-brand-300 hover:shadow-sm transition-all flex flex-col justify-between">
        <div>
          <div class="w-10 h-10 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center mb-3">
            <Keyboard class="w-5 h-5" />
          </div>
          <h3 class="font-bold text-slate-900 text-base">3. Gõ Từ Theo Nghĩa</h3>
          <p class="text-sm text-slate-600 mt-1.5 leading-relaxed">
            Nhìn định nghĩa tiếng Việt và gõ lại chính xác từ vựng tiếng Anh để rèn luyện chính tả.
          </p>
        </div>
        <div class="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between">
          <span class="text-xs font-semibold text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-sm">Spelling</span>
          <router-link to="/games/play/WORD_MEANING">
            <AppButton variant="primary" size="sm">Bắt đầu chơi</AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 4: Sentence Completion -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs hover:border-brand-300 hover:shadow-sm transition-all flex flex-col justify-between">
        <div>
          <div class="w-10 h-10 rounded-md bg-amber-50 text-amber-600 flex items-center justify-center mb-3">
            <FileText class="w-5 h-5" />
          </div>
          <h3 class="font-bold text-slate-900 text-base">4. Điền Từ Vào Câu</h3>
          <p class="text-sm text-slate-600 mt-1.5 leading-relaxed">
            Chọn từ vựng chính xác để điền vào chỗ trống trong câu ví dụ thực tế.
          </p>
        </div>
        <div class="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between">
          <span class="text-xs font-semibold text-amber-600 bg-amber-50 px-2.5 py-1 rounded-sm">Ngữ cảnh</span>
          <router-link to="/games/play/SENTENCE_COMPLETION">
            <AppButton variant="primary" size="sm">Bắt đầu chơi</AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 5: Grammar Quiz -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs hover:border-brand-300 hover:shadow-sm transition-all flex flex-col justify-between">
        <div>
          <div class="w-10 h-10 rounded-md bg-rose-50 text-rose-600 flex items-center justify-center mb-3">
            <Sparkles class="w-5 h-5" />
          </div>
          <h3 class="font-bold text-slate-900 text-base">5. Trắc Nghiệm Ngữ Pháp</h3>
          <p class="text-sm text-slate-600 mt-1.5 leading-relaxed">
            Kiểm tra khả năng phân biệt cấu trúc và quy tắc ngữ pháp đã học.
          </p>
        </div>
        <div class="pt-4 mt-4 border-t border-slate-100 flex items-center justify-between">
          <span class="text-xs font-semibold text-rose-600 bg-rose-50 px-2.5 py-1 rounded-sm">Ngữ pháp</span>
          <router-link to="/games/play/GRAMMAR_QUIZ">
            <AppButton variant="primary" size="sm">Bắt đầu chơi</AppButton>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppButton from '../../components/common/AppButton.vue';
import { Gamepad2, Layers, HelpCircle, Keyboard, FileText, Sparkles } from 'lucide-vue-next';
</script>
