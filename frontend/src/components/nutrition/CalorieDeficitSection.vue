<template>
  <div class="bg-white rounded-md border border-slate-200 overflow-hidden shadow-2xs">
    <!-- Header -->
    <div class="px-5 py-4 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 bg-gradient-to-r from-slate-50/70 to-white">
      <div class="flex items-center gap-3">
        <div class="p-2.5 rounded-md bg-rose-50 text-rose-600 border border-rose-100">
          <Flame class="w-5 h-5" />
        </div>
        <div>
          <h2 class="text-lg sm:text-xl font-bold text-slate-900 flex flex-wrap items-center gap-2.5">
            <span>Calories tiêu thụ & thâm hụt</span>
            <span
              :class="[
                'px-2.5 py-0.5 text-xs font-bold rounded-sm border',
                statusBadgeClass
              ]"
            >
              {{ statusDisplay }}
            </span>
          </h2>
          <p class="text-xs text-slate-500 mt-0.5">
            Theo dõi TDEE nền, năng lượng tập luyện theo MET và cán cân thâm hụt calo ngày {{ formattedDate }}
          </p>
        </div>
      </div>

      <!-- Quick TDEE & Weight Settings -->
      <div class="flex items-center gap-3 self-end sm:self-auto">
        <div class="flex items-center gap-1.5 bg-slate-50 border border-slate-200 rounded-md px-2.5 py-1.5">
          <span class="text-xs font-semibold text-slate-500">TDEE nền:</span>
          <input
            v-model.number="localTdee"
            type="number"
            min="500"
            max="6000"
            step="50"
            class="w-16 text-xs font-bold text-slate-900 bg-white border border-slate-200 rounded px-1.5 py-0.5 text-right focus:outline-emerald-500"
            @change="onTdeeOrWeightChange"
          />
          <span class="text-[11px] text-slate-400 font-medium">kcal</span>
        </div>

        <div class="flex items-center gap-1.5 bg-slate-50 border border-slate-200 rounded-md px-2.5 py-1.5">
          <span class="text-xs font-semibold text-slate-500">Cân nặng:</span>
          <input
            v-model.number="localWeight"
            type="number"
            min="20"
            max="300"
            step="0.5"
            class="w-14 text-xs font-bold text-slate-900 bg-white border border-slate-200 rounded px-1.5 py-0.5 text-right focus:outline-emerald-500"
            @change="onTdeeOrWeightChange"
          />
          <span class="text-[11px] text-slate-400 font-medium">kg</span>
        </div>
      </div>
    </div>



    <!-- Main Content: Add Activity Form & Activity Log Table -->
    <div class="p-5 space-y-5">
      <!-- Add Activity Form -->
      <div class="bg-slate-50 p-5 rounded-md border border-slate-200">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4 pb-3 border-b border-slate-200/80">
          <div class="flex items-center gap-3">
            <div class="p-2.5 rounded-md bg-rose-50 text-rose-600 border border-rose-100">
              <Dumbbell class="w-5 h-5" />
            </div>
            <div>
              <h2 class="text-lg sm:text-xl font-bold text-slate-900">
                Ghi nhận hoạt động thể chất hôm nay
              </h2>
              <p class="text-xs text-slate-500 mt-0.5">
                Tính năng lượng tiêu hao chuẩn theo MET Compendium
              </p>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleAddActivity" class="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
          <!-- Select Activity -->
          <div class="md:col-span-6">
            <label class="block text-xs font-semibold text-slate-700 mb-1">
              Chọn hoạt động <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <select
                v-model="selectedActivityId"
                class="w-full text-xs font-medium text-slate-800 bg-white border border-slate-200 rounded-md px-3 py-2 pr-8 focus:outline-emerald-500 focus:border-emerald-500 cursor-pointer"
                required
                @change="updatePreviewKcal"
              >
                <option :value="null" disabled>-- Chọn hoạt động thể chất --</option>
                <optgroup v-for="(group, catName) in groupedActivities" :key="catName" :label="formatCategory(catName)">
                  <option
                    v-for="act in group"
                    :key="act.id"
                    :value="act.id"
                  >
                    {{ act.name }} (MET: {{ act.metValue }} - {{ formatIntensity(act.intensity) }})
                  </option>
                </optgroup>
              </select>
            </div>
          </div>

          <!-- Duration Input -->
          <div class="md:col-span-3">
            <label class="block text-xs font-semibold text-slate-700 mb-1">
              Thời gian (phút) <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <input
                v-model.number="durationMinutes"
                type="number"
                min="1"
                max="600"
                step="1"
                placeholder="Ví dụ: 20"
                class="w-full text-xs font-semibold text-slate-800 bg-white border border-slate-200 rounded-md px-3 py-2 focus:outline-emerald-500 focus:border-emerald-500"
                required
                @input="updatePreviewKcal"
              />
              <span class="absolute right-3 top-2 text-xs text-slate-400 font-medium">phút</span>
            </div>
          </div>

          <!-- Real-time Preview & Submit Button -->
          <div class="md:col-span-3 flex items-center gap-2">
            <div class="flex-1 text-center bg-white border border-slate-200 rounded-md py-1.5 px-2">
              <span class="block text-[10px] text-slate-400 font-medium">Net Tiêu thụ</span>
              <span class="text-xs font-bold text-rose-600">≈ {{ previewKcal }} kcal</span>
            </div>
            <button
              type="submit"
              :disabled="isSubmitting || !selectedActivityId || !durationMinutes"
              class="inline-flex items-center justify-center gap-1.5 px-4 py-2 text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-md shadow-xs transition-colors cursor-pointer whitespace-nowrap"
            >
              <Plus class="w-4 h-4" />
              <span>{{ isSubmitting ? 'Đang lưu...' : 'Thêm' }}</span>
            </button>
          </div>
        </form>

        <!-- Quick Duration Shortcuts -->
        <div class="flex flex-wrap items-center gap-2 mt-3 pt-2.5 border-t border-slate-200/70">
          <span class="text-[11px] text-slate-400 font-medium">Thời gian nhanh:</span>
          <button
            v-for="mins in [10, 15, 20, 30, 45, 60]"
            :key="mins"
            type="button"
            class="px-2 py-0.5 text-[11px] font-medium bg-white hover:bg-rose-50 text-slate-600 hover:text-rose-700 border border-slate-200 hover:border-rose-200 rounded transition-colors cursor-pointer"
            @click="setQuickDuration(mins)"
          >
            {{ mins }} phút
          </button>
        </div>
      </div>

      <!-- Activity Logs Table -->
      <div class="bg-white rounded-md border border-slate-200 overflow-hidden shadow-2xs">
        <div class="px-4 py-3 border-b border-slate-100 flex flex-wrap items-center justify-between gap-3">
          <div class="flex items-center gap-2.5">
            <h4 class="text-xs font-bold text-slate-900">
              Danh sách hoạt động đã ghi nhận hôm nay
            </h4>
            <span class="px-2 py-0.5 text-[11px] font-semibold bg-slate-100 text-slate-700 rounded-full">
              {{ activityLogs.length }}
            </span>

            <!-- Nút Thêm ảnh cạnh tiêu đề -->
            <button
              type="button"
              class="inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold rounded-md border transition-colors cursor-pointer bg-white hover:bg-rose-50 text-rose-600 border-rose-200 shadow-2xs"
              @click="openUploadModal"
            >
              <Camera class="w-3.5 h-3.5" />
              <span>Thêm ảnh</span>
              <span v-if="todayPhotos.length > 0" class="px-1.5 py-0.2 text-[10px] font-bold bg-rose-100 text-rose-700 rounded-full">
                {{ todayPhotos.length }}
              </span>
            </button>
          </div>

          <div class="flex items-center gap-3">
            <button
              v-if="todayPhotos.length > 0"
              type="button"
              class="text-xs font-semibold text-slate-500 hover:text-rose-600 transition-colors inline-flex items-center gap-1 cursor-pointer"
              @click="openGalleryModal"
            >
              <Images class="w-3.5 h-3.5" />
              <span>Xem {{ todayPhotos.length }} ảnh</span>
            </button>
            <span class="text-xs font-bold text-rose-600">
              Tổng cộng: +{{ Math.round(deficitSummary.activityCalories) }} kcal
            </span>
          </div>
        </div>

        <!-- Compact Photo Strip if photos exist -->
        <div v-if="todayPhotos.length > 0" class="px-4 py-2 bg-rose-50/40 border-b border-rose-100/60 flex items-center gap-2.5 overflow-x-auto">
          <span class="text-[11px] font-bold text-rose-800 shrink-0 flex items-center gap-1">
            <Camera class="w-3.5 h-3.5 text-rose-600" />
            <span>Ảnh tập:</span>
          </span>

          <div
            v-for="photo in todayPhotos"
            :key="photo.id"
            class="relative group shrink-0 w-11 h-11 rounded-md overflow-hidden border border-rose-200 cursor-pointer shadow-2xs"
            @click="previewPhotoItem(photo)"
          >
            <img :src="photo.imageUrl" :alt="photo.caption || 'Ảnh tập'" class="w-full h-full object-cover group-hover:scale-110 transition-transform" />
            <div class="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white">
              <Maximize2 class="w-3.5 h-3.5" />
            </div>
          </div>

          <button
            type="button"
            class="w-11 h-11 rounded-md border border-dashed border-rose-300 hover:border-rose-500 bg-white hover:bg-rose-50 flex flex-col items-center justify-center text-rose-600 text-[10px] font-bold shrink-0 transition-colors cursor-pointer"
            title="Thêm ảnh khác"
            @click="openUploadModal"
          >
            <Plus class="w-3.5 h-3.5" />
          </button>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left text-xs border-collapse">
            <thead>
              <tr class="bg-slate-50/80 border-b border-slate-100 text-slate-600 font-semibold">
                <th class="py-2.5 px-3">Hoạt động</th>
                <th class="py-2.5 px-2.5 text-center">Phân loại</th>
                <th class="py-2.5 px-2.5 text-center">Cường độ</th>
                <th class="py-2.5 px-3 text-right">Thời gian</th>
                <th class="py-2.5 px-2.5 text-right">MET Snapshot</th>
                <th class="py-2.5 px-2.5 text-right">Weight</th>
                <th class="py-2.5 px-3 text-right text-rose-600">Net Calories</th>
                <th class="py-2.5 px-3 text-center w-20">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="log in activityLogs"
                :key="log.id"
                class="hover:bg-slate-50/70 transition-colors group"
              >
                <!-- Name -->
                <td class="py-3 px-3">
                  <span class="font-bold text-slate-900">{{ log.activityName }}</span>
                </td>

                <!-- Category -->
                <td class="py-3 px-2.5 text-center">
                  <span :class="['px-2 py-0.5 text-[10px] font-semibold rounded-sm border', getCategoryBadgeClass(log.category)]">
                    {{ formatCategory(log.category) }}
                  </span>
                </td>

                <!-- Intensity -->
                <td class="py-3 px-2.5 text-center">
                  <span :class="['px-2 py-0.5 text-[10px] font-semibold rounded-sm border', getIntensityBadgeClass(log.intensity)]">
                    {{ formatIntensity(log.intensity) }}
                  </span>
                </td>

                <!-- Duration (Editable) -->
                <td class="py-3 px-3 text-right font-medium text-slate-800">
                  <div v-if="editingLogId === log.id" class="flex items-center justify-end gap-1">
                    <input
                      v-model.number="editDurationValue"
                      type="number"
                      min="1"
                      max="600"
                      class="w-14 text-xs font-bold text-slate-900 bg-white border border-emerald-400 rounded px-1.5 py-0.5 text-right focus:outline-emerald-500"
                      @keyup.enter="saveEditLog(log)"
                      @keyup.esc="cancelEdit"
                    />
                    <span class="text-[11px] text-slate-500">p</span>
                    <button
                      type="button"
                      class="p-1 text-emerald-600 hover:bg-emerald-50 rounded cursor-pointer"
                      title="Lưu"
                      @click="saveEditLog(log)"
                    >
                      <Check class="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      class="p-1 text-slate-400 hover:bg-slate-100 rounded cursor-pointer"
                      title="Hủy"
                      @click="cancelEdit"
                    >
                      <X class="w-3.5 h-3.5" />
                    </button>
                  </div>
                  <div v-else class="flex items-center justify-end gap-1.5">
                    <span class="font-bold">{{ log.durationMinutes }}</span>
                    <span class="text-slate-500 font-normal">phút</span>
                  </div>
                </td>

                <!-- MET Snapshot -->
                <td class="py-3 px-2.5 text-right font-mono text-slate-600">
                  {{ log.metValue ? Number(log.metValue).toFixed(1) : '-' }}
                </td>

                <!-- Weight Snapshot -->
                <td class="py-3 px-2.5 text-right font-mono text-slate-600">
                  {{ log.weightKg ? Number(log.weightKg).toFixed(1) : '-' }} kg
                </td>

                <!-- Calories Burned -->
                <td class="py-3 px-3 text-right font-bold text-rose-600">
                  ≈ {{ Math.round(log.caloriesBurned) }} kcal
                </td>

                <!-- Actions -->
                <td class="py-3 px-3 text-center">
                  <div class="flex items-center justify-center gap-1">
                    <button
                      v-if="editingLogId !== log.id"
                      type="button"
                      class="p-1 text-slate-400 hover:text-emerald-600 hover:bg-slate-100 rounded transition-colors cursor-pointer"
                      title="Chỉnh sửa thời gian"
                      @click="startEdit(log)"
                    >
                      <Edit3 class="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      class="p-1 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition-colors cursor-pointer"
                      title="Xóa hoạt động"
                      @click="confirmDeleteLog(log)"
                    >
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>

                  </div>
                </td>
              </tr>

              <!-- Empty State -->
              <tr v-if="activityLogs.length === 0">
                <td colspan="8" class="py-8 text-center text-slate-400">
                  <div class="flex flex-col items-center justify-center space-y-2">
                    <Dumbbell class="w-6 h-6 text-slate-300" />
                    <p class="text-xs font-medium text-slate-600">Chưa có hoạt động thể chất nào được ghi nhận cho ngày này.</p>
                    <div class="flex flex-wrap justify-center gap-1.5 mt-2">
                      <button
                        v-for="rec in quickRecommendations"
                        :key="rec.name"
                        type="button"
                        class="px-2.5 py-1 text-[11px] font-medium bg-slate-50 hover:bg-rose-50 text-slate-600 hover:text-rose-700 border border-slate-200 hover:border-rose-200 rounded transition-colors cursor-pointer"
                        @click="selectRecommendation(rec)"
                      >
                        + {{ rec.name }} ({{ rec.mins }}p)
                      </button>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Confirm Delete Activity Modal -->
    <AppConfirmDialog
      v-model="showDeleteConfirm"
      title="Xóa hoạt động tập luyện"
      :message="deletingActivity ? `Bạn có chắc chắn muốn xóa hoạt động '${deletingActivity.activityName}' (${deletingActivity.durationMinutes} phút) khỏi nhật ký không?` : 'Bạn có chắc chắn muốn xóa hoạt động này không?'"
      confirm-text="Xóa hoạt động"
      confirm-variant="danger"
      :loading="isDeletingActivity"
      @confirm="executeDeleteActivity"
    />

    <!-- Confirm Delete Photo Modal -->
    <AppConfirmDialog
      v-model="showDeletePhotoConfirm"
      title="Xóa ảnh tập luyện"
      message="Bạn có chắc chắn muốn xóa ảnh tập luyện này không? Hành động này không thể hoàn tác."
      confirm-text="Xóa ảnh"
      confirm-variant="danger"
      :loading="isDeletingPhoto"
      @confirm="executeDeletePhoto"
    />

    <!-- Modal 1: Upload Photo Modal -->
    <AppModal v-model="showUploadModal" title="Thêm ảnh tập luyện / Check-in" size="md">
      <form class="space-y-4" @submit.prevent="submitPhoto">
        <div>
          <label class="block text-xs font-bold text-slate-700 mb-1">Ngày ghi nhận</label>
          <input
            v-model="uploadForm.logDate"
            type="date"
            class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-white focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500"
            required
          />
        </div>

        <div>
          <label class="block text-xs font-bold text-slate-700 mb-1">Chọn ảnh từ máy</label>
          <div
            class="border-2 border-dashed border-slate-200 hover:border-rose-400 rounded-lg p-4 text-center cursor-pointer bg-slate-50/50 hover:bg-rose-50/20 transition-all relative"
            @click="triggerFileInput"
          >
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleFileSelected"
            />
            <div v-if="previewUrl" class="space-y-2">
              <img :src="previewUrl" alt="Preview" class="max-h-48 mx-auto rounded-md object-contain shadow-2xs" />
              <p class="text-xs font-semibold text-rose-600">Nhấn để chọn ảnh khác</p>
            </div>
            <div v-else class="space-y-1.5 py-4">
              <UploadCloud class="w-8 h-8 text-slate-400 mx-auto" />
              <p class="text-xs font-bold text-slate-700">Nhấn để chọn ảnh tập luyện</p>
              <p class="text-[11px] text-slate-400">Hỗ trợ JPG, PNG, WEBP</p>
            </div>
          </div>
        </div>

        <div v-if="!selectedFile">
          <label class="block text-xs font-bold text-slate-700 mb-1">Hoặc dán đường dẫn ảnh (URL)</label>
          <AppInput
            id="photo-url"
            v-model="uploadForm.imageUrl"
            placeholder="https://..."
            @input="onUrlInput"
          />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            id="photo-weight"
            v-model.number="uploadForm.weightKg"
            label="Cân nặng hôm nay (kg) (tùy chọn)"
            type="number"
            step="0.1"
            placeholder="68.5"
          />

          <AppInput
            id="photo-caption"
            v-model="uploadForm.caption"
            label="Ghi chú buổi tập"
            placeholder="Ví dụ: Tập ngực + bụng..."
          />
        </div>

        <div class="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" type="button" @click="showUploadModal = false">
            Hủy
          </AppButton>
          <AppButton variant="primary" size="md" type="submit" :loading="isUploading" :disabled="!selectedFile && !uploadForm.imageUrl">
            Lưu ảnh tập
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- Modal 2: Gallery Modal -->
    <AppModal v-model="showGalleryModal" :title="`Album ảnh tập luyện ngày ${formattedDate}`" size="lg">
      <div class="space-y-4">
        <div v-if="todayPhotos.length === 0" class="py-8 text-center bg-slate-50 rounded-lg border border-dashed border-slate-200 text-xs text-slate-500">
          Chưa có ảnh nào được lưu cho ngày này.
        </div>
        <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
          <div
            v-for="photo in todayPhotos"
            :key="photo.id"
            class="relative bg-white rounded-lg border border-slate-200 overflow-hidden shadow-2xs group"
          >
            <div class="aspect-square bg-slate-100 relative overflow-hidden cursor-pointer" @click="previewPhotoItem(photo)">
              <img :src="photo.imageUrl" :alt="photo.caption || 'Ảnh tập'" class="w-full h-full object-cover group-hover:scale-105 transition-transform" />
              <div class="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-white">
                <Maximize2 class="w-5 h-5" />
              </div>
              <span v-if="photo.weightKg" class="absolute top-1.5 left-1.5 px-1.5 py-0.5 text-[10px] font-bold rounded bg-black/60 text-white backdrop-blur-xs inline-flex items-center gap-0.5">
                <Scale class="w-2.5 h-2.5 text-rose-300" />
                <span>{{ photo.weightKg }} kg</span>
              </span>
            </div>
            <div class="p-2 bg-white flex items-center justify-between text-xs">
              <span class="truncate text-slate-700 font-medium text-[11px]">{{ photo.caption || 'Ảnh tập' }}</span>
              <button
                type="button"
                title="Xóa ảnh"
                class="text-slate-400 hover:text-rose-600 transition-colors p-1 cursor-pointer"
                @click="confirmDeletePhoto(photo.id)"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>

        <div class="flex justify-between items-center pt-3 border-t border-slate-100">
          <AppButton variant="outline" size="sm" :icon="Plus" @click="openUploadModal">
            Thêm ảnh mới
          </AppButton>
          <AppButton variant="secondary" size="sm" @click="showGalleryModal = false">
            Đóng
          </AppButton>
        </div>
      </div>
    </AppModal>

    <!-- Modal 3: Preview Zoom Photo -->
    <AppModal v-model="showPreviewModal" :title="previewPhoto?.caption || 'Chi tiết ảnh tập luyện'" size="lg">
      <div v-if="previewPhoto" class="space-y-3">
        <div class="bg-black/90 rounded-lg overflow-hidden flex items-center justify-center p-2 max-h-[70vh]">
          <img
            :src="previewPhoto.imageUrl"
            :alt="previewPhoto.caption || 'Ảnh tập luyện'"
            class="max-h-[65vh] w-auto object-contain rounded"
          />
        </div>
        <div class="flex items-center justify-between text-xs text-slate-600 bg-slate-50 p-3 rounded-lg">
          <div>
            <span class="font-bold text-slate-800">Ngày: {{ formattedDate }}</span>
            <span v-if="previewPhoto.weightKg" class="ml-3 font-semibold text-rose-600 inline-flex items-center gap-1">
              <Scale class="w-3 h-3 text-rose-500" />
              <span>Cân nặng: {{ previewPhoto.weightKg }} kg</span>
            </span>
          </div>
          <button
            type="button"
            class="text-rose-600 hover:text-rose-800 font-semibold inline-flex items-center gap-1 cursor-pointer"
            @click="confirmDeletePhoto(previewPhoto.id)"
          >
            <Trash2 class="w-3.5 h-3.5" />
            <span>Xóa ảnh này</span>
          </button>
        </div>
      </div>
    </AppModal>
  </div>
</template>


<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import {
  Flame,
  Plus,
  Trash2,
  Edit3,
  Check,
  X,
  Dumbbell,
  Scale,
  Camera,
  Images,
  Maximize2,
  UploadCloud,
} from 'lucide-vue-next';
import { nutritionService } from '../../services/nutrition.service';
import { healthPhotoService } from '../../services/health-photo.service';
import type { HealthWorkoutPhoto } from '../../types/health-photo.types';
import AppConfirmDialog from '../common/AppConfirmDialog.vue';
import AppModal from '../common/AppModal.vue';
import AppInput from '../common/AppInput.vue';
import AppButton from '../common/AppButton.vue';
import { useToastStore } from '../../stores/toast.store';
import type {
  NutritionActivity,
  NutritionActivityLog,
  CalorieDeficitSummary,
} from '../../types/nutrition';

const toast = useToastStore();

const props = defineProps<{
  logDate: string;
  foodCalories: number;
  initialTdee?: number;
  initialWeight?: number;
}>();

const emit = defineEmits<{
  (e: 'updated', summary: CalorieDeficitSummary): void;
}>();

// State
const activities = ref<NutritionActivity[]>([]);
const activityLogs = ref<NutritionActivityLog[]>([]);
const showDeleteConfirm = ref<boolean>(false);
const deletingActivity = ref<NutritionActivityLog | null>(null);
const isDeletingActivity = ref<boolean>(false);

// Photos State
const todayPhotos = ref<HealthWorkoutPhoto[]>([]);
const showUploadModal = ref(false);
const showGalleryModal = ref(false);
const showPreviewModal = ref(false);
const previewPhoto = ref<HealthWorkoutPhoto | null>(null);

const showDeletePhotoConfirm = ref(false);
const deletingPhotoId = ref<number | null>(null);
const isDeletingPhoto = ref(false);

const fileInputRef = ref<HTMLInputElement | null>(null);
const selectedFile = ref<File | null>(null);
const previewUrl = ref<string | null>(null);
const isUploading = ref(false);

const uploadForm = reactive({
  logDate: props.logDate || new Date().toISOString().split('T')[0],
  imageUrl: '',
  caption: '',
  weightKg: undefined as number | undefined,
});

const deficitSummary = ref<CalorieDeficitSummary>({
  logDate: props.logDate,
  weightKg: props.initialWeight || 68,
  tdee: props.initialTdee || 2100,
  foodCalories: props.foodCalories || 0,
  activityCalories: 0,
  totalCaloriesBurned: (props.initialTdee || 2100),
  calorieBalance: (props.initialTdee || 2100) - (props.foodCalories || 0),
  status: 'DEFICIT',
  activityLogs: [],
  loggedFoodsCount: 0,
});

const localTdee = ref<number>(props.initialTdee || 2100);
const localWeight = ref<number>(props.initialWeight || 68);

const selectedActivityId = ref<number | null>(null);
const durationMinutes = ref<number | null>(20);
const previewKcal = ref<number>(0);
const isSubmitting = ref<boolean>(false);

const editingLogId = ref<number | null>(null);
const editDurationValue = ref<number>(20);

// Recommendations for empty state
const quickRecommendations = [
  { name: 'Nhảy dây', mins: 20 },
  { name: 'Tabata / HIIT', mins: 12 },
  { name: 'Tập tạ - nhiều bài', mins: 60 },
  { name: 'Cầu lông', mins: 60 },
];

// Computed
const formattedDate = computed(() => {
  if (!props.logDate) return '';
  const [year, month, day] = props.logDate.split('-');
  return `${day}/${month}/${year}`;
});

const groupedActivities = computed(() => {
  const groups: Record<string, NutritionActivity[]> = {};
  for (const act of activities.value) {
    const cat = act.category || 'OTHER';
    if (!groups[cat]) groups[cat] = [];
    groups[cat].push(act);
  }
  return groups;
});

const statusDisplay = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'Thâm hụt';
    case 'SURPLUS':
      return 'Thặng dư';
    default:
      return 'Cân bằng';
  }
});

const statusBadgeClass = computed(() => {
  switch (deficitSummary.value.status) {
    case 'DEFICIT':
      return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    case 'SURPLUS':
      return 'bg-amber-50 text-amber-700 border-amber-200';
    default:
      return 'bg-blue-50 text-blue-700 border-blue-200';
  }
});


// Methods
const formatCategory = (category: string) => {
  const map: Record<string, string> = {
    CARDIO: 'Cardio',
    STRENGTH: 'Tập tạ (Strength)',
    CALISTHENICS: 'Calisthenics',
    SPORTS: 'Thể thao',
    GENERAL: 'Phổ thông',
  };
  return map[category] || category;
};

const formatIntensity = (intensity: string) => {
  const map: Record<string, string> = {
    HIGH: 'Cường độ cao',
    MODERATE: 'Trung bình',
    LOW: 'Nhẹ nhàng',
  };
  return map[intensity] || intensity;
};

const getCategoryBadgeClass = (category: string) => {
  const map: Record<string, string> = {
    CARDIO: 'bg-rose-50 text-rose-700 border-rose-200',
    STRENGTH: 'bg-blue-50 text-blue-700 border-blue-200',
    CALISTHENICS: 'bg-purple-50 text-purple-700 border-purple-200',
    SPORTS: 'bg-amber-50 text-amber-700 border-amber-200',
  };
  return map[category] || 'bg-slate-50 text-slate-700 border-slate-200';
};

const getIntensityBadgeClass = (intensity: string) => {
  const map: Record<string, string> = {
    HIGH: 'bg-rose-50 text-rose-700 border-rose-200',
    MODERATE: 'bg-amber-50 text-amber-700 border-amber-200',
    LOW: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  };
  return map[intensity] || 'bg-slate-50 text-slate-700 border-slate-200';
};

// Calculate real-time preview Net MET Calories
const updatePreviewKcal = () => {
  if (!selectedActivityId.value || !durationMinutes.value || durationMinutes.value <= 0) {
    previewKcal.value = 0;
    return;
  }
  const act = activities.value.find((a) => a.id === selectedActivityId.value);
  if (!act) {
    previewKcal.value = 0;
    return;
  }
  const met = Number(act.metValue);
  const weight = Number(localWeight.value);
  const duration = Number(durationMinutes.value);
  // Net MET formula: (MET - 1) * 3.5 * weight / 200 * duration
  const netMet = Math.max(0, met - 1);
  const cal = (netMet * 3.5 * weight * duration) / 200;
  previewKcal.value = Math.round(cal);
};

const setQuickDuration = (mins: number) => {
  durationMinutes.value = mins;
  updatePreviewKcal();
};

const selectRecommendation = (rec: { name: string; mins: number }) => {
  const found = activities.value.find((a) => a.name.toLowerCase().includes(rec.name.toLowerCase()));
  if (found) {
    selectedActivityId.value = found.id;
    durationMinutes.value = rec.mins;
    updatePreviewKcal();
  }
};

const loadActivities = async () => {
  try {
    activities.value = await nutritionService.getActivities();
    if (activities.value.length > 0 && !selectedActivityId.value) {
      selectedActivityId.value = activities.value[0].id;
      updatePreviewKcal();
    }
  } catch (err) {
    console.error('Failed to load activities:', err);
  }
};

const fetchDeficitData = async () => {
  try {
    const summary = await nutritionService.getDeficitSummary(
      props.logDate,
      localTdee.value,
      localWeight.value
    );
    deficitSummary.value = summary;
    activityLogs.value = summary.activityLogs || [];
    emit('updated', summary);
  } catch (err) {
    console.error('Failed to fetch deficit summary:', err);
  }
};

const onTdeeOrWeightChange = () => {
  updatePreviewKcal();
  fetchDeficitData();
};

const handleAddActivity = async () => {
  if (!selectedActivityId.value || !durationMinutes.value || durationMinutes.value <= 0) return;
  isSubmitting.value = true;
  try {
    await nutritionService.addActivityLog({
      activityId: selectedActivityId.value,
      logDate: props.logDate,
      durationMinutes: durationMinutes.value,
      weightKg: localWeight.value,
    });
    toast.success('Đã ghi nhận hoạt động thể chất thành công!');
    await fetchDeficitData();
  } catch (err: any) {
    console.error('Failed to add activity log:', err);
    toast.error(err?.response?.data?.message || 'Không thể thêm hoạt động thể chất');
  } finally {
    isSubmitting.value = false;
  }
};

const startEdit = (log: NutritionActivityLog) => {
  editingLogId.value = log.id;
  editDurationValue.value = log.durationMinutes;
};

const cancelEdit = () => {
  editingLogId.value = null;
};

const saveEditLog = async (log: NutritionActivityLog) => {
  if (!editDurationValue.value || editDurationValue.value <= 0) return;
  try {
    await nutritionService.updateActivityLog(log.id, {
      activityId: log.activityId || 0,
      logDate: log.logDate,
      durationMinutes: editDurationValue.value,
      weightKg: localWeight.value,
    });
    toast.success('Đã cập nhật thời gian tập luyện!');
    editingLogId.value = null;
    await fetchDeficitData();
  } catch (err: any) {
    console.error('Failed to update activity log:', err);
    toast.error(err?.response?.data?.message || 'Không thể cập nhật hoạt động');
  }
};

const confirmDeleteLog = (log: NutritionActivityLog) => {
  deletingActivity.value = log;
  showDeleteConfirm.value = true;
};

const executeDeleteActivity = async () => {
  if (!deletingActivity.value) return;
  isDeletingActivity.value = true;
  try {
    await nutritionService.deleteActivityLog(deletingActivity.value.id);
    toast.success('Đã xóa hoạt động thể chất');
    showDeleteConfirm.value = false;
    deletingActivity.value = null;
    await fetchDeficitData();
  } catch (err: any) {
    console.error('Failed to delete activity log:', err);
    toast.error(err?.response?.data?.message || 'Không thể xóa hoạt động');
  } finally {
    isDeletingActivity.value = false;
  }
};


// Photo Methods
const loadTodayPhotos = async () => {
  try {
    todayPhotos.value = await healthPhotoService.getPhotosByDate(props.logDate);
  } catch (err) {
    console.error('Failed to load today photos:', err);
  }
};

const openUploadModal = () => {
  uploadForm.logDate = props.logDate || new Date().toISOString().split('T')[0];
  uploadForm.imageUrl = '';
  uploadForm.caption = '';
  uploadForm.weightKg = localWeight.value || undefined;
  selectedFile.value = null;
  previewUrl.value = null;
  showUploadModal.value = true;
};

const openGalleryModal = () => {
  showGalleryModal.value = true;
};

const previewPhotoItem = (photo: HealthWorkoutPhoto) => {
  previewPhoto.value = photo;
  showPreviewModal.value = true;
};

const triggerFileInput = () => {
  fileInputRef.value?.click();
};

const handleFileSelected = (e: Event) => {
  const target = e.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    const file = target.files[0];
    selectedFile.value = file;
    previewUrl.value = URL.createObjectURL(file);
  }
};

const onUrlInput = () => {
  if (uploadForm.imageUrl) {
    previewUrl.value = uploadForm.imageUrl;
  }
};

const submitPhoto = async () => {
  isUploading.value = true;
  try {
    if (selectedFile.value) {
      await healthPhotoService.uploadPhoto(
        selectedFile.value,
        uploadForm.logDate,
        uploadForm.caption || undefined,
        uploadForm.weightKg || undefined
      );
    } else if (uploadForm.imageUrl) {
      await healthPhotoService.savePhoto({
        logDate: uploadForm.logDate,
        imageUrl: uploadForm.imageUrl,
        caption: uploadForm.caption || undefined,
        weightKg: uploadForm.weightKg || undefined,
      });
    }
    toast.success('Đã lưu ảnh tập luyện thành công!');
    showUploadModal.value = false;
    await loadTodayPhotos();
  } catch (err: any) {
    console.error('Failed to submit photo:', err);
    toast.error('Không thể lưu ảnh tập luyện');
  } finally {
    isUploading.value = false;
  }
};

const confirmDeletePhoto = (id: number) => {
  deletingPhotoId.value = id;
  showDeletePhotoConfirm.value = true;
};

const executeDeletePhoto = async () => {
  if (!deletingPhotoId.value) return;
  isDeletingPhoto.value = true;
  try {
    await healthPhotoService.deletePhoto(deletingPhotoId.value);
    toast.success('Đã xóa ảnh tập luyện');
    showDeletePhotoConfirm.value = false;
    showPreviewModal.value = false;
    deletingPhotoId.value = null;
    await loadTodayPhotos();
  } catch (err) {
    console.error('Failed to delete photo:', err);
    toast.error('Không thể xóa ảnh');
  } finally {
    isDeletingPhoto.value = false;
  }
};

// Lifecycle & Watchers
onMounted(async () => {
  await loadActivities();
  await fetchDeficitData();
  await loadTodayPhotos();
});

watch(
  () => props.logDate,
  () => {
    fetchDeficitData();
    loadTodayPhotos();
  }
);

watch(
  () => props.foodCalories,
  () => {
    fetchDeficitData();
  }
);

defineExpose({
  fetchDeficitData,
  loadTodayPhotos,
});
</script>
