<template>
  <div class="pm-milestone-card">
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
      <el-dropdown trigger="click" @command="handleCommand">
        <el-button link type="primary" @click.stop>
          <el-icon><MoreFilled /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="edit">
              <el-icon><Edit /></el-icon>
              {{ $t('common.edit') }}
            </el-dropdown-item>
            <el-dropdown-item v-if="milestone.status !== 'COMPLETED'" command="complete">
              <el-icon><CircleCheck /></el-icon>
              {{ $t('project.completeMilestone') }}
            </el-dropdown-item>
            <el-dropdown-item command="delete" divided>
              <el-icon><Delete /></el-icon>
              {{ $t('common.delete') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <div class="pm-milestone-description" v-if="milestone.description">
      <p>{{ milestone.description }}</p>
    </div>

    <div class="pm-milestone-footer" v-if="milestone.status">
      <span class="pm-milestone-days" :class="daysClass">
        {{ daysText }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Calendar, MoreFilled, Edit, Delete, CircleCheck } from '@element-plus/icons-vue'

const { t } = useI18n()

const props = defineProps({
  milestone: {
    type: Object,
    required: true,
    default: () => ({
      id: null,
      name: '',
      description: '',
      targetDate: null,
      status: 'PLANNING'
    })
  }
})

const emit = defineEmits(['edit', 'delete', 'complete', 'click'])

const statusType = computed(() => {
  if (props.milestone.status === 'COMPLETED') return 'success'
  if (props.milestone.status === 'IN_PROGRESS') return 'warning'
  return 'info'
})

const statusText = computed(() => {
  if (props.milestone.status === 'COMPLETED') return t('project.completed')
  if (props.milestone.status === 'IN_PROGRESS') return t('project.inProgress')
  return t('project.planning')
})

const daysUntil = computed(() => {
  if (!props.milestone.targetDate) return null
  const target = new Date(props.milestone.targetDate)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  target.setHours(0, 0, 0, 0)
  return Math.ceil((target - today) / (1000 * 60 * 60 * 24))
})

const daysClass = computed(() => {
  if (props.milestone.status === 'COMPLETED') return 'completed'
  if (daysUntil.value === null) return ''
  if (daysUntil.value < 0) return 'overdue'
  if (daysUntil.value <= 7) return 'soon'
  return 'normal'
})

const daysText = computed(() => {
  if (props.milestone.status === 'COMPLETED') return t('project.completed')
  if (daysUntil.value === null) return ''
  if (daysUntil.value < 0) return t('project.overdueDays', { days: Math.abs(daysUntil.value) })
  if (daysUntil.value === 0) return t('project.dueToday')
  return t('project.dueInDays', { days: daysUntil.value })
})

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const handleCommand = (command) => {
  if (command === 'edit') {
    emit('edit', props.milestone)
  } else if (command === 'delete') {
    emit('delete', props.milestone)
  } else if (command === 'complete') {
    emit('complete', props.milestone)
  }
}
</script>

<style scoped>
.pm-milestone-card {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
  transition: all var(--pm-transition-normal);
}

.pm-milestone-card:hover {
  box-shadow: var(--pm-shadow-md);
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

.pm-milestone-description {
  margin-top: var(--pm-space-md);
  padding-top: var(--pm-space-md);
  border-top: 1px solid var(--pm-border-light);
}

.pm-milestone-description p {
  margin: 0;
  font-size: 13px;
  color: var(--pm-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.pm-milestone-footer {
  margin-top: var(--pm-space-md);
  padding-top: var(--pm-space-md);
  border-top: 1px solid var(--pm-border-light);
}

.pm-milestone-days {
  font-size: 12px;
  font-weight: 500;
}

.pm-milestone-days.completed {
  color: var(--pm-success);
}

.pm-milestone-days.overdue {
  color: var(--pm-danger);
}

.pm-milestone-days.soon {
  color: var(--pm-warning);
}

.pm-milestone-days.normal {
  color: var(--pm-text-secondary);
}
</style>
