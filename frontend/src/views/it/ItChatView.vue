<template>
  <div class="h-[calc(100vh-6.5rem)] flex flex-col md:flex-row gap-4 pb-4 w-full">
    <!-- LEFT SIDEBAR: CHAT SESSIONS -->
    <div
      :class="[
        'w-full md:w-80 bg-white rounded-md border border-slate-200 shadow-xs flex flex-col shrink-0 overflow-hidden transition-all duration-200',
        showMobileSessions ? 'block' : 'hidden md:flex'
      ]"
    >
      <!-- Header / New Chat Button -->
      <div class="p-4 border-b border-slate-100 bg-slate-50/50 space-y-3">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <Bot class="w-5 h-5 text-indigo-600" />
            <h2 class="font-bold text-slate-800 text-sm">Lịch sử Hội thoại AI</h2>
          </div>
          <button
            type="button"
            class="md:hidden p-1 text-slate-400 hover:text-slate-600 rounded-md"
            @click="showMobileSessions = false"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <button
          type="button"
          class="w-full flex items-center justify-center gap-2 py-2 px-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md text-xs font-semibold shadow-xs hover:shadow transition-all"
          @click="createNewSession"
        >
          <Plus class="w-4 h-4" />
          <span>Phiên Tư vấn Mới</span>
        </button>

        <!-- Search Sessions -->
        <div class="relative">
          <Search class="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            v-model="sessionSearch"
            type="text"
            placeholder="Tìm cuộc trò chuyện..."
            class="w-full pl-8.5 pr-3 py-1.5 text-xs bg-white border border-slate-200 rounded-md focus:outline-none focus:border-indigo-500 text-slate-800"
          />
        </div>
      </div>

      <!-- Session List -->
      <div class="flex-1 overflow-y-auto p-2 space-y-1">
        <div v-if="loadingSessions" class="p-4 text-center text-xs text-slate-400">
          Đang tải lịch sử...
        </div>
        <div v-else-if="filteredSessions.length === 0" class="p-6 text-center text-xs text-slate-400">
          Chưa có cuộc trò chuyện nào
        </div>
        <div
          v-for="s in filteredSessions"
          :key="s.id"
          :class="[
            'group relative flex items-center justify-between p-2.5 rounded-md cursor-pointer text-xs transition-all border',
            currentSession?.id === s.id
              ? 'bg-indigo-50/80 border-indigo-200 text-indigo-900 font-semibold shadow-2xs'
              : 'border-transparent hover:bg-slate-50 text-slate-700 hover:text-slate-900'
          ]"
          @click="selectSession(s.id)"
        >
          <div class="flex items-center gap-2.5 truncate flex-1 min-w-0 pr-2">
            <MessagesSquare class="w-4 h-4 shrink-0 text-indigo-500" />
            <span class="truncate">{{ s.title }}</span>
          </div>

          <div class="flex items-center gap-1 shrink-0">
            <span
              v-if="s.topicCategory"
              class="px-1.5 py-0.5 text-[9px] font-bold rounded-sm bg-slate-100 text-slate-500 group-hover:bg-indigo-100 group-hover:text-indigo-700 transition-colors"
            >
              {{ formatCategoryTag(s.topicCategory) }}
            </span>
            <button
              type="button"
              title="Xóa phiên"
              class="opacity-0 group-hover:opacity-100 p-1 text-slate-400 hover:text-rose-600 rounded-sm transition-opacity"
              @click.stop="deleteSession(s.id)"
            >
              <Trash2 class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- RIGHT MAIN: CHAT THREAD & INPUT -->
    <div class="flex-1 bg-white rounded-md border border-slate-200 shadow-xs flex flex-col overflow-hidden min-w-0">
      <!-- Chat Header -->
      <div class="px-5 py-3 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between shrink-0">
        <div class="flex items-center gap-3 min-w-0">
          <button
            type="button"
            class="md:hidden p-1.5 text-slate-500 hover:bg-slate-200 rounded-md"
            @click="showMobileSessions = true"
          >
            <Menu class="w-5 h-5" />
          </button>
          <div class="w-8 h-8 rounded-md bg-gradient-to-tr from-indigo-600 to-purple-600 text-white flex items-center justify-center shrink-0 shadow-xs">
            <Bot class="w-4 h-4" />
          </div>
          <div class="min-w-0">
            <div class="flex items-center gap-2">
              <h1 class="font-bold text-slate-900 text-sm truncate">
                {{ currentSession?.title || 'AI System Architect & Coding Mentor' }}
              </h1>
              <button
                type="button"
                @click="openApiKeyModal"
                :class="[
                  'px-2 py-0.5 rounded-sm text-[10px] font-bold border flex items-center gap-1.5 transition-all cursor-pointer',
                  hasGeminiKey
                    ? 'bg-emerald-50 text-emerald-700 border-emerald-300 hover:bg-emerald-100'
                    : 'bg-amber-50 text-amber-800 border-amber-300 hover:bg-amber-100'
                ]"
                :title="hasGeminiKey ? 'Đang kết nối trực tiếp Gemini AI - Nhấn để đổi key' : 'Đang ở chế độ Offline - Nhấn để nhập Gemini API Key miễn phí'"
              >
                <span :class="['w-1.5 h-1.5 rounded-full', hasGeminiKey ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500']"></span>
                <span>{{ hasGeminiKey ? 'Gemini 1.5 Flash Live' : 'Chưa nhập API Key' }}</span>
                <KeyRound class="w-3 h-3 text-slate-400" />
              </button>
            </div>
          </div>
        </div>

        <!-- Action tools -->
        <div class="flex items-center gap-1.5">
          <button
            type="button"
            title="Cấu hình Google Gemini API Key"
            class="p-2 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors text-xs font-medium flex items-center gap-1.5"
            @click="openApiKeyModal"
          >
            <KeyRound class="w-4 h-4 text-indigo-500" />
            <span class="hidden sm:inline">Cấu hình Key</span>
          </button>

          <button
            type="button"
            title="Làm mới cuộc trò chuyện"
            class="p-2 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors text-xs font-medium flex items-center gap-1.5"
            @click="createNewSession"
          >
            <Plus class="w-4 h-4" />
            <span class="hidden sm:inline">Mới</span>
          </button>
        </div>
      </div>

      <!-- Messages Stream -->
      <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 bg-slate-50/30">
        <!-- Empty Welcome Screen with Sample Prompts -->
        <div v-if="!currentSession || !currentSession.messages || currentSession.messages.length === 0" class="w-full max-w-4xl mx-auto py-8 space-y-6">
          <div class="text-center space-y-2.5">
            <div class="w-12 h-12 rounded-md bg-indigo-100 text-indigo-600 flex items-center justify-center mx-auto shadow-xs border border-indigo-200">
              <Cpu class="w-6 h-6" />
            </div>
            <h3 class="text-base sm:text-lg font-bold text-slate-900">
              Hỏi đáp Kiến trúc Hệ thống & Code Chuyên sâu
            </h3>
            <p class="text-xs text-slate-500 max-w-xl mx-auto leading-relaxed">
              Nhận tư vấn từ AI Principal Architect về Microservices, Kafka Event-Driven, tối ưu hóa Database Indexing, giải quyết Deadlock, và sinh sơ đồ Mermaid trực quan.
            </p>
          </div>

          <!-- Suggested Topic Cards -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
            <button
              v-for="(prompt, idx) in suggestedPrompts"
              :key="idx"
              type="button"
              class="p-3.5 rounded-md border border-slate-200 bg-white hover:border-indigo-300 hover:bg-indigo-50/50 transition-all text-left group shadow-2xs"
              @click="useSuggestedPrompt(prompt.text, prompt.category)"
            >
              <div class="flex items-center gap-2 mb-1">
                <span class="p-1 rounded-sm bg-indigo-100 text-indigo-700 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                  <Sparkles class="w-3.5 h-3.5" />
                </span>
                <span class="text-xs font-bold text-slate-800 group-hover:text-indigo-700">
                  {{ prompt.title }}
                </span>
              </div>
              <p class="text-[11px] text-slate-500 line-clamp-2">
                {{ prompt.text }}
              </p>
            </button>
          </div>
        </div>

        <!-- Message List -->
        <template v-else>
          <div
            v-for="(msg, index) in currentSession.messages"
            :key="msg.id || index"
            :class="[
              'flex gap-3 w-full',
              msg.senderRole === 'USER' ? 'ml-auto justify-end' : 'mr-auto justify-start'
            ]"
          >
            <!-- AI Avatar -->
            <div
              v-if="msg.senderRole === 'ASSISTANT'"
              class="w-7 h-7 rounded-md bg-gradient-to-tr from-indigo-600 to-purple-600 text-white flex items-center justify-center shrink-0 shadow-2xs mt-1"
            >
              <Bot class="w-3.5 h-3.5" />
            </div>

            <!-- Message Bubble -->
            <div
              :class="[
                'p-4 rounded-md text-xs sm:text-sm leading-relaxed max-w-[90%] sm:max-w-[85%] space-y-3',
                msg.senderRole === 'USER'
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'bg-white border border-slate-200 text-slate-800 shadow-2xs'
              ]"
            >
              <!-- Content Body -->
              <div class="whitespace-pre-wrap font-sans break-words">
                {{ msg.content }}
              </div>

              <!-- Mermaid Diagram Box -->
              <div
                v-if="msg.mermaidDiagram"
                class="mt-3 p-3.5 rounded-md bg-slate-950 text-indigo-300 font-mono text-xs border border-slate-800 space-y-2"
              >
                <div class="flex items-center justify-between text-[11px] text-slate-400 border-b border-slate-800 pb-2">
                  <div class="flex items-center gap-1.5 text-indigo-400 font-semibold">
                    <Network class="w-4 h-4" />
                    <span>Sơ đồ Kiến trúc Mermaid</span>
                  </div>
                  <button
                    type="button"
                    class="hover:text-white transition-colors flex items-center gap-1 text-[10px]"
                    @click="copyText(msg.mermaidDiagram)"
                  >
                    <Copy class="w-3 h-3" />
                    <span>Sao chép</span>
                  </button>
                </div>
                <pre class="overflow-x-auto text-[11px] text-emerald-400 leading-normal">{{ msg.mermaidDiagram }}</pre>
              </div>

              <!-- Action Toolbar for Assistant Messages -->
              <div
                v-if="msg.senderRole === 'ASSISTANT'"
                class="flex items-center justify-between pt-2 border-t border-slate-100 text-[11px] text-slate-400"
              >
                <span>{{ formatTime(msg.createdAt) }}</span>
                <div class="flex items-center gap-2">
                  <button
                    type="button"
                    title="Sao chép câu trả lời"
                    class="p-1 hover:text-indigo-600 hover:bg-slate-100 rounded-sm transition-colors flex items-center gap-1"
                    @click="copyText(msg.content)"
                  >
                    <Copy class="w-3.5 h-3.5" />
                    <span>Sao chép</span>
                  </button>
                  <button
                    type="button"
                    title="Lưu đoạn này vào Sổ tay Kiến thức"
                    class="p-1 hover:text-indigo-600 hover:bg-indigo-50 text-indigo-600 font-semibold rounded-sm transition-colors flex items-center gap-1"
                    @click="openSaveToNoteModal(msg)"
                  >
                    <BookMarked class="w-3.5 h-3.5" />
                    <span>Lưu vào Sổ tay</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- User Avatar -->
            <div
              v-if="msg.senderRole === 'USER'"
              class="w-7 h-7 rounded-md overflow-hidden shrink-0 mt-1 shadow-2xs flex items-center justify-center border border-slate-200 bg-indigo-50 text-indigo-700"
            >
              <img
                v-if="authStore.user?.avatarUrl"
                :src="authStore.user.avatarUrl"
                class="w-full h-full object-cover"
                alt="User"
              />
              <User v-else class="w-3.5 h-3.5" />
            </div>
          </div>

          <!-- Loading Indicator -->
          <div v-if="sendingMessage" class="flex gap-3 mr-auto items-center">
            <div class="w-7 h-7 rounded-md bg-indigo-600 text-white flex items-center justify-center shrink-0 animate-pulse">
              <Bot class="w-3.5 h-3.5" />
            </div>
            <div class="p-3 bg-white border border-slate-200 rounded-md shadow-2xs flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-indigo-600 animate-bounce"></span>
              <span class="w-2 h-2 rounded-full bg-indigo-600 animate-bounce [animation-delay:0.2s]"></span>
              <span class="w-2 h-2 rounded-full bg-indigo-600 animate-bounce [animation-delay:0.4s]"></span>
              <span class="text-xs text-slate-500 ml-1">AI Architect đang phân tích kiến trúc...</span>
            </div>
          </div>
        </template>
      </div>

      <!-- Chat Input Box -->
      <div class="p-3 sm:p-4 border-t border-slate-200 bg-white shrink-0">
        <form class="flex items-end gap-2" @submit.prevent="() => sendMessage()">
          <div class="flex-1 relative">
            <textarea
              v-model="chatInput"
              rows="2"
              placeholder="Hỏi về Saga Pattern, Kafka Idempotent, B-Tree Indexing, Circuit Breaker, tối ưu SQL..."
              class="w-full text-xs sm:text-sm bg-slate-50 border border-slate-200 rounded-md p-2.5 pr-10 focus:outline-none focus:border-indigo-500 focus:bg-white resize-none text-slate-900 transition-colors shadow-2xs"
              @keydown.enter.exact.prevent="() => sendMessage()"
            ></textarea>
          </div>

          <button
            type="submit"
            :disabled="!chatInput.trim() || sendingMessage"
            class="h-10 px-4 sm:px-5 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-md font-semibold text-xs flex items-center justify-center gap-1.5 shadow-xs hover:shadow transition-all shrink-0"
          >
            <Send class="w-4 h-4" />
            <span class="hidden sm:inline">Gửi</span>
          </button>
        </form>
      </div>
    </div>

    <!-- MODAL: LƯU ĐOẠN CHAT VÀO SỔ TAY KIẾN THỨC -->
    <AppModal
      v-model="showSaveNoteModal"
      title="Lưu vào Sổ tay Kiến thức"
      size="lg"
    >
      <form class="space-y-4" @submit.prevent="saveQuickNote">
        <AppInput
          v-model="quickNoteForm.title"
          label="Tiêu đề Ghi chú"
          placeholder="e.g. Tổng hợp Kiến trúc Kafka Idempotent Producer"
          required
        />

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">Danh mục</label>
            <select
              v-model="quickNoteForm.category"
              class="w-full text-xs bg-white border border-slate-300 rounded-md px-3 py-2 focus:outline-none focus:border-indigo-500"
            >
              <option value="MICROSERVICES">Microservices & Hệ thống Phân tán</option>
              <option value="KAFKA_DISTRIBUTED">Apache Kafka & Event-Driven</option>
              <option value="DATABASE_OPTIMIZATION">SQL & Database Engine Tuning</option>
              <option value="JAVA_SPRING">Java Spring Boot Enterprise</option>
              <option value="SYSTEM_DESIGN">System Design & High Availability</option>
              <option value="DEVOPS_CLOUD">DevOps & Cloud Architecture</option>
              <option value="DSA_LEETCODE">Cấu trúc Dữ liệu & Giải thuật</option>
            </select>
          </div>

          <AppInput
            v-model="quickNoteForm.tags"
            label="Thẻ Tags (cách nhau dấu phẩy)"
            placeholder="kafka, idempotency, distributed"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">Nội dung ghi chú (Markdown)</label>
          <textarea
            v-model="quickNoteForm.contentMarkdown"
            rows="8"
            required
            class="w-full text-xs font-mono bg-white border border-slate-300 rounded-md p-3 focus:outline-none focus:border-indigo-500"
          ></textarea>
        </div>

        <div v-if="quickNoteForm.diagramMermaid">
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">Sơ đồ Mermaid đi kèm</label>
          <textarea
            v-model="quickNoteForm.diagramMermaid"
            rows="4"
            class="w-full text-xs font-mono bg-slate-950 text-emerald-400 border border-slate-800 rounded-md p-3"
          ></textarea>
        </div>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" @click="showSaveNoteModal = false">
            Hủy
          </AppButton>
          <AppButton type="submit" variant="primary" size="md" :loading="savingNote">
            Lưu vào Sổ tay
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
            Google cung cấp <strong class="text-indigo-700">API Key miễn phí 100%</strong> (không yêu cầu thẻ tín dụng). Khi nhập key, AI sẽ giải đáp câu hỏi và debug code theo thời gian thực linh hoạt nhất!
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
import { ref, reactive, computed, onMounted, nextTick } from 'vue';
import { itStudioService } from '../../services/it-studio.service';
import { useAuthStore } from '../../stores/auth.store';
import { useToastStore } from '../../stores/toast.store';
import type { ItChatSession, ItChatMessage } from '../../types';
import AppInput from '../../components/common/AppInput.vue';
import AppButton from '../../components/common/AppButton.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  Bot,
  Plus,
  Trash2,
  Sparkles,
  Cpu,
  Copy,
  Network,
  Send,
  Search,
  BookMarked,
  MessagesSquare,
  Menu,
  X,
  KeyRound,
  ExternalLink,
  User,
} from 'lucide-vue-next';

const authStore = useAuthStore();
const toastStore = useToastStore();

const showMobileSessions = ref(false);
const sessions = ref<ItChatSession[]>([]);
const currentSession = ref<ItChatSession | null>(null);
const loadingSessions = ref(false);
const sendingMessage = ref(false);
const sessionSearch = ref('');
const chatInput = ref('');
const messagesContainer = ref<HTMLElement | null>(null);

const suggestedPrompts = [
  {
    title: 'Kafka Exactly-Once Semantics',
    category: 'KAFKA_DISTRIBUTED',
    text: 'Giải thích chi tiết cách Apache Kafka kết hợp Idempotent Producer và Transactional Coordinator để đạt chuẩn Exactly-Once Semantics. Hãy kèm theo sơ đồ luồng Mermaid.',
  },
  {
    title: 'Transactional Outbox Pattern',
    category: 'MICROSERVICES',
    text: 'Giải thích bản chất của Transactional Outbox Pattern trong Microservices. Tại sao không nên gọi Kafka Producer trực tiếp bên trong Spring @Transactional? Sinh sơ đồ Mermaid.',
  },
  {
    title: 'Tối ưu Indexing & Execution Plan',
    category: 'DATABASE_OPTIMIZATION',
    text: 'Phân tích nguyên lý B-Tree Index, Leftmost Prefix Rule trong Composite Index, và các trường hợp phổ biến làm mất Index (Implicit Type Casting, Leading Wildcard LIKE).',
  },
  {
    title: 'Saga Pattern vs 2PC (Distributed Tx)',
    category: 'SYSTEM_DESIGN',
    text: 'So sánh 2-Phase Commit (2PC) và Saga Pattern (Choreography vs Orchestration) khi xử lý giao dịch phân tán giữa các microservices. Khi nào nên dùng giải pháp nào?',
  },
];

const filteredSessions = computed(() => {
  if (!sessionSearch.value.trim()) return sessions.value;
  const q = sessionSearch.value.toLowerCase();
  return sessions.value.filter((s) => s.title.toLowerCase().includes(q));
});

onMounted(async () => {
  await loadSessions();
});

async function loadSessions() {
  loadingSessions.value = true;
  try {
    sessions.value = await itStudioService.getSessions();
    if (sessions.value.length > 0 && !currentSession.value) {
      await selectSession(sessions.value[0].id);
    }
  } catch (err) {
    console.warn('Failed to load chat sessions:', err);
  } finally {
    loadingSessions.value = false;
  }
}

async function selectSession(sessionId: number) {
  try {
    currentSession.value = await itStudioService.getSessionById(sessionId);
    showMobileSessions.value = false;
    await scrollToBottom();
  } catch (err) {
    toastStore.error('Không thể tải chi tiết phiên chat');
  }
}

function createNewSession() {
  currentSession.value = {
    id: 0,
    title: 'Hội thoại Mới',
    topicCategory: 'SYSTEM_DESIGN',
    messages: [],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };
  showMobileSessions.value = false;
}

async function deleteSession(id: number) {
  if (id === 0) {
    createNewSession();
    return;
  }
  try {
    await itStudioService.deleteSession(id);
    sessions.value = sessions.value.filter((s) => s.id !== id);
    if (currentSession.value?.id === id) {
      currentSession.value = sessions.value[0] || null;
      if (currentSession.value) {
        await selectSession(currentSession.value.id);
      } else {
        createNewSession();
      }
    }
    toastStore.success('Đã xóa phiên chat');
  } catch (err) {
    toastStore.error('Không thể xóa phiên chat');
  }
}

function useSuggestedPrompt(text: string, category: string) {
  chatInput.value = text;
  sendMessage(category);
}

async function sendMessage(categoryOverride?: string) {
  if (!chatInput.value.trim() || sendingMessage.value) return;

  const promptText = chatInput.value.trim();
  chatInput.value = '';

  const activeCategory = categoryOverride || currentSession.value?.topicCategory || 'SYSTEM_DESIGN';
  const targetSessionId = (currentSession.value && currentSession.value.id > 0) ? currentSession.value.id : undefined;

  // Append user message locally
  if (!currentSession.value) {
    createNewSession();
  }
  if (!currentSession.value!.messages) {
    currentSession.value!.messages = [];
  }
  currentSession.value!.messages.push({
    id: Date.now(),
    sessionId: targetSessionId || 0,
    senderRole: 'USER',
    content: promptText,
    createdAt: new Date().toISOString(),
  });

  await scrollToBottom();
  sendingMessage.value = true;

  try {
    const updatedSession = await itStudioService.sendMessage({
      sessionId: targetSessionId,
      prompt: promptText,
      topicCategory: activeCategory,
    });

    currentSession.value = updatedSession;
    
    // Update or insert in sessions list
    const foundIdx = sessions.value.findIndex(s => s.id === updatedSession.id);
    if (foundIdx >= 0) {
      sessions.value[foundIdx] = updatedSession;
    } else {
      sessions.value.unshift(updatedSession);
    }

    await scrollToBottom();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'AI đang bận, vui lòng thử lại');
  } finally {
    sendingMessage.value = false;
  }
}

async function scrollToBottom() {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
}

function copyText(text?: string) {
  if (!text) return;
  navigator.clipboard.writeText(text);
  toastStore.success('Đã sao chép vào clipboard');
}

function formatCategoryTag(cat: string) {
  const map: Record<string, string> = {
    KAFKA_DISTRIBUTED: 'Kafka',
    MICROSERVICES: 'Microservices',
    DATABASE_OPTIMIZATION: 'Database',
    JAVA_SPRING: 'Spring Boot',
    SYSTEM_DESIGN: 'System Design',
    DEVOPS_CLOUD: 'DevOps',
    DSA_LEETCODE: 'DSA',
  };
  return map[cat] || cat;
}

function formatTime(dateStr?: string) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
}

// -------------------------------------------------------------
// SAVE CHAT TO NOTE MODAL
// -------------------------------------------------------------
const showSaveNoteModal = ref(false);
const savingNote = ref(false);
const quickNoteForm = reactive({
  title: '',
  category: 'SYSTEM_DESIGN',
  tags: '',
  contentMarkdown: '',
  diagramMermaid: '',
});

function openSaveToNoteModal(msg: ItChatMessage) {
  quickNoteForm.title = currentSession.value?.title || 'Kiến trúc IT';
  quickNoteForm.category = currentSession.value?.topicCategory || 'SYSTEM_DESIGN';
  quickNoteForm.tags = 'ai-architect, notes';
  quickNoteForm.contentMarkdown = msg.content;
  quickNoteForm.diagramMermaid = msg.mermaidDiagram || '';
  showSaveNoteModal.value = true;
}

async function saveQuickNote() {
  if (!quickNoteForm.title.trim() || !quickNoteForm.contentMarkdown.trim()) {
    toastStore.error('Tiêu đề và nội dung không được để trống');
    return;
  }
  savingNote.value = true;
  try {
    await itStudioService.createNote(quickNoteForm);
    toastStore.success('Đã lưu bài học vào Sổ tay Kiến thức!');
    showSaveNoteModal.value = false;
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể lưu ghi chú');
  } finally {
    savingNote.value = false;
  }
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
