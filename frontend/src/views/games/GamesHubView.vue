<template>
  <div class="space-y-6 w-full select-none">
    <!-- Hero Arcade Header Banner -->
    <div class="relative overflow-hidden bg-gradient-to-r from-slate-950 via-indigo-950 to-slate-900 text-white p-6 sm:p-8 rounded-2xl border border-indigo-800/40 shadow-xl">
      <!-- Ambient Glow Elements -->
      <div class="absolute -top-16 -right-16 w-64 h-64 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none"></div>
      <div class="absolute -bottom-16 -left-16 w-64 h-64 bg-rose-500/10 rounded-full blur-3xl pointer-events-none"></div>

      <div class="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div class="space-y-2 max-w-xl">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-400/30 text-xs font-black uppercase tracking-wider shadow-xs">
            <Gamepad2 class="w-4 h-4 animate-bounce" />
            <span>Arcade Learning Arena • Trò chơi tương tác</span>
          </div>
          <h2 class="text-2xl sm:text-3xl md:text-4xl font-black tracking-tight text-white drop-shadow-md">
            Luyện Tiếng Anh Cực Cuốn
          </h2>
          <p class="text-xs sm:text-sm text-slate-300 leading-relaxed">
            Nâng trình từ vựng và phản xạ ngữ pháp qua các mini-game theo phong cách arcade: tính giờ, combo streak, bảng xếp hạng và hiệu ứng âm thanh sống động!
          </p>
        </div>

        <!-- Quick Launch CTA & Sound Control -->
        <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
          <button
            type="button"
            class="px-5 py-3 rounded-xl font-extrabold text-sm text-white bg-gradient-to-r from-indigo-600 via-indigo-500 to-rose-500 hover:from-indigo-500 hover:to-rose-400 shadow-lg shadow-indigo-500/25 transition-all transform hover:scale-105 active:scale-95 flex items-center justify-center gap-2"
            @click="playRandomGame"
          >
            <Zap class="w-4 h-4 fill-current" />
            <span>Chơi Ngẫu Nhiên (Random)</span>
          </button>
        </div>
      </div>

      <!-- Quick Feature Bar -->
      <div class="relative z-10 grid grid-cols-2 sm:grid-cols-4 gap-3 pt-6 mt-6 border-t border-white/10 text-xs">
        <div class="flex items-center gap-2.5 text-slate-300">
          <div class="p-2 rounded-lg bg-white/10 text-amber-400">
            <Flame class="w-4 h-4 fill-current" />
          </div>
          <div>
            <span class="font-bold text-white block">Combo Multiplier</span>
            <span class="text-[11px] text-slate-400">Tăng điểm x2 - x5</span>
          </div>
        </div>

        <div class="flex items-center gap-2.5 text-slate-300">
          <div class="p-2 rounded-lg bg-white/10 text-rose-400">
            <Heart class="w-4 h-4 fill-current" />
          </div>
          <div>
            <span class="font-bold text-white block">3 Mạng (Lives)</span>
            <span class="text-[11px] text-slate-400">Thử thách arcade</span>
          </div>
        </div>

        <div class="flex items-center gap-2.5 text-slate-300">
          <div class="p-2 rounded-lg bg-white/10 text-emerald-400">
            <Sparkles class="w-4 h-4" />
          </div>
          <div>
            <span class="font-bold text-white block">Rank S+ & Huy Chương</span>
            <span class="text-[11px] text-slate-400">Xếp hạng độ chính xác</span>
          </div>
        </div>

        <div class="flex items-center gap-2.5 text-slate-300">
          <div class="p-2 rounded-lg bg-white/10 text-indigo-400">
            <Volume2 class="w-4 h-4" />
          </div>
          <div>
            <span class="font-bold text-white block">Âm Thanh Realtime</span>
            <span class="text-[11px] text-slate-400">SFX & Phát âm chuẩn</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Games Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      <!-- Game 1: Flashcard -->
      <div class="group bg-white p-6 rounded-2xl border-2 border-slate-200/80 shadow-xs hover:border-indigo-400 hover:shadow-xl transition-all duration-300 flex flex-col justify-between relative overflow-hidden">
        <div class="absolute top-0 right-0 w-28 h-28 bg-indigo-50 rounded-full blur-2xl group-hover:bg-indigo-100 transition-colors pointer-events-none"></div>

        <div class="space-y-3 relative z-10">
          <div class="flex items-center justify-between">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-tr from-indigo-500 to-indigo-700 text-white flex items-center justify-center shadow-md shadow-indigo-100 group-hover:scale-110 transition-transform">
              <Layers class="w-6 h-6" />
            </div>
            <span class="text-[11px] font-bold px-2.5 py-1 rounded-full bg-indigo-50 text-indigo-700 border border-indigo-200 flex items-center gap-1.5">
              <CheckCircle2 class="w-3.5 h-3.5 text-indigo-600" />
              <span>Dễ • Spaced Repetition</span>
            </span>
          </div>

          <div>
            <h3 class="font-black text-slate-900 text-lg group-hover:text-indigo-600 transition-colors">
              1. Flashcard 3D Lật Thẻ
            </h3>
            <p class="text-xs sm:text-sm text-slate-600 mt-1.5 leading-relaxed">
              Lật thẻ 3D kiểm tra trí nhớ từ vựng, tự đánh giá Spaced Repetition kèm phát âm chuẩn và ví dụ trực quan.
            </p>
          </div>

          <div class="flex items-center gap-2 text-xs text-slate-500 font-semibold pt-1">
            <span class="flex items-center gap-1 text-amber-600 bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
              <Zap class="w-3 h-3 fill-current" /> +100 XP
            </span>
            <span class="bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
              3D Interactive
            </span>
          </div>
        </div>

        <div class="pt-5 mt-4 border-t border-slate-100 flex items-center justify-between relative z-10">
          <span class="text-xs font-bold text-slate-500">Từ vựng TOEIC</span>
          <router-link to="/games/play/FLASHCARD" @click="gameAudio.playClick">
            <AppButton variant="primary" size="md" class="group-hover:shadow-md">
              Vào đấu trường
            </AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 2: Multiple Choice -->
      <div class="group bg-white p-6 rounded-2xl border-2 border-slate-200/80 shadow-xs hover:border-purple-400 hover:shadow-xl transition-all duration-300 flex flex-col justify-between relative overflow-hidden">
        <div class="absolute top-0 right-0 w-28 h-28 bg-purple-50 rounded-full blur-2xl group-hover:bg-purple-100 transition-colors pointer-events-none"></div>

        <div class="space-y-3 relative z-10">
          <div class="flex items-center justify-between">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-tr from-purple-500 to-purple-700 text-white flex items-center justify-center shadow-md shadow-purple-100 group-hover:scale-110 transition-transform">
              <HelpCircle class="w-6 h-6" />
            </div>
            <span class="text-[11px] font-bold px-2.5 py-1 rounded-full bg-purple-50 text-purple-700 border border-purple-200 flex items-center gap-1.5">
              <Clock class="w-3.5 h-3.5 text-purple-600" />
              <span>Trung bình • Speed Run</span>
            </span>
          </div>

          <div>
            <h3 class="font-black text-slate-900 text-lg group-hover:text-purple-600 transition-colors">
              2. Trắc Nghiệm 4 Lựa Chọn
            </h3>
            <p class="text-xs sm:text-sm text-slate-600 mt-1.5 leading-relaxed">
              Chọn nhanh nghĩa tiếng Việt chính xác với phím tắt 1, 2, 3, 4 hoặc A, B, C, D dưới áp lực thời gian.
            </p>
          </div>

          <div class="flex items-center gap-2 text-xs text-slate-500 font-semibold pt-1">
            <span class="flex items-center gap-1 text-purple-600 bg-purple-50 px-2 py-0.5 rounded border border-purple-200">
              <Flame class="w-3 h-3 fill-current" /> Combo Streak
            </span>
            <span class="bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
              Hotkeys A/B/C/D
            </span>
          </div>
        </div>

        <div class="pt-5 mt-4 border-t border-slate-100 flex items-center justify-between relative z-10">
          <span class="text-xs font-bold text-slate-500">Trắc nghiệm</span>
          <router-link to="/games/play/MULTIPLE_CHOICE" @click="gameAudio.playClick">
            <AppButton variant="primary" size="md" class="group-hover:shadow-md">
              Vào đấu trường
            </AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 3: Word Meaning / Spelling -->
      <div class="group bg-white p-6 rounded-2xl border-2 border-slate-200/80 shadow-xs hover:border-emerald-400 hover:shadow-xl transition-all duration-300 flex flex-col justify-between relative overflow-hidden">
        <div class="absolute top-0 right-0 w-28 h-28 bg-emerald-50 rounded-full blur-2xl group-hover:bg-emerald-100 transition-colors pointer-events-none"></div>

        <div class="space-y-3 relative z-10">
          <div class="flex items-center justify-between">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-tr from-emerald-500 to-emerald-700 text-white flex items-center justify-center shadow-md shadow-emerald-100 group-hover:scale-110 transition-transform">
              <Keyboard class="w-6 h-6" />
            </div>
            <span class="text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1.5">
              <Flame class="w-3.5 h-3.5 text-emerald-600" />
              <span>Thử thách • Wordle Scramble</span>
            </span>
          </div>

          <div>
            <h3 class="font-black text-slate-900 text-lg group-hover:text-emerald-600 transition-colors">
              3. Gõ Từ & Ghép Chữ Cái
            </h3>
            <p class="text-xs sm:text-sm text-slate-600 mt-1.5 leading-relaxed">
              Nhìn định nghĩa tiếng Việt và ghép các ô chữ cái xáo trộn hoặc gõ trực tiếp từ bàn phím để rèn chính tả.
            </p>
          </div>

          <div class="flex items-center gap-2 text-xs text-slate-500 font-semibold pt-1">
            <span class="flex items-center gap-1 text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
              <Sparkles class="w-3 h-3" /> Letter Tiles
            </span>
            <span class="bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
              Hint Support
            </span>
          </div>
        </div>

        <div class="pt-5 mt-4 border-t border-slate-100 flex items-center justify-between relative z-10">
          <span class="text-xs font-bold text-slate-500">Chính tả (Spelling)</span>
          <router-link to="/games/play/WORD_MEANING" @click="gameAudio.playClick">
            <AppButton variant="primary" size="md" class="group-hover:shadow-md">
              Vào đấu trường
            </AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 4: Sentence Completion -->
      <div class="group bg-white p-6 rounded-2xl border-2 border-slate-200/80 shadow-xs hover:border-amber-400 hover:shadow-xl transition-all duration-300 flex flex-col justify-between relative overflow-hidden">
        <div class="absolute top-0 right-0 w-28 h-28 bg-amber-50 rounded-full blur-2xl group-hover:bg-amber-100 transition-colors pointer-events-none"></div>

        <div class="space-y-3 relative z-10">
          <div class="flex items-center justify-between">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-tr from-amber-500 to-amber-700 text-white flex items-center justify-center shadow-md shadow-amber-100 group-hover:scale-110 transition-transform">
              <FileText class="w-6 h-6" />
            </div>
            <span class="text-[11px] font-bold px-2.5 py-1 rounded-full bg-amber-50 text-amber-700 border border-amber-200 flex items-center gap-1.5">
              <Clock class="w-3.5 h-3.5 text-amber-600" />
              <span>Trung bình • Ngữ cảnh TOEIC</span>
            </span>
          </div>

          <div>
            <h3 class="font-black text-slate-900 text-lg group-hover:text-amber-600 transition-colors">
              4. Điền Từ Vào Câu
            </h3>
            <p class="text-xs sm:text-sm text-slate-600 mt-1.5 leading-relaxed">
              Chọn từ vựng chính xác để điền vào chỗ trống trong câu văn mẫu xuất hiện trong đề thi TOEIC Part 5 & 6.
            </p>
          </div>

          <div class="flex items-center gap-2 text-xs text-slate-500 font-semibold pt-1">
            <span class="flex items-center gap-1 text-amber-600 bg-amber-50 px-2 py-0.5 rounded border border-amber-200">
              <Zap class="w-3 h-3 fill-current" /> Ngữ cảnh thực
            </span>
            <span class="bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
              Part 5 Training
            </span>
          </div>
        </div>

        <div class="pt-5 mt-4 border-t border-slate-100 flex items-center justify-between relative z-10">
          <span class="text-xs font-bold text-slate-500">Câu mẫu & Cụm từ</span>
          <router-link to="/games/play/SENTENCE_COMPLETION" @click="gameAudio.playClick">
            <AppButton variant="primary" size="md" class="group-hover:shadow-md">
              Vào đấu trường
            </AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 5: Grammar Quiz -->
      <div class="group bg-white p-6 rounded-2xl border-2 border-slate-200/80 shadow-xs hover:border-rose-400 hover:shadow-xl transition-all duration-300 flex flex-col justify-between relative overflow-hidden">
        <div class="absolute top-0 right-0 w-28 h-28 bg-rose-50 rounded-full blur-2xl group-hover:bg-rose-100 transition-colors pointer-events-none"></div>

        <div class="space-y-3 relative z-10">
          <div class="flex items-center justify-between">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-tr from-rose-500 to-rose-700 text-white flex items-center justify-center shadow-md shadow-rose-100 group-hover:scale-110 transition-transform">
              <Sparkles class="w-6 h-6" />
            </div>
            <span class="text-[11px] font-bold px-2.5 py-1 rounded-full bg-rose-50 text-rose-700 border border-rose-200 flex items-center gap-1.5">
              <Award class="w-3.5 h-3.5 text-rose-600" />
              <span>Nâng cao • Ngữ pháp cốt lõi</span>
            </span>
          </div>

          <div>
            <h3 class="font-black text-slate-900 text-lg group-hover:text-rose-600 transition-colors">
              5. Trắc Nghiệm Cấu Trúc Ngữ Pháp
            </h3>
            <p class="text-xs sm:text-sm text-slate-600 mt-1.5 leading-relaxed">
              Kiểm tra khả năng phân biệt cấu trúc các thì, mệnh đề quan hệ, câu điều kiện và quy tắc ngữ pháp TOEIC.
            </p>
          </div>

          <div class="flex items-center gap-2 text-xs text-slate-500 font-semibold pt-1">
            <span class="flex items-center gap-1 text-rose-600 bg-rose-50 px-2 py-0.5 rounded border border-rose-200">
              <Award class="w-3 h-3" /> Master Rules
            </span>
            <span class="bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
              Grammar Matrix
            </span>
          </div>
        </div>

        <div class="pt-5 mt-4 border-t border-slate-100 flex items-center justify-between relative z-10">
          <span class="text-xs font-bold text-slate-500">Ngữ pháp chuyên sâu</span>
          <router-link to="/games/play/GRAMMAR_QUIZ" @click="gameAudio.playClick">
            <AppButton variant="primary" size="md" class="group-hover:shadow-md">
              Vào đấu trường
            </AppButton>
          </router-link>
        </div>
      </div>

      <!-- Game 6: Sky Shooter (Bắn Máy Bay) -->
      <div class="group bg-white p-6 rounded-2xl border-2 border-slate-200/80 shadow-xs hover:border-cyan-400 hover:shadow-xl transition-all duration-300 flex flex-col justify-between relative overflow-hidden">
        <div class="absolute top-0 right-0 w-28 h-28 bg-cyan-50 rounded-full blur-2xl group-hover:bg-cyan-100 transition-colors pointer-events-none"></div>

        <div class="space-y-3 relative z-10">
          <div class="flex items-center justify-between">
            <div class="w-12 h-12 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-700 text-white flex items-center justify-center shadow-md shadow-cyan-100 group-hover:scale-110 transition-transform">
              <Crosshair class="w-6 h-6" />
            </div>
            <span class="text-[11px] font-bold px-2.5 py-1 rounded-full bg-cyan-50 text-cyan-800 border border-cyan-200 flex items-center gap-1.5">
              <Flame class="w-3.5 h-3.5 text-cyan-600" />
              <span>Thử thách • Sky Dogfight</span>
            </span>
          </div>

          <div>
            <h3 class="font-black text-slate-900 text-lg group-hover:text-cyan-600 transition-colors">
              6. Bắn Máy Bay Từ Vựng (Sky Shooter)
            </h3>
            <p class="text-xs sm:text-sm text-slate-600 mt-1.5 leading-relaxed">
              Điều khiển chiến cơ vũ trụ, ngắm bắn và tiêu diệt phi thuyền địch mang nghĩa chính xác của từ vựng tiếng Anh.
            </p>
          </div>

          <div class="flex items-center gap-2 text-xs text-slate-500 font-semibold pt-1">
            <span class="flex items-center gap-1 text-cyan-700 bg-cyan-50 px-2 py-0.5 rounded border border-cyan-200">
              <Zap class="w-3 h-3 fill-current" /> +120 XP
            </span>
            <span class="bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
              Laser Plasma Combat
            </span>
          </div>
        </div>

        <div class="pt-5 mt-4 border-t border-slate-100 flex items-center justify-between relative z-10">
          <span class="text-xs font-bold text-slate-500">Phản xạ & Không chiến</span>
          <router-link to="/games/play/AIRPLANE_SHOOTER" @click="gameAudio.playClick">
            <AppButton variant="primary" size="md" class="group-hover:shadow-md">
              Vào đấu trường
            </AppButton>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import AppButton from '../../components/common/AppButton.vue';
import { gameAudio } from '../../utils/game-audio';
import {
  Gamepad2,
  Layers,
  HelpCircle,
  Keyboard,
  FileText,
  Sparkles,
  Zap,
  Flame,
  Heart,
  Volume2,
  Award,
  CheckCircle2,
  Clock,
  Crosshair,
} from 'lucide-vue-next';

const router = useRouter();
const gameCodes = ['FLASHCARD', 'MULTIPLE_CHOICE', 'WORD_MEANING', 'SENTENCE_COMPLETION', 'GRAMMAR_QUIZ', 'AIRPLANE_SHOOTER'];

function playRandomGame() {
  gameAudio.playClick();
  const randomCode = gameCodes[Math.floor(Math.random() * gameCodes.length)];
  router.push(`/games/play/${randomCode}`);
}
</script>
