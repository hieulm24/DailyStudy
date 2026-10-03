<template>
  <div class="space-y-6">
    <!-- Header Banner -->
    <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 shrink-0 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center border border-brand-100/60 shadow-xs">
          <FolderArchive class="w-7 h-7" />
        </div>
        <div>
          <h2 class="text-xl sm:text-2xl lg:text-3xl font-bold text-slate-900 tracking-tight">
            Kho tài liệu & Liên kết (Resource Hub)
          </h2>
          <p class="text-sm sm:text-base text-slate-500 mt-1">
            Lưu trữ tài liệu Word, Excel, PDF, đề thi TOEIC và bookmark các trang web học tập hữu ích
          </p>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex flex-wrap items-center gap-2.5 shrink-0">
        <AppButton variant="secondary" size="md" :icon="LinkIcon" @click="openAddLinkModal">
          + Thêm liên kết
        </AppButton>
        <AppButton variant="primary" size="md" :icon="Upload" @click="openUploadModal">
          + Tải lên tài liệu
        </AppButton>
      </div>
    </div>

    <!-- Main Navigation Tabs -->
    <div class="flex items-center gap-2 border-b border-slate-200 pb-2">
      <button
        type="button"
        :class="[
          'flex items-center gap-2 px-4 py-2.5 text-sm font-semibold rounded-md transition-all',
          activeTab === 'DOCUMENTS'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
        ]"
        @click="activeTab = 'DOCUMENTS'"
      >
        <Files class="w-4 h-4" />
        <span>Tệp tài liệu (Files)</span>
        <span
          :class="[
            'px-2 py-0.5 text-xs rounded-full font-bold',
            activeTab === 'DOCUMENTS' ? 'bg-white/20 text-white' : 'bg-slate-200 text-slate-700',
          ]"
        >
          {{ docStats?.totalDocuments || 0 }}
        </span>
      </button>

      <button
        type="button"
        :class="[
          'flex items-center gap-2 px-4 py-2.5 text-sm font-semibold rounded-md transition-all',
          activeTab === 'LINKS'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900',
        ]"
        @click="activeTab = 'LINKS'"
      >
        <Globe class="w-4 h-4" />
        <span>Liên kết học tập (Links)</span>
        <span
          :class="[
            'px-2 py-0.5 text-xs rounded-full font-bold',
            activeTab === 'LINKS' ? 'bg-white/20 text-white' : 'bg-slate-200 text-slate-700',
          ]"
        >
          {{ linksTotalElements }}
        </span>
      </button>
    </div>

    <!-- ============================================================ -->
    <!-- TAB 1: DOCUMENTS (FILES) -->
    <!-- ============================================================ -->
    <!-- ============================================================ -->
    <!-- TAB 1: DOCUMENTS (FILES) -->
    <!-- ============================================================ -->
    <div v-if="activeTab === 'DOCUMENTS'" class="space-y-6">
      <!-- Statistics Counters Grid -->
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
        <!-- Tổng tài liệu -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-brand-300 hover:shadow-sm transition-all min-h-[115px]">
          <div class="flex items-center justify-between">
            <span class="text-sm font-bold text-slate-700">Tổng Tài Liệu</span>
            <div class="w-9 h-9 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center border border-brand-100/60">
              <Files class="w-5 h-5" />
            </div>
          </div>
          <div class="mt-2">
            <div class="text-3xl sm:text-4xl font-extrabold text-brand-600 tracking-tight">{{ docStats?.totalDocuments || 0 }}</div>
            <span class="text-xs sm:text-sm text-slate-400 mt-1 block">tệp đã tải lên</span>
          </div>
        </div>

        <!-- Dung lượng dùng -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-indigo-300 hover:shadow-sm transition-all min-h-[115px]">
          <div class="flex items-center justify-between">
            <span class="text-sm font-bold text-slate-700">Dung Lượng Dùng</span>
            <div class="w-9 h-9 rounded-md bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-100/60">
              <HardDrive class="w-5 h-5" />
            </div>
          </div>
          <div class="mt-2">
            <div class="text-3xl sm:text-4xl font-extrabold text-indigo-600 tracking-tight truncate">{{ docStats?.formattedTotalSize || '0 B' }}</div>
            <span class="text-xs sm:text-sm text-slate-400 mt-1 block">trên máy chủ</span>
          </div>
        </div>

        <!-- File Excel -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-emerald-300 hover:shadow-sm transition-all min-h-[115px]">
          <div class="flex items-center justify-between">
            <span class="text-sm font-bold text-slate-700">File Excel</span>
            <div class="w-9 h-9 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-100/60">
              <FileSpreadsheet class="w-5 h-5" />
            </div>
          </div>
          <div class="mt-2">
            <div class="text-3xl sm:text-4xl font-extrabold text-emerald-600 tracking-tight">{{ docStats?.totalExcel || 0 }}</div>
            <span class="text-xs sm:text-sm text-slate-400 mt-1 block">.xlsx, .xls, .csv</span>
          </div>
        </div>

        <!-- File Word -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-blue-300 hover:shadow-sm transition-all min-h-[115px]">
          <div class="flex items-center justify-between">
            <span class="text-sm font-bold text-slate-700">File Word</span>
            <div class="w-9 h-9 rounded-md bg-blue-50 text-blue-600 flex items-center justify-center border border-blue-100/60">
              <FileText class="w-5 h-5" />
            </div>
          </div>
          <div class="mt-2">
            <div class="text-3xl sm:text-4xl font-extrabold text-blue-600 tracking-tight">{{ docStats?.totalWord || 0 }}</div>
            <span class="text-xs sm:text-sm text-slate-400 mt-1 block">.docx, .doc</span>
          </div>
        </div>

        <!-- File PDF -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-rose-300 hover:shadow-sm transition-all min-h-[115px]">
          <div class="flex items-center justify-between">
            <span class="text-sm font-bold text-slate-700">File PDF</span>
            <div class="w-9 h-9 rounded-md bg-rose-50 text-rose-600 flex items-center justify-center border border-rose-100/60">
              <FileCode class="w-5 h-5" />
            </div>
          </div>
          <div class="mt-2">
            <div class="text-3xl sm:text-4xl font-extrabold text-rose-600 tracking-tight">{{ docStats?.totalPdf || 0 }}</div>
            <span class="text-xs sm:text-sm text-slate-400 mt-1 block">đề thi, ebook</span>
          </div>
        </div>

        <!-- Yêu Thích -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-amber-300 hover:shadow-sm transition-all min-h-[115px]">
          <div class="flex items-center justify-between">
            <span class="text-sm font-bold text-slate-700">Yêu Thích</span>
            <div class="w-9 h-9 rounded-md bg-amber-50 text-amber-600 flex items-center justify-center border border-amber-100/60">
              <Star class="w-5 h-5 fill-amber-500 text-amber-500" />
            </div>
          </div>
          <div class="mt-2">
            <div class="text-3xl sm:text-4xl font-extrabold text-amber-500 tracking-tight">{{ docStats?.totalFavorites || 0 }}</div>
            <span class="text-xs sm:text-sm text-slate-400 mt-1 block">đã đánh dấu sao</span>
          </div>
        </div>
      </div>

      <!-- Filters & View Toggle Bar -->
      <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs space-y-4">
        <!-- Top row: Search + Actions -->
        <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <!-- Search input -->
          <div class="flex-1 max-w-lg">
            <AppSearch
              v-model="docFilter.search"
              placeholder="Tìm theo tên tài liệu, tên file gốc, mô tả..."
              @search="() => loadDocuments(0)"
            />
          </div>

          <!-- Right Controls -->
          <div class="flex flex-wrap items-center gap-3">
            <!-- Favorite toggle filter -->
            <button
              type="button"
              :class="[
                'inline-flex items-center gap-2 px-4 py-2.5 text-sm font-semibold rounded-md border transition-all shadow-xs',
                docFilter.isFavorite
                  ? 'bg-amber-50 text-amber-800 border-amber-300 ring-1 ring-amber-300'
                  : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50',
              ]"
              @click="toggleFavoriteFilter"
            >
              <Star :class="['w-4.5 h-4.5', docFilter.isFavorite ? 'fill-amber-500 text-amber-500' : 'text-slate-400']" />
              <span>Đã gắn sao</span>
            </button>

            <!-- Sort By -->
            <div class="flex items-center gap-2">
              <span class="text-xs font-semibold text-slate-500 hidden sm:inline">Sắp xếp:</span>
              <select
                v-model="docFilter.sortBy"
                class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3.5 py-2.5 shadow-xs focus:outline-none focus:border-brand-500 font-medium"
                @change="() => loadDocuments(0)"
              >
                <option value="createdAt">Mới nhất</option>
                <option value="title">Tên (A-Z)</option>
                <option value="fileSize">Dung lượng</option>
                <option value="downloadCount">Lượt tải</option>
              </select>
            </div>

            <!-- View Toggle -->
            <div class="inline-flex rounded-md border border-slate-200 bg-slate-50 p-1">
              <button
                type="button"
                :class="[
                  'px-3 py-1.5 rounded-sm text-xs font-bold transition-all flex items-center gap-1.5',
                  viewMode === 'GRID'
                    ? 'bg-white text-brand-600 shadow-xs border border-slate-200/80'
                    : 'text-slate-500 hover:text-slate-900',
                ]"
                title="Dạng lưới thẻ"
                @click="viewMode = 'GRID'"
              >
                <LayoutGrid class="w-4 h-4" />
                <span class="hidden sm:inline">Lưới</span>
              </button>
              <button
                type="button"
                :class="[
                  'px-3 py-1.5 rounded-sm text-xs font-bold transition-all flex items-center gap-1.5',
                  viewMode === 'TABLE'
                    ? 'bg-white text-brand-600 shadow-xs border border-slate-200/80'
                    : 'text-slate-500 hover:text-slate-900',
                ]"
                title="Dạng bảng danh sách"
                @click="viewMode = 'TABLE'"
              >
                <List class="w-4 h-4" />
                <span class="hidden sm:inline">Bảng</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Bottom row: Categories & File Types filters -->
        <div class="pt-3 border-t border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div class="flex flex-wrap items-center gap-3">
            <!-- Category Filter -->
            <div class="flex items-center gap-2">
              <span class="text-xs font-semibold text-slate-500">Danh mục:</span>
              <select
                v-model="docFilter.category"
                class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3.5 py-2 shadow-xs focus:outline-none focus:border-brand-500 font-medium"
                @change="() => loadDocuments(0)"
              >
                <option value="">Tất cả danh mục</option>
                <option value="TOEIC">Đề thi & Tài liệu TOEIC</option>
                <option value="VOCABULARY">Từ vựng (Vocabulary)</option>
                <option value="GRAMMAR">Ngữ pháp (Grammar)</option>
                <option value="LISTENING">Luyện nghe (Listening)</option>
                <option value="SPEAKING">Luyện nói (Speaking)</option>
                <option value="TEST_EXAM">Đề thi thử (Practice Test)</option>
                <option value="GENERAL">Tài liệu chung</option>
                <option value="OTHER">Khác</option>
              </select>
            </div>

            <!-- File Type Filter -->
            <div class="flex items-center gap-2">
              <span class="text-xs font-semibold text-slate-500">Định dạng:</span>
              <select
                v-model="docFilter.fileType"
                class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3.5 py-2 shadow-xs focus:outline-none focus:border-brand-500 font-medium"
                @change="() => loadDocuments(0)"
              >
                <option value="">Tất cả định dạng</option>
                <option value="EXCEL">Excel (.xlsx, .xls, .csv)</option>
                <option value="WORD">Word (.docx, .doc)</option>
                <option value="PDF">PDF (.pdf)</option>
                <option value="POWERPOINT">PowerPoint (.pptx)</option>
                <option value="IMAGE">Hình ảnh (.png, .jpg)</option>
                <option value="TEXT">Văn bản (.txt)</option>
                <option value="OTHER">Khác</option>
              </select>
            </div>
          </div>

          <!-- Total indicator -->
          <div class="text-xs text-slate-500 font-medium">
            Hiển thị <strong class="text-slate-800">{{ documents.length }}</strong> tài liệu
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoadingDocs" class="py-16 text-center text-slate-400">
        <Loader2 class="w-8 h-8 animate-spin mx-auto text-brand-600 mb-2" />
        <p class="text-sm">Đang tải danh sách tài liệu...</p>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="documents.length === 0"
        class="bg-white p-12 text-center rounded-md border border-slate-200 shadow-xs space-y-3"
      >
        <div class="w-16 h-16 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto">
          <FolderArchive class="w-8 h-8" />
        </div>
        <h3 class="text-base font-bold text-slate-800">Chưa có tài liệu nào trong kho</h3>
        <p class="text-sm text-slate-500 max-w-md mx-auto">
          Tải lên các file Excel danh sách từ vựng, tài liệu Word ngữ pháp, PDF đề thi TOEIC để ôn tập và tra cứu bất cứ lúc nào.
        </p>
        <AppButton variant="primary" size="md" :icon="Upload" class="mt-2" @click="openUploadModal">
          + Tải lên tài liệu đầu tiên
        </AppButton>
      </div>

      <!-- GRID VIEW -->
      <div v-else-if="viewMode === 'GRID'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="doc in documents"
          :key="doc.id"
          class="bg-white p-5 rounded-md border border-slate-200 hover:border-brand-300 hover:shadow-sm transition-all flex flex-col justify-between group"
        >
          <div>
            <!-- Card Header: Icon + Category Badge + Star -->
            <div class="flex items-start justify-between gap-3 mb-3">
              <div class="flex items-center gap-3">
                <div :class="['w-12 h-12 rounded-md flex items-center justify-center border shrink-0', getFileTypeStyles(doc.fileType).bg]">
                  <component :is="getFileTypeStyles(doc.fileType).icon" :class="['w-6 h-6', getFileTypeStyles(doc.fileType).color]" />
                </div>
                <div>
                  <span class="inline-block px-2 py-0.5 text-xs font-semibold rounded-md bg-slate-100 text-slate-700">
                    {{ getCategoryLabel(doc.category) }}
                  </span>
                  <span class="text-xs text-slate-400 block mt-0.5">{{ doc.formattedFileSize }}</span>
                </div>
              </div>

              <!-- Favorite Toggle -->
              <button
                type="button"
                class="p-1 text-slate-300 hover:text-amber-500 transition-colors"
                :title="doc.isFavorite ? 'Bỏ yêu thích' : 'Đánh dấu yêu thích'"
                @click="toggleFavoriteDoc(doc)"
              >
                <Star :class="['w-5 h-5', doc.isFavorite ? 'fill-amber-500 text-amber-500' : '']" />
              </button>
            </div>

            <!-- Title & Filename -->
            <h4 class="text-base font-bold text-slate-900 line-clamp-2 group-hover:text-brand-600 transition-colors" :title="doc.title">
              {{ doc.title }}
            </h4>
            <p class="text-xs text-slate-500 mt-1 truncate font-mono" :title="doc.fileName">
              📄 {{ doc.fileName }}
            </p>

            <!-- Description if any -->
            <p v-if="doc.description" class="text-xs text-slate-600 mt-2 line-clamp-2 bg-slate-50 p-2 rounded-xs border border-slate-100">
              {{ doc.description }}
            </p>
          </div>

          <!-- Card Footer & Actions -->
          <div class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
            <span :title="formatDate(doc.createdAt)">📅 {{ formatDateShort(doc.createdAt) }}</span>
            <div class="flex items-center gap-1.5">
              <!-- Preview button if PDF or Image -->
              <a
                v-if="doc.fileType === 'PDF' || doc.fileType === 'IMAGE'"
                :href="documentService.getPreviewUrl(doc.id)"
                target="_blank"
                class="p-1.5 rounded-md hover:bg-slate-100 text-slate-600 transition-colors"
                title="Xem trước"
              >
                <Eye class="w-4 h-4" />
              </a>

              <!-- Edit button -->
              <button
                type="button"
                class="p-1.5 rounded-md hover:bg-slate-100 text-slate-600 transition-colors"
                title="Chỉnh sửa thông tin"
                @click="openEditDocModal(doc)"
              >
                <Edit2 class="w-4 h-4" />
              </button>

              <!-- Delete button -->
              <button
                type="button"
                class="p-1.5 rounded-md hover:bg-rose-50 text-slate-400 hover:text-rose-600 transition-colors"
                title="Xóa tệp"
                @click="confirmDeleteDoc(doc)"
              >
                <Trash2 class="w-4 h-4" />
              </button>

              <!-- Download Button -->
              <button
                type="button"
                class="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold rounded-md bg-brand-50 text-brand-700 hover:bg-brand-600 hover:text-white transition-all shadow-xs"
                @click="downloadDoc(doc)"
              >
                <Download class="w-3.5 h-3.5" />
                <span>Tải về</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- TABLE VIEW -->
      <div v-else class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left text-sm text-slate-700">
            <thead class="bg-slate-50/80 border-b border-slate-200 text-xs text-slate-500 font-semibold uppercase">
              <tr>
                <th class="py-3.5 px-3 w-12 text-center">STT</th>
                <th class="py-3.5 px-3 w-10 text-center">
                  <Star class="w-3.5 h-3.5 mx-auto text-amber-500 fill-amber-500" />
                </th>
                <th class="py-3.5 px-4">Tài liệu</th>
                <th class="py-3.5 px-4">Định dạng</th>
                <th class="py-3.5 px-4">Danh mục</th>
                <th class="py-3.5 px-4 text-right">Dung lượng</th>
                <th class="py-3.5 px-4 text-center">Lượt tải</th>
                <th class="py-3.5 px-4">Ngày tải</th>
                <th class="py-3.5 px-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="(doc, index) in documents" :key="doc.id" class="hover:bg-slate-50/70 transition-colors">
                <td class="py-3 px-3 text-center font-bold text-slate-400 font-mono text-xs">
                  #{{ docPage * docPageSize + index + 1 }}
                </td>
                <td class="py-3 px-4 text-center">
                  <button type="button" @click="toggleFavoriteDoc(doc)">
                    <Star :class="['w-4 h-4 mx-auto', doc.isFavorite ? 'fill-amber-500 text-amber-500' : 'text-slate-300']" />
                  </button>
                </td>
                <td class="py-3 px-4">
                  <div class="font-bold text-slate-900 hover:text-brand-600 cursor-pointer" @click="downloadDoc(doc)">
                    {{ doc.title }}
                  </div>
                  <div class="text-xs text-slate-400 font-mono truncate max-w-xs">{{ doc.fileName }}</div>
                </td>
                <td class="py-3 px-4">
                  <span :class="['inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-xs font-semibold border', getFileTypeStyles(doc.fileType).bg, getFileTypeStyles(doc.fileType).color]">
                    {{ doc.fileType }}
                  </span>
                </td>
                <td class="py-3 px-4">
                  <span class="px-2 py-0.5 rounded-md text-xs font-medium bg-slate-100 text-slate-700">
                    {{ getCategoryLabel(doc.category) }}
                  </span>
                </td>
                <td class="py-3 px-4 text-right font-mono text-xs">{{ doc.formattedFileSize }}</td>
                <td class="py-3 px-4 text-center text-xs font-semibold text-slate-600">{{ doc.downloadCount }}</td>
                <td class="py-3 px-4 text-xs text-slate-500">{{ formatDateShort(doc.createdAt) }}</td>
                <td class="py-3 px-4 text-right">
                  <div class="flex items-center justify-end gap-1">
                    <a
                      v-if="doc.fileType === 'PDF' || doc.fileType === 'IMAGE'"
                      :href="documentService.getPreviewUrl(doc.id)"
                      target="_blank"
                      class="p-1.5 rounded-md hover:bg-slate-100 text-slate-600"
                      title="Xem trước"
                    >
                      <Eye class="w-4 h-4" />
                    </a>
                    <button
                      type="button"
                      class="p-1.5 rounded-md hover:bg-slate-100 text-slate-600"
                      title="Sửa"
                      @click="openEditDocModal(doc)"
                    >
                      <Edit2 class="w-4 h-4" />
                    </button>
                    <button
                      type="button"
                      class="p-1.5 rounded-md hover:bg-rose-50 text-slate-400 hover:text-rose-600"
                      title="Xóa"
                      @click="confirmDeleteDoc(doc)"
                    >
                      <Trash2 class="w-4 h-4" />
                    </button>
                    <button
                      type="button"
                      class="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-semibold rounded-md bg-brand-50 text-brand-700 hover:bg-brand-600 hover:text-white transition-colors"
                      @click="downloadDoc(doc)"
                    >
                      <Download class="w-3.5 h-3.5" />
                      <span>Tải</span>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Documents Pagination -->
      <div v-if="documents.length > 0" class="bg-white rounded-md border border-slate-200 shadow-xs">
        <AppPagination
          :current-page="docPage"
          :total-pages="docTotalPages"
          :page-size="docPageSize"
          :total-elements="docTotalElements"
          @update:page="handleDocPageChange"
        />
      </div>
    </div>

    <!-- ============================================================ -->
    <!-- TAB 2: STUDY LINKS (BOOKMARKS) -->
    <!-- ============================================================ -->
    <div v-else class="space-y-5">
      <!-- Filter Bar for Links -->
      <div class="bg-white p-4.5 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div class="flex-1 max-w-md">
          <AppSearch v-model="linkFilter.search" placeholder="Tìm theo tên trang web, URL..." @search="() => loadLinks(0)" />
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <select
            v-model="linkFilter.category"
            class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
            @change="() => loadLinks(0)"
          >
            <option value="">Mọi danh mục</option>
            <option value="DICTIONARY">Từ điển online (Oxford, Cambridge...)</option>
            <option value="TOEIC">Đề thi & Luyện TOEIC</option>
            <option value="LISTENING">Kênh Luyện nghe (Youtube, Podcast)</option>
            <option value="GRAMMAR">Ngữ pháp & Bài tập</option>
            <option value="READING">Đọc báo & Tin tức tiếng Anh</option>
            <option value="GENERAL">Trang web chung</option>
          </select>

          <button
            type="button"
            :class="[
              'inline-flex items-center gap-1.5 px-3 py-2 text-sm font-medium rounded-md border transition-colors shadow-xs',
              linkFilter.isFavorite
                ? 'bg-amber-50 text-amber-700 border-amber-300'
                : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
            ]"
            @click="toggleFavoriteLinkFilter"
          >
            <Star :class="['w-4 h-4', linkFilter.isFavorite ? 'fill-amber-500 text-amber-500' : 'text-slate-400']" />
            <span>Đã ghim</span>
          </button>
        </div>
      </div>

      <!-- Loading Links -->
      <div v-if="isLoadingLinks" class="py-16 text-center text-slate-400">
        <Loader2 class="w-8 h-8 animate-spin mx-auto text-brand-600 mb-2" />
        <p class="text-sm">Đang tải danh sách liên kết...</p>
      </div>

      <!-- Empty Links -->
      <div
        v-else-if="studyLinks.length === 0"
        class="bg-white p-12 text-center rounded-md border border-slate-200 shadow-xs space-y-3"
      >
        <div class="w-16 h-16 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto">
          <Globe class="w-8 h-8" />
        </div>
        <h3 class="text-base font-bold text-slate-800">Chưa lưu đường dẫn học tập nào</h3>
        <p class="text-sm text-slate-500 max-w-md mx-auto">
          Lưu lại các website luyện đề TOEIC, kênh Youtube luyện nghe hay từ điển trực tuyến để mở nhanh chỉ với 1 click.
        </p>
        <AppButton variant="primary" size="md" :icon="LinkIcon" class="mt-2" @click="openAddLinkModal">
          + Thêm liên kết đầu tiên
        </AppButton>
      </div>

      <!-- Links Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="link in studyLinks"
          :key="link.id"
          class="bg-white p-5 rounded-md border border-slate-200 hover:border-brand-300 hover:shadow-sm transition-all flex flex-col justify-between group"
        >
          <div>
            <!-- Header: Globe icon + Category + Star -->
            <div class="flex items-start justify-between gap-3 mb-2.5">
              <div class="flex items-center gap-3">
                <div class="w-11 h-11 rounded-md bg-sky-50 text-sky-600 flex items-center justify-center border border-sky-100 shrink-0">
                  <Globe class="w-5 h-5" />
                </div>
                <div>
                  <span class="inline-block px-2 py-0.5 text-xs font-semibold rounded-md bg-slate-100 text-slate-700">
                    {{ getLinkCategoryLabel(link.category) }}
                  </span>
                  <span class="text-xs text-slate-400 block mt-0.5">Đã mở: {{ link.clickCount }} lần</span>
                </div>
              </div>

              <!-- Star Toggle -->
              <button
                type="button"
                class="p-1 text-slate-300 hover:text-amber-500 transition-colors"
                :title="link.isFavorite ? 'Bỏ ghim' : 'Ghim liên kết'"
                @click="toggleFavoriteLink(link)"
              >
                <Star :class="['w-5 h-5', link.isFavorite ? 'fill-amber-500 text-amber-500' : '']" />
              </button>
            </div>

            <!-- Title & URL -->
            <h4 class="text-base font-bold text-slate-900 line-clamp-1 group-hover:text-brand-600 transition-colors" :title="link.title">
              {{ link.title }}
            </h4>
            <a
              :href="link.url"
              target="_blank"
              class="text-xs text-brand-600 hover:underline block truncate font-mono mt-1"
              @click="handleOpenLink(link)"
            >
              🔗 {{ link.url }}
            </a>

            <!-- Description if any -->
            <p v-if="link.description" class="text-xs text-slate-600 mt-2.5 line-clamp-2 bg-slate-50 p-2 rounded-xs border border-slate-100">
              {{ link.description }}
            </p>
          </div>

          <!-- Footer Actions -->
          <div class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
            <span class="text-xs text-slate-400">{{ formatDateShort(link.createdAt) }}</span>

            <div class="flex items-center gap-1.5">
              <button
                type="button"
                class="p-1.5 rounded-md hover:bg-slate-100 text-slate-600 transition-colors"
                title="Sửa liên kết"
                @click="openEditLinkModal(link)"
              >
                <Edit2 class="w-4 h-4" />
              </button>
              <button
                type="button"
                class="p-1.5 rounded-md hover:bg-rose-50 text-slate-400 hover:text-rose-600 transition-colors"
                title="Xóa liên kết"
                @click="confirmDeleteLink(link)"
              >
                <Trash2 class="w-4 h-4" />
              </button>

              <button
                type="button"
                class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-md bg-brand-600 text-white hover:bg-brand-700 transition-all shadow-xs"
                @click="handleOpenLink(link)"
              >
                <span>Mở link</span>
                <ExternalLink class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Links Pagination -->
      <div v-if="studyLinks.length > 0" class="bg-white rounded-md border border-slate-200 shadow-xs">
        <AppPagination
          :current-page="linksPage"
          :total-pages="linksTotalPages"
          :page-size="linksPageSize"
          :total-elements="linksTotalElements"
          @update:page="handleLinkPageChange"
        />
      </div>
    </div>

    <!-- ============================================================ -->
    <!-- MODAL 1: UPLOAD DOCUMENT -->
    <!-- ============================================================ -->
    <AppModal v-model="showUploadModal" title="Tải lên tài liệu học tập" size="md">
      <form @submit.prevent="submitUpload" class="space-y-4">
        <!-- File Dropzone -->
        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Chọn tệp tin (Excel, Word, PDF, PPT, Ảnh...) <span class="text-rose-500">*</span>
          </label>
          <div
            :class="[
              'border-2 border-dashed rounded-md p-6 text-center transition-all cursor-pointer',
              selectedFile ? 'border-brand-500 bg-brand-50/30' : 'border-slate-300 hover:border-brand-400 bg-slate-50/50',
            ]"
            @click="triggerFileInput"
            @dragover.prevent
            @drop.prevent="handleFileDrop"
          >
            <input
              ref="fileInputRef"
              type="file"
              class="hidden"
              @change="handleFileChange"
              accept=".xlsx,.xls,.csv,.docx,.doc,.pdf,.pptx,.ppt,.txt,.png,.jpg,.jpeg"
            />

            <div v-if="!selectedFile" class="space-y-2">
              <UploadCloud class="w-10 h-10 text-brand-600 mx-auto" />
              <p class="text-sm font-semibold text-slate-800">
                Kéo thả file vào đây hoặc <span class="text-brand-600 underline">Chọn từ máy tính</span>
              </p>
              <p class="text-xs text-slate-400">
                Hỗ trợ Excel (.xlsx, .xls), Word (.docx), PDF, PowerPoint, hình ảnh (Tối đa 50MB)
              </p>
            </div>

            <div v-else class="flex items-center justify-between bg-white p-3 rounded-md border border-brand-200">
              <div class="flex items-center gap-3 truncate">
                <FileText class="w-8 h-8 text-brand-600 shrink-0" />
                <div class="text-left truncate">
                  <div class="text-sm font-bold text-slate-900 truncate">{{ selectedFile.name }}</div>
                  <div class="text-xs text-slate-400">{{ (selectedFile.size / (1024 * 1024)).toFixed(2) }} MB</div>
                </div>
              </div>
              <button
                type="button"
                class="p-1 text-slate-400 hover:text-rose-600"
                @click.stop="selectedFile = null"
              >
                <X class="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>

        <!-- Title -->
        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Tiêu đề hiển thị (Tùy chọn)
          </label>
          <input
            v-model="uploadForm.title"
            type="text"
            placeholder="Ví dụ: Tổng hợp 600 từ vựng TOEIC thường gặp"
            class="w-full text-sm border border-slate-200 rounded-md px-3 py-2 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs"
          />
        </div>

        <!-- Category -->
        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Danh mục tài liệu
          </label>
          <select
            v-model="uploadForm.category"
            class="w-full text-sm border border-slate-200 rounded-md px-3 py-2 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs"
          >
            <option value="TOEIC">Đề thi & Tài liệu TOEIC</option>
            <option value="VOCABULARY">Từ vựng (Vocabulary)</option>
            <option value="GRAMMAR">Ngữ pháp (Grammar)</option>
            <option value="LISTENING">Luyện nghe (Listening)</option>
            <option value="SPEAKING">Luyện nói (Speaking)</option>
            <option value="TEST_EXAM">Đề thi thử (Practice Test)</option>
            <option value="GENERAL">Tài liệu chung</option>
            <option value="OTHER">Khác</option>
          </select>
        </div>

        <!-- Description -->
        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Ghi chú / Mô tả nội dung
          </label>
          <textarea
            v-model="uploadForm.description"
            rows="3"
            placeholder="Ghi chú về tài liệu này, mẹo học hoặc nguồn tham khảo..."
            class="w-full text-sm border border-slate-200 rounded-md p-3 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs resize-none"
          ></textarea>
        </div>

        <div class="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showUploadModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="isUploading" :disabled="!selectedFile">
            Tải lên ngay
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- ============================================================ -->
    <!-- MODAL 2: EDIT DOCUMENT METADATA -->
    <!-- ============================================================ -->
    <AppModal v-model="showEditDocModal" title="Chỉnh sửa thông tin tài liệu" size="md">
      <form @submit.prevent="submitEditDoc" class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Tiêu đề tài liệu <span class="text-rose-500">*</span>
          </label>
          <input
            v-model="editDocForm.title"
            type="text"
            required
            class="w-full text-sm border border-slate-200 rounded-md px-3 py-2 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Danh mục
          </label>
          <select
            v-model="editDocForm.category"
            class="w-full text-sm border border-slate-200 rounded-md px-3 py-2 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs"
          >
            <option value="TOEIC">Đề thi & Tài liệu TOEIC</option>
            <option value="VOCABULARY">Từ vựng (Vocabulary)</option>
            <option value="GRAMMAR">Ngữ pháp (Grammar)</option>
            <option value="LISTENING">Luyện nghe (Listening)</option>
            <option value="SPEAKING">Luyện nói (Speaking)</option>
            <option value="TEST_EXAM">Đề thi thử (Practice Test)</option>
            <option value="GENERAL">Tài liệu chung</option>
            <option value="OTHER">Khác</option>
          </select>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Ghi chú / Mô tả
          </label>
          <textarea
            v-model="editDocForm.description"
            rows="3"
            class="w-full text-sm border border-slate-200 rounded-md p-3 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs resize-none"
          ></textarea>
        </div>

        <div class="flex items-center gap-2">
          <input
            id="editDocFav"
            type="checkbox"
            v-model="editDocForm.isFavorite"
            class="rounded-xs border-slate-300 text-brand-600 focus:ring-brand-500"
          />
          <label for="editDocFav" class="text-sm font-medium text-slate-700">Đánh dấu tài liệu yêu thích ⭐</label>
        </div>

        <div class="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showEditDocModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="isSavingDoc">
            Lưu thay đổi
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- ============================================================ -->
    <!-- MODAL 3: ADD / EDIT STUDY LINK -->
    <!-- ============================================================ -->
    <AppModal v-model="showLinkModal" :title="isEditingLink ? 'Chỉnh sửa liên kết học tập' : 'Thêm liên kết học tập mới'" size="md">
      <form @submit.prevent="submitLink" class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Tên gợi nhớ liên kết <span class="text-rose-500">*</span>
          </label>
          <input
            v-model="linkForm.title"
            type="text"
            required
            placeholder="Ví dụ: Kênh Youtube TED-Ed Luyện nghe"
            class="w-full text-sm border border-slate-200 rounded-md px-3 py-2 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Đường dẫn URL <span class="text-rose-500">*</span>
          </label>
          <input
            v-model="linkForm.url"
            type="text"
            required
            placeholder="https://www.youtube.com/@TEDEd hoặc oxfordlearnersdictionaries.com"
            class="w-full text-sm border border-slate-200 rounded-md px-3 py-2 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs font-mono"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Danh mục
          </label>
          <select
            v-model="linkForm.category"
            class="w-full text-sm border border-slate-200 rounded-md px-3 py-2 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs"
          >
            <option value="DICTIONARY">Từ điển online (Oxford, Cambridge...)</option>
            <option value="TOEIC">Đề thi & Luyện TOEIC</option>
            <option value="LISTENING">Kênh Luyện nghe (Youtube, Podcast)</option>
            <option value="GRAMMAR">Ngữ pháp & Bài tập</option>
            <option value="READING">Đọc báo & Tin tức tiếng Anh</option>
            <option value="GENERAL">Trang web chung</option>
          </select>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1">
            Ghi chú / Hướng dẫn
          </label>
          <textarea
            v-model="linkForm.description"
            rows="3"
            placeholder="Ghi chú về cách khai thác trang web này..."
            class="w-full text-sm border border-slate-200 rounded-md p-3 bg-white text-slate-800 focus:outline-none focus:border-brand-500 shadow-xs resize-none"
          ></textarea>
        </div>

        <div class="flex items-center gap-2">
          <input
            id="linkFav"
            type="checkbox"
            v-model="linkForm.isFavorite"
            class="rounded-xs border-slate-300 text-brand-600 focus:ring-brand-500"
          />
          <label for="linkFav" class="text-sm font-medium text-slate-700">Ghim lên đầu mục yêu thích ⭐</label>
        </div>

        <div class="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showLinkModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="isSavingLink">
            {{ isEditingLink ? 'Lưu thay đổi' : 'Lưu liên kết' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- Confirm Delete Modal -->
    <AppModal v-model="showDeleteModal" title="Xác nhận xóa" size="sm">
      <div class="space-y-3 py-2">
        <p class="text-sm text-slate-600">
          Bạn có chắc chắn muốn xóa {{ deleteType === 'DOC' ? 'tài liệu' : 'liên kết' }}:
          <strong class="text-slate-900">{{ itemToDelete?.title }}</strong> không?
        </p>
        <p v-if="deleteType === 'DOC'" class="text-xs text-rose-500">
          Tệp tin thực tế trên máy chủ cũng sẽ bị xóa hoàn toàn.
        </p>
      </div>
      <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
        <AppButton variant="secondary" size="sm" @click="showDeleteModal = false">Hủy</AppButton>
        <AppButton variant="danger" size="sm" :loading="isDeleting" @click="executeDelete">
          Xác nhận xóa
        </AppButton>
      </div>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import type {
  LearningDocument,
  DocumentStatistics,
  StudyLink,
} from '../../types';
import { documentService } from '../../services/document.service';
import { studyLinkService } from '../../services/study-link.service';
import AppButton from '../../components/common/AppButton.vue';
import AppSearch from '../../components/common/AppSearch.vue';
import AppModal from '../../components/common/AppModal.vue';
import AppPagination from '../../components/common/AppPagination.vue';
import {
  FolderArchive,
  Files,
  HardDrive,
  FileSpreadsheet,
  FileText,
  FileCode,
  File,
  Presentation,
  Image as ImageIcon,
  Upload,
  UploadCloud,
  Download,
  Eye,
  Trash2,
  Edit2,
  Star,
  Globe,
  ExternalLink,
  Link as LinkIcon,
  LayoutGrid,
  List,
  Loader2,
  X,
} from 'lucide-vue-next';

const route = useRoute();

// Main Tab: 'DOCUMENTS' | 'LINKS'
const activeTab = ref<'DOCUMENTS' | 'LINKS'>('DOCUMENTS');
const viewMode = ref<'GRID' | 'TABLE'>('GRID');

// Documents state
const documents = ref<LearningDocument[]>([]);
const docStats = ref<DocumentStatistics | null>(null);
const isLoadingDocs = ref(false);
const docPage = ref(0);
const docPageSize = ref(12);
const docTotalPages = ref(1);
const docTotalElements = ref(0);
const docFilter = reactive({
  search: '',
  category: '',
  fileType: '',
  isFavorite: undefined as boolean | undefined,
  sortBy: 'createdAt',
  sortDirection: 'DESC',
});

// Study Links state
const studyLinks = ref<StudyLink[]>([]);
const linksPage = ref(0);
const linksPageSize = ref(12);
const linksTotalPages = ref(1);
const linksTotalElements = ref(0);
const isLoadingLinks = ref(false);
const linkFilter = reactive({
  search: '',
  category: '',
  isFavorite: undefined as boolean | undefined,
  sortBy: 'createdAt',
  sortDirection: 'DESC',
});

// Modals
const showUploadModal = ref(false);
const isUploading = ref(false);
const fileInputRef = ref<HTMLInputElement | null>(null);
const selectedFile = ref<File | null>(null);
const uploadForm = reactive({
  title: '',
  category: 'TOEIC',
  description: '',
});

const showEditDocModal = ref(false);
const isSavingDoc = ref(false);
const currentEditDocId = ref<number | null>(null);
const editDocForm = reactive({
  title: '',
  category: 'TOEIC',
  description: '',
  isFavorite: false,
});

const showLinkModal = ref(false);
const isEditingLink = ref(false);
const isSavingLink = ref(false);
const currentEditLinkId = ref<number | null>(null);
const linkForm = reactive({
  title: '',
  url: '',
  category: 'TOEIC',
  description: '',
  isFavorite: false,
});

// Delete modal
const showDeleteModal = ref(false);
const deleteType = ref<'DOC' | 'LINK'>('DOC');
const itemToDelete = ref<{ id: number; title: string } | null>(null);
const isDeleting = ref(false);

onMounted(async () => {
  if (route.query.tab === 'links') {
    activeTab.value = 'LINKS';
  }
  await Promise.all([loadDocuments(), loadDocStats(), loadLinks()]);

  if (route.query.action === 'upload') {
    openUploadModal();
  } else if (route.query.action === 'add-link') {
    openAddLinkModal();
  }
});

// ==================== DOCUMENTS METHODS ====================
async function loadDocuments(page = 0) {
  docPage.value = page;
  isLoadingDocs.value = true;
  try {
    const res = await documentService.getDocuments({
      ...docFilter,
      page: docPage.value,
      size: docPageSize.value,
    });
    documents.value = res.items || [];
    docTotalPages.value = res.totalPages || 1;
    docTotalElements.value = res.totalElements || 0;
  } catch (e) {
    console.error(e);
  } finally {
    isLoadingDocs.value = false;
  }
}

function handleDocPageChange(page: number) {
  loadDocuments(page);
}

async function loadDocStats() {
  try {
    docStats.value = await documentService.getStatistics();
  } catch (e) {
    console.error(e);
  }
}

function toggleFavoriteFilter() {
  docFilter.isFavorite = docFilter.isFavorite ? undefined : true;
  loadDocuments();
}

async function toggleFavoriteDoc(doc: LearningDocument) {
  try {
    const updated = await documentService.toggleFavorite(doc.id);
    doc.isFavorite = updated.isFavorite;
    loadDocStats();
  } catch (e) {
    console.error(e);
  }
}

function openUploadModal() {
  selectedFile.value = null;
  uploadForm.title = '';
  uploadForm.category = 'TOEIC';
  uploadForm.description = '';
  showUploadModal.value = true;
}

function triggerFileInput() {
  fileInputRef.value?.click();
}

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0];
    if (!uploadForm.title) {
      uploadForm.title = selectedFile.value.name.replace(/\.[^/.]+$/, '');
    }
  }
}

function handleFileDrop(e: DragEvent) {
  if (e.dataTransfer?.files && e.dataTransfer.files[0]) {
    selectedFile.value = e.dataTransfer.files[0];
    if (!uploadForm.title) {
      uploadForm.title = selectedFile.value.name.replace(/\.[^/.]+$/, '');
    }
  }
}

async function submitUpload() {
  if (!selectedFile.value) return;
  isUploading.value = true;
  try {
    await documentService.uploadDocument(
      selectedFile.value,
      uploadForm.title,
      uploadForm.category,
      uploadForm.description
    );
    showUploadModal.value = false;
    await Promise.all([loadDocuments(), loadDocStats()]);
  } catch (e) {
    console.error(e);
  } finally {
    isUploading.value = false;
  }
}

function openEditDocModal(doc: LearningDocument) {
  currentEditDocId.value = doc.id;
  editDocForm.title = doc.title;
  editDocForm.category = doc.category || 'GENERAL';
  editDocForm.description = doc.description || '';
  editDocForm.isFavorite = doc.isFavorite;
  showEditDocModal.value = true;
}

async function submitEditDoc() {
  if (!currentEditDocId.value) return;
  isSavingDoc.value = true;
  try {
    await documentService.updateDocument(currentEditDocId.value, editDocForm);
    showEditDocModal.value = false;
    await Promise.all([loadDocuments(), loadDocStats()]);
  } catch (e) {
    console.error(e);
  } finally {
    isSavingDoc.value = false;
  }
}

async function downloadDoc(doc: LearningDocument) {
  try {
    await documentService.downloadFile(doc.id, doc.fileName);
    doc.downloadCount += 1;
  } catch (e) {
    console.error(e);
  }
}

function confirmDeleteDoc(doc: LearningDocument) {
  deleteType.value = 'DOC';
  itemToDelete.value = { id: doc.id, title: doc.title };
  showDeleteModal.value = true;
}

// ==================== STUDY LINKS METHODS ====================
async function loadLinks(page = 0) {
  linksPage.value = page;
  isLoadingLinks.value = true;
  try {
    const res = await studyLinkService.getLinks({
      ...linkFilter,
      page: linksPage.value,
      size: linksPageSize.value,
    });
    studyLinks.value = res.items || [];
    linksTotalElements.value = res.totalElements;
    linksTotalPages.value = res.totalPages || 1;
  } catch (e) {
    console.error(e);
  } finally {
    isLoadingLinks.value = false;
  }
}

function handleLinkPageChange(page: number) {
  loadLinks(page);
}

function toggleFavoriteLinkFilter() {
  linkFilter.isFavorite = linkFilter.isFavorite ? undefined : true;
  loadLinks();
}

async function toggleFavoriteLink(link: StudyLink) {
  try {
    const updated = await studyLinkService.toggleFavorite(link.id);
    link.isFavorite = updated.isFavorite;
  } catch (e) {
    console.error(e);
  }
}

function openAddLinkModal() {
  isEditingLink.value = false;
  currentEditLinkId.value = null;
  linkForm.title = '';
  linkForm.url = '';
  linkForm.category = 'TOEIC';
  linkForm.description = '';
  linkForm.isFavorite = false;
  showLinkModal.value = true;
}

function openEditLinkModal(link: StudyLink) {
  isEditingLink.value = true;
  currentEditLinkId.value = link.id;
  linkForm.title = link.title;
  linkForm.url = link.url;
  linkForm.category = link.category || 'GENERAL';
  linkForm.description = link.description || '';
  linkForm.isFavorite = link.isFavorite;
  showLinkModal.value = true;
}

async function submitLink() {
  isSavingLink.value = true;
  try {
    if (isEditingLink.value && currentEditLinkId.value) {
      await studyLinkService.updateLink(currentEditLinkId.value, linkForm);
    } else {
      await studyLinkService.createLink(linkForm);
    }
    showLinkModal.value = false;
    await loadLinks();
  } catch (e) {
    console.error(e);
  } finally {
    isSavingLink.value = false;
  }
}

async function handleOpenLink(link: StudyLink) {
  try {
    studyLinkService.recordClick(link.id);
    link.clickCount += 1;
  } catch (e) {
    console.error(e);
  }
  window.open(link.url, '_blank');
}

function confirmDeleteLink(link: StudyLink) {
  deleteType.value = 'LINK';
  itemToDelete.value = { id: link.id, title: link.title };
  showDeleteModal.value = true;
}

// Common Delete
async function executeDelete() {
  if (!itemToDelete.value) return;
  isDeleting.value = true;
  try {
    if (deleteType.value === 'DOC') {
      await documentService.deleteDocument(itemToDelete.value.id);
      await Promise.all([loadDocuments(), loadDocStats()]);
    } else {
      await studyLinkService.deleteLink(itemToDelete.value.id);
      await loadLinks();
    }
    showDeleteModal.value = false;
  } catch (e) {
    console.error(e);
  } finally {
    isDeleting.value = false;
  }
}

// Helpers
function getFileTypeStyles(fileType: string) {
  switch (fileType) {
    case 'EXCEL':
      return { bg: 'bg-emerald-50 border-emerald-200', color: 'text-emerald-600', icon: FileSpreadsheet };
    case 'WORD':
      return { bg: 'bg-blue-50 border-blue-200', color: 'text-blue-600', icon: FileText };
    case 'PDF':
      return { bg: 'bg-rose-50 border-rose-200', color: 'text-rose-600', icon: FileCode };
    case 'POWERPOINT':
      return { bg: 'bg-amber-50 border-amber-200', color: 'text-amber-600', icon: Presentation };
    case 'IMAGE':
      return { bg: 'bg-purple-50 border-purple-200', color: 'text-purple-600', icon: ImageIcon };
    default:
      return { bg: 'bg-slate-50 border-slate-200', color: 'text-slate-600', icon: File };
  }
}

function getCategoryLabel(category?: string) {
  switch (category) {
    case 'TOEIC': return 'Đề thi & Tài liệu TOEIC';
    case 'VOCABULARY': return 'Từ vựng';
    case 'GRAMMAR': return 'Ngữ pháp';
    case 'LISTENING': return 'Luyện nghe';
    case 'SPEAKING': return 'Luyện nói';
    case 'TEST_EXAM': return 'Đề thi thử';
    default: return 'Tài liệu chung';
  }
}

function getLinkCategoryLabel(category?: string) {
  switch (category) {
    case 'DICTIONARY': return 'Từ điển online';
    case 'TOEIC': return 'Luyện TOEIC';
    case 'LISTENING': return 'Luyện nghe';
    case 'GRAMMAR': return 'Ngữ pháp';
    case 'READING': return 'Đọc báo';
    default: return 'Trang web chung';
  }
}

function formatDate(d?: string) {
  if (!d) return '';
  return new Date(d).toLocaleString('vi-VN');
}

function formatDateShort(d?: string) {
  if (!d) return '';
  return new Date(d).toLocaleDateString('vi-VN');
}
</script>
