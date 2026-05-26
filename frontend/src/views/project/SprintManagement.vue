<template>
  <div class="sprint-management-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('project.sprintManagement') }}</h1>
      <div class="header-right">
        <div class="filters">
          <el-input v-model="searchQuery" :placeholder="$t('project.searchTasks')" />
          <el-select v-model="filterType" clearable>
            <el-option :label="$t('project.task')" value="TASK" />
            <el-option :label="$t('project.story')" value="STORY" />
            <el-option :label="$t('project.bug')" value="BUG" />
            <el-option :label="$t('project.epic')" value="EPIC" />
          </el-select>
          <el-select v-model="filterPriority" clearable>
            <el-option :label="$t('project.p0')" value="P0" />
            <el-option :label="$t('project.p1')" value="P1" />
            <el-option :label="$t('project.p2')" value="P2" />
            <el-option :label="$t('project.p3')" value="P3" />
          </el-select>
        </div>
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
      @batch-assign="onBatchAssignSprint"
      @batch-remove="onBatchRemove"
      @clear-selection="onClearSelection"
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
import { getSprints, batchAssignTasks, batchRemoveTasks } from '@/api/project'
import { getTasksByProject, updateTask } from '@/api/task'

const { t } = useI18n()
const route = useRoute()

// Refs
const sprints = ref([])
const tasks = ref([])
const selectedTasks = ref([])
const loading = ref(false)
const searchQuery = ref('')
const filterType = ref(null)
const filterPriority = ref(null)

// Lanes computed - backlog lane first (sprintId=null), then sprint lanes
const lanes = computed(() => {
  const filterTasks = (taskList) => {
    return taskList.filter(t => {
      if (searchQuery.value) {
        const q = searchQuery.value.toLowerCase()
        if (!t.title?.toLowerCase().includes(q) && !t.description?.toLowerCase().includes(q)) {
          return false
        }
      }
      if (filterType.value && t.type !== filterType.value) return false
      if (filterPriority.value && t.priority !== filterPriority.value) return false
      return true
    })
  }

  const backlogTasks = filterTasks(tasks.value.filter(task => !task.sprintId))
  const backlogLane = {
    id: null,
    name: t('project.backlog'),
    status: 'backlog',
    taskCount: backlogTasks.length,
    tasks: backlogTasks,
    capacityHours: 0
  }

  const sprintLanes = sprints.value.map(sprint => {
    const sprintTasks = filterTasks(tasks.value.filter(task => task.sprintId === sprint.id))
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

const onBatchAssignSprint = async (targetSprintId) => {
  try {
    await batchAssignTasks(targetSprintId, selectedTasks.value)
    selectedTasks.value.forEach(id => {
      const task = tasks.value.find(t => t.id === id)
      if (task) task.sprintId = targetSprintId
    })
    selectedTasks.value = []
    ElMessage.success(t('project.tasksAssigned'))
  } catch (e) {
    ElMessage.error(t('project.assignFailed'))
  }
}

const onClearSelection = () => {
  selectedTasks.value = []
}

const onBatchRemove = async () => {
  const firstTask = tasks.value.find(t => selectedTasks.value.includes(t.id))
  if (!firstTask?.sprintId) return

  try {
    await batchRemoveTasks(firstTask.sprintId, selectedTasks.value)
    selectedTasks.value.forEach(id => {
      const task = tasks.value.find(t => t.id === id)
      if (task) task.sprintId = null
    })
    selectedTasks.value = []
    ElMessage.success(t('project.tasksRemoved'))
  } catch (e) {
    ElMessage.error(t('project.removeFailed'))
  }
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

.filters {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
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
