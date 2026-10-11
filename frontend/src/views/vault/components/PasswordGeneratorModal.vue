<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs animate-in fade-in duration-150">
    <div class="bg-white rounded-lg border border-slate-200 shadow-2xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-150">
      <!-- Header -->
      <div class="flex items-center justify-between px-5 py-4 border-b border-slate-100 bg-slate-50/60">
        <div class="flex items-center gap-2.5">
          <div class="p-2 rounded-md bg-indigo-50 text-indigo-600">
            <Sparkles class="w-4.5 h-4.5" />
          </div>
          <div>
            <h3 class="text-sm sm:text-base font-bold text-slate-900">Sinh mật khẩu ngẫu nhiên</h3>
            <p class="text-[11px] text-slate-500">Mã hóa ngẫu nhiên an toàn bảo mật cao</p>
          </div>
        </div>
        <button
          type="button"
          class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
          @click="$emit('close')"
        >
          <X class="w-4.5 h-4.5" />
        </button>
      </div>

      <!-- Body -->
      <div class="p-5 space-y-4">
        <!-- Generated Password Display Box -->
        <div class="relative bg-slate-50 border border-slate-200 rounded-lg p-3.5 group">
          <div class="flex items-center justify-between gap-2">
            <span class="font-mono text-sm sm:text-base font-bold text-slate-800 break-all select-all">
              {{ generatedPassword }}
            </span>
            <div class="flex items-center gap-1 shrink-0">
              <button
                type="button"
                class="p-2 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors cursor-pointer"
                title="Tạo lại"
                @click="generate"
              >
                <RefreshCw :class="['w-4 h-4', isGenerating ? 'animate-spin' : '']" />
              </button>
              <button
                type="button"
                class="p-2 text-slate-500 hover:text-emerald-600 hover:bg-emerald-50 rounded-md transition-colors cursor-pointer"
                title="Sao chép"
                @click="copyPassword"
              >
                <Check v-if="copied" class="w-4 h-4 text-emerald-600" />
                <Copy v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- Strength Meter -->
          <div class="mt-3 pt-2.5 border-t border-slate-200/80 flex items-center justify-between text-xs">
            <div class="flex items-center gap-2">
              <span class="text-slate-500 font-medium">Độ mạnh:</span>
              <span :class="['font-bold px-2 py-0.5 rounded-sm text-[11px]', strengthBadgeClass]">
                {{ strengthLabel }}
              </span>
            </div>
            <div class="flex items-center gap-1">
              <div
                v-for="i in 4"
                :key="i"
                :class="[
                  'w-6 h-1.5 rounded-full transition-all',
                  i <= strengthLevel ? strengthBarColor : 'bg-slate-200'
                ]"
              ></div>
            </div>
          </div>
        </div>

        <!-- Length Slider -->
        <div class="space-y-1.5">
          <div class="flex items-center justify-between text-xs">
            <label class="font-semibold text-slate-700">Độ dài mật khẩu:</label>
            <span class="font-bold text-indigo-600 font-mono text-sm">{{ options.length }} ký tự</span>
          </div>
          <input
            v-model.number="options.length"
            type="range"
            min="8"
            max="64"
            class="w-full accent-indigo-600 cursor-pointer"
            @input="generate"
          />
          <div class="flex justify-between text-[10px] text-slate-400">
            <span>8 (Tối thiểu)</span>
            <span>16 (Khuyên dùng)</span>
            <span>32</span>
            <span>64 (Tối đa)</span>
          </div>
        </div>

        <!-- Options Checkboxes -->
        <div class="space-y-2 pt-1 border-t border-slate-100 text-xs">
          <label class="flex items-center gap-2.5 cursor-pointer text-slate-700 hover:text-slate-900">
            <input
              v-model="options.includeUppercase"
              type="checkbox"
              class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer"
              @change="generate"
            />
            <span>Chữ hoa (A-Z)</span>
          </label>
          <label class="flex items-center gap-2.5 cursor-pointer text-slate-700 hover:text-slate-900">
            <input
              v-model="options.includeLowercase"
              type="checkbox"
              class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer"
              @change="generate"
            />
            <span>Chữ thường (a-z)</span>
          </label>
          <label class="flex items-center gap-2.5 cursor-pointer text-slate-700 hover:text-slate-900">
            <input
              v-model="options.includeNumbers"
              type="checkbox"
              class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer"
              @change="generate"
            />
            <span>Chữ số (0-9)</span>
          </label>
          <label class="flex items-center gap-2.5 cursor-pointer text-slate-700 hover:text-slate-900">
            <input
              v-model="options.includeSymbols"
              type="checkbox"
              class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer"
              @change="generate"
            />
            <span>Ký tự đặc biệt (!@#$%^&*...)</span>
          </label>
          <label class="flex items-center gap-2.5 cursor-pointer text-slate-700 hover:text-slate-900">
            <input
              v-model="options.excludeAmbiguous"
              type="checkbox"
              class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer"
              @change="generate"
            />
            <span>Tránh ký tự dễ nhầm lẫn (l, 1, I, O, 0)</span>
          </label>
        </div>
      </div>

      <!-- Footer -->
      <div class="px-5 py-3.5 border-t border-slate-100 bg-slate-50/60 flex items-center justify-end gap-2.5">
        <button
          type="button"
          class="px-3.5 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200/60 rounded-md transition-colors cursor-pointer"
          @click="$emit('close')"
        >
          Hủy bỏ
        </button>
        <button
          type="button"
          class="px-4 py-2 text-xs font-bold bg-indigo-600 hover:bg-indigo-700 text-white rounded-md transition-colors flex items-center gap-1.5 shadow-xs cursor-pointer"
          @click="selectPassword"
        >
          <Check class="w-3.5 h-3.5" />
          <span>Sử dụng mật khẩu này</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { Sparkles, X, RefreshCw, Copy, Check } from 'lucide-vue-next';
import { useToastStore } from '../../../stores/toast.store';

defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'selected', password: string): void;
}>();

const toast = useToastStore();
const isGenerating = ref(false);
const copied = ref(false);
const generatedPassword = ref('');

const options = reactive({
  length: 16,
  includeUppercase: true,
  includeLowercase: true,
  includeNumbers: true,
  includeSymbols: true,
  excludeAmbiguous: false,
});

const generate = () => {
  let upper = 'ABCDEFGHJKLMNPQRSTUVWXYZ';
  let lower = 'abcdefghijkmnopqrstuvwxyz';
  let digits = '23456789';
  let symbols = '!@#$%^&*()-_=+[]{}|;:,.<>?';

  if (!options.excludeAmbiguous) {
    upper += 'IO';
    lower += 'l';
    digits += '01';
  }

  let pool = '';
  if (options.includeUppercase) pool += upper;
  if (options.includeLowercase) pool += lower;
  if (options.includeNumbers) pool += digits;
  if (options.includeSymbols) pool += symbols;

  if (!pool) pool = lower + digits;

  const len = Math.max(8, Math.min(options.length, 64));
  let result = '';
  const cryptoObj = window.crypto || (window as any).msCrypto;
  const values = new Uint32Array(len);
  cryptoObj.getRandomValues(values);

  for (let i = 0; i < len; i++) {
    result += pool[values[i] % pool.length];
  }

  generatedPassword.value = result;
};

const copyPassword = async () => {
  if (!generatedPassword.value) return;
  try {
    await navigator.clipboard.writeText(generatedPassword.value);
    copied.value = true;
    toast.success('Đã sao chép mật khẩu vào bộ nhớ đệm!');
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  } catch (e) {
    toast.error('Không thể sao chép mật khẩu');
  }
};

const selectPassword = () => {
  emit('selected', generatedPassword.value);
  emit('close');
};

const strengthScore = computed(() => {
  const p = generatedPassword.value;
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

const strengthLevel = computed(() => {
  const s = strengthScore.value;
  if (s >= 80) return 4;
  if (s >= 60) return 3;
  if (s >= 40) return 2;
  return 1;
});

const strengthLabel = computed(() => {
  switch (strengthLevel.value) {
    case 4: return 'Rất mạnh';
    case 3: return 'Mạnh';
    case 2: return 'Trung bình';
    default: return 'Yếu';
  }
});

const strengthBadgeClass = computed(() => {
  switch (strengthLevel.value) {
    case 4: return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    case 3: return 'bg-blue-50 text-blue-700 border border-blue-200';
    case 2: return 'bg-amber-50 text-amber-700 border border-amber-200';
    default: return 'bg-rose-50 text-rose-700 border border-rose-200';
  }
});

const strengthBarColor = computed(() => {
  switch (strengthLevel.value) {
    case 4: return 'bg-emerald-500';
    case 3: return 'bg-blue-500';
    case 2: return 'bg-amber-500';
    default: return 'bg-rose-500';
  }
});

onMounted(() => {
  generate();
});
</script>
