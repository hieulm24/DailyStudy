<template>
  <div class="min-h-screen bg-slate-50 flex">
    <!-- Sidebar Backdrop for mobile -->
    <div
      v-if="isSidebarOpen"
      class="fixed inset-0 z-30 bg-slate-900/40 backdrop-blur-xs lg:hidden"
      @click="isSidebarOpen = false"
    />

    <!-- Sidebar -->
    <AppSidebar :is-open="isSidebarOpen" @close="isSidebarOpen = false" />

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col min-w-0 lg:pl-72">
      <AppHeader @toggle-sidebar="isSidebarOpen = !isSidebarOpen" @quick-add="showQuickAddModal = true" />

      <main class="flex-1 p-4 sm:p-5 lg:p-6 w-full">
        <router-view />
      </main>
    </div>

    <!-- Quick Add Modal -->
    <AppModal v-model="showQuickAddModal" title="Thêm nội dung học tập mới" size="md">
      <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 py-2">
        <button
          type="button"
          class="flex flex-col items-center p-4 rounded-md border border-slate-200 hover:border-brand-500 hover:bg-brand-50/50 transition-all text-center group"
          @click="navigateTo('/vocabulary?action=add')"
        >
          <BookOpen class="w-6 h-6 text-brand-600 mb-2 group-hover:scale-110 transition-transform" />
          <span class="text-xs font-semibold text-slate-800">Thêm Từ Vựng</span>
          <span class="text-[10px] text-slate-500 mt-0.5">Flashcard & Nghĩa</span>
        </button>

        <button
          type="button"
          class="flex flex-col items-center p-4 rounded-md border border-slate-200 hover:border-brand-500 hover:bg-brand-50/50 transition-all text-center group"
          @click="navigateTo('/grammar?action=add')"
        >
          <Sparkles class="w-6 h-6 text-purple-600 mb-2 group-hover:scale-110 transition-transform" />
          <span class="text-xs font-semibold text-slate-800">Thêm Ngữ Pháp</span>
          <span class="text-[10px] text-slate-500 mt-0.5">Cấu trúc & Ví dụ</span>
        </button>

        <button
          type="button"
          class="flex flex-col items-center p-4 rounded-md border border-slate-200 hover:border-brand-500 hover:bg-brand-50/50 transition-all text-center group"
          @click="navigateTo('/listening?action=add')"
        >
          <Headphones class="w-6 h-6 text-emerald-600 mb-2 group-hover:scale-110 transition-transform" />
          <span class="text-xs font-semibold text-slate-800">Ghi Bài Nghe</span>
          <span class="text-[10px] text-slate-500 mt-0.5">Audio & Podcast</span>
        </button>

        <button
          type="button"
          class="flex flex-col items-center p-4 rounded-md border border-slate-200 hover:border-brand-500 hover:bg-brand-50/50 transition-all text-center group"
          @click="navigateTo('/speaking?action=add')"
        >
          <Mic class="w-6 h-6 text-rose-600 mb-2 group-hover:scale-110 transition-transform" />
          <span class="text-xs font-semibold text-slate-800">Ghi Luyện Nói</span>
          <span class="text-[10px] text-slate-500 mt-0.5">Topic & Recording</span>
        </button>

        <button
          type="button"
          class="flex flex-col items-center p-4 rounded-md border border-slate-200 hover:border-brand-500 hover:bg-brand-50/50 transition-all text-center group"
          @click="navigateTo('/documents?action=upload')"
        >
          <Upload class="w-6 h-6 text-amber-600 mb-2 group-hover:scale-110 transition-transform" />
          <span class="text-xs font-semibold text-slate-800">Tải Tài Liệu</span>
          <span class="text-[10px] text-slate-500 mt-0.5">Excel, Word, PDF</span>
        </button>

        <button
          type="button"
          class="flex flex-col items-center p-4 rounded-md border border-slate-200 hover:border-brand-500 hover:bg-brand-50/50 transition-all text-center group"
          @click="navigateTo('/documents?action=add-link')"
        >
          <Globe class="w-6 h-6 text-sky-600 mb-2 group-hover:scale-110 transition-transform" />
          <span class="text-xs font-semibold text-slate-800">Lưu Liên Kết</span>
          <span class="text-[10px] text-slate-500 mt-0.5">Youtube, Website</span>
        </button>

        <button
          type="button"
          class="flex flex-col items-center p-4 rounded-md border border-slate-200 hover:border-brand-500 hover:bg-brand-50/50 transition-all text-center group"
          @click="navigateTo('/tasks?action=add')"
        >
          <CheckSquare class="w-6 h-6 text-emerald-600 mb-2 group-hover:scale-110 transition-transform" />
          <span class="text-xs font-semibold text-slate-800">Việc Cần Làm</span>
          <span class="text-[10px] text-slate-500 mt-0.5">Kế hoạch hàng ngày</span>
        </button>
      </div>
    </AppModal>

    <!-- Global Toast Component -->
    <AppToast />

    <!-- Global Floating Text Selection & AI Translation Tooltip -->
    <TextSelectionPopup />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useReviewStore } from '../../stores/review.store';
import AppSidebar from './AppSidebar.vue';
import AppHeader from './AppHeader.vue';
import AppModal from '../common/AppModal.vue';
import AppToast from '../common/AppToast.vue';
import TextSelectionPopup from '../common/TextSelectionPopup.vue';
import { BookOpen, Sparkles, Headphones, Mic, Upload, Globe, CheckSquare } from 'lucide-vue-next';

const isSidebarOpen = ref(false);
const showQuickAddModal = ref(false);

const router = useRouter();
const reviewStore = useReviewStore();

onMounted(() => {
  reviewStore.fetchSummary();
});

function navigateTo(path: string) {
  showQuickAddModal.value = false;
  router.push(path);
}
</script>
