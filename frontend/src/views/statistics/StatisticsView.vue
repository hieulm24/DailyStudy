<template>
  <div class="space-y-6 w-full">
    <!-- Header Banner -->
    <div class="bg-white p-6 sm:p-8 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-5">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 shrink-0 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center border border-brand-100/60 shadow-xs">
          <BarChart3 class="w-7 h-7" />
        </div>
        <div>
          <h2 class="text-xl sm:text-2xl lg:text-3xl font-bold text-slate-900 tracking-tight">
            Báo cáo & Thống kê hệ thống (Statistics)
          </h2>
          <p class="text-sm sm:text-base text-slate-500 mt-1">
            Theo dõi mức độ chuyên cần, biểu đồ tăng trưởng kiến thức và thống kê tiến độ công việc hàng ngày
          </p>
        </div>
      </div>

      <!-- Right Header Status & Action -->
      <div class="flex items-center gap-3 shrink-0 self-start md:self-auto">
        <div class="flex items-center gap-2 px-3.5 py-2 rounded-md bg-emerald-50 text-emerald-700 border border-emerald-100 text-xs sm:text-sm font-semibold">
          <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
          <span>Dữ liệu thời gian thực</span>
        </div>
        <button
          type="button"
          @click="refreshCurrentTabData"
          class="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs sm:text-sm font-medium text-slate-700 bg-white border border-slate-200 rounded-md hover:bg-slate-50 transition-colors shadow-xs"
          title="Làm mới dữ liệu"
        >
          <RefreshCw :class="['w-4 h-4 text-slate-500', isRefreshing ? 'animate-spin' : '']" />
          <span>Làm mới</span>
        </button>
      </div>
    </div>

    <!-- Main Tab Navigation Switcher -->
    <div class="bg-white p-1.5 rounded-md border border-slate-200 shadow-xs flex items-center gap-1">
      <button
        type="button"
        @click="activeTab = 'ENGLISH'"
        :class="[
          'flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-md text-sm font-bold transition-all',
          activeTab === 'ENGLISH'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
        ]"
      >
        <BookOpen class="w-4.5 h-4.5" />
        <span>Thống kê học Tiếng Anh</span>
      </button>

      <button
        type="button"
        @click="activeTab = 'TASKS'"
        :class="[
          'flex-1 flex items-center justify-center gap-2 py-3 px-4 rounded-md text-sm font-bold transition-all',
          activeTab === 'TASKS'
            ? 'bg-brand-600 text-white shadow-xs'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
        ]"
      >
        <CheckSquare class="w-4.5 h-4.5" />
        <span>Thống kê việc hàng ngày (Daily Tasks)</span>
      </button>
    </div>

    <!-- TAB 1: ENGLISH LEARNING STATISTICS -->
    <div v-if="activeTab === 'ENGLISH'" class="space-y-6">
      <!-- Overview Counters Grid -->
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3.5">
        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <span class="text-xs sm:text-sm text-slate-600 font-semibold block">Tổng Từ Vựng</span>
          <span class="text-2xl sm:text-3xl font-extrabold text-brand-600 mt-1">{{ summary?.totalVocabulary || 0 }}</span>
          <span class="text-xs text-slate-400 block mt-1">mục đã lưu</span>
        </div>

        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <span class="text-xs sm:text-sm text-slate-600 font-semibold block">Tổng Ngữ Pháp</span>
          <span class="text-2xl sm:text-3xl font-extrabold text-purple-600 mt-1">{{ summary?.totalGrammar || 0 }}</span>
          <span class="text-xs text-slate-400 block mt-1">cấu trúc</span>
        </div>

        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <span class="text-xs sm:text-sm text-slate-600 font-semibold block">Bài Luyện Nghe</span>
          <span class="text-2xl sm:text-3xl font-extrabold text-emerald-600 mt-1">{{ summary?.totalListening || 0 }}</span>
          <span class="text-xs text-slate-400 block mt-1">bài hoàn thành</span>
        </div>

        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <span class="text-xs sm:text-sm text-slate-600 font-semibold block">Bài Luyện Nói</span>
          <span class="text-2xl sm:text-3xl font-extrabold text-rose-600 mt-1">{{ summary?.totalSpeaking || 0 }}</span>
          <span class="text-xs text-slate-400 block mt-1">chủ đề đã nói</span>
        </div>

        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <span class="text-xs sm:text-sm text-slate-600 font-semibold block">Lượt Ôn Tập</span>
          <span class="text-2xl sm:text-3xl font-extrabold text-amber-600 mt-1">{{ summary?.totalReviews || 0 }}</span>
          <span class="text-xs text-slate-400 block mt-1">lượt spaced rep</span>
        </div>

        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <span class="text-xs sm:text-sm text-slate-600 font-semibold block">Lượt Chơi Game</span>
          <span class="text-2xl sm:text-3xl font-extrabold text-indigo-600 mt-1">{{ summary?.totalGameSessions || 0 }}</span>
          <span class="text-xs text-slate-400 block mt-1">phiên thử thách</span>
        </div>
      </div>

      <!-- Streak & Study Days Card + Heatmap -->
      <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs space-y-4">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
          <div class="flex items-center gap-3.5">
            <div class="p-3 rounded-md bg-orange-50 text-orange-600">
              <Flame class="w-7 h-7 fill-orange-500" />
            </div>
            <div>
              <h3 class="text-lg font-bold text-slate-900">
                Chuỗi học liên tục (Study Streak): {{ heatmapData?.currentStreak || 0 }} ngày
              </h3>
              <p class="text-sm sm:text-base text-slate-500 mt-0.5">
                Kỷ lục cao nhất: <strong class="text-slate-800">{{ heatmapData?.longestStreak || 0 }} ngày</strong> | Tổng số ngày đã học: <strong class="text-slate-800">{{ heatmapData?.totalStudyDays || 0 }} ngày</strong>
              </p>
            </div>
          </div>
        </div>

        <!-- Contribution Heatmap Grid (Last 90 days) -->
        <div>
          <div class="flex items-center justify-between text-xs text-slate-500 mb-2">
            <span>Lịch sử chuyên cần (90 ngày gần nhất)</span>
            <div class="flex items-center gap-1 text-[10px]">
              <span>Ít</span>
              <span class="w-2.5 h-2.5 rounded-xs bg-slate-100"></span>
              <span class="w-2.5 h-2.5 rounded-xs bg-emerald-200"></span>
              <span class="w-2.5 h-2.5 rounded-xs bg-emerald-400"></span>
              <span class="w-2.5 h-2.5 rounded-xs bg-emerald-600"></span>
              <span>Nhiều</span>
            </div>
          </div>

          <div class="flex flex-wrap gap-1.5 p-3 bg-slate-50/70 rounded-md border border-slate-100 max-h-40 overflow-y-auto">
            <div
              v-for="day in heatmapData?.heatmapDays"
              :key="day.date"
              :title="`${day.date}: ${day.count} hoạt động học tập`"
              :class="[
                'w-3.5 h-3.5 rounded-xs transition-transform hover:scale-125 cursor-pointer',
                getHeatmapColor(day.level),
              ]"
            ></div>
          </div>
        </div>
      </div>

      <!-- Chart Section with Range Controls -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs space-y-4">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
          <div>
            <h3 class="text-sm font-semibold text-slate-900">Phân tích chi tiết theo thời gian</h3>
            <p class="text-xs text-slate-500">So sánh số lượng nội dung học của từng kỹ năng</p>
          </div>

          <!-- Filter Controls -->
          <div class="flex flex-wrap items-center gap-2">
            <div class="inline-flex rounded-md border border-slate-200 bg-white p-0.5 text-xs">
              <button
                type="button"
                :class="[
                  'px-2.5 py-1 font-medium rounded-sm transition-colors',
                  chartRange === 'LAST_7_DAYS' ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-50',
                ]"
                @click="setChartRange('LAST_7_DAYS')"
              >
                7 ngày
              </button>
              <button
                type="button"
                :class="[
                  'px-2.5 py-1 font-medium rounded-sm transition-colors',
                  chartRange === 'LAST_30_DAYS' ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-50',
                ]"
                @click="setChartRange('LAST_30_DAYS')"
              >
                30 ngày
              </button>
              <button
                type="button"
                :class="[
                  'px-2.5 py-1 font-medium rounded-sm transition-colors',
                  chartRange === 'THIS_MONTH' ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-50',
                ]"
                @click="setChartRange('THIS_MONTH')"
              >
                Tháng này
              </button>
              <button
                type="button"
                :class="[
                  'px-2.5 py-1 font-medium rounded-sm transition-colors',
                  chartRange === 'CUSTOM' ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-50',
                ]"
                @click="setChartRange('CUSTOM')"
              >
                Tùy chọn
              </button>
            </div>

            <div v-if="chartRange === 'CUSTOM'" class="flex items-center gap-1.5 text-xs">
              <input
                type="date"
                v-model="customFromDate"
                class="border border-slate-200 rounded-md px-2 py-1 bg-white text-slate-700"
              />
              <span>-</span>
              <input
                type="date"
                v-model="customToDate"
                class="border border-slate-200 rounded-md px-2 py-1 bg-white text-slate-700"
              />
              <AppButton variant="secondary" size="sm" @click="loadCustomChart">Xem</AppButton>
            </div>
          </div>
        </div>

        <div class="h-72 w-full">
          <Line v-if="chartDataConfig" :data="chartDataConfig" :options="chartOptions" />
          <div v-else class="h-full flex items-center justify-center text-xs text-slate-400">
            Đang tải dữ liệu biểu đồ...
          </div>
        </div>
      </div>
    </div>

    <!-- TAB 2: DAILY TASKS STATISTICS -->
    <div v-else-if="activeTab === 'TASKS'" class="space-y-6">
      <!-- Task Stats Filter Toolbar -->
      <div class="bg-white p-4 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div class="flex items-center gap-2">
          <div class="w-10 h-10 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center font-bold">
            <Calendar class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-base font-bold text-slate-900">Bộ lọc thời gian thống kê công việc</h3>
            <p class="text-xs text-slate-500">Xem tiến độ hoàn thành theo từng ngày</p>
          </div>
        </div>

        <!-- Filter Controls -->
        <div class="flex flex-wrap items-center gap-2">
          <div class="inline-flex rounded-md border border-slate-200 bg-white p-0.5 text-xs">
            <button
              type="button"
              :class="[
                'px-3 py-1.5 font-medium rounded-sm transition-colors',
                taskDateFilterRange === 'LAST_7_DAYS' ? 'bg-brand-600 text-white shadow-xs' : 'text-slate-600 hover:bg-slate-50',
              ]"
              @click="setTaskDateRange('LAST_7_DAYS')"
            >
              7 ngày qua
            </button>
            <button
              type="button"
              :class="[
                'px-3 py-1.5 font-medium rounded-sm transition-colors',
                taskDateFilterRange === 'LAST_30_DAYS' ? 'bg-brand-600 text-white shadow-xs' : 'text-slate-600 hover:bg-slate-50',
              ]"
              @click="setTaskDateRange('LAST_30_DAYS')"
            >
              30 ngày qua
            </button>
            <button
              type="button"
              :class="[
                'px-3 py-1.5 font-medium rounded-sm transition-colors',
                taskDateFilterRange === 'THIS_MONTH' ? 'bg-brand-600 text-white shadow-xs' : 'text-slate-600 hover:bg-slate-50',
              ]"
              @click="setTaskDateRange('THIS_MONTH')"
            >
              Tháng này
            </button>
            <button
              type="button"
              :class="[
                'px-3 py-1.5 font-medium rounded-sm transition-colors',
                taskDateFilterRange === 'ALL' ? 'bg-brand-600 text-white shadow-xs' : 'text-slate-600 hover:bg-slate-50',
              ]"
              @click="setTaskDateRange('ALL')"
            >
              Tất cả
            </button>
            <button
              type="button"
              :class="[
                'px-3 py-1.5 font-medium rounded-sm transition-colors',
                taskDateFilterRange === 'CUSTOM' ? 'bg-brand-600 text-white shadow-xs' : 'text-slate-600 hover:bg-slate-50',
              ]"
              @click="setTaskDateRange('CUSTOM')"
            >
              Tùy chọn
            </button>
          </div>

          <div v-if="taskDateFilterRange === 'CUSTOM'" class="flex items-center gap-1.5 text-xs">
            <input
              type="date"
              v-model="taskCustomFromDate"
              class="border border-slate-200 rounded-md px-2.5 py-1.5 bg-white text-slate-700"
            />
            <span>-</span>
            <input
              type="date"
              v-model="taskCustomToDate"
              class="border border-slate-200 rounded-md px-2.5 py-1.5 bg-white text-slate-700"
            />
            <AppButton variant="secondary" size="sm" @click="() => loadTaskStats(0)">Xem</AppButton>
          </div>
        </div>
      </div>

      <!-- Task Stats Overview Cards -->
      <div class="grid grid-cols-2 sm:grid-cols-2 lg:grid-cols-5 gap-3.5">
        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs sm:text-sm text-slate-600 font-semibold">Tổng ngày có việc</span>
            <div class="w-8 h-8 rounded-md bg-slate-100 text-slate-600 flex items-center justify-center">
              <Calendar class="w-4 h-4" />
            </div>
          </div>
          <span class="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-2">{{ taskStats?.totalDaysWithTasks || 0 }}</span>
          <span class="text-xs text-slate-400 mt-1">ngày đã lên lịch</span>
        </div>

        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs sm:text-sm text-slate-600 font-semibold">Tổng số công việc</span>
            <div class="w-8 h-8 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center">
              <ListTodo class="w-4 h-4" />
            </div>
          </div>
          <span class="text-2xl sm:text-3xl font-extrabold text-brand-600 mt-2">{{ taskStats?.totalTasksAllTime || 0 }}</span>
          <span class="text-xs text-slate-400 mt-1">mục tiêu tổng cộng</span>
        </div>

        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs sm:text-sm text-emerald-700 font-semibold">Đã hoàn thành</span>
            <div class="w-8 h-8 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <CheckCircle2 class="w-4 h-4" />
            </div>
          </div>
          <span class="text-2xl sm:text-3xl font-extrabold text-emerald-600 mt-2">{{ taskStats?.totalCompletedAllTime || 0 }}</span>
          <span class="text-xs text-slate-400 mt-1">việc đã làm xong</span>
        </div>

        <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs sm:text-sm text-purple-700 font-semibold">Ngày hoàn hảo (100%)</span>
            <div class="w-8 h-8 rounded-md bg-purple-50 text-purple-600 flex items-center justify-center">
              <Trophy class="w-4 h-4" />
            </div>
          </div>
          <span class="text-2xl sm:text-3xl font-extrabold text-purple-600 mt-2">{{ taskStats?.totalDaysCompletedFull || 0 }}</span>
          <span class="text-xs text-slate-400 mt-1">ngày xong 100% mục tiêu</span>
        </div>

        <div class="col-span-2 sm:col-span-2 lg:col-span-1 bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <span class="text-xs sm:text-sm text-amber-700 font-semibold">Tỷ lệ hoàn thành chung</span>
            <div class="w-8 h-8 rounded-md bg-amber-50 text-amber-600 flex items-center justify-center">
              <Award class="w-4 h-4" />
            </div>
          </div>
          <div class="mt-2">
            <span class="text-2xl sm:text-3xl font-extrabold text-amber-600">{{ taskStats?.overallCompletionRate || 0 }}%</span>
            <div class="w-full bg-slate-100 rounded-full h-2 mt-2 overflow-hidden">
              <div
                class="h-full rounded-full transition-all duration-500 bg-gradient-to-r from-amber-500 to-emerald-500"
                :style="{ width: `${taskStats?.overallCompletionRate || 0}%` }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Daily Task Summaries Table -->
      <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
        <div class="p-4 sm:p-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-50/50">
          <div class="flex items-center gap-2">
            <h3 class="text-sm font-bold text-slate-900 uppercase tracking-wide">
              Bảng theo dõi tiến độ công việc từng ngày
            </h3>
            <span class="text-xs bg-brand-100 text-brand-800 font-bold px-2 py-0.5 rounded-full">
              {{ dailySummaries.length }} ngày
            </span>
          </div>

          <div class="text-xs text-slate-500">
            Ấn nút "Xem chi tiết" để xem và thao tác công việc của ngày đó
          </div>
        </div>

        <!-- Table Loading -->
        <div v-if="isLoadingTasksStats" class="p-12 text-center text-slate-400 text-sm">
          <RefreshCw class="w-6 h-6 animate-spin mx-auto mb-2 text-brand-500" />
          Đang tổng hợp dữ liệu ngày...
        </div>

        <!-- Empty State -->
        <div v-else-if="dailySummaries.length === 0" class="p-12 text-center">
          <div class="w-16 h-16 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-3">
            <Calendar class="w-8 h-8" />
          </div>
          <h4 class="text-base font-bold text-slate-800">Chưa có dữ liệu việc cần làm trong khoảng thời gian này</h4>
          <p class="text-xs sm:text-sm text-slate-500 mt-1 max-w-md mx-auto">
            Hãy bắt đầu lập kế hoạch và thêm danh sách việc cần làm mỗi ngày để theo dõi hiệu suất nhé!
          </p>
          <router-link
            to="/tasks"
            class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 text-xs sm:text-sm font-semibold text-white bg-brand-600 rounded-md hover:bg-brand-700 transition-colors shadow-xs"
          >
            <Plus class="w-4 h-4" />
            <span>Tạo danh sách việc hôm nay</span>
          </router-link>
        </div>

        <!-- Table Data -->
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left border-collapse text-xs sm:text-sm">
            <thead>
              <tr class="bg-slate-50 text-slate-600 font-bold border-b border-slate-200">
                <th class="py-3 px-4 w-12 text-center">STT</th>
                <th class="py-3 px-4">Ngày thực hiện</th>
                <th class="py-3 px-4 text-center">Tổng số việc</th>
                <th class="py-3 px-4 text-center">Đã hoàn thành</th>
                <th class="py-3 px-4 text-center">Chưa xong</th>
                <th class="py-3 px-4 w-48">Tiến độ (%)</th>
                <th class="py-3 px-4 text-center">Đánh giá</th>
                <th class="py-3 px-4 text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="(item, index) in dailySummaries"
                :key="item.taskDate"
                class="hover:bg-slate-50/80 transition-colors"
              >
                <!-- STT -->
                <td class="py-3.5 px-4 text-center font-bold text-slate-400 font-mono">
                  #{{ taskSummariesPage * taskSummariesPageSize + index + 1 }}
                </td>

                <!-- Ngày -->
                <td class="py-3.5 px-4">
                  <div class="font-bold text-slate-900">
                    {{ formatDateDisplay(item.taskDate) }}
                  </div>
                  <div class="text-xs text-brand-600 font-semibold">
                    {{ item.dayOfWeek }}
                  </div>
                </td>

                <!-- Tổng số việc -->
                <td class="py-3.5 px-4 text-center font-bold text-slate-800">
                  <span class="px-2.5 py-1 rounded bg-slate-100 text-slate-700 font-mono">
                    {{ item.totalTasks }}
                  </span>
                </td>

                <!-- Đã hoàn thành -->
                <td class="py-3.5 px-4 text-center font-bold text-emerald-600">
                  <span class="px-2.5 py-1 rounded bg-emerald-50 text-emerald-700 font-mono">
                    {{ item.completedTasks }}
                  </span>
                </td>

                <!-- Chưa xong -->
                <td class="py-3.5 px-4 text-center font-bold text-amber-600">
                  <span class="px-2.5 py-1 rounded bg-amber-50 text-amber-700 font-mono">
                    {{ item.pendingTasks + item.inProgressTasks }}
                  </span>
                </td>

                <!-- Tiến độ % with Progress bar -->
                <td class="py-3.5 px-4">
                  <div class="flex items-center justify-between text-xs font-bold text-slate-700 mb-1">
                    <span>{{ item.completionRate }}%</span>
                    <span class="text-[10px] text-slate-400">{{ item.completedTasks }}/{{ item.totalTasks }}</span>
                  </div>
                  <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                    <div
                      :class="[
                        'h-full rounded-full transition-all duration-300',
                        item.completionRate === 100
                          ? 'bg-emerald-500'
                          : item.completionRate >= 50
                          ? 'bg-brand-500'
                          : 'bg-amber-500'
                      ]"
                      :style="{ width: `${item.completionRate}%` }"
                    ></div>
                  </div>
                </td>

                <!-- Đánh giá trạng thái -->
                <td class="py-3.5 px-4 text-center">
                  <span
                    :class="[
                      'inline-block px-2.5 py-1 rounded-md text-xs font-bold shadow-2xs',
                      item.completionRate === 100
                        ? 'bg-emerald-100 text-emerald-800'
                        : item.completionRate >= 70
                        ? 'bg-blue-100 text-blue-800'
                        : item.completionRate >= 40
                        ? 'bg-amber-100 text-amber-800'
                        : 'bg-rose-100 text-rose-800'
                    ]"
                  >
                    {{ getEvaluationLabel(item.completionRate) }}
                  </span>
                </td>

                <!-- Nút Xem chi tiết -->
                <td class="py-3.5 px-4 text-center">
                  <button
                    type="button"
                    @click="openDayDetailModal(item.taskDate)"
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 border border-brand-200/60 rounded-md transition-colors shadow-2xs"
                  >
                    <Eye class="w-3.5 h-3.5" />
                    <span>Xem chi tiết</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Daily Summaries Table Pagination -->
          <AppPagination
            :current-page="taskSummariesPage"
            :total-pages="taskSummariesTotalPages"
            :page-size="taskSummariesPageSize"
            :total-elements="taskSummariesTotalElements"
            @update:page="loadTaskStats($event)"
          />
        </div>
      </div>
    </div>

    <!-- Modal Chi Tiết Công Việc Ngày -->
    <AppModal
      v-model="showDayDetailModal"
      :title="`Chi tiết công việc ngày: ${selectedModalDateFormatted}`"
      size="2xl"
    >
      <div class="space-y-5 py-2">
        <!-- Day summary stats inside modal -->
        <div class="p-4 sm:p-5 bg-slate-50/90 rounded-md border border-slate-200 flex flex-wrap items-center justify-between gap-4 text-xs sm:text-sm shadow-2xs">
          <div class="flex flex-wrap items-center gap-6 sm:gap-8">
            <div>
              <span class="text-slate-500 font-medium">Tổng số việc:</span>
              <strong class="ml-1.5 text-slate-900 font-extrabold text-sm sm:text-base">{{ modalTasks.length }}</strong>
            </div>
            <div>
              <span class="text-slate-500 font-medium">Đã hoàn thành:</span>
              <strong class="ml-1.5 text-emerald-600 font-extrabold text-sm sm:text-base">{{ modalCompletedCount }}</strong>
            </div>
            <div>
              <span class="text-slate-500 font-medium">Tiến độ:</span>
              <strong class="ml-1.5 text-brand-600 font-extrabold text-sm sm:text-base">{{ modalCompletionRate }}%</strong>
            </div>
            <div>
              <span class="text-slate-500 font-medium">Tổng thời gian:</span>
              <strong class="ml-1.5 text-indigo-600 font-extrabold text-sm sm:text-base">{{ modalTotalEstimatedTimeDisplay }}</strong>
            </div>
          </div>

          <router-link
            :to="`/tasks?date=${selectedModalDate}`"
            class="inline-flex items-center gap-1.5 font-bold text-xs sm:text-sm text-brand-600 hover:text-brand-800 hover:underline bg-white px-3.5 py-1.5 rounded-md border border-brand-200/80 shadow-2xs transition-colors"
          >
            <span>Mở trang quản lý ngày này</span>
            <ExternalLink class="w-4 h-4" />
          </router-link>
        </div>

        <!-- Task Items in Modal -->
        <div v-if="isLoadingModalTasks" class="p-12 text-center text-slate-400 text-sm">
          <RefreshCw class="w-6 h-6 animate-spin mx-auto mb-2 text-brand-500" />
          Đang tải chi tiết các công việc...
        </div>

        <div v-else-if="modalTasks.length === 0" class="p-12 text-center text-slate-400 text-sm">
          Không có việc nào được ghi nhận cho ngày này.
        </div>

        <div v-else class="overflow-x-auto border border-slate-200 rounded-md max-h-[72vh]">
          <table class="w-full min-w-[850px] text-left border-collapse text-xs sm:text-sm">
            <thead>
              <tr class="bg-slate-50 text-slate-700 font-bold border-b border-slate-200 uppercase tracking-wider text-[11px] sm:text-xs whitespace-nowrap">
                <th class="py-3.5 px-3.5 w-14 text-center">STT</th>
                <th class="py-3.5 px-3.5 w-16 text-center">Xong</th>
                <th class="py-3.5 px-4 min-w-[320px]">Nội dung công việc</th>
                <th class="py-3.5 px-4 text-center w-32 whitespace-nowrap">Phân loại</th>
                <th class="py-3.5 px-4 text-center w-32 whitespace-nowrap">Mức độ ưu tiên</th>
                <th class="py-3.5 px-4 text-center w-28 whitespace-nowrap">Thời gian</th>
                <th class="py-3.5 px-4 text-center w-36 whitespace-nowrap">Trạng thái</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="(task, idx) in sortedModalTasks"
                :key="task.id"
                :class="[
                  'hover:bg-slate-50/80 transition-colors',
                  task.isCompleted ? 'bg-slate-50/70' : 'bg-white'
                ]"
              >
                <!-- STT -->
                <td class="py-3 px-3 text-center font-bold text-slate-400 font-mono">
                  #{{ idx + 1 }}
                </td>

                <!-- Checkbox Xong -->
                <td class="py-3 px-3 text-center">
                  <div class="flex justify-center">
                    <button
                      type="button"
                      @click="toggleModalTaskCompletion(task)"
                      :class="[
                        'w-5 h-5 rounded border-2 flex items-center justify-center transition-all',
                        task.isCompleted
                          ? 'bg-emerald-500 border-emerald-500 text-white shadow-xs'
                          : 'border-slate-300 hover:border-brand-500 bg-white text-transparent'
                      ]"
                      title="Tích để hoàn thành / bỏ hoàn thành"
                    >
                      <Check class="w-3.5 h-3.5 stroke-[3]" />
                    </button>
                  </div>
                </td>

                <!-- Tiêu đề & Ghi chú -->
                <td class="py-3 px-4">
                  <div>
                    <div
                      :class="[
                        'font-bold transition-all break-words leading-snug',
                        task.isCompleted ? 'line-through text-slate-400' : 'text-slate-900'
                      ]"
                    >
                      {{ task.title }}
                    </div>
                    <p
                      v-if="task.description"
                      :class="[
                        'text-xs mt-0.5 leading-relaxed',
                        task.isCompleted ? 'text-slate-400 line-through' : 'text-slate-500'
                      ]"
                    >
                      {{ task.description }}
                    </p>
                  </div>
                </td>

                <!-- Phân loại -->
                <td class="py-3 px-3.5 text-center">
                  <span
                    v-if="task.category"
                    :class="[
                      'inline-block px-2 py-0.5 text-[11px] font-bold rounded uppercase tracking-wider',
                      getCategoryBadgeClass(task.category)
                    ]"
                  >
                    {{ getCategoryLabel(task.category) }}
                  </span>
                  <span v-else class="text-xs text-slate-400">—</span>
                </td>

                <!-- Mức độ ưu tiên -->
                <td class="py-3 px-3.5 text-center">
                  <span
                    :class="[
                      'inline-block px-2 py-0.5 text-[11px] font-bold rounded',
                      getPriorityBadgeClass(task.priority)
                    ]"
                  >
                    {{ getPriorityLabel(task.priority) }}
                  </span>
                </td>

                <!-- Thời gian -->
                <td class="py-3 px-3.5 text-center">
                  <span
                    v-if="task.estimatedTime"
                    class="inline-flex items-center gap-1 px-2 py-0.5 text-[11px] font-medium text-slate-600 bg-slate-100 rounded"
                  >
                    <Clock class="w-2.5 h-2.5 text-slate-400" />
                    <span>{{ task.estimatedTime }}</span>
                  </span>
                  <span v-else class="text-xs text-slate-400">—</span>
                </td>

                <!-- Trạng thái -->
                <td class="py-3 px-3.5 text-center">
                  <span
                    :class="[
                      'inline-block text-[11px] font-bold px-2.5 py-1 rounded-md shadow-2xs',
                      task.isCompleted || task.status === 'COMPLETED'
                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-300'
                        : task.status === 'IN_PROGRESS'
                        ? 'bg-sky-50 text-sky-700 border border-sky-300'
                        : task.status === 'CANCELLED'
                        ? 'bg-slate-100 text-slate-600 border border-slate-300'
                        : 'bg-amber-50 text-amber-700 border border-amber-300'
                    ]"
                  >
                    {{ task.isCompleted || task.status === 'COMPLETED' ? 'Đã hoàn thành' : task.status === 'IN_PROGRESS' ? 'Đang thực hiện' : task.status === 'CANCELLED' ? 'Đã hủy' : 'Chưa làm' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex justify-end pt-3 border-t border-slate-100">
          <button
            type="button"
            @click="showDayDetailModal = false"
            class="px-4 py-2 text-xs sm:text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-md transition-colors"
          >
            Đóng
          </button>
        </div>
      </div>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { statisticsService } from '../../services/statistics.service';
import { taskService } from '../../services/task.service';
import { useToastStore } from '../../stores/toast.store';
import type {
  DashboardSummary,
  ChartData,
  StreakHeatmap,
  DailyTaskStats,
  DailyTaskSummary,
  DailyTask,
} from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppModal from '../../components/common/AppModal.vue';
import AppPagination from '../../components/common/AppPagination.vue';
import {
  BarChart3,
  Flame,
  RefreshCw,
  BookOpen,
  CheckSquare,
  Calendar,
  ListTodo,
  CheckCircle2,
  Trophy,
  Award,
  Eye,
  Check,
  Clock,
  ExternalLink,
  Plus,
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

const route = useRoute();
const toast = useToastStore();

// Tab state: 'ENGLISH' | 'TASKS'
const activeTab = ref<'ENGLISH' | 'TASKS'>('ENGLISH');

// Tab 1 state: English Learning Stats
const summary = ref<DashboardSummary | null>(null);
const heatmapData = ref<StreakHeatmap | null>(null);
const chartData = ref<ChartData | null>(null);
const chartRange = ref('LAST_7_DAYS');
const customFromDate = ref('');
const customToDate = ref('');
const isRefreshing = ref(false);

// Tab 2 state: Daily Task Stats
const taskStats = ref<DailyTaskStats | null>(null);
const dailySummaries = ref<DailyTaskSummary[]>([]);
const taskSummariesPage = ref(0);
const taskSummariesPageSize = ref(10);
const taskSummariesTotalPages = ref(1);
const taskSummariesTotalElements = ref(0);
const taskDateFilterRange = ref('LAST_30_DAYS');
const taskCustomFromDate = ref('');
const taskCustomToDate = ref('');
const isLoadingTasksStats = ref(false);

// Modal state
const showDayDetailModal = ref(false);
const selectedModalDate = ref('');
const modalTasks = ref<DailyTask[]>([]);
const isLoadingModalTasks = ref(false);

onMounted(async () => {
  if (route.query.tab === 'tasks') {
    activeTab.value = 'TASKS';
  }

  await Promise.all([
    loadEnglishData(),
    loadTaskStats(),
  ]);
});

watch(
  () => route.query.tab,
  (newTab) => {
    if (newTab === 'tasks') {
      activeTab.value = 'TASKS';
    } else if (newTab === 'english') {
      activeTab.value = 'ENGLISH';
    }
  }
);

async function refreshCurrentTabData() {
  isRefreshing.value = true;
  try {
    if (activeTab.value === 'ENGLISH') {
      await loadEnglishData();
    } else {
      await loadTaskStats();
    }
    toast.success('Đã cập nhật dữ liệu thống kê mới nhất');
  } finally {
    isRefreshing.value = false;
  }
}

// ----------------------------------------------------
// TAB 1 METHODS (English Stats)
// ----------------------------------------------------
async function loadEnglishData() {
  await Promise.all([
    loadSummary(),
    loadHeatmap(),
    loadChart(chartRange.value, customFromDate.value, customToDate.value),
  ]);
}

async function loadSummary() {
  try {
    summary.value = await statisticsService.getDashboardSummary();
  } catch (e) {
    console.error(e);
  }
}

async function loadHeatmap() {
  try {
    heatmapData.value = await statisticsService.getStreakAndHeatmap();
  } catch (e) {
    console.error(e);
  }
}

async function loadChart(range: string, from?: string, to?: string) {
  try {
    chartData.value = await statisticsService.getChartData(range, from, to);
  } catch (e) {
    console.error(e);
  }
}

function setChartRange(range: string) {
  chartRange.value = range;
  if (range !== 'CUSTOM') {
    loadChart(range);
  }
}

function loadCustomChart() {
  if (customFromDate.value && customToDate.value) {
    loadChart('CUSTOM', customFromDate.value, customToDate.value);
  }
}

function getHeatmapColor(level: number) {
  switch (level) {
    case 4:
      return 'bg-emerald-600';
    case 3:
      return 'bg-emerald-500';
    case 2:
      return 'bg-emerald-300';
    case 1:
      return 'bg-emerald-200';
    case 0:
    default:
      return 'bg-slate-200/80';
  }
}

const chartDataConfig = computed(() => {
  if (!chartData.value) return null;
  return {
    labels: chartData.value.labels,
    datasets: [
      {
        label: 'Từ vựng (Vocabulary)',
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        data: chartData.value.vocabularyData,
        tension: 0.3,
        fill: true,
      },
      {
        label: 'Ngữ pháp (Grammar)',
        borderColor: '#a855f7',
        backgroundColor: 'transparent',
        data: chartData.value.grammarData,
        tension: 0.3,
      },
      {
        label: 'Luyện nghe (Listening)',
        borderColor: '#10b981',
        backgroundColor: 'transparent',
        data: chartData.value.listeningData,
        tension: 0.3,
      },
      {
        label: 'Luyện nói (Speaking)',
        borderColor: '#f43f5e',
        backgroundColor: 'transparent',
        data: chartData.value.speakingData,
        tension: 0.3,
      },
      {
        label: 'Ôn tập (Review)',
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
        font: { size: 11 },
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
      ticks: { precision: 0, font: { size: 10 } },
      grid: { color: '#f1f5f9' },
    },
    x: {
      ticks: { font: { size: 10 } },
      grid: { display: false },
    },
  },
};

// ----------------------------------------------------
// TAB 2 METHODS (Daily Tasks Stats)
// ----------------------------------------------------
async function loadTaskStats(page = 0) {
  taskSummariesPage.value = page;
  isLoadingTasksStats.value = true;
  try {
    let fromDate: string | undefined;
    let toDate: string | undefined;

    const today = new Date();
    const toStr = today.toISOString().split('T')[0];

    if (taskDateFilterRange.value === 'LAST_7_DAYS') {
      const past = new Date();
      past.setDate(past.getDate() - 6);
      fromDate = past.toISOString().split('T')[0];
      toDate = toStr;
    } else if (taskDateFilterRange.value === 'LAST_30_DAYS') {
      const past = new Date();
      past.setDate(past.getDate() - 29);
      fromDate = past.toISOString().split('T')[0];
      toDate = toStr;
    } else if (taskDateFilterRange.value === 'THIS_MONTH') {
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, '0');
      fromDate = `${year}-${month}-01`;
      toDate = toStr;
    } else if (taskDateFilterRange.value === 'CUSTOM') {
      fromDate = taskCustomFromDate.value || undefined;
      toDate = taskCustomToDate.value || undefined;
    }

    const [stats, summariesPage] = await Promise.all([
      taskService.getTaskStats(fromDate, toDate),
      taskService.getDailySummaries(fromDate, toDate, taskSummariesPage.value, taskSummariesPageSize.value),
    ]);

    taskStats.value = stats;
    dailySummaries.value = summariesPage?.items || [];
    taskSummariesTotalPages.value = summariesPage?.totalPages || 1;
    taskSummariesTotalElements.value = summariesPage?.totalElements || 0;
  } catch (error) {
    console.error(error);
    toast.error('Không thể tải thống kê công việc');
  } finally {
    isLoadingTasksStats.value = false;
  }
}

function getEvaluationLabel(rate?: number) {
  if (rate === undefined || rate === null) return 'Chưa có việc';
  if (rate === 100) return 'Xuất sắc 100%';
  if (rate >= 70) return 'Đạt mục tiêu';
  if (rate >= 40) return 'Đang tiến triển';
  return 'Cần cố gắng';
}

function setTaskDateRange(range: string) {
  taskDateFilterRange.value = range;
  if (range !== 'CUSTOM') {
    loadTaskStats();
  }
}

function formatDateDisplay(dateStr: string) {
  if (!dateStr) return '';
  const [y, m, d] = dateStr.split('-');
  return `${d}/${m}/${y}`;
}

const selectedModalDateFormatted = computed(() => {
  if (!selectedModalDate.value) return '';
  const dateObj = new Date(selectedModalDate.value + 'T00:00:00');
  const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
  const dayName = days[dateObj.getDay()];
  const [y, m, d] = selectedModalDate.value.split('-');
  return `${dayName}, ${d}/${m}/${y}`;
});

const modalCompletedCount = computed(() => modalTasks.value.filter((t) => t.isCompleted || t.status === 'COMPLETED').length);
const modalCompletionRate = computed(() => {
  if (modalTasks.value.length === 0) return 0;
  return Math.round((modalCompletedCount.value / modalTasks.value.length) * 100);
});

function parseEstimatedMinutes(timeStr?: string): number {
  if (!timeStr || !timeStr.trim()) return 0;
  const str = timeStr.trim().toLowerCase().replace(/,/g, '.');
  const comboMatch = str.match(/(\d+(?:\.\d+)?)\s*(?:h|giờ|tiếng)\s*(\d+)?\s*(?:m|p|phút)?/);
  if (comboMatch) {
    const hours = parseFloat(comboMatch[1]) || 0;
    const mins = comboMatch[2] ? parseFloat(comboMatch[2]) : 0;
    return Math.round(hours * 60 + mins);
  }
  const minsMatch = str.match(/^(\d+(?:\.\d+)?)\s*(?:m|p|phút|min|mins)?$/);
  if (minsMatch) {
    const val = parseFloat(minsMatch[1]);
    if (val <= 12 && !str.includes('p') && !str.includes('m') && !str.includes('phút')) {
      return Math.round(val * 60);
    }
    return Math.round(val);
  }
  const numMatch = str.match(/\d+(?:\.\d+)?/);
  if (numMatch) {
    const val = parseFloat(numMatch[0]);
    if (str.includes('h') || str.includes('giờ') || str.includes('tiếng') || val <= 12) {
      return Math.round(val * 60);
    }
    return Math.round(val);
  }
  return 0;
}

function formatMinutesToReadable(minutes: number): string {
  if (!minutes || minutes <= 0) return '0 phút';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  if (h > 0 && m > 0) return `${h}h ${m}p`;
  if (h > 0 && m === 0) return `${h} giờ`;
  return `${m} phút`;
}

const modalTotalEstimatedTimeDisplay = computed(() => {
  const total = modalTasks.value.reduce((acc, t) => acc + parseEstimatedMinutes(t.estimatedTime), 0);
  return formatMinutesToReadable(total);
});

const sortedModalTasks = computed(() => {
  return [...modalTasks.value].sort((a, b) => {
    const aDone = a.isCompleted || a.status === 'COMPLETED' ? 1 : 0;
    const bDone = b.isCompleted || b.status === 'COMPLETED' ? 1 : 0;
    if (aDone !== bDone) {
      return aDone - bDone;
    }
    return (a.displayOrder || 0) - (b.displayOrder || 0);
  });
});

async function openDayDetailModal(date: string) {
  selectedModalDate.value = date;
  showDayDetailModal.value = true;
  isLoadingModalTasks.value = true;
  try {
    modalTasks.value = await taskService.getTasksByDate(date);
  } catch (error) {
    toast.error('Không thể tải danh sách việc của ngày');
  } finally {
    isLoadingModalTasks.value = false;
  }
}

async function toggleModalTaskCompletion(task: DailyTask) {
  const originalState = task.isCompleted;
  task.isCompleted = !originalState;
  task.status = task.isCompleted ? 'COMPLETED' : 'TODO';

  try {
    const updated = await taskService.toggleTask(task.id);
    task.isCompleted = updated.isCompleted;
    task.status = updated.status;
    // Refresh background stats silently
    loadTaskStats();
  } catch (error) {
    task.isCompleted = originalState;
    task.status = originalState ? 'COMPLETED' : 'TODO';
    toast.error('Lỗi khi cập nhật trạng thái');
  }
}

function getCategoryLabel(cat?: string) {
  switch (cat) {
    case 'ENGLISH':
      return 'Tiếng Anh';
    case 'WORK':
      return 'Công việc';
    case 'STUDY':
      return 'Học tập';
    case 'PERSONAL':
      return 'Cá nhân';
    case 'HEALTH':
      return 'Sức khỏe';
    case 'PROJECT':
      return 'Dự án';
    default:
      return 'Khác';
  }
}

function getCategoryBadgeClass(cat?: string) {
  switch (cat) {
    case 'ENGLISH':
      return 'bg-indigo-50 text-indigo-700 border border-indigo-200';
    case 'WORK':
      return 'bg-blue-50 text-blue-700 border border-blue-200';
    case 'STUDY':
      return 'bg-purple-50 text-purple-700 border border-purple-200';
    case 'PERSONAL':
      return 'bg-rose-50 text-rose-700 border border-rose-200';
    case 'HEALTH':
      return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    case 'PROJECT':
      return 'bg-amber-50 text-amber-700 border border-amber-200';
    default:
      return 'bg-slate-100 text-slate-700 border border-slate-200';
  }
}

function getPriorityLabel(priority?: string) {
  switch (priority) {
    case 'URGENT':
      return 'Khẩn cấp';
    case 'HIGH':
      return 'Ưu tiên Cao';
    case 'MEDIUM':
      return 'Trung bình';
    case 'LOW':
      return 'Thấp';
    default:
      return 'Trung bình';
  }
}

function getPriorityBadgeClass(priority?: string) {
  switch (priority) {
    case 'URGENT':
      return 'bg-rose-100 text-rose-800 font-bold';
    case 'HIGH':
      return 'bg-orange-100 text-orange-800';
    case 'MEDIUM':
      return 'bg-yellow-50 text-yellow-800 border border-yellow-200';
    case 'LOW':
      return 'bg-slate-100 text-slate-600';
    default:
      return 'bg-slate-100 text-slate-600';
  }
}
</script>
