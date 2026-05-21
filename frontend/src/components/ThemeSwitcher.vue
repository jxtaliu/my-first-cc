<template>
  <el-dropdown @command="handleCommand" trigger="click">
    <span class="theme-switcher">
      <span class="theme-dot" :style="{ background: currentAccent }"></span>
      <span class="theme-name">{{ themeStore.themeName }}</span>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="(config, key) in themeStore.themes"
          :key="key"
          :command="key"
        >
          <span class="theme-option">
            <span class="theme-dot small" :style="{ background: config.colors.accent }"></span>
            {{ config.name }}
          </span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const currentAccent = computed(() => themeStore.colors.accent)

const handleCommand = (theme) => {
  themeStore.setTheme(theme)
}
</script>

<style scoped>
.theme-switcher {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  background: var(--theme-card-bg);
  border: 1px solid var(--theme-border);
  transition: all 0.2s;
}

.theme-switcher:hover {
  border-color: var(--theme-accent);
}

.theme-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--theme-accent);
}

.theme-dot.small {
  width: 8px;
  height: 8px;
}

.theme-name {
  font-size: 13px;
  color: var(--theme-text);
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
