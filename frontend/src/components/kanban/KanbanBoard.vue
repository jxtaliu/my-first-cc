<template>
  <div class="pm-kanban-board" ref="boardRef">
    <KanbanColumn
      v-for="column in columns"
      :key="column.id"
      :id="column.id"
      :title="column.title"
      :status="column.status"
      :status-color="column.color"
      :tasks="getTasksByStatus(column.status)"
      :wip-limit="column.wipLimit"
      :show-progress="showProgress"
      :allow-add="allowAdd"
      :selected-tasks="selectedTasks"
      @task-click="onTaskClick"
      @task-drag-start="onTaskDragStart"
      @task-drag-end="onTaskDragEnd"
      @task-drop="onTaskDrop"
      @add-task="onAddTask"
      @toggle-select="onToggleSelect"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import KanbanColumn from './KanbanColumn.vue'

const props = defineProps({
  tasks: {
    type: Array,
    default: () => []
  },
  columns: {
    type: Array,
    required: true,
    default: () => [
      { id: 'todo', title: '待办', status: 'todo', color: '#94A3B8' },
      { id: 'in_progress', title: '进行中', status: 'in_progress', color: '#3B82F6', wipLimit: 5 },
      { id: 'development', title: '开发完成', status: 'development', color: '#8B5CF6', wipLimit: 3 },
      { id: 'testing', title: '测试中', status: 'testing', color: '#F59E0B', wipLimit: 3 },
      { id: 'done', title: '已完成', status: 'done', color: '#10B981' }
    ]
  },
  showProgress: {
    type: Boolean,
    default: true
  },
  allowAdd: {
    type: Boolean,
    default: true
  },
  swimlaneMode: {
    type: String,
    default: 'none' // none, assignee, priority, type
  }
})

const emit = defineEmits(['task-click', 'task-drop', 'add-task', 'task-update', 'selection-change'])

const boardRef = ref(null)
const draggingTask = ref(null)
const selectedTasks = ref([])

const getTasksByStatus = (status) => {
  let filtered = props.tasks.filter(task => task.status === status)

  // Apply swimlane filtering if needed
  if (props.swimlaneMode !== 'none') {
    // Grouping is handled at parent level, here we just filter
  }

  return filtered
}

const onTaskClick = (task) => {
  emit('task-click', task)
}

const onTaskDragStart = (task) => {
  draggingTask.value = task
}

const onTaskDragEnd = (task) => {
  draggingTask.value = null
}

const onToggleSelect = (task) => {
  const index = selectedTasks.value.findIndex(t => t.id === task.id)
  if (index >= 0) {
    selectedTasks.value.splice(index, 1)
  } else {
    selectedTasks.value.push(task)
  }
  emit('selection-change', selectedTasks.value)
}

const onTaskDrop = ({ taskId, targetStatus, targetColumn }) => {
  // dataTransfer stores taskId as string, but task.id is number
  const numericTaskId = Number(taskId)
  // If we have selected tasks and the dropped task is one of them, move all selected
  const selectedIds = selectedTasks.value.map(t => t.id)
  if (selectedIds.includes(numericTaskId) && selectedIds.length > 1) {
    // Batch move all selected tasks
    emit('task-drop', {
      taskIds: selectedIds,
      targetStatus,
      targetColumn
    })
    // Clear selection after batch move
    selectedTasks.value = []
  } else {
    // Single task move
    emit('task-drop', {
      taskId: numericTaskId,
      taskIds: null,
      targetStatus,
      targetColumn
    })
  }
}

const onAddTask = ({ status, columnId }) => {
  emit('add-task', { status, columnId })
}

// Scroll the board
const scrollTo = (direction) => {
  if (!boardRef.value) return
  const scrollAmount = 340 // column width + gap
  if (direction === 'left') {
    boardRef.value.scrollBy({ left: -scrollAmount, behavior: 'smooth' })
  } else {
    boardRef.value.scrollBy({ left: scrollAmount, behavior: 'smooth' })
  }
}

// Expose methods for parent components
defineExpose({
  scrollTo,
  clearSelection: () => {
    selectedTasks.value = []
  }
})
</script>

<style scoped>
.pm-kanban-board {
  display: flex;
  gap: var(--pm-space-lg);
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: var(--pm-space-lg);
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
  height: calc(100vh - 180px);
  min-height: 400px;
}

.pm-kanban-board::-webkit-scrollbar {
  height: 8px;
}

.pm-kanban-board::-webkit-scrollbar-track {
  background: var(--pm-border-light);
  border-radius: 4px;
}

.pm-kanban-board::-webkit-scrollbar-thumb {
  background: var(--pm-border);
  border-radius: 4px;
}

.pm-kanban-board::-webkit-scrollbar-thumb:hover {
  background: var(--pm-text-muted);
}
</style>
