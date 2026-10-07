<template>
  <div class="space-y-6 pb-12">
    <!-- Header & Action Bar -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-white p-5 sm:p-6 rounded-xl border border-slate-200/80 shadow-2xs">
      <div class="space-y-1">
        <div class="flex items-center gap-2.5">
          <div class="p-2 rounded-lg bg-indigo-50 text-indigo-600 border border-indigo-100">
            <BookMarked class="w-6 h-6" />
          </div>
          <div>
            <h1 class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">Đọc sách & Triết lý sống</h1>
            <p class="text-xs sm:text-sm text-slate-500 font-medium">Theo dõi tiến độ đọc, lưu giữ tinh hoa tri thức và các câu triết lý tâm đắc.</p>
          </div>
        </div>
      </div>

      <div class="flex items-center gap-2.5 flex-wrap sm:flex-nowrap">
        <AppButton variant="outline" size="sm" :icon="Quote" @click="openAddQuoteModal(null)">
          Thêm triết lý
        </AppButton>
        <AppButton variant="primary" size="sm" :icon="Plus" @click="openAddBookModal">
          Thêm sách mới
        </AppButton>
      </div>
    </div>

    <!-- Statistics Overview Metric Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
      <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-2xs space-y-2 hover:border-brand-200 transition-colors">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Tổng số sách</span>
          <div class="p-2 rounded-lg bg-brand-50 text-brand-600">
            <BookOpen class="w-4.5 h-4.5" />
          </div>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="text-2xl sm:text-3xl font-black text-slate-900">{{ stats.totalBooks }}</span>
          <span class="text-xs text-slate-500">cuốn sách</span>
        </div>
        <div class="flex items-center gap-2 text-xs font-medium text-slate-500 pt-1">
          <span class="text-emerald-600 font-semibold">{{ stats.completedBooks }} đã đọc xong</span>
          <span>•</span>
          <span class="text-amber-600 font-semibold">{{ stats.readingBooks }} đang đọc</span>
        </div>
      </div>

      <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-2xs space-y-2 hover:border-amber-200 transition-colors">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Đang đọc</span>
          <div class="p-2 rounded-lg bg-amber-50 text-amber-600">
            <Hourglass class="w-4.5 h-4.5" />
          </div>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="text-2xl sm:text-3xl font-black text-amber-600">{{ stats.readingBooks }}</span>
          <span class="text-xs text-slate-500">cuốn đang đọc</span>
        </div>
        <div class="text-xs text-slate-500 truncate">
          {{ stats.wantToReadBooks }} cuốn dự định đọc tiếp
        </div>
      </div>

      <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-2xs space-y-2 hover:border-emerald-200 transition-colors">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Trang đã đọc</span>
          <div class="p-2 rounded-lg bg-emerald-50 text-emerald-600">
            <FileText class="w-4.5 h-4.5" />
          </div>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="text-2xl sm:text-3xl font-black text-emerald-600">{{ stats.totalPagesRead.toLocaleString() }}</span>
          <span class="text-xs text-slate-500">trang tích lũy</span>
        </div>
        <div class="text-xs text-slate-500">
          Tổng khối lượng kiến thức đã nạp
        </div>
      </div>

      <div class="bg-white p-4 sm:p-5 rounded-xl border border-slate-200/80 shadow-2xs space-y-2 hover:border-purple-200 transition-colors">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">Kho triết lý & Trích dẫn</span>
          <div class="p-2 rounded-lg bg-purple-50 text-purple-600">
            <Sparkles class="w-4.5 h-4.5" />
          </div>
        </div>
        <div class="flex items-baseline gap-2">
          <span class="text-2xl sm:text-3xl font-black text-purple-600">{{ stats.totalQuotes }}</span>
          <span class="text-xs text-slate-500">câu tâm đắc</span>
        </div>
        <div class="flex items-center gap-1.5 text-xs text-purple-600 font-medium">
          <Star class="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
          <span>{{ stats.favoriteQuotes }} câu triết lý yêu thích</span>
        </div>
      </div>
    </div>

    <!-- Main Navigation Tabs -->
    <div class="border-b border-slate-200 flex items-center justify-between">
      <div class="flex items-center gap-2 -mb-px">
        <button
          type="button"
          :class="[
            'flex items-center gap-2 px-4 py-3 text-sm font-bold border-b-2 transition-all',
            activeTab === 'books'
              ? 'border-brand-600 text-brand-600 bg-brand-50/20'
              : 'border-transparent text-slate-600 hover:text-slate-900 hover:border-slate-300',
          ]"
          @click="activeTab = 'books'"
        >
          <BookOpen class="w-4 h-4" />
          <span>Tủ sách của tôi ({{ stats.totalBooks }})</span>
        </button>

        <button
          type="button"
          :class="[
            'flex items-center gap-2 px-4 py-3 text-sm font-bold border-b-2 transition-all',
            activeTab === 'quotes'
              ? 'border-brand-600 text-brand-600 bg-brand-50/20'
              : 'border-transparent text-slate-600 hover:text-slate-900 hover:border-slate-300',
          ]"
          @click="activeTab = 'quotes'"
        >
          <Quote class="w-4 h-4" />
          <span>Triết lý & Trích dẫn ({{ stats.totalQuotes }})</span>
        </button>
      </div>
    </div>

    <!-- TAB 1: TỦ SÁCH CỦA TÔI -->
    <div v-if="activeTab === 'books'" class="space-y-4">
      <!-- Search & Filters Bar -->
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 bg-white p-3.5 rounded-xl border border-slate-200/80 shadow-2xs">
        <div class="flex items-center gap-2 flex-1 max-w-md">
          <div class="relative w-full">
            <Search class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="Tìm theo tên sách, tác giả..."
              class="w-full pl-9 pr-3 py-2 text-sm rounded-lg border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
              @input="debounceSearch"
            />
          </div>
        </div>

        <div class="flex items-center gap-2 overflow-x-auto pb-1 md:pb-0">
          <!-- Status Filters -->
          <div class="flex items-center bg-slate-100 p-1 rounded-lg shrink-0">
            <button
              v-for="st in statusFilters"
              :key="st.value"
              type="button"
              :class="[
                'px-2.5 py-1 text-xs font-bold rounded-md transition-colors',
                selectedStatus === st.value
                  ? 'bg-white text-slate-900 shadow-2xs'
                  : 'text-slate-600 hover:text-slate-900',
              ]"
              @click="setStatusFilter(st.value)"
            >
              {{ st.label }}
            </button>
          </div>

          <!-- Category filter -->
          <select
            v-if="stats.categories.length > 0"
            v-model="selectedCategory"
            class="px-2.5 py-1.5 text-xs font-semibold rounded-lg border border-slate-200 bg-white text-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            @change="loadBooks"
          >
            <option value="">Tất cả thể loại</option>
            <option v-for="cat in stats.categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
        </div>
      </div>

      <!-- Books Grid -->
      <div v-if="loadingBooks" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="i in 6" :key="i" class="h-48 bg-slate-100 animate-pulse rounded-xl" />
      </div>

      <div v-else-if="books.length === 0" class="bg-white p-12 text-center rounded-xl border border-dashed border-slate-200 space-y-3">
        <div class="w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto">
          <BookOpen class="w-6 h-6" />
        </div>
        <div class="space-y-1">
          <h3 class="text-base font-bold text-slate-800">Chưa có cuốn sách nào</h3>
          <p class="text-xs text-slate-500">Bắt đầu thêm các cuốn sách bạn đang đọc hoặc dự định đọc để theo dõi tiến độ!</p>
        </div>
        <AppButton variant="primary" size="sm" :icon="Plus" @click="openAddBookModal">
          Thêm sách đầu tiên
        </AppButton>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4.5">
        <div
          v-for="book in books"
          :key="book.id"
          class="bg-white rounded-xl border border-slate-200/90 shadow-2xs hover:shadow-md hover:border-slate-300 transition-all flex flex-col justify-between overflow-hidden group"
        >
          <!-- Top Card Section -->
          <div class="p-5 space-y-3">
            <div class="flex items-start justify-between gap-3">
              <div class="flex items-start gap-3 min-w-0">
                <!-- Book Icon / Cover representation -->
                <div class="w-11 h-13 rounded-lg bg-gradient-to-br from-indigo-500 to-brand-600 text-white flex items-center justify-center font-bold text-base shrink-0 shadow-2xs">
                  {{ book.title.charAt(0).toUpperCase() }}
                </div>
                <div class="min-w-0 space-y-0.5">
                  <h3
                    class="text-base font-bold text-slate-900 group-hover:text-brand-600 transition-colors line-clamp-1 cursor-pointer"
                    @click="goToBookDetail(book.id)"
                  >
                    {{ book.title }}
                  </h3>
                  <p class="text-xs font-semibold text-slate-500 line-clamp-1">
                    {{ book.author || 'Chưa rõ tác giả' }}
                  </p>
                </div>
              </div>

              <!-- Status Badge -->
              <span
                :class="[
                  'px-2 py-0.5 text-[11px] font-bold rounded-full uppercase tracking-wider shrink-0',
                  getStatusBadgeClass(book.status),
                ]"
              >
                {{ getStatusLabel(book.status) }}
              </span>
            </div>

            <!-- Category & Rating -->
            <div class="flex items-center justify-between text-xs text-slate-500 pt-1">
              <span v-if="book.category" class="inline-flex items-center px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 font-medium text-[11px]">
                {{ book.category }}
              </span>
              <span v-else class="text-[11px] text-slate-400">Chưa phân loại</span>

              <div v-if="book.rating" class="flex items-center gap-0.5 text-amber-500">
                <Star v-for="s in book.rating" :key="s" class="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
              </div>
            </div>

            <!-- Description if any -->
            <p v-if="book.description" class="text-xs text-slate-600 line-clamp-2 leading-relaxed bg-slate-50/70 p-2 rounded-md">
              {{ book.description }}
            </p>

            <!-- Progress Bar -->
            <div class="space-y-1.5 pt-1">
              <div class="flex items-center justify-between text-xs">
                <span class="font-bold text-slate-700">
                  {{ book.currentPage }} / {{ book.totalPages > 0 ? book.totalPages : '?' }} trang
                </span>
                <span class="font-extrabold text-brand-600">
                  {{ book.progressPercentage }}%
                </span>
              </div>
              <div class="w-full h-2 rounded-full bg-slate-100 overflow-hidden">
                <div
                  class="h-full rounded-full bg-gradient-to-r from-brand-500 to-indigo-600 transition-all duration-300"
                  :style="{ width: `${book.progressPercentage}%` }"
                />
              </div>
            </div>
          </div>

          <!-- Bottom Card Footer / Actions -->
          <div class="px-4 py-2.5 bg-slate-50/70 border-t border-slate-100 flex items-center justify-between text-xs">
            <div class="flex items-center gap-1.5 font-semibold text-indigo-600">
              <Quote class="w-3.5 h-3.5" />
              <span>{{ book.quoteCount || 0 }} triết lý</span>
            </div>

            <div class="flex items-center gap-1">
              <button
                type="button"
                title="Xem chi tiết sách & Triết lý"
                class="p-1.5 text-slate-500 hover:text-indigo-600 hover:bg-white rounded-md transition-colors"
                @click="goToBookDetail(book.id)"
              >
                <Eye class="w-4 h-4" />
              </button>
              <button
                type="button"
                title="Cập nhật tiến độ đọc"
                class="p-1.5 text-slate-500 hover:text-brand-600 hover:bg-white rounded-md transition-colors"
                @click="openQuickProgressModal(book)"
              >
                <TrendingUp class="w-4 h-4" />
              </button>
              <button
                type="button"
                title="Chỉnh sửa sách"
                class="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-white rounded-md transition-colors"
                @click="openEditBookModal(book)"
              >
                <Edit2 class="w-4 h-4" />
              </button>
              <button
                type="button"
                title="Xóa sách"
                class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
                @click="confirmDeleteBook(book)"
              >
                <Trash2 class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB 2: KHO TRIẾT LÝ & TRÍCH DẪN HAY -->
    <div v-else-if="activeTab === 'quotes'" class="space-y-4">
      <!-- Search & Filters for Quotes -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white p-3.5 rounded-xl border border-slate-200/80 shadow-2xs">
        <div class="relative flex-1 max-w-md">
          <Search class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            v-model="quoteKeyword"
            type="text"
            placeholder="Tìm theo nội dung câu triết lý, bài học..."
            class="w-full pl-9 pr-3 py-2 text-sm rounded-lg border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            @input="debounceSearchQuotes"
          />
        </div>

        <div class="flex items-center gap-2">
          <button
            type="button"
            :class="[
              'flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold rounded-lg border transition-all',
              filterOnlyFavorite
                ? 'bg-amber-50 text-amber-800 border-amber-300 shadow-2xs'
                : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
            ]"
            @click="toggleOnlyFavorite"
          >
            <Star :class="['w-3.5 h-3.5', filterOnlyFavorite ? 'fill-amber-500 text-amber-500' : 'text-slate-400']" />
            <span>Chỉ xem câu tâm đắc nhất</span>
          </button>

          <AppButton variant="primary" size="sm" :icon="Plus" @click="openAddQuoteModal(null)">
            Thêm triết lý
          </AppButton>
        </div>
      </div>

      <!-- Quotes List / Masonry-style Grid -->
      <div v-if="loadingQuotes" class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div v-for="i in 4" :key="i" class="h-36 bg-slate-100 animate-pulse rounded-xl" />
      </div>

      <div v-else-if="quotes.length === 0" class="bg-white p-12 text-center rounded-xl border border-dashed border-slate-200 space-y-3">
        <div class="w-12 h-12 rounded-full bg-purple-50 text-purple-600 flex items-center justify-center mx-auto">
          <Quote class="w-6 h-6" />
        </div>
        <div class="space-y-1">
          <h3 class="text-base font-bold text-slate-800">Chưa có câu triết lý nào</h3>
          <p class="text-xs text-slate-500">Trong quá trình đọc sách, hãy lưu lại những câu danh ngôn, triết lý đắt giá nhất tại đây!</p>
        </div>
        <AppButton variant="primary" size="sm" :icon="Plus" @click="openAddQuoteModal(null)">
          Lưu câu triết lý đầu tiên
        </AppButton>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="q in quotes"
          :key="q.id"
          class="bg-white p-5 rounded-xl border border-slate-200/90 shadow-2xs hover:shadow-md hover:border-indigo-200 transition-all flex flex-col justify-between space-y-3 relative group"
        >
          <div class="space-y-2.5">
            <!-- Quote Header with Book Info -->
            <div class="flex items-start justify-between gap-2 border-b border-slate-100 pb-2">
              <div class="space-y-0.5">
                <button
                  type="button"
                  class="text-xs font-bold text-brand-700 hover:text-brand-900 hover:underline inline-flex items-center gap-1.5 text-left cursor-pointer"
                  @click="goToBookDetail(q.bookId)"
                >
                  <BookOpen class="w-3.5 h-3.5 text-brand-600 shrink-0" />
                  <span>{{ q.bookTitle }}</span>
                  <ArrowRight class="w-3 h-3 text-brand-400 shrink-0" />
                </button>
                <p v-if="q.bookAuthor" class="text-[11px] text-slate-500 font-medium">Tác giả: {{ q.bookAuthor }}</p>
              </div>

              <!-- Favorite Button -->
              <button
                type="button"
                title="Đánh dấu tâm đắc"
                class="p-1.5 rounded-md hover:bg-slate-100 transition-colors"
                @click="toggleFavorite(q)"
              >
                <Star :class="['w-4 h-4', q.isFavorite ? 'fill-amber-400 text-amber-400' : 'text-slate-300 hover:text-amber-400']" />
              </button>
            </div>

            <!-- Quote Body -->
            <div class="relative pl-3 border-l-3 border-indigo-400">
              <p class="text-sm font-semibold text-slate-900 leading-relaxed italic">
                “{{ q.quoteText }}”
              </p>
              <div v-if="q.pageNumber || q.chapter" class="mt-1 text-[11px] text-slate-400 font-medium">
                <span v-if="q.chapter">Chương: {{ q.chapter }}</span>
                <span v-if="q.chapter && q.pageNumber"> • </span>
                <span v-if="q.pageNumber">Trang {{ q.pageNumber }}</span>
              </div>
            </div>

            <!-- Note / Personal reflection -->
            <div v-if="q.note" class="bg-amber-50/60 border border-amber-100 p-2.5 rounded-lg text-xs text-amber-900 space-y-0.5">
              <div class="font-bold text-[11px] text-amber-800 uppercase tracking-wider flex items-center gap-1">
                <Lightbulb class="w-3.5 h-3.5 text-amber-600" />
                <span>Bài học đúc kết:</span>
              </div>
              <p class="leading-relaxed">{{ q.note }}</p>
            </div>
          </div>

          <!-- Quote Actions -->
          <div class="flex items-center justify-between pt-2 border-t border-slate-100 text-[11px] text-slate-400">
            <span>{{ formatDate(q.createdAt) }}</span>
            <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
              <button
                type="button"
                class="p-1 hover:text-slate-700 hover:bg-slate-100 rounded"
                @click="openEditQuoteModal(q)"
              >
                <Edit2 class="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                class="p-1 hover:text-rose-600 hover:bg-rose-50 rounded"
                @click="deleteQuote(q.id)"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL 1: THÊM / SỬA SÁCH -->
    <AppModal v-model="showBookModal" :title="isEditingBook ? 'Chỉnh sửa thông tin sách' : 'Thêm cuốn sách mới'" size="lg">
      <form class="space-y-4" @submit.prevent="saveBook">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            id="book-title"
            v-model="bookForm.title"
            label="Tên sách"
            placeholder="Ví dụ: Đắc nhân tâm, Nhà giả kim..."
            required
          />

          <AppInput
            id="book-author"
            v-model="bookForm.author"
            label="Tác giả"
            placeholder="Ví dụ: Dale Carnegie, Paulo Coelho..."
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Thể loại sách</label>
            <input
              v-model="bookForm.category"
              list="category-suggestions"
              placeholder="Chọn hoặc nhập thể loại..."
              class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            />
            <datalist id="category-suggestions">
              <option value="Phát triển bản thân" />
              <option value="Kinh doanh & Đầu tư" />
              <option value="Tâm lý học" />
              <option value="Triết học & Tư duy" />
              <option value="Công nghệ & Lập trình" />
              <option value="Tiểu thuyết & Văn học" />
              <option value="Lịch sử & Xã hội" />
              <option value="Sức khỏe & Lối sống" />
            </datalist>
          </div>

          <AppInput
            id="book-total-pages"
            v-model.number="bookForm.totalPages"
            label="Tổng số trang"
            type="number"
            min="0"
            placeholder="350"
          />

          <AppInput
            id="book-current-page"
            v-model.number="bookForm.currentPage"
            label="Trang đang đọc"
            type="number"
            min="0"
            placeholder="0"
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Trạng thái đọc</label>
            <select
              v-model="bookForm.status"
              class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            >
              <option value="WANT_TO_READ">Dự định đọc</option>
              <option value="READING">Đang đọc</option>
              <option value="COMPLETED">Đã đọc xong</option>
              <option value="ON_HOLD">Tạm dừng</option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Đánh giá (sao)</label>
            <div class="flex items-center gap-1.5 pt-1.5">
              <button
                v-for="star in 5"
                :key="star"
                type="button"
                class="p-1 hover:scale-110 transition-transform"
                @click="bookForm.rating = (bookForm.rating === star ? null : star)"
              >
                <Star
                  :class="[
                    'w-5 h-5 transition-colors',
                    bookForm.rating && bookForm.rating >= star
                      ? 'fill-amber-400 text-amber-400'
                      : 'text-slate-300 hover:text-amber-300',
                  ]"
                />
              </button>
              <span v-if="bookForm.rating" class="text-xs font-bold text-amber-600 ml-2">
                {{ bookForm.rating }} / 5 sao
              </span>
            </div>
          </div>
        </div>

        <AppTextarea
          id="book-description"
          v-model="bookForm.description"
          label="Mô tả tổng quan / Tóm tắt nội dung sách"
          rows="3"
          placeholder="Ghi chú ngắn về nội dung, giá trị cuốn sách mang lại..."
        />

        <div class="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showBookModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="savingBook">
            {{ isEditingBook ? 'Lưu thay đổi' : 'Thêm vào tủ sách' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- MODAL 2: CẬP NHẬT TIẾN ĐỘ ĐỌC NHANH -->
    <AppModal v-model="showProgressModal" title="Cập nhật tiến độ đọc" size="md">
      <form v-if="selectedBookForProgress" class="space-y-4" @submit.prevent="saveQuickProgress">
        <div class="p-3 bg-slate-50 rounded-lg space-y-1">
          <h4 class="text-sm font-bold text-slate-900">{{ selectedBookForProgress.title }}</h4>
          <p class="text-xs text-slate-500">Tổng số trang: {{ selectedBookForProgress.totalPages || 'Chưa thiết lập' }}</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            id="progress-current-page"
            v-model.number="progressForm.currentPage"
            label="Trang hiện tại vừa đọc tới"
            type="number"
            min="0"
            required
          />

          <AppInput
            id="progress-pages-today"
            v-model.number="progressForm.pagesReadToday"
            label="Số trang đọc hôm nay"
            type="number"
            min="0"
            placeholder="Số trang vừa đọc xong"
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
            <option value="ON_HOLD">Tạm dừng</option>
          </select>
        </div>

        <div v-if="progressForm.status === 'COMPLETED'" class="space-y-2 p-3 bg-emerald-50/60 rounded-lg border border-emerald-100">
          <label class="block text-xs font-bold text-emerald-800">Đánh giá cuốn sách khi hoàn thành</label>
          <div class="flex items-center gap-1.5">
            <button
              v-for="star in 5"
              :key="star"
              type="button"
              class="p-1 hover:scale-110 transition-transform"
              @click="progressForm.rating = star"
            >
              <Star
                :class="[
                  'w-5 h-5 transition-colors',
                  progressForm.rating && progressForm.rating >= star
                    ? 'fill-amber-400 text-amber-400'
                    : 'text-slate-300',
                ]"
              />
            </button>
          </div>
        </div>

        <div class="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showProgressModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="savingProgress">
            Lưu tiến độ
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- MODAL 3: THÊM / SỬA CÂU TRIẾT LÝ & TRÍCH DẪN -->
    <AppModal v-model="showQuoteModal" :title="isEditingQuote ? 'Sửa câu triết lý' : 'Lưu câu triết lý / Trích dẫn hay'" size="lg">
      <form class="space-y-4" @submit.prevent="saveQuote">
        <div v-if="!isEditingQuote">
          <label class="block text-xs font-bold text-slate-700 mb-1">Thuộc cuốn sách nào</label>
          <select
            v-model="quoteForm.bookId"
            class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            required
          >
            <option :value="null" disabled>-- Chọn cuốn sách --</option>
            <option v-for="b in books" :key="b.id" :value="b.id">{{ b.title }} ({{ b.author || 'Tác giả khác' }})</option>
          </select>
        </div>

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
            id="quote-page"
            v-model.number="quoteForm.pageNumber"
            label="Trang số"
            type="number"
            min="1"
            placeholder="Ví dụ: 45"
          />

          <AppInput
            id="quote-chapter"
            v-model="quoteForm.chapter"
            label="Chương / Mục"
            placeholder="Ví dụ: Chương 3: Tư duy tích cực"
          />
        </div>

        <AppTextarea
          id="quote-note"
          v-model="quoteForm.note"
          label="Bài học rút ra / Cảm nghĩ cá nhân (Áp dụng cho cuộc sống)"
          rows="2"
          placeholder="Ý nghĩa của câu nói đối với bạn, bài học thực tế áp dụng..."
        />

        <div class="flex items-center gap-2 pt-1">
          <input
            id="quote-fav"
            v-model="quoteForm.isFavorite"
            type="checkbox"
            class="w-4 h-4 rounded text-brand-600 border-slate-300 focus:ring-brand-500"
          />
          <label for="quote-fav" class="text-xs font-bold text-slate-700 cursor-pointer select-none inline-flex items-center gap-1.5">
            <span>Đánh dấu là câu triết lý tâm đắc nhất (Yêu thích)</span>
            <Star class="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
          </label>
        </div>

        <div class="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showQuoteModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="savingQuote">
            {{ isEditingQuote ? 'Lưu thay đổi' : 'Lưu trích dẫn' }}
          </AppButton>
        </div>
      </form>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { bookService } from '../../services/book.service';
import type { Book, BookQuote, ReadingStats, BookStatus } from '../../types/book.types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppTextarea from '../../components/common/AppTextarea.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  BookMarked,
  BookOpen,
  Quote,
  Plus,
  Search,
  Star,
  Hourglass,
  FileText,
  Sparkles,
  TrendingUp,
  Edit2,
  Trash2,
  ArrowRight,
  Lightbulb,
  Eye,
} from 'lucide-vue-next';

const router = useRouter();

function goToBookDetail(id: number) {
  router.push(`/reading/${id}`);
}

const activeTab = ref<'books' | 'quotes'>('books');

// Stats
const stats = reactive<ReadingStats>({
  totalBooks: 0,
  completedBooks: 0,
  readingBooks: 0,
  wantToReadBooks: 0,
  onHoldBooks: 0,
  totalPagesRead: 0,
  totalQuotes: 0,
  favoriteQuotes: 0,
  categories: [],
  booksByCategory: {},
  recentBooks: [],
});

// Books State
const books = ref<Book[]>([]);
const loadingBooks = ref(false);
const searchKeyword = ref('');
const selectedStatus = ref('');
const selectedCategory = ref('');

// Quotes State
const quotes = ref<BookQuote[]>([]);
const loadingQuotes = ref(false);
const quoteKeyword = ref('');
const filterOnlyFavorite = ref(false);

// Modals
const showBookModal = ref(false);
const isEditingBook = ref(false);
const editingBookId = ref<number | null>(null);
const savingBook = ref(false);

const bookForm = reactive({
  title: '',
  author: '',
  category: '',
  description: '',
  totalPages: 0,
  currentPage: 0,
  status: 'WANT_TO_READ' as BookStatus,
  rating: null as number | null,
});

// Quick Progress Modal
const showProgressModal = ref(false);
const selectedBookForProgress = ref<Book | null>(null);
const savingProgress = ref(false);
const progressForm = reactive({
  currentPage: 0,
  pagesReadToday: 0,
  status: 'READING' as BookStatus,
  rating: null as number | null,
});

// Quote Modal
const showQuoteModal = ref(false);
const isEditingQuote = ref(false);
const editingQuoteId = ref<number | null>(null);
const savingQuote = ref(false);
const quoteForm = reactive({
  bookId: null as number | null,
  quoteText: '',
  pageNumber: undefined as number | undefined,
  chapter: '',
  note: '',
  isFavorite: false,
});

const statusFilters = [
  { label: 'Tất cả', value: '' },
  { label: 'Đang đọc', value: 'READING' },
  { label: 'Đã xong', value: 'COMPLETED' },
  { label: 'Dự định', value: 'WANT_TO_READ' },
  { label: 'Tạm dừng', value: 'ON_HOLD' },
];

onMounted(() => {
  loadStats();
  loadBooks();
  loadQuotes();
});

async function loadStats() {
  try {
    const data = await bookService.getStats();
    Object.assign(stats, data);
  } catch (err) {
    console.error('Failed to load reading stats', err);
  }
}

async function loadBooks() {
  loadingBooks.value = true;
  try {
    const res = await bookService.getBooks({
      status: selectedStatus.value || undefined,
      category: selectedCategory.value || undefined,
      keyword: searchKeyword.value.trim() || undefined,
      page: 0,
      size: 50,
    });
    books.value = res.items;
  } catch (err) {
    console.error('Failed to load books', err);
  } finally {
    loadingBooks.value = false;
  }
}

async function loadQuotes() {
  loadingQuotes.value = true;
  try {
    const res = await bookService.getAllQuotes({
      isFavorite: filterOnlyFavorite.value ? true : undefined,
      keyword: quoteKeyword.value.trim() || undefined,
      page: 0,
      size: 50,
    });
    quotes.value = res.items;
  } catch (err) {
    console.error('Failed to load quotes', err);
  } finally {
    loadingQuotes.value = false;
  }
}

let searchTimer: any = null;
function debounceSearch() {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    loadBooks();
  }, 300);
}

let quoteTimer: any = null;
function debounceSearchQuotes() {
  clearTimeout(quoteTimer);
  quoteTimer = setTimeout(() => {
    loadQuotes();
  }, 300);
}

function setStatusFilter(val: string) {
  selectedStatus.value = val;
  loadBooks();
}

function toggleOnlyFavorite() {
  filterOnlyFavorite.value = !filterOnlyFavorite.value;
  loadQuotes();
}

// Book Modal Actions
function openAddBookModal() {
  isEditingBook.value = false;
  editingBookId.value = null;
  bookForm.title = '';
  bookForm.author = '';
  bookForm.category = '';
  bookForm.description = '';
  bookForm.totalPages = 0;
  bookForm.currentPage = 0;
  bookForm.status = 'WANT_TO_READ';
  bookForm.rating = null;
  showBookModal.value = true;
}

function openEditBookModal(book: Book) {
  isEditingBook.value = true;
  editingBookId.value = book.id;
  bookForm.title = book.title;
  bookForm.author = book.author || '';
  bookForm.category = book.category || '';
  bookForm.description = book.description || '';
  bookForm.totalPages = book.totalPages || 0;
  bookForm.currentPage = book.currentPage || 0;
  bookForm.status = book.status;
  bookForm.rating = book.rating || null;
  showBookModal.value = true;
}

async function saveBook() {
  if (!bookForm.title.trim()) return;
  savingBook.value = true;
  try {
    if (isEditingBook.value && editingBookId.value) {
      await bookService.updateBook(editingBookId.value, {
        title: bookForm.title,
        author: bookForm.author || undefined,
        category: bookForm.category || undefined,
        description: bookForm.description || undefined,
        totalPages: bookForm.totalPages,
        currentPage: bookForm.currentPage,
        status: bookForm.status,
        rating: bookForm.rating || undefined,
      });
    } else {
      await bookService.createBook({
        title: bookForm.title,
        author: bookForm.author || undefined,
        category: bookForm.category || undefined,
        description: bookForm.description || undefined,
        totalPages: bookForm.totalPages,
        currentPage: bookForm.currentPage,
        status: bookForm.status,
        rating: bookForm.rating || undefined,
      });
    }
    showBookModal.value = false;
    await Promise.all([loadBooks(), loadStats()]);
  } catch (err) {
    console.error('Failed to save book', err);
  } finally {
    savingBook.value = false;
  }
}

async function confirmDeleteBook(book: Book) {
  if (!confirm(`Bạn có chắc chắn muốn xóa cuốn sách "${book.title}" và toàn bộ trích dẫn của nó?`)) return;
  try {
    await bookService.deleteBook(book.id);
    await Promise.all([loadBooks(), loadStats(), loadQuotes()]);
  } catch (err) {
    console.error('Failed to delete book', err);
  }
}

// Quick Progress
function openQuickProgressModal(book: Book) {
  selectedBookForProgress.value = book;
  progressForm.currentPage = book.currentPage || 0;
  progressForm.pagesReadToday = 0;
  progressForm.status = book.status;
  progressForm.rating = book.rating || null;
  showProgressModal.value = true;
}

async function saveQuickProgress() {
  if (!selectedBookForProgress.value) return;
  savingProgress.value = true;
  try {
    await bookService.updateProgress(selectedBookForProgress.value.id, {
      currentPage: progressForm.currentPage,
      pagesReadToday: progressForm.pagesReadToday > 0 ? progressForm.pagesReadToday : undefined,
      status: progressForm.status,
      rating: progressForm.rating || undefined,
    });
    showProgressModal.value = false;
    await Promise.all([loadBooks(), loadStats()]);
  } catch (err) {
    console.error('Failed to update progress', err);
  } finally {
    savingProgress.value = false;
  }
}

// Quote Modal Actions
function openAddQuoteModal(defaultBookId: number | null) {
  isEditingQuote.value = false;
  editingQuoteId.value = null;
  quoteForm.bookId = defaultBookId || (books.value.length > 0 ? books.value[0].id : null);
  quoteForm.quoteText = '';
  quoteForm.pageNumber = undefined;
  quoteForm.chapter = '';
  quoteForm.note = '';
  quoteForm.isFavorite = false;
  showQuoteModal.value = true;
}

function openEditQuoteModal(quote: BookQuote) {
  isEditingQuote.value = true;
  editingQuoteId.value = quote.id;
  quoteForm.bookId = quote.bookId;
  quoteForm.quoteText = quote.quoteText;
  quoteForm.pageNumber = quote.pageNumber ?? undefined;
  quoteForm.chapter = quote.chapter || '';
  quoteForm.note = quote.note || '';
  quoteForm.isFavorite = quote.isFavorite;
  showQuoteModal.value = true;
}

async function saveQuote() {
  if (!quoteForm.quoteText.trim()) return;
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
    } else if (quoteForm.bookId) {
      await bookService.addQuote(quoteForm.bookId, {
        quoteText: quoteForm.quoteText,
        pageNumber: quoteForm.pageNumber || undefined,
        chapter: quoteForm.chapter || undefined,
        note: quoteForm.note || undefined,
        isFavorite: quoteForm.isFavorite,
      });
    }
    showQuoteModal.value = false;
    await Promise.all([loadQuotes(), loadStats(), loadBooks()]);
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
    await loadStats();
  } catch (err) {
    console.error('Failed to toggle favorite quote', err);
  }
}

async function deleteQuote(id: number) {
  if (!confirm('Bạn có chắc chắn muốn xóa câu trích dẫn này?')) return;
  try {
    await bookService.deleteQuote(id);
    await Promise.all([loadQuotes(), loadStats(), loadBooks()]);
  } catch (err) {
    console.error('Failed to delete quote', err);
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
    case 'READING': return 'bg-amber-100 text-amber-800 border border-amber-200';
    case 'COMPLETED': return 'bg-emerald-100 text-emerald-800 border border-emerald-200';
    case 'WANT_TO_READ': return 'bg-sky-100 text-sky-800 border border-sky-200';
    case 'ON_HOLD': return 'bg-slate-100 text-slate-700 border border-slate-200';
    default: return 'bg-slate-100 text-slate-700';
  }
}

function formatDate(d: string) {
  if (!d) return '';
  const dt = new Date(d);
  return dt.toLocaleDateString('vi-VN');
}
</script>
