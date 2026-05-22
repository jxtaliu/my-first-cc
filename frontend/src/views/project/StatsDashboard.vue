<template>
  <div class="stats-dashboard-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('project.statsDashboard') }}</h1>
        <p class="pm-text-small">{{ $t('project.statsDashboardDesc') }}</p>
      </div>
      <div class="stats-actions">
        <el-select v-model="selectedProjects" multiple :placeholder="$t('project.selectProjects')" style="width: 300px">
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
        <el-select v-model="timeRange" style="width: 150px">
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
          <el-option label="本季度" value="quarter" />
          <el-option label="本年" value="year" />
        </el-select>
        <el-button type="primary" @click="onExport">
          <el-icon><Download /></el-icon>
          {{ $t('project.export') }}
        </el-button>
      </div>
    </div>

    <!-- KPI Cards -->
    <div class="stats-kpi-grid">
      <StatCard
        :label="$t('project.totalTasks')"
        :value="kpiStats.totalTasks"
        subtitle="总任务数"
      />
      <StatCard
        :label="$t('project.completedTasks')"
        :value="kpiStats.completed"
        subtitle="已完成"
        :trend="8"
      />
      <StatCard
        :label="$t('project.inProgressTasks')"
        :value="kpiStats.inProgress"
        subtitle="进行中"
      />
      <StatCard
        :label="$t('project.blockedTasks')"
        :value="kpiStats.blocked"
        subtitle="阻塞"
      />
      <StatCard
        :label="$t('project.overdueTasks')"
        :value="kpiStats.overdue"
        subtitle="逾期"
      />
      <StatCard
        :label="$t('project.taskCompletionRate')"
        :value="stats.taskCompletionRate"
        format="percent"
        :trend="5"
        :show-progress="true"
        :progress="stats.taskCompletionRate"
      />
    </div>

    <!-- Charts Row 1 -->
    <div class="stats-charts-row">
      <!-- Burndown Chart -->
      <div class="pm-card stats-chart">
        <div class="stats-chart-header">
          <h3 class="stats-chart-title">{{ $t('project.burndownChart') }}</h3>
          <el-select v-model="burndownSprint" size="small" style="width: 120px">
            <el-option label="Sprint 15" value="15" />
            <el-option label="Sprint 14" value="14" />
          </el-select>
        </div>
        <div class="stats-chart-content">
          <BurndownChart
            :ideal-data="burndownData.idealData"
            :actual-data="burndownData.actualData"
            :x-axis-data="burndownData.xAxisData"
            :total-points="100"
          />
        </div>
      </div>

      <!-- Work Hours Chart -->
      <div class="pm-card stats-chart">
        <div class="stats-chart-header">
          <h3 class="stats-chart-title">{{ $t('project.workHoursComparison') }}</h3>
        </div>
        <div class="stats-chart-content">
          <div class="hours-comparison">
            <div class="hours-bar-group">
              <span class="hours-label">Alpha项目</span>
              <div class="hours-bar-container">
                <div class="hours-bar actual" style="width: 85%;"></div>
                <div class="hours-bar estimate" style="width: 100%;"></div>
              </div>
              <span class="hours-value">85/100h</span>
            </div>
            <div class="hours-bar-group">
              <span class="hours-label">Beta项目</span>
              <div class="hours-bar-container">
                <div class="hours-bar actual" style="width: 65%;"></div>
                <div class="hours-bar estimate" style="width: 80%;"></div>
              </div>
              <span class="hours-value">65/80h</span>
            </div>
            <div class="hours-bar-group">
              <span class="hours-label">Gamma项目</span>
              <div class="hours-bar-container">
                <div class="hours-bar actual" style="width: 120%;"></div>
                <div class="hours-bar estimate" style="width: 100%;"></div>
              </div>
              <span class="hours-value">120/100h</span>
            </div>
          </div>
          <div class="hours-legend">
            <span class="legend-item"><span class="legend-dot actual"></span> 实际</span>
            <span class="legend-item"><span class="legend-dot estimate"></span> 预估</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Row 2 -->
    <div class="stats-charts-row">
      <!-- Project Comparison Table -->
      <div class="pm-card stats-chart stats-chart-wide">
        <div class="stats-chart-header">
          <h3 class="stats-chart-title">{{ $t('project.projectComparison') }}</h3>
        </div>
        <div class="stats-table-wrapper">
          <table class="pm-table">
            <thead>
              <tr>
                <th>{{ $t('project.projectName') }}</th>
                <th>{{ $t('project.progress') }}</th>
                <th>{{ $t('project.workEfficiency') }}</th>
                <th>{{ $t('project.quality') }}</th>
                <th>{{ $t('project.collaboration') }}</th>
                <th>{{ $t('project.overall') }}</th>
                <th>{{ $t('project.trend') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="project in projectComparisons" :key="project.id">
                <td>
                  <div class="project-name-cell">
                    <span class="project-status-dot" :style="{ background: project.statusColor }"></span>
                    {{ project.name }}
                  </div>
                </td>
                <td>
                  <div class="progress-cell">
                    <div class="pm-progress" style="width: 80px;">
                      <div class="pm-progress-bar" :style="{ width: project.progress + '%' }"></div>
                    </div>
                    <span>{{ project.progress }}%</span>
                  </div>
                </td>
                <td>
                  <span :class="getEfficiencyClass(project.efficiency)">
                    {{ project.efficiency }}x
                  </span>
                </td>
                <td>
                  <span :class="getQualityClass(project.quality)">
                    {{ project.quality }}%
                  </span>
                </td>
                <td>
                  <span :class="getCollabClass(project.collaboration)">
                    {{ project.collaboration }}%
                  </span>
                </td>
                <td>
                  <div class="overall-score">
                    <span class="score-value">{{ project.overall }}</span>
                    <el-tag :type="getOverallTagType(project.overall)" size="small">
                      {{ getOverallLabel(project.overall) }}
                    </el-tag>
                  </div>
                </td>
                <td>
                  <span :class="getTrendClass(project.trend)">
                    {{ project.trend > 0 ? '↑' : '↓' }} {{ Math.abs(project.trend) }}%
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Charts Row 3 -->
    <div class="stats-charts-row">
      <!-- Milestone Progress -->
      <div class="pm-card stats-chart">
        <div class="stats-chart-header">
          <h3 class="stats-chart-title">{{ $t('project.milestoneProgress') }}</h3>
        </div>
        <div class="stats-chart-content">
          <div class="milestone-progress-list">
            <div v-for="milestone in milestoneProgress" :key="milestone.id" class="milestone-progress-item">
              <div class="milestone-progress-header">
                <span class="milestone-name">{{ milestone.name }}</span>
                <span class="milestone-percent">{{ milestone.completed }}/{{ milestone.total }}</span>
              </div>
              <div class="pm-progress">
                <div
                  class="pm-progress-bar"
                  :class="getProgressBarClass(milestone.percent)"
                  :style="{ width: milestone.percent + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Team Work Distribution / Heatmap -->
      <div class="pm-card stats-chart">
        <div class="stats-chart-header">
          <h3 class="stats-chart-title">{{ $t('project.teamWorkDistribution') }}</h3>
        </div>
        <div class="stats-chart-content">
          <HeatmapChart
            :data="heatmapData"
            :x-axis-labels="['P0', 'P1', 'P2', 'P3']"
            :y-axis-labels="['张三', '李四', '王五', '赵六']"
          />
        </div>
      </div>
    </div>

    <!-- Charts Row 4 -->
    <div class="stats-charts-row">
      <!-- CFD Chart -->
      <div class="pm-card stats-chart stats-chart-wide">
        <div class="stats-chart-header">
          <h3 class="stats-chart-title">{{ $t('project.cumulativeFlow') }}</h3>
        </div>
        <div class="stats-chart-content">
          <CfdChart :data="cfdData" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import StatCard from '@/components/common/StatCard.vue'
import BurndownChart from '@/components/charts/BurndownChart.vue'
import CfdChart from '@/components/charts/CfdChart.vue'
import HeatmapChart from '@/components/charts/HeatmapChart.vue'
import { getProjectStats } from '@/api/project'

const { t } = useI18n()

const selectedProjects = ref([])
const timeRange = ref('month')
const burndownSprint = ref('15')
const loading = ref(false)

// KPI Stats
const kpiStats = ref({
  totalTasks: 0,
  completed: 0,
  inProgress: 0,
  blocked: 0,
  overdue: 0
})

const stats = ref({
  taskCompletionRate: 78,
  workEfficiency: 1.2,
  defectDensity: 0.05,
  teamThroughput: 12,
  milestoneAchievement: 85,
  avgTaskDuration: 3.5
})

// Chart data
const burndownData = ref({
  idealData: [],
  actualData: [],
  xAxisData: []
})

const cfdData = ref([])

const heatmapData = ref([])

const projects = ref([
  { id: 1, name: 'SME-PM系统' },
  { id: 2, name: '客户CRM项目' },
  { id: 3, name: '电商平台' }
])

const projectComparisons = ref([
  {
    id: 1,
    name: 'SME-PM系统',
    statusColor: '#10B981',
    progress: 85,
    efficiency: 1.2,
    quality: 92,
    collaboration: 88,
    overall: 87,
    trend: 12
  },
  {
    id: 2,
    name: '客户CRM项目',
    statusColor: '#3B82F6',
    progress: 72,
    efficiency: 0.9,
    quality: 85,
    collaboration: 80,
    overall: 78,
    trend: 5
  },
  {
    id: 3,
    name: '电商平台',
    statusColor: '#F59E0B',
    progress: 65,
    efficiency: 1.5,
    quality: 78,
    collaboration: 75,
    overall: 74,
    trend: -3
  }
])

const milestoneProgress = ref([
  { id: 1, name: 'Sprint 15 完成', completed: 16, total: 20, percent: 80 },
  { id: 2, name: '发布 V2.0', completed: 22, total: 25, percent: 88 },
  { id: 3, name: '用户模块上线', completed: 8, total: 15, percent: 53 },
  { id: 4, name: '支付模块集成', completed: 3, total: 10, percent: 30 }
])

const teamDistribution = ref([
  {
    name: '张三',
    totalTasks: 8,
    taskDistribution: { '进行中': 3, '待办': 2, '已完成': 2, '阻塞': 1 }
  },
  {
    name: '李四',
    totalTasks: 6,
    taskDistribution: { '进行中': 2, '待办': 1, '已完成': 3, '阻塞': 0 }
  },
  {
    name: '王五',
    totalTasks: 7,
    taskDistribution: { '进行中': 4, '待办': 1, '已完成': 2, '阻塞': 0 }
  },
  {
    name: '赵六',
    totalTasks: 5,
    taskDistribution: { '进行中': 1, '待办': 2, '已完成': 2, '阻塞': 0 }
  }
])

const getStatusColor = (status) => {
  const colors = {
    '待办': '#94A3B8',
    '进行中': '#3B82F6',
    '已完成': '#10B981',
    '阻塞': '#EF4444'
  }
  return colors[status] || '#94A3B8'
}

const getEfficiencyClass = (value) => {
  if (value >= 1.0) return 'efficiency-good'
  if (value >= 0.8) return 'efficiency-warning'
  return 'efficiency-bad'
}

const getQualityClass = (value) => {
  if (value >= 90) return 'quality-good'
  if (value >= 75) return 'quality-warning'
  return 'quality-bad'
}

const getCollabClass = (value) => {
  if (value >= 85) return 'collab-good'
  if (value >= 70) return 'collab-warning'
  return 'collab-bad'
}

const getTrendClass = (value) => {
  return value >= 0 ? 'trend-positive' : 'trend-negative'
}

const getOverallTagType = (value) => {
  if (value >= 85) return 'success'
  if (value >= 70) return 'warning'
  return 'danger'
}

const getOverallLabel = (value) => {
  if (value >= 85) return '优秀'
  if (value >= 70) return '良好'
  return '需改进'
}

const getProgressBarClass = (percent) => {
  if (percent >= 80) return 'success'
  if (percent >= 50) return 'warning'
  return 'danger'
}

const onExport = () => {
  ElMessage.success(t('project.exportSuccess'))
}

// Load project statistics from API
async function loadStats() {
  if (selectedProjects.value.length === 0) return

  loading.value = true
  try {
    const projectId = selectedProjects.value[0]
    const res = await getProjectStats(projectId)
    const data = res.data || res

    // Update KPI stats
    if (data.kpi) {
      kpiStats.value = {
        totalTasks: data.kpi.totalTasks || 0,
        completed: data.kpi.completed || 0,
        inProgress: data.kpi.inProgress || 0,
        blocked: data.kpi.blocked || 0,
        overdue: data.kpi.overdue || 0
      }
    }

    // Update burndown data
    if (data.burndown) {
      burndownData.value = {
        idealData: data.burndown.ideal || [],
        actualData: data.burndown.actual || [],
        xAxisData: data.burndown.days || []
      }
    }

    // Update CFD data
    if (data.cfd) {
      cfdData.value = data.cfd
    }

    // Update heatmap data
    if (data.heatmap) {
      heatmapData.value = data.heatmap
    }

    // Update general stats if available
    if (data.taskCompletionRate !== undefined) {
      stats.value.taskCompletionRate = data.taskCompletionRate
    }
    if (data.workEfficiency !== undefined) {
      stats.value.workEfficiency = data.workEfficiency
    }
  } catch (error) {
    console.error('Failed to load project stats:', error)
    // Use mock data on error
    loadMockData()
  } finally {
    loading.value = false
  }
}

// Load mock data for demo
function loadMockData() {
  burndownData.value = {
    idealData: [100, 90, 80, 70, 60, 50, 40, 30, 20, 10],
    actualData: [100, 95, 88, 78, 68, 55, 42, 30, 18, null],
    xAxisData: ['Day 1', 'Day 2', 'Day 3', 'Day 4', 'Day 5', 'Day 6', 'Day 7', 'Day 8', 'Day 9', 'Day 10']
  }

  cfdData.value = [
    { date: 'Day 1', todo: 20, in_progress: 5, done: 0 },
    { date: 'Day 2', todo: 18, in_progress: 8, done: 2 },
    { date: 'Day 3', todo: 15, in_progress: 10, done: 5 },
    { date: 'Day 4', todo: 12, in_progress: 12, done: 8 },
    { date: 'Day 5', todo: 10, in_progress: 10, done: 12 },
    { date: 'Day 6', todo: 8, in_progress: 8, done: 16 },
    { date: 'Day 7', todo: 5, in_progress: 6, done: 21 }
  ]

  heatmapData.value = [
    { assignee: '张三', priority: 'P0', count: 3 },
    { assignee: '张三', priority: 'P1', count: 5 },
    { assignee: '张三', priority: 'P2', count: 4 },
    { assignee: '李四', priority: 'P0', count: 2 },
    { assignee: '李四', priority: 'P1', count: 4 },
    { assignee: '李四', priority: 'P2', count: 3 },
    { assignee: '王五', priority: 'P1', count: 6 },
    { assignee: '王五', priority: 'P2', count: 5 },
    { assignee: '王五', priority: 'P3', count: 2 },
    { assignee: '赵六', priority: 'P0', count: 1 },
    { assignee: '赵六', priority: 'P2', count: 4 },
    { assignee: '赵六', priority: 'P3', count: 3 }
  ]

  kpiStats.value = {
    totalTasks: 48,
    completed: 21,
    inProgress: 12,
    blocked: 3,
    overdue: 5
  }
}

onMounted(() => {
  selectedProjects.value = projects.value.map(p => p.id)
  loadMockData() // Load mock data by default
  // loadStats() // Uncomment when API is ready
})
</script>

<style scoped>
.stats-dashboard-page {
  max-width: 1600px;
}

.stats-actions {
  display: flex;
  gap: var(--pm-space-md);
}

.stats-kpi-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--pm-space-lg);
  margin-bottom: var(--pm-space-2xl);
}

.stats-charts-row {
  display: flex;
  gap: var(--pm-space-lg);
  margin-bottom: var(--pm-space-2xl);
}

.stats-chart {
  flex: 1;
  min-width: 0;
}

.stats-chart-wide {
  flex: 2;
}

.stats-chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-lg);
}

.stats-chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin: 0;
}

.stats-chart-content {
  min-height: 200px;
}

/* Burndown Chart */
.burndown-chart {
  display: flex;
  align-items: stretch;
  height: 200px;
}

.burndown-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-right: var(--pm-space-sm);
  font-size: 11px;
  color: var(--pm-text-muted);
}

.burndown-canvas {
  flex: 1;
  position: relative;
}

.burndown-svg {
  width: 100%;
  height: 100%;
}

.burndown-x-axis {
  display: flex;
  justify-content: space-between;
  padding-top: var(--pm-space-sm);
  padding-left: 30px;
  font-size: 11px;
  color: var(--pm-text-muted);
}

.burndown-legend {
  display: flex;
  gap: var(--pm-space-lg);
  justify-content: center;
  margin-top: var(--pm-space-md);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-xs);
  font-size: 12px;
  color: var(--pm-text-secondary);
}

.legend-line {
  width: 20px;
  height: 3px;
}

.legend-line.ideal {
  background: #94A3B8;
  background-image: repeating-linear-gradient(
    90deg,
    #94A3B8,
    #94A3B8 4px,
    transparent 4px,
    transparent 8px
  );
}

.legend-line.actual {
  background: #3B82F6;
}

/* Hours Comparison */
.hours-comparison {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
}

.hours-bar-group {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.hours-label {
  width: 80px;
  font-size: 13px;
  color: var(--pm-text-secondary);
}

.hours-bar-container {
  flex: 1;
  height: 20px;
  position: relative;
}

.hours-bar {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  border-radius: 4px;
}

.hours-bar.estimate {
  background: var(--pm-border-light);
  z-index: 1;
}

.hours-bar.actual {
  background: linear-gradient(90deg, var(--pm-status-in-progress), var(--pm-accent));
  z-index: 2;
}

.hours-value {
  width: 70px;
  text-align: right;
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-primary);
}

.hours-legend {
  display: flex;
  gap: var(--pm-space-lg);
  justify-content: center;
  margin-top: var(--pm-space-lg);
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.legend-dot.actual {
  background: var(--pm-accent);
}

.legend-dot.estimate {
  background: var(--pm-border);
}

/* Project Table */
.stats-table-wrapper {
  overflow-x: auto;
}

.project-name-cell {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.project-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.efficiency-good { color: var(--pm-status-done); font-weight: 600; }
.efficiency-warning { color: var(--pm-status-review); font-weight: 600; }
.efficiency-bad { color: var(--pm-status-blocked); font-weight: 600; }

.quality-good { color: var(--pm-status-done); }
.quality-warning { color: var(--pm-status-review); }
.quality-bad { color: var(--pm-status-blocked); }

.collab-good { color: var(--pm-status-done); }
.collab-warning { color: var(--pm-status-review); }
.collab-bad { color: var(--pm-status-blocked); }

.trend-positive { color: var(--pm-status-done); }
.trend-negative { color: var(--pm-status-blocked); }

.overall-score {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.score-value {
  font-weight: 600;
  font-size: 16px;
  color: var(--pm-text-primary);
}

/* Milestone Progress */
.milestone-progress-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
}

.milestone-progress-item {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xs);
}

.milestone-progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.milestone-name {
  color: var(--pm-text-primary);
  font-weight: 500;
}

.milestone-percent {
  color: var(--pm-text-secondary);
}

/* Team Distribution */
.team-distribution {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
}

.team-member {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.team-member-info {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  width: 80px;
}

.team-member-name {
  font-size: 13px;
  color: var(--pm-text-primary);
}

.team-member-tasks {
  flex: 1;
}

.task-bar-container {
  height: 8px;
  display: flex;
  border-radius: 4px;
  overflow: hidden;
  background: var(--pm-border-light);
}

.task-bar-segment {
  height: 100%;
}

.team-member-count {
  width: 30px;
  text-align: right;
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-secondary);
}
</style>
