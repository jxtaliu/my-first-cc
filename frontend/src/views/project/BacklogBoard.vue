<template>
  <div class="backlog-board-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div class="backlog-header-left">
        <h1 class="pm-heading-1">{{ $t('project.backlog') }}</h1>
        <div class="backlog-breadcrumb" v-if="project">
          <span class="breadcrumb-item">{{ project.name }} -</span>
          <span class="breadcrumb-current">{{ $t('project.backlog') }}</span>
        </div>
      </div>
      <div class="backlog-header-right">
        <el-select v-model="filterProject" :placeholder="$t('project.allProjects')" clearable style="width: 180px">
          <el-option
            v-for="proj in projects"
            :key="proj.id"
            :label="proj.name"
            :value="proj.id"
          />
        </el-select>
        <el-select v-model="filterPriority" :placeholder="$t('project.priority')" clearable style="width: 120px">
          <el-option label="P0" value="P0" />
          <el-option label="P1" value="P1" />
          <el-option label="P2" value="P2" />
          <el-option label="P3" value="P3" />
        </el-select>
        <el-button type="primary" @click="onQuickAdd">
          <el-icon><Plus /></el-icon>
          {{ $t('project.quickAdd') }}
        </el-button>
      </div>
    </div>

    <!-- Backlog Stats -->
    <div class="backlog-stats">
      <div class="backlog-stat">
        <span class="backlog-stat-value">{{ totalBacklogTasks }}</span>
        <span class="backlog-stat-label">{{ $t('project.backlogTasks') }}</span>
      </div>
      <div class="backlog-stat">
        <span class="backlog-stat-value">{{ totalEstimate }}h</span>
        <span class="backlog-stat-label">{{ $t('project.estimatedHours') }}</span>
      </div>
      <div class="backlog-stat">
        <span class="backlog-stat-value">{{ sprintCount }}</span>
        <span class="backlog-stat-label">{{ $t('project.availableSprints') }}</span>
      </div>
      <div class="backlog-stat">
        <el-dropdown trigger="click" @command="handleBulkAction">
          <el-button text size="small">
            {{ $t('project.bulkActions') }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="assign">{{ $t('project.assignToSprint') }}</el-dropdown-item>
              <el-dropdown-item command="priority">{{ $t('project.setPriority') }}</el-dropdown-item>
              <el-dropdown-item command="delete" divided>{{ $t('common.delete') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- Backlog Content -->
    <div class="backlog-content" v-loading="loading">
      <!-- Priority Tabs -->
      <div class="backlog-tabs">
        <div
          v-for="priority in priorityOrder"
          :key="priority"
          class="backlog-priority-section"
        >
          <div class="priority-header" @click="togglePriorityCollapse(priority)">
            <div class="priority-header-left">
              <el-icon class="priority-collapse-icon" :class="{ collapsed: collapsedPriorities.includes(priority) }">
                <ArrowRight />
              </el-icon>
              <span class="priority-badge" :class="getPriorityClass(priority)">{{ priority }}</span>
              <span class="priority-label">{{ getPriorityLabel(priority) }}</span>
              <span class="priority-count">({{ getTasksByPriority(priority).length }})</span>
            </div>
            <div class="priority-header-right">
              <span class="priority-estimate" v-if="getTotalEstimateByPriority(priority) > 0">
                {{ getTotalEstimateByPriority(priority) }}h
              </span>
            </div>
          </div>

          <div class="priority-tasks" v-show="!collapsedPriorities.includes(priority)">
            <div
              v-for="task in getTasksByPriority(priority)"
              :key="task.id"
              class="backlog-task-item"
              :class="{ selected: selectedTasks.includes(task.id) }"
              draggable="true"
              @dragstart="onDragStart(task)"
              @dragend="onDragEnd"
              @click="onTaskClick(task)"
            >
              <div class="task-checkbox">
                <el-checkbox
                  :model-value="selectedTasks.includes(task.id)"
                  @change="toggleTaskSelection(task.id)"
                />
              </div>
              <div class="task-content">
                <div class="task-header">
                  <span class="task-title">{{ task.title }}</span>
                  <span class="task-type-badge" :class="getTypeClass(task.type)">
                    {{ getTypeLabel(task.type) }}
                  </span>
                </div>
                <div class="task-meta">
                  <span class="task-project" v-if="task.projectName">{{ task.projectName }}</span>
                  <span class="task-sprint" v-if="task.sprintName">{{ task.sprintName }}</span>
                  <span class="task-estimate" v-if="task.estimateHours">{{ task.estimateHours }}h</span>
                </div>
              </div>
              <div class="task-actions">
                <el-button text size="small" circle @click.stop="onEditTask(task)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button text size="small" circle @click.stop="onDeleteTask(task)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>

            <div v-if="getTasksByPriority(priority).length === 0" class="priority-empty">
              {{ $t('project.noTasksInPriority') }}
            </div>
          </div>
        </div>
      </div>

      <!-- Sprint Drop Zone -->
      <div class="sprint-drop-section">
        <div class="sprint-drop-header">
          <h3>{{ $t('project.dragToSprint') }}</h3>
        </div>
        <div class="sprint-drop-zones">
          <div
            v-for="sprint in availableSprints"
            :key="sprint.id"
            class="sprint-drop-zone"
            :class="{ 'drag-over': dragOverSprint === sprint.id }"
            @dragover.prevent="onSprintDragOver(sprint.id)"
            @dragleave="onSprintDragLeave"
            @drop="onSprintDrop(sprint, $event)"
          >
            <div class="sprint-zone-header">
              <span class="sprint-zone-name">{{ sprint.name }}</span>
              <span class="sprint-zone-status" :class="getSprintStatusClass(sprint.status)">
                {{ getSprintStatusText(sprint.status) }}
              </span>
            </div>
            <div class="sprint-zone-tasks">
              <span class="sprint-zone-count">{{ sprint.taskCount || 0 }} {{ $t('project.tasks') }}</span>
            </div>
          </div>
        </div>
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
            <el-form-item :label="$t('project.project')" class="form-col">
              <el-select v-model="editingTask.projectId" style="width: 100%">
                <el-option
                  v-for="proj in projects"
                  :key="proj.id"
                  :label="proj.name"
                  :value="proj.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('project.type')" class="form-col">
              <el-select v-model="editingTask.type" style="width: 100%">
                <el-option label="任务" value="task" />
                <el-option label="缺陷" value="bug" />
                <el-option label="故事" value="story" />
                <el-option label="史诗" value="epic" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item :label="$t('project.priority')" class="form-col">
              <el-select v-model="editingTask.priority" style="width: 100%">
                <el-option label="P0 紧急" value="P0" />
                <el-option label="P1 高" value="P1" />
                <el-option label="P2 中" value="P2" />
                <el-option label="P3 低" value="P3" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('project.estimateHours')" class="form-col">
              <el-input-number v-model="editingTask.estimateHours" :min="0" :step="0.5" />
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

    <!-- Quick Add Dialog -->
    <QuickAddDialog
      v-model="showQuickAdd"
      status="backlog"
      @submit="onQuickAddSubmit"
    />

    <!-- Assign to Sprint Dialog -->
    <el-dialog
      v-model="showAssignDialog"
      :title="$t('project.assignToSprint')"
      width="400px"
      class="pm-dialog"
    >
      <el-form label-position="top">
        <el-form-item :label="$t('project.selectSprint')">
          <el-select v-model="selectedSprintId" style="width: 100%">
            <el-option
              v-for="sprint in availableSprints"
              :key="sprint.id"
              :label="sprint.name"
              :value="sprint.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="onAssignToSprint">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowRight, ArrowDown, Edit, Delete } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import QuickAddDialog from '@/components/kanban/QuickAddDialog.vue'
import { useKanban } from '@/composables/useKanban'
import { getProjects, getSprints, updateSprint, getSprintTasks } from '@/api/project'
import { getTasksByProject, createTask as apiCreateTask, updateTask as apiUpdateTask, deleteTask as apiDeleteTask } from '@/api/task'

const { t } = useI18n()
const route = useRoute()

// Kanban composable
const { columns: kanbanColumnsFromApi, loadTaskStatuses, normalizeTask, statusCodeToId } = useKanban()

// Refs
const projects = ref([])
const sprints = ref([])
const backlogTasks = ref([])
const selectedTasks = ref([])
const filterProject = ref(null)
const filterPriority = ref(null)
const collapsedPriorities = ref([])
const showTaskDetail = ref(false)
const showQuickAdd = ref(false)
const showAssignDialog = ref(false)
const selectedSprintId = ref(null)
const editingTask = ref(null)
const loading = ref(false)
const draggingTask = ref(null)
const dragOverSprint = ref(null)

const priorityOrder = ['P0', 'P1', 'P2', 'P3']

// Kanban columns
const kanbanColumns = computed(() => {
  if (kanbanColumnsFromApi.value.length > 0) {
    return kanbanColumnsFromApi.value
  }
  return [
    { id: 'todo', title: '待办', status: 'todo', color: '#94A3B8' },
    { id: 'in_progress', title: '进行中', status: 'in_progress', color: '#3B82F6' },
    { id: 'done', title: '已完成', status: 'done', color: '#10B981' }
  ]
})

// Computed project from route
const project = computed(() => {
  if (route.params.id) {
    return projects.value.find(p => p.id === parseInt(route.params.id))
  }
  return null
})

// Available sprints (not completed)
const availableSprints = computed(() => {
  return sprints.value.filter(s => s.status !== 'COMPLETED')
})

// Sprint count
const sprintCount = computed(() => availableSprints.value.length)

// Total backlog tasks
const totalBacklogTasks = computed(() => backlogTasks.value.length)

// Total estimate
const totalEstimate = computed(() => {
  return backlogTasks.value.reduce((sum, task) => sum + (task.estimateHours || 0), 0)
})

// Filter tasks
const filteredTasks = computed(() => {
  let result = backlogTasks.value
  if (filterProject.value) {
    result = result.filter(task => task.projectId === filterProject.value)
  }
  if (filterPriority.value) {
    result = result.filter(task => task.priority === filterPriority.value)
  }
  return result
})

// Get tasks by priority
const getTasksByPriority = (priority) => {
  return filteredTasks.value.filter(task => task.priority === priority)
}

// Get total estimate by priority
const getTotalEstimateByPriority = (priority) => {
  return getTasksByPriority(priority).reduce((sum, task) => sum + (task.estimateHours || 0), 0)
}

// Priority helpers
const getPriorityClass = (priority) => {
  return `priority-${priority.toLowerCase()}`
}

const getPriorityLabel = (priority) => {
  const labels = {
    P0: '紧急',
    P1: '高',
    P2: '中',
    P3: '低'
  }
  return labels[priority] || priority
}

const getTypeClass = (type) => {
  const map = {
    'epic': 'type-epic',
    'feature': 'type-feature',
    'story': 'type-story',
    'task': 'type-task',
    'bug': 'type-bug'
  }
  return map[type] || 'type-task'
}

const getTypeLabel = (type) => {
  const map = {
    'epic': 'Epic',
    'feature': 'Feature',
    'story': 'Story',
    'task': 'Task',
    'bug': 'Bug'
  }
  return map[type] || type
}

const getSprintStatusClass = (status) => {
  const map = {
    'PLANNING': 'status-planning',
    'ACTIVE': 'status-active',
    'COMPLETED': 'status-completed'
  }
  return map[status] || 'status-planning'
}

const getSprintStatusText = (status) => {
  const map = {
    'PLANNING': '规划中',
    'ACTIVE': '进行中',
    'COMPLETED': '已完成'
  }
  return map[status] || status
}

// Toggle priority collapse
const togglePriorityCollapse = (priority) => {
  const index = collapsedPriorities.value.indexOf(priority)
  if (index >= 0) {
    collapsedPriorities.value.splice(index, 1)
  } else {
    collapsedPriorities.value.push(priority)
  }
}

// Toggle task selection
const toggleTaskSelection = (taskId) => {
  const index = selectedTasks.value.indexOf(taskId)
  if (index >= 0) {
    selectedTasks.value.splice(index, 1)
  } else {
    selectedTasks.value.push(taskId)
  }
}

// Load data
async function loadData() {
  loading.value = true
  try {
    await loadTaskStatuses(null)

    const projectsRes = await getProjects()
    projects.value = projectsRes.data || projectsRes || []

    const projectId = route.params.id
    if (projectId) {
      filterProject.value = parseInt(projectId)
    }

    for (const proj of projects.value) {
      try {
        const sprintsRes = await getSprints(proj.id)
        const projectSprints = sprintsRes.data || sprintsRes || []
        sprints.value.push(...projectSprints.map(s => ({ ...s, projectName: proj.name })))
      } catch (e) {
        console.warn(`Failed to load sprints for project ${proj.id}`)
      }
    }

    const backlog = []
    for (const proj of projects.value) {
      try {
        const tasksRes = await getTasksByProject(proj.id)
        const tasks = tasksRes.data || tasksRes || []
        const unassignedTasks = tasks.filter(task => !task.sprintId)
        backlog.push(...unassignedTasks.map(task => ({
          ...normalizeTask(task),
          projectName: proj.name
        })))
      } catch (e) {
        console.warn(`Failed to load tasks for project ${proj.id}`)
      }
    }

    backlogTasks.value = backlog.sort((a, b) => {
      const priorityOrder = { P0: 0, P1: 1, P2: 2, P3: 3 }
      return (priorityOrder[a.priority] || 4) - (priorityOrder[b.priority] || 4)
    })
  } catch (error) {
    console.error('Failed to load data:', error)
    ElMessage.error('Failed to load data')
  } finally {
    loading.value = false
  }
}

// Drag handlers
const onDragStart = (task) => {
  draggingTask.value = task
}

const onDragEnd = () => {
  draggingTask.value = null
  dragOverSprint.value = null
}

const onSprintDragOver = (sprintId) => {
  dragOverSprint.value = sprintId
}

const onSprintDragLeave = () => {
  dragOverSprint.value = null
}

const onSprintDrop = async (sprint, event) => {
  event.preventDefault()
  dragOverSprint.value = null

  if (!draggingTask.value) return

  try {
    const newStatusId = statusCodeToId.value['todo']
    await apiUpdateTask(draggingTask.value.id, {
      sprintId: sprint.id,
      statusId: newStatusId
    })

    const index = backlogTasks.value.findIndex(t => t.id === draggingTask.value.id)
    if (index >= 0) {
      backlogTasks.value.splice(index, 1)
    }

    ElMessage.success(t('project.taskAssignedToSprint'))
    draggingTask.value = null
  } catch (error) {
    console.error('Failed to assign task to sprint:', error)
    ElMessage.error('Failed to assign task to sprint')
  }
}

// Task handlers
const onTaskClick = (task) => {
  editingTask.value = { ...task }
  showTaskDetail.value = true
}

const onEditTask = (task) => {
  onTaskClick(task)
}

const onDeleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(
      t('project.confirmDeleteTask'),
      t('common.warning'),
      { type: 'warning' }
    )
    await apiDeleteTask(task.id)
    const index = backlogTasks.value.findIndex(t => t.id === task.id)
    if (index >= 0) {
      backlogTasks.value.splice(index, 1)
    }
    ElMessage.success(t('project.taskDeleted'))
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete task:', error)
      ElMessage.error('Failed to delete task')
    }
  }
}

const onSaveTask = async () => {
  if (!editingTask.value) return

  try {
    const updateData = {
      title: editingTask.value.title,
      description: editingTask.value.description,
      type: editingTask.value.type,
      priority: editingTask.value.priority,
      projectId: editingTask.value.projectId,
      statusId: statusCodeToId.value['todo'],
      estimateHours: editingTask.value.estimateHours
    }
    await apiUpdateTask(editingTask.value.id, updateData)

    const index = backlogTasks.value.findIndex(t => t.id === editingTask.value.id)
    if (index >= 0) {
      backlogTasks.value[index] = { ...backlogTasks.value[index], ...editingTask.value }
    }

    ElMessage.success(t('project.taskUpdated'))
    showTaskDetail.value = false
  } catch (error) {
    console.error('Failed to save task:', error)
    ElMessage.error('Failed to save task')
  }
}

const onQuickAdd = () => {
  showQuickAdd.value = true
}

const onQuickAddSubmit = async ({ title, type, priority }) => {
  try {
    const createData = {
      title,
      type,
      priority,
      projectId: filterProject.value || projects.value[0]?.id,
      statusId: statusCodeToId.value['todo']
    }
    const res = await apiCreateTask(createData)
    const newTask = normalizeTask(res.data || res)
    backlogTasks.value.unshift(newTask)
    ElMessage.success(t('project.taskCreated'))
    showQuickAdd.value = false
  } catch (error) {
    console.error('Failed to create task:', error)
    ElMessage.error('Failed to create task')
  }
}

// Bulk actions
const handleBulkAction = (command) => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning(t('project.selectTasksFirst'))
    return
  }

  if (command === 'assign') {
    showAssignDialog.value = true
  } else if (command === 'priority') {
    ElMessage.info(t('project.setPriorityFeature'))
  } else if (command === 'delete') {
    onBulkDelete()
  }
}

const onAssignToSprint = async () => {
  if (!selectedSprintId.value) {
    ElMessage.warning(t('project.selectSprintFirst'))
    return
  }

  try {
    const newStatusId = statusCodeToId.value['todo']
    for (const taskId of selectedTasks.value) {
      await apiUpdateTask(taskId, {
        sprintId: selectedSprintId.value,
        statusId: newStatusId
      })
    }

    backlogTasks.value = backlogTasks.value.filter(t => !selectedTasks.value.includes(t.id))
    selectedTasks.value = []
    showAssignDialog.value = false
    ElMessage.success(t('project.tasksAssignedToSprint'))
  } catch (error) {
    console.error('Failed to assign tasks:', error)
    ElMessage.error('Failed to assign tasks')
  }
}

const onBulkDelete = async () => {
  try {
    await ElMessageBox.confirm(
      t('project.confirmDeleteTasks'),
      t('common.warning'),
      { type: 'warning' }
    )
    for (const taskId of selectedTasks.value) {
      await apiDeleteTask(taskId)
    }
    backlogTasks.value = backlogTasks.value.filter(t => !selectedTasks.value.includes(t.id))
    selectedTasks.value = []
    ElMessage.success(t('project.tasksDeleted'))
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete tasks:', error)
      ElMessage.error('Failed to delete tasks')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.backlog-board-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.backlog-header-left {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.backlog-breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  font-size: 14px;
}

.breadcrumb-item {
  color: var(--pm-text-secondary);
}

.breadcrumb-current {
  color: var(--pm-text-primary);
  font-weight: 500;
}

.backlog-header-right {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.backlog-stats {
  display: flex;
  align-items: center;
  gap: var(--pm-space-xl);
  padding: var(--pm-space-lg) var(--pm-space-xl);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  margin-bottom: var(--pm-space-xl);
}

.backlog-stat {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xs);
}

.backlog-stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--pm-text-primary);
}

.backlog-stat-label {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.backlog-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xl);
}

.backlog-tabs {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
}

.backlog-priority-section {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  overflow: hidden;
}

.priority-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--pm-space-lg) var(--pm-space-xl);
  background: var(--pm-background);
  cursor: pointer;
  user-select: none;
}

.priority-header:hover {
  background: var(--pm-border-light);
}

.priority-header-left {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.priority-collapse-icon {
  transition: transform var(--pm-transition-fast);
  color: var(--pm-text-muted);
  font-size: 14px;
}

.priority-collapse-icon.collapsed {
  transform: rotate(0deg);
}

.priority-collapse-icon:not(.collapsed) {
  transform: rotate(90deg);
}

.priority-badge {
  padding: 4px 8px;
  border-radius: var(--pm-radius-sm);
  font-size: 12px;
  font-weight: 700;
}

.priority-p0 {
  background: rgba(239, 68, 68, 0.15);
  color: #EF4444;
}

.priority-p1 {
  background: rgba(249, 115, 22, 0.15);
  color: #F97316;
}

.priority-p2 {
  background: rgba(245, 158, 11, 0.15);
  color: #F59E0B;
}

.priority-p3 {
  background: rgba(148, 163, 184, 0.15);
  color: #94A3B8;
}

.priority-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--pm-text-primary);
}

.priority-count {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.priority-header-right {
  display: flex;
  align-items: center;
}

.priority-estimate {
  font-size: 13px;
  color: var(--pm-text-secondary);
  padding: 2px 8px;
  background: var(--pm-border-light);
  border-radius: var(--pm-radius-sm);
}

.priority-tasks {
  padding: var(--pm-space-md);
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.priority-empty {
  padding: var(--pm-space-xl);
  text-align: center;
  color: var(--pm-text-muted);
  font-size: 13px;
}

.backlog-task-item {
  display: flex;
  align-items: flex-start;
  gap: var(--pm-space-md);
  padding: var(--pm-space-md);
  background: var(--pm-background);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-md);
  cursor: pointer;
  transition: all var(--pm-transition-fast);
}

.backlog-task-item:hover {
  border-color: var(--pm-primary);
  box-shadow: var(--pm-shadow-sm);
}

.backlog-task-item.selected {
  border-color: var(--pm-accent);
  background: rgba(0, 212, 170, 0.05);
}

.backlog-task-item.dragging {
  opacity: 0.5;
}

.task-checkbox {
  padding-top: 2px;
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-header {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-xs);
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--pm-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-type-badge {
  padding: 2px 6px;
  border-radius: var(--pm-radius-xs);
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
}

.type-epic {
  background: rgba(139, 92, 246, 0.15);
  color: #8B5CF6;
}

.type-feature {
  background: rgba(59, 130, 246, 0.15);
  color: #3B82F6;
}

.type-story {
  background: rgba(16, 185, 129, 0.15);
  color: #10B981;
}

.type-task {
  background: rgba(148, 163, 184, 0.15);
  color: #94A3B8;
}

.type-bug {
  background: rgba(239, 68, 68, 0.15);
  color: #EF4444;
}

.task-meta {
  display: flex;
  gap: var(--pm-space-md);
  font-size: 12px;
  color: var(--pm-text-muted);
}

.task-project, .task-sprint {
  padding: 2px 6px;
  background: var(--pm-border-light);
  border-radius: var(--pm-radius-xs);
}

.task-actions {
  display: flex;
  gap: var(--pm-space-xs);
  opacity: 0;
  transition: opacity var(--pm-transition-fast);
}

.backlog-task-item:hover .task-actions {
  opacity: 1;
}

.sprint-drop-section {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-xl);
}

.sprint-drop-header h3 {
  margin: 0 0 var(--pm-space-lg) 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--pm-text-primary);
}

.sprint-drop-zones {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--pm-space-md);
}

.sprint-drop-zone {
  padding: var(--pm-space-lg);
  border: 2px dashed var(--pm-border);
  border-radius: var(--pm-radius-md);
  transition: all var(--pm-transition-fast);
}

.sprint-drop-zone.drag-over {
  border-color: var(--pm-accent);
  background: rgba(0, 212, 170, 0.05);
}

.sprint-zone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-sm);
}

.sprint-zone-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--pm-text-primary);
}

.sprint-zone-status {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: var(--pm-radius-xs);
}

.status-planning {
  background: rgba(148, 163, 184, 0.15);
  color: #94A3B8;
}

.status-active {
  background: rgba(16, 185, 129, 0.15);
  color: #10B981;
}

.status-completed {
  background: rgba(59, 130, 246, 0.15);
  color: #3B82F6;
}

.sprint-zone-count {
  font-size: 12px;
  color: var(--pm-text-muted);
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
