<template>
  <div class="project-board-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div class="project-board-header-left">
        <h1 class="pm-heading-1">{{ project?.name || $t('project.selectProject') }}</h1>
        <div class="project-board-breadcrumb" v-if="project">
          <span class="breadcrumb-item">{{ $t('project.sprint') }}: </span>
          <el-select v-model="currentSprintId" placeholder="选择Sprint" size="small" class="sprint-select">
            <el-option
              v-for="sprint in sprints"
              :key="sprint.id"
              :label="sprint.name"
              :value="sprint.id"
            />
          </el-select>
        </div>
      </div>
      <div class="project-board-header-right">
        <el-button-group>
          <el-button
            v-for="view in viewOptions"
            :key="view.value"
            :type="currentView === view.value ? 'primary' : 'default'"
            @click="currentView = view.value"
          >
            <el-icon v-if="view.icon"><component :is="view.icon" /></el-icon>
            {{ view.label }}
          </el-button>
        </el-button-group>
        <el-button type="primary" @click="onCreateTask">
          <el-icon><Plus /></el-icon>
          {{ $t('project.createTask') }}
        </el-button>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="project-board-filters">
      <div class="filter-left">
        <el-select v-model="swimlaneMode" placeholder="泳道" size="default" style="width: 120px">
          <el-option label="无泳道" value="none" />
          <el-option label="按负责人" value="assignee" />
          <el-option label="按优先级" value="priority" />
          <el-option label="按类型" value="type" />
        </el-select>
        <el-input
          v-model="searchQuery"
          :placeholder="$t('project.searchTasks')"
          prefix-icon="Search"
          clearable
          style="width: 200px"
        />
      </div>
      <div class="filter-right">
        <el-select v-model="selectedPriority" placeholder="优先级" clearable size="default" style="width: 120px">
          <el-option label="全部" :value="null" />
          <el-option label="P0 紧急" value="P0" />
          <el-option label="P1 高" value="P1" />
          <el-option label="P2 中" value="P2" />
          <el-option label="P3 低" value="P3" />
        </el-select>
        <span class="task-count" :class="{ 'filter-active': selectedPriority }">{{ filteredTasks.length }} {{ $t('project.tasks') }}</span>
      </div>
    </div>

    <!-- Kanban Board -->
    <div class="project-board-content" v-loading="loading">
      <KanbanBoard
        ref="kanbanBoardRef"
        :tasks="filteredTasks"
        :columns="kanbanColumns"
        :show-progress="true"
        :allow-add="true"
        :swimlane-mode="swimlaneMode"
        @task-click="onTaskClick"
        @task-drop="onTaskDrop"
        @add-task="onAddTask"
      />
    </div>

    <!-- Task Detail Dialog -->
    <el-dialog
      v-model="showTaskDetail"
      :title="editingTask?.title || $t('project.taskDetail')"
      width="700px"
      class="pm-dialog task-detail-dialog"
    >
      <div class="task-detail-content" v-if="editingTask">
        <el-form :model="editingTask" label-position="top">
          <el-form-item :label="$t('project.taskTitle')">
            <el-input v-model="editingTask.title" />
          </el-form-item>

          <div class="form-row">
            <el-form-item :label="$t('project.status')" class="form-col">
              <el-select v-model="editingTask.status" style="width: 100%">
                <el-option
                  v-for="col in kanbanColumns"
                  :key="col.status"
                  :label="col.title"
                  :value="col.status"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('project.priority')" class="form-col">
              <el-select v-model="editingTask.priority" style="width: 100%">
                <el-option label="P0 紧急" value="P0" />
                <el-option label="P1 高" value="P1" />
                <el-option label="P2 中" value="P2" />
                <el-option label="P3 低" value="P3" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item :label="$t('project.estimateHours')" class="form-col">
              <el-input-number v-model="editingTask.estimateHours" :min="0" :step="0.5" />
            </el-form-item>
            <el-form-item :label="$t('project.remainingHours')" class="form-col">
              <el-input-number v-model="editingTask.remainingHours" :min="0" :step="0.5" />
            </el-form-item>
            <el-form-item :label="$t('project.actualHours')" class="form-col">
              <el-input-number v-model="editingTask.actualHours" :min="0" :step="0.5" />
            </el-form-item>
          </div>

          <el-form-item :label="$t('project.description')">
            <el-input
              v-model="editingTask.description"
              type="textarea"
              :rows="4"
            />
          </el-form-item>

          <el-form-item :label="$t('project.dependencies')">
            <div class="dependencies-list" v-if="editingTask.dependencies?.length">
              <div v-for="dep in editingTask.dependencies" :key="dep.id" class="dependency-item">
                <span class="dependency-type">{{ dep.type }}</span>
                <span class="dependency-task">{{ dep.taskTitle }}</span>
                <span class="dependency-status" :class="dep.status">{{ dep.statusText }}</span>
              </div>
            </div>
            <el-button text @click="onAddDependency">
              <el-icon><Plus /></el-icon>
              {{ $t('project.addDependency') }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showTaskDetail = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="onSaveTask">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Quick Add Task Dialog -->
    <QuickAddDialog
      v-model="showQuickAdd"
      :status="quickAddStatus"
      @submit="onQuickAddSubmit"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus, Grid, List, Calendar } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import KanbanBoard from '@/components/kanban/KanbanBoard.vue'
import QuickAddDialog from '@/components/kanban/QuickAddDialog.vue'
import { useKanban } from '@/composables/useKanban'
import { getProject, getSprints, getSprintTasks } from '@/api/project'
import { getTasksByProject, moveTask as apiMoveTask, createTask as apiCreateTask, updateTask as apiUpdateTask } from '@/api/task'

const { t } = useI18n()
const route = useRoute()

// Kanban composable
const { columns: kanbanColumnsFromApi, loadTaskStatuses, normalizeTask, statusCodeToId, statusIdToCode } = useKanban()

// Refs
const kanbanBoardRef = ref(null)
const project = ref(null)
const sprints = ref([])
const currentSprintId = ref(null)
const tasks = ref([])
const searchQuery = ref('')
const swimlaneMode = ref('none')
const selectedPriority = ref(null)
const currentView = ref('kanban')
const showTaskDetail = ref(false)
const editingTask = ref(null)
const loading = ref(false)
const showQuickAdd = ref(false)
const quickAddStatus = ref('todo')

// View options
const viewOptions = [
  { label: '看板', value: 'kanban', icon: 'Grid' },
  { label: '列表', value: 'list', icon: 'List' },
  { label: '甘特图', value: 'gantt', icon: 'Calendar' }
]

// Kanban columns - use API columns if available, otherwise fallback
const kanbanColumns = computed(() => {
  if (kanbanColumnsFromApi.value.length > 0) {
    return kanbanColumnsFromApi.value
  }
  return [
    { id: 'todo', title: '待办', status: 'todo', color: '#94A3B8' },
    { id: 'in_progress', title: '进行中', status: 'in_progress', color: '#3B82F6', wipLimit: 5 },
    { id: 'development', title: '开发完成', status: 'development', color: '#8B5CF6', wipLimit: 3 },
    { id: 'testing', title: '测试中', status: 'testing', color: '#F59E0B', wipLimit: 3 },
    { id: 'done', title: '已完成', status: 'done', color: '#10B981' }
  ]
})

// Filter tasks based on search and priority
const filteredTasks = computed(() => {
  let result = tasks.value

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(task =>
      task.title?.toLowerCase().includes(query) ||
      task.description?.toLowerCase().includes(query)
    )
  }

  // Filter by priority
  if (selectedPriority.value) {
    result = result.filter(task => task.priority === selectedPriority.value)
  }

  return result
})

// Load project data from API
async function loadProjectData() {
  const projectId = route.params.id
  if (!projectId) {
    ElMessage.error('Project ID is required')
    return
  }

  loading.value = true
  try {
    // Load project info
    const projectRes = await getProject(projectId)
    project.value = projectRes.data || projectRes

    // Load sprints
    const sprintsRes = await getSprints(projectId)
    sprints.value = sprintsRes.data || sprintsRes || []

    // Load task statuses for the project
    await loadTaskStatuses(projectId)

    // Set current sprint to active one or first sprint
    const activeSprint = sprints.value.find(s => s.status === 'ACTIVE') || sprints.value[0]
    if (activeSprint) {
      currentSprintId.value = activeSprint.id
    }
  } catch (error) {
    console.error('Failed to load project data:', error)
    ElMessage.error('Failed to load project data')
  } finally {
    loading.value = false
  }
}

// Load tasks for current sprint
async function loadSprintTasks() {
  const projectId = route.params.id
  if (!currentSprintId.value) {
    tasks.value = []
    return
  }

  loading.value = true
  try {
    const res = await getSprintTasks(projectId, currentSprintId.value)
    const rawTasks = res.data || res || []
    tasks.value = rawTasks.map(task => {
      const normalized = normalizeTask(task)
      normalized.dependencies = task.dependencies || []
      return normalized
    })
  } catch (error) {
    console.error('Failed to load sprint tasks:', error)
    ElMessage.error('Failed to load tasks')
  } finally {
    loading.value = false
  }
}

// Watch for sprint changes
watch(currentSprintId, () => {
  if (currentSprintId.value) {
    loadSprintTasks()
  }
})

// Event handlers
const onTaskClick = (task) => {
  editingTask.value = { ...task }
  showTaskDetail.value = true
}

const onTaskDrop = async ({ taskId, targetStatus }) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (!task) return

  const oldStatus = task.status
  const newStatusId = statusCodeToId.value[targetStatus]

  if (oldStatus === targetStatus) return

  try {
    await apiMoveTask(taskId, { statusId: newStatusId, sprintId: currentSprintId.value })
    task.status = targetStatus
    task.statusId = newStatusId
    // Recalculate progress based on new status
    if (targetStatus === 'done') {
      task.progress = 100
      task.remainingHours = 0
    }
    ElMessage.success(t('project.taskStatusUpdated'))
  } catch (error) {
    console.error('Failed to move task:', error)
    ElMessage.error('Failed to update task status')
  }
}

const onAddTask = ({ status }) => {
  quickAddStatus.value = status
  showQuickAdd.value = true
}

const onQuickAddSubmit = async ({ title, type, priority, status }) => {
  const projectId = route.params.id
  try {
    const createData = {
      title,
      type,
      priority,
      statusId: statusCodeToId.value[status],
      projectId: projectId ? parseInt(projectId) : null,
      sprintId: currentSprintId.value
    }
    const res = await apiCreateTask(createData)
    const newTask = normalizeTask(res.data || res)
    tasks.value.push(newTask)
    ElMessage.success(t('project.taskCreated'))
    showQuickAdd.value = false
  } catch (error) {
    console.error('Failed to create task:', error)
    ElMessage.error('Failed to create task')
  }
}

const onCreateTask = () => {
  onAddTask({ status: 'TODO' })
}

const onSaveTask = async () => {
  if (!editingTask.value) return

  try {
    if (editingTask.value.id) {
      // Update existing task
      const updateData = {
        title: editingTask.value.title,
        description: editingTask.value.description,
        type: editingTask.value.type,
        priority: editingTask.value.priority,
        statusId: statusCodeToId.value[editingTask.value.status] || editingTask.value.statusId,
        estimateHours: editingTask.value.estimateHours,
        remainingHours: editingTask.value.remainingHours,
        actualHours: editingTask.value.actualHours,
        progress: editingTask.value.progress
      }
      await apiUpdateTask(editingTask.value.id, updateData)

      // Update local task
      const index = tasks.value.findIndex(t => t.id === editingTask.value.id)
      if (index !== -1) {
        tasks.value[index] = { ...tasks.value[index], ...editingTask.value }
      }
      ElMessage.success(t('project.taskUpdated'))
    } else {
      // Create new task
      const createData = {
        title: editingTask.value.title,
        description: editingTask.value.description,
        type: editingTask.value.type || 'task',
        priority: editingTask.value.priority || 'P3',
        statusId: statusCodeToId.value[editingTask.value.status] || editingTask.value.statusId,
        projectId: editingTask.value.projectId,
        sprintId: editingTask.value.sprintId,
        estimateHours: editingTask.value.estimateHours || 0,
        remainingHours: editingTask.value.remainingHours || 0,
        actualHours: editingTask.value.actualHours || 0,
        progress: editingTask.value.progress || 0
      }
      const res = await apiCreateTask(createData)
      const newTask = normalizeTask(res.data || res)
      tasks.value.push(newTask)
      ElMessage.success(t('project.taskCreated'))
    }
    showTaskDetail.value = false
  } catch (error) {
    console.error('Failed to save task:', error)
    ElMessage.error('Failed to save task')
  }
}

const onAddDependency = () => {
  ElMessage.info(t('project.addDependency'))
}

onMounted(() => {
  loadProjectData()
})
</script>

<style scoped>
.project-board-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.project-board-header-left {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.project-board-breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  font-size: 14px;
}

.breadcrumb-item {
  color: var(--pm-text-secondary);
}

.sprint-select {
  width: 140px;
}

.project-board-header-right {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
}

.project-board-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--pm-space-lg) 0;
  margin-bottom: var(--pm-space-md);
}

.filter-left {
  display: flex;
  gap: var(--pm-space-md);
}

.filter-right {
  display: flex;
  align-items: center;
}

.task-count {
  font-size: 14px;
  color: var(--pm-text-secondary);
}

.task-count.filter-active {
  color: var(--pm-primary);
  font-weight: 600;
}

.project-board-content {
  flex: 1;
  overflow: hidden;
}

.task-detail-dialog :deep(.el-dialog__body) {
  padding-top: var(--pm-space-lg);
}

.task-detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.form-row {
  display: flex;
  gap: var(--pm-space-lg);
}

.form-col {
  flex: 1;
}

.dependencies-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
  margin-bottom: var(--pm-space-md);
}

.dependency-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-sm) var(--pm-space-md);
  background: var(--pm-background);
  border-radius: var(--pm-radius-sm);
}

.dependency-type {
  font-size: 11px;
  font-weight: 600;
  color: var(--pm-text-secondary);
  background: var(--pm-border-light);
  padding: 2px 6px;
  border-radius: var(--pm-radius-xs);
}

.dependency-task {
  flex: 1;
  font-size: 13px;
  color: var(--pm-text-primary);
}

.dependency-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: var(--pm-radius-sm);
}

.dependency-status.in_progress {
  background: rgba(59, 130, 246, 0.1);
  color: var(--pm-status-in-progress);
}

.dependency-status.done {
  background: rgba(16, 185, 129, 0.1);
  color: var(--pm-status-done);
}
</style>
