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
            class="px-2.5 py-1.5 border border-slate-200 rounded text-slate-600 hover:bg-slate-50 transition-colors font-medium"
          >
            Hôm qua
          </button>
          <button
            type="button"
            @click="jumpToDate(1)"
            class="px-2.5 py-1.5 border border-slate-200 rounded text-slate-600 hover:bg-slate-50 transition-colors font-medium"
          >
            Ngày mai
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
      <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
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
      <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
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
      <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
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
      <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
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
      <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
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
      <div class="bg-white p-4.5 sm:p-5 rounded-md border border-slate-200 shadow-xs flex flex-col justify-between">
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
          Tích chọn checkbox để đánh dấu hoàn thành
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
                task.isCompleted ? 'bg-slate-50/70' : 'bg-white'
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
                    :class="[
                      'w-6 h-6 rounded-md border-2 flex items-center justify-center transition-all duration-200',
                      task.isCompleted
                        ? 'bg-emerald-500 border-emerald-500 text-white shadow-xs scale-105'
                        : 'border-slate-300 hover:border-brand-500 bg-white text-transparent hover:text-slate-300'
                    ]"
                    title="Tích để hoàn thành / bỏ hoàn thành"
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

              <!-- 7. Trạng thái -->
              <td class="py-3.5 px-4 text-center align-middle whitespace-nowrap">
                <span
                  :class="[
                    'inline-block text-xs px-3 py-1 rounded-md shadow-2xs whitespace-nowrap',
                    getStatusBadgeClass(task)
                  ]"
                >
                  {{ getStatusLabel(task) }}
                </span>
              </td>

              <!-- 8. Thao tác -->
              <td class="py-3.5 px-4 text-center align-middle whitespace-nowrap">
                <div class="flex items-center justify-center gap-1.5">
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
      size="xl"
    >
      <form @submit.prevent="saveTask" class="space-y-6 py-3">
        <!-- Section 1: Thông tin cơ bản -->
        <div class="bg-slate-50/70 p-5 sm:p-6 rounded-md border border-slate-200/80 space-y-5">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-5 sm:gap-6">
            <div class="md:col-span-1">
              <label class="block text-xs sm:text-sm font-bold text-slate-700 uppercase tracking-wide mb-2">
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
              <label class="block text-xs sm:text-sm font-bold text-slate-700 uppercase tracking-wide mb-2">
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
              <label class="block text-xs sm:text-sm font-bold text-slate-700 uppercase tracking-wide mb-2">
                Phân loại (Category)
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
              <label class="block text-xs sm:text-sm font-bold text-slate-700 uppercase tracking-wide mb-2">
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
              <label class="block text-xs sm:text-sm font-bold text-slate-700 uppercase tracking-wide mb-2">
                Trạng thái ban đầu
              </label>
              <select
                v-model="form.status"
                class="w-full text-sm sm:text-base px-3.5 py-2.5 rounded-md border border-slate-200 bg-white text-slate-800 font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
              >
                <option value="TODO">Chưa làm (Todo)</option>
                <option value="IN_PROGRESS">Đang làm (In Progress)</option>
                <option value="COMPLETED">Đã xong (Completed)</option>
                <option value="CANCELLED">Đã hủy (Cancelled)</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Section 2: Thời gian & Chi tiết -->
        <div class="bg-slate-50/70 p-5 sm:p-6 rounded-md border border-slate-200/80 space-y-5">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-5 sm:gap-6">
            <div>
              <label class="block text-xs sm:text-sm font-bold text-slate-700 uppercase tracking-wide mb-2">
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
              <label class="block text-xs sm:text-sm font-bold text-slate-700 uppercase tracking-wide mb-2">
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
            <label class="block text-xs sm:text-sm font-bold text-slate-700 uppercase tracking-wide mb-2">
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
          <div class="flex items-center gap-3 p-3.5 bg-white rounded-md border border-slate-200">
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
import type { DailyTask, DailyTaskRequest } from '../../types';
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
} from 'lucide-vue-next';

const route = useRoute();
const toast = useToastStore();

// Date selection state (YYYY-MM-DD)
const todayStr = new Date().toISOString().split('T')[0];
const selectedDate = ref(todayStr);

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
  selectedDate.value = current.toISOString().split('T')[0];
  loadTasksForSelectedDate();
}

function jumpToDate(daysOffset: number) {
  const target = new Date();
  target.setDate(target.getDate() + daysOffset);
  selectedDate.value = target.toISOString().split('T')[0];
  loadTasksForSelectedDate();
}

// KPI Computations
const completedCount = computed(() => tasks.value.filter((t) => t.isCompleted || t.status === 'COMPLETED').length);
const inProgressCount = computed(() => tasks.value.filter((t) => !t.isCompleted && t.status === 'IN_PROGRESS').length);
const pendingCount = computed(() => tasks.value.filter((t) => !t.isCompleted && t.status === 'TODO').length);

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
  // Optimistic local update
  const originalState = task.isCompleted;
  task.isCompleted = !originalState;
  task.status = task.isCompleted ? 'COMPLETED' : 'TODO';

  try {
    const updated = await taskService.toggleTask(task.id);
    task.isCompleted = updated.isCompleted;
    task.status = updated.status;
    task.completedAt = updated.completedAt;
  } catch (error) {
    // Rollback
    task.isCompleted = originalState;
    task.status = originalState ? 'COMPLETED' : 'TODO';
    toast.error('Lỗi khi cập nhật trạng thái');
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
  form.isCompleted = task.isCompleted;
  form.displayOrder = task.displayOrder || 0;
  form.estimatedTime = task.estimatedTime || '';
  showTaskModal.value = true;
}

async function saveTask() {
  if (!form.title.trim()) {
    toast.warning('Vui lòng nhập tiêu đề công việc');
    return;
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
  } catch (error) {
    console.error(error);
    toast.error('Không thể lưu công việc');
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

function getStatusBadgeClass(task: DailyTask) {
  if (task.isCompleted || task.status === 'COMPLETED') {
    return 'bg-emerald-50 text-emerald-700 border border-emerald-300 font-bold';
  }
  switch (task.status) {
    case 'IN_PROGRESS':
      return 'bg-sky-50 text-sky-700 border border-sky-300 font-bold';
    case 'CANCELLED':
      return 'bg-slate-100 text-slate-600 border border-slate-300 font-medium';
    case 'TODO':
    default:
      return 'bg-amber-50 text-amber-700 border border-amber-300 font-bold';
  }
}
</script>
