<template>
  <div class="my-board-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('project.myBoard') }}</h1>
        <p class="pm-text-small">{{ $t('project.myBoardDesc') }}</p>
      </div>
      <div class="my-board-actions">
        <el-select v-model="filterProject" :placeholder="$t('project.allProjects')" clearable style="width: 180px">
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
      </div>
    </div>

    <!-- Stats Summary -->
    <div class="my-board-stats">
      <StatCard
        :label="$t('project.myTasks')"
        :value="myTasks.length"
        :trend="5"
        :subtitle="$t('project.inProgress')"
      />
      <StatCard
        :label="$t('project.completedThisWeek')"
        :value="completedThisWeek"
        :trend="12"
        :subtitle="$t('project.completedThisWeek')"
      />
      <StatCard
        :label="$t('project.overdueTasks')"
        :value="overdueTasks"
        :trend="-2"
        :class="{ 'pm-stat-danger': overdueTasks > 0 }"
        :subtitle="$t('project.needsAttention')"
      />
      <StatCard
        :label="$t('project.blockedTasks')"
        :value="blockedTasks"
        :trend="0"
        :subtitle="$t('project.waitingToResolve')"
      />
    </div>

    <!-- Kanban Board -->
    <div class="my-board-content" v-loading="loading">
      <KanbanBoard
        ref="kanbanBoardRef"
        :tasks="filteredMyTasks"
        :columns="kanbanColumns"
        :show-progress="true"
        :allow-add="true"
        @task-click="onTaskClick"
        @task-drop="onTaskDrop"
        @add-task="onAddTask"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import KanbanBoard from '@/components/kanban/KanbanBoard.vue'
import StatCard from '@/components/common/StatCard.vue'
import { useKanban } from '@/composables/useKanban'
import { useAuthStore } from '@/stores/auth'
import { getTasksByAssignee } from '@/api/task'
import { getProjects } from '@/api/project'

const { t } = useI18n()
const authStore = useAuthStore()

const kanbanBoardRef = ref(null)
const filterProject = ref(null)
const projects = ref([])
const myTasks = ref([])
const loading = ref(false)

// Kanban composable
const { columns: kanbanColumnsFromApi, loadTaskStatuses, normalizeTask, statusCodeToId, statusIdToCode } = useKanban()

// Kanban columns - use API columns if available, otherwise fallback
const kanbanColumns = computed(() => {
  if (kanbanColumnsFromApi.value.length > 0) {
    return kanbanColumnsFromApi.value
  }
  return [
    { id: 'todo', title: t('project.colTodo'), status: 'todo', color: '#94A3B8' },
    { id: 'in_progress', title: t('project.colInProgress'), status: 'in_progress', color: '#3B82F6', wipLimit: 5 },
    { id: 'development', title: t('project.colDevelopment'), status: 'development', color: '#8B5CF6', wipLimit: 3 },
    { id: 'testing', title: t('project.colTesting'), status: 'testing', color: '#F59E0B', wipLimit: 3 },
    { id: 'done', title: t('project.colDone'), status: 'done', color: '#10B981' }
  ]
})

const filteredMyTasks = computed(() => {
  if (!filterProject.value) return myTasks.value
  return myTasks.value.filter(task => task.projectId === filterProject.value)
})

const completedThisWeek = computed(() => {
  return myTasks.value.filter(task => {
    if (task.status !== 'done') return false
    const doneDate = new Date(task.doneDate || task.updatedAt)
    const weekAgo = new Date()
    weekAgo.setDate(weekAgo.getDate() - 7)
    return doneDate >= weekAgo
  }).length
})

const overdueTasks = computed(() => {
  const now = new Date()
  return myTasks.value.filter(task => {
    if (task.status === 'done') return false
    if (!task.dueDate) return false
    return new Date(task.dueDate) < now
  }).length
})

const blockedTasks = computed(() => {
  return myTasks.value.filter(task => task.status === 'blocked').length
})

// Load my tasks from API
async function loadMyTasks() {
  if (!authStore.user?.id) {
    ElMessage.error('User not logged in')
    return
  }

  loading.value = true
  try {
    // Load system task statuses
    await loadTaskStatuses(null)

    // Load user's assigned tasks
    const res = await getTasksByAssignee(authStore.user.id)
    const rawTasks = res.data || res || []
    myTasks.value = rawTasks.map(task => normalizeTask(task))
  } catch (error) {
    console.error('Failed to load my tasks:', error)
    ElMessage.error('Failed to load tasks')
  } finally {
    loading.value = false
  }
}

// Load projects for filter
async function loadProjects() {
  try {
    const res = await getProjects()
    projects.value = res.data || res || []
  } catch (error) {
    console.error('Failed to load projects:', error)
  }
}

const onTaskClick = (task) => {
  ElMessage.info(`${t('project.viewTask')}: ${task.title}`)
}

const onTaskDrop = ({ taskId, targetStatus }) => {
  // dataTransfer stores taskId as string, but task.id is number
  const task = myTasks.value.find(t => t.id === Number(taskId))
  if (task) {
    const oldStatus = task.status
    task.status = targetStatus
    if (targetStatus === 'done') {
      task.progress = 100
      task.remainingHours = 0
      task.doneDate = new Date()
    }
    ElMessage.success(t('project.taskStatusUpdated'))
  }
}

const onAddTask = ({ status }) => {
  ElMessage.info(t('project.createTask'))
}

onMounted(async () => {
  // Ensure user is loaded
  if (!authStore.user) {
    await authStore.getCurrentUser()
  }
  await Promise.all([
    loadMyTasks(),
    loadProjects()
  ])
})
</script>

<style scoped>
.my-board-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.my-board-actions {
  display: flex;
  gap: var(--pm-space-md);
}

.my-board-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--pm-space-lg);
  margin-bottom: var(--pm-space-xl);
}

.my-board-content {
  flex: 1;
  overflow: hidden;
}

.pm-stat-danger {
  border-color: var(--pm-status-blocked);
}
</style>
