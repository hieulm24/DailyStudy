import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authService } from '../services/auth.service';
import type { UserProfile } from '../types';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'));
  const user = ref<UserProfile | null>(
    localStorage.getItem('user') ? JSON.parse(localStorage.getItem('user')!) : null
  );
  const loadingProfile = ref(false);

  const isAuthenticated = computed(() => !!token.value);
  const userDisplayName = computed(() => user.value?.displayName || 'Minh Hiếu');
  const userInitial = computed(() => {
    const name = user.value?.displayName || 'H';
    return name.trim().charAt(0).toUpperCase();
  });

  async function login(email: string, password: string) {
    const res = await authService.login({ email, password });
    token.value = res.token;
    localStorage.setItem('token', res.token);

    // Fetch full profile
    await fetchCurrentUser();
    return res;
  }

  async function fetchCurrentUser() {
    if (!token.value) return null;
    loadingProfile.value = true;
    try {
      const profile = await authService.getCurrentUser();
      user.value = profile;
      localStorage.setItem('user', JSON.stringify(profile));
      return profile;
    } catch (err) {
      console.warn('Failed to fetch user profile:', err);
      return null;
    } finally {
      loadingProfile.value = false;
    }
  }

  function updateUser(updatedUser: Partial<UserProfile>) {
    if (user.value) {
      user.value = { ...user.value, ...updatedUser };
      localStorage.setItem('user', JSON.stringify(user.value));
    }
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
    loadingProfile,
    userDisplayName,
    userInitial,
    login,
    fetchCurrentUser,
    updateUser,
    logout,
  };
});
