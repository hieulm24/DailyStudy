<template>
  <div class="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="flex justify-center">
        <img
          src="/logo.png"
          alt="DailyStudy Hub Logo"
          class="w-16 h-16 object-contain rounded-md shadow-sm border border-slate-200 bg-white p-1"
        />
      </div>
      <h2 class="mt-4 text-center text-xl font-bold tracking-tight text-slate-900">
        DailyStudy Hub
      </h2>
      <p class="mt-1 text-center text-xs text-slate-500">
        Hệ thống quản lý kế hoạch hàng ngày & học tập thông minh
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4">
      <div class="bg-white py-8 px-6 shadow-sm border border-slate-200 rounded-md sm:px-10">
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
              @click="fillDefaultAccount"
            >
              Dùng tài khoản mặc định
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
        </form>

        <div class="mt-6 pt-4 border-t border-slate-100 text-center">
          <p class="text-[11px] text-slate-400">
            Hệ thống chạy LOCAL hoàn toàn riêng tư & an toàn.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../../stores/auth.store';
import { useToastStore } from '../../stores/toast.store';
import AppInput from '../../components/common/AppInput.vue';
import AppButton from '../../components/common/AppButton.vue';
import { Mail, Lock } from 'lucide-vue-next';

const email = ref('hieulm24@gmail.com');
const password = ref('L@nhminhhieudeptrai.1');
const rememberMe = ref(true);
const loading = ref(false);
const errorMessage = ref('');

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const toastStore = useToastStore();

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
