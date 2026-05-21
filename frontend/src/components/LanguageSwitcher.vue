<template>
  <el-dropdown @command="handleCommand" trigger="click">
    <span class="lang-switcher">
      <el-icon><Globe /></el-icon>
      <span>{{ currentLocale === 'zh-CN' ? '中文' : 'EN' }}</span>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh-CN" :disabled="currentLocale === 'zh-CN'">
          中文
        </el-dropdown-item>
        <el-dropdown-item command="en-US" :disabled="currentLocale === 'en-US'">
          English
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Globe } from '@element-plus/icons-vue'

const { locale } = useI18n()

const currentLocale = computed(() => locale.value)

const handleCommand = (lang) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}
</script>

<style scoped>
.lang-switcher {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  background: var(--theme-card-bg);
  border: 1px solid var(--theme-border);
  font-size: 13px;
  color: var(--theme-text);
  transition: all 0.2s;
}

.lang-switcher:hover {
  border-color: var(--theme-accent);
}
</style>
