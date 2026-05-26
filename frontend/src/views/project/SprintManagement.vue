<template>
  <div class="sprint-management-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('project.sprintManagement') }}</h1>
      <div class="header-right">
        <el-button type="primary" @click="onCreateSprint">
          <el-icon><Plus /></el-icon>
          {{ $t('project.createSprint') }}
        </el-button>
      </div>
    </div>

    <!-- Lanes Container -->
    <div class="lanes-container" v-loading="loading">
      <SprintLane
        v-for="lane in lanes"
        :key="lane.id ?? 'backlog'"
        :lane="lane"
        :selected-tasks="selectedTasks"
        @task-select="onTaskSelect"
        @task-click="onTaskClick"
        @drop="onDropTask"
      />
    </div>

    <!-- Batch Action Bar -->
    <BatchActionBar
      v-if="selectedTasks.length > 0"
      :selected-count="selectedTasks.length"
      :sprints="sprints"
      @assign-sprint="onBatchAssignSprint"
      @delete="onBatchDelete"
      @clear="onClearSelection"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import SprintLane from '@/components/sprint/SprintLane.vue'
import BatchActionBar from '@/components/sprint/BatchActionBar.vue'
import { getSprints } from '@/api/project'
import { getTasksByProject, updateTask } from '@/api/task'

const { t } = useI18n()
const route = useRoute()

// Refs
const sprints = ref([])
const tasks = ref([])
const selectedTasks = ref([])
const loading = ref(false)

// Lanes computed - backlog lane first (sprintId=null), then sprint lanes
const lanes = computed(() => {
  const backlogTasks = tasks.value.filter(task => !task.sprintId)
  const backlogLane = {
    id: null,
    name: t('project.backlog'),
    status: 'backlog',
    taskCount: backlogTasks.length,
    tasks: backlogTasks,
    capacityHours: 0
  }

  const sprintLanes = sprints.value.map(sprint => {
    const sprintTasks = tasks.value.filter(task => task.sprintId === sprint.id)
    return {
      id: sprint.id,
      name: sprint.name,
      status: sprint.status,
      taskCount: sprintTasks.length,
      tasks: sprintTasks,
      capacityHours: sprint.capacityHours || 0
    }
  })

  return [backlogLane, ...sprintLanes]
})

// Load data from route.params.projectId
async function loadData() {
  const projectId = route.params.projectId
  if (!projectId) {
    ElMessage.error(t('project.projectIdRequired'))
    return
  }

  loading.value = true
  try {
    // Load sprints
    const sprintsRes = await getSprints(projectId)
    sprints.value = sprintsRes.data || sprintsRes || []

    // Load tasks
    const tasksRes = await getTasksByProject(projectId)
    tasks.value = tasksRes.data || tasksRes || []
  } catch (error) {
    console.error('Failed to load data:', error)
    ElMessage.error(t('project.loadProjectFailed'))
  } finally {
    loading.value = false
  }
}

// Event handlers
const onCreateSprint = () => {
  ElMessage.info(t('project.createSprint'))
}

const onTaskSelect = (taskId, isSelected) => {
  if (isSelected) {
    if (!selectedTasks.value.includes(taskId)) {
      selectedTasks.value.push(taskId)
    }
  } else {
    selectedTasks.value = selectedTasks.value.filter(id => id !== taskId)
  }
}

const onTaskClick = (task) => {
  ElMessage.info(`Task clicked: ${task.title}`)
}

const onBatchAssignSprint = (sprintId) => {
  ElMessage.info(`Assign to sprint: ${sprintId}`)
}

const onBatchDelete = () => {
  ElMessage.info('Batch delete')
}

const onClearSelection = () => {
  selectedTasks.value = []
}

const onDropTask = async ({ taskId, targetSprintId }) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (!task || task.sprintId === targetSprintId) return

  try {
    await updateTask(taskId, { sprintId: targetSprintId })
    task.sprintId = targetSprintId
    ElMessage.success(t('project.taskMoved'))
  } catch (e) {
    ElMessage.error(t('project.taskMoveFailed'))
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.sprint-management-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.pm-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--pm-space-lg);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.lanes-container {
  flex: 1;
  overflow-x: auto;
  overflow-y: hidden;
  display: flex;
  gap: var(--pm-space-lg);
  padding: var(--pm-space-md) 0;
}
</style>
