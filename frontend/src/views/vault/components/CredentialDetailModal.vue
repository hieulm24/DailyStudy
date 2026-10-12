<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-5 bg-slate-900/60 backdrop-blur-xs animate-in fade-in duration-150"
  >
    <div
      class="bg-white rounded-md border border-slate-200 shadow-2xl w-full max-w-5xl xl:max-w-6xl max-h-[94vh] flex flex-col overflow-hidden animate-in zoom-in-95 duration-150"
    >
      <!-- Loading State -->
      <div v-if="isLoading" class="p-12 flex flex-col items-center justify-center gap-3">
        <Loader2 class="w-8 h-8 text-indigo-600 animate-spin" />
        <span class="text-sm font-medium text-slate-500">Đang tải thông tin tài khoản...</span>
      </div>

      <!-- Content State -->
      <template v-else-if="credential">
        <!-- Modal Top Bar Header -->
        <div class="px-6 py-4.5 border-b border-slate-100 bg-slate-50/70 flex items-center justify-between shrink-0">
          <div class="flex items-center gap-3.5 min-w-0">
            <!-- Platform Avatar / Initial -->
            <div
              :class="[
                'w-12 h-12 rounded-md flex items-center justify-center font-bold text-base shrink-0 shadow-xs border',
                getCategoryStyle(credential.categoryCode).badgeClass,
              ]"
            >
              <component :is="getCategoryStyle(credential.categoryCode).icon" class="w-6 h-6" />
            </div>

            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <h3 class="text-base sm:text-lg font-bold text-slate-900 truncate" :title="credential.title">
                  {{ credential.title }}
                </h3>
                <button
                  type="button"
                  :class="[
                    'p-1 rounded-md transition-colors cursor-pointer shrink-0',
                    credential.isFavorite
                      ? 'text-amber-500 hover:text-amber-600'
                      : 'text-slate-300 hover:text-amber-400',
                  ]"
                  :title="credential.isFavorite ? 'Bỏ yêu thích' : 'Đánh dấu yêu thích'"
                  @click="handleToggleFavorite"
                >
                  <Star :class="['w-4.5 h-4.5', credential.isFavorite ? 'fill-amber-400' : '']" />
                </button>
              </div>

              <div class="flex items-center gap-2 text-xs text-slate-500 mt-0.5 flex-wrap">
                <span v-if="credential.serviceName" class="font-medium text-slate-700">
                  {{ credential.serviceName }}
                </span>
                <span v-if="credential.serviceName && credential.categoryName" class="text-slate-300">•</span>
                <span
                  v-if="credential.categoryName"
                  class="px-2 py-0.5 rounded-md font-semibold text-[11px] bg-indigo-50 text-indigo-700 border border-indigo-100"
                >
                  {{ credential.categoryName }}
                </span>
                <span v-if="credential.isDeleted" class="px-2 py-0.5 rounded-md font-semibold text-[11px] bg-rose-50 text-rose-700 border border-rose-200">
                  Trong thùng rác
                </span>
              </div>
            </div>
          </div>

          <!-- Top Action Buttons -->
          <div class="flex items-center gap-1.5 shrink-0">
            <button
              v-if="!credential.isDeleted"
              type="button"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-md bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border border-indigo-200 transition-colors cursor-pointer"
              @click="$emit('edit', credential.id)"
            >
              <Pencil class="w-3.5 h-3.5" />
              <span class="hidden sm:inline">Chỉnh sửa</span>
            </button>
            <button
              type="button"
              class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
              @click="$emit('close')"
            >
              <X class="w-5 h-5" />
            </button>
          </div>
        </div>

        <!-- Scrollable Modal Body -->
        <div class="p-6 overflow-y-auto space-y-6 flex-1 text-slate-700 text-sm">
          <!-- 1. Primary Credentials Card -->
          <div class="bg-slate-50/70 border border-slate-200/90 rounded-md p-5 space-y-4">
            <h4 class="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
              <KeyRound class="w-3.5 h-3.5 text-indigo-600" />
              Thông tin đăng nhập chính
            </h4>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-3.5">
              <!-- Username -->
              <div v-if="credential.username" class="bg-white p-3 rounded-md border border-slate-200/80 shadow-xs">
                <span class="text-[11px] font-semibold text-slate-400 block mb-0.5">Tên đăng nhập / Nickname</span>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-semibold text-slate-900 truncate select-all">{{ credential.username }}</span>
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer shrink-0"
                    title="Sao chép tên đăng nhập"
                    @click="copyText(credential.username!, 'username', 'Tên đăng nhập')"
                  >
                    <Check v-if="copiedField === 'username'" class="w-3.5 h-3.5 text-emerald-600" />
                    <Copy v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <!-- Email -->
              <div v-if="credential.email" class="bg-white p-3 rounded-lg border border-slate-200/80 shadow-2xs">
                <span class="text-[11px] font-semibold text-slate-400 block mb-0.5">Email tài khoản</span>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-semibold text-slate-900 truncate select-all">{{ credential.email }}</span>
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer shrink-0"
                    title="Sao chép email"
                    @click="copyText(credential.email!, 'email', 'Email')"
                  >
                    <Check v-if="copiedField === 'email'" class="w-3.5 h-3.5 text-emerald-600" />
                    <Copy v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <!-- Phone -->
              <div v-if="credential.phoneNumber" class="bg-white p-3 rounded-lg border border-slate-200/80 shadow-2xs">
                <span class="text-[11px] font-semibold text-slate-400 block mb-0.5">Số điện thoại</span>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-semibold text-slate-900 truncate select-all">{{ credential.phoneNumber }}</span>
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer shrink-0"
                    title="Sao chép số điện thoại"
                    @click="copyText(credential.phoneNumber!, 'phone', 'Số điện thoại')"
                  >
                    <Check v-if="copiedField === 'phone'" class="w-3.5 h-3.5 text-emerald-600" />
                    <Copy v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <!-- Password -->
              <div class="bg-white p-3 rounded-lg border border-slate-200/80 shadow-2xs md:col-span-2">
                <div class="flex items-center justify-between mb-0.5">
                  <span class="text-[11px] font-semibold text-slate-400">Mật khẩu</span>
                  <div class="flex items-center gap-1.5">
                    <span :class="['px-2 py-0.5 text-[10px] font-bold rounded-sm uppercase', passwordStrengthBadge.class]">
                      {{ passwordStrengthBadge.label }}
                    </span>
                    <span v-if="credential.passwordChangedAt" class="text-[10px] text-slate-400">
                      • Đổi: {{ formatDate(credential.passwordChangedAt) }}
                    </span>
                  </div>
                </div>

                <div class="flex items-center justify-between gap-2 bg-slate-50 px-3 py-2 rounded-md border border-slate-200/80">
                  <span class="font-mono text-sm font-semibold text-slate-900 tracking-wider break-all select-all">
                    {{ showPassword ? (credential.password || '••••••••') : '••••••••••••••••' }}
                  </span>

                  <div class="flex items-center gap-1 shrink-0">
                    <button
                      type="button"
                      class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-200/60 rounded-md transition-colors cursor-pointer"
                      :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                      @click="showPassword = !showPassword"
                    >
                      <EyeOff v-if="showPassword" class="w-4 h-4" />
                      <Eye v-else class="w-4 h-4" />
                    </button>
                    <button
                      type="button"
                      class="p-1.5 text-indigo-600 hover:text-indigo-700 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer"
                      title="Sao chép mật khẩu"
                      @click="copyText(credential.password || '', 'password', 'Mật khẩu')"
                    >
                      <Check v-if="copiedField === 'password'" class="w-4 h-4 text-emerald-600" />
                      <Copy v-else class="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>

              <!-- PIN code (if exists) -->
              <div v-if="credential.pinCode" class="bg-white p-3 rounded-lg border border-slate-200/80 shadow-2xs">
                <span class="text-[11px] font-semibold text-slate-400 block mb-0.5">Mã PIN / Mật mã giao dịch</span>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-mono font-semibold text-slate-900">
                    {{ showPin ? credential.pinCode : '••••••' }}
                  </span>
                  <div class="flex items-center gap-1">
                    <button
                      type="button"
                      class="p-1.5 text-slate-400 hover:text-slate-600 rounded-md transition-colors cursor-pointer"
                      @click="showPin = !showPin"
                    >
                      <EyeOff v-if="showPin" class="w-3.5 h-3.5" />
                      <Eye v-else class="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer"
                      title="Sao chép PIN"
                      @click="copyText(credential.pinCode!, 'pin', 'Mã PIN')"
                    >
                      <Check v-if="copiedField === 'pin'" class="w-3.5 h-3.5 text-emerald-600" />
                      <Copy v-else class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>

              <!-- Website URL -->
              <div v-if="credential.websiteUrl" class="bg-white p-3 rounded-lg border border-slate-200/80 shadow-2xs">
                <span class="text-[11px] font-semibold text-slate-400 block mb-0.5">Website đăng nhập</span>
                <div class="flex items-center justify-between gap-2">
                  <a
                    :href="ensureHttp(credential.websiteUrl)"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="text-indigo-600 hover:text-indigo-700 hover:underline truncate font-medium flex items-center gap-1"
                  >
                    <span class="truncate">{{ credential.websiteUrl }}</span>
                    <ExternalLink class="w-3 h-3 shrink-0" />
                  </a>
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer shrink-0"
                    title="Sao chép liên kết"
                    @click="copyText(credential.websiteUrl!, 'website', 'Đường dẫn')"
                  >
                    <Check v-if="copiedField === 'website'" class="w-3.5 h-3.5 text-emerald-600" />
                    <Copy v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <!-- Recovery Email -->
              <div v-if="credential.recoveryEmail" class="bg-white p-3 rounded-lg border border-slate-200/80 shadow-2xs">
                <span class="text-[11px] font-semibold text-slate-400 block mb-0.5">Email khôi phục (Recovery)</span>
                <div class="flex items-center justify-between gap-2">
                  <span class="font-medium text-slate-800 truncate select-all">{{ credential.recoveryEmail }}</span>
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer shrink-0"
                    title="Sao chép email khôi phục"
                    @click="copyText(credential.recoveryEmail!, 'recEmail', 'Email khôi phục')"
                  >
                    <Check v-if="copiedField === 'recEmail'" class="w-3.5 h-3.5 text-emerald-600" />
                    <Copy v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <!-- Linked Credential -->
              <div v-if="credential.linkedCredentialTitle" class="bg-white p-3 rounded-lg border border-slate-200/80 shadow-2xs">
                <span class="text-[11px] font-semibold text-slate-400 block mb-0.5">Tài khoản liên kết / Đăng ký qua</span>
                <div class="flex items-center gap-1.5 text-indigo-700 font-medium">
                  <Link2 class="w-3.5 h-3.5 shrink-0" />
                  <span class="truncate">{{ credential.linkedCredentialTitle }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 2. Security & 2FA Information -->
          <div
            v-if="credential.totpSecret || credential.recoveryCodes || credential.securityQuestions"
            class="bg-amber-50/50 border border-amber-200/80 rounded-md p-5 space-y-4"
          >
            <h4 class="text-xs font-bold uppercase tracking-wider text-amber-800 flex items-center gap-1.5">
              <ShieldCheck class="w-3.5 h-3.5 text-amber-600" />
              Bảo mật 2FA & Mã khôi phục dự phòng
            </h4>

            <div class="space-y-3">
              <!-- TOTP Secret -->
              <div v-if="credential.totpSecret" class="bg-white p-3 rounded-md border border-amber-200/60 shadow-xs">
                <div class="flex items-center justify-between mb-1">
                  <span class="text-[11px] font-semibold text-slate-500">Mã bí mật xác thực 2 bước (TOTP Secret Key)</span>
                  <button
                    type="button"
                    class="text-xs text-indigo-600 hover:text-indigo-800 font-medium cursor-pointer"
                    @click="showTotp = !showTotp"
                  >
                    {{ showTotp ? 'Ẩn mã' : 'Hiện mã' }}
                  </button>
                </div>
                <div class="flex items-center justify-between gap-2 bg-slate-50 px-3 py-1.5 rounded-md">
                  <span class="font-mono text-xs font-semibold text-slate-800 break-all select-all">
                    {{ showTotp ? credential.totpSecret : '•••• •••• •••• ••••' }}
                  </span>
                  <button
                    type="button"
                    class="p-1 text-slate-400 hover:text-indigo-600 rounded-md cursor-pointer shrink-0"
                    title="Sao chép TOTP"
                    @click="copyText(credential.totpSecret!, 'totp', 'Mã 2FA')"
                  >
                    <Check v-if="copiedField === 'totp'" class="w-3.5 h-3.5 text-emerald-600" />
                    <Copy v-else class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <!-- Recovery Codes -->
              <div v-if="credential.recoveryCodes" class="bg-white p-3 rounded-md border border-amber-200/60 shadow-xs">
                <div class="flex items-center justify-between mb-1.5">
                  <span class="text-[11px] font-semibold text-slate-500">Mã khôi phục dự phòng (Backup / Recovery Codes)</span>
                  <button
                    type="button"
                    class="inline-flex items-center gap-1 text-xs text-indigo-600 hover:text-indigo-800 font-medium cursor-pointer"
                    @click="copyText(credential.recoveryCodes!, 'recoveryCodes', 'Mã dự phòng')"
                  >
                    <Check v-if="copiedField === 'recoveryCodes'" class="w-3 h-3 text-emerald-600" />
                    <Copy v-else class="w-3 h-3" />
                    <span>Sao chép tất cả</span>
                  </button>
                </div>
                <pre class="bg-slate-50 p-2.5 rounded-md border border-slate-200/80 font-mono text-xs text-slate-800 whitespace-pre-wrap select-all">{{ credential.recoveryCodes }}</pre>
              </div>

              <!-- Security Questions -->
              <div v-if="credential.securityQuestions" class="bg-white p-3 rounded-md border border-amber-200/60 shadow-xs">
                <span class="text-[11px] font-semibold text-slate-500 block mb-1">Câu hỏi bảo mật</span>
                <p class="text-xs text-slate-700 whitespace-pre-wrap bg-slate-50 p-2.5 rounded-md border border-slate-200/80">
                  {{ credential.securityQuestions }}
                </p>
              </div>
            </div>
          </div>

          <!-- 3. Custom Fields (if any) -->
          <div v-if="parsedCustomFields.length > 0" class="bg-slate-50/70 border border-slate-200/90 rounded-md p-5 space-y-3">
            <h4 class="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
              <FolderPlus class="w-3.5 h-3.5 text-indigo-600" />
              Trường thông tin bổ sung (Tùy biến)
            </h4>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              <div
                v-for="(cf, idx) in parsedCustomFields"
                :key="idx"
                class="bg-white p-2.5 rounded-md border border-slate-200/80 shadow-xs flex items-center justify-between gap-2"
              >
                <div class="min-w-0">
                  <span class="text-[11px] font-semibold text-slate-400 block truncate">{{ cf.key }}</span>
                  <span class="text-xs font-semibold text-slate-800 truncate block select-all">{{ cf.value }}</span>
                </div>
                <button
                  type="button"
                  class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer shrink-0"
                  :title="`Sao chép ${cf.key}`"
                  @click="copyText(cf.value, `cf_${idx}`, cf.key)"
                >
                  <Check v-if="copiedField === `cf_${idx}`" class="w-3.5 h-3.5 text-emerald-600" />
                  <Copy v-else class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>

          <!-- 4. Notes / Ghi chú -->
          <div v-if="credential.notes" class="bg-slate-50/70 border border-slate-200/90 rounded-md p-5 space-y-2">
            <h4 class="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
              <FileText class="w-3.5 h-3.5 text-indigo-600" />
              Ghi chú bảo mật
            </h4>
            <p class="text-xs text-slate-700 whitespace-pre-wrap bg-white p-3 rounded-md border border-slate-200/80 leading-relaxed shadow-xs">
              {{ credential.notes }}
            </p>
          </div>

          <!-- 5. Tệp tin đính kèm (Full file support: Photo, PDF, Key, PEM, ZIP, Word, TXT...) -->
          <div class="bg-slate-50/70 border border-slate-200/90 rounded-md p-5 space-y-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <Paperclip class="w-4 h-4 text-indigo-600" />
                <h4 class="text-xs font-bold uppercase tracking-wider text-slate-700">
                  Tệp tin đính kèm bảo mật
                </h4>
                <span class="px-2 py-0.5 text-[11px] font-bold rounded-full bg-indigo-100 text-indigo-700">
                  {{ credential.attachments ? credential.attachments.length : 0 }}
                </span>
              </div>

              <!-- Upload button trigger -->
              <div>
                <input
                  ref="fileUploadInput"
                  type="file"
                  multiple
                  class="hidden"
                  @change="onFileSelectedForUpload"
                />
                <button
                  type="button"
                  :disabled="isUploadingAttachment"
                  class="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold rounded-md bg-white border border-slate-200 hover:bg-indigo-50 hover:text-indigo-700 hover:border-indigo-300 transition-colors shadow-xs cursor-pointer text-slate-700 disabled:opacity-50"
                  @click="fileUploadInput?.click()"
                >
                  <Loader2 v-if="isUploadingAttachment" class="w-3.5 h-3.5 animate-spin text-indigo-600" />
                  <UploadCloud v-else class="w-3.5 h-3.5 text-indigo-600" />
                  <span>{{ isUploadingAttachment ? 'Đang tải...' : '+ Tải thêm tệp' }}</span>
                </button>
              </div>
            </div>

            <!-- List of attachments -->
            <div v-if="credential.attachments && credential.attachments.length > 0" class="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              <div
                v-for="att in credential.attachments"
                :key="att.id"
                class="bg-white p-3 rounded-md border border-slate-200 hover:border-indigo-200 transition-colors flex items-center justify-between gap-2.5 group shadow-xs"
              >
                <!-- File Icon & Details -->
                <div class="flex items-center gap-2.5 min-w-0 flex-1">
                  <div class="p-2 rounded-md bg-slate-100 text-slate-600 shrink-0 group-hover:bg-indigo-50 group-hover:text-indigo-600 transition-colors">
                    <component :is="getFileIcon(att.fileName)" class="w-4.5 h-4.5" />
                  </div>
                  <div class="min-w-0 flex-1">
                    <span class="text-xs font-semibold text-slate-800 truncate block" :title="att.fileName">
                      {{ att.fileName }}
                    </span>
                    <div class="flex items-center gap-2 text-[10px] text-slate-400 mt-0.5">
                      <span>{{ formatFileSize(att.fileSize) }}</span>
                      <span>•</span>
                      <span>{{ formatDate(att.createdAt) }}</span>
                    </div>
                  </div>
                </div>

                <!-- Attachment Actions -->
                <div class="flex items-center gap-1 shrink-0">
                  <!-- Preview Button -->
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer"
                    title="Xem trước tệp"
                    @click="previewAttachment(att)"
                  >
                    <Eye class="w-3.5 h-3.5" />
                  </button>

                  <!-- Download Button -->
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-md transition-colors cursor-pointer"
                    title="Tải tệp về máy"
                    @click="downloadAttachment(att)"
                  >
                    <Download class="w-3.5 h-3.5" />
                  </button>

                  <!-- Delete Button -->
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors cursor-pointer"
                    title="Xóa tệp đính kèm này"
                    @click="confirmDeleteAttachment(att.id)"
                  >
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Empty attachments placeholder -->
            <div
              v-else
              class="border border-dashed border-slate-200 rounded-md p-5 text-center text-xs text-slate-400 bg-white cursor-pointer hover:border-indigo-300 hover:bg-indigo-50/20 transition-colors"
              @click="fileUploadInput?.click()"
            >
              <UploadCloud class="w-6 h-6 mx-auto mb-1 text-slate-300" />
              <p class="font-medium text-slate-600">Chưa có tệp tin đính kèm</p>
              <p class="text-[11px] text-slate-400 mt-0.5">Hỗ trợ đầy đủ: Ảnh CCCD/CMND, PDF, Chứng chỉ PEM/Key, File mã QR, ZIP, Backup Codes...</p>
            </div>
          </div>

          <!-- 6. Lịch sử thay đổi mật khẩu (Password History) -->
          <div
            v-if="credential.passwordHistories && credential.passwordHistories.length > 0"
            class="bg-slate-50/70 border border-slate-200/90 rounded-md p-5 space-y-3"
          >
            <div
              class="flex items-center justify-between cursor-pointer select-none"
              @click="showPasswordHistory = !showPasswordHistory"
            >
              <div class="flex items-center gap-2">
                <History class="w-4 h-4 text-indigo-600" />
                <h4 class="text-xs font-bold uppercase tracking-wider text-slate-700">
                  Lịch sử mật khẩu cũ
                </h4>
                <span class="px-2 py-0.5 text-[11px] font-bold rounded-full bg-slate-200 text-slate-700">
                  {{ credential.passwordHistories.length }}
                </span>
              </div>
              <ChevronDown
                :class="['w-4 h-4 text-slate-400 transition-transform duration-200', showPasswordHistory ? 'rotate-180' : '']"
              />
            </div>

            <div v-show="showPasswordHistory" class="space-y-2 pt-1 border-t border-slate-200/70">
              <div
                v-for="ph in credential.passwordHistories"
                :key="ph.id"
                class="bg-white p-2.5 rounded-md border border-slate-200/80 text-xs flex items-center justify-between gap-3 shadow-xs"
              >
                <div class="min-w-0">
                  <div class="flex items-center gap-2">
                    <span class="font-mono font-medium text-slate-800">
                      {{ revealedOldPasswords[ph.id] ? ph.oldPassword : '••••••••••••' }}
                    </span>
                    <button
                      type="button"
                      class="text-slate-400 hover:text-slate-600 cursor-pointer"
                      @click="revealedOldPasswords[ph.id] = !revealedOldPasswords[ph.id]"
                    >
                      <EyeOff v-if="revealedOldPasswords[ph.id]" class="w-3 h-3" />
                      <Eye v-else class="w-3 h-3" />
                    </button>
                    <button
                      type="button"
                      class="text-slate-400 hover:text-indigo-600 cursor-pointer"
                      title="Sao chép mật khẩu cũ"
                      @click="copyText(ph.oldPassword, `old_${ph.id}`, 'Mật khẩu cũ')"
                    >
                      <Check v-if="copiedField === `old_${ph.id}`" class="w-3 h-3 text-emerald-600" />
                      <Copy v-else class="w-3 h-3" />
                    </button>
                  </div>
                  <div class="text-[10px] text-slate-400 mt-0.5 flex items-center gap-2">
                    <span>{{ formatDate(ph.changedAt) }}</span>
                    <span v-if="ph.changedReason">• Lý do: {{ ph.changedReason }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Modal Footer Actions -->
        <div class="px-6 py-3.5 bg-slate-50 border-t border-slate-200/80 flex items-center justify-between shrink-0">
          <!-- Trash or Restore Action -->
          <div>
            <template v-if="credential.isDeleted">
              <div class="flex items-center gap-2">
                <button
                  type="button"
                  class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-md bg-emerald-600 text-white hover:bg-emerald-700 transition-colors cursor-pointer"
                  @click="handleRestore"
                >
                  <RotateCcw class="w-3.5 h-3.5" />
                  <span>Khôi phục tài khoản</span>
                </button>
                <button
                  type="button"
                  class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-md bg-rose-50 text-rose-700 border border-rose-200 hover:bg-rose-100 transition-colors cursor-pointer"
                  @click="showPermanentDeleteConfirm = true"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                  <span>Xóa vĩnh viễn</span>
                </button>
              </div>
            </template>
            <template v-else>
              <button
                type="button"
                class="inline-flex items-center gap-1.5 px-2.5 py-1.5 text-xs font-medium text-slate-500 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors cursor-pointer"
                @click="showTrashConfirm = true"
              >
                <Trash2 class="w-3.5 h-3.5" />
                <span>Chuyển vào thùng rác</span>
              </button>
            </template>
          </div>

          <div class="flex items-center gap-2">
            <button
              v-if="!credential.isDeleted"
              type="button"
              class="px-4 py-2 text-xs font-semibold rounded-md bg-indigo-600 text-white hover:bg-indigo-700 shadow-xs transition-colors cursor-pointer"
              @click="$emit('edit', credential.id)"
            >
              Chỉnh sửa thông tin
            </button>
            <button
              type="button"
              class="px-4 py-2 text-xs font-semibold rounded-md border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 transition-colors cursor-pointer"
              @click="$emit('close')"
            >
              Đóng
            </button>
          </div>
        </div>
      </template>
    </div>

    <!-- Lightbox / Preview Submodal for Attachments -->
    <div
      v-if="previewState.isOpen"
      class="fixed inset-0 z-60 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-150"
      @click.self="closePreview"
    >
      <div class="bg-white rounded-md shadow-2xl max-w-4xl max-h-[90vh] w-full flex flex-col overflow-hidden border border-slate-700">
        <!-- Preview Header -->
        <div class="px-5 py-3 bg-slate-900 text-white flex items-center justify-between shrink-0">
          <div class="flex items-center gap-2 min-w-0">
            <component :is="getFileIcon(previewState.fileName)" class="w-4.5 h-4.5 text-indigo-400 shrink-0" />
            <span class="text-sm font-semibold truncate">{{ previewState.fileName }}</span>
          </div>
          <div class="flex items-center gap-2">
            <button
              type="button"
              class="inline-flex items-center gap-1.5 px-3 py-1 text-xs font-medium rounded-md bg-indigo-600 hover:bg-indigo-700 text-white cursor-pointer"
              @click="downloadAttachment(previewState.attachment!)"
            >
              <Download class="w-3.5 h-3.5" />
              <span>Tải về</span>
            </button>
            <button
              type="button"
              class="p-1.5 text-slate-400 hover:text-white rounded-md cursor-pointer"
              @click="closePreview"
            >
              <X class="w-4.5 h-4.5" />
            </button>
          </div>
        </div>

        <!-- Preview Body -->
        <div class="p-4 overflow-auto flex-1 flex items-center justify-center bg-slate-950/20 min-h-[300px]">
          <div v-if="previewState.loading" class="flex flex-col items-center gap-2 text-slate-500">
            <Loader2 class="w-7 h-7 animate-spin text-indigo-600" />
            <span class="text-xs">Đang tải xem trước...</span>
          </div>
          <template v-else>
            <!-- Image Preview -->
            <img
              v-if="previewState.isImage"
              :src="previewState.blobUrl"
              class="max-w-full max-h-[75vh] object-contain rounded-md shadow-md"
              alt="Preview"
            />
            <!-- PDF Preview -->
            <iframe
              v-else-if="previewState.isPdf"
              :src="previewState.blobUrl"
              class="w-full h-[75vh] rounded-md border border-slate-200"
            />
            <!-- Text/Code Preview -->
            <pre
              v-else-if="previewState.isText"
              class="w-full h-full max-h-[70vh] p-4 bg-slate-900 text-emerald-400 rounded-md font-mono text-xs overflow-auto whitespace-pre-wrap select-all"
            >{{ previewState.textContent }}</pre>
            <!-- Other file fallback -->
            <div v-else class="text-center p-8 text-slate-600">
              <FileText class="w-12 h-12 mx-auto text-slate-400 mb-2" />
              <p class="font-medium text-sm">Định dạng tệp này không hỗ trợ xem trực tiếp trong trình duyệt</p>
              <p class="text-xs text-slate-400 mt-1 mb-4">Vui lòng tải tệp tin về thiết bị để mở bằng ứng dụng chuyên dụng.</p>
              <button
                type="button"
                class="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold rounded-md bg-indigo-600 text-white hover:bg-indigo-700 cursor-pointer"
                @click="downloadAttachment(previewState.attachment!)"
              >
                <Download class="w-4 h-4" />
                <span>Tải về tệp tin</span>
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- Confirm Move to Trash Dialog -->
    <AppConfirmDialog
      v-model="showTrashConfirm"
      title="Chuyển vào thùng rác"
      message="Chuyển tài khoản này vào thùng rác? Bạn có thể khôi phục lại bất kỳ lúc nào."
      confirm-text="Chuyển vào thùng rác"
      confirm-variant="danger"
      type="warning"
      :loading="isActionLoading"
      @confirm="executeMoveToTrash"
    />

    <!-- Confirm Permanent Delete Dialog -->
    <AppConfirmDialog
      v-model="showPermanentDeleteConfirm"
      title="Xác nhận xóa vĩnh viễn"
      message="CẢNH BÁO: Xóa vĩnh viễn tài khoản và tất cả tệp đính kèm liên quan? Hành động này KHÔNG THỂ khôi phục lại!"
      confirm-text="Xóa vĩnh viễn"
      confirm-variant="danger"
      type="error"
      :loading="isActionLoading"
      @confirm="executePermanentDelete"
    />

    <!-- Confirm Delete Attachment Dialog -->
    <AppConfirmDialog
      v-model="showDeleteAttachmentConfirm"
      title="Xóa tệp đính kèm"
      message="Bạn có chắc chắn muốn xóa tệp đính kèm này không?"
      confirm-text="Xóa tệp"
      confirm-variant="danger"
      type="warning"
      :loading="isActionLoading"
      @confirm="executeDeleteAttachment"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import AppConfirmDialog from '../../../components/common/AppConfirmDialog.vue';
import {
  X,
  Star,
  Pencil,
  Copy,
  Check,
  Eye,
  EyeOff,
  ExternalLink,
  KeyRound,
  ShieldCheck,
  FolderPlus,
  FileText,
  Paperclip,
  Download,
  Trash2,
  UploadCloud,
  History,
  ChevronDown,
  RotateCcw,
  Link2,
  Loader2,
  FileCode,
  FileArchive,
  FileImage,
  Globe,
  Mail,
  Gamepad2,
  Briefcase,
  Share2,
  Server,
} from 'lucide-vue-next';
import { vaultService } from '../../../services/vault.service';
import { useToastStore } from '../../../stores/toast.store';
import type {
  AccountCredentialDetail,
  AccountAttachment,
} from '../../../types/vault.types';

const props = defineProps<{
  isOpen: boolean;
  credentialId?: number | null;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'edit', id: number): void;
  (e: 'changed'): void;
}>();

const toast = useToastStore();
const isLoading = ref(false);
const credential = ref<AccountCredentialDetail | null>(null);

const showPassword = ref(false);
const showPin = ref(false);
const showTotp = ref(false);
const showPasswordHistory = ref(false);
const revealedOldPasswords = reactive<Record<number, boolean>>({});
const copiedField = ref<string | null>(null);

const fileUploadInput = ref<HTMLInputElement | null>(null);
const isUploadingAttachment = ref(false);

// Attachment preview state
const previewState = reactive<{
  isOpen: boolean;
  loading: boolean;
  fileName: string;
  blobUrl: string;
  isImage: boolean;
  isPdf: boolean;
  isText: boolean;
  textContent: string;
  attachment: AccountAttachment | null;
}>({
  isOpen: false,
  loading: false,
  fileName: '',
  blobUrl: '',
  isImage: false,
  isPdf: false,
  isText: false,
  textContent: '',
  attachment: null,
});

// Watch opening and fetch detail
watch(
  () => [props.isOpen, props.credentialId],
  async ([open, id]) => {
    if (open && id) {
      showPassword.value = false;
      showPin.value = false;
      showTotp.value = false;
      showPasswordHistory.value = false;
      await fetchCredentialDetail(Number(id));
    } else {
      credential.value = null;
    }
  },
  { immediate: true }
);

async function fetchCredentialDetail(id: number) {
  isLoading.value = true;
  try {
    const data = await vaultService.getCredentialDetail(id);
    credential.value = data;
  } catch (err: any) {
    toast.error('Không thể tải chi tiết tài khoản: ' + (err.response?.data?.message || err.message));
    emit('close');
  } finally {
    isLoading.value = false;
  }
}

// 1-Click Copy Helper
async function copyText(text: string, fieldKey: string, fieldLabel: string) {
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
  } catch (e) {
    toast.error('Không thể sao chép vào bộ nhớ tạm');
  }
}

// Ensure http prefix
function ensureHttp(url: string): string {
  if (!url) return '';
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  return 'https://' + url;
}

// Format date helper
function formatDate(dateStr?: string): string {
  if (!dateStr) return '';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return dateStr;
  }
}

// Format file size
function formatFileSize(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  let size = bytes;
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024;
    i++;
  }
  return `${size.toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
}

// Strength Badge
const passwordStrengthBadge = computed(() => {
  const strength = credential.value?.passwordStrength || 'MEDIUM';
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
});

// Category Style Map
function getCategoryStyle(code?: string) {
  switch (code) {
    case 'EMAIL':
      return { icon: Mail, badgeClass: 'bg-blue-50 text-blue-600 border-blue-200' };
    case 'SOCIAL':
      return { icon: Share2, badgeClass: 'bg-indigo-50 text-indigo-600 border-indigo-200' };
    case 'WORK':
      return { icon: Briefcase, badgeClass: 'bg-amber-50 text-amber-600 border-amber-200' };
    case 'GAMING':
      return { icon: Gamepad2, badgeClass: 'bg-purple-50 text-purple-600 border-purple-200' };
    case 'DEV_SERVER':
      return { icon: Server, badgeClass: 'bg-emerald-50 text-emerald-600 border-emerald-200' };
    default:
      return { icon: Globe, badgeClass: 'bg-slate-100 text-slate-700 border-slate-200' };
  }
}

// File Icon determination
function getFileIcon(fileName: string) {
  const ext = fileName.split('.').pop()?.toLowerCase() || '';
  if (['png', 'jpg', 'jpeg', 'webp', 'svg', 'gif'].includes(ext)) return FileImage;
  if (['pdf'].includes(ext)) return FileText;
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return FileArchive;
  if (['key', 'pem', 'crt', 'json', 'yaml', 'xml', 'sql', 'sh', 'env'].includes(ext)) return FileCode;
  return FileText;
}

// Custom fields JSON parsing
const parsedCustomFields = computed(() => {
  if (!credential.value?.customFields) return [];
  try {
    const parsed = JSON.parse(credential.value.customFields);
    if (Array.isArray(parsed)) return parsed;
    if (typeof parsed === 'object') {
      return Object.entries(parsed).map(([key, value]) => ({ key, value: String(value) }));
    }
  } catch (e) {
    // If not JSON, return as single raw field
    return [{ key: 'Dữ liệu', value: credential.value.customFields }];
  }
  return [];
});

// Favorite toggle
async function handleToggleFavorite() {
  if (!credential.value) return;
  try {
    await vaultService.toggleFavorite(credential.value.id);
    credential.value.isFavorite = !credential.value.isFavorite;
    toast.success(credential.value.isFavorite ? 'Đã thêm vào danh sách Yêu thích' : 'Đã bỏ yêu thích');
    emit('changed');
  } catch (err: any) {
    toast.error('Không thể cập nhật yêu thích');
  }
}

// Trash / Restore / Delete Confirm Dialog States
const showTrashConfirm = ref(false);
const showPermanentDeleteConfirm = ref(false);
const showDeleteAttachmentConfirm = ref(false);
const attachmentToDeleteId = ref<number | null>(null);
const isActionLoading = ref(false);

async function executeMoveToTrash() {
  if (!credential.value) return;
  isActionLoading.value = true;
  try {
    await vaultService.moveToTrash(credential.value.id);
    toast.success('Đã chuyển tài khoản vào thùng rác');
    showTrashConfirm.value = false;
    emit('changed');
    emit('close');
  } catch (err: any) {
    toast.error('Không thể chuyển vào thùng rác');
  } finally {
    isActionLoading.value = false;
  }
}

async function handleRestore() {
  if (!credential.value) return;
  try {
    await vaultService.restoreFromTrash(credential.value.id);
    toast.success('Đã khôi phục tài khoản thành công');
    emit('changed');
    emit('close');
  } catch (err: any) {
    toast.error('Không thể khôi phục tài khoản');
  }
}

async function executePermanentDelete() {
  if (!credential.value) return;
  isActionLoading.value = true;
  try {
    await vaultService.deletePermanently(credential.value.id);
    toast.success('Đã xóa vĩnh viễn tài khoản');
    showPermanentDeleteConfirm.value = false;
    emit('changed');
    emit('close');
  } catch (err: any) {
    toast.error('Không thể xóa tài khoản');
  } finally {
    isActionLoading.value = false;
  }
}

// Upload attachments directly from detail modal
async function onFileSelectedForUpload(event: Event) {
  const input = event.target as HTMLInputElement;
  if (!input.files || input.files.length === 0 || !credential.value) return;

  isUploadingAttachment.value = true;
  try {
    for (let i = 0; i < input.files.length; i++) {
      const file = input.files[i];
      const newAtt = await vaultService.uploadAttachment(credential.value.id, file);
      credential.value.attachments.push(newAtt);
    }
    toast.success('Tải lên tệp đính kèm thành công');
    emit('changed');
  } catch (err: any) {
    toast.error('Không thể tải tệp lên: ' + (err.response?.data?.message || err.message));
  } finally {
    isUploadingAttachment.value = false;
    input.value = '';
  }
}

// Delete attachment
function confirmDeleteAttachment(attachmentId: number) {
  attachmentToDeleteId.value = attachmentId;
  showDeleteAttachmentConfirm.value = true;
}

async function executeDeleteAttachment() {
  if (!attachmentToDeleteId.value) return;
  isActionLoading.value = true;
  try {
    await vaultService.deleteAttachment(attachmentToDeleteId.value);
    if (credential.value) {
      credential.value.attachments = credential.value.attachments.filter((a) => a.id !== attachmentToDeleteId.value);
    }
    toast.success('Đã xóa tệp đính kèm');
    showDeleteAttachmentConfirm.value = false;
    attachmentToDeleteId.value = null;
    emit('changed');
  } catch (err: any) {
    toast.error('Không thể xóa tệp');
  } finally {
    isActionLoading.value = false;
  }
}

// Download attachment
async function downloadAttachment(att: AccountAttachment) {
  try {
    toast.info(`Đang chuẩn bị tải về ${att.fileName}...`);
    await vaultService.downloadAttachmentFile(att.id, att.fileName);
  } catch (err: any) {
    toast.error('Tải tệp thất bại: ' + (err.response?.data?.message || err.message));
  }
}

// Preview attachment
async function previewAttachment(att: AccountAttachment) {
  const ext = att.fileName.split('.').pop()?.toLowerCase() || '';
  const isImg = ['png', 'jpg', 'jpeg', 'webp', 'svg', 'gif'].includes(ext);
  const isPdfFile = ext === 'pdf';
  const isTxt = ['txt', 'key', 'pem', 'crt', 'json', 'yaml', 'xml', 'csv', 'md', 'env', 'sh'].includes(ext);

  previewState.isOpen = true;
  previewState.loading = true;
  previewState.fileName = att.fileName;
  previewState.attachment = att;
  previewState.isImage = isImg;
  previewState.isPdf = isPdfFile;
  previewState.isText = isTxt;
  previewState.textContent = '';
  previewState.blobUrl = '';

  try {
    const { url } = await vaultService.fetchAttachmentBlob(att.id);
    previewState.blobUrl = url;

    if (isTxt) {
      const res = await fetch(url);
      previewState.textContent = await res.text();
    }
  } catch (err: any) {
    toast.error('Không thể mở xem trước tệp: ' + (err.response?.data?.message || err.message));
    closePreview();
  } finally {
    previewState.loading = false;
  }
}

function closePreview() {
  if (previewState.blobUrl) {
    window.URL.revokeObjectURL(previewState.blobUrl);
  }
  previewState.isOpen = false;
  previewState.blobUrl = '';
  previewState.attachment = null;
}
</script>
