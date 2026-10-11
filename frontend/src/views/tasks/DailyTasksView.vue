<template>
  <div class="space-y-6 w-full">
    <!-- Header Banner -->
    <div class="bg-white p-6 sm:p-8 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-5">
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 shrink-0 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center border border-brand-100/60 shadow-xs">
          <CheckSquare class="w-7 h-7" />
        </div>
        <div>
          <div class="flex items-center gap-2.5">
            <h2 class="text-xl sm:text-2xl lg:text-3xl font-bold text-slate-900 tracking-tight">
              Kế hoạch & Việc cần làm hàng ngày
            </h2>
          </div>
          <p class="text-sm sm:text-base text-slate-500 mt-1">
            Quản lý mục tiêu, danh sách việc cần làm trong ngày và theo dõi tiến độ hoàn thành
          </p>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex flex-wrap items-center gap-2.5 shrink-0">
        <router-link
          to="/statistics?tab=tasks"
          class="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs sm:text-sm font-medium text-slate-700 bg-white border border-slate-200 rounded-md hover:bg-slate-50 transition-colors shadow-xs"
        >
          <BarChart3 class="w-4 h-4 text-brand-600" />
          <span>Thống kê theo ngày</span>
        </router-link>

        <button
          type="button"
          @click="openQuickAddModal"
          class="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs sm:text-sm font-bold text-amber-700 bg-amber-50 border border-amber-200 rounded-md hover:bg-amber-100/80 transition-colors shadow-xs cursor-pointer"
        >
          <Zap class="w-4 h-4 text-amber-600 fill-amber-500" />
          <span>Thêm nhanh</span>
        </button>

        <AppButton variant="primary" size="md" :icon="Plus" @click="openCreateModal">
          <span>Thêm việc mới</span>
        </AppButton>
      </div>
    </div>

    <!-- Date Navigation Bar -->
    <div class="bg-white p-4 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div class="flex flex-wrap items-center gap-2">
        <div class="flex items-center bg-slate-100 rounded-md p-1 border border-slate-200/80">
          <button
            type="button"
            @click="navigateDate(-1)"
            class="p-1.5 rounded text-slate-600 hover:text-slate-900 hover:bg-white transition-colors"
            title="Ngày trước"
          >
            <ChevronLeft class="w-4 h-4" />
          </button>
          
          <button
            type="button"
            @click="setToday"
            :class="[
              'px-3 py-1 text-xs sm:text-sm font-semibold rounded transition-colors',
              isTodaySelected ? 'bg-brand-600 text-white shadow-xs' : 'text-slate-700 hover:bg-white'
            ]"
          >
            Hôm nay
          </button>

          <button
            type="button"
            @click="navigateDate(1)"
            class="p-1.5 rounded text-slate-600 hover:text-slate-900 hover:bg-white transition-colors"
            title="Ngày sau"
          >
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>

        <!-- Quick Jump Buttons -->
        <div class="hidden sm:flex items-center gap-1.5 text-xs">
          <button
            type="button"
            @click="jumpToDate(-1)"
            class="px-2.5 py-1.5 border border-slate-200 rounded text-slate-600 hover:bg-slate-50 transition-colors font-medium cursor-pointer"
          >
            Hôm qua
          </button>
          <button
            type="button"
            @click="jumpToDate(1)"
            class="px-2.5 py-1.5 border border-slate-200 rounded text-slate-600 hover:bg-slate-50 transition-colors font-medium cursor-pointer"
          >
            Ngày mai
          </button>
          <button
            type="button"
            @click="copyFromYesterday"
            :disabled="isCopying"
            class="inline-flex items-center gap-1 px-2.5 py-1.5 border border-slate-200 rounded text-slate-700 bg-white hover:bg-slate-50 hover:border-slate-300 transition-colors font-semibold shadow-2xs cursor-pointer disabled:opacity-50"
            title="Sao chép các công việc của ngày hôm qua sang ngày đang chọn"
          >
            <Copy class="w-3.5 h-3.5 text-brand-600" />
            <span>{{ isCopying ? 'Đang chép...' : 'Sao chép từ hôm qua' }}</span>
          </button>
        </div>

        <!-- Date Input Picker -->
        <div class="flex items-center gap-2 pl-1">
          <div class="relative">
            <input
              type="date"
              v-model="selectedDate"
              @change="loadTasksForSelectedDate"
              class="w-40 sm:w-44 text-xs sm:text-sm px-3 py-1.5 rounded-md border border-slate-200 bg-white text-slate-800 font-semibold focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors"
            />
          </div>
          <span class="text-xs sm:text-sm font-bold text-brand-700 bg-brand-50 px-2.5 py-1 rounded-md border border-brand-100">
            {{ formattedSelectedDateLabel }}
          </span>
        </div>
      </div>

      <!-- Quick Summary pills with Total Estimated Hours -->
      <div class="flex flex-wrap items-center gap-2.5">
        <!-- Total Estimated Time Pill -->
        <div class="flex items-center gap-1.5 px-3 py-1 rounded-md bg-indigo-50 border border-indigo-200/80 text-xs sm:text-sm font-semibold text-indigo-800 shadow-2xs">
          <Clock class="w-4 h-4 text-indigo-600" />
          <span>Tổng giờ cần: <strong class="font-bold text-indigo-950">{{ totalEstimatedTimeDisplay }}</strong></span>
          <span v-if="remainingEstimatedMinutes > 0" class="text-[11px] text-indigo-600 font-normal">
            (Còn ~{{ remainingEstimatedTimeDisplay }})
          </span>
        </div>

        <!-- Task progress pill -->
        <div class="flex items-center gap-2 text-xs sm:text-sm bg-slate-50 px-3 py-1 rounded-md border border-slate-200">
          <span class="text-slate-500">Tiến độ:</span>
          <span class="font-bold text-slate-900">{{ completedCount }}/{{ tasks.length }} việc</span>
          <span
            :class="[
              'px-2 py-0.5 rounded-full text-xs font-bold',
              completionPercentage === 100
                ? 'bg-emerald-100 text-emerald-800'
                : completionPercentage >= 50
                ? 'bg-blue-100 text-blue-800'
                : 'bg-amber-100 text-amber-800'
            ]"
          >
            {{ completionPercentage }}%
          </span>
        </div>
      </div>
    </div>

    <!-- Daily KPI Metric Cards (6 Columns) -->
    <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3.5">
      <!-- 1. Tổng số việc -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs sm:text-sm text-slate-600 font-semibold">Tổng số việc</span>
          <div class="w-8 h-8 rounded-md bg-slate-100 text-slate-600 flex items-center justify-center">
            <ListTodo class="w-4 h-4" />
          </div>
        </div>
        <span class="text-2xl sm:text-3xl font-extrabold text-slate-900 mt-2">{{ tasks.length }}</span>
        <span class="text-xs text-slate-400 mt-1">mục tiêu trong ngày</span>
      </div>

      <!-- 2. Đã hoàn thành -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs sm:text-sm text-emerald-700 font-semibold">Đã hoàn thành</span>
          <div class="w-8 h-8 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center">
            <CheckCircle2 class="w-4 h-4" />
          </div>
        </div>
        <span class="text-2xl sm:text-3xl font-extrabold text-emerald-600 mt-2">{{ completedCount }}</span>
        <span class="text-xs text-slate-400 mt-1">mục tiêu xong</span>
      </div>

      <!-- 3. Đang thực hiện -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs sm:text-sm text-sky-700 font-semibold">Đang thực hiện</span>
          <div class="w-8 h-8 rounded-md bg-sky-50 text-sky-600 flex items-center justify-center">
            <Clock class="w-4 h-4" />
          </div>
        </div>
        <span class="text-2xl sm:text-3xl font-extrabold text-sky-600 mt-2">{{ inProgressCount }}</span>
        <span class="text-xs text-slate-400 mt-1">đang tiến hành</span>
      </div>

      <!-- 4. Chưa làm -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs sm:text-sm text-amber-700 font-semibold">Chưa làm</span>
          <div class="w-8 h-8 rounded-md bg-amber-50 text-amber-600 flex items-center justify-center">
            <AlertCircle class="w-4 h-4" />
          </div>
        </div>
        <span class="text-2xl sm:text-3xl font-extrabold text-amber-600 mt-2">{{ pendingCount }}</span>
        <span class="text-xs text-slate-400 mt-1">cần giải quyết</span>
      </div>

      <!-- 5. Tổng thời gian cần làm -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs sm:text-sm text-indigo-700 font-semibold">Tổng giờ cần làm</span>
          <div class="w-8 h-8 rounded-md bg-indigo-50 text-indigo-600 flex items-center justify-center">
            <Hourglass class="w-4 h-4" />
          </div>
        </div>
        <span class="text-2xl sm:text-3xl font-extrabold text-indigo-600 mt-2">{{ totalEstimatedTimeDisplay }}</span>
        <span class="text-xs text-slate-400 mt-1">Còn lại: {{ remainingEstimatedTimeDisplay }}</span>
      </div>

      <!-- 6. Tỷ lệ hoàn thành -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs sm:text-sm text-brand-700 font-semibold">Tỷ lệ xong</span>
          <div class="w-8 h-8 rounded-md bg-brand-50 text-brand-600 flex items-center justify-center">
            <Award class="w-4 h-4" />
          </div>
        </div>
        <div class="mt-2">
          <div class="flex items-baseline justify-between">
            <span class="text-2xl sm:text-3xl font-extrabold text-brand-600">{{ completionPercentage }}%</span>
            <span class="text-[11px] font-semibold text-slate-500">{{ dayEvaluationText }}</span>
          </div>
          <!-- Mini Progress Bar -->
          <div class="w-full bg-slate-100 rounded-full h-2 mt-2 overflow-hidden">
            <div
              class="h-full rounded-full transition-all duration-500 bg-gradient-to-r from-brand-500 to-emerald-500"
              :style="{ width: `${completionPercentage}%` }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters & Category Tabs -->
    <div class="bg-white p-4 sm:p-5 rounded-md border border-slate-200 shadow-xs space-y-4">
      <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <!-- Category Filter Pills -->
        <div class="flex flex-wrap items-center gap-1.5">
          <button
            v-for="cat in categoryOptions"
            :key="cat.value"
            type="button"
            @click="selectedCategory = cat.value"
            :class="[
              'px-3 py-1.5 text-xs font-semibold rounded-md transition-colors border',
              selectedCategory === cat.value
                ? 'bg-brand-600 text-white border-brand-600 shadow-xs'
                : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
            ]"
          >
            {{ cat.label }}
          </button>
        </div>

        <!-- Priority & Status Filters + Search -->
        <div class="flex flex-wrap items-center gap-2.5">
          <select
            v-model="selectedPriority"
            class="text-xs px-3 py-1.5 rounded-md border border-slate-200 bg-white text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20"
          >
            <option value="">Tất cả mức ưu tiên</option>
            <option value="URGENT">Khẩn cấp (Urgent)</option>
            <option value="HIGH">Ưu tiên Cao (High)</option>
            <option value="MEDIUM">Trung bình (Medium)</option>
            <option value="LOW">Thấp (Low)</option>
          </select>

          <select
            v-model="selectedCompletionFilter"
            class="text-xs px-3 py-1.5 rounded-md border border-slate-200 bg-white text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20"
          >
            <option value="ALL">Tất cả trạng thái</option>
            <option value="PENDING">Chưa hoàn thành</option>
            <option value="COMPLETED">Đã hoàn thành</option>
          </select>

          <div class="relative min-w-[180px]">
            <input
              type="text"
              v-model="searchQuery"
              placeholder="Tìm việc..."
              class="w-full text-xs pl-8 pr-3 py-1.5 rounded-md border border-slate-200 bg-white text-slate-700 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
            />
            <Search class="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-2.5" />
          </div>
        </div>
      </div>
    </div>

    <!-- Task List Table / Cards -->
    <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
      <div class="p-4 sm:p-5 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-slate-50/50">
        <div class="flex flex-wrap items-center gap-2.5">
          <h3 class="text-sm font-bold text-slate-900 uppercase tracking-wide">
            Danh sách việc ngày {{ formattedSelectedDateDisplay }}
          </h3>
          <span class="text-xs bg-brand-100 text-brand-800 font-bold px-2 py-0.5 rounded-full">
            {{ filteredTasks.length }} việc
          </span>
          <span v-if="totalEstimatedMinutes > 0" class="inline-flex items-center gap-1.5 text-xs bg-indigo-50 text-indigo-800 font-semibold px-2.5 py-1 rounded-md border border-indigo-200/80 shadow-2xs">
            <Clock class="w-3.5 h-3.5 text-indigo-600 shrink-0" />
            <span>Tổng thời gian: <strong class="font-bold text-indigo-950">{{ totalEstimatedTimeDisplay }}</strong></span>
            <span v-if="remainingEstimatedMinutes > 0" class="text-[11px] text-indigo-600 font-normal">
              (Còn lại: {{ remainingEstimatedTimeDisplay }})
            </span>
          </span>
        </div>

        <div class="text-xs text-slate-500">
          <span v-if="isSelectedDateFuture" class="inline-flex items-center gap-1.5 text-amber-700 bg-amber-50 px-2.5 py-1 rounded-md border border-amber-200 font-semibold">
            <AlertCircle class="w-3.5 h-3.5 text-amber-600 shrink-0" />
            Ngày trong tương lai — Chỉ lập kế hoạch, không thể tích hoàn thành
          </span>
          <span v-else>
            Tích chọn checkbox để đánh dấu hoàn thành
          </span>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="p-12 text-center text-slate-400 text-sm">
        <RefreshCw class="w-6 h-6 animate-spin mx-auto mb-2 text-brand-500" />
        Đang tải danh sách công việc...
      </div>

      <!-- Empty State -->
      <div v-else-if="filteredTasks.length === 0" class="p-12 text-center">
        <div class="w-16 h-16 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-3">
          <CheckSquare class="w-8 h-8" />
        </div>
        <h4 class="text-base font-bold text-slate-800">Không có việc cần làm nào</h4>
        <p class="text-xs sm:text-sm text-slate-500 mt-1 max-w-md mx-auto">
          {{ tasks.length === 0 ? 'Bạn chưa lên danh sách việc cần làm cho ngày này. Hãy tạo việc mới ngay!' : 'Không có công việc nào khớp với bộ lọc hiện tại.' }}
        </p>
        <button
          type="button"
          @click="openCreateModal"
          class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 text-xs sm:text-sm font-semibold text-white bg-brand-600 rounded-md hover:bg-brand-700 transition-colors shadow-xs"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm việc cho ngày {{ formattedSelectedDateDisplay }}</span>
        </button>
      </div>

      <!-- Tasks Table with Explicit Headers -->
      <div v-else class="overflow-x-auto">
        <table class="w-full min-w-[850px] text-left border-collapse text-xs sm:text-sm">
          <thead>
            <tr class="bg-slate-50 text-slate-700 font-bold border-b border-slate-200 text-xs sm:text-sm whitespace-nowrap">
              <th class="py-3.5 px-3 w-12 text-center">STT</th>
              <th class="py-3.5 px-3 w-16 text-center">Xong</th>
              <th class="py-3.5 px-4 min-w-[280px]">Tên việc & Chi tiết</th>
              <th class="py-3.5 px-4 text-center whitespace-nowrap">Phân loại</th>
              <th class="py-3.5 px-4 text-center whitespace-nowrap">Độ ưu tiên</th>
              <th class="py-3.5 px-4 text-center whitespace-nowrap">Thời gian</th>
              <th class="py-3.5 px-4 text-center whitespace-nowrap">Trạng thái</th>
              <th class="py-3.5 px-4 text-center whitespace-nowrap">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="(task, index) in paginatedTasks"
              :key="task.id"
              :class="[
                'hover:bg-slate-50/80 transition-colors group align-middle',
                task.isCompleted
                  ? 'bg-slate-50/70'
                  : task.status === 'IN_PROGRESS'
                  ? 'bg-sky-50/40 hover:bg-sky-50/70'
                  : 'bg-white'
              ]"
            >
              <!-- 1. STT + Reorder Arrows -->
              <td class="py-3.5 px-3 text-center align-middle whitespace-nowrap">
                <div class="flex items-center justify-center gap-1">
                  <span class="text-xs font-bold text-slate-400 font-mono">
                    #{{ currentPage * pageSize + index + 1 }}
                  </span>
                  <div class="flex flex-col gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      type="button"
                      @click="moveTask(index, -1)"
                      :disabled="index === 0"
                      class="p-0.5 text-slate-400 hover:text-slate-700 disabled:opacity-20"
                      title="Di chuyển lên"
                    >
                      <ChevronUp class="w-3 h-3" />
                    </button>
                    <button
                      type="button"
                      @click="moveTask(index, 1)"
                      :disabled="index === filteredTasks.length - 1"
                      class="p-0.5 text-slate-400 hover:text-slate-700 disabled:opacity-20"
                      title="Di chuyển xuống"
                    >
                      <ChevronDown class="w-3 h-3" />
                    </button>
                  </div>
                </div>
              </td>

              <!-- 2. Checkbox Xong -->
              <td class="py-3.5 px-3 text-center align-middle whitespace-nowrap">
                <div class="flex justify-center">
                  <button
                    type="button"
                    @click="toggleTaskCompletion(task)"
                    :disabled="isFutureDate(task.taskDate)"
                    :class="[
                      'w-6 h-6 rounded-md border-2 flex items-center justify-center transition-all duration-200',
                      isFutureDate(task.taskDate)
                        ? 'bg-slate-100 border-slate-200 text-slate-300 cursor-not-allowed opacity-60'
                        : task.isCompleted
                          ? 'bg-emerald-500 border-emerald-500 text-white shadow-xs scale-105 cursor-pointer'
                          : 'border-slate-300 hover:border-brand-500 bg-white text-transparent hover:text-slate-300 cursor-pointer'
                    ]"
                    :title="isFutureDate(task.taskDate) ? 'Không thể đánh dấu hoàn thành công việc ở ngày trong tương lai' : 'Tích để hoàn thành / bỏ hoàn thành'"
                  >
                    <Check class="w-4 h-4 stroke-[3]" />
                  </button>
                </div>
              </td>

              <!-- 3. Nội dung công việc & Ghi chú -->
              <td class="py-3.5 px-4 align-middle">
                <div>
                  <div
                    :class="[
                      'text-sm font-bold transition-all break-words leading-snug',
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

              <!-- 4. Phân loại -->
              <td class="py-3.5 px-4 text-center align-middle whitespace-nowrap">
                <span
                  v-if="task.category"
                  :class="[
                    'inline-block px-2.5 py-1 text-xs font-bold rounded-md uppercase tracking-wider shadow-2xs',
                    getCategoryBadgeClass(task.category)
                  ]"
                >
                  {{ getCategoryLabel(task.category) }}
                </span>
                <span v-else class="text-xs text-slate-400">—</span>
              </td>

              <!-- 5. Mức độ ưu tiên -->
              <td class="py-3.5 px-4 text-center align-middle whitespace-nowrap">
                <span
                  :class="[
                    'inline-block px-2.5 py-1 text-xs font-bold rounded-md shadow-2xs',
                    getPriorityBadgeClass(task.priority)
                  ]"
                >
                  {{ getPriorityLabel(task.priority) }}
                </span>
              </td>

              <!-- 6. Thời gian ước tính -->
              <td class="py-3.5 px-4 text-center align-middle whitespace-nowrap">
                <span
                  v-if="task.estimatedTime"
                  class="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-slate-700 bg-slate-100 rounded-md border border-slate-200/80"
                >
                  <Clock class="w-3 h-3 text-slate-400" />
                  <span>{{ task.estimatedTime }}</span>
                </span>
                <span v-else class="text-xs text-slate-400">—</span>
              </td>

              <!-- 7. Trạng thái (Dropdown chọn trực tiếp tinh tế, chuẩn giao diện) -->
              <td class="py-3.5 px-4 text-center align-middle whitespace-nowrap">
                <div class="inline-flex items-center justify-center">
                  <div class="relative inline-block">
                    <select
                      :value="task.isCompleted ? 'COMPLETED' : (task.status || 'TODO')"
                      @change="changeTaskStatus(task, ($event.target as HTMLSelectElement).value)"
                      :class="[
                        'text-xs font-semibold py-1 pl-2.5 pr-6 rounded-md border appearance-none cursor-pointer focus:outline-none focus:ring-2 focus:ring-brand-500/20 transition-all shadow-2xs',
                        getStatusSelectClass(task)
                      ]"
                      title="Bấm để thay đổi trạng thái công việc"
                    >
                      <option value="TODO">Chưa làm</option>
                      <option value="IN_PROGRESS">Đang thực hiện</option>
                      <option value="COMPLETED" :disabled="isFutureDate(task.taskDate)">
                        Đã hoàn thành {{ isFutureDate(task.taskDate) ? '(Khóa tương lai)' : '' }}
                      </option>
                      <option value="CANCELLED">Đã hủy</option>
                    </select>
                    <ChevronDown class="w-3.5 h-3.5 text-current opacity-60 absolute right-1.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                  </div>
                </div>
              </td>

              <!-- 8. Thao tác -->
              <td class="py-3.5 px-4 text-center align-middle whitespace-nowrap">
                <div class="flex items-center justify-center gap-1.5">
                  <!-- Quick Start / Pause In Progress Button -->
                  <button
                    v-if="!task.isCompleted && task.status !== 'COMPLETED'"
                    type="button"
                    @click="toggleTaskInProgress(task)"
                    :class="[
                      'p-1.5 rounded-md transition-colors',
                      task.status === 'IN_PROGRESS'
                        ? 'text-sky-600 bg-sky-100/80 hover:bg-sky-200'
                        : 'text-slate-400 hover:text-sky-600 hover:bg-sky-50'
                    ]"
                    :title="task.status === 'IN_PROGRESS' ? 'Tạm dừng (đưa về Chưa làm)' : 'Bắt đầu làm (chuyển sang Đang thực hiện)'"
                  >
                    <Pause v-if="task.status === 'IN_PROGRESS'" class="w-4 h-4 text-sky-600 fill-sky-600" />
                    <Play v-else class="w-4 h-4" />
                  </button>

                  <!-- Edit Button -->
                  <button
                    type="button"
                    @click="openEditModal(task)"
                    class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded-md transition-colors"
                    title="Chỉnh sửa công việc"
                  >
                    <Edit2 class="w-4 h-4" />
                  </button>

                  <!-- Delete Button -->
                  <button
                    type="button"
                    @click="confirmDeleteTask(task)"
                    class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
                    title="Xóa công việc"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Table Pagination -->
        <AppPagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :page-size="pageSize"
          :total-elements="filteredTasks.length"
          @update:page="currentPage = $event"
        />
      </div>
    </div>

    <!-- Create / Edit Task Modal -->
    <AppModal
      v-model="showTaskModal"
      :title="isEditing ? 'Chỉnh sửa việc cần làm' : 'Thêm việc cần làm mới'"
      size="2xl"
    >
      <form @submit.prevent="saveTask" class="space-y-5 py-3">
        <!-- Box Gợi ý việc thường làm nằm ngang riêng biệt (Horizontal Quick Suggestions Bar) -->
        <div
          v-if="!isEditing && quickPillSuggestions.length > 0"
          class="bg-gradient-to-r from-amber-50/70 via-slate-50 to-indigo-50/50 p-4 rounded-md border border-amber-200/80 shadow-2xs space-y-2.5"
        >
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-2 min-w-0">
              <div class="w-6 h-6 rounded-md bg-amber-100 text-amber-700 flex items-center justify-center shrink-0">
                <Zap class="w-3.5 h-3.5 text-amber-600 fill-amber-600" />
              </div>
              <div class="flex items-baseline gap-2 min-w-0">
                <span class="text-xs sm:text-sm font-bold text-slate-800 whitespace-nowrap">
                  Gợi ý việc thường làm
                </span>
                <span class="text-xs text-slate-500 font-medium truncate hidden sm:inline">
                  (Bấm 1-click để tự động điền tiêu đề, loại việc, thời gian & mức ưu tiên)
                </span>
              </div>
            </div>

            <button
              type="button"
              @click="openQuickAddModal"
              class="inline-flex items-center gap-1.5 text-xs font-bold text-brand-600 hover:text-brand-700 bg-white hover:bg-brand-50 px-2.5 py-1 rounded-md border border-brand-200/80 shadow-2xs transition-colors shrink-0 cursor-pointer"
              title="Mở popup chọn nhiều việc cùng lúc"
            >
              <Layers class="w-3.5 h-3.5 text-brand-600" />
              <span>Thêm hàng loạt</span>
              <ChevronRight class="w-3.5 h-3.5 text-slate-400" />
            </button>
          </div>

          <!-- Dải thẻ gợi ý nằm ngang, cuộn ngang mượt -->
          <div class="flex items-center gap-2 overflow-x-auto pb-1 pt-0.5 no-scrollbar">
            <button
              v-for="(p, idx) in quickPillSuggestions"
              :key="idx"
              type="button"
              @click="applyQuickPill(p)"
              :class="[
                'group inline-flex items-center gap-2 px-3 py-1.5 text-xs font-semibold rounded-md border shadow-2xs transition-all whitespace-nowrap cursor-pointer shrink-0',
                form.title === p.title
                  ? 'bg-brand-600 text-white border-brand-600 shadow-xs'
                  : 'bg-white hover:bg-brand-50 hover:text-brand-700 text-slate-700 border-slate-200 hover:border-brand-300'
              ]"
              :title="`Bấm để điền: ${p.title} (${p.estimatedTime || 'N/A'})`"
            >
              <Zap
                :class="[
                  'w-3.5 h-3.5 shrink-0 transition-colors',
                  form.title === p.title
                    ? 'text-amber-300 fill-amber-300'
                    : 'text-amber-500 fill-amber-500 group-hover:text-brand-600 group-hover:fill-brand-600'
                ]"
              />
              <span>{{ p.title }}</span>
              <span
                v-if="p.estimatedTime"
                :class="[
                  'inline-flex items-center gap-1 text-[10px] font-medium px-1.5 py-0.5 rounded transition-colors',
                  form.title === p.title
                    ? 'bg-brand-700 text-white'
                    : 'bg-slate-100 group-hover:bg-brand-100 text-slate-500 group-hover:text-brand-700'
                ]"
              >
                <Clock class="w-2.5 h-2.5 opacity-70" />
                <span>{{ p.estimatedTime }}</span>
              </span>
            </button>
          </div>
        </div>

        <!-- Section 1: Thông tin cơ bản -->
        <div class="bg-slate-50/70 p-5 sm:p-6 rounded-md border border-slate-200/80 space-y-5">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-5 sm:gap-6">
            <div class="md:col-span-1">
              <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
                Ngày thực hiện <span class="text-rose-500">*</span>
              </label>
              <input
                type="date"
                v-model="form.taskDate"
                required
                class="w-full text-sm sm:text-base px-3.5 py-2.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
              />
            </div>

            <div class="md:col-span-2">
              <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
                Tiêu đề công việc <span class="text-rose-500">*</span>
              </label>
              <input
                type="text"
                v-model="form.title"
                required
                placeholder="Nhập tên việc cần làm..."
                class="w-full text-sm sm:text-base px-3.5 py-2.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
              />
            </div>
          </div>

          <!-- Phân loại & Mức độ ưu tiên & Trạng thái -->
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-5 sm:gap-6 pt-1">
            <div>
              <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
                Phân loại công việc
              </label>
              <select
                v-model="form.category"
                class="w-full text-sm sm:text-base px-3.5 py-2.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
              >
                <option value="ENGLISH">Học Tiếng Anh</option>
                <option value="WORK">Công việc cơ quan</option>
                <option value="STUDY">Học tập & Đọc sách</option>
                <option value="PERSONAL">Việc cá nhân & Gia đình</option>
                <option value="HEALTH">Sức khỏe & Thể thao</option>
                <option value="PROJECT">Dự án riêng</option>
                <option value="OTHER">Khác</option>
              </select>
            </div>

            <div>
              <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
                Mức độ ưu tiên
              </label>
              <select
                v-model="form.priority"
                class="w-full text-sm sm:text-base px-3.5 py-2.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
              >
                <option value="URGENT">Khẩn cấp (Urgent)</option>
                <option value="HIGH">Ưu tiên Cao (High)</option>
                <option value="MEDIUM">Trung bình (Medium)</option>
                <option value="LOW">Thấp (Low)</option>
              </select>
            </div>

            <div>
              <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
                Trạng thái ban đầu
              </label>
              <select
                v-model="form.status"
                class="w-full text-sm sm:text-base px-3.5 py-2.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
              >
                <option value="TODO">Chưa làm (Todo)</option>
                <option value="IN_PROGRESS">Đang làm (In Progress)</option>
                <option value="COMPLETED" :disabled="isFutureDate(form.taskDate)">
                  Đã xong (Completed) {{ isFutureDate(form.taskDate) ? '— (Không khả dụng cho ngày tương lai)' : '' }}
                </option>
                <option value="CANCELLED">Đã hủy (Cancelled)</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Section 2: Thời gian & Chi tiết -->
        <div class="bg-slate-50/70 p-5 sm:p-6 rounded-md border border-slate-200/80 space-y-5">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-5 sm:gap-6">
            <div>
              <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
                Thời gian ước tính / Khung giờ
              </label>
              <input
                type="text"
                v-model="form.estimatedTime"
                placeholder="VD: 30 phút, 1h, 2 giờ..."
                class="w-full text-sm sm:text-base px-3.5 py-2.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
              />
              <!-- Quick Select Presets -->
              <div class="flex flex-wrap items-center gap-1.5 mt-2">
                <span class="text-[11px] text-slate-400 font-medium">Chọn nhanh:</span>
                <button
                  v-for="preset in ['15 phút', '30 phút', '45 phút', '1 giờ', '1.5 giờ', '2 giờ']"
                  :key="preset"
                  type="button"
                  @click="form.estimatedTime = preset"
                  :class="[
                    'px-2 py-0.5 text-xs font-semibold rounded transition-colors border',
                    form.estimatedTime === preset
                      ? 'bg-indigo-600 text-white border-indigo-600'
                      : 'bg-slate-100 hover:bg-slate-200 text-slate-700 border-slate-200'
                  ]"
                >
                  {{ preset }}
                </button>
              </div>
            </div>

            <div>
              <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
                Thứ tự hiển thị (STT)
              </label>
              <input
                type="number"
                v-model.number="form.displayOrder"
                min="0"
                placeholder="0"
                class="w-full text-sm sm:text-base px-3.5 py-2.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
              />
            </div>
          </div>

          <!-- Ghi chú chi tiết -->
          <div class="pt-1">
            <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
              Ghi chú / Chi tiết công việc
            </label>
            <textarea
              v-model="form.description"
              rows="4"
              placeholder="Ghi chú thêm (nếu có)..."
              class="w-full text-sm sm:text-base p-3.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium leading-relaxed focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
            ></textarea>
          </div>

          <!-- Checkbox đánh dấu hoàn thành nhanh -->
          <div
            v-if="!isFutureDate(form.taskDate)"
            class="flex items-center gap-3 p-3.5 bg-white rounded-md border border-slate-200"
          >
            <input
              type="checkbox"
              id="isCompletedCheck"
              v-model="form.isCompleted"
              class="w-5 h-5 text-brand-600 rounded border-slate-300 focus:ring-brand-500 cursor-pointer"
            />
            <label for="isCompletedCheck" class="text-sm font-bold text-slate-800 cursor-pointer select-none">
              Đánh dấu việc này đã hoàn thành ngay khi tạo
            </label>
          </div>
          <div
            v-else
            class="flex items-center gap-2.5 p-3.5 bg-amber-50 rounded-md border border-amber-200 text-xs text-amber-800"
          >
            <AlertCircle class="w-4 h-4 text-amber-600 shrink-0" />
            <span>Công việc thuộc ngày tương lai ({{ form.taskDate }}) — chỉ tạo để lên kế hoạch trước, không thể đánh dấu hoàn thành.</span>
          </div>
        </div>

        <!-- Modal Footer Buttons -->
        <div class="flex items-center justify-end gap-3.5 pt-4 border-t border-slate-200">
          <button
            type="button"
            @click="showTaskModal = false"
            class="px-5 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-md transition-colors"
          >
            Hủy bỏ
          </button>
          <AppButton type="submit" variant="primary" size="md" :loading="isSaving">
            {{ isEditing ? 'Cập nhật công việc' : 'Tạo việc mới' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- Quick Add Modal (Thêm nhanh công việc hàng ngày) -->
    <AppModal
      v-model="showQuickAddModal"
      title="Thêm nhanh các công việc thường ngày"
      size="2xl"
    >
      <div class="space-y-4">
        <!-- Subheader & Date Context -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-3.5 bg-brand-50/70 rounded-md border border-brand-100">
          <div>
            <div class="flex items-center gap-2">
              <Zap class="w-4 h-4 text-amber-500 fill-amber-500" />
              <span class="text-sm font-bold text-slate-900">
                Thêm vào ngày: <span class="text-brand-700 underline font-extrabold">{{ formattedSelectedDateLabel }}</span>
              </span>
            </div>
            <p class="text-xs text-slate-500 mt-0.5">
              Chọn các công việc quen thuộc bên dưới để tạo ngay lập tức mà không cần gõ lại.
            </p>
          </div>

          <div class="flex items-center gap-2 self-start sm:self-auto shrink-0">
            <button
              type="button"
              @click="toggleSelectAll"
              class="px-3 py-1.5 text-xs font-semibold rounded-md border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 transition-colors shadow-2xs cursor-pointer"
            >
              {{ isAllSelected ? 'Bỏ chọn tất cả' : 'Chọn tất cả' }}
            </button>
          </div>
        </div>

        <!-- Tab Switcher & Search Bar -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <!-- Tabs -->
          <div class="flex items-center gap-1.5 p-1 bg-slate-100 rounded-md border border-slate-200">
            <button
              type="button"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold rounded transition-colors cursor-pointer"
              :class="quickAddTab === 'FREQUENT' ? 'bg-white text-brand-700 shadow-2xs' : 'text-slate-600 hover:text-slate-900'"
              @click="quickAddTab = 'FREQUENT'"
            >
              <Zap class="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
              <span>Việc thường làm ({{ frequentTasks.length }})</span>
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold rounded transition-colors cursor-pointer"
              :class="quickAddTab === 'CUSTOM' ? 'bg-white text-brand-700 shadow-2xs' : 'text-slate-600 hover:text-slate-900'"
              @click="quickAddTab = 'CUSTOM'"
            >
              <Star class="w-3.5 h-3.5 text-amber-500 fill-amber-400" />
              <span>Mẫu của tôi ({{ customPresets.length }})</span>
            </button>
          </div>

          <!-- Search & Category Filters -->
          <div class="flex items-center gap-2 flex-1 sm:max-w-md">
            <div class="relative flex-1">
              <Search class="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                v-model="quickSearchQuery"
                placeholder="Tìm việc mẫu..."
                class="w-full pl-8 pr-3 py-1.5 text-xs rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:border-brand-500 shadow-2xs"
              />
            </div>

            <select
              v-model="quickCategoryFilter"
              class="text-xs px-2.5 py-1.5 rounded-md border border-slate-200 bg-white text-slate-700 font-medium shadow-2xs focus:outline-none"
            >
              <option value="ALL">Tất cả danh mục</option>
              <option value="ENGLISH">Tiếng Anh</option>
              <option value="WORK">Công việc</option>
              <option value="STUDY">Học tập</option>
              <option value="HEALTH">Sức khỏe</option>
              <option value="PERSONAL">Cá nhân</option>
            </select>
          </div>
        </div>

        <!-- Custom Preset Creator (Only in CUSTOM tab) -->
        <div v-if="quickAddTab === 'CUSTOM'" class="p-3.5 bg-slate-50 rounded-md border border-slate-200 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-800">Tự tạo mẫu công việc quen thuộc</span>
            <button
              type="button"
              @click="showNewPresetForm = !showNewPresetForm"
              class="inline-flex items-center gap-1 text-xs text-brand-600 hover:text-brand-700 font-semibold cursor-pointer"
            >
              <X v-if="showNewPresetForm" class="w-3.5 h-3.5" />
              <Plus v-else class="w-3.5 h-3.5" />
              <span>{{ showNewPresetForm ? 'Đóng form' : 'Tạo mẫu mới' }}</span>
            </button>
          </div>

          <form v-if="showNewPresetForm" @submit.prevent="saveNewCustomPreset" class="grid grid-cols-1 sm:grid-cols-4 gap-2.5 pt-2">
            <input
              type="text"
              v-model="newPresetForm.title"
              required
              placeholder="Tiêu đề việc mẫu..."
              class="sm:col-span-2 text-xs px-3 py-2 rounded-md border border-slate-200 bg-white focus:outline-none focus:border-brand-500"
            />
            <select
              v-model="newPresetForm.category"
              class="text-xs px-3 py-2 rounded-md border border-slate-200 bg-white focus:outline-none"
            >
              <option value="ENGLISH">Tiếng Anh</option>
              <option value="WORK">Công việc</option>
              <option value="STUDY">Học tập</option>
              <option value="HEALTH">Sức khỏe</option>
              <option value="PERSONAL">Cá nhân</option>
            </select>
            <div class="flex items-center gap-2">
              <input
                type="text"
                v-model="newPresetForm.estimatedTime"
                placeholder="VD: 30 phút"
                class="w-full text-xs px-3 py-2 rounded-md border border-slate-200 bg-white focus:outline-none"
              />
              <button
                type="submit"
                class="px-3 py-2 bg-brand-600 text-white rounded-md text-xs font-bold hover:bg-brand-700 shrink-0 cursor-pointer"
              >
                Lưu
              </button>
            </div>
          </form>
        </div>

        <!-- Task List Items -->
        <div v-if="isLoadingFrequent" class="p-12 text-center text-sm text-slate-400">
          Đang tải danh sách công việc thường làm...
        </div>

        <div v-else-if="filteredQuickTasks.length === 0" class="p-12 text-center text-sm text-slate-400 bg-slate-50/50 rounded-md border border-slate-100">
          Không tìm thấy công việc nào phù hợp với bộ lọc.
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3 max-h-[50vh] overflow-y-auto pr-1">
          <div
            v-for="item in filteredQuickTasks"
            :key="item.idKey"
            @click="toggleSelectTask(item.idKey)"
            :class="[
              'p-3.5 rounded-md border transition-all cursor-pointer flex items-start gap-3 select-none',
              isTaskSelected(item.idKey)
                ? 'bg-brand-50/70 border-brand-500 ring-2 ring-brand-500/10 shadow-2xs'
                : 'bg-white border-slate-200 hover:border-slate-300 hover:bg-slate-50/50',
            ]"
          >
            <!-- Checkbox -->
            <div class="pt-0.5">
              <input
                type="checkbox"
                :checked="isTaskSelected(item.idKey)"
                class="w-4.5 h-4.5 text-brand-600 rounded border-slate-300 focus:ring-brand-500 cursor-pointer pointer-events-none"
              />
            </div>

            <!-- Content -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between gap-2">
                <span class="text-sm font-bold text-slate-900 truncate">
                  {{ item.title }}
                </span>
                <!-- Delete preset button if in custom tab -->
                <button
                  v-if="quickAddTab === 'CUSTOM'"
                  type="button"
                  @click.stop="deleteCustomPreset(item.customIndex!)"
                  class="p-1 text-slate-400 hover:text-rose-600 rounded cursor-pointer"
                  title="Xóa mẫu này"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>

              <!-- Meta pills -->
              <div class="flex flex-wrap items-center gap-1.5 mt-2">
                <span class="text-[11px] font-semibold px-2 py-0.5 rounded" :class="getCategoryBadgeClass(item.category)">
                  {{ getCategoryLabel(item.category) }}
                </span>
                <span class="text-[11px] font-bold px-1.5 py-0.5 rounded" :class="getPriorityBadgeClass(item.priority)">
                  {{ getPriorityLabel(item.priority) }}
                </span>
                <span
                  v-if="item.estimatedTime"
                  class="inline-flex items-center gap-1 text-[11px] font-medium px-1.5 py-0.5 rounded bg-slate-100 text-slate-600"
                >
                  <Clock class="w-3 h-3 text-slate-400" />
                  <span>{{ item.estimatedTime }}</span>
                </span>
                <span v-if="item.frequency && item.frequency > 0" class="text-[11px] font-medium px-1.5 py-0.5 rounded bg-amber-50 text-amber-700 border border-amber-200/60">
                  Đã làm {{ item.frequency }} lần
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="flex items-center justify-between pt-4 border-t border-slate-200">
          <div class="text-xs font-semibold text-slate-600">
            Đã chọn: <span class="font-bold text-brand-600 text-sm">{{ selectedTaskCount }}</span> công việc
          </div>

          <div class="flex items-center gap-2.5">
            <button
              type="button"
              @click="showQuickAddModal = false"
              class="px-4 py-2 text-xs sm:text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
            >
              Hủy bỏ
            </button>
            <AppButton
              variant="primary"
              size="md"
              :disabled="selectedTaskCount === 0"
              :loading="isBulkSaving"
              @click="submitBulkTasks"
            >
              <span>Thêm {{ selectedTaskCount > 0 ? selectedTaskCount : '' }} việc vào ngày này</span>
            </AppButton>
          </div>
        </div>
      </div>
    </AppModal>

    <!-- Delete Confirmation Modal -->
    <AppConfirmDialog
      v-model="showDeleteConfirm"
      title="Xác nhận xóa công việc"
      :message="`Bạn có chắc chắn muốn xóa việc '${taskToDelete?.title}' khỏi danh sách?`"
      confirm-text="Xóa vĩnh viễn"
      confirm-variant="danger"
      @confirm="executeDeleteTask"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { taskService } from '../../services/task.service';
import { useToastStore } from '../../stores/toast.store';
import type { DailyTask, DailyTaskRequest, FrequentTask } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppModal from '../../components/common/AppModal.vue';
import AppConfirmDialog from '../../components/common/AppConfirmDialog.vue';
import AppPagination from '../../components/common/AppPagination.vue';
import {
  CheckSquare,
  Plus,
  BarChart3,
  ChevronLeft,
  ChevronRight,
  ChevronUp,
  ChevronDown,
  Check,
  CheckCircle2,
  Clock,
  Hourglass,
  AlertCircle,
  Award,
  ListTodo,
  Search,
  RefreshCw,
  Edit2,
  Trash2,
  Play,
  Pause,
  Zap,
  Copy,
  Star,
  Layers,
  X,
} from 'lucide-vue-next';

const route = useRoute();
const toast = useToastStore();

// Local Date Helpers
function getLocalDateString(date = new Date()): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

// Date selection state (YYYY-MM-DD)
const todayStr = getLocalDateString();
const selectedDate = ref(todayStr);

function isFutureDate(dateStr?: string): boolean {
  if (!dateStr) return false;
  return dateStr > todayStr;
}

const isSelectedDateFuture = computed(() => {
  return isFutureDate(selectedDate.value);
});

// Tasks data
const tasks = ref<DailyTask[]>([]);
const isLoading = ref(false);
const isSaving = ref(false);

// Filter states
const selectedCategory = ref('ALL');
const selectedPriority = ref('');
const selectedCompletionFilter = ref('ALL');
const searchQuery = ref('');

// Modals
const showTaskModal = ref(false);
const isEditing = ref(false);
const editingTaskId = ref<number | null>(null);
const showDeleteConfirm = ref(false);
const taskToDelete = ref<DailyTask | null>(null);

// Form state
const form = reactive<DailyTaskRequest>({
  taskDate: todayStr,
  title: '',
  description: '',
  category: 'ENGLISH',
  priority: 'MEDIUM',
  status: 'TODO',
  isCompleted: false,
  displayOrder: 0,
  estimatedTime: '',
});

watch(
  () => form.taskDate,
  (newDate) => {
    if (isFutureDate(newDate)) {
      if (form.isCompleted) form.isCompleted = false;
      if (form.status === 'COMPLETED') form.status = 'TODO';
    }
  }
);

const categoryOptions = [
  { label: 'Tất cả danh mục', value: 'ALL' },
  { label: 'Tiếng Anh', value: 'ENGLISH' },
  { label: 'Công việc', value: 'WORK' },
  { label: 'Học tập', value: 'STUDY' },
  { label: 'Cá nhân', value: 'PERSONAL' },
  { label: 'Sức khỏe', value: 'HEALTH' },
  { label: 'Dự án', value: 'PROJECT' },
  { label: 'Khác', value: 'OTHER' },
];

onMounted(async () => {
  if (route.query.date && typeof route.query.date === 'string') {
    selectedDate.value = route.query.date;
  }
  await loadTasksForSelectedDate();

  if (route.query.action === 'add') {
    openCreateModal();
  }

  loadCustomPresets();
  loadFrequentTasks();
});

watch(
  () => route.query.date,
  (newDate) => {
    if (newDate && typeof newDate === 'string' && newDate !== selectedDate.value) {
      selectedDate.value = newDate;
      loadTasksForSelectedDate();
    }
  }
);

// Date Helpers
const isTodaySelected = computed(() => selectedDate.value === todayStr);

const formattedSelectedDateDisplay = computed(() => {
  if (!selectedDate.value) return '';
  const [y, m, d] = selectedDate.value.split('-');
  return `${d}/${m}/${y}`;
});

const formattedSelectedDateLabel = computed(() => {
  if (!selectedDate.value) return '';
  const dateObj = new Date(selectedDate.value + 'T00:00:00');
  const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
  const dayName = days[dateObj.getDay()];
  const [y, m, d] = selectedDate.value.split('-');
  return `${dayName}, ${d}/${m}/${y}`;
});

function setToday() {
  selectedDate.value = todayStr;
  loadTasksForSelectedDate();
}

function navigateDate(daysOffset: number) {
  const current = new Date(selectedDate.value + 'T00:00:00');
  current.setDate(current.getDate() + daysOffset);
  selectedDate.value = getLocalDateString(current);
  loadTasksForSelectedDate();
}

function jumpToDate(daysOffset: number) {
  const target = new Date();
  target.setDate(target.getDate() + daysOffset);
  selectedDate.value = getLocalDateString(target);
  loadTasksForSelectedDate();
}

// KPI Computations
const completedCount = computed(() => tasks.value.filter((t) => t.isCompleted || t.status === 'COMPLETED').length);
const inProgressCount = computed(() => tasks.value.filter((t) => !t.isCompleted && t.status === 'IN_PROGRESS').length);
const pendingCount = computed(() => tasks.value.filter((t) => !t.isCompleted && t.status !== 'COMPLETED' && t.status !== 'IN_PROGRESS').length);

const completionPercentage = computed(() => {
  if (tasks.value.length === 0) return 0;
  return Math.round((completedCount.value / tasks.value.length) * 100);
});

// Time Parsing and Computations
function parseEstimatedMinutes(timeStr?: string): number {
  if (!timeStr || !timeStr.trim()) return 0;
  const str = timeStr.trim().toLowerCase().replace(/,/g, '.');

  // Case 1: "1h30", "1h30p", "1h 30m", "1 giờ 30 phút", "1 tiếng 30 phút"
  const comboMatch = str.match(/(\d+(?:\.\d+)?)\s*(?:h|giờ|tiếng)\s*(\d+)?\s*(?:m|p|phút)?/);
  if (comboMatch) {
    const hours = parseFloat(comboMatch[1]) || 0;
    const mins = comboMatch[2] ? parseFloat(comboMatch[2]) : 0;
    return Math.round(hours * 60 + mins);
  }

  // Case 2: Only minutes or plain number, e.g., "30 phút", "45p", "15m"
  const minsMatch = str.match(/^(\d+(?:\.\d+)?)\s*(?:m|p|phút|min|mins)?$/);
  if (minsMatch) {
    const val = parseFloat(minsMatch[1]);
    if (val <= 12 && !str.includes('p') && !str.includes('m') && !str.includes('phút')) {
      return Math.round(val * 60);
    }
    return Math.round(val);
  }

  // Fallback: extract any numbers
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
  if (h > 0 && m > 0) {
    return `${h}h ${m}p`;
  }
  if (h > 0 && m === 0) {
    return `${h} giờ`;
  }
  return `${m} phút`;
}

const totalEstimatedMinutes = computed(() => {
  return tasks.value.reduce((acc, t) => acc + parseEstimatedMinutes(t.estimatedTime), 0);
});

const completedEstimatedMinutes = computed(() => {
  return tasks.value
    .filter((t) => t.isCompleted || t.status === 'COMPLETED')
    .reduce((acc, t) => acc + parseEstimatedMinutes(t.estimatedTime), 0);
});

const remainingEstimatedMinutes = computed(() => {
  return Math.max(0, totalEstimatedMinutes.value - completedEstimatedMinutes.value);
});

const totalEstimatedTimeDisplay = computed(() => {
  return formatMinutesToReadable(totalEstimatedMinutes.value);
});

const remainingEstimatedTimeDisplay = computed(() => {
  return formatMinutesToReadable(remainingEstimatedMinutes.value);
});

const dayEvaluationText = computed(() => {
  if (tasks.value.length === 0) return 'Chưa có việc';
  if (completionPercentage.value === 100) return 'Xuất sắc 100%';
  if (completionPercentage.value >= 70) return 'Đạt mục tiêu tốt';
  if (completionPercentage.value >= 40) return 'Đang tiến triển';
  return 'Cần cố gắng';
});

// Filtered and Sorted Task List (Uncompleted on top, Completed at bottom)
const filteredTasks = computed(() => {
  const list = tasks.value.filter((t) => {
    // Category filter
    if (selectedCategory.value !== 'ALL' && t.category !== selectedCategory.value) {
      return false;
    }
    // Priority filter
    if (selectedPriority.value && t.priority !== selectedPriority.value) {
      return false;
    }
    // Completion filter
    if (selectedCompletionFilter.value === 'COMPLETED' && !t.isCompleted && t.status !== 'COMPLETED') {
      return false;
    }
    if (selectedCompletionFilter.value === 'PENDING' && (t.isCompleted || t.status === 'COMPLETED')) {
      return false;
    }
    // Search query
    if (searchQuery.value.trim()) {
      const q = searchQuery.value.toLowerCase();
      const matchTitle = t.title.toLowerCase().includes(q);
      const matchDesc = t.description?.toLowerCase().includes(q) || false;
      if (!matchTitle && !matchDesc) return false;
    }
    return true;
  });

  // Sort: Tasks that are NOT completed come first, completed tasks move to the bottom
  return list.sort((a, b) => {
    const aDone = a.isCompleted || a.status === 'COMPLETED' ? 1 : 0;
    const bDone = b.isCompleted || b.status === 'COMPLETED' ? 1 : 0;
    if (aDone !== bDone) {
      return aDone - bDone;
    }
    return (a.displayOrder || 0) - (b.displayOrder || 0);
  });
});

// Pagination for tasks
const currentPage = ref(0);
const pageSize = ref(10);

const paginatedTasks = computed(() => {
  const start = currentPage.value * pageSize.value;
  return filteredTasks.value.slice(start, start + pageSize.value);
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredTasks.value.length / pageSize.value));
});

watch([selectedCategory, selectedPriority, selectedCompletionFilter, searchQuery, selectedDate], () => {
  currentPage.value = 0;
});

// API Operations
async function loadTasksForSelectedDate() {
  if (!selectedDate.value) return;
  isLoading.value = true;
  try {
    tasks.value = await taskService.getTasksByDate(selectedDate.value);
  } catch (error) {
    console.error(error);
    toast.error('Không thể tải danh sách công việc');
  } finally {
    isLoading.value = false;
  }
}

async function toggleTaskCompletion(task: DailyTask) {
  if (isFutureDate(task.taskDate)) {
    toast.warning('Không thể đánh dấu hoàn thành công việc của ngày trong tương lai!');
    return;
  }

  // Optimistic local update
  const originalState = task.isCompleted;
  task.isCompleted = !originalState;
  task.status = task.isCompleted ? 'COMPLETED' : 'TODO';

  try {
    const updated = await taskService.toggleTask(task.id);
    task.isCompleted = updated.isCompleted;
    task.status = updated.status;
    task.completedAt = updated.completedAt;
  } catch (error: any) {
    // Rollback
    task.isCompleted = originalState;
    task.status = originalState ? 'COMPLETED' : 'TODO';
    const msg = error?.response?.data?.message || 'Lỗi khi cập nhật trạng thái';
    toast.error(msg);
  }
}

function openCreateModal() {
  isEditing.value = false;
  editingTaskId.value = null;
  form.taskDate = selectedDate.value;
  form.title = '';
  form.description = '';
  form.category = selectedCategory.value !== 'ALL' ? selectedCategory.value : 'ENGLISH';
  form.priority = 'MEDIUM';
  form.status = 'TODO';
  form.isCompleted = false;
  form.displayOrder = tasks.value.length;
  form.estimatedTime = '';
  loadCustomPresets();
  if (frequentTasks.value.length === 0) {
    loadFrequentTasks();
  }
  showTaskModal.value = true;
}

function openEditModal(task: DailyTask) {
  isEditing.value = true;
  editingTaskId.value = task.id;
  form.taskDate = task.taskDate;
  form.title = task.title;
  form.description = task.description || '';
  form.category = task.category || 'ENGLISH';
  form.priority = task.priority || 'MEDIUM';
  form.status = task.status || 'TODO';
  form.isCompleted = isFutureDate(task.taskDate) ? false : task.isCompleted;
  form.displayOrder = task.displayOrder || 0;
  form.estimatedTime = task.estimatedTime || '';
  showTaskModal.value = true;
}

async function saveTask() {
  if (!form.title.trim()) {
    toast.warning('Vui lòng nhập tiêu đề công việc');
    return;
  }

  if (isFutureDate(form.taskDate)) {
    form.isCompleted = false;
    if (form.status === 'COMPLETED') {
      form.status = 'TODO';
    }
  }

  isSaving.value = true;
  try {
    if (isEditing.value && editingTaskId.value) {
      await taskService.updateTask(editingTaskId.value, form);
      toast.success('Đã cập nhật công việc');
    } else {
      await taskService.createTask(form);
      toast.success('Đã thêm công việc mới');
    }
    showTaskModal.value = false;
    await loadTasksForSelectedDate();
  } catch (error: any) {
    console.error(error);
    const msg = error?.response?.data?.message || 'Không thể lưu công việc';
    toast.error(msg);
  } finally {
    isSaving.value = false;
  }
}

function confirmDeleteTask(task: DailyTask) {
  taskToDelete.value = task;
  showDeleteConfirm.value = true;
}

async function executeDeleteTask() {
  if (!taskToDelete.value) return;
  try {
    await taskService.deleteTask(taskToDelete.value.id);
    toast.success('Đã xóa công việc');
    await loadTasksForSelectedDate();
  } catch (error) {
    toast.error('Không thể xóa công việc');
  } finally {
    showDeleteConfirm.value = false;
    taskToDelete.value = null;
  }
}

async function moveTask(index: number, direction: number) {
  const targetIndex = index + direction;
  if (targetIndex < 0 || targetIndex >= filteredTasks.value.length) return;

  const currentTask = filteredTasks.value[index];
  const targetTask = filteredTasks.value[targetIndex];

  // Swap displayOrder
  const tempOrder = currentTask.displayOrder;
  currentTask.displayOrder = targetTask.displayOrder;
  targetTask.displayOrder = tempOrder;

  // Swap in array
  tasks.value.sort((a, b) => a.displayOrder - b.displayOrder);

  // Send reorder payload to server
  try {
    await taskService.reorderTasks([
      { id: currentTask.id, displayOrder: currentTask.displayOrder },
      { id: targetTask.id, displayOrder: targetTask.displayOrder },
    ]);
  } catch (error) {
    console.error(error);
  }
}

// Styling Helpers
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
      return 'bg-rose-100 text-rose-800 font-extrabold animate-pulse';
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

function getStatusLabel(task: DailyTask) {
  if (task.isCompleted || task.status === 'COMPLETED') {
    return 'Đã hoàn thành';
  }
  switch (task.status) {
    case 'IN_PROGRESS':
      return 'Đang thực hiện';
    case 'CANCELLED':
      return 'Đã hủy';
    case 'TODO':
    default:
      return 'Chưa làm';
  }
}

function getStatusSelectClass(task: DailyTask) {
  if (task.isCompleted || task.status === 'COMPLETED') {
    return 'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100';
  }
  switch (task.status) {
    case 'IN_PROGRESS':
      return 'bg-sky-50 text-sky-700 border-sky-200 hover:bg-sky-100';
    case 'CANCELLED':
      return 'bg-slate-100 text-slate-600 border-slate-200 hover:bg-slate-200';
    case 'TODO':
    default:
      return 'bg-amber-50 text-amber-700 border-amber-200 hover:bg-amber-100';
  }
}

async function changeTaskStatus(task: DailyTask, newStatus: string) {
  if (newStatus === 'COMPLETED' && isFutureDate(task.taskDate)) {
    toast.warning('Không thể đánh dấu hoàn thành công việc của ngày trong tương lai!');
    return;
  }

  const prevStatus = task.status;
  const prevCompleted = task.isCompleted;

  task.status = newStatus;
  task.isCompleted = newStatus === 'COMPLETED';

  try {
    const updated = await taskService.updateStatus(task.id, newStatus);
    task.status = updated.status;
    task.isCompleted = updated.isCompleted;
    task.completedAt = updated.completedAt;
    toast.success(`Đã chuyển trạng thái sang: ${getStatusLabel(task)}`);
  } catch (error: any) {
    task.status = prevStatus;
    task.isCompleted = prevCompleted;
    const msg = error?.response?.data?.message || 'Lỗi khi cập nhật trạng thái';
    toast.error(msg);
  }
}

async function toggleTaskInProgress(task: DailyTask) {
  const targetStatus = task.status === 'IN_PROGRESS' ? 'TODO' : 'IN_PROGRESS';
  await changeTaskStatus(task, targetStatus);
}

// ==========================================
// QUICK ADD & TASK TEMPLATES LOGIC
// ==========================================
const showQuickAddModal = ref(false);
const isLoadingFrequent = ref(false);
const isBulkSaving = ref(false);
const isCopying = ref(false);

const frequentTasks = ref<FrequentTask[]>([]);
const selectedTaskKeys = ref<Set<string>>(new Set());
const quickAddTab = ref<'FREQUENT' | 'CUSTOM'>('FREQUENT');
const quickSearchQuery = ref('');
const quickCategoryFilter = ref('ALL');

const CUSTOM_PRESETS_STORAGE_KEY = 'pmh_daily_task_custom_presets';
const customPresets = ref<FrequentTask[]>([]);
const showNewPresetForm = ref(false);
const newPresetForm = reactive({
  title: '',
  category: 'ENGLISH',
  priority: 'MEDIUM',
  estimatedTime: '',
});

function loadCustomPresets() {
  try {
    const raw = localStorage.getItem(CUSTOM_PRESETS_STORAGE_KEY);
    if (raw) {
      customPresets.value = JSON.parse(raw);
    } else {
      customPresets.value = [];
    }
  } catch (e) {
    customPresets.value = [];
  }
}

function saveNewCustomPreset() {
  if (!newPresetForm.title.trim()) return;
  customPresets.value.push({
    title: newPresetForm.title.trim(),
    category: newPresetForm.category,
    priority: newPresetForm.priority as any,
    estimatedTime: newPresetForm.estimatedTime.trim() || undefined,
  });
  localStorage.setItem(CUSTOM_PRESETS_STORAGE_KEY, JSON.stringify(customPresets.value));
  newPresetForm.title = '';
  newPresetForm.estimatedTime = '';
  showNewPresetForm.value = false;
  toast.success('Đã lưu mẫu công việc mới');
}

function deleteCustomPreset(index: number) {
  customPresets.value.splice(index, 1);
  localStorage.setItem(CUSTOM_PRESETS_STORAGE_KEY, JSON.stringify(customPresets.value));
  toast.success('Đã xóa mẫu công việc');
}

const DEFAULT_HABIT_PRESETS: FrequentTask[] = [
  { title: 'Học 30 từ vựng TOEIC mới', category: 'ENGLISH', priority: 'HIGH', estimatedTime: '30 phút' },
  { title: 'Luyện đề Listening TOEIC Part 1 & 2', category: 'ENGLISH', priority: 'HIGH', estimatedTime: '45 phút' },
  { title: 'Luyện đọc Reading TOEIC Part 5 & 7', category: 'ENGLISH', priority: 'HIGH', estimatedTime: '45 phút' },
  { title: 'Tập thể dục / Chạy bộ / Gym', category: 'HEALTH', priority: 'MEDIUM', estimatedTime: '45 phút' },
  { title: 'Đọc 10 trang sách / Note kiến thức IT', category: 'STUDY', priority: 'MEDIUM', estimatedTime: '30 phút' },
  { title: 'Check email & Lên kế hoạch công việc', category: 'WORK', priority: 'MEDIUM', estimatedTime: '20 phút' },
  { title: 'Review code & Dọn dẹp task tồn', category: 'WORK', priority: 'HIGH', estimatedTime: '1 giờ' },
  { title: 'Dọn dẹp bàn làm việc & Nghỉ ngơi', category: 'PERSONAL', priority: 'LOW', estimatedTime: '15 phút' },
];

// Quick Habit Pill Suggestions for regular Add Task modal
const quickPillSuggestions = computed(() => {
  const combined: FrequentTask[] = [
    ...customPresets.value,
    ...frequentTasks.value,
    ...DEFAULT_HABIT_PRESETS,
  ];
  const uniqueTitles = new Set<string>();
  const list: FrequentTask[] = [];
  for (const item of combined) {
    const key = item.title.trim().toLowerCase();
    if (!uniqueTitles.has(key)) {
      uniqueTitles.add(key);
      list.push(item);
    }
    if (list.length >= 12) break;
  }
  return list;
});

function applyQuickPill(p: FrequentTask) {
  form.title = p.title;
  if (p.category) form.category = p.category;
  if (p.priority) form.priority = p.priority as any;
  if (p.estimatedTime) form.estimatedTime = p.estimatedTime;
  toast.success(`Đã chọn: ${p.title}`);
}

async function loadFrequentTasks() {
  try {
    isLoadingFrequent.value = true;
    frequentTasks.value = await taskService.getFrequentTasks(30);
  } catch (e) {
    console.error(e);
  } finally {
    isLoadingFrequent.value = false;
  }
}

async function openQuickAddModal() {
  selectedTaskKeys.value = new Set();
  quickSearchQuery.value = '';
  quickCategoryFilter.value = 'ALL';
  loadCustomPresets();
  showQuickAddModal.value = true;
  await loadFrequentTasks();
}

interface QuickTaskDisplayItem extends FrequentTask {
  idKey: string;
  customIndex?: number;
}

const currentSourceQuickTasks = computed<QuickTaskDisplayItem[]>(() => {
  if (quickAddTab.value === 'CUSTOM') {
    return customPresets.value.map((item, idx) => ({
      ...item,
      idKey: `custom_${idx}_${item.title}`,
      customIndex: idx,
    }));
  }
  return frequentTasks.value.map((item, idx) => ({
    ...item,
    idKey: `frequent_${idx}_${item.title}`,
  }));
});

const filteredQuickTasks = computed(() => {
  return currentSourceQuickTasks.value.filter((item) => {
    if (quickCategoryFilter.value !== 'ALL' && item.category !== quickCategoryFilter.value) {
      return false;
    }
    if (quickSearchQuery.value.trim()) {
      const q = quickSearchQuery.value.toLowerCase();
      if (!item.title.toLowerCase().includes(q)) return false;
    }
    return true;
  });
});

function isTaskSelected(idKey: string) {
  return selectedTaskKeys.value.has(idKey);
}

function toggleSelectTask(idKey: string) {
  if (selectedTaskKeys.value.has(idKey)) {
    selectedTaskKeys.value.delete(idKey);
  } else {
    selectedTaskKeys.value.add(idKey);
  }
}

const isAllSelected = computed(() => {
  if (filteredQuickTasks.value.length === 0) return false;
  return filteredQuickTasks.value.every((t) => selectedTaskKeys.value.has(t.idKey));
});

function toggleSelectAll() {
  if (isAllSelected.value) {
    for (const t of filteredQuickTasks.value) {
      selectedTaskKeys.value.delete(t.idKey);
    }
  } else {
    for (const t of filteredQuickTasks.value) {
      selectedTaskKeys.value.add(t.idKey);
    }
  }
}

const selectedTaskCount = computed(() => selectedTaskKeys.value.size);

async function submitBulkTasks() {
  if (selectedTaskCount.value === 0) {
    toast.warning('Vui lòng chọn ít nhất 1 công việc để thêm');
    return;
  }

  const allAvailable = currentSourceQuickTasks.value;
  const chosenItems = allAvailable.filter((t) => selectedTaskKeys.value.has(t.idKey));

  if (chosenItems.length === 0) return;

  isBulkSaving.value = true;
  try {
    const payload = chosenItems.map((item) => ({
      taskDate: selectedDate.value,
      title: item.title,
      description: item.description || '',
      category: item.category || 'ENGLISH',
      priority: (item.priority || 'MEDIUM') as any,
      status: 'TODO' as any,
      isCompleted: false,
      estimatedTime: item.estimatedTime || '',
    }));

    await taskService.bulkCreateTasks({
      taskDate: selectedDate.value,
      tasks: payload,
    });

    toast.success(`Đã thêm ${chosenItems.length} công việc vào ngày ${formattedSelectedDateLabel.value}!`);
    showQuickAddModal.value = false;
    await loadTasksForSelectedDate();
  } catch (error: any) {
    const msg = error?.response?.data?.message || 'Không thể thêm công việc';
    toast.error(msg);
  } finally {
    isBulkSaving.value = false;
  }
}

async function copyFromYesterday() {
  const current = new Date(selectedDate.value + 'T00:00:00');
  current.setDate(current.getDate() - 1);
  const yesterdayStr = getLocalDateString(current);

  isCopying.value = true;
  try {
    const res = await taskService.copyTasksFromDate({
      sourceDate: yesterdayStr,
      targetDate: selectedDate.value,
      onlyIncomplete: false,
    });
    toast.success(`Đã sao chép ${res.length} công việc từ ngày hôm qua!`);
    await loadTasksForSelectedDate();
  } catch (error: any) {
    const msg = error?.response?.data?.message || 'Không thể sao chép công việc từ hôm qua';
    toast.warning(msg);
  } finally {
    isCopying.value = false;
  }
}
</script>
