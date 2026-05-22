<template>
  <div class="sprint-board-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div class="sprint-board-header-left">
        <div class="sprint-selector-wrapper">
          <span class="sprint-label">{{ $t('project.sprint') }}:</span>
          <el-select
            v-model="currentSprintId"
            :placeholder="$t('project.selectSprint')"
            size="default"
            class="sprint-select"
            @change="onSprintChange"
          >
            <el-option
              v-for="sprint in sprints"
              :key="sprint.id"
              :label="sprint.name"
              :value="sprint.id"
            >
              <div class="sprint-option">
                <span class="sprint-option-name">{{ sprint.name }}</span>
                <span class="sprint-option-status" :class="getSprintStatusClass(sprint.status)">
                  {{ getSprintStatusText(sprint.status) }}
                </span>
              </div>
            </el-option>
          </el-select>
        </div>
      </div>
      <div class="sprint-board-header-right">
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

    <!-- Sprint Info Banner -->
    <div class="sprint-info-banner" v-if="currentSprint" :class="getSprintBannerClass(currentSprint.status)">
      <div class="sprint-info-main">
        <div class="sprint-goal" v-if="currentSprint.goal">
          <span class="sprint-goal-label">{{ $t('project.sprintGoal') }}:</span>
          <span class="sprint-goal-text">{{ currentSprint.goal }}</span>
        </div>
        <div class="sprint-dates">
          <span class="sprint-date-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(currentSprint.startDate) }} - {{ formatDate(currentSprint.endDate) }}
          </span>
          <span class="sprint-status-badge" :class="getSprintStatusClass(currentSprint.status)">
            {{ getSprintStatusText(currentSprint.status) }}
          </span>
        </div>
      </div>
      <div class="sprint-stats">
        <div class="sprint-stat">
          <span class="sprint-stat-value">{{ totalTasks }}</span>
          <span class="sprint-stat-label">{{ $t('project.totalTasks') }}</span>
        </div>
        <div class="sprint-stat">
          <span class="sprint-stat-value completed">{{ completedTasks }}</span>
          <span class="sprint-stat-label">{{ $t('project.completed') }}</span>
        </div>
        <div class="sprint-stat">
          <span class="sprint-stat-value remaining">{{ remainingTasks }}</span>
          <span class="sprint-stat-label">{{ $t('project.remaining') }}</span>
        </div>
        <div class="sprint-stat">
          <span class="sprint-stat-value hours">{{ totalHours }}h</span>
          <span class="sprint-stat-label">{{ $t('project.totalHours') }}</span>
        </div>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="sprint-board-filters">
      <div class="filter-left">
        <el-select v-model="swimlaneMode" :placeholder="$t('project.swimlaneMode')" style="width: 140px">
          <el-option :label="$t('project.noSwimlane')" value="none" />
          <el-option :label="$t('project.byAssignee')" value="assignee" />
          <el-option :label="$t('project.byPriority')" value="priority" />
          <el-option :label="$t('project.byType')" value="type" />
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
        <span class="task-count">{{ filteredTasks.length }} {{ $t('project.tasks') }}</span>
      </div>
    </div>

    <!-- Kanban Board or Swimlanes -->
    <div class="sprint-board-content" v-loading="loading">
      <!-- Swimlanes View -->
      <template v-if="swimlaneMode !== 'none'">
        <Swimlane
          v-for="swimlane in swimlanes"
          :key="swimlane.key"
          :label="swimlane.label"
          :group-key="swimlane.key"
          :tasks="swimlane.tasks"
          :columns="kanbanColumns"
          :show-progress="true"
          @task-click="onTaskClick"
          @task-drop="onTaskDrop"
          @expand="onExpandSwimlane"
        />
      </template>

      <!-- Standard Kanban View -->
      <KanbanBoard
        v-else
        ref="kanbanBoardRef"
        :tasks="filteredTasks"
        :columns="kanbanColumns"
        :show-progress="true"
        :allow-add="true"
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
import Swimlane from '@/components/kanban/Swimlane.vue'
import QuickAddDialog from '@/components/kanban/QuickAddDialog.vue'
import { useKanban } from '@/composables/useKanban'
import { getProject, getSprints, getSprintTasks } from '@/api/project'
import { moveTask as apiMoveTask, createTask as apiCreateTask, updateTask as apiUpdateTask } from '@/api/task'

const { t } = useI18n()
const route = useRoute()

// Kanban composable
const { columns: kanbanColumnsFromApi, loadTaskStatuses, normalizeTask, statusCodeToId, statusIdToCode } = useKanban()

// Refs
const kanbanBoardRef = ref(null)
const project = ref(null)
const sprints = ref([])
const currentSprintId = ref(null)
const currentSprint = ref(null)
const tasks = ref([])
const searchQuery = ref('')
const swimlaneMode = ref('none')
const currentView = ref('kanban')
const showTaskDetail = ref(false)
const editingTask = ref(null)
const loading = ref(false)
const showQuickAdd = ref(false)
const quickAddStatus = ref('todo')

// View options
const viewOptions = [
  { label: '看板', value: 'kanban', icon: 'Grid' },
  { label: '列表', value: 'list', icon: 'List' }
]

// Kanban columns
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

// Sprint statistics
const totalTasks = computed(() => tasks.value.length)
const completedTasks = computed(() => tasks.value.filter(t => t.status === 'done').length)
const remainingTasks = computed(() => totalTasks.value - completedTasks.value)
const totalHours = computed(() => tasks.value.reduce((sum, t) => sum + (t.estimateHours || 0), 0))

// Filter tasks based on search
const filteredTasks = computed(() => {
  if (!searchQuery.value) return tasks.value
  const query = searchQuery.value.toLowerCase()
  return tasks.value.filter(task =>
    task.title?.toLowerCase().includes(query) ||
    task.description?.toLowerCase().includes(query)
  )
})

// Swimlanes grouping
const swimlanes = computed(() => {
  if (swimlaneMode.value === 'none') return []

  const groups = {}
  tasks.value.forEach(task => {
    let key, label
    switch (swimlaneMode.value) {
      case 'assignee':
        key = task.assignee || 'unassigned'
        label = task.assigneeName || task.assignee || 'Unassigned'
        break
      case 'priority':
        key = task.priority || 'P3'
        label = task.priority || 'P3'
        break
      case 'type':
        key = task.type || 'task'
        label = getTypeLabel(task.type)
        break
      default:
        key = 'other'
        label = 'Other'
    }
    if (!groups[key]) {
      groups[key] = { key, label, tasks: [] }
    }
    groups[key].tasks.push(task)
  })

  return Object.values(groups).sort((a, b) => {
    if (swimlaneMode.value === 'priority') {
      const priorityOrder = { P0: 0, P1: 1, P2: 2, P3: 3 }
      return (priorityOrder[a.key] || 4) - (priorityOrder[b.key] || 4)
    }
    return a.label.localeCompare(b.label)
  })
})

// Sprint status helpers
const getSprintStatusClass = (status) => {
  const map = {
    'PLANNING': 'planning',
    'ACTIVE': 'active',
    'COMPLETED': 'completed'
  }
  return map[status] || 'planning'
}

const getSprintStatusText = (status) => {
  const map = {
    'PLANNING': '规划中',
    'ACTIVE': '进行中',
    'COMPLETED': '已完成'
  }
  return map[status] || status
}

const getSprintBannerClass = (status) => {
  const map = {
    'PLANNING': 'banner-planning',
    'ACTIVE': 'banner-active',
    'COMPLETED': 'banner-completed'
  }
  return map[status] || 'banner-planning'
}

const getTypeLabel = (type) => {
  const map = {
    'epic': 'Epic',
    'feature': 'Feature',
    'story': 'Story',
    'task': 'Task',
    'bug': 'Bug',
    'subtask': 'Sub-task'
  }
  return map[type] || type
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// Load project data
async function loadProjectData() {
  const projectId = route.params.id
  if (!projectId) {
    ElMessage.error('Project ID is required')
    return
  }

  loading.value = true
  try {
    const projectRes = await getProject(projectId)
    project.value = projectRes.data || projectRes

    const sprintsRes = await getSprints(projectId)
    sprints.value = sprintsRes.data || sprintsRes || []

    await loadTaskStatuses(projectId)

    const activeSprint = sprints.value.find(s => s.status === 'ACTIVE') || sprints.value[0]
    if (activeSprint) {
      currentSprintId.value = activeSprint.id
      currentSprint.value = activeSprint
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
    tasks.value = rawTasks.map(task => normalizeTask(task))

    currentSprint.value = sprints.value.find(s => s.id === currentSprintId.value)
  } catch (error) {
    console.error('Failed to load sprint tasks:', error)
    ElMessage.error('Failed to load tasks')
  } finally {
    loading.value = false
  }
}

const onSprintChange = () => {
  loadSprintTasks()
}

const onTaskClick = (task) => {
  editingTask.value = { ...task }
  showTaskDetail.value = true
}

const onTaskDrop = async ({ taskId, targetStatus }) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (!task) return

  const oldStatus = task.status
  if (oldStatus === targetStatus) return

  try {
    const newStatusId = statusCodeToId.value[targetStatus]
    await apiMoveTask(taskId, { statusId: newStatusId, sprintId: currentSprintId.value })
    task.status = targetStatus
    task.statusId = newStatusId
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
  onAddTask({ status: 'todo' })
}

const onSaveTask = async () => {
  if (!editingTask.value) return

  try {
    if (editingTask.value.id) {
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

      const index = tasks.value.findIndex(t => t.id === editingTask.value.id)
      if (index !== -1) {
        tasks.value[index] = { ...tasks.value[index], ...editingTask.value }
      }
      ElMessage.success(t('project.taskUpdated'))
    }
    showTaskDetail.value = false
  } catch (error) {
    console.error('Failed to save task:', error)
    ElMessage.error('Failed to save task')
  }
}

const onExpandSwimlane = ({ groupKey, label }) => {
  ElMessage.info(`Expand swimlane: ${label}`)
}

onMounted(() => {
  loadProjectData()
})
</script>

<style scoped>
.sprint-board-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.sprint-board-header-left {
  display: flex;
  align-items: center;
}

.sprint-selector-wrapper {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.sprint-label {
  font-size: 14px;
  color: var(--pm-text-secondary);
  font-weight: 500;
}

.sprint-select {
  width: 180px;
}

.sprint-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sprint-option-name {
  font-weight: 500;
}

.sprint-option-status {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: var(--pm-radius-xs);
}

.sprint-option-status.planning {
  background: rgba(148, 163, 184, 0.2);
  color: var(--pm-text-secondary);
}

.sprint-option-status.active {
  background: rgba(16, 185, 129, 0.2);
  color: var(--pm-status-done);
}

.sprint-option-status.completed {
  background: rgba(59, 130, 246, 0.2);
  color: var(--pm-status-in-progress);
}

.sprint-board-header-right {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
}

.sprint-info-banner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--pm-space-xl);
  border-radius: var(--pm-radius-lg);
  margin-bottom: var(--pm-space-lg);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
}

.sprint-info-banner.banner-active {
  border-left: 4px solid var(--pm-status-done);
}

.sprint-info-banner.banner-planning {
  border-left: 4px solid var(--pm-status-todo);
}

.sprint-info-banner.banner-completed {
  border-left: 4px solid var(--pm-status-in-progress);
}

.sprint-info-main {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
}

.sprint-goal {
  display: flex;
  align-items: flex-start;
  gap: var(--pm-space-sm);
}

.sprint-goal-label {
  font-size: 13px;
  color: var(--pm-text-secondary);
  font-weight: 500;
}

.sprint-goal-text {
  font-size: 14px;
  color: var(--pm-text-primary);
}

.sprint-dates {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
}

.sprint-date-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  font-size: 13px;
  color: var(--pm-text-muted);
}

.sprint-status-badge {
  padding: 4px 10px;
  border-radius: var(--pm-radius-sm);
  font-size: 12px;
  font-weight: 600;
}

.sprint-status-badge.planning {
  background: rgba(148, 163, 184, 0.2);
  color: var(--pm-text-secondary);
}

.sprint-status-badge.active {
  background: rgba(16, 185, 129, 0.2);
  color: var(--pm-status-done);
}

.sprint-status-badge.completed {
  background: rgba(59, 130, 246, 0.2);
  color: var(--pm-status-in-progress);
}

.sprint-stats {
  display: flex;
  gap: var(--pm-space-xl);
}

.sprint-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--pm-space-xs);
}

.sprint-stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--pm-text-primary);
}

.sprint-stat-value.completed {
  color: var(--pm-status-done);
}

.sprint-stat-value.remaining {
  color: var(--pm-status-in-progress);
}

.sprint-stat-value.hours {
  color: var(--pm-accent);
}

.sprint-stat-label {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.sprint-board-filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-lg);
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

.sprint-board-content {
  flex: 1;
  overflow-y: auto;
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
</style>
