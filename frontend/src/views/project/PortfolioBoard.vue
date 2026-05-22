<template>
  <div class="portfolio-board-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('portfolio.title') }}</h1>
        <p class="pm-text-small">{{ $t('portfolio.description') }}</p>
      </div>
      <div class="portfolio-actions">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('portfolio.searchProjects')"
          prefix-icon="Search"
          style="width: 240px"
          clearable
        />
        <el-select v-model="filterStatus" style="width: 140px">
          <el-option :label="$t('project.all')" value="" />
          <el-option :label="$t('project.active')" value="active" />
          <el-option :label="$t('project.planning')" value="planning" />
          <el-option :label="$t('project.completed')" value="completed" />
          <el-option :label="$t('project.paused')" value="paused" />
        </el-select>
        <el-select v-model="filterDepartment" style="width: 140px">
          <el-option :label="$t('portfolio.allDepartments')" value="" />
          <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
        </el-select>
        <el-button-group>
          <el-button :type="viewMode === 'grid' ? 'primary' : ''" @click="viewMode = 'grid'">
            <el-icon><Grid /></el-icon>
          </el-button>
          <el-button :type="viewMode === 'list' ? 'primary' : ''" @click="viewMode = 'list'">
            <el-icon><List /></el-icon>
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- Sort Options -->
    <div class="portfolio-sort">
      <span class="sort-label">{{ $t('portfolio.sortBy') }}:</span>
      <el-radio-group v-model="sortBy" size="small">
        <el-radio-button value="name">{{ $t('portfolio.name') }}</el-radio-button>
        <el-radio-button value="progress">{{ $t('portfolio.progress') }}</el-radio-button>
        <el-radio-button value="health">{{ $t('portfolio.health') }}</el-radio-button>
        <el-radio-button value="updated">{{ $t('portfolio.recentlyUpdated') }}</el-radio-button>
      </el-radio-group>
      <el-button text size="small" @click="sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'">
        <el-icon>
          <component :is="sortOrder === 'asc' ? 'SortUp' : 'SortDown'" />
        </el-icon>
      </el-button>
    </div>

    <!-- Portfolio Summary -->
    <div class="portfolio-summary">
      <div class="summary-card">
        <span class="summary-value">{{ filteredProjects.length }}</span>
        <span class="summary-label">{{ $t('portfolio.totalProjects') }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-value">{{ activeProjects }}</span>
        <span class="summary-label">{{ $t('portfolio.activeProjects') }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-value">{{ onTrackProjects }}</span>
        <span class="summary-label">{{ $t('portfolio.onTrackProjects') }}</span>
      </div>
      <div class="summary-card">
        <span class="summary-value">{{ atRiskProjects }}</span>
        <span class="summary-label">{{ $t('portfolio.atRiskProjects') }}</span>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="portfolio-loading">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- Empty State -->
    <el-empty
      v-else-if="!filteredProjects.length"
      :description="$t('common.noData')"
    />

    <!-- Grid View -->
    <div v-else-if="viewMode === 'grid'" class="portfolio-grid">
      <div
        v-for="project in filteredProjects"
        :key="project.id"
        class="project-card"
        @click="navigateToProject(project)"
      >
        <!-- Health Indicator -->
        <div class="project-health-badge" :class="project.health">
          <el-icon><component :is="getHealthIcon(project.health)" /></el-icon>
          {{ getHealthLabel(project.health) }}
        </div>

        <!-- Project Header -->
        <div class="project-card-header">
          <div class="project-type-badge" :class="project.type.toLowerCase()">
            {{ project.type }}
          </div>
          <el-dropdown trigger="click" @click.stop>
            <el-button text size="small">
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click.stop="navigateToProject(project)">
                  <el-icon><View /></el-icon>
                  {{ $t('portfolio.viewDetails') }}
                </el-dropdown-item>
                <el-dropdown-item @click.stop="toggleFavorite(project)">
                  <el-icon><Star /></el-icon>
                  {{ isFavorite(project.id) ? $t('portfolio.unfavorite') : $t('portfolio.favorite') }}
                </el-dropdown-item>
                <el-dropdown-item divided @click.stop="openQuickActions(project)">
                  <el-icon><Setting /></el-icon>
                  {{ $t('portfolio.quickActions') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- Project Info -->
        <h3 class="project-card-name">{{ project.name }}</h3>
        <p class="project-card-description">{{ project.description || $t('project.noDescription') }}</p>

        <!-- Progress -->
        <div class="project-progress-section">
          <div class="project-progress-header">
            <span class="project-progress-label">{{ $t('portfolio.progress') }}</span>
            <span class="project-progress-value">{{ project.progress }}%</span>
          </div>
          <div class="pm-progress">
            <div
              class="pm-progress-bar"
              :class="getProgressClass(project.progress)"
              :style="{ width: project.progress + '%' }"
            ></div>
          </div>
        </div>

        <!-- Metrics -->
        <div class="project-metrics">
          <div class="metric-item">
            <el-icon><Document /></el-icon>
            <span>{{ project.taskCount || 0 }}</span>
          </div>
          <div class="metric-item">
            <el-icon><User /></el-icon>
            <span>{{ project.memberCount || 0 }}</span>
          </div>
          <div class="metric-item">
            <el-icon><Clock /></el-icon>
            <span>{{ formatTimeAgo(project.updatedAt) }}</span>
          </div>
        </div>

        <!-- Team Avatars -->
        <div class="project-team">
          <div class="team-avatars">
            <el-avatar
              v-for="(member, index) in project.members?.slice(0, 4)"
              :key="member.id"
              :size="28"
              :src="member.avatar"
              :style="{ marginLeft: index > 0 ? '-8px' : '0', zIndex: 10 - index }"
            >
              {{ member.name?.charAt(0) }}
            </el-avatar>
            <el-avatar
              v-if="project.members?.length > 4"
              :size="28"
              class="more-members"
              :style="{ marginLeft: '-8px', zIndex: 0 }"
            >
              +{{ project.members.length - 4 }}
            </el-avatar>
          </div>
          <div class="project-status-badge" :class="project.status">
            {{ getStatusLabel(project.status) }}
          </div>
        </div>
      </div>
    </div>

    <!-- List View -->
    <div v-else class="portfolio-list">
      <el-table :data="filteredProjects" @row-click="navigateToProject" stripe>
        <el-table-column :label="$t('project.name')" min-width="200">
          <template #default="{ row }">
            <div class="project-list-name">
              <span class="project-list-status" :style="{ background: getStatusColor(row.status) }"></span>
              <span class="project-list-title">{{ row.name }}</span>
              <el-tag size="small" :type="row.type === 'SCRUM' ? 'success' : 'warning'">
                {{ row.type }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('portfolio.department')" width="140">
          <template #default="{ row }">
            {{ row.department?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('portfolio.progress')" width="160">
          <template #default="{ row }">
            <div class="project-list-progress">
              <el-progress
                :percentage="row.progress"
                :color="getProgressColor(row.progress)"
                :show-text="false"
                style="width: 100px"
              />
              <span>{{ row.progress }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('portfolio.health')" width="100">
          <template #default="{ row }">
            <el-tag :type="getHealthTagType(row.health)" size="small">
              {{ getHealthLabel(row.health) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('portfolio.tasks')" width="80" align="center">
          <template #default="{ row }">
            {{ row.taskCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('portfolio.members')" width="100" align="center">
          <template #default="{ row }">
            <div class="team-avatars-list">
              <el-avatar
                v-for="(member, index) in row.members?.slice(0, 3)"
                :key="member.id"
                :size="24"
                :src="member.avatar"
                :style="{ marginLeft: index > 0 ? '-6px' : '0' }"
              >
                {{ member.name?.charAt(0) }}
              </el-avatar>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('portfolio.updated')" width="120">
          <template #default="{ row }">
            {{ formatTimeAgo(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.actions')" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click.stop="navigateToProject(row)">
              <el-icon><View /></el-icon>
            </el-button>
            <el-button type="danger" text size="small" @click.stop="toggleFavorite(row)">
              <el-icon>
                <component :is="isFavorite(row.id) ? 'Star' : 'StarFilled'" />
              </el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  Grid,
  List,
  Search,
  More,
  View,
  Star,
  StarFilled,
  Setting,
  Document,
  User,
  Clock,
  SortUp,
  SortDown,
  CircleCheck,
  Warning,
  Clock as ClockIcon
} from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()

// State
const loading = ref(false)
const projects = ref([])
const searchQuery = ref('')
const filterStatus = ref('')
const filterDepartment = ref('')
const sortBy = ref('name')
const sortOrder = ref('asc')
const viewMode = ref('grid')
const favorites = ref(new Set())
const departments = ref([
  { id: 1, name: 'Engineering' },
  { id: 2, name: 'Product' },
  { id: 3, name: 'Design' },
  { id: 4, name: 'Marketing' }
])

// Computed
const filteredProjects = computed(() => {
  let result = [...projects.value]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(p =>
      p.name.toLowerCase().includes(query) ||
      p.description?.toLowerCase().includes(query)
    )
  }

  // Status filter
  if (filterStatus.value) {
    result = result.filter(p => p.status === filterStatus.value)
  }

  // Department filter
  if (filterDepartment.value) {
    result = result.filter(p => p.department?.id === filterDepartment.value)
  }

  // Sort
  result.sort((a, b) => {
    let comparison = 0
    switch (sortBy.value) {
      case 'name':
        comparison = a.name.localeCompare(b.name)
        break
      case 'progress':
        comparison = a.progress - b.progress
        break
      case 'health':
        const healthOrder = { healthy: 3, at_risk: 2, critical: 1 }
        comparison = (healthOrder[a.health] || 0) - (healthOrder[b.health] || 0)
        break
      case 'updated':
        comparison = new Date(b.updatedAt) - new Date(a.updatedAt)
        break
    }
    return sortOrder.value === 'asc' ? comparison : -comparison
  })

  return result
})

const activeProjects = computed(() => {
  return projects.value.filter(p => p.status === 'active').length
})

const onTrackProjects = computed(() => {
  return projects.value.filter(p => p.health === 'healthy').length
})

const atRiskProjects = computed(() => {
  return projects.value.filter(p => p.health === 'at_risk' || p.health === 'critical').length
})

// Methods
const getHealthIcon = (health) => {
  switch (health) {
    case 'healthy': return 'CircleCheck'
    case 'at_risk': return 'Warning'
    case 'critical': return 'ClockIcon'
    default: return 'CircleCheck'
  }
}

const getHealthLabel = (health) => {
  switch (health) {
    case 'healthy': return t('portfolio.healthy')
    case 'at_risk': return t('portfolio.atRisk')
    case 'critical': return t('portfolio.critical')
    default: return health
  }
}

const getHealthTagType = (health) => {
  switch (health) {
    case 'healthy': return 'success'
    case 'at_risk': return 'warning'
    case 'critical': return 'danger'
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

const getStatusColor = (status) => {
  switch (status) {
    case 'active': return '#10B981'
    case 'planning': return '#3B82F6'
    case 'completed': return '#8B5CF6'
    case 'paused': return '#F59E0B'
    default: return '#94A3B8'
  }
}

const getProgressClass = (progress) => {
  if (progress >= 80) return 'success'
  if (progress >= 50) return 'warning'
  return 'danger'
}

const getProgressColor = (progress) => {
  if (progress >= 80) return '#10B981'
  if (progress >= 50) return '#F59E0B'
  return '#EF4444'
}

const formatTimeAgo = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffDays === 0) return t('notification.justNow')
  if (diffDays === 1) return t('portfolio.yesterday')
  if (diffDays < 7) return t('notification.daysAgo', { days: diffDays })
  if (diffDays < 30) return t('portfolio.weeksAgo', { weeks: Math.floor(diffDays / 7) })
  return t('portfolio.monthsAgo', { months: Math.floor(diffDays / 30) })
}

const isFavorite = (projectId) => {
  return favorites.value.has(projectId)
}

const toggleFavorite = (project) => {
  if (favorites.value.has(project.id)) {
    favorites.value.delete(project.id)
  } else {
    favorites.value.add(project.id)
  }
  ElMessage.success(
    favorites.value.has(project.id)
      ? t('portfolio.addedToFavorites')
      : t('portfolio.removedFromFavorites')
  )
}

const navigateToProject = (project) => {
  router.push(`/projects/${project.id}`)
}

const openQuickActions = (project) => {
  ElMessage.info(t('portfolio.quickActionsComingSoon'))
}

// Load projects
const loadProjects = async () => {
  loading.value = true
  try {
    // In production, this would call the API
    // const res = await getProjects()
    // projects.value = res.data || []

    // Mock data for demo
    projects.value = [
      {
        id: 1,
        name: 'SME-PM系统',
        description: '中小企业工时与项目管理工具',
        type: 'SCRUM',
        status: 'active',
        health: 'healthy',
        progress: 85,
        taskCount: 48,
        memberCount: 6,
        updatedAt: new Date(Date.now() - 86400000).toISOString(),
        department: { id: 1, name: 'Engineering' },
        members: [
          { id: 1, name: '张三', avatar: '' },
          { id: 2, name: '李四', avatar: '' },
          { id: 3, name: '王五', avatar: '' },
          { id: 4, name: '赵六', avatar: '' },
          { id: 5, name: '孙七', avatar: '' }
        ]
      },
      {
        id: 2,
        name: '客户CRM项目',
        description: '客户关系管理系统开发',
        type: 'SCRUM',
        status: 'active',
        health: 'at_risk',
        progress: 62,
        taskCount: 35,
        memberCount: 4,
        updatedAt: new Date(Date.now() - 172800000).toISOString(),
        department: { id: 1, name: 'Engineering' },
        members: [
          { id: 2, name: '李四', avatar: '' },
          { id: 3, name: '王五', avatar: '' },
          { id: 6, name: '周八', avatar: '' }
        ]
      },
      {
        id: 3,
        name: '电商平台',
        description: 'B2C电商平台重构',
        type: 'KANBAN',
        status: 'planning',
        health: 'healthy',
        progress: 25,
        taskCount: 18,
        memberCount: 5,
        updatedAt: new Date(Date.now() - 3600000).toISOString(),
        department: { id: 2, name: 'Product' },
        members: [
          { id: 1, name: '张三', avatar: '' },
          { id: 4, name: '赵六', avatar: '' },
          { id: 7, name: '吴九', avatar: '' }
        ]
      },
      {
        id: 4,
        name: '物流系统',
        description: '智能物流调度系统',
        type: 'SCRUM',
        status: 'paused',
        health: 'critical',
        progress: 45,
        taskCount: 28,
        memberCount: 3,
        updatedAt: new Date(Date.now() - 604800000).toISOString(),
        department: { id: 1, name: 'Engineering' },
        members: [
          { id: 2, name: '李四', avatar: '' },
          { id: 8, name: '郑十', avatar: '' }
        ]
      },
      {
        id: 5,
        name: '品牌官网',
        description: '公司官网改版升级',
        type: 'KANBAN',
        status: 'completed',
        health: 'healthy',
        progress: 100,
        taskCount: 22,
        memberCount: 3,
        updatedAt: new Date(Date.now() - 2592000000).toISOString(),
        department: { id: 3, name: 'Design' },
        members: [
          { id: 9, name: '钱一', avatar: '' },
          { id: 10, name: '大二', avatar: '' }
        ]
      }
    ]
  } catch (error) {
    console.error('Failed to load projects:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.portfolio-board-page {
  max-width: 1600px;
}

.pm-page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--pm-space-lg);
}

.portfolio-actions {
  display: flex;
  gap: var(--pm-space-md);
  align-items: center;
}

/* Sort Options */
.portfolio-sort {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-xl);
  padding: var(--pm-space-md) var(--pm-space-lg);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-md);
}

.sort-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-secondary);
}

/* Portfolio Summary */
.portfolio-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--pm-space-lg);
  margin-bottom: var(--pm-space-2xl);
}

.summary-card {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--pm-space-sm);
  transition: all var(--pm-transition-normal);
}

.summary-card:hover {
  box-shadow: var(--pm-shadow-md);
  transform: translateY(-2px);
}

.summary-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--pm-primary);
  font-family: 'Plus Jakarta Sans', sans-serif;
}

.summary-label {
  font-size: 13px;
  color: var(--pm-text-secondary);
}

/* Portfolio Grid */
.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--pm-space-lg);
}

.project-card {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
  cursor: pointer;
  transition: all var(--pm-transition-normal);
  position: relative;
}

.project-card:hover {
  border-color: var(--pm-accent);
  box-shadow: var(--pm-shadow-lg);
  transform: translateY(-4px);
}

/* Health Badge */
.project-health-badge {
  position: absolute;
  top: var(--pm-space-lg);
  right: var(--pm-space-lg);
  display: flex;
  align-items: center;
  gap: var(--pm-space-xs);
  padding: 2px 8px;
  border-radius: var(--pm-radius-sm);
  font-size: 11px;
  font-weight: 600;
}

.project-health-badge.healthy {
  background: rgba(16, 185, 129, 0.1);
  color: var(--pm-status-done);
}

.project-health-badge.at_risk {
  background: rgba(245, 158, 11, 0.1);
  color: var(--pm-status-review);
}

.project-health-badge.critical {
  background: rgba(239, 68, 68, 0.1);
  color: var(--pm-status-blocked);
}

/* Project Card Header */
.project-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-md);
}

.project-type-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--pm-radius-xs);
  text-transform: uppercase;
}

.project-type-badge.scrum {
  background: rgba(16, 185, 129, 0.1);
  color: var(--pm-status-done);
}

.project-type-badge.kanban {
  background: rgba(59, 130, 246, 0.1);
  color: var(--pm-status-in-progress);
}

.project-card-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin: 0 0 var(--pm-space-sm) 0;
  padding-right: 60px;
}

.project-card-description {
  font-size: 13px;
  color: var(--pm-text-secondary);
  margin: 0 0 var(--pm-space-lg) 0;
  line-height: 1.5;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* Progress Section */
.project-progress-section {
  margin-bottom: var(--pm-space-lg);
}

.project-progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-xs);
}

.project-progress-label {
  font-size: 12px;
  color: var(--pm-text-secondary);
}

.project-progress-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--pm-text-primary);
}

/* Metrics */
.project-metrics {
  display: flex;
  gap: var(--pm-space-lg);
  margin-bottom: var(--pm-space-lg);
  padding-bottom: var(--pm-space-lg);
  border-bottom: 1px solid var(--pm-border-light);
}

.metric-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-xs);
  font-size: 13px;
  color: var(--pm-text-secondary);
}

.metric-item .el-icon {
  font-size: 14px;
  color: var(--pm-text-muted);
}

/* Team Section */
.project-team {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team-avatars {
  display: flex;
}

.team-avatars .el-avatar {
  border: 2px solid var(--pm-card);
}

.more-members {
  background: var(--pm-background);
  color: var(--pm-text-secondary);
  font-size: 10px;
}

.project-status-badge {
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: var(--pm-radius-sm);
}

.project-status-badge.active {
  background: rgba(16, 185, 129, 0.1);
  color: var(--pm-status-done);
}

.project-status-badge.planning {
  background: rgba(59, 130, 246, 0.1);
  color: var(--pm-status-in-progress);
}

.project-status-badge.completed {
  background: rgba(139, 92, 246, 0.1);
  color: #8B5CF6;
}

.project-status-badge.paused {
  background: rgba(245, 158, 11, 0.1);
  color: var(--pm-status-review);
}

/* List View */
.portfolio-list {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  overflow: hidden;
}

.project-list-name {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.project-list-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.project-list-title {
  font-weight: 500;
  color: var(--pm-text-primary);
}

.project-list-progress {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  font-size: 13px;
  color: var(--pm-text-secondary);
}

.team-avatars-list {
  display: flex;
}

.team-avatars-list .el-avatar {
  border: 2px solid var(--pm-card);
}

/* Loading State */
.portfolio-loading {
  padding: var(--pm-space-xl);
}

@media (max-width: 1200px) {
  .portfolio-summary {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .pm-page-header {
    flex-direction: column;
    gap: var(--pm-space-lg);
  }

  .portfolio-actions {
    flex-wrap: wrap;
    width: 100%;
  }

  .portfolio-summary {
    grid-template-columns: 1fr;
  }

  .portfolio-grid {
    grid-template-columns: 1fr;
  }
}
</style>
