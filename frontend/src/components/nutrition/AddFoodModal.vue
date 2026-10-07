<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 md:p-8 bg-slate-900/40 backdrop-blur-xs">
    <div
      class="bg-white rounded-md border border-slate-200 shadow-2xl w-full max-w-5xl xl:max-w-6xl max-h-[94vh] min-h-[620px] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-150"
    >
      <!-- Modal Header -->
      <div class="flex items-center justify-between px-8 py-5 border-b border-slate-100 shrink-0">
        <div class="flex items-center gap-3.5">
          <div class="p-2.5 rounded-md bg-emerald-50 text-emerald-600">
            <Utensils class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-base sm:text-lg font-bold text-slate-900">
              {{ editingLog ? 'Chỉnh sửa thực phẩm' : 'Thêm thực phẩm vào ngày' }}
            </h3>
            <p class="text-xs text-slate-500 mt-0.5">Ngày ghi nhận: {{ formattedDate }}</p>
          </div>
        </div>
        <button
          type="button"
          class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-md transition-colors cursor-pointer"
          @click="closeModal"
        >
          <X class="w-5 h-5" />
        </button>
      </div>

      <!-- Search Mode Tabs (Local vs USDA Online) -->
      <div v-if="!editingLog" class="flex border-b border-slate-200/80 px-8 pt-3 bg-slate-50/70 gap-3 shrink-0">
        <button
          type="button"
          :class="[
            'pb-3 px-5 text-xs font-semibold border-b-2 transition-colors flex items-center gap-2 cursor-pointer',
            searchMode === 'local'
              ? 'border-emerald-600 text-emerald-700 bg-white rounded-t-md shadow-2xs font-bold'
              : 'border-transparent text-slate-500 hover:text-slate-700',
          ]"
          @click="switchSearchMode('local')"
        >
          <Package class="w-4 h-4 text-emerald-600" />
          <span>Món có sẵn & cá nhân</span>
        </button>
        <button
          type="button"
          :class="[
            'pb-3 px-5 text-xs font-semibold border-b-2 transition-colors flex items-center gap-2 cursor-pointer',
            searchMode === 'usda'
              ? 'border-emerald-600 text-emerald-700 bg-white rounded-t-md shadow-2xs font-bold'
              : 'border-transparent text-slate-500 hover:text-slate-700',
          ]"
          @click="switchSearchMode('usda')"
        >
          <Globe class="w-4 h-4 text-blue-600" />
          <span>Tra cứu USDA online</span>
        </button>
      </div>

      <!-- Modal Body -->
      <div class="p-8 overflow-y-auto space-y-6 flex-1">
        <!-- Success Added Notification inside Modal -->
        <div
          v-if="lastAddedFood && !editingLog"
          class="p-4 bg-emerald-50 border border-emerald-200 text-emerald-900 rounded-md flex items-center justify-between text-xs animate-in fade-in slide-in-from-top-2 duration-200"
        >
          <div class="flex items-center gap-2.5">
            <CheckCircle2 class="w-4.5 h-4.5 text-emerald-600 shrink-0" />
            <span>
              Đã thêm <strong>{{ lastAddedFood.quantity }} {{ lastAddedFood.unit }} {{ lastAddedFood.name }} ({{ (lastAddedFood.state || '').toLowerCase() }})</strong> • <strong>{{ lastAddedFood.calories }} kcal</strong> vào nhật ký. Bạn có thể chọn tiếp món khác ở bên dưới!
            </span>
          </div>
          <button
            type="button"
            class="text-emerald-700 hover:text-emerald-950 p-1 cursor-pointer"
            @click="lastAddedFood = null"
          >
            <X class="w-4 h-4" />
          </button>
        </div>

        <!-- Search Input & Quick Chips -->
        <div v-if="!editingLog" class="space-y-3">
          <div class="relative">
            <div class="flex items-center justify-between mb-2">
              <label class="text-xs font-semibold text-slate-700 flex items-center gap-1.5">
                <Search class="w-3.5 h-3.5 text-slate-400" />
                <span>{{ searchMode === 'local' ? 'Tìm hoặc chọn món ăn' : 'Tra cứu USDA online' }}</span>
              </label>
              <span v-if="isSearching" class="text-xs text-slate-400 flex items-center gap-1.5">
                <Loader2 class="w-3 h-3 animate-spin text-emerald-600" /> Đang tìm kiếm...
              </span>
            </div>

            <div class="relative">
              <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                ref="searchInputRef"
                v-model="searchQuery"
                type="text"
                :placeholder="searchMode === 'local' ? 'Gõ để tìm món (VD: Ức gà, Cơm, Trứng...)' : 'Tìm món USDA (VD: Salmon, Beef...)'"
                class="w-full pl-11 pr-10 py-3 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600 transition-colors shadow-2xs"
                @input="onSearchInput"
                @focus="showSearchResults = true"
                @keydown.enter.prevent="selectFirstResult"
              />
              <button
                v-if="searchQuery"
                type="button"
                class="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-1 cursor-pointer"
                @click="searchQuery = ''; onSearchInput()"
              >
                <X class="w-4 h-4" />
              </button>
            </div>

            <!-- LOCAL Autocomplete Dropdown -->
            <div
              v-if="searchMode === 'local' && showSearchResults && searchResults.length > 0"
              class="absolute z-30 left-0 right-0 mt-2 bg-white border border-slate-200 rounded-md shadow-2xl max-h-80 overflow-y-auto divide-y divide-slate-100"
            >
              <div
                v-for="item in searchResults"
                :key="item.foodId || item.userFoodId"
                class="px-5 py-3 hover:bg-emerald-50/60 cursor-pointer transition-colors flex items-center justify-between group"
                @click="selectFood(item)"
              >
                <div>
                  <div class="flex items-center gap-2.5">
                    <span class="text-sm font-bold text-slate-900 group-hover:text-emerald-700 transition-colors">{{ item.name }}</span>
                    <span
                      v-if="item.isUserCustom"
                      class="px-2 py-0.5 text-[11px] font-medium bg-amber-50 text-amber-700 border border-amber-200 rounded-sm"
                    >
                      Món cá nhân
                    </span>
                    <span
                      v-else
                      class="px-2 py-0.5 text-[11px] font-medium bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-sm flex items-center gap-1"
                    >
                      <CheckCircle2 class="w-3 h-3 text-emerald-600" />
                      Verified source
                    </span>
                  </div>
                  <div class="text-xs text-slate-500 mt-1 flex items-center gap-2">
                    <span v-if="item.category" class="font-medium text-slate-600">{{ item.category }}</span>
                    <span>•</span>
                    <span>Trạng thái: {{ item.variants.map((v) => v.state).join(', ') }}</span>
                  </div>
                </div>
                <ChevronRight class="w-4 h-4 text-slate-400 group-hover:text-emerald-600 transition-colors" />
              </div>
            </div>

            <!-- USDA Online Results Dropdown -->
            <div
              v-if="searchMode === 'usda' && showSearchResults && usdaResults.length > 0"
              class="absolute z-30 left-0 right-0 mt-2 bg-white border border-slate-200 rounded-md shadow-2xl max-h-80 overflow-y-auto divide-y divide-slate-100"
            >
              <div
                v-for="item in usdaResults"
                :key="item.fdcId"
                class="px-5 py-3 hover:bg-blue-50/60 cursor-pointer transition-colors flex items-center justify-between group"
                @click="importAndSelectUsda(item)"
              >
                <div class="min-w-0 pr-3">
                  <div class="flex items-center gap-2.5">
                    <span class="text-sm font-bold text-slate-900 truncate group-hover:text-blue-700">{{ item.description }}</span>
                    <span class="px-2 py-0.5 text-[10px] font-medium bg-blue-50 text-blue-700 border border-blue-200 rounded-sm shrink-0">
                      USDA #{{ item.fdcId }}
                    </span>
                  </div>
                  <div class="text-xs text-slate-500 mt-1">
                    <span>{{ item.calories }} kcal • {{ item.protein }}g Protein • {{ item.carbohydrate }}g Carb • {{ item.fat }}g Fat / 100{{ item.servingSizeUnit || 'g' }}</span>
                  </div>
                </div>
                <button
                  type="button"
                  class="px-3.5 py-1.5 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-md shrink-0 flex items-center gap-1.5 shadow-xs cursor-pointer"
                >
                  <Download class="w-3.5 h-3.5" />
                  <span>Chọn món</span>
                </button>
              </div>
            </div>

            <!-- Empty search result with "Tạo thực phẩm mới" prompt -->
            <div
              v-if="showSearchResults && searchQuery.trim().length > 1 && ((searchMode === 'local' && searchResults.length === 0) || (searchMode === 'usda' && usdaResults.length === 0)) && !isSearching"
              class="absolute z-30 left-0 right-0 mt-2 p-6 bg-white border border-slate-200 rounded-md shadow-2xl text-center"
            >
              <AlertCircle class="w-7 h-7 text-slate-400 mx-auto mb-2.5" />
              <p class="text-sm text-slate-600 mb-3.5">Chưa tìm thấy thực phẩm "{{ searchQuery }}" trong hệ thống.</p>
              <div class="flex items-center justify-center gap-3">
                <button
                  v-if="searchMode === 'local'"
                  type="button"
                  class="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold bg-blue-600 hover:bg-blue-700 text-white rounded-md transition-colors shadow-xs cursor-pointer"
                  @click="switchSearchMode('usda'); onSearchInput()"
                >
                  <Globe class="w-3.5 h-3.5" />
                  <span>Tra cứu USDA online</span>
                </button>
                <button
                  type="button"
                  class="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white rounded-md transition-colors shadow-xs cursor-pointer"
                  @click="openCreateCustomModal"
                >
                  <Plus class="w-3.5 h-3.5" />
                  <span>Tạo món cá nhân</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Quick Food Suggestion Chips -->
          <div v-if="searchMode === 'local' && quickSuggestions.length > 0" class="flex items-center gap-2 overflow-x-auto pb-1 text-xs">
            <span class="text-slate-400 font-medium shrink-0">Món nhanh:</span>
            <button
              v-for="item in quickSuggestions"
              :key="item.foodId || item.userFoodId"
              type="button"
              :class="[
                'px-3 py-1 rounded-md border text-xs font-medium transition-colors shrink-0 cursor-pointer shadow-2xs',
                selectedFood && ((selectedFood.foodId && selectedFood.foodId === item.foodId) || (selectedFood.userFoodId && selectedFood.userFoodId === item.userFoodId))
                  ? 'bg-emerald-100/90 text-emerald-800 border-emerald-300 font-bold'
                  : 'bg-slate-50 text-slate-700 border-slate-200 hover:bg-emerald-50 hover:text-emerald-700',
              ]"
              @click="selectFood(item)"
            >
              {{ item.name }}
            </button>
            <button
              type="button"
              class="px-2.5 py-1 rounded-md border border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-100 text-xs font-semibold shrink-0 cursor-pointer transition-colors"
              @click="openCreateCustomModal"
            >
              + Món mới
            </button>
          </div>
        </div>

        <!-- Selected Food Banner -->
        <div v-if="selectedFood" class="p-4 bg-slate-50 border border-slate-200 rounded-md flex items-center justify-between shadow-2xs">
          <div class="flex items-center gap-3.5">
            <div class="p-2.5 rounded-md bg-emerald-100/80 text-emerald-700">
              <CheckCircle2 class="w-5 h-5" />
            </div>
            <div>
              <span class="text-xs text-slate-500 font-medium">Món ăn đang chọn:</span>
              <div class="text-base font-bold text-slate-900 flex items-center gap-2 mt-0.5">
                {{ selectedFood.name }}
                <span class="text-xs font-normal text-slate-500">
                  ({{ selectedFood.dataSource || 'Hệ thống' }})
                </span>
              </div>
            </div>
          </div>
          <button
            v-if="!editingLog"
            type="button"
            class="text-xs font-semibold text-emerald-600 hover:text-emerald-700 hover:underline px-3 py-1 cursor-pointer"
            @click="focusSearch"
          >
            Đổi món khác
          </button>
        </div>

        <!-- Food Details & Live Calculation Section (Spacious 2-column layout) -->
        <div v-if="selectedFood" class="grid grid-cols-1 lg:grid-cols-12 gap-7 items-start">
          <!-- Left Column: State & Quantity Configuration (7 cols) -->
          <div class="lg:col-span-7 space-y-6">
            <!-- Food State Selector (Sống / Chín / Luộc / Chiên / Nướng...) -->
            <div class="space-y-2.5">
              <label class="block text-xs font-semibold text-slate-700">
                Trạng thái thực phẩm <span class="text-rose-500">*</span>
              </label>
              <div class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-2.5">
                <button
                  v-for="state in availableStates"
                  :key="state"
                  type="button"
                  :class="[
                    'px-3.5 py-2.5 text-xs font-semibold rounded-md border text-center transition-colors shadow-2xs cursor-pointer',
                    selectedState === state
                      ? 'bg-emerald-600 text-white border-emerald-600 shadow-xs font-bold'
                      : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50',
                  ]"
                  @click="selectedState = state; updatePreview()"
                >
                  {{ state }}
                </button>
              </div>
              <p class="text-[11px] text-slate-400 mt-1">
                Lưu ý: Thực phẩm sống và chín/luộc/chiên có tỷ lệ calories & protein khác nhau trên 100g.
              </p>
            </div>

            <!-- Quantity & Unit Input -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-2">
                  Số lượng <span class="text-rose-500">*</span>
                </label>
                <input
                  ref="quantityInputRef"
                  v-model.number="quantity"
                  type="number"
                  step="any"
                  min="0.1"
                  placeholder="VD: 400 hoặc 4.5"
                  class="w-full px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600 shadow-2xs"
                  @input="updatePreview"
                  @keydown.enter.prevent="submitLog"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-2">
                  Đơn vị <span class="text-rose-500">*</span>
                </label>
                <select
                  v-model="selectedUnit"
                  class="w-full px-4 py-2.5 text-sm bg-white border border-slate-200 rounded-md focus:outline-hidden focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-600 shadow-2xs cursor-pointer"
                  @change="updatePreview"
                >
                  <option v-for="unit in availableUnits" :key="unit" :value="unit">
                    {{ unit }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Quick Quantity Preset Buttons -->
            <div class="space-y-1.5">
              <span class="text-xs text-slate-500 font-medium">Chọn nhanh số lượng:</span>
              <div class="flex flex-wrap items-center gap-2">
                <button
                  v-for="preset in quickQuantityPresets"
                  :key="preset.label"
                  type="button"
                  class="px-3 py-1 text-xs bg-slate-50 hover:bg-emerald-50 text-slate-700 hover:text-emerald-700 border border-slate-200 rounded-md transition-colors cursor-pointer shadow-2xs"
                  @click="applyQuantityPreset(preset)"
                >
                  {{ preset.label }}
                </button>
              </div>
            </div>
          </div>

          <!-- Right Column: Live Calculation Preview Card (5 cols) -->
          <div class="lg:col-span-5 p-6 bg-emerald-50/70 border border-emerald-200/80 rounded-md space-y-5">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2 text-xs font-bold text-emerald-950">
                <Sparkles class="w-4 h-4 text-emerald-600" />
                <span>Dinh dưỡng tạm tính:</span>
              </div>
              <span class="text-xs font-semibold text-emerald-800 bg-white px-3 py-1 rounded-sm border border-emerald-200 shadow-2xs">
                {{ quantity || 0 }} {{ selectedUnit }} ({{ (selectedState || '').toLowerCase() }})
              </span>
            </div>

            <!-- Prominent Calories Header Card -->
            <div class="p-4 bg-white rounded-md border border-emerald-100 shadow-2xs text-center">
              <span class="block text-xs font-medium text-slate-500">Năng lượng</span>
              <span class="text-2xl font-extrabold text-emerald-700 mt-1 block">
                {{ Math.round(previewNutrition.calories) }} <span class="text-sm font-semibold text-slate-500">kcal</span>
              </span>
            </div>

            <!-- 3 Core Macros Grid -->
            <div class="grid grid-cols-3 gap-3 text-center">
              <div class="p-3 bg-white rounded-md border border-emerald-100 shadow-2xs">
                <span class="block text-xs font-medium text-blue-600">Protein</span>
                <span class="text-base font-bold text-blue-700 mt-1 block">{{ previewNutrition.protein.toFixed(1) }}g</span>
              </div>
              <div class="p-3 bg-white rounded-md border border-emerald-100 shadow-2xs">
                <span class="block text-xs font-medium text-amber-600">Carb</span>
                <span class="text-base font-bold text-amber-700 mt-1 block">{{ previewNutrition.carbohydrate.toFixed(1) }}g</span>
              </div>
              <div class="p-3 bg-white rounded-md border border-emerald-100 shadow-2xs">
                <span class="block text-xs font-medium text-rose-600">Fat</span>
                <span class="text-base font-bold text-rose-700 mt-1 block">{{ previewNutrition.fat.toFixed(1) }}g</span>
              </div>
            </div>

            <div v-if="previewNutrition.fiber > 0" class="px-3.5 py-2 bg-white/80 rounded-md border border-emerald-100 text-xs text-slate-600 flex items-center justify-between">
              <span>Chất xơ (Fiber):</span>
              <span class="font-bold text-emerald-800">{{ previewNutrition.fiber.toFixed(1) }}g</span>
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <p v-if="errorMessage" class="text-xs text-rose-600 font-medium flex items-center gap-1.5">
          <AlertCircle class="w-4 h-4" />
          <span>{{ errorMessage }}</span>
        </p>
      </div>

      <!-- Modal Footer -->
      <div class="flex items-center justify-between px-8 py-5 bg-slate-50 border-t border-slate-100 shrink-0">
        <button
          type="button"
          class="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-200/60 rounded-md transition-colors cursor-pointer"
          @click="closeModal"
        >
          {{ lastAddedFood ? 'Hoàn tất / Đóng' : 'Đóng' }}
        </button>
        <button
          type="button"
          :disabled="isSubmitting || !isValid"
          class="inline-flex items-center gap-2 px-6 py-2.5 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white rounded-md shadow-sm transition-colors cursor-pointer"
          @click="submitLog"
        >
          <Plus v-if="!editingLog" class="w-4 h-4" />
          <Save v-else class="w-4 h-4" />
          <span>{{ editingLog ? 'Lưu thay đổi' : 'Thêm vào nhật ký (Enter)' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import {
  Utensils,
  X,
  Search,
  ChevronRight,
  Plus,
  Save,
  Globe,
  Download,
  Package,
  CheckCircle2,
  AlertCircle,
  Sparkles,
  Loader2,
} from 'lucide-vue-next';
import { nutritionService } from '../../services/nutrition.service';
import { useToastStore } from '../../stores/toast.store';
import type {
  NutritionFoodSearchItem,
  NutritionDailyLogItem,
  NutritionDailyLogRequest,
  UsdaFoodItem,
} from '../../types/nutrition';

const props = defineProps<{
  isOpen: boolean;
  logDate: string;
  editingLog?: NutritionDailyLogItem | null;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'saved'): void;
  (e: 'open-create-custom', initialName?: string): void;
}>();

const toast = useToastStore();
const lastAddedFood = ref<{ name: string; state?: string; quantity: number; unit: string; calories: number } | null>(null);

const searchInputRef = ref<HTMLInputElement | null>(null);
const quantityInputRef = ref<HTMLInputElement | null>(null);

const searchMode = ref<'local' | 'usda'>('local');
const searchQuery = ref('');
const allFoods = ref<NutritionFoodSearchItem[]>([]);
const searchResults = ref<NutritionFoodSearchItem[]>([]);
const usdaResults = ref<UsdaFoodItem[]>([]);
const isSearching = ref(false);
const showSearchResults = ref(false);

const selectedFood = ref<NutritionFoodSearchItem | null>(null);
const selectedState = ref('Sống');
const quantity = ref<number | null>(null);
const selectedUnit = ref('g');

const isSubmitting = ref(false);
const errorMessage = ref('');

const previewNutrition = ref({
  calories: 0,
  protein: 0,
  carbohydrate: 0,
  fat: 0,
  fiber: 0,
});

const formattedDate = computed(() => {
  if (!props.logDate) return '';
  const [y, m, d] = props.logDate.split('-');
  return `${d}/${m}/${y}`;
});

const EXACT_QUICK_FOODS = [
  'trứng gà',
  'ức gà',
  'cơm',
  'rau cải',
  'chuối',
  'bưởi',
  'đậu phụ',
  'dầu ăn',
];

const quickSuggestions = computed(() => {
  if (!allFoods.value.length) return [];
  const result: NutritionFoodSearchItem[] = [];
  for (const keyword of EXACT_QUICK_FOODS) {
    const found = allFoods.value.find((f) => f.name.toLowerCase().includes(keyword));
    if (found && !result.some((r) => (r.foodId && r.foodId === found.foodId) || (r.userFoodId && r.userFoodId === found.userFoodId))) {
      result.push(found);
    }
  }
  return result;
});

const availableStates = computed(() => {
  if (!selectedFood.value) return ['Sống', 'Chín', 'Luộc', 'Tươi', 'Chiên', 'Nướng', 'Tiêu chuẩn'];
  return selectedFood.value.variants.map((v) => v.state);
});

const availableUnits = computed(() => {
  const defaults = ['g', 'kg', 'ml', 'L'];
  if (!selectedFood.value) return defaults;
  const foodUnits = selectedFood.value.units.map((u) => u.unit);
  return Array.from(new Set([...foodUnits, ...defaults]));
});

const quickQuantityPresets = computed(() => {
  if (!selectedFood.value) return [];
  const currentUnit = selectedUnit.value.toLowerCase();
  if (currentUnit === 'g' || currentUnit === 'gram') {
    return [
      { label: '100g', amount: 100, unit: 'g' },
      { label: '200g', amount: 200, unit: 'g' },
      { label: '300g', amount: 300, unit: 'g' },
      { label: '400g', amount: 400, unit: 'g' },
      { label: '500g', amount: 500, unit: 'g' },
    ];
  } else if (currentUnit.includes('bát')) {
    return [
      { label: '0.5 bát', amount: 0.5, unit: selectedUnit.value },
      { label: '1 bát', amount: 1, unit: selectedUnit.value },
      { label: '1.5 bát', amount: 1.5, unit: selectedUnit.value },
      { label: '2 bát', amount: 2, unit: selectedUnit.value },
      { label: '3 bát', amount: 3, unit: selectedUnit.value },
      { label: '4.5 bát', amount: 4.5, unit: selectedUnit.value },
    ];
  } else if (currentUnit.includes('quả') || currentUnit.includes('trứng') || currentUnit.includes('cái')) {
    return [
      { label: '1 quả', amount: 1, unit: selectedUnit.value },
      { label: '2 quả', amount: 2, unit: selectedUnit.value },
      { label: '3 quả', amount: 3, unit: selectedUnit.value },
      { label: '4 quả', amount: 4, unit: selectedUnit.value },
      { label: '5 quả', amount: 5, unit: selectedUnit.value },
    ];
  }
  return [
    { label: '100g', amount: 100, unit: 'g' },
    { label: '200g', amount: 200, unit: 'g' },
    { label: '1 đơn vị', amount: 1, unit: selectedUnit.value },
  ];
});

const applyQuantityPreset = (preset: { label: string; amount: number; unit: string }) => {
  selectedUnit.value = preset.unit;
  quantity.value = preset.amount;
  updatePreview();
};

const isValid = computed(() => {
  return (
    selectedFood.value !== null &&
    selectedState.value &&
    quantity.value !== null &&
    quantity.value > 0 &&
    selectedUnit.value
  );
});

const switchSearchMode = (mode: 'local' | 'usda') => {
  searchMode.value = mode;
  searchResults.value = [];
  usdaResults.value = [];
  onSearchInput();
};

let searchTimeout: any = null;
const onSearchInput = () => {
  clearTimeout(searchTimeout);
  if (!searchQuery.value.trim()) {
    searchResults.value = allFoods.value.slice(0, 10);
    usdaResults.value = [];
    return;
  }
  isSearching.value = true;
  searchTimeout = setTimeout(async () => {
    try {
      if (searchMode.value === 'local') {
        searchResults.value = await nutritionService.searchFoods(searchQuery.value);
      } else {
        usdaResults.value = await nutritionService.searchUsdaFoods(searchQuery.value, 10);
      }
    } catch (e) {
      console.error('Failed to search foods:', e);
    } finally {
      isSearching.value = false;
    }
  }, 250);
};

const selectFirstResult = () => {
  if (searchMode.value === 'local' && searchResults.value.length > 0) {
    selectFood(searchResults.value[0]);
  } else if (searchMode.value === 'usda' && usdaResults.value.length > 0) {
    importAndSelectUsda(usdaResults.value[0]);
  }
};

const selectFood = (food: NutritionFoodSearchItem) => {
  selectedFood.value = food;
  showSearchResults.value = false;

  // Pick default state
  if (food.variants.length > 0) {
    selectedState.value = food.variants[0].state;
  }

  // Pick default unit & quantity
  if (food.units.length > 0) {
    selectedUnit.value = food.units[0].unit;
    quantity.value = 1;
  } else {
    selectedUnit.value = food.variants[0]?.servingUnit || 'g';
    quantity.value = food.variants[0]?.servingAmount || 100;
  }

  updatePreview();
  nextTick(() => {
    quantityInputRef.value?.focus();
  });
};

const importAndSelectUsda = async (usdaItem: UsdaFoodItem) => {
  isSearching.value = true;
  try {
    const imported = await nutritionService.importUsdaFood(usdaItem.fdcId);
    selectFood(imported);
    searchMode.value = 'local';
  } catch (e: any) {
    errorMessage.value = 'Không thể nhập món từ USDA: ' + (e.response?.data?.message || e.message);
  } finally {
    isSearching.value = false;
  }
};

const focusSearch = () => {
  searchQuery.value = '';
  showSearchResults.value = true;
  searchResults.value = allFoods.value.slice(0, 10);
  nextTick(() => {
    searchInputRef.value?.focus();
  });
};

const openCreateCustomModal = () => {
  const name = searchQuery.value.trim();
  closeModal();
  emit('open-create-custom', name);
};

const updatePreview = () => {
  if (!selectedFood.value || !quantity.value || quantity.value <= 0) {
    previewNutrition.value = { calories: 0, protein: 0, carbohydrate: 0, fat: 0, fiber: 0 };
    return;
  }

  const variant = selectedFood.value.variants.find(
    (v) => v.state.toLowerCase() === selectedState.value.toLowerCase()
  ) || selectedFood.value.variants[0];

  if (!variant) return;

  const servingAmount = variant.servingAmount || 100;
  let multiplier = 1;
  const unit = selectedUnit.value.toLowerCase();

  if (unit === 'g' || unit === 'gram') {
    multiplier = quantity.value / servingAmount;
  } else if (unit === 'kg') {
    multiplier = (quantity.value * 1000) / servingAmount;
  } else if (unit === 'ml') {
    multiplier = quantity.value / servingAmount;
  } else if (unit === 'l' || unit === 'lít') {
    multiplier = (quantity.value * 1000) / servingAmount;
  } else {
    // Check conversion in units
    const foodUnit = selectedFood.value.units.find(
      (u) => u.unit.toLowerCase() === selectedUnit.value.toLowerCase()
    );
    if (foodUnit) {
      if (foodUnit.gramValue) {
        multiplier = (quantity.value * foodUnit.gramValue) / servingAmount;
      } else if (foodUnit.mlValue) {
        multiplier = (quantity.value * foodUnit.mlValue) / servingAmount;
      }
    } else {
      multiplier = quantity.value;
    }
  }

  previewNutrition.value = {
    calories: (variant.calories || 0) * multiplier,
    protein: (variant.protein || 0) * multiplier,
    carbohydrate: (variant.carbohydrate || 0) * multiplier,
    fat: (variant.fat || 0) * multiplier,
    fiber: (variant.fiber || 0) * multiplier,
  };
};

const submitLog = async () => {
  if (!isValid.value || !selectedFood.value || !quantity.value) return;

  errorMessage.value = '';
  isSubmitting.value = true;

  try {
    const variant = selectedFood.value.variants.find(
      (v) => v.state.toLowerCase() === selectedState.value.toLowerCase()
    ) || selectedFood.value.variants[0];

    const payload: NutritionDailyLogRequest = {
      logDate: props.logDate,
      foodVariantId: !selectedFood.value.isUserCustom ? variant?.id : undefined,
      userFoodId: selectedFood.value.isUserCustom ? selectedFood.value.userFoodId : undefined,
      quantity: quantity.value,
      unit: selectedUnit.value,
    };

    if (props.editingLog) {
      await nutritionService.updateDailyLog(props.editingLog.id, payload);
      toast.success('Đã cập nhật món ăn thành công!');
      emit('saved');
      closeModal();
    } else {
      await nutritionService.addDailyLog(payload);
      const addedItem = {
        name: selectedFood.value.name,
        state: selectedState.value,
        quantity: quantity.value,
        unit: selectedUnit.value,
        calories: Math.round(previewNutrition.value.calories),
      };
      lastAddedFood.value = addedItem;
      toast.success(`Đã thêm "${addedItem.quantity} ${addedItem.unit} ${addedItem.name} (${(addedItem.state || '').toLowerCase()})" vào nhật ký!`);
      emit('saved');

      // Clear search & input for the next food addition
      searchQuery.value = '';
      showSearchResults.value = false;
      errorMessage.value = '';
      quantity.value = 100;

      // Focus search box so user can immediately type the next item
      nextTick(() => {
        searchInputRef.value?.focus();
      });
    }
  } catch (err: any) {
    errorMessage.value = err.response?.data?.message || 'Có lỗi xảy ra khi lưu thực phẩm';
  } finally {
    isSubmitting.value = false;
  }
};

const closeModal = () => {
  emit('close');
};

watch(
  () => props.isOpen,
  async (open) => {
    if (open) {
      errorMessage.value = '';
      lastAddedFood.value = null;
      searchMode.value = 'local';
      searchQuery.value = '';
      showSearchResults.value = false;

      // Load all foods for quick selection
      try {
        allFoods.value = await nutritionService.searchFoods('');
      } catch (e) {
        console.error('Failed to load all foods:', e);
      }

      if (props.editingLog) {
        // Pre-fill for edit
        quantity.value = props.editingLog.quantity;
        selectedUnit.value = props.editingLog.unit;
        selectedState.value = props.editingLog.state;
        searchQuery.value = '';

        if (props.editingLog.foodVariantId) {
          try {
            const detail = await nutritionService.getFoodDetail(props.editingLog.foodVariantId);
            selectedFood.value = detail;
          } catch (e) {
            console.error('Failed to load food detail for edit:', e);
          }
        }
        updatePreview();
      } else {
        // When opening for ADD: Auto-select Ức gà / first popular item so all form fields are visible
        if (quickSuggestions.value.length > 0) {
          selectFood(quickSuggestions.value[0]);
        } else if (allFoods.value.length > 0) {
          selectFood(allFoods.value[0]);
        }
      }
    }
  }
);
</script>
