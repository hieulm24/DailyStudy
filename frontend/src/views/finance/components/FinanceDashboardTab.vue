<template>
  <div class="space-y-6">
    <!-- Filters & Period Bar -->
    <div class="bg-white p-4 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center font-bold">
          <Calendar class="w-5 h-5" />
        </div>
        <div>
          <h3 class="text-base font-bold text-slate-900">Tổng quan & Thống kê tài chính</h3>
          <p class="text-xs text-slate-500">
            Xem số liệu thu chi, phân tích dòng tiền, cơ cấu danh mục và tiến độ tích lũy
          </p>
        </div>
      </div>

      <!-- Quick Month Picker & Refresh -->
      <div class="flex flex-wrap items-center gap-2.5 w-full md:w-auto">
        <select
          v-model="selectedMonth"
          @change="onPeriodChange"
          class="px-3 py-2 text-xs sm:text-sm font-medium bg-slate-50 border border-slate-200 rounded-md text-slate-700 hover:bg-slate-100/80 focus:outline-none focus:ring-2 focus:ring-brand-500"
        >
          <option v-for="m in 12" :key="m" :value="m">Tháng {{ m }}</option>
        </select>

        <select
          v-model="selectedYear"
          @change="onPeriodChange"
          class="px-3 py-2 text-xs sm:text-sm font-medium bg-slate-50 border border-slate-200 rounded-md text-slate-700 hover:bg-slate-100/80 focus:outline-none focus:ring-2 focus:ring-brand-500"
        >
          <option v-for="y in availableYears" :key="y" :value="y">Năm {{ y }}</option>
        </select>

        <button
          type="button"
          @click="setToCurrentMonth"
          class="px-3 py-2 text-xs sm:text-sm font-semibold text-brand-600 bg-brand-50 hover:bg-brand-100/80 rounded-md transition-colors"
        >
          Tháng hiện tại
        </button>

        <button
          type="button"
          @click="fetchDashboardData"
          :disabled="loading"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs sm:text-sm font-medium text-slate-700 bg-white border border-slate-200 rounded-md hover:bg-slate-50 transition-colors shadow-xs"
          title="Làm mới dữ liệu"
        >
          <RefreshCw :class="['w-4 h-4 text-slate-500', loading ? 'animate-spin' : '']" />
          <span>Làm mới</span>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading && !dashboardData" class="flex flex-col items-center justify-center py-20 bg-white rounded-md border border-slate-200">
      <div class="w-10 h-10 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      <span class="mt-4 text-sm font-medium text-slate-500">Đang tổng hợp dữ liệu tài chính...</span>
    </div>

    <!-- Main Content Grid -->
    <div v-else class="space-y-6">
      <!-- 6 Key Financial KPI Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <!-- Thu nhập trong tháng -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-emerald-300 transition-all">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Thu nhập tháng</span>
            <div class="w-8 h-8 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <TrendingUp class="w-4 h-4" />
            </div>
          </div>
          <div class="mt-3">
            <span class="text-lg font-bold text-emerald-600 tracking-tight">
              +{{ formatCurrency(dashboardData?.monthlyIncome) }}
            </span>
            <p class="text-[11px] text-slate-400 mt-1">Tháng {{ selectedMonth }}/{{ selectedYear }}</p>
          </div>
        </div>

        <!-- Chi tiêu trong tháng -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-rose-300 transition-all">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Chi tiêu tháng</span>
            <div class="w-8 h-8 rounded-md bg-rose-50 text-rose-600 flex items-center justify-center">
              <TrendingDown class="w-4 h-4" />
            </div>
          </div>
          <div class="mt-3">
            <span class="text-lg font-bold text-rose-600 tracking-tight">
              -{{ formatCurrency(dashboardData?.monthlyExpense) }}
            </span>
            <p class="text-[11px] text-slate-400 mt-1">Tháng {{ selectedMonth }}/{{ selectedYear }}</p>
          </div>
        </div>

        <!-- Còn lại sau thu chi (Tiết kiệm ròng) -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-brand-300 transition-all">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Còn lại sau thu chi</span>
            <div
              :class="[
                'w-8 h-8 rounded-md flex items-center justify-center',
                (dashboardData?.monthlyNetBalance || 0) >= 0 ? 'bg-brand-50 text-brand-600' : 'bg-rose-50 text-rose-600',
              ]"
            >
              <Scale class="w-4 h-4" />
            </div>
          </div>
          <div class="mt-3">
            <span
              :class="[
                'text-lg font-bold tracking-tight',
                (dashboardData?.monthlyNetBalance || 0) >= 0 ? 'text-brand-600' : 'text-rose-600',
              ]"
            >
              {{ formatCurrency(dashboardData?.monthlyNetBalance) }}
            </span>
            <p class="text-[11px] text-slate-400 mt-1">Dòng tiền thuần</p>
          </div>
        </div>

        <!-- Tỷ lệ tiết kiệm -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-purple-300 transition-all">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Tỷ lệ tiết kiệm</span>
            <div class="w-8 h-8 rounded-md bg-purple-50 text-purple-600 flex items-center justify-center">
              <Percent class="w-4 h-4" />
            </div>
          </div>
          <div class="mt-3">
            <span class="text-lg font-bold text-purple-600 tracking-tight">
              {{ savingsRate.toFixed(1) }}%
            </span>
            <p class="text-[11px] text-slate-400 mt-1">Trên tổng thu nhập</p>
          </div>
        </div>

        <!-- Tổng số dư các tài khoản -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-indigo-300 transition-all">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Tổng số dư các ví</span>
            <div class="w-8 h-8 rounded-md bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Wallet class="w-4 h-4" />
            </div>
          </div>
          <div class="mt-3">
            <span class="text-lg font-bold text-indigo-700 tracking-tight">
              {{ formatCurrency(dashboardData?.totalAccountBalance) }}
            </span>
            <p class="text-[11px] text-slate-400 mt-1">Tổng tài sản thực tế</p>
          </div>
        </div>

        <!-- Đã phân bổ cho mục tiêu -->
        <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between hover:border-amber-300 transition-all">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Đã tích lũy mục tiêu</span>
            <div class="w-8 h-8 rounded-md bg-amber-50 text-amber-600 flex items-center justify-center">
              <Target class="w-4 h-4" />
            </div>
          </div>
          <div class="mt-3">
            <span class="text-lg font-bold text-amber-600 tracking-tight">
              {{ formatCurrency(dashboardData?.totalAllocatedToGoals) }}
            </span>
            <p class="text-[11px] text-slate-400 mt-1">Khóa cho các kế hoạch</p>
          </div>
        </div>
      </div>

      <!-- Budget Planning & Projected Surplus Section -->
      <FinanceBudgetSection
        ref="budgetSectionRef"
        :month="selectedMonth"
        :year="selectedYear"
        @dataChanged="fetchDashboardData"
      />

      <!-- Charts Section: Row 1 - Biểu đồ Thu chi & Biểu đồ Biến động số dư -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Monthly Income vs Expense Bar Chart -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs">
          <div class="flex items-center justify-between mb-4">
            <div>
              <h3 class="text-base font-bold text-slate-900">So sánh thu chi qua các tháng</h3>
              <p class="text-xs text-slate-500">Đối sánh dòng tiền vào và dòng tiền ra</p>
            </div>
            <div class="flex items-center gap-3 text-xs">
              <span class="flex items-center gap-1.5 font-medium text-emerald-700">
                <span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> Thu nhập
              </span>
              <span class="flex items-center gap-1.5 font-medium text-rose-700">
                <span class="w-2.5 h-2.5 rounded-full bg-rose-500"></span> Chi tiêu
              </span>
            </div>
          </div>

          <div class="h-64 sm:h-72 w-full flex items-center justify-center">
            <Bar v-if="trendChartConfig" :data="trendChartConfig" :options="trendChartOptions" />
            <div v-else class="text-xs text-slate-400">Chưa có dữ liệu biểu đồ</div>
          </div>
        </div>

        <!-- Balance Trend Line Chart -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs">
          <div class="flex items-center justify-between mb-4">
            <div>
              <h3 class="text-base font-bold text-slate-900">Lịch sử biến động & Tích lũy số dư</h3>
              <p class="text-xs text-slate-500">Đường tăng trưởng tổng tài sản tích lũy qua từng tháng</p>
            </div>
          </div>

          <div class="h-64 sm:h-72 w-full flex items-center justify-center">
            <Line v-if="balanceTrendConfig" :data="balanceTrendConfig" :options="balanceTrendOptions" />
            <div v-else class="text-xs text-slate-400">Chưa có dữ liệu biến động số dư</div>
          </div>
        </div>
      </div>

      <!-- Charts Section: Row 2 - Hai biểu đồ Donut: Chi tiêu & Thu nhập theo danh mục -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Expense by Category Donut Chart -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div>
            <div class="flex items-center justify-between mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-900">Cơ cấu chi tiêu theo danh mục</h3>
                <p class="text-xs text-slate-500">Phân bổ chi tiêu trong tháng {{ selectedMonth }}/{{ selectedYear }}</p>
              </div>
            </div>

            <div class="h-56 sm:h-60 w-full flex items-center justify-center">
              <Doughnut
                v-if="categoryChartConfig && (dashboardData?.categoryExpenses?.length || 0) > 0"
                :data="categoryChartConfig"
                :options="categoryChartOptions"
              />
              <div v-else class="flex flex-col items-center justify-center text-center p-6 text-slate-400 text-xs">
                <PieChart class="w-8 h-8 text-slate-300 mb-2" />
                <span>Chưa có giao dịch chi tiêu trong tháng {{ selectedMonth }}/{{ selectedYear }}</span>
              </div>
            </div>
          </div>

          <!-- Category Legend List -->
          <div
            v-if="(dashboardData?.categoryExpenses?.length || 0) > 0"
            class="mt-4 pt-3 border-t border-slate-100 space-y-2 max-h-44 overflow-y-auto"
          >
            <div
              v-for="cat in dashboardData?.categoryExpenses.slice(0, 6)"
              :key="cat.categoryId"
              class="flex items-center justify-between text-xs"
            >
              <div class="flex items-center gap-2 truncate">
                <span class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: cat.color || '#64748b' }"></span>
                <span class="font-medium text-slate-700 truncate">{{ cat.categoryName }} ({{ cat.count }} lần)</span>
              </div>
              <div class="flex items-center gap-2 font-semibold">
                <span class="text-slate-900">{{ formatCurrency(cat.totalAmount) }}</span>
                <span class="text-slate-400 text-[11px]">({{ cat.percentage.toFixed(1) }}%)</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Income by Category Donut Chart -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div>
            <div class="flex items-center justify-between mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-900">Cơ cấu nguồn thu nhập</h3>
                <p class="text-xs text-slate-500">Nguồn tiền vào trong tháng {{ selectedMonth }}/{{ selectedYear }}</p>
              </div>
            </div>

            <div class="h-56 sm:h-60 w-full flex items-center justify-center">
              <Doughnut
                v-if="incomeCategoryConfig && (reportData?.incomeByCategory?.length || 0) > 0"
                :data="incomeCategoryConfig"
                :options="categoryChartOptions"
              />
              <div v-else class="flex flex-col items-center justify-center text-center p-6 text-slate-400 text-xs">
                <PieChart class="w-8 h-8 text-slate-300 mb-2" />
                <span>Chưa có nguồn thu nào trong tháng {{ selectedMonth }}/{{ selectedYear }}</span>
              </div>
            </div>
          </div>

          <!-- Income Legend List -->
          <div
            v-if="(reportData?.incomeByCategory?.length || 0) > 0"
            class="mt-4 pt-3 border-t border-slate-100 space-y-2 max-h-44 overflow-y-auto"
          >
            <div
              v-for="cat in reportData?.incomeByCategory.slice(0, 6)"
              :key="cat.categoryId"
              class="flex items-center justify-between text-xs"
            >
              <div class="flex items-center gap-2 truncate">
                <span class="w-2.5 h-2.5 rounded-full shrink-0" :style="{ backgroundColor: cat.color || '#10b981' }"></span>
                <span class="font-medium text-slate-700 truncate">{{ cat.categoryName }} ({{ cat.count }} lần)</span>
              </div>
              <div class="flex items-center gap-2 font-semibold">
                <span class="text-emerald-600">{{ formatCurrency(cat.totalAmount) }}</span>
                <span class="text-slate-400 text-[11px]">({{ cat.percentage.toFixed(1) }}%)</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Financial Goals & Recent Transactions Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Financial Goals Summary -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div>
            <div class="flex items-center justify-between mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-900">Mục tiêu tài chính & Tiến độ</h3>
                <p class="text-xs text-slate-500">Các kế hoạch tiết kiệm đang thực hiện</p>
              </div>
              <button
                type="button"
                @click="$emit('switchTab', 'goals')"
                class="text-xs font-semibold text-brand-600 hover:text-brand-700 hover:underline flex items-center gap-1"
              >
                <span>Quản lý tất cả</span>
                <ChevronRight class="w-4 h-4" />
              </button>
            </div>

            <!-- Empty Goals State -->
            <div
              v-if="!dashboardData?.goalsSummary || dashboardData.goalsSummary.length === 0"
              class="py-12 flex flex-col items-center justify-center text-center bg-slate-50/60 rounded-md border border-dashed border-slate-200"
            >
              <Target class="w-9 h-9 text-slate-300 mb-2" />
              <p class="text-sm font-semibold text-slate-700">Chưa có mục tiêu nào</p>
              <p class="text-xs text-slate-400 max-w-xs mt-1">
                Tạo các mục tiêu như Mua xe, Mua laptop, Quỹ dự phòng để theo dõi tích lũy
              </p>
              <button
                type="button"
                @click="$emit('openAddGoal')"
                class="mt-3 px-3 py-1.5 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-md transition-colors"
              >
                + Thêm mục tiêu ngay
              </button>
            </div>

            <!-- Goal Cards List -->
            <div v-else class="space-y-3.5">
              <div
                v-for="goal in dashboardData.goalsSummary.slice(0, 4)"
                :key="goal.id"
                class="p-3.5 rounded-md border border-slate-200 bg-slate-50/40 hover:bg-white hover:border-brand-200 transition-all cursor-pointer"
                @click="$emit('switchTab', 'goals')"
              >
                <div class="flex items-center justify-between gap-2">
                  <div class="flex items-center gap-2.5 truncate">
                    <div
                      class="w-7 h-7 rounded-md flex items-center justify-center text-white shrink-0 text-xs font-bold"
                      :style="{ backgroundColor: goal.color || '#2563eb' }"
                    >
                      <Target class="w-4 h-4" />
                    </div>
                    <span class="text-sm font-bold text-slate-800 truncate">{{ goal.name }}</span>
                  </div>
                  <span
                    :class="[
                      'px-2 py-0.5 text-[11px] font-bold rounded-full border',
                      getGoalStatusLabel(goal.status).class,
                    ]"
                  >
                    {{ getGoalStatusLabel(goal.status).label }}
                  </span>
                </div>

                <!-- Progress Bar -->
                <div class="mt-3">
                  <div class="flex items-center justify-between text-xs mb-1.5">
                    <span class="text-slate-500 font-medium">
                      {{ formatCurrency(goal.currentAmount) }} / {{ formatCurrency(goal.targetAmount) }}
                    </span>
                    <span class="font-bold text-brand-600">{{ goal.progressPercentage.toFixed(1) }}%</span>
                  </div>
                  <div class="w-full h-2 rounded-full bg-slate-200 overflow-hidden">
                    <div
                      class="h-full rounded-full transition-all duration-500"
                      :style="{
                        width: `${Math.min(100, goal.progressPercentage)}%`,
                        backgroundColor: goal.color || '#2563eb',
                      }"
                    ></div>
                  </div>
                </div>

                <!-- Footer note -->
                <div class="mt-2.5 flex items-center justify-between text-[11px] text-slate-400">
                  <span v-if="goal.remainingAmount > 0">
                    Còn thiếu: <strong class="text-slate-600 font-semibold">{{ formatCompactCurrency(goal.remainingAmount) }}</strong>
                  </span>
                  <span v-else class="text-emerald-600 font-bold">Đã đạt mục tiêu!</span>

                  <span v-if="goal.targetDate">Hạn: {{ formatDate(goal.targetDate) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Recent Transactions Table -->
        <div class="bg-white p-5 sm:p-6 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div>
            <div class="flex items-center justify-between mb-4">
              <div>
                <h3 class="text-base font-bold text-slate-900">Giao dịch gần nhất</h3>
                <p class="text-xs text-slate-500">Các hoạt động tài chính mới nhất</p>
              </div>
              <button
                type="button"
                @click="$emit('switchTab', 'transactions')"
                class="text-xs font-semibold text-brand-600 hover:text-brand-700 hover:underline flex items-center gap-1"
              >
                <span>Xem tất cả</span>
                <ChevronRight class="w-4 h-4" />
              </button>
            </div>

            <!-- Empty Transactions State -->
            <div
              v-if="!dashboardData?.recentTransactions || dashboardData.recentTransactions.length === 0"
              class="py-12 flex flex-col items-center justify-center text-center bg-slate-50/60 rounded-md border border-dashed border-slate-200"
            >
              <Receipt class="w-9 h-9 text-slate-300 mb-2" />
              <p class="text-sm font-semibold text-slate-700">Chưa có giao dịch nào</p>
              <p class="text-xs text-slate-400 max-w-xs mt-1">
                Bắt đầu ghi chép các khoản thu, chi hoặc chuyển khoản để quản lý tài chính
              </p>
              <button
                type="button"
                @click="$emit('openAddTransaction')"
                class="mt-3 px-3 py-1.5 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-md transition-colors"
              >
                + Ghi chép giao dịch ngay
              </button>
            </div>

            <!-- Transactions list -->
            <div v-else class="divide-y divide-slate-100">
              <div
                v-for="tx in dashboardData.recentTransactions.slice(0, 6)"
                :key="tx.id"
                class="py-3 flex items-center justify-between gap-3 hover:bg-slate-50/60 px-2 rounded-md transition-colors"
              >
                <div class="flex items-center gap-3 truncate">
                  <div
                    :class="[
                      'w-8 h-8 rounded-md flex items-center justify-center shrink-0 font-bold',
                      tx.type === 'INCOME'
                        ? 'bg-emerald-50 text-emerald-600'
                        : tx.type === 'EXPENSE'
                        ? 'bg-rose-50 text-rose-600'
                        : 'bg-indigo-50 text-indigo-600',
                    ]"
                  >
                    <ArrowDownLeft v-if="tx.type === 'INCOME'" class="w-4 h-4" />
                    <ArrowUpRight v-else-if="tx.type === 'EXPENSE'" class="w-4 h-4" />
                    <ArrowRightLeft v-else class="w-4 h-4" />
                  </div>
                  <div class="truncate">
                    <p class="text-xs font-bold text-slate-800 truncate">
                      {{ tx.description || tx.categoryName || (tx.type === 'TRANSFER' ? 'Chuyển tiền nội bộ' : 'Giao dịch') }}
                    </p>
                    <p class="text-[11px] text-slate-400 truncate">
                      {{ tx.accountName }}
                      <span v-if="tx.toAccountName"> ➔ {{ tx.toAccountName }}</span>
                      • {{ formatDate(tx.transactionDate) }}
                    </p>
                  </div>
                </div>

                <div class="text-right shrink-0">
                  <span
                    :class="[
                      'text-xs sm:text-sm font-bold',
                      tx.type === 'INCOME'
                        ? 'text-emerald-600'
                        : tx.type === 'EXPENSE'
                        ? 'text-rose-600'
                        : 'text-indigo-600',
                    ]"
                  >
                    {{ tx.type === 'INCOME' ? '+' : tx.type === 'EXPENSE' ? '-' : '' }}{{ formatCurrency(tx.amount) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import {
  Calendar,
  RefreshCw,
  TrendingUp,
  TrendingDown,
  Scale,
  Percent,
  Wallet,
  Target,
  PieChart,
  ChevronRight,
  Receipt,
  ArrowDownLeft,
  ArrowUpRight,
  ArrowRightLeft,
} from 'lucide-vue-next';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar, Doughnut, Line } from 'vue-chartjs';
import { financialService } from '../../../services/financial.service';
import type { FinancialDashboardData, FinancialReportData } from '../../../types';
import { formatCurrency, formatCompactCurrency, formatDate, getGoalStatusLabel } from '../../../utils/finance';
import FinanceBudgetSection from './FinanceBudgetSection.vue';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  ArcElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

defineEmits<{
  (e: 'switchTab', tab: string): void;
  (e: 'openAddTransaction'): void;
  (e: 'openAddGoal'): void;
}>();

const budgetSectionRef = ref<any>(null);
const loading = ref(false);
const dashboardData = ref<FinancialDashboardData | null>(null);
const reportData = ref<FinancialReportData | null>(null);

const now = new Date();
const selectedMonth = ref(now.getMonth() + 1);
const selectedYear = ref(now.getFullYear());

const availableYears = computed(() => {
  const current = now.getFullYear();
  return [current - 2, current - 1, current, current + 1];
});

const savingsRate = computed(() => {
  const income = dashboardData.value?.monthlyIncome || 0;
  if (income <= 0) return 0;
  const net = dashboardData.value?.monthlyNetBalance || 0;
  return Math.max(0, (net / income) * 100);
});

async function fetchDashboardData() {
  loading.value = true;
  try {
    const params = {
      year: selectedYear.value,
      month: selectedMonth.value,
    };
    const [dash, rep] = await Promise.all([
      financialService.getDashboard(params),
      financialService.getReports(params),
    ]);
    dashboardData.value = dash;
    reportData.value = rep;
  } catch (err) {
    console.error('Failed to load financial dashboard & reports', err);
  } finally {
    loading.value = false;
  }
}

function onPeriodChange() {
  fetchDashboardData();
}

function setToCurrentMonth() {
  selectedMonth.value = now.getMonth() + 1;
  selectedYear.value = now.getFullYear();
  fetchDashboardData();
}

// ==========================================
// CHART CONFIGURATIONS
// ==========================================

const trendChartConfig = computed(() => {
  if (!dashboardData.value?.monthlyTrends) return null;
  const labels = dashboardData.value.monthlyTrends.map((t) => t.label);
  const incomeData = dashboardData.value.monthlyTrends.map((t) => t.income);
  const expenseData = dashboardData.value.monthlyTrends.map((t) => t.expense);

  return {
    labels,
    datasets: [
      {
        label: 'Thu nhập',
        data: incomeData,
        backgroundColor: '#10b981',
        borderRadius: 4,
        maxBarThickness: 32,
      },
      {
        label: 'Chi tiêu',
        data: expenseData,
        backgroundColor: '#f43f5e',
        borderRadius: 4,
        maxBarThickness: 32,
      },
    ],
  };
});

const trendChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (context: any) => `${context.dataset.label}: ${formatCurrency(context.raw)}`,
      },
    },
  },
  scales: {
    x: { grid: { display: false } },
    y: {
      ticks: {
        callback: (val: any) => formatCompactCurrency(val),
      },
    },
  },
};

const balanceTrendConfig = computed(() => {
  if (!reportData.value?.balanceTrend || reportData.value.balanceTrend.length === 0) return null;
  const labels = reportData.value.balanceTrend.map((b) => {
    if (b.date && b.date.includes('-')) {
      const parts = b.date.split('-');
      return `Tháng ${parseInt(parts[1], 10)}/${parts[0]}`;
    }
    return b.date;
  });
  const balanceData = reportData.value.balanceTrend.map((b) => b.balance);

  return {
    labels,
    datasets: [
      {
        label: 'Tổng tài sản tích lũy',
        data: balanceData,
        borderColor: '#2563eb',
        backgroundColor: 'rgba(37, 99, 235, 0.12)',
        fill: true,
        tension: 0.35,
        pointBackgroundColor: '#2563eb',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6,
      },
    ],
  };
});

const balanceTrendOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (context: any) => `Tổng tài sản: ${formatCurrency(context.raw)}`,
      },
    },
  },
  scales: {
    x: { grid: { display: false } },
    y: {
      ticks: {
        callback: (val: any) => formatCompactCurrency(val),
      },
    },
  },
};

const categoryChartConfig = computed(() => {
  if (!dashboardData.value?.categoryExpenses || dashboardData.value.categoryExpenses.length === 0) {
    return null;
  }
  const labels = dashboardData.value.categoryExpenses.map((c) => c.categoryName);
  const data = dashboardData.value.categoryExpenses.map((c) => c.totalAmount);
  const backgroundColor = dashboardData.value.categoryExpenses.map(
    (c, i) => c.color || ['#ef4444', '#f97316', '#eab308', '#10b981', '#06b6d4', '#3b82f6', '#8b5cf6', '#ec4899'][i % 8]
  );

  return {
    labels,
    datasets: [
      {
        data,
        backgroundColor,
        borderWidth: 2,
        borderColor: '#ffffff',
      },
    ],
  };
});

const incomeCategoryConfig = computed(() => {
  if (!reportData.value?.incomeByCategory || reportData.value.incomeByCategory.length === 0) {
    return null;
  }
  const labels = reportData.value.incomeByCategory.map((c) => c.categoryName);
  const data = reportData.value.incomeByCategory.map((c) => c.totalAmount);
  const backgroundColor = reportData.value.incomeByCategory.map(
    (c, i) => c.color || ['#10b981', '#06b6d4', '#3b82f6', '#8b5cf6', '#f59e0b', '#ec4899'][i % 6]
  );

  return {
    labels,
    datasets: [
      {
        data,
        backgroundColor,
        borderWidth: 2,
        borderColor: '#ffffff',
      },
    ],
  };
});

const categoryChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (context: any) => `${context.label}: ${formatCurrency(context.raw)}`,
      },
    },
  },
  cutout: '68%',
};

onMounted(() => {
  fetchDashboardData();
});

defineExpose({
  refresh: () => {
    fetchDashboardData();
    budgetSectionRef.value?.refresh?.();
  },
});
</script>
