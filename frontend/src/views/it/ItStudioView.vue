<template>
  <div class="space-y-6 max-w-7xl mx-auto pb-16">
    <!-- Header Hero Banner -->
    <div class="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 rounded-2xl p-6 sm:p-8 text-white relative overflow-hidden border border-slate-800 shadow-sm">
      <div class="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:20px_20px]"></div>
      
      <div class="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div class="space-y-2">
          <div class="flex items-center gap-2 flex-wrap">
            <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">
              Principal Architect AI
            </span>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-purple-500/20 text-purple-300 border border-purple-500/30">
              Kafka & Microservices
            </span>
            <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-teal-500/20 text-teal-300 border border-teal-500/30">
              SQL & Database Engine
            </span>
          </div>
          <h1 class="text-2xl sm:text-3xl font-extrabold tracking-tight">
            IT Studio & System Architecture Hub
          </h1>
          <p class="text-xs sm:text-sm text-slate-300 max-w-3xl leading-relaxed">
            Học tập và giải quyết chuyên sâu các bài toán Kiến trúc hệ thống phân tán, Microservices, Kafka Event-Driven, Tối ưu SQL, và Trình thực hành Code trực tiếp.
          </p>
        </div>

        <!-- Master Tabs -->
        <div class="flex items-center bg-slate-800/90 p-1.5 rounded-xl border border-slate-700/80 shrink-0 self-start md:self-auto gap-1">
          <button
            type="button"
            class="px-4 py-2 text-xs font-bold rounded-lg transition-all flex items-center gap-2"
            :class="activeMasterTab === 'chat' ? 'bg-indigo-600 text-white shadow-xs' : 'text-slate-300 hover:text-white hover:bg-slate-700/60'"
            @click="activeMasterTab = 'chat'"
          >
            <Bot class="w-4 h-4" />
            <span>AI Architect Chat</span>
          </button>

          <button
            type="button"
            class="px-4 py-2 text-xs font-bold rounded-lg transition-all flex items-center gap-2"
            :class="activeMasterTab === 'playground' ? 'bg-indigo-600 text-white shadow-xs' : 'text-slate-300 hover:text-white hover:bg-slate-700/60'"
            @click="activeMasterTab = 'playground'"
          >
            <Terminal class="w-4 h-4" />
            <span>Playground & SQL</span>
          </button>

          <button
            type="button"
            class="px-4 py-2 text-xs font-bold rounded-lg transition-all flex items-center gap-2"
            :class="activeMasterTab === 'notes' ? 'bg-indigo-600 text-white shadow-xs' : 'text-slate-300 hover:text-white hover:bg-slate-700/60'"
            @click="activeMasterTab = 'notes'"
          >
            <BookMarked class="w-4 h-4" />
            <span>Sổ tay Kiến trúc</span>
          </button>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- TAB 1: AI SYSTEM ARCHITECT & CODING MENTOR (CHAT)                         -->
    <!-- ========================================================================= -->
    <div v-if="activeMasterTab === 'chat'" class="grid grid-cols-1 lg:grid-cols-4 gap-6 min-h-[680px]">
      <!-- Chat Sessions Sidebar -->
      <div class="lg:col-span-1 bg-white rounded-2xl border border-slate-200 p-4 flex flex-col justify-between shadow-xs space-y-4">
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
              <MessagesSquare class="w-4 h-4 text-indigo-600" />
              <span>Chủ đề thảo luận</span>
            </h3>
            <button
              type="button"
              class="p-1.5 text-xs font-semibold text-indigo-600 hover:bg-indigo-50 rounded-md transition-colors flex items-center gap-1"
              @click="startNewSession"
            >
              <Plus class="w-4 h-4" />
              <span>Tạo mới</span>
            </button>
          </div>

          <!-- Session List -->
          <div v-if="loadingSessions" class="text-center py-8 text-xs text-slate-400">
            Đang tải danh sách...
          </div>
          <div v-else-if="sessions.length === 0" class="text-center py-8 text-xs text-slate-400 space-y-2">
            <Bot class="w-8 h-8 text-slate-300 mx-auto" />
            <p>Chưa có cuộc trò chuyện nào. Hãy bắt đầu hỏi AI Architect ngay!</p>
          </div>
          <div v-else class="space-y-1.5 max-h-[500px] overflow-y-auto pr-1">
            <div
              v-for="s in sessions"
              :key="s.id"
              class="p-2.5 rounded-xl text-xs transition-all flex items-center justify-between group cursor-pointer border"
              :class="currentSession?.id === s.id ? 'bg-indigo-50 border-indigo-200 text-indigo-950 font-bold' : 'bg-slate-50/60 border-slate-100 text-slate-700 hover:bg-slate-100/80'"
              @click="selectSession(s)"
            >
              <div class="flex flex-col truncate pr-2">
                <span class="truncate">{{ s.title }}</span>
                <span class="text-[10px] text-slate-400 font-normal mt-0.5">
                  {{ formatSessionCategory(s.topicCategory) }}
                </span>
              </div>
              <button
                type="button"
                title="Xóa cuộc trò chuyện này"
                class="opacity-0 group-hover:opacity-100 p-1 text-slate-400 hover:text-rose-600 rounded transition-opacity"
                @click.stop="confirmDeleteSession(s.id)"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>

        <!-- System Prompt Guide Card -->
        <div class="p-3 bg-indigo-50/50 rounded-xl border border-indigo-100 text-[11px] text-indigo-900 space-y-1">
          <div class="font-bold flex items-center gap-1">
            <Sparkles class="w-3.5 h-3.5 text-indigo-600" />
            <span>AI Mentor Chuyên sâu:</span>
          </div>
          <p class="text-indigo-700 leading-relaxed">
            Hỗ trợ giải đáp Microservices, Kafka, Tối ưu SQL, Caching Redis, Bẫy Deadlock, và tự động vẽ Sơ đồ kiến trúc Mermaid.
          </p>
        </div>
      </div>

      <!-- Main Chat Messages & Input Panel -->
      <div class="lg:col-span-3 bg-white rounded-2xl border border-slate-200 flex flex-col justify-between shadow-xs overflow-hidden">
        <!-- Chat Header -->
        <div class="p-4 border-b border-slate-100 bg-slate-50/60 flex items-center justify-between flex-wrap gap-2">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center font-bold">
              <Cpu class="w-4 h-4" />
            </div>
            <div>
              <h2 class="text-sm font-bold text-slate-900">
                {{ currentSession?.title || 'Phiên thảo luận Kiến trúc mới' }}
              </h2>
              <span class="text-[11px] text-slate-500">
                Chủ đề: <strong class="text-indigo-600">{{ formatSessionCategory(currentSession?.topicCategory || selectedCategory) }}</strong>
              </span>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <button
              v-if="currentSession && currentSession.messages.length > 0"
              type="button"
              class="px-2.5 py-1 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-md transition-colors border border-brand-200 flex items-center gap-1"
              @click="saveCurrentChatAsNote"
            >
              <Save class="w-3.5 h-3.5" />
              <span>Lưu thành Ghi chú IT</span>
            </button>
          </div>
        </div>

        <!-- Chat Message Scroll Area -->
        <div class="p-4 sm:p-6 space-y-5 flex-1 overflow-y-auto max-h-[520px] bg-slate-50/30" ref="chatScrollRef">
          <!-- Empty Welcome State -->
          <div v-if="!currentSession || currentSession.messages.length === 0" class="text-center py-12 space-y-4 max-w-xl mx-auto">
            <div class="w-14 h-14 rounded-2xl bg-indigo-100 text-indigo-600 flex items-center justify-center mx-auto shadow-xs">
              <Bot class="w-7 h-7" />
            </div>
            <div class="space-y-1">
              <h3 class="text-base font-bold text-slate-900">Xin chào! Tôi là AI Principal Architect</h3>
              <p class="text-xs text-slate-500 leading-relaxed">
                Bạn có thể hỏi tôi bất kỳ bài toán nào về Kiến trúc hệ thống, Kafka, Tối ưu SQL, Spring Boot, Microservices, hoặc gửi code/ảnh chụp để tôi debug và phân tích!
              </p>
            </div>

            <!-- Quick Question Suggestions -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 text-left pt-2">
              <button
                v-for="(sug, idx) in quickSuggestions"
                :key="idx"
                type="button"
                class="p-2.5 text-xs bg-white border border-slate-200 hover:border-indigo-300 hover:bg-indigo-50/40 rounded-xl transition-all text-slate-700 text-left font-medium"
                @click="applySuggestion(sug)"
              >
                {{ sug }}
              </button>
            </div>
          </div>

          <!-- Message Bubbles -->
          <div v-else class="space-y-5">
            <div
              v-for="msg in currentSession.messages"
              :key="msg.id"
              class="flex flex-col gap-1.5"
              :class="msg.senderRole === 'USER' ? 'items-end' : 'items-start'"
            >
              <!-- Sender Header -->
              <div class="flex items-center gap-1.5 text-[11px] text-slate-400">
                <span v-if="msg.senderRole === 'USER'" class="font-bold text-slate-700">Bạn</span>
                <span v-else class="font-bold text-indigo-600 flex items-center gap-1">
                  <Sparkles class="w-3 h-3 text-indigo-500" />
                  AI Architect Mentor
                </span>
                <span>• {{ formatMsgTime(msg.createdAt) }}</span>
              </div>

              <!-- Message Content Body -->
              <div
                class="p-4 rounded-2xl text-xs sm:text-sm max-w-full sm:max-w-[88%] leading-relaxed shadow-2xs space-y-3"
                :class="msg.senderRole === 'USER' ? 'bg-indigo-600 text-white rounded-tr-xs' : 'bg-white text-slate-800 border border-slate-200 rounded-tl-xs'"
              >
                <!-- Markdown Content Render -->
                <div class="prose prose-sm max-w-none text-inherit leading-relaxed whitespace-pre-line">
                  {{ msg.content }}
                </div>

                <!-- Attached Code Snippet -->
                <div v-if="msg.codeSnippet" class="bg-slate-900 text-slate-100 rounded-xl p-3.5 font-mono text-xs overflow-x-auto space-y-2 border border-slate-800">
                  <div class="flex items-center justify-between text-slate-400 text-[11px] border-b border-slate-800 pb-1.5">
                    <span class="font-bold uppercase">{{ msg.language || 'Code' }}</span>
                    <button
                      type="button"
                      class="hover:text-white transition-colors flex items-center gap-1"
                      @click="copyToClipboard(msg.codeSnippet)"
                    >
                      <Copy class="w-3.5 h-3.5" />
                      <span>Copy Code</span>
                    </button>
                  </div>
                  <pre class="whitespace-pre overflow-x-auto"><code>{{ msg.codeSnippet }}</code></pre>
                </div>

                <!-- Mermaid Diagram Section -->
                <div v-if="msg.mermaidDiagram" class="bg-slate-900 text-indigo-300 rounded-xl p-3.5 font-mono text-xs space-y-2 border border-indigo-900/50">
                  <div class="flex items-center justify-between text-indigo-400 text-[11px] border-b border-slate-800 pb-1.5">
                    <span class="font-bold flex items-center gap-1">
                      <Network class="w-3.5 h-3.5" />
                      Sơ đồ Kiến trúc / Luồng dữ liệu (Mermaid)
                    </span>
                    <button
                      type="button"
                      class="hover:text-white transition-colors flex items-center gap-1"
                      @click="copyToClipboard(msg.mermaidDiagram)"
                    >
                      <Copy class="w-3.5 h-3.5" />
                      <span>Copy Mermaid</span>
                    </button>
                  </div>
                  <pre class="whitespace-pre overflow-x-auto text-emerald-400"><code>{{ msg.mermaidDiagram }}</code></pre>
                </div>
              </div>
            </div>

            <!-- Loading Generating Indicator -->
            <div v-if="sendingMessage" class="flex items-center gap-2 p-3.5 bg-white border border-slate-200 rounded-2xl max-w-sm text-xs text-indigo-700 font-medium animate-pulse shadow-2xs">
              <Bot class="w-4 h-4 animate-spin" />
              <span>AI Architect đang phân tích giải pháp & vẽ sơ đồ...</span>
            </div>
          </div>
        </div>

        <!-- Chat Input Form & Toolbars -->
        <div class="p-4 border-t border-slate-200 bg-white space-y-3">
          <!-- Code snippet attachment drawer -->
          <div v-if="showCodeInput" class="p-3 bg-slate-900 rounded-xl border border-slate-800 space-y-2 text-white">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-slate-300 flex items-center gap-1.5">
                <Code class="w-3.5 h-3.5 text-indigo-400" />
                Đính kèm đoạn code / SQL cần phân tích
              </span>
              <div class="flex items-center gap-2">
                <select
                  v-model="inputLanguage"
                  class="text-xs bg-slate-800 border border-slate-700 text-slate-200 rounded px-2 py-1 focus:outline-none"
                >
                  <option value="JAVA">Java (Spring Boot)</option>
                  <option value="SQL">SQL (Query / DDL)</option>
                  <option value="JAVASCRIPT">JavaScript / TypeScript</option>
                  <option value="PYTHON">Python</option>
                  <option value="YAML">Docker / K8s YAML</option>
                </select>
                <button
                  type="button"
                  class="text-xs text-slate-400 hover:text-rose-400"
                  @click="showCodeInput = false; inputCodeSnippet = ''"
                >
                  Đóng
                </button>
              </div>
            </div>
            <textarea
              v-model="inputCodeSnippet"
              rows="4"
              class="w-full bg-slate-950 text-slate-100 font-mono text-xs p-2.5 rounded-lg border border-slate-800 focus:outline-none focus:border-indigo-500"
              placeholder="// Dán đoạn code Java, SQL hoặc cấu hình tại đây..."
            ></textarea>
          </div>

          <!-- Category and Mode Toolbar -->
          <div class="flex items-center justify-between gap-2 flex-wrap text-xs">
            <div class="flex items-center gap-1.5">
              <span class="text-slate-500 font-medium">Chủ đề:</span>
              <select
                v-model="selectedCategory"
                class="bg-slate-50 border border-slate-200 text-slate-700 rounded-md px-2.5 py-1 text-xs font-semibold focus:outline-none focus:border-indigo-500"
              >
                <option value="SYSTEM_DESIGN">Kiến trúc Hệ thống (System Design)</option>
                <option value="KAFKA_MESSAGE_QUEUE">Kafka & Message Queues</option>
                <option value="DATABASE_SQL">Tối ưu SQL & Database Engine</option>
                <option value="HIGH_CONCURRENCY">High Concurrency & Redis Caching</option>
                <option value="BACKEND_JAVA">Java Spring Boot & Backend</option>
                <option value="FRONTEND">Frontend & Fullstack</option>
                <option value="DEVOPS_CLOUD">DevOps & Cloud (Docker, K8s)</option>
              </select>
            </div>

            <div class="flex items-center gap-2">
              <button
                type="button"
                class="px-2.5 py-1 text-xs font-semibold rounded-md border transition-colors flex items-center gap-1"
                :class="showCodeInput ? 'bg-indigo-50 border-indigo-300 text-indigo-700' : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'"
                @click="showCodeInput = !showCodeInput"
              >
                <Code class="w-3.5 h-3.5" />
                <span>+ Đính kèm Code / SQL</span>
              </button>
            </div>
          </div>

          <!-- Text input row -->
          <form class="flex items-end gap-2" @submit.prevent="handleSendMessage">
            <textarea
              v-model="inputPrompt"
              rows="2"
              class="flex-1 text-xs sm:text-sm bg-slate-50 border border-slate-300 rounded-xl p-3 shadow-xs focus:outline-none focus:border-indigo-500 focus:bg-white resize-none"
              placeholder="Nhập câu hỏi kỹ thuật, kiến trúc, kafka, tối ưu SQL... (Nhấn Enter để gửi)"
              @keydown.enter.exact.prevent="handleSendMessage"
            ></textarea>

            <AppButton
              type="submit"
              variant="primary"
              size="md"
              class="px-5 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white shadow-xs"
              :disabled="!inputPrompt.trim() || sendingMessage"
              :loading="sendingMessage"
              :icon="Send"
            >
              Gửi
            </AppButton>
          </form>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- TAB 2: CODE & SQL PLAYGROUND & LIVE EXECUTOR                              -->
    <!-- ========================================================================= -->
    <div v-if="activeMasterTab === 'playground'" class="space-y-4">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 min-h-[600px]">
        <!-- Left Pane: Code Editor -->
        <div class="bg-slate-900 rounded-2xl border border-slate-800 p-4 flex flex-col justify-between shadow-xs text-white space-y-3">
          <!-- Toolbar -->
          <div class="flex items-center justify-between border-b border-slate-800 pb-3 flex-wrap gap-2">
            <div class="flex items-center gap-2">
              <Terminal class="w-4 h-4 text-indigo-400" />
              <select
                v-model="playgroundLang"
                class="text-xs bg-slate-800 border border-slate-700 text-slate-200 rounded-md px-2.5 py-1.5 font-bold focus:outline-none"
                @change="loadLanguageTemplate"
              >
                <option value="SQL">SQL Studio (Query & Data Table)</option>
                <option value="JAVASCRIPT">JavaScript / Node.js</option>
                <option value="TYPESCRIPT">TypeScript</option>
                <option value="JAVA">Java 17 / Spring Boot</option>
                <option value="PYTHON">Python 3</option>
              </select>
            </div>

            <div class="flex items-center gap-2">
              <button
                type="button"
                class="px-3 py-1.5 text-xs font-semibold bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-md transition-colors"
                @click="loadLanguageTemplate"
              >
                Mẫu code
              </button>
              <button
                type="button"
                class="px-3 py-1.5 text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white rounded-md transition-colors flex items-center gap-1 shadow-xs"
                :disabled="executingPlayground"
                @click="runPlayground('RUN')"
              >
                <Play class="w-3.5 h-3.5 fill-current" />
                <span>Chạy Code</span>
              </button>
            </div>
          </div>

          <!-- Code Textarea -->
          <div class="flex-1 flex flex-col">
            <textarea
              v-model="playgroundCode"
              class="w-full flex-1 bg-slate-950 text-slate-100 font-mono text-xs sm:text-sm p-4 rounded-xl border border-slate-800 focus:outline-none focus:border-indigo-500 leading-relaxed resize-none min-h-[420px]"
              placeholder="Nhập code hoặc câu lệnh SQL tại đây..."
            ></textarea>
          </div>

          <!-- AI Action Buttons Footer -->
          <div class="flex items-center justify-between pt-2 border-t border-slate-800 flex-wrap gap-2 text-xs">
            <div class="flex items-center gap-2">
              <button
                type="button"
                class="px-3 py-1.5 rounded-md bg-purple-950/70 text-purple-300 hover:bg-purple-900 border border-purple-800/60 font-semibold transition-colors flex items-center gap-1"
                :disabled="executingPlayground"
                @click="runPlayground('DEBUG_AI')"
              >
                <Bug class="w-3.5 h-3.5" />
                <span>AI Tìm & Sửa lỗi</span>
              </button>

              <button
                type="button"
                class="px-3 py-1.5 rounded-md bg-teal-950/70 text-teal-300 hover:bg-teal-900 border border-teal-800/60 font-semibold transition-colors flex items-center gap-1"
                :disabled="executingPlayground"
                @click="runPlayground('OPTIMIZE_AI')"
              >
                <Zap class="w-3.5 h-3.5" />
                <span>AI Tối ưu hiệu năng</span>
              </button>
            </div>

            <button
              type="button"
              class="px-3 py-1.5 text-slate-400 hover:text-white transition-colors flex items-center gap-1"
              @click="openSaveSnippetModal"
            >
              <Save class="w-3.5 h-3.5" />
              <span>Lưu vào Snippet</span>
            </button>
          </div>
        </div>

        <!-- Right Pane: Terminal Output & SQL Result Table -->
        <div class="bg-white rounded-2xl border border-slate-200 p-5 flex flex-col justify-between shadow-xs space-y-4">
          <div class="space-y-3 flex-1 flex flex-col">
            <div class="flex items-center justify-between border-b border-slate-100 pb-2.5">
              <h3 class="text-xs font-bold uppercase tracking-wider text-slate-600 flex items-center gap-1.5">
                <Terminal class="w-4 h-4 text-emerald-600" />
                <span>Kết quả thực thi (Console Output)</span>
              </h3>
              <span v-if="playgroundResult?.executionTimeMs !== undefined" class="text-[11px] text-slate-400 font-mono">
                Thời gian: {{ playgroundResult.executionTimeMs }}ms
              </span>
            </div>

            <!-- SQL Table Output -->
            <div v-if="playgroundResult?.sqlResultTable && playgroundResult.sqlResultTable.length > 0" class="space-y-2">
              <div class="text-xs font-bold text-slate-700 flex items-center gap-1">
                <Database class="w-3.5 h-3.5 text-indigo-600" />
                <span>Bảng dữ liệu kết quả ({{ playgroundResult.sqlResultTable.length }} bản ghi):</span>
              </div>
              <div class="border border-slate-200 rounded-xl overflow-x-auto max-h-60">
                <table class="w-full text-left text-xs border-collapse font-mono">
                  <thead class="bg-slate-50 border-b border-slate-200 text-slate-600">
                    <tr>
                      <th v-for="col in playgroundResult.sqlColumns" :key="col" class="px-3 py-2 font-bold uppercase">
                        {{ col }}
                      </th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    <tr v-for="(row, rIdx) in playgroundResult.sqlResultTable" :key="rIdx" class="hover:bg-slate-50">
                      <td v-for="col in playgroundResult.sqlColumns" :key="col" class="px-3 py-2 text-slate-800">
                        {{ row[col] }}
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Console Log Window -->
            <div class="flex-1 bg-slate-950 text-emerald-400 font-mono text-xs p-4 rounded-xl border border-slate-900 overflow-y-auto max-h-64 whitespace-pre-wrap">
              {{ playgroundResult?.stdout || 'Chưa có kết quả chạy. Nhấn "Chạy Code" hoặc nhờ AI phân tích.' }}
            </div>

            <!-- AI In-Depth Analysis Box -->
            <div v-if="playgroundResult?.aiAnalysis" class="p-4 bg-indigo-50/70 border border-indigo-200 rounded-xl text-xs text-indigo-950 space-y-2 overflow-y-auto max-h-56">
              <div class="font-bold flex items-center gap-1.5 text-indigo-800">
                <Sparkles class="w-4 h-4 text-indigo-600" />
                <span>Phân tích từ AI Architect:</span>
              </div>
              <div class="whitespace-pre-line leading-relaxed text-slate-800">
                {{ playgroundResult.aiAnalysis }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- TAB 3: IT KNOWLEDGE BASE & NOTEBOOK (SỔ TAY KIẾN TRÚC)                   -->
    <!-- ========================================================================= -->
    <div v-if="activeMasterTab === 'notes'" class="space-y-4">
      <!-- Search & Category Filters -->
      <div class="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex flex-col md:flex-row items-center justify-between gap-3">
        <div class="flex items-center gap-2 w-full md:w-auto flex-1">
          <div class="relative flex-1 max-w-md">
            <input
              v-model="notesSearch"
              type="text"
              class="w-full text-xs bg-slate-50 border border-slate-300 rounded-xl pl-9 pr-3.5 py-2.5 focus:outline-none focus:border-indigo-500 focus:bg-white"
              placeholder="Tìm theo tiêu đề ghi chú, Kafka, SQL, Microservices, tag..."
              @input="loadNotes"
            />
            <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          </div>

          <select
            v-model="notesCategory"
            class="text-xs bg-slate-50 border border-slate-300 text-slate-700 rounded-xl px-3 py-2.5 font-medium focus:outline-none focus:border-indigo-500"
            @change="loadNotes"
          >
            <option value="">Tất cả danh mục</option>
            <option value="SYSTEM_DESIGN">System Design & Microservices</option>
            <option value="KAFKA_MESSAGE_QUEUE">Kafka & Message Queues</option>
            <option value="DATABASE_SQL">Tối ưu SQL & Database Engine</option>
            <option value="HIGH_CONCURRENCY">High Concurrency & Redis</option>
            <option value="BACKEND_JAVA">Java Spring Boot</option>
            <option value="DEVOPS_CLOUD">DevOps & Cloud</option>
          </select>
        </div>

        <AppButton
          variant="primary"
          size="md"
          :icon="Plus"
          class="bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl shadow-xs shrink-0"
          @click="openNewNoteModal"
        >
          Tạo ghi chú mới
        </AppButton>
      </div>

      <!-- Notes Grid -->
      <div v-if="loadingNotes" class="bg-white p-12 rounded-2xl border border-slate-200 text-center text-xs text-slate-400">
        Đang tải sổ tay ghi chú...
      </div>
      <div v-else-if="notes.length === 0" class="bg-white p-12 rounded-2xl border border-slate-200 text-center space-y-3">
        <BookOpen class="w-10 h-10 text-slate-300 mx-auto" />
        <h4 class="text-sm font-bold text-slate-800">Chưa có ghi chú nào</h4>
        <p class="text-xs text-slate-500">Hãy thêm ghi chú kiến trúc hoặc lưu từ cuộc trò chuyện với AI.</p>
        <AppButton variant="primary" size="sm" :icon="Plus" @click="openNewNoteModal">
          Tạo ghi chú đầu tiên
        </AppButton>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="n in notes"
          :key="n.id"
          class="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs hover:border-indigo-300 transition-all flex flex-col justify-between group cursor-pointer space-y-3"
          @click="openEditNoteModal(n)"
        >
          <div class="space-y-2">
            <div class="flex items-start justify-between gap-2">
              <span class="px-2 py-0.5 rounded-md text-[10px] font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">
                {{ formatSessionCategory(n.category) }}
              </span>
              <button
                type="button"
                title="Đánh dấu yêu thích"
                class="text-slate-300 hover:text-amber-500 transition-colors"
                :class="n.isFavorite ? 'text-amber-500' : ''"
                @click.stop="toggleNoteFavorite(n)"
              >
                <Bookmark class="w-4 h-4 fill-current" />
              </button>
            </div>

            <h3 class="font-bold text-slate-900 group-hover:text-indigo-600 transition-colors text-base line-clamp-1">
              {{ n.title }}
            </h3>

            <p class="text-xs text-slate-600 line-clamp-3 leading-relaxed">
              {{ n.contentMarkdown }}
            </p>

            <div v-if="n.diagramMermaid" class="p-2 bg-slate-900 text-indigo-300 rounded-lg text-[10px] font-mono line-clamp-1 border border-slate-800">
              [Sơ đồ Mermaid]: {{ n.diagramMermaid.substring(0, 45) }}...
            </div>
          </div>

          <div class="flex items-center justify-between pt-3 border-t border-slate-100 text-[11px] text-slate-400" @click.stop>
            <span>Cập nhật: {{ formatNoteDate(n.updatedAt) }}</span>
            <div class="flex items-center gap-1">
              <button
                type="button"
                class="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded"
                @click="openEditNoteModal(n)"
              >
                <Edit class="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                class="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded"
                @click="confirmDeleteNote(n.id)"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================================= -->
    <!-- MODAL: TẠO / CHỈNH SỬA GHI CHÚ IT                                         -->
    <!-- ========================================================================= -->
    <AppModal
      v-model="showNoteModal"
      :title="editingNoteId ? 'Chỉnh sửa Ghi chú IT' : 'Tạo Ghi chú Kiến trúc IT mới'"
      size="lg"
    >
      <form class="space-y-4" @submit.prevent="saveNote">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <AppInput
            v-model="noteForm.title"
            label="Tiêu đề ghi chú"
            placeholder="e.g. Bản chất Index B-Tree & Nguyên tắc Leftmost Prefix"
            required
          />

          <div>
            <label class="block text-xs font-semibold text-slate-700 mb-1.5">Danh mục</label>
            <select
              v-model="noteForm.category"
              class="w-full text-xs bg-white border border-slate-300 rounded-md px-3.5 py-2.5 shadow-xs focus:outline-none focus:border-indigo-500 font-medium"
            >
              <option value="SYSTEM_DESIGN">System Design & Microservices</option>
              <option value="KAFKA_MESSAGE_QUEUE">Kafka & Message Queues</option>
              <option value="DATABASE_SQL">Tối ưu SQL & Database Engine</option>
              <option value="HIGH_CONCURRENCY">High Concurrency & Redis</option>
              <option value="BACKEND_JAVA">Java Spring Boot</option>
              <option value="FRONTEND">Frontend & Fullstack</option>
              <option value="DEVOPS_CLOUD">DevOps & Cloud</option>
            </select>
          </div>
        </div>

        <AppInput
          v-model="noteForm.tags"
          label="Thẻ tag (phân cách bằng dấu phẩy)"
          placeholder="kafka, idempotency, dlq, microservices"
        />

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">
            Nội dung chi tiết (Hỗ trợ Markdown)
          </label>
          <textarea
            v-model="noteForm.contentMarkdown"
            rows="8"
            required
            class="w-full text-xs font-mono bg-white border border-slate-300 rounded-md p-3 shadow-xs focus:outline-none focus:border-indigo-500"
            placeholder="# Tiêu đề lớn&#10;&#10;**1. Bản chất vấn đề:**&#10;- Ý chính 1...&#10;- Ý chính 2...&#10;&#10;```java&#10;// Code mẫu&#10;```"
          ></textarea>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">
            Mã sơ đồ Mermaid (Tùy chọn)
          </label>
          <textarea
            v-model="noteForm.diagramMermaid"
            rows="4"
            class="w-full text-xs font-mono bg-slate-950 text-emerald-400 border border-slate-800 rounded-md p-3 focus:outline-none"
            placeholder="sequenceDiagram&#10;    Client->>Kafka: Send Event&#10;    Kafka-->>Worker: Consume Event"
          ></textarea>
        </div>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
          <AppButton variant="secondary" size="md" @click="showNoteModal = false">
            Hủy
          </AppButton>
          <AppButton
            type="submit"
            variant="primary"
            size="md"
            class="bg-indigo-600 hover:bg-indigo-700 text-white"
            :loading="savingNote"
          >
            Lưu ghi chú
          </AppButton>
        </div>
      </form>
    </AppModal>

    <!-- MODAL: LƯU CODE SNIPPET -->
    <AppModal
      v-model="showSnippetModal"
      title="Lưu Code / SQL Snippet vào Kho"
      size="md"
    >
      <form class="space-y-4" @submit.prevent="saveSnippet">
        <AppInput
          v-model="snippetForm.title"
          label="Tiêu đề Snippet"
          placeholder="e.g. Truy vấn CTE đệ quy trong SQL"
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
              <option value="TYPESCRIPT">TypeScript</option>
              <option value="PYTHON">Python</option>
              <option value="YAML">YAML</option>
            </select>
          </div>
          <AppInput
            v-model="snippetForm.tags"
            label="Tags"
            placeholder="sql, cte, optimize"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-700 mb-1.5">Mã code</label>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue';
import { itStudioService } from '../../services/it-studio.service';
import { useToastStore } from '../../stores/toast.store';
import type { ItNote, ItChatSession, ItPlaygroundExecutionResponse } from '../../types';
import AppInput from '../../components/common/AppInput.vue';
import AppButton from '../../components/common/AppButton.vue';
import AppModal from '../../components/common/AppModal.vue';
import {
  Bot,
  Terminal,
  BookMarked,
  MessagesSquare,
  Plus,
  Trash2,
  Sparkles,
  Cpu,
  Save,
  Copy,
  Network,
  Code,
  Send,
  Play,
  Bug,
  Zap,
  Database,
  Search,
  BookOpen,
  Bookmark,
  Edit,
} from 'lucide-vue-next';

const toastStore = useToastStore();

const activeMasterTab = ref<'chat' | 'playground' | 'notes'>('chat');

// -------------------------------------------------------------
// CHAT STATE
// -------------------------------------------------------------
const sessions = ref<ItChatSession[]>([]);
const currentSession = ref<ItChatSession | null>(null);
const loadingSessions = ref(false);
const sendingMessage = ref(false);
const inputPrompt = ref('');
const inputCodeSnippet = ref('');
const inputLanguage = ref('JAVA');
const selectedCategory = ref('SYSTEM_DESIGN');
const showCodeInput = ref(false);
const chatScrollRef = ref<HTMLDivElement | null>(null);

const quickSuggestions = [
  'Thiết kế luồng Order qua Kafka (Saga Pattern & Outbox)',
  'Tối ưu Index B-Tree trong SQL khi có 10 triệu bản ghi',
  'Xử lý Cache Avalanche và Cache Penetration với Redis',
  'Bản chất 4 mức Transaction Isolation Level và Deadlock',
];

// -------------------------------------------------------------
// PLAYGROUND STATE
// -------------------------------------------------------------
const playgroundLang = ref('SQL');
const playgroundCode = ref(`-- Demo SQL Index Optimization & Query
SELECT u.id, u.display_name, COUNT(o.id) AS total_orders
FROM users u
INNER JOIN orders o ON u.id = o.user_id
WHERE u.is_active = 1
  AND o.created_at >= '2026-01-01'
GROUP BY u.id, u.display_name
HAVING COUNT(o.id) > 5
ORDER BY total_orders DESC;`);

const executingPlayground = ref(false);
const playgroundResult = ref<ItPlaygroundExecutionResponse | null>(null);
const showSnippetModal = ref(false);
const savingSnippet = ref(false);
const snippetForm = reactive({
  title: '',
  language: 'SQL',
  codeContent: '',
  tags: '',
});

// -------------------------------------------------------------
// NOTES STATE
// -------------------------------------------------------------
const notes = ref<ItNote[]>([]);
const loadingNotes = ref(false);
const notesSearch = ref('');
const notesCategory = ref('');
const showNoteModal = ref(false);
const savingNote = ref(false);
const editingNoteId = ref<number | null>(null);

const noteForm = reactive({
  title: '',
  category: 'SYSTEM_DESIGN',
  tags: '',
  contentMarkdown: '',
  diagramMermaid: '',
});

onMounted(async () => {
  await Promise.all([
    loadChatSessions(),
    loadNotes(),
  ]);
});

// -------------------------------------------------------------
// CHAT METHODS
// -------------------------------------------------------------
async function loadChatSessions() {
  loadingSessions.value = true;
  try {
    sessions.value = await itStudioService.getSessions();
    if (sessions.value.length > 0 && !currentSession.value) {
      currentSession.value = sessions.value[0];
    }
  } catch (err) {
    console.warn('Failed to load chat sessions:', err);
  } finally {
    loadingSessions.value = false;
  }
}

function startNewSession() {
  currentSession.value = null;
  inputPrompt.value = '';
  inputCodeSnippet.value = '';
  showCodeInput.value = false;
}

function selectSession(session: ItChatSession) {
  currentSession.value = session;
  scrollToBottom();
}

function applySuggestion(sug: string) {
  inputPrompt.value = sug;
  handleSendMessage();
}

async function handleSendMessage() {
  if (!inputPrompt.value.trim() || sendingMessage.value) return;

  const promptText = inputPrompt.value.trim();
  const codeText = inputCodeSnippet.value.trim() || undefined;
  const langText = inputLanguage.value || undefined;
  const catText = selectedCategory.value;

  inputPrompt.value = '';
  inputCodeSnippet.value = '';
  showCodeInput.value = false;
  sendingMessage.value = true;

  try {
    const updated = await itStudioService.sendMessage({
      sessionId: currentSession.value?.id,
      topicCategory: catText,
      prompt: promptText,
      codeSnippet: codeText,
      language: langText,
    });

    currentSession.value = updated;
    await loadChatSessions();
    scrollToBottom();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể kết nối tới AI Architect');
  } finally {
    sendingMessage.value = false;
  }
}

async function confirmDeleteSession(sessionId: number) {
  try {
    await itStudioService.deleteSession(sessionId);
    toastStore.success('Đã xóa phiên thảo luận');
    if (currentSession.value?.id === sessionId) {
      currentSession.value = null;
    }
    await loadChatSessions();
  } catch (err) {
    toastStore.error('Không thể xóa phiên chat');
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatScrollRef.value) {
      chatScrollRef.value.scrollTop = chatScrollRef.value.scrollHeight;
    }
  });
}

function formatSessionCategory(cat?: string) {
  switch (cat) {
    case 'SYSTEM_DESIGN': return 'System Design & Microservices';
    case 'KAFKA_MESSAGE_QUEUE': return 'Kafka & Message Queues';
    case 'DATABASE_SQL': return 'SQL & Database Engine';
    case 'HIGH_CONCURRENCY': return 'High Concurrency & Redis';
    case 'BACKEND_JAVA': return 'Java Spring Boot';
    case 'FRONTEND': return 'Frontend & Fullstack';
    case 'DEVOPS_CLOUD': return 'DevOps & Cloud';
    default: return 'Kiến trúc IT';
  }
}

function formatMsgTime(dateStr?: string) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
}

function copyToClipboard(text?: string) {
  if (!text) return;
  navigator.clipboard.writeText(text);
  toastStore.success('Đã sao chép vào bộ nhớ tạm!');
}

function saveCurrentChatAsNote() {
  if (!currentSession.value || currentSession.value.messages.length === 0) return;
  const lastAiMsg = [...currentSession.value.messages].reverse().find(m => m.senderRole === 'ASSISTANT');
  
  noteForm.title = currentSession.value.title;
  noteForm.category = currentSession.value.topicCategory || 'SYSTEM_DESIGN';
  noteForm.tags = 'ai-architect, interview';
  noteForm.contentMarkdown = lastAiMsg?.content || currentSession.value.title;
  noteForm.diagramMermaid = lastAiMsg?.mermaidDiagram || '';
  editingNoteId.value = null;
  showNoteModal.value = true;
}

// -------------------------------------------------------------
// PLAYGROUND METHODS
// -------------------------------------------------------------
function loadLanguageTemplate() {
  if (playgroundLang.value === 'SQL') {
    playgroundCode.value = `-- Demo SQL Index Optimization & Query
SELECT u.id, u.display_name, COUNT(o.id) AS total_orders
FROM users u
INNER JOIN orders o ON u.id = o.user_id
WHERE u.is_active = 1
  AND o.created_at >= '2026-01-01'
GROUP BY u.id, u.display_name
HAVING COUNT(o.id) > 5
ORDER BY total_orders DESC;`;
  } else if (playgroundLang.value === 'JAVA') {
    playgroundCode.value = `// Demo Spring Boot Saga Pattern Event Handler
@Service
@RequiredArgsConstructor
public class OrderSagaCoordinator {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void processOrder(OrderRequest request) {
        // 1. Save Outbox Event
        OrderCreatedEvent event = new OrderCreatedEvent(request.getOrderId(), request.getAmount());
        kafkaTemplate.send("order-events", request.getOrderId().toString(), event);
        log.info("Dispatched order event to Kafka topic 'order-events'");
    }
}`;
  } else if (playgroundLang.value === 'JAVASCRIPT') {
    playgroundCode.value = `// Demo Async Queue Concurrency Worker
async function processBatch(items, concurrencyLimit = 3) {
  const results = [];
  const executing = [];
  for (const item of items) {
    const p = Promise.resolve().then(() => simulateTask(item));
    results.push(p);
    if (concurrencyLimit <= items.length) {
      const e = p.then(() => executing.splice(executing.indexOf(e), 1));
      executing.push(e);
      if (executing.length >= concurrencyLimit) {
        await Promise.race(executing);
      }
    }
  }
  return Promise.all(results);
}`;
  } else if (playgroundLang.value === 'PYTHON') {
    playgroundCode.value = `# Demo Redis Distributed Lock with Context Manager
import redis
import time

r = redis.Redis(host='localhost', port=6379, db=0)

def acquire_lock(lock_name, acquire_timeout=10, lock_timeout=10):
    identifier = str(time.time())
    end = time.time() + acquire_timeout
    while time.time() < end:
        if r.set(f"lock:{lock_name}", identifier, ex=lock_timeout, nx=True):
            return identifier
        time.sleep(0.001)
    return False`;
  }
}

async function runPlayground(action: string) {
  if (!playgroundCode.value.trim()) {
    toastStore.error('Vui lòng nhập code để chạy');
    return;
  }
  executingPlayground.value = true;
  try {
    const res = await itStudioService.executeOrAnalyze({
      language: playgroundLang.value,
      code: playgroundCode.value,
      action: action,
    });
    playgroundResult.value = res;
    toastStore.success('Thực thi hoàn tất!');
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể thực thi code');
  } finally {
    executingPlayground.value = false;
  }
}

function openSaveSnippetModal() {
  snippetForm.title = `Snippet: ${playgroundLang.value} - ${new Date().toLocaleTimeString()}`;
  snippetForm.language = playgroundLang.value;
  snippetForm.codeContent = playgroundCode.value;
  snippetForm.tags = playgroundLang.value.toLowerCase();
  showSnippetModal.value = true;
}

async function saveSnippet() {
  if (!snippetForm.title.trim() || !snippetForm.codeContent.trim()) {
    toastStore.error('Tiêu đề và nội dung code không được rỗng');
    return;
  }
  savingSnippet.value = true;
  try {
    await itStudioService.createSnippet(snippetForm);
    toastStore.success('Đã lưu code snippet thành công!');
    showSnippetModal.value = false;
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể lưu snippet');
  } finally {
    savingSnippet.value = false;
  }
}

// -------------------------------------------------------------
// NOTES METHODS
// -------------------------------------------------------------
async function loadNotes() {
  loadingNotes.value = true;
  try {
    const res = await itStudioService.getNotes({
      category: notesCategory.value || undefined,
      search: notesSearch.value.trim() || undefined,
      page: 0,
      size: 20,
    });
    notes.value = res.items;
  } catch (err) {
    console.warn('Failed to load notes:', err);
  } finally {
    loadingNotes.value = false;
  }
}

function openNewNoteModal() {
  editingNoteId.value = null;
  noteForm.title = '';
  noteForm.category = 'SYSTEM_DESIGN';
  noteForm.tags = '';
  noteForm.contentMarkdown = '';
  noteForm.diagramMermaid = '';
  showNoteModal.value = true;
}

function openEditNoteModal(note: ItNote) {
  editingNoteId.value = note.id;
  noteForm.title = note.title;
  noteForm.category = note.category;
  noteForm.tags = note.tags || '';
  noteForm.contentMarkdown = note.contentMarkdown;
  noteForm.diagramMermaid = note.diagramMermaid || '';
  showNoteModal.value = true;
}

async function saveNote() {
  if (!noteForm.title.trim() || !noteForm.contentMarkdown.trim()) {
    toastStore.error('Tiêu đề và nội dung ghi chú không được để trống');
    return;
  }
  savingNote.value = true;
  try {
    if (editingNoteId.value) {
      await itStudioService.updateNote(editingNoteId.value, noteForm);
      toastStore.success('Cập nhật ghi chú thành công!');
    } else {
      await itStudioService.createNote(noteForm);
      toastStore.success('Tạo ghi chú IT thành công!');
    }
    showNoteModal.value = false;
    await loadNotes();
  } catch (err: any) {
    toastStore.error(err.response?.data?.message || 'Không thể lưu ghi chú');
  } finally {
    savingNote.value = false;
  }
}

async function toggleNoteFavorite(note: ItNote) {
  try {
    const updated = await itStudioService.toggleFavorite(note.id);
    note.isFavorite = updated.isFavorite;
  } catch (err) {
    toastStore.error('Không thể cập nhật yêu thích');
  }
}

async function confirmDeleteNote(id: number) {
  try {
    await itStudioService.deleteNote(id);
    toastStore.success('Đã xóa ghi chú');
    await loadNotes();
  } catch (err) {
    toastStore.error('Không thể xóa ghi chú');
  }
}

function formatNoteDate(dateStr?: string) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
}
</script>
