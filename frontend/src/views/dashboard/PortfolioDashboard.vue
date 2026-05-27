<template>
  <div class="portfolio-dashboard pm-page" v-loading="loading">
    <!-- Page Header -->
    <div class="pm-page-header">
      <h1 class="pm-heading-1">Portfolio 仪表盘</h1>
      <div class="header-filters">
        <el-select v-model="timeRange" placeholder="时间范围" style="width: 140px">
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
          <el-option label="本季度" value="quarter" />
        </el-select>
      </div>
    </div>

    <!-- Portfolio Stats Row -->
    <div class="stats-cards">
      <div class="stat-card" @click="handleStatClick('projects')">
        <div class="stat-icon projects">
          <el-icon><Folder /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ summary.totalProjects }}</div>
          <div class="stat-label">活跃项目</div>
        </div>
      </div>
      <div class="stat-card" @click="handleStatClick('tasks')">
        <div class="stat-icon tasks">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ summary.totalTasks }}</div>
          <div class="stat-label">总任务数</div>
        </div>
      </div>
      <div class="stat-card" @click="handleStatClick('completion')">
        <div class="stat-icon completion">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ summary.completionRate }}%</div>
          <div class="stat-label">完成率</div>
        </div>
      </div>
      <div class="stat-card" @click="handleStatClick('blocked')">
        <div class="stat-icon blocked">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ summary.blockedRate }}%</div>
          <div class="stat-label">阻塞率</div>
        </div>
      </div>
    </div>

    <!-- Key Metrics Row -->
    <div class="metrics-row">
      <!-- Project Health -->
      <div class="dashboard-card">
        <div class="card-header">
          <h3>项目健康度</h3>
        </div>
        <div class="card-body">
          <div class="health-chart">
            <div class="donut-chart">
              <el-progress type="donut" :percentage="100" :width="120" :stroke-width="12" color="#10B981">
                <template #default>
                  <span class="donut-center-text">{{ projectHealth.onTrack }}</span>
                  <span class="donut-center-label">正常</span>
                </template>
              </el-progress>
            </div>
            <div class="health-legend">
              <div class="legend-item">
                <span class="legend-dot on-track"></span>
                <span class="legend-label">正常</span>
                <span class="legend-value">{{ projectHealth.onTrack }}</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot at-risk"></span>
                <span class="legend-label">有风险</span>
                <span class="legend-value">{{ projectHealth.atRisk }}</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot off-track"></span>
                <span class="legend-label">延误</span>
                <span class="legend-value">{{ projectHealth.offTrack }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sprint Overview -->
      <div class="dashboard-card">
        <div class="card-header">
          <h3>Sprint 概览</h3>
        </div>
        <div class="card-body">
          <div class="sprint-stats">
            <div class="sprint-stat">
              <span class="sprint-stat-value">{{ sprintOverview.activeSprints }}</span>
              <span class="sprint-stat-label">活跃 Sprint</span>
            </div>
            <div class="sprint-stat">
              <span class="sprint-stat-value">{{ sprintOverview.avgVelocity }}</span>
              <span class="sprint-stat-label">平均速度</span>
            </div>
            <div class="sprint-stat">
              <span class="sprint-stat-value">{{ sprintOverview.teamLoad }}%</span>
              <span class="sprint-stat-label">团队负载</span>
            </div>
          </div>
          <div class="sprint-bars">
            <div class="sprint-bar-item">
              <span class="sprint-bar-label">当前 Sprint</span>
              <div class="sprint-bar-container">
                <div class="sprint-bar" :style="{ width: sprintOverview.sprintProgress + '%' }"></div>
              </div>
              <span class="sprint-bar-value">{{ sprintOverview.sprintProgress }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Bug Trend -->
      <div class="dashboard-card">
        <div class="card-header">
          <h3>Bug 趋势（7天）</h3>
        </div>
        <div class="card-body">
          <div class="bug-trend-chart">
            <svg viewBox="0 0 200 80" class="trend-svg">
              <!-- Grid lines -->
              <line x1="0" y1="75" x2="200" y2="75" stroke="#E5E7EB" stroke-width="1" />
              <line x1="0" y1="50" x2="200" y2="50" stroke="#E5E7EB" stroke-width="1" stroke-dasharray="4" />
              <line x1="0" y1="25" x2="200" y2="25" stroke="#E5E7EB" stroke-width="1" stroke-dasharray="4" />
              <line x1="0" y1="0" x2="200" y2="0" stroke="#E5E7EB" stroke-width="1" />
              <!-- Created line -->
              <polyline
                :points="bugTrend.createdPoints"
                fill="none"
                stroke="#EF4444"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <!-- Resolved line -->
              <polyline
                :points="bugTrend.resolvedPoints"
                fill="none"
                stroke="#10B981"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <div class="trend-legend">
              <span class="trend-item"><span class="trend-dot created"></span> 新增</span>
              <span class="trend-item"><span class="trend-dot resolved"></span> 已修复</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Hours Logged -->
      <div class="dashboard-card">
        <div class="card-header">
          <h3>工时填报</h3>
        </div>
        <div class="card-body">
          <div class="hours-progress">
            <div class="hours-circle">
              <el-progress type="circle" :percentage="hoursLogged.loggedPercent" :width="100" :stroke-width="10" color="#3B82F6">
                <template #default>
                  <div class="hours-center">
                    <span class="hours-value">{{ hoursLogged.logged }}</span>
                    <span class="hours-label">/ {{ hoursLogged.total }}h</span>
                  </div>
                </template>
              </el-progress>
            </div>
            <div class="hours-stats">
              <div class="hours-stat">
                <span class="hours-stat-label">利用率</span>
                <span class="hours-stat-value">{{ hoursLogged.utilization }}%</span>
              </div>
              <div class="hours-stat">
                <span class="hours-stat-label">今日填报</span>
                <span class="hours-stat-value">{{ hoursLogged.today }}h</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Projects At Risk & Upcoming Milestones -->
    <div class="alerts-section">
      <!-- Projects At Risk -->
      <div class="dashboard-card">
        <div class="card-header">
          <h3>风险项目</h3>
          <el-tag type="warning" size="small">{{ riskProjects.length }} 项</el-tag>
        </div>
        <div class="card-body">
          <div v-if="riskProjects.length > 0" class="risk-list">
            <div v-for="project in riskProjects" :key="project.id" class="risk-item">
              <div class="risk-icon" :class="project.riskLevel">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="risk-info">
                <span class="risk-name">{{ project.name }}</span>
                <span class="risk-desc">{{ project.riskDesc }}</span>
              </div>
              <el-tag :type="getRiskTagType(project.riskLevel)" size="small">
                {{ project.riskLevel === 'high' ? '高风险' : '中风险' }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="所有项目均正常" :image-size="40" />
        </div>
      </div>

      <!-- Upcoming Milestones -->
      <div class="dashboard-card">
        <div class="card-header">
          <h3>即将到期里程碑</h3>
          <el-tag type="info" size="small">{{ upcomingMilestones.length }} 个</el-tag>
        </div>
        <div class="card-body">
          <div v-if="upcomingMilestones.length > 0" class="milestone-list">
            <div v-for="milestone in upcomingMilestones" :key="milestone.id" class="milestone-item">
              <div class="milestone-date-badge">
                <span class="date-day">{{ milestone.day }}</span>
                <span class="date-month">{{ milestone.month }}</span>
              </div>
              <div class="milestone-info">
                <span class="milestone-name">{{ milestone.name }}</span>
                <span class="milestone-project">{{ milestone.projectName }}</span>
              </div>
              <el-tag :type="milestone.daysLeft <= 3 ? 'danger' : milestone.daysLeft <= 7 ? 'warning' : 'info'" size="small">
                {{ milestone.daysLeft }} 天
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无即将到期的里程碑" :image-size="40" />
        </div>
      </div>
    </div>

    <!-- Team Performance -->
    <div class="dashboard-card">
      <div class="card-header">
        <h3>团队绩效</h3>
      </div>
      <div class="card-body">
        <div class="team-leaderboard">
          <div v-for="(member, index) in teamPerformance" :key="member.id" class="leaderboard-item">
            <div class="rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
            <div class="member-avatar">
              <el-avatar :size="32" :style="{ backgroundColor: member.color }">
                {{ member.name.charAt(0) }}
              </el-avatar>
            </div>
            <div class="member-info">
              <span class="member-name">{{ member.name }}</span>
              <span class="member-team">{{ member.team }}</span>
            </div>
            <div class="member-tasks">
              <span class="tasks-completed">{{ member.tasksCompleted }}</span>
              <span class="tasks-label">任务</span>
            </div>
            <div class="member-progress">
              <el-progress :percentage="member.performanceScore" :stroke-width="6" :show-text="false" :color="member.color" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Folder, Document, CircleCheck, Warning } from '@element-plus/icons-vue'

// Mock data - will be replaced with API calls
const loading = ref(false)
const timeRange = ref('month')

// Summary stats
const summary = computed(() => ({
  totalProjects: 12,
  totalTasks: 1247,
  completionRate: 68,
  blockedRate: 3.2
}))

// Project health breakdown
const projectHealth = computed(() => ({
  onTrack: 8,
  atRisk: 3,
  offTrack: 1
}))

// Sprint overview
const sprintOverview = computed(() => ({
  activeSprints: 5,
  avgVelocity: 42,
  teamLoad: 78,
  sprintProgress: 65
}))

// Bug trend data
const bugTrend = computed(() => ({
  createdPoints: '10,60 40,45 70,55 100,35 130,50 160,30 190,40',
  resolvedPoints: '10,70 40,55 70,60 100,45 130,55 160,40 190,50'
}))

// Hours logged
const hoursLogged = computed(() => ({
  logged: 1240,
  total: 1600,
  loggedPercent: 78,
  utilization: 82,
  today: 48
}))

// Risk projects
const riskProjects = ref([
  { id: 1, name: 'Mobile App v2.0', riskDesc: '进度延误 15%', riskLevel: 'high' },
  { id: 2, name: 'Payment System', riskDesc: '阻塞任务 3个', riskLevel: 'medium' },
  { id: 3, name: 'API Gateway', riskDesc: '资源不足', riskLevel: 'medium' }
])

// Upcoming milestones
const upcomingMilestones = ref([
  { id: 1, name: 'V2.0 Release', projectName: 'Mobile App', day: '28', month: 'May', daysLeft: 2 },
  { id: 2, name: 'Beta Launch', projectName: 'Payment System', day: '02', month: 'Jun', daysLeft: 7 },
  { id: 3, name: 'API v1.0', projectName: 'API Gateway', day: '05', month: 'Jun', daysLeft: 10 }
])

// Team performance
const teamPerformance = ref([
  { id: 1, name: 'Alice Chen', team: 'Frontend', tasksCompleted: 24, performanceScore: 95, color: '#3B82F6' },
  { id: 2, name: 'Bob Wang', team: 'Backend', tasksCompleted: 21, performanceScore: 88, color: '#10B981' },
  { id: 3, name: 'Carol Liu', team: 'QA', tasksCompleted: 18, performanceScore: 82, color: '#8B5CF6' },
  { id: 4, name: 'David Zhang', team: 'DevOps', tasksCompleted: 16, performanceScore: 78, color: '#F59E0B' },
  { id: 5, name: 'Emma Li', team: 'Frontend', tasksCompleted: 15, performanceScore: 75, color: '#EC4899' }
])

function handleStatClick(type) {
  console.log('Stat clicked:', type)
}

function getRiskTagType(level) {
  const map = { high: 'danger', medium: 'warning' }
  return map[level] || 'info'
}
</script>

<style scoped>
.portfolio-dashboard {
  padding: var(--pm-space-lg);
  height: 100vh;
  overflow-y: auto;
  background: var(--el-fill-color-light);
}

.pm-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  cursor: pointer;
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

.stat-icon.projects { background: linear-gradient(135deg, #3B82F6, #2563EB); color: white; }
.stat-icon.tasks { background: linear-gradient(135deg, #8B5CF6, #7C3AED); color: white; }
.stat-icon.completion { background: linear-gradient(135deg, #10B981, #059669); color: white; }
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

/* Metrics Row */
.metrics-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-lg);
}

/* Dashboard Card */
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

.card-body {
  padding: var(--pm-space-lg);
}

/* Health Chart */
.health-chart {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
}

.donut-chart {
  flex-shrink: 0;
}

.donut-center-text {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.donut-center-label {
  display: block;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.health-legend {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot.on-track { background: #10B981; }
.legend-dot.at-risk { background: #F59E0B; }
.legend-dot.off-track { background: #EF4444; }

.legend-label {
  flex: 1;
  font-size: 13px;
  color: var(--el-text-color-primary);
}

.legend-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* Sprint Stats */
.sprint-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: var(--pm-space-lg);
}

.sprint-stat {
  text-align: center;
}

.sprint-stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.sprint-stat-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.sprint-bars {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.sprint-bar-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.sprint-bar-label {
  width: 80px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.sprint-bar-container {
  flex: 1;
  height: 8px;
  background: var(--el-fill-color);
  border-radius: 4px;
  overflow: hidden;
}

.sprint-bar {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #10B981);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.sprint-bar-value {
  width: 40px;
  text-align: right;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* Bug Trend Chart */
.bug-trend-chart {
  height: 100px;
}

.trend-svg {
  width: 100%;
  height: 80px;
}

.trend-legend {
  display: flex;
  justify-content: center;
  gap: var(--pm-space-lg);
  margin-top: var(--pm-space-sm);
}

.trend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.trend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.trend-dot.created { background: #EF4444; }
.trend-dot.resolved { background: #10B981; }

/* Hours Progress */
.hours-progress {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
}

.hours-center {
  text-align: center;
}

.hours-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.hours-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.hours-stats {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.hours-stat {
  display: flex;
  justify-content: space-between;
}

.hours-stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.hours-stat-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* Alerts Section */
.alerts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-lg);
}

/* Risk List */
.risk-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.risk-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-sm);
  background: var(--el-fill-color-light);
  border-radius: var(--pm-radius-md);
}

.risk-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.risk-icon.high { background: rgba(239, 68, 68, 0.1); color: #EF4444; }
.risk-icon.medium { background: rgba(245, 158, 11, 0.1); color: #F59E0B; }

.risk-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.risk-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.risk-desc {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

/* Milestone List */
.milestone-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.milestone-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-sm);
  background: var(--el-fill-color-light);
  border-radius: var(--pm-radius-md);
}

.milestone-date-badge {
  width: 40px;
  height: 40px;
  background: var(--el-color-primary);
  border-radius: var(--pm-radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
}

.date-day {
  font-size: 16px;
  font-weight: 700;
  line-height: 1;
}

.date-month {
  font-size: 9px;
  text-transform: uppercase;
}

.milestone-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.milestone-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.milestone-project {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

/* Team Leaderboard */
.team-leaderboard {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-sm);
  background: var(--el-fill-color-light);
  border-radius: var(--pm-radius-md);
}

.rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: var(--el-fill-color);
  color: var(--el-text-color-secondary);
}

.rank.rank-1 { background: linear-gradient(135deg, #FFD700, #FFA500); color: white; }
.rank.rank-2 { background: linear-gradient(135deg, #C0C0C0, #A0A0A0); color: white; }
.rank.rank-3 { background: linear-gradient(135deg, #CD7F32, #A0522D); color: white; }

.member-avatar {
  flex-shrink: 0;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.member-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.member-team {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.member-tasks {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 50px;
}

.tasks-completed {
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.tasks-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
}

.member-progress {
  width: 100px;
}
</style>
