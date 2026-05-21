<template>
  <div :class="['app-root', `theme-${themeStore.currentTheme}`]">
    <router-view />
  </div>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

.app-root {
  height: 100%;
  font-family: 'DM Sans', sans-serif;
}
</style>
