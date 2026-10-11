<template>
  <div class="space-y-6">
    <!-- Header Controls & Overall Metrics -->
    <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div>
        <h3 class="text-base font-bold text-slate-900">Mục tiêu tài chính & Kế hoạch tiết kiệm</h3>
        <p class="text-xs text-slate-500 mt-0.5">
          Quản lý đa mục tiêu (Mua xe, Laptop, Quỹ dự phòng, Du lịch...), theo dõi tiến độ và tốc độ tích lũy thực tế
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-2.5 w-full md:w-auto">
        <!-- Status filter buttons -->
        <div class="flex items-center gap-1 bg-slate-100 p-1 rounded-md text-xs font-semibold">
          <button
            type="button"
            @click="filterStatus = ''"
            :class="[
              'px-2.5 py-1 rounded transition-colors',
              filterStatus === '' ? 'bg-white text-slate-900 shadow-2xs font-bold' : 'text-slate-600 hover:text-slate-900',
            ]"
          >
            Tất cả ({{ goals.length }})
          </button>
          <button
            type="button"
            @click="filterStatus = 'ACTIVE'"
            :class="[
              'px-2.5 py-1 rounded transition-colors',
              filterStatus === 'ACTIVE' ? 'bg-white text-emerald-700 shadow-2xs font-bold' : 'text-slate-600 hover:text-slate-900',
            ]"
          >
            Đang chạy
          </button>
          <button
            type="button"
            @click="filterStatus = 'COMPLETED'"
            :class="[
              'px-2.5 py-1 rounded transition-colors',
              filterStatus === 'COMPLETED' ? 'bg-white text-blue-700 shadow-2xs font-bold' : 'text-slate-600 hover:text-slate-900',
            ]"
          >
            Hoàn thành
          </button>
        </div>

        <button
          type="button"
          @click="openAddGoalModal"
          class="inline-flex items-center justify-center gap-1.5 px-3.5 py-2 text-xs sm:text-sm font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-md transition-colors shadow-xs"
        >
          <Plus class="w-4 h-4" />
          <span>Tạo mục tiêu</span>
        </button>

        <button
          type="button"
          @click="loadGoals"
          :disabled="loading"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs sm:text-sm font-medium text-slate-700 bg-white border border-slate-200 rounded-md hover:bg-slate-50 transition-colors shadow-xs"
          title="Làm mới"
        >
          <RefreshCw :class="['w-4 h-4 text-slate-500', loading ? 'animate-spin' : '']" />
        </button>
      </div>
    </div>

    <!-- Goals Metric Highlights -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex items-center gap-4">
        <div class="w-11 h-11 rounded-md bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
          <Target class="w-6 h-6" />
        </div>
        <div>
          <span class="text-xs text-slate-400 font-semibold uppercase">Tổng tiền mục tiêu</span>
          <p class="text-xl font-bold text-slate-900 tracking-tight">{{ formatCurrency(totalTargetAmount) }}</p>
        </div>
      </div>

      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex items-center gap-4">
        <div class="w-11 h-11 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
          <PiggyBank class="w-6 h-6" />
        </div>
        <div>
          <span class="text-xs text-slate-400 font-semibold uppercase">Đã tích lũy được</span>
          <p class="text-xl font-bold text-emerald-600 tracking-tight">{{ formatCurrency(totalCurrentAmount) }}</p>
        </div>
      </div>

      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex items-center gap-4">
        <div class="w-11 h-11 rounded-md bg-amber-50 text-amber-600 flex items-center justify-center shrink-0">
          <Percent class="w-6 h-6" />
        </div>
        <div>
          <span class="text-xs text-slate-400 font-semibold uppercase">Tiến độ chung</span>
          <p class="text-xl font-bold text-amber-600 tracking-tight">{{ overallProgressPercentage.toFixed(1) }}%</p>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading && goals.length === 0" class="flex flex-col items-center justify-center py-16 bg-white rounded-md border border-slate-200">
      <div class="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      <span class="mt-3 text-xs font-medium text-slate-500">Đang tải danh sách mục tiêu...</span>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="filteredGoals.length === 0"
      class="py-16 flex flex-col items-center justify-center text-center bg-white rounded-md border border-slate-200 p-6"
    >
      <Target class="w-12 h-12 text-slate-300 mb-3" />
      <h3 class="text-sm font-bold text-slate-800">Chưa có mục tiêu nào</h3>
      <p class="text-xs text-slate-500 max-w-sm mt-1">
        Bạn có thể tạo bất kỳ mục tiêu tài chính nào như Mua xe máy, Laptop mới, Quỹ dự phòng, Đi du lịch...
      </p>
      <button
        type="button"
        @click="openAddGoalModal"
        class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-md transition-colors shadow-xs"
      >
        <Plus class="w-4 h-4" />
        <span>Tạo mục tiêu đầu tiên</span>
      </button>
    </div>

    <!-- Goals Grid Cards -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-5">
      <div
        v-for="g in filteredGoals"
        :key="g.id"
        class="bg-white p-5 rounded-md border border-slate-200 shadow-xs hover:border-brand-200 hover:shadow-sm transition-all flex flex-col justify-between"
      >
        <!-- Top Title & Status -->
        <div>
          <div class="flex items-start justify-between gap-3">
            <div class="flex items-center gap-3">
              <div
                class="w-11 h-11 rounded-md flex items-center justify-center text-white shrink-0 font-bold shadow-2xs"
                :style="{ backgroundColor: g.color || '#2563eb' }"
              >
                <Target class="w-6 h-6" />
              </div>
              <div>
                <h4 class="text-base font-bold text-slate-900">{{ g.name }}</h4>
                <p v-if="g.description" class="text-xs text-slate-500 line-clamp-1 mt-0.5">
                  {{ g.description }}
                </p>
              </div>
            </div>

            <!-- Status Badge -->
            <span
              :class="[
                'px-2.5 py-0.5 text-xs font-bold rounded-full border shrink-0',
                getGoalStatusLabel(g.status).class,
              ]"
            >
              {{ getGoalStatusLabel(g.status).label }}
            </span>
          </div>

          <!-- Target vs Accumulated -->
          <div class="mt-4 pt-3 border-t border-slate-100 grid grid-cols-2 gap-3 text-xs">
            <div>
              <span class="text-slate-400 font-medium text-[11px]">Đã tích lũy:</span>
              <p class="text-base font-bold text-emerald-600">{{ formatCurrency(g.currentAmount) }}</p>
            </div>
            <div class="text-right">
              <span class="text-slate-400 font-medium text-[11px]">Mục tiêu cần đạt:</span>
              <p class="text-base font-bold text-slate-900">{{ formatCurrency(g.targetAmount) }}</p>
            </div>
          </div>

          <!-- Progress Bar with Percentage -->
          <div class="mt-2.5">
            <div class="flex items-center justify-between text-xs mb-1 font-bold">
              <span class="text-slate-600">Tiến độ hoàn thành:</span>
              <span class="text-brand-600">{{ g.progressPercentage.toFixed(1) }}%</span>
            </div>
            <div class="w-full h-2.5 rounded-full bg-slate-100 overflow-hidden">
              <div
                class="h-full rounded-full transition-all duration-500"
                :style="{
                  width: `${Math.min(100, g.progressPercentage)}%`,
                  backgroundColor: g.color || '#2563eb',
                }"
              ></div>
            </div>
          </div>

          <!-- Plan Calculations & Deadlines (Requirement 6.5) -->
          <div class="mt-3.5 p-3 rounded-md bg-slate-50 border border-slate-100 space-y-1.5 text-xs">
            <div class="flex items-center justify-between">
              <span class="text-slate-500">Số tiền còn thiếu:</span>
              <strong class="text-slate-800 font-bold">{{ formatCurrency(g.remainingAmount) }}</strong>
            </div>

            <div v-if="g.targetDate" class="flex items-center justify-between">
              <span class="text-slate-500">Hạn hoàn thành:</span>
              <span class="font-semibold text-slate-700">{{ formatDate(g.targetDate) }}</span>
            </div>

            <div v-if="g.monthlySavingsNeeded" class="flex items-center justify-between">
              <span class="text-slate-500">Cần tiết kiệm mỗi tháng:</span>
              <span class="font-bold text-brand-700">~{{ formatCurrency(g.monthlySavingsNeeded) }} / tháng</span>
            </div>

            <div v-if="g.planStatusNote" class="pt-1 border-t border-slate-200/60 text-[11px] text-slate-500 flex items-center gap-1.5">
              <Info class="w-3.5 h-3.5 text-slate-400 shrink-0" />
              <span>{{ g.planStatusNote }}</span>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="mt-4 pt-3 border-t border-slate-100 flex flex-wrap items-center justify-between gap-2 text-xs">
          <!-- Primary Goal Actions -->
          <div class="flex items-center gap-1.5">
            <button
              type="button"
              @click="openContributeModal(g, 'DEPOSIT')"
              class="px-2.5 py-1.5 font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded transition-colors flex items-center gap-1"
            >
              <PlusCircle class="w-3.5 h-3.5" />
              <span>Tích lũy</span>
            </button>

            <button
              type="button"
              :disabled="g.currentAmount <= 0"
              @click="openContributeModal(g, 'WITHDRAW')"
              class="px-2.5 py-1.5 font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 disabled:opacity-40 rounded transition-colors flex items-center gap-1"
            >
              <MinusCircle class="w-3.5 h-3.5" />
              <span>Rút tiền</span>
            </button>

            <button
              type="button"
              @click="openContributionsHistory(g)"
              class="px-2.5 py-1.5 font-semibold text-brand-600 hover:bg-brand-50 rounded transition-colors flex items-center gap-1"
            >
              <History class="w-3.5 h-3.5" />
              <span>Lịch sử ({{ g.contributionCount }})</span>
            </button>
          </div>

          <!-- Secondary Goal Control Actions -->
          <div class="flex items-center gap-1">
            <!-- Toggle Status (Pause / Resume / Complete) -->
            <button
              v-if="g.status === 'ACTIVE'"
              type="button"
              @click="changeGoalStatus(g.id, 'PAUSED')"
              class="p-1.5 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded"
              title="Tạm dừng mục tiêu"
            >
              <Pause class="w-4 h-4" />
            </button>
            <button
              v-else-if="g.status === 'PAUSED'"
              type="button"
              @click="changeGoalStatus(g.id, 'ACTIVE')"
              class="p-1.5 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded"
              title="Tiếp tục mục tiêu"
            >
              <Play class="w-4 h-4" />
            </button>

            <button
              type="button"
              @click="openEditGoalModal(g)"
              class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded"
              title="Sửa mục tiêu"
            >
              <Edit2 class="w-4 h-4" />
            </button>

            <button
              type="button"
              @click="confirmDeleteGoal(g)"
              class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded"
              title="Xóa mục tiêu"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==========================================
         MODAL: CREATE / EDIT GOAL
    ========================================== -->
    <AppModal
      v-model="isGoalModalOpen"
      :title="isEditingGoal ? 'Chỉnh sửa mục tiêu' : 'Tạo mục tiêu tài chính mới'"
      size="md"
    >
      <form @submit.prevent="saveGoal" class="space-y-4 text-xs">
        <div>
          <label class="block font-bold text-slate-700 mb-1">Tên mục tiêu *</label>
          <input
            v-model="goalForm.name"
            type="text"
            required
            placeholder="Ví dụ: Mua xe máy Honda Air Blade, Mua Laptop Macbook Pro, Quỹ dự phòng 6 tháng..."
            class="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-md text-slate-900 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div>
          <label class="block font-bold text-slate-700 mb-1">Số tiền mục tiêu cần đạt (VNĐ) *</label>
          <CurrencyInput
            v-model="goalForm.targetAmount"
            placeholder="Ví dụ: 10.000.000"
            :required="true"
          />
        </div>

        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block font-bold text-slate-700 mb-1">Ngày bắt đầu *</label>
            <input
              v-model="goalForm.startDate"
              type="date"
              required
              class="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>

          <div>
            <label class="block font-bold text-slate-700 mb-1">Hạn hoàn thành (Dự kiến)</label>
            <input
              v-model="goalForm.targetDate"
              type="date"
              class="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
            />
          </div>
        </div>

        <!-- Color Palette Picker -->
        <div>
          <label class="block font-bold text-slate-700 mb-1.5">Màu sắc nhận diện</label>
          <div class="flex items-center gap-2">
            <button
              v-for="c in colorOptions"
              :key="c"
              type="button"
              @click="goalForm.color = c"
              :style="{ backgroundColor: c }"
              :class="[
                'w-6 h-6 rounded-full transition-all border-2',
                goalForm.color === c ? 'scale-110 border-slate-800' : 'border-transparent',
              ]"
            ></button>
          </div>
        </div>

        <div>
          <label class="block font-bold text-slate-700 mb-1">Mô tả / Ý nghĩa mục tiêu</label>
          <textarea
            v-model="goalForm.description"
            rows="2"
            placeholder="Kế hoạch tiết kiệm cá nhân, lý do đạt mục tiêu..."
            class="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500 resize-none"
          ></textarea>
        </div>

        <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
          <button
            type="button"
            @click="isGoalModalOpen = false"
            class="px-3.5 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-md"
          >
            Hủy
          </button>
          <button
            type="submit"
            :disabled="submitting"
            class="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 disabled:opacity-50 rounded-md shadow-xs"
          >
            <Save class="w-3.5 h-3.5" />
            <span>{{ isEditingGoal ? 'Lưu thay đổi' : 'Tạo mục tiêu' }}</span>
          </button>
        </div>
      </form>
    </AppModal>

    <!-- ==========================================
         MODAL: DEPOSIT / WITHDRAW MONEY TO GOAL
    ========================================== -->
    <AppModal
      v-model="isContributionModalOpen"
      :title="contributionType === 'DEPOSIT' ? `Tích lũy vào mục tiêu: ${targetGoalForContribute?.name}` : `Rút tiền khỏi mục tiêu: ${targetGoalForContribute?.name}`"
      size="sm"
    >
      <form @submit.prevent="submitContribution" class="space-y-4 text-xs">
        <div>
          <label class="block font-bold text-slate-700 mb-1">
            {{ contributionType === 'DEPOSIT' ? 'Trích tiền từ tài khoản / Ví *' : 'Rút tiền chuyển về tài khoản *' }}
          </label>
          <select
            v-model="contributionForm.accountId"
            required
            class="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
          >
            <option :value="undefined" disabled>-- Chọn tài khoản --</option>
            <option v-for="a in availableAccounts" :key="a.id" :value="a.id">
              {{ a.name }} (Số dư khả dụng: {{ formatCurrency(a.availableBalance) }})
            </option>
          </select>
        </div>

        <div>
          <label class="block font-bold text-slate-700 mb-1">Số tiền (VNĐ) *</label>
          <CurrencyInput
            v-model="contributionForm.amount"
            placeholder="Ví dụ: 1.000.000"
            :required="true"
          />
        </div>

        <div>
          <label class="block font-bold text-slate-700 mb-1">Ngày thực hiện *</label>
          <input
            v-model="contributionForm.contributionDate"
            type="datetime-local"
            required
            class="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div>
          <label class="block font-bold text-slate-700 mb-1">Ghi chú</label>
          <input
            v-model="contributionForm.note"
            type="text"
            placeholder="Ví dụ: Trích từ lương tháng 10, Tiết kiệm thêm..."
            class="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
          <button
            type="button"
            @click="isContributionModalOpen = false"
            class="px-3 py-1.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-md"
          >
            Hủy
          </button>
          <button
            type="submit"
            :disabled="submittingContribution"
            :class="[
              'inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold text-white rounded-md shadow-xs disabled:opacity-50',
              contributionType === 'DEPOSIT' ? 'bg-emerald-600 hover:bg-emerald-700' : 'bg-rose-600 hover:bg-rose-700',
            ]"
          >
            <Check class="w-4 h-4" />
            <span>{{ contributionType === 'DEPOSIT' ? 'Xác nhận tích lũy' : 'Xác nhận rút tiền' }}</span>
          </button>
        </div>
      </form>
    </AppModal>

    <!-- ==========================================
         MODAL: CONTRIBUTIONS HISTORY & DETAIL
    ========================================== -->
    <AppModal
      v-model="isHistoryModalOpen"
      :title="`Lịch sử tích lũy: ${targetGoalForHistory?.name}`"
      size="lg"
    >
      <div class="space-y-4">
        <!-- Goal current progress header -->
        <div class="p-3 bg-slate-50 border border-slate-200 rounded-md flex items-center justify-between text-xs">
          <div>
            <span class="text-slate-500">Đã tích lũy:</span>
            <strong class="text-sm font-bold text-emerald-600 ml-1.5">
              {{ formatCurrency(targetGoalForHistory?.currentAmount) }}
            </strong>
          </div>
          <div>
            <span class="text-slate-500">Mục tiêu:</span>
            <strong class="font-bold text-slate-800 ml-1.5">
              {{ formatCurrency(targetGoalForHistory?.targetAmount) }}
            </strong>
          </div>
          <div>
            <span class="text-slate-500">Tiến độ:</span>
            <strong class="font-bold text-brand-600 ml-1.5">
              {{ targetGoalForHistory?.progressPercentage.toFixed(1) }}%
            </strong>
          </div>
        </div>

        <!-- History Table -->
        <div v-if="historyLoading" class="py-12 flex items-center justify-center">
          <div class="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <div v-else-if="goalContributionsList.length === 0" class="py-10 text-center text-xs text-slate-400">
          Chưa có lần tích lũy hoặc rút tiền nào cho mục tiêu này.
        </div>

        <div v-else class="overflow-x-auto border border-slate-200 rounded-md">
          <table class="w-full text-left border-collapse text-xs">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold">
                <th class="py-2.5 px-3">Ngày</th>
                <th class="py-2.5 px-3">Loại</th>
                <th class="py-2.5 px-3">Tài khoản</th>
                <th class="py-2.5 px-3">Ghi chú</th>
                <th class="py-2.5 px-3 text-right">Số tiền</th>
                <th class="py-2.5 px-3 text-center">Xóa</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="c in goalContributionsList" :key="c.id" class="hover:bg-slate-50/60">
                <td class="py-2.5 px-3 text-slate-600 whitespace-nowrap">{{ formatDateTime(c.contributionDate) }}</td>
                <td class="py-2.5 px-3 whitespace-nowrap">
                  <span
                    :class="[
                      'px-1.5 py-0.5 rounded font-bold text-[10.5px]',
                      c.type === 'DEPOSIT' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700',
                    ]"
                  >
                    {{ c.type === 'DEPOSIT' ? 'Tích lũy' : 'Rút ra' }}
                  </span>
                </td>
                <td class="py-2.5 px-3 text-slate-700 font-medium whitespace-nowrap">{{ c.accountName }}</td>
                <td class="py-2.5 px-3 text-slate-500">{{ c.note || '-' }}</td>
                <td
                  :class="[
                    'py-2.5 px-3 text-right font-bold whitespace-nowrap',
                    c.type === 'DEPOSIT' ? 'text-emerald-600' : 'text-rose-600',
                  ]"
                >
                  {{ c.type === 'DEPOSIT' ? '+' : '-' }}{{ formatCurrency(c.amount) }}
                </td>
                <td class="py-2.5 px-3 text-center whitespace-nowrap">
                  <button
                    type="button"
                    @click="confirmDeleteGoalContribution(c)"
                    class="p-1 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded"
                    title="Xóa khoản này"
                  >
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </AppModal>

    <!-- Confirm Delete Goal Dialog -->
    <AppConfirmDialog
      v-model="isDeleteGoalOpen"
      title="Xác nhận xóa mục tiêu tài chính"
      :message="`Bạn có chắc chắn muốn xóa mục tiêu '${goalToDelete?.name}'? Toàn bộ lịch sử tích lũy của mục tiêu này sẽ bị xóa bỏ.`"
      confirm-text="Xóa mục tiêu"
      confirm-variant="danger"
      :loading="deletingGoal"
      @confirm="executeDeleteGoal"
    />

    <!-- Confirm Delete Contribution Dialog (Confirm base) -->
    <AppConfirmDialog
      v-model="isDeleteContributionOpen"
      title="Xác nhận xóa giao dịch tích lũy"
      :message="`Bạn có chắc chắn muốn xóa khoản ${contributionToDelete?.type === 'DEPOSIT' ? 'nạp' : 'rút'} ${formatCurrency(contributionToDelete?.amount)} này không? Số tiền trong mục tiêu sẽ tự động được cập nhật lại.`"
      confirm-text="Xóa khoản này"
      confirm-variant="danger"
      :loading="deletingContribution"
      @confirm="executeDeleteContribution"
    />

    <!-- Base Error Alert Dialog (Confirm err base) -->
    <AppConfirmDialog
      v-model="isErrorDialogOpen"
      type="error"
      :title="errorDialogTitle"
      :message="errorDialogMessage"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import {
  Plus,
  RefreshCw,
  Target,
  PiggyBank,
  Percent,
  PlusCircle,
  MinusCircle,
  History,
  Edit2,
  Trash2,
  Pause,
  Play,
  Save,
  Check,
  Info,
} from 'lucide-vue-next';
import AppModal from '../../../components/common/AppModal.vue';
import AppConfirmDialog from '../../../components/common/AppConfirmDialog.vue';
import CurrencyInput from '../../../components/common/CurrencyInput.vue';
import { useToastStore } from '../../../stores/toast.store';
import { financialService } from '../../../services/financial.service';
import type {
  FinancialGoal,
  FinancialGoalRequest,
  FinancialGoalContribution,
  FinancialGoalContributionRequest,
  FinancialAccount,
} from '../../../types';
import { formatCurrency, formatDate, formatDateTime, getGoalStatusLabel } from '../../../utils/finance';

const toastStore = useToastStore();

const emit = defineEmits<{
  (e: 'dataChanged'): void;
}>();

const loading = ref(false);
const submitting = ref(false);
const goals = ref<FinancialGoal[]>([]);
const availableAccounts = ref<FinancialAccount[]>([]);
const filterStatus = ref('');

const colorOptions = [
  '#2563eb', // blue
  '#10b981', // emerald
  '#f59e0b', // amber
  '#8b5cf6', // purple
  '#ec4899', // pink
  '#06b6d4', // cyan
  '#ef4444', // red
];

// Calculated total metrics
const totalTargetAmount = computed(() => {
  return goals.value.reduce((s, g) => s + (g.targetAmount || 0), 0);
});

const totalCurrentAmount = computed(() => {
  return goals.value.reduce((s, g) => s + (g.currentAmount || 0), 0);
});

const overallProgressPercentage = computed(() => {
  if (totalTargetAmount.value <= 0) return 0;
  return Math.min(100, (totalCurrentAmount.value / totalTargetAmount.value) * 100);
});

const filteredGoals = computed(() => {
  if (!filterStatus.value) return goals.value;
  return goals.value.filter((g) => g.status === filterStatus.value);
});

// ==========================================
// GOAL CRUD MODAL
// ==========================================
const isGoalModalOpen = ref(false);
const isEditingGoal = ref(false);
const editingGoalId = ref<number | null>(null);

const goalForm = ref<FinancialGoalRequest>({
  name: '',
  description: '',
  targetAmount: 10000000,
  startDate: new Date().toISOString().slice(0, 10),
  targetDate: null,
  color: '#2563eb',
});

// Delete Goal
const isDeleteGoalOpen = ref(false);
const goalToDelete = ref<FinancialGoal | null>(null);
const deletingGoal = ref(false);

// ==========================================
// CONTRIBUTIONS MODAL
// ==========================================
const isContributionModalOpen = ref(false);
const submittingContribution = ref(false);
const targetGoalForContribute = ref<FinancialGoal | null>(null);
const contributionType = ref<'DEPOSIT' | 'WITHDRAW'>('DEPOSIT');

const contributionForm = ref<FinancialGoalContributionRequest>({
  goalId: 0,
  accountId: 0,
  type: 'DEPOSIT',
  amount: 0,
  contributionDate: new Date().toISOString().slice(0, 16),
  note: '',
});

// History modal
const isHistoryModalOpen = ref(false);
const historyLoading = ref(false);
const targetGoalForHistory = ref<FinancialGoal | null>(null);
const goalContributionsList = ref<FinancialGoalContribution[]>([]);

async function loadGoals() {
  loading.value = true;
  try {
    const [goalsRes, accsRes] = await Promise.all([
      financialService.getGoals(),
      financialService.getAccounts(true),
    ]);
    goals.value = goalsRes;
    availableAccounts.value = accsRes;
  } catch (err) {
    console.error('Failed to load goals', err);
  } finally {
    loading.value = false;
  }
}

function openAddGoalModal() {
  isEditingGoal.value = false;
  editingGoalId.value = null;
  goalForm.value = {
    name: '',
    description: '',
    targetAmount: 10000000,
    startDate: new Date().toISOString().slice(0, 10),
    targetDate: null,
    color: '#2563eb',
  };
  isGoalModalOpen.value = true;
}

function openEditGoalModal(g: FinancialGoal) {
  isEditingGoal.value = true;
  editingGoalId.value = g.id;
  goalForm.value = {
    name: g.name,
    description: g.description || '',
    targetAmount: g.targetAmount,
    startDate: g.startDate,
    targetDate: g.targetDate || null,
    color: g.color || '#2563eb',
  };
  isGoalModalOpen.value = true;
}

async function saveGoal() {
  if (!goalForm.value.name.trim() || !goalForm.value.targetAmount) return;
  submitting.value = true;
  try {
    if (isEditingGoal.value && editingGoalId.value) {
      await financialService.updateGoal(editingGoalId.value, goalForm.value);
      toastStore.success('Cập nhật mục tiêu tài chính thành công');
    } else {
      await financialService.createGoal(goalForm.value);
      toastStore.success('Thêm mục tiêu tài chính thành công');
    }
    isGoalModalOpen.value = false;
    await loadGoals();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi xảy ra khi lưu mục tiêu', 'Lỗi lưu mục tiêu');
  } finally {
    submitting.value = false;
  }
}

async function changeGoalStatus(goalId: number, status: string) {
  try {
    await financialService.updateGoalStatus(goalId, status);
    toastStore.success('Đã cập nhật trạng thái mục tiêu');
    await loadGoals();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi khi cập nhật trạng thái', 'Lỗi cập nhật');
  }
}

function confirmDeleteGoal(g: FinancialGoal) {
  goalToDelete.value = g;
  isDeleteGoalOpen.value = true;
}

async function executeDeleteGoal() {
  if (!goalToDelete.value) return;
  deletingGoal.value = true;
  try {
    await financialService.deleteGoal(goalToDelete.value.id);
    toastStore.success('Đã xóa mục tiêu tài chính thành công');
    isDeleteGoalOpen.value = false;
    goalToDelete.value = null;
    await loadGoals();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Không thể xóa mục tiêu', 'Lỗi xóa mục tiêu');
  } finally {
    deletingGoal.value = false;
  }
}

// ==========================================
// CONTRIBUTIONS METHODS
// ==========================================
function openContributeModal(g: FinancialGoal, type: 'DEPOSIT' | 'WITHDRAW') {
  targetGoalForContribute.value = g;
  contributionType.value = type;

  const now = new Date();
  const localIso = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);

  contributionForm.value = {
    goalId: g.id,
    accountId: availableAccounts.value[0]?.id || 0,
    type,
    amount: type === 'DEPOSIT' ? Math.min(1000000, g.remainingAmount || 1000000) : 0,
    contributionDate: localIso,
    note: type === 'DEPOSIT' ? 'Tích lũy tiền' : 'Rút tiền',
  };
  isContributionModalOpen.value = true;
}

async function submitContribution() {
  if (!contributionForm.value.amount || contributionForm.value.amount <= 0) return;
  submittingContribution.value = true;
  try {
    await financialService.addGoalContribution(contributionForm.value);
    toastStore.success('Đã ghi nhận tích lũy/rút tiền thành công');
    isContributionModalOpen.value = false;
    await loadGoals();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi khi thực hiện đóng góp/rút tiền', 'Lỗi giao dịch');
  } finally {
    submittingContribution.value = false;
  }
}

async function openContributionsHistory(g: FinancialGoal) {
  targetGoalForHistory.value = g;
  isHistoryModalOpen.value = true;
  historyLoading.value = true;
  try {
    goalContributionsList.value = await financialService.getGoalContributions(g.id);
  } catch (err) {
    console.error('Failed to load contributions', err);
  } finally {
    historyLoading.value = false;
  }
}

const isDeleteContributionOpen = ref(false);
const contributionToDelete = ref<FinancialGoalContribution | null>(null);
const deletingContribution = ref(false);

function confirmDeleteGoalContribution(c: FinancialGoalContribution) {
  contributionToDelete.value = c;
  isDeleteContributionOpen.value = true;
}

async function executeDeleteContribution() {
  if (!contributionToDelete.value) return;
  deletingContribution.value = true;
  try {
    await financialService.deleteGoalContribution(contributionToDelete.value.id);
    toastStore.success('Đã xóa giao dịch tích lũy');
    isDeleteContributionOpen.value = false;
    contributionToDelete.value = null;
    if (targetGoalForHistory.value) {
      goalContributionsList.value = await financialService.getGoalContributions(targetGoalForHistory.value.id);
    }
    await loadGoals();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi khi xóa khoản đóng góp', 'Lỗi xóa giao dịch');
  } finally {
    deletingContribution.value = false;
  }
}

// Error Dialog state (Base confirm err)
const isErrorDialogOpen = ref(false);
const errorDialogTitle = ref('Thông báo lỗi');
const errorDialogMessage = ref('');

function showError(msg: string, title = 'Thông báo lỗi') {
  errorDialogTitle.value = title;
  errorDialogMessage.value = msg;
  isErrorDialogOpen.value = true;
  toastStore.error(msg);
}

onMounted(() => {
  loadGoals();
});

defineExpose({
  openAddModal: openAddGoalModal,
  refresh: loadGoals,
});
</script>
