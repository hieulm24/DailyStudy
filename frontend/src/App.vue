<template>
  <div id="app" class="min-h-screen relative">
    <!-- Top Route Progress Bar -->
    <div
      v-if="isNavigating"
      class="fixed top-0 left-0 right-0 z-50 h-0.5 bg-brand-600 animate-pulse shadow-sm"
    ></div>

    <!-- Router View with transition -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const isNavigating = ref(false);

router.beforeEach((_to, _from, next) => {
  isNavigating.value = true;
  next();
});

router.afterEach(() => {
  setTimeout(() => {
    isNavigating.value = false;
  }, 150);
});

onMounted(() => {
  // Smoothly remove any HTML static loader if still present
  const staticLoader = document.getElementById('app-loader');
  if (staticLoader) {
    staticLoader.remove();
  }
});
</script>

