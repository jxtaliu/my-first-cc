<template>
  <div class="pm-milestone-card" @click="onClick">
    <div class="pm-milestone-header">
      <div class="pm-milestone-icon">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 2L12.5 7.5L18 8L14 12L15 18L10 15L5 18L6 12L2 8L7.5 7.5L10 2Z" fill="currentColor"/>
        </svg>
      </div>
      <div class="pm-milestone-info">
        <h4 class="pm-milestone-title">{{ milestone.name }}</h4>
        <p class="pm-milestone-date">
          <el-icon><Calendar /></el-icon>
          {{ formatDate(milestone.targetDate) }}
        </p>
      </div>
      <div class="pm-milestone-status">
        <el-tag :type="statusType" size="small">
          {{ statusText }}
        </el-tag>
      </div>
    </div>

    <div class="pm-milestone-progress-section" v-if="showProgress">
      <div class="pm-milestone-progress-header">
        <span class="pm-milestone-progress-label">进度</span>
        <span class="pm-milestone-progress-value">{{ milestone.completedTasks }}/{{ milestone.totalTasks }}</span>
      </div>
      <div class="pm-progress">
        <div
          class="pm-progress-bar"
          :class="progressClass"
          :style="{ width: progressPercent + '%' }"
        ></div>
      </div>
      <div class="pm-milestone-progress-percent">{{ progressPercent }}%</div>
    </div>

    <div class="pm-milestone-projects" v-if="milestone.projectNames && milestone.projectNames.length">
      <span class="pm-milestone-projects-label">关联项目:</span>
      <div class="pm-milestone-projects-list">
        <el-tag
          v-for="project in milestone.projectNames.slice(0, 3)"
          :key="project"
          size="small"
          type="info"
        >
          {{ project }}
        </el-tag>
        <el-tag v-if="milestone.projectNames.length > 3" size="small" type="info">
          +{{ milestone.projectNames.length - 3 }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Calendar } from '@element-plus/icons-vue'

const props = defineProps({
  milestone: {
    type: Object,
    required: true,
    default: () => ({
      id: null,
      name: '',
      targetDate: null,
      totalTasks: 0,
      completedTasks: 0,
      projectNames: []
    })
  },
  showProgress: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click'])

const progressPercent = computed(() => {
  if (!props.milestone.totalTasks) return 0
  return Math.round((props.milestone.completedTasks / props.milestone.totalTasks) * 100)
})

const statusType = computed(() => {
  const daysUntil = getDaysUntilTarget()
  if (daysUntil < 0) return 'danger'
  if (daysUntil <= 3) return 'warning'
  return 'success'
})

const statusText = computed(() => {
  const daysUntil = getDaysUntilTarget()
  if (daysUntil < 0) return '已逾期'
  if (daysUntil === 0) return '今日到期'
  if (daysUntil <= 3) return `${daysUntil}天后到期`
  return '进行中'
})

const progressClass = computed(() => {
  if (progressPercent.value >= 100) return 'success'
  if (progressPercent.value >= 70) return 'success'
  if (progressPercent.value >= 30) return 'warning'
  return 'danger'
})

const getDaysUntilTarget = () => {
  if (!props.milestone.targetDate) return 999
  const target = new Date(props.milestone.targetDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  target.setHours(0, 0, 0, 0)
  return Math.ceil((target - today) / (1000 * 60 * 60 * 24))
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const onClick = () => {
  emit('click', props.milestone)
}
</script>

<style scoped>
.pm-milestone-card {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
  cursor: pointer;
  transition: all var(--pm-transition-normal);
}

.pm-milestone-card:hover {
  box-shadow: var(--pm-shadow-md);
  transform: translateY(-2px);
}

.pm-milestone-header {
  display: flex;
  align-items: flex-start;
  gap: var(--pm-space-md);
}

.pm-milestone-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--pm-radius-md);
  background: linear-gradient(135deg, var(--pm-accent) 0%, var(--pm-accent-dark) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pm-milestone-info {
  flex: 1;
  min-width: 0;
}

.pm-milestone-title {
  margin: 0 0 var(--pm-space-xs) 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--pm-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pm-milestone-date {
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--pm-space-xs);
  font-size: 12px;
  color: var(--pm-text-secondary);
}

.pm-milestone-status {
  flex-shrink: 0;
}

.pm-milestone-progress-section {
  margin-top: var(--pm-space-lg);
  padding-top: var(--pm-space-lg);
  border-top: 1px solid var(--pm-border-light);
}

.pm-milestone-progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-sm);
}

.pm-milestone-progress-label {
  font-size: 12px;
  color: var(--pm-text-secondary);
}

.pm-milestone-progress-value {
  font-size: 12px;
  font-weight: 600;
  color: var(--pm-text-primary);
}

.pm-milestone-progress-percent {
  text-align: right;
  font-size: 12px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin-top: var(--pm-space-xs);
}

.pm-milestone-projects {
  margin-top: var(--pm-space-lg);
  padding-top: var(--pm-space-lg);
  border-top: 1px solid var(--pm-border-light);
}

.pm-milestone-projects-label {
  font-size: 12px;
  color: var(--pm-text-secondary);
  display: block;
  margin-bottom: var(--pm-space-sm);
}

.pm-milestone-projects-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--pm-space-xs);
}
</style>
