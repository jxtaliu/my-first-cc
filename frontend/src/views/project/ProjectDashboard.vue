<template>
  <div class="project-dashboard pm-page" v-loading="loading">
    <!-- Page Header -->
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ project?.name || $t('project.selectProject') }}</h1>
    </div>

    <!-- Stats Cards -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalTasks }}</div>
          <div class="stat-label">{{ $t('project.totalTasks') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon in-progress">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.inProgressTasks }}</div>
          <div class="stat-label">{{ $t('project.inProgress') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon done">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.doneTasks }}</div>
          <div class="stat-label">{{ $t('project.doneTasks') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon blocked">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.blockedTasks }}</div>
          <div class="stat-label">{{ $t('project.blockedTasks') }}</div>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="dashboard-grid">
      <!-- Left Column -->
      <div class="dashboard-left">
        <!-- Task Distribution -->
        <div class="dashboard-card">
          <div class="card-header">
            <h3>{{ $t('project.taskDistribution') }}</h3>
          </div>
          <div class="card-body">
            <div class="distribution-section">
              <h4>{{ $t('project.byType') }}</h4>
              <div class="distribution-bars">
                <div v-for="item in distributionByType" :key="item.type" class="distribution-item">
                  <span class="dist-label">{{ item.label }}</span>
                  <div class="dist-bar-container">
                    <div class="dist-bar" :style="{ width: item.percent + '%', background: item.color }"></div>
                  </div>
                  <span class="dist-value">{{ item.count }}</span>
                </div>
              </div>
            </div>
            <div class="distribution-section">
              <h4>{{ $t('project.byPriority') }}</h4>
              <div class="distribution-bars">
                <div v-for="item in distributionByPriority" :key="item.priority" class="distribution-item">
                  <span class="dist-label">{{ item.label }}</span>
                  <div class="dist-bar-container">
                    <div class="dist-bar" :style="{ width: item.percent + '%', background: item.color }"></div>
                  </div>
                  <span class="dist-value">{{ item.count }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Current Sprint Overview -->
        <div class="dashboard-card">
          <div class="card-header">
            <h3>{{ $t('project.currentSprint') }}</h3>
            <el-tag v-if="currentSprint" :type="sprintStatusType">{{ currentSprint.status }}</el-tag>
          </div>
          <div class="card-body" v-if="currentSprint">
            <div class="sprint-info">
              <div class="sprint-name">{{ currentSprint.name }}</div>
              <div class="sprint-dates" v-if="currentSprint.startDate">
                {{ formatDate(currentSprint.startDate) }} - {{ formatDate(currentSprint.endDate) }}
              </div>
            </div>
            <div class="sprint-progress">
              <div class="progress-header">
                <span>{{ $t('project.completion') }}: {{ sprintStats.completed }}/{{ sprintStats.total }}</span>
                <span>{{ sprintStats.percent }}%</span>
              </div>
              <el-progress :percentage="sprintStats.percent" :stroke-width="8" />
            </div>
            <div class="sprint-meta">
              <div class="meta-item">
                <span class="meta-label">{{ $t('project.remainingDays') }}:</span>
                <span class="meta-value">{{ sprintStats.remainingDays }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">{{ $t('project.capacity') }}:</span>
                <span class="meta-value">{{ sprintStats.capacityHours }}h</span>
              </div>
            </div>
          </div>
          <div class="card-body empty-state" v-else>
            <el-empty :description="$t('project.noActiveSprint')" />
          </div>
        </div>
      </div>

      <!-- Right Column -->
      <div class="dashboard-right">
        <!-- Milestone Timeline -->
        <div class="dashboard-card">
          <div class="card-header">
            <h3>{{ $t('project.milestones') }}</h3>
          </div>
          <div class="card-body">
            <div v-if="milestones.length > 0" class="milestone-timeline">
              <div v-for="milestone in milestones" :key="milestone.id" class="milestone-item" :class="{ overdue: isOverdue(milestone) }">
                <div class="milestone-date">{{ formatDate(milestone.endDate) }}</div>
                <div class="milestone-info">
                  <div class="milestone-name">{{ milestone.name }}</div>
                  <div class="milestone-status" :class="milestone.status?.toLowerCase()">{{ milestone.status }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else :description="$t('project.noMilestones')" />
          </div>
        </div>

        <!-- Team Load -->
        <div class="dashboard-card">
          <div class="card-header">
            <h3>{{ $t('project.teamLoad') }}</h3>
          </div>
          <div class="card-body">
            <div v-if="teamLoad.length > 0" class="team-load-list">
              <div v-for="member in teamLoad" :key="member.id" class="team-member-item">
                <div class="member-info">
                  <span class="member-name">{{ member.name }}</span>
                  <span class="member-tasks">{{ member.taskCount }} {{ $t('project.tasks') }}</span>
                </div>
                <div class="member-load-bar">
                  <div class="load-bar" :style="{ width: Math.min(member.loadPercent, 100) + '%', background: member.loadColor }"></div>
                </div>
              </div>
            </div>
            <el-empty v-else :description="$t('project.noTeamData')" />
          </div>
        </div>

        <!-- Recent Activity -->
        <div class="dashboard-card">
          <div class="card-header">
            <h3>{{ $t('project.recentActivity') }}</h3>
          </div>
          <div class="card-body">
            <div v-if="recentTasks.length > 0" class="recent-list">
              <div v-for="task in recentTasks" :key="task.id" class="recent-item">
                <span class="recent-type" :class="'type-' + task.type?.toLowerCase()">{{ task.type }}</span>
                <span class="recent-title">{{ task.title }}</span>
                <span class="recent-time">{{ formatRelativeTime(task.updatedAt) }}</span>
              </div>
            </div>
            <el-empty v-else :description="$t('project.noRecentActivity')" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Document, Clock, CircleCheck, Warning } from '@element-plus/icons-vue'
import { useProjectStore } from '@/stores/project'
import { getProject } from '@/api/project'
import { getTasksByProject } from '@/api/task'
import { getSprints } from '@/api/project'
import { getMilestones } from '@/api/milestone'

const { t } = useI18n()
const projectStore = useProjectStore()

// Refs
const project = ref(null)
const tasks = ref([])
const sprints = ref([])
const milestones = ref([])
const loading = ref(false)

// Load data
async function loadData() {
  const projectId = projectStore.currentProjectId
  if (!projectId) return

  loading.value = true
  try {
    const [projectRes, tasksRes, sprintsRes, milestonesRes] = await Promise.all([
      getProject(projectId),
      getTasksByProject(projectId),
      getSprints(projectId),
      getMilestones(projectId)
    ])

    project.value = projectRes.data || projectRes
    tasks.value = tasksRes.data || tasksRes || []
    sprints.value = sprintsRes.data || sprintsRes || []
    milestones.value = (milestonesRes.data || milestonesRes || []).slice(0, 5)
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
    ElMessage.error(t('project.loadFailed'))
  } finally {
    loading.value = false
  }
}

// Stats
const stats = computed(() => {
  const totalTasks = tasks.value.filter(t => ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase())).length
  const doneTasks = tasks.value.filter(t =>
    ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase()) &&
    t.status?.toUpperCase() === 'DONE'
  ).length
  const inProgressTasks = tasks.value.filter(t =>
    ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase()) &&
    t.status?.toUpperCase() === 'IN_PROGRESS'
  ).length
  const blockedTasks = tasks.value.filter(t => t.blocked || t.dependencyBlocked).length

  return { totalTasks, doneTasks, inProgressTasks, blockedTasks }
})

// Distribution by Type
const distributionByType = computed(() => {
  const types = ['EPIC', 'FEATURE', 'STORY', 'TASK', 'SUBTASK', 'BUG']
  const labels = { EPIC: 'Epic', FEATURE: 'Feature', STORY: 'Story', TASK: 'Task', SUBTASK: 'Subtask', BUG: 'Bug' }
  const colors = { EPIC: '#8B5CF6', FEATURE: '#3B82F6', STORY: '#10B981', TASK: '#F59E0B', SUBTASK: '#6B7280', BUG: '#EF4444' }

  const counts = {}
  types.forEach(type => {
    counts[type] = tasks.value.filter(t => t.type?.toUpperCase() === type).length
  })

  const total = Object.values(counts).reduce((a, b) => a + b, 0) || 1

  return types.map(type => ({
    type,
    label: labels[type],
    color: colors[type],
    count: counts[type],
    percent: Math.round((counts[type] / total) * 100)
  }))
})

// Distribution by Priority
const distributionByPriority = computed(() => {
  const priorities = ['P0', 'P1', 'P2', 'P3']
  const labels = { P0: 'P0', P1: 'P1', P2: 'P2', P3: 'P3' }
  const colors = { P0: '#EF4444', P1: '#F59E0B', P2: '#3B82F6', P3: '#6B7280' }

  const counts = {}
  priorities.forEach(p => {
    counts[p] = tasks.value.filter(t => t.priority === p).length
  })

  const total = Object.values(counts).reduce((a, b) => a + b, 0) || 1

  return priorities.map(p => ({
    priority: p,
    label: labels[p],
    color: colors[p],
    count: counts[p],
    percent: Math.round((counts[p] / total) * 100)
  }))
})

// Current Sprint
const currentSprint = computed(() => {
  return sprints.value.find(s => s.status === 'ACTIVE' || s.status === 'IN_PROGRESS')
})

const sprintStatusType = computed(() => {
  if (!currentSprint.value) return 'info'
  const statusMap = { 'PLANNING': 'warning', 'ACTIVE': 'success', 'COMPLETED': 'info' }
  return statusMap[currentSprint.value.status] || 'info'
})

const sprintStats = computed(() => {
  if (!currentSprint.value) return { completed: 0, total: 0, percent: 0, remainingDays: 0, capacityHours: 0 }

  const sprintId = currentSprint.value.id
  const sprintTasks = tasks.value.filter(t => Number(t.sprintId) === Number(sprintId))
  const total = sprintTasks.length
  const completed = sprintTasks.filter(t => t.status?.toUpperCase() === 'DONE').length
  const percent = total > 0 ? Math.round((completed / total) * 100) : 0

  let remainingDays = 0
  if (currentSprint.value.endDate) {
    const end = new Date(currentSprint.value.endDate)
    const now = new Date()
    remainingDays = Math.max(0, Math.ceil((end - now) / (1000 * 60 * 60 * 24)))
  }

  return {
    completed,
    total,
    percent,
    remainingDays,
    capacityHours: currentSprint.value.capacityHours || 0
  }
})

// Team Load
const teamLoad = computed(() => {
  const memberMap = {}

  tasks.value.forEach(task => {
    if (task.assigneeId) {
      if (!memberMap[task.assigneeId]) {
        memberMap[task.assigneeId] = {
          id: task.assigneeId,
          name: task.assigneeName || `User ${task.assigneeId}`,
          taskCount: 0,
          loadPercent: 0,
          loadColor: '#10B981'
        }
      }
      memberMap[task.assigneeId].taskCount++
    }
  })

  return Object.values(memberMap)
    .map(m => ({
      ...m,
      loadPercent: Math.min(m.taskCount * 10, 100),
      loadColor: m.taskCount > 10 ? '#EF4444' : m.taskCount > 7 ? '#F59E0B' : '#10B981'
    }))
    .sort((a, b) => b.taskCount - a.taskCount)
    .slice(0, 5)
})

// Recent Tasks
const recentTasks = computed(() => {
  return [...tasks.value]
    .sort((a, b) => new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0))
    .slice(0, 8)
})

// Helpers
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString()
}

function isOverdue(milestone) {
  if (!milestone.endDate || milestone.status === 'COMPLETED') return false
  return new Date(milestone.endDate) < new Date()
}

function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return t('notification.justNow')
  if (minutes < 60) return t('notification.minutesAgo', { minutes })
  if (hours < 24) return t('notification.hoursAgo', { hours })
  return t('notification.daysAgo', { days })
}

// Watch for project changes
watch(() => projectStore.currentProjectId, () => {
  if (projectStore.currentProjectId) {
    loadData()
  }
})

onMounted(() => {
  if (projectStore.currentProjectId) {
    loadData()
  }
})
</script>

<style scoped>
.project-dashboard {
  padding: var(--pm-space-lg);
  overflow-y: auto;
  height: 100vh;
}

.pm-page-header {
  margin-bottom: var(--pm-space-lg);
}

/* Stats Cards */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--pm-space-lg);
  margin-bottom: var(--pm-space-lg);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-lg);
  background: var(--el-bg-color);
  border-radius: var(--pm-radius-md);
  border: 1px solid var(--el-border-color);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.total { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
.stat-icon.in-progress { background: rgba(245, 158, 11, 0.1); color: #F59E0B; }
.stat-icon.done { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.stat-icon.blocked { background: rgba(239, 68, 68, 0.1); color: #EF4444; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

/* Dashboard Grid */
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--pm-space-lg);
}

.dashboard-left, .dashboard-right {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
}

.dashboard-card {
  background: var(--el-bg-color);
  border-radius: var(--pm-radius-md);
  border: 1px solid var(--el-border-color);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--pm-space-md) var(--pm-space-lg);
  border-bottom: 1px solid var(--el-border-color);
  background: var(--el-fill-color-light);
}

.card-header h3 {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
}

.card-body {
  padding: var(--pm-space-lg);
}

.card-body.empty-state {
  padding: var(--pm-space-xl);
}

/* Distribution */
.distribution-section {
  margin-bottom: var(--pm-space-lg);
}

.distribution-section:last-child {
  margin-bottom: 0;
}

.distribution-section h4 {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin: 0 0 var(--pm-space-md) 0;
}

.distribution-bars {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.dist-label {
  width: 70px;
  font-size: 13px;
  color: var(--el-text-color-primary);
}

.dist-bar-container {
  flex: 1;
  height: 8px;
  background: var(--el-fill-color);
  border-radius: 4px;
  overflow: hidden;
}

.dist-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.dist-value {
  width: 30px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* Sprint */
.sprint-info {
  margin-bottom: var(--pm-space-md);
}

.sprint-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.sprint-dates {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.sprint-progress {
  margin-bottom: var(--pm-space-md);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: var(--pm-space-sm);
}

.sprint-meta {
  display: flex;
  gap: var(--pm-space-lg);
}

.meta-item {
  display: flex;
  gap: var(--pm-space-sm);
  font-size: 13px;
}

.meta-label {
  color: var(--el-text-color-secondary);
}

.meta-value {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* Milestone Timeline */
.milestone-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
}

.milestone-item {
  display: flex;
  gap: var(--pm-space-md);
  padding: var(--pm-space-sm) 0;
  border-left: 2px solid var(--el-border-color);
  padding-left: var(--pm-space-md);
}

.milestone-item.overdue {
  border-left-color: #EF4444;
}

.milestone-date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  width: 80px;
  flex-shrink: 0;
}

.milestone-info {
  flex: 1;
}

.milestone-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.milestone-status {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-top: 4px;
  display: inline-block;
}

.milestone-status.completed {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.milestone-status.active, .milestone-status.in_progress {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.milestone-status.planning {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

/* Team Load */
.team-load-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
}

.team-member-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.member-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.member-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.member-tasks {
  color: var(--el-text-color-secondary);
}

.member-load-bar {
  height: 6px;
  background: var(--el-fill-color);
  border-radius: 3px;
  overflow: hidden;
}

.load-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

/* Recent Activity */
.recent-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.recent-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-sm) 0;
  font-size: 13px;
}

.recent-type {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.recent-type.type-epic { background: rgba(139, 92, 246, 0.1); color: #8B5CF6; }
.recent-type.type-feature { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
.recent-type.type-story { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.recent-type.type-task { background: rgba(245, 158, 11, 0.1); color: #F59E0B; }
.recent-type.type-subtask { background: rgba(107, 114, 128, 0.1); color: #6B7280; }
.recent-type.type-bug { background: rgba(239, 68, 68, 0.1); color: #EF4444; }

.recent-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--el-text-color-primary);
}

.recent-time {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  flex-shrink: 0;
}
</style>
