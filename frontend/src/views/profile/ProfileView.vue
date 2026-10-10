<template>
  <div class="space-y-6 max-w-5xl mx-auto pb-16">
    <!-- Profile Hero Card -->
    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs">
      <!-- Gradient Banner Cover -->
      <div class="h-36 sm:h-48 bg-gradient-to-r from-brand-600 via-brand-500 to-indigo-600 relative overflow-hidden">
        <div class="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:16px_16px]"></div>
        <div class="absolute -right-8 -bottom-8 w-44 h-44 bg-white/10 rounded-full blur-xl pointer-events-none"></div>
      </div>

      <!-- Avatar & Hero Info Section -->
      <div class="px-6 sm:px-8 pb-6 pt-0 relative">
        <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-4 -mt-16 sm:-mt-20 mb-4">
          <!-- Avatar with Edit Overlay -->
          <div class="relative group w-28 h-28 sm:w-36 sm:h-36 rounded-full ring-4 ring-white shadow-md bg-white overflow-hidden shrink-0">
            <img
              v-if="userForm.avatarUrl"
              :src="userForm.avatarUrl"
              alt="Avatar"
              class="w-full h-full object-cover"
            />
            <div
              v-else
              class="w-full h-full bg-brand-50 text-brand-600 flex items-center justify-center font-extrabold text-3xl sm:text-4xl"
            >
              {{ userInitial }}
            </div>

            <!-- Upload / Change Button Overlay -->
            <button
              type="button"
              class="absolute inset-0 bg-slate-900/60 text-white flex flex-col items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer text-xs font-semibold gap-1"
              @click="openAvatarModal"
            >
              <Camera class="w-5 h-5" />
              <span>Đổi ảnh</span>
            </button>
          </div>

          <!-- Quick Action Buttons -->
          <div class="flex items-center gap-2.5">
            <AppButton
              variant="secondary"
              size="sm"
              :icon="Camera"
              @click="openAvatarModal"
            >
              Đổi ảnh đại diện
            </AppButton>
            <AppButton
              variant="primary"
              size="sm"
              :icon="Save"
              :loading="savingProfile"
              @click="saveProfile"
            >
              Lưu thông tin
            </AppButton>
          </div>
        </div>

        <!-- Name & Email Details -->
        <div class="space-y-1.5">
          <div class="flex flex-wrap items-center gap-2.5">
            <h1 class="text-2xl sm:text-3xl font-bold text-slate-900">
              {{ authStore.user?.displayName || 'Minh Hiếu' }}
            </h1>
            <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
              <CheckCircle2 class="w-3.5 h-3.5" />
              Tài khoản hoạt động
            </span>
            <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-brand-50 text-brand-700 border border-brand-200">
              <Trophy class="w-3.5 h-3.5" />
              Mục tiêu TOEIC: {{ authStore.user?.targetScore || 650 }}+
            </span>
          </div>
          <p class="text-sm text-slate-500 flex items-center gap-2">
            <Mail class="w-4 h-4 text-slate-400" />
            <span>{{ authStore.user?.email || 'hieulm24@gmail.com' }}</span>
            <span class="text-slate-300">•</span>
            <Calendar class="w-4 h-4 text-slate-400" />
            <span>Tham gia: {{ formatJoinedDate(authStore.user?.createdAt) }}</span>
          </p>
        </div>
      </div>

      <!-- Quick Metrics Summary Grid -->
      <div class="grid grid-cols-2 sm:grid-cols-4 divide-x divide-y sm:divide-y-0 divide-slate-100 border-t border-slate-100 bg-slate-50/50">
        <div class="p-4 sm:p-5 text-center sm:text-left">
          <div class="flex items-center justify-center sm:justify-start gap-2 text-xs font-semibold text-slate-500 mb-1">
            <Flame class="w-4 h-4 text-amber-500" />
            <span>Chuỗi học tập</span>
          </div>
          <div class="text-xl sm:text-2xl font-black text-amber-600">
            {{ displayStreak }} <span class="text-xs font-normal text-slate-400">ngày liên tiếp</span>
          </div>
        </div>

        <div class="p-4 sm:p-5 text-center sm:text-left">
          <div class="flex items-center justify-center sm:justify-start gap-2 text-xs font-semibold text-slate-500 mb-1">
            <BookOpen class="w-4 h-4 text-brand-500" />
            <span>Từ vựng đã thêm</span>
          </div>
          <div class="text-xl sm:text-2xl font-black text-brand-600">
            {{ displayTotalVocab }} <span class="text-xs font-normal text-slate-400">từ</span>
          </div>
        </div>

        <div class="p-4 sm:p-5 text-center sm:text-left">
          <div class="flex items-center justify-center sm:justify-start gap-2 text-xs font-semibold text-slate-500 mb-1">
            <Sparkles class="w-4 h-4 text-purple-500" />
            <span>Ngữ pháp đang học</span>
          </div>
          <div class="text-xl sm:text-2xl font-black text-purple-600">
            {{ displayTotalGrammar }} <span class="text-xs font-normal text-slate-400">chủ đề</span>
          </div>
        </div>

        <div class="p-4 sm:p-5 text-center sm:text-left">
          <div class="flex items-center justify-center sm:justify-start gap-2 text-xs font-semibold text-slate-500 mb-1">
            <Clock class="w-4 h-4 text-teal-500" />
            <span>Mục tiêu hàng ngày</span>
          </div>
          <div class="text-xl sm:text-2xl font-black text-teal-600">
            {{ displayDailyTarget }} <span class="text-xs font-normal text-slate-400">phút/ngày</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="flex border-b border-slate-200 bg-white rounded-xl p-1.5 shadow-2xs gap-1">
      <button
        type="button"
        class="flex-1 py-2.5 px-4 text-sm font-semibold rounded-lg transition-all flex items-center justify-center gap-2"
        :class="activeTab === 'info' ? 'bg-brand-50 text-brand-700 shadow-2xs' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
        @click="activeTab = 'info'"
      >
        <UserIcon class="w-4 h-4" />
        <span>Thông tin cá nhân</span>
      </button>

      <button
        type="button"
        class="flex-1 py-2.5 px-4 text-sm font-semibold rounded-lg transition-all flex items-center justify-center gap-2"
        :class="activeTab === 'security' ? 'bg-brand-50 text-brand-700 shadow-2xs' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
        @click="activeTab = 'security'"
      >
        <KeyRound class="w-4 h-4" />
        <span>Đổi mật khẩu</span>
      </button>

      <button
        type="button"
        class="flex-1 py-2.5 px-4 text-sm font-semibold rounded-lg transition-all flex items-center justify-center gap-2"
        :class="activeTab === 'forgot' ? 'bg-brand-50 text-brand-700 shadow-2xs' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
        @click="activeTab = 'forgot'"
      >
        <MailCheck class="w-4 h-4" />
        <span>Quên mật khẩu & Mã OTP</span>
      </button>
    </div>

    <!-- ============================================================= -->
    <!-- TAB 1: THÔNG TIN CÁ NHÂN (PROFILE DETAILS)                     -->
    <!-- ============================================================= -->
    <div v-if="activeTab === 'info'" class="bg-white rounded-2xl border border-slate-200 p-6 sm:p-8 shadow-xs space-y-6">
      <div>
        <h2 class="text-lg font-bold text-slate-900">Chi tiết thông tin tài khoản</h2>
        <p class="text-xs text-slate-500 mt-0.5">Cập nhật họ tên, mục tiêu TOEIC và thời gian học tập cá nhân.</p>
      </div>

      <form class="space-y-5" @submit.prevent="saveProfile">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <AppInput
            v-model="userForm.displayName"
            label="Họ và tên hiển thị"
            placeholder="e.g. Lãnh Minh Hiếu"
            :icon="UserIcon"
            required
          />

          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Địa chỉ Email (Không thể thay đổi)
            </label>
            <div class="relative">
              <input
                type="email"
                :value="authStore.user?.email || 'hieulm24@gmail.com'"
                disabled
                class="w-full text-sm bg-slate-100 border border-slate-200 text-slate-500 rounded-md px-3.5 py-2.5 cursor-not-allowed font-medium pl-10"
              />
              <Mail class="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            </div>
            <p class="text-[11px] text-slate-400 mt-1">Email được dùng làm tên đăng nhập cố định của hệ thống.</p>
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-5">
          <AppInput
            v-model="userForm.phoneNumber"
            label="Số điện thoại liên hệ"
            placeholder="e.g. 0912345678"
            :icon="Phone"
          />

          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Mục tiêu điểm TOEIC
            </label>
            <select
              v-model="userForm.targetScore"
              class="w-full text-sm bg-white border border-slate-300 rounded-md px-3.5 py-2.5 shadow-xs focus:outline-none focus:border-brand-500 font-medium"
            >
              <option :value="450">450+ TOEIC (Sơ cấp)</option>
              <option :value="550">550+ TOEIC (Cơ bản)</option>
              <option :value="650">650+ TOEIC (Mục tiêu hiện tại)</option>
              <option :value="750">750+ TOEIC (Trung cao cấp)</option>
              <option :value="850">850+ TOEIC (Nâng cao)</option>
              <option :value="990">990 TOEIC (Tối đa)</option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Thời gian học mỗi ngày
            </label>
            <select
              v-model="userForm.dailyLearningTarget"
              class="w-full text-sm bg-white border border-slate-300 rounded-md px-3.5 py-2.5 shadow-xs focus:outline-none focus:border-brand-500 font-medium"
            >
              <option :value="15">15 phút / ngày (Nhẹ nhàng)</option>
              <option :value="30">30 phút / ngày (Tiêu chuẩn)</option>
              <option :value="45">45 phút / ngày (Quyết tâm)</option>
              <option :value="60">60 phút / ngày (Cường độ cao)</option>
              <option :value="90">90 phút / ngày (Tối đa)</option>
            </select>
          </div>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">
            Giới thiệu bản thân & Phương châm học tập (Bio)
          </label>
          <textarea
            v-model="userForm.bio"
            rows="3"
            class="w-full text-sm bg-white border border-slate-300 rounded-md p-3 shadow-xs focus:outline-none focus:border-brand-500 placeholder:text-slate-400"
            placeholder="Ghi chú mục tiêu, lời nhắc nhở học tập mỗi ngày..."
          ></textarea>
        </div>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
          <AppButton
            type="submit"
            variant="primary"
            size="md"
            :icon="Save"
            :loading="savingProfile"
          >
            Lưu thay đổi thông tin
          </AppButton>
        </div>
      </form>
    </div>

    <!-- ============================================================= -->
    <!-- TAB 2: ĐỔI MẬT KHẨU (CHANGE PASSWORD)                          -->
    <!-- ============================================================= -->
    <div v-if="activeTab === 'security'" class="bg-white rounded-2xl border border-slate-200 p-6 sm:p-8 shadow-xs space-y-6">
      <div>
        <h2 class="text-lg font-bold text-slate-900">Đổi mật khẩu tài khoản</h2>
        <p class="text-xs text-slate-500 mt-0.5">Để bảo vệ tài khoản, hãy sử dụng mật khẩu mạnh có chữ hoa, chữ thường và số.</p>
      </div>

      <form class="space-y-4 max-w-xl" @submit.prevent="handleChangePassword">
        <AppInput
          v-model="pwdForm.currentPassword"
          type="password"
          label="Mật khẩu hiện tại"
          placeholder="••••••••"
          :icon="Lock"
          required
        />

        <AppInput
          v-model="pwdForm.newPassword"
          type="password"
          label="Mật khẩu mới (tối thiểu 6 ký tự)"
          placeholder="••••••••"
          :icon="Key"
          required
        />

        <AppInput
          v-model="pwdForm.confirmPassword"
          type="password"
          label="Xác nhận lại mật khẩu mới"
          placeholder="••••••••"
          :icon="CheckCircle2"
          required
        />

        <!-- Password match warning -->
        <div
          v-if="pwdForm.newPassword && pwdForm.confirmPassword && pwdForm.newPassword !== pwdForm.confirmPassword"
          class="p-3 bg-rose-50 text-rose-700 rounded-md text-xs font-semibold flex items-center gap-2 border border-rose-200"
        >
          <AlertCircle class="w-4 h-4 shrink-0" />
          <span>Mật khẩu mới và xác nhận mật khẩu chưa trùng khớp.</span>
        </div>

        <div class="flex items-center justify-between pt-4 border-t border-slate-100">
          <button
            type="button"
            class="text-xs text-brand-600 hover:text-brand-700 font-semibold hover:underline flex items-center gap-1"
            @click="activeTab = 'forgot'"
          >
            <HelpCircle class="w-3.5 h-3.5" />
            <span>Quên mật khẩu? Gửi mã xác thực về Email</span>
          </button>

          <AppButton
            type="submit"
            variant="primary"
            size="md"
            :icon="Lock"
            :loading="changingPassword"
          >
            Cập nhật mật khẩu
          </AppButton>
        </div>
      </form>
    </div>

    <!-- ============================================================= -->
    <!-- TAB 3: QUÊN MẬT KHẨU & ĐẶT LẠI QUA MÃ OTP EMAIL                -->
    <!-- ============================================================= -->
    <div v-if="activeTab === 'forgot'" class="bg-white rounded-2xl border border-slate-200 p-6 sm:p-8 shadow-xs space-y-6">
      <div>
        <h2 class="text-lg font-bold text-slate-900">Khôi phục mật khẩu qua Email OTP</h2>
        <p class="text-xs text-slate-500 mt-0.5">Hệ thống sẽ gửi mã xác thực 6 số đến email đăng ký của bạn để bạn tạo mật khẩu mới.</p>
      </div>

      <!-- Step 1: Send OTP -->
      <div v-if="forgotStep === 1" class="max-w-md space-y-4">
        <div class="p-4 bg-brand-50 border border-brand-200 rounded-xl text-xs text-brand-900 space-y-1">
          <div class="font-bold flex items-center gap-1.5">
            <Mail class="w-4 h-4 text-brand-600" />
            <span>Bước 1: Nhập email nhận mã OTP</span>
          </div>
          <p class="text-brand-700">Mã xác thực có hiệu lực trong 10 phút. Vui lòng kiểm tra hộp thư (bao gồm cả thư rác/spam).</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleSendOtp">
          <AppInput
            v-model="forgotForm.email"
            type="email"
            label="Email tài khoản"
            placeholder="hieulm24@gmail.com"
            :icon="Mail"
            required
          />

          <AppButton
            type="submit"
            variant="primary"
            size="md"
            full-width
            :icon="Send"
            :loading="sendingOtp"
          >
            Gửi mã xác thực OTP
          </AppButton>
        </form>
      </div>

      <!-- Step 2: Enter OTP & New Password -->
      <div v-else-if="forgotStep === 2" class="max-w-md space-y-4">
        <div class="p-4 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-900 space-y-1">
          <div class="font-bold flex items-center gap-1.5">
            <CheckCircle2 class="w-4 h-4 text-emerald-600" />
            <span>Mã OTP đã được gửi thành công đến: {{ forgotForm.email }}</span>
          </div>
          <p class="text-emerald-700">Hãy nhập mã 6 số bên dưới và đặt lại mật khẩu mới cho bạn.</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleResetPasswordWithOtp">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Mã xác thực OTP (6 chữ số)
            </label>
            <input
              v-model="forgotForm.otpCode"
              type="text"
              maxlength="10"
              required
              placeholder="e.g. 123456"
              class="w-full text-center tracking-widest text-xl font-bold bg-white border border-slate-300 rounded-md p-2.5 shadow-xs focus:outline-none focus:border-brand-500 font-mono text-brand-700"
            />
          </div>

          <AppInput
            v-model="forgotForm.newPassword"
            type="password"
            label="Mật khẩu mới (tối thiểu 6 ký tự)"
            placeholder="••••••••"
            :icon="Key"
            required
          />

          <AppInput
            v-model="forgotForm.confirmPassword"
            type="password"
            label="Xác nhận lại mật khẩu mới"
            placeholder="••••••••"
            :icon="CheckCircle2"
            required
          />

          <div class="flex items-center gap-3 pt-2">
            <AppButton
              type="button"
              variant="secondary"
              size="md"
              @click="forgotStep = 1"
            >
              Gửi lại mã khác
            </AppButton>

            <AppButton
              type="submit"
              variant="primary"
              size="md"
              class="flex-1"
              :icon="Check"
              :loading="resettingPassword"
            >
              Xác nhận & Lưu mật khẩu mới
            </AppButton>
          </div>
        </form>
      </div>

      <!-- Step 3: Success Completed -->
      <div v-else class="max-w-md p-6 bg-emerald-50 rounded-2xl border border-emerald-200 text-center space-y-3">
        <div class="w-12 h-12 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto">
          <CheckCircle2 class="w-6 h-6" />
        </div>
        <h3 class="text-base font-bold text-slate-900">Mật khẩu đã được đặt lại thành công!</h3>
        <p class="text-xs text-slate-600">Bạn đã cập nhật mật khẩu mới thành công và có thể tiếp tục sử dụng hệ thống bình thường.</p>
        <div class="pt-2">
          <AppButton variant="primary" size="sm" @click="activeTab = 'info'">
            Quay lại thông tin cá nhân
          </AppButton>
        </div>
      </div>
    </div>

    <!-- ============================================================= -->
    <!-- MODAL ĐỔI ẢNH ĐẠI DIỆN (AVATAR MODAL)                          -->
    <!-- ============================================================= -->
    <AppModal
      v-model="showAvatarModal"
      title="Cập nhật ảnh đại diện"
      size="md"
    >
      <div class="space-y-5">
        <!-- Preset Avatar Options -->
        <div>
          <label class="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2.5">
            Chọn avatar có sẵn
          </label>
          <div class="grid grid-cols-5 gap-3">
            <button
              v-for="(avatar, idx) in presetAvatars"
              :key="idx"
              type="button"
              class="w-14 h-14 rounded-full overflow-hidden border-2 transition-all p-0.5 hover:scale-105 cursor-pointer"
              :class="userForm.avatarUrl === avatar ? 'border-brand-500 ring-2 ring-brand-200' : 'border-slate-200 hover:border-slate-400'"
              @click="userForm.avatarUrl = avatar"
            >
              <img :src="avatar" class="w-full h-full object-cover rounded-full" />
            </button>
          </div>
        </div>

        <!-- Custom Image URL -->
        <div class="pt-2 border-t border-slate-100 space-y-3">
          <AppInput
            v-model="userForm.avatarUrl"
            label="Hoặc nhập đường dẫn ảnh (URL)"
            placeholder="https://example.com/avatar.jpg"
            :icon="Link2"
          />
        </div>

        <!-- Local File Upload -->
        <div class="pt-2 border-t border-slate-100 space-y-2">
          <label class="block text-xs font-bold text-slate-700 uppercase tracking-wider">
            Hoặc tải file từ máy tính
          </label>
          <input
            type="file"
            accept="image/*"
            class="block w-full text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-xs file:font-semibold file:bg-brand-50 file:text-brand-700 hover:file:bg-brand-100 cursor-pointer border border-slate-200 rounded-md p-1"
            @change="handleAvatarFileUpload"
          />
        </div>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" @click="showAvatarModal = false">
            Hủy
          </AppButton>
          <AppButton
            variant="primary"
            size="md"
            :icon="Check"
            :loading="savingAvatar"
            @click="confirmAvatarChange"
          >
            Áp dụng ảnh này
          </AppButton>
        </div>
      </div>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useAuthStore } from '../../stores/auth.store';
import { useToastStore } from '../../stores/toast.store';
import { authService } from '../../services/auth.service';
import { statisticsService } from '../../services/statistics.service';
import type { DashboardSummary } from '../../types';
import AppInput from '../../components/common/AppInput.vue';
import AppButton from '../../components/common/AppButton.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  User as UserIcon,
  Mail,
  Phone,
  Calendar,
  Flame,
  BookOpen,
  Sparkles,
  Clock,
  Save,
  KeyRound,
  MailCheck,
  Camera,
  CheckCircle2,
  Trophy,
  Lock,
  Key,
  AlertCircle,
  HelpCircle,
  Send,
  Check,
  Link2,
} from 'lucide-vue-next';

const authStore = useAuthStore();
const toastStore = useToastStore();
const dashboardStats = ref<DashboardSummary | null>(null);

const activeTab = ref<'info' | 'security' | 'forgot'>('info');
const savingProfile = ref(false);
const changingPassword = ref(false);
const sendingOtp = ref(false);
const resettingPassword = ref(false);
const showAvatarModal = ref(false);
const savingAvatar = ref(false);

const forgotStep = ref<1 | 2 | 3>(1);

// Preset avatars
const presetAvatars = [
  'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80',
];

const userForm = reactive({
  displayName: '',
  avatarUrl: '',
  phoneNumber: '',
  bio: '',
  targetScore: 650,
  dailyLearningTarget: 30,
});

const pwdForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const forgotForm = reactive({
  email: '',
  otpCode: '',
  newPassword: '',
  confirmPassword: '',
});

const userInitial = computed(() => {
  const name = userForm.displayName || authStore.user?.displayName || 'H';
  return name.trim().charAt(0).toUpperCase();
});

const displayStreak = computed(() => {
  if (dashboardStats.value?.currentStreak !== undefined) return dashboardStats.value.currentStreak;
  return authStore.user?.currentStreak || 0;
});

const displayTotalVocab = computed(() => {
  if (dashboardStats.value?.totalVocabulary !== undefined) return dashboardStats.value.totalVocabulary;
  return authStore.user?.totalVocabulary || 0;
});

const displayTotalGrammar = computed(() => {
  if (dashboardStats.value?.totalGrammar !== undefined) return dashboardStats.value.totalGrammar;
  return authStore.user?.totalGrammar || 0;
});

const displayDailyTarget = computed(() => {
  return userForm.dailyLearningTarget || dashboardStats.value?.dailyLearningTarget || authStore.user?.dailyLearningTarget || 30;
});

onMounted(async () => {
  await Promise.all([
    loadUserProfile(),
    loadDashboardStats(),
  ]);
});

async function loadDashboardStats() {
  try {
    const summary = await statisticsService.getDashboardSummary();
    if (summary) {
      dashboardStats.value = summary;
      if (summary.dailyLearningTarget && !userForm.dailyLearningTarget) {
        userForm.dailyLearningTarget = summary.dailyLearningTarget;
      }
    }
  } catch (err) {
    console.warn('Failed to load dashboard summary for profile:', err);
  }
}

async function loadUserProfile() {
  const profile = await authStore.fetchCurrentUser();
  if (profile) {
    userForm.displayName = profile.displayName || '';
    userForm.avatarUrl = profile.avatarUrl || '';
    userForm.phoneNumber = profile.phoneNumber || '';
    userForm.bio = profile.bio || '';
    userForm.targetScore = profile.targetScore || 650;
    userForm.dailyLearningTarget = profile.dailyLearningTarget || 30;
    forgotForm.email = profile.email || 'hieulm24@gmail.com';
  }
}

function formatJoinedDate(dateStr?: string) {
  if (!dateStr) return '01/10/2026';
  try {
    const d = new Date(dateStr);
    return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
  } catch {
    return '01/10/2026';
  }
}

async function saveProfile() {
  if (!userForm.displayName.trim()) {
    toastStore.error('Họ tên hiển thị không được để trống');
    return;
  }
  savingProfile.value = true;
  try {
    const updated = await authService.updateProfile({
      displayName: userForm.displayName.trim(),
      avatarUrl: userForm.avatarUrl.trim() || undefined,
      phoneNumber: userForm.phoneNumber.trim() || undefined,
      bio: userForm.bio.trim() || undefined,
      targetScore: userForm.targetScore,
      dailyLearningTarget: userForm.dailyLearningTarget,
    });
    authStore.updateUser(updated);
    toastStore.success('Đã lưu thông tin cá nhân thành công!');
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể cập nhật thông tin');
  } finally {
    savingProfile.value = false;
  }
}

async function handleChangePassword() {
  if (!pwdForm.currentPassword) {
    toastStore.error('Vui lòng nhập mật khẩu hiện tại');
    return;
  }
  if (pwdForm.newPassword.length < 6) {
    toastStore.error('Mật khẩu mới phải có ít nhất 6 ký tự');
    return;
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    toastStore.error('Mật khẩu mới và xác nhận mật khẩu không khớp');
    return;
  }

  changingPassword.value = true;
  try {
    const msg = await authService.changePassword({
      currentPassword: pwdForm.currentPassword,
      newPassword: pwdForm.newPassword,
      confirmPassword: pwdForm.confirmPassword,
    });
    toastStore.success(msg || 'Đổi mật khẩu thành công!');
    pwdForm.currentPassword = '';
    pwdForm.newPassword = '';
    pwdForm.confirmPassword = '';
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Đổi mật khẩu thất bại. Kiểm tra lại mật khẩu hiện tại.');
  } finally {
    changingPassword.value = false;
  }
}

async function handleSendOtp() {
  if (!forgotForm.email.trim()) {
    toastStore.error('Vui lòng nhập email');
    return;
  }
  sendingOtp.value = true;
  try {
    const res = await authService.sendForgotPasswordOtp({ email: forgotForm.email.trim() });
    toastStore.success('Mã OTP xác thực đã được gửi về email của bạn!');
    if (res?.devOtpPreview) {
      forgotForm.otpCode = res.devOtpPreview;
      toastStore.info(`[Dev OTP Preview]: ${res.devOtpPreview}`);
    }
    forgotStep.value = 2;
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể gửi mã OTP về email');
  } finally {
    sendingOtp.value = false;
  }
}

async function handleResetPasswordWithOtp() {
  if (!forgotForm.otpCode.trim()) {
    toastStore.error('Vui lòng nhập mã OTP 6 số');
    return;
  }
  if (forgotForm.newPassword.length < 6) {
    toastStore.error('Mật khẩu mới phải có ít nhất 6 ký tự');
    return;
  }
  if (forgotForm.newPassword !== forgotForm.confirmPassword) {
    toastStore.error('Mật khẩu mới và xác nhận mật khẩu không khớp');
    return;
  }

  resettingPassword.value = true;
  try {
    const msg = await authService.resetPasswordWithOtp({
      email: forgotForm.email.trim(),
      otpCode: forgotForm.otpCode.trim(),
      newPassword: forgotForm.newPassword,
      confirmPassword: forgotForm.confirmPassword,
    });
    toastStore.success(msg || 'Đặt lại mật khẩu thành công!');
    forgotStep.value = 3;
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Mã OTP không đúng hoặc đã hết hạn');
  } finally {
    resettingPassword.value = false;
  }
}

function openAvatarModal() {
  showAvatarModal.value = true;
}

async function handleAvatarFileUpload(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  savingAvatar.value = true;
  try {
    const avatarUrl = await authService.uploadAvatar(file);
    userForm.avatarUrl = avatarUrl;
    toastStore.success('Đã tải ảnh đại diện lên thành công!');
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể tải ảnh đại diện');
  } finally {
    savingAvatar.value = false;
  }
}

async function confirmAvatarChange() {
  showAvatarModal.value = false;
  await saveProfile();
}
</script>
