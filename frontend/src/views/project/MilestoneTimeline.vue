<template>
  <div class="milestone-timeline-page pm-page">
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('project.milestoneTimeline') }}</h1>
    </div>
    <div class="timeline-container" v-loading="loading">
      <div class="timeline-content" v-if="milestones.length > 0">
        <!-- Timeline axis -->
        <div class="timeline-axis">
          <div class="axis-today" :style="{ left: todayPosition + 'px' }">
            <div class="today-label">{{ $t('project.today') }}</div>
            <div class="today-line"></div>
          </div>
          <div class="axis-markers">
            <div
              v-for="(marker, index) in timelineMarkers"
              :key="index"
              class="axis-marker"
              :style="{ left: marker.position + 'px' }"
            >
              <span class="marker-label">{{ marker.label }}</span>
            </div>
          </div>
        </div>
        <!-- Milestone rows -->
        <div class="timeline-milestones">
          <div
            v-for="milestone in milestones"
            :key="milestone.id"
            class="timeline-milestone-row"
          >
            <div class="milestone-info">
              <span class="milestone-name">{{ milestone.name }}</span>
              <span class="milestone-date">{{ formatDate(milestone.targetDate) }}</span>
            </div>
            <div class="milestone-track">
              <div
                class="milestone-dot"
                :class="'status-' + (milestone.status || 'planning').toLowerCase()"
                :style="{ left: getMilestonePosition(milestone) + 'px' }"
                :title="milestone.name + ': ' + formatDate(milestone.targetDate)"
              />
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else :description="$t('project.noMilestones')" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useProjectStore } from '@/stores/project'
import { getMilestonesByProject } from '@/api/milestone'

const { t } = useI18n()
const projectStore = useProjectStore()

const loading = ref(false)
const milestones = ref([])

const dayWidth = 30  // pixels per day
const timelineDays = 90  // show 90 days timeline

const todayPosition = computed(() => {
  return 200  // offset for milestone name column
})

const timelineMarkers = computed(() => {
  const markers = []
  const today = new Date()
  for (let i = -30; i <= timelineDays; i += 7) {
    const date = new Date(today)
    date.setDate(date.getDate() + i)
    markers.push({
      label: `${date.getMonth() + 1}/${date.getDate()}`,
      position: todayPosition.value + (i * dayWidth)
    })
  }
  return markers
})

const getMilestonePosition = (milestone) => {
  if (!milestone.targetDate) return todayPosition.value
  const target = new Date(milestone.targetDate)
  const today = new Date()
  const daysDiff = Math.floor((target - today) / (1000 * 60 * 60 * 24))
  return todayPosition.value + (daysDiff * dayWidth)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`
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

onMounted(() => {
  loadMilestones()
})

watch(() => projectStore.currentProjectId, () => {
  loadMilestones()
})
</script>

<style scoped>
.milestone-timeline-page {
  max-width: 1400px;
}

.timeline-container {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  min-height: 400px;
}

.timeline-content {
  position: relative;
}

.timeline-axis {
  position: relative;
  height: 50px;
  border-bottom: 2px solid var(--el-border-color);
  margin-left: 200px;
}

.axis-today {
  position: absolute;
  top: 0;
  z-index: 10;
}

.today-label {
  font-size: 11px;
  color: var(--el-color-primary);
  font-weight: 600;
  white-space: nowrap;
}

.today-line {
  width: 1px;
  height: 400px;
  background: var(--el-color-primary);
  opacity: 0.3;
  margin-left: -0.5px;
}

.axis-markers {
  position: relative;
  height: 100%;
}

.axis-marker {
  position: absolute;
  top: 20px;
}

.marker-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

.timeline-milestones {
  margin-top: 20px;
}

.timeline-milestone-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.milestone-info {
  width: 200px;
  flex-shrink: 0;
  padding-right: 12px;
}

.milestone-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.milestone-date {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.milestone-track {
  flex: 1;
  position: relative;
  height: 40px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
}

.milestone-dot {
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  top: 12px;
  transform: translateX(-50%);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.milestone-dot.status-planning { background: #3B82F6; }
.milestone-dot.status-in_progress { background: #F59E0B; }
.milestone-dot.status-completed { background: #10B981; }
</style>
