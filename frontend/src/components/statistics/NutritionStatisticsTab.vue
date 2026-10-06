<template>
  <div class="space-y-6">
    <!-- Filter & Control Bar -->
    <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div class="flex flex-wrap items-center gap-1.5 bg-slate-50 p-1.5 rounded-md border border-slate-200">
        <button
          v-for="filter in rangeFilters"
          :key="filter.value"
          type="button"
          :class="[
            'px-3 py-1.5 text-xs font-semibold rounded-md transition-all cursor-pointer',
            selectedRange === filter.value
              ? 'bg-brand-600 text-white shadow-xs'
              : 'text-slate-600 hover:text-slate-900 hover:bg-white'
          ]"
          @click="selectRange(filter.value)"
        >
          {{ filter.label }}
        </button>
      </div>

      <!-- Custom Date Picker if selected -->
      <div v-if="selectedRange === 'CUSTOM'" class="flex items-center gap-2">
        <input
          v-model="customFromDate"
          type="date"
          class="text-xs font-medium border border-slate-200 rounded-md px-2.5 py-1.5 bg-white text-slate-800 focus:outline-brand-500"
          @change="fetchData"
        />
        <span class="text-xs text-slate-400">đến</span>
        <input
          v-model="customToDate"
          type="date"
          class="text-xs font-medium border border-slate-200 rounded-md px-2.5 py-1.5 bg-white text-slate-800 focus:outline-brand-500"
          @change="fetchData"
        />
      </div>

      <!-- TDEE & Weight Snapshot Display -->
      <div class="flex items-center gap-2 self-end md:self-auto text-xs text-slate-500 font-medium">
        <span class="bg-slate-100 px-2.5 py-1 rounded-md border border-slate-200">
          TDEE chuẩn: <strong class="text-slate-800">{{ stats?.tdee || 2100 }} kcal</strong>
        </span>
        <span class="bg-slate-100 px-2.5 py-1 rounded-md border border-slate-200">
          Cân nặng: <strong class="text-slate-800">{{ stats?.weightKg || 68 }} kg</strong>
        </span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="bg-white p-12 rounded-md border border-slate-200 text-center text-slate-400">
      <RefreshCw class="w-8 h-8 mx-auto animate-spin text-brand-500 mb-2" />
      <p class="text-sm font-medium">Đang tải và tổng hợp số liệu dinh dưỡng & tập luyện...</p>
    </div>

    <div v-else-if="stats" class="space-y-6">
      <!-- 6-Metric Overview Cards Grid -->
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3.5">
        <!-- 1. Food Intake -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Tổng Calo Ăn</span>
            <Utensils class="w-4 h-4 text-emerald-600" />
          </div>
          <div class="my-2">
            <span class="text-2xl font-bold text-slate-900">{{ Math.round(stats.totalFoodCalories).toLocaleString() }}</span>
            <span class="text-xs text-slate-400 ml-1 font-medium">kcal</span>
          </div>
          <span class="text-[11px] text-slate-400">
            TB: <strong class="text-slate-700 font-semibold">{{ Math.round(stats.avgDailyFoodCalories) }}</strong> kcal/ngày
          </span>
        </div>

        <!-- 2. Activity Net Burned -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Calo Tập Luyện</span>
            <Zap class="w-4 h-4 text-rose-600" />
          </div>
          <div class="my-2">
            <span class="text-2xl font-bold text-rose-600">+{{ Math.round(stats.totalActivityCalories).toLocaleString() }}</span>
            <span class="text-xs text-slate-400 ml-1 font-medium">kcal</span>
          </div>
          <span class="text-[11px] text-slate-400">
            {{ stats.workoutSessionsCount }} buổi • {{ Math.round(stats.totalWorkoutMinutes) }} phút
          </span>
        </div>

        <!-- 3. Total Burned -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Tổng Tiêu Hao</span>
            <Flame class="w-4 h-4 text-amber-600" />
          </div>
          <div class="my-2">
            <span class="text-2xl font-bold text-amber-700">{{ Math.round(stats.totalBurnedCalories).toLocaleString() }}</span>
            <span class="text-xs text-slate-400 ml-1 font-medium">kcal</span>
          </div>
          <span class="text-[11px] text-slate-400">
            TB: <strong class="text-slate-700 font-semibold">{{ Math.round(stats.avgDailyBurnedCalories) }}</strong> kcal/ngày
          </span>
        </div>

        <!-- 4. Net Calorie Balance -->
        <div
          :class="[
            'p-5 rounded-md border shadow-xs flex flex-col justify-between',
            stats.netCalorieBalance > 0
              ? 'bg-emerald-50/50 border-emerald-200'
              : stats.netCalorieBalance < 0
              ? 'bg-amber-50/50 border-amber-200'
              : 'bg-slate-50 border-slate-200'
          ]"
        >
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-slate-700">Thâm Hụt Ròng</span>
            <Scale
              class="w-4 h-4"
              :class="stats.netCalorieBalance > 0 ? 'text-emerald-600' : (stats.netCalorieBalance < 0 ? 'text-amber-600' : 'text-slate-400')"
            />
          </div>
          <div class="my-2 flex items-baseline gap-1">
            <span
              class="text-2xl font-black tracking-tight"
              :class="stats.netCalorieBalance > 0 ? 'text-emerald-700' : (stats.netCalorieBalance < 0 ? 'text-amber-700' : 'text-slate-800')"
            >
              {{ stats.netCalorieBalance > 0 ? '-' : (stats.netCalorieBalance < 0 ? '+' : '') }}{{ Math.abs(Math.round(stats.netCalorieBalance)).toLocaleString() }}
            </span>
            <span class="text-xs text-slate-500 font-medium">kcal</span>
          </div>
          <span
            class="text-[11px] font-semibold"
            :class="stats.netCalorieBalance > 0 ? 'text-emerald-600' : (stats.netCalorieBalance < 0 ? 'text-amber-600' : 'text-slate-400')"
          >
            <template v-if="stats.loggedDaysCount && stats.loggedDaysCount > 0">
              ≈ {{ Math.abs(stats.estimatedFatKgChange).toFixed(2) }} kg {{ stats.netCalorieBalance > 0 ? 'mỡ giảm' : 'tăng nạp' }}
            </template>
            <template v-else>
              Chưa có ngày ghi nhận
            </template>
          </span>
        </div>

        <!-- 5. Deficit Rate -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Tỷ Lệ Thâm Hụt</span>
            <Target class="w-4 h-4 text-blue-600" />
          </div>
          <div class="my-2">
            <span class="text-2xl font-bold text-blue-700">{{ stats.deficitRatePercent }}%</span>
          </div>
          <span class="text-[11px] text-slate-400">
            {{ stats.deficitDaysCount }}/{{ stats.loggedDaysCount || 0 }} ngày đạt Deficit
          </span>
        </div>

        <!-- 6. Daily Average Macros -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Macro TB Ngày</span>
            <Dna class="w-4 h-4 text-purple-600" />
          </div>
          <div class="my-2 grid grid-cols-3 gap-1 text-center bg-slate-50 p-1.5 rounded border border-slate-100">
            <div>
              <span class="block text-[9px] text-slate-400 font-semibold uppercase">Protein</span>
              <span class="text-xs font-bold text-blue-600">{{ Math.round(stats.avgDailyProtein) }}g</span>
            </div>
            <div>
              <span class="block text-[9px] text-slate-400 font-semibold uppercase">Carb</span>
              <span class="text-xs font-bold text-amber-600">{{ Math.round(stats.avgDailyCarbohydrate) }}g</span>
            </div>
            <div>
              <span class="block text-[9px] text-slate-400 font-semibold uppercase">Fat</span>
              <span class="text-xs font-bold text-rose-600">{{ Math.round(stats.avgDailyFat) }}g</span>
            </div>
          </div>
          <span class="text-[11px] text-slate-400">
            Chất xơ: {{ (stats.totalFiber / (stats.daysCount || 1)).toFixed(1) }}g/ngày
          </span>
        </div>
      </div>


      <!-- Main Energy Balance Trend Chart -->
      <div class="bg-white p-6 rounded-md border border-slate-200 shadow-xs space-y-4">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
          <div>
            <h3 class="text-sm font-bold text-slate-900 flex items-center gap-2">
              <BarChart3 class="w-4.5 h-4.5 text-brand-600" />
              Biểu đồ Cán cân Năng lượng (Ăn vào vs Tiêu hao theo ngày)
            </h3>
            <p class="text-xs text-slate-500 mt-0.5">
              So sánh lượng Calories Nạp vào (Intake) và Tổng Tiêu hao (Burned = TDEE + Tập) trong chu kỳ {{ stats.daysCount }} ngày
            </p>
          </div>

          <!-- Legend -->
          <div class="flex flex-wrap items-center gap-3 text-xs font-medium">
            <div class="flex items-center gap-1.5">
              <span class="w-3 h-3 rounded-xs bg-emerald-500"></span>
              <span class="text-slate-600">Ăn vào (Intake)</span>
            </div>
            <div class="flex items-center gap-1.5">
              <span class="w-3 h-3 rounded-xs bg-rose-500"></span>
              <span class="text-slate-600">Tổng Tiêu hao (Burned)</span>
            </div>
            <div class="flex items-center gap-1.5">
              <span class="w-3 h-0.5 bg-slate-400 border-t border-dashed"></span>
              <span class="text-slate-600">TDEE nền ({{ stats.tdee }})</span>
            </div>
          </div>
        </div>

        <!-- Custom Energy Balance Bar Chart Visualization -->
        <div class="pt-4 pb-2">
          <div class="h-64 flex items-end gap-2 sm:gap-4 justify-between border-b border-slate-200 px-2">
            <div
              v-for="item in stats.dailyTrend"
              :key="item.date"
              class="flex-1 flex flex-col items-center h-full justify-end group relative"
            >
              <!-- Tooltip on Hover -->
              <div class="absolute bottom-full mb-2 hidden group-hover:flex flex-col z-30 bg-slate-900 text-white text-[11px] rounded-md px-3 py-2 shadow-lg w-44 pointer-events-none transition-all">
                <span class="font-bold border-b border-slate-700 pb-1 mb-1 text-slate-200">
                  {{ item.dayOfWeek }}, {{ formatDateShort(item.date) }}
                </span>
                <div class="space-y-0.5">
                  <div class="flex justify-between">
                    <span class="text-slate-400">Ăn vào:</span>
                    <strong class="text-emerald-400">{{ Math.round(item.foodCalories) }} kcal</strong>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-slate-400">Tập luyện:</span>
                    <strong class="text-rose-400">+{{ Math.round(item.activityCalories) }} kcal</strong>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-slate-400">Tổng tiêu hao:</span>
                    <strong class="text-amber-400">{{ Math.round(item.totalBurned) }} kcal</strong>
                  </div>
                  <div class="flex justify-between pt-1 border-t border-slate-800">
                    <span class="text-slate-400">Cán cân:</span>
                    <strong :class="item.status === 'DEFICIT' ? 'text-emerald-400' : 'text-amber-400'">
                      {{ item.status === 'DEFICIT' ? '-' : '+' }}{{ Math.abs(Math.round(item.calorieBalance)) }} kcal ({{ item.status }})
                    </strong>
                  </div>
                </div>
              </div>

              <!-- Dual Bars: Food Intake vs Total Burned -->
              <div class="w-full flex items-end justify-center gap-1 h-52">
                <!-- Bar 1: Food Intake -->
                <div
                  class="w-1/2 max-w-6 bg-emerald-500 hover:bg-emerald-600 rounded-t-xs transition-all duration-300 relative"
                  :style="{ height: `${getBarHeight(item.foodCalories, maxChartCalorie)}%` }"
                ></div>

                <!-- Bar 2: Total Burned -->
                <div
                  class="w-1/2 max-w-6 bg-rose-500 hover:bg-rose-600 rounded-t-xs transition-all duration-300 relative"
                  :style="{ height: `${getBarHeight(item.totalBurned, maxChartCalorie)}%` }"
                ></div>
              </div>

              <!-- Date Label -->
              <span class="text-[10px] font-semibold text-slate-500 mt-2 truncate w-full text-center">
                {{ formatDateShort(item.date) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Workout & Activity Distribution Row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Workout Volume & Trends -->
        <div class="bg-white p-6 rounded-md border border-slate-200 shadow-xs space-y-4">
          <div class="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h3 class="text-sm font-bold text-slate-900 flex items-center gap-2">
                <Dumbbell class="w-4.5 h-4.5 text-rose-600" />
                Thời lượng tập luyện theo ngày
              </h3>
              <p class="text-xs text-slate-500 mt-0.5">
                Tổng cộng {{ Math.round(stats.totalWorkoutMinutes) }} phút tập trong chu kỳ
              </p>
            </div>
            <span class="text-xs font-bold text-rose-600 bg-rose-50 px-2.5 py-1 rounded-md border border-rose-100">
              TB: {{ (stats.totalWorkoutMinutes / (stats.daysCount || 1)).toFixed(0) }} phút/ngày
            </span>
          </div>

          <!-- Workout Bars -->
          <div class="h-44 flex items-end gap-2 justify-between pt-2 border-b border-slate-100">
            <div
              v-for="item in stats.dailyTrend"
              :key="item.date"
              class="flex-1 flex flex-col items-center h-full justify-end group relative"
            >
              <div
                class="w-full max-w-5 bg-rose-500/80 hover:bg-rose-600 rounded-t-xs transition-all duration-300"
                :style="{ height: `${getBarHeight(item.workoutMinutes, maxWorkoutMinutes)}%` }"
                :title="`${item.date}: ${item.workoutMinutes} phút tập, ${item.activityCalories} kcal`"
              ></div>
              <span class="text-[10px] text-slate-400 mt-1 truncate">
                {{ formatDateShort(item.date) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Activity Category Breakdown -->
        <div class="bg-white p-6 rounded-md border border-slate-200 shadow-xs space-y-4">
          <div class="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h3 class="text-sm font-bold text-slate-900 flex items-center gap-2">
                <Award class="w-4.5 h-4.5 text-amber-500" />
                Cơ cấu các môn tập luyện
              </h3>
              <p class="text-xs text-slate-500 mt-0.5">
                Phân bổ thời gian & năng lượng theo nhóm bài tập
              </p>
            </div>
          </div>

          <div v-if="stats.categoryBreakdown.length > 0" class="space-y-3 pt-1">
            <div
              v-for="cat in stats.categoryBreakdown"
              :key="cat.category"
              class="space-y-1"
            >
              <div class="flex items-center justify-between text-xs">
                <span class="font-bold text-slate-800">{{ cat.categoryName }}</span>
                <span class="text-slate-500">
                  <strong class="text-rose-600 font-semibold">{{ Math.round(cat.totalCalories) }} kcal</strong> • {{ Math.round(cat.totalMinutes) }} phút ({{ cat.percentage }}%)
                </span>
              </div>
              <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                <div
                  class="h-2 rounded-full transition-all duration-500"
                  :class="getCategoryColorClass(cat.category)"
                  :style="{ width: `${cat.percentage}%` }"
                ></div>
              </div>
            </div>
          </div>

          <div v-else class="text-xs text-slate-400 py-8 text-center">
            Chưa có ghi nhận bài tập nào trong khoảng thời gian này.
          </div>
        </div>
      </div>

      <!-- Detailed Daily Breakdown Table -->
      <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
        <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 class="text-sm font-bold text-slate-900">
              Nhật ký chi tiết chu kỳ theo ngày
            </h3>
            <p class="text-xs text-slate-500 mt-0.5">
              Bảng theo dõi toàn bộ thực phẩm, bài tập, tổng tiêu hao và cán cân năng lượng từng ngày
            </p>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="bg-slate-50/80 border-b border-slate-100 text-slate-600 font-semibold">
                <th class="py-3 px-4">Ngày</th>
                <th class="py-3 px-3 text-right">Ăn vào (Kcal)</th>
                <th class="py-3 px-3 text-right text-rose-600">Tập luyện (Kcal)</th>
                <th class="py-3 px-3 text-right text-amber-700">Tổng Tiêu hao</th>
                <th class="py-3 px-3 text-right">Cán cân Calo</th>
                <th class="py-3 px-3 text-center">Trạng thái</th>
                <th class="py-3 px-3 text-right text-blue-700">Protein</th>
                <th class="py-3 px-3 text-right text-amber-700">Carb</th>
                <th class="py-3 px-3 text-right text-rose-700">Fat</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="item in stats.dailyTrend"
                :key="item.date"
                class="hover:bg-slate-50/70 transition-colors"
              >
                <!-- Date -->
                <td class="py-3 px-4">
                  <div class="flex flex-col">
                    <span class="font-bold text-slate-900">{{ formatDateFull(item.date) }}</span>
                    <span class="text-[10px] text-slate-400">{{ item.dayOfWeek }}</span>
                  </div>
                </td>

                <!-- Food Kcal & Count -->
                <td class="py-3 px-3 text-right font-medium text-slate-800">
                  <span>{{ Math.round(item.foodCalories).toLocaleString() }} kcal</span>
                  <span v-if="item.foodCount > 0" class="block text-[10px] text-slate-400 font-normal">
                    ({{ item.foodCount }} món)
                  </span>
                </td>

                <!-- Workout Kcal & Duration -->
                <td class="py-3 px-3 text-right font-medium text-rose-600">
                  <span v-if="item.activityCalories > 0">+{{ Math.round(item.activityCalories) }} kcal</span>
                  <span v-else class="text-slate-300">-</span>
                  <span v-if="item.workoutMinutes > 0" class="block text-[10px] text-slate-400 font-normal">
                    ({{ Math.round(item.workoutMinutes) }} phút)
                  </span>
                </td>

                <!-- Total Burned -->
                <td class="py-3 px-3 text-right font-bold text-amber-700">
                  <span v-if="item.status !== 'NO_DATA'">{{ Math.round(item.totalBurned).toLocaleString() }} kcal</span>
                  <span v-else class="text-slate-300 font-normal">-</span>
                </td>

                <!-- Calorie Balance -->
                <td
                  class="py-3 px-3 text-right font-bold"
                  :class="item.status === 'DEFICIT' ? 'text-emerald-700' : (item.status === 'SURPLUS' ? 'text-amber-700' : (item.status === 'NO_DATA' ? 'text-slate-400 font-normal' : 'text-blue-700'))"
                >
                  <span v-if="item.status !== 'NO_DATA'">
                    {{ item.status === 'DEFICIT' ? '-' : (item.status === 'SURPLUS' ? '+' : '') }}{{ Math.abs(Math.round(item.calorieBalance)).toLocaleString() }} kcal
                  </span>
                  <span v-else class="text-slate-300 font-normal">-</span>
                </td>

                <!-- Status Badge -->
                <td class="py-3 px-3 text-center">
                  <span
                    :class="[
                      'px-2 py-0.5 text-[10px] font-bold rounded-sm border uppercase',
                      item.status === 'DEFICIT'
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : item.status === 'SURPLUS'
                        ? 'bg-amber-50 text-amber-700 border-amber-200'
                        : item.status === 'MAINTENANCE'
                        ? 'bg-blue-50 text-blue-700 border-blue-200'
                        : 'bg-slate-100 text-slate-500 border-slate-200 font-normal'
                    ]"
                  >
                    {{ item.status === 'NO_DATA' ? 'Chưa ghi nhận' : item.status }}
                  </span>
                </td>

                <!-- Protein -->
                <td class="py-3 px-3 text-right font-medium text-blue-700">
                  {{ item.protein > 0 ? `${item.protein.toFixed(1)}g` : '-' }}
                </td>

                <!-- Carb -->
                <td class="py-3 px-3 text-right font-medium text-amber-700">
                  {{ item.carbohydrate > 0 ? `${item.carbohydrate.toFixed(1)}g` : '-' }}
                </td>

                <!-- Fat -->
                <td class="py-3 px-3 text-right font-medium text-rose-700">
                  {{ item.fat > 0 ? `${item.fat.toFixed(1)}g` : '-' }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import {
  BarChart3,
  Utensils,
  Zap,
  Flame,
  Scale,
  Target,
  Dna,
  Dumbbell,
  Award,
  RefreshCw,
} from 'lucide-vue-next';
import { nutritionService } from '../../services/nutrition.service';
import type { NutritionStatisticsResponse } from '../../types/nutrition';

const rangeFilters = [
  { label: '7 Ngày gần nhất', value: '7_DAYS' },
  { label: '14 Ngày', value: '14_DAYS' },
  { label: '30 Ngày', value: '30_DAYS' },
  { label: 'Tháng này', value: 'THIS_MONTH' },
  { label: 'Tùy chỉnh', value: 'CUSTOM' },
];

const selectedRange = ref<string>('7_DAYS');
const customFromDate = ref<string>('');
const customToDate = ref<string>('');
const isLoading = ref<boolean>(false);
const stats = ref<NutritionStatisticsResponse | null>(null);

const targetSettings = ref(nutritionService.getTargetSettings());

const maxChartCalorie = computed(() => {
  if (!stats.value?.dailyTrend || stats.value.dailyTrend.length === 0) return 3000;
  const max = Math.max(
    ...stats.value.dailyTrend.map((d) => Math.max(Number(d.foodCalories), Number(d.totalBurned)))
  );
  return Math.max(max * 1.15, 2500);
});

const maxWorkoutMinutes = computed(() => {
  if (!stats.value?.dailyTrend || stats.value.dailyTrend.length === 0) return 60;
  const max = Math.max(...stats.value.dailyTrend.map((d) => Number(d.workoutMinutes)));
  return Math.max(max * 1.2, 30);
});

const selectRange = (range: string) => {
  selectedRange.value = range;
  if (range !== 'CUSTOM') {
    fetchData();
  }
};

const getBarHeight = (value: number, max: number) => {
  if (!value || !max) return 0;
  return Math.min(100, Math.max(4, Math.round((value / max) * 100)));
};

const formatDateShort = (dateStr: string) => {
  if (!dateStr) return '';
  const [, m, d] = dateStr.split('-');
  return `${d}/${m}`;
};

const formatDateFull = (dateStr: string) => {
  if (!dateStr) return '';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
};

const getCategoryColorClass = (category: string) => {
  switch (category) {
    case 'CARDIO':
      return 'bg-rose-500';
    case 'STRENGTH':
      return 'bg-blue-600';
    case 'CALISTHENICS':
      return 'bg-purple-600';
    case 'SPORTS':
      return 'bg-amber-500';
    default:
      return 'bg-emerald-500';
  }
};

const fetchData = async () => {
  isLoading.value = true;
  try {
    const data = await nutritionService.getStatistics({
      range: selectedRange.value,
      fromDate: selectedRange.value === 'CUSTOM' ? customFromDate.value : undefined,
      toDate: selectedRange.value === 'CUSTOM' ? customToDate.value : undefined,
      tdee: targetSettings.value.targetCalories || 2100,
      weight: targetSettings.value.weight || 68,
    });
    stats.value = data;
  } catch (err) {
    console.error('Failed to fetch nutrition statistics:', err);
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});

defineExpose({
  fetchData,
});
</script>
