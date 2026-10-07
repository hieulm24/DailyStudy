<template>
  <div class="space-y-6 pb-12">
    <!-- Breadcrumb & Top Navigation -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div class="flex items-center gap-2">
        <router-link
          to="/reading"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg bg-white border border-slate-200 text-slate-600 hover:text-brand-600 hover:bg-slate-50 transition-colors shadow-2xs"
        >
          <ArrowLeft class="w-3.5 h-3.5" />
          <span>Danh sách sách</span>
        </router-link>
        <span class="text-slate-300">/</span>
        <span class="text-xs font-semibold text-slate-500 truncate max-w-xs sm:max-w-md">
          {{ book?.title || 'Đang tải thông tin sách...' }}
        </span>
      </div>

      <!-- Action Buttons -->
      <div v-if="book" class="flex flex-wrap items-center gap-2">
        <AppButton variant="primary" size="sm" :icon="Plus" @click="openAddQuoteModal">
          Thêm triết lý / Trích dẫn
        </AppButton>
        <AppButton variant="secondary" size="sm" :icon="TrendingUp" @click="openQuickProgressModal">
          Cập nhật trang đọc
        </AppButton>
        <AppButton variant="secondary" size="sm" :icon="Edit2" @click="openEditBookModal">
          Sửa sách
        </AppButton>
        <button
          type="button"
          title="Xóa cuốn sách này"
          class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg border border-slate-200 bg-white transition-colors"
          @click="confirmDeleteBook"
        >
          <Trash2 class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 space-y-4">
        <div class="h-64 bg-slate-100 rounded-2xl animate-pulse" />
        <div class="h-96 bg-slate-100 rounded-2xl animate-pulse" />
      </div>
      <div class="space-y-4">
        <div class="h-48 bg-slate-100 rounded-2xl animate-pulse" />
        <div class="h-64 bg-slate-100 rounded-2xl animate-pulse" />
      </div>
    </div>

    <!-- Main Content -->
    <div v-else-if="book" class="space-y-6">
      <!-- HERO / BOOK OVERVIEW BANNER -->
      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-2xs overflow-hidden">
        <div class="p-6 sm:p-8 bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 text-white relative">
          <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(99,102,241,0.15),transparent_50%)]" />
          
          <div class="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div class="flex items-start gap-4">
              <div class="w-16 h-20 sm:w-20 sm:h-26 rounded-xl bg-gradient-to-br from-brand-500 to-indigo-600 flex flex-col items-center justify-center text-white shadow-lg shrink-0 border border-white/20">
                <BookOpen class="w-8 h-8 opacity-90" />
                <span class="text-[10px] font-bold uppercase tracking-wider mt-1 opacity-80">Sách</span>
              </div>

              <div class="space-y-2">
                <div class="flex flex-wrap items-center gap-2">
                  <span :class="['px-2.5 py-0.5 text-xs font-bold rounded-full shadow-2xs', getStatusBadgeClass(book.status)]">
                    {{ getStatusLabel(book.status) }}
                  </span>
                  <span v-if="book.category" class="inline-flex items-center gap-1 px-2.5 py-0.5 text-xs font-semibold rounded-full bg-white/15 text-white/90 border border-white/10">
                    <Folder class="w-3 h-3 text-brand-300" />
                    <span>{{ book.category }}</span>
                  </span>
                </div>

                <h1 class="text-xl sm:text-2xl lg:text-3xl font-black tracking-tight text-white">
                  {{ book.title }}
                </h1>

                <div class="text-sm text-slate-300 font-medium flex flex-wrap items-center gap-3">
                  <span>Tác giả: <strong class="text-white">{{ book.author || 'Chưa rõ' }}</strong></span>
                  <span v-if="book.rating" class="inline-flex items-center gap-1 text-amber-300 font-bold bg-white/10 px-2 py-0.5 rounded-md">
                    <Star class="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                    <span>{{ book.rating }}/5</span>
                  </span>
                </div>
              </div>
            </div>

            <!-- Reading Progress Gauge Card in Banner -->
            <div class="bg-white/10 backdrop-blur-md rounded-xl p-4 border border-white/10 min-w-[220px] space-y-2.5">
              <div class="flex items-center justify-between text-xs">
                <span class="text-slate-300 font-medium">Tiến độ đọc</span>
                <span class="text-brand-300 font-extrabold text-sm">{{ book.progressPercentage }}%</span>
              </div>
              <div class="w-full h-2.5 rounded-full bg-black/30 overflow-hidden">
                <div
                  class="h-full rounded-full bg-gradient-to-r from-brand-400 to-emerald-400 transition-all duration-300"
                  :style="{ width: `${book.progressPercentage}%` }"
                />
              </div>
              <div class="flex items-center justify-between text-[11px] text-slate-300">
                <span>Trang hiện tại: <strong class="text-white">{{ book.currentPage }}</strong></span>
                <span>Tổng: <strong class="text-white">{{ book.totalPages > 0 ? book.totalPages : '?' }}</strong> trang</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Meta Sub-bar -->
        <div class="px-6 py-3.5 bg-slate-50 border-t border-slate-100 flex flex-wrap items-center justify-between gap-4 text-xs text-slate-600">
          <div class="flex flex-wrap items-center gap-4 sm:gap-6">
            <span v-if="book.startDate" class="inline-flex items-center gap-1.5">
              <Calendar class="w-3.5 h-3.5 text-slate-400" />
              <span class="text-slate-400">Bắt đầu:</span>
              <strong class="text-slate-700">{{ formatDate(book.startDate) }}</strong>
            </span>
            <span v-if="book.completedDate" class="inline-flex items-center gap-1.5 text-emerald-700">
              <CheckCircle2 class="w-3.5 h-3.5 text-emerald-600" />
              <span>Hoàn thành:</span>
              <strong>{{ formatDate(book.completedDate) }}</strong>
            </span>
            <span class="inline-flex items-center gap-1.5">
              <Clock class="w-3.5 h-3.5 text-slate-400" />
              <span class="text-slate-400">Cập nhật:</span>
              <strong>{{ formatDate(book.updatedAt) }}</strong>
            </span>
          </div>

          <!-- Fast Page Increment Buttons -->
          <div class="flex items-center gap-1.5">
            <span class="text-[11px] text-slate-400 mr-1 hidden sm:inline">Tăng nhanh:</span>
            <button
              v-for="inc in [5, 10, 20]"
              :key="inc"
              type="button"
              class="px-2 py-1 bg-white hover:bg-brand-50 text-slate-700 hover:text-brand-600 font-bold rounded border border-slate-200 text-xs transition-colors cursor-pointer"
              @click="quickIncrementPage(inc)"
            >
              +{{ inc }} trang
            </button>
          </div>
        </div>
      </div>

      <!-- 2-COLUMN LAYOUT -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- LEFT 2 COLS: QUOTES & PHILOSOPHY SECTION (TÂM ĐIỂM) -->
        <div class="lg:col-span-2 space-y-6">
          <!-- QUOTES HEADER & TOOLBAR -->
          <div class="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-2xs space-y-4">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
              <div class="flex items-center gap-2.5">
                <div class="p-2 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100">
                  <Quote class="w-5 h-5" />
                </div>
                <div>
                  <h2 class="text-base sm:text-lg font-black text-slate-900 flex items-center gap-2">
                    <span>Kho Triết Lý & Trích Dẫn Của Sách</span>
                    <span class="px-2 py-0.5 rounded-full text-xs font-extrabold bg-indigo-100 text-indigo-800">
                      {{ filteredQuotes.length }}
                    </span>
                  </h2>
                  <p class="text-xs text-slate-500">
                    Lưu lại các câu nói hay, tư tưởng sâu sắc và bài học ứng dụng vào cuộc sống
                  </p>
                </div>
              </div>

              <AppButton variant="primary" size="sm" :icon="Plus" @click="openAddQuoteModal">
                Thêm câu nói / Triết lý
              </AppButton>
            </div>

            <!-- Search & Favorite Filter -->
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5">
              <div class="relative flex-1">
                <Search class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                <input
                  v-model="quoteSearchKeyword"
                  type="text"
                  placeholder="Tìm theo nội dung câu trích dẫn, bài học..."
                  class="w-full pl-9 pr-3 py-1.5 text-xs rounded-lg border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/20"
                />
              </div>

              <button
                type="button"
                :class="[
                  'flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold rounded-lg border transition-all shrink-0 cursor-pointer',
                  filterFavoriteOnly
                    ? 'bg-amber-50 text-amber-800 border-amber-300 shadow-2xs'
                    : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
                ]"
                @click="filterFavoriteOnly = !filterFavoriteOnly"
              >
                <Star :class="['w-3.5 h-3.5', filterFavoriteOnly ? 'fill-amber-500 text-amber-500' : 'text-slate-400']" />
                <span>Chỉ xem câu tâm đắc</span>
              </button>
            </div>
          </div>

          <!-- QUOTES LIST -->
          <div v-if="filteredQuotes.length === 0" class="bg-white rounded-2xl border border-slate-200/80 p-8 text-center space-y-3">
            <div class="w-12 h-12 rounded-full bg-indigo-50 text-indigo-500 flex items-center justify-center mx-auto">
              <Sparkles class="w-6 h-6" />
            </div>
            <div class="space-y-1">
              <p class="text-sm font-bold text-slate-800">
                {{ quoteSearchKeyword ? 'Không tìm thấy câu trích dẫn nào phù hợp' : 'Chưa có câu triết lý nào được ghi lại' }}
              </p>
              <p class="text-xs text-slate-500 max-w-md mx-auto">
                Trong quá trình đọc cuốn sách này, hãy ghi lại những câu nói chạm tới cảm xúc hoặc những bài học sâu sắc để xem lại sau này!
              </p>
            </div>
            <AppButton variant="primary" size="sm" :icon="Plus" @click="openAddQuoteModal">
              Ghi lại câu triết lý đầu tiên
            </AppButton>
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="quote in filteredQuotes"
              :key="quote.id"
              class="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-2xs space-y-3.5 hover:shadow-md transition-all relative group"
            >
              <!-- Top bar of quote card -->
              <div class="flex items-start justify-between gap-3">
                <div class="flex items-center gap-2">
                  <span v-if="quote.chapter" class="inline-flex items-center gap-1 px-2.5 py-0.5 text-[11px] font-bold rounded-md bg-indigo-50 text-indigo-700 border border-indigo-100">
                    <Bookmark class="w-3 h-3 text-indigo-600" />
                    <span>{{ quote.chapter }}</span>
                  </span>
                  <span v-if="quote.pageNumber" class="inline-flex items-center gap-1 px-2.5 py-0.5 text-[11px] font-bold rounded-md bg-slate-100 text-slate-700">
                    <BookOpen class="w-3 h-3 text-slate-500" />
                    <span>Trang {{ quote.pageNumber }}</span>
                  </span>
                </div>

                <div class="flex items-center gap-1">
                  <button
                    type="button"
                    :title="quote.isFavorite ? 'Bỏ yêu thích' : 'Đánh dấu tâm đắc'"
                    class="p-1.5 rounded-lg hover:bg-slate-100 transition-colors cursor-pointer"
                    @click="toggleFavorite(quote)"
                  >
                    <Star :class="['w-4 h-4', quote.isFavorite ? 'fill-amber-400 text-amber-400' : 'text-slate-300 hover:text-slate-500']" />
                  </button>
                  <button
                    type="button"
                    title="Chỉnh sửa câu này"
                    class="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-colors cursor-pointer"
                    @click="openEditQuoteModal(quote)"
                  >
                    <Edit2 class="w-3.5 h-3.5" />
                  </button>
                  <button
                    type="button"
                    title="Xóa câu này"
                    class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors cursor-pointer"
                    @click="confirmDeleteQuote(quote.id)"
                  >
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <!-- Main Quote Text -->
              <div class="relative pl-5 border-l-3 border-brand-500 py-1">
                <p class="text-sm sm:text-base font-serif italic text-slate-900 leading-relaxed font-semibold">
                  “{{ quote.quoteText }}”
                </p>
              </div>

              <!-- Personal Lesson / Practical Application -->
              <div v-if="quote.note" class="bg-gradient-to-r from-amber-50/80 to-amber-50/30 p-3.5 rounded-xl border border-amber-200/80 space-y-1">
                <div class="flex items-center gap-1.5 text-amber-900 font-bold text-xs">
                  <Lightbulb class="w-3.5 h-3.5 text-amber-600" />
                  <span>Bài học đúc kết & Áp dụng thực tế:</span>
                </div>
                <p class="text-xs text-amber-950/90 leading-relaxed whitespace-pre-line pl-5 font-medium">
                  {{ quote.note }}
                </p>
              </div>

              <!-- Quote Footer -->
              <div class="flex items-center justify-between text-[11px] text-slate-400 pt-1 border-t border-slate-100">
                <span>Ghi chú lúc: {{ formatDate(quote.createdAt) }}</span>
                <span v-if="quote.isFavorite" class="inline-flex items-center gap-1 text-amber-600 font-bold">
                  <Star class="w-3 h-3 fill-amber-500 text-amber-500" />
                  <span>Câu tâm đắc</span>
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- RIGHT 1 COL: BOOK DESCRIPTION & READING LOG HISTORY -->
        <div class="space-y-6">
          <!-- Book Description Card -->
          <div class="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-2xs space-y-3">
            <h3 class="text-sm font-black text-slate-900 flex items-center gap-2">
              <FileText class="w-4 h-4 text-brand-600" />
              <span>Mô Tả & Tổng Quan Sách</span>
            </h3>

            <div v-if="book.description" class="text-xs text-slate-600 leading-relaxed whitespace-pre-line bg-slate-50/70 p-3.5 rounded-xl border border-slate-100">
              {{ book.description }}
            </div>
            <p v-else class="text-xs text-slate-400 italic">
              Chưa có mô tả tóm tắt cho cuốn sách này.
            </p>

            <div v-if="book.reviewNotes" class="space-y-1 pt-2 border-t border-slate-100">
              <h4 class="text-xs font-bold text-slate-700">Đánh giá chung:</h4>
              <p class="text-xs text-slate-600 bg-amber-50/50 p-2.5 rounded-lg border border-amber-100">{{ book.reviewNotes }}</p>
            </div>
          </div>

          <!-- Quick Progress Update Card -->
          <div class="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-2xs space-y-3.5">
            <h3 class="text-sm font-black text-slate-900 flex items-center gap-2">
              <TrendingUp class="w-4 h-4 text-emerald-600" />
              <span>Cập Nhật Nhanh Trang Đọc</span>
            </h3>

            <div class="space-y-2">
              <label class="block text-xs font-bold text-slate-700">Đang đọc tới trang số:</label>
              <div class="flex items-center gap-2">
                <input
                  v-model.number="quickPageInput"
                  type="number"
                  min="0"
                  :max="book.totalPages > 0 ? book.totalPages : 9999"
                  class="flex-1 px-3 py-2 text-sm font-bold rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
                />
                <button
                  type="button"
                  :disabled="quickPageInput === book.currentPage || savingProgress"
                  class="px-4 py-2 bg-brand-600 hover:bg-brand-700 disabled:opacity-50 text-white font-bold text-xs rounded-lg transition-colors shadow-2xs cursor-pointer"
                  @click="saveQuickPageInput"
                >
                  {{ savingProgress ? 'Lưu...' : 'Lưu' }}
                </button>
              </div>
            </div>

            <!-- Status Quick Switch -->
            <div class="space-y-1.5 pt-2 border-t border-slate-100">
              <label class="block text-xs font-bold text-slate-700">Đổi trạng thái đọc:</label>
              <div class="grid grid-cols-2 gap-1.5 text-xs">
                <button
                  v-for="st in statusOptions"
                  :key="st.value"
                  type="button"
                  :class="[
                    'px-2 py-1.5 rounded-lg font-bold border transition-all text-center cursor-pointer',
                    book.status === st.value
                      ? 'bg-brand-50 text-brand-700 border-brand-300 shadow-2xs'
                      : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
                  ]"
                  @click="switchStatus(st.value)"
                >
                  {{ st.label }}
                </button>
              </div>
            </div>
          </div>

          <!-- Reading Logs / History Card -->
          <div class="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-2xs space-y-3">
            <div class="flex items-center justify-between">
              <h3 class="text-sm font-black text-slate-900 flex items-center gap-2">
                <History class="w-4 h-4 text-sky-600" />
                <span>Nhật Ký Đọc Gần Đây</span>
              </h3>
              <span class="text-xs text-slate-400 font-semibold">{{ book.readingLogs?.length || 0 }} lượt</span>
            </div>

            <div v-if="!book.readingLogs || book.readingLogs.length === 0" class="text-xs text-slate-400 italic text-center py-4 bg-slate-50 rounded-xl border border-dashed border-slate-200">
              Chưa có nhật ký ghi nhận số trang đọc.
            </div>

            <div v-else class="space-y-2 max-h-60 overflow-y-auto pr-1">
              <div
                v-for="log in book.readingLogs"
                :key="log.id"
                class="flex items-center justify-between p-2.5 bg-slate-50 rounded-lg text-xs"
              >
                <div class="space-y-0.5">
                  <span class="font-bold text-slate-800">{{ formatDate(log.logDate) }}</span>
                  <p v-if="log.note" class="text-[11px] text-slate-500">{{ log.note }}</p>
                </div>
                <span class="font-black text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
                  +{{ log.pagesRead }} trang
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL 1: THÊM / SỬA TRIẾT LÝ & TRÍCH DẪN -->
    <AppModal
      v-model="showQuoteModal"
      :title="isEditingQuote ? 'Chỉnh sửa câu triết lý / trích dẫn' : `Thêm triết lý mới cho sách: ${book?.title}`"
      size="lg"
    >
      <form class="space-y-4" @submit.prevent="saveQuote">
        <AppTextarea
          id="quote-text"
          v-model="quoteForm.quoteText"
          label="Nội dung câu nói / triết lý hay"
          rows="3"
          placeholder="Nhập nguyên văn câu trích dẫn hoặc câu nói tâm đắc..."
          required
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            id="quote-chapter"
            v-model="quoteForm.chapter"
            label="Chương / Mục"
            placeholder="Ví dụ: Chương 3: Tư duy tích cực"
          />

          <AppInput
            id="quote-page"
            v-model.number="quoteForm.pageNumber"
            label="Trang số"
            type="number"
            min="1"
            placeholder="Ví dụ: 45"
          />
        </div>

        <AppTextarea
          id="quote-note"
          v-model="quoteForm.note"
          label="Bài học rút ra / Cảm nghĩ cá nhân (Áp dụng cho cuộc sống)"
          rows="3"
          placeholder="Ý nghĩa của câu nói đối với bạn, bài học thực tế áp dụng vào công việc & cuộc sống..."
        />

        <div class="flex items-center gap-2 pt-1">
          <input
            id="quote-fav"
            v-model="quoteForm.isFavorite"
            type="checkbox"
            class="w-4 h-4 rounded text-brand-600 focus:ring-brand-500 border-slate-300"
          />
          <label for="quote-fav" class="text-xs font-bold text-slate-700 cursor-pointer flex items-center gap-1.5">
            <span>Đánh dấu là câu triết lý tâm đắc nhất</span>
            <Star class="w-3.5 h-3.5 fill-amber-500 text-amber-500" />
          </label>
        </div>

        <div class="flex justify-end gap-2.5 pt-4 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showQuoteModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="savingQuote">
            {{ isEditingQuote ? 'Lưu thay đổi' : 'Lưu triết lý' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- MODAL 2: SỬA THÔNG TIN SÁCH -->
    <AppModal v-model="showBookModal" title="Chỉnh sửa thông tin sách" size="lg">
      <form class="space-y-4" @submit.prevent="saveBook">
        <AppInput
          id="edit-book-title"
          v-model="bookForm.title"
          label="Tên cuốn sách"
          placeholder="Nhập tên sách..."
          required
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            id="edit-book-author"
            v-model="bookForm.author"
            label="Tác giả"
            placeholder="Ví dụ: Dale Carnegie, James Clear..."
          />

          <AppInput
            id="edit-book-category"
            v-model="bookForm.category"
            label="Thể loại / Danh mục"
            placeholder="Phát triển bản thân, Kinh doanh, Triết học..."
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <AppInput
            id="edit-book-total-pages"
            v-model.number="bookForm.totalPages"
            label="Tổng số trang"
            type="number"
            min="1"
            placeholder="300"
          />

          <AppInput
            id="edit-book-current-page"
            v-model.number="bookForm.currentPage"
            label="Trang hiện tại"
            type="number"
            min="0"
            placeholder="0"
          />

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Trạng thái đọc</label>
            <select
              v-model="bookForm.status"
              class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            >
              <option value="READING">Đang đọc</option>
              <option value="COMPLETED">Đã xong</option>
              <option value="WANT_TO_READ">Dự định đọc</option>
              <option value="ON_HOLD">Tạm dừng</option>
            </select>
          </div>
        </div>

        <AppTextarea
          id="edit-book-desc"
          v-model="bookForm.description"
          label="Mô tả tổng quan / Tóm tắt nội dung"
          rows="3"
          placeholder="Tóm tắt ngắn gọn nội dung cuốn sách..."
        />

        <AppTextarea
          id="edit-book-review"
          v-model="bookForm.reviewNotes"
          label="Đánh giá / Cảm nhận cá nhân sau khi đọc"
          rows="2"
          placeholder="Cảm nhận chung về cuốn sách..."
        />

        <div class="flex justify-end gap-2.5 pt-4 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showBookModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="savingBook">
            Lưu thay đổi
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- MODAL 3: CẬP NHẬT TIẾN ĐỘ ĐỌC CHI TIẾT -->
    <AppModal v-model="showProgressModal" title="Cập nhật tiến độ đọc" size="md">
      <form class="space-y-4" @submit.prevent="saveProgressModal">
        <div>
          <label class="block text-xs font-bold text-slate-700 mb-1">Trang hiện tại đang đọc tới</label>
          <input
            v-model.number="progressForm.currentPage"
            type="number"
            min="0"
            :max="book?.totalPages || 9999"
            class="w-full px-3 py-2 text-sm font-bold rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            required
          />
        </div>

        <div>
          <label class="block text-xs font-bold text-slate-700 mb-1">Số trang đã đọc hôm nay (Ghi nhận vào nhật ký)</label>
          <input
            v-model.number="progressForm.pagesReadToday"
            type="number"
            min="0"
            placeholder="Ví dụ: 15"
            class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
          />
        </div>

        <div>
          <label class="block text-xs font-bold text-slate-700 mb-1">Trạng thái</label>
          <select
            v-model="progressForm.status"
            class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
          >
            <option value="READING">Đang đọc</option>
            <option value="COMPLETED">Đã đọc xong</option>
            <option value="WANT_TO_READ">Dự định đọc</option>
            <option value="ON_HOLD">Tạm dừng</option>
          </select>
        </div>

        <div class="flex justify-end gap-2.5 pt-4 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showProgressModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="savingProgress">
            Cập nhật tiến độ
          </AppButton>
        </div>
      </form>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { bookService } from '../../services/book.service';
import type { BookDetail, BookQuote, BookStatus } from '../../types/book.types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppTextarea from '../../components/common/AppTextarea.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  ArrowLeft,
  BookOpen,
  Quote,
  Plus,
  Search,
  Star,
  FileText,
  Sparkles,
  TrendingUp,
  Edit2,
  Trash2,
  Folder,
  Bookmark,
  Lightbulb,
  Calendar,
  CheckCircle2,
  Clock,
  History,
} from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();

const bookId = Number(route.params.id);
const book = ref<BookDetail | null>(null);
const loading = ref(true);

const quoteSearchKeyword = ref('');
const filterFavoriteOnly = ref(false);
const quickPageInput = ref(0);
const savingProgress = ref(false);

// Quote Modal State
const showQuoteModal = ref(false);
const isEditingQuote = ref(false);
const editingQuoteId = ref<number | null>(null);
const savingQuote = ref(false);
const quoteForm = reactive({
  quoteText: '',
  pageNumber: undefined as number | undefined,
  chapter: '',
  note: '',
  isFavorite: false,
});

// Edit Book Modal State
const showBookModal = ref(false);
const savingBook = ref(false);
const bookForm = reactive({
  title: '',
  author: '',
  category: '',
  description: '',
  reviewNotes: '',
  totalPages: 0,
  currentPage: 0,
  status: 'READING' as BookStatus,
  rating: undefined as number | undefined,
});

// Quick Progress Modal State
const showProgressModal = ref(false);
const progressForm = reactive({
  currentPage: 0,
  pagesReadToday: 0,
  status: 'READING' as BookStatus,
});

const statusOptions: { label: string; value: BookStatus }[] = [
  { label: 'Đang đọc', value: 'READING' },
  { label: 'Đã xong', value: 'COMPLETED' },
  { label: 'Dự định', value: 'WANT_TO_READ' },
  { label: 'Tạm dừng', value: 'ON_HOLD' },
];

const filteredQuotes = computed(() => {
  if (!book.value || !book.value.quotes) return [];
  return book.value.quotes.filter((q) => {
    if (filterFavoriteOnly.value && !q.isFavorite) return false;
    if (quoteSearchKeyword.value.trim()) {
      const kw = quoteSearchKeyword.value.toLowerCase();
      const matchText = q.quoteText.toLowerCase().includes(kw);
      const matchNote = q.note ? q.note.toLowerCase().includes(kw) : false;
      const matchChapter = q.chapter ? q.chapter.toLowerCase().includes(kw) : false;
      return matchText || matchNote || matchChapter;
    }
    return true;
  });
});

onMounted(() => {
  loadBookDetail();
});

async function loadBookDetail() {
  if (!bookId || isNaN(bookId)) {
    router.push('/reading');
    return;
  }
  loading.value = true;
  try {
    const data = await bookService.getBookDetail(bookId);
    book.value = data;
    quickPageInput.value = data.currentPage;
  } catch (err) {
    console.error('Failed to load book detail', err);
  } finally {
    loading.value = false;
  }
}

// Quote actions
function openAddQuoteModal() {
  isEditingQuote.value = false;
  editingQuoteId.value = null;
  quoteForm.quoteText = '';
  quoteForm.pageNumber = book.value?.currentPage || undefined;
  quoteForm.chapter = '';
  quoteForm.note = '';
  quoteForm.isFavorite = false;
  showQuoteModal.value = true;
}

function openEditQuoteModal(quote: BookQuote) {
  isEditingQuote.value = true;
  editingQuoteId.value = quote.id;
  quoteForm.quoteText = quote.quoteText;
  quoteForm.pageNumber = quote.pageNumber ?? undefined;
  quoteForm.chapter = quote.chapter || '';
  quoteForm.note = quote.note || '';
  quoteForm.isFavorite = quote.isFavorite;
  showQuoteModal.value = true;
}

async function saveQuote() {
  if (!quoteForm.quoteText.trim() || !book.value) return;
  savingQuote.value = true;
  try {
    if (isEditingQuote.value && editingQuoteId.value) {
      await bookService.updateQuote(editingQuoteId.value, {
        quoteText: quoteForm.quoteText,
        pageNumber: quoteForm.pageNumber || undefined,
        chapter: quoteForm.chapter || undefined,
        note: quoteForm.note || undefined,
        isFavorite: quoteForm.isFavorite,
      });
    } else {
      await bookService.addQuote(book.value.id, {
        quoteText: quoteForm.quoteText,
        pageNumber: quoteForm.pageNumber || undefined,
        chapter: quoteForm.chapter || undefined,
        note: quoteForm.note || undefined,
        isFavorite: quoteForm.isFavorite,
      });
    }
    showQuoteModal.value = false;
    await loadBookDetail();
  } catch (err) {
    console.error('Failed to save quote', err);
  } finally {
    savingQuote.value = false;
  }
}

async function toggleFavorite(quote: BookQuote) {
  try {
    const updated = await bookService.toggleFavoriteQuote(quote.id);
    quote.isFavorite = updated.isFavorite;
  } catch (err) {
    console.error('Failed to toggle favorite', err);
  }
}

async function confirmDeleteQuote(id: number) {
  if (!confirm('Bạn có chắc chắn muốn xóa câu triết lý này?')) return;
  try {
    await bookService.deleteQuote(id);
    await loadBookDetail();
  } catch (err) {
    console.error('Failed to delete quote', err);
  }
}

// Quick Progress
async function quickIncrementPage(amount: number) {
  if (!book.value) return;
  const newPage = (book.value.currentPage || 0) + amount;
  const targetPage = book.value.totalPages > 0 ? Math.min(newPage, book.value.totalPages) : newPage;
  try {
    await bookService.updateProgress(book.value.id, {
      currentPage: targetPage,
      pagesReadToday: amount,
    });
    await loadBookDetail();
  } catch (err) {
    console.error('Failed to increment progress', err);
  }
}

async function saveQuickPageInput() {
  if (!book.value || quickPageInput.value === book.value.currentPage) return;
  savingProgress.value = true;
  try {
    const pagesRead = quickPageInput.value > book.value.currentPage
      ? quickPageInput.value - book.value.currentPage
      : undefined;
    await bookService.updateProgress(book.value.id, {
      currentPage: quickPageInput.value,
      pagesReadToday: pagesRead,
    });
    await loadBookDetail();
  } catch (err) {
    console.error('Failed to save quick page', err);
  } finally {
    savingProgress.value = false;
  }
}

async function switchStatus(newStatus: BookStatus) {
  if (!book.value || book.value.status === newStatus) return;
  try {
    await bookService.updateProgress(book.value.id, {
      currentPage: book.value.currentPage,
      status: newStatus,
    });
    await loadBookDetail();
  } catch (err) {
    console.error('Failed to switch status', err);
  }
}

// Book Modal Actions
function openEditBookModal() {
  if (!book.value) return;
  bookForm.title = book.value.title;
  bookForm.author = book.value.author || '';
  bookForm.category = book.value.category || '';
  bookForm.description = book.value.description || '';
  bookForm.reviewNotes = book.value.reviewNotes || '';
  bookForm.totalPages = book.value.totalPages || 0;
  bookForm.currentPage = book.value.currentPage || 0;
  bookForm.status = book.value.status;
  bookForm.rating = book.value.rating ?? undefined;
  showBookModal.value = true;
}

async function saveBook() {
  if (!bookForm.title.trim() || !book.value) return;
  savingBook.value = true;
  try {
    await bookService.updateBook(book.value.id, {
      title: bookForm.title,
      author: bookForm.author || undefined,
      category: bookForm.category || undefined,
      description: bookForm.description || undefined,
      reviewNotes: bookForm.reviewNotes || undefined,
      totalPages: bookForm.totalPages,
      currentPage: bookForm.currentPage,
      status: bookForm.status,
      rating: bookForm.rating || undefined,
    });
    showBookModal.value = false;
    await loadBookDetail();
  } catch (err) {
    console.error('Failed to save book info', err);
  } finally {
    savingBook.value = false;
  }
}

async function confirmDeleteBook() {
  if (!book.value) return;
  if (!confirm(`Bạn có chắc chắn muốn xóa cuốn sách "${book.value.title}" và toàn bộ triết lý của nó?`)) return;
  try {
    await bookService.deleteBook(book.value.id);
    router.push('/reading');
  } catch (err) {
    console.error('Failed to delete book', err);
  }
}

// Quick Progress Modal
function openQuickProgressModal() {
  if (!book.value) return;
  progressForm.currentPage = book.value.currentPage || 0;
  progressForm.pagesReadToday = 0;
  progressForm.status = book.value.status;
  showProgressModal.value = true;
}

async function saveProgressModal() {
  if (!book.value) return;
  savingProgress.value = true;
  try {
    await bookService.updateProgress(book.value.id, {
      currentPage: progressForm.currentPage,
      pagesReadToday: progressForm.pagesReadToday > 0 ? progressForm.pagesReadToday : undefined,
      status: progressForm.status,
    });
    showProgressModal.value = false;
    await loadBookDetail();
  } catch (err) {
    console.error('Failed to update progress modal', err);
  } finally {
    savingProgress.value = false;
  }
}

// Helpers
function getStatusLabel(st: BookStatus) {
  switch (st) {
    case 'READING': return 'Đang đọc';
    case 'COMPLETED': return 'Đã xong';
    case 'WANT_TO_READ': return 'Dự định';
    case 'ON_HOLD': return 'Tạm dừng';
    default: return st;
  }
}

function getStatusBadgeClass(st: BookStatus) {
  switch (st) {
    case 'READING': return 'bg-amber-500/20 text-amber-200 border border-amber-400/30';
    case 'COMPLETED': return 'bg-emerald-500/20 text-emerald-200 border border-emerald-400/30';
    case 'WANT_TO_READ': return 'bg-sky-500/20 text-sky-200 border border-sky-400/30';
    case 'ON_HOLD': return 'bg-slate-500/20 text-slate-200 border border-slate-400/30';
    default: return 'bg-slate-500/20 text-slate-200';
  }
}

function formatDate(d: string) {
  if (!d) return '';
  const dt = new Date(d);
  return dt.toLocaleDateString('vi-VN');
}
</script>
