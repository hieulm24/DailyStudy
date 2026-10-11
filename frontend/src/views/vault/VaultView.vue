<template>
  <div class="space-y-6 w-full animate-in fade-in duration-200">
    <!-- Header Banner (Base style) -->
    <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-5">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 shrink-0 rounded-md bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-100/60 shadow-xs">
          <KeyRound class="w-7 h-7" />
        </div>
        <div>
          <h2 class="text-xl sm:text-2xl lg:text-3xl font-bold text-slate-900 tracking-tight">
            Két mật khẩu & Quản lý tài khoản
          </h2>
          <p class="text-xs sm:text-sm text-slate-500 mt-1">
            Lưu trữ, mã hóa AES-256 và quản lý toàn bộ tài khoản, mật khẩu, mã 2FA và tệp đính kèm bí mật
          </p>
        </div>
      </div>

      <!-- Quick Action Buttons -->
      <div class="flex items-center gap-2.5 shrink-0 self-start md:self-auto">
        <button
          type="button"
          class="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs sm:text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-md transition-colors shadow-xs cursor-pointer"
          @click="openAddModal"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm tài khoản</span>
        </button>
      </div>
    </div>

    <!-- Summary Metric Cards (Base style, generous padding) -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Total Accounts -->
      <div
        :class="[
          'bg-white p-5 rounded-md border shadow-xs flex flex-col justify-between transition-all cursor-pointer',
          activeTab === 'ALL' ? 'border-indigo-500 ring-2 ring-indigo-500/20' : 'border-slate-200 hover:border-slate-300',
        ]"
        @click="switchTab('ALL')"
      >
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500">Tổng tài khoản</span>
          <div class="w-9 h-9 rounded-md bg-indigo-50 text-indigo-600 flex items-center justify-center">
            <KeyRound class="w-4.5 h-4.5" />
          </div>
        </div>
        <div class="mt-3">
          <span class="text-2xl font-bold text-slate-900 tracking-tight">{{ summary.totalAccounts }}</span>
          <p class="text-xs text-slate-400 mt-1">Tất cả tài khoản trong két</p>
        </div>
      </div>

      <!-- Favorites -->
      <div
        :class="[
          'bg-white p-5 rounded-md border shadow-xs flex flex-col justify-between transition-all cursor-pointer',
          activeTab === 'FAVORITES' ? 'border-amber-500 ring-2 ring-amber-500/20' : 'border-slate-200 hover:border-slate-300',
        ]"
        @click="switchTab('FAVORITES')"
      >
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500">Yêu thích</span>
          <div class="w-9 h-9 rounded-md bg-amber-50 text-amber-600 flex items-center justify-center">
            <Star class="w-4.5 h-4.5 fill-amber-400" />
          </div>
        </div>
        <div class="mt-3">
          <span class="text-2xl font-bold text-amber-600 tracking-tight">{{ summary.favoriteCount }}</span>
          <p class="text-xs text-slate-400 mt-1">Đã đánh dấu sao ghim đầu</p>
        </div>
      </div>

      <!-- Attachments -->
      <div
        class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between"
      >
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500">Tệp đính kèm</span>
          <div class="w-9 h-9 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center">
            <Paperclip class="w-4.5 h-4.5" />
          </div>
        </div>
        <div class="mt-3">
          <span class="text-2xl font-bold text-emerald-600 tracking-tight">{{ summary.totalAttachments }}</span>
          <p class="text-xs text-slate-400 mt-1">Ảnh, PDF, Key, Zip lưu trữ</p>
        </div>
      </div>

      <!-- Trash -->
      <div
        :class="[
          'bg-white p-5 rounded-md border shadow-xs flex flex-col justify-between transition-all cursor-pointer',
          activeTab === 'TRASH' ? 'border-rose-500 ring-2 ring-rose-500/20' : 'border-slate-200 hover:border-slate-300',
        ]"
        @click="switchTab('TRASH')"
      >
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-500">Thùng rác</span>
          <div class="w-9 h-9 rounded-md bg-rose-50 text-rose-600 flex items-center justify-center">
            <Trash2 class="w-4.5 h-4.5" />
          </div>
        </div>
        <div class="mt-3">
          <span class="text-2xl font-bold text-rose-600 tracking-tight">{{ summary.trashCount }}</span>
          <p class="text-xs text-slate-400 mt-1">Có thể khôi phục lại</p>
        </div>
      </div>
    </div>

    <!-- Category Filters, Search & View Controls -->
    <div class="bg-white rounded-md border border-slate-200 p-4 sm:p-5 shadow-xs space-y-4">
      <!-- Search & View Mode Switcher -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <!-- Search input -->
        <div class="relative flex-1 max-w-md">
          <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm theo tiêu đề, dịch vụ, username, email..."
            class="w-full pl-9 pr-8 py-2 text-sm bg-slate-50 border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 transition-colors"
            @input="onSearchInput"
          />
          <button
            v-if="searchQuery"
            type="button"
            class="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 cursor-pointer"
            @click="clearSearch"
          >
            <X class="w-3.5 h-3.5" />
          </button>
        </div>

        <!-- View mode toggle -->
        <div class="flex items-center gap-1.5 self-end sm:self-auto bg-slate-100 p-1 rounded-md border border-slate-200/80">
          <button
            type="button"
            :class="[
              'p-1.5 rounded-md transition-colors cursor-pointer',
              viewMode === 'GRID' ? 'bg-white text-indigo-600 shadow-2xs' : 'text-slate-500 hover:text-slate-800',
            ]"
            title="Dạng thẻ lưới"
            @click="viewMode = 'GRID'"
          >
            <LayoutGrid class="w-4 h-4" />
          </button>
          <button
            type="button"
            :class="[
              'p-1.5 rounded-md transition-colors cursor-pointer',
              viewMode === 'LIST' ? 'bg-white text-indigo-600 shadow-2xs' : 'text-slate-500 hover:text-slate-800',
            ]"
            title="Dạng danh sách bảng"
            @click="viewMode = 'LIST'"
          >
            <List class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- Category Filter Pills -->
      <div class="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-thin text-xs">
        <button
          type="button"
          :class="[
            'px-3 py-1.5 rounded-md font-semibold transition-all shrink-0 cursor-pointer flex items-center gap-1.5 border',
            activeTab === 'ALL'
              ? 'bg-indigo-600 text-white border-indigo-600 shadow-2xs'
              : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
          ]"
          @click="switchTab('ALL')"
        >
          <FolderArchive class="w-3.5 h-3.5" />
          <span>Tất cả</span>
          <span :class="['px-1.5 py-0.2 rounded-full text-[10px]', activeTab === 'ALL' ? 'bg-indigo-500 text-white' : 'bg-slate-100 text-slate-600']">
            {{ summary.totalAccounts }}
          </span>
        </button>

        <button
          type="button"
          :class="[
            'px-3 py-1.5 rounded-md font-semibold transition-all shrink-0 cursor-pointer flex items-center gap-1.5 border',
            activeTab === 'FAVORITES'
              ? 'bg-amber-500 text-white border-amber-500 shadow-2xs'
              : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
          ]"
          @click="switchTab('FAVORITES')"
        >
          <Star class="w-3.5 h-3.5" />
          <span>Yêu thích</span>
          <span :class="['px-1.5 py-0.2 rounded-full text-[10px]', activeTab === 'FAVORITES' ? 'bg-amber-600 text-white' : 'bg-slate-100 text-slate-600']">
            {{ summary.favoriteCount }}
          </span>
        </button>

        <!-- Categories breakdown tabs -->
        <button
          v-for="cat in categories"
          :key="cat.id"
          type="button"
          :class="[
            'px-3 py-1.5 rounded-md font-semibold transition-all shrink-0 cursor-pointer flex items-center gap-1.5 border',
            selectedCategoryId === cat.id
              ? 'bg-indigo-600 text-white border-indigo-600 shadow-2xs'
              : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50',
          ]"
          @click="selectCategory(cat.id)"
        >
          <component :is="getCategoryIcon(cat.code)" class="w-3.5 h-3.5" />
          <span>{{ cat.name }}</span>
          <span
            v-if="getCategoryCount(cat.id) > 0"
            :class="[
              'px-1.5 py-0.2 rounded-full text-[10px]',
              selectedCategoryId === cat.id ? 'bg-indigo-500 text-white' : 'bg-slate-100 text-slate-600',
            ]"
          >
            {{ getCategoryCount(cat.id) }}
          </span>
        </button>

        <!-- Trash Tab -->
        <button
          type="button"
          :class="[
            'px-3 py-1.5 rounded-md font-semibold transition-all shrink-0 cursor-pointer flex items-center gap-1.5 border ml-auto',
            activeTab === 'TRASH'
              ? 'bg-rose-600 text-white border-rose-600 shadow-2xs'
              : 'bg-white text-rose-600 border-rose-200 hover:bg-rose-50',
          ]"
          @click="switchTab('TRASH')"
        >
          <Trash2 class="w-3.5 h-3.5" />
          <span>Thùng rác</span>
          <span v-if="summary.trashCount > 0" class="px-1.5 py-0.2 rounded-full text-[10px] bg-rose-100 text-rose-800">
            {{ summary.trashCount }}
          </span>
        </button>
      </div>
    </div>

    <!-- Main List/Grid View -->
    <div>
      <!-- Loading State -->
      <div v-if="isLoading" class="p-16 flex flex-col items-center justify-center gap-3">
        <Loader2 class="w-8 h-8 text-indigo-600 animate-spin" />
        <span class="text-sm font-medium text-slate-500">Đang tải dữ liệu Két bảo mật...</span>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="credentials.length === 0"
        class="bg-white rounded-md border border-slate-200 p-12 text-center max-w-lg mx-auto shadow-xs"
      >
        <div class="w-14 h-14 rounded-md bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto mb-4 border border-indigo-100 shadow-xs">
          <KeyRound class="w-7 h-7" />
        </div>
        <h3 class="text-base font-bold text-slate-900 mb-1">
          {{ activeTab === 'TRASH' ? 'Thùng rác trống' : 'Không có tài khoản nào' }}
        </h3>
        <p class="text-xs text-slate-500 max-w-sm mx-auto mb-5">
          {{
            activeTab === 'TRASH'
              ? 'Không có tài khoản nào trong thùng rác.'
              : searchQuery
              ? 'Không tìm thấy tài khoản nào phù hợp với từ khóa tìm kiếm.'
              : 'Hãy lưu trữ thông tin đăng nhập, mật khẩu và tài liệu bảo mật đầu tiên của bạn vào Két.'
          }}
        </p>
        <button
          v-if="activeTab !== 'TRASH'"
          type="button"
          class="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-md bg-indigo-600 text-white hover:bg-indigo-700 transition-colors shadow-xs cursor-pointer"
          @click="openAddModal"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm tài khoản mới ngay</span>
        </button>
      </div>

      <!-- 1. Grid Cards Mode -->
      <div v-else-if="viewMode === 'GRID'" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        <div
          v-for="item in credentials"
          :key="item.id"
          class="bg-white rounded-md border border-slate-200 hover:border-indigo-300 hover:shadow-sm transition-all p-5 flex flex-col justify-between group shadow-xs"
        >
          <!-- Card Header -->
          <div class="space-y-3">
            <div class="flex items-start justify-between gap-2.5">
              <div class="flex items-center gap-3 min-w-0">
                <!-- Icon badge -->
                <div
                  :class="[
                    'w-10 h-10 rounded-md flex items-center justify-center shrink-0 border shadow-xs',
                    getCategoryBadgeClass(item.categoryCode),
                  ]"
                >
                  <component :is="getCategoryIcon(item.categoryCode)" class="w-5 h-5" />
                </div>

                <div class="min-w-0">
                  <h3
                    class="text-sm font-bold text-slate-900 truncate hover:text-indigo-600 cursor-pointer"
                    :title="item.title"
                    @click="openDetailModal(item.id)"
                  >
                    {{ item.title }}
                  </h3>
                  <div class="flex items-center gap-1.5 text-xs text-slate-500 mt-0.5">
                    <span v-if="item.serviceName" class="font-medium text-slate-700 truncate">
                      {{ item.serviceName }}
                    </span>
                    <span v-if="item.serviceName && item.categoryName" class="text-slate-300">•</span>
                    <span v-if="item.categoryName" class="text-[11px] text-slate-400 truncate">
                      {{ item.categoryName }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- Top Star Favorite button -->
              <button
                type="button"
                :class="[
                  'p-1.5 rounded-md transition-colors cursor-pointer shrink-0',
                  item.isFavorite
                    ? 'text-amber-500 hover:text-amber-600'
                    : 'text-slate-300 hover:text-amber-400 group-hover:text-slate-400',
                ]"
                :title="item.isFavorite ? 'Bỏ yêu thích' : 'Đánh dấu yêu thích'"
                @click.stop="toggleFavorite(item)"
              >
                <Star :class="['w-4 h-4', item.isFavorite ? 'fill-amber-400' : '']" />
              </button>
            </div>

            <!-- Credentials Quick Rows -->
            <div class="space-y-2 pt-1 text-xs">
              <!-- Username or Email -->
              <div
                v-if="item.username || item.email"
                class="flex items-center justify-between gap-2 bg-slate-50/80 px-2.5 py-1.5 rounded-lg border border-slate-100"
              >
                <div class="flex items-center gap-1.5 truncate text-slate-600">
                  <User class="w-3.5 h-3.5 text-slate-400 shrink-0" />
                  <span class="truncate font-medium text-slate-800 select-all">
                    {{ item.username || item.email }}
                  </span>
                </div>
                <button
                  type="button"
                  class="p-1 text-slate-400 hover:text-indigo-600 rounded transition-colors cursor-pointer shrink-0"
                  title="Sao chép tên đăng nhập"
                  @click.stop="copyQuick(item.username || item.email || '', `u_${item.id}`, 'Tên đăng nhập')"
                >
                  <Check v-if="copiedField === `u_${item.id}`" class="w-3.5 h-3.5 text-emerald-600" />
                  <Copy v-else class="w-3.5 h-3.5" />
                </button>
              </div>

              <!-- Password -->
              <div
                class="flex items-center justify-between gap-2 bg-slate-50/80 px-2.5 py-1.5 rounded-lg border border-slate-100"
              >
                <div class="flex items-center gap-1.5 truncate">
                  <Lock class="w-3.5 h-3.5 text-slate-400 shrink-0" />
                  <span class="font-mono text-slate-800 tracking-wider truncate font-medium select-all">
                    {{ revealedPasswords[item.id] ? (item.password || '••••••••') : '••••••••••••' }}
                  </span>
                </div>
                <div class="flex items-center gap-0.5 shrink-0">
                  <button
                    type="button"
                    class="p-1 text-slate-400 hover:text-slate-600 rounded transition-colors cursor-pointer"
                    :title="revealedPasswords[item.id] ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                    @click.stop="revealedPasswords[item.id] = !revealedPasswords[item.id]"
                  >
                    <EyeOff v-if="revealedPasswords[item.id]" class="w-3.5 h-3.5" />
                    <Eye v-else class="w-3.5 h-3.5" />
                  </button>
                  <button
                    type="button"
                    class="p-1 text-slate-400 hover:text-indigo-600 rounded transition-colors cursor-pointer"
                    title="Sao chép mật khẩu"
                    @click.stop="copyQuick(item.password || '', `p_${item.id}`, 'Mật khẩu')"
                  >
                    <Check v-if="copiedField === `p_${item.id}`" class="w-3.5 h-3.5 text-emerald-600" />
                    <Copy v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Features & Tags Bar -->
            <div class="flex items-center gap-1.5 flex-wrap pt-1">
              <!-- Password strength -->
              <span :class="['px-2 py-0.5 text-[10px] font-bold rounded-sm uppercase', getStrengthBadge(item.passwordStrength).class]">
                {{ getStrengthBadge(item.passwordStrength).label }}
              </span>

              <!-- Attachments pill -->
              <span
                v-if="item.attachmentCount > 0"
                class="inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-semibold rounded-md bg-emerald-50 text-emerald-700 border border-emerald-200"
              >
                <Paperclip class="w-3 h-3" />
                <span>{{ item.attachmentCount }} tệp</span>
              </span>

              <!-- 2FA pill -->
              <span
                v-if="item.hasTotp"
                class="inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-semibold rounded-md bg-amber-50 text-amber-700 border border-amber-200"
              >
                <ShieldCheck class="w-3 h-3" />
                <span>2FA</span>
              </span>

              <!-- Notes pill -->
              <span
                v-if="item.hasNotes"
                class="inline-flex items-center gap-1 px-2 py-0.5 text-[10px] font-semibold rounded-md bg-slate-100 text-slate-600"
              >
                <FileText class="w-3 h-3" />
                <span>Ghi chú</span>
              </span>
            </div>
          </div>

          <!-- Card Footer Actions -->
          <div class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
            <!-- External link if available -->
            <a
              v-if="item.websiteUrl"
              :href="ensureHttp(item.websiteUrl)"
              target="_blank"
              rel="noopener noreferrer"
              class="text-indigo-600 hover:text-indigo-700 hover:underline inline-flex items-center gap-1 truncate max-w-[140px]"
              @click.stop
            >
              <ExternalLink class="w-3 h-3 shrink-0" />
              <span class="truncate">Đăng nhập</span>
            </a>
            <span v-else class="text-[11px] text-slate-400">
              {{ formatDateSimple(item.updatedAt) }}
            </span>

            <!-- Action buttons -->
            <div class="flex items-center gap-1">
              <button
                type="button"
                class="px-2.5 py-1 rounded-md text-xs font-semibold bg-slate-100 text-slate-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors cursor-pointer"
                @click="openDetailModal(item.id)"
              >
                Xem chi tiết
              </button>

              <button
                v-if="!item.isDeleted"
                type="button"
                class="p-1 text-slate-400 hover:text-indigo-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
                title="Chỉnh sửa"
                @click="openEditModal(item.id)"
              >
                <Pencil class="w-3.5 h-3.5" />
              </button>

              <button
                v-if="item.isDeleted"
                type="button"
                class="p-1 text-emerald-600 hover:bg-emerald-50 rounded-md transition-colors cursor-pointer"
                title="Khôi phục"
                @click="restoreItem(item.id)"
              >
                <RotateCcw class="w-3.5 h-3.5" />
              </button>

              <button
                v-else
                type="button"
                class="p-1 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors cursor-pointer"
                title="Chuyển vào thùng rác"
                @click="moveToTrash(item.id)"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. Table List Mode -->
      <div v-else class="bg-white rounded-md border border-slate-200 overflow-hidden shadow-xs">
        <div class="overflow-x-auto">
          <table class="w-full text-left text-xs">
            <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
              <tr>
                <th class="py-3 px-4 w-12 text-center">★</th>
                <th class="py-3 px-4">Tài khoản & Dịch vụ</th>
                <th class="py-3 px-4">Tên đăng nhập / Email</th>
                <th class="py-3 px-4">Mật khẩu</th>
                <th class="py-3 px-4">Độ mạnh</th>
                <th class="py-3 px-4">Đính kèm</th>
                <th class="py-3 px-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="item in credentials"
                :key="item.id"
                class="hover:bg-slate-50/70 transition-colors group cursor-pointer"
                @click="openDetailModal(item.id)"
              >
                <!-- Favorite -->
                <td class="py-3 px-4 text-center" @click.stop>
                  <button
                    type="button"
                    :class="[
                      'p-1 rounded-md transition-colors cursor-pointer',
                      item.isFavorite ? 'text-amber-500' : 'text-slate-300 hover:text-amber-400',
                    ]"
                    @click="toggleFavorite(item)"
                  >
                    <Star :class="['w-4 h-4', item.isFavorite ? 'fill-amber-400' : '']" />
                  </button>
                </td>

                <!-- Title & Category -->
                <td class="py-3 px-4">
                  <div class="flex items-center gap-2.5">
                    <div :class="['w-8 h-8 rounded-lg flex items-center justify-center shrink-0 border', getCategoryBadgeClass(item.categoryCode)]">
                      <component :is="getCategoryIcon(item.categoryCode)" class="w-4 h-4" />
                    </div>
                    <div class="min-w-0">
                      <span class="font-bold text-slate-900 block truncate group-hover:text-indigo-600">
                        {{ item.title }}
                      </span>
                      <span v-if="item.serviceName" class="text-[11px] text-slate-400 block truncate">
                        {{ item.serviceName }}
                      </span>
                    </div>
                  </div>
                </td>

                <!-- Username / Email -->
                <td class="py-3 px-4" @click.stop>
                  <div v-if="item.username || item.email" class="flex items-center gap-1.5">
                    <span class="font-medium text-slate-800 truncate max-w-[180px] select-all">
                      {{ item.username || item.email }}
                    </span>
                    <button
                      type="button"
                      class="p-1 text-slate-400 hover:text-indigo-600 rounded cursor-pointer shrink-0"
                      title="Sao chép"
                      @click="copyQuick(item.username || item.email || '', `tbl_u_${item.id}`, 'Tên đăng nhập')"
                    >
                      <Check v-if="copiedField === `tbl_u_${item.id}`" class="w-3.5 h-3.5 text-emerald-600" />
                      <Copy v-else class="w-3.5 h-3.5" />
                    </button>
                  </div>
                  <span v-else class="text-slate-300">—</span>
                </td>

                <!-- Password -->
                <td class="py-3 px-4" @click.stop>
                  <div class="flex items-center gap-1.5">
                    <span class="font-mono text-slate-800 tracking-wider">
                      {{ revealedPasswords[item.id] ? item.password : '••••••••••••' }}
                    </span>
                    <button
                      type="button"
                      class="p-1 text-slate-400 hover:text-slate-600 rounded cursor-pointer"
                      @click="revealedPasswords[item.id] = !revealedPasswords[item.id]"
                    >
                      <EyeOff v-if="revealedPasswords[item.id]" class="w-3.5 h-3.5" />
                      <Eye v-else class="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      class="p-1 text-slate-400 hover:text-indigo-600 rounded cursor-pointer"
                      title="Sao chép"
                      @click="copyQuick(item.password || '', `tbl_p_${item.id}`, 'Mật khẩu')"
                    >
                      <Check v-if="copiedField === `tbl_p_${item.id}`" class="w-3.5 h-3.5 text-emerald-600" />
                      <Copy v-else class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>

                <!-- Strength -->
                <td class="py-3 px-4">
                  <span :class="['px-2 py-0.5 text-[10px] font-bold rounded-sm uppercase', getStrengthBadge(item.passwordStrength).class]">
                    {{ getStrengthBadge(item.passwordStrength).label }}
                  </span>
                </td>

                <!-- Attachments -->
                <td class="py-3 px-4">
                  <span
                    v-if="item.attachmentCount > 0"
                    class="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 font-semibold text-[11px]"
                  >
                    <Paperclip class="w-3 h-3" />
                    <span>{{ item.attachmentCount }}</span>
                  </span>
                  <span v-else class="text-slate-300">0</span>
                </td>

                <!-- Actions -->
                <td class="py-3 px-4 text-right" @click.stop>
                  <div class="flex items-center justify-end gap-1">
                    <button
                      v-if="!item.isDeleted"
                      type="button"
                      class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer"
                      title="Chỉnh sửa"
                      @click="openEditModal(item.id)"
                    >
                      <Pencil class="w-3.5 h-3.5" />
                    </button>
                    <button
                      v-if="item.isDeleted"
                      type="button"
                      class="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded-md transition-colors cursor-pointer"
                      title="Khôi phục"
                      @click="restoreItem(item.id)"
                    >
                      <RotateCcw class="w-3.5 h-3.5" />
                    </button>
                    <button
                      v-else
                      type="button"
                      class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors cursor-pointer"
                      title="Thùng rác"
                      @click="moveToTrash(item.id)"
                    >
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <!-- 1. Add / Edit Credential Modal -->
    <AddEditCredentialModal
      :is-open="showAddEditModal"
      :categories="categories"
      :editing-id="editingCredentialId"
      @close="closeAddEditModal"
      @saved="onCredentialSaved"
    />

    <!-- 2. Credential Detail Modal -->
    <CredentialDetailModal
      :is-open="showDetailModal"
      :credential-id="detailCredentialId"
      @close="closeDetailModal"
      @edit="onEditFromDetail"
      @changed="onDetailChanged"
    />


  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import {
  KeyRound,
  ShieldCheck,
  Star,
  Paperclip,
  Trash2,
  Plus,
  Search,
  X,
  LayoutGrid,
  List,
  FolderArchive,
  User,
  Lock,
  Eye,
  EyeOff,
  Copy,
  Check,
  ExternalLink,
  Pencil,
  RotateCcw,
  FileText,
  Loader2,
  Mail,
  Share2,
  Briefcase,
  Gamepad2,
  Server,
  Globe,
  ShoppingCart,
  GraduationCap,
} from 'lucide-vue-next';
import { vaultService } from '../../services/vault.service';
import { useToastStore } from '../../stores/toast.store';
import AddEditCredentialModal from './components/AddEditCredentialModal.vue';
import CredentialDetailModal from './components/CredentialDetailModal.vue';
import type {
  AccountCategory,
  AccountCredential,
  VaultSummary,
} from '../../types/vault.types';

const toast = useToastStore();

// States
const isLoading = ref(false);
const categories = ref<AccountCategory[]>([]);
const credentials = ref<AccountCredential[]>([]);
const summary = reactive<VaultSummary>({
  totalAccounts: 0,
  favoriteCount: 0,
  weakPasswordCount: 0,
  mediumPasswordCount: 0,
  strongPasswordCount: 0,
  trashCount: 0,
  totalAttachments: 0,
  categoryBreakdowns: [],
});

// UI Views & Filter Tab
type FilterTab = 'ALL' | 'FAVORITES' | 'CATEGORY' | 'TRASH';
const activeTab = ref<FilterTab>('ALL');
const selectedCategoryId = ref<number | undefined>(undefined);
const searchQuery = ref('');
const viewMode = ref<'GRID' | 'LIST'>('GRID');

const revealedPasswords = reactive<Record<number, boolean>>({});
const copiedField = ref<string | null>(null);

// Modal Triggers
const showAddEditModal = ref(false);
const editingCredentialId = ref<number | null>(null);

const showDetailModal = ref(false);
const detailCredentialId = ref<number | null>(null);

// Search debounce timer
let searchTimeout: any = null;

onMounted(async () => {
  await Promise.all([loadCategories(), loadSummary(), loadCredentials()]);
});

async function loadCategories() {
  try {
    categories.value = await vaultService.getCategories();
  } catch (err) {
    console.error('Failed to load vault categories', err);
  }
}

async function loadSummary() {
  try {
    const data = await vaultService.getSummary();
    Object.assign(summary, data);
  } catch (err) {
    console.error('Failed to load vault summary', err);
  }
}

async function loadCredentials() {
  isLoading.value = true;
  try {
    const isFavorite = activeTab.value === 'FAVORITES' ? true : undefined;
    const isDeleted = activeTab.value === 'TRASH' ? true : false;
    const categoryId = activeTab.value === 'CATEGORY' ? selectedCategoryId.value : undefined;

    const pageData = await vaultService.getCredentials({
      categoryId,
      isFavorite,
      isDeleted,
      search: searchQuery.value.trim() || undefined,
      page: 0,
      size: 100,
    });
    credentials.value = pageData.items || [];
  } catch (err: any) {
    toast.error('Không thể tải danh sách tài khoản: ' + (err.response?.data?.message || err.message));
  } finally {
    isLoading.value = false;
  }
}

function switchTab(tab: FilterTab) {
  activeTab.value = tab;
  selectedCategoryId.value = undefined;
  loadCredentials();
}

function selectCategory(categoryId: number) {
  activeTab.value = 'CATEGORY';
  selectedCategoryId.value = categoryId;
  loadCredentials();
}

function onSearchInput() {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    loadCredentials();
  }, 300);
}

function clearSearch() {
  searchQuery.value = '';
  loadCredentials();
}

// Quick Copy Helper
async function copyQuick(text: string, fieldKey: string, fieldLabel: string) {
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
    copiedField.value = fieldKey;
    toast.success(`Đã sao chép ${fieldLabel}`);
    setTimeout(() => {
      if (copiedField.value === fieldKey) {
        copiedField.value = null;
      }
    }, 2000);
  } catch {
    toast.error('Không thể sao chép');
  }
}

// Toggle Favorite
async function toggleFavorite(item: AccountCredential) {
  try {
    await vaultService.toggleFavorite(item.id);
    item.isFavorite = !item.isFavorite;
    toast.success(item.isFavorite ? 'Đã thêm vào Yêu thích' : 'Đã bỏ yêu thích');
    loadSummary();
    if (activeTab.value === 'FAVORITES' && !item.isFavorite) {
      credentials.value = credentials.value.filter((c) => c.id !== item.id);
    }
  } catch (err) {
    toast.error('Không thể cập nhật yêu thích');
  }
}

// Trash / Restore
async function moveToTrash(id: number) {
  if (!confirm('Chuyển tài khoản vào thùng rác? Bạn có thể khôi phục lại bất kỳ lúc nào.')) return;
  try {
    await vaultService.moveToTrash(id);
    toast.success('Đã chuyển tài khoản vào thùng rác');
    loadCredentials();
    loadSummary();
  } catch (err) {
    toast.error('Không thể chuyển vào thùng rác');
  }
}

async function restoreItem(id: number) {
  try {
    await vaultService.restoreFromTrash(id);
    toast.success('Đã khôi phục tài khoản');
    loadCredentials();
    loadSummary();
  } catch (err) {
    toast.error('Không thể khôi phục tài khoản');
  }
}

// Modal open/close handlers
function openAddModal() {
  editingCredentialId.value = null;
  showAddEditModal.value = true;
}

function openEditModal(id: number) {
  editingCredentialId.value = id;
  showAddEditModal.value = true;
}

function closeAddEditModal() {
  showAddEditModal.value = false;
  editingCredentialId.value = null;
}

function onCredentialSaved() {
  closeAddEditModal();
  loadCredentials();
  loadSummary();
}

function openDetailModal(id: number) {
  detailCredentialId.value = id;
  showDetailModal.value = true;
}

function closeDetailModal() {
  showDetailModal.value = false;
  detailCredentialId.value = null;
}

function onEditFromDetail(id: number) {
  closeDetailModal();
  openEditModal(id);
}

function onDetailChanged() {
  loadCredentials();
  loadSummary();
}

// Category Count helper from summary
function getCategoryCount(catId: number): number {
  const found = summary.categoryBreakdowns?.find((c) => c.categoryId === catId);
  return found ? found.count : 0;
}

// Helper icons & styles
function getCategoryIcon(code?: string) {
  switch (code) {
    case 'EMAIL':
      return Mail;
    case 'SOCIAL':
      return Share2;
    case 'WORK':
      return Briefcase;
    case 'GAMING':
      return Gamepad2;
    case 'DEV_SERVER':
      return Server;
    case 'FINANCE':
      return ShieldCheck;
    case 'SHOPPING':
      return ShoppingCart;
    case 'STUDY':
      return GraduationCap;
    default:
      return Globe;
  }
}

function getCategoryBadgeClass(code?: string): string {
  switch (code) {
    case 'EMAIL':
      return 'bg-blue-50 text-blue-600 border-blue-200';
    case 'SOCIAL':
      return 'bg-indigo-50 text-indigo-600 border-indigo-200';
    case 'WORK':
      return 'bg-amber-50 text-amber-600 border-amber-200';
    case 'GAMING':
      return 'bg-purple-50 text-purple-600 border-purple-200';
    case 'DEV_SERVER':
      return 'bg-emerald-50 text-emerald-600 border-emerald-200';
    case 'FINANCE':
      return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    case 'SHOPPING':
      return 'bg-rose-50 text-rose-600 border-rose-200';
    case 'STUDY':
      return 'bg-teal-50 text-teal-600 border-teal-200';
    default:
      return 'bg-slate-100 text-slate-700 border-slate-200';
  }
}

function getStrengthBadge(strength?: string) {
  switch (strength) {
    case 'VERY_STRONG':
      return { label: 'Rất mạnh', class: 'bg-emerald-100 text-emerald-800' };
    case 'STRONG':
      return { label: 'Mạnh', class: 'bg-emerald-50 text-emerald-700' };
    case 'MEDIUM':
      return { label: 'Trung bình', class: 'bg-amber-100 text-amber-800' };
    case 'WEAK':
    default:
      return { label: 'Yếu', class: 'bg-rose-100 text-rose-800' };
  }
}

function ensureHttp(url: string): string {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  return 'https://' + url;
}

function formatDateSimple(dateStr?: string): string {
  if (!dateStr) return '';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN');
  } catch {
    return dateStr;
  }
}
</script>
