<template>
  <div class="project-compare-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('projectCompare.title') }}</h1>
        <p class="pm-text-small">{{ $t('projectCompare.description') }}</p>
      </div>
      <div class="compare-actions">
        <el-select
          v-model="selectedProjects"
          multiple
          :placeholder="$t('projectCompare.selectProjects')"
          style="width: 320px"
          @change="handleProjectChange"
        >
          <el-option
            v-for="project in availableProjects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
        <el-select v-model="timeRange" style="width: 140px">
          <el-option :label="$t('project.thisWeek')" value="week" />
          <el-option :label="$t('project.thisMonth')" value="month" />
          <el-option :label="$t('project.thisQuarter')" value="quarter" />
          <el-option :label="$t('project.thisYear')" value="year" />
        </el-select>
      </div>
    </div>

    <!-- Comparison Cards -->
    <div class="compare-metrics-grid" v-if="selectedProjects.length >= 2">
      <!-- Completion Rate -->
      <div class="compare-card">
        <div class="compare-card-header">
          <span class="compare-card-title">{{ $t('projectCompare.completionRate') }}</span>
          <el-icon class="compare-card-icon"><PieChart /></el-icon>
        </div>
        <div class="compare-card-chart">
          <BarChart
            :data="completionRateData"
            :labels="completionRateLabels"
            :colors="chartColors"
          />
        </div>
      </div>

      <!-- Work Efficiency -->
      <div class="compare-card">
        <div class="compare-card-header">
          <span class="compare-card-title">{{ $t('projectCompare.workEfficiency') }}</span>
          <el-icon class="compare-card-icon"><Odometer /></el-icon>
        </div>
        <div class="compare-card-chart">
          <BarChart
            :data="workEfficiencyData"
            :labels="workEfficiencyLabels"
            :colors="chartColors"
            :showAverage="true"
          />
        </div>
      </div>

      <!-- Defect Density -->
      <div class="compare-card">
        <div class="compare-card-header">
          <span class="compare-card-title">{{ $t('projectCompare.defectDensity') }}</span>
          <el-icon class="compare-card-icon"><Warning /></el-icon>
        </div>
        <div class="compare-card-chart">
          <BarChart
            :data="defectDensityData"
            :labels="defectDensityLabels"
            :colors="chartColors"
            :invertColor="true"
          />
        </div>
      </div>

      <!-- Team Throughput -->
      <div class="compare-card">
        <div class="compare-card-header">
          <span class="compare-card-title">{{ $t('projectCompare.teamThroughput') }}</span>
          <el-icon class="compare-card-icon"><TrendCharts /></el-icon>
        </div>
        <div class="compare-card-chart">
          <BarChart
            :data="teamThroughputData"
            :labels="teamThroughputLabels"
            :colors="chartColors"
          />
        </div>
      </div>

      <!-- Milestone Achievement -->
      <div class="compare-card">
        <div class="compare-card-header">
          <span class="compare-card-title">{{ $t('projectCompare.milestoneAchievement') }}</span>
          <el-icon class="compare-card-icon"><Flag /></el-icon>
        </div>
        <div class="compare-card-chart">
          <BarChart
            :data="milestoneAchievementData"
            :labels="milestoneAchievementLabels"
            :colors="chartColors"
          />
        </div>
      </div>

      <!-- Schedule Performance -->
      <div class="compare-card">
        <div class="compare-card-header">
          <span class="compare-card-title">{{ $t('projectCompare.schedulePerformance') }}</span>
          <el-icon class="compare-card-icon"><Timer /></el-icon>
        </div>
        <div class="compare-card-chart">
          <BarChart
            :data="schedulePerformanceData"
            :labels="schedulePerformanceLabels"
            :colors="chartColors"
          />
        </div>
      </div>
    </div>

    <!-- Radar Chart for Overall Comparison -->
    <div class="compare-radar-section" v-if="selectedProjects.length >= 2">
      <div class="pm-card compare-radar-card">
        <div class="compare-radar-header">
          <h3 class="compare-section-title">{{ $t('projectCompare.overallComparison') }}</h3>
          <el-tag type="info">{{ $t('projectCompare.radarHint') }}</el-tag>
        </div>
        <div class="compare-radar-chart">
          <RadarChart :data="radarData" :labels="radarLabels" :colors="chartColors" />
        </div>
      </div>
    </div>

    <!-- Project Selection Guide -->
    <div v-if="selectedProjects.length < 2" class="compare-guide">
      <div class="compare-guide-icon">
        <el-icon><DataAnalysis /></el-icon>
      </div>
      <h3 class="compare-guide-title">{{ $t('projectCompare.selectAtLeastTwo') }}</h3>
      <p class="compare-guide-text">{{ $t('projectCompare.selectHint') }}</p>
    </div>

    <!-- Detailed Comparison Table -->
    <div class="compare-table-section" v-if="selectedProjects.length >= 2">
      <div class="pm-card">
        <div class="compare-table-header">
          <h3 class="compare-section-title">{{ $t('projectCompare.detailedMetrics') }}</h3>
        </div>
        <div class="compare-table-wrapper">
          <table class="pm-table">
            <thead>
              <tr>
                <th>{{ $t('projectCompare.metric') }}</th>
                <th v-for="project in selectedProjectDetails" :key="project.id">
                  <div class="project-header-cell">
                    <span class="project-status-indicator" :style="{ background: project.statusColor }"></span>
                    {{ project.name }}
                  </div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="metric in detailedMetrics" :key="metric.key">
                <td class="metric-name-cell">{{ metric.label }}</td>
                <td v-for="project in selectedProjectDetails" :key="project.id">
                  <span class="metric-value" :class="metric.class">
                    {{ getMetricValue(project, metric.key) }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Insights Section -->
    <div class="compare-insights-section" v-if="selectedProjects.length >= 2">
      <h3 class="compare-section-title">{{ $t('projectCompare.insights') }}</h3>
      <div class="insights-grid">
        <div
          v-for="insight in insights"
          :key="insight.id"
          class="insight-card"
          :class="insight.type"
        >
          <div class="insight-icon">
            <el-icon>
              <component :is="insight.icon" />
            </el-icon>
          </div>
          <div class="insight-content">
            <span class="insight-title">{{ insight.title }}</span>
            <span class="insight-description">{{ insight.description }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  PieChart,
  Odometer,
  Warning,
  TrendCharts,
  Flag,
  Timer,
  DataAnalysis
} from '@element-plus/icons-vue'
import BarChart from '@/components/charts/BarChart.vue'
import RadarChart from '@/components/charts/RadarChart.vue'

const { t } = useI18n()

// State
const selectedProjects = ref([])
const timeRange = ref('month')
const loading = ref(false)

// Chart colors
const chartColors = ['#00D4AA', '#3B82F6', '#F59E0B', '#8B5CF6', '#EF4444', '#10B981']

// Available projects
const availableProjects = ref([
  { id: 1, name: 'SME-PM系统', statusColor: '#10B981' },
  { id: 2, name: '客户CRM项目', statusColor: '#3B82F6' },
  { id: 3, name: '电商平台', statusColor: '#F59E0B' },
  { id: 4, name: '物流系统', statusColor: '#8B5CF6' }
])

// Selected project details
const selectedProjectDetails = computed(() => {
  return selectedProjects.value.map(id => {
    const project = availableProjects.value.find(p => p.id === id)
    return {
      ...project,
      completionRate: Math.random() * 40 + 60,
      workEfficiency: Math.random() * 0.6 + 0.7,
      defectDensity: Math.random() * 3 + 0.5,
      teamThroughput: Math.floor(Math.random() * 20 + 10),
      milestoneAchievement: Math.random() * 30 + 70,
      schedulePerformance: Math.random() * 20 + 80
    }
  })
})

// Bar chart data for completion rate
const completionRateData = computed(() => {
  return selectedProjectDetails.value.map(p => p.completionRate.toFixed(1))
})

const completionRateLabels = computed(() => {
  return selectedProjectDetails.value.map(p => p.name)
})

// Work efficiency
const workEfficiencyData = computed(() => {
  return selectedProjectDetails.value.map(p => p.workEfficiency.toFixed(2))
})

const workEfficiencyLabels = computed(() => {
  return selectedProjectDetails.value.map(p => p.name)
})

// Defect density
const defectDensityData = computed(() => {
  return selectedProjectDetails.value.map(p => p.defectDensity.toFixed(2))
})

const defectDensityLabels = computed(() => {
  return selectedProjectDetails.value.map(p => p.name)
})

// Team throughput
const teamThroughputData = computed(() => {
  return selectedProjectDetails.value.map(p => p.teamThroughput)
})

const teamThroughputLabels = computed(() => {
  return selectedProjectDetails.value.map(p => p.name)
})

// Milestone achievement
const milestoneAchievementData = computed(() => {
  return selectedProjectDetails.value.map(p => p.milestoneAchievement.toFixed(1))
})

const milestoneAchievementLabels = computed(() => {
  return selectedProjectDetails.value.map(p => p.name)
})

// Schedule performance
const schedulePerformanceData = computed(() => {
  return selectedProjectDetails.value.map(p => p.schedulePerformance.toFixed(1))
})

const schedulePerformanceLabels = computed(() => {
  return selectedProjectDetails.value.map(p => p.name)
})

// Radar chart data
const radarData = computed(() => {
  const metrics = ['completionRate', 'workEfficiency', 'defectDensity', 'teamThroughput', 'milestoneAchievement', 'schedulePerformance']
  return selectedProjectDetails.value.map(project => {
    return metrics.map(metric => {
      const value = project[metric]
      // Normalize to 0-100 scale
      if (metric === 'defectDensity') {
        return Math.max(0, 100 - value * 20)
      }
      if (metric === 'workEfficiency') {
        return value * 80
      }
      return value
    })
  })
})

const radarLabels = computed(() => [
  t('projectCompare.completionRate'),
  t('projectCompare.workEfficiency'),
  t('projectCompare.quality'),
  t('projectCompare.teamThroughput'),
  t('projectCompare.milestoneAchievement'),
  t('projectCompare.schedulePerformance')
])

// Detailed metrics
const detailedMetrics = computed(() => [
  { key: 'completionRate', label: t('projectCompare.completionRate'), format: 'percent', class: 'metric-good' },
  { key: 'workEfficiency', label: t('projectCompare.workEfficiency'), format: ' multiplier', class: 'metric-good' },
  { key: 'defectDensity', label: t('projectCompare.defectDensity'), format: '', class: 'metric-bad' },
  { key: 'teamThroughput', label: t('projectCompare.teamThroughput'), format: ' tasks', class: '' },
  { key: 'milestoneAchievement', label: t('projectCompare.milestoneAchievement'), format: 'percent', class: 'metric-good' },
  { key: 'schedulePerformance', label: t('projectCompare.schedulePerformance'), format: 'percent', class: 'metric-good' }
])

// Get metric value for a project
const getMetricValue = (project, key) => {
  const value = project[key]
  if (key === 'completionRate' || key === 'milestoneAchievement' || key === 'schedulePerformance') {
    return value.toFixed(1) + '%'
  }
  if (key === 'workEfficiency') {
    return value.toFixed(2) + 'x'
  }
  if (key === 'defectDensity') {
    return value.toFixed(2)
  }
  if (key === 'teamThroughput') {
    return value + ' ' + t('projectCompare.tasks')
  }
  return value
}

// Insights
const insights = computed(() => {
  if (selectedProjectDetails.value.length < 2) return []

  const projects = selectedProjectDetails.value
  const bestCompletion = projects.reduce((a, b) => a.completionRate > b.completionRate ? a : b)
  const bestEfficiency = projects.reduce((a, b) => a.workEfficiency > b.workEfficiency ? a : b)
  const lowestDefects = projects.reduce((a, b) => a.defectDensity < b.defectDensity ? a : b)

  return [
    {
      id: 1,
      type: 'success',
      icon: 'Star',
      title: t('projectCompare.bestCompletion'),
      description: `${bestCompletion.name} ${t('projectCompare.hasHighest')} (${bestCompletion.completionRate.toFixed(1)}%)`
    },
    {
      id: 2,
      type: 'info',
      icon: 'TrendCharts',
      title: t('projectCompare.bestEfficiency'),
      description: `${bestEfficiency.name} ${t('projectCompare.hasHighest')} (${bestEfficiency.workEfficiency.toFixed(2)}x)`
    },
    {
      id: 3,
      type: 'warning',
      icon: 'Warning',
      title: t('projectCompare.lowestDefects'),
      description: `${lowestDefects.name} ${t('projectCompare.hasLowest')} (${lowestDefects.defectDensity.toFixed(2)})`
    }
  ]
})

// Handle project selection change
const handleProjectChange = () => {
  // Reload comparison data when selection changes
}

onMounted(() => {
  // Load available projects
})
</script>

<style scoped>
.project-compare-page {
  max-width: 1600px;
}

.pm-page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--pm-space-2xl);
}

.compare-actions {
  display: flex;
  gap: var(--pm-space-md);
}

/* Comparison Metrics Grid */
.compare-metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--pm-space-lg);
  margin-bottom: var(--pm-space-2xl);
}

.compare-card {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
  transition: all var(--pm-transition-normal);
}

.compare-card:hover {
  box-shadow: var(--pm-shadow-md);
  transform: translateY(-2px);
}

.compare-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-lg);
}

.compare-card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--pm-text-primary);
}

.compare-card-icon {
  font-size: 20px;
  color: var(--pm-accent);
}

.compare-card-chart {
  height: 200px;
}

/* Radar Chart Section */
.compare-radar-section {
  margin-bottom: var(--pm-space-2xl);
}

.compare-radar-card {
  padding: var(--pm-space-xl);
}

.compare-radar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-xl);
}

.compare-section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin: 0 0 var(--pm-space-lg) 0;
}

.compare-radar-chart {
  height: 400px;
}

/* Comparison Guide */
.compare-guide {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--pm-space-3xl);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  margin-bottom: var(--pm-space-2xl);
}

.compare-guide-icon {
  font-size: 48px;
  color: var(--pm-text-muted);
  margin-bottom: var(--pm-space-lg);
}

.compare-guide-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin: 0 0 var(--pm-space-sm) 0;
}

.compare-guide-text {
  font-size: 14px;
  color: var(--pm-text-secondary);
  margin: 0;
}

/* Detailed Comparison Table */
.compare-table-section {
  margin-bottom: var(--pm-space-2xl);
}

.compare-table-wrapper {
  overflow-x: auto;
}

.project-header-cell {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.project-status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.metric-name-cell {
  font-weight: 500;
  color: var(--pm-text-secondary);
}

.metric-value {
  font-weight: 600;
  font-size: 14px;
}

.metric-good {
  color: var(--pm-status-done);
}

.metric-warning {
  color: var(--pm-status-review);
}

.metric-bad {
  color: var(--pm-status-blocked);
}

/* Insights Section */
.compare-insights-section {
  margin-bottom: var(--pm-space-2xl);
}

.compare-insights-section .compare-section-title {
  margin-bottom: var(--pm-space-lg);
}

.insights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--pm-space-lg);
}

.insight-card {
  display: flex;
  align-items: flex-start;
  gap: var(--pm-space-md);
  padding: var(--pm-space-lg);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  border-left: 4px solid;
}

.insight-card.success {
  border-left-color: var(--pm-status-done);
  background: rgba(16, 185, 129, 0.05);
}

.insight-card.success .insight-icon {
  color: var(--pm-status-done);
}

.insight-card.info {
  border-left-color: var(--pm-status-in-progress);
  background: rgba(59, 130, 246, 0.05);
}

.insight-card.info .insight-icon {
  color: var(--pm-status-in-progress);
}

.insight-card.warning {
  border-left-color: var(--pm-status-review);
  background: rgba(245, 158, 11, 0.05);
}

.insight-card.warning .insight-icon {
  color: var(--pm-status-review);
}

.insight-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.insight-content {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xs);
}

.insight-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--pm-text-primary);
}

.insight-description {
  font-size: 13px;
  color: var(--pm-text-secondary);
}

@media (max-width: 1200px) {
  .compare-metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .compare-metrics-grid {
    grid-template-columns: 1fr;
  }

  .pm-page-header {
    flex-direction: column;
    gap: var(--pm-space-lg);
  }

  .compare-actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
