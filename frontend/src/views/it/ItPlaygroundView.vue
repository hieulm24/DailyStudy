<template>
  <div class="space-y-6 w-full">
    <!-- Header Banner -->
    <div class="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 rounded-md p-6 sm:p-7 text-white relative overflow-hidden border border-slate-800 shadow-sm">
      <div class="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:20px_20px]"></div>
      <div class="relative z-10 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div class="space-y-1.5">
          <div class="flex items-center gap-2">
            <span class="px-2.5 py-0.5 rounded-sm text-xs font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              Interactive Sandbox
            </span>
            <span class="px-2.5 py-0.5 rounded-sm text-xs font-bold bg-teal-500/20 text-teal-300 border border-teal-500/30">
              AI Debug & Performance Engine
            </span>
          </div>
          <h1 class="text-xl sm:text-2xl font-bold tracking-tight">
            Trình Luyện Code & Tối ưu SQL Trực tiếp
          </h1>
          <p class="text-xs sm:text-sm text-slate-300 max-w-2xl leading-relaxed">
            Thực hành truy vấn SQL với bảng kết quả động, kiểm thử code Java Spring Boot, JavaScript, Python kèm AI phân tích lỗi và tối ưu hiệu năng $O(N)$.
          </p>
        </div>

        <div class="flex items-center gap-2">
          <button
            type="button"
            @click="openApiKeyModal"
            :class="[
              'px-3.5 py-1.5 rounded-md text-xs font-semibold border flex items-center gap-2 transition-all cursor-pointer',
              hasGeminiKey
                ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40 hover:bg-emerald-500/30'
                : 'bg-amber-500/20 text-amber-300 border-amber-500/40 hover:bg-amber-500/30'
            ]"
            :title="hasGeminiKey ? 'Đang kết nối trực tiếp Gemini AI - Nhấn để đổi key' : 'Đang ở chế độ Offline - Nhấn để nhập Gemini API Key miễn phí'"
          >
            <span :class="['w-2 h-2 rounded-full', hasGeminiKey ? 'bg-emerald-400 animate-pulse' : 'bg-amber-400']"></span>
            <span>{{ hasGeminiKey ? 'Gemini 1.5 Flash Live' : 'Chưa nhập API Key' }}</span>
            <KeyRound class="w-3.5 h-3.5" />
          </button>

          <button
            type="button"
            class="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-md text-xs font-semibold border border-slate-700 flex items-center gap-2 transition-all"
            @click="activeOutputTab = 'snippets'; loadSnippets()"
          >
            <Code2 class="w-4 h-4 text-indigo-400" />
            <span>Kho Snippets ({{ snippets.length }})</span>
          </button>
        </div>
      </div>
    </div>

    <!-- IDE Workspace Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-5 items-start">
      <!-- LEFT COLUMN: CODE EDITOR (7 Cols) -->
      <div class="lg:col-span-7 bg-slate-950 rounded-md border border-slate-800 shadow-sm overflow-hidden flex flex-col">
        <!-- Editor Header & Controls -->
        <div class="p-3 bg-slate-900 border-b border-slate-800 flex flex-wrap items-center justify-between gap-3">
          <!-- Language Selector -->
          <div class="flex items-center gap-2">
            <div class="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-slate-800 border border-slate-700 text-xs font-mono text-indigo-400">
              <Terminal class="w-3.5 h-3.5" />
              <select
                v-model="playgroundLanguage"
                class="bg-transparent text-white font-semibold focus:outline-none cursor-pointer"
                @change="applySampleTemplate"
              >
                <option value="SQL" class="bg-slate-900 text-white">SQL (Database Engine)</option>
                <option value="JAVA" class="bg-slate-900 text-white">Java (Spring Boot / Core)</option>
                <option value="JAVASCRIPT" class="bg-slate-900 text-white">JavaScript / TypeScript</option>
                <option value="PYTHON" class="bg-slate-900 text-white">Python</option>
              </select>
            </div>

            <!-- Sample Template Buttons -->
            <button
              type="button"
              class="px-2.5 py-1 text-[11px] rounded-md bg-slate-800/80 hover:bg-slate-700 text-slate-300 transition-colors"
              @click="applySampleTemplate"
            >
              Mẫu code chuẩn
            </button>
          </div>

          <!-- Quick Action Buttons -->
          <div class="flex items-center gap-1.5">
            <button
              type="button"
              title="Xóa màn hình code"
              class="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded-md transition-colors"
              @click="playgroundCode = ''"
            >
              <RotateCcw class="w-3.5 h-3.5" />
            </button>
            <button
              type="button"
              title="Lưu thành Snippet"
              class="px-2.5 py-1 text-xs font-semibold rounded-md bg-slate-800 hover:bg-indigo-600 hover:text-white text-slate-300 border border-slate-700 transition-all flex items-center gap-1"
              @click="openSaveSnippetModal"
            >
              <Save class="w-3.5 h-3.5" />
              <span>Lưu</span>
            </button>
          </div>
        </div>

        <!-- Code Textarea Editor -->
        <div class="relative bg-slate-950 p-4 font-mono text-xs sm:text-sm">
          <textarea
            v-model="playgroundCode"
            rows="18"
            class="w-full bg-transparent text-slate-100 focus:outline-none resize-y font-mono leading-relaxed selection:bg-indigo-600/40"
            placeholder="Nhập code hoặc truy vấn SQL vào đây..."
            spellcheck="false"
          ></textarea>
        </div>

        <!-- Execution Toolbar -->
        <div class="p-3 bg-slate-900 border-t border-slate-800 flex flex-wrap items-center justify-between gap-3">
          <div class="flex items-center gap-2">
            <AppButton
              variant="primary"
              size="sm"
              :icon="Play"
              class="bg-emerald-600 hover:bg-emerald-700 text-white font-bold"
              :loading="executing"
              @click="runCode"
            >
              Chạy code
            </AppButton>

            <AppButton
              variant="secondary"
              size="sm"
              :icon="Bug"
              class="bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 border border-rose-800/50"
              :loading="debugging"
              @click="debugWithAi"
            >
              AI Sửa lỗi
            </AppButton>

            <AppButton
              variant="secondary"
              size="sm"
              :icon="Zap"
              class="bg-indigo-950/40 hover:bg-indigo-900/60 text-indigo-300 border border-indigo-800/50"
              :loading="optimizing"
              @click="optimizeWithAi"
            >
              AI Tối ưu
            </AppButton>
          </div>

          <span class="text-[11px] text-slate-500">
            {{ playgroundLanguage }} • {{ countLines(playgroundCode) }} dòng
          </span>
        </div>
      </div>

      <!-- RIGHT COLUMN: OUTPUT & AI ANALYSIS (5 Cols) -->
      <div class="lg:col-span-5 bg-white rounded-md border border-slate-200 shadow-sm overflow-hidden flex flex-col min-h-[520px]">
        <!-- Output Navigation Tabs -->
        <div class="p-2 bg-slate-50 border-b border-slate-200 flex items-center gap-1">
          <button
            type="button"
            :class="[
              'px-3 py-1.5 rounded-md text-xs font-bold transition-all flex items-center gap-1.5',
              activeOutputTab === 'result'
                ? 'bg-white text-indigo-600 shadow-2xs border border-slate-200'
                : 'text-slate-600 hover:text-slate-900'
            ]"
            @click="activeOutputTab = 'result'"
          >
            <Terminal class="w-3.5 h-3.5" />
            <span>Kết quả thực thi</span>
          </button>

          <button
            type="button"
            :class="[
              'px-3 py-1.5 rounded-md text-xs font-bold transition-all flex items-center gap-1.5',
              activeOutputTab === 'analysis'
                ? 'bg-white text-indigo-600 shadow-2xs border border-slate-200'
                : 'text-slate-600 hover:text-slate-900'
            ]"
            @click="activeOutputTab = 'analysis'"
          >
            <Sparkles class="w-3.5 h-3.5 text-indigo-600" />
            <span>AI Phân tích & Tối ưu</span>
          </button>

          <button
            type="button"
            :class="[
              'px-3 py-1.5 rounded-md text-xs font-bold transition-all flex items-center gap-1.5',
              activeOutputTab === 'snippets'
                ? 'bg-white text-indigo-600 shadow-2xs border border-slate-200'
                : 'text-slate-600 hover:text-slate-900'
            ]"
            @click="activeOutputTab = 'snippets'; loadSnippets()"
          >
            <Code2 class="w-3.5 h-3.5" />
            <span>Kho Snippets</span>
          </button>
        </div>

        <!-- TAB 1: RESULT CONSOLE / SQL DATA TABLE -->
        <div v-show="activeOutputTab === 'result'" class="p-4 flex-1 flex flex-col space-y-4 overflow-y-auto">
          <div v-if="!executionResult && !executing" class="text-center py-16 text-xs text-slate-400 space-y-2">
            <Play class="w-8 h-8 mx-auto text-slate-300" />
            <p>Nhấn <strong>"Chạy code"</strong> để xem kết quả thực thi và dữ liệu trả về.</p>
          </div>

          <div v-if="executing" class="text-center py-16 text-xs text-slate-500 space-y-2">
            <div class="w-6 h-6 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
            <p>Đang thực thi mã nguồn...</p>
          </div>

          <template v-if="executionResult && !executing">
            <!-- Execution Status Banner -->
            <div
              :class="[
                'p-3 rounded-md border text-xs flex items-center justify-between',
                executionResult.success
                  ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
                  : 'bg-rose-50 border-rose-200 text-rose-800'
              ]"
            >
              <div class="flex items-center gap-2">
                <CheckCircle2 v-if="executionResult.success" class="w-4 h-4 text-emerald-600" />
                <AlertCircle v-else class="w-4 h-4 text-rose-600" />
                <span class="font-bold">{{ executionResult.success ? 'Thực thi thành công' : 'Có lỗi khi thực thi' }}</span>
              </div>
              <span class="text-[10px] opacity-75 font-mono">{{ executionResult.executionTimeMs || 0 }}ms</span>
            </div>

            <!-- SQL Interactive Data Table (If SQL) -->
            <div v-if="executionResult.sqlColumns && executionResult.sqlColumns.length > 0" class="space-y-2">
              <div class="flex items-center justify-between text-xs text-slate-500">
                <span class="font-semibold text-slate-700 flex items-center gap-1">
                  <Database class="w-3.5 h-3.5 text-indigo-600" />
                  <span>Bảng dữ liệu trả về ({{ executionResult.sqlResultTable?.length || 0 }} bản ghi)</span>
                </span>
              </div>

              <div class="overflow-x-auto border border-slate-200 rounded-md max-h-72">
                <table class="w-full text-left text-xs font-mono">
                  <thead class="bg-slate-100 text-slate-700 uppercase font-bold text-[10px] sticky top-0">
                    <tr>
                      <th
                        v-for="col in executionResult.sqlColumns"
                        :key="col"
                        class="px-3 py-2 border-b border-slate-200"
                      >
                        {{ col }}
                      </th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100 bg-white">
                    <tr
                      v-for="(row, rIdx) in executionResult.sqlResultTable"
                      :key="rIdx"
                      class="hover:bg-slate-50"
                    >
                      <td
                        v-for="col in executionResult.sqlColumns"
                        :key="col"
                        class="px-3 py-2 text-slate-800 whitespace-nowrap"
                      >
                        {{ row[col] !== undefined ? row[col] : 'NULL' }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Output Logs / Console -->
            <div class="space-y-1.5">
              <span class="text-xs font-bold text-slate-700">Console Output / Logs:</span>
              <pre class="p-3 bg-slate-950 text-slate-100 rounded-md text-xs font-mono overflow-x-auto leading-normal border border-slate-800 max-h-60">{{ executionResult.stdout || executionResult.stderr || 'Không có log đầu ra' }}</pre>
            </div>
          </template>
        </div>

        <!-- TAB 2: AI ANALYSIS & OPTIMIZATION -->
        <div v-show="activeOutputTab === 'analysis'" class="p-4 flex-1 overflow-y-auto space-y-4">
          <div v-if="!aiAnalysisContent && !debugging && !optimizing" class="text-center py-16 text-xs text-slate-400 space-y-2">
            <Sparkles class="w-8 h-8 mx-auto text-slate-300" />
            <p>Nhấn <strong>"AI Sửa lỗi"</strong> hoặc <strong>"AI Tối ưu"</strong> để nhận phân tích chuyên sâu.</p>
          </div>

          <div v-if="debugging || optimizing" class="text-center py-16 text-xs text-slate-500 space-y-2">
            <div class="w-6 h-6 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
            <p>{{ debugging ? 'AI đang phân tích & debug lỗi...' : 'AI đang đánh giá độ phức tạp & tối ưu...' }}</p>
          </div>

          <div v-if="aiAnalysisContent && !debugging && !optimizing" class="space-y-4">
            <div class="flex items-center justify-between border-b border-slate-100 pb-2">
              <span class="font-bold text-xs text-slate-900 flex items-center gap-1.5">
                <Sparkles class="w-4 h-4 text-indigo-600" />
                <span>Báo cáo Đánh giá từ Principal AI</span>
              </span>
              <button
                type="button"
                class="text-xs text-indigo-600 hover:text-indigo-800 flex items-center gap-1 font-semibold"
                @click="copyText(aiAnalysisContent)"
              >
                <Copy class="w-3.5 h-3.5" />
                <span>Sao chép</span>
              </button>
            </div>

            <!-- Analysis Markdown Body -->
            <div class="prose prose-slate max-w-none text-xs leading-relaxed font-sans whitespace-pre-wrap text-slate-800 bg-slate-50 p-4 rounded-md border border-slate-200">
              {{ aiAnalysisContent }}
            </div>
          </div>
        </div>

        <!-- TAB 3: SNIPPETS REPOSITORY -->
        <div v-show="activeOutputTab === 'snippets'" class="p-4 flex-1 overflow-y-auto space-y-3">
          <div class="flex items-center justify-between pb-2 border-b border-slate-100">
            <span class="font-bold text-xs text-slate-800">Kho Code Snippets</span>
            <button
              type="button"
              class="text-xs text-indigo-600 hover:text-indigo-800 flex items-center gap-1 font-semibold"
              @click="openSaveSnippetModal"
            >
              <Plus class="w-3.5 h-3.5" />
              <span>Lưu code hiện tại</span>
            </button>
          </div>

          <div v-if="loadingSnippets" class="text-center py-8 text-xs text-slate-400">
            Đang tải kho snippets...
          </div>
          <div v-else-if="snippets.length === 0" class="text-center py-8 text-xs text-slate-400">
            Chưa có snippet nào được lưu
          </div>

          <div
            v-for="s in snippets"
            :key="s.id"
            class="p-3 bg-slate-50 hover:bg-indigo-50/50 rounded-md border border-slate-200 transition-all space-y-2 group cursor-pointer"
            @click="loadSnippetIntoEditor(s)"
          >
            <div class="flex items-center justify-between">
              <span class="font-bold text-xs text-slate-900 group-hover:text-indigo-600 truncate max-w-[200px]">
                {{ s.title }}
              </span>
              <span class="px-1.5 py-0.5 rounded-sm text-[10px] font-bold bg-slate-200 text-slate-700">
                {{ s.language }}
              </span>
            </div>

            <p v-if="s.explanation" class="text-[11px] text-slate-500 line-clamp-1">
              {{ s.explanation }}
            </p>

            <div class="flex items-center justify-between pt-1 text-[10px] text-slate-400 border-t border-slate-200/60" @click.stop>
              <span>{{ formatDate(s.updatedAt || s.createdAt) }}</span>
              <div class="flex items-center gap-2">
                <button
                  type="button"
                  class="hover:text-indigo-600"
                  title="Nạp vào Editor"
                  @click="loadSnippetIntoEditor(s)"
                >
                  Nạp code
                </button>
                <button
                  type="button"
                  class="hover:text-rose-600"
                  title="Xóa"
                  @click="deleteSnippet(s.id)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL: LƯU CODE SNIPPET -->
    <AppModal
      v-model="showSnippetModal"
      title="Lưu Code Snippet vào Kho"
      size="md"
    >
      <form class="space-y-4" @submit.prevent="saveSnippet">
        <AppInput
          v-model="snippetForm.title"
          label="Tiêu đề Snippet"
          placeholder="e.g. Truy vấn CTE đệ quy SQL phân cấp"
          required
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">Ngôn ngữ</label>
            <select
              v-model="snippetForm.language"
              class="w-full text-xs bg-white border border-slate-300 rounded-md px-3 py-2"
            >
              <option value="SQL">SQL</option>
              <option value="JAVA">Java</option>
              <option value="JAVASCRIPT">JavaScript</option>
              <option value="PYTHON">Python</option>
            </select>
          </div>

          <AppInput
            v-model="snippetForm.tags"
            label="Thẻ Tags"
            placeholder="cte, recursion, sql"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">Mô tả tóm tắt</label>
          <input
            v-model="snippetForm.explanation"
            type="text"
            class="w-full text-xs bg-white border border-slate-300 rounded-md px-3 py-2"
            placeholder="Giải thích ngắn gọn mục đích đoạn code"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">Nội dung Code</label>
          <textarea
            v-model="snippetForm.codeContent"
            rows="6"
            class="w-full font-mono text-xs bg-slate-950 text-slate-100 p-3 rounded-md"
            required
          ></textarea>
        </div>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" @click="showSnippetModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="md" :loading="savingSnippet">
            Lưu Snippet
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- MODAL: CẤU HÌNH GOOGLE GEMINI API KEY -->
    <AppModal
      v-model="showApiKeyModal"
      title="Cấu hình Google Gemini AI Key"
      size="md"
    >
      <div class="space-y-4 text-xs text-slate-700">
        <div class="p-3.5 rounded-md bg-indigo-50/80 border border-indigo-100 space-y-2">
          <div class="flex items-center gap-2 text-indigo-900 font-bold text-xs">
            <Sparkles class="w-4 h-4 text-indigo-600" />
            <span>Tích hợp Google Gemini 1.5 Flash Trực tiếp</span>
          </div>
          <p class="leading-relaxed text-slate-600">
            Google cung cấp <strong class="text-indigo-700">API Key miễn phí 100%</strong> (không yêu cầu thẻ tín dụng). Khi nhập key, AI sẽ phân tích lỗi, tối ưu hiệu năng và giải thích code trực tiếp theo thời gian thực!
          </p>
          <a
            href="https://aistudio.google.com/app/apikey"
            target="_blank"
            rel="noopener noreferrer"
            class="inline-flex items-center gap-1 text-xs font-bold text-indigo-600 hover:text-indigo-800 underline mt-1"
          >
            <span>👉 Nhấp vào đây để lấy Gemini API Key miễn phí (Google AI Studio)</span>
            <ExternalLink class="w-3.5 h-3.5" />
          </a>
        </div>

        <div>
          <AppInput
            v-model="geminiKeyInput"
            label="Google Gemini API Key"
            placeholder="AIzaSy..."
            type="password"
            hint="Key được lưu trực tiếp trong trình duyệt (localStorage) để bảo mật cho bạn."
          />
        </div>

        <div class="flex items-center justify-between pt-3 border-t border-slate-100">
          <button
            v-if="hasGeminiKey"
            type="button"
            class="text-xs text-rose-600 hover:text-rose-800 font-semibold underline"
            @click="clearApiKey"
          >
            Xóa API Key
          </button>
          <div v-else></div>

          <div class="flex items-center gap-2">
            <AppButton variant="secondary" size="sm" @click="showApiKeyModal = false">
              Đóng
            </AppButton>
            <AppButton variant="primary" size="sm" @click="saveApiKey">
              Lưu API Key
            </AppButton>
          </div>
        </div>
      </div>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { itStudioService } from '../../services/it-studio.service';
import { useToastStore } from '../../stores/toast.store';
import type { ItCodeSnippet, ItPlaygroundExecutionResponse } from '../../types';
import AppButton from '../../components/common/AppButton.vue';
import AppInput from '../../components/common/AppInput.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  Terminal,
  Play,
  Bug,
  Zap,
  Save,
  RotateCcw,
  Sparkles,
  Database,
  Copy,
  Plus,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Code2,
  KeyRound,
  ExternalLink,
} from 'lucide-vue-next';

const toastStore = useToastStore();

const playgroundLanguage = ref<'SQL' | 'JAVA' | 'JAVASCRIPT' | 'PYTHON'>('SQL');
const playgroundCode = ref(`-- Truy vấn CTE đệ quy và tổng hợp phân cấp danh mục
WITH RecursiveCategory AS (
    SELECT id, name, parent_id, 1 as Level
    FROM categories
    WHERE parent_id IS NULL
    UNION ALL
    SELECT c.id, c.name, c.parent_id, rc.Level + 1
    FROM categories c
    INNER JOIN RecursiveCategory rc ON c.parent_id = rc.id
)
SELECT id, name, parent_id, Level
FROM RecursiveCategory
ORDER BY Level, id;`);

const activeOutputTab = ref<'result' | 'analysis' | 'snippets'>('result');
const executing = ref(false);
const debugging = ref(false);
const optimizing = ref(false);
const executionResult = ref<ItPlaygroundExecutionResponse | null>(null);
const aiAnalysisContent = ref<string>('');

// Snippets
const snippets = ref<ItCodeSnippet[]>([]);
const loadingSnippets = ref(false);
const showSnippetModal = ref(false);
const savingSnippet = ref(false);

const snippetForm = reactive({
  title: '',
  language: 'SQL',
  codeContent: '',
  explanation: '',
  tags: '',
});

onMounted(() => {
  loadSnippets();
});

const sampleTemplates: Record<string, string> = {
  SQL: `-- Truy vấn CTE đệ quy và tổng hợp phân cấp danh mục
WITH RecursiveCategory AS (
    SELECT id, name, parent_id, 1 as Level
    FROM categories
    WHERE parent_id IS NULL
    UNION ALL
    SELECT c.id, c.name, c.parent_id, rc.Level + 1
    FROM categories c
    INNER JOIN RecursiveCategory rc ON c.parent_id = rc.id
)
SELECT id, name, parent_id, Level
FROM RecursiveCategory
ORDER BY Level, id;`,

  JAVA: `package com.englishlearning.demo;

import java.util.concurrent.*;

public class CompletableFutureExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        CompletableFuture<String> userTask = CompletableFuture.supplyAsync(() -> {
            return "User Profile Loaded";
        }, executor);

        CompletableFuture<String> orderTask = CompletableFuture.supplyAsync(() -> {
            return "Orders Loaded";
        }, executor);

        CompletableFuture.allOf(userTask, orderTask)
            .thenRun(() -> System.out.println("All data aggregated successfully!"))
            .join();
            
        executor.shutdown();
    }
}`,

  JAVASCRIPT: `// Async concurrent worker with Promise.allSettled
async function fetchSystemMetrics() {
  const endpoints = ['/api/kafka/lag', '/api/db/pool', '/api/redis/memory'];
  
  const results = await Promise.allSettled(
    endpoints.map(url => ({ endpoint: url, status: 'HEALTHY', latencyMs: Math.floor(Math.random() * 20) + 5 }))
  );
  
  console.log('System Health Summary:', JSON.stringify(results, null, 2));
}

fetchSystemMetrics();`,

  PYTHON: `# Distributed Rate Limiter Token Bucket Simulation
import time

class TokenBucket:
    def __init__(self, capacity, fill_rate):
        self.capacity = capacity
        self.fill_rate = fill_rate
        self.tokens = capacity
        self.last_fill = time.time()

    def allow_request(self, tokens=1):
        now = time.time()
        self.tokens = min(self.capacity, self.tokens + (now - self.last_fill) * self.fill_rate)
        self.last_fill = now
        if self.tokens >= tokens:
            self.tokens -= tokens
            return True
        return False

bucket = TokenBucket(capacity=10, fill_rate=2)
print("Request 1:", bucket.allow_request(5))
print("Request 2:", bucket.allow_request(6))
`,
};

function applySampleTemplate() {
  playgroundCode.value = sampleTemplates[playgroundLanguage.value] || '';
}

function countLines(code: string) {
  if (!code) return 0;
  return code.split('\n').length;
}

async function runCode() {
  if (!playgroundCode.value.trim()) {
    toastStore.error('Vui lòng nhập code để chạy');
    return;
  }
  executing.value = true;
  activeOutputTab.value = 'result';
  try {
    executionResult.value = await itStudioService.executeOrAnalyze({
      language: playgroundLanguage.value,
      code: playgroundCode.value,
      action: 'RUN',
    });
    toastStore.success('Thực thi hoàn tất');
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể thực thi code');
  } finally {
    executing.value = false;
  }
}

async function debugWithAi() {
  if (!playgroundCode.value.trim()) return;
  debugging.value = true;
  activeOutputTab.value = 'analysis';
  try {
    const res = await itStudioService.executeOrAnalyze({
      language: playgroundLanguage.value,
      code: playgroundCode.value,
      action: 'DEBUG_AI',
    });
    executionResult.value = res;
    aiAnalysisContent.value = res.aiAnalysis || (res.stdout || 'Không có nội dung phân tích.');
    toastStore.success('AI đã phân tích và tìm ra phương án sửa lỗi!');
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể debug code');
  } finally {
    debugging.value = false;
  }
}

async function optimizeWithAi() {
  if (!playgroundCode.value.trim()) return;
  optimizing.value = true;
  activeOutputTab.value = 'analysis';
  try {
    const res = await itStudioService.executeOrAnalyze({
      language: playgroundLanguage.value,
      code: playgroundCode.value,
      action: 'OPTIMIZE_AI',
    });
    executionResult.value = res;
    aiAnalysisContent.value = res.aiAnalysis || (res.stdout || 'Không có nội dung phân tích.');
    toastStore.success('AI đã đưa ra bản tối ưu hiệu năng $O(N)$!');
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể tối ưu code');
  } finally {
    optimizing.value = false;
  }
}

async function loadSnippets() {
  loadingSnippets.value = true;
  try {
    const res = await itStudioService.getSnippets({});
    snippets.value = res.items;
  } catch (err) {
    console.warn('Failed to load snippets:', err);
  } finally {
    loadingSnippets.value = false;
  }
}

function openSaveSnippetModal() {
  snippetForm.title = '';
  snippetForm.language = playgroundLanguage.value;
  snippetForm.codeContent = playgroundCode.value;
  snippetForm.explanation = '';
  snippetForm.tags = '';
  showSnippetModal.value = true;
}

async function saveSnippet() {
  if (!snippetForm.title.trim() || !snippetForm.codeContent.trim()) {
    toastStore.error('Tiêu đề và nội dung code không được để trống');
    return;
  }
  savingSnippet.value = true;
  try {
    await itStudioService.createSnippet(snippetForm);
    toastStore.success('Đã lưu code snippet vào kho!');
    showSnippetModal.value = false;
    await loadSnippets();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể lưu snippet');
  } finally {
    savingSnippet.value = false;
  }
}

function loadSnippetIntoEditor(s: ItCodeSnippet) {
  playgroundLanguage.value = s.language as any;
  playgroundCode.value = s.codeContent;
  toastStore.success(`Đã nạp "${s.title}" vào trình soạn thảo!`);
}

async function deleteSnippet(id: number) {
  try {
    await itStudioService.deleteSnippet(id);
    snippets.value = snippets.value.filter((s) => s.id !== id);
    toastStore.success('Đã xóa snippet');
  } catch (err) {
    toastStore.error('Không thể xóa snippet');
  }
}

function copyText(text: string) {
  if (!text) return;
  navigator.clipboard.writeText(text);
  toastStore.success('Đã sao chép vào clipboard');
}

function formatDate(dateStr?: string) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
}

// -------------------------------------------------------------
// GOOGLE GEMINI API KEY MODAL
// -------------------------------------------------------------
const showApiKeyModal = ref(false);
const geminiKeyInput = ref(localStorage.getItem('gemini_api_key') || '');
const hasGeminiKey = computed(() => !!localStorage.getItem('gemini_api_key'));

function openApiKeyModal() {
  geminiKeyInput.value = localStorage.getItem('gemini_api_key') || '';
  showApiKeyModal.value = true;
}

function saveApiKey() {
  if (geminiKeyInput.value.trim()) {
    localStorage.setItem('gemini_api_key', geminiKeyInput.value.trim());
    toastStore.success('Đã lưu Google Gemini API Key! AI đã sẵn sàng hoạt động trực tiếp.');
  } else {
    localStorage.removeItem('gemini_api_key');
    toastStore.info('Đã chuyển về chế độ Offline AI.');
  }
  showApiKeyModal.value = false;
}

function clearApiKey() {
  localStorage.removeItem('gemini_api_key');
  geminiKeyInput.value = '';
  toastStore.info('Đã xóa API Key. Hệ thống chuyển về chế độ Offline.');
  showApiKeyModal.value = false;
}
</script>
