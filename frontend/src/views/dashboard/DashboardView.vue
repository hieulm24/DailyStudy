<template>
  <div class="space-y-6">
    <!-- Welcome Banner & Quick Action Buttons -->
    <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <div class="flex items-center gap-3">
          <h2 class="text-xl sm:text-2xl font-bold text-slate-900">
            Xin chào, {{ authStore.user?.displayName || 'Minh Hiếu' }} 👋
          </h2>
          <span class="inline-flex items-center gap-1.5 px-3 py-1 text-xs font-semibold rounded-sm bg-orange-50 text-orange-700 border border-orange-200">
            <Flame class="w-4 h-4 text-orange-500 fill-orange-500" />
            {{ summary?.currentStreak || 0 }} ngày liên tiếp
          </span>
        </div>
        <p class="text-sm sm:text-base text-slate-500 mt-1.5">
          Hôm nay là ngày tuyệt vời để nâng cao trình độ tiếng Anh của bạn.
        </p>
      </div>

      <!-- Quick Navigate Buttons -->
      <div class="flex flex-wrap items-center gap-2">
        <router-link to="/vocabulary">
          <AppButton variant="outline" size="sm" :icon="BookOpen">Từ vựng</AppButton>
        </router-link>
        <router-link to="/grammar">
          <AppButton variant="outline" size="sm" :icon="Sparkles">Ngữ pháp</AppButton>
        </router-link>
        <router-link to="/listening">
          <AppButton variant="outline" size="sm" :icon="Headphones">Luyện nghe</AppButton>
        </router-link>
        <router-link to="/speaking">
          <AppButton variant="outline" size="sm" :icon="Mic">Luyện nói</AppButton>
        </router-link>
        <router-link to="/review">
          <AppButton variant="primary" size="sm" :icon="RefreshCw">Ôn tập</AppButton>
        </router-link>
      </div>
    </div>

    <!-- Review Reminder Banner -->
    <div
      v-if="summary && summary.todayNeedReview > 0"
      class="bg-amber-50 border border-amber-200 p-4 rounded-md flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 shadow-xs"
    >
      <div class="flex items-center gap-3">
        <div class="p-2 bg-amber-100 text-amber-700 rounded-md">
          <Bell class="w-5 h-5" />
        </div>
        <div>
          <h4 class="text-sm font-semibold text-amber-900">
            Bạn có {{ summary.todayNeedReview }} nội dung cần ôn tập hôm nay!
          </h4>
          <p class="text-xs text-amber-700 mt-0.5">
            Ôn tập theo Spaced Repetition giúp duy trì trí nhớ dài hạn và phản xạ tự nhiên.
          </p>
        </div>
      </div>
      <router-link to="/review">
        <AppButton variant="primary" size="sm" :icon="RefreshCw">
          Bắt đầu ôn ngay
        </AppButton>
      </router-link>
    </div>
    <div
      v-else-if="summary"
      class="bg-emerald-50 border border-emerald-200 p-3.5 rounded-md flex items-center gap-2.5 text-xs text-emerald-800"
    >
      <CheckCircle2 class="w-4 h-4 text-emerald-600 flex-shrink-0" />
      <span>Tuyệt vời! Bạn đã hoàn thành toàn bộ mục ôn tập hôm nay (You're all caught up!).</span>
    </div>

    <!-- Today's Stats Grid -->
    <div>
      <div class="flex items-center justify-between mb-3">
        <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500">
          Kết quả học tập hôm nay (Today's Learning)
        </h3>
        <span class="text-xs text-slate-400 font-medium">Mục tiêu: {{ summary?.dailyLearningTarget || 30 }} mục / ngày</span>
      </div>

      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
        <!-- Vocab Today -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs">
          <div class="flex items-center justify-between text-slate-500 mb-1">
            <span class="text-xs font-medium">Từ vựng</span>
            <BookOpen class="w-4 h-4 text-brand-600" />
          </div>
          <div class="text-2xl font-bold text-slate-900">{{ summary?.todayVocabulary || 0 }}</div>
          <div class="text-[10px] text-slate-400 mt-1">Tổng cộng: {{ summary?.totalVocabulary || 0 }}</div>
        </div>

        <!-- Grammar Today -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs">
          <div class="flex items-center justify-between text-slate-500 mb-1">
            <span class="text-xs font-medium">Ngữ pháp</span>
            <Sparkles class="w-4 h-4 text-purple-600" />
          </div>
          <div class="text-2xl font-bold text-slate-900">{{ summary?.todayGrammar || 0 }}</div>
          <div class="text-[10px] text-slate-400 mt-1">Tổng cộng: {{ summary?.totalGrammar || 0 }}</div>
        </div>

        <!-- Listening Today -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs">
          <div class="flex items-center justify-between text-slate-500 mb-1">
            <span class="text-xs font-medium">Luyện nghe</span>
            <Headphones class="w-4 h-4 text-emerald-600" />
          </div>
          <div class="text-2xl font-bold text-slate-900">{{ summary?.todayListening || 0 }}</div>
          <div class="text-[10px] text-slate-400 mt-1">Tổng cộng: {{ summary?.totalListening || 0 }}</div>
        </div>

        <!-- Speaking Today -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs">
          <div class="flex items-center justify-between text-slate-500 mb-1">
            <span class="text-xs font-medium">Luyện nói</span>
            <Mic class="w-4 h-4 text-rose-600" />
          </div>
          <div class="text-2xl font-bold text-slate-900">{{ summary?.todaySpeaking || 0 }}</div>
          <div class="text-[10px] text-slate-400 mt-1">Tổng cộng: {{ summary?.totalSpeaking || 0 }}</div>
        </div>

        <!-- Total Today -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs">
          <div class="flex items-center justify-between text-slate-500 mb-1">
            <span class="text-xs font-medium">Tổng hôm nay</span>
            <Target class="w-4 h-4 text-indigo-600" />
          </div>
          <div class="text-2xl font-bold text-brand-600">{{ summary?.todayTotal || 0 }}</div>
          <div class="text-[10px] text-slate-400 mt-1">Mục đã học</div>
        </div>

        <!-- Due Review -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs">
          <div class="flex items-center justify-between text-slate-500 mb-1">
            <span class="text-xs font-medium">Cần ôn tập</span>
            <RefreshCw class="w-4 h-4 text-amber-600" />
          </div>
          <div class="text-2xl font-bold text-amber-600">{{ summary?.todayNeedReview || 0 }}</div>
          <div class="text-[10px] text-slate-400 mt-1">Đã ôn: {{ summary?.todayCompletedReview || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- Today's Progress Bar -->
    <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs">
      <div class="flex items-center justify-between text-xs mb-2">
        <span class="font-semibold text-slate-700">Tiến độ mục tiêu học hôm nay</span>
        <span class="font-bold text-brand-600">{{ summary?.todayProgressPercent || 0 }}%</span>
      </div>
      <div class="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
        <div
          class="bg-brand-600 h-2.5 rounded-full transition-all duration-500 ease-out"
          :style="{ width: `${summary?.todayProgressPercent || 0}%` }"
        ></div>
      </div>
    </div>

    <!-- Chart & Activity Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
      <!-- Chart Column (2/3 width) -->
      <div class="lg:col-span-2 bg-white p-4 rounded-md border border-slate-200 shadow-xs">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between pb-3 mb-4 border-b border-slate-100 gap-2">
          <div>
            <h3 class="text-sm font-semibold text-slate-900">Biểu đồ học tập & ôn luyện</h3>
            <p class="text-xs text-slate-500">Số lượng nội dung đã tiếp thu qua từng ngày</p>
          </div>
          <div class="inline-flex rounded-md border border-slate-200 bg-white p-0.5 text-xs">
            <button
              type="button"
              :class="[
                'px-2.5 py-1 font-medium rounded-sm transition-colors',
                chartRange === 'LAST_7_DAYS' ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-50',
              ]"
              @click="loadChart('LAST_7_DAYS')"
            >
              7 ngày
            </button>
            <button
              type="button"
              :class="[
                'px-2.5 py-1 font-medium rounded-sm transition-colors',
                chartRange === 'LAST_30_DAYS' ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-50',
              ]"
              @click="loadChart('LAST_30_DAYS')"
            >
              30 ngày
            </button>
            <button
              type="button"
              :class="[
                'px-2.5 py-1 font-medium rounded-sm transition-colors',
                chartRange === 'THIS_MONTH' ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-50',
              ]"
              @click="loadChart('THIS_MONTH')"
            >
              Tháng này
            </button>
          </div>
        </div>

        <!-- Chart Container -->
        <div class="h-64 w-full">
          <Line v-if="chartDataConfig" :data="chartDataConfig" :options="chartOptions" />
          <div v-else class="h-full flex items-center justify-center text-xs text-slate-400">
            Đang tải dữ liệu biểu đồ...
          </div>
        </div>
      </div>

      <!-- Today's Activity Timeline (1/3 width) -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex flex-col">
        <div class="flex items-center justify-between pb-3 mb-4 border-b border-slate-100">
          <div>
            <h3 class="text-sm font-semibold text-slate-900">Hoạt động gần đây</h3>
            <p class="text-xs text-slate-500">Lịch sử tương tác hệ thống</p>
          </div>
          <router-link to="/statistics" class="text-xs font-medium text-brand-600 hover:text-brand-700">
            Xem tất cả
          </router-link>
        </div>

        <div v-if="summary && summary.recentActivities && summary.recentActivities.length > 0" class="flex-1 space-y-3 overflow-y-auto max-h-64 pr-1">
          <div
            v-for="act in summary.recentActivities"
            :key="act.id"
            class="flex items-start gap-3 p-2 rounded-md hover:bg-slate-50 transition-colors text-xs"
          >
            <div class="p-1.5 rounded-md bg-slate-100 text-slate-600 mt-0.5 flex-shrink-0">
              <component :is="getActivityIcon(act.activityType)" class="w-3.5 h-3.5 text-brand-600" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="font-medium text-slate-800 truncate">{{ act.title }}</p>
              <p v-if="act.description" class="text-[11px] text-slate-500 truncate mt-0.5">{{ act.description }}</p>
              <span class="text-[10px] text-slate-400 block mt-1">{{ formatTime(act.activityDate) }}</span>
            </div>
          </div>
        </div>

        <AppEmptyState
          v-else
          title="Chưa có hoạt động nào"
          description="Bắt đầu thêm từ vựng hoặc học ngữ pháp để ghi lại tiến độ!"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '../../stores/auth.store';
import { statisticsService } from '../../services/statistics.service';
import type { DashboardSummary, ChartData } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppEmptyState from '../../components/common/AppEmptyState.vue';
import {
  BookOpen,
  Sparkles,
  Headphones,
  Mic,
  RefreshCw,
  Flame,
  Bell,
  CheckCircle2,
  Target,
  Gamepad2,
} from 'lucide-vue-next';

// Chart.js imports
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js';
import { Line } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler);

const authStore = useAuthStore();
const summary = ref<DashboardSummary | null>(null);
const chartData = ref<ChartData | null>(null);
const chartRange = ref('LAST_7_DAYS');

onMounted(async () => {
  await loadDashboard();
  await loadChart('LAST_7_DAYS');
});

async function loadDashboard() {
  try {
    summary.value = await statisticsService.getDashboardSummary();
  } catch (e) {
    console.error('Failed to load dashboard', e);
  }
}

async function loadChart(range: string) {
  chartRange.value = range;
  try {
    chartData.value = await statisticsService.getChartData(range);
  } catch (e) {
    console.error('Failed to load chart data', e);
  }
}

const chartDataConfig = computed(() => {
  if (!chartData.value) return null;
  return {
    labels: chartData.value.labels,
    datasets: [
      {
        label: 'Từ vựng',
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        data: chartData.value.vocabularyData,
        tension: 0.3,
        fill: true,
      },
      {
        label: 'Ngữ pháp',
        borderColor: '#a855f7',
        backgroundColor: 'transparent',
        data: chartData.value.grammarData,
        tension: 0.3,
      },
      {
        label: 'Luyện nghe & nói',
        borderColor: '#10b981',
        backgroundColor: 'transparent',
        data: chartData.value.listeningData.map((v, i) => v + chartData.value!.speakingData[i]),
        tension: 0.3,
      },
      {
        label: 'Ôn tập',
        borderColor: '#f59e0b',
        backgroundColor: 'transparent',
        data: chartData.value.reviewData,
        tension: 0.3,
      },
    ],
  };
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top' as const,
      labels: {
        boxWidth: 10,
        font: {
          size: 11,
        },
      },
    },
    tooltip: {
      mode: 'index' as const,
      intersect: false,
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        precision: 0,
        font: { size: 10 },
      },
      grid: {
        color: '#f1f5f9',
      },
    },
    x: {
      ticks: {
        font: { size: 10 },
      },
      grid: {
        display: false,
      },
    },
  },
};

function getActivityIcon(type: string) {
  switch (type) {
    case 'ADD_VOCABULARY':
      return BookOpen;
    case 'LEARN_GRAMMAR':
      return Sparkles;
    case 'LISTEN':
      return Headphones;
    case 'SPEAK':
      return Mic;
    case 'REVIEW':
      return RefreshCw;
    case 'PLAY_GAME':
      return Gamepad2;
    default:
      return BookOpen;
  }
}

function formatTime(isoStr: string) {
  if (!isoStr) return '';
  const d = new Date(isoStr);
  return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) + ' - ' + d.toLocaleDateString('vi-VN');
}
</script>
