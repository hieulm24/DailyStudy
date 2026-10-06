<template>
  <div class="bg-white rounded-md border border-slate-200 overflow-hidden shadow-2xs">
    <!-- Header -->
    <div class="px-5 py-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 bg-gradient-to-r from-slate-50/70 to-white">
      <div class="flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-rose-50 text-rose-600 border border-rose-100">
          <Flame class="w-5 h-5" />
        </div>
        <div>
          <h2 class="text-lg sm:text-xl font-bold text-slate-900 flex flex-wrap items-center gap-2.5">
            <span>Calories tiêu thụ & thâm hụt</span>
            <span
              :class="[
                'px-2.5 py-0.5 text-xs font-bold rounded-sm border',
                statusBadgeClass
              ]"
            >
              {{ statusDisplay }}
            </span>
          </h2>
          <p class="text-xs text-slate-500 mt-0.5">
            Theo dõi TDEE nền, năng lượng tập luyện theo MET và cán cân thâm hụt calo ngày {{ formattedDate }}
          </p>
        </div>
      </div>

      <!-- Quick TDEE & Weight Settings -->
      <div class="flex items-center gap-3 self-end sm:self-auto">
        <div class="flex items-center gap-1.5 bg-slate-50 border border-slate-200 rounded-md px-2.5 py-1.5">
          <span class="text-xs font-semibold text-slate-500">TDEE nền:</span>
          <input
            v-model.number="localTdee"
            type="number"
            min="500"
            max="6000"
            step="50"
            class="w-16 text-xs font-bold text-slate-900 bg-white border border-slate-200 rounded px-1.5 py-0.5 text-right focus:outline-emerald-500"
            @change="onTdeeOrWeightChange"
          />
          <span class="text-[11px] text-slate-400 font-medium">kcal</span>
        </div>

        <div class="flex items-center gap-1.5 bg-slate-50 border border-slate-200 rounded-md px-2.5 py-1.5">
          <span class="text-xs font-semibold text-slate-500">Cân nặng:</span>
          <input
            v-model.number="localWeight"
            type="number"
            min="20"
            max="300"
            step="0.5"
            class="w-14 text-xs font-bold text-slate-900 bg-white border border-slate-200 rounded px-1.5 py-0.5 text-right focus:outline-emerald-500"
            @change="onTdeeOrWeightChange"
          />
          <span class="text-[11px] text-slate-400 font-medium">kg</span>
        </div>
      </div>
    </div>

    <!-- Energy Balance Overview 4-Cards Grid -->
    <div class="p-5 border-b border-slate-100 bg-slate-50/40">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3.5">
        <!-- 1. Calories Consumed (Food) -->
        <div class="p-4 bg-white rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Calories ăn vào</span>
            <Utensils class="w-4 h-4 text-emerald-600" />
          </div>
          <div class="mt-2.5">
            <span class="text-xl font-bold text-slate-900">{{ Math.round(deficitSummary.foodCalories).toLocaleString() }}</span>
            <span class="text-xs text-slate-400 ml-1 font-medium">kcal</span>
          </div>
          <div class="text-[11px] text-slate-500 mt-1">
            Từ {{ deficitSummary.loggedFoodsCount }} món đã ghi nhận
          </div>
        </div>

        <!-- 2. Activity Net Calories -->
        <div class="p-4 bg-white rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Calories hoạt động</span>
            <Activity class="w-4 h-4 text-rose-600" />
          </div>
          <div class="mt-2.5">
            <span class="text-xl font-bold text-rose-600">+{{ Math.round(deficitSummary.activityCalories).toLocaleString() }}</span>
            <span class="text-xs text-slate-400 ml-1 font-medium">kcal</span>
          </div>
          <div class="text-[11px] text-slate-500 mt-1">
            {{ activityLogs.length }} hoạt động (Net MET)
          </div>
        </div>

        <!-- 3. Total Calories Burned -->
        <div class="p-4 bg-white rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Tổng tiêu hao</span>
            <Flame class="w-4 h-4 text-amber-600" />
          </div>
          <div class="mt-2.5">
            <span class="text-xl font-bold text-amber-700">{{ Math.round(deficitSummary.totalCaloriesBurned).toLocaleString() }}</span>
            <span class="text-xs text-slate-400 ml-1 font-medium">kcal</span>
          </div>
          <div class="text-[11px] text-slate-500 mt-1">
            TDEE ({{ localTdee }}) + Hoạt động ({{ Math.round(deficitSummary.activityCalories) }})
          </div>
        </div>

        <!-- 4. Calorie Balance (Deficit / Surplus) -->
        <div
          :class="[
            'p-4 rounded-md border shadow-2xs flex flex-col justify-between transition-colors',
            balanceCardClass
          ]"
        >
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-slate-700">
              {{ deficitSummary.status === 'DEFICIT' ? 'Thâm hụt' : (deficitSummary.status === 'SURPLUS' ? 'Thặng dư' : 'Cân bằng') }}
            </span>
            <component :is="balanceIcon" class="w-4 h-4" :class="balanceIconColor" />
          </div>
          <div class="mt-2.5 flex items-baseline gap-1">
            <span class="text-2xl font-black tracking-tight" :class="balanceTextColor">
              {{ deficitSummary.status === 'DEFICIT' ? '-' : (deficitSummary.status === 'SURPLUS' ? '+' : '') }}{{ Math.abs(Math.round(deficitSummary.calorieBalance)).toLocaleString() }}
            </span>
            <span class="text-xs text-slate-500 font-medium">kcal</span>
          </div>
          <div class="text-[11px] font-semibold mt-1" :class="balanceSubtitleColor">
            {{ balanceExplanation }}
          </div>
        </div>
      </div>

      <!-- Visual Deficit / Burned Progress Meter -->
      <div class="mt-4 bg-white p-3.5 rounded-md border border-slate-200 shadow-2xs">
        <div class="flex items-center justify-between text-xs font-semibold mb-1.5">
          <span class="text-slate-600">Tiến độ tiêu hao vs Ăn vào:</span>
          <span class="text-slate-900 font-bold">
            {{ Math.round(deficitSummary.foodCalories) }} / {{ Math.round(deficitSummary.totalCaloriesBurned) }} kcal
            <span class="text-slate-400 font-normal">({{ burnedPercent }}%)</span>
          </span>
        </div>
        <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden flex">
          <div
            :class="progressBarColor"
            class="h-2 rounded-full transition-all duration-500"
            :style="{ width: `${Math.min(100, burnedPercent)}%` }"
          ></div>
        </div>
      </div>
    </div>

    <!-- Main Content: Add Activity Form & Activity Log Table -->
    <div class="p-5 space-y-5">
      <!-- Add Activity Form -->
      <div class="bg-slate-50 p-4 rounded-md border border-slate-200">
        <div class="flex items-center justify-between mb-3">
          <h3 class="text-xs font-bold text-slate-900 flex items-center gap-2">
            <Zap class="w-4 h-4 text-amber-500" />
            Ghi nhận hoạt động thể chất hôm nay
          </h3>
          <span class="text-[11px] text-slate-500">Tính năng lượng tiêu hao chuẩn theo MET Compendium</span>
        </div>

        <form @submit.prevent="handleAddActivity" class="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
          <!-- Select Activity -->
          <div class="md:col-span-6">
            <label class="block text-xs font-semibold text-slate-700 mb-1">
              Chọn hoạt động <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <select
                v-model="selectedActivityId"
                class="w-full text-xs font-medium text-slate-800 bg-white border border-slate-200 rounded-md px-3 py-2 pr-8 focus:outline-emerald-500 focus:border-emerald-500 cursor-pointer"
                required
                @change="updatePreviewKcal"
              >
                <option :value="null" disabled>-- Chọn hoạt động thể chất --</option>
                <optgroup v-for="(group, catName) in groupedActivities" :key="catName" :label="formatCategory(catName)">
                  <option
                    v-for="act in group"
                    :key="act.id"
                    :value="act.id"
                  >
                    {{ act.name }} (MET: {{ act.metValue }} - {{ formatIntensity(act.intensity) }})
                  </option>
                </optgroup>
              </select>
            </div>
          </div>

          <!-- Duration Input -->
          <div class="md:col-span-3">
            <label class="block text-xs font-semibold text-slate-700 mb-1">
              Thời gian (phút) <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <input
                v-model.number="durationMinutes"
                type="number"
                min="1"
                max="600"
                step="1"
                placeholder="Ví dụ: 20"
                class="w-full text-xs font-semibold text-slate-800 bg-white border border-slate-200 rounded-md px-3 py-2 focus:outline-emerald-500 focus:border-emerald-500"
                required
                @input="updatePreviewKcal"
              />
              <span class="absolute right-3 top-2 text-xs text-slate-400 font-medium">phút</span>
            </div>
          </div>

          <!-- Real-time Preview & Submit Button -->
          <div class="md:col-span-3 flex items-center gap-2">
            <div class="flex-1 text-center bg-white border border-slate-200 rounded-md py-1.5 px-2">
              <span class="block text-[10px] text-slate-400 font-medium">Net Tiêu thụ</span>
              <span class="text-xs font-bold text-rose-600">≈ {{ previewKcal }} kcal</span>
            </div>
            <button
              type="submit"
              :disabled="isSubmitting || !selectedActivityId || !durationMinutes"
              class="inline-flex items-center justify-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-md shadow-xs transition-colors cursor-pointer whitespace-nowrap"
            >
              <Plus class="w-4 h-4" />
              <span>{{ isSubmitting ? 'Đang lưu...' : 'Thêm' }}</span>
            </button>
          </div>
        </form>

        <!-- Quick Duration Shortcuts -->
        <div class="flex flex-wrap items-center gap-2 mt-3 pt-2.5 border-t border-slate-200/70">
          <span class="text-[11px] text-slate-400 font-medium">Thời gian nhanh:</span>
          <button
            v-for="mins in [10, 15, 20, 30, 45, 60]"
            :key="mins"
            type="button"
            class="px-2 py-0.5 text-[11px] font-medium bg-white hover:bg-rose-50 text-slate-600 hover:text-rose-700 border border-slate-200 hover:border-rose-200 rounded transition-colors cursor-pointer"
            @click="setQuickDuration(mins)"
          >
            {{ mins }} phút
          </button>
        </div>
      </div>

      <!-- Activity Logs Table -->
      <div class="bg-white rounded-md border border-slate-200 overflow-hidden shadow-2xs">
        <div class="px-4 py-3 border-b border-slate-100 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <h4 class="text-xs font-bold text-slate-900">
              Danh sách hoạt động đã ghi nhận hôm nay
            </h4>
            <span class="px-2 py-0.5 text-[11px] font-semibold bg-slate-100 text-slate-700 rounded-full">
              {{ activityLogs.length }}
            </span>
          </div>
          <span class="text-xs font-bold text-rose-600">
            Tổng cộng: +{{ Math.round(deficitSummary.activityCalories) }} kcal
          </span>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="bg-slate-50/80 border-b border-slate-100 text-slate-600 font-semibold">
                <th class="py-2.5 px-3">Hoạt động</th>
                <th class="py-2.5 px-2.5 text-center">Phân loại</th>
                <th class="py-2.5 px-2.5 text-center">Cường độ</th>
                <th class="py-2.5 px-3 text-right">Thời gian</th>
                <th class="py-2.5 px-2.5 text-right">MET Snapshot</th>
                <th class="py-2.5 px-2.5 text-right">Weight</th>
                <th class="py-2.5 px-3 text-right text-rose-600">Net Calories</th>
                <th class="py-2.5 px-3 text-center w-20">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="log in activityLogs"
                :key="log.id"
                class="hover:bg-slate-50/70 transition-colors group"
              >
                <!-- Name -->
                <td class="py-3 px-3">
                  <span class="font-bold text-slate-900">{{ log.activityName }}</span>
                </td>

                <!-- Category -->
                <td class="py-3 px-2.5 text-center">
                  <span :class="['px-2 py-0.5 text-[10px] font-semibold rounded-sm border', getCategoryBadgeClass(log.category)]">
                    {{ formatCategory(log.category) }}
                  </span>
                </td>

                <!-- Intensity -->
                <td class="py-3 px-2.5 text-center">
                  <span :class="['px-2 py-0.5 text-[10px] font-semibold rounded-sm border', getIntensityBadgeClass(log.intensity)]">
                    {{ formatIntensity(log.intensity) }}
                  </span>
                </td>

                <!-- Duration (Editable) -->
                <td class="py-3 px-3 text-right font-medium text-slate-800">
                  <div v-if="editingLogId === log.id" class="flex items-center justify-end gap-1">
                    <input
                      v-model.number="editDurationValue"
                      type="number"
                      min="1"
                      max="600"
                      class="w-14 text-xs font-bold text-slate-900 bg-white border border-emerald-400 rounded px-1.5 py-0.5 text-right focus:outline-emerald-500"
                      @keyup.enter="saveEditLog(log)"
                      @keyup.esc="cancelEdit"
                    />
                    <span class="text-[11px] text-slate-500">p</span>
                    <button
                      type="button"
                      class="p-1 text-emerald-600 hover:bg-emerald-50 rounded cursor-pointer"
                      title="Lưu"
                      @click="saveEditLog(log)"
                    >
                      <Check class="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      class="p-1 text-slate-400 hover:bg-slate-100 rounded cursor-pointer"
                      title="Hủy"
                      @click="cancelEdit"
                    >
                      <X class="w-3.5 h-3.5" />
                    </button>
                  </div>
                  <div v-else class="flex items-center justify-end gap-1.5">
                    <span class="font-bold">{{ log.durationMinutes }}</span>
                    <span class="text-slate-500 font-normal">phút</span>
                  </div>
                </td>

                <!-- MET Snapshot -->
                <td class="py-3 px-2.5 text-right font-mono text-slate-600">
                  {{ log.metValue ? Number(log.metValue).toFixed(1) : '-' }}
                </td>

                <!-- Weight Snapshot -->
                <td class="py-3 px-2.5 text-right font-mono text-slate-600">
                  {{ log.weightKg ? Number(log.weightKg).toFixed(1) : '-' }} kg
                </td>

                <!-- Calories Burned -->
                <td class="py-3 px-3 text-right font-bold text-rose-600">
                  ≈ {{ Math.round(log.caloriesBurned) }} kcal
                </td>

                <!-- Actions -->
                <td class="py-3 px-3 text-center">
                  <div class="flex items-center justify-center gap-1">
                    <button
                      v-if="editingLogId !== log.id"
                      type="button"
                      class="p-1 text-slate-400 hover:text-emerald-600 hover:bg-slate-100 rounded transition-colors cursor-pointer"
                      title="Chỉnh sửa thời gian"
                      @click="startEdit(log)"
                    >
                      <Edit3 class="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      class="p-1 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors cursor-pointer"
                      title="Xóa hoạt động"
                      @click="confirmDeleteLog(log)"
                    >
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>

                  </div>
                </td>
              </tr>

              <!-- Empty State -->
              <tr v-if="activityLogs.length === 0">
                <td colspan="8" class="py-8 text-center text-slate-400">
                  <div class="flex flex-col items-center justify-center space-y-2">
                    <Dumbbell class="w-6 h-6 text-slate-300" />
                    <p class="text-xs font-medium text-slate-600">Chưa có hoạt động thể chất nào được ghi nhận cho ngày này.</p>
                    <div class="flex flex-wrap justify-center gap-1.5 mt-2">
                      <button
                        v-for="rec in quickRecommendations"
                        :key="rec.name"
                        type="button"
                        class="px-2.5 py-1 text-[11px] font-medium bg-slate-50 hover:bg-rose-50 text-slate-600 hover:text-rose-700 border border-slate-200 hover:border-rose-200 rounded transition-colors cursor-pointer"
                        @click="selectRecommendation(rec)"
                      >
                        + {{ rec.name }} ({{ rec.mins }}p)
                      </button>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Confirm Delete Activity Modal -->
    <AppConfirmDialog
      v-model="showDeleteConfirm"
      title="Xóa hoạt động tập luyện"
      :message="deletingActivity ? `Bạn có chắc chắn muốn xóa hoạt động '${deletingActivity.activityName}' (${deletingActivity.durationMinutes} phút) khỏi nhật ký không?` : 'Bạn có chắc chắn muốn xóa hoạt động này không?'"
      confirm-text="Xóa hoạt động"
      confirm-variant="danger"
      :loading="isDeletingActivity"
      @confirm="executeDeleteActivity"
    />
  </div>
</template>


<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import {
  Flame,
  Activity,
  Zap,
  Utensils,
  Plus,
  Trash2,
  Edit3,
  Check,
  X,
  Dumbbell,
  TrendingDown,
  TrendingUp,
  Scale,
} from 'lucide-vue-next';
import { nutritionService } from '../../services/nutrition.service';
import AppConfirmDialog from '../common/AppConfirmDialog.vue';
import { useToastStore } from '../../stores/toast.store';
import type {
  NutritionActivity,
  NutritionActivityLog,
  CalorieDeficitSummary,
} from '../../types/nutrition';

const toast = useToastStore();

const props = defineProps<{
  logDate: string;
  foodCalories: number;
  initialTdee?: number;
  initialWeight?: number;
}>();

const emit = defineEmits<{
  (e: 'updated', summary: CalorieDeficitSummary): void;
}>();

// State
const activities = ref<NutritionActivity[]>([]);
const activityLogs = ref<NutritionActivityLog[]>([]);
const showDeleteConfirm = ref<boolean>(false);
const deletingActivity = ref<NutritionActivityLog | null>(null);
const isDeletingActivity = ref<boolean>(false);

const deficitSummary = ref<CalorieDeficitSummary>({
  logDate: props.logDate,
  weightKg: props.initialWeight || 68,
  tdee: props.initialTdee || 2100,
  foodCalories: props.foodCalories || 0,
  activityCalories: 0,
  totalCaloriesBurned: (props.initialTdee || 2100),
  calorieBalance: (props.initialTdee || 2100) - (props.foodCalories || 0),
  status: 'DEFICIT',
  activityLogs: [],
  loggedFoodsCount: 0,
});

const localTdee = ref<number>(props.initialTdee || 2100);
const localWeight = ref<number>(props.initialWeight || 68);

const selectedActivityId = ref<number | null>(null);
const durationMinutes = ref<number | null>(20);
const previewKcal = ref<number>(0);
const isSubmitting = ref<boolean>(false);

const editingLogId = ref<number | null>(null);
const editDurationValue = ref<number>(20);

// Recommendations for empty state
const quickRecommendations = [
  { name: 'Nhảy dây', mins: 20 },
  { name: 'Tabata / HIIT', mins: 12 },
  { name: 'Tập tạ - nhiều bài', mins: 60 },
  { name: 'Cầu lông', mins: 60 },
];

// Computed
const formattedDate = computed(() => {
  if (!props.logDate) return '';
  const [year, month, day] = props.logDate.split('-');
  return `${day}/${month}/${year}`;
});

const groupedActivities = computed(() => {
  const groups: Record<string, NutritionActivity[]> = {};
  for (const act of activities.value) {
    const cat = act.category || 'OTHER';
    if (!groups[cat]) groups[cat] = [];
    groups[cat].push(act);
  }
  return groups;
});

const statusDisplay = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'Thâm hụt';
    case 'SURPLUS':
      return 'Thặng dư';
    default:
      return 'Cân bằng';
  }
});

const statusBadgeClass = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    case 'SURPLUS':
      return 'bg-amber-50 text-amber-700 border-amber-200';
    default:
      return 'bg-blue-50 text-blue-700 border-blue-200';
  }
});

const balanceCardClass = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'bg-emerald-50/60 border-emerald-200';
    case 'SURPLUS':
      return 'bg-amber-50/60 border-amber-200';
    default:
      return 'bg-blue-50/60 border-blue-200';
  }
});

const balanceIcon = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return TrendingDown;
    case 'SURPLUS':
      return TrendingUp;
    default:
      return Scale;
  }
});

const balanceIconColor = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'text-emerald-600';
    case 'SURPLUS':
      return 'text-amber-600';
    default:
      return 'text-blue-600';
  }
});

const balanceTextColor = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'text-emerald-700';
    case 'SURPLUS':
      return 'text-amber-700';
    default:
      return 'text-blue-700';
  }
});

const balanceSubtitleColor = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'text-emerald-600';
    case 'SURPLUS':
      return 'text-amber-600';
    default:
      return 'text-blue-600';
  }
});

const balanceExplanation = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'Nạp ít hơn tiêu hao (Giảm mỡ)';
    case 'SURPLUS':
      return 'Nạp nhiều hơn tiêu hao (Tăng cân)';
    default:
      return 'Năng lượng cân bằng (Giữ cân)';
  }
});

const burnedPercent = computed(() => {
  if (!deficitSummary.value.totalCaloriesBurned || deficitSummary.value.totalCaloriesBurned === 0) return 0;
  return Math.round((deficitSummary.value.foodCalories / deficitSummary.value.totalCaloriesBurned) * 100);
});

const progressBarColor = computed(() => {
  if (burnedPercent.value <= 100) return 'bg-emerald-500';
  return 'bg-amber-500';
});

// Methods
const formatCategory = (category: string) => {
  const map: Record<string, string> = {
    CARDIO: 'Cardio',
    STRENGTH: 'Tập tạ (Strength)',
    CALISTHENICS: 'Calisthenics',
    SPORTS: 'Thể thao',
    GENERAL: 'Phổ thông',
  };
  return map[category] || category;
};

const formatIntensity = (intensity: string) => {
  const map: Record<string, string> = {
    HIGH: 'Cường độ cao',
    MODERATE: 'Trung bình',
    LOW: 'Nhẹ nhàng',
  };
  return map[intensity] || intensity;
};

const getCategoryBadgeClass = (category: string) => {
  const map: Record<string, string> = {
    CARDIO: 'bg-rose-50 text-rose-700 border-rose-200',
    STRENGTH: 'bg-blue-50 text-blue-700 border-blue-200',
    CALISTHENICS: 'bg-purple-50 text-purple-700 border-purple-200',
    SPORTS: 'bg-amber-50 text-amber-700 border-amber-200',
  };
  return map[category] || 'bg-slate-50 text-slate-700 border-slate-200';
};

const getIntensityBadgeClass = (intensity: string) => {
  const map: Record<string, string> = {
    HIGH: 'bg-rose-50 text-rose-700 border-rose-200',
    MODERATE: 'bg-amber-50 text-amber-700 border-amber-200',
    LOW: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  };
  return map[intensity] || 'bg-slate-50 text-slate-700 border-slate-200';
};

// Calculate real-time preview Net MET Calories
const updatePreviewKcal = () => {
  if (!selectedActivityId.value || !durationMinutes.value || durationMinutes.value <= 0) {
    previewKcal.value = 0;
    return;
  }
  const act = activities.value.find((a) => a.id === selectedActivityId.value);
  if (!act) {
    previewKcal.value = 0;
    return;
  }
  const met = Number(act.metValue);
  const weight = Number(localWeight.value);
  const duration = Number(durationMinutes.value);
  // Net MET formula: (MET - 1) * 3.5 * weight / 200 * duration
  const netMet = Math.max(0, met - 1);
  const cal = (netMet * 3.5 * weight * duration) / 200;
  previewKcal.value = Math.round(cal);
};

const setQuickDuration = (mins: number) => {
  durationMinutes.value = mins;
  updatePreviewKcal();
};

const selectRecommendation = (rec: { name: string; mins: number }) => {
  const found = activities.value.find((a) => a.name.toLowerCase().includes(rec.name.toLowerCase()));
  if (found) {
    selectedActivityId.value = found.id;
    durationMinutes.value = rec.mins;
    updatePreviewKcal();
  }
};

const loadActivities = async () => {
  try {
    activities.value = await nutritionService.getActivities();
    if (activities.value.length > 0 && !selectedActivityId.value) {
      selectedActivityId.value = activities.value[0].id;
      updatePreviewKcal();
    }
  } catch (err) {
    console.error('Failed to load activities:', err);
  }
};

const fetchDeficitData = async () => {
  try {
    const summary = await nutritionService.getDeficitSummary(
      props.logDate,
      localTdee.value,
      localWeight.value
    );
    deficitSummary.value = summary;
    activityLogs.value = summary.activityLogs || [];
    emit('updated', summary);
  } catch (err) {
    console.error('Failed to fetch deficit summary:', err);
  }
};

const onTdeeOrWeightChange = () => {
  updatePreviewKcal();
  fetchDeficitData();
};

const handleAddActivity = async () => {
  if (!selectedActivityId.value || !durationMinutes.value || durationMinutes.value <= 0) return;
  isSubmitting.value = true;
  try {
    await nutritionService.addActivityLog({
      activityId: selectedActivityId.value,
      logDate: props.logDate,
      durationMinutes: durationMinutes.value,
      weightKg: localWeight.value,
    });
    toast.success('Đã ghi nhận hoạt động thể chất thành công!');
    await fetchDeficitData();
  } catch (err: any) {
    console.error('Failed to add activity log:', err);
    toast.error(err?.response?.data?.message || 'Không thể thêm hoạt động thể chất');
  } finally {
    isSubmitting.value = false;
  }
};

const startEdit = (log: NutritionActivityLog) => {
  editingLogId.value = log.id;
  editDurationValue.value = log.durationMinutes;
};

const cancelEdit = () => {
  editingLogId.value = null;
};

const saveEditLog = async (log: NutritionActivityLog) => {
  if (!editDurationValue.value || editDurationValue.value <= 0) return;
  try {
    await nutritionService.updateActivityLog(log.id, {
      activityId: log.activityId || 0,
      logDate: log.logDate,
      durationMinutes: editDurationValue.value,
      weightKg: localWeight.value,
    });
    toast.success('Đã cập nhật thời gian tập luyện!');
    editingLogId.value = null;
    await fetchDeficitData();
  } catch (err: any) {
    console.error('Failed to update activity log:', err);
    toast.error(err?.response?.data?.message || 'Không thể cập nhật hoạt động');
  }
};

const confirmDeleteLog = (log: NutritionActivityLog) => {
  deletingActivity.value = log;
  showDeleteConfirm.value = true;
};

const executeDeleteActivity = async () => {
  if (!deletingActivity.value) return;
  isDeletingActivity.value = true;
  try {
    await nutritionService.deleteActivityLog(deletingActivity.value.id);
    toast.success('Đã xóa hoạt động thể chất');
    showDeleteConfirm.value = false;
    deletingActivity.value = null;
    await fetchDeficitData();
  } catch (err: any) {
    console.error('Failed to delete activity log:', err);
    toast.error(err?.response?.data?.message || 'Không thể xóa hoạt động');
  } finally {
    isDeletingActivity.value = false;
  }
};


// Lifecycle & Watchers
onMounted(async () => {
  await loadActivities();
  await fetchDeficitData();
});

watch(
  () => props.logDate,
  () => {
    fetchDeficitData();
  }
);

watch(
  () => props.foodCalories,
  () => {
    fetchDeficitData();
  }
);

defineExpose({
  fetchDeficitData,
});
</script>
