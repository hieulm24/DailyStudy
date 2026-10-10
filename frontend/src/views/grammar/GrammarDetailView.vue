<template>
  <div class="space-y-6">
    <!-- Top Back & Header Bar -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-md border border-slate-200 shadow-xs">
      <div class="flex items-start sm:items-center gap-3.5">
        <button
          type="button"
          class="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-md transition-colors shrink-0 cursor-pointer"
          title="Quay lại danh sách ngữ pháp"
          @click="router.push('/grammar')"
        >
          <ArrowLeft class="w-5 h-5" />
        </button>
        <div>
          <div class="flex items-center gap-2.5 flex-wrap">
            <h2 class="text-xl sm:text-2xl font-bold text-slate-900">
              {{ grammar?.topic || 'Đang tải chủ đề...' }}
            </h2>
            <AppBadge v-if="grammar?.level" :level="grammar.level">{{ grammar.level }}</AppBadge>
            <AppBadge :variant="getStatusVariant(grammar?.status)">{{ getStatusLabel(grammar?.status) }}</AppBadge>
          </div>
          <p class="text-xs sm:text-sm text-slate-500 mt-1">
            Trang chi tiết lý thuyết, công thức và phòng luyện tập bài tập AI chuyên sâu
          </p>
        </div>
      </div>

      <div class="flex items-center gap-2 flex-wrap sm:self-center">
        <AppButton
          variant="primary"
          size="md"
          :icon="Sparkles"
          @click="activeTab = 'practice'"
        >
          Luyện tập AI
        </AppButton>
        <AppButton
          variant="secondary"
          size="md"
          :icon="Edit"
          @click="openEditModal"
        >
          Chỉnh sửa
        </AppButton>
      </div>
    </div>

    <!-- Quick Stats Cards -->
    <div class="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-purple-50 text-purple-600 shrink-0">
          <BookOpen class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Mức độ thông thạo</div>
          <div class="text-lg font-bold text-slate-900 flex items-center gap-1">
            <span>{{ grammar?.masteryLevel || 0 }}/5</span>
            <div class="flex items-center">
              <Star
                v-for="s in 5"
                :key="s"
                class="w-3.5 h-3.5"
                :class="s <= (grammar?.masteryLevel || 0) ? 'text-amber-500 fill-amber-500' : 'text-slate-200'"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-indigo-50 text-indigo-600 shrink-0">
          <RotateCcw class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Số lần ôn tập</div>
          <div class="text-lg font-bold text-indigo-600">
            {{ grammar?.reviewCount || 0 }} <span class="text-xs font-normal text-slate-400">lần</span>
          </div>
        </div>
      </div>

      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-emerald-50 text-emerald-600 shrink-0">
          <CheckCircle2 class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Bài tập AI đã làm</div>
          <div class="text-lg font-bold text-emerald-600">
            {{ exerciseHistoryList.length }} <span class="text-xs font-normal text-slate-400">bài</span>
          </div>
        </div>
      </div>

      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-amber-50 text-amber-600 shrink-0">
          <Award class="w-5 h-5" />
        </div>
        <div>
          <div class="text-[11px] font-semibold text-slate-500">Điểm AI cao nhất</div>
          <div class="text-lg font-bold text-amber-600">
            {{ highestScore }}<span class="text-xs font-normal text-slate-400">%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
      <div class="flex items-center border-b border-slate-200 px-4 sm:px-6 gap-2 sm:gap-6 overflow-x-auto">
        <button
          type="button"
          class="py-3.5 px-2 text-sm font-bold border-b-2 transition-all flex items-center gap-2 whitespace-nowrap cursor-pointer"
          :class="activeTab === 'theory' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-500 hover:text-slate-800'"
          @click="activeTab = 'theory'"
        >
          <BookOpen class="w-4 h-4" />
          <span>Lý thuyết & Công thức</span>
        </button>

        <button
          type="button"
          class="py-3.5 px-2 text-sm font-bold border-b-2 transition-all flex items-center gap-2 whitespace-nowrap cursor-pointer"
          :class="activeTab === 'practice' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-500 hover:text-slate-800'"
          @click="activeTab = 'practice'"
        >
          <Sparkles class="w-4 h-4 text-purple-600" />
          <span>Luyện tập bài tập AI</span>
          <span class="text-[10px] bg-purple-100 text-purple-700 font-bold px-1.5 py-0.2 rounded-full">AI Quiz</span>
        </button>

        <button
          type="button"
          class="py-3.5 px-2 text-sm font-bold border-b-2 transition-all flex items-center gap-2 whitespace-nowrap cursor-pointer"
          :class="activeTab === 'history' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-500 hover:text-slate-800'"
          @click="activeTab = 'history'"
        >
          <History class="w-4 h-4" />
          <span>Lịch sử làm bài ({{ exerciseHistoryList.length }})</span>
        </button>
      </div>

      <!-- ============================================================= -->
      <!-- TAB 1: THEORY & FORMULAS (LÝ THUYẾT & CÔNG THỨC)              -->
      <!-- ============================================================= -->
      <div v-if="activeTab === 'theory'" class="p-6 sm:p-8 space-y-6">
        <div v-if="loadingGrammar" class="py-12 text-center text-slate-400">
          Đang tải thông tin ngữ pháp...
        </div>

        <div v-else class="space-y-6">
          <!-- Main Structure Formula Banner -->
          <div v-if="grammar?.structure" class="space-y-2">
            <span class="text-xs font-bold text-slate-600 uppercase tracking-wider block">
              Cấu trúc công thức tổng quát
            </span>
            <div class="p-4 sm:p-5 bg-purple-50/80 rounded-lg border border-purple-200 flex items-center justify-between gap-3">
              <div class="font-mono text-lg sm:text-xl text-purple-900 font-bold">
                {{ grammar.structure }}
              </div>
              <button
                type="button"
                class="text-xs text-purple-700 hover:text-purple-900 hover:bg-purple-100 px-2.5 py-1 rounded transition-colors font-semibold cursor-pointer"
                @click="copyFormula(grammar.structure)"
              >
                Sao chép
              </button>
            </div>
          </div>

          <!-- 3 Variations: (+), (-), (?) -->
          <div
            v-if="grammar?.positiveStructure || grammar?.negativeStructure || grammar?.questionStructure"
            class="space-y-2"
          >
            <span class="text-xs font-bold text-slate-600 uppercase tracking-wider block">
              3 Dạng cấu trúc cơ bản
            </span>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <!-- Positive (+) -->
              <div v-if="grammar?.positiveStructure" class="p-4 bg-emerald-50/60 rounded-lg border border-emerald-200/80 space-y-1.5">
                <div class="flex items-center gap-1.5 text-xs font-bold text-emerald-800 uppercase tracking-wider">
                  <span class="w-4 h-4 rounded-full bg-emerald-600 text-white flex items-center justify-center text-[10px] font-bold">+</span>
                  <span>Thể Khẳng định (+)</span>
                </div>
                <div class="font-mono text-sm sm:text-base text-slate-900 font-bold pt-1">
                  {{ grammar.positiveStructure }}
                </div>
              </div>

              <!-- Negative (-) -->
              <div v-if="grammar?.negativeStructure" class="p-4 bg-rose-50/60 rounded-lg border border-rose-200/80 space-y-1.5">
                <div class="flex items-center gap-1.5 text-xs font-bold text-rose-800 uppercase tracking-wider">
                  <span class="w-4 h-4 rounded-full bg-rose-600 text-white flex items-center justify-center text-[10px] font-bold">-</span>
                  <span>Thể Phủ định (-)</span>
                </div>
                <div class="font-mono text-sm sm:text-base text-slate-900 font-bold pt-1">
                  {{ grammar.negativeStructure }}
                </div>
              </div>

              <!-- Question (?) -->
              <div v-if="grammar?.questionStructure" class="p-4 bg-indigo-50/60 rounded-lg border border-indigo-200/80 space-y-1.5">
                <div class="flex items-center gap-1.5 text-xs font-bold text-indigo-800 uppercase tracking-wider">
                  <span class="w-4 h-4 rounded-full bg-indigo-600 text-white flex items-center justify-center text-[10px] font-bold">?</span>
                  <span>Thể Nghi vấn (?)</span>
                </div>
                <div class="font-mono text-sm sm:text-base text-slate-900 font-bold pt-1">
                  {{ grammar.questionStructure }}
                </div>
              </div>
            </div>
          </div>

          <!-- Usage Section -->
          <div v-if="grammar?.usage" class="space-y-2">
            <span class="text-xs font-bold text-slate-600 uppercase tracking-wider block">
              Cách dùng & Ngữ cảnh chuyên sâu
            </span>
            <div class="p-5 bg-slate-50/80 rounded-lg border border-slate-200 text-slate-800 leading-relaxed text-sm sm:text-base whitespace-pre-line">
              {{ grammar.usage }}
            </div>
          </div>

          <!-- Signal Words & Common Mistakes -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- Signal Words -->
            <div v-if="grammar?.signalWords" class="p-5 bg-amber-50/60 rounded-lg border border-amber-200/80 space-y-2">
              <span class="text-xs font-bold text-amber-900 uppercase tracking-wider flex items-center gap-1.5">
                <Flame class="w-4 h-4 text-amber-600" />
                <span>Dấu hiệu nhận biết (Signal Words)</span>
              </span>
              <p class="text-sm font-semibold text-slate-800 leading-relaxed">
                {{ grammar.signalWords }}
              </p>
            </div>

            <!-- Common Mistakes -->
            <div v-if="grammar?.commonMistakes" class="p-5 bg-rose-50/60 rounded-lg border border-rose-200/80 space-y-2">
              <span class="text-xs font-bold text-rose-900 uppercase tracking-wider flex items-center gap-1.5">
                <AlertCircle class="w-4 h-4 text-rose-600" />
                <span>Lỗi thường gặp & Bẫy TOEIC (Common Pitfalls)</span>
              </span>
              <p class="text-sm text-slate-800 leading-relaxed">
                {{ grammar.commonMistakes }}
              </p>
            </div>
          </div>

          <!-- Real Examples -->
          <div v-if="grammar?.examples && grammar.examples.length > 0" class="space-y-2">
            <span class="text-xs font-bold text-slate-600 uppercase tracking-wider block">
              Câu ví dụ thực tế
            </span>
            <div class="space-y-2.5">
              <div
                v-for="(ex, exIdx) in grammar.examples"
                :key="exIdx"
                class="p-4 bg-brand-50/40 rounded-lg border border-brand-100 space-y-1"
              >
                <div class="text-sm font-bold text-slate-900">
                  "{{ ex.exampleSentence }}"
                </div>
                <div v-if="ex.meaning" class="text-xs sm:text-sm text-slate-600">
                  {{ ex.meaning }}
                </div>
              </div>
            </div>
          </div>

          <!-- Personal Notes -->
          <div v-if="grammar?.note" class="p-4 bg-slate-50 rounded-lg border border-slate-200 space-y-1.5">
            <span class="text-xs font-bold text-slate-600 uppercase tracking-wider block">
              Ghi chú cá nhân
            </span>
            <p class="text-sm text-slate-700 whitespace-pre-line">{{ grammar.note }}</p>
          </div>

          <!-- Practice Call to Action in Theory tab -->
          <div class="p-5 bg-gradient-to-r from-purple-50 via-indigo-50 to-brand-50 rounded-lg border border-purple-200 flex flex-col sm:flex-row items-center justify-between gap-4">
            <div class="space-y-1 text-center sm:text-left">
              <h4 class="text-base font-bold text-purple-950">Đã nắm chắc lý thuyết chủ đề này?</h4>
              <p class="text-xs text-purple-700">Hãy thử sức với các dạng bài tập AI TOEIC: Trắc nghiệm, Điền từ chia động từ và Sửa lỗi sai.</p>
            </div>
            <AppButton
              variant="primary"
              size="md"
              :icon="Sparkles"
              @click="activeTab = 'practice'"
            >
              Làm bài tập ngay
            </AppButton>
          </div>
        </div>
      </div>

      <!-- ============================================================= -->
      <!-- TAB 2: AI PRACTICE QUIZ ENGINE                                -->
      <!-- ============================================================= -->
      <div v-if="activeTab === 'practice'" class="p-6 sm:p-8 space-y-6">
        <!-- MODE A: GENERATOR & SETUP SCREEN (FULL WIDTH & SPATIOUS) -->
        <div v-if="quizState === 'SETUP'" class="w-full space-y-6">
          <!-- Banner Hero -->
          <div class="p-6 bg-gradient-to-r from-purple-50/90 via-indigo-50/70 to-slate-50 rounded-xl border border-purple-200/80 flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div class="space-y-1.5">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="p-2 rounded-lg bg-purple-100 text-purple-700 font-bold inline-flex items-center gap-1.5 text-xs">
                  <Sparkles class="w-4 h-4 text-purple-600" />
                  AI Practice Engine
                </span>
                <span class="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-indigo-100 text-indigo-800">
                  Chuẩn đề thi TOEIC Part 5 & 6
                </span>
              </div>
              <h3 class="text-xl sm:text-2xl font-bold text-slate-900">
                Luyện tập ngữ pháp chuyên sâu: {{ grammar?.topic }}
              </h3>
              <p class="text-xs sm:text-sm text-slate-600 max-w-2xl leading-relaxed">
                Hệ thống AI tự động phân tích cấu trúc, dấu hiệu nhận biết và các bẫy thường gặp của chủ đề này để biên soạn bộ câu hỏi thực tế.
              </p>
            </div>

            <div class="flex items-center gap-2.5 self-start md:self-center shrink-0">
              <AppButton
                variant="secondary"
                size="md"
                :icon="BookOpen"
                @click="activeTab = 'theory'"
              >
                Xem lại Lý thuyết
              </AppButton>
              <AppButton
                variant="primary"
                size="md"
                :loading="generatingQuiz"
                :icon="Sparkles"
                @click="handleGenerateQuiz"
              >
                {{ generatingQuiz ? 'AI đang tạo đề...' : 'Bắt đầu làm bài' }}
              </AppButton>
            </div>
          </div>

          <!-- Exercise Type Selector (4 Wide Responsive Cards with Lucide Icons) -->
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <label class="text-xs font-bold text-slate-700 uppercase tracking-wider block">
                Chọn dạng bài tập muốn rèn luyện:
              </label>
              <span class="text-xs text-slate-400">Chọn 1 trong 4 chế độ</span>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <!-- 1. MIXED -->
              <div
                class="p-4 rounded-xl border-2 transition-all cursor-pointer flex flex-col justify-between gap-3 group relative overflow-hidden"
                :class="
                  quizConfig.exerciseType === 'MIXED'
                    ? 'border-purple-600 bg-purple-50/50 text-purple-950 shadow-sm ring-2 ring-purple-200'
                    : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50/80 text-slate-800'
                "
                @click="quizConfig.exerciseType = 'MIXED'"
              >
                <div class="flex items-start justify-between">
                  <div class="p-2.5 rounded-lg bg-purple-100 text-purple-700 shrink-0">
                    <Layers class="w-5 h-5" />
                  </div>
                  <div
                    class="w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors"
                    :class="quizConfig.exerciseType === 'MIXED' ? 'border-purple-600 bg-purple-600 text-white' : 'border-slate-300 bg-white'"
                  >
                    <Check v-if="quizConfig.exerciseType === 'MIXED'" class="w-3 h-3" />
                  </div>
                </div>
                <div>
                  <div class="text-sm font-bold text-slate-900">Tổng hợp các dạng</div>
                  <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                    Kết hợp linh hoạt cả trắc nghiệm, điền từ chia động từ và tìm lỗi sai.
                  </p>
                </div>
              </div>

              <!-- 2. MULTIPLE_CHOICE -->
              <div
                class="p-4 rounded-xl border-2 transition-all cursor-pointer flex flex-col justify-between gap-3 group relative overflow-hidden"
                :class="
                  quizConfig.exerciseType === 'MULTIPLE_CHOICE'
                    ? 'border-blue-600 bg-blue-50/50 text-blue-950 shadow-sm ring-2 ring-blue-200'
                    : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50/80 text-slate-800'
                "
                @click="quizConfig.exerciseType = 'MULTIPLE_CHOICE'"
              >
                <div class="flex items-start justify-between">
                  <div class="p-2.5 rounded-lg bg-blue-100 text-blue-700 shrink-0">
                    <ListChecks class="w-5 h-5" />
                  </div>
                  <div
                    class="w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors"
                    :class="quizConfig.exerciseType === 'MULTIPLE_CHOICE' ? 'border-blue-600 bg-blue-600 text-white' : 'border-slate-300 bg-white'"
                  >
                    <Check v-if="quizConfig.exerciseType === 'MULTIPLE_CHOICE'" class="w-3 h-3" />
                  </div>
                </div>
                <div>
                  <div class="text-sm font-bold text-slate-900">Trắc nghiệm 4 đáp án</div>
                  <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                    Format câu hỏi hoàn thành câu chuẩn đề thi TOEIC Part 5 & 6 (A, B, C, D).
                  </p>
                </div>
              </div>

              <!-- 3. FILL_IN_BLANK -->
              <div
                class="p-4 rounded-xl border-2 transition-all cursor-pointer flex flex-col justify-between gap-3 group relative overflow-hidden"
                :class="
                  quizConfig.exerciseType === 'FILL_IN_BLANK'
                    ? 'border-emerald-600 bg-emerald-50/50 text-emerald-950 shadow-sm ring-2 ring-emerald-200'
                    : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50/80 text-slate-800'
                "
                @click="quizConfig.exerciseType = 'FILL_IN_BLANK'"
              >
                <div class="flex items-start justify-between">
                  <div class="p-2.5 rounded-lg bg-emerald-100 text-emerald-700 shrink-0">
                    <PenTool class="w-5 h-5" />
                  </div>
                  <div
                    class="w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors"
                    :class="quizConfig.exerciseType === 'FILL_IN_BLANK' ? 'border-emerald-600 bg-emerald-600 text-white' : 'border-slate-300 bg-white'"
                  >
                    <Check v-if="quizConfig.exerciseType === 'FILL_IN_BLANK'" class="w-3 h-3" />
                  </div>
                </div>
                <div>
                  <div class="text-sm font-bold text-slate-900">Điền từ / Chia động từ</div>
                  <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                    Rèn luyện kỹ năng chia dạng đúng của từ gốc trong ngoặc đơn.
                  </p>
                </div>
              </div>

              <!-- 4. ERROR_CORRECTION -->
              <div
                class="p-4 rounded-xl border-2 transition-all cursor-pointer flex flex-col justify-between gap-3 group relative overflow-hidden"
                :class="
                  quizConfig.exerciseType === 'ERROR_CORRECTION'
                    ? 'border-rose-600 bg-rose-50/50 text-rose-950 shadow-sm ring-2 ring-rose-200'
                    : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50/80 text-slate-800'
                "
                @click="quizConfig.exerciseType = 'ERROR_CORRECTION'"
              >
                <div class="flex items-start justify-between">
                  <div class="p-2.5 rounded-lg bg-rose-100 text-rose-700 shrink-0">
                    <Search class="w-5 h-5" />
                  </div>
                  <div
                    class="w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors"
                    :class="quizConfig.exerciseType === 'ERROR_CORRECTION' ? 'border-rose-600 bg-rose-600 text-white' : 'border-slate-300 bg-white'"
                  >
                    <Check v-if="quizConfig.exerciseType === 'ERROR_CORRECTION'" class="w-3 h-3" />
                  </div>
                </div>
                <div>
                  <div class="text-sm font-bold text-slate-900">Tìm & Sửa lỗi sai</div>
                  <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                    Phát hiện vị trí sai ngữ pháp trong câu và hiển thị cách sửa chuẩn xác.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Configuration Form Grid -->
          <div class="p-6 bg-white rounded-xl border border-slate-200 shadow-xs space-y-4">
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label class="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Số lượng câu hỏi
                </label>
                <select
                  v-model="quizConfig.numberOfQuestions"
                  class="w-full text-sm bg-white border border-slate-300 rounded-md px-3.5 py-2.5 shadow-xs focus:outline-none focus:border-brand-500 font-medium"
                >
                  <option :value="5">5 câu (Nhanh - ~3 phút)</option>
                  <option :value="10">10 câu (Tiêu chuẩn - ~7 phút)</option>
                </select>
              </div>

              <div>
                <label class="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Cấp độ mục tiêu
                </label>
                <select
                  v-model="quizConfig.level"
                  class="w-full text-sm bg-white border border-slate-300 rounded-md px-3.5 py-2.5 shadow-xs focus:outline-none focus:border-brand-500 font-medium"
                >
                  <option value="A0">A0 - Mất gốc (Mới học lại, từ siêu dễ, câu ngắn 5-8 từ)</option>
                  <option value="A1">A1 - Nhập môn (Cơ bản đời thường, câu ngắn dễ hiểu)</option>
                  <option value="A2">A2 - Sơ cấp (TOEIC 350 - 450)</option>
                  <option value="B1">B1 - Trung cấp (TOEIC 500 - 650)</option>
                  <option value="B2">B2 - Nâng cao (TOEIC 700 - 850)</option>
                  <option value="C1">C1 - Thành thạo (TOEIC 900+)</option>
                </select>
              </div>

              <div>
                <label class="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Yêu cầu trọng tâm (Tùy chọn)
                </label>
                <input
                  v-model="quizConfig.customFocus"
                  type="text"
                  class="w-full text-sm bg-white border border-slate-300 rounded-md px-3.5 py-2.5 shadow-xs focus:outline-none focus:border-brand-500"
                  placeholder="e.g. Bẫy chia thì, hoặc phân biệt với quá khứ đơn..."
                />
              </div>
            </div>
          </div>

          <!-- Bottom Action Buttons -->
          <div class="flex items-center justify-end gap-3 pt-2">
            <AppButton
              variant="secondary"
              size="lg"
              :icon="BookOpen"
              @click="activeTab = 'theory'"
            >
              Quay lại Lý thuyết
            </AppButton>

            <AppButton
              variant="primary"
              size="lg"
              class="px-8"
              :loading="generatingQuiz"
              :icon="Sparkles"
              @click="handleGenerateQuiz"
            >
              {{ generatingQuiz ? 'AI đang tạo đề thi...' : 'Bắt đầu làm bài tập ngay' }}
            </AppButton>
          </div>
        </div>

        <!-- MODE B: ACTIVE QUIZ IN PROGRESS (FULL WIDTH & SPATIOUS) -->
        <div v-else-if="quizState === 'ACTIVE'" class="w-full space-y-6">
          <!-- Quiz Top Header & Progress -->
          <div class="flex items-center justify-between bg-slate-50 p-4 sm:p-5 rounded-xl border border-slate-200 flex-wrap gap-3">
            <div class="flex items-center gap-3">
              <span class="text-xs font-bold text-slate-500 uppercase tracking-wider">Tiến độ bài làm:</span>
              <span class="text-base font-extrabold text-brand-700">
                Câu {{ currentQuestionIndex + 1 }} / {{ activeQuestions.length }}
              </span>
            </div>

            <div class="flex items-center gap-3">
              <div class="flex items-center gap-1.5 text-xs font-mono font-bold text-slate-700 bg-white px-3 py-1.5 rounded-md border border-slate-200 shadow-xs">
                <Clock class="w-4 h-4 text-purple-600" />
                <span>{{ formatSeconds(timerSeconds) }}</span>
              </div>

              <button
                type="button"
                class="text-xs font-semibold text-slate-500 hover:text-rose-600 px-2.5 py-1.5 rounded-md hover:bg-rose-50 transition-colors cursor-pointer"
                title="Dừng làm bài và quay lại"
                @click="cancelQuiz"
              >
                Hủy bài
              </button>
            </div>
          </div>

          <!-- Progress Bar -->
          <div class="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden">
            <div
              class="bg-brand-600 h-full transition-all duration-300"
              :style="{ width: `${((currentQuestionIndex + 1) / activeQuestions.length) * 100}%` }"
            ></div>
          </div>

          <!-- Main Question Workspace Card (Full Width) -->
          <div v-if="currentQuestion" class="bg-white p-6 sm:p-8 rounded-xl border border-slate-200 shadow-xs space-y-6">
            <!-- Question Meta Banner -->
            <div class="space-y-3 pb-4 border-b border-slate-100">
              <div class="flex items-center justify-between flex-wrap gap-2">
                <div class="flex items-center gap-2.5">
                  <span class="text-xs font-extrabold uppercase tracking-wider text-purple-700 bg-purple-50 px-3 py-1 rounded-md border border-purple-200">
                    Câu hỏi #{{ currentQuestionIndex + 1 }}
                  </span>

                  <!-- Lucide Icon Question Type Badge -->
                  <div class="inline-flex items-center gap-1.5 text-xs font-bold px-2.5 py-1 rounded-md" :class="getQuestionTypeBadgeClass(currentQuestion.questionType)">
                    <component :is="getQuestionTypeIcon(currentQuestion.questionType)" class="w-3.5 h-3.5" />
                    <span>{{ getQuestionTypeLabel(currentQuestion.questionType) }}</span>
                  </div>
                </div>

                <span class="text-xs text-slate-400 font-medium">TOEIC Practice Mode</span>
              </div>

              <!-- Prompt Instruction -->
              <div v-if="currentQuestion.prompt" class="p-3 bg-slate-50 rounded-lg border border-slate-200 text-xs font-semibold text-slate-700 flex items-center gap-2">
                <HelpCircle class="w-4 h-4 text-purple-600 shrink-0" />
                <span><strong>Yêu cầu:</strong> {{ currentQuestion.prompt }}</span>
              </div>

              <!-- Question Sentence -->
              <p class="text-lg sm:text-2xl font-bold text-slate-900 leading-relaxed pt-2">
                {{ currentQuestion.question }}
              </p>
            </div>

            <!-- ========================================================= -->
            <!-- QUESTION TYPE 1: FILL_IN_BLANK                           -->
            <!-- ========================================================= -->
            <div v-if="currentQuestion.questionType === 'FILL_IN_BLANK'" class="space-y-4 pt-1">
              <div v-if="currentQuestion.baseWord" class="flex items-center gap-2 text-xs font-semibold text-purple-950 bg-purple-50 p-3 rounded-lg border border-purple-200">
                <Tag class="w-4 h-4 text-purple-600 shrink-0" />
                <span>Động từ / Từ gốc cần chia:</span>
                <span class="font-mono font-bold bg-white px-2.5 py-1 rounded border border-purple-300 text-purple-800 text-sm">
                  {{ currentQuestion.baseWord }}
                </span>
              </div>

              <div class="space-y-2">
                <label class="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Nhập câu trả lời của bạn:
                </label>
                <input
                  v-model="userAnswers[currentQuestion.id]"
                  type="text"
                  class="w-full text-lg font-semibold text-slate-900 bg-brand-50/20 border-2 border-brand-400 rounded-lg px-4 py-3.5 shadow-xs focus:outline-none focus:border-brand-600 focus:bg-white focus:ring-4 focus:ring-brand-100 transition-all placeholder:text-slate-400 placeholder:font-normal"
                  :placeholder="`Nhập từ / dạng chia đúng (VD: ${currentQuestion.baseWord || 'câu trả lời'})...`"
                  @keydown.enter.prevent="handleNextOrSubmit"
                />
                <p class="text-xs text-slate-400 italic">
                  Gợi ý: Nhập câu trả lời rồi ấn <strong>Enter</strong> hoặc nút <strong>'Câu tiếp theo'</strong> để chuyển câu.
                </p>
              </div>

              <!-- Suggestion word pills if options provided -->
              <div v-if="currentQuestion.options && currentQuestion.options.length > 0" class="pt-2">
                <span class="text-xs font-semibold text-slate-500 block mb-2">Hoặc chọn nhanh từ các phương án gợi ý:</span>
                <div class="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                  <button
                    v-for="(opt, optIdx) in currentQuestion.options"
                    :key="optIdx"
                    type="button"
                    class="text-sm px-4 py-2.5 rounded-lg border-2 transition-all cursor-pointer text-left font-medium"
                    :class="
                      userAnswers[currentQuestion.id] === opt
                        ? 'border-brand-600 bg-brand-600 text-white font-bold shadow-xs'
                        : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50 text-slate-800'
                    "
                    @click="selectAnswer(currentQuestion.id, opt)"
                  >
                    {{ opt }}
                  </button>
                </div>
              </div>
            </div>

            <!-- ========================================================= -->
            <!-- QUESTION TYPE 2: ERROR_CORRECTION                        -->
            <!-- ========================================================= -->
            <div v-else-if="currentQuestion.questionType === 'ERROR_CORRECTION'" class="space-y-3 pt-1">
              <span class="text-xs font-bold text-slate-600 uppercase tracking-wider block">
                Chọn phần gạch chân bị sai ngữ pháp:
              </span>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                <div
                  v-for="(opt, optIdx) in currentQuestion.options"
                  :key="optIdx"
                  class="p-4 rounded-xl border-2 transition-all cursor-pointer flex items-center gap-3.5 group"
                  :class="
                    isUserSelectedOption(currentQuestion, opt)
                      ? 'border-rose-500 bg-rose-50/60 text-rose-950 shadow-xs ring-2 ring-rose-200'
                      : 'border-slate-200 hover:border-slate-300 hover:bg-slate-50/60 text-slate-800 bg-white'
                  "
                  @click="selectAnswer(currentQuestion.id, opt)"
                >
                  <span
                    class="w-7 h-7 rounded-full flex items-center justify-center font-bold text-xs shrink-0 transition-colors"
                    :class="
                      isUserSelectedOption(currentQuestion, opt)
                        ? 'bg-rose-600 text-white'
                        : 'bg-slate-100 text-slate-600 group-hover:bg-slate-200'
                    "
                  >
                    {{ getOptionLetter(optIdx) }}
                  </span>
                  <span class="text-base font-semibold">{{ opt }}</span>
                  <span v-if="isUserSelectedOption(currentQuestion, opt)" class="ml-auto text-xs font-bold text-rose-600 bg-rose-100 px-2 py-0.5 rounded">
                    Đã chọn
                  </span>
                </div>
              </div>
            </div>

            <!-- ========================================================= -->
            <!-- QUESTION TYPE 3: MULTIPLE_CHOICE                         -->
            <!-- ========================================================= -->
            <div v-else class="space-y-3 pt-1">
              <span class="text-xs font-bold text-slate-600 uppercase tracking-wider block">
                Chọn phương án chính xác nhất:
              </span>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3.5">
                <div
                  v-for="(opt, optIdx) in currentQuestion.options"
                  :key="optIdx"
                  class="p-4 rounded-xl border-2 transition-all cursor-pointer flex items-center gap-3.5 group"
                  :class="
                    userAnswers[currentQuestion.id] === opt
                      ? 'border-brand-600 bg-brand-50/40 text-brand-900 shadow-xs ring-2 ring-brand-200'
                      : 'border-slate-200 hover:border-slate-300 hover:bg-slate-50/60 text-slate-800 bg-white'
                  "
                  @click="selectAnswer(currentQuestion.id, opt)"
                >
                  <span
                    class="w-7 h-7 rounded-full flex items-center justify-center font-bold text-xs shrink-0 transition-colors"
                    :class="
                      userAnswers[currentQuestion.id] === opt
                        ? 'bg-brand-600 text-white'
                        : 'bg-slate-100 text-slate-600 group-hover:bg-slate-200'
                    "
                  >
                    {{ getOptionLetter(optIdx) }}
                  </span>
                  <span class="text-base font-semibold">{{ opt }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Bottom Navigation Controls -->
          <div class="flex items-center justify-between pt-2">
            <AppButton
              variant="secondary"
              size="md"
              :disabled="currentQuestionIndex === 0"
              @click="currentQuestionIndex--"
            >
              ← Câu trước
            </AppButton>

            <div class="flex items-center gap-2">
              <AppButton
                v-if="currentQuestionIndex < activeQuestions.length - 1"
                variant="primary"
                size="md"
                @click="currentQuestionIndex++"
              >
                Câu tiếp theo →
              </AppButton>

              <AppButton
                v-else
                variant="primary"
                size="md"
                :loading="submittingQuiz"
                :icon="CheckCircle2"
                @click="handleSubmitQuiz"
              >
                Nộp bài & Chấm điểm
              </AppButton>
            </div>
          </div>
        </div>

        <!-- MODE C: QUIZ RESULT & DEEP AI ANALYSIS (FULL WIDTH & SPATIOUS) -->
        <div v-else-if="quizState === 'RESULT'" class="w-full space-y-6">
          <!-- Score Summary Banner -->
          <div class="p-6 sm:p-8 rounded-xl border text-center space-y-4" :class="getScoreBannerClass(lastResult?.score || 0)">
            <div class="inline-flex p-3 rounded-full bg-white shadow-xs">
              <Award class="w-8 h-8" :class="getScoreIconClass(lastResult?.score || 0)" />
            </div>
            <div>
              <h3 class="text-2xl sm:text-3xl font-extrabold">
                {{ lastResult?.score }}% - {{ getScoreTitle(lastResult?.score || 0) }}
              </h3>
              <p class="text-sm font-medium opacity-90 mt-1">
                Đúng <strong>{{ lastResult?.correctCount }}</strong> / {{ lastResult?.totalQuestions }} câu hỏi • Thời gian làm bài: {{ formatSeconds(lastResult?.timeSpentSeconds || 0) }}
              </p>
            </div>

            <!-- Top Action Navigation Buttons -->
            <div class="pt-3 flex items-center justify-center gap-2.5 flex-wrap">
              <AppButton
                variant="secondary"
                size="md"
                :icon="BookOpen"
                @click="activeTab = 'theory'"
              >
                Quay lại Lý thuyết
              </AppButton>

              <AppButton
                variant="secondary"
                size="md"
                :icon="ArrowLeft"
                @click="router.push('/grammar')"
              >
                Danh sách chủ đề
              </AppButton>

              <AppButton
                variant="primary"
                size="md"
                :icon="Sparkles"
                @click="quizState = 'SETUP'"
              >
                Làm bài tập mới
              </AppButton>

              <AppButton
                variant="secondary"
                size="md"
                :icon="History"
                @click="activeTab = 'history'"
              >
                Xem lịch sử
              </AppButton>
            </div>
          </div>

          <!-- Detailed Question by Question Explanations -->
          <div class="space-y-4">
            <h4 class="text-base font-bold text-slate-900 flex items-center justify-between">
              <span>Phân tích chi tiết từng câu từ AI</span>
              <span class="text-xs text-slate-500 font-normal">
                {{ lastResult?.correctCount }}/{{ lastResult?.totalQuestions }} câu chính xác
              </span>
            </h4>

            <div
              v-for="(q, qIdx) in lastResult?.questions"
              :key="q.id"
              class="p-6 bg-white rounded-xl border border-slate-200 shadow-xs space-y-4"
            >
              <!-- Question Header & Status -->
              <div class="space-y-2">
                <div class="flex items-center justify-between flex-wrap gap-2">
                  <div class="flex items-center gap-2">
                    <span
                      class="text-xs font-bold px-2.5 py-1 rounded"
                      :class="isQuestionCorrect(q) ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'"
                    >
                      Câu #{{ qIdx + 1 }}: {{ isQuestionCorrect(q) ? 'Chính xác ✓' : 'Chưa đúng ✗' }}
                    </span>
                    <div class="inline-flex items-center gap-1.5 text-xs font-bold px-2.5 py-0.5 rounded" :class="getQuestionTypeBadgeClass(q.questionType)">
                      <component :is="getQuestionTypeIcon(q.questionType)" class="w-3.5 h-3.5" />
                      <span>{{ getQuestionTypeLabel(q.questionType) }}</span>
                    </div>
                  </div>

                  <span v-if="q.baseWord" class="text-xs text-purple-700 bg-purple-50 px-2 py-0.5 rounded border border-purple-200">
                    Từ gốc: <strong>{{ q.baseWord }}</strong>
                  </span>
                </div>

                <p class="text-base sm:text-lg font-bold text-slate-900 pt-1">
                  {{ q.question }}
                </p>
                <p v-if="q.translation" class="text-xs sm:text-sm text-slate-600 italic">
                  Dịch nghĩa: {{ q.translation }}
                </p>
              </div>

              <!-- REVIEW DISPLAY BASED ON QUESTION TYPE -->
              <!-- 1. Fill In The Blank Review -->
              <div v-if="q.questionType === 'FILL_IN_BLANK'" class="p-4 rounded-lg border bg-slate-50/80 space-y-3 text-sm">
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div
                    class="p-3.5 rounded-lg border"
                    :class="isQuestionCorrect(q) ? 'bg-emerald-50/80 border-emerald-300 text-emerald-950' : 'bg-rose-50/80 border-rose-300 text-rose-950'"
                  >
                    <div class="text-xs font-bold uppercase tracking-wider mb-1 flex items-center gap-1">
                      <span v-if="isQuestionCorrect(q)" class="text-emerald-800">✓ Bạn đã điền chính xác:</span>
                      <span v-else class="text-rose-800">✗ Bạn đã điền:</span>
                    </div>
                    <div class="text-base font-bold font-mono">
                      {{ lastResult?.userAnswers[q.id] || '(Bỏ trống không điền)' }}
                    </div>
                  </div>

                  <div class="p-3.5 rounded-lg border bg-emerald-50/80 border-emerald-300 text-emerald-950">
                    <div class="text-xs font-bold uppercase tracking-wider text-emerald-800 mb-1">
                      ✓ Đáp án đúng chuẩn xác:
                    </div>
                    <div class="text-base font-bold font-mono text-emerald-900">
                      {{ q.correctAnswer }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 2. Error Correction Review -->
              <div v-else-if="q.questionType === 'ERROR_CORRECTION'" class="space-y-3">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                  <div
                    v-for="(opt, oIdx) in q.options"
                    :key="oIdx"
                    class="p-3 rounded-lg border flex items-center justify-between"
                    :class="getErrorCorrectionOptionClass(q, opt)"
                  >
                    <span class="font-medium">
                      {{ opt }}
                    </span>
                    <span v-if="isOptionCorrectAnswer(q, opt)" class="text-xs font-bold text-emerald-700">✓ Vị trí lỗi sai</span>
                    <span v-else-if="isUserSelectedOption(q, opt)" class="text-xs font-bold text-rose-700">✗ Bạn đã chọn</span>
                  </div>
                </div>

                <!-- Corrected Form Highlight -->
                <div v-if="q.correctedWord" class="p-3.5 bg-emerald-50 rounded-lg border border-emerald-300 text-sm flex items-start gap-2.5">
                  <CheckCircle2 class="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                  <div>
                    <span class="text-xs font-bold text-emerald-900 uppercase tracking-wider block">Sửa lại đúng ngữ pháp:</span>
                    <span class="text-sm font-bold text-emerald-950 font-mono">{{ q.correctedWord }}</span>
                  </div>
                </div>
              </div>

              <!-- 3. Multiple Choice Review -->
              <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                <div
                  v-for="(opt, oIdx) in q.options"
                  :key="oIdx"
                  class="p-3 rounded-lg border flex items-center justify-between"
                  :class="getOptionResultClass(q, opt)"
                >
                  <span class="font-medium">
                    {{ getOptionLetter(oIdx) }}. {{ opt }}
                  </span>
                  <span v-if="opt === q.correctAnswer" class="text-xs font-bold text-emerald-700">✓ Đáp án đúng</span>
                  <span v-else-if="lastResult?.userAnswers[q.id] === opt" class="text-xs font-bold text-rose-700">✗ Bạn đã chọn</span>
                </div>
              </div>

              <!-- AI Deep Explanation -->
              <div class="p-4 bg-purple-50/60 rounded-lg border border-purple-200 space-y-2 text-sm">
                <div class="text-xs font-bold text-purple-900 uppercase tracking-wider flex items-center gap-1.5">
                  <Sparkles class="w-3.5 h-3.5 text-purple-600" />
                  <span>Giải thích ngữ pháp chuyên sâu của AI:</span>
                </div>
                <p class="text-slate-800 leading-relaxed">{{ q.explanation }}</p>

                <div v-if="q.grammarTip" class="pt-2 border-t border-purple-200/60 flex items-start gap-1.5 text-xs text-purple-950 font-medium">
                  <Lightbulb class="w-3.5 h-3.5 text-amber-500 shrink-0 mt-0.5" />
                  <span><strong>Mẹo thi TOEIC:</strong> {{ q.grammarTip }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Bottom Action Navigation Bar -->
          <div class="p-5 bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-3">
            <div class="flex items-center gap-2 flex-wrap w-full sm:w-auto">
              <AppButton
                variant="secondary"
                size="md"
                :icon="BookOpen"
                @click="activeTab = 'theory'"
              >
                Quay lại Lý thuyết
              </AppButton>
              <AppButton
                variant="secondary"
                size="md"
                :icon="ArrowLeft"
                @click="router.push('/grammar')"
              >
                Danh sách chủ đề
              </AppButton>
            </div>

            <div class="flex items-center gap-2 flex-wrap w-full sm:w-auto justify-end">
              <AppButton
                variant="secondary"
                size="md"
                :icon="History"
                @click="activeTab = 'history'"
              >
                Xem lịch sử
              </AppButton>
              <AppButton
                variant="primary"
                size="md"
                :icon="Sparkles"
                @click="quizState = 'SETUP'"
              >
                Làm bài tập mới
              </AppButton>
            </div>
          </div>
        </div>
      </div>

      <!-- ============================================================= -->
      <!-- TAB 3: EXERCISE HISTORY LIST (LỊCH SỬ BÀI TẬP ĐÃ LÀM)         -->
      <!-- ============================================================= -->
      <div v-if="activeTab === 'history'" class="p-6 sm:p-8 space-y-4">
        <div v-if="exerciseHistoryList.length === 0" class="text-center py-12 space-y-3">
          <History class="w-10 h-10 text-slate-300 mx-auto" />
          <h4 class="text-base font-bold text-slate-800">Chưa có bài tập nào được ghi nhận</h4>
          <p class="text-xs text-slate-500">Hãy chuyển sang tab Luyện tập AI để làm bài tập đầu tiên nhé.</p>
          <AppButton variant="primary" size="sm" :icon="Sparkles" @click="activeTab = 'practice'">
            Làm bài tập ngay
          </AppButton>
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="h in exerciseHistoryList"
            :key="h.id"
            class="p-4 bg-white hover:bg-slate-50/60 rounded-xl border border-slate-200 transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-3 cursor-pointer"
            @click="viewHistoryItem(h)"
          >
            <div class="space-y-1">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-sm font-bold text-slate-900">{{ h.title }}</span>
                <span class="text-xs font-bold px-2 py-0.5 rounded" :class="h.score >= 80 ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'">
                  {{ h.score }}% ({{ h.correctCount }}/{{ h.totalQuestions }} câu)
                </span>
                <span class="inline-flex items-center gap-1 text-xs font-semibold px-2 py-0.5 rounded bg-purple-50 text-purple-700 border border-purple-200">
                  <component :is="getQuestionTypeIcon(h.exerciseType)" class="w-3 h-3" />
                  <span>{{ getQuestionTypeLabel(h.exerciseType) }}</span>
                </span>
              </div>
              <div class="text-xs text-slate-400">
                Làm lúc: {{ formatDate(h.completedAt) }} • Thời gian: {{ formatSeconds(h.timeSpentSeconds) }}
              </div>
            </div>

            <div class="flex items-center gap-2 self-end sm:self-center">
              <AppButton variant="secondary" size="sm" :icon="Eye" @click.stop="viewHistoryItem(h)">
                Xem lại bài làm
              </AppButton>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Grammar Modal -->
    <AppModal
      v-model="showEditModal"
      title="Chỉnh sửa chủ đề ngữ pháp"
      size="lg"
    >
      <form class="space-y-4" @submit.prevent="saveGrammarEdit">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            v-model="editForm.topic"
            label="Chủ đề ngữ pháp (Topic)"
            required
          />
          <AppSelect
            v-model="editForm.level"
            label="Cấp độ (Level)"
            :options="levelOptions"
          />
        </div>

        <AppInput
          v-model="editForm.structure"
          label="Cấu trúc tổng quát (Structure)"
          placeholder="e.g. S + have/has + V3/ed"
        />

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 p-3 bg-slate-50 rounded-md border border-slate-200">
          <AppInput
            v-model="editForm.positiveStructure"
            label="Khẳng định (+)"
            placeholder="S + have/has + V3"
          />
          <AppInput
            v-model="editForm.negativeStructure"
            label="Phủ định (-)"
            placeholder="S + have/has + not + V3"
          />
          <AppInput
            v-model="editForm.questionStructure"
            label="Nghi vấn (?)"
            placeholder="Have/Has + S + V3?"
          />
        </div>

        <AppTextarea
          v-model="editForm.usage"
          label="Cách dùng (Usage)"
          rows="3"
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            v-model="editForm.signalWords"
            label="Dấu hiệu nhận biết"
            placeholder="already, yet, since, for..."
          />
          <AppInput
            v-model="editForm.commonMistakes"
            label="Lỗi thường gặp"
            placeholder="Nhầm lẫn với quá khứ đơn..."
          />
        </div>

        <AppTextarea
          v-model="editForm.note"
          label="Ghi chú cá nhân (Note)"
          rows="2"
        />

        <div class="flex justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton variant="secondary" size="md" @click="showEditModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="md" :loading="savingEdit">
            Lưu thay đổi
          </AppButton>
        </div>
      </form>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { grammarService } from '../../services/grammar.service';
import { useToastStore } from '../../stores/toast.store';
import type { Grammar, GrammarExerciseQuestion, GrammarExerciseHistory } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppTextarea from '../../components/common/AppTextarea.vue';
import AppSelect from '../../components/common/AppSelect.vue';
import AppBadge from '../../components/common/AppBadge.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  ArrowLeft,
  BookOpen,
  Sparkles,
  History,
  RotateCcw,
  CheckCircle2,
  Award,
  Edit,
  Flame,
  AlertCircle,
  Clock,
  Lightbulb,
  Eye,
  Star,
  Layers,
  ListChecks,
  PenTool,
  Search,
  Check,
  Tag,
  HelpCircle,
} from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();
const toastStore = useToastStore();

const grammarId = Number(route.params.id);
const grammar = ref<Grammar | null>(null);
const loadingGrammar = ref(false);
const activeTab = ref<'theory' | 'practice' | 'history'>('theory');

// Exercise History State
const exerciseHistoryList = ref<GrammarExerciseHistory[]>([]);

// Quiz State
type QuizState = 'SETUP' | 'ACTIVE' | 'RESULT';
const quizState = ref<QuizState>('SETUP');
const generatingQuiz = ref(false);
const submittingQuiz = ref(false);

const quizConfig = reactive({
  numberOfQuestions: 5,
  level: 'B1',
  exerciseType: 'MIXED',
  customFocus: '',
});

const activeQuestions = ref<GrammarExerciseQuestion[]>([]);
const currentQuestionIndex = ref(0);
const userAnswers = reactive<Record<number, string>>({});
const timerSeconds = ref(0);
let timerInterval: any = null;

const lastResult = ref<GrammarExerciseHistory | null>(null);

// Edit Modal State
const showEditModal = ref(false);
const savingEdit = ref(false);
const editForm = reactive({
  topic: '',
  level: 'B1',
  structure: '',
  positiveStructure: '',
  negativeStructure: '',
  questionStructure: '',
  usage: '',
  signalWords: '',
  commonMistakes: '',
  note: '',
});

const levelOptions = [
  { label: 'A0 - Mất gốc (Re-learning)', value: 'A0' },
  { label: 'A1 - Beginner (Nhập môn)', value: 'A1' },
  { label: 'A2 - Elementary (Sơ cấp)', value: 'A2' },
  { label: 'B1 - Intermediate (Trung cấp)', value: 'B1' },
  { label: 'B2 - Upper Intermediate (Nâng cao)', value: 'B2' },
  { label: 'C1 - Advanced (Thành thạo)', value: 'C1' },
  { label: 'C2 - Mastery', value: 'C2' },
];

const currentQuestion = computed(() => {
  return activeQuestions.value[currentQuestionIndex.value] || null;
});

const highestScore = computed(() => {
  if (exerciseHistoryList.value.length === 0) return 0;
  return Math.max(...exerciseHistoryList.value.map(h => h.score));
});

onMounted(() => {
  loadGrammarDetail();
  loadExerciseHistory();

  if (route.query.tab === 'practice') {
    activeTab.value = 'practice';
  }
});

onUnmounted(() => {
  stopTimer();
});

async function loadGrammarDetail() {
  loadingGrammar.value = true;
  try {
    grammar.value = await grammarService.getGrammarById(grammarId);
    if (grammar.value.level) {
      quizConfig.level = grammar.value.level;
    }
  } catch (err) {
    toastStore.error('Không thể tải thông tin ngữ pháp');
    router.push('/grammar');
  } finally {
    loadingGrammar.value = false;
  }
}

async function loadExerciseHistory() {
  try {
    exerciseHistoryList.value = await grammarService.getTopicHistory(grammarId);
  } catch (e) {
    // fallback
  }
}

// --- Quiz Engine ---
async function handleGenerateQuiz() {
  generatingQuiz.value = true;
  try {
    const questions = await grammarService.generateExercises({
      topicId: grammarId,
      numberOfQuestions: quizConfig.numberOfQuestions,
      exerciseType: quizConfig.exerciseType,
      level: quizConfig.level,
      customFocus: quizConfig.customFocus || undefined,
    });

    if (!questions || questions.length === 0) {
      toastStore.error('Không thể tạo bài tập, vui lòng thử lại');
      return;
    }

    activeQuestions.value = questions;
    currentQuestionIndex.value = 0;
    // reset answers
    for (const key in userAnswers) {
      delete userAnswers[key];
    }

    startTimer();
    quizState.value = 'ACTIVE';
    toastStore.success(`Đã tạo ${questions.length} câu bài tập ngữ pháp thành công!`);
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Có lỗi khi tạo bài tập AI');
  } finally {
    generatingQuiz.value = false;
  }
}

function selectAnswer(questionId: number, option: string) {
  userAnswers[questionId] = option;
}

function handleNextOrSubmit() {
  if (currentQuestionIndex.value < activeQuestions.value.length - 1) {
    currentQuestionIndex.value++;
  } else {
    handleSubmitQuiz();
  }
}

function startTimer() {
  stopTimer();
  timerSeconds.value = 0;
  timerInterval = setInterval(() => {
    timerSeconds.value++;
  }, 1000);
}

function stopTimer() {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
}

function cancelQuiz() {
  stopTimer();
  quizState.value = 'SETUP';
}

async function handleSubmitQuiz() {
  stopTimer();
  submittingQuiz.value = true;

  try {
    let correctCount = 0;
    activeQuestions.value.forEach(q => {
      const selected = userAnswers[q.id];
      if (checkAnswerIsCorrect(q, selected)) {
        correctCount++;
      }
    });

    const total = activeQuestions.value.length;
    const score = Math.round((correctCount / total) * 100);

    const savedHistory = await grammarService.submitExercise({
      topicId: grammarId,
      title: `Luyện tập: ${grammar.value?.topic || 'Ngữ pháp'}`,
      exerciseType: quizConfig.exerciseType,
      level: quizConfig.level,
      totalQuestions: total,
      correctCount: correctCount,
      score: score,
      timeSpentSeconds: timerSeconds.value,
      questions: activeQuestions.value,
      userAnswers: { ...userAnswers },
    });

    lastResult.value = savedHistory;
    quizState.value = 'RESULT';
    loadExerciseHistory();
    loadGrammarDetail(); // refresh mastery level
    toastStore.success(`Đã hoàn thành bài tập! Điểm số: ${score}%`);
  } catch (err: any) {
    toastStore.error('Không thể lưu kết quả bài tập');
  } finally {
    submittingQuiz.value = false;
  }
}

function viewHistoryItem(historyItem: GrammarExerciseHistory) {
  lastResult.value = historyItem;
  quizState.value = 'RESULT';
  activeTab.value = 'practice';
}

function checkAnswerIsCorrect(q: GrammarExerciseQuestion, userAns?: any): boolean {
  if (userAns === undefined || userAns === null) return false;
  const userStr = String(userAns).trim().toLowerCase();
  const correctStr = (q.correctAnswer || '').trim().toLowerCase();
  if (!userStr) return false;

  if (q.questionType === 'FILL_IN_BLANK') {
    if (userStr === correctStr) return true;
    const cleanU = userStr.replace(/[.,/#!$%^&*;:{}=\-_`~()]/g, '').trim();
    const cleanC = correctStr.replace(/[.,/#!$%^&*;:{}=\-_`~()]/g, '').trim();
    return cleanU === cleanC;
  }

  if (q.questionType === 'ERROR_CORRECTION') {
    if (userStr === correctStr) return true;
    if (userStr.includes(correctStr) || correctStr.includes(userStr)) return true;
    if (q.correctIndex !== undefined && q.options && q.options[q.correctIndex]) {
      const optStr = q.options[q.correctIndex].trim().toLowerCase();
      if (userStr === optStr || userStr.includes(optStr) || optStr.includes(userStr)) return true;
    }
    return false;
  }

  // MULTIPLE_CHOICE
  if (userStr === correctStr) return true;
  if (q.correctIndex !== undefined && q.options && q.options[q.correctIndex]) {
    if (userStr === q.options[q.correctIndex].trim().toLowerCase()) return true;
  }
  return false;
}

function isQuestionCorrect(q: GrammarExerciseQuestion): boolean {
  if (!lastResult.value) return false;
  const userAns = lastResult.value.userAnswers[q.id];
  return checkAnswerIsCorrect(q, userAns);
}

function isOptionCorrectAnswer(q: GrammarExerciseQuestion, opt: string): boolean {
  const cleanOpt = opt.trim().toLowerCase();
  const cleanCorrect = (q.correctAnswer || '').trim().toLowerCase();
  if (cleanOpt === cleanCorrect || cleanOpt.includes(cleanCorrect) || cleanCorrect.includes(cleanOpt)) return true;
  if (q.correctIndex !== undefined && q.options && q.options[q.correctIndex]) {
    return cleanOpt === q.options[q.correctIndex].trim().toLowerCase();
  }
  return false;
}

function isUserSelectedOption(q: GrammarExerciseQuestion, opt: string): boolean {
  const ansSource = quizState.value === 'RESULT' && lastResult.value
    ? lastResult.value.userAnswers[q.id]
    : userAnswers[q.id];

  if (!ansSource) return false;
  const userStr = String(ansSource).trim().toLowerCase();
  const cleanOpt = opt.trim().toLowerCase();
  return userStr === cleanOpt || (userStr.length > 0 && cleanOpt.includes(userStr));
}

function getErrorCorrectionOptionClass(q: GrammarExerciseQuestion, opt: string): string {
  const isCorrect = isOptionCorrectAnswer(q, opt);
  const isSelected = isUserSelectedOption(q, opt);

  if (isCorrect) {
    return 'border-emerald-500 bg-emerald-50 text-emerald-950 font-bold';
  }
  if (isSelected && !isCorrect) {
    return 'border-rose-400 bg-rose-50 text-rose-950 font-bold';
  }
  return 'border-slate-200 bg-white text-slate-700';
}

function getOptionResultClass(q: GrammarExerciseQuestion, optionText: string): string {
  if (!lastResult.value) return 'border-slate-200 bg-white text-slate-800';
  const userAns = lastResult.value.userAnswers[q.id];

  if (optionText === q.correctAnswer) {
    return 'border-emerald-500 bg-emerald-50 text-emerald-900 font-semibold';
  }
  if (userAns === optionText && optionText !== q.correctAnswer) {
    return 'border-rose-400 bg-rose-50 text-rose-900 font-semibold';
  }
  return 'border-slate-200 bg-white text-slate-600';
}

function getQuestionTypeBadgeClass(qType?: string): string {
  switch (qType) {
    case 'FILL_IN_BLANK':
      return 'bg-emerald-100 text-emerald-800';
    case 'ERROR_CORRECTION':
      return 'bg-rose-100 text-rose-800';
    case 'MULTIPLE_CHOICE':
      return 'bg-blue-100 text-blue-800';
    case 'MIXED':
      return 'bg-purple-100 text-purple-800';
    default:
      return 'bg-slate-100 text-slate-800';
  }
}

function getQuestionTypeIcon(qType?: string): any {
  switch (qType) {
    case 'FILL_IN_BLANK':
      return PenTool;
    case 'ERROR_CORRECTION':
      return Search;
    case 'MULTIPLE_CHOICE':
      return ListChecks;
    case 'MIXED':
      return Layers;
    default:
      return Sparkles;
  }
}

function getQuestionTypeLabel(qType?: string): string {
  switch (qType) {
    case 'FILL_IN_BLANK':
      return 'Điền từ / Chia động từ';
    case 'ERROR_CORRECTION':
      return 'Tìm & Sửa lỗi sai';
    case 'MULTIPLE_CHOICE':
      return 'Trắc nghiệm 4 đáp án';
    case 'MIXED':
      return 'Tổng hợp các dạng';
    default:
      return 'Bài tập ngữ pháp';
  }
}

// --- Edit Modal ---
function openEditModal() {
  if (!grammar.value) return;
  editForm.topic = grammar.value.topic;
  editForm.level = grammar.value.level || 'B1';
  editForm.structure = grammar.value.structure || '';
  editForm.positiveStructure = grammar.value.positiveStructure || '';
  editForm.negativeStructure = grammar.value.negativeStructure || '';
  editForm.questionStructure = grammar.value.questionStructure || '';
  editForm.usage = grammar.value.usage || '';
  editForm.signalWords = grammar.value.signalWords || '';
  editForm.commonMistakes = grammar.value.commonMistakes || '';
  editForm.note = grammar.value.note || '';
  showEditModal.value = true;
}

async function saveGrammarEdit() {
  if (!editForm.topic.trim()) {
    toastStore.warning('Vui lòng nhập tên chủ đề ngữ pháp');
    return;
  }

  savingEdit.value = true;
  try {
    const updated = await grammarService.updateGrammar(grammarId, {
      topic: editForm.topic,
      level: editForm.level,
      structure: editForm.structure,
      positiveStructure: editForm.positiveStructure,
      negativeStructure: editForm.negativeStructure,
      questionStructure: editForm.questionStructure,
      usage: editForm.usage,
      signalWords: editForm.signalWords,
      commonMistakes: editForm.commonMistakes,
      note: editForm.note,
    });
    grammar.value = updated;
    showEditModal.value = false;
    toastStore.success('Cập nhật chủ đề ngữ pháp thành công');
  } catch (err: any) {
    toastStore.error('Không thể cập nhật chủ đề ngữ pháp');
  } finally {
    savingEdit.value = false;
  }
}

// --- Helpers ---
function copyFormula(text: string) {
  navigator.clipboard.writeText(text);
  toastStore.success('Đã sao chép công thức!');
}

function getOptionLetter(idx: number): string {
  const letters = ['A', 'B', 'C', 'D', 'E', 'F'];
  return letters[idx] || String(idx + 1);
}

function formatSeconds(sec: number): string {
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m}:${s < 10 ? '0' : ''}${s}`;
}

function formatDate(isoStr?: string): string {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  return d.toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' });
}

function getStatusVariant(status?: string): 'slate' | 'primary' | 'warning' | 'success' {
  switch (status?.toUpperCase()) {
    case 'NEW':
      return 'primary';
    case 'LEARNING':
    case 'REVIEW':
      return 'warning';
    case 'MASTERED':
      return 'success';
    default:
      return 'slate';
  }
}

function getStatusLabel(status?: string): string {
  switch (status?.toUpperCase()) {
    case 'NEW':
      return 'Mới';
    case 'LEARNING':
      return 'Đang học';
    case 'REVIEW':
      return 'Cần ôn';
    case 'MASTERED':
      return 'Thành thạo';
    default:
      return status || 'Mới';
  }
}

function getScoreBannerClass(score: number): string {
  if (score >= 80) return 'bg-emerald-50 text-emerald-900 border-emerald-200';
  if (score >= 50) return 'bg-amber-50 text-amber-900 border-amber-200';
  return 'bg-rose-50 text-rose-900 border-rose-200';
}

function getScoreIconClass(score: number): string {
  if (score >= 80) return 'text-emerald-600';
  if (score >= 50) return 'text-amber-600';
  return 'text-rose-600';
}

function getScoreTitle(score: number): string {
  if (score === 100) return 'Tuyệt vời! Hoàn hảo 100%';
  if (score >= 80) return 'Rất tốt! Nắm vững ngữ pháp';
  if (score >= 60) return 'Khá tốt! Cần lưu ý một số bẫy';
  return 'Cần ôn tập thêm lý thuyết';
}
</script>
