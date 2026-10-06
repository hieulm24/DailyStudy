<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 md:p-8 bg-slate-900/40 backdrop-blur-xs">
    <div
      class="bg-white rounded-md border border-slate-200 shadow-2xl w-full max-w-2xl lg:max-w-3xl max-h-[94vh] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-150"
    >
      <!-- Modal Header -->
      <div class="flex items-center justify-between px-8 py-5 border-b border-slate-100 shrink-0">
        <div class="flex items-center gap-3.5">
          <div class="p-2.5 rounded-md bg-blue-50 text-blue-600">
            <Target class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-base sm:text-lg font-bold text-slate-900">Thiết lập mục tiêu dinh dưỡng</h3>
            <p class="text-xs text-slate-500 mt-0.5">Đặt chỉ tiêu Calories và Macro hằng ngày</p>
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
        <!-- Body Stats -->
        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-2">Chỉ số thể trạng cá nhân</label>
          <div class="grid grid-cols-2 gap-5">
            <div>
              <label class="block text-xs font-medium text-slate-600 mb-1.5">
                Cân nặng (kg)
              </label>
              <input
                v-model.number="form.weight"
                type="number"
                step="0.1"
                placeholder="68"
                class="w-full px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 shadow-2xs"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-600 mb-1.5">
                Chiều cao (cm)
              </label>
              <input
                v-model.number="form.height"
                type="number"
                placeholder="172"
                class="w-full px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 shadow-2xs"
              />
            </div>
          </div>
        </div>

        <!-- Macro Targets -->
        <div class="space-y-5 pt-5 border-t border-slate-100">
          <div>
            <div class="flex items-center justify-between mb-2">
              <label class="text-xs font-semibold text-slate-700">
                Mục tiêu Calories hằng ngày (kcal/ngày)
              </label>
              <span class="text-xs font-bold text-slate-900 bg-slate-100 px-2.5 py-0.5 rounded-sm">{{ form.targetCalories }} kcal</span>
            </div>
            <input
              v-model.number="form.targetCalories"
              type="number"
              min="500"
              max="6000"
              placeholder="2100"
              class="w-full px-4 py-3 text-base bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600 font-bold text-slate-900 shadow-2xs"
            />
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div class="p-4 bg-slate-50/80 rounded-md border border-slate-200/80">
              <label class="block text-xs font-semibold text-blue-700 mb-1.5">Protein (g/ngày)</label>
              <input
                v-model.number="form.targetProtein"
                type="number"
                min="10"
                placeholder="130"
                class="w-full px-3.5 py-2 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 shadow-2xs"
              />
              <span class="text-[11px] text-slate-400 mt-1.5 block">≈ {{ Math.round((form.targetProtein || 0) * 4) }} kcal</span>
            </div>
            <div class="p-4 bg-slate-50/80 rounded-md border border-slate-200/80">
              <label class="block text-xs font-semibold text-amber-700 mb-1.5">Carb (g/ngày)</label>
              <input
                v-model.number="form.targetCarb"
                type="number"
                min="10"
                placeholder="250"
                class="w-full px-3.5 py-2 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 focus:border-amber-600 shadow-2xs"
              />
              <span class="text-[11px] text-slate-400 mt-1.5 block">≈ {{ Math.round((form.targetCarb || 0) * 4) }} kcal</span>
            </div>
            <div class="p-4 bg-slate-50/80 rounded-md border border-slate-200/80">
              <label class="block text-xs font-semibold text-rose-700 mb-1.5">Fat (g/ngày)</label>
              <input
                v-model.number="form.targetFat"
                type="number"
                min="10"
                placeholder="60"
                class="w-full px-3.5 py-2 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-rose-500/20 focus:border-rose-600 shadow-2xs"
              />
              <span class="text-[11px] text-slate-400 mt-1.5 block">≈ {{ Math.round((form.targetFat || 0) * 9) }} kcal</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="flex items-center justify-between px-8 py-5 bg-slate-50 border-t border-slate-100 shrink-0">
        <button
          type="button"
          class="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200/60 rounded-md transition-colors cursor-pointer"
          @click="closeModal"
        >
          Hủy bỏ
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-2 px-6 py-2.5 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-md shadow-sm transition-colors cursor-pointer"
          @click="saveTargets"
        >
          <Save class="w-4 h-4" />
          <span>Lưu mục tiêu</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { Target, X, Save } from 'lucide-vue-next';
import { nutritionService } from '../../services/nutrition.service';
import type { NutritionTargetSettings } from '../../types/nutrition';

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'saved', targets: NutritionTargetSettings): void;
}>();

const form = ref<NutritionTargetSettings>({
  weight: 68,
  height: 172,
  targetCalories: 2100,
  targetProtein: 130,
  targetCarb: 250,
  targetFat: 60,
});

const saveTargets = () => {
  nutritionService.saveTargetSettings(form.value);
  emit('saved', form.value);
  closeModal();
};

const closeModal = () => {
  emit('close');
};

watch(
  () => props.isOpen,
  (open) => {
    if (open) {
      form.value = nutritionService.getTargetSettings();
    }
  }
);
</script>
