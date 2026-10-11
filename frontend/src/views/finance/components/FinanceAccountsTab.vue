<template>
  <div class="space-y-6">
    <!-- Top Summary Banner -->
    <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
      <div>
        <h3 class="text-base font-bold text-slate-900">Quản lý ví & Tài khoản tài chính</h3>
        <p class="text-xs text-slate-500 mt-0.5">
          Theo dõi số dư thực tế, phân bổ tiền vào mục tiêu và dòng tiền từng tài khoản
        </p>
      </div>

      <div class="flex items-center gap-3 w-full md:w-auto">
        <button
          type="button"
          @click="openAddAccountModal"
          class="inline-flex items-center justify-center gap-1.5 px-4 py-2 text-xs sm:text-sm font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-md transition-colors shadow-xs"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm tài khoản mới</span>
        </button>

        <button
          type="button"
          @click="loadAccounts"
          :disabled="loading"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs sm:text-sm font-medium text-slate-700 bg-white border border-slate-200 rounded-md hover:bg-slate-50 transition-colors shadow-xs"
          title="Làm mới"
        >
          <RefreshCw :class="['w-4 h-4 text-slate-500', loading ? 'animate-spin' : '']" />
          <span>Làm mới</span>
        </button>
      </div>
    </div>

    <!-- Quick Asset Breakdown Cards -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
      <div class="bg-gradient-to-br from-indigo-600 to-indigo-700 text-white p-5 rounded-md shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-indigo-100 uppercase tracking-wider">Tổng số dư toàn bộ ví</span>
          <Wallet class="w-5 h-5 text-indigo-200" />
        </div>
        <div class="mt-4">
          <span class="text-2xl font-bold tracking-tight">{{ formatCurrency(totalBalance) }}</span>
          <p class="text-[11px] text-indigo-200 mt-1">Bao gồm tất cả tài khoản đang hoạt động</p>
        </div>
      </div>

      <div class="bg-gradient-to-br from-amber-500 to-amber-600 text-white p-5 rounded-md shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-amber-100 uppercase tracking-wider">Đã phân bổ cho mục tiêu</span>
          <Target class="w-5 h-5 text-amber-200" />
        </div>
        <div class="mt-4">
          <span class="text-2xl font-bold tracking-tight">{{ formatCurrency(totalAllocated) }}</span>
          <p class="text-[11px] text-amber-200 mt-1">Đã khóa cho các kế hoạch tiết kiệm</p>
        </div>
      </div>

      <div class="bg-gradient-to-br from-emerald-600 to-emerald-700 text-white p-5 rounded-md shadow-xs flex flex-col justify-between">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-emerald-100 uppercase tracking-wider">Số dư khả dụng</span>
          <CheckCircle2 class="w-5 h-5 text-emerald-200" />
        </div>
        <div class="mt-4">
          <span class="text-2xl font-bold tracking-tight">{{ formatCurrency(totalAvailable) }}</span>
          <p class="text-[11px] text-emerald-200 mt-1">Số dư tự do cho chi tiêu sinh hoạt</p>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading && accounts.length === 0" class="flex flex-col items-center justify-center py-16 bg-white rounded-md border border-slate-200">
      <div class="w-8 h-8 border-4 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      <span class="mt-3 text-xs font-medium text-slate-500">Đang tải danh sách tài khoản...</span>
    </div>

    <!-- Empty Accounts State -->
    <div
      v-else-if="accounts.length === 0"
      class="py-16 flex flex-col items-center justify-center text-center bg-white rounded-md border border-slate-200 p-6"
    >
      <CreditCard class="w-12 h-12 text-slate-300 mb-3" />
      <h3 class="text-sm font-bold text-slate-800">Chưa có tài khoản nào</h3>
      <p class="text-xs text-slate-500 max-w-sm mt-1">
        Hãy thêm các tài khoản như Tiền mặt, Techcombank, Vietcombank, Momo để bắt đầu quản lý dòng tiền.
      </p>
      <button
        type="button"
        @click="openAddAccountModal"
        class="mt-4 inline-flex items-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-brand-600 hover:bg-brand-700 rounded-md transition-colors shadow-xs"
      >
        <Plus class="w-4 h-4" />
        <span>Thêm tài khoản ngay</span>
      </button>
    </div>

    <!-- Accounts Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      <div
        v-for="acc in accounts"
        :key="acc.id"
        :class="[
          'relative p-5 rounded-md border transition-all flex flex-col justify-between overflow-hidden',
          acc.isActive
            ? 'bg-white border-slate-200 shadow-xs hover:border-brand-300 hover:shadow-sm'
            : 'bg-slate-50 border-slate-200/80 opacity-70',
        ]"
      >
        <!-- Card Top: Name & Badges -->
        <div>
          <div class="flex items-start justify-between gap-2">
            <div class="flex items-center gap-3">
              <div
                :class="[
                  'w-10 h-10 rounded-md flex items-center justify-center font-bold shrink-0',
                  acc.accountType === 'CASH'
                    ? 'bg-emerald-50 text-emerald-600'
                    : acc.accountType === 'BANK'
                    ? 'bg-blue-50 text-blue-600'
                    : acc.accountType === 'EWALLET'
                    ? 'bg-pink-50 text-pink-600'
                    : 'bg-purple-50 text-purple-600',
                ]"
              >
                <Banknote v-if="acc.accountType === 'CASH'" class="w-5 h-5" />
                <Landmark v-else-if="acc.accountType === 'BANK'" class="w-5 h-5" />
                <Smartphone v-else-if="acc.accountType === 'EWALLET'" class="w-5 h-5" />
                <CreditCard v-else class="w-5 h-5" />
              </div>
              <div class="truncate">
                <h4 class="text-sm font-bold text-slate-900 truncate">{{ acc.name }}</h4>
                <span class="text-[11px] font-medium text-slate-500">
                  {{ getAccountTypeLabel(acc.accountType) }}
                </span>
              </div>
            </div>

            <!-- Active / Inactive Badge -->
            <span
              :class="[
                'px-2 py-0.5 text-[10.5px] font-bold rounded-full border shrink-0',
                acc.isActive
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                  : 'bg-slate-100 text-slate-500 border-slate-200',
              ]"
            >
              {{ acc.isActive ? 'Đang dùng' : 'Ngừng dùng' }}
            </span>
          </div>

          <!-- Balance Details -->
          <div class="mt-4 pt-3 border-t border-slate-100 space-y-2">
            <div>
              <span class="text-[11px] text-slate-400 font-medium">Số dư thực tế</span>
              <p
                :class="[
                  'text-xl font-bold tracking-tight',
                  acc.currentBalance >= 0 ? 'text-slate-900' : 'text-rose-600',
                ]"
              >
                {{ formatCurrency(acc.currentBalance) }}
              </p>
            </div>

            <div class="grid grid-cols-2 gap-2 text-xs pt-2 border-t border-slate-100/70">
              <div>
                <span class="text-[10.5px] text-slate-400">Đã tích lũy mục tiêu:</span>
                <p class="font-semibold text-amber-600">{{ formatCompactCurrency(acc.allocatedToGoal) }}</p>
              </div>
              <div>
                <span class="text-[10.5px] text-slate-400">Số dư khả dụng:</span>
                <p class="font-semibold text-emerald-600">{{ formatCompactCurrency(acc.availableBalance) }}</p>
              </div>
            </div>

            <div class="text-[11px] text-slate-400 flex items-center justify-between pt-1">
              <span>Số dư ban đầu: {{ formatCompactCurrency(acc.initialBalance) }}</span>
            </div>
          </div>
        </div>

        <!-- Card Actions -->
        <div class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
          <button
            type="button"
            @click="viewAccountHistory(acc)"
            class="text-brand-600 hover:text-brand-700 font-semibold flex items-center gap-1 hover:underline"
          >
            <History class="w-3.5 h-3.5" />
            <span>Lịch sử giao dịch</span>
          </button>

          <div class="flex items-center gap-1">
            <button
              type="button"
              @click="openEditAccountModal(acc)"
              class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded transition-colors"
              title="Chỉnh sửa tài khoản"
            >
              <Edit2 class="w-3.5 h-3.5" />
            </button>
            <button
              type="button"
              @click="toggleAccountActive(acc)"
              class="p-1.5 text-slate-400 hover:text-amber-600 hover:bg-amber-50 rounded transition-colors"
              :title="acc.isActive ? 'Ngừng sử dụng tài khoản' : 'Kích hoạt lại tài khoản'"
            >
              <Power class="w-3.5 h-3.5" />
            </button>
            <button
              type="button"
              @click="confirmDeleteAccount(acc)"
              class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors"
              title="Xóa tài khoản"
            >
              <Trash2 class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ==========================================
         MODAL: ADD / EDIT ACCOUNT
    ========================================== -->
    <AppModal
      v-model="isModalOpen"
      :title="isEditing ? 'Chỉnh sửa tài khoản & Điều chỉnh số dư' : 'Thêm tài khoản mới'"
      size="lg"
    >
      <form @submit.prevent="saveAccount" class="space-y-4 sm:space-y-5 py-2">
        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Tên tài khoản / Ví <span class="text-rose-500">*</span></label>
          <input
            v-model="formData.name"
            type="text"
            required
            placeholder="Ví dụ: Techcombank, Tiền mặt, Ví Momo..."
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-900 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          />
        </div>

        <div>
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Loại tài khoản <span class="text-rose-500">*</span></label>
          <select
            v-model="formData.accountType"
            required
            class="w-full px-3.5 py-2.5 text-xs sm:text-sm bg-white border border-slate-200 rounded-md text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-colors shadow-2xs"
          >
            <option value="CASH">Tiền mặt</option>
            <option value="BANK">Tài khoản Ngân hàng</option>
            <option value="EWALLET">Ví điện tử (Momo/ZaloPay/ShopeePay)</option>
            <option value="CREDIT">Thẻ tín dụng</option>
            <option value="OTHER">Khác</option>
          </select>
        </div>

        <!-- WHEN CREATING NEW ACCOUNT -->
        <div v-if="!isEditing">
          <label class="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">Số dư ban đầu (VNĐ) <span class="text-rose-500">*</span></label>
          <CurrencyInput
            v-model="formData.initialBalance"
            placeholder="Ví dụ: 2.000.000"
            :required="true"
            :allow-zero="true"
            :quick-amounts="[500000, 1000000, 2000000, 5000000, 10000000]"
          />
        </div>

        <!-- WHEN EDITING: INTERACTIVE BALANCE ADJUSTMENT & LIVE CALCULATION -->
        <div v-else class="space-y-4 p-4 rounded-md border border-slate-200 bg-slate-50/70">
          <div class="flex items-center justify-between">
            <span class="text-xs sm:text-sm font-bold text-slate-800">Điều chỉnh số dư ví</span>
            <div class="text-xs text-slate-500">
              Số dư hiện có: <strong class="text-sm font-bold text-slate-900 ml-1">{{ formatCurrency(selectedAccountForEdit?.currentBalance) }}</strong>
            </div>
          </div>

          <!-- Mode Selector Tabs -->
          <div class="grid grid-cols-3 gap-2 p-1 bg-white rounded-md border border-slate-200 text-xs sm:text-sm font-bold">
            <button
              type="button"
              @click="balanceAdjustMode = 'add'"
              :class="[
                'py-2 px-3 rounded transition-all flex items-center justify-center gap-1.5 cursor-pointer',
                balanceAdjustMode === 'add'
                  ? 'bg-emerald-600 text-white shadow-xs'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50',
              ]"
            >
              <Plus class="w-3.5 h-3.5" />
              <span>Thêm tiền (+)</span>
            </button>
            <button
              type="button"
              @click="balanceAdjustMode = 'subtract'"
              :class="[
                'py-2 px-3 rounded transition-all flex items-center justify-center gap-1.5 cursor-pointer',
                balanceAdjustMode === 'subtract'
                  ? 'bg-rose-600 text-white shadow-xs'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50',
              ]"
            >
              <Minus class="w-3.5 h-3.5" />
              <span>Trừ bớt (-)</span>
            </button>
            <button
              type="button"
              @click="balanceAdjustMode = 'set'"
              :class="[
                'py-2 px-3 rounded transition-all flex items-center justify-center gap-1.5 cursor-pointer',
                balanceAdjustMode === 'set'
                  ? 'bg-brand-600 text-white shadow-xs'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50',
              ]"
            >
              <Equal class="w-3.5 h-3.5" />
              <span>Đặt số mới</span>
            </button>
          </div>

          <!-- Input according to mode -->
          <div v-if="balanceAdjustMode === 'add'">
            <CurrencyInput
              v-model="adjustAmount"
              label="Số tiền muốn thêm vào (+)"
              placeholder="Nhập số tiền thêm..."
              :min="0"
              :quick-amounts="[100000, 200000, 500000, 1000000, 2000000, 5000000]"
            />
          </div>

          <div v-else-if="balanceAdjustMode === 'subtract'">
            <CurrencyInput
              v-model="adjustAmount"
              label="Số tiền muốn trừ đi (-)"
              placeholder="Nhập số tiền trừ..."
              :min="0"
              :quick-amounts="[100000, 200000, 500000, 1000000, 2000000, 5000000]"
            />
          </div>

          <div v-else>
            <CurrencyInput
              v-model="directNewBalance"
              label="Số dư mong muốn mới (₫)"
              placeholder="Nhập số dư trực tiếp..."
              :allow-zero="true"
              :quick-amounts="[1000000, 2000000, 5000000, 10000000, 20000000]"
            />
          </div>

          <!-- Live Calculation Preview Card -->
          <div
            :class="[
              'p-3.5 rounded-md border flex flex-col sm:flex-row sm:items-center justify-between gap-3 transition-all',
              balanceAdjustMode === 'add'
                ? 'bg-emerald-50/80 border-emerald-200'
                : balanceAdjustMode === 'subtract'
                ? 'bg-rose-50/80 border-rose-200'
                : 'bg-indigo-50/80 border-indigo-200',
            ]"
          >
            <!-- Calculation left side -->
            <div class="flex items-center gap-3 flex-wrap text-xs sm:text-sm">
              <div>
                <span class="block text-[10px] sm:text-xs text-slate-500 font-semibold uppercase">Số tiền hiện có</span>
                <span class="font-bold text-slate-800 text-sm sm:text-base">{{ formatCurrency(selectedAccountForEdit?.currentBalance) }}</span>
              </div>

              <span
                :class="[
                  'text-lg font-extrabold px-1',
                  balanceAdjustMode === 'add'
                    ? 'text-emerald-600'
                    : balanceAdjustMode === 'subtract'
                    ? 'text-rose-600'
                    : 'text-indigo-600',
                ]"
              >
                {{ balanceAdjustMode === 'add' ? '+' : balanceAdjustMode === 'subtract' ? '-' : '➔' }}
              </span>

              <div
                :class="[
                  balanceAdjustMode === 'add'
                    ? 'text-emerald-700'
                    : balanceAdjustMode === 'subtract'
                    ? 'text-rose-700'
                    : 'text-indigo-700',
                ]"
              >
                <span class="block text-[10px] sm:text-xs opacity-75 font-semibold uppercase">
                  {{ balanceAdjustMode === 'add' ? 'Thêm tiền' : balanceAdjustMode === 'subtract' ? 'Trừ đi' : 'Số mới' }}
                </span>
                <span class="font-bold text-sm sm:text-base">
                  {{ balanceAdjustMode === 'set' ? formatCurrency(directNewBalance) : formatCurrency(adjustAmount) }}
                </span>
              </div>

              <span class="text-lg font-extrabold text-slate-400 px-1">=</span>
            </div>

            <!-- New Result right side -->
            <div class="text-left sm:text-right">
              <span class="block text-[10px] sm:text-xs text-slate-500 font-semibold uppercase">Số dư mới sau update</span>
              <span
                :class="[
                  'text-base sm:text-lg font-extrabold tracking-tight',
                  finalProjectedBalance >= 0 ? 'text-emerald-700' : 'text-rose-600',
                ]"
              >
                {{ formatCurrency(finalProjectedBalance) }}
              </span>
              <p v-if="finalProjectedBalance < 0" class="text-[10px] sm:text-xs text-rose-600 font-bold mt-0.5">
                ⚠️ Cảnh báo: Số dư ví sẽ bị âm!
              </p>
            </div>
          </div>
        </div>

        <div class="flex items-center gap-2 pt-1">
          <input
            id="accountActiveCheck"
            v-model="formData.isActive"
            type="checkbox"
            class="w-4.5 h-4.5 rounded text-brand-600 focus:ring-brand-500 cursor-pointer"
          />
          <label for="accountActiveCheck" class="text-xs sm:text-sm font-medium text-slate-700 cursor-pointer select-none">
            Kích hoạt sử dụng tài khoản này
          </label>
        </div>

        <div class="flex items-center justify-end gap-3 pt-4 border-t border-slate-200">
          <button
            type="button"
            @click="isModalOpen = false"
            class="px-4 py-2.5 text-xs sm:text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
          >
            Hủy
          </button>
          <button
            type="submit"
            :disabled="submitting"
            class="inline-flex items-center gap-1.5 px-5 py-2.5 text-xs sm:text-sm font-bold text-white bg-brand-600 hover:bg-brand-700 disabled:opacity-50 rounded-md transition-colors shadow-xs cursor-pointer"
          >
            <Save class="w-4 h-4" />
            <span>{{ isEditing ? 'Lưu thay đổi' : 'Thêm tài khoản' }}</span>
          </button>
        </div>
      </form>
    </AppModal>

    <!-- ==========================================
         MODAL: ACCOUNT HISTORY
    ========================================== -->
    <AppModal
      v-model="isHistoryOpen"
      :title="`Lịch sử giao dịch: ${selectedAccountForHistory?.name || ''}`"
      size="2xl"
    >
      <div class="space-y-4">
        <!-- Quick Stats of Account -->
        <div class="bg-slate-50 p-3.5 rounded-md border border-slate-200 flex flex-wrap items-center justify-between gap-3 text-xs">
          <div>
            <span class="text-slate-500">Số dư hiện tại:</span>
            <strong class="text-sm font-bold text-slate-900 ml-1.5">
              {{ formatCurrency(selectedAccountForHistory?.currentBalance) }}
            </strong>
          </div>
          <div>
            <span class="text-slate-500">Đã tích lũy mục tiêu:</span>
            <strong class="font-bold text-amber-600 ml-1.5">
              {{ formatCurrency(selectedAccountForHistory?.allocatedToGoal) }}
            </strong>
          </div>
          <div>
            <span class="text-slate-500">Khả dụng:</span>
            <strong class="font-bold text-emerald-600 ml-1.5">
              {{ formatCurrency(selectedAccountForHistory?.availableBalance) }}
            </strong>
          </div>
        </div>

        <!-- History Transactions Table -->
        <div v-if="historyLoading" class="py-12 flex items-center justify-center">
          <div class="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
        </div>

        <div v-else-if="accountTransactions.length === 0" class="py-10 text-center text-xs text-slate-400">
          Chưa có giao dịch nào phát sinh trên tài khoản này.
        </div>

        <div v-else class="overflow-x-auto border border-slate-200 rounded-md">
          <table class="w-full text-left border-collapse text-xs">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold">
                <th class="py-2.5 px-3">Ngày</th>
                <th class="py-2.5 px-3">Loại</th>
                <th class="py-2.5 px-3">Danh mục / Chi tiết</th>
                <th class="py-2.5 px-3 text-right">Số tiền</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="t in accountTransactions" :key="t.id" class="hover:bg-slate-50/60">
                <td class="py-2.5 px-3 whitespace-nowrap text-slate-600">{{ formatDateTime(t.transactionDate) }}</td>
                <td class="py-2.5 px-3 whitespace-nowrap">
                  <span
                    :class="[
                      'px-1.5 py-0.5 rounded font-bold text-[10.5px]',
                      t.type === 'INCOME'
                        ? 'bg-emerald-50 text-emerald-700'
                        : t.type === 'EXPENSE'
                        ? 'bg-rose-50 text-rose-700'
                        : 'bg-indigo-50 text-indigo-700',
                    ]"
                  >
                    {{ t.type === 'INCOME' ? 'Thu' : t.type === 'EXPENSE' ? 'Chi' : 'Chuyển' }}
                  </span>
                </td>
                <td class="py-2.5 px-3 text-slate-800">
                  <span class="font-medium">{{ t.description || t.categoryName || '-' }}</span>
                  <span v-if="t.toAccountName" class="text-indigo-600 text-[11px] block">➔ Đến: {{ t.toAccountName }}</span>
                </td>
                <td class="py-2.5 px-3 text-right font-bold whitespace-nowrap">
                  <span
                    :class="[
                      t.type === 'INCOME'
                        ? 'text-emerald-600'
                        : t.type === 'EXPENSE'
                        ? 'text-rose-600'
                        : 'text-indigo-600',
                    ]"
                  >
                    {{ t.type === 'INCOME' ? '+' : t.type === 'EXPENSE' ? '-' : '' }}{{ formatCurrency(t.amount) }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </AppModal>

    <!-- Confirm Delete Account Dialog -->
    <AppConfirmDialog
      v-model="isDeleteOpen"
      title="Xác nhận xóa tài khoản"
      :message="`Bạn có chắc chắn muốn xóa tài khoản '${accountToDelete?.name}' không? Thao tác này chỉ khả dụng khi tài khoản chưa có giao dịch phát sinh.`"
      confirm-text="Xóa tài khoản"
      confirm-variant="danger"
      :loading="deleting"
      @confirm="executeDeleteAccount"
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
  Wallet,
  Target,
  CheckCircle2,
  CreditCard,
  Banknote,
  Landmark,
  Smartphone,
  Edit2,
  Power,
  Trash2,
  Save,
  History,
  Minus,
  Equal,
} from 'lucide-vue-next';
import AppModal from '../../../components/common/AppModal.vue';
import AppConfirmDialog from '../../../components/common/AppConfirmDialog.vue';
import CurrencyInput from '../../../components/common/CurrencyInput.vue';
import { useToastStore } from '../../../stores/toast.store';
import { financialService } from '../../../services/financial.service';
import type {
  FinancialAccount,
  FinancialAccountRequest,
  FinancialTransaction,
} from '../../../types';
import { formatCurrency, formatCompactCurrency, formatDateTime, getAccountTypeLabel } from '../../../utils/finance';

const emit = defineEmits<{
  (e: 'dataChanged'): void;
}>();

const loading = ref(false);
const submitting = ref(false);
const deleting = ref(false);
const accounts = ref<FinancialAccount[]>([]);

// Total asset calculations
const totalBalance = computed(() => {
  return accounts.value
    .filter((a) => a.isActive)
    .reduce((sum, a) => sum + (a.currentBalance || 0), 0);
});

const totalAllocated = computed(() => {
  return accounts.value
    .filter((a) => a.isActive)
    .reduce((sum, a) => sum + (a.allocatedToGoal || 0), 0);
});

const totalAvailable = computed(() => {
  return accounts.value
    .filter((a) => a.isActive)
    .reduce((sum, a) => sum + (a.availableBalance || 0), 0);
});

// Modal state
const isModalOpen = ref(false);
const isEditing = ref(false);
const editingId = ref<number | null>(null);
const selectedAccountForEdit = ref<FinancialAccount | null>(null);

// Balance Adjustment Mode: 'add' (thêm tiền), 'subtract' (trừ bớt), 'set' (đặt số mới)
const balanceAdjustMode = ref<'add' | 'subtract' | 'set'>('add');
const adjustAmount = ref<number>(0);
const directNewBalance = ref<number>(0);

const finalProjectedBalance = computed(() => {
  const current = selectedAccountForEdit.value?.currentBalance || 0;
  if (balanceAdjustMode.value === 'add') {
    return current + (adjustAmount.value || 0);
  } else if (balanceAdjustMode.value === 'subtract') {
    return current - (adjustAmount.value || 0);
  } else {
    return directNewBalance.value || 0;
  }
});

const formData = ref<FinancialAccountRequest>({
  name: '',
  accountType: 'BANK',
  initialBalance: 0,
  isActive: true,
});

// Delete state
const isDeleteOpen = ref(false);
const accountToDelete = ref<FinancialAccount | null>(null);

// History modal state
const isHistoryOpen = ref(false);
const historyLoading = ref(false);
const selectedAccountForHistory = ref<FinancialAccount | null>(null);
const accountTransactions = ref<FinancialTransaction[]>([]);

async function loadAccounts() {
  loading.value = true;
  try {
    accounts.value = await financialService.getAccounts(false);
  } catch (err) {
    console.error('Failed to load accounts', err);
  } finally {
    loading.value = false;
  }
}

function openAddAccountModal() {
  isEditing.value = false;
  editingId.value = null;
  selectedAccountForEdit.value = null;
  adjustAmount.value = 0;
  directNewBalance.value = 0;
  formData.value = {
    name: '',
    accountType: 'BANK',
    initialBalance: 0,
    isActive: true,
  };
  isModalOpen.value = true;
}

function openEditAccountModal(acc: FinancialAccount) {
  isEditing.value = true;
  editingId.value = acc.id;
  selectedAccountForEdit.value = acc;
  balanceAdjustMode.value = 'add';
  adjustAmount.value = 0;
  directNewBalance.value = acc.currentBalance || 0;
  formData.value = {
    name: acc.name,
    accountType: acc.accountType,
    initialBalance: acc.initialBalance,
    isActive: acc.isActive,
  };
  isModalOpen.value = true;
}

const toastStore = useToastStore();

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

async function saveAccount() {
  if (!formData.value.name.trim()) return;
  submitting.value = true;
  try {
    if (isEditing.value && editingId.value && selectedAccountForEdit.value) {
      // Calculate target new balance from adjustment and update initialBalance with delta
      const targetBalance = finalProjectedBalance.value;
      const currentBal = selectedAccountForEdit.value.currentBalance || 0;
      const initialBal = selectedAccountForEdit.value.initialBalance || 0;
      const delta = targetBalance - currentBal;
      formData.value.initialBalance = initialBal + delta;

      await financialService.updateAccount(editingId.value, formData.value);
      toastStore.success('Cập nhật tài khoản & số dư thành công');
    } else {
      await financialService.createAccount(formData.value);
      toastStore.success('Thêm tài khoản mới thành công');
    }
    isModalOpen.value = false;
    await loadAccounts();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi xảy ra khi lưu tài khoản', 'Lỗi lưu tài khoản');
  } finally {
    submitting.value = false;
  }
}

async function toggleAccountActive(acc: FinancialAccount) {
  try {
    await financialService.toggleAccount(acc.id);
    toastStore.success('Đã cập nhật trạng thái tài khoản');
    await loadAccounts();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Có lỗi xảy ra khi cập nhật trạng thái', 'Lỗi cập nhật');
  }
}

function confirmDeleteAccount(acc: FinancialAccount) {
  accountToDelete.value = acc;
  isDeleteOpen.value = true;
}

async function executeDeleteAccount() {
  if (!accountToDelete.value) return;
  deleting.value = true;
  try {
    await financialService.deleteAccount(accountToDelete.value.id);
    toastStore.success('Đã xóa tài khoản thành công');
    isDeleteOpen.value = false;
    accountToDelete.value = null;
    await loadAccounts();
    emit('dataChanged');
  } catch (err: any) {
    showError(err?.response?.data?.message || 'Không thể xóa tài khoản', 'Lỗi xóa tài khoản');
  } finally {
    deleting.value = false;
  }
}

async function viewAccountHistory(acc: FinancialAccount) {
  selectedAccountForHistory.value = acc;
  isHistoryOpen.value = true;
  historyLoading.value = true;
  try {
    const res = await financialService.getTransactions({
      accountId: acc.id,
      size: 50,
    });
    accountTransactions.value = res.items;
  } catch (err) {
    console.error('Failed to load account transactions history', err);
  } finally {
    historyLoading.value = false;
  }
}

onMounted(() => {
  loadAccounts();
});

defineExpose({
  openAddModal: openAddAccountModal,
  refresh: loadAccounts,
});
</script>
