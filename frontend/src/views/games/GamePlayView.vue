<template>
  <div class="max-w-3xl mx-auto space-y-4 select-none">
    <!-- Floating XP / Score Popups -->
    <div v-if="floatingScore" class="fixed top-24 left-1/2 -translate-x-1/2 pointer-events-none z-50">
      <div class="animate-float-score bg-gradient-to-r from-amber-500 to-rose-500 text-white font-black text-xl px-4 py-2 rounded-full shadow-lg flex items-center gap-2 border border-amber-300">
        <Sparkles class="w-5 h-5 animate-spin" />
        {{ floatingScore }}
      </div>
    </div>

    <!-- Top Arcade HUD -->
    <div class="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 text-white p-3.5 sm:p-4 rounded-xl shadow-md border border-indigo-800/40">
      <div class="flex items-center justify-between gap-2">
        <!-- Left: Back & Game Info -->
        <div class="flex items-center gap-2.5">
          <router-link
            to="/games"
            class="p-2 text-slate-300 hover:text-white hover:bg-white/10 rounded-lg transition-all"
            title="Quay lại kho game"
            @click="gameAudio.playClick"
          >
            <ArrowLeft class="w-5 h-5" />
          </router-link>
          <div>
            <div class="flex items-center gap-2 flex-wrap">
              <h2 class="text-sm sm:text-base font-extrabold tracking-wide text-white drop-shadow-sm">
                {{ sessionData?.gameName || 'Mini Game' }}
              </h2>
              <span class="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded bg-indigo-500/30 text-indigo-300 border border-indigo-400/30">
                Stage {{ currentQuestionIndex + 1 }}/{{ totalQuestions || 10 }}
              </span>
              <span v-if="topicName && topicName !== 'Tất cả chủ đề'" class="text-[10px] font-bold px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 border border-amber-400/30">
                Chủ đề: {{ topicName }}
              </span>
            </div>
            <span class="text-[11px] text-slate-400">Phiên chơi #{{ sessionData?.sessionId || '---' }}</span>
          </div>
        </div>

        <!-- Center: Lives (Hearts) -->
        <div v-if="!isGameOver && sessionData" class="flex items-center gap-1 bg-black/30 px-3 py-1.5 rounded-full border border-white/10">
          <span
            v-for="i in maxLives"
            :key="i"
            class="transition-transform duration-300 inline-block"
            :class="i <= lives ? 'scale-100 text-rose-500 animate-pulse' : 'scale-90 text-slate-600 opacity-40'"
          >
            <Heart class="w-4 h-4 fill-current" />
          </span>
        </div>

        <!-- Right: Combo & Score & Sound Controls -->
        <div class="flex items-center gap-3">
          <!-- Combo Badge -->
          <div
            v-if="comboStreak >= 2 && !isGameOver"
            class="hidden sm:flex items-center gap-1 px-2.5 py-1 rounded-full bg-gradient-to-r from-amber-500 to-rose-500 text-white font-black text-xs shadow-md animate-combo-glow"
          >
            <Flame class="w-3.5 h-3.5 fill-current animate-bounce" />
            <span>x{{ comboStreak }} COMBO!</span>
          </div>

          <!-- Total Score -->
          <div class="text-right">
            <span class="text-[10px] text-slate-400 block font-semibold uppercase tracking-wider">Điểm số</span>
            <span class="text-base sm:text-lg font-black text-amber-400 tracking-tight font-mono">
              {{ currentScore }}
            </span>
          </div>

          <!-- Audio Mute Toggle -->
          <button
            type="button"
            class="p-2 text-slate-300 hover:text-white hover:bg-white/10 rounded-lg transition-all"
            :title="isMuted ? 'Bật âm thanh' : 'Tắt âm thanh'"
            @click="toggleAudio"
          >
            <VolumeX v-if="isMuted" class="w-4 h-4 text-rose-400" />
            <Volume2 v-else class="w-4 h-4 text-emerald-400" />
          </button>
        </div>
      </div>

      <!-- Timer & Progress Bar (Active Game) -->
      <div v-if="!isGameOver && sessionData" class="mt-3 pt-2.5 border-t border-white/10 space-y-1.5">
        <div class="flex items-center justify-between text-[11px] font-semibold text-slate-300">
          <div class="flex items-center gap-1.5">
            <Clock class="w-3.5 h-3.5" :class="isTimerUrgent ? 'text-rose-400 animate-spin' : 'text-indigo-300'" />
            <span :class="isTimerUrgent ? 'text-rose-400 font-bold animate-pulse' : ''">
              Thời gian: {{ timeLeft }}s
            </span>
          </div>
          <span class="text-slate-400">
            Tiến độ: {{ Math.round(((currentQuestionIndex + (hasAnsweredCurrent ? 1 : 0)) / totalQuestions) * 100) }}%
          </span>
        </div>

        <!-- Timer progress bar -->
        <div class="w-full bg-slate-800 rounded-full h-2 overflow-hidden p-0.5 border border-white/10">
          <div
            class="h-full rounded-full transition-all duration-300 ease-linear"
            :class="timerBarColorClass"
            :style="{ width: `${(timeLeft / maxQuestionTime) * 100}%` }"
          ></div>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="bg-white p-16 rounded-xl border border-slate-200 text-center shadow-xs space-y-4">
      <div class="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
      <p class="text-sm font-semibold text-slate-600">Đang khởi tạo đấu trường trò chơi...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMessage" class="bg-white p-12 rounded-xl border border-rose-200 text-center space-y-4 shadow-sm">
      <div class="w-14 h-14 bg-rose-50 text-rose-500 rounded-full flex items-center justify-center mx-auto">
        <AlertCircle class="w-8 h-8" />
      </div>
      <div class="space-y-1">
        <h3 class="text-base font-bold text-slate-900">Không thể bắt đầu phiên chơi</h3>
        <p class="text-xs text-slate-600 max-w-md mx-auto">{{ errorMessage }}</p>
      </div>
      <div class="pt-2">
        <router-link to="/games">
          <AppButton variant="primary" size="md">Quay lại danh sách Game</AppButton>
        </router-link>
      </div>
    </div>

    <!-- Game Over / Victory Screen -->
    <div
      v-else-if="isGameOver && gameResult"
      class="bg-white p-6 sm:p-8 rounded-xl border border-slate-200 shadow-lg text-center space-y-6 animate-fadeIn"
    >
      <!-- Rank Badge & Celebration -->
      <div class="space-y-2">
        <div
          class="w-20 h-20 rounded-2xl flex items-center justify-center mx-auto shadow-lg transform hover:scale-105 transition-transform"
          :class="resultTierConfig.bg"
        >
          <component :is="resultTierConfig.icon" class="w-10 h-10 text-white" />
        </div>
        <div class="pt-1">
          <span class="inline-block text-xs font-black uppercase tracking-widest px-3 py-1 rounded-full text-white shadow-xs" :class="resultTierConfig.tagBg">
            HẠNG {{ resultTierConfig.tier }} • {{ resultTierConfig.title }}
          </span>
          <h3 class="text-2xl font-black text-slate-900 mt-2">
            {{ isVictory ? 'Chúc Mừng Chiến Thắng!' : 'Hoàn Thành Màn Chơi!' }}
          </h3>
          <p class="text-xs text-slate-500">{{ gameResult.gameName }}</p>
        </div>

        <!-- Stars Rating -->
        <div class="flex justify-center gap-1.5 pt-1">
          <Star
            v-for="s in 3"
            :key="s"
            class="w-7 h-7 transition-all"
            :class="s <= resultTierConfig.stars ? 'text-amber-400 fill-amber-400 filter drop-shadow' : 'text-slate-200'"
          />
        </div>
      </div>

      <!-- Score Summary Grid -->
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 max-w-lg mx-auto bg-slate-50 p-4 rounded-xl border border-slate-200 text-xs">
        <div class="space-y-1">
          <span class="text-slate-400 text-[11px] block font-medium">Tổng điểm</span>
          <span class="text-xl font-black text-indigo-600 font-mono">{{ currentScore }}</span>
        </div>
        <div class="space-y-1 border-l border-slate-200">
          <span class="text-slate-400 text-[11px] block font-medium">Độ chính xác</span>
          <span class="text-xl font-black text-emerald-600 font-mono">{{ gameResult.score }}%</span>
        </div>
        <div class="space-y-1 border-l border-slate-200">
          <span class="text-slate-400 text-[11px] block font-medium">Combo cao nhất</span>
          <span class="text-xl font-black text-amber-500 font-mono flex items-center justify-center gap-1">
            <Flame class="w-4 h-4 fill-current text-amber-500" /> x{{ maxComboStreak }}
          </span>
        </div>
        <div class="space-y-1 border-l border-slate-200">
          <span class="text-slate-400 text-[11px] block font-medium">Đúng / Tổng</span>
          <span class="text-xl font-black text-slate-800 font-mono">{{ gameResult.correctAnswers }}/{{ gameResult.totalQuestions }}</span>
        </div>
      </div>

      <!-- Mistakes Review Section -->
      <div v-if="wrongAnswersList.length > 0" class="max-w-lg mx-auto text-left space-y-2">
        <div class="flex items-center justify-between">
          <span class="text-xs font-bold text-slate-700 flex items-center gap-1.5">
            <BookOpen class="w-3.5 h-3.5 text-rose-500" />
            Xem lại {{ wrongAnswersList.length }} câu cần chú ý:
          </span>
        </div>
        <div class="space-y-2 max-h-48 overflow-y-auto pr-1">
          <div
            v-for="(w, idx) in wrongAnswersList"
            :key="idx"
            class="p-2.5 bg-rose-50/70 border border-rose-200 rounded-lg text-xs space-y-1"
          >
            <div class="font-bold text-rose-900">{{ w.question }}</div>
            <div class="text-emerald-700 font-semibold flex items-center gap-1.5">
              <CheckCircle2 class="w-3.5 h-3.5 text-emerald-600 shrink-0" />
              <span>Đáp án đúng: {{ w.correctAnswer }}</span>
            </div>
            <div v-if="w.explanation" class="text-slate-600 italic text-[11px]">{{ w.explanation }}</div>
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex flex-wrap justify-center gap-3 pt-2">
        <AppButton variant="primary" size="md" :icon="RotateCcw" @click="startNewGame">
          Chơi lại ván mới
        </AppButton>
        <router-link to="/games">
          <AppButton variant="secondary" size="md" :icon="Gamepad2">
            Quay lại kho game
          </AppButton>
        </router-link>
      </div>
    </div>

    <!-- Active Question Arena -->
    <div
      v-else-if="currentQuestion"
      :class="[
        'bg-white rounded-xl border shadow-sm overflow-hidden flex flex-col justify-between min-h-[420px] transition-all',
        isShaking ? 'animate-shake border-rose-400 shadow-rose-100' : 'border-slate-200',
      ]"
    >
      <!-- Question Card Body -->
      <div class="p-6 sm:p-8 space-y-6">
        <!-- Question Prompt & Header -->
        <div class="text-center space-y-2.5">
          <div class="flex items-center justify-center gap-2">
            <span v-if="currentQuestion.prompt" class="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-600 border border-slate-200">
              {{ currentQuestion.prompt }}
            </span>
            <span v-if="currentQuestion.difficulty" class="text-[11px] font-bold px-2 py-0.5 rounded bg-indigo-50 text-indigo-600 border border-indigo-200">
              Level {{ currentQuestion.difficulty }}
            </span>
          </div>

          <!-- Main Question Display -->
          <div class="relative flex items-center justify-center gap-3">
            <h3 class="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight leading-snug">
              {{ currentQuestion.questionText }}
            </h3>
            <!-- Speaker TTS Button -->
            <button
              v-if="gameCode !== 'WORD_MEANING'"
              type="button"
              class="p-2 rounded-full text-indigo-600 hover:bg-indigo-50 border border-indigo-200 transition-transform active:scale-90"
              title="Phát âm từ này"
              @click="speakCurrentWord"
            >
              <Volume2 class="w-5 h-5" />
            </button>
          </div>
        </div>

        <!-- Mode 6: Airplane / Sky Shooter (Arcade Space Jet Arena) -->
        <div
          v-if="gameCode === 'AIRPLANE_SHOOTER' && currentQuestion.options && currentQuestion.options.length > 0"
          class="relative w-full rounded-2xl bg-gradient-to-b from-slate-950 via-slate-900 to-indigo-950 p-4 sm:p-6 overflow-hidden border-2 border-indigo-500/40 shadow-2xl min-h-[460px] flex flex-col justify-between"
          @mousemove="handleJetMouseMove"
        >
          <!-- Starry Space Background Effect -->
          <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-indigo-900/30 via-slate-950/80 to-black pointer-events-none"></div>

          <!-- Top Mission Target HUD -->
          <div class="relative z-10 text-center space-y-1 bg-black/60 p-3 rounded-xl border border-cyan-500/40 backdrop-blur-xs max-w-lg mx-auto w-full shadow-lg">
            <div class="flex items-center justify-center gap-1.5 text-cyan-400 text-xs font-black uppercase tracking-widest">
              <Crosshair class="w-3.5 h-3.5 animate-spin text-cyan-400" />
              <span>MỤC TIÊU PHÁ HỦY TÀU ĐỊCH</span>
            </div>
            <div class="flex items-center justify-center gap-2">
              <h4 class="text-2xl sm:text-3xl font-black text-white tracking-tight drop-shadow-[0_0_12px_rgba(34,211,238,0.6)]">
                {{ currentQuestion.questionText.replace('Nghĩa của từ "', '').replace('" là gì?', '') }}
              </h4>
              <button
                type="button"
                class="p-1.5 rounded-full bg-cyan-500/20 text-cyan-300 hover:bg-cyan-500/40 transition-transform active:scale-90"
                title="Phát âm từ này"
                @click="speakCurrentWord"
              >
                <Volume2 class="w-4 h-4" />
              </button>
            </div>
            <p v-if="currentQuestion.prompt" class="text-[11px] text-cyan-200/80 font-mono">
              {{ currentQuestion.prompt }}
            </p>
          </div>

          <!-- Enemy Squad Formation (4 Enemy Planes / Drones) -->
          <div class="relative z-10 grid grid-cols-2 sm:grid-cols-4 gap-3 my-4">
            <div
              v-for="(opt, idx) in currentQuestion.options"
              :key="opt.id"
              :class="[
                'group relative p-3.5 rounded-xl border-2 transition-all cursor-pointer text-center flex flex-col items-center justify-between min-h-[140px]',
                getAirplaneEnemyClass(opt),
              ]"
              @click="shootEnemyPlane(opt, idx)"
            >
              <!-- Enemy Target Reticle & Letter Badge -->
              <div class="flex items-center justify-between w-full text-[11px] font-mono font-black">
                <span
                  class="w-6 h-6 rounded-md flex items-center justify-center border"
                  :class="getAirplaneBadgeClass(opt, idx)"
                >
                  {{ ['A', 'B', 'C', 'D'][idx] }}
                </span>
                <Crosshair class="w-4 h-4 text-cyan-400 opacity-60 group-hover:opacity-100 group-hover:scale-125 transition-all" />
              </div>

              <!-- Enemy Spaceship Graphic -->
              <div class="my-2 transform transition-transform group-hover:scale-115 group-hover:-translate-y-1">
                <!-- SVG Alien Fighter Jet -->
                <svg class="w-12 h-12 filter drop-shadow-[0_0_8px_rgba(244,63,94,0.6)]" viewBox="0 0 64 64" fill="none">
                  <path d="M32 6L40 24L58 34L42 38L40 54L32 46L24 54L22 38L6 34L24 24L32 6Z" fill="#e11d48" stroke="#fecdd3" stroke-width="2" />
                  <circle cx="32" cy="28" r="4" fill="#38bdf8" />
                  <path d="M30 48L32 58L34 48Z" fill="#fbbf24" />
                </svg>
              </div>

              <!-- Enemy Meaning Label Box -->
              <div class="w-full bg-black/60 p-2 rounded-lg border border-white/10 text-xs font-bold text-slate-100 group-hover:text-cyan-300 group-hover:border-cyan-400/50 transition-colors line-clamp-2">
                {{ opt.optionText }}
              </div>

              <!-- Explosion or Defeat Overlay -->
              <div
                v-if="hasAnsweredCurrent && opt.isCorrect"
                class="absolute inset-0 bg-emerald-500/20 backdrop-blur-2xs rounded-xl flex items-center justify-center border-2 border-emerald-400 animate-pulse"
              >
                <div class="bg-emerald-600 text-white p-2 rounded-full shadow-lg">
                  <Check class="w-6 h-6 animate-bounce" />
                </div>
              </div>
              <div
                v-else-if="hasAnsweredCurrent && selectedOptionId === opt.id && !opt.isCorrect"
                class="absolute inset-0 bg-rose-500/20 backdrop-blur-2xs rounded-xl flex items-center justify-center border-2 border-rose-400"
              >
                <div class="bg-rose-600 text-white p-2 rounded-full shadow-lg">
                  <X class="w-6 h-6" />
                </div>
              </div>
            </div>
          </div>

          <!-- Player Jet Fighter Area (Bottom) -->
          <div class="relative z-10 flex flex-col items-center pt-2">
            <!-- Animated Laser Bolt FX -->
            <div
              v-if="laserActive"
              class="absolute bottom-16 h-48 w-1.5 bg-gradient-to-t from-cyan-400 via-sky-200 to-white rounded-full shadow-[0_0_15px_#22d3ee] animate-ping pointer-events-none"
              :style="{ left: `${laserTargetX}%` }"
            ></div>

            <!-- Player Fighter Jet -->
            <div
              class="transition-transform duration-100 ease-out flex flex-col items-center cursor-crosshair group"
              :style="{ transform: `translateX(${playerJetOffset}px)` }"
            >
              <!-- Player Jet SVG -->
              <svg class="w-16 h-16 filter drop-shadow-[0_0_12px_rgba(56,189,248,0.8)]" viewBox="0 0 64 64" fill="none">
                <!-- Wing Lasers -->
                <rect x="8" y="24" width="4" height="12" rx="2" fill="#38bdf8" />
                <rect x="52" y="24" width="4" height="12" rx="2" fill="#38bdf8" />
                <!-- Fuselage -->
                <path d="M32 4L42 28L58 40L44 44L40 58L32 50L24 58L20 44L6 40L22 28L32 4Z" fill="#0284c7" stroke="#bae6fd" stroke-width="2" />
                <!-- Cockpit Canopy -->
                <ellipse cx="32" cy="24" rx="4" ry="10" fill="#38bdf8" stroke="#e0f2fe" stroke-width="1.5" />
                <!-- Engine Flames -->
                <path d="M28 52L32 62L36 52Z" fill="#f59e0b" class="animate-pulse" />
              </svg>

              <!-- Controls Prompt -->
              <span class="text-[10px] text-cyan-300/80 font-mono mt-1 bg-black/40 px-2.5 py-0.5 rounded-full border border-cyan-500/20">
                Bấm phím 1, 2, 3, 4 hoặc A, B, C, D để BẮN TÀU ĐÍCH
              </span>
            </div>
          </div>
        </div>

        <!-- Mode 1: Multiple Choice & Sentence Completion (4 Options) -->
        <div
          v-else-if="currentQuestion.options && currentQuestion.options.length > 0 && gameCode !== 'FLASHCARD' && gameCode !== 'AIRPLANE_SHOOTER'"
          class="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2"
        >
          <button
            v-for="(opt, idx) in currentQuestion.options"
            :key="opt.id"
            type="button"
            :disabled="hasAnsweredCurrent"
            :class="[
              'group p-4 text-left font-semibold rounded-xl border-2 transition-all flex items-center justify-between cursor-pointer relative overflow-hidden',
              getOptionClass(opt),
            ]"
            @click="handleSelectOption(opt)"
          >
            <div class="flex items-center gap-3">
              <!-- Keyboard shortcut tag [A] [B] [C] [D] -->
              <span
                class="w-7 h-7 rounded-lg text-xs font-black flex items-center justify-center transition-colors font-mono"
                :class="getOptionBadgeClass(opt, idx)"
              >
                {{ ['A', 'B', 'C', 'D'][idx] || idx + 1 }}
              </span>
              <span class="text-sm sm:text-base font-bold">{{ opt.optionText }}</span>
            </div>

            <!-- Feedback Icon -->
            <div>
              <Check v-if="hasAnsweredCurrent && opt.isCorrect" class="w-5 h-5 text-emerald-600 animate-bounce" />
              <X v-else-if="hasAnsweredCurrent && selectedOptionId === opt.id && !opt.isCorrect" class="w-5 h-5 text-rose-600" />
            </div>
          </button>
        </div>

        <!-- Mode 2: Word Meaning / Spelling (Duolingo & Wordle Scramble Style) -->
        <div v-else-if="gameCode === 'WORD_MEANING'" class="max-w-md mx-auto space-y-4 pt-2">
          <!-- Letter slot display -->
          <div class="flex flex-wrap items-center justify-center gap-1.5 min-h-[44px] p-2 bg-slate-50 rounded-xl border border-slate-200">
            <span
              v-for="(_, idx) in targetLetters"
              :key="idx"
              class="w-8 h-10 sm:w-9 sm:h-11 rounded-lg border-2 flex items-center justify-center font-mono text-lg font-black uppercase transition-all shadow-xs"
              :class="userTypedLetters[idx] ? 'bg-indigo-600 text-white border-indigo-700 shadow-indigo-100 scale-105' : 'bg-white border-slate-300 text-slate-300'"
            >
              {{ userTypedLetters[idx] || '' }}
            </span>
          </div>

          <!-- Typing Input Field -->
          <div class="space-y-2">
            <AppInput
              ref="textInputRef"
              v-model="userTypedAnswer"
              placeholder="Gõ từ tiếng Anh hoặc bấm các chữ cái dưới..."
              :disabled="hasAnsweredCurrent"
              @enter="submitTypedAnswer"
              @input="onTypedInput"
            />
          </div>

          <!-- Scrambled Letter Tiles Clicker -->
          <div v-if="!hasAnsweredCurrent" class="space-y-2">
            <div class="flex flex-wrap items-center justify-center gap-1.5">
              <button
                v-for="(tile, idx) in scrambledTiles"
                :key="idx"
                type="button"
                :disabled="tile.used"
                class="w-9 h-10 sm:w-10 sm:h-11 rounded-lg font-mono font-black text-base transition-all border-2 flex items-center justify-center active:scale-90"
                :class="tile.used ? 'bg-slate-100 text-slate-300 border-slate-200 cursor-not-allowed' : 'bg-white hover:bg-indigo-50 hover:border-indigo-400 border-slate-300 text-slate-800 shadow-xs cursor-pointer'"
                @click="clickLetterTile(tile, idx)"
              >
                {{ tile.char }}
              </button>
            </div>

            <!-- Letter Controls: Hint & Clear -->
            <div class="flex items-center justify-center gap-2 pt-1">
              <button
                type="button"
                class="px-3 py-1.5 text-xs font-bold rounded-lg border border-amber-300 bg-amber-50 text-amber-800 hover:bg-amber-100 flex items-center gap-1 transition-all"
                :disabled="hintsRemaining <= 0"
                @click="useHintLetter"
              >
                <Lightbulb class="w-3.5 h-3.5 text-amber-600" />
                <span>Gợi ý 1 chữ ({{ hintsRemaining }})</span>
              </button>
              <button
                type="button"
                class="px-3 py-1.5 text-xs font-bold rounded-lg border border-slate-300 bg-slate-50 text-slate-700 hover:bg-slate-100 flex items-center gap-1 transition-all"
                @click="clearTypedAnswer"
              >
                <Trash2 class="w-3.5 h-3.5 text-slate-500" />
                <span>Xóa hết</span>
              </button>
            </div>
          </div>

          <AppButton
            v-if="!hasAnsweredCurrent"
            variant="primary"
            size="md"
            full-width
            :disabled="!userTypedAnswer.trim()"
            @click="submitTypedAnswer"
          >
            Kiểm tra đáp án (Enter)
          </AppButton>
        </div>

        <!-- Mode 3: Flashcard 3D Perspective Card Arena -->
        <div v-else-if="gameCode === 'FLASHCARD'" class="max-w-md mx-auto space-y-4 pt-2">
          <!-- 3D Flip Card Container -->
          <div
            class="perspective-1000 w-full h-56 sm:h-64 cursor-pointer"
            @click="toggleFlashcardFlip"
          >
            <div
              class="w-full h-full relative preserve-3d transition-transform duration-500 rounded-2xl shadow-md border-2"
              :class="[
                isFlashcardFlipped ? 'rotate-y-180 border-indigo-400 shadow-indigo-100' : 'border-slate-200 hover:border-indigo-300',
              ]"
            >
              <!-- Front Side of Flashcard -->
              <div class="absolute inset-0 backface-hidden bg-gradient-to-b from-white to-indigo-50/40 p-6 rounded-2xl flex flex-col justify-between items-center text-center">
                <span class="text-[11px] font-bold uppercase tracking-wider text-indigo-600 bg-indigo-50 px-2.5 py-0.5 rounded-full border border-indigo-100">
                  Mặt trước • Từ vựng
                </span>
                <div class="space-y-1">
                  <h4 class="text-3xl sm:text-4xl font-black text-slate-900 tracking-tight">
                    {{ currentQuestion.questionText }}
                  </h4>
                  <p v-if="currentQuestion.prompt" class="text-sm font-semibold text-indigo-600 font-mono">
                    {{ currentQuestion.prompt }}
                  </p>
                </div>
                <div class="flex items-center gap-2 text-xs font-semibold text-slate-400">
                  <Rotate3d class="w-4 h-4 text-indigo-500 animate-spin" style="animation-duration: 4s;" />
                  <span>Nhấn vào thẻ hoặc bấm <strong>Space</strong> để lật</span>
                </div>
              </div>

              <!-- Back Side of Flashcard -->
              <div class="absolute inset-0 backface-hidden rotate-y-180 bg-gradient-to-b from-white to-emerald-50/40 p-6 rounded-2xl flex flex-col justify-between items-center text-center">
                <span class="text-[11px] font-bold uppercase tracking-wider text-emerald-700 bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-100">
                  Mặt sau • Ý nghĩa & Ví dụ
                </span>
                <div v-if="isFlashcardFlipped" class="space-y-2">
                  <h4 class="text-2xl sm:text-3xl font-black text-emerald-800 tracking-tight">
                    {{ currentQuestion.targetAnswer }}
                  </h4>
                  <p v-if="currentQuestion.explanation" class="text-xs text-slate-600 italic bg-white/80 p-2 rounded-lg border border-slate-100">
                    "{{ currentQuestion.explanation }}"
                  </p>
                </div>
                <div v-else class="h-12 flex items-center justify-center">
                  <!-- Empty placeholder during rotation so answer is never revealed before flipping -->
                </div>
                <div class="text-xs font-bold text-slate-500">
                  Hãy tự đánh giá mức độ ghi nhớ ở bên dưới:
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Explanation & Feedback Banner (Only for non-flashcard games) -->
        <div
          v-if="hasAnsweredCurrent && (currentQuestion.explanation || currentQuestion.targetAnswer) && gameCode !== 'FLASHCARD'"
          :class="[
            'p-4 rounded-xl text-sm border-2 animate-fadeIn flex items-start gap-3',
            lastAnswerCorrect ? 'bg-emerald-50 border-emerald-300 text-emerald-900' : 'bg-rose-50 border-rose-300 text-rose-900',
          ]"
        >
          <div class="p-1 rounded-full text-white mt-0.5" :class="lastAnswerCorrect ? 'bg-emerald-500' : 'bg-rose-500'">
            <Check v-if="lastAnswerCorrect" class="w-4 h-4" />
            <X v-else class="w-4 h-4" />
          </div>
          <div class="space-y-1 flex-1">
            <div class="font-black text-sm flex items-center justify-between">
              <span>{{ lastAnswerCorrect ? 'Chính xác! Xuất sắc lắm!' : 'Chưa chính xác!' }}</span>
              <span v-if="lastAnswerCorrect" class="text-xs font-bold text-emerald-700 font-mono">+100 PTS</span>
            </div>
            <div class="text-xs font-medium">
              <span class="font-bold">Đáp án đúng:</span> {{ currentQuestion.targetAnswer || currentQuestion.explanation }}
            </div>
            <div v-if="currentQuestion.explanation && currentQuestion.explanation !== currentQuestion.targetAnswer" class="text-xs text-slate-600 italic pt-0.5">
              {{ currentQuestion.explanation }}
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom Navigation & Rating Footer -->
      <div class="px-6 py-4 bg-slate-50/80 border-t border-slate-200 flex flex-col sm:flex-row items-center justify-between gap-3">
        <!-- Shortcut hint -->
        <div class="text-xs text-slate-500 flex items-center gap-2">
          <span v-if="!hasAnsweredCurrent && (currentQuestion.options?.length || 0) > 0" class="hidden sm:inline-flex items-center gap-1.5">
            <Lightbulb class="w-3.5 h-3.5 text-amber-500" />
            <span>Phím tắt: Bấm phím <strong>1, 2, 3, 4</strong> hoặc <strong>A, B, C, D</strong> để chọn</span>
          </span>
          <span v-else-if="hasAnsweredCurrent" class="text-indigo-600 font-semibold flex items-center gap-1.5 animate-pulse">
            <Loader2 class="w-3.5 h-3.5 animate-spin text-indigo-600" />
            <span>Đang tự động chuyển câu tiếp theo...</span>
          </span>
        </div>

        <!-- Flashcard rating buttons -->
        <div v-if="gameCode === 'FLASHCARD' && isFlashcardFlipped && !hasAnsweredCurrent" class="flex gap-2 w-full sm:w-auto">
          <AppButton variant="danger" size="sm" :icon="XCircle" class="flex-1 sm:flex-none" @click="handleFlashcardRating(false)">
            Chưa thuộc (1)
          </AppButton>
          <AppButton variant="success" size="sm" :icon="CheckCircle2" class="flex-1 sm:flex-none" @click="handleFlashcardRating(true)">
            Đã nhớ tốt (2)
          </AppButton>
        </div>

        <!-- Next Question button -->
        <AppButton
          v-else-if="hasAnsweredCurrent"
          variant="primary"
          size="md"
          :icon="ChevronRight"
          class="w-full sm:w-auto"
          @click="nextQuestion"
        >
          {{ currentQuestionIndex < totalQuestions - 1 ? 'Câu tiếp theo (Enter)' : 'Tổng kết điểm số' }}
        </AppButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { gameService, type AnswerSubmission } from '../../services/game.service';
import { useToastStore } from '../../stores/toast.store';
import { gameAudio } from '../../utils/game-audio';
import { triggerConfetti } from '../../utils/confetti';
import type { GameSessionStart, GameQuestion, GameOption, GameResult } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import {
  ArrowLeft,
  Trophy,
  RotateCcw,
  ChevronRight,
  Check,
  CheckCircle2,
  X,
  XCircle,
  AlertCircle,
  Heart,
  Flame,
  Volume2,
  VolumeX,
  Clock,
  Sparkles,
  Lightbulb,
  Trash2,
  Rotate3d,
  Star,
  Gamepad2,
  BookOpen,
  Award,
  Crown,
  Medal,
  Crosshair,
  Loader2,
} from 'lucide-vue-next';

const route = useRoute();
const toastStore = useToastStore();

const gameCode = computed(() => (route.params.code as string) || 'FLASHCARD');
const topicId = computed(() => (route.query.topicId ? Number(route.query.topicId) : undefined));
const topicName = computed(() => (route.query.topicName as string) || 'Tất cả chủ đề');

const loading = ref(true);
const errorMessage = ref('');
const sessionData = ref<GameSessionStart | null>(null);
const currentQuestionIndex = ref(0);
const userTypedAnswer = ref('');
const selectedOptionId = ref<number | null>(null);
const isFlashcardFlipped = ref(false);

const hasAnsweredCurrent = ref(false);
const lastAnswerCorrect = ref(false);
const isShaking = ref(false);

// Airplane Shooter States
const laserActive = ref(false);
const laserTargetX = ref(50);
const playerJetOffset = ref(0);

// Lives & Combos & Score
const maxLives = 3;
const lives = ref(3);
const comboStreak = ref(0);
const maxComboStreak = ref(0);
const currentScore = ref(0);
const floatingScore = ref<string | null>(null);

// Timer
const maxQuestionTime = 20; // 20s per question
const timeLeft = ref(20);
let timerInterval: any = null;

// Recorded submissions & mistakes
const recordedAnswers = ref<AnswerSubmission[]>([]);
const correctCount = ref(0);
const wrongCount = ref(0);
const wrongAnswersList = ref<Array<{ question: string; correctAnswer: string; explanation?: string }>>([]);

const isGameOver = ref(false);
const isVictory = ref(false);
const gameResult = ref<GameResult | null>(null);
const isMuted = ref(gameAudio.isMuted);

// Scramble letter tiles for Word Meaning mode
interface LetterTile {
  char: string;
  used: boolean;
  originalIndex: number;
}
const scrambledTiles = ref<LetterTile[]>([]);
const userTypedLetters = computed(() => userTypedAnswer.value.toUpperCase().split(''));
const targetLetters = computed(() => (currentQuestion.value?.targetAnswer || '').toUpperCase().split(''));
const hintsRemaining = ref(2);

const currentQuestion = computed<GameQuestion | null>(() => {
  if (!sessionData.value || !sessionData.value.questions) return null;
  return sessionData.value.questions[currentQuestionIndex.value] || null;
});

const totalQuestions = computed(() => sessionData.value?.questions?.length || 0);

const isTimerUrgent = computed(() => timeLeft.value <= 5 && !hasAnsweredCurrent.value);

const timerBarColorClass = computed(() => {
  if (timeLeft.value > 10) return 'bg-emerald-500';
  if (timeLeft.value > 5) return 'bg-amber-500';
  return 'animate-timer-urgent';
});

// Tier Rank Configuration
const resultTierConfig = computed(() => {
  const score = gameResult.value?.score || 0;
  if (score >= 95) {
    return {
      tier: 'S+',
      title: 'Huyền Thoại (Godlike)',
      icon: Crown,
      bg: 'bg-gradient-to-tr from-amber-500 via-rose-500 to-indigo-600',
      tagBg: 'bg-gradient-to-r from-amber-500 to-rose-500',
      stars: 3,
    };
  }
  if (score >= 80) {
    return {
      tier: 'S',
      title: 'Xuất Sắc (Master)',
      icon: Trophy,
      bg: 'bg-gradient-to-tr from-amber-400 to-amber-600',
      tagBg: 'bg-amber-500',
      stars: 3,
    };
  }
  if (score >= 60) {
    return {
      tier: 'A',
      title: 'Khá Giỏi (Pro)',
      icon: Award,
      bg: 'bg-gradient-to-tr from-indigo-500 to-indigo-700',
      tagBg: 'bg-indigo-600',
      stars: 2,
    };
  }
  if (score >= 40) {
    return {
      tier: 'B',
      title: 'Tạm Ổn (Good)',
      icon: Medal,
      bg: 'bg-gradient-to-tr from-emerald-500 to-emerald-700',
      tagBg: 'bg-emerald-600',
      stars: 1,
    };
  }
  return {
    tier: 'C',
    title: 'Cần Ôn Lại (Practice)',
    icon: RotateCcw,
    bg: 'bg-gradient-to-tr from-slate-500 to-slate-700',
    tagBg: 'bg-slate-600',
    stars: 0,
  };
});

onMounted(() => {
  startNewGame();
  window.addEventListener('keydown', handleGlobalKeydown);
});

onUnmounted(() => {
  stopTimer();
  clearAutoAdvance();
  window.removeEventListener('keydown', handleGlobalKeydown);
});

function toggleAudio() {
  isMuted.value = gameAudio.toggleMute();
}

function speakCurrentWord() {
  if (!currentQuestion.value) return;
  const word = currentQuestion.value.questionText || currentQuestion.value.targetAnswer || '';
  gameAudio.speak(word);
}

async function startNewGame() {
  stopTimer();
  clearAutoAdvance();
  loading.value = true;
  errorMessage.value = '';
  isGameOver.value = false;
  isVictory.value = false;
  gameResult.value = null;
  currentQuestionIndex.value = 0;
  recordedAnswers.value = [];
  wrongAnswersList.value = [];
  correctCount.value = 0;
  wrongCount.value = 0;
  lives.value = maxLives;
  comboStreak.value = 0;
  maxComboStreak.value = 0;
  currentScore.value = 0;
  hasAnsweredCurrent.value = false;
  selectedOptionId.value = null;
  userTypedAnswer.value = '';
  isFlashcardFlipped.value = false;
  hintsRemaining.value = 2;

  try {
    const res = await gameService.startGame(gameCode.value, topicId.value);
    sessionData.value = res;
    initQuestionState();
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Không thể bắt đầu phiên chơi';
  } finally {
    loading.value = false;
  }
}

function initQuestionState() {
  hasAnsweredCurrent.value = false;
  selectedOptionId.value = null;
  userTypedAnswer.value = '';
  isFlashcardFlipped.value = false;
  hintsRemaining.value = 2;

  if (gameCode.value === 'WORD_MEANING' && currentQuestion.value?.targetAnswer) {
    setupScrambleTiles(currentQuestion.value.targetAnswer);
  }

  startTimer();
}

function setupScrambleTiles(targetWord: string) {
  const letters = targetWord.toUpperCase().split('');
  // Add 2 random distractors if short
  const extraChars = ['A', 'E', 'I', 'O', 'U', 'R', 'S', 'T', 'L', 'N'];
  const pool = [...letters];
  if (pool.length <= 6) {
    for (let i = 0; i < 2; i++) {
      pool.push(extraChars[Math.floor(Math.random() * extraChars.length)]);
    }
  }
  // Shuffle
  for (let i = pool.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [pool[i], pool[j]] = [pool[j], pool[i]];
  }

  scrambledTiles.value = pool.map((char, index) => ({
    char,
    used: false,
    originalIndex: index,
  }));
}

function startTimer() {
  stopTimer();
  timeLeft.value = maxQuestionTime;
  timerInterval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
      if (timeLeft.value <= 5 && timeLeft.value > 0) {
        gameAudio.playTimerTick(true);
      }
    } else {
      stopTimer();
      handleTimeOut();
    }
  }, 1000);
}

function stopTimer() {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
}

let autoAdvanceTimeout: any = null;

function clearAutoAdvance() {
  if (autoAdvanceTimeout) {
    clearTimeout(autoAdvanceTimeout);
    autoAdvanceTimeout = null;
  }
}

function scheduleAutoAdvance(delayMs: number = 850) {
  clearAutoAdvance();
  autoAdvanceTimeout = setTimeout(() => {
    if (hasAnsweredCurrent.value && !isGameOver.value) {
      nextQuestion();
    }
  }, delayMs);
}

function handleTimeOut() {
  if (hasAnsweredCurrent.value) return;
  toastStore.warning('Hết thời gian cho câu hỏi này!');
  triggerMistakeEffects('Hết giờ');
  scheduleAutoAdvance(1200);
}

function handleSelectOption(option: GameOption) {
  if (hasAnsweredCurrent.value) return;
  stopTimer();

  selectedOptionId.value = option.id;
  hasAnsweredCurrent.value = true;
  const isCorrect = !!option.isCorrect;
  lastAnswerCorrect.value = isCorrect;

  if (isCorrect) {
    triggerCorrectEffects(option.optionText);
  } else {
    triggerMistakeEffects(option.optionText);
  }

  recordedAnswers.value.push({
    questionId: currentQuestion.value?.id,
    selectedOptionId: option.id,
    answerText: option.optionText,
    isCorrect,
  });

  // Automatically advance to the next question
  scheduleAutoAdvance(gameCode.value === 'AIRPLANE_SHOOTER' ? 750 : 900);
}

function submitTypedAnswer() {
  if (hasAnsweredCurrent.value || !userTypedAnswer.value.trim()) return;
  stopTimer();

  const typed = userTypedAnswer.value.trim().toLowerCase();
  const target = (currentQuestion.value?.targetAnswer || '').trim().toLowerCase();
  const isCorrect = typed === target;

  hasAnsweredCurrent.value = true;
  lastAnswerCorrect.value = isCorrect;

  if (isCorrect) {
    triggerCorrectEffects(userTypedAnswer.value.trim());
  } else {
    triggerMistakeEffects(userTypedAnswer.value.trim());
  }

  recordedAnswers.value.push({
    questionId: currentQuestion.value?.id,
    answerText: userTypedAnswer.value.trim(),
    isCorrect,
  });

  // Automatically advance to next question
  scheduleAutoAdvance(1000);
}

function clickLetterTile(tile: LetterTile, _idx: number) {
  if (hasAnsweredCurrent.value || tile.used) return;
  gameAudio.playClick();
  tile.used = true;
  userTypedAnswer.value += tile.char.toLowerCase();
}

function useHintLetter() {
  if (hintsRemaining.value <= 0 || hasAnsweredCurrent.value) return;
  const target = (currentQuestion.value?.targetAnswer || '').toUpperCase();
  const currentLen = userTypedAnswer.value.length;
  if (currentLen < target.length) {
    const nextChar = target[currentLen];
    // Find unused tile
    const found = scrambledTiles.value.find((t) => t.char === nextChar && !t.used);
    if (found) found.used = true;
    userTypedAnswer.value += nextChar.toLowerCase();
    hintsRemaining.value--;
    gameAudio.playClick();
  }
}

function clearTypedAnswer() {
  userTypedAnswer.value = '';
  scrambledTiles.value.forEach((t) => (t.used = false));
  gameAudio.playClick();
}

function onTypedInput() {
  // Sync scramble tiles state with typed text
  const typed = userTypedAnswer.value.toUpperCase().split('');
  scrambledTiles.value.forEach((t) => (t.used = false));
  typed.forEach((char) => {
    const tile = scrambledTiles.value.find((t) => t.char === char && !t.used);
    if (tile) tile.used = true;
  });
}

function toggleFlashcardFlip() {
  isFlashcardFlipped.value = !isFlashcardFlipped.value;
  gameAudio.playCardFlip();
}

function handleFlashcardRating(isCorrect: boolean) {
  if (hasAnsweredCurrent.value) return;
  stopTimer();
  hasAnsweredCurrent.value = true;
  lastAnswerCorrect.value = isCorrect;

  if (isCorrect) {
    triggerCorrectEffects('MASTERED');
  } else {
    triggerMistakeEffects('FORGOT');
  }

  recordedAnswers.value.push({
    questionId: currentQuestion.value?.id,
    answerText: isCorrect ? 'MASTERED' : 'FORGOT',
    isCorrect,
  });

  // Step 1: Lật thẻ về mặt trước của từ hiện tại ngay lập tức để giấu đáp án
  isFlashcardFlipped.value = false;

  // Step 2: Đợi animation lật thẻ hoàn tất (280ms) rồi mới nạp câu hỏi mới
  setTimeout(() => {
    nextQuestion();
  }, 280);
}

function triggerCorrectEffects(_answerText: string) {
  comboStreak.value++;
  if (comboStreak.value > maxComboStreak.value) {
    maxComboStreak.value = comboStreak.value;
  }
  correctCount.value++;

  // Score physics: Base + Time Bonus + Combo Multiplier
  const basePoints = 100;
  const timeBonus = timeLeft.value * 5;
  const comboBonus = (comboStreak.value - 1) * 25;
  const earned = basePoints + timeBonus + comboBonus;
  currentScore.value += earned;

  floatingScore.value = `+${earned} XP (Combo x${comboStreak.value})`;
  setTimeout(() => {
    floatingScore.value = null;
  }, 900);

  gameAudio.playCorrect(comboStreak.value);

  // Confetti on big combos
  if (comboStreak.value >= 3 && comboStreak.value % 3 === 0) {
    triggerConfetti({ particleCount: 40, duration: 1500 });
  }
}

function triggerMistakeEffects(_answerText: string) {
  comboStreak.value = 0;
  wrongCount.value++;
  lives.value = Math.max(0, lives.value - 1);

  // Screen shake
  isShaking.value = true;
  setTimeout(() => {
    isShaking.value = false;
  }, 450);

  gameAudio.playWrong();
  gameAudio.playHeartLost();

  // Record for review
  if (currentQuestion.value) {
    wrongAnswersList.value.push({
      question: currentQuestion.value.questionText,
      correctAnswer: currentQuestion.value.targetAnswer || currentQuestion.value.options?.find((o) => o.isCorrect)?.optionText || '',
      explanation: currentQuestion.value.explanation,
    });
  }
}

function getOptionClass(option: GameOption) {
  if (!hasAnsweredCurrent.value) {
    return 'bg-white hover:bg-indigo-50/50 hover:border-indigo-400 border-slate-200 text-slate-800 shadow-xs hover:shadow-md';
  }

  if (option.isCorrect) {
    return 'bg-emerald-50 border-emerald-400 text-emerald-950 font-black ring-2 ring-emerald-300 shadow-sm';
  }

  if (selectedOptionId.value === option.id && !option.isCorrect) {
    return 'bg-rose-50 border-rose-400 text-rose-950 font-black ring-2 ring-rose-300 shadow-sm';
  }

  return 'bg-slate-50 border-slate-200 text-slate-400 opacity-50';
}

function getOptionBadgeClass(option: GameOption, _idx: number) {
  if (!hasAnsweredCurrent.value) {
    return 'bg-slate-100 text-slate-700 group-hover:bg-indigo-600 group-hover:text-white border border-slate-300';
  }
  if (option.isCorrect) {
    return 'bg-emerald-600 text-white border-emerald-700';
  }
  if (selectedOptionId.value === option.id && !option.isCorrect) {
    return 'bg-rose-600 text-white border-rose-700';
  }
  return 'bg-slate-200 text-slate-500';
}

function handleJetMouseMove(e: MouseEvent) {
  const target = e.currentTarget as HTMLElement;
  if (!target) return;
  const rect = target.getBoundingClientRect();
  const mouseX = e.clientX - rect.left;
  const centerX = rect.width / 2;
  const maxOffset = rect.width * 0.38;
  playerJetOffset.value = Math.max(-maxOffset, Math.min(maxOffset, mouseX - centerX));
}

function shootEnemyPlane(option: GameOption, idx: number) {
  if (hasAnsweredCurrent.value) return;

  // Align laser position with lane
  laserTargetX.value = (idx + 0.5) * 25;
  laserActive.value = true;
  gameAudio.playLaserShot();

  setTimeout(() => {
    laserActive.value = false;
    if (option.isCorrect) {
      gameAudio.playExplosion();
    }
    handleSelectOption(option);
  }, 120);
}

function getAirplaneEnemyClass(option: GameOption) {
  if (!hasAnsweredCurrent.value) {
    return 'bg-slate-900/80 border-slate-700/80 hover:border-cyan-400 hover:bg-slate-800/90 shadow-md hover:shadow-cyan-500/20';
  }
  if (option.isCorrect) {
    return 'bg-emerald-950/80 border-emerald-400 text-white shadow-lg shadow-emerald-500/30';
  }
  if (selectedOptionId.value === option.id && !option.isCorrect) {
    return 'bg-rose-950/80 border-rose-500 text-white shadow-lg shadow-rose-500/30';
  }
  return 'bg-slate-950/60 border-slate-800 text-slate-500 opacity-40';
}

function getAirplaneBadgeClass(option: GameOption, _idx: number) {
  if (!hasAnsweredCurrent.value) {
    return 'bg-slate-800 text-cyan-300 border-cyan-500/40 group-hover:bg-cyan-500 group-hover:text-black';
  }
  if (option.isCorrect) {
    return 'bg-emerald-500 text-white border-emerald-400';
  }
  if (selectedOptionId.value === option.id && !option.isCorrect) {
    return 'bg-rose-500 text-white border-rose-400';
  }
  return 'bg-slate-800 text-slate-500 border-slate-700';
}

function handleGlobalKeydown(e: KeyboardEvent) {
  if (isGameOver.value || loading.value) return;

  // Spacebar to flip flashcard or continue
  const targetEl = e.target as HTMLElement | null;
  const isInputField = targetEl && (targetEl.tagName === 'INPUT' || targetEl.tagName === 'TEXTAREA');

  if (e.code === 'Space' && !isInputField) {
    e.preventDefault();
    if (gameCode.value === 'FLASHCARD' && !isFlashcardFlipped.value) {
      toggleFlashcardFlip();
    } else if (hasAnsweredCurrent.value) {
      nextQuestion();
    }
    return;
  }

  // Enter to advance when answered
  if (e.key === 'Enter' && hasAnsweredCurrent.value) {
    e.preventDefault();
    nextQuestion();
    return;
  }

  // Number / Letter hotkeys for options (1-4 / A-D)
  if (!hasAnsweredCurrent.value && currentQuestion.value?.options?.length) {
    const key = e.key.toUpperCase();
    const map: Record<string, number> = { '1': 0, 'A': 0, '2': 1, 'B': 1, '3': 2, 'C': 2, '4': 3, 'D': 3 };
    if (key in map) {
      const idx = map[key];
      const opt = currentQuestion.value.options[idx];
      if (opt) {
        if (gameCode.value === 'AIRPLANE_SHOOTER') {
          shootEnemyPlane(opt, idx);
        } else {
          handleSelectOption(opt);
        }
      }
    }
  }

  // Flashcard hotkeys: 1 -> Incorrect, 2 -> Correct
  if (gameCode.value === 'FLASHCARD' && isFlashcardFlipped.value && !hasAnsweredCurrent.value) {
    if (e.key === '1') handleFlashcardRating(false);
    if (e.key === '2') handleFlashcardRating(true);
  }
}

async function nextQuestion() {
  clearAutoAdvance();
  gameAudio.playClick();
  if (currentQuestionIndex.value < totalQuestions.value - 1) {
    currentQuestionIndex.value++;
    initQuestionState();
    nextTick(() => {
      // Focus text input if spelling mode
    });
  } else {
    await finishGame();
  }
}

async function finishGame() {
  stopTimer();
  clearAutoAdvance();
  if (!sessionData.value) return;

  try {
    const res = await gameService.submitAnswers(sessionData.value.sessionId, recordedAnswers.value);
    gameResult.value = res;
    isGameOver.value = true;
    isVictory.value = (res.score || 0) >= 70;

    if (isVictory.value) {
      gameAudio.playVictory();
      triggerConfetti({ particleCount: 120, duration: 3500 });
    } else {
      gameAudio.playGameOver();
    }
  } catch {
    toastStore.error('Không thể lưu kết quả trò chơi');
  }
}
</script>
