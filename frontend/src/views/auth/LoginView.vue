<template>
  <div class="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="flex justify-center">
        <img
          src="/logo.png"
          alt="My Life Logo"
          style="max-height: 96px; width: auto; object-fit: contain;"
        />
      </div>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4">
      <div class="bg-white py-8 px-6 shadow-sm border border-slate-200 rounded-2xl sm:px-10">
        <form class="space-y-4" @submit.prevent="handleLogin">
          <AppInput
            id="email"
            v-model="email"
            label="Địa chỉ Email"
            type="email"
            placeholder="hieulm24@gmail.com"
            :icon="Mail"
            required
            :error="errorMessage"
          />

          <AppInput
            id="password"
            v-model="password"
            label="Mật khẩu"
            type="password"
            placeholder="••••••••"
            :icon="Lock"
            required
          />

          <div class="flex items-center justify-between text-xs">
            <label class="flex items-center text-slate-600 cursor-pointer select-none">
              <input type="checkbox" v-model="rememberMe" class="rounded-sm text-brand-600 border-slate-300 focus:ring-brand-500 mr-2" />
              Ghi nhớ đăng nhập
            </label>
            <button
              type="button"
              class="text-brand-600 hover:text-brand-700 font-medium"
              @click="openForgotModal"
            >
              Quên mật khẩu?
            </button>
          </div>

          <div class="pt-2">
            <AppButton
              type="submit"
              variant="primary"
              :loading="loading"
              full-width
            >
              Đăng nhập hệ thống
            </AppButton>
          </div>

          <div class="pt-2 text-center">
            <button
              type="button"
              class="text-xs text-slate-500 hover:text-brand-600 font-medium hover:underline"
              @click="fillDefaultAccount"
            >
              Điền tài khoản mặc định (Admin)
            </button>
          </div>
        </form>

        <div class="mt-6 pt-4 border-t border-slate-100 text-center">
          <p class="text-[11px] text-slate-400">
            Hệ thống chạy LOCAL hoàn toàn riêng tư & an toàn.
          </p>
        </div>
      </div>
    </div>

    <!-- Forgot Password Modal -->
    <AppModal
      v-model="showForgotModal"
      title="Khôi phục mật khẩu qua Email OTP"
      size="md"
    >
      <!-- Step 1: Send OTP -->
      <div v-if="forgotStep === 1" class="space-y-4">
        <div class="p-3.5 bg-brand-50 border border-brand-200 rounded-xl text-xs text-brand-900 space-y-1">
          <div class="font-bold flex items-center gap-1.5">
            <Mail class="w-4 h-4 text-brand-600" />
            <span>Nhập email tài khoản cần khôi phục</span>
          </div>
          <p class="text-brand-700">Hệ thống sẽ gửi mã OTP 6 số xác thực về email của bạn (hiệu lực 10 phút).</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleSendOtp">
          <AppInput
            v-model="forgotEmail"
            type="email"
            label="Email tài khoản"
            placeholder="hieulm24@gmail.com"
            :icon="Mail"
            required
          />

          <div class="flex items-center justify-end gap-3 pt-2">
            <AppButton variant="secondary" size="md" @click="showForgotModal = false">
              Hủy
            </AppButton>
            <AppButton
              type="submit"
              variant="primary"
              size="md"
              :icon="Send"
              :loading="sendingOtp"
            >
              Gửi mã OTP
            </AppButton>
          </div>
        </form>
      </div>

      <!-- Step 2: Verify OTP & Reset Password -->
      <div v-else-if="forgotStep === 2" class="space-y-4">
        <div class="p-3.5 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-900 space-y-1">
          <div class="font-bold flex items-center gap-1.5">
            <CheckCircle2 class="w-4 h-4 text-emerald-600" />
            <span>Mã OTP đã được gửi đến: {{ forgotEmail }}</span>
          </div>
          <p class="text-emerald-700">Vui lòng nhập mã xác thực và mật khẩu mới.</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleResetPassword">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Mã xác thực OTP (6 chữ số)
            </label>
            <input
              v-model="forgotOtpCode"
              type="text"
              maxlength="10"
              required
              placeholder="123456"
              class="w-full text-center tracking-widest text-xl font-bold bg-white border border-slate-300 rounded-md p-2.5 shadow-xs focus:outline-none focus:border-brand-500 font-mono text-brand-700"
            />
          </div>

          <AppInput
            v-model="forgotNewPassword"
            type="password"
            label="Mật khẩu mới (tối thiểu 6 ký tự)"
            placeholder="••••••••"
            :icon="Key"
            required
          />

          <AppInput
            v-model="forgotConfirmPassword"
            type="password"
            label="Xác nhận lại mật khẩu mới"
            placeholder="••••••••"
            :icon="CheckCircle2"
            required
          />

          <div class="flex items-center justify-between pt-3 border-t border-slate-100">
            <button
              type="button"
              class="text-xs text-slate-500 hover:text-brand-600 font-semibold"
              @click="forgotStep = 1"
            >
              Gửi lại mã khác
            </button>

            <div class="flex items-center gap-2">
              <AppButton variant="secondary" size="md" @click="showForgotModal = false">
                Hủy
              </AppButton>
              <AppButton
                type="submit"
                variant="primary"
                size="md"
                :icon="Check"
                :loading="resettingPassword"
              >
                Đặt lại mật khẩu
              </AppButton>
            </div>
          </div>
        </form>
      </div>

      <!-- Step 3: Success -->
      <div v-else class="text-center py-4 space-y-3">
        <div class="w-12 h-12 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto">
          <CheckCircle2 class="w-6 h-6" />
        </div>
        <h4 class="text-base font-bold text-slate-900">Đặt lại mật khẩu thành công!</h4>
        <p class="text-xs text-slate-600">Bạn có thể đăng nhập ngay bây giờ bằng mật khẩu mới vừa tạo.</p>
        <div class="pt-2">
          <AppButton variant="primary" size="md" full-width @click="showForgotModal = false">
            Đăng nhập ngay
          </AppButton>
        </div>
      </div>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../../stores/auth.store';
import { useToastStore } from '../../stores/toast.store';
import { authService } from '../../services/auth.service';
import AppInput from '../../components/common/AppInput.vue';
import AppButton from '../../components/common/AppButton.vue';
import AppModal from '../../components/common/AppModal.vue';
import { Mail, Lock, Send, CheckCircle2, Key, Check } from 'lucide-vue-next';

const email = ref('hieulm24@gmail.com');
const password = ref('L@nhminhhieudeptrai.1');
const rememberMe = ref(true);
const loading = ref(false);
const errorMessage = ref('');

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const toastStore = useToastStore();

// Forgot Password Modal State
const showForgotModal = ref(false);
const forgotStep = ref<1 | 2 | 3>(1);
const forgotEmail = ref('hieulm24@gmail.com');
const forgotOtpCode = ref('');
const forgotNewPassword = ref('');
const forgotConfirmPassword = ref('');
const sendingOtp = ref(false);
const resettingPassword = ref(false);

function openForgotModal() {
  forgotStep.value = 1;
  forgotEmail.value = email.value.trim() || 'hieulm24@gmail.com';
  forgotOtpCode.value = '';
  forgotNewPassword.value = '';
  forgotConfirmPassword.value = '';
  showForgotModal.value = true;
}

async function handleSendOtp() {
  if (!forgotEmail.value.trim()) {
    toastStore.error('Vui lòng nhập email');
    return;
  }
  sendingOtp.value = true;
  try {
    const res = await authService.sendForgotPasswordOtp({ email: forgotEmail.value.trim() });
    toastStore.success('Mã OTP xác thực đã được gửi về email của bạn!');
    if (res?.devOtpPreview) {
      forgotOtpCode.value = res.devOtpPreview;
      toastStore.info(`[Dev OTP Preview]: ${res.devOtpPreview}`);
    }
    forgotStep.value = 2;
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể gửi mã OTP về email');
  } finally {
    sendingOtp.value = false;
  }
}

async function handleResetPassword() {
  if (!forgotOtpCode.value.trim()) {
    toastStore.error('Vui lòng nhập mã OTP 6 số');
    return;
  }
  if (forgotNewPassword.value.length < 6) {
    toastStore.error('Mật khẩu mới phải có ít nhất 6 ký tự');
    return;
  }
  if (forgotNewPassword.value !== forgotConfirmPassword.value) {
    toastStore.error('Mật khẩu mới và xác nhận mật khẩu không khớp');
    return;
  }

  resettingPassword.value = true;
  try {
    const msg = await authService.resetPasswordWithOtp({
      email: forgotEmail.value.trim(),
      otpCode: forgotOtpCode.value.trim(),
      newPassword: forgotNewPassword.value,
      confirmPassword: forgotConfirmPassword.value,
    });
    toastStore.success(msg || 'Đặt lại mật khẩu thành công!');
    password.value = forgotNewPassword.value;
    forgotStep.value = 3;
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Mã OTP không đúng hoặc đã hết hạn');
  } finally {
    resettingPassword.value = false;
  }
}

function fillDefaultAccount() {
  email.value = 'hieulm24@gmail.com';
  password.value = 'L@nhminhhieudeptrai.1';
  toastStore.info('Đã điền thông tin tài khoản mặc định');
}

async function handleLogin() {
  errorMessage.value = '';
  loading.value = true;
  try {
    await authStore.login(email.value.trim(), password.value);
    toastStore.success('Đăng nhập thành công! Chào mừng trở lại.');
    const redirect = (route.query.redirect as string) || '/dashboard';
    router.push(redirect);
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email/mật khẩu.';
    toastStore.error(errorMessage.value);
  } finally {
    loading.value = false;
  }
}
</script>
