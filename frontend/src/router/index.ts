import { createRouter, createWebHistory } from 'vue-router';
import AppLayout from '../components/layout/AppLayout.vue';
import LoginView from '../views/auth/LoginView.vue';
import DashboardView from '../views/dashboard/DashboardView.vue';
import VocabularyListView from '../views/vocabulary/VocabularyListView.vue';
import GrammarListView from '../views/grammar/GrammarListView.vue';
import ListeningListView from '../views/listening/ListeningListView.vue';
import SpeakingListView from '../views/speaking/SpeakingListView.vue';
import ReviewView from '../views/review/ReviewView.vue';
import GamesHubView from '../views/games/GamesHubView.vue';
import GamePlayView from '../views/games/GamePlayView.vue';
import DocumentListView from '../views/documents/DocumentListView.vue';
import NutritionDashboardView from '../views/nutrition/NutritionDashboardView.vue';
import ReadingView from '../views/reading/ReadingView.vue';
import BookDetailView from '../views/reading/BookDetailView.vue';
import DailyTasksView from '../views/tasks/DailyTasksView.vue';
import StatisticsView from '../views/statistics/StatisticsView.vue';
import SettingsView from '../views/settings/SettingsView.vue';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { public: true },
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: DashboardView,
      },
      {
        path: 'vocabulary',
        name: 'Vocabulary',
        component: VocabularyListView,
      },
      {
        path: 'grammar',
        name: 'Grammar',
        component: GrammarListView,
      },
      {
        path: 'listening',
        name: 'Listening',
        component: ListeningListView,
      },
      {
        path: 'speaking',
        name: 'Speaking',
        component: SpeakingListView,
      },
      {
        path: 'review',
        name: 'Review',
        component: ReviewView,
      },
      {
        path: 'games',
        name: 'GamesHub',
        component: GamesHubView,
      },
      {
        path: 'games/play/:code',
        name: 'GamePlay',
        component: GamePlayView,
      },
      {
        path: 'documents',
        name: 'Documents',
        component: DocumentListView,
      },
      {
        path: 'nutrition',
        name: 'Nutrition',
        component: NutritionDashboardView,
      },
      {
        path: 'reading',
        name: 'Reading',
        component: ReadingView,
      },
      {
        path: 'reading/:id',
        name: 'BookDetail',
        component: BookDetailView,
      },
      {
        path: 'tasks',
        name: 'DailyTasks',
        component: DailyTasksView,
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: StatisticsView,
      },
      {
        path: 'settings',
        name: 'Settings',
        component: SettingsView,
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token');
  const isPublic = to.matched.some((record) => record.meta.public);

  if (!isPublic && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } });
  } else if (to.path === '/login' && token) {
    next('/dashboard');
  } else {
    next();
  }
});

export default router;
