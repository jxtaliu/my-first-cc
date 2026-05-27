<template>
  <div
    class="pm-kanban-column"
    :class="{ 'pm-wip-exceeded': isWipExceeded }"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDrop"
  >
    <!-- Column Header -->
    <div class="pm-kanban-column-header">
      <div class="pm-kanban-column-title">
        <span class="pm-kanban-column-status-dot" :style="{ background: statusColor }"></span>
        <span>{{ title }}</span>
        <span class="pm-kanban-column-count">{{ tasks.length }}</span>
      </div>
    </div>

    <!-- WIP Limit Warning -->
    <div class="pm-kanban-column-wip" v-if="wipLimit">
      <span class="pm-wip-limit" :class="{ exceeded: isWipExceeded }">
        WIP: {{ tasks.length }}/{{ wipLimit }}
      </span>
    </div>

    <!-- Column Body: Task Cards -->
    <div class="pm-kanban-column-body" ref="columnBody">
      <TaskCard
        v-for="task in tasks"
        :key="task.id"
        :task="task"
        :show-progress="showProgress"
        :is-draggable="isTaskDraggable(task)"
        :subtask-tooltip="getSubtaskTooltip(task)"
        :computed-progress="getComputedProgress ? getComputedProgress(task) : null"
        :selected="isTaskSelected(task)"
        @click="onTaskClick"
        @dragstart="onTaskDragStart"
        @dragend="onTaskDragEnd"
        @toggle-select="onToggleSelect"
      />

      <!-- Empty State -->
      <div v-if="tasks.length === 0" class="pm-kanban-column-empty">
        <div class="pm-empty">
          <span class="pm-empty-text">{{ $t('project.noTasks') }}</span>
        </div>
      </div>

      <!-- Drop Zone Indicator -->
      <div
        v-if="isDragOver"
        class="pm-kanban-column-dropzone"
        :class="{ 'drag-over': isDragOver }"
      ></div>
    </div>

    <!-- Column Footer: Quick Add -->
    <div class="pm-kanban-column-footer" v-if="allowAdd">
      <el-button
        text
        size="small"
        class="pm-kanban-add-btn"
        @click="onQuickAdd"
      >
        <el-icon><Plus /></el-icon>
        {{ $t('project.addTask') }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import TaskCard from './TaskCard.vue'

const props = defineProps({
  id: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  status: {
    type: String,
    default: 'todo'
  },
  statusColor: {
    type: String,
    default: '#94A3B8'
  },
  tasks: {
    type: Array,
    default: () => []
  },
  wipLimit: {
    type: Number,
    default: null
  },
  showProgress: {
    type: Boolean,
    default: true
  },
  allowAdd: {
    type: Boolean,
    default: true
  },
  isTaskDraggable: {
    type: Function,
    default: () => true
  },
  getSubtaskTooltip: {
    type: Function,
    default: () => ''
  },
  getComputedProgress: {
    type: Function,
    default: null
  },
  selectedTasks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['task-click', 'task-drag-start', 'task-drag-end', 'task-drop', 'add-task', 'toggle-select'])

const isDragOver = ref(false)

const isWipExceeded = computed(() => {
  if (!props.wipLimit) return false
  return props.tasks.length >= props.wipLimit
})

const isTaskSelected = (task) => {
  return props.selectedTasks.some(t => t.id === task.id)
}

const onToggleSelect = (task) => {
  emit('toggle-select', task)
}

const onTaskClick = (task) => {
  emit('task-click', task)
}

const onTaskDragStart = (task) => {
  emit('task-drag-start', task)
}

const onTaskDragEnd = (task) => {
  isDragOver.value = false
  emit('task-drag-end', task)
}

const onDragOver = (e) => {
  e.preventDefault()
  isDragOver.value = true
}

const onDragLeave = (e) => {
  // Only set to false if leaving the column entirely
  if (!e.currentTarget.contains(e.relatedTarget)) {
    isDragOver.value = false
  }
}

const onDrop = (e) => {
  e.preventDefault()
  isDragOver.value = false
  const taskId = e.dataTransfer.getData('taskId')
  if (taskId) {
    emit('task-drop', {
      taskId,
      targetStatus: props.status,
      targetColumn: props.id
    })
  }
}

const onQuickAdd = () => {
  emit('add-task', { status: props.status, columnId: props.id })
}
</script>

<style scoped>
.pm-kanban-column {
  background: var(--pm-background);
  border-radius: var(--pm-radius-md);
  display: flex;
  flex-direction: column;
  transition: all var(--pm-transition-fast);
  border: 1px solid var(--pm-border);
  min-width: 280px;
  max-width: 320px;
  height: 100%;
  box-sizing: border-box;
}

.pm-kanban-column.pm-wip-exceeded {
  border: 2px solid var(--pm-status-blocked);
  animation: pm-pulse-danger 1s infinite;
}

@keyframes pm-pulse-danger {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.85; }
}

.pm-kanban-column-header {
  padding: var(--pm-space-sm) var(--pm-space-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--pm-border);
  background: var(--pm-card);
  border-radius: var(--pm-radius-md) var(--pm-radius-md) 0 0;
  flex-shrink: 0;
}

.pm-kanban-column-title {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  font-weight: 600;
  font-size: 14px;
  color: var(--pm-text-primary);
}

.pm-kanban-column-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.pm-kanban-column-count {
  background: var(--pm-border-light);
  color: var(--pm-text-secondary);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.pm-kanban-column-wip {
  padding: var(--pm-space-xs) var(--pm-space-md);
  background: var(--pm-border-light);
  flex-shrink: 0;
}

.pm-wip-limit {
  font-size: 11px;
  color: var(--pm-text-muted);
  display: flex;
  align-items: center;
  gap: var(--pm-space-xs);
}

.pm-wip-limit.exceeded {
  color: var(--pm-status-blocked);
  font-weight: 600;
}

.pm-kanban-column-body {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: var(--pm-space-xs);
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 100px;
}

.pm-kanban-column-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: var(--pm-space-lg);
}

.pm-kanban-column-dropzone {
  border: 2px dashed var(--pm-border);
  border-radius: var(--pm-radius-md);
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--pm-transition-fast);
}

.pm-kanban-column-dropzone.drag-over {
  border-color: var(--pm-accent);
  background: rgba(0, 212, 170, 0.05);
}

.pm-kanban-column-footer {
  padding: var(--pm-space-xs) var(--pm-space-sm);
  border-top: 1px solid var(--pm-border-light);
  background: var(--pm-card);
  border-radius: 0 0 var(--pm-radius-md) var(--pm-radius-md);
  flex-shrink: 0;
}

.pm-kanban-add-btn {
  width: 100%;
  justify-content: flex-start;
  color: var(--pm-text-muted);
}

.pm-kanban-add-btn:hover {
  color: var(--pm-primary);
  background: var(--pm-border-light);
}
</style>
