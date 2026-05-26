<template>
  <div class="project-dashboard pm-page" v-loading="loading">
    <!-- Page Header -->
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ project?.name || $t('project.selectProject') }}</h1>
    </div>

    <!-- Stats Cards Row -->
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

    <!-- Main Content - Two Columns -->
    <div class="dashboard-content">
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
            <el-empty v-else :description="$t('project.noTeamData')" :image-size="60" />
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
            <el-empty v-else :description="$t('project.noRecentActivity')" :image-size="60" />
          </div>
        </div>
      </div>

      <!-- Right Column -->
      <div class="dashboard-right">
        <!-- Sprint Gantt Chart -->
        <div class="dashboard-card">
          <div class="card-header">
            <h3>{{ $t('project.sprintGantt') }}</h3>
            <div class="header-right">
              <el-tag v-if="currentSprints.length" type="success">{{ currentSprints.length }} {{ $t('project.activeSprints') }}</el-tag>
            </div>
          </div>
          <div class="card-body" v-if="sprints.length">
            <div class="gantt-chart">
              <!-- Gantt Header -->
              <div class="gantt-header-row">
                <div class="gantt-col-name">{{ $t('project.sprintName') }}</div>
                <div class="gantt-col-timeline">
                  <div class="timeline-header">
                    <div
                      v-for="week in timelineWeeks"
                      :key="week.key"
                      class="week-cell"
                      :class="{ current: week.isCurrent }"
                    >
                      {{ week.label }}
                    </div>
                  </div>
                </div>
              </div>
              <!-- Gantt Rows -->
              <div class="gantt-body">
                <div v-for="sprint in sprints" :key="sprint.id" class="gantt-row">
                  <div class="gantt-col-name">
                    <span class="sprint-name-text">{{ sprint.name }}</span>
                    <el-tag size="small" :type="getSprintTagType(sprint.status)">{{ sprint.status }}</el-tag>
                  </div>
                  <div class="gantt-col-timeline">
                    <div class="timeline-grid">
                      <div
                        v-if="sprint.startDate && sprint.endDate"
                        class="sprint-bar"
                        :class="['status-' + sprint.status.toLowerCase(), { active: sprint.status === 'ACTIVE' }]"
                        :style="getSprintBarStyle(sprint)"
                      >
                        <span class="bar-label">{{ sprint.name }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="card-body empty-state" v-else>
            <el-empty :description="$t('project.noSprints')" :image-size="60" />
          </div>
        </div>

        <!-- Milestone Timeline - Horizontal -->
        <div class="dashboard-card milestone-timeline-card">
          <div class="card-header">
            <h3>{{ $t('project.milestones') }}</h3>
          </div>
          <div class="card-body" v-if="milestones.length">
            <div class="milestone-timeline">
              <!-- Date markers above line -->
              <div class="timeline-dates">
                <span
                  v-for="marker in timelineMarkers"
                  :key="marker.key"
                  class="date-label"
                  :style="{ left: marker.percent + '%' }"
                >
                  {{ marker.label }}
                </span>
              </div>
              <!-- Main timeline axis -->
              <div class="timeline-axis">
                <div class="axis-line"></div>
                <!-- Today indicator -->
                <div class="today-marker" :style="{ left: todayPosition + '%' }">
                  <div class="today-flag"></div>
                  <span class="today-text">{{ $t('project.today') }}</span>
                </div>
                <!-- Milestone dots -->
                <div
                  v-for="milestone in milestones"
                  :key="milestone.id"
                  class="milestone-dot-wrapper"
                  :class="'status-' + (milestone.status || 'planning').toLowerCase()"
                  :style="{ left: getMilestonePosition(milestone) + '%' }"
                >
                  <div class="milestone-line-connector"></div>
                  <div class="milestone-circle"></div>
                  <div class="milestone-label">
                    <span class="label-name">{{ milestone.name }}</span>
                    <span class="label-date">{{ formatDate(milestone.targetDate) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="card-body empty-state" v-else>
            <el-empty :description="$t('project.noMilestones')" :image-size="60" />
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
import { getTasksByProject } from '@/api/task'
import { getSprints } from '@/api/project'
import { getMilestonesByProject } from '@/api/milestone'

const { t } = useI18n()
const projectStore = useProjectStore()

// Refs
const tasks = ref([])
const sprints = ref([])
const milestones = ref([])
const loading = ref(false)

// Computed project from store
const project = computed(() => projectStore.currentProject)

// Load data
async function loadData() {
  const projectId = projectStore.currentProjectId
  if (!projectId) return

  loading.value = true
  try {
    const [tasksRes, sprintsRes, milestonesRes] = await Promise.all([
      getTasksByProject(projectId),
      getSprints(projectId),
      getMilestonesByProject(projectId)
    ])

    tasks.value = tasksRes.data || tasksRes || []
    sprints.value = sprintsRes.data || sprintsRes || []
    milestones.value = (milestonesRes.data || milestonesRes || []).slice(0, 6)
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

// Current Sprints
const currentSprints = computed(() => {
  return sprints.value.filter(s => s.status === 'ACTIVE')
})

// Sprint Gantt - Weekly view
const timelineWeeks = computed(() => {
  const weeks = []
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  let minDate = new Date(today)
  let maxDate = new Date(today)

  sprints.value.forEach(sprint => {
    if (sprint.startDate) {
      const start = new Date(sprint.startDate)
      if (start < minDate) minDate = start
    }
    if (sprint.endDate) {
      const end = new Date(sprint.endDate)
      if (end > maxDate) maxDate = end
    }
  })

  minDate.setDate(minDate.getDate() - 7)
  maxDate.setDate(maxDate.getDate() + 21)

  const dayOfWeek = minDate.getDay()
  const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek
  const monday = new Date(minDate)
  monday.setDate(minDate.getDate() + daysToMonday)

  let current = new Date(monday)
  let count = 0
  while (current <= maxDate && count < 10) {
    weeks.push({
      key: current.toISOString().split('T')[0],
      label: `${current.getMonth() + 1}/${current.getDate()}`,
      isCurrent: current <= today && new Date(current.getTime() + 6 * 24 * 60 * 60 * 1000) >= today
    })
    current.setDate(current.getDate() + 7)
    count++
  }

  return weeks
})

const timelineTotalDays = computed(() => {
  return timelineWeeks.value.length * 7
})

function getSprintBarStyle(sprint) {
  if (!timelineWeeks.value.length) return {}

  const startDate = new Date(sprint.startDate)
  const endDate = new Date(sprint.endDate)
  startDate.setHours(0, 0, 0, 0)
  endDate.setHours(0, 0, 0, 0)

  const firstWeek = timelineWeeks.value[0]
  const firstDate = new Date(firstWeek.key)
  firstDate.setHours(0, 0, 0, 0)

  const startOffset = Math.floor((startDate - firstDate) / (1000 * 60 * 60 * 24))
  const duration = Math.floor((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1

  const leftPercent = (startOffset / timelineTotalDays.value) * 100
  const widthPercent = (duration / timelineTotalDays.value) * 100

  return {
    left: `${Math.max(0, leftPercent)}%`,
    width: `${Math.min(100 - leftPercent, widthPercent)}%`
  }
}

function getSprintTagType(status) {
  const map = { 'PLANNING': 'info', 'ACTIVE': 'success', 'COMPLETED': '', 'ARCHIVED': 'warning' }
  return map[status] || 'info'
}

// Milestone Timeline - use sprint date range for better distribution
const milestoneRange = computed(() => {
  if (milestones.value.length === 0) {
    const today = new Date()
    return {
      start: new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000),
      end: new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000),
      totalDays: 60
    }
  }

  const dates = milestones.value
    .filter(m => m.targetDate)
    .map(m => new Date(m.targetDate))

  if (dates.length === 0) {
    const today = new Date()
    return {
      start: new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000),
      end: new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000),
      totalDays: 60
    }
  }

  const minDate = new Date(Math.min(...dates))
  const maxDate = new Date(Math.max(...dates))

  // Add padding: 7 days before earliest, 7 days after latest
  const start = new Date(minDate.getTime() - 7 * 24 * 60 * 60 * 1000)
  const end = new Date(maxDate.getTime() + 7 * 24 * 60 * 60 * 1000)
  const totalDays = Math.ceil((end - start) / (24 * 60 * 60 * 1000))

  return { start, end, totalDays }
})

const timelineMarkers = computed(() => {
  const { start, end, totalDays } = milestoneRange.value
  const markers = []

  // Determine interval based on total days
  let interval = 5
  if (totalDays >= 60) interval = 15
  else if (totalDays >= 30) interval = 10

  for (let i = 0; i <= totalDays; i += interval) {
    const date = new Date(start.getTime() + i * 24 * 60 * 60 * 1000)
    const percent = (i / totalDays) * 100
    markers.push({
      key: i,
      label: `${date.getMonth() + 1}/${date.getDate()}`,
      percent: percent
    })
  }
  return markers
})

function getMilestonePosition(milestone) {
  if (!milestone.targetDate) return 50
  const { start, totalDays } = milestoneRange.value
  const target = new Date(milestone.targetDate)
  const daysDiff = Math.floor((target - start) / (24 * 60 * 60 * 1000))
  const percent = (daysDiff / totalDays) * 100
  return Math.max(0, Math.min(100, percent))
}

function getMilestoneStatusClass(milestone) {
  const status = (milestone.status || 'PLANNING').toUpperCase()
  if (status === 'COMPLETED') return 'status-completed'
  if (status === 'IN_PROGRESS') return 'status-in-progress'
  return 'status-planning'
}

const todayPosition = computed(() => {
  const { start, totalDays } = milestoneRange.value
  const today = new Date()
  const daysDiff = Math.floor((today - start) / (24 * 60 * 60 * 1000))
  const percent = (daysDiff / totalDays) * 100
  return Math.max(0, Math.min(100, percent))
})

const timelineMonths = computed(() => {
  const { start, end, totalDays } = milestoneRange.value
  const months = []
  let currentMonth = -1
  let currentYear = -1

  for (let i = 0; i <= totalDays; i++) {
    const date = new Date(start.getTime() + i * 24 * 60 * 60 * 1000)
    if (date.getMonth() !== currentMonth || date.getFullYear() !== currentYear) {
      currentMonth = date.getMonth()
      currentYear = date.getFullYear()
      const percent = (i / totalDays) * 100
      const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      months.push({
        key: i,
        label: monthNames[currentMonth],
        percent: percent
      })
    }
  }
  return months
})

function isOverdue(milestone) {
  if (!milestone.endDate || milestone.status === 'COMPLETED') return false
  return new Date(milestone.endDate) < new Date()
}

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
    .slice(0, 6)
})

// Helpers
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
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
  height: 100vh;
  overflow: hidden;
  background: var(--el-fill-color-light);
}

.pm-page-header {
  margin-bottom: var(--pm-space-lg);
}

.pm-page-header h1 {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* Stats Cards */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-lg);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-lg);
  background: var(--el-bg-color);
  border-radius: var(--pm-radius-lg);
  border: 1px solid var(--el-border-color-light);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.stat-icon.total { background: linear-gradient(135deg, #3B82F6, #2563EB); color: white; }
.stat-icon.in-progress { background: linear-gradient(135deg, #F59E0B, #D97706); color: white; }
.stat-icon.done { background: linear-gradient(135deg, #10B981, #059669); color: white; }
.stat-icon.blocked { background: linear-gradient(135deg, #EF4444, #DC2626); color: white; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

/* Dashboard Content */
.dashboard-content {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: var(--pm-space-lg);
  align-items: start;
}

.dashboard-left, .dashboard-right {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
}

/* Cards */
.dashboard-card {
  background: var(--el-bg-color);
  border-radius: var(--pm-radius-lg);
  border: 1px solid var(--el-border-color-light);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--pm-space-md) var(--pm-space-lg);
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-lighter);
}

.card-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
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
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin: 0 0 var(--pm-space-sm) 0;
  font-weight: 500;
}

.distribution-bars {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.dist-label {
  width: 60px;
  font-size: 12px;
  color: var(--el-text-color-primary);
}

.dist-bar-container {
  flex: 1;
  height: 6px;
  background: var(--el-fill-color);
  border-radius: 3px;
  overflow: hidden;
}

.dist-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.dist-value {
  width: 24px;
  text-align: right;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* Sprint Gantt */
.gantt-chart {
  overflow: hidden;
}

.gantt-header-row {
  display: flex;
  border-bottom: 2px solid var(--el-border-color);
  background: var(--el-fill-color-light);
}

.gantt-col-name {
  width: 140px;
  min-width: 140px;
  padding: var(--pm-space-sm) var(--pm-space-md);
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  border-right: 1px solid var(--el-border-color-light);
}

.gantt-col-timeline {
  flex: 1;
  overflow-x: auto;
}

.timeline-header {
  display: flex;
}

.week-cell {
  flex-shrink: 0;
  width: 50px;
  padding: var(--pm-space-sm);
  text-align: center;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  border-right: 1px solid var(--el-border-color-light);
}

.week-cell.current {
  background: rgba(59, 130, 246, 0.08);
  color: #3B82F6;
  font-weight: 600;
}

.gantt-body {
  max-height: 220px;
  overflow-y: auto;
}

.gantt-row {
  display: flex;
  border-bottom: 1px solid var(--el-border-color-light);
}

.gantt-row:last-child {
  border-bottom: none;
}

.gantt-row .gantt-col-name {
  display: flex;
  flex-direction: column;
  gap: 4px;
  justify-content: center;
  border-right: 1px solid var(--el-border-color-light);
}

.sprint-name-text {
  font-size: 12px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gantt-col-timeline {
  position: relative;
  min-height: 44px;
}

.timeline-grid {
  position: relative;
  height: 100%;
  background: repeating-linear-gradient(
    90deg,
    transparent,
    transparent 49px,
    var(--el-border-color-light) 49px,
    var(--el-border-color-light) 50px
  );
}

.sprint-bar {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  padding: 0 var(--pm-space-sm);
  font-size: 11px;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.sprint-bar.status-planning { background: linear-gradient(135deg, #94A3B8, #64748B); }
.sprint-bar.status-active { background: linear-gradient(135deg, #10B981, #059669); }
.sprint-bar.status-completed { background: linear-gradient(135deg, #3B82F6, #2563EB); }
.sprint-bar.status-archived { background: linear-gradient(135deg, #6B7280, #4B5563); }
.sprint-bar.active {
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.bar-label {
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Milestone Timeline - Horizontal with blue dots */
.milestone-timeline {
  width: 100%;
  min-height: 80px;
}

.timeline-dates {
  position: relative;
  height: 24px;
  margin-bottom: 4px;
}

.date-label {
  position: absolute;
  transform: translateX(-50%);
  font-size: 11px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  padding: 0 4px;
}

.timeline-axis {
  position: relative;
  height: 60px;
}

.axis-line {
  position: absolute;
  top: 20px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--el-border-color);
}

.today-marker {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.today-flag {
  width: 2px;
  height: 20px;
  background: var(--el-color-primary);
}

.today-text {
  font-size: 10px;
  color: var(--el-color-primary);
  font-weight: 600;
  margin-top: 2px;
  white-space: nowrap;
}

.milestone-dot-wrapper {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  z-index: 5;
}

.milestone-line-connector {
  width: 2px;
  height: 20px;
  background: var(--el-border-color);
}

.milestone-circle {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  margin-top: -1px;
  background: #3B82F6;
}

.milestone-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 8px;
  white-space: nowrap;
}

.label-name {
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.label-date {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.milestone-dot-wrapper.status-completed .milestone-circle { background: #10B981; }
.milestone-dot-wrapper.status-in_progress .milestone-circle { background: #F59E0B; }

/* Team Load */
.team-load-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.team-member-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.member-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
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
  gap: var(--pm-space-sm);
  padding: 6px 0;
  font-size: 12px;
}

.recent-type {
  font-size: 9px;
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
  font-size: 11px;
  flex-shrink: 0;
}
</style>
