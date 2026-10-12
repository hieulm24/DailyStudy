<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-5 bg-slate-900/50 backdrop-blur-xs animate-in fade-in duration-150">
    <div class="bg-white rounded-md border border-slate-200 shadow-2xl w-full max-w-5xl xl:max-w-6xl max-h-[94vh] flex flex-col overflow-hidden animate-in zoom-in-95 duration-150">
      <!-- Modal Header -->
      <div class="flex items-center justify-between px-7 py-5 border-b border-slate-100 bg-slate-50/70 shrink-0">
        <div class="flex items-center gap-3.5">
          <div class="p-2.5 rounded-md bg-indigo-50 text-indigo-600 border border-indigo-100">
            <KeyRound class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-base sm:text-lg font-bold text-slate-900">
              {{ editingId ? 'Chỉnh sửa tài khoản' : 'Thêm tài khoản vào Két bảo mật' }}
            </h3>
            <p class="text-xs text-slate-500">Mật khẩu được mã hóa an toàn chuẩn AES-256 trên hệ thống</p>
          </div>
        </div>
        <button
          type="button"
          class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
          @click="$emit('close')"
        >
          <X class="w-5 h-5" />
        </button>
      </div>

      <!-- Quick Platform Chips (When adding new) -->
      <div v-if="!editingId" class="px-7 py-3 bg-slate-50 border-b border-slate-200/80 flex items-center gap-2 overflow-x-auto scrollbar-none shrink-0 text-xs">
        <span class="text-slate-400 font-semibold shrink-0">Nền tảng nhanh:</span>
        <button
          v-for="platform in POPULAR_PLATFORMS"
          :key="platform.name"
          type="button"
          class="px-2.5 py-1 rounded-md border border-slate-200 bg-white hover:bg-indigo-50 hover:text-indigo-700 hover:border-indigo-300 transition-colors text-slate-700 font-medium shrink-0 cursor-pointer shadow-xs"
          @click="applyPlatformTemplate(platform)"
        >
          {{ platform.name }}
        </button>
      </div>

      <!-- Modal Body (Scrollable) -->
      <form id="credentialForm" @submit.prevent="handleSubmit" class="p-7 overflow-y-auto space-y-6 flex-1">
        <!-- Section 1: Basic Identifiers -->
        <div class="space-y-4">
          <div class="grid grid-cols-1 sm:grid-cols-12 gap-4">
            <!-- Title -->
            <div class="sm:col-span-8">
              <label class="block text-xs font-bold text-slate-700 mb-1">
                Tiêu đề gợi nhớ <span class="text-rose-500">*</span>
              </label>
              <input
                v-model="form.title"
                type="text"
                required
                placeholder="VD: Facebook cá nhân, Gmail công việc, Steam game..."
                class="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 transition-colors"
              />
            </div>

            <!-- Category -->
            <div class="sm:col-span-4">
              <label class="block text-xs font-bold text-slate-700 mb-1">Danh mục</label>
              <select
                v-model="form.categoryId"
                class="w-full px-3 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 transition-colors"
              >
                <option :value="undefined">-- Chọn danh mục --</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <!-- Service Name -->
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">Dịch vụ / Nền tảng</label>
              <input
                v-model="form.serviceName"
                type="text"
                placeholder="VD: Google, Facebook, Steam, Garena, GitHub..."
                class="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 transition-colors"
              />
            </div>

            <!-- Website URL -->
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">Website đăng nhập</label>
              <input
                v-model="form.websiteUrl"
                type="text"
                placeholder="https://facebook.com/login"
                class="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 transition-colors"
              />
            </div>
          </div>
        </div>

        <!-- Section 2: Credentials (Username, Email, Phone & Password) -->
        <div class="p-5 bg-slate-50/80 border border-slate-200/90 rounded-md space-y-4">
          <div class="text-xs font-bold text-slate-900 flex items-center gap-2">
            <Lock class="w-4 h-4 text-indigo-600" />
            <span>Thông tin đăng nhập</span>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label class="block text-[11px] font-semibold text-slate-600 mb-1">Tên đăng nhập (Username)</label>
              <input
                v-model="form.username"
                type="text"
                placeholder="username / id"
                class="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600 transition-colors"
              />
            </div>
            <div>
              <label class="block text-[11px] font-semibold text-slate-600 mb-1">Email đăng nhập</label>
              <input
                v-model="form.email"
                type="text"
                placeholder="example@gmail.com"
                class="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600 transition-colors"
              />
            </div>
            <div>
              <label class="block text-[11px] font-semibold text-slate-600 mb-1">Số điện thoại liên kết</label>
              <input
                v-model="form.phoneNumber"
                type="text"
                placeholder="0912..."
                class="w-full px-3 py-2 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600 transition-colors"
              />
            </div>
          </div>

          <!-- Password with Eye Toggle -->
          <div class="space-y-1.5 pt-1">
            <label class="block text-xs font-bold text-slate-700">
              Mật khẩu <span class="text-rose-500">*</span>
            </label>

            <div class="relative">
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                placeholder="Nhập mật khẩu..."
                class="w-full pl-3.5 pr-10 py-2.5 text-sm font-mono bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-600 transition-colors"
                @input="evaluatePassword"
              />
              <div class="absolute right-2 top-1/2 -translate-y-1/2 flex items-center">
                <button
                  type="button"
                  class="p-1.5 text-slate-400 hover:text-slate-600 rounded-sm cursor-pointer"
                  :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                  @click="showPassword = !showPassword"
                >
                  <EyeOff v-if="showPassword" class="w-4 h-4" />
                  <Eye v-else class="w-4 h-4" />
                </button>
              </div>
            </div>

            <!-- Password Strength Bar -->
            <div v-if="form.password" class="flex items-center gap-2 pt-1 text-[11px]">
              <span class="text-slate-500 font-medium">Độ an toàn:</span>
              <div class="flex items-center gap-1 flex-1 max-w-xs">
                <div
                  v-for="i in 4"
                  :key="i"
                  :class="[
                    'h-1.5 flex-1 rounded-full transition-all',
                    i <= passwordLevel ? passwordBarColor : 'bg-slate-200'
                  ]"
                ></div>
              </div>
              <span :class="['font-bold', passwordTextColor]">{{ passwordLevelLabel }}</span>
            </div>

            <!-- Password Change Reason (If editing) -->
            <div v-if="editingId && isPasswordChanged" class="pt-2 animate-in fade-in duration-200">
              <label class="block text-[11px] font-semibold text-amber-700 mb-1">
                Lý do đổi mật khẩu (Lưu vào lịch sử):
              </label>
              <input
                v-model="form.passwordChangeReason"
                type="text"
                placeholder="VD: Đổi định kỳ, Nghi ngờ lộ pass, Quên pass..."
                class="w-full px-3 py-1.5 text-xs bg-amber-50/50 border border-amber-200 rounded-md focus:outline-hidden focus:border-amber-500"
              />
            </div>
          </div>
        </div>

        <!-- Section 3: Expandable Security & 2FA / Custom Fields / Notes -->
        <div class="space-y-3">
          <!-- Accordion Toggle Button -->
          <button
            type="button"
            class="w-full flex items-center justify-between p-3.5 bg-slate-50 hover:bg-slate-100/80 border border-slate-200 rounded-md transition-colors text-xs font-bold text-slate-800 cursor-pointer"
            @click="isSecurityExpanded = !isSecurityExpanded"
          >
            <div class="flex items-center gap-2">
              <ShieldCheck class="w-4 h-4 text-emerald-600" />
              <span>Bảo mật 2FA, Email khôi phục & Thông tin mở rộng</span>
              <span v-if="hasAdvancedData" class="px-2 py-0.5 rounded-full bg-indigo-100 text-indigo-700 text-[10px]">
                Đã điền
              </span>
            </div>
            <ChevronDown :class="['w-4 h-4 text-slate-400 transition-transform duration-200', isSecurityExpanded ? 'rotate-180' : '']" />
          </button>

          <!-- Accordion Content -->
          <div v-show="isSecurityExpanded" class="p-5 border border-slate-200 rounded-md space-y-4 bg-white animate-in fade-in duration-150">
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              <!-- Recovery Email -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Email khôi phục tài khoản</label>
                <input
                  v-model="form.recoveryEmail"
                  type="text"
                  placeholder="recovery@gmail.com"
                  class="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
                />
              </div>

              <!-- Linked Credential -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Liên kết tới tài khoản khác</label>
                <select
                  v-model="form.linkedCredentialId"
                  class="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
                >
                  <option :value="undefined">-- Không liên kết --</option>
                  <option
                    v-for="cred in allActiveCredentials.filter(c => c.id !== editingId)"
                    :key="cred.id"
                    :value="cred.id"
                  >
                    {{ cred.title }} ({{ cred.serviceName || cred.username || cred.email || 'Acc' }})
                  </option>
                </select>
              </div>

              <!-- PIN Code -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Mã PIN / Mật khẩu cấp 2</label>
                <input
                  v-model="form.pinCode"
                  type="text"
                  placeholder="Mã PIN 6 số hoặc mật khẩu cấp 2"
                  class="w-full px-3 py-2 text-xs font-mono bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
                />
              </div>

              <!-- TOTP Secret Key -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Khóa bí mật 2FA (TOTP Secret)</label>
                <input
                  v-model="form.totpSecret"
                  type="text"
                  placeholder="JBSWY3DPEHPK3PXP..."
                  class="w-full px-3 py-2 text-xs font-mono bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
                />
              </div>
            </div>

            <!-- 2-column for Backup Codes & Security Questions -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <!-- 2FA Backup Codes -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Mã dự phòng 2FA (Recovery / Backup Codes)</label>
                <textarea
                  v-model="form.recoveryCodes"
                  rows="2"
                  placeholder="Dán các mã dự phòng (mỗi dòng một mã)..."
                  class="w-full px-3 py-2 text-xs font-mono bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
                ></textarea>
              </div>

              <!-- Security Questions -->
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1">Câu hỏi & Câu trả lời bảo mật</label>
                <textarea
                  v-model="form.securityQuestions"
                  rows="2"
                  placeholder="VD: Con vật cưng đầu tiên: Miu; Ngày sinh khai báo: 01/01/1990..."
                  class="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
                ></textarea>
              </div>
            </div>

            <!-- Custom Fields (Key - Value) -->
            <div class="space-y-2 pt-2 border-t border-slate-100">
              <div class="flex items-center justify-between">
                <label class="text-xs font-semibold text-slate-700">Trường tùy biến mở rộng (Custom Fields)</label>
                <button
                  type="button"
                  class="text-[11px] font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1 cursor-pointer"
                  @click="addCustomField"
                >
                  <Plus class="w-3.5 h-3.5" />
                  <span>Thêm trường</span>
                </button>
              </div>

              <div v-for="(field, idx) in customFieldList" :key="idx" class="flex items-center gap-2">
                <input
                  v-model="field.key"
                  type="text"
                  placeholder="Tên trường (VD: Server, UID, Token...)"
                  class="w-1/3 px-3 py-1.5 text-xs bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
                />
                <input
                  v-model="field.value"
                  type="text"
                  placeholder="Giá trị"
                  class="flex-1 px-3 py-1.5 text-xs bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
                />
                <button
                  type="button"
                  class="p-1.5 text-slate-400 hover:text-rose-600 rounded cursor-pointer"
                  @click="removeCustomField(idx)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>

            <!-- Notes -->
            <div class="pt-2 border-t border-slate-100">
              <label class="block text-xs font-semibold text-slate-700 mb-1">Ghi chú bổ sung</label>
              <textarea
                v-model="form.notes"
                rows="3"
                placeholder="Ghi chú chi tiết cho tài khoản này..."
                class="w-full px-3 py-2 text-xs bg-white border border-slate-200 rounded-md focus:outline-hidden focus:border-indigo-600"
              ></textarea>
            </div>
          </div>
        </div>

        <!-- Section 4: File Attachments -->
        <div class="p-5 bg-slate-50/60 border border-slate-200 rounded-md space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2 text-xs font-bold text-slate-800">
              <Paperclip class="w-4 h-4 text-indigo-600" />
              <span>Tệp đính kèm bảo mật (Hỗ trợ full các loại tệp: Ảnh, PDF, Key, Backup, Docx, Zip...)</span>
            </div>
            <button
              type="button"
              class="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center gap-1 cursor-pointer"
              @click="triggerFileInput"
            >
              <UploadCloud class="w-3.5 h-3.5" />
              <span>+ Chọn tệp</span>
            </button>
            <input
              ref="fileInputRef"
              type="file"
              multiple
              class="hidden"
              @change="handleFileSelect"
            />
          </div>

          <!-- Existing attachments (if editing) -->
          <div v-if="existingAttachments.length > 0" class="space-y-1.5">
            <span class="text-[11px] font-semibold text-slate-500">Tệp đã đính kèm:</span>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
              <div
                v-for="att in existingAttachments"
                :key="att.id"
                class="flex items-center justify-between p-2.5 bg-white border border-slate-200 rounded-md text-xs group shadow-xs"
              >
                <div class="flex items-center gap-2 truncate pr-2">
                  <FileText class="w-4 h-4 text-indigo-600 shrink-0" />
                  <span class="font-medium text-slate-800 truncate">{{ att.fileName }}</span>
                  <span class="text-[10px] text-slate-400 shrink-0">({{ formatFileSize(att.fileSize) }})</span>
                </div>
                <button
                  type="button"
                  class="p-1 text-slate-400 hover:text-rose-600 rounded cursor-pointer shrink-0"
                  title="Xóa tệp"
                  @click="removeExistingAttachment(att.id)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>

          <!-- Pending files to upload -->
          <div v-if="pendingFiles.length > 0" class="space-y-1.5">
            <span class="text-[11px] font-semibold text-emerald-600">Tệp mới chuẩn bị tải lên:</span>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
              <div
                v-for="(f, idx) in pendingFiles"
                :key="idx"
                class="flex items-center justify-between p-2.5 bg-emerald-50/50 border border-emerald-200 rounded-md text-xs shadow-xs"
              >
                <div class="flex items-center gap-2 truncate pr-2">
                  <FileUp class="w-4 h-4 text-emerald-600 shrink-0" />
                  <span class="font-medium text-emerald-900 truncate">{{ f.name }}</span>
                  <span class="text-[10px] text-emerald-600 shrink-0">({{ formatFileSize(f.size) }})</span>
                </div>
                <button
                  type="button"
                  class="p-1 text-emerald-600 hover:text-rose-600 rounded cursor-pointer shrink-0"
                  title="Hủy tệp"
                  @click="removePendingFile(idx)"
                >
                  <X class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>

          <p v-if="existingAttachments.length === 0 && pendingFiles.length === 0" class="text-[11px] text-slate-400 text-center py-2">
            Chưa có tệp đính kèm nào. Bạn có thể đính kèm ảnh CCCD, ảnh mã QR 2FA, file backup codes, license key...
          </p>
        </div>

        <!-- Section 5: Favorite Checkbox -->
        <div class="flex items-center gap-2">
          <label class="flex items-center gap-2 text-xs font-semibold text-slate-700 cursor-pointer">
            <input
              v-model="form.isFavorite"
              type="checkbox"
              class="w-4 h-4 text-amber-500 rounded focus:ring-amber-400 border-slate-300 cursor-pointer"
            />
            <span class="flex items-center gap-1">
              <Star class="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
              Đánh dấu tài khoản yêu thích (Ghim lên đầu trang)
            </span>
          </label>
        </div>
      </form>

      <!-- Modal Footer -->
      <div class="px-6 py-4 border-t border-slate-100 bg-slate-50/70 flex items-center justify-between shrink-0">
        <button
          type="button"
          class="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200/60 rounded-md transition-colors cursor-pointer"
          @click="$emit('close')"
        >
          Hủy bỏ
        </button>

        <button
          type="submit"
          form="credentialForm"
          :disabled="isSubmitting"
          class="px-5 py-2 text-xs font-bold bg-indigo-600 hover:bg-indigo-700 text-white rounded-md transition-colors flex items-center gap-2 shadow-xs cursor-pointer disabled:opacity-50"
        >
          <Loader2 v-if="isSubmitting" class="w-3.5 h-3.5 animate-spin" />
          <Check v-else class="w-3.5 h-3.5" />
          <span>{{ editingId ? 'Lưu thay đổi' : 'Lưu tài khoản' }}</span>
        </button>
      </div>
    </div>

    <!-- Confirm Delete Attachment Dialog -->
    <AppConfirmDialog
      v-model="showDeleteAttachmentConfirm"
      title="Xóa tệp đính kèm"
      message="Bạn có chắc chắn muốn xóa tệp đính kèm này không? Thao tác này sẽ gỡ tệp khỏi tài khoản ngay lập tức."
      confirm-text="Xóa tệp"
      confirm-variant="danger"
      type="warning"
      :loading="isDeletingAttachment"
      @confirm="executeDeleteAttachment"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue';
import AppConfirmDialog from '../../../components/common/AppConfirmDialog.vue';
import {
  KeyRound,
  X,
  Lock,
  Eye,
  EyeOff,
  ShieldCheck,
  ChevronDown,
  Plus,
  Trash2,
  Paperclip,
  UploadCloud,
  FileText,
  FileUp,
  Star,
  Check,
  Loader2,
} from 'lucide-vue-next';
import { vaultService } from '../../../services/vault.service';
import { useToastStore } from '../../../stores/toast.store';
import type {
  AccountCategory,
  AccountCredential,
  AccountAttachment,
  CreateAccountCredentialRequest,
  UpdateAccountCredentialRequest,
} from '../../../types/vault.types';

const props = defineProps<{
  isOpen: boolean;
  categories: AccountCategory[];
  editingId?: number | null;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'saved'): void;
}>();

const toast = useToastStore();
const isSubmitting = ref(false);
const showPassword = ref(false);
const isSecurityExpanded = ref(false);

const originalPassword = ref('');
const allActiveCredentials = ref<AccountCredential[]>([]);
const existingAttachments = ref<AccountAttachment[]>([]);
const pendingFiles = ref<File[]>([]);
const fileInputRef = ref<HTMLInputElement | null>(null);

const customFieldList = ref<{ key: string; value: string }[]>([]);

const form = reactive({
  categoryId: undefined as number | undefined,
  title: '',
  serviceName: '',
  websiteUrl: '',
  appPackageName: '',
  username: '',
  email: '',
  phoneNumber: '',
  password: '',
  passwordChangeReason: '',
  recoveryEmail: '',
  linkedCredentialId: undefined as number | undefined,
  pinCode: '',
  securityQuestions: '',
  totpSecret: '',
  recoveryCodes: '',
  notes: '',
  isFavorite: false,
});

const POPULAR_PLATFORMS = [
  { name: 'Google', service: 'Google', categoryCode: 'EMAIL', url: 'https://accounts.google.com' },
  { name: 'Facebook', service: 'Facebook', categoryCode: 'SOCIAL', url: 'https://facebook.com' },
  { name: 'Instagram', service: 'Instagram', categoryCode: 'SOCIAL', url: 'https://instagram.com' },
  { name: 'Steam', service: 'Steam', categoryCode: 'GAME', url: 'https://store.steampowered.com/login' },
  { name: 'Garena', service: 'Garena', categoryCode: 'GAME', url: 'https://sso.garena.com' },
  { name: 'TikTok', service: 'TikTok', categoryCode: 'SOCIAL', url: 'https://tiktok.com' },
  { name: 'GitHub', service: 'GitHub', categoryCode: 'WORK_DEV', url: 'https://github.com/login' },
  { name: 'Discord', service: 'Discord', categoryCode: 'SOCIAL', url: 'https://discord.com/login' },
  { name: 'Zalo', service: 'Zalo', categoryCode: 'SOCIAL', url: 'https://chat.zalo.me' },
  { name: 'Netflix', service: 'Netflix', categoryCode: 'ENTERTAINMENT', url: 'https://netflix.com' },
  { name: 'Spotify', service: 'Spotify', categoryCode: 'ENTERTAINMENT', url: 'https://spotify.com' },
  { name: 'Shopee', service: 'Shopee', categoryCode: 'SHOPPING', url: 'https://shopee.vn/buyer/login' },
  { name: 'BIDV', service: 'BIDV', categoryCode: 'BANKING', url: 'https://smartbanking.bidv.com.vn' },
];

const applyPlatformTemplate = (platform: typeof POPULAR_PLATFORMS[0]) => {
  form.serviceName = platform.service;
  if (!form.title) {
    form.title = `Tài khoản ${platform.name}`;
  }
  form.websiteUrl = platform.url;
  const matchedCat = props.categories.find(c => c.code === platform.categoryCode);
  if (matchedCat) {
    form.categoryId = matchedCat.id;
  }
};

const isPasswordChanged = computed(() => {
  return props.editingId && form.password !== originalPassword.value;
});

const hasAdvancedData = computed(() => {
  return (
    !!form.recoveryEmail ||
    !!form.linkedCredentialId ||
    !!form.pinCode ||
    !!form.totpSecret ||
    !!form.recoveryCodes ||
    !!form.securityQuestions ||
    customFieldList.value.length > 0 ||
    !!form.notes
  );
});

const addCustomField = () => {
  customFieldList.value.push({ key: '', value: '' });
};

const removeCustomField = (index: number) => {
  customFieldList.value.splice(index, 1);
};

const triggerFileInput = () => {
  fileInputRef.value?.click();
};

const handleFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    for (let i = 0; i < target.files.length; i++) {
      pendingFiles.value.push(target.files[i]);
    }
  }
  target.value = '';
};

const removePendingFile = (index: number) => {
  pendingFiles.value.splice(index, 1);
};

const showDeleteAttachmentConfirm = ref(false);
const attachmentToDeleteId = ref<number | null>(null);
const isDeletingAttachment = ref(false);

const removeExistingAttachment = (attachmentId: number) => {
  attachmentToDeleteId.value = attachmentId;
  showDeleteAttachmentConfirm.value = true;
};

const executeDeleteAttachment = async () => {
  if (!attachmentToDeleteId.value) return;
  isDeletingAttachment.value = true;
  try {
    await vaultService.deleteAttachment(attachmentToDeleteId.value);
    existingAttachments.value = existingAttachments.value.filter(a => a.id !== attachmentToDeleteId.value);
    toast.success('Đã xóa tệp đính kèm');
    showDeleteAttachmentConfirm.value = false;
    attachmentToDeleteId.value = null;
  } catch (e: any) {
    toast.error('Không thể xóa tệp');
  } finally {
    isDeletingAttachment.value = false;
  }
};

const formatFileSize = (bytes: number) => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
};

// Password Strength
const passwordScore = computed(() => {
  const p = form.password;
  if (!p) return 0;
  let score = 0;
  if (p.length >= 8) score += 20;
  if (p.length >= 12) score += 20;
  if (p.length >= 16) score += 10;
  if (/[a-z]/.test(p)) score += 10;
  if (/[A-Z]/.test(p)) score += 10;
  if (/\d/.test(p)) score += 15;
  if (/[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(p)) score += 15;
  return Math.min(100, score);
});

const passwordLevel = computed(() => {
  const s = passwordScore.value;
  if (s >= 80) return 4;
  if (s >= 60) return 3;
  if (s >= 40) return 2;
  return 1;
});

const passwordLevelLabel = computed(() => {
  switch (passwordLevel.value) {
    case 4: return 'Rất mạnh';
    case 3: return 'Mạnh';
    case 2: return 'Trung bình';
    default: return 'Yếu';
  }
});

const passwordBarColor = computed(() => {
  switch (passwordLevel.value) {
    case 4: return 'bg-emerald-500';
    case 3: return 'bg-blue-500';
    case 2: return 'bg-amber-500';
    default: return 'bg-rose-500';
  }
});

const passwordTextColor = computed(() => {
  switch (passwordLevel.value) {
    case 4: return 'text-emerald-700';
    case 3: return 'text-blue-700';
    case 2: return 'text-amber-700';
    default: return 'text-rose-700';
  }
});

const evaluatePassword = () => {
  // Triggers reactivity
};

const resetForm = () => {
  form.categoryId = undefined;
  form.title = '';
  form.serviceName = '';
  form.websiteUrl = '';
  form.appPackageName = '';
  form.username = '';
  form.email = '';
  form.phoneNumber = '';
  form.password = '';
  form.passwordChangeReason = '';
  form.recoveryEmail = '';
  form.linkedCredentialId = undefined;
  form.pinCode = '';
  form.securityQuestions = '';
  form.totpSecret = '';
  form.recoveryCodes = '';
  form.notes = '';
  form.isFavorite = false;
  originalPassword.value = '';
  customFieldList.value = [];
  existingAttachments.value = [];
  pendingFiles.value = [];
  showPassword.value = false;
  isSecurityExpanded.value = false;
};

const loadEditingData = async (id: number) => {
  try {
    const detail = await vaultService.getCredentialDetail(id);
    form.categoryId = detail.categoryId;
    form.title = detail.title;
    form.serviceName = detail.serviceName || '';
    form.websiteUrl = detail.websiteUrl || '';
    form.appPackageName = detail.appPackageName || '';
    form.username = detail.username || '';
    form.email = detail.email || '';
    form.phoneNumber = detail.phoneNumber || '';
    form.password = detail.password || '';
    originalPassword.value = detail.password || '';
    form.passwordChangeReason = '';
    form.recoveryEmail = detail.recoveryEmail || '';
    form.linkedCredentialId = detail.linkedCredentialId;
    form.pinCode = detail.pinCode || '';
    form.securityQuestions = detail.securityQuestions || '';
    form.totpSecret = detail.totpSecret || '';
    form.recoveryCodes = detail.recoveryCodes || '';
    form.notes = detail.notes || '';
    form.isFavorite = detail.isFavorite || false;

    // Custom fields
    if (detail.customFields) {
      try {
        const parsed = JSON.parse(detail.customFields);
        if (Array.isArray(parsed)) {
          customFieldList.value = parsed;
        }
      } catch (e) {
        customFieldList.value = [];
      }
    }

    existingAttachments.value = detail.attachments || [];
  } catch (e: any) {
    toast.error('Không thể tải thông tin tài khoản');
  }
};

const handleSubmit = async () => {
  if (!form.title.trim() || !form.password) {
    toast.error('Vui lòng điền tiêu đề và mật khẩu');
    return;
  }

  isSubmitting.value = true;
  try {
    const customFieldsJson = customFieldList.value.filter(f => f.key.trim()).length > 0
      ? JSON.stringify(customFieldList.value.filter(f => f.key.trim()))
      : undefined;

    let savedCredentialId = props.editingId;

    if (props.editingId) {
      const payload: UpdateAccountCredentialRequest = {
        categoryId: form.categoryId,
        title: form.title,
        serviceName: form.serviceName,
        websiteUrl: form.websiteUrl,
        appPackageName: form.appPackageName,
        username: form.username,
        email: form.email,
        phoneNumber: form.phoneNumber,
        password: form.password,
        passwordChangeReason: form.passwordChangeReason,
        recoveryEmail: form.recoveryEmail,
        linkedCredentialId: form.linkedCredentialId,
        pinCode: form.pinCode,
        securityQuestions: form.securityQuestions,
        totpSecret: form.totpSecret,
        recoveryCodes: form.recoveryCodes,
        customFields: customFieldsJson,
        notes: form.notes,
        isFavorite: form.isFavorite,
      };
      await vaultService.updateCredential(props.editingId, payload);
      toast.success('Đã cập nhật tài khoản thành công!');
    } else {
      const payload: CreateAccountCredentialRequest = {
        categoryId: form.categoryId,
        title: form.title,
        serviceName: form.serviceName,
        websiteUrl: form.websiteUrl,
        appPackageName: form.appPackageName,
        username: form.username,
        email: form.email,
        phoneNumber: form.phoneNumber,
        password: form.password,
        recoveryEmail: form.recoveryEmail,
        linkedCredentialId: form.linkedCredentialId,
        pinCode: form.pinCode,
        securityQuestions: form.securityQuestions,
        totpSecret: form.totpSecret,
        recoveryCodes: form.recoveryCodes,
        customFields: customFieldsJson,
        notes: form.notes,
        isFavorite: form.isFavorite,
      };
      const created = await vaultService.createCredential(payload);
      savedCredentialId = created.id;
      toast.success('Đã lưu tài khoản mới vào Két bảo mật!');
    }

    // Upload pending files if any
    if (savedCredentialId && pendingFiles.value.length > 0) {
      for (const file of pendingFiles.value) {
        try {
          await vaultService.uploadAttachment(savedCredentialId, file);
        } catch (err: any) {
          console.error('Failed to upload file:', file.name, err);
        }
      }
    }

    emit('saved');
    emit('close');
  } catch (err: any) {
    toast.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu tài khoản');
  } finally {
    isSubmitting.value = false;
  }
};

watch(
  () => props.isOpen,
  async (open) => {
    if (open) {
      resetForm();
      try {
        allActiveCredentials.value = await vaultService.getAllActiveCredentials();
      } catch (e) {
        // Ignored
      }
      if (props.editingId) {
        await loadEditingData(props.editingId);
      }
    }
  }
);
</script>
