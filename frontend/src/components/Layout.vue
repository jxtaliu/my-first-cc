<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">SME PM</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>{{ $t('nav.dashboard') }}</span>
        </el-menu-item>
        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <span>{{ $t('nav.projects') }}</span>
        </el-menu-item>
        <el-menu-item index="/timesheet">
          <el-icon><Clock /></el-icon>
          <span>{{ $t('nav.timesheet') }}</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>{{ $t('nav.admin') }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <ThemeSwitcher />
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ authStore.user?.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">Logout</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'

const router = useRouter()
const authStore = useAuthStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: var(--theme-primary);
  color: var(--theme-text);
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: var(--theme-text);
  background: var(--theme-primary);
  border-bottom: 1px solid var(--theme-border);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--theme-card-bg);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  border-bottom: 1px solid var(--theme-border);
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.main-content {
  background: var(--theme-bg);
  color: var(--theme-text);
  padding: 20px;
}
</style>
