import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authService } from '../services/auth.service';
import type { User } from '../types';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'));
  const user = ref<User | null>(
    localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null
  );

  const isAuthenticated = computed(() => !!token.value);

  async function login(email: string, password: string) {
    const res = await authService.login({ email, password });
    token.value = res.token;
    user.value = {
      id: res.id,
      email: res.email,
      displayName: res.displayName,
      avatarUrl: res.avatarUrl,
    };
    localStorage.setItem('token', res.token);
    localStorage.setItem('user', JSON.stringify(user.value));
    return res;
  }

  function logout() {
    token.value = null;
    user.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  }

  return {
    token,
    user,
    isAuthenticated,
    login,
    logout,
  };
});
