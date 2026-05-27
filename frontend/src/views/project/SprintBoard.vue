<template>
  <div class="sprint-board-page pm-page">
    <!-- Page Header -->
    <div class="sprint-board-header">
      <div class="sprint-board-header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
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
            {{ formatDate(currentSprint.startDate) }} ~ {{ formatDate(currentSprint.endDate) }}
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

    <!-- Kanban Board -->
    <div class="sprint-board-content" v-loading="loading">
      <!-- Standard Kanban View -->
      <div class="pm-kanban-board-compact">
        <KanbanColumn
          v-for="column in kanbanColumns"
          :key="column.id"
          :id="column.id"
          :title="column.title"
          :status="column.status"
          :status-color="column.color"
          :tasks="getTasksByStatus(column.status)"
          :wip-limit="column.wipLimit"
          :show-progress="true"
          :allow-add="true"
          @task-click="onTaskClick"
          @task-drop="onTaskDrop"
          @add-task="onAddTask"
        />
      </div>
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
                <el-option :label="$t('project.p0')" value="P0" />
                <el-option :label="$t('project.p1')" value="P1" />
                <el-option :label="$t('project.p2')" value="P2" />
                <el-option :label="$t('project.p3')" value="P3" />
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
import { Plus, Calendar, ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import KanbanColumn from '@/components/kanban/KanbanColumn.vue'
import QuickAddDialog from '@/components/kanban/QuickAddDialog.vue'
import { useKanban } from '@/composables/useKanban'
import { getProject, getSprints, getSprintTasks } from '@/api/project'
import { moveTask as apiMoveTask, createTask as apiCreateTask, updateTask as apiUpdateTask } from '@/api/task'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()

const goBack = () => {
  router.push(`/projects/${route.params.id}/sprints`)
}

// Kanban composable
const { columns: kanbanColumnsFromApi, loadTaskStatuses, normalizeTask, statusCodeToId, statusIdToCode } = useKanban()

// Refs
const project = ref(null)
const sprints = ref([])
const currentSprintId = ref(null)
const currentSprint = ref(null)
const tasks = ref([])
const showTaskDetail = ref(false)
const editingTask = ref(null)
const loading = ref(false)
const showQuickAdd = ref(false)
const quickAddStatus = ref('todo')

// Kanban columns
const kanbanColumns = computed(() => {
  if (kanbanColumnsFromApi.value.length > 0) {
    return kanbanColumnsFromApi.value
  }
  // 如果没有从 API 获取到数据，显示默认的 4 个状态
  return [
    { id: 'todo', title: t('project.todo'), status: 'todo', color: '#94A3B8' },
    { id: 'in_progress', title: t('project.inProgress'), status: 'in_progress', color: '#3B82F6', wipLimit: 5 },
    { id: 'in_review', title: t('project.inReview'), status: 'in_review', color: '#8B5CF6', wipLimit: 3 },
    { id: 'done', title: t('project.done'), status: 'done', color: '#10B981' }
  ]
})

// Sprint statistics
const totalTasks = computed(() => tasks.value.length)
const completedTasks = computed(() => tasks.value.filter(t => t.status === 'done').length)
const remainingTasks = computed(() => totalTasks.value - completedTasks.value)
const totalHours = computed(() => tasks.value.reduce((sum, t) => sum + (t.estimateHours || 0), 0))

// Get tasks by status for kanban column
const getTasksByStatus = (status) => {
  return tasks.value.filter(task => task.status === status)
}

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
    'PLANNING': t('project.planning'),
    'ACTIVE': t('project.active'),
    'COMPLETED': t('project.completed')
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

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

// Load project data
async function loadProjectData() {
  const projectId = route.params.id
  if (!projectId) {
    ElMessage.error(t('project.projectIdRequired'))
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
    ElMessage.error(t('project.loadProjectFailed'))
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
    ElMessage.error(t('project.loadTasksFailed'))
  } finally {
    loading.value = false
  }
}

const onSprintChange = () => {
  loadSprintTasks()
}

// Watch for sprint changes
watch(currentSprintId, (newVal) => {
  if (newVal) {
    loadSprintTasks()
  }
})

const onTaskClick = (task) => {
  editingTask.value = { ...task }
  showTaskDetail.value = true
}

const onTaskDrop = async ({ taskId, targetStatus }) => {
  // dataTransfer only stores strings, so taskId comes as string but t.id is number
  const task = tasks.value.find(t => t.id === Number(taskId))
  if (!task) return

  const oldStatus = task.status
  if (oldStatus === targetStatus) return

  const projectId = route.params.id
  try {
    // Now using status directly (backend Task.status field stores code)
    await apiMoveTask(taskId, { status: targetStatus.toUpperCase(), sprintId: currentSprintId.value, projectId })
    task.status = targetStatus
    task.statusId = targetStatus.toUpperCase()
    if (targetStatus === 'done') {
      task.progress = 100
      task.remainingHours = 0
    }
    ElMessage.success(t('project.taskStatusUpdated'))
  } catch (error) {
    console.error('Failed to move task:', error)
    ElMessage.error(t('project.updateTaskStatusFailed'))
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
    ElMessage.error(t('project.createTaskFailed'))
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
    ElMessage.error(t('project.saveTaskFailed'))
  }
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
  padding: var(--pm-space-lg);
  gap: var(--pm-space-md);
}

.sprint-board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.sprint-board-header-left {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
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
  width: 160px;
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
  font-size: 10px;
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
  gap: var(--pm-space-md);
}

/* Sprint Info Banner - Compact */
.sprint-info-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--pm-space-md) var(--pm-space-xl);
  border-radius: var(--pm-radius-lg);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  flex-shrink: 0;
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
  align-items: center;
  gap: var(--pm-space-xl);
}

.sprint-goal {
  display: flex;
  align-items: center;
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
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sprint-dates {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.sprint-date-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  font-size: 13px;
  color: var(--pm-text-muted);
}

.sprint-stats {
  display: flex;
  gap: var(--pm-space-2xl);
}

.sprint-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.sprint-stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--pm-text-primary);
  line-height: 1.2;
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
  font-size: 11px;
  color: var(--pm-text-muted);
}

/* Kanban Board - Fit 6 columns */
.sprint-board-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.pm-kanban-board-compact {
  display: flex;
  gap: var(--pm-space-md);
  flex: 1;
  overflow: hidden;
}

.pm-kanban-board-compact :deep(.pm-kanban-column) {
  flex: 1;
  min-width: 0;
  max-height: 100%;
  width: auto;
}

.pm-kanban-board-compact :deep(.pm-kanban-column-header) {
  padding: var(--pm-space-md);
}

.pm-kanban-board-compact :deep(.pm-kanban-column-title) {
  font-size: 13px;
}

.pm-kanban-board-compact :deep(.pm-kanban-column-count) {
  font-size: 11px;
  padding: 1px 6px;
}

.pm-kanban-board-compact :deep(.pm-kanban-column-body) {
  padding: var(--pm-space-sm);
  min-height: 80px;
}

.pm-kanban-board-compact :deep(.pm-kanban-column-footer) {
  padding: var(--pm-space-sm) var(--pm-space-md);
}

.pm-kanban-board-compact :deep(.pm-kanban-add-btn) {
  font-size: 12px;
}

.pm-kanban-board-compact :deep(.pm-kanban-column-wip) {
  padding: var(--pm-space-xs) var(--pm-space-md);
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
