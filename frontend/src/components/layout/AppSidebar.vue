<template>
  <aside
    :class="[
      'fixed inset-y-0 left-0 z-40 flex flex-col w-72 bg-white border-r border-slate-200 transition-transform duration-200 ease-in-out lg:translate-x-0',
      isOpen ? 'translate-x-0' : '-translate-x-full',
    ]"
  >
    <!-- Brand Logo Header -->
    <div class="relative flex items-center justify-center h-28 px-4 border-b border-slate-200 bg-white overflow-hidden shrink-0">
      <router-link to="/dashboard" class="flex items-center justify-center w-full h-full group" @click="$emit('close')">
        <img
          src="/logo.png"
          alt="Logo"
          style="max-height: 84px; max-width: 200px; width: auto; height: auto; object-fit: contain;"
          class="shrink-0 group-hover:scale-105 transition-transform duration-200 drop-shadow-xs"
        />
      </router-link>
      <button
        type="button"
        class="lg:hidden absolute right-3 top-3 p-1.5 text-slate-400 hover:text-slate-600 rounded-md"
        @click="$emit('close')"
      >
        <X class="w-5 h-5" />
      </button>
    </div>

    <!-- Navigation Menu -->
    <nav class="flex-1 px-3 py-4 space-y-1.5 overflow-y-auto">
      <!-- Tổng quan (Dashboard) -->
      <router-link
        to="/dashboard"
        :class="[
          'flex items-center justify-between px-3.5 py-2.5 text-[14.5px] font-medium rounded-lg transition-all duration-150 group',
          $route.path === '/dashboard'
            ? 'bg-brand-50 text-brand-700 font-semibold shadow-2xs'
            : 'text-slate-700 hover:bg-slate-100/80 hover:text-slate-900',
        ]"
        @click="$emit('close')"
      >
        <div class="flex items-center gap-3">
          <LayoutDashboard
            :class="[
              'w-5 h-5 transition-colors',
              $route.path === '/dashboard' ? 'text-brand-600' : 'text-slate-400 group-hover:text-slate-600',
            ]"
          />
          <span>Tổng quan</span>
        </div>
      </router-link>

      <!-- Thống kê hoạt động -->
      <router-link
        to="/statistics"
        :class="[
          'flex items-center justify-between px-3.5 py-2.5 text-[14.5px] font-medium rounded-lg transition-all duration-150 group',
          $route.path === '/statistics'
            ? 'bg-brand-50 text-brand-700 font-semibold shadow-2xs'
            : 'text-slate-700 hover:bg-slate-100/80 hover:text-slate-900',
        ]"
        @click="$emit('close')"
      >
        <div class="flex items-center gap-3">
          <BarChart3
            :class="[
              'w-5 h-5 transition-colors',
              $route.path === '/statistics' ? 'text-brand-600' : 'text-slate-400 group-hover:text-slate-600',
            ]"
          />
          <span>Thống kê hoạt động</span>
        </div>
      </router-link>

      <!-- Kế hoạch hàng ngày -->
      <router-link
        to="/tasks"
        :class="[
          'flex items-center justify-between px-3.5 py-2.5 text-[14.5px] font-medium rounded-lg transition-all duration-150 group',
          $route.path === '/tasks'
            ? 'bg-brand-50 text-brand-700 font-semibold shadow-2xs'
            : 'text-slate-700 hover:bg-slate-100/80 hover:text-slate-900',
        ]"
        @click="$emit('close')"
      >
        <div class="flex items-center gap-3">
          <CalendarCheck
            :class="[
              'w-5 h-5 transition-colors',
              $route.path === '/tasks' ? 'text-brand-600' : 'text-slate-400 group-hover:text-slate-600',
            ]"
          />
          <span>Kế hoạch hàng ngày</span>
        </div>
      </router-link>

      <!-- Sức khỏe -->
      <router-link
        to="/nutrition"
        :class="[
          'flex items-center justify-between px-3.5 py-2.5 text-[14.5px] font-medium rounded-lg transition-all duration-150 group',
          $route.path === '/nutrition'
            ? 'bg-brand-50 text-brand-700 font-semibold shadow-2xs'
            : 'text-slate-700 hover:bg-slate-100/80 hover:text-slate-900',
        ]"
        @click="$emit('close')"
      >
        <div class="flex items-center gap-3">
          <HeartPulse
            :class="[
              'w-5 h-5 transition-colors',
              $route.path === '/nutrition' ? 'text-brand-600' : 'text-slate-400 group-hover:text-slate-600',
            ]"
          />
          <span>Sức khỏe</span>
        </div>
      </router-link>

      <!-- Đọc sách -->
      <router-link
        to="/reading"
        :class="[
          'flex items-center justify-between px-3.5 py-2.5 text-[14.5px] font-medium rounded-lg transition-all duration-150 group',
          $route.path === '/reading'
            ? 'bg-brand-50 text-brand-700 font-semibold shadow-2xs'
            : 'text-slate-700 hover:bg-slate-100/80 hover:text-slate-900',
        ]"
        @click="$emit('close')"
      >
        <div class="flex items-center gap-3">
          <BookMarked
            :class="[
              'w-5 h-5 transition-colors',
              $route.path === '/reading' ? 'text-brand-600' : 'text-slate-400 group-hover:text-slate-600',
            ]"
          />
          <span>Đọc sách</span>
        </div>
      </router-link>

      <!-- Tiếng Anh (Dropdown Menu) -->
      <div>
        <button
          type="button"
          :class="[
            'w-full flex items-center justify-between px-3.5 py-2.5 text-[14.5px] font-medium rounded-lg transition-all duration-150 group',
            isEnglishActive
              ? 'text-brand-700 font-semibold bg-brand-50/70 shadow-2xs'
              : 'text-slate-700 hover:bg-slate-100/80 hover:text-slate-900',
          ]"
          @click="isEnglishOpen = !isEnglishOpen"
        >
          <div class="flex items-center gap-3">
            <GraduationCap
              :class="[
                'w-5 h-5 transition-colors',
                isEnglishActive ? 'text-brand-600' : 'text-slate-400 group-hover:text-slate-600',
              ]"
            />
            <span>Tiếng Anh</span>
          </div>
          <ChevronDown
            :class="[
              'w-4.5 h-4.5 text-slate-400 transition-transform duration-200',
              isEnglishOpen ? 'rotate-180 text-brand-600' : '',
            ]"
          />
        </button>

        <!-- Sub-menu items -->
        <transition
          enter-active-class="transition-all duration-200 ease-out"
          enter-from-class="opacity-0 max-h-0 overflow-hidden"
          enter-to-class="opacity-100 max-h-60"
          leave-active-class="transition-all duration-150 ease-in"
          leave-from-class="opacity-100 max-h-60"
          leave-to-class="opacity-0 max-h-0 overflow-hidden"
        >
          <div
            v-show="isEnglishOpen"
            class="mt-1 ml-4 pl-3 space-y-1 border-l-2 border-slate-100"
          >
            <router-link
              v-for="sub in englishSubItems"
              :key="sub.path"
              :to="sub.path"
              :class="[
                'flex items-center gap-2.5 px-3 py-2 text-[13.5px] font-medium rounded-md transition-colors group',
                $route.path === sub.path
                  ? 'bg-brand-50 text-brand-700 font-semibold'
                  : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900',
              ]"
              @click="$emit('close')"
            >
              <component
                :is="sub.icon"
                :class="[
                  'w-4 h-4 transition-colors',
                  $route.path === sub.path ? 'text-brand-600' : 'text-slate-400 group-hover:text-slate-600',
                ]"
              />
              <span>{{ sub.title }}</span>
            </router-link>
          </div>
        </transition>
      </div>

      <!-- Other Menu Items -->
      <router-link
        v-for="item in otherNavItems"
        :key="item.path"
        :to="item.path"
        :class="[
          'flex items-center justify-between px-3.5 py-2.5 text-[14.5px] font-medium rounded-lg transition-all duration-150 group',
          $route.path === item.path
            ? 'bg-brand-50 text-brand-700 font-semibold shadow-2xs'
            : 'text-slate-700 hover:bg-slate-100/80 hover:text-slate-900',
        ]"
        @click="$emit('close')"
      >
        <div class="flex items-center gap-3">
          <component
            :is="item.icon"
            :class="[
              'w-5 h-5 transition-colors',
              $route.path === item.path ? 'text-brand-600' : 'text-slate-400 group-hover:text-slate-600',
            ]"
          />
          <span>{{ item.title }}</span>
        </div>

        <!-- Review badge -->
        <span
          v-if="item.badge && reviewStore.summary.totalDue > 0"
          class="inline-flex items-center px-2 py-0.5 text-xs font-semibold rounded-full bg-amber-500 text-white animate-pulse"
        >
          {{ reviewStore.summary.totalDue }}
        </span>
      </router-link>
    </nav>

    <!-- User Profile & Logout Footer -->
    <div class="p-3.5 border-t border-slate-100 bg-slate-50/50">
      <div class="flex items-center justify-between p-3 rounded-md bg-white border border-slate-200">
        <div class="flex items-center gap-3 overflow-hidden">
          <div class="w-9 h-9 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center font-bold text-sm flex-shrink-0">
            {{ userInitial }}
          </div>
          <div class="flex flex-col truncate">
            <span class="text-sm font-bold text-slate-800 truncate">{{ authStore.user?.displayName || 'Minh Hiếu' }}</span>
            <span class="text-xs text-slate-500 truncate">{{ authStore.user?.email || 'hieulm24@gmail.com' }}</span>
          </div>
        </div>
        <button
          type="button"
          title="Đăng xuất"
          class="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
          @click="authStore.logout()"
        >
          <LogOut class="w-4 h-4" />
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '../../stores/auth.store';
import { useReviewStore } from '../../stores/review.store';
import {
  LayoutDashboard,
  BarChart3,
  HeartPulse,
  BookMarked,
  GraduationCap,
  BookOpen,
  Sparkles,
  Headphones,
  Mic,
  RefreshCw,
  Gamepad2,
  FolderArchive,
  CalendarCheck,
  Settings,
  LogOut,
  X,
  ChevronDown,
} from 'lucide-vue-next';

defineProps<{
  isOpen: boolean;
}>();

defineEmits<{
  (e: 'close'): void;
}>();

const route = useRoute();
const authStore = useAuthStore();
const reviewStore = useReviewStore();

const userInitial = computed(() => {
  const name = authStore.user?.displayName || 'Hiếu';
  return name.charAt(0).toUpperCase();
});

const isEnglishOpen = ref(true);

const englishRoutes = ['/vocabulary', '/grammar', '/listening', '/speaking'];
const isEnglishActive = computed(() => englishRoutes.some(p => route.path.startsWith(p)));

// Auto-expand English menu if currently navigating inside any English route
watch(
  () => route.path,
  (newPath) => {
    if (englishRoutes.some(p => newPath.startsWith(p))) {
      isEnglishOpen.value = true;
    }
  },
  { immediate: true }
);

const englishSubItems = [
  { title: 'Từ vựng', path: '/vocabulary', icon: BookOpen },
  { title: 'Ngữ pháp', path: '/grammar', icon: Sparkles },
  { title: 'Luyện nghe', path: '/listening', icon: Headphones },
  { title: 'Luyện nói', path: '/speaking', icon: Mic },
];

const otherNavItems = [
  { title: 'Ôn tập Tiếng Anh', path: '/review', icon: RefreshCw, badge: true },
  { title: 'Trò chơi Tiếng Anh', path: '/games', icon: Gamepad2 },
  { title: 'Kho tài liệu', path: '/documents', icon: FolderArchive },
  { title: 'Cài đặt', path: '/settings', icon: Settings },
];
</script>
