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

    <!-- Key Metrics Row -->
    <div class="metrics-row">
      <!-- Milestone Progress -->
      <el-tooltip content="已完成的里程碑数量占总里程碑的比例，反映项目整体交付进度" placement="top" effect="dark">
        <div class="dashboard-card metric-card">
          <div class="card-header">
            <h3>里程碑进度</h3>
          </div>
          <div class="card-body">
            <div class="metric-content">
              <div class="metric-main">
                <span class="metric-value">{{ milestoneProgress.completed }}/{{ milestoneProgress.total }}</span>
                <span class="metric-label">里程碑</span>
              </div>
              <div class="metric-progress">
                <el-progress :percentage="milestoneProgress.percent" :stroke-width="8" :show-text="false" />
                <span class="progress-text">{{ milestoneProgress.percent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </el-tooltip>

      <!-- Sprint Completion Rate -->
      <el-tooltip content="当前Sprint中已完成任务与计划任务的比率，衡量团队交付效率" placement="top" effect="dark">
        <div class="dashboard-card metric-card">
          <div class="card-header">
            <h3>Sprint完成率</h3>
          </div>
          <div class="card-body">
            <div class="metric-content">
              <div class="metric-main">
                <span class="metric-value">{{ sprintCompletion.completedTasks }}/{{ sprintCompletion.totalTasks }}</span>
                <span class="metric-label">任务</span>
              </div>
              <div class="metric-progress">
                <el-progress :percentage="sprintCompletion.percent" :stroke-width="8" :show-text="false" />
                <span class="progress-text">{{ sprintCompletion.percent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </el-tooltip>

      <!-- Release Countdown -->
      <el-tooltip content="距离下一个未完成里程碑的剩余天数，帮助跟踪关键里程碑" placement="top" effect="dark">
        <div class="dashboard-card metric-card">
          <div class="card-header">
            <h3>发布倒计时</h3>
          </div>
          <div class="card-body">
            <div class="metric-content center">
              <div class="countdown-display">
                <span class="countdown-value">{{ releaseCountdown.days }}</span>
                <span class="countdown-label">天</span>
              </div>
              <span class="countdown-target">{{ releaseCountdown.milestoneName }}</span>
            </div>
          </div>
        </div>
      </el-tooltip>

      <!-- Bug Density -->
      <el-tooltip content="Bug数量占总任务的比例，越低越好（优秀<8%，中等8-15%，过高>15%）" placement="top" effect="dark">
        <div class="dashboard-card metric-card">
          <div class="card-header">
            <h3>缺陷密度</h3>
          </div>
          <div class="card-body">
            <div class="metric-content center">
              <div class="metric-main">
                <span class="metric-value" :class="bugDensity.level">{{ bugDensity.value }}%</span>
                <span class="metric-label">缺陷占比</span>
              </div>
              <span class="density-desc">{{ bugDensity.desc }}</span>
            </div>
          </div>
        </div>
      </el-tooltip>

      <!-- Per Capita Output -->
      <el-tooltip content="团队成员平均完成的タスク数量，衡量个人产出效率" placement="top" effect="dark">
        <div class="dashboard-card metric-card">
          <div class="card-header">
            <h3>人均产出</h3>
          </div>
          <div class="card-body">
            <div class="metric-content center">
              <div class="metric-main">
                <span class="metric-value">{{ perCapitaOutput.value }}</span>
                <span class="metric-label">任务/人</span>
              </div>
              <span class="team-size">团队规模: {{ perCapitaOutput.teamSize }}</span>
            </div>
          </div>
        </div>
      </el-tooltip>

      <!-- Release Readiness -->
      <el-tooltip content="项目整体完成度百分比，阻塞任务数影响发布就绪状态" placement="top" effect="dark">
        <div class="dashboard-card metric-card">
          <div class="card-header">
            <h3>发布就绪度</h3>
          </div>
          <div class="card-body">
            <div class="metric-content">
              <div class="metric-main">
                <span class="metric-value">{{ releaseReadiness.percent }}%</span>
                <span class="metric-label">完成度</span>
              </div>
              <div class="metric-progress">
                <el-progress :percentage="releaseReadiness.percent" :stroke-width="8" :show-text="false" />
                <span class="progress-text blocked-count" v-if="releaseReadiness.blockedCount > 0">
                  <el-icon><Warning /></el-icon> {{ releaseReadiness.blockedCount }} 阻塞
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-tooltip>
    </div>

    <!-- Advanced Metrics Row (待完善) -->
    <div class="metrics-row advanced">
      <div class="row-header">
        <span class="row-title">高级指标</span>
        <el-tag type="warning" size="small">待完善</el-tag>
      </div>

      <div class="metrics-row-scroll">
        <!-- 缺陷修复周期 -->
        <el-tooltip content="Bug从创建到关闭的平均时间，反映团队响应速度" placement="top" effect="dark">
          <div class="dashboard-card metric-card mock">
            <div class="card-header">
              <h3>缺陷修复周期</h3>
              <el-tag type="info" size="small" effect="plain">Mock</el-tag>
            </div>
            <div class="card-body">
              <div class="metric-content center">
                <div class="metric-main">
                  <span class="metric-value">{{ advancedMetrics.bugFixCycle.value }}</span>
                  <span class="metric-label">{{ advancedMetrics.bugFixCycle.unit }}</span>
                </div>
                <span class="mock-indicator">
                  <el-icon><DataAnalysis /></el-icon> 待接入数据
                </span>
              </div>
            </div>
          </div>
        </el-tooltip>

        <!-- 代码评审覆盖率 -->
        <el-tooltip content="已评审的PR数占总PR数的比例，衡量代码评审参与度" placement="top" effect="dark">
          <div class="dashboard-card metric-card mock">
            <div class="card-header">
              <h3>代码评审覆盖率</h3>
              <el-tag type="info" size="small" effect="plain">Mock</el-tag>
            </div>
            <div class="card-body">
              <div class="metric-content">
                <div class="metric-main">
                  <span class="metric-value">{{ advancedMetrics.codeReviewCoverage.value }}%</span>
                  <span class="metric-label">已评审/总PR</span>
                </div>
                <div class="metric-progress">
                  <el-progress :percentage="advancedMetrics.codeReviewCoverage.value" :stroke-width="6" :show-text="false" color="#8B5CF6" />
                  <span class="progress-text">{{ advancedMetrics.codeReviewCoverage.value }}%</span>
                </div>
              </div>
            </div>
          </div>
        </el-tooltip>

        <!-- Velocity趋势 -->
        <el-tooltip content="过去几个Sprint完成的故事点趋势，衡量团队交付能力" placement="top" effect="dark">
          <div class="dashboard-card metric-card mock">
            <div class="card-header">
              <h3>Velocity趋势</h3>
              <el-tag type="info" size="small" effect="plain">Mock</el-tag>
            </div>
            <div class="card-body">
              <div class="velocity-chart">
                <div class="velocity-bars">
                  <div
                    v-for="(point, index) in advancedMetrics.velocityTrend"
                    :key="index"
                    class="velocity-bar-wrapper"
                  >
                    <div class="velocity-bar" :style="{ height: point + '%' }"></div>
                    <span class="velocity-label">S{{ index + 1 }}</span>
                  </div>
                </div>
                <div class="velocity-line">
                  <svg viewBox="0 0 100 30" preserveAspectRatio="none">
                    <polyline
                      :points="getVelocityPolyline()"
                      fill="none"
                      stroke="#3B82F6"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </el-tooltip>

        <!-- 工时准确度 -->
        <el-tooltip content="实际工时与预估工时的比率，>100%表示超时，<100%表示节省" placement="top" effect="dark">
          <div class="dashboard-card metric-card mock">
            <div class="card-header">
              <h3>工时准确度</h3>
              <el-tag type="info" size="small" effect="plain">Mock</el-tag>
            </div>
            <div class="card-body">
              <div class="metric-content center">
                <div class="metric-main">
                  <span class="metric-value" :class="advancedMetrics.workHourAccuracy.class">
                    {{ advancedMetrics.workHourAccuracy.value }}%
                  </span>
                  <span class="metric-label">{{ advancedMetrics.workHourAccuracy.label }}</span>
                </div>
                <div class="accuracy-meter">
                  <div class="meter-bar">
                    <div class="meter-target"></div>
                    <div class="meter-indicator" :style="{ left: advancedMetrics.workHourAccuracy.value + '%' }"></div>
                  </div>
                  <div class="meter-labels">
                    <span>偏少</span>
                    <span>100%</span>
                    <span>偏多</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tooltip>

        <!-- 燃尽图 -->
        <el-tooltip content="Sprint期间剩余工作量的趋势，理想情况下应线性下降至0" placement="top" effect="dark">
          <div class="dashboard-card metric-card mock">
            <div class="card-header">
              <h3>燃尽图</h3>
              <el-tag type="info" size="small" effect="plain">Mock</el-tag>
            </div>
            <div class="card-body">
              <div class="burndown-chart">
                <svg viewBox="0 0 120 60" preserveAspectRatio="none">
                  <!-- Ideal line -->
                  <line x1="5" y1="5" x2="115" y2="55" stroke="#E5E7EB" stroke-width="1" stroke-dasharray="4" />
                  <!-- Actual line -->
                  <polyline
                    :points="advancedMetrics.burndown.points"
                    fill="none"
                    stroke="#10B981"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <!-- Points -->
                  <circle
                    v-for="(point, index) in advancedMetrics.burndown.dataPoints"
                    :key="index"
                    :cx="point.x"
                    :cy="point.y"
                    r="3"
                    fill="#10B981"
                  />
                </svg>
                <div class="chart-labels">
                  <span>Sprint开始</span>
                  <span>Sprint结束</span>
                </div>
              </div>
            </div>
          </div>
        </el-tooltip>

        <!-- 需求蔓延指数 -->
        <el-tooltip content="新增需求数与原始需求数的比率，越低表示范围蔓延控制越好" placement="top" effect="dark">
          <div class="dashboard-card metric-card mock">
            <div class="card-header">
              <h3>需求蔓延指数</h3>
              <el-tag type="info" size="small" effect="plain">Mock</el-tag>
            </div>
            <div class="card-body">
              <div class="metric-content center">
                <div class="metric-main">
                  <span class="metric-value" :class="advancedMetrics.scopeCreep.class">
                    {{ advancedMetrics.scopeCreep.value }}%
                  </span>
                  <span class="metric-label">{{ advancedMetrics.scopeCreep.label }}</span>
                </div>
                <span class="scope-info">
                  <span>新增 {{ advancedMetrics.scopeCreep.added }}</span>
                  <span>/</span>
                  <span>原始 {{ advancedMetrics.scopeCreep.original }}</span>
                </span>
              </div>
            </div>
          </div>
        </el-tooltip>

        <!-- 更新频率 -->
        <el-tooltip content="任务评论/更新的数量，衡量团队协作活跃度" placement="top" effect="dark">
          <div class="dashboard-card metric-card mock">
            <div class="card-header">
              <h3>更新频率</h3>
              <el-tag type="info" size="small" effect="plain">Mock</el-tag>
            </div>
            <div class="card-body">
              <div class="metric-content center">
                <div class="metric-main">
                  <span class="metric-value">{{ advancedMetrics.updateFrequency.value }}</span>
                  <span class="metric-label">次/天</span>
                </div>
                <div class="frequency-trend">
                  <span class="trend-label">周趋势</span>
                  <span class="trend-icon up">
                    <el-icon><Top /></el-icon>
                  </span>
                  <span class="trend-value">+12%</span>
                </div>
              </div>
            </div>
          </div>
        </el-tooltip>

        <!-- Blocked任务处理时间 -->
        <el-tooltip content="任务从被阻塞到解除阻塞的平均时间，越短越好" placement="top" effect="dark">
          <div class="dashboard-card metric-card mock">
            <div class="card-header">
              <h3>Blocked处理时间</h3>
              <el-tag type="info" size="small" effect="plain">Mock</el-tag>
            </div>
            <div class="card-body">
              <div class="metric-content center">
                <div class="metric-main">
                  <span class="metric-value" :class="advancedMetrics.blockedTime.class">
                    {{ advancedMetrics.blockedTime.value }}
                  </span>
                  <span class="metric-label">{{ advancedMetrics.blockedTime.unit }}</span>
                </div>
                <span class="blocked-status">
                  <el-icon><Warning /></el-icon> {{ advancedMetrics.blockedTime.count }}个阻塞中
                </span>
              </div>
            </div>
          </div>
        </el-tooltip>
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
import { Document, Clock, CircleCheck, Warning, DataAnalysis, Top } from '@element-plus/icons-vue'
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

// ===== Key Metrics =====

// Milestone Progress
const milestoneProgress = computed(() => {
  const total = milestones.value.length
  const completed = milestones.value.filter(m => (m.status || '').toUpperCase() === 'COMPLETED').length
  const percent = total > 0 ? Math.round((completed / total) * 100) : 0
  return { total, completed, percent }
})

// Sprint Completion Rate (based on active sprints)
const sprintCompletion = computed(() => {
  const activeSprints = sprints.value.filter(s => s.status === 'ACTIVE')
  if (activeSprints.length === 0) {
    return { totalTasks: 0, completedTasks: 0, percent: 0 }
  }

  // Get tasks that belong to active sprints
  const sprintTaskIds = new Set()
  activeSprints.forEach(sprint => {
    // Find tasks for this sprint - using sprintId field if available
    tasks.value.forEach(task => {
      if (task.sprintId === sprint.id) {
        sprintTaskIds.add(task.id)
      }
    })
  })

  // If no tasks have sprintId, use all tasks as proxy
  if (sprintTaskIds.size === 0) {
    const totalTasks = tasks.value.filter(t => ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase())).length
    const completedTasks = tasks.value.filter(t =>
      ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase()) &&
      t.status?.toUpperCase() === 'DONE'
    ).length
    const percent = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0
    return { totalTasks, completedTasks, percent }
  }

  const sprintTasks = tasks.value.filter(t => sprintTaskIds.has(t.id))
  const totalTasks = sprintTasks.length
  const completedTasks = sprintTasks.filter(t => t.status?.toUpperCase() === 'DONE').length
  const percent = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0
  return { totalTasks, completedTasks, percent }
})

// Release Countdown (next upcoming milestone)
const releaseCountdown = computed(() => {
  const today = new Date()
  const upcomingMilestones = milestones.value
    .filter(m => m.targetDate && (m.status || '').toUpperCase() !== 'COMPLETED')
    .map(m => ({ ...m, targetDate: new Date(m.targetDate) }))
    .filter(m => m.targetDate >= today)
    .sort((a, b) => a.targetDate - b.targetDate)

  if (upcomingMilestones.length === 0) {
    return { days: '-', milestoneName: '-' }
  }

  const nextMilestone = upcomingMilestones[0]
  const daysDiff = Math.ceil((nextMilestone.targetDate - today) / (24 * 60 * 60 * 1000))
  return {
    days: daysDiff,
    milestoneName: nextMilestone.name
  }
})

// Bug Density (Bug count / Total tasks)
const bugDensity = computed(() => {
  const totalTasks = tasks.value.filter(t => ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase())).length
  const bugCount = tasks.value.filter(t => t.type?.toUpperCase() === 'BUG').length
  const density = totalTasks > 0 ? Math.round((bugCount / totalTasks) * 100) : 0

  let level = 'low'
  let desc = '优秀'
  if (density > 15) {
    level = 'high'
    desc = '过高'
  } else if (density > 8) {
    level = 'medium'
    desc = '中等'
  }

  return { value: density, level, desc }
})

// Per Capita Output
const perCapitaOutput = computed(() => {
  const teamMembers = new Set()
  tasks.value.forEach(task => {
    if (task.assigneeId) {
      teamMembers.add(task.assigneeId)
    }
  })

  const completedTasks = tasks.value.filter(t =>
    ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase()) &&
    t.status?.toUpperCase() === 'DONE'
  ).length

  const teamSize = teamMembers.size || 1
  const output = (completedTasks / teamSize).toFixed(1)

  return { value: output, teamSize }
})

// Release Readiness
const releaseReadiness = computed(() => {
  const totalTasks = tasks.value.filter(t => ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase())).length
  const completedTasks = tasks.value.filter(t =>
    ['TASK', 'SUBTASK', 'BUG'].includes(t.type?.toUpperCase()) &&
    t.status?.toUpperCase() === 'DONE'
  ).length
  const blockedCount = tasks.value.filter(t => t.blocked || t.dependencyBlocked).length

  const percent = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0
  return { percent, blockedCount }
})

// ===== Advanced Metrics (Mock - 待完善) =====
const advancedMetrics = computed(() => {
  // Mock data - these will be replaced with real API data later
  return {
    // 缺陷修复周期 - Bug从创建到关闭的平均时间
    bugFixCycle: {
      value: 2.5,
      unit: '天'
    },
    // 代码评审覆盖率 - 已评审的PR数/总PR数
    codeReviewCoverage: {
      value: 75
    },
    // Velocity趋势 - 过去几个Sprint完成的故事点
    velocityTrend: [65, 72, 68, 80, 85, 78],
    // 工时准确度 - 实际工时/预估工时
    workHourAccuracy: {
      value: 108,
      label: '略有超时',
      class: 'medium'
    },
    // 燃尽图数据
    burndown: {
      dataPoints: [
        { x: 10, y: 10 },
        { x: 30, y: 18 },
        { x: 50, y: 28 },
        { x: 70, y: 35 },
        { x: 90, y: 45 },
        { x: 110, y: 52 }
      ],
      points: '10,10 30,18 50,28 70,35 90,45 110,52'
    },
    // 需求蔓延指数 - 新增需求/原始需求
    scopeCreep: {
      value: 115,
      label: '略有蔓延',
      class: 'medium',
      added: 23,
      original: 20
    },
    // 更新频率 - 任务评论/更新的数量
    updateFrequency: {
      value: 48
    },
    // Blocked任务处理时间
    blockedTime: {
      value: 4.2,
      unit: '小时',
      class: 'low',
      count: 2
    }
  }
})

function getVelocityPolyline() {
  const points = advancedMetrics.value.velocityTrend
  const maxHeight = 30
  const width = 100
  const stepX = width / (points.length - 1)

  return points.map((point, index) => {
    const x = index * stepX
    const y = maxHeight - (point / 100) * maxHeight
    return `${x},${y}`
  }).join(' ')
}

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
  overflow-y: auto;
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

/* Key Metrics Row */
.metrics-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-lg);
}

.metric-card {
  min-height: 100px;
}

.metric-card .card-header h3 {
  font-size: 12px;
}

.metric-card .card-body {
  padding: var(--pm-space-md);
}

.metric-content {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
  height: 100%;
}

.metric-content.center {
  align-items: center;
  text-align: center;
}

.metric-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  line-height: 1;
}

.metric-value.low { color: #10B981; }
.metric-value.medium { color: #F59E0B; }
.metric-value.high { color: #EF4444; }

.metric-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.metric-progress {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.metric-progress :deep(.el-progress) {
  flex: 1;
}

.progress-text {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  min-width: 36px;
}

.blocked-count {
  display: flex;
  align-items: center;
  gap: 2px;
  color: #EF4444;
  font-size: 11px;
}

.countdown-display {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.countdown-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--el-color-primary);
  line-height: 1;
}

.countdown-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.countdown-target {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.density-desc {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.team-size {
  font-size: 11px;
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

/* Advanced Metrics Row */
.metrics-row.advanced {
  display: block;
  margin-bottom: var(--pm-space-lg);
}

.metrics-row.advanced .row-header {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-md);
}

.metrics-row.advanced .row-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.metrics-row.advanced .metrics-row-scroll {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--pm-space-md);
}

.metrics-row.advanced .metric-card.mock {
  opacity: 0.85;
  border-style: dashed;
  min-height: 90px;
}

.metrics-row.advanced .metric-card.mock .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metrics-row.advanced .metric-card.mock .card-header h3 {
  font-size: 12px;
}

.metrics-row.advanced .metric-card.mock .card-body {
  padding: var(--pm-space-sm) var(--pm-space-md);
}

.metrics-row.advanced .mock-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

/* Velocity Chart */
.velocity-chart {
  height: 50px;
  position: relative;
}

.velocity-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 100%;
  padding-bottom: 16px;
}

.velocity-bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  width: 100%;
}

.velocity-bar {
  width: 8px;
  background: linear-gradient(180deg, #3B82F6, #60A5FA);
  border-radius: 2px 2px 0 0;
  transition: height 0.3s ease;
  margin-top: auto;
}

.velocity-label {
  font-size: 9px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
  position: absolute;
  bottom: 0;
}

.velocity-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 30px;
}

.velocity-line svg {
  width: 100%;
  height: 100%;
}

/* Accuracy Meter */
.accuracy-meter {
  width: 100%;
  margin-top: 6px;
}

.meter-bar {
  position: relative;
  height: 6px;
  background: linear-gradient(90deg, #F59E0B 0%, #10B981 50%, #F59E0B 100%);
  border-radius: 3px;
}

.meter-target {
  position: absolute;
  left: 50%;
  top: -2px;
  width: 2px;
  height: 10px;
  background: #1F2937;
  transform: translateX(-50%);
}

.meter-indicator {
  position: absolute;
  top: -2px;
  width: 8px;
  height: 10px;
  background: #1F2937;
  border-radius: 2px;
  transform: translateX(-50%);
}

.meter-labels {
  display: flex;
  justify-content: space-between;
  font-size: 8px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

/* Burndown Chart */
.burndown-chart {
  height: 50px;
  position: relative;
}

.burndown-chart svg {
  width: 100%;
  height: 100%;
}

.burndown-chart .chart-labels {
  display: flex;
  justify-content: space-between;
  font-size: 8px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

/* Frequency Trend */
.frequency-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
}

.trend-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
}

.trend-icon {
  display: flex;
  align-items: center;
}

.trend-icon.up {
  color: #10B981;
}

.trend-icon.down {
  color: #EF4444;
}

.trend-value {
  font-size: 10px;
  font-weight: 600;
  color: #10B981;
}

/* Scope Info */
.scope-info {
  display: flex;
  gap: 4px;
  font-size: 10px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

/* Blocked Status */
.blocked-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: #EF4444;
  margin-top: 4px;
}

/* Custom Tooltip Styling */
:deep(.el-tooltip__popper) {
  max-width: 280px;
  font-size: 13px;
  line-height: 1.6;
}

:deep(.el-tooltip__popper.is-dark) {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 10px;
  padding: 12px 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

:deep(.el-tooltip__popper.is-dark .el-tooltip__arrow::before) {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(59, 130, 246, 0.3);
}

:deep(.el-tooltip__popper.is-dark) .el-tooltip__arrow::after {
  border-left-color: #16213e;
}
</style>
