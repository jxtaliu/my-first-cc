<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <div class="logo">SME PM</div>
      <el-menu :default-active="$route.path" router @select="handleMenuSelect">
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>{{ $t('nav.dashboard') }}</span>
        </el-menu-item>
        <!-- 我的工作 -->
        <el-sub-menu index="/my-work">
          <template #title>
            <el-icon><Grid /></el-icon>
            <span>{{ $t('nav.myWork') }}</span>
          </template>
          <el-menu-item index="/projects/my-board">
            <el-icon><Grid /></el-icon>
            <span>{{ $t('nav.myBoard') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/my-tasks">
            <el-icon><List /></el-icon>
            <span>{{ $t('nav.myTasks') }}</span>
          </el-menu-item>
          <el-menu-item index="/timesheet/my">
            <el-icon><Clock /></el-icon>
            <span>{{ $t('nav.myTimesheet') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <!-- 项目总览 -->
        <el-sub-menu index="/project-overview">
          <template #title>
            <el-icon><FolderOpened /></el-icon>
            <span>{{ $t('nav.projectOverview') }}</span>
          </template>
          <el-menu-item index="/projects">
            <el-icon><Folder /></el-icon>
            <span>{{ $t('nav.projects') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/board">
            <el-icon><Grid /></el-icon>
            <span>{{ $t('nav.projectBoard') }}</span>
          </el-menu-item>
          <el-menu-item :index="`/projects/sprint-management/${projectStore.currentProjectId}`">
            <el-icon><Timer /></el-icon>
            <span>{{ $t('nav.sprintManagement') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/milestones">
            <el-icon><Flag /></el-icon>
            <span>{{ $t('nav.milestones') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/stats">
            <el-icon><TrendCharts /></el-icon>
            <span>{{ $t('nav.projectStats') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/gantt">
            <el-icon><Connection /></el-icon>
            <span>{{ $t('nav.gantt') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/backlog">
            <el-icon><List /></el-icon>
            <span>{{ $t('nav.backlog') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/compare">
            <el-icon><DocumentCopy /></el-icon>
            <span>{{ $t('nav.compare') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/portfolio">
            <el-icon><TrendCharts /></el-icon>
            <span>{{ $t('nav.portfolio') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <!-- 需求管理 -->
        <el-sub-menu index="/requirements">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>{{ $t('nav.requirements') }}</span>
          </template>
          <el-menu-item index="/requirements">
            <el-icon><List /></el-icon>
            <span>{{ $t('nav.requirementsList') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <!-- 项目设置 -->
        <el-sub-menu index="/project-settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>{{ $t('nav.projectSettings') }}</span>
          </template>
          <el-menu-item index="/projects/settings/sprint">
            <el-icon><Timer /></el-icon>
            <span>{{ $t('nav.sprintSettings') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/settings/members">
            <el-icon><User /></el-icon>
            <span>{{ $t('nav.memberRoles') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/settings/templates">
            <el-icon><Document /></el-icon>
            <span>{{ $t('nav.projectTemplates') }}</span>
          </el-menu-item>
          <el-menu-item index="/projects/settings/notifications">
            <el-icon><Bell /></el-icon>
            <span>{{ $t('nav.notificationSettings') }}</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/timesheet">
          <el-icon><Clock /></el-icon>
          <span>{{ $t('nav.timesheet') }}</span>
        </el-menu-item>
        <el-menu-item index="/timesheet/approval">
          <el-icon><Clock /></el-icon>
          <span>{{ $t('nav.timesheetApproval') }}</span>
        </el-menu-item>
        <el-menu-item index="/notification">
          <el-icon><Bell /></el-icon>
          <span>{{ $t('nav.notification') }}</span>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" type="primary" style="margin-left: 8px" />
        </el-menu-item>
        <el-sub-menu index="/admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>{{ $t('nav.admin') }}</span>
          </template>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>{{ $t('nav.userManagement') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/roles">
            <el-icon><Key /></el-icon>
            <span>{{ $t('nav.roleManagement') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/departments">
            <el-icon><OfficeBuilding /></el-icon>
            <span>{{ $t('nav.departmentManagement') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/dicts">
            <el-icon><Document /></el-icon>
            <span>{{ $t('nav.dictManagement') }}</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <ThemeSwitcher />
          <LanguageSwitcher />
        </div>
        <div class="header-right">
          <!-- 全局项目选择器 -->
          <el-select
            v-model="projectStore.currentProjectId"
            :placeholder="$t('project.selectProject')"
            clearable
            class="project-selector"
            @change="handleProjectChange"
          >
            <el-option
              v-for="project in projectStore.projects"
              :key="project.projectId"
              :label="project.name"
              :value="project.projectId"
            />
          </el-select>
          <NotificationBell />
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ authStore.user?.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">{{ $t('common.logout') }}</el-dropdown-item>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import NotificationBell from '@/components/common/NotificationBell.vue'
import { DataAnalysis, Folder, FolderOpened, Flag, TrendCharts, Connection, DocumentCopy, Grid, Timer, User, List, Clock, Bell, Setting, Key, OfficeBuilding, Document } from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/notification'

const router = useRouter()
const authStore = useAuthStore()
const projectStore = useProjectStore()
const unreadCount = ref(0)

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount(authStore.user?.id)
    unreadCount.value = res.data || 0
  } catch (e) {
    unreadCount.value = 3 // Demo value
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}

const handleMenuSelect = (index) => {
  // 如果是需求池页面，使用全局项目选择
  if (index === '/requirements') {
    // 需求池将使用 projectStore.currentProjectId
  }
}

const handleProjectChange = (projectId) => {
  projectStore.setCurrentProject(projectId)
}

onMounted(async () => {
  // 如果有 token 但没有 user，先获取用户信息
  if (authStore.token && !authStore.user) {
    try {
      await authStore.getCurrentUser()
    } catch (e) {
      console.error('Failed to get current user:', e)
    }
  }
  if (authStore.user?.id) {
    fetchUnreadCount()
    projectStore.loadProjects()
  }
})
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

.project-selector {
  width: 180px;
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
