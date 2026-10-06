<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 md:p-8 bg-slate-900/40 backdrop-blur-xs">
    <div
      class="bg-white rounded-md border border-slate-200 shadow-2xl w-full max-w-4xl lg:max-w-5xl max-h-[94vh] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-150"
    >
      <!-- Modal Header -->
      <div class="flex items-center justify-between px-8 py-5 border-b border-slate-100 shrink-0">
        <div class="flex items-center gap-3.5">
          <div class="p-2.5 rounded-md bg-amber-50 text-amber-600">
            <PlusCircle class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-base sm:text-lg font-bold text-slate-900">Tạo món ăn cá nhân mới</h3>
            <p class="text-xs text-slate-500 mt-0.5">Lưu vào kho thực phẩm riêng của bạn</p>
          </div>
        </div>
        <button
          type="button"
          class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
          @click="closeModal"
        >
          <X class="w-5 h-5" />
        </button>
      </div>

      <!-- Modal Body -->
      <div class="p-8 overflow-y-auto space-y-6 flex-1">
        <!-- Name & State & Serving Row -->
        <div class="grid grid-cols-1 md:grid-cols-12 gap-5">
          <!-- Name (6 cols) -->
          <div class="md:col-span-6">
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Tên thực phẩm / Món ăn <span class="text-rose-500">*</span>
            </label>
            <input
              v-model="name"
              type="text"
              placeholder="VD: Bánh mì pate, Sinh tố bơ..."
              class="w-full px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 focus:border-amber-600 shadow-2xs"
            />
          </div>

          <!-- State (2 cols) -->
          <div class="md:col-span-2">
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Trạng thái
            </label>
            <input
              v-model="state"
              type="text"
              placeholder="VD: Chín, Tươi..."
              class="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 focus:border-amber-600 shadow-2xs"
            />
          </div>

          <!-- Serving Amount (2 cols) -->
          <div class="md:col-span-2">
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Khối lượng chuẩn
            </label>
            <input
              v-model.number="servingAmount"
              type="number"
              min="1"
              placeholder="100"
              class="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 focus:border-amber-600 shadow-2xs"
            />
          </div>

          <!-- Serving Unit (2 cols) -->
          <div class="md:col-span-2">
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Đơn vị chuẩn
            </label>
            <select
              v-model="servingUnit"
              class="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 focus:border-amber-600 shadow-2xs cursor-pointer"
            >
              <option value="g">gram (g)</option>
              <option value="ml">milliliter (ml)</option>
              <option value="cái">cái</option>
              <option value="khẩu phần">khẩu phần</option>
            </select>
          </div>
        </div>

        <!-- Macros Box -->
        <div class="p-6 bg-slate-50/80 border border-slate-200 rounded-md space-y-4">
          <div class="flex items-center gap-2 text-xs font-bold text-slate-800">
            <Sparkles class="w-4 h-4 text-amber-500" />
            <span>Giá trị dinh dưỡng trên mỗi {{ servingAmount || 100 }}{{ servingUnit }}</span>
            <span class="text-rose-500">*</span>
          </div>
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div class="p-3.5 bg-white rounded-md border border-slate-200/80 shadow-2xs">
              <label class="block text-xs font-semibold text-slate-700 mb-1.5">Calories (kcal)</label>
              <input
                v-model.number="calories"
                type="number"
                min="0"
                step="any"
                placeholder="0"
                class="w-full px-3 py-2 text-sm bg-slate-50/50 border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600"
              />
            </div>
            <div class="p-3.5 bg-white rounded-md border border-slate-200/80 shadow-2xs">
              <label class="block text-xs font-semibold text-blue-700 mb-1.5">Protein (g)</label>
              <input
                v-model.number="protein"
                type="number"
                min="0"
                step="any"
                placeholder="0"
                class="w-full px-3 py-2 text-sm bg-slate-50/50 border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600"
              />
            </div>
            <div class="p-3.5 bg-white rounded-md border border-slate-200/80 shadow-2xs">
              <label class="block text-xs font-semibold text-amber-700 mb-1.5">Carb (g)</label>
              <input
                v-model.number="carbohydrate"
                type="number"
                min="0"
                step="any"
                placeholder="0"
                class="w-full px-3 py-2 text-sm bg-slate-50/50 border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 focus:border-amber-600"
              />
            </div>
            <div class="p-3.5 bg-white rounded-md border border-slate-200/80 shadow-2xs">
              <label class="block text-xs font-semibold text-rose-700 mb-1.5">Fat (g)</label>
              <input
                v-model.number="fat"
                type="number"
                min="0"
                step="any"
                placeholder="0"
                class="w-full px-3 py-2 text-sm bg-slate-50/50 border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-rose-500/20 focus:border-rose-600"
              />
            </div>
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-600 mb-1.5">Chất xơ - Fiber (g) (tùy chọn)</label>
            <input
              v-model.number="fiber"
              type="number"
              min="0"
              step="any"
              placeholder="0"
              class="w-full max-w-xs px-3.5 py-2 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-teal-500/20 focus:border-teal-600 shadow-2xs"
            />
          </div>
        </div>

        <!-- Data Source & Description -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Nguồn dữ liệu / Nhãn bao bì
            </label>
            <input
              v-model="dataSource"
              type="text"
              placeholder="VD: Nhãn bao bì, Cá nhân..."
              class="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 focus:border-amber-600 shadow-2xs"
            />
          </div>
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">
              Ghi chú thêm
            </label>
            <input
              v-model="description"
              type="text"
              placeholder="VD: 2 quả trứng + 10g bơ..."
              class="w-full px-3.5 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 focus:border-amber-600 shadow-2xs"
            />
          </div>
        </div>

        <!-- Error Message -->
        <p v-if="errorMessage" class="text-xs text-rose-600 font-medium flex items-center gap-1.5">
          <AlertCircle class="w-4 h-4" />
          <span>{{ errorMessage }}</span>
        </p>
      </div>

      <!-- Modal Footer -->
      <div class="flex items-center justify-between px-6 py-4 bg-slate-50 border-t border-slate-100">
        <button
          type="button"
          class="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200/60 rounded-md transition-colors"
          @click="closeModal"
        >
          Hủy bỏ
        </button>
        <button
          type="button"
          :disabled="isSubmitting || !name || calories === null"
          class="inline-flex items-center gap-2 px-5 py-2.5 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white rounded-md shadow-sm transition-colors cursor-pointer"
          @click="submitUserFood"
        >
          <Save class="w-4 h-4" />
          <span>Lưu thực phẩm mới</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { PlusCircle, X, Save, Sparkles, AlertCircle } from 'lucide-vue-next';
import { nutritionService } from '../../services/nutrition.service';
import type { NutritionUserFoodRequest } from '../../types/nutrition';

const props = defineProps<{
  isOpen: boolean;
  initialName?: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'created'): void;
}>();

const name = ref('');
const state = ref('Tươi/Chín');
const servingAmount = ref<number>(100);
const servingUnit = ref('g');
const calories = ref<number | null>(null);
const protein = ref<number | null>(null);
const carbohydrate = ref<number | null>(null);
const fat = ref<number | null>(null);
const fiber = ref<number | null>(0);
const dataSource = ref('Món tự tạo');
const description = ref('');

const isSubmitting = ref(false);
const errorMessage = ref('');

const submitUserFood = async () => {
  if (!name.value.trim() || calories.value === null || protein.value === null || carbohydrate.value === null || fat.value === null) {
    errorMessage.value = 'Vui lòng điền đầy đủ Tên, Calories, Protein, Carb và Fat';
    return;
  }

  isSubmitting.value = true;
  errorMessage.value = '';

  try {
    const payload: NutritionUserFoodRequest = {
      name: name.value.trim(),
      state: state.value.trim() || 'Tươi/Chín',
      servingAmount: servingAmount.value || 100,
      servingUnit: servingUnit.value || 'g',
      calories: calories.value,
      protein: protein.value,
      carbohydrate: carbohydrate.value,
      fat: fat.value,
      fiber: fiber.value || 0,
      dataSource: dataSource.value.trim() || 'Món tự tạo',
      description: description.value.trim(),
    };

    await nutritionService.createUserFood(payload);
    emit('created');
    closeModal();
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Có lỗi xảy ra khi tạo thực phẩm';
  } finally {
    isSubmitting.value = false;
  }
};

const closeModal = () => {
  emit('close');
};

watch(
  () => props.isOpen,
  (open) => {
    if (open) {
      name.value = props.initialName || '';
      state.value = 'Tươi/Chín';
      servingAmount.value = 100;
      servingUnit.value = 'g';
      calories.value = null;
      protein.value = null;
      carbohydrate.value = null;
      fat.value = null;
      fiber.value = 0;
      dataSource.value = 'Món tự tạo';
      description.value = '';
      errorMessage.value = '';
    }
  }
);
</script>
