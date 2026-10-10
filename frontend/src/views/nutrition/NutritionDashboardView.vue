<template>
  <div class="space-y-6 pb-12">
    <!-- Header: Title & Actions & Date Navigation -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs">
      <div>
        <div class="flex items-center gap-3.5">
          <div class="p-3 rounded-md bg-emerald-50 text-emerald-600 border border-emerald-100">
            <Apple class="w-6 h-6" />
          </div>
          <div>
            <h1 class="text-xl sm:text-2xl font-bold text-slate-900">
              Dinh dưỡng hằng ngày
            </h1>
            <p class="text-sm sm:text-base text-slate-500 mt-1">
              Theo dõi Calo, Macro và Vi chất chuẩn xác cho mục tiêu vóc dáng & sức khỏe
            </p>
          </div>
        </div>
      </div>

      <!-- Date Navigator -->
      <div class="flex flex-wrap items-center gap-2">
        <div class="flex items-center bg-slate-50 border border-slate-200 rounded-md p-1 shadow-2xs">
          <button
            type="button"
            class="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-white rounded-md transition-colors"
            title="Ngày trước"
            @click="navigateDate(-1)"
          >
            <ChevronLeft class="w-4 h-4" />
          </button>
          <div class="relative px-3 py-1 flex items-center gap-2 cursor-pointer">
            <Calendar class="w-4 h-4 text-emerald-600" />
            <span class="text-xs font-bold text-slate-800">{{ formattedSelectedDate }}</span>
            <input
              v-model="currentDate"
              type="date"
              class="absolute inset-0 opacity-0 cursor-pointer w-full h-full"
              @change="fetchDailySummary"
            />
          </div>
          <button
            type="button"
            class="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-white rounded-md transition-colors"
            title="Ngày sau"
            @click="navigateDate(1)"
          >
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>

        <button
          type="button"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-md transition-colors shadow-2xs cursor-pointer"
          title="Thiết lập mục tiêu"
          @click="isTargetModalOpen = true"
        >
          <Target class="w-4 h-4 text-blue-600" />
          <span class="hidden sm:inline">Mục tiêu</span>
        </button>

        <button
          type="button"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-md transition-colors shadow-2xs cursor-pointer"
          title="Sao chép toàn bộ món từ hôm qua"
          :disabled="isCopying"
          @click="copyYesterday"
        >
          <Copy class="w-4 h-4 text-amber-600" />
          <span class="hidden sm:inline">Sao chép hôm qua</span>
        </button>

        <button
          type="button"
          class="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white rounded-md shadow-xs transition-colors cursor-pointer"
          @click="openAddModal()"
        >
          <Plus class="w-4 h-4" />
          <span>Thêm thực phẩm</span>
        </button>
      </div>
    </div>

    <!-- Daily Energy Balance & Deficit Overview (4 Boxes + Meter) -->
    <div class="bg-white p-5 rounded-md border border-slate-200 shadow-xs space-y-4">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- 1. Calories ăn vào (Nạp) -->
        <div class="p-4 bg-slate-50/60 hover:bg-slate-50 rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between transition-colors">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Calories ăn vào</span>
            <Utensils class="w-4 h-4 text-emerald-600" />
          </div>
          <div class="mt-2.5 flex items-baseline gap-1">
            <span class="text-2xl font-bold text-slate-900">{{ summary.totalCalories.toLocaleString() }}</span>
            <span class="text-xs text-slate-400 font-medium">kcal</span>
          </div>
          <div class="text-[11px] text-slate-500 mt-1 flex items-center justify-between">
            <span>Từ {{ summary.items.length }} món đã ghi nhận</span>
            <span class="font-medium text-slate-400">Mục tiêu: {{ targetSettings.targetCalories }} kcal</span>
          </div>
        </div>

        <!-- 2. Calories hoạt động (Đốt thêm) -->
        <div class="p-4 bg-slate-50/60 hover:bg-slate-50 rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between transition-colors">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Calories hoạt động</span>
            <Activity class="w-4 h-4 text-rose-600" />
          </div>
          <div class="mt-2.5 flex items-baseline gap-1">
            <span class="text-2xl font-bold text-rose-600">+{{ Math.round(deficitSummary.activityCalories).toLocaleString() }}</span>
            <span class="text-xs text-slate-400 font-medium">kcal</span>
          </div>
          <div class="text-[11px] text-slate-500 mt-1">
            {{ deficitSummary.activityLogs?.length || 0 }} hoạt động (Net MET)
          </div>
        </div>

        <!-- 3. Tổng tiêu hao (TDEE + Hoạt động) -->
        <div class="p-4 bg-slate-50/60 hover:bg-slate-50 rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between transition-colors">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">Tổng tiêu hao</span>
            <Flame class="w-4 h-4 text-amber-600" />
          </div>
          <div class="mt-2.5 flex items-baseline gap-1">
            <span class="text-2xl font-bold text-amber-700">{{ Math.round(deficitSummary.totalCaloriesBurned).toLocaleString() }}</span>
            <span class="text-xs text-slate-400 font-medium">kcal</span>
          </div>
          <div class="text-[11px] text-slate-500 mt-1">
            TDEE ({{ deficitSummary.tdee || targetSettings.targetCalories || 2100 }}) + Hoạt động ({{ Math.round(deficitSummary.activityCalories) }})
          </div>
        </div>

        <!-- 4. Calorie Balance (Thâm hụt / Thặng dư) -->
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
      <div class="bg-slate-50 p-3.5 rounded-md border border-slate-200/80">
        <div class="flex items-center justify-between text-xs font-semibold mb-1.5">
          <span class="text-slate-600">Tiến độ tiêu hao vs Ăn vào:</span>
          <span class="text-slate-900 font-bold">
            {{ summary.totalCalories.toLocaleString() }} / {{ Math.round(deficitSummary.totalCaloriesBurned).toLocaleString() }} kcal
            <span class="text-slate-400 font-normal">({{ burnedPercent }}%)</span>
          </span>
        </div>
        <div class="w-full bg-slate-200/70 rounded-full h-2 overflow-hidden flex">
          <div
            :class="progressBarColor"
            class="h-2 rounded-full transition-all duration-500"
            :style="{ width: `${Math.min(100, burnedPercent)}%` }"
          ></div>
        </div>
      </div>
    </div>

    <!-- Daily Macronutrients Breakdown (4 Cards Grid) -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Protein -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between">
        <div>
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-semibold text-slate-500">Protein (Đạm)</span>
            <Dna class="w-4 h-4 text-blue-600" />
          </div>
          <div class="flex items-baseline gap-1.5 mt-1">
            <span class="text-2xl font-bold text-blue-700">{{ summary.totalProtein.toFixed(1) }}g</span>
            <span class="text-xs text-slate-500 font-medium">/ {{ targetSettings.targetProtein }}g</span>
          </div>
        </div>
        <div class="w-full bg-slate-100 rounded-full h-1.5 mt-4 overflow-hidden">
          <div
            class="bg-blue-600 h-1.5 rounded-full transition-all duration-300"
            :style="{ width: `${Math.min(100, (summary.totalProtein / targetSettings.targetProtein) * 100)}%` }"
          ></div>
        </div>
      </div>

      <!-- Carbohydrate -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between">
        <div>
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-semibold text-slate-500">Carbohydrate (Đường bột)</span>
            <Wheat class="w-4 h-4 text-amber-600" />
          </div>
          <div class="flex items-baseline gap-1.5 mt-1">
            <span class="text-2xl font-bold text-amber-700">{{ summary.totalCarbohydrate.toFixed(1) }}g</span>
            <span class="text-xs text-slate-500 font-medium">/ {{ targetSettings.targetCarb }}g</span>
          </div>
        </div>
        <div class="w-full bg-slate-100 rounded-full h-1.5 mt-4 overflow-hidden">
          <div
            class="bg-amber-500 h-1.5 rounded-full transition-all duration-300"
            :style="{ width: `${Math.min(100, (summary.totalCarbohydrate / targetSettings.targetCarb) * 100)}%` }"
          ></div>
        </div>
      </div>

      <!-- Fat -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between">
        <div>
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-semibold text-slate-500">Fat (Chất béo)</span>
            <Droplet class="w-4 h-4 text-rose-600" />
          </div>
          <div class="flex items-baseline gap-1.5 mt-1">
            <span class="text-2xl font-bold text-rose-700">{{ summary.totalFat.toFixed(1) }}g</span>
            <span class="text-xs text-slate-500 font-medium">/ {{ targetSettings.targetFat }}g</span>
          </div>
        </div>
        <div class="w-full bg-slate-100 rounded-full h-1.5 mt-4 overflow-hidden">
          <div
            class="bg-rose-500 h-1.5 rounded-full transition-all duration-300"
            :style="{ width: `${Math.min(100, (summary.totalFat / targetSettings.targetFat) * 100)}%` }"
          ></div>
        </div>
      </div>

      <!-- Fiber -->
      <div class="bg-white p-5 rounded-md border border-slate-200 shadow-2xs flex flex-col justify-between">
        <div>
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-semibold text-slate-500">Fiber (Chất xơ)</span>
            <Sprout class="w-4 h-4 text-teal-600" />
          </div>
          <div class="flex items-baseline gap-1.5 mt-1">
            <span class="text-2xl font-bold text-teal-700">{{ summary.totalFiber.toFixed(1) }}g</span>
            <span class="text-xs text-slate-500 font-medium">/ 28g</span>
          </div>
        </div>
        <div class="w-full bg-slate-100 rounded-full h-1.5 mt-4 overflow-hidden">
          <div
            class="bg-teal-600 h-1.5 rounded-full transition-all duration-300"
            :style="{ width: `${Math.min(100, (summary.totalFiber / 28) * 100)}%` }"
          ></div>
        </div>
      </div>
    </div>

    <!-- TAB CONTROLS (Thực phẩm & Hoạt động) -->
    <div class="flex items-center gap-2 border-b border-slate-200">
      <button
        type="button"
        :class="[
          'inline-flex items-center gap-2 px-4 py-2.5 text-sm font-bold border-b-2 transition-all cursor-pointer -mb-px',
          activeTab === 'foods'
            ? 'border-emerald-600 text-emerald-700 bg-white rounded-t-lg shadow-2xs'
            : 'border-transparent text-slate-500 hover:text-slate-800 hover:border-slate-300',
        ]"
        @click="activeTab = 'foods'"
      >
        <Utensils class="w-4 h-4" />
        <span>Nhật ký thực phẩm hôm nay</span>
        <span class="px-2 py-0.5 text-xs font-extrabold rounded-full bg-emerald-100 text-emerald-800">
          {{ summary.items.length }}
        </span>
      </button>

      <button
        type="button"
        :class="[
          'inline-flex items-center gap-2 px-4 py-2.5 text-sm font-bold border-b-2 transition-all cursor-pointer -mb-px',
          activeTab === 'activity'
            ? 'border-rose-600 text-rose-700 bg-white rounded-t-lg shadow-2xs'
            : 'border-transparent text-slate-500 hover:text-slate-800 hover:border-slate-300',
        ]"
        @click="activeTab = 'activity'"
      >
        <Flame class="w-4 h-4" />
        <span>Hoạt động thể chất & Thâm hụt Calo</span>
      </button>
    </div>

    <!-- TAB 1 CONTENT: FOOD LIST & MICRONUTRIENTS -->
    <div v-show="activeTab === 'foods'" class="space-y-6">
      <!-- Quick Add Bar (Frequently Used / Recent Items) -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-2xs">
        <div class="flex items-center justify-between mb-2.5">
          <div class="flex items-center gap-1.5 text-xs font-bold text-slate-700">
            <Sparkles class="w-3.5 h-3.5 text-amber-500" />
            <span>Thêm nhanh món thường dùng:</span>
          </div>
          <span class="text-[11px] text-slate-400">Thêm nhanh trong 1 lượt click</span>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <div
            v-for="item in visibleQuickAddList"
            :key="item.foodVariantId ? `v-${item.foodVariantId}` : `u-${item.userFoodId}`"
            class="inline-flex items-center bg-slate-50 hover:bg-emerald-50 text-slate-700 hover:text-emerald-700 border border-slate-200 hover:border-emerald-200 rounded-md transition-colors group shadow-2xs overflow-hidden"
          >
            <button
              type="button"
              class="inline-flex items-center gap-1.5 px-2.5 py-1.5 text-xs font-medium cursor-pointer"
              @click="quickAdd(item)"
            >
              <Plus class="w-3.5 h-3.5 text-slate-400 group-hover:text-emerald-600" />
              <span>{{ item.defaultQuantity }} {{ item.defaultUnit }} {{ item.name }} ({{ (item.state || '').toLowerCase() }})</span>
            </button>
            <button
              type="button"
              class="px-1.5 py-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-colors cursor-pointer border-l border-slate-200 group-hover:border-emerald-200"
              title="Ẩn món này khỏi danh sách thêm nhanh"
              @click.stop="dismissQuickAddItem(item)"
            >
              <X class="w-3 h-3" />
            </button>
          </div>

          <button
            type="button"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 rounded-md transition-colors cursor-pointer"
            @click="openAddModal()"
          >
            <Search class="w-3.5 h-3.5" />
            <span>Tìm món khác...</span>
          </button>
        </div>
      </div>

      <!-- Main Food List Table Card -->
      <div class="bg-white rounded-md border border-slate-200 overflow-hidden shadow-2xs">
      <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="p-2.5 rounded-md bg-emerald-50 text-emerald-600 border border-emerald-100">
            <Utensils class="w-5 h-5" />
          </div>
          <div>
            <h2 class="text-lg sm:text-xl font-bold text-slate-900">
              Danh sách thực phẩm hôm nay
            </h2>
            <p class="text-xs text-slate-500 mt-0.5">
              {{ summary.items.length }} món đã ghi nhận
            </p>
          </div>
        </div>

        <button
          type="button"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 border border-emerald-200 rounded-md transition-colors cursor-pointer"
          @click="openAddModal()"
        >
          <Plus class="w-3.5 h-3.5" />
          <span>Thêm món</span>
        </button>
      </div>

      <!-- Food Table -->
      <div class="overflow-x-auto">
        <table class="w-full text-left text-sm border-collapse">
          <thead>
            <tr class="bg-slate-50/75 border-b border-slate-100 text-xs font-semibold text-slate-600">
              <th class="py-3 px-4">Thực phẩm</th>
              <th class="py-3 px-3 text-center">Trạng thái</th>
              <th class="py-3 px-3 text-right">Lượng</th>
              <th class="py-3 px-3 text-right">Kcal</th>
              <th class="py-3 px-3 text-right text-blue-700">Protein</th>
              <th class="py-3 px-3 text-right text-amber-700">Carb</th>
              <th class="py-3 px-3 text-right text-rose-700">Fat</th>
              <th class="py-3 px-4 text-center w-20">Thao tác</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr
              v-for="item in summary.items"
              :key="item.id"
              class="hover:bg-slate-50/60 transition-colors group"
            >
              <!-- Food Name & Source Badge -->
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-2">
                  <span class="font-bold text-slate-900">{{ item.foodName }}</span>
                  <span class="text-xs text-slate-500 font-medium">({{ (item.state || '').toLowerCase() }})</span>
                  <span
                    v-if="item.isUserCustom"
                    class="px-2 py-0.5 text-[11px] font-medium bg-amber-50 text-amber-700 border border-amber-200 rounded-sm"
                  >
                    Ước tính
                  </span>
                  <span
                    v-else
                    class="px-2 py-0.5 text-[11px] font-medium bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-sm flex items-center gap-1"
                  >
                    <CheckCircle2 class="w-3 h-3 text-emerald-600" />
                    Verified
                  </span>
                </div>
                <!-- Micronutrients snippet for this item -->
                <div v-if="item.micronutrients.length > 0" class="text-xs text-slate-400 mt-1">
                  <span v-for="(m, idx) in item.micronutrients.slice(0, 3)" :key="m.id || idx">
                    {{ m.nutrientName }}: {{ m.amount }} {{ m.unit }}<span v-if="idx < Math.min(item.micronutrients.length, 3) - 1"> • </span>
                  </span>
                </div>
              </td>

              <!-- State Badge -->
              <td class="py-3.5 px-3 text-center">
                <span :class="['px-2.5 py-1 text-xs font-semibold rounded-sm border lowercase', getStateBadgeClass(item.state)]">
                  {{ (item.state || '').toLowerCase() }}
                </span>
              </td>

              <!-- Quantity & Unit -->
              <td class="py-3.5 px-3 text-right font-medium text-slate-700">
                <span class="font-semibold text-slate-900">{{ item.quantity }} {{ item.unit }}</span>
                <span
                  v-if="item.calculatedGrams && item.unit !== 'g'"
                  class="block text-xs text-slate-400 font-normal"
                >
                  (~{{ Math.round(item.calculatedGrams) }} g)
                </span>
                <span
                  v-else-if="item.calculatedMl && item.unit !== 'ml'"
                  class="block text-xs text-slate-400 font-normal"
                >
                  (~{{ Math.round(item.calculatedMl) }} ml)
                </span>
              </td>

              <!-- Macros -->
              <td class="py-3.5 px-3 text-right font-bold text-slate-900">
                {{ Math.round(item.calories) }}
              </td>
              <td class="py-3.5 px-3 text-right font-semibold text-blue-700">
                {{ item.protein.toFixed(1) }}g
              </td>
              <td class="py-3.5 px-3 text-right font-semibold text-amber-700">
                {{ item.carbohydrate.toFixed(1) }}g
              </td>
              <td class="py-3.5 px-3 text-right font-semibold text-rose-700">
                {{ item.fat.toFixed(1) }}g
              </td>

              <!-- Actions -->
              <td class="py-3.5 px-4 text-center">
                <div class="flex items-center justify-center gap-1.5">
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-emerald-600 hover:bg-slate-100 rounded-sm transition-colors cursor-pointer"
                    title="Chỉnh sửa"
                    @click="editLog(item)"
                  >
                    <Edit3 class="w-4 h-4" />
                  </button>
                  <button
                    type="button"
                    class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-sm transition-colors cursor-pointer"
                    title="Xóa"
                    @click="confirmDeleteFood(item)"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>

                </div>
              </td>
            </tr>

            <!-- Empty Row -->
            <tr v-if="summary.items.length === 0">
              <td colspan="8" class="py-12 text-center text-slate-400">
                <div class="flex flex-col items-center justify-center space-y-2.5">
                  <Utensils class="w-8 h-8 text-slate-300" />
                  <p class="text-sm font-medium text-slate-600">Chưa có thực phẩm nào được ghi nhận cho ngày này.</p>
                  <button
                    type="button"
                    class="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-emerald-600 bg-emerald-50 hover:bg-emerald-100 rounded-md transition-colors cursor-pointer"
                    @click="openAddModal()"
                  >
                    <Plus class="w-3.5 h-3.5" />
                    <span>Thêm thực phẩm đầu tiên</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>

          <!-- Table Footer: Totals -->
          <tfoot v-if="summary.items.length > 0" class="bg-slate-50 border-t-2 border-slate-200">
            <tr class="font-bold text-slate-900 text-xs">
              <td class="py-4 px-4" colspan="2">
                Tổng cộng hôm nay:
              </td>
              <td class="py-4 px-3 text-right text-slate-500 font-semibold text-xs">
                {{ summary.items.length }} món
              </td>
              <td class="py-4 px-3 text-right text-sm font-bold text-slate-900">
                {{ summary.totalCalories.toLocaleString() }} kcal
              </td>
              <td class="py-4 px-3 text-right text-sm font-bold text-blue-700">
                {{ summary.totalProtein.toFixed(1) }}g
              </td>
              <td class="py-4 px-3 text-right text-sm font-bold text-amber-700">
                {{ summary.totalCarbohydrate.toFixed(1) }}g
              </td>
              <td class="py-4 px-3 text-right text-sm font-bold text-rose-700">
                {{ summary.totalFat.toFixed(1) }}g
              </td>
              <td class="py-4 px-4 text-center"></td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>

    <!-- Micronutrients Section (Vitamins & Minerals) -->
    <div class="bg-white p-5 rounded-md border border-slate-200 shadow-2xs">
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-2.5">
          <div class="p-2 rounded-md bg-amber-50 text-amber-600">
            <Sparkles class="w-4.5 h-4.5" />
          </div>
          <div>
            <h3 class="text-sm font-bold text-slate-900">
              Vitamin & khoáng chất nổi bật
            </h3>
            <p class="text-xs text-slate-500 mt-0.5">
              Tổng hợp từ các thực phẩm đã ăn trong ngày
            </p>
          </div>
        </div>
      </div>

      <div v-if="summary.topMicronutrients.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
        <div
          v-for="micro in summary.topMicronutrients"
          :key="micro.nutrientName"
          class="p-3 bg-slate-50 border border-slate-200 rounded-md flex flex-col justify-between"
        >
          <span class="text-xs font-semibold text-slate-600 truncate">{{ micro.nutrientName }}</span>
          <span class="text-sm font-bold text-emerald-700 mt-1.5">
            {{ micro.totalAmount }} <span class="text-xs font-medium text-slate-500">{{ micro.unit }}</span>
          </span>
        </div>
      </div>

      <div v-else class="text-xs text-slate-400 py-3 text-center">
        Chưa có dữ liệu vi chất cho ngày này.
      </div>
    </div>
  </div>

  <!-- TAB 2 CONTENT: CALORIE DEFICIT & PHYSICAL ACTIVITY -->
  <div v-show="activeTab === 'activity'" class="space-y-6">
    <CalorieDeficitSection
      ref="deficitSectionRef"
      :log-date="currentDate"
      :food-calories="summary.totalCalories"
      :initial-tdee="targetSettings.targetCalories || 2100"
      :initial-weight="targetSettings.weight || 68"
      @updated="onDeficitUpdated"
    />
  </div>

    <!-- Modals -->
    <AddFoodModal
      :is-open="isAddModalOpen"
      :log-date="currentDate"
      :editing-log="editingLogItem"
      @close="closeAddModal"
      @saved="fetchDailySummary"
      @open-create-custom="openCreateCustomModal"
    />

    <CreateUserFoodModal
      :is-open="isCreateCustomOpen"
      :initial-name="customInitialName"
      @close="isCreateCustomOpen = false"
      @created="onCustomFoodCreated"
    />

    <NutritionTargetModal
      :is-open="isTargetModalOpen"
      @close="isTargetModalOpen = false"
      @saved="onTargetSaved"
    />

    <!-- Confirm Delete Food Modal -->
    <AppConfirmDialog
      v-model="showDeleteFoodConfirm"
      title="Xóa thực phẩm khỏi nhật ký"
      :message="deletingFoodItem ? `Bạn có chắc chắn muốn xóa '${deletingFoodItem.foodName}' (${deletingFoodItem.quantity} ${deletingFoodItem.unit}) khỏi nhật ký không?` : 'Bạn có chắc chắn muốn xóa món này khỏi nhật ký?'"
      confirm-text="Xóa món"
      confirm-variant="danger"
      :loading="isDeletingFood"
      @confirm="executeDeleteFood"
    />
  </div>
</template>


<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import {
  Apple,
  Calendar,
  ChevronLeft,
  ChevronRight,
  Plus,
  Copy,
  Target,
  Flame,
  Dna,
  Wheat,
  Droplet,
  Sprout,
  Sparkles,
  Utensils,
  Edit3,
  Trash2,
  Search,
  CheckCircle2,
  Activity,
  TrendingDown,
  TrendingUp,
  Scale,
  X,
} from 'lucide-vue-next';
import { nutritionService } from '../../services/nutrition.service';
import AddFoodModal from '../../components/nutrition/AddFoodModal.vue';
import CreateUserFoodModal from '../../components/nutrition/CreateUserFoodModal.vue';
import NutritionTargetModal from '../../components/nutrition/NutritionTargetModal.vue';
import CalorieDeficitSection from '../../components/nutrition/CalorieDeficitSection.vue';
import AppConfirmDialog from '../../components/common/AppConfirmDialog.vue';
import { useToastStore } from '../../stores/toast.store';
import type {
  NutritionDailySummary,
  NutritionDailyLogItem,
  NutritionRecentFood,
  NutritionTargetSettings,
  CalorieDeficitSummary,
} from '../../types/nutrition';

const toast = useToastStore();
const activeTab = ref<'foods' | 'activity'>('foods');
const deficitSectionRef = ref<InstanceType<typeof CalorieDeficitSection> | null>(null);

const showDeleteFoodConfirm = ref(false);
const deletingFoodItem = ref<NutritionDailyLogItem | null>(null);
const isDeletingFood = ref(false);

const getTodayString = () => {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const currentDate = ref(getTodayString());
const summary = ref<NutritionDailySummary>({
  logDate: getTodayString(),
  totalCalories: 0,
  totalProtein: 0,
  totalCarbohydrate: 0,
  totalFat: 0,
  totalFiber: 0,
  items: [],
  topMicronutrients: [],
});

const deficitSummary = ref<CalorieDeficitSummary>({
  logDate: getTodayString(),
  weightKg: 68,
  tdee: 2100,
  foodCalories: 0,
  activityCalories: 0,
  totalCaloriesBurned: 2100,
  calorieBalance: 2100,
  status: 'DEFICIT',
  activityLogs: [],
  loggedFoodsCount: 0,
});

const quickAddList = ref<NutritionRecentFood[]>([]);
const isCopying = ref(false);

const DISMISSED_QUICK_ADD_KEY = 'nutrition_dismissed_quick_add';
const dismissedQuickAddKeys = ref<string[]>([]);

try {
  const saved = localStorage.getItem(DISMISSED_QUICK_ADD_KEY);
  if (saved) {
    dismissedQuickAddKeys.value = JSON.parse(saved);
  }
} catch (e) {
  console.error('Failed to parse dismissed quick add items:', e);
}

const getItemKey = (item: NutritionRecentFood) => {
  return (item.name || '').trim().toLowerCase();
};

const visibleQuickAddList = computed(() => {
  return quickAddList.value.filter((item) => {
    const key = getItemKey(item);
    return !dismissedQuickAddKeys.value.includes(key);
  });
});

const dismissQuickAddItem = (item: NutritionRecentFood) => {
  const key = getItemKey(item);
  if (!dismissedQuickAddKeys.value.includes(key)) {
    dismissedQuickAddKeys.value.push(key);
    localStorage.setItem(DISMISSED_QUICK_ADD_KEY, JSON.stringify(dismissedQuickAddKeys.value));
    toast.success(`Đã ẩn '${item.name}' khỏi danh sách thêm nhanh`);
  }
};

const isAddModalOpen = ref(false);
const editingLogItem = ref<NutritionDailyLogItem | null>(null);
const isCreateCustomOpen = ref(false);
const customInitialName = ref('');
const isTargetModalOpen = ref(false);

const targetSettings = ref<NutritionTargetSettings>(nutritionService.getTargetSettings());

const formattedSelectedDate = computed(() => {
  if (!currentDate.value) return '';
  const [y, m, d] = currentDate.value.split('-');
  return `${d}/${m}/${y}`;
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
  return Math.round(((deficitSummary.value.foodCalories || summary.value.totalCalories) / deficitSummary.value.totalCaloriesBurned) * 100);
});

const progressBarColor = computed(() => {
  if (burnedPercent.value <= 100) return 'bg-emerald-500';
  return 'bg-amber-500';
});

const onDeficitUpdated = (newDeficit: CalorieDeficitSummary) => {
  deficitSummary.value = newDeficit;
};

const navigateDate = (days: number) => {
  const d = new Date(currentDate.value);
  d.setDate(d.getDate() + days);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  currentDate.value = `${year}-${month}-${day}`;
  fetchDailySummary();
};

const fetchDailySummary = async () => {
  try {
    const data = await nutritionService.getDailySummary(currentDate.value);
    summary.value = data;
    await fetchDeficitSummary();
    deficitSectionRef.value?.fetchDeficitData();
  } catch (e) {
    console.error('Failed to daily summary:', e);
  }
};

const fetchDeficitSummary = async () => {
  try {
    const data = await nutritionService.getDeficitSummary(
      currentDate.value,
      targetSettings.value.targetCalories || 2100,
      targetSettings.value.weight || 68
    );
    deficitSummary.value = data;
  } catch (e) {
    console.error('Failed to fetch deficit summary:', e);
  }
};

const fetchRecentFoods = async () => {
  try {
    quickAddList.value = await nutritionService.getRecentFoods();
  } catch (e) {
    console.error('Failed to fetch recent foods:', e);
  }
};

const quickAdd = async (item: NutritionRecentFood) => {
  try {
    await nutritionService.addDailyLog({
      logDate: currentDate.value,
      foodVariantId: item.foodVariantId,
      userFoodId: item.userFoodId,
      quantity: item.defaultQuantity || 100,
      unit: item.defaultUnit || 'g',
    });
    toast.success(`Đã thêm nhanh: ${item.defaultQuantity || 100} ${item.defaultUnit || 'g'} ${item.name} (${(item.state || '').toLowerCase()})`);
    await fetchDailySummary();
    await fetchRecentFoods();
  } catch (e: any) {
    console.error('Failed to quick add food:', e);
    toast.error(e?.response?.data?.message || 'Không thể thêm món ăn');
  }
};

const copyYesterday = async () => {
  const d = new Date(currentDate.value);
  d.setDate(d.getDate() - 1);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const yesterdayStr = `${year}-${month}-${day}`;

  isCopying.value = true;
  try {
    const res = await nutritionService.copyDayLogs(yesterdayStr, currentDate.value);
    summary.value = res;
    await fetchDeficitSummary();
    toast.success('Đã sao chép toàn bộ món từ hôm qua!');
  } catch (e: any) {
    console.error('Failed to copy yesterday:', e);
    toast.error(e?.response?.data?.message || 'Không thể sao chép dữ liệu');
  } finally {
    isCopying.value = false;
  }
};

const openAddModal = () => {
  editingLogItem.value = null;
  isAddModalOpen.value = true;
};

const editLog = (item: NutritionDailyLogItem) => {
  editingLogItem.value = item;
  isAddModalOpen.value = true;
};

const closeAddModal = () => {
  isAddModalOpen.value = false;
  editingLogItem.value = null;
};

const confirmDeleteFood = (item: NutritionDailyLogItem) => {
  deletingFoodItem.value = item;
  showDeleteFoodConfirm.value = true;
};

const executeDeleteFood = async () => {
  if (!deletingFoodItem.value) return;
  isDeletingFood.value = true;
  try {
    await nutritionService.deleteDailyLog(deletingFoodItem.value.id);
    toast.success('Đã xóa món ăn khỏi nhật ký');
    showDeleteFoodConfirm.value = false;
    deletingFoodItem.value = null;
    await fetchDailySummary();
    await fetchRecentFoods();
  } catch (e: any) {
    console.error('Failed to delete log:', e);
    toast.error(e?.response?.data?.message || 'Không thể xóa món ăn');
  } finally {
    isDeletingFood.value = false;
  }
};


const openCreateCustomModal = (initialName?: string) => {
  customInitialName.value = initialName || '';
  isCreateCustomOpen.value = true;
};

const onCustomFoodCreated = () => {
  fetchDailySummary();
  fetchRecentFoods();
};

const onTargetSaved = (targets: NutritionTargetSettings) => {
  targetSettings.value = targets;
  fetchDeficitSummary();
};

const getStateBadgeClass = (state: string) => {
  const s = (state || '').toLowerCase();
  if (s.includes('sống') || s.includes('raw')) {
    return 'bg-blue-50 text-blue-700 border-blue-200';
  } else if (s.includes('chín') || s.includes('luộc') || s.includes('hấp')) {
    return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  } else if (s.includes('chiên') || s.includes('rán')) {
    return 'bg-rose-50 text-rose-700 border-rose-200';
  } else if (s.includes('nướng')) {
    return 'bg-amber-50 text-amber-700 border-amber-200';
  } else if (s.includes('tươi')) {
    return 'bg-teal-50 text-teal-700 border-teal-200';
  }
  return 'bg-slate-50 text-slate-700 border-slate-200';
};

onMounted(() => {
  fetchDailySummary();
  fetchRecentFoods();
});
</script>
