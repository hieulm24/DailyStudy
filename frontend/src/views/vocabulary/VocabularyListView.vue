<template>
  <div class="space-y-6">
    <!-- ============================================================= -->
    <!-- VIEW 1: TOPIC MASTER LIST (Chế độ xem danh sách chủ đề)       -->
    <!-- ============================================================= -->
    <div v-if="!selectedTopic" class="space-y-6">
      <!-- Header & Action Row -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs">
        <div>
          <div class="flex items-center gap-2.5">
            <h2 class="text-xl sm:text-2xl font-bold text-slate-900">Quản lý chủ đề & từ vựng</h2>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-brand-50 text-brand-700 border border-brand-200">
              {{ topicPageData.totalElements }} chủ đề
            </span>
          </div>
          <p class="text-sm sm:text-base text-slate-500 mt-1">
            Quản lý từ vựng phân loại theo chủ đề, tra cứu và luyện tập hiệu quả
          </p>
        </div>
        <div class="flex items-center gap-2.5 flex-wrap">
          <AppButton variant="secondary" size="md" :icon="Plus" @click="openAddWordModal(null)">
            Thêm từ vựng lẻ
          </AppButton>
          <AppButton variant="primary" size="md" :icon="FolderPlus" @click="openAddTopicModal">
            Tạo chủ đề mới
          </AppButton>
        </div>
      </div>

      <!-- Quick Stats Metric Cards -->
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
        <!-- Total Topics -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
          <div class="p-2.5 rounded-md bg-brand-50 text-brand-600 shrink-0">
            <FolderTree class="w-5 h-5" />
          </div>
          <div>
            <div class="text-[11px] font-semibold text-slate-500">Tổng số chủ đề</div>
            <div class="text-lg sm:text-xl font-bold text-slate-900 leading-tight">
              {{ topicPageData.totalElements }} <span class="text-xs font-normal text-slate-400">chủ đề</span>
            </div>
          </div>
        </div>

        <!-- Total Vocabularies -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
          <div class="p-2.5 rounded-md bg-indigo-50 text-indigo-600 shrink-0">
            <BookOpen class="w-5 h-5" />
          </div>
          <div>
            <div class="text-[11px] font-semibold text-slate-500">Tổng kho từ vựng</div>
            <div class="text-lg sm:text-xl font-bold text-indigo-600 leading-tight">
              {{ totalAllWords }} <span class="text-xs font-normal text-slate-400">từ</span>
            </div>
          </div>
        </div>

        <!-- Mastered Words -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
          <div class="p-2.5 rounded-md bg-emerald-50 text-emerald-600 shrink-0">
            <CheckCircle2 class="w-5 h-5" />
          </div>
          <div>
            <div class="text-[11px] font-semibold text-slate-500">Từ đã thuộc lòng</div>
            <div class="text-lg sm:text-xl font-bold text-emerald-600 leading-tight">
              {{ totalMasteredWords }} <span class="text-xs font-normal text-slate-400">từ</span>
            </div>
          </div>
        </div>

        <!-- Learning Words -->
        <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs flex items-center gap-3">
          <div class="p-2.5 rounded-md bg-amber-50 text-amber-600 shrink-0">
            <Flame class="w-5 h-5" />
          </div>
          <div>
            <div class="text-[11px] font-semibold text-slate-500">Đang học & Cần ôn</div>
            <div class="text-lg sm:text-xl font-bold text-amber-600 leading-tight">
              {{ totalLearningWords }} <span class="text-xs font-normal text-slate-400">từ</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Search & Filters for Topics -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs space-y-3">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
          <AppSearch v-model="topicFilter.search" placeholder="Tìm theo tên chủ đề, mô tả..." @search="loadTopics" />

          <div class="flex items-center gap-2 flex-wrap">
            <select
              v-model="topicFilter.level"
              class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
              @change="loadTopics"
            >
              <option value="">Mọi cấp độ (Level)</option>
              <option value="A1">A1 - Beginner</option>
              <option value="A2">A2 - Elementary</option>
              <option value="B1">B1 - Intermediate</option>
              <option value="B2">B2 - Upper Intermediate</option>
              <option value="C1">C1 - Advanced</option>
              <option value="C2">C2 - Mastery</option>
            </select>

            <select
              v-model="topicFilter.status"
              class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
              @change="loadTopics"
            >
              <option value="">Mọi trạng thái</option>
              <option value="NEW">Mới tạo (NEW)</option>
              <option value="LEARNING">Đang học (LEARNING)</option>
              <option value="MASTERED">Đã hoàn thành (MASTERED)</option>
            </select>

            <select
              v-model="topicFilter.sortBy"
              class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
              @change="loadTopics"
            >
              <option value="createdAt">Mới tạo nhất</option>
              <option value="name">Tên chủ đề (A-Z)</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Master Table: STT | Tên chủ đề | Tổng từ vựng | Trạng thái | Thao tác -->
      <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
        <div v-if="loadingTopics" class="py-12 text-center text-sm text-slate-400">
          Đang tải danh sách chủ đề...
        </div>

        <div v-else-if="topicPageData.items.length === 0">
          <AppEmptyState
            :icon="FolderPlus"
            title="Chưa có chủ đề nào"
            description="Hãy tạo chủ đề mới để gom nhóm và học từ vựng một cách có hệ thống."
            action-text="Tạo chủ đề đầu tiên"
            @action="openAddTopicModal"
          />
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm text-slate-800">
            <thead class="bg-slate-50 border-b border-slate-200 text-xs font-bold uppercase text-slate-600 tracking-wider">
              <tr>
                <th class="py-3.5 px-4 w-16 text-center">STT</th>
                <th class="py-3.5 px-4">Tên chủ đề</th>
                <th class="py-3.5 px-4">Tổng từ vựng</th>
                <th class="py-3.5 px-4">Trạng thái</th>
                <th class="py-3.5 px-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="(item, index) in topicPageData.items"
                :key="item.id"
                class="hover:bg-slate-50/80 transition-colors cursor-pointer group"
                @click="openTopicDetail(item)"
              >
                <!-- STT -->
                <td class="py-4 px-4 text-center font-semibold text-slate-500 text-xs sm:text-sm">
                  {{ topicPageData.page * topicPageData.size + index + 1 }}
                </td>

                <!-- Tên chủ đề -->
                <td class="py-4 px-4">
                  <div class="flex items-start gap-3">
                    <div class="p-2 rounded-lg bg-brand-50 text-brand-600 group-hover:bg-brand-600 group-hover:text-white transition-colors mt-0.5 shrink-0">
                      <Folder class="w-4 h-4" />
                    </div>
                    <div>
                      <div class="flex items-center gap-2 flex-wrap">
                        <span class="text-base font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                          {{ item.name }}
                        </span>
                        <AppBadge v-if="item.level" :level="item.level">
                          {{ item.level }}
                        </AppBadge>
                      </div>
                      <p v-if="item.description" class="text-xs text-slate-500 mt-1 line-clamp-1 max-w-md">
                        {{ item.description }}
                      </p>
                      <p v-else class="text-xs text-slate-400 mt-1 italic">
                        Chưa có mô tả
                      </p>
                    </div>
                  </div>
                </td>

                <!-- Tổng từ vựng -->
                <td class="py-4 px-4">
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-bold text-slate-900">{{ item.totalVocabularies }}</span>
                      <span class="text-xs text-slate-500">từ vựng</span>
                    </div>
                    <div v-if="item.totalVocabularies > 0" class="flex items-center gap-2 mt-1">
                      <div class="w-24 bg-slate-100 h-1.5 rounded-full overflow-hidden flex">
                        <div
                          class="bg-emerald-500 h-full"
                          :style="{ width: `${(item.masteredCount / item.totalVocabularies) * 100}%` }"
                          title="Đã thuộc"
                        ></div>
                        <div
                          class="bg-amber-500 h-full"
                          :style="{ width: `${(item.learningCount / item.totalVocabularies) * 100}%` }"
                          title="Đang học"
                        ></div>
                      </div>
                      <span class="text-[11px] text-slate-400">
                        {{ item.masteredCount }}/{{ item.totalVocabularies }} thuộc
                      </span>
                    </div>
                    <div v-else class="text-[11px] text-slate-400 mt-0.5">
                      Chưa có từ nào
                    </div>
                  </div>
                </td>

                <!-- Trạng thái -->
                <td class="py-4 px-4">
                  <AppBadge :variant="getTopicStatusVariant(item.status)" dot>
                    {{ getTopicStatusLabel(item.status) }}
                  </AppBadge>
                </td>

                <!-- Thao tác -->
                <td class="py-4 px-4 text-right" @click.stop>
                  <div class="flex items-center justify-end gap-1.5">
                    <!-- Detail Button -->
                    <button
                      type="button"
                      class="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-md bg-brand-50 text-brand-700 hover:bg-brand-600 hover:text-white font-medium text-xs transition-colors shadow-2xs border border-brand-200/80"
                      title="Xem chi tiết danh sách từ vựng trong chủ đề"
                      @click="openTopicDetail(item)"
                    >
                      <Eye class="w-3.5 h-3.5" />
                      <span>Chi tiết</span>
                    </button>

                    <!-- Quick Add Word to this Topic -->
                    <button
                      type="button"
                      class="inline-flex items-center gap-1 px-2 py-1.5 rounded-md text-slate-600 hover:text-brand-600 hover:bg-slate-100 font-medium text-xs transition-colors"
                      title="Thêm từ vào chủ đề này"
                      @click="openAddWordModal(item.id)"
                    >
                      <Plus class="w-3.5 h-3.5" />
                      <span class="hidden xl:inline">Thêm từ</span>
                    </button>

                    <!-- Edit Topic -->
                    <button
                      type="button"
                      title="Chỉnh sửa chủ đề"
                      class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded-md transition-colors"
                      @click="openEditTopicModal(item)"
                    >
                      <Edit class="w-4 h-4" />
                    </button>

                    <!-- Delete Topic -->
                    <button
                      type="button"
                      title="Xóa chủ đề"
                      class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
                      @click="confirmDeleteTopic(item)"
                    >
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Topic Pagination -->
        <AppPagination
          :current-page="topicPageData.page"
          :total-pages="topicPageData.totalPages"
          :page-size="topicPageData.size"
          :total-elements="topicPageData.totalElements"
          @update:page="handleTopicPageChange"
          @update:page-size="handleTopicPageSizeChange"
        />
      </div>
    </div>

    <!-- ============================================================= -->
    <!-- VIEW 2: TOPIC DETAIL (Chi tiết từ vựng trong một chủ đề)       -->
    <!-- ============================================================= -->
    <div v-else class="space-y-6">
      <!-- Back Navigation & Topic Header -->
      <div class="bg-white p-6 sm:p-7 rounded-md border border-slate-200 shadow-xs space-y-4">
        <div class="flex items-center justify-between gap-4 flex-wrap">
          <button
            type="button"
            class="inline-flex items-center gap-1.5 text-sm font-semibold text-slate-600 hover:text-brand-600 transition-colors"
            @click="backToTopics"
          >
            <ArrowLeft class="w-4 h-4" />
            <span>Quay lại danh sách chủ đề</span>
          </button>

          <div class="flex items-center gap-2">
            <AppButton variant="secondary" size="sm" :icon="Edit" @click="openEditTopicModal(selectedTopic)">
              Sửa chủ đề
            </AppButton>
            <AppButton variant="primary" size="sm" :icon="Plus" @click="openAddWordModal(selectedTopic.id)">
              Thêm từ vựng vào chủ đề
            </AppButton>
          </div>
        </div>

        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pt-2 border-t border-slate-100">
          <div>
            <div class="flex items-center gap-2.5 flex-wrap">
              <span class="p-2 rounded-md bg-brand-50 text-brand-600">
                <FolderOpen class="w-5 h-5" />
              </span>
              <h2 class="text-2xl font-bold text-slate-900">{{ selectedTopic.name }}</h2>
              <AppBadge v-if="selectedTopic.level" :level="selectedTopic.level">
                {{ selectedTopic.level }}
              </AppBadge>
              <AppBadge :variant="getTopicStatusVariant(selectedTopic.status)" dot>
                {{ getTopicStatusLabel(selectedTopic.status) }}
              </AppBadge>
            </div>
            <p v-if="selectedTopic.description" class="text-sm text-slate-500 mt-1.5 max-w-2xl">
              {{ selectedTopic.description }}
            </p>
          </div>

          <div class="flex items-center gap-4 bg-slate-50 px-4 py-2.5 rounded-md border border-slate-200 shrink-0">
            <div class="text-center">
              <div class="text-[11px] text-slate-500 font-medium">Tổng số từ</div>
              <div class="text-lg font-bold text-slate-900">{{ vocabPageData.totalElements }}</div>
            </div>
            <div class="w-px h-8 bg-slate-200"></div>
            <div class="text-center">
              <div class="text-[11px] text-emerald-600 font-medium">Đã thuộc</div>
              <div class="text-lg font-bold text-emerald-600">{{ topicMasteredCount }}</div>
            </div>
            <div class="w-px h-8 bg-slate-200"></div>
            <div class="text-center">
              <div class="text-[11px] text-amber-600 font-medium">Đang học</div>
              <div class="text-lg font-bold text-amber-600">{{ topicLearningCount }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Search & Filters inside this Topic -->
      <div class="bg-white p-4 rounded-md border border-slate-200 shadow-xs space-y-3">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
          <AppSearch v-model="vocabFilter.search" placeholder="Tìm từ hoặc nghĩa trong chủ đề này..." @search="loadVocabularies" />

          <div class="flex items-center gap-2 flex-wrap">
            <select
              v-model="vocabFilter.partOfSpeech"
              class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
              @change="loadVocabularies"
            >
              <option value="">Mọi loại từ (Part of Speech)</option>
              <option value="noun">Danh từ (Noun)</option>
              <option value="verb">Động từ (Verb)</option>
              <option value="adjective">Tính từ (Adjective)</option>
              <option value="adverb">Trạng từ (Adverb)</option>
              <option value="preposition">Giới từ (Preposition)</option>
              <option value="conjunction">Liên từ (Conjunction)</option>
              <option value="idiom">Thành ngữ (Idiom)</option>
            </select>

            <select
              v-model="vocabFilter.status"
              class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
              @change="loadVocabularies"
            >
              <option value="">Mọi trạng thái</option>
              <option value="NEW">Mới (NEW)</option>
              <option value="LEARNING">Đang học (LEARNING)</option>
              <option value="MASTERED">Đã thuộc lòng (MASTERED)</option>
            </select>

            <select
              v-model="vocabFilter.sortBy"
              class="text-sm bg-white border border-slate-200 text-slate-700 rounded-md px-3 py-2 shadow-xs focus:outline-none focus:border-brand-500"
              @change="loadVocabularies"
            >
              <option value="createdAt">Mới thêm nhất</option>
              <option value="word">Theo bảng chữ cái (A-Z)</option>
              <option value="masteryLevel">Mức độ thông thạo</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Detailed Vocabulary Table inside Topic -->
      <div class="bg-white rounded-md border border-slate-200 shadow-xs overflow-hidden">
        <div v-if="loadingVocabs" class="py-12 text-center text-sm text-slate-400">
          Đang tải từ vựng trong chủ đề...
        </div>

        <div v-else-if="vocabPageData.items.length === 0">
          <AppEmptyState
            :icon="BookOpen"
            title="Chưa có từ vựng nào trong chủ đề này"
            description="Hãy thêm từ vựng để bắt đầu học và ghi nhớ."
            action-text="Thêm từ vựng vào chủ đề"
            @action="openAddWordModal(selectedTopic.id)"
          />
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm text-slate-800">
            <thead class="bg-slate-50 border-b border-slate-200 text-xs font-bold uppercase text-slate-600 tracking-wider">
              <tr>
                <th class="py-3.5 px-4 w-12 text-center">STT</th>
                <th class="py-3.5 px-4">Từ vựng (Word)</th>
                <th class="py-3.5 px-4">Nghĩa tiếng Việt (Meaning)</th>
                <th class="py-3.5 px-4">Loại / Level</th>
                <th class="py-3.5 px-4">Ví dụ thực tế</th>
                <th class="py-3.5 px-4">Trạng thái</th>
                <th class="py-3.5 px-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="(item, vIdx) in vocabPageData.items"
                :key="item.id"
                class="hover:bg-slate-50/80 transition-colors cursor-pointer group"
                @click="openWordDetailModal(item)"
              >
                <!-- STT -->
                <td class="py-3.5 px-4 text-center text-xs text-slate-400 font-medium">
                  {{ vocabPageData.page * vocabPageData.size + vIdx + 1 }}
                </td>

                <!-- Word & Pronunciation & Audio -->
                <td class="py-3.5 px-4 font-semibold text-slate-900">
                  <div class="flex items-center gap-2">
                    <button
                      type="button"
                      class="p-1 rounded-full text-slate-400 hover:text-brand-600 hover:bg-brand-50 transition-colors"
                      title="Phát âm từ vựng"
                      @click.stop="speakWord(item.word)"
                    >
                      <Volume2 class="w-4 h-4" />
                    </button>
                    <div>
                      <div class="text-base font-bold text-brand-600 group-hover:text-brand-700 transition-colors">
                        {{ item.word }}
                      </div>
                      <span v-if="item.pronunciation" class="text-slate-400 font-normal text-xs font-mono">
                        {{ item.pronunciation }}
                      </span>
                    </div>
                  </div>
                </td>

                <!-- Meaning -->
                <td class="py-3.5 px-4 text-slate-900 font-medium text-sm max-w-xs">
                  {{ item.meaning }}
                </td>

                <!-- Part of Speech & Level -->
                <td class="py-3.5 px-4">
                  <div class="flex items-center gap-1.5 flex-wrap">
                    <span v-if="item.partOfSpeech" class="px-2 py-0.5 rounded-sm bg-slate-100 text-slate-700 text-xs font-semibold">
                      {{ item.partOfSpeech }}
                    </span>
                    <AppBadge v-if="item.level" :level="item.level">
                      {{ item.level }}
                    </AppBadge>
                  </div>
                </td>

                <!-- Example -->
                <td class="py-3.5 px-4 text-slate-600 text-sm max-w-sm">
                  <div v-if="item.examples && item.examples.length > 0">
                    <p class="italic text-slate-800 line-clamp-1">"{{ item.examples[0].exampleSentence }}"</p>
                    <p v-if="item.examples[0].meaning" class="text-xs text-slate-500 line-clamp-1">{{ item.examples[0].meaning }}</p>
                  </div>
                  <div v-else-if="item.contextSentence">
                    <p class="italic text-purple-800 line-clamp-1">"{{ item.contextSentence }}"</p>
                    <p v-if="item.contextMeaning" class="text-xs text-slate-500 line-clamp-1">{{ item.contextMeaning }}</p>
                  </div>
                  <span v-else class="text-slate-300">-</span>
                  <div v-if="item.contextSentence && item.examples && item.examples.length > 0" class="mt-1">
                    <span class="inline-flex items-center text-[10px] font-semibold text-purple-700 bg-purple-50 px-1.5 py-0.2 rounded border border-purple-200">
                      + Ngữ cảnh dài
                    </span>
                  </div>
                </td>

                <!-- Status -->
                <td class="py-3.5 px-4">
                  <AppBadge :variant="getVocabStatusVariant(item.status)" dot>
                    {{ getVocabStatusLabel(item.status) }}
                  </AppBadge>
                </td>

                <!-- Actions -->
                <td class="py-3.5 px-4 text-right" @click.stop>
                  <div class="flex items-center justify-end gap-1">
                    <button
                      v-if="item.status !== 'MASTERED'"
                      type="button"
                      title="Đánh dấu thuộc lòng"
                      class="p-1.5 text-slate-400 hover:text-emerald-600 hover:bg-emerald-50 rounded-md transition-colors"
                      @click="markMastered(item)"
                    >
                      <CheckCircle2 class="w-4 h-4" />
                    </button>
                    <button
                      type="button"
                      title="Chỉnh sửa từ vựng"
                      class="p-1.5 text-slate-400 hover:text-brand-600 hover:bg-brand-50 rounded-md transition-colors"
                      @click="openEditWordModal(item)"
                    >
                      <Edit class="w-4 h-4" />
                    </button>
                    <button
                      type="button"
                      title="Xóa từ vựng"
                      class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
                      @click="confirmDeleteWord(item)"
                    >
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Vocab Pagination -->
        <AppPagination
          :current-page="vocabPageData.page"
          :total-pages="vocabPageData.totalPages"
          :page-size="vocabPageData.size"
          :total-elements="vocabPageData.totalElements"
          @update:page="handleVocabPageChange"
          @update:page-size="handleVocabPageSizeChange"
        />
      </div>
    </div>

    <!-- ============================================================= -->
    <!-- MODAL 1: ADD / EDIT TOPIC (KÈM NHẬP HÀNG LOẠT TỪ VỰNG)        -->
    <!-- ============================================================= -->
    <AppModal
      v-model="showTopicModal"
      :title="isEditingTopic ? 'Chỉnh sửa chủ đề từ vựng' : 'Tạo chủ đề từ vựng mới'"
      size="2xl"
    >
      <form class="space-y-5" @submit.prevent="saveTopic">
        <!-- Topic Basic Information -->
        <div class="p-4 sm:p-5 bg-slate-50/80 rounded-lg border border-slate-200/80 space-y-4">
          <div class="text-xs font-bold uppercase tracking-wider text-slate-700">Thông tin chủ đề</div>
          <div class="grid grid-cols-1 sm:grid-cols-12 gap-4">
            <div class="sm:col-span-8">
              <AppInput
                v-model="topicForm.name"
                label="Tên chủ đề (Topic Name)"
                placeholder="e.g. Daily Business & Office, Airport & Travel..."
                required
              />
            </div>
            <div class="sm:col-span-4">
              <AppSelect
                v-model="topicForm.level"
                label="Cấp độ (Level)"
                placeholder="Chọn cấp độ"
                :options="levelOptions"
              />
            </div>
          </div>

          <AppTextarea
            v-model="topicForm.description"
            label="Mô tả chủ đề (Description)"
            placeholder="Mô tả mục tiêu, ngữ cảnh hoặc nội dung của chủ đề này..."
            rows="2"
          />
        </div>

        <!-- Batch Vocabulary Addition (Only when creating) -->
        <div v-if="!isEditingTopic" class="space-y-3 pt-1">
          <div class="flex items-center justify-between gap-3 flex-wrap">
            <div>
              <span class="text-sm font-bold text-slate-800">
                Thêm danh sách từ vựng vào chủ đề ({{ topicForm.vocabularies.length }} từ)
              </span>
              <p class="text-xs text-slate-500">
                Điền thông tin các từ vựng thuộc chủ đề này để thêm cùng lúc một cách nhanh chóng
              </p>
            </div>
            <AppButton
              type="button"
              variant="outline"
              size="sm"
              :icon="Plus"
              @click="addBatchWordRow"
            >
              Thêm dòng từ vựng
            </AppButton>
          </div>

          <div v-if="topicForm.vocabularies.length === 0" class="p-6 rounded-lg border-2 border-dashed border-slate-200 text-center text-xs text-slate-500 bg-slate-50/50 space-y-2">
            <p>Chưa có từ vựng nào trong danh sách.</p>
            <AppButton
              type="button"
              variant="secondary"
              size="sm"
              :icon="Plus"
              @click="addBatchWordRow"
            >
              Thêm dòng từ đầu tiên
            </AppButton>
          </div>

          <div v-else class="space-y-3.5 max-h-[480px] overflow-y-auto pr-1">
            <div
              v-for="(row, rIdx) in topicForm.vocabularies"
              :key="rIdx"
              class="p-4 bg-white hover:bg-slate-50/40 rounded-lg border border-slate-200/90 shadow-2xs space-y-3 relative group transition-all"
            >
              <!-- Row Header -->
              <div class="flex items-center justify-between pb-2 border-b border-slate-100">
                <div class="flex items-center gap-2">
                  <span class="inline-flex items-center justify-center w-5 h-5 rounded-full bg-brand-600 text-white font-bold text-[11px]">
                    {{ rIdx + 1 }}
                  </span>
                  <span class="text-xs font-bold text-slate-800">Từ vựng #{{ rIdx + 1 }}</span>
                </div>
                <button
                  type="button"
                  class="text-xs text-rose-500 hover:text-rose-700 font-semibold inline-flex items-center gap-1 px-2 py-0.5 rounded hover:bg-rose-50 transition-colors"
                  @click="removeBatchWordRow(rIdx)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                  <span>Xóa dòng</span>
                </button>
              </div>

              <!-- Row 1: Word, Pronunciation, Part of Speech, Meaning -->
              <div class="grid grid-cols-1 sm:grid-cols-12 gap-3 items-start">
                <div class="sm:col-span-3">
                  <AppInput
                    v-model="row.word"
                    label="Từ vựng (Word)"
                    placeholder="e.g. Schedule"
                    required
                  />
                </div>
                <div class="sm:col-span-3">
                  <AppInput
                    v-model="row.pronunciation"
                    label="Phát âm (IPA)"
                    placeholder="e.g. /ˈskedʒ.uːl/"
                  />
                </div>
                <div class="sm:col-span-2">
                  <AppSelect
                    v-model="row.partOfSpeech"
                    label="Từ loại"
                    :options="partOfSpeechOptions"
                  />
                </div>
                <div class="sm:col-span-4">
                  <AppInput
                    v-model="row.meaning"
                    label="Nghĩa tiếng Việt (Meaning)"
                    placeholder="e.g. Lịch trình, sắp xếp thời gian"
                    required
                  />
                </div>
              </div>

              <!-- Row 2: Example Sentence & Meaning -->
              <div class="grid grid-cols-1 sm:grid-cols-12 gap-3 items-start">
                <div class="sm:col-span-6">
                  <AppInput
                    v-model="row.exampleSentence"
                    label="Câu ví dụ ngắn"
                    placeholder="e.g. Everything is going according to schedule."
                  />
                </div>
                <div class="sm:col-span-6">
                  <AppInput
                    v-model="row.exampleMeaning"
                    label="Nghĩa câu ví dụ ngắn"
                    placeholder="e.g. Mọi thứ đang diễn ra đúng theo lịch trình."
                  />
                </div>
              </div>

              <!-- Row 3: Extended Context Sentence & Meaning -->
              <div class="grid grid-cols-1 sm:grid-cols-12 gap-3 items-start p-2.5 rounded-md bg-purple-50/40 border border-purple-100">
                <div class="sm:col-span-6">
                  <AppInput
                    v-model="row.contextSentence"
                    label="Ngữ cảnh / Câu dài (Tiếng Anh)"
                    placeholder="e.g. All staff members must submit their revised quarterly schedules by Friday."
                  />
                </div>
                <div class="sm:col-span-6">
                  <AppInput
                    v-model="row.contextMeaning"
                    label="Dịch nghĩa câu dài (Tiếng Việt)"
                    placeholder="e.g. Toàn bộ nhân viên phải nộp lại lịch trình quý đã sửa đổi trước thứ Sáu."
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex justify-end gap-2 pt-3 border-t border-slate-100">
          <AppButton type="button" variant="secondary" size="md" @click="showTopicModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="md" :loading="savingTopic">
            {{ isEditingTopic ? 'Lưu thay đổi' : 'Tạo chủ đề' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- ============================================================= -->
    <!-- MODAL 2: ADD / EDIT VOCABULARY WORD (CÓ CHỌN CHỦ ĐỀ)          -->
    <!-- ============================================================= -->
    <AppModal
      v-model="showWordModal"
      :title="isEditingWord ? 'Chỉnh sửa từ vựng' : 'Thêm từ vựng mới'"
      size="xl"
    >
      <form class="space-y-4" @submit.prevent="saveWord">
        <!-- Topic & Level Row -->
        <div class="grid grid-cols-1 sm:grid-cols-12 gap-4">
          <div class="sm:col-span-7">
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">Thuộc chủ đề (Topic)</label>
            <select
              v-model="wordForm.topicId"
              class="block w-full text-sm rounded-md border border-slate-300 bg-white shadow-sm px-3 py-2 text-slate-900 focus:border-brand-500 focus:ring-1 focus:ring-brand-500 focus:outline-none"
            >
              <option :value="null">-- Không chọn chủ đề (Từ vựng chung) --</option>
              <option v-for="t in allTopicsList" :key="t.id" :value="t.id">
                {{ t.name }} {{ t.level ? `(${t.level})` : '' }}
              </option>
            </select>
          </div>
          <div class="sm:col-span-5">
            <AppSelect
              v-model="wordForm.level"
              label="Cấp độ (Level)"
              placeholder="Chọn cấp độ"
              :options="levelOptions"
            />
          </div>
        </div>

        <!-- Word, Pronunciation, Part of Speech -->
        <div class="grid grid-cols-1 sm:grid-cols-12 gap-4">
          <div class="sm:col-span-5">
            <AppInput
              v-model="wordForm.word"
              label="Từ vựng (Word)"
              placeholder="e.g. negotiation"
              required
            />
          </div>
          <div class="sm:col-span-4">
            <AppInput
              v-model="wordForm.pronunciation"
              label="Phát âm (Pronunciation)"
              placeholder="e.g. /nɪˌɡoʊ.ʃiˈeɪ.ʃən/"
            />
          </div>
          <div class="sm:col-span-3">
            <AppSelect
              v-model="wordForm.partOfSpeech"
              label="Từ loại (Part of Speech)"
              placeholder="Chọn loại từ"
              :options="partOfSpeechOptions"
            />
          </div>
        </div>

        <!-- Meaning -->
        <AppTextarea
          v-model="wordForm.meaning"
          label="Nghĩa tiếng Việt (Meaning)"
          placeholder="e.g. sự đàm phán, thương lượng"
          rows="2"
          required
        />

        <!-- Short Example Section -->
        <div class="p-4 bg-slate-50/80 rounded-md border border-slate-200 space-y-3">
          <span class="text-xs font-bold uppercase tracking-wider text-slate-700 block">Câu ví dụ ngắn (Short Example)</span>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <AppInput
              v-model="wordForm.exampleSentence"
              label="Câu ví dụ tiếng Anh"
              placeholder="e.g. The contract is currently under negotiation."
            />
            <AppInput
              v-model="wordForm.exampleMeaning"
              label="Dịch nghĩa câu ví dụ"
              placeholder="e.g. Hợp đồng hiện đang trong quá trình đàm phán."
            />
          </div>
        </div>

        <!-- Extended Context Sentence Section (Câu dài hơn / Ngữ cảnh đoạn văn) -->
        <div class="p-4 bg-purple-50/50 rounded-md border border-purple-200/80 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold uppercase tracking-wider text-purple-800 block">
              Ngữ cảnh / Câu ví dụ mở rộng (Dài hơn)
            </span>
            <span class="text-[11px] font-semibold text-purple-700 bg-purple-100 px-2 py-0.5 rounded">
              TOEIC Context Sentence
            </span>
          </div>
          <div class="space-y-3">
            <AppTextarea
              v-model="wordForm.contextSentence"
              label="Câu dài / Ngữ cảnh tiếng Anh"
              placeholder="e.g. After months of intense negotiation, both corporate parties finally reached a consensus on the new international trade agreement."
              rows="2"
            />
            <AppTextarea
              v-model="wordForm.contextMeaning"
              label="Dịch nghĩa câu dài tiếng Việt"
              placeholder="e.g. Sau nhiều tháng đàm phán căng thẳng, cả hai tập đoàn cuối cùng đã đạt được sự đồng thuận về hiệp định thương mại quốc tế mới."
              rows="2"
            />
          </div>
        </div>

        <AppTextarea
          v-model="wordForm.note"
          label="Ghi chú cá nhân (Note)"
          placeholder="Mẹo nhớ từ, collocation, ngữ cảnh..."
          rows="2"
        />

        <div class="flex justify-end gap-2 pt-2 border-t border-slate-100">
          <AppButton type="button" variant="secondary" size="md" @click="showWordModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="md" :loading="savingWord">
            {{ isEditingWord ? 'Lưu thay đổi' : 'Thêm từ vựng' }}
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- ============================================================= -->
    <!-- MODAL 3: WORD DETAIL MODAL (Xem chi tiết từ vựng)             -->
    <!-- ============================================================= -->
    <AppModal
      v-model="showWordDetailModal"
      title="Chi tiết từ vựng"
      size="md"
    >
      <div v-if="selectedWord" class="space-y-4">
        <div class="flex items-start justify-between pb-3 border-b border-slate-100">
          <div>
            <div class="flex items-center gap-2.5">
              <h3 class="text-2xl font-bold text-brand-600">{{ selectedWord.word }}</h3>
              <button
                type="button"
                class="p-1.5 rounded-full text-slate-400 hover:text-brand-600 hover:bg-brand-50 transition-colors"
                title="Phát âm từ vựng"
                @click="speakWord(selectedWord.word)"
              >
                <Volume2 class="w-5 h-5" />
              </button>
            </div>
            <span v-if="selectedWord.pronunciation" class="text-sm text-slate-500 font-mono">{{ selectedWord.pronunciation }}</span>
            <div class="flex items-center gap-2 mt-1.5 flex-wrap">
              <span v-if="selectedWord.partOfSpeech" class="text-xs text-slate-600 font-medium px-2 py-0.5 bg-slate-100 rounded">
                ({{ selectedWord.partOfSpeech }})
              </span>
              <AppBadge v-if="selectedWord.level" :level="selectedWord.level">{{ selectedWord.level }}</AppBadge>
              <AppBadge :variant="getVocabStatusVariant(selectedWord.status)">{{ getVocabStatusLabel(selectedWord.status) }}</AppBadge>
              <span v-if="selectedWord.topicName" class="text-xs text-brand-700 bg-brand-50 px-2 py-0.5 rounded border border-brand-200">
                Chủ đề: {{ selectedWord.topicName }}
              </span>
            </div>
          </div>
          <div class="text-right text-xs text-slate-500 shrink-0">
            <div>Đã ôn: <strong>{{ selectedWord.reviewCount }}</strong> lần</div>
            <div class="mt-0.5 inline-flex items-center gap-1">
              <span>Thông thạo: <strong>{{ selectedWord.masteryLevel }}/5</strong></span>
              <Star class="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
            </div>
          </div>
        </div>

        <div>
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Nghĩa tiếng Việt</h4>
          <p class="text-base font-semibold text-slate-900 bg-slate-50 p-3.5 rounded-md border border-slate-100">{{ selectedWord.meaning }}</p>
        </div>

        <div v-if="selectedWord.examples && selectedWord.examples.length > 0">
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Câu ví dụ thực tế</h4>
          <div class="p-3.5 bg-brand-50/40 rounded-md border border-brand-100 space-y-1.5">
            <p class="text-sm font-semibold text-slate-900">"{{ selectedWord.examples[0].exampleSentence }}"</p>
            <p v-if="selectedWord.examples[0].meaning" class="text-sm text-slate-600">{{ selectedWord.examples[0].meaning }}</p>
          </div>
        </div>

        <!-- Extended Long Context Sentence -->
        <div v-if="selectedWord.contextSentence">
          <h4 class="text-xs font-bold text-purple-700 uppercase tracking-wider mb-1.5 flex items-center justify-between">
            <span>Ngữ cảnh / Câu ví dụ mở rộng (Dài hơn)</span>
            <span class="text-[10px] font-semibold text-purple-600 bg-purple-100/70 px-2 py-0.2 rounded">TOEIC Context</span>
          </h4>
          <div class="p-3.5 bg-purple-50/60 rounded-md border border-purple-200 space-y-1.5">
            <p class="text-sm font-medium text-slate-900 leading-relaxed">"{{ selectedWord.contextSentence }}"</p>
            <p v-if="selectedWord.contextMeaning" class="text-sm text-purple-950 font-normal leading-relaxed">{{ selectedWord.contextMeaning }}</p>
          </div>
        </div>

        <div v-if="selectedWord.note">
          <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1.5">Ghi chú cá nhân</h4>
          <p class="text-sm text-slate-700 bg-slate-50 p-3 rounded-md border border-slate-100">{{ selectedWord.note }}</p>
        </div>

        <div class="flex items-center justify-between pt-3 border-t border-slate-100 text-xs">
          <div class="text-slate-400">
            Thêm ngày: {{ formatDate(selectedWord.createdAt) }}
          </div>
          <div class="flex items-center gap-2">
            <AppButton v-if="selectedWord.status !== 'MASTERED'" variant="outline" size="sm" @click="markMastered(selectedWord)">
              Thuộc lòng
            </AppButton>
            <AppButton variant="primary" size="sm" :icon="Edit" @click="openEditWordModal(selectedWord)">
              Sửa
            </AppButton>
          </div>
        </div>
      </div>
    </AppModal>

    <!-- ============================================================= -->
    <!-- DELETE TOPIC CONFIRM DIALOG                                   -->
    <!-- ============================================================= -->
    <AppConfirmDialog
      v-model="showDeleteTopicDialog"
      title="Xác nhận xóa chủ đề"
      :message="`Bạn có chắc chắn muốn xóa chủ đề '${topicToDelete?.name}' không? Các từ vựng bên trong sẽ chuyển về trạng thái không thuộc chủ đề.`"
      :loading="deletingTopic"
      @confirm="handleDeleteTopic"
    />

    <!-- ============================================================= -->
    <!-- DELETE WORD CONFIRM DIALOG                                    -->
    <!-- ============================================================= -->
    <AppConfirmDialog
      v-model="showDeleteWordDialog"
      title="Xác nhận xóa từ vựng"
      :message="`Bạn có chắc chắn muốn xóa từ vựng '${wordToDelete?.word}' không? Hành động này sẽ xóa cả lịch sử ôn tập liên quan.`"
      :loading="deletingWord"
      @confirm="handleDeleteWord"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { vocabularyService } from '../../services/vocabulary.service';
import { useToastStore } from '../../stores/toast.store';
import { useReviewStore } from '../../stores/review.store';
import type { Vocabulary, VocabularyTopic, PageResponse } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppTextarea from '../../components/common/AppTextarea.vue';
import AppSelect from '../../components/common/AppSelect.vue';
import AppModal from '../../components/common/AppModal.vue';
import AppBadge from '../../components/common/AppBadge.vue';
import AppPagination from '../../components/common/AppPagination.vue';
import AppEmptyState from '../../components/common/AppEmptyState.vue';
import AppConfirmDialog from '../../components/common/AppConfirmDialog.vue';
import AppSearch from '../../components/common/AppSearch.vue';
import {
  Plus,
  FolderPlus,
  Folder,
  FolderOpen,
  FolderTree,
  BookOpen,
  Edit,
  Trash2,
  Star,
  CheckCircle2,
  Flame,
  Eye,
  ArrowLeft,
  Volume2,
} from 'lucide-vue-next';

const route = useRoute();
const toastStore = useToastStore();
const reviewStore = useReviewStore();

// View State
const selectedTopic = ref<VocabularyTopic | null>(null);

// Overall Counts
const totalAllWords = ref(0);
const totalMasteredWords = ref(0);
const totalLearningWords = ref(0);
const allTopicsList = ref<VocabularyTopic[]>([]);

// Topic Management State
const loadingTopics = ref(false);
const savingTopic = ref(false);
const deletingTopic = ref(false);
const showTopicModal = ref(false);
const showDeleteTopicDialog = ref(false);
const isEditingTopic = ref(false);
const topicToDelete = ref<VocabularyTopic | null>(null);

const topicPageData = ref<PageResponse<VocabularyTopic>>({
  items: [],
  page: 0,
  size: 15,
  totalElements: 0,
  totalPages: 0,
  isFirst: true,
  isLast: true,
});

const topicFilter = reactive({
  search: '',
  level: '',
  status: '',
  page: 0,
  size: 15,
  sortBy: 'createdAt',
  sortDirection: 'DESC',
});

interface BatchWordItem {
  word: string;
  meaning: string;
  pronunciation?: string;
  partOfSpeech?: string;
  exampleSentence?: string;
  exampleMeaning?: string;
  contextSentence?: string;
  contextMeaning?: string;
}

const topicForm = reactive({
  id: 0,
  name: '',
  description: '',
  level: 'B1',
  status: 'NEW',
  vocabularies: [] as BatchWordItem[],
});

// Vocabularies Inside Topic State
const loadingVocabs = ref(false);
const savingWord = ref(false);
const deletingWord = ref(false);
const showWordModal = ref(false);
const showWordDetailModal = ref(false);
const showDeleteWordDialog = ref(false);
const isEditingWord = ref(false);
const selectedWord = ref<Vocabulary | null>(null);
const wordToDelete = ref<Vocabulary | null>(null);

const vocabPageData = ref<PageResponse<Vocabulary>>({
  items: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  isFirst: true,
  isLast: true,
  });

const vocabFilter = reactive({
  topicId: undefined as number | undefined,
  search: '',
  level: '',
  partOfSpeech: '',
  status: '',
  page: 0,
  size: 20,
  sortBy: 'createdAt',
  sortDirection: 'DESC',
});

const topicMasteredCount = computed(() => {
  return selectedTopic.value?.masteredCount || 0;
});

const topicLearningCount = computed(() => {
  if (!selectedTopic.value) return 0;
  return Math.max(0, selectedTopic.value.totalVocabularies - selectedTopic.value.masteredCount);
});

const wordForm = reactive({
  id: 0,
  topicId: null as number | null,
  word: '',
  meaning: '',
  pronunciation: '',
  partOfSpeech: 'noun',
  level: 'B1',
  note: '',
  exampleSentence: '',
  exampleMeaning: '',
  contextSentence: '',
  contextMeaning: '',
});

const partOfSpeechOptions = [
  { label: 'Danh từ (Noun)', value: 'noun' },
  { label: 'Động từ (Verb)', value: 'verb' },
  { label: 'Tính từ (Adjective)', value: 'adjective' },
  { label: 'Trạng từ (Adverb)', value: 'adverb' },
  { label: 'Giới từ (Preposition)', value: 'preposition' },
  { label: 'Liên từ (Conjunction)', value: 'conjunction' },
  { label: 'Thành ngữ (Idiom / Phrasal)', value: 'idiom' },
];

const levelOptions = [
  { label: 'A1 - Beginner', value: 'A1' },
  { label: 'A2 - Elementary', value: 'A2' },
  { label: 'B1 - Intermediate', value: 'B1' },
  { label: 'B2 - Upper Intermediate', value: 'B2' },
  { label: 'C1 - Advanced', value: 'C1' },
  { label: 'C2 - Mastery', value: 'C2' },
];

// --- Lifecycle ---
onMounted(() => {
  loadTopics();
  loadAllTopicsDropdown();
  fetchOverallStats();

  if (route.query.action === 'add') {
    openAddTopicModal();
  }
});

// --- Speech Synthesis ---
function speakWord(text: string) {
  if ('speechSynthesis' in window && text) {
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 0.9;
    window.speechSynthesis.speak(utterance);
  }
}

// --- Topic Functions ---
async function loadTopics() {
  loadingTopics.value = true;
  try {
    const res = await vocabularyService.getTopics({
      search: topicFilter.search || undefined,
      level: topicFilter.level || undefined,
      status: topicFilter.status || undefined,
      page: topicFilter.page,
      size: topicFilter.size,
      sortBy: topicFilter.sortBy,
      sortDirection: topicFilter.sortDirection,
    });
    topicPageData.value = res;
  } catch (err) {
    toastStore.error('Không thể tải danh sách chủ đề');
  } finally {
    loadingTopics.value = false;
  }
}

async function loadAllTopicsDropdown() {
  try {
    allTopicsList.value = await vocabularyService.getAllTopics();
  } catch (e) {
    // fallback
  }
}

async function fetchOverallStats() {
  try {
    const resAll = await vocabularyService.getVocabularies({ page: 0, size: 1 });
    totalAllWords.value = resAll.totalElements;

    const resMastered = await vocabularyService.getVocabularies({ status: 'MASTERED', page: 0, size: 1 });
    totalMasteredWords.value = resMastered.totalElements;
    totalLearningWords.value = Math.max(0, totalAllWords.value - totalMasteredWords.value);
  } catch (e) {
    // fallback
  }
}

function handleTopicPageChange(newPage: number) {
  topicFilter.page = newPage;
  loadTopics();
}

function handleTopicPageSizeChange(newSize: number) {
  topicFilter.size = newSize;
  topicFilter.page = 0;
  loadTopics();
}

function openAddTopicModal() {
  isEditingTopic.value = false;
  topicForm.id = 0;
  topicForm.name = '';
  topicForm.description = '';
  topicForm.level = 'B1';
  topicForm.status = 'NEW';
  topicForm.vocabularies = [
    {
      word: '',
      meaning: '',
      pronunciation: '',
      partOfSpeech: 'noun',
      exampleSentence: '',
      exampleMeaning: '',
      contextSentence: '',
      contextMeaning: '',
    }
  ];
  showTopicModal.value = true;
}

function openEditTopicModal(topic: VocabularyTopic) {
  isEditingTopic.value = true;
  topicForm.id = topic.id;
  topicForm.name = topic.name;
  topicForm.description = topic.description || '';
  topicForm.level = topic.level || 'B1';
  topicForm.status = topic.status || 'NEW';
  topicForm.vocabularies = [];
  showTopicModal.value = true;
}

function addBatchWordRow() {
  topicForm.vocabularies.push({
    word: '',
    meaning: '',
    pronunciation: '',
    partOfSpeech: 'noun',
    exampleSentence: '',
    exampleMeaning: '',
    contextSentence: '',
    contextMeaning: '',
  });
}

function removeBatchWordRow(index: number) {
  topicForm.vocabularies.splice(index, 1);
}

async function saveTopic() {
  if (!topicForm.name.trim()) {
    toastStore.warning('Vui lòng nhập tên chủ đề');
    return;
  }

  savingTopic.value = true;
  try {
    if (isEditingTopic.value) {
      const updated = await vocabularyService.updateTopic(topicForm.id, {
        name: topicForm.name,
        description: topicForm.description,
        level: topicForm.level,
        status: topicForm.status,
      });
      toastStore.success('Cập nhật chủ đề thành công');
      if (selectedTopic.value && selectedTopic.value.id === topicForm.id) {
        selectedTopic.value = updated;
      }
    } else {
      const validBatchWords = topicForm.vocabularies.filter(v => v.word.trim() && v.meaning.trim());
      await vocabularyService.createTopic({
        name: topicForm.name,
        description: topicForm.description,
        level: topicForm.level,
        status: topicForm.status,
        vocabularies: validBatchWords,
      });
      toastStore.success(`Tạo chủ đề thành công${validBatchWords.length > 0 ? ` cùng ${validBatchWords.length} từ vựng` : ''}!`);
    }

    showTopicModal.value = false;
    loadTopics();
    loadAllTopicsDropdown();
    fetchOverallStats();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu chủ đề');
  } finally {
    savingTopic.value = false;
  }
}

function confirmDeleteTopic(topic: VocabularyTopic) {
  topicToDelete.value = topic;
  showDeleteTopicDialog.value = true;
}

async function handleDeleteTopic() {
  if (!topicToDelete.value) return;
  deletingTopic.value = true;
  try {
    await vocabularyService.deleteTopic(topicToDelete.value.id);
    toastStore.success('Đã xóa chủ đề');
    showDeleteTopicDialog.value = false;
    if (selectedTopic.value && selectedTopic.value.id === topicToDelete.value.id) {
      selectedTopic.value = null;
    }
    loadTopics();
    loadAllTopicsDropdown();
  } catch (e) {
    toastStore.error('Không thể xóa chủ đề');
  } finally {
    deletingTopic.value = false;
  }
}

// --- Topic Detail Mode ---
function openTopicDetail(topic: VocabularyTopic) {
  selectedTopic.value = topic;
  vocabFilter.topicId = topic.id;
  vocabFilter.search = '';
  vocabFilter.partOfSpeech = '';
  vocabFilter.status = '';
  vocabFilter.page = 0;
  loadVocabularies();
}

function backToTopics() {
  selectedTopic.value = null;
  loadTopics();
  loadAllTopicsDropdown();
  fetchOverallStats();
}

async function loadVocabularies() {
  if (!selectedTopic.value) return;
  loadingVocabs.value = true;
  try {
    const res = await vocabularyService.getVocabularies({
      topicId: selectedTopic.value.id,
      search: vocabFilter.search || undefined,
      partOfSpeech: vocabFilter.partOfSpeech || undefined,
      status: vocabFilter.status || undefined,
      page: vocabFilter.page,
      size: vocabFilter.size,
      sortBy: vocabFilter.sortBy,
      sortDirection: vocabFilter.sortDirection,
    });
    vocabPageData.value = res;
  } catch (err) {
    toastStore.error('Không thể tải từ vựng trong chủ đề');
  } finally {
    loadingVocabs.value = false;
  }
}

function handleVocabPageChange(newPage: number) {
  vocabFilter.page = newPage;
  loadVocabularies();
}

function handleVocabPageSizeChange(newSize: number) {
  vocabFilter.size = newSize;
  vocabFilter.page = 0;
  loadVocabularies();
}

// --- Individual Word Functions ---
function openAddWordModal(topicId?: number | null) {
  isEditingWord.value = false;
  wordForm.id = 0;
  wordForm.topicId = topicId !== undefined ? topicId : (selectedTopic.value ? selectedTopic.value.id : null);
  wordForm.word = '';
  wordForm.meaning = '';
  wordForm.pronunciation = '';
  wordForm.partOfSpeech = 'noun';
  wordForm.level = selectedTopic.value ? selectedTopic.value.level || 'B1' : 'B1';
  wordForm.note = '';
  wordForm.exampleSentence = '';
  wordForm.exampleMeaning = '';
  wordForm.contextSentence = '';
  wordForm.contextMeaning = '';
  showWordModal.value = true;
}

function openEditWordModal(item: Vocabulary) {
  isEditingWord.value = true;
  wordForm.id = item.id;
  wordForm.topicId = item.topicId || (selectedTopic.value ? selectedTopic.value.id : null);
  wordForm.word = item.word;
  wordForm.meaning = item.meaning;
  wordForm.pronunciation = item.pronunciation || '';
  wordForm.partOfSpeech = item.partOfSpeech || 'noun';
  wordForm.level = item.level || 'B1';
  wordForm.note = item.note || '';
  wordForm.contextSentence = item.contextSentence || '';
  wordForm.contextMeaning = item.contextMeaning || '';

  if (item.examples && item.examples.length > 0) {
    wordForm.exampleSentence = item.examples[0].exampleSentence || '';
    wordForm.exampleMeaning = item.examples[0].meaning || '';
  } else {
    wordForm.exampleSentence = '';
    wordForm.exampleMeaning = '';
  }

  showWordDetailModal.value = false;
  showWordModal.value = true;
}

function openWordDetailModal(item: Vocabulary) {
  selectedWord.value = item;
  showWordDetailModal.value = true;
}

async function saveWord() {
  if (!wordForm.word.trim() || !wordForm.meaning.trim()) {
    toastStore.warning('Vui lòng nhập từ vựng và nghĩa tiếng Việt');
    return;
  }

  savingWord.value = true;
  try {
    if (isEditingWord.value) {
      await vocabularyService.updateVocabulary(wordForm.id, {
        topicId: wordForm.topicId || undefined,
        word: wordForm.word,
        meaning: wordForm.meaning,
        pronunciation: wordForm.pronunciation,
        partOfSpeech: wordForm.partOfSpeech,
        level: wordForm.level,
        note: wordForm.note,
        exampleSentence: wordForm.exampleSentence,
        exampleMeaning: wordForm.exampleMeaning,
        contextSentence: wordForm.contextSentence,
        contextMeaning: wordForm.contextMeaning,
      });
      toastStore.success('Cập nhật từ vựng thành công');
    } else {
      await vocabularyService.createVocabulary({
        topicId: wordForm.topicId || undefined,
        word: wordForm.word,
        meaning: wordForm.meaning,
        pronunciation: wordForm.pronunciation,
        partOfSpeech: wordForm.partOfSpeech,
        level: wordForm.level,
        note: wordForm.note,
        exampleSentence: wordForm.exampleSentence,
        exampleMeaning: wordForm.exampleMeaning,
        contextSentence: wordForm.contextSentence,
        contextMeaning: wordForm.contextMeaning,
      });
      toastStore.success('Thêm từ vựng mới thành công');
    }

    showWordModal.value = false;
    if (selectedTopic.value) {
      loadVocabularies();
      // refresh topic stats
      try {
        const refreshed = await vocabularyService.getTopicById(selectedTopic.value.id);
        selectedTopic.value = refreshed;
      } catch (e) {}
    }
    loadTopics();
    fetchOverallStats();
    reviewStore.fetchSummary();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu từ vựng');
  } finally {
    savingWord.value = false;
  }
}

function confirmDeleteWord(item: Vocabulary) {
  wordToDelete.value = item;
  showDeleteWordDialog.value = true;
}

async function handleDeleteWord() {
  if (!wordToDelete.value) return;
  deletingWord.value = true;
  try {
    await vocabularyService.deleteVocabulary(wordToDelete.value.id);
    toastStore.success('Đã xóa từ vựng thành công');
    showDeleteWordDialog.value = false;
    showWordDetailModal.value = false;
    if (selectedTopic.value) {
      loadVocabularies();
      try {
        const refreshed = await vocabularyService.getTopicById(selectedTopic.value.id);
        selectedTopic.value = refreshed;
      } catch (e) {}
    }
    loadTopics();
    fetchOverallStats();
    reviewStore.fetchSummary();
  } catch (err) {
    toastStore.error('Không thể xóa từ vựng');
  } finally {
    deletingWord.value = false;
  }
}

async function markMastered(item: Vocabulary) {
  try {
    const updated = await vocabularyService.markAsMastered(item.id);
    if (selectedWord.value && selectedWord.value.id === item.id) {
      selectedWord.value = updated;
    }
    toastStore.success('Đã đánh dấu từ vựng là Thuộc Lòng!');
    if (selectedTopic.value) {
      loadVocabularies();
      try {
        const refreshed = await vocabularyService.getTopicById(selectedTopic.value.id);
        selectedTopic.value = refreshed;
      } catch (e) {}
    }
    loadTopics();
    fetchOverallStats();
    reviewStore.fetchSummary();
  } catch (err) {
    toastStore.error('Không thể cập nhật trạng thái');
  }
}

// --- Helpers ---
function getTopicStatusVariant(status: string): 'slate' | 'primary' | 'warning' | 'success' {
  switch (status?.toUpperCase()) {
    case 'NEW':
      return 'primary';
    case 'LEARNING':
      return 'warning';
    case 'MASTERED':
      return 'success';
    default:
      return 'slate';
  }
}

function getTopicStatusLabel(status: string): string {
  switch (status?.toUpperCase()) {
    case 'NEW':
      return 'Mới';
    case 'LEARNING':
      return 'Đang học';
    case 'MASTERED':
      return 'Hoàn thành';
    default:
      return status || 'Mới';
  }
}

function getVocabStatusVariant(status: string): 'slate' | 'primary' | 'warning' | 'success' {
  switch (status?.toUpperCase()) {
    case 'NEW':
      return 'primary';
    case 'LEARNING':
    case 'REVIEW':
      return 'warning';
    case 'MASTERED':
      return 'success';
    default:
      return 'slate';
  }
}

function getVocabStatusLabel(status: string): string {
  switch (status?.toUpperCase()) {
    case 'NEW':
      return 'Mới';
    case 'LEARNING':
      return 'Đang học';
    case 'REVIEW':
      return 'Cần ôn';
    case 'MASTERED':
      return 'Thuộc lòng';
    default:
      return status || 'Mới';
  }
}

function formatDate(isoStr?: string): string {
  if (!isoStr) return '';
  return new Date(isoStr).toLocaleDateString('vi-VN');
}
</script>
