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
      @task-click="onTaskClick"
      @task-drag-start="onTaskDragStart"
      @task-drag-end="onTaskDragEnd"
      @task-drop="onTaskDrop"
      @add-task="onAddTask"
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

const emit = defineEmits(['task-click', 'task-drop', 'add-task', 'task-update'])

const boardRef = ref(null)
const draggingTask = ref(null)

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

const onTaskDrop = ({ taskId, targetStatus, targetColumn }) => {
  emit('task-drop', {
    taskId,
    targetStatus,
    targetColumn
  })
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
  scrollTo
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
