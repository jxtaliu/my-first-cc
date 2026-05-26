<template>
  <div class="milestones-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('project.milestones') }}</h1>
        <p class="pm-text-small">{{ $t('project.milestonesDesc') }}</p>
      </div>
      <div class="header-actions">
        <el-select v-model="filterStatus" :placeholder="$t('project.filterByStatus')" clearable style="width: 150px">
          <el-option :label="$t('project.allMilestones')" :value="null" />
          <el-option :label="$t('project.planning')" value="PLANNING" />
          <el-option :label="$t('project.inProgress')" value="IN_PROGRESS" />
          <el-option :label="$t('project.completed')" value="COMPLETED" />
        </el-select>
        <el-button type="primary" @click="onCreateMilestone">
          <el-icon><Plus /></el-icon>
          {{ $t('project.createMilestone') }}
        </el-button>
      </div>
    </div>

    <!-- Timeline View -->
    <div class="milestones-timeline-section">
      <div class="section-title">{{ $t('project.timeline') }}</div>
      <div class="timeline-container" v-loading="loading">
        <div class="timeline-wrapper">
          <!-- Date labels above line -->
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
            <!-- Main horizontal line -->
            <div class="axis-line"></div>
            <!-- Today indicator -->
            <div class="today-marker" :style="{ left: todayPosition + '%' }">
              <div class="today-flag"></div>
              <span class="today-text">{{ $t('project.today') }}</span>
            </div>
            <!-- Milestone dots on the line -->
            <div
              v-for="milestone in milestones"
              :key="milestone.id"
              class="milestone-dot-wrapper"
              :class="'status-' + (milestone.status || 'planning').toLowerCase()"
              :style="{ left: getMilestonePosition(milestone) + '%' }"
              @click="onEditMilestone(milestone)"
            >
              <div class="milestone-line-connector"></div>
              <div class="milestone-circle"></div>
              <div class="milestone-label">
                <span class="label-name">{{ milestone.name }}</span>
                <span class="label-date">({{ formatDate(milestone.targetDate) }})</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Milestones Cards Grid -->
    <div class="milestones-cards-section">
      <div class="section-title">{{ $t('project.milestones') }}</div>
      <div class="milestones-grid" v-loading="loading">
        <MilestoneCard
          v-for="milestone in filteredMilestones"
          :key="milestone.id"
          :milestone="milestone"
          @edit="onEditMilestone"
          @delete="onDeleteMilestone"
          @complete="onMarkComplete"
        />
        <el-empty v-if="filteredMilestones.length === 0 && !loading" :description="$t('project.noMilestones')" />
      </div>
    </div>

    <!-- Gantt View -->
    <div class="milestones-gantt-section">
      <div class="section-title">{{ $t('project.ganttView') }}</div>
      <div class="gantt-container" v-loading="loading">
        <div class="gantt-header">
          <div class="gantt-label-col">{{ $t('project.milestoneName') }}</div>
          <div class="gantt-timeline-area">
            <div class="gantt-timeline-header">
              <span
                v-for="period in timelinePeriods"
                :key="period.key"
                class="period-label"
                :style="{ left: period.percent + '%' }"
              >
                {{ period.label }}
              </span>
            </div>
          </div>
        </div>
        <div class="gantt-body">
          <div
            v-for="milestone in filteredMilestones"
            :key="milestone.id"
            class="gantt-row"
          >
            <div class="gantt-label-col">{{ milestone.name }}</div>
            <div class="gantt-timeline-area">
              <div class="gantt-axis-line"></div>
              <div
                class="gantt-bar"
                :class="'status-' + (milestone.status || 'planning').toLowerCase()"
                :style="getGanttBarStyle(milestone)"
              >
                <span class="gantt-bar-label">{{ milestone.name }}</span>
                <span class="gantt-bar-date">{{ formatDate(milestone.targetDate) }}</span>
              </div>
            </div>
          </div>
          <div v-if="filteredMilestones.length === 0" class="gantt-empty">
            <el-empty :description="$t('project.noMilestones')" />
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Milestone Dialog -->
    <el-dialog
      v-model="showMilestoneDialog"
      :title="editingMilestone?.id ? $t('project.editMilestone') : $t('project.createMilestone')"
      width="500px"
      class="pm-dialog"
    >
      <el-form :model="milestoneForm" label-position="top">
        <el-form-item :label="$t('project.milestoneName')">
          <el-input v-model="milestoneForm.name" :placeholder="$t('project.milestoneNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('project.description')">
          <el-input
            v-model="milestoneForm.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('project.descriptionPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="$t('project.targetDate')">
          <el-date-picker
            v-model="milestoneForm.targetDate"
            type="date"
            :placeholder="$t('project.selectDate')"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMilestoneDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="onSaveMilestone">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import MilestoneCard from '@/components/charts/MilestoneCard.vue'
import { useProjectStore } from '@/stores/project'
import {
  getMilestonesByProject,
  createMilestone,
  updateMilestone,
  deleteMilestone,
  completeMilestone
} from '@/api/milestone'

const { t } = useI18n()
const projectStore = useProjectStore()

const milestones = ref([])
const loading = ref(false)
const showMilestoneDialog = ref(false)
const editingMilestone = ref(null)
const filterStatus = ref(null)

const milestoneForm = ref({
  name: '',
  description: '',
  targetDate: null
})

const filteredMilestones = computed(() => {
  let result = milestones.value
  if (filterStatus.value) {
    result = result.filter(m => m.status === filterStatus.value)
  }
  return result
})

// Timeline computed values based on actual milestone dates
const timelineRange = computed(() => {
  if (milestones.value.length === 0) {
    // Default: today +/- 30 days
    const today = new Date()
    return {
      start: new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000),
      end: new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000),
      totalDays: 60
    }
  }

  // Find min and max target dates from milestones
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
  const { start, end, totalDays } = timelineRange.value
  const markers = []

  // Determine interval based on total days
  // < 30 days: every 5 days, >= 30 days: every 10 days, >= 60 days: every 15 days
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

const timelinePeriods = computed(() => {
  const { start, end, totalDays } = timelineRange.value
  const periods = []
  let currentMonth = -1

  for (let i = 0; i <= totalDays; i++) {
    const date = new Date(start.getTime() + i * 24 * 60 * 60 * 1000)
    if (date.getMonth() !== currentMonth) {
      currentMonth = date.getMonth()
      const percent = (i / totalDays) * 100
      periods.push({
        key: i,
        label: `${date.getFullYear()}/${date.getMonth() + 1}`,
        percent: percent
      })
    }
  }
  return periods
})

const getMilestonePosition = (milestone) => {
  if (!milestone.targetDate) return 50
  const { start, totalDays } = timelineRange.value
  const target = new Date(milestone.targetDate)
  const daysDiff = Math.floor((target - start) / (24 * 60 * 60 * 1000))
  const percent = (daysDiff / totalDays) * 100
  return Math.max(0, Math.min(100, percent))
}

const todayPosition = computed(() => {
  const { start, totalDays } = timelineRange.value
  const today = new Date()
  const daysDiff = Math.floor((today - start) / (24 * 60 * 60 * 1000))
  const percent = (daysDiff / totalDays) * 100
  return Math.max(0, Math.min(100, percent))
})

const getGanttBarStyle = (milestone) => {
  if (!milestone.targetDate) return {}
  const { start, totalDays } = timelineRange.value
  const target = new Date(milestone.targetDate)
  const daysDiff = Math.floor((target - start) / (24 * 60 * 60 * 1000))
  const percent = (daysDiff / totalDays) * 100
  const clampedPercent = Math.max(0, Math.min(100, percent))
  const isOverdue = daysDiff < 0 && milestone.status !== 'COMPLETED'
  return {
    left: clampedPercent + '%',
    backgroundColor: milestone.status === 'COMPLETED' ? '#10B981' : isOverdue ? '#EF4444' : '#3B82F6'
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const loadMilestones = async () => {
  const projectId = projectStore.currentProjectId
  if (!projectId) {
    milestones.value = []
    return
  }
  loading.value = true
  try {
    const res = await getMilestonesByProject(projectId)
    milestones.value = res.data || []
  } catch (e) {
    console.error('Failed to load milestones:', e)
  } finally {
    loading.value = false
  }
}

const onCreateMilestone = () => {
  editingMilestone.value = null
  milestoneForm.value = {
    name: '',
    description: '',
    targetDate: null
  }
  showMilestoneDialog.value = true
}

const onEditMilestone = (milestone) => {
  editingMilestone.value = milestone
  milestoneForm.value = {
    name: milestone.name,
    description: milestone.description,
    targetDate: milestone.targetDate
  }
  showMilestoneDialog.value = true
}

const onSaveMilestone = async () => {
  if (!milestoneForm.value.name) {
    ElMessage.warning(t('project.milestoneNameRequired'))
    return
  }
  try {
    const data = {
      name: milestoneForm.value.name,
      description: milestoneForm.value.description,
      targetDate: milestoneForm.value.targetDate,
      projectId: projectStore.currentProjectId,
      status: editingMilestone.value ? undefined : 'PLANNING'
    }
    if (editingMilestone.value) {
      await updateMilestone(editingMilestone.value.id, data)
    } else {
      await createMilestone(data)
    }
    ElMessage.success(t('project.milestoneSaved'))
    showMilestoneDialog.value = false
    loadMilestones()
  } catch (e) {
    ElMessage.error(t('project.saveFailed'))
  }
}

const onMarkComplete = async (milestone) => {
  try {
    await ElMessageBox.confirm(
      t('project.confirmCompleteMilestone'),
      t('project.completeMilestone'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    await completeMilestone(milestone.id)
    ElMessage.success(t('project.milestoneCompleted'))
    loadMilestones()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('project.completeFailed'))
    }
  }
}

const onDeleteMilestone = async (milestone) => {
  try {
    await ElMessageBox.confirm(
      t('project.confirmDeleteMilestone'),
      t('project.deleteMilestone'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    await deleteMilestone(milestone.id)
    ElMessage.success(t('project.milestoneDeleted'))
    loadMilestones()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(t('project.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadMilestones()
})

watch(() => projectStore.currentProjectId, () => {
  loadMilestones()
})
</script>

<style scoped>
.milestones-page {
  max-width: 1400px;
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xl);
}

.pm-page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-actions {
  display: flex;
  gap: var(--pm-space-md);
  align-items: center;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--pm-text-secondary);
  margin-bottom: var(--pm-space-md);
}

/* Timeline Section */
.milestones-timeline-section {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
}

.timeline-container {
  overflow: hidden;
}

.timeline-wrapper {
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
  color: var(--pm-text-secondary);
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
  background: var(--pm-border);
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
  background: var(--pm-color-primary);
}

.today-text {
  font-size: 10px;
  color: var(--pm-color-primary);
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
  background: var(--pm-border);
}

.milestone-circle {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  margin-top: -1px;
}

.milestone-label {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 8px;
  white-space: nowrap;
  gap: 4px;
}

.label-name {
  font-size: 11px;
  font-weight: 600;
  color: var(--pm-text-primary);
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.label-date {
  font-size: 10px;
  color: var(--pm-text-secondary);
}

.milestone-dot-wrapper.status-planning .milestone-circle { background: #3B82F6; }
.milestone-dot-wrapper.status-in_progress .milestone-circle { background: #F59E0B; }
.milestone-dot-wrapper.status-completed .milestone-circle { background: #10B981; }

/* Cards Section */
.milestones-cards-section {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
}

.milestones-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--pm-space-lg);
}

/* Gantt Section */
.milestones-gantt-section {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
}

.gantt-container {
  overflow: hidden;
}

.gantt-header {
  display: flex;
  border-bottom: 1px solid var(--pm-border);
  padding-bottom: var(--pm-space-sm);
  margin-bottom: var(--pm-space-sm);
}

.gantt-label-col {
  width: 150px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--pm-text-secondary);
  padding-right: var(--pm-space-md);
}

.gantt-timeline-area {
  flex: 1;
  position: relative;
  height: 24px;
}

.gantt-timeline-header {
  position: relative;
  height: 20px;
}

.period-label {
  position: absolute;
  font-size: 11px;
  color: var(--pm-text-secondary);
  transform: translateX(-50%);
  top: 0;
}

.gantt-body {
  min-height: 100px;
}

.gantt-row {
  display: flex;
  align-items: center;
  padding: var(--pm-space-sm) 0;
  border-bottom: 1px solid var(--pm-border-light);
}

.gantt-row .gantt-label-col {
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gantt-axis-line {
  position: absolute;
  top: 11px;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--pm-border);
}

.gantt-bar {
  position: absolute;
  height: 22px;
  border-radius: 4px;
  padding: 0 8px;
  color: white;
  font-size: 12px;
  display: flex;
  align-items: center;
  transform: translateX(-50%);
  min-width: 80px;
  top: 1px;
}

.gantt-bar-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gantt-bar-date {
  font-size: 10px;
  opacity: 0.9;
  margin-left: 6px;
  white-space: nowrap;
}

.gantt-bar.status-planning { background: #3B82F6; }
.gantt-bar.status-in_progress { background: #F59E0B; }
.gantt-bar.status-completed { background: #10B981; }

.gantt-empty {
  padding: var(--pm-space-xl);
}
</style>
