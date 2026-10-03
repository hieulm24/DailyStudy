<template>
  <aside
    :class="[
      'fixed inset-y-0 left-0 z-40 flex flex-col w-72 bg-white border-r border-slate-200 transition-transform duration-200 ease-in-out lg:translate-x-0',
      isOpen ? 'translate-x-0' : '-translate-x-full',
    ]"
  >
    <!-- Brand Logo Header -->
    <div class="flex items-center justify-between h-20 px-6 border-b border-slate-100">
      <router-link to="/dashboard" class="flex items-center gap-3.5 group" @click="$emit('close')">
        <img
          src="/logo.png"
          alt="DailyStudy Hub Logo"
          class="w-10 h-10 shrink-0 object-contain rounded-md"
        />
        <div class="flex flex-col min-w-0">
          <span class="text-lg font-bold tracking-tight text-slate-900 group-hover:text-brand-600 transition-colors truncate">DailyStudy Hub</span>
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider truncate">Kế hoạch & Học tập</span>
        </div>
      </router-link>
      <button
        type="button"
        class="lg:hidden p-1.5 text-slate-400 hover:text-slate-600 rounded-md"
        @click="$emit('close')"
      >
        <X class="w-5 h-5" />
      </button>
    </div>

    <!-- Navigation Menu -->
    <nav class="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        :class="[
          'flex items-center justify-between px-3 py-2.5 text-sm font-medium rounded-md transition-colors group',
          $route.path === item.path
            ? 'bg-brand-50 text-brand-700 font-semibold'
            : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900',
        ]"
        @click="$emit('close')"
      >
        <div class="flex items-center gap-3">
          <component
            :is="item.icon"
            :class="[
              'w-4.5 h-4.5 transition-colors',
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
import { computed } from 'vue';
import { useAuthStore } from '../../stores/auth.store';
import { useReviewStore } from '../../stores/review.store';
import {
  LayoutDashboard,
  BookOpen,
  Sparkles,
  Headphones,
  Mic,
  RefreshCw,
  Gamepad2,
  FolderArchive,
  CalendarCheck,
  BarChart3,
  Settings,
  LogOut,
  X,
} from 'lucide-vue-next';

defineProps<{
  isOpen: boolean;
}>();

defineEmits<{
  (e: 'close'): void;
}>();

const authStore = useAuthStore();
const reviewStore = useReviewStore();

const userInitial = computed(() => {
  const name = authStore.user?.displayName || 'Hiếu';
  return name.charAt(0).toUpperCase();
});

const navItems = [
  { title: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
  { title: 'Từ vựng (Vocabulary)', path: '/vocabulary', icon: BookOpen },
  { title: 'Ngữ pháp (Grammar)', path: '/grammar', icon: Sparkles },
  { title: 'Luyện nghe (Listening)', path: '/listening', icon: Headphones },
  { title: 'Luyện nói (Speaking)', path: '/speaking', icon: Mic },
  { title: 'Ôn tập (Review)', path: '/review', icon: RefreshCw, badge: true },
  { title: 'Mini Games', path: '/games', icon: Gamepad2 },
  { title: 'Kho tài liệu (Resources)', path: '/documents', icon: FolderArchive },
  { title: 'Kế hoạch & Việc làm', path: '/tasks', icon: CalendarCheck },
  { title: 'Thống kê (Statistics)', path: '/statistics', icon: BarChart3 },
  { title: 'Cài đặt (Settings)', path: '/settings', icon: Settings },
];
</script>
