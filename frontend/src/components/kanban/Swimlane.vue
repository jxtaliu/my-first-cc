<template>
  <div class="pm-swimlane" :class="{ collapsed: isCollapsed }">
    <!-- Swimlane Header -->
    <div class="pm-swimlane-header" @click="toggleCollapse">
      <div class="pm-swimlane-header-left">
        <el-icon class="pm-swimlane-collapse-icon" :class="{ rotated: !isCollapsed }">
          <ArrowRight />
        </el-icon>
        <div class="pm-swimlane-avatar" v-if="avatarText">
          <span class="pm-avatar pm-avatar-sm">{{ avatarText }}</span>
        </div>
        <div class="pm-swimlane-label">
          <span class="pm-swimlane-title">{{ label }}</span>
          <span class="pm-swimlane-count">({{ totalTasks }})</span>
        </div>
      </div>
      <div class="pm-swimlane-header-right">
        <div class="pm-swimlane-stats">
          <span class="pm-swimlane-stat" v-if="totalEstimate > 0">
            <span class="pm-stat-icon">&#9201;</span>
            {{ totalEstimate }}h
          </span>
          <span class="pm-swimlane-stat workload" :class="workloadClass" v-if="totalEstimate > 0">
            {{ workloadLabel }}
          </span>
        </div>
        <el-button text size="small" circle @click.stop="onExpand">
          <el-icon><FullScreen /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- Swimlane Content -->
    <div class="pm-swimlane-content" v-show="!isCollapsed">
      <div class="pm-swimlane-columns">
        <div
          v-for="column in columns"
          :key="column.id"
          class="pm-swimlane-column"
          @dragover.prevent="onDragOver(column)"
          @dragleave="onDragLeave(column)"
          @drop="onDrop(column, $event)"
        >
          <div class="pm-swimlane-column-inner" :class="{ 'drag-over': dragOverColumn === column.id }">
            <TaskCard
              v-for="task in getTasksByColumn(column.status)"
              :key="task.id"
              :task="task"
              :show-progress="showProgress"
              @click="onTaskClick(task)"
              @dragstart="onTaskDragStart(task)"
              @dragend="onTaskDragEnd(task)"
            />
            <div v-if="getTasksByColumn(column.status).length === 0" class="pm-swimlane-empty">
              {{ $t('project.noTasks') }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Collapsed Summary -->
    <div class="pm-swimlane-collapsed-summary" v-if="isCollapsed">
      <div class="pm-swimlane-mini-task" v-for="task in collapsedTasks" :key="task.id">
        <span class="pm-mini-task-status" :style="{ background: getStatusColor(task.status) }"></span>
        <span class="pm-mini-task-title">{{ task.title }}</span>
      </div>
      <span v-if="remainingTaskCount > 0" class="pm-swimlane-more">
        +{{ remainingTaskCount }} {{ $t('project.more') }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowRight, FullScreen } from '@element-plus/icons-vue'
import TaskCard from './TaskCard.vue'

const props = defineProps({
  label: {
    type: String,
    required: true
  },
  groupKey: {
    type: String,
    required: true
  },
  tasks: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    required: true
  },
  showProgress: {
    type: Boolean,
    default: true
  },
  maxCollapsedTasks: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['task-click', 'task-drop', 'task-drag-start', 'task-drag-end', 'expand'])

const isCollapsed = ref(false)
const dragOverColumn = ref(null)

const avatarText = computed(() => {
  if (!props.label) return ''
  return props.label.charAt(0).toUpperCase()
})

const totalTasks = computed(() => props.tasks.length)

const totalEstimate = computed(() => {
  return props.tasks.reduce((sum, task) => sum + (task.estimateHours || 0), 0)
})

const workloadClass = computed(() => {
  if (totalEstimate.value >= 40) return 'overloaded'
  if (totalEstimate.value >= 30) return 'high'
  if (totalEstimate.value >= 20) return 'medium'
  return 'low'
})

const workloadLabel = computed(() => {
  const classes = {
    low: 'Light',
    medium: 'Medium',
    high: 'High',
    overloaded: 'Overloaded'
  }
  return classes[workloadClass.value]
})

const collapsedTasks = computed(() => {
  return props.tasks.slice(0, props.maxCollapsedTasks)
})

const remainingTaskCount = computed(() => {
  return Math.max(0, props.tasks.length - props.maxCollapsedTasks)
})

const getTasksByColumn = (status) => {
  return props.tasks.filter(task => task.status === status)
}

const getStatusColor = (status) => {
  const column = props.columns.find(c => c.status === status)
  return column?.color || '#94A3B8'
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

const onExpand = () => {
  emit('expand', { groupKey: props.groupKey, label: props.label })
}

const onTaskClick = (task) => {
  emit('task-click', task)
}

const onTaskDragStart = (task) => {
  emit('task-drag-start', task)
}

const onTaskDragEnd = (task) => {
  dragOverColumn.value = null
  emit('task-drag-end', task)
}

const onDragOver = (column) => {
  dragOverColumn.value = column.id
}

const onDragLeave = (column) => {
  if (dragOverColumn.value === column.id) {
    dragOverColumn.value = null
  }
}

const onDrop = (column, event) => {
  event.preventDefault()
  dragOverColumn.value = null
  const taskId = event.dataTransfer.getData('taskId')
  if (taskId) {
    emit('task-drop', {
      taskId,
      targetStatus: column.status,
      targetColumn: column.id
    })
  }
}
</script>

<style scoped>
.pm-swimlane {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  margin-bottom: var(--pm-space-lg);
  overflow: hidden;
  transition: all var(--pm-transition-normal);
}

.pm-swimlane:hover {
  box-shadow: var(--pm-shadow-md);
}

.pm-swimlane.collapsed {
  margin-bottom: var(--pm-space-md);
}

.pm-swimlane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--pm-space-lg) var(--pm-space-xl);
  background: var(--pm-background);
  border-bottom: 1px solid var(--pm-border);
  cursor: pointer;
  user-select: none;
}

.pm-swimlane-header:hover {
  background: var(--pm-border-light);
}

.pm-swimlane-header-left {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.pm-swimlane-collapse-icon {
  transition: transform var(--pm-transition-fast);
  color: var(--pm-text-muted);
  font-size: 14px;
}

.pm-swimlane-collapse-icon.rotated {
  transform: rotate(90deg);
}

.pm-swimlane-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
}

.pm-swimlane-label {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.pm-swimlane-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--pm-text-primary);
}

.pm-swimlane-count {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.pm-swimlane-header-right {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
}

.pm-swimlane-stats {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.pm-swimlane-stat {
  display: flex;
  align-items: center;
  gap: var(--pm-space-xs);
  font-size: 12px;
  color: var(--pm-text-secondary);
  padding: 2px 8px;
  background: var(--pm-border-light);
  border-radius: var(--pm-radius-sm);
}

.pm-stat-icon {
  font-size: 10px;
}

.pm-swimlane-stat.workload.low {
  color: var(--pm-status-done);
  background: rgba(16, 185, 129, 0.1);
}

.pm-swimlane-stat.workload.medium {
  color: var(--pm-status-testing);
  background: rgba(245, 158, 11, 0.1);
}

.pm-swimlane-stat.workload.high {
  color: var(--pm-status-in-progress);
  background: rgba(59, 130, 246, 0.1);
}

.pm-swimlane-stat.workload.overloaded {
  color: var(--pm-status-blocked);
  background: rgba(239, 68, 68, 0.1);
}

.pm-swimlane-content {
  padding: var(--pm-space-lg);
  background: var(--pm-background);
}

.pm-swimlane-columns {
  display: grid;
  grid-template-columns: repeat(v-bind(columns.length), minmax(280px, 1fr));
  gap: var(--pm-space-lg);
}

.pm-swimlane-column {
  min-height: 80px;
}

.pm-swimlane-column-inner {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
  padding: var(--pm-space-md);
  background: var(--pm-card);
  border-radius: var(--pm-radius-md);
  border: 2px dashed transparent;
  transition: all var(--pm-transition-fast);
  min-height: 100px;
}

.pm-swimlane-column-inner.drag-over {
  border-color: var(--pm-accent);
  background: rgba(0, 212, 170, 0.05);
}

.pm-swimlane-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: var(--pm-text-muted);
  font-size: 13px;
}

.pm-swimlane-collapsed-summary {
  display: flex;
  flex-wrap: wrap;
  gap: var(--pm-space-sm);
  padding: var(--pm-space-md) var(--pm-space-xl);
  background: var(--pm-background);
}

.pm-swimlane-mini-task {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  padding: 4px 10px;
  background: var(--pm-card);
  border-radius: var(--pm-radius-sm);
  font-size: 12px;
}

.pm-mini-task-status {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.pm-mini-task-title {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--pm-text-secondary);
}

.pm-swimlane-more {
  display: flex;
  align-items: center;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--pm-text-muted);
}
</style>
