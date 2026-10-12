<template>
  <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
    <!-- Header Banner -->
    <div class="p-5 sm:p-6 border-b border-slate-100 flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-emerald-50/40 via-white to-brand-50/30">
      <div class="flex items-start sm:items-center gap-3.5">
        <div class="w-11 h-11 rounded-md bg-emerald-600 text-white flex items-center justify-center shrink-0 shadow-xs">
          <Calculator class="w-6 h-6" />
        </div>
        <div>
          <div class="flex items-center gap-2 flex-wrap">
            <h3 class="text-base sm:text-lg font-bold text-slate-900 tracking-tight">
              Kế hoạch chi tiêu, Dự báo số dư ví & Đối soát mục tiêu
            </h3>
            <span class="px-2 py-0.5 text-[11px] font-bold rounded-full bg-emerald-100 text-emerald-800">
              Tháng {{ month }}/{{ year }}
            </span>
          </div>
          <p class="text-xs text-slate-500 mt-0.5">
            Dự kiến nguồn thu, phân bổ chi tiêu cho từng ví, đối soát tự động xem có đủ tiền tích lũy cho mục tiêu hay không
          </p>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex flex-wrap items-center gap-2 shrink-0">
        <button
          type="button"
          @click="openPlanIncomeModal"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs sm:text-sm font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-md transition-colors shadow-xs"
        >
          <Pencil class="w-3.5 h-3.5 text-slate-500" />
          <span>{{ budgetPlan?.expectedIncome ? 'Sửa thu nhập dự kiến' : 'Đặt thu nhập dự kiến' }}</span>
        </button>

        <button
          type="button"
          @click="openAddBudgetItemModal"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs sm:text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-md transition-colors shadow-xs"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm khoản dự chi</span>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="py-12 flex flex-col items-center justify-center text-center">
      <div class="w-8 h-8 border-3 border-emerald-500 border-t-transparent rounded-full animate-spin"></div>
      <span class="mt-3 text-xs text-slate-500 font-medium">Đang tính toán dòng tiền & kế hoạch...</span>
    </div>

    <div v-else class="p-5 sm:p-6 space-y-6">
      <!-- 4 Highlight Summary Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- 1. Thu nhập (Dự kiến hoặc Đồng bộ từ thực tế) -->
        <div class="p-4 rounded-md border border-slate-200/80 bg-slate-50/50 flex flex-col justify-between hover:bg-white hover:border-slate-300 transition-all">
          <div class="flex items-center justify-between text-xs font-semibold text-slate-500">
            <span v-if="budgetPlan?.isIncomeFromActual" class="text-emerald-700 font-bold flex items-center gap-1">
              Thu thực tế (Tự đồng bộ)
            </span>
            <span v-else>Thu nhập dự kiến (A)</span>
            <div class="flex items-center gap-2">
              <button
                type="button"
                @click="openPlanIncomeModal"
                class="text-brand-600 hover:text-brand-700 hover:underline text-[11px]"
                :title="budgetPlan?.isIncomeFromActual ? 'Đặt kế hoạch dự kiến riêng cho tháng này/sau' : 'Đổi thu nhập dự kiến'"
              >
                {{ budgetPlan?.isIncomeFromActual ? 'Đặt dự kiến' : 'Chỉnh sửa' }}
              </button>
              <button
                v-if="(budgetPlan?.expectedIncome || 0) > 0"
                type="button"
                @click="confirmDeletePlanIncome"
                class="text-rose-500 hover:text-rose-700 hover:underline text-[11px]"
                title="Xóa thu nhập dự kiến"
              >
                Xóa
              </button>
            </div>
          </div>
          <div class="mt-2.5">
            <span class="text-xl font-bold text-emerald-600 tracking-tight">
              {{ formatCurrency(budgetPlan?.effectiveIncome != null ? budgetPlan?.effectiveIncome : budgetPlan?.expectedIncome) }}
            </span>
            <p class="text-[11px] text-slate-500 mt-0.5 truncate">
              <span v-if="budgetPlan?.isIncomeFromActual" class="text-emerald-700 font-medium">
                Đồng bộ từ các giao dịch thu tháng {{ month }}
              </span>
              <span v-else-if="budgetPlan?.expectedIncomeAccountName" class="inline-flex items-center gap-1 text-slate-600">
                <Wallet class="w-3 h-3 text-emerald-600" />
                Về: {{ budgetPlan?.expectedIncomeAccountName }}
                <span v-if="(budgetPlan?.totalActualIncome || 0) > 0" class="text-slate-400">
                  (Đã thu: +{{ formatCurrency(budgetPlan?.totalActualIncome) }})
                </span>
              </span>
              <span v-else class="text-slate-400">
                {{ budgetPlan?.planNote || `Mục tiêu nguồn thu tháng ${month}` }}
                <span v-if="(budgetPlan?.totalActualIncome || 0) > 0" class="text-emerald-600 font-semibold ml-1">
                  (Đã thu: +{{ formatCurrency(budgetPlan?.totalActualIncome) }})
                </span>
              </span>
            </p>
          </div>
        </div>

        <!-- 2. Tổng dự chi -->
        <div class="p-4 rounded-md border border-slate-200/80 bg-slate-50/50 flex flex-col justify-between hover:bg-white hover:border-slate-300 transition-all">
          <div class="flex items-center justify-between text-xs font-semibold text-slate-500">
            <span>Tổng dự kiến chi (B)</span>
            <span class="text-[11px] font-bold text-slate-400">{{ budgetPlan?.items?.length || 0 }} khoản</span>
          </div>
          <div class="mt-2.5">
            <span class="text-xl font-bold text-slate-800 tracking-tight">
              {{ formatCurrency(budgetPlan?.totalPlannedExpense) }}
            </span>
            <p class="text-[11px] text-slate-400 mt-0.5">
              Phân bổ chi tiêu trong tháng
            </p>
          </div>
        </div>

        <!-- 3. DỰ KIẾN SẼ DƯ - HERO METRIC -->
        <div
          :class="[
            'p-4 rounded-md border flex flex-col justify-between transition-all shadow-xs',
            (budgetPlan?.expectedSurplus || 0) >= 0
              ? 'border-emerald-200 bg-emerald-50/60'
              : 'border-rose-200 bg-rose-50/60',
          ]"
        >
          <div class="flex items-center justify-between text-xs font-bold">
            <span :class="(budgetPlan?.expectedSurplus || 0) >= 0 ? 'text-emerald-800' : 'text-rose-800'">
              {{ budgetPlan?.isIncomeFromActual ? 'DỰ KIẾN DƯ (THỰC THU - DỰ CHI)' : 'DỰ KIẾN SẼ DƯ (A - B)' }}
            </span>
            <Sparkles v-if="(budgetPlan?.expectedSurplus || 0) >= 0" class="w-4 h-4 text-emerald-600" />
            <AlertTriangle v-else class="w-4 h-4 text-rose-600" />
          </div>
          <div class="mt-2.5">
            <span
              :class="[
                'text-xl font-extrabold tracking-tight',
                (budgetPlan?.expectedSurplus || 0) >= 0 ? 'text-emerald-700' : 'text-rose-600',
              ]"
            >
              {{ (budgetPlan?.expectedSurplus || 0) >= 0 ? '+' : '' }}{{ formatCurrency(budgetPlan?.expectedSurplus) }}
            </span>
            <p
              :class="[
                'text-[11px] font-medium mt-0.5',
                (budgetPlan?.expectedSurplus || 0) >= 0 ? 'text-emerald-700/80' : 'text-rose-700/80',
              ]"
            >
              {{ (budgetPlan?.expectedSurplus || 0) >= 0 ? 'Khả năng tích lũy cuối tháng' : 'Cảnh báo chi vượt nguồn thu!' }}
            </p>
          </div>
        </div>

        <!-- 4. Dự báo tổng tài sản ví sau chi tiêu -->
        <div class="p-4 rounded-md border border-slate-200/80 bg-slate-50/50 flex flex-col justify-between hover:bg-white hover:border-slate-300 transition-all">
          <div class="flex items-center justify-between text-xs font-semibold text-slate-500">
            <span>Dự báo tổng ví sau chi</span>
            <span class="text-[11px] font-bold text-indigo-600">Dòng tiền ví</span>
          </div>
          <div class="mt-2.5">
            <span class="text-xl font-bold text-indigo-700 tracking-tight">
              {{ formatCurrency(budgetPlan?.projectedTotalWalletBalance) }}
            </span>
            <p class="text-[11px] text-slate-400 mt-0.5 truncate">
              Hiện tại: <span class="font-semibold text-slate-600">{{ formatCurrency(budgetPlan?.totalCurrentWalletBalance) }}</span>
            </p>
          </div>
        </div>
      </div>

      <!-- SECTION 1: Dự báo biến động số dư từng Ví (Account Cashflow Projections) -->
      <div v-if="budgetPlan?.accountForecasts && budgetPlan.accountForecasts.length > 0" class="border border-slate-200 rounded-md overflow-hidden bg-white">
        <div class="p-3.5 bg-slate-50/80 border-b border-slate-200 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <Wallet class="w-4 h-4 text-brand-600" />
            <h4 class="text-xs sm:text-sm font-bold text-slate-800">
              Dự báo biến động số dư từng ví & tiền mặt ({{ budgetPlan.accountForecasts.length }} nguồn tiền)
            </h4>
          </div>
          <span class="text-xs text-slate-500 hidden sm:inline">
            Số dư hiện tại ➔ Dự chi từ ví ➔ Dự kiến số dư còn lại
          </span>
        </div>

        <div class="p-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
          <div
            v-for="acc in budgetPlan.accountForecasts"
            :key="acc.accountId"
            :class="[
              'p-3.5 rounded-md border transition-all flex flex-col justify-between',
              acc.isDeficit
                ? 'border-rose-300 bg-rose-50/30'
                : 'border-slate-200 bg-slate-50/30 hover:bg-white hover:border-slate-300',
            ]"
          >
            <div>
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-1.5 min-w-0">
                  <Banknote v-if="acc.accountType === 'CASH'" class="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                  <Landmark v-else-if="acc.accountType === 'BANK'" class="w-3.5 h-3.5 text-blue-600 shrink-0" />
                  <Smartphone v-else-if="acc.accountType === 'EWALLET'" class="w-3.5 h-3.5 text-pink-600 shrink-0" />
                  <CreditCard v-else class="w-3.5 h-3.5 text-purple-600 shrink-0" />
                  <span class="text-xs font-bold text-slate-800 truncate">{{ acc.accountName }}</span>
                </div>
                <span
                  v-if="acc.isDeficit"
                  class="px-1.5 py-0.5 text-[10px] font-bold rounded bg-rose-100 text-rose-700"
                >
                  Cảnh báo thiếu tiền
                </span>
                <span
                  v-else-if="acc.plannedExpense > 0"
                  class="text-[11px] font-medium text-slate-400"
                >
                  Dự chi: -{{ formatCurrency(acc.plannedExpense) }}
                </span>
                <span v-else class="text-[11px] text-slate-400">Không dự chi</span>
              </div>

              <!-- Metrics -->
              <div class="space-y-1 text-xs">
                <div class="flex items-center justify-between text-slate-500">
                  <span>Số dư hiện tại:</span>
                  <span class="font-semibold text-slate-700">{{ formatCurrency(acc.currentBalance) }}</span>
                </div>
                <div v-if="(acc.actualIncome || 0) > 0" class="flex items-center justify-between text-slate-500">
                  <span>Đã thu về ví:</span>
                  <span class="font-semibold text-slate-700">+{{ formatCurrency(acc.actualIncome) }}</span>
                </div>
                <div v-if="(acc.remainingPlannedIncome || 0) > 0" class="flex items-center justify-between text-emerald-600">
                  <span>{{ (acc.actualIncome || 0) > 0 ? 'Dự kiến còn thu:' : 'Thu nhập đổ về ví:' }}</span>
                  <span class="font-semibold">+{{ formatCurrency(acc.remainingPlannedIncome) }}</span>
                </div>
                <div v-if="(acc.actualSpent || 0) > 0" class="flex items-center justify-between text-slate-500">
                  <span>Đã chi trong tháng:</span>
                  <span class="font-semibold text-slate-700">-{{ formatCurrency(acc.actualSpent) }}</span>
                </div>
                <div v-if="acc.plannedExpense > 0" class="flex items-center justify-between text-rose-600">
                  <span>{{ (acc.actualSpent || 0) > 0 ? 'Dự kiến còn phải chi:' : 'Dự kiến chi từ ví:' }}</span>
                  <span class="font-semibold">-{{ formatCurrency(acc.remainingPlannedExpense ?? acc.plannedExpense) }}</span>
                </div>
              </div>
            </div>

            <!-- Projected balance footer -->
            <div class="mt-3 pt-2 border-t border-slate-200/60 flex items-center justify-between text-xs">
              <span class="font-medium text-slate-600">Dự kiến còn lại:</span>
              <span
                :class="[
                  'font-bold',
                  acc.isDeficit ? 'text-rose-600 text-sm' : 'text-slate-900',
                ]"
              >
                {{ formatCurrency(acc.projectedBalance) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- SECTION 3: Budget Items Table / Breakdown -->
      <div class="border border-slate-200 rounded-md overflow-hidden">
        <div class="p-4 bg-slate-50/80 border-b border-slate-200 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <Coins class="w-4 h-4 text-emerald-600" />
            <h4 class="text-xs sm:text-sm font-bold text-slate-800">
              Chi tiết các khoản phân bổ dự chi ({{ budgetPlan?.items?.length || 0 }})
            </h4>
          </div>
          <span class="text-xs text-slate-500 hidden sm:inline">
            Đối sánh kế hoạch vs Tiền thực tế đã tiêu
          </span>
        </div>

        <!-- Empty State -->
        <div
          v-if="!budgetPlan?.items || budgetPlan.items.length === 0"
          class="py-12 px-4 flex flex-col items-center justify-center text-center bg-white"
        >
          <div class="w-12 h-12 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center mb-3">
            <Coins class="w-6 h-6" />
          </div>
          <p class="text-sm font-bold text-slate-700">Chưa thiết lập khoản dự kiến chi nào</p>
          <p class="text-xs text-slate-400 max-w-sm mt-1">
            Hãy thêm các khoản bạn dự kiến tiêu trong tháng này (như Ăn uống, Tiền nhà, Đi lại...) và chọn ví chi tiền để hệ thống tự động tính toán.
          </p>
          <button
            type="button"
            @click="openAddBudgetItemModal"
            class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 text-xs sm:text-sm font-bold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 rounded-md transition-colors"
          >
            <Plus class="w-4 h-4" />
            <span>+ Lên kế hoạch khoản chi đầu tiên</span>
          </button>
        </div>

        <!-- Table View -->
        <div v-else class="overflow-x-auto bg-white">
          <table class="w-full text-left border-collapse text-xs sm:text-sm">
            <thead>
              <tr class="border-b border-slate-200 bg-slate-50/40 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                <th class="py-3 px-4">Khoản chi tiêu</th>
                <th class="py-3 px-4">Chi từ ví</th>
                <th class="py-3 px-4 text-right">Dự kiến chi</th>
                <th class="py-3 px-4 text-right">Đã chi thực tế</th>
                <th class="py-3 px-4 text-right">Còn lại / Vượt</th>
                <th class="py-3 px-4 min-w-[130px]">Tiến độ sử dụng</th>
                <th class="py-3 px-4 text-center w-20">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="item in budgetPlan.items"
                :key="item.id"
                class="hover:bg-slate-50/60 transition-colors"
              >
                <!-- Category Name & Color -->
                <td class="py-3 px-4">
                  <div class="flex items-center gap-2.5">
                    <span
                      class="w-3 h-3 rounded-full shrink-0"
                      :style="{ backgroundColor: item.categoryColor || '#10b981' }"
                    ></span>
                    <div>
                      <span class="font-bold text-slate-800">{{ item.categoryName }}</span>
                      <p v-if="item.note" class="text-[11px] text-slate-400 font-normal truncate max-w-xs">
                        {{ item.note }}
                      </p>
                    </div>
                  </div>
                </td>

                <!-- Account / Wallet Badge -->
                <td class="py-3 px-4">
                  <span
                    :class="[
                      'inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-[11px] font-semibold',
                      item.accountId ? 'bg-indigo-50 text-indigo-700' : 'bg-slate-100 text-slate-600',
                    ]"
                  >
                    <Banknote v-if="item.accountType === 'CASH'" class="w-3 h-3 text-emerald-600 shrink-0" />
                    <Landmark v-else-if="item.accountType === 'BANK'" class="w-3 h-3 text-blue-600 shrink-0" />
                    <Smartphone v-else-if="item.accountType === 'EWALLET'" class="w-3 h-3 text-pink-600 shrink-0" />
                    <CreditCard v-else-if="item.accountType === 'CREDIT'" class="w-3 h-3 text-purple-600 shrink-0" />
                    <Wallet v-else class="w-3 h-3 text-slate-500 shrink-0" />
                    <span>{{ item.accountName || 'Tất cả ví' }}</span>
                  </span>
                </td>

                <!-- Planned Amount -->
                <td class="py-3 px-4 text-right font-bold text-slate-800">
                  {{ formatCurrency(item.plannedAmount) }}
                </td>

                <!-- Actual Spent -->
                <td class="py-3 px-4 text-right font-medium text-rose-600">
                  {{ formatCurrency(item.actualSpent) }}
                </td>

                <!-- Remaining / Exceeded -->
                <td class="py-3 px-4 text-right">
                  <span
                    v-if="!item.isExceeded"
                    class="font-medium text-slate-600"
                  >
                    Còn {{ formatCurrency(item.remainingAmount) }}
                  </span>
                  <span
                    v-else
                    class="font-bold text-rose-600 flex items-center justify-end gap-1"
                  >
                    <AlertTriangle class="w-3.5 h-3.5 text-rose-500 shrink-0" />
                    <span>Vượt {{ formatCurrency(Math.abs(item.remainingAmount)) }}</span>
                  </span>
                </td>

                <!-- Progress Bar -->
                <td class="py-3 px-4">
                  <div class="space-y-1">
                    <div class="flex items-center justify-between text-[11px]">
                      <span
                        :class="[
                          'font-bold',
                          item.percentageSpent >= 100
                            ? 'text-rose-600'
                            : item.percentageSpent >= 75
                            ? 'text-amber-600'
                            : 'text-emerald-600',
                        ]"
                      >
                        {{ item.percentageSpent.toFixed(1) }}%
                      </span>
                      <span
                        v-if="item.isExceeded"
                        class="px-1.5 py-0.2 text-[10px] font-bold rounded bg-rose-100 text-rose-700"
                      >
                        Bội chi
                      </span>
                    </div>
                    <div class="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                      <div
                        :class="[
                          'h-full rounded-full transition-all duration-300',
                          item.percentageSpent >= 100
                            ? 'bg-rose-500'
                            : item.percentageSpent >= 75
                            ? 'bg-amber-500'
                            : 'bg-emerald-500',
                        ]"
                        :style="{ width: `${Math.min(100, item.percentageSpent)}%` }"
                      ></div>
                    </div>
                  </div>
                </td>

                <!-- Actions -->
                <td class="py-3 px-4 text-center">
                  <div class="flex items-center justify-center gap-1.5">
                    <button
                      type="button"
                      @click="openEditBudgetItemModal(item)"
                      class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded transition-colors"
                      title="Chỉnh sửa hạn mức"
                    >
                      <Pencil class="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      @click="confirmDeleteBudgetItem(item)"
                      class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors"
                      title="Xóa khoản dự chi"
                    >
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- MODAL 1: Thiết lập Thu nhập dự kiến -->
    <AppModal
      v-model="showPlanIncomeModal"
      title="Thiết lập Thu nhập dự kiến tháng"
      size="lg"
    >
      <form @submit.prevent="submitPlanIncome" class="space-y-4 sm:space-y-5 py-1">
        <div class="p-3.5 rounded-md bg-emerald-50/70 border border-emerald-100 text-xs sm:text-sm text-emerald-800 leading-relaxed">
          Nhập số tiền thu nhập bạn dự kiến kiếm được trong <strong>Tháng {{ month }}/{{ year }}</strong> và chọn ví nhận tiền. Hệ thống sẽ tính số dư dự kiến của ví đó và đối soát với các mục tiêu tài chính.
        </div>

        <CurrencyInput
          v-model="planIncomeForm.expectedIncome"
          label="Thu nhập dự kiến tháng (₫)"
          placeholder="Nhập số tiền kỳ vọng..."
          :required="true"
          :min="0"
          :quick-amounts="[5000000, 10000000, 15000000, 20000000, 30000000, 50000000]"
        />

        <!-- Chọn ví nhận tiền -->
        <AccountSelect
          v-model="planIncomeForm.accountId"
          :accounts="allAccounts"
          label="Ví / Tài khoản nhận tiền (tùy chọn)"
          placeholder="-- Không chỉ định ví (Tính tổng chung) --"
        />

        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
            Ghi chú kế hoạch (tùy chọn)
          </label>
          <textarea
            v-model="planIncomeForm.note"
            rows="3"
            placeholder="Ví dụ: Lương cứng + Thưởng dự án tháng này..."
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-colors shadow-2xs text-slate-800 resize-none"
          ></textarea>
        </div>

        <div class="flex items-center justify-between gap-3 pt-4 border-t border-slate-200">
          <div>
            <button
              v-if="(budgetPlan?.expectedIncome || 0) > 0"
              type="button"
              @click="confirmDeletePlanIncome"
              class="px-4 py-2.5 text-xs sm:text-sm font-semibold text-rose-600 hover:text-rose-700 hover:bg-rose-50 rounded-md transition-colors cursor-pointer"
            >
              Xóa thu nhập dự kiến
            </button>
          </div>
          <div class="flex items-center gap-3">
            <AppButton
              variant="secondary"
              size="md"
              @click="showPlanIncomeModal = false"
            >
              Hủy
            </AppButton>
            <AppButton
              variant="primary"
              size="md"
              type="submit"
              :loading="savingIncome"
            >
              Lưu thu nhập dự kiến
            </AppButton>
          </div>
        </div>
      </form>
    </AppModal>

    <!-- MODAL 2: Thêm / Sửa khoản dự kiến chi -->
    <AppModal
      v-model="showBudgetItemModal"
      :title="isEditingItem ? 'Chỉnh sửa khoản dự chi' : 'Thêm khoản dự kiến chi'"
      size="lg"
    >
      <form @submit.prevent="submitBudgetItem" class="space-y-4 sm:space-y-5 py-1">
        <!-- Danh mục chi tiêu -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
            Khoản chi tiêu (Danh mục) <span class="text-rose-500">*</span>
          </label>
          <select
            v-model="budgetItemForm.categoryId"
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-colors shadow-2xs text-slate-800"
            required
          >
            <option :value="0" disabled>-- Chọn danh mục chi tiêu --</option>
            <option
              v-for="cat in expenseCategories"
              :key="cat.id"
              :value="cat.id"
            >
              {{ cat.name }}
            </option>
          </select>
          <p v-if="expenseCategories.length === 0" class="text-[11px] text-amber-600 mt-1">
            Chưa có danh mục chi tiêu nào. Bạn hãy tạo danh mục ở tab "Danh mục thu chi".
          </p>
        </div>

        <!-- Chọn ví chi tiền -->
        <AccountSelect
          v-model="budgetItemForm.accountId"
          :accounts="allAccounts"
          label="Dự kiến chi từ ví / nguồn tiền nào? (Tiền mặt, Ngân hàng...)"
          placeholder="-- Chọn nguồn tiền (Tiền mặt hoặc Ngân hàng) --"
          :required="true"
        />

        <!-- Số tiền dự kiến -->
        <CurrencyInput
          v-model="budgetItemForm.plannedAmount"
          label="Số tiền dự kiến tiêu (₫)"
          placeholder="Nhập số tiền dự kiến..."
          :required="true"
          :min="1000"
          :quick-amounts="[500000, 1000000, 2000000, 3000000, 5000000, 10000000]"
        />

        <!-- Ghi chú khoản chi -->
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
            Ghi chú (tùy chọn)
          </label>
          <input
            v-model="budgetItemForm.note"
            type="text"
            placeholder="Ví dụ: Giới hạn tiền ăn trưa, tiền trọ..."
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-colors shadow-2xs text-slate-800"
          />
        </div>

        <div class="flex items-center justify-end gap-3 pt-4 border-t border-slate-200">
          <AppButton
            variant="secondary"
            size="md"
            @click="showBudgetItemModal = false"
          >
            Hủy
          </AppButton>
          <AppButton
            variant="primary"
            size="md"
            type="submit"
            :loading="savingItem"
          >
            {{ isEditingItem ? 'Cập nhật' : 'Thêm khoản dự chi' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- Confirm Dialog: Xóa khoản dự chi -->
    <AppConfirmDialog
      v-model="showConfirmDelete"
      title="Xác nhận xóa khoản dự chi"
      :message="deleteMessage"
      confirm-text="Xóa khoản này"
      cancel-text="Hủy"
      confirm-variant="danger"
      :loading="deletingItem"
      @confirm="executeDeleteItem"
    />

    <!-- Confirm Dialog: Xóa thu nhập dự kiến -->
    <AppConfirmDialog
      v-model="showConfirmDeletePlan"
      title="Xác nhận xóa Thu nhập dự kiến"
      :message="`Bạn có chắc chắn muốn xóa Thu nhập dự kiến (${formatCurrency(budgetPlan?.expectedIncome)}) của Tháng ${month}/${year}? Số tiền thu nhập dự kiến sẽ được đưa về 0 ₫.`"
      confirm-text="Xóa thu nhập"
      cancel-text="Hủy"
      confirm-variant="danger"
      :loading="deletingPlan"
      @confirm="executeDeletePlanIncome"
    />

    <!-- Error Alert Dialog -->
    <AppConfirmDialog
      v-model="showErrorDialog"
      type="error"
      title="Thông báo lỗi"
      :message="errorMessage"
      confirm-text="Đóng"
      :show-cancel="false"
      @confirm="showErrorDialog = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import {
  Calculator,
  Plus,
  Pencil,
  Trash2,
  Sparkles,
  AlertTriangle,
  Coins,
  Wallet,
  Banknote,
  Landmark,
  Smartphone,
  CreditCard,
} from 'lucide-vue-next';
import AppModal from '../../../components/common/AppModal.vue';
import AppButton from '../../../components/common/AppButton.vue';
import AppConfirmDialog from '../../../components/common/AppConfirmDialog.vue';
import CurrencyInput from '../../../components/common/CurrencyInput.vue';
import AccountSelect from './AccountSelect.vue';
import { financialService } from '../../../services/financial.service';
import type {
  MonthlyBudgetPlan,
  FinancialBudgetItem,
  FinancialCategory,
  FinancialAccount,
} from '../../../types';
import { formatCurrency } from '../../../utils/finance';

const props = defineProps<{
  month: number;
  year: number;
}>();

const emit = defineEmits<{
  (e: 'dataChanged'): void;
}>();

const loading = ref(false);
const budgetPlan = ref<MonthlyBudgetPlan | null>(null);
const allCategories = ref<FinancialCategory[]>([]);
const allAccounts = ref<FinancialAccount[]>([]);

// Expense Categories for dropdown
const expenseCategories = computed(() => {
  return allCategories.value.filter((c) => c.type === 'EXPENSE' && c.isActive);
});

// Modal 1: Plan Income State
const showPlanIncomeModal = ref(false);
const savingIncome = ref(false);
const planIncomeForm = ref<{
  expectedIncome: number;
  accountId: number | null;
  note: string;
}>({
  expectedIncome: 0,
  accountId: null,
  note: '',
});

// Modal 2: Budget Item State
const showBudgetItemModal = ref(false);
const isEditingItem = ref(false);
const savingItem = ref(false);
const editingItemId = ref<number | null>(null);
const budgetItemForm = ref<{
  categoryId: number;
  accountId: number | null;
  plannedAmount: number;
  note: string;
}>({
  categoryId: 0,
  accountId: null,
  plannedAmount: 0,
  note: '',
});

// Delete Confirmation State
const showConfirmDelete = ref(false);
const deletingItem = ref(false);
const itemToDelete = ref<FinancialBudgetItem | null>(null);
const deleteMessage = computed(() => {
  if (!itemToDelete.value) return '';
  return `Bạn có chắc chắn muốn xóa khoản dự chi "${itemToDelete.value.categoryName}" (${formatCurrency(itemToDelete.value.plannedAmount)}) khỏi kế hoạch Tháng ${props.month}/${props.year}?`;
});

const showConfirmDeletePlan = ref(false);
const deletingPlan = ref(false);

// Error State
const showErrorDialog = ref(false);
const errorMessage = ref('');

function showError(msg: string) {
  errorMessage.value = msg;
  showErrorDialog.value = true;
}

// Fetch Budget Plan Data
async function fetchBudgetPlan() {
  loading.value = true;
  try {
    const data = await financialService.getBudgetPlan(props.month, props.year);
    budgetPlan.value = data;
  } catch (err: any) {
    console.error('Failed to load budget plan', err);
  } finally {
    loading.value = false;
  }
}

// Fetch Categories & Accounts
async function fetchMasterData() {
  try {
    const [cats, accs] = await Promise.all([
      financialService.getCategories('EXPENSE', true),
      financialService.getAccounts(true),
    ]);
    allCategories.value = cats;
    allAccounts.value = accs;
  } catch (err) {
    console.error('Failed to load master data', err);
  }
}

// ==========================================
// PLAN INCOME ACTIONS
// ==========================================
function openPlanIncomeModal() {
  planIncomeForm.value = {
    expectedIncome: budgetPlan.value?.expectedIncome || 0,
    accountId: budgetPlan.value?.expectedIncomeAccountId || null,
    note: budgetPlan.value?.planNote || '',
  };
  showPlanIncomeModal.value = true;
}

async function submitPlanIncome() {
  savingIncome.value = true;
  try {
    await financialService.saveMonthlyPlan({
      month: props.month,
      year: props.year,
      expectedIncome: planIncomeForm.value.expectedIncome || 0,
      accountId: planIncomeForm.value.accountId,
      note: planIncomeForm.value.note?.trim() || undefined,
    });
    showPlanIncomeModal.value = false;
    await fetchBudgetPlan();
    emit('dataChanged');
  } catch (err: any) {
    showError(err.response?.data?.message || 'Không thể lưu thu nhập dự kiến');
  } finally {
    savingIncome.value = false;
  }
}

function confirmDeletePlanIncome() {
  showConfirmDeletePlan.value = true;
}

async function executeDeletePlanIncome() {
  deletingPlan.value = true;
  try {
    await financialService.deleteMonthlyPlan(props.month, props.year);
    showConfirmDeletePlan.value = false;
    showPlanIncomeModal.value = false;
    await fetchBudgetPlan();
    emit('dataChanged');
  } catch (err: any) {
    showError(err.response?.data?.message || 'Không thể xóa thu nhập dự kiến');
  } finally {
    deletingPlan.value = false;
  }
}

// ==========================================
// BUDGET ITEM ACTIONS
// ==========================================
function openAddBudgetItemModal() {
  isEditingItem.value = false;
  editingItemId.value = null;
  budgetItemForm.value = {
    categoryId: expenseCategories.value[0]?.id || 0,
    accountId: allAccounts.value[0]?.id || null,
    plannedAmount: 0,
    note: '',
  };
  showBudgetItemModal.value = true;
}

function openEditBudgetItemModal(item: FinancialBudgetItem) {
  isEditingItem.value = true;
  editingItemId.value = item.id;
  budgetItemForm.value = {
    categoryId: item.categoryId,
    accountId: item.accountId || (allAccounts.value[0]?.id || null),
    plannedAmount: item.plannedAmount,
    note: item.note || '',
  };
  showBudgetItemModal.value = true;
}

async function submitBudgetItem() {
  if (!budgetItemForm.value.categoryId) {
    showError('Vui lòng chọn danh mục chi tiêu');
    return;
  }
  if (!budgetItemForm.value.accountId) {
    showError('Vui lòng chọn ví thanh toán cho khoản dự chi');
    return;
  }
  if (!budgetItemForm.value.plannedAmount || budgetItemForm.value.plannedAmount <= 0) {
    showError('Vui lòng nhập số tiền dự kiến chi lớn hơn 0');
    return;
  }

  // Chặn cứng: Kiểm tra số dư ví hiện tại
  const selectedAccount = allAccounts.value.find((a) => a.id === budgetItemForm.value.accountId);
  const currentBalance = selectedAccount?.currentBalance ?? 0;

  // Tính tổng dự chi của ví này trong tháng (loại trừ khoản đang sửa nếu có)
  const otherPlannedForAccount = (budgetPlan.value?.items || [])
    .filter((item) => item.accountId === budgetItemForm.value.accountId && item.id !== editingItemId.value)
    .reduce((sum, item) => sum + (item.plannedAmount || 0), 0);

  const newTotalPlanned = otherPlannedForAccount + (budgetItemForm.value.plannedAmount || 0);

  if (newTotalPlanned > currentBalance) {
    if (currentBalance <= 0) {
      showError(`Ví "${selectedAccount?.name || 'này'}" hiện không có tiền (${formatCurrency(currentBalance)}), không thể lập khoản dự chi!`);
    } else {
      showError(
        `Số dư ví "${selectedAccount?.name}" không đủ! Hiện có ${formatCurrency(currentBalance)}, nhưng tổng dự kiến chi từ ví này sẽ là ${formatCurrency(newTotalPlanned)}.`
      );
    }
    return;
  }

  savingItem.value = true;
  try {
    await financialService.saveBudgetItem({
      id: editingItemId.value || undefined,
      month: props.month,
      year: props.year,
      categoryId: budgetItemForm.value.categoryId,
      accountId: budgetItemForm.value.accountId,
      plannedAmount: budgetItemForm.value.plannedAmount,
      note: budgetItemForm.value.note?.trim() || undefined,
    });
    showBudgetItemModal.value = false;
    await fetchBudgetPlan();
    emit('dataChanged');
  } catch (err: any) {
    showError(err.response?.data?.message || 'Không thể lưu khoản dự chi');
  } finally {
    savingItem.value = false;
  }
}

function confirmDeleteBudgetItem(item: FinancialBudgetItem) {
  itemToDelete.value = item;
  showConfirmDelete.value = true;
}

async function executeDeleteItem() {
  if (!itemToDelete.value) return;
  deletingItem.value = true;
  try {
    await financialService.deleteBudgetItem(itemToDelete.value.id);
    showConfirmDelete.value = false;
    itemToDelete.value = null;
    await fetchBudgetPlan();
    emit('dataChanged');
  } catch (err: any) {
    showError(err.response?.data?.message || 'Không thể xóa khoản dự chi');
  } finally {
    deletingItem.value = false;
  }
}

// Watch month and year changes
watch(
  () => [props.month, props.year],
  () => {
    fetchBudgetPlan();
  }
);

onMounted(() => {
  fetchBudgetPlan();
  fetchMasterData();
});

defineExpose({
  refresh: () => {
    fetchBudgetPlan();
    fetchMasterData();
  },
});
</script>
