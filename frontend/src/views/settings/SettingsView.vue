<template>
  <div class="space-y-6 w-full">
    <!-- Header -->
    <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <div class="flex items-center gap-3">
          <div class="p-2.5 rounded-md bg-brand-50 text-brand-600">
            <Settings class="w-6 h-6" />
          </div>
          <div>
            <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Cài đặt hệ thống (Settings)</h2>
            <p class="text-sm sm:text-base text-slate-500 mt-1">
              Tùy chỉnh tài khoản, mục tiêu học tập và sao lưu / khôi phục dữ liệu
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- User Profile & Target Settings Form -->
    <div class="bg-white p-4 sm:p-5 rounded-md border border-slate-200 shadow-xs space-y-4">
      <h3 class="text-sm font-bold text-slate-900 pb-2 border-b border-slate-100">
        Thông tin tài khoản & Mục tiêu học
      </h3>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <AppInput
          v-model="form.displayName"
          label="Tên hiển thị"
          placeholder="Lãnh Minh Hiếu"
        />
        <AppInput
          v-model="form.email"
          label="Email (Cố định)"
          disabled
        />
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <AppInput
          v-model="form.dailyLearningTarget"
          label="Mục tiêu học tập hàng ngày (Số lượng mục)"
          type="number"
          placeholder="30"
          hint="Bao gồm từ vựng, ngữ pháp, bài nghe, bài nói và lượt ôn tập"
        />
        <AppSelect
          v-model="form.language"
          label="Ngôn ngữ giao diện"
          :options="languageOptions"
        />
      </div>

      <div class="flex items-center gap-2 pt-2">
        <input
          id="reviewEnabled"
          type="checkbox"
          v-model="form.reviewEnabled"
          class="rounded-sm text-brand-600 border-slate-300 focus:ring-brand-500"
        />
        <label for="reviewEnabled" class="text-xs text-slate-700 cursor-pointer select-none">
          Bật thuật toán nhắc nhở ôn tập Spaced Repetition
        </label>
      </div>

      <div class="pt-2 flex justify-end">
        <AppButton variant="primary" size="sm" :loading="saving" @click="saveSettings">
          Lưu cài đặt
        </AppButton>
      </div>
    </div>

    <!-- Backup & Restore (Export / Import JSON) -->
    <div class="bg-white p-6 rounded-md border border-slate-200 shadow-xs space-y-5">
      <div>
        <h3 class="text-sm font-bold text-slate-900">Sao lưu & Khôi phục dữ liệu (Backup / Restore)</h3>
        <p class="text-xs text-slate-500 mt-1">
          Dễ dàng xuất toàn bộ dữ liệu ra file JSON để lưu trữ hoặc khôi phục khi cần thiết
        </p>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
        <!-- Export Box -->
        <div class="p-4 rounded-md border border-slate-200 bg-slate-50/50 flex flex-col justify-between space-y-3">
          <div>
            <div class="flex items-center gap-2 text-slate-800 font-semibold text-xs mb-1">
              <Download class="w-4 h-4 text-brand-600" />
              <span>Xuất dữ liệu (Export JSON)</span>
            </div>
            <p class="text-[11px] text-slate-500 leading-relaxed">
              Tải về toàn bộ từ vựng, ngữ pháp, bài nghe, bài nói và cài đặt dưới dạng tệp JSON.
            </p>
          </div>
          <AppButton variant="outline" size="sm" :loading="exporting" @click="handleExport">
            Tải xuống file Backup (.json)
          </AppButton>
        </div>

        <!-- Import Box -->
        <div class="p-4 rounded-md border border-slate-200 bg-slate-50/50 flex flex-col justify-between space-y-3">
          <div>
            <div class="flex items-center gap-2 text-slate-800 font-semibold text-xs mb-1">
              <Upload class="w-4 h-4 text-emerald-600" />
              <span>Nhập dữ liệu (Import JSON)</span>
            </div>
            <p class="text-[11px] text-slate-500 leading-relaxed">
              Chọn tệp backup JSON để nạp lại dữ liệu học tập vào tài khoản hiện tại.
            </p>
          </div>
          <div>
            <input
              ref="fileInput"
              type="file"
              accept=".json"
              class="hidden"
              @change="handleFileSelected"
            />
            <AppButton variant="primary" size="sm" :loading="importing" @click="triggerFileInput">
              Chọn tệp JSON để khôi phục
            </AppButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { settingsService } from '../../services/settings.service';
import { useAuthStore } from '../../stores/auth.store';
import { useToastStore } from '../../stores/toast.store';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppSelect from '../../components/common/AppSelect.vue';
import { Settings, Download, Upload } from 'lucide-vue-next';

const authStore = useAuthStore();
const toastStore = useToastStore();

const saving = ref(false);
const exporting = ref(false);
const importing = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);

const form = reactive({
  displayName: '',
  email: '',
  dailyLearningTarget: 30,
  language: 'vi',
  theme: 'LIGHT',
  timezone: 'Asia/Ho_Chi_Minh',
  reviewEnabled: true,
});

const languageOptions = [
  { label: 'Tiếng Việt', value: 'vi' },
  { label: 'English', value: 'en' },
];

onMounted(async () => {
  try {
    const s = await settingsService.getSettings();
    form.displayName = s.displayName || authStore.user?.displayName || '';
    form.email = s.email || authStore.user?.email || '';
    form.dailyLearningTarget = s.dailyLearningTarget || 30;
    form.language = s.language || 'vi';
    form.theme = s.theme || 'LIGHT';
    form.timezone = s.timezone || 'Asia/Ho_Chi_Minh';
    form.reviewEnabled = s.reviewEnabled ?? true;
  } catch (e) {
    console.error(e);
  }
});

async function saveSettings() {
  saving.value = true;
  try {
    const updated = await settingsService.updateSettings({
      displayName: form.displayName,
      dailyLearningTarget: Number(form.dailyLearningTarget) || 30,
      language: form.language,
      theme: form.theme,
      timezone: form.timezone,
      reviewEnabled: form.reviewEnabled,
    });
    if (authStore.user) {
      authStore.user.displayName = updated.displayName || form.displayName;
    }
    toastStore.success('Đã lưu cài đặt thành công');
  } catch (err) {
    toastStore.error('Không thể lưu cài đặt');
  } finally {
    saving.value = false;
  }
}

async function handleExport() {
  exporting.value = true;
  try {
    const data = await settingsService.exportData();
    const jsonStr = JSON.stringify(data, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `english-learning-backup-${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
    toastStore.success('Đã tải xuống file backup thành công!');
  } catch (err) {
    toastStore.error('Không thể xuất dữ liệu');
  } finally {
    exporting.value = false;
  }
}

function triggerFileInput() {
  fileInput.value?.click();
}

async function handleFileSelected(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  importing.value = true;
  const reader = new FileReader();
  reader.onload = async (e) => {
    try {
      const content = e.target?.result as string;
      const parsed = JSON.parse(content);
      await settingsService.importData(parsed);
      toastStore.success('Khôi phục dữ liệu từ file backup thành công!');
    } catch (err) {
      toastStore.error('File backup không hợp lệ hoặc lỗi import');
    } finally {
      importing.value = false;
      if (fileInput.value) fileInput.value.value = '';
    }
  };
  reader.readAsText(file);
}
</script>
