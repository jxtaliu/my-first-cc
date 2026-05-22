<template>
  <div class="project-layout" v-if="project">
    <!-- Project Header -->
    <div class="project-header">
      <div class="project-header-content">
        <!-- Breadcrumb -->
        <el-breadcrumb separator="/" class="project-breadcrumb">
          <el-breadcrumb-item :to="{ path: '/projects' }">
            <el-icon><HomeFilled /></el-icon>
            {{ $t('nav.projects') }}
          </el-breadcrumb-item>
          <el-breadcrumb-item>{{ project.name }}</el-breadcrumb-item>
        </el-breadcrumb>

        <!-- Project Title Row -->
        <div class="project-title-row">
          <div class="project-title-content">
            <h1 class="project-title">{{ project.name }}</h1>
            <div class="project-meta">
              <el-tag :type="getTypeTagType(project.type)" size="small">
                {{ project.type }}
              </el-tag>
              <el-tag :type="getStatusTagType(project.status)" size="small">
                {{ getStatusLabel(project.status) }}
              </el-tag>
              <span class="project-meta-divider">|</span>
              <span class="project-meta-item">
                <el-icon><User /></el-icon>
                {{ project.memberCount || 0 }} {{ $t('project.members') }}
              </span>
              <span class="project-meta-item">
                <el-icon><Document /></el-icon>
                {{ project.taskCount || 0 }} {{ $t('project.tasks') }}
              </span>
            </div>
          </div>

          <!-- Project Actions -->
          <div class="project-actions">
            <el-button @click="handleSettings">
              <el-icon><Setting /></el-icon>
              {{ $t('common.settings') }}
            </el-button>
            <el-dropdown trigger="click" @command="handleCommand">
              <el-button type="primary">
                {{ $t('project.actions') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="board">
                    <el-icon><Board /></el-icon>
                    {{ $t('project.board') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="backlog">
                    <el-icon><List /></el-icon>
                    {{ $t('project.backlog') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="gantt">
                    <el-icon><Histogram /></el-icon>
                    {{ $t('project.ganttView') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="milestones">
                    <el-icon><Flag /></el-icon>
                    {{ $t('project.milestones') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="stats" divided>
                    <el-icon><DataAnalysis /></el-icon>
                    {{ $t('project.statsDashboard') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- Project Navigation Tabs -->
    <div class="project-nav">
      <div class="project-nav-content">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="project-tabs">
          <el-tab-pane name="board">
            <template #label>
              <span class="tab-label">
                <el-icon><Board /></el-icon>
                {{ $t('project.board') }}
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="backlog">
            <template #label>
              <span class="tab-label">
                <el-icon><List /></el-icon>
                {{ $t('project.backlog') }}
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="gantt">
            <template #label>
              <span class="tab-label">
                <el-icon><Histogram /></el-icon>
                {{ $t('project.ganttView') }}
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="milestones">
            <template #label>
              <span class="tab-label">
                <el-icon><Flag /></el-icon>
                {{ $t('project.milestones') }}
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="team">
            <template #label>
              <span class="tab-label">
                <el-icon><User /></el-icon>
                {{ $t('project.teamBoard') }}
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="sprints">
            <template #label>
              <span class="tab-label">
                <el-icon><TrendCharts /></el-icon>
                {{ $t('project.sprints') }}
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="stats">
            <template #label>
              <span class="tab-label">
                <el-icon><DataAnalysis /></el-icon>
                {{ $t('project.statsDashboard') }}
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>

        <!-- Quick Filters -->
        <div class="project-quick-filters">
          <el-select
            v-model="filterAssignee"
            :placeholder="$t('project.filterByAssignee')"
            clearable
            size="small"
            style="width: 140px"
          >
            <el-option
              v-for="member in project.members"
              :key="member.id"
              :label="member.name"
              :value="member.id"
            />
          </el-select>
          <el-select
            v-model="filterPriority"
            :placeholder="$t('project.filterByPriority')"
            clearable
            size="small"
            style="width: 120px"
          >
            <el-option label="P0" value="P0" />
            <el-option label="P1" value="P1" />
            <el-option label="P2" value="P2" />
            <el-option label="P3" value="P3" />
          </el-select>
          <el-button size="small" @click="clearFilters" v-if="filterAssignee || filterPriority">
            {{ $t('project.clearFilters') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Content Area -->
    <div class="project-content">
      <router-view
        v-if="project"
        :project="project"
        :filter-assignee="filterAssignee"
        :filter-priority="filterPriority"
      />
    </div>
  </div>

  <!-- Loading State -->
  <div v-else-if="loading" class="project-loading">
    <el-skeleton :rows="6" animated />
  </div>

  <!-- Error State -->
  <div v-else class="project-error">
    <el-result
      icon="error"
      :title="$t('project.projectNotFound')"
      :sub-title="$t('project.projectNotFoundHint')"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/projects')">
          {{ $t('project.backToProjects') }}
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  HomeFilled,
  User,
  Document,
  Setting,
  ArrowDown,
  Board,
  List,
  Histogram,
  Flag,
  TrendCharts,
  DataAnalysis
} from '@element-plus/icons-vue'
import { getProject } from '@/api/project'

const props = defineProps({
  projectId: {
    type: [Number, String],
    required: true
  }
})

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

// State
const project = ref(null)
const loading = ref(true)
const activeTab = ref('board')
const filterAssignee = ref(null)
const filterPriority = ref(null)

// Tab to route mapping
const tabRoutes = {
  board: 'ProjectBoard',
  backlog: 'BacklogBoard',
  gantt: 'GanttView',
  milestones: 'Milestones',
  team: 'TeamBoard',
  sprints: 'SprintBoard',
  stats: 'ProjectStats'
}

// Methods
const getTypeTagType = (type) => {
  return type === 'SCRUM' ? 'success' : 'warning'
}

const getStatusTagType = (status) => {
  switch (status) {
    case 'active': return 'success'
    case 'planning': return 'primary'
    case 'completed': return 'info'
    case 'paused': return 'warning'
    default: return 'info'
  }
}

const getStatusLabel = (status) => {
  switch (status) {
    case 'active': return t('project.active')
    case 'planning': return t('project.planning')
    case 'completed': return t('project.completed')
    case 'paused': return t('project.paused')
    default: return status
  }
}

const handleTabChange = (tabName) => {
  const routeName = tabRoutes[tabName]
  if (routeName) {
    router.push({ name: routeName, params: { id: props.projectId } })
  }
}

const handleSettings = () => {
  router.push({ name: 'MemberRoles', params: { id: props.projectId } })
}

const handleCommand = (command) => {
  switch (command) {
    case 'board':
      router.push({ name: 'ProjectBoard', params: { id: props.projectId } })
      activeTab.value = 'board'
      break
    case 'backlog':
      router.push({ name: 'BacklogBoard', params: { id: props.projectId } })
      activeTab.value = 'backlog'
      break
    case 'gantt':
      router.push({ name: 'GanttView', params: { id: props.projectId } })
      activeTab.value = 'gantt'
      break
    case 'milestones':
      router.push({ name: 'Milestones', params: { id: props.projectId } })
      activeTab.value = 'milestones'
      break
    case 'stats':
      router.push({ name: 'ProjectStats', params: { id: props.projectId } })
      activeTab.value = 'stats'
      break
  }
}

const clearFilters = () => {
  filterAssignee.value = null
  filterPriority.value = null
}

// Load project data
const loadProject = async () => {
  loading.value = true
  try {
    const res = await getProject(props.projectId)
    project.value = res.data || res
  } catch (error) {
    console.error('Failed to load project:', error)
    // Use mock data on error
    project.value = {
      id: props.projectId,
      name: 'SME-PM系统',
      description: '中小企业工时与项目管理工具',
      type: 'SCRUM',
      status: 'active',
      memberCount: 6,
      taskCount: 48,
      members: [
        { id: 1, name: '张三', avatar: '' },
        { id: 2, name: '李四', avatar: '' },
        { id: 3, name: '王五', avatar: '' },
        { id: 4, name: '赵六', avatar: '' },
        { id: 5, name: '孙七', avatar: '' },
        { id: 6, name: '周八', avatar: '' }
      ]
    }
  } finally {
    loading.value = false
  }
}

// Sync active tab with route
const syncTabWithRoute = () => {
  const currentRouteName = route.name
  const tabEntry = Object.entries(tabRoutes).find(([_, name]) => name === currentRouteName)
  if (tabEntry) {
    activeTab.value = tabEntry[0]
  }
}

// Watch for project ID changes
watch(() => props.projectId, () => {
  loadProject()
  syncTabWithRoute()
})

onMounted(() => {
  loadProject()
  syncTabWithRoute()
})
</script>

<style scoped>
.project-layout {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

/* Project Header */
.project-header {
  background: var(--pm-card);
  border-bottom: 1px solid var(--pm-border);
  padding: var(--pm-space-xl) 0;
}

.project-header-content {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 var(--pm-space-xl);
}

/* Breadcrumb */
.project-breadcrumb {
  margin-bottom: var(--pm-space-md);
}

.project-breadcrumb :deep(.el-breadcrumb__item) {
  font-size: 13px;
}

/* Title Row */
.project-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--pm-space-xl);
}

.project-title-content {
  flex: 1;
  min-width: 0;
}

.project-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--pm-text-primary);
  margin: 0 0 var(--pm-space-sm) 0;
  font-family: 'Plus Jakarta Sans', sans-serif;
}

.project-meta {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  flex-wrap: wrap;
}

.project-meta-divider {
  color: var(--pm-border);
}

.project-meta-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-xs);
  font-size: 13px;
  color: var(--pm-text-secondary);
}

.project-meta-item .el-icon {
  font-size: 14px;
}

/* Project Actions */
.project-actions {
  display: flex;
  gap: var(--pm-space-sm);
  flex-shrink: 0;
}

/* Project Navigation */
.project-nav {
  background: var(--pm-card);
  border-bottom: 1px solid var(--pm-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.project-nav-content {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 var(--pm-space-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Project Tabs */
.project-tabs {
  flex: 1;
}

.project-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.project-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.project-tabs :deep(.el-tabs__item) {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  color: var(--pm-text-secondary);
  transition: color var(--pm-transition-fast);
}

.project-tabs :deep(.el-tabs__item:hover) {
  color: var(--pm-text-primary);
}

.project-tabs :deep(.el-tabs__item.is-active) {
  color: var(--pm-accent);
}

.project-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--pm-accent);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: var(--pm-space-xs);
}

.tab-label .el-icon {
  font-size: 16px;
}

/* Quick Filters */
.project-quick-filters {
  display: flex;
  gap: var(--pm-space-sm);
  align-items: center;
  padding: var(--pm-space-sm) 0;
}

/* Content Area */
.project-content {
  flex: 1;
  max-width: 1600px;
  width: 100%;
  margin: 0 auto;
  padding: var(--pm-space-xl);
}

/* Loading State */
.project-loading {
  max-width: 1600px;
  margin: 0 auto;
  padding: var(--pm-space-2xl);
}

/* Error State */
.project-error {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  padding: var(--pm-space-2xl);
}

@media (max-width: 1024px) {
  .project-nav-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--pm-space-md);
  }

  .project-tabs {
    width: 100%;
    overflow-x: auto;
  }

  .project-tabs :deep(.el-tabs__nav) {
    white-space: nowrap;
  }

  .project-quick-filters {
    padding-bottom: var(--pm-space-md);
  }
}

@media (max-width: 768px) {
  .project-title-row {
    flex-direction: column;
  }

  .project-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
