<template>
  <div class="task-board-page pm-page">
    <!-- Page Header -->
    <div class="task-board-header">
      <div class="header-left">
        <h1 class="pm-heading-1">{{ $t('nav.taskBoard') }}</h1>
      </div>
    </div>

    <!-- Toolbar -->
    <div class="task-board-toolbar">
      <el-input
        v-model="searchText"
        :placeholder="$t('project.searchTasks')"
        clearable
        class="search-input"
        @input="onSearchChange"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="filterType" :placeholder="$t('project.filterByType')" clearable class="filter-select">
        <el-option :label="$t('project.allTypes')" value="" />
        <el-option :label="$t('project.type_epic')" value="EPIC" />
        <el-option :label="$t('project.type_feature')" value="FEATURE" />
        <el-option :label="$t('project.type_story')" value="STORY" />
        <el-option :label="$t('project.type_task')" value="TASK" />
        <el-option :label="$t('project.type_bug')" value="BUG" />
        <el-option :label="$t('project.type_subtask')" value="SUBTASK" />
      </el-select>

      <el-select v-model="filterPriority" :placeholder="$t('project.filterByPriority')" clearable class="filter-select">
        <el-option :label="$t('project.allPriorities')" value="" />
        <el-option :label="$t('project.p0')" value="P0" />
        <el-option :label="$t('project.p1')" value="P1" />
        <el-option :label="$t('project.p2')" value="P2" />
        <el-option :label="$t('project.p3')" value="P3" />
      </el-select>

      <div class="toolbar-divider"></div>

      <el-select v-model="swimlaneMode" :placeholder="$t('project.swimlaneMode')" class="swimlane-select">
        <el-option :label="$t('project.noSwimlane')" value="none" />
        <el-option :label="$t('project.byAssignee')" value="assignee" />
        <el-option :label="$t('project.byPriority')" value="priority" />
        <el-option :label="$t('project.byType')" value="type" />
        <el-option :label="$t('project.byDueDate')" value="dueDate" />
        <el-option :label="$t('project.byProgress')" value="progress" />
      </el-select>
    </div>

    <!-- Kanban Board -->
    <div class="task-board-content" v-loading="loading">
      <!-- No Swimlane Mode -->
      <div v-if="swimlaneMode === 'none'" class="kanban-board">
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
          :is-task-draggable="canChangeStatus"
          :get-subtask-tooltip="getSubtaskDistribution"
          :get-computed-progress="getTaskProgress"
          @task-click="onTaskClick"
          @task-drop="onTaskDrop"
        />
      </div>

      <!-- Swimlane Mode: By Assignee -->
      <div v-else-if="swimlaneMode === 'assignee'" class="swimlane-board">
        <div class="swimlane-header">
          <div class="swimlane-label-col">{{ $t('project.assignee') }}</div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-column-header"
              :style="{ borderTopColor: column.color }"
            >
              {{ column.title }}
            </div>
          </div>
        </div>
        <div
          v-for="(tasks, assigneeId) in tasksByAssignee"
          :key="assigneeId"
          class="swimlane-row"
        >
          <div class="swimlane-label-col">
            <div class="assignee-info">
              <el-avatar :size="24" class="assignee-avatar">
                {{ getAssigneeName(assigneeId).charAt(0) }}
              </el-avatar>
              <span class="assignee-name">{{ getAssigneeName(assigneeId) }}</span>
            </div>
          </div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-cell"
              @dragover.prevent="onCellDragOver"
              @drop="(e) => onCellDrop(e, column.status, assigneeId)"
            >
              <TaskCard
                v-for="task in getTasksByAssigneeAndStatus(assigneeId, column.status)"
                :key="task.id"
                :task="task"
                :show-progress="true"
                :is-draggable="canChangeStatus(task)"
                :computed-progress="getTaskProgress(task)"
                :subtask-tooltip="getSubtaskDistribution(task)"
                @click="onTaskClick"
                @dragstart="(e) => onTaskDragStart(e, task)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Swimlane Mode: By Priority -->
      <div v-else-if="swimlaneMode === 'priority'" class="swimlane-board">
        <div class="swimlane-header">
          <div class="swimlane-label-col">{{ $t('project.priority') }}</div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-column-header"
              :style="{ borderTopColor: column.color }"
            >
              {{ column.title }}
            </div>
          </div>
        </div>
        <div
          v-for="priority in priorities"
          :key="priority"
          class="swimlane-row"
        >
          <div class="swimlane-label-col">
            <div class="priority-info">
              <span class="priority-badge" :class="'priority-' + priority.toLowerCase()">{{ priority }}</span>
            </div>
          </div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-cell"
              @dragover.prevent="onCellDragOver"
              @drop="(e) => onCellDrop(e, column.status, null, priority)"
            >
              <TaskCard
                v-for="task in getTasksByPriorityAndStatus(priority, column.status)"
                :key="task.id"
                :task="task"
                :show-progress="true"
                :is-draggable="canChangeStatus(task)"
                :computed-progress="getTaskProgress(task)"
                :subtask-tooltip="getSubtaskDistribution(task)"
                @click="onTaskClick"
                @dragstart="(e) => onTaskDragStart(e, task)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Swimlane Mode: By Type -->
      <div v-else-if="swimlaneMode === 'type'" class="swimlane-board">
        <div class="swimlane-header">
          <div class="swimlane-label-col">{{ $t('project.type') || 'Type' }}</div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-column-header"
              :style="{ borderTopColor: column.color }"
            >
              {{ column.title }}
            </div>
          </div>
        </div>
        <div
          v-for="type in taskTypes"
          :key="type"
          class="swimlane-row"
        >
          <div class="swimlane-label-col">
            <span class="type-label">{{ getTypeLabel(type) }}</span>
          </div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-cell"
              @dragover.prevent="onCellDragOver"
              @drop="(e) => onCellDropByType(e, column.status, type)"
            >
              <TaskCard
                v-for="task in getTasksByTypeAndStatus(type, column.status)"
                :key="task.id"
                :task="task"
                :show-progress="true"
                :is-draggable="canChangeStatus(task)"
                :computed-progress="getTaskProgress(task)"
                :subtask-tooltip="getSubtaskDistribution(task)"
                @click="onTaskClick"
                @dragstart="(e) => onTaskDragStart(e, task)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Swimlane Mode: By Due Date -->
      <div v-else-if="swimlaneMode === 'dueDate'" class="swimlane-board">
        <div class="swimlane-header">
          <div class="swimlane-label-col">{{ $t('project.dueDate') || 'Due Date' }}</div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-column-header"
              :style="{ borderTopColor: column.color }"
            >
              {{ column.title }}
            </div>
          </div>
        </div>
        <div
          v-for="period in dueDatePeriods"
          :key="period.key"
          class="swimlane-row"
        >
          <div class="swimlane-label-col">
            <span class="due-date-label" :class="'period-' + period.key">{{ period.label }}</span>
          </div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-cell"
              @dragover.prevent="onCellDragOver"
              @drop="(e) => onCellDropByDueDate(e, column.status, period.key)"
            >
              <TaskCard
                v-for="task in getTasksByDueDateAndStatus(period.key, column.status)"
                :key="task.id"
                :task="task"
                :show-progress="true"
                :is-draggable="canChangeStatus(task)"
                :computed-progress="getTaskProgress(task)"
                :subtask-tooltip="getSubtaskDistribution(task)"
                @click="onTaskClick"
                @dragstart="(e) => onTaskDragStart(e, task)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Swimlane Mode: By Progress -->
      <div v-else-if="swimlaneMode === 'progress'" class="swimlane-board">
        <div class="swimlane-header">
          <div class="swimlane-label-col">{{ $t('project.progress') || 'Progress' }}</div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-column-header"
              :style="{ borderTopColor: column.color }"
            >
              {{ column.title }}
            </div>
          </div>
        </div>
        <div
          v-for="range in progressRanges"
          :key="range.key"
          class="swimlane-row"
        >
          <div class="swimlane-label-col">
            <span class="progress-label">{{ range.label }}</span>
          </div>
          <div class="swimlane-timeline-col">
            <div
              v-for="column in kanbanColumns"
              :key="column.id"
              class="swimlane-cell"
              @dragover.prevent="onCellDragOver"
              @drop="(e) => onCellDropByProgress(e, column.status, range.key)"
            >
              <TaskCard
                v-for="task in getTasksByProgressAndStatus(range.key, column.status)"
                :key="task.id"
                :task="task"
                :show-progress="true"
                :is-draggable="canChangeStatus(task)"
                :computed-progress="getTaskProgress(task)"
                :subtask-tooltip="getSubtaskDistribution(task)"
                @click="onTaskClick"
                @dragstart="(e) => onTaskDragStart(e, task)"
              />
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
      class="pm-dialog"
    >
      <div class="task-detail-content" v-if="editingTask">
        <el-form :model="editingTask" label-position="top" :disabled="true">
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
        <el-button @click="showTaskDetail = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import KanbanColumn from '@/components/kanban/KanbanColumn.vue'
import TaskCard from '@/components/kanban/TaskCard.vue'
import { useKanban } from '@/composables/useKanban'
import { useProjectStore } from '@/stores/project'
import { getTasksByProject as apiGetTasksByProject, moveTask as apiMoveTask, updateTask as apiUpdateTask } from '@/api/task'
import { getProjectMembers } from '@/api/project'

const { t, locale } = useI18n()
const projectStore = useProjectStore()

// Kanban composable
const { columns: kanbanColumnsFromApi, loadTaskStatuses, normalizeTask, statusCodeToId, statusIdToCode } = useKanban()

// Refs
const tasks = ref([])
const members = ref([])
const loading = ref(false)
const showTaskDetail = ref(false)
const editingTask = ref(null)
// Filters
const searchText = ref('')
const filterType = ref('')
const filterPriority = ref('')
const swimlaneMode = ref('none')

// Priority list
const priorities = ['P0', 'P1', 'P2', 'P3']

// Task types list
const taskTypes = ['EPIC', 'FEATURE', 'STORY', 'TASK', 'SUBTASK', 'BUG']

// Due date periods
const dueDatePeriods = computed(() => [
  { key: 'overdue', label: t('project.overdue') },
  { key: 'today', label: t('project.dueToday') },
  { key: 'thisWeek', label: t('project.thisWeek') },
  { key: 'thisMonth', label: t('project.thisMonth') },
  { key: 'later', label: t('project.later') },
  { key: 'noDate', label: t('project.noDueDate') }
])

// Progress ranges
const progressRanges = computed(() => [
  { key: 'notStarted', label: t('project.notStarted'), min: 0, max: 0 },
  { key: 'inProgress', label: t('project.inProgress'), min: 1, max: 99 },
  { key: 'completed', label: t('project.completed'), min: 100, max: 100 }
])

// Dragging state
const draggingTask = ref(null)

// Kanban columns
const kanbanColumns = computed(() => {
  if (kanbanColumnsFromApi.value.length > 0) {
    return kanbanColumnsFromApi.value
  }
  return [
    { id: 'todo', title: t('project.todo'), status: 'todo', color: '#94A3B8' },
    { id: 'in_progress', title: t('project.inProgress'), status: 'in_progress', color: '#3B82F6', wipLimit: 5 },
    { id: 'in_review', title: t('project.inReview'), status: 'in_review', color: '#8B5CF6', wipLimit: 3 },
    { id: 'done', title: t('project.done'), status: 'done', color: '#10B981' }
  ]
})

// Filtered tasks
const filteredTasks = computed(() => {
  let result = tasks.value

  // Search filter
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    result = result.filter(task =>
      task.title?.toLowerCase().includes(search) ||
      task.description?.toLowerCase().includes(search)
    )
  }

  // Type filter
  if (filterType.value) {
    result = result.filter(task => task.type === filterType.value)
  }

  // Priority filter
  if (filterPriority.value) {
    result = result.filter(task => task.priority === filterPriority.value)
  }

  return result
})

// Parent types that have derived status from subtasks
const parentTypes = ['EPIC', 'FEATURE', 'STORY']

// Leaf types that should be counted
const leafTypes = ['TASK', 'SUBTASK', 'BUG']

// Build a map of id -> children (direct children only)
const childrenMap = computed(() => {
  const map = {}
  tasks.value.forEach(task => {
    if (task.parentId) {
      if (!map[task.parentId]) map[task.parentId] = []
      map[task.parentId].push(task)
    }
  })
  return map
})

// Recursively get all leaf descendants (Task/Subtask/Bug) for a given task
function getAllLeafDescendants(taskId) {
  const leaves = []
  const directChildren = childrenMap.value[taskId] || []
  directChildren.forEach(child => {
    if (leafTypes.includes(child.type)) {
      // It's a leaf node, count it
      leaves.push(child)
    } else {
      // It's an intermediate node (EPIC/FEATURE/STORY), recurse
      leaves.push(...getAllLeafDescendants(child.id))
    }
  })
  return leaves
}

// Compute subtask info for each task (count only leaf nodes: Task/Subtask/Bug)
const subtaskInfoMap = computed(() => {
  const infoMap = {}

  // For each parent type task (EPIC, FEATURE, STORY), compute recursive leaf stats
  tasks.value.forEach(task => {
    if (parentTypes.includes(task.type)) {
      const leafDescendants = getAllLeafDescendants(task.id)
      const total = leafDescendants.length

      if (total === 0) {
        infoMap[task.id] = {
          total: 0,
          progress: 0,
          statusCount: { todo: 0, in_progress: 0, in_review: 0, done: 0 },
          completedCount: 0
        }
        return
      }

      const statusCount = {
        todo: 0,
        in_progress: 0,
        in_review: 0,
        done: 0
      }
      let completedCount = 0

      leafDescendants.forEach(child => {
        const status = child.status?.toLowerCase() || 'todo'
        if (statusCount[status] !== undefined) statusCount[status]++
        else statusCount['todo']++
        if (status === 'done') completedCount++
      })

      const progress = Math.round((completedCount / total) * 100)
      infoMap[task.id] = {
        total,
        progress,
        statusCount,
        completedCount
      }
    }
  })

  return infoMap
})

// Get progress for a task (derived from subtasks for parent types)
const getTaskProgress = (task) => {
  if (!parentTypes.includes(task.type)) return task.progress || 0
  const info = subtaskInfoMap.value[task.id]
  return info ? info.progress : 0
}

// Get derived status for parent types (Epic/Feature/Story)
const getDerivedStatus = (task) => {
  // Only apply derived status logic for parent types
  if (!parentTypes.includes(task.type)) return task.status

  const info = subtaskInfoMap.value[task.id]
  if (!info || info.total === 0) return 'todo'

  const { statusCount, total } = info
  const { todo, in_progress, in_review, done } = statusCount

  // Priority: IN_REVIEW > IN_PROGRESS > (DONE or TODO)
  // 1. If any leaf is IN_REVIEW
  if (in_review > 0) return 'in_review'

  // 2. If any leaf is IN_PROGRESS
  if (in_progress > 0) return 'in_progress'

  // 3. If all leaves are DONE (100%)
  if (done === total) return 'done'

  // 4. Otherwise (all TODO, or mix of TODO and some DONE but not all)
  return 'todo'
}

// Get subtask distribution text for tooltip
const getSubtaskDistribution = (task) => {
  if (!parentTypes.includes(task.type)) return ''
  const info = subtaskInfoMap.value[task.id]
  if (!info || info.total === 0) return `${t('project.' + task.type.toLowerCase())}: 0 ${t('project.tasks')}`
  const { statusCount, total, progress } = info
  const parts = []
  if (statusCount.todo > 0) parts.push(`${statusCount.todo} ${t('project.todo')}`)
  if (statusCount.in_progress > 0) parts.push(`${statusCount.in_progress} ${t('project.inProgress')}`)
  if (statusCount.in_review > 0) parts.push(`${statusCount.in_review} ${t('project.inReview')}`)
  if (statusCount.done > 0) parts.push(`${statusCount.done} ${t('project.done')}`)
  return `${t('project.' + task.type.toLowerCase())}: ${total} ${t('project.tasks')} (${progress}%)\n${parts.join(', ')}`
}

// Check if task type allows direct status change
const canChangeStatus = (task) => {
  return !parentTypes.includes(task.type)
}

// Tasks by status (for no swimlane mode) - using derived status for parent types
const getTasksByStatus = (status) => {
  return filteredTasks.value.filter(task => {
    const effectiveStatus = getDerivedStatus(task)
    return effectiveStatus === status
  })
}

// Tasks by assignee (for assignee swimlane mode)
const tasksByAssignee = computed(() => {
  const groups = {}
  filteredTasks.value.forEach(task => {
    const key = String(task.assigneeId || 'unassigned')
    if (!groups[key]) groups[key] = []
    groups[key].push(task)
  })
  return groups
})

const getAssigneeName = (assigneeId) => {
  if (assigneeId === 'unassigned') return t('project.unassigned')
  const member = members.value.find(m => String(m.userId) === String(assigneeId))
  return member?.realName || member?.username || assigneeId
}

const getTasksByAssigneeAndStatus = (assigneeId, status) => {
  return filteredTasks.value.filter(task => {
    const effectiveStatus = getDerivedStatus(task)
    return String(task.assigneeId || 'unassigned') === String(assigneeId) && effectiveStatus === status
  })
}

// Tasks by priority (for priority swimlane mode)
const getTasksByPriorityAndStatus = (priority, status) => {
  return filteredTasks.value.filter(task => {
    const effectiveStatus = getDerivedStatus(task)
    return task.priority === priority && effectiveStatus === status
  })
}

// Get type label
const getTypeLabel = (type) => {
  const typeKey = type.toLowerCase()
  // Try type_* key first, then fall back to the type itself
  return t(`project.type_${typeKey}`) || t(`project.${typeKey}`) || type
}

// Tasks by type and status (for type swimlane mode)
const getTasksByTypeAndStatus = (type, status) => {
  return filteredTasks.value.filter(task => {
    const effectiveStatus = getDerivedStatus(task)
    return task.type === type && effectiveStatus === status
  })
}

// Classify due date into period
const classifyDueDate = (dueDate) => {
  if (!dueDate) return 'noDate'
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const due = new Date(dueDate)
  due.setHours(0, 0, 0, 0)
  const diffDays = Math.ceil((due - today) / (1000 * 60 * 60 * 24))
  if (diffDays < 0) return 'overdue'
  if (diffDays === 0) return 'today'
  if (diffDays <= 7) return 'thisWeek'
  if (diffDays <= 30) return 'thisMonth'
  return 'later'
}

// Tasks by due date period and status (for due date swimlane mode)
const getTasksByDueDateAndStatus = (period, status) => {
  return filteredTasks.value.filter(task => {
    const effectiveStatus = getDerivedStatus(task)
    if (effectiveStatus !== status) return false
    const taskPeriod = classifyDueDate(task.dueDate)
    return taskPeriod === period
  })
}

// Tasks by progress range and status (for progress swimlane mode)
const getTasksByProgressAndStatus = (rangeKey, status) => {
  return filteredTasks.value.filter(task => {
    const effectiveStatus = getDerivedStatus(task)
    if (effectiveStatus !== status) return false
    // Use derived progress for parent types, task.progress for leaf types
    const progress = parentTypes.includes(task.type) ? getTaskProgress(task) : (task.progress || 0)
    if (rangeKey === 'notStarted') return progress === 0
    if (rangeKey === 'inProgress') return progress > 0 && progress < 100
    if (rangeKey === 'completed') return progress === 100
    return false
  })
}

// Load project data
async function loadProjectData() {
  const projectId = projectStore.currentProjectId
  if (!projectId) {
    ElMessage.error(t('project.projectIdRequired'))
    return
  }

  loading.value = true
  try {
    // Load project members for assignee names
    const membersRes = await getProjectMembers(projectId)
    members.value = membersRes.data || membersRes || []

    await loadTaskStatuses(projectId)
    await loadTasks()
  } catch (error) {
    console.error('Failed to load project data:', error)
    ElMessage.error(t('project.loadProjectFailed'))
  } finally {
    loading.value = false
  }
}

// Load tasks for project
async function loadTasks() {
  const projectId = projectStore.currentProjectId
  if (!projectId) return

  try {
    const res = await apiGetTasksByProject(projectId)
    const rawTasks = res.data || res || []
    tasks.value = rawTasks.map(task => normalizeTask(task))
  } catch (error) {
    console.error('Failed to load tasks:', error)
    ElMessage.error(t('project.loadTasksFailed'))
  }
}

// Watch for project changes
watch(() => projectStore.currentProjectId, () => {
  loadProjectData()
})

// Task click handler
const onTaskClick = (task) => {
  editingTask.value = { ...task }
  showTaskDetail.value = true
}

// Task drop handler (for no swimlane mode)
const onTaskDrop = async ({ taskId, targetStatus }) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (!task) return

  const oldStatus = task.status
  if (oldStatus === targetStatus) return

  try {
    await apiMoveTask(taskId, { status: targetStatus.toUpperCase(), projectId: projectStore.currentProjectId })
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

// Cell drop handler (for swimlane mode)
const onCellDrop = async (e, targetStatus, assigneeId, priority) => {
  if (!draggingTask.value) return

  const task = draggingTask.value
  const oldStatus = task.status

  // Check if assignee/priority matches (use string comparison)
  if (assigneeId !== null && String(task.assigneeId || 'unassigned') !== String(assigneeId)) return
  if (priority !== null && task.priority !== priority) return

  if (oldStatus === targetStatus) return

  try {
    await apiMoveTask(task.id, { status: targetStatus.toUpperCase(), projectId: projectStore.currentProjectId })
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
  } finally {
    draggingTask.value = null
  }
}

// Drag handlers for swimlane mode
const onTaskDragStart = (e, task) => {
  draggingTask.value = task
}

const onCellDragOver = (e) => {
  e.preventDefault()
}

// Cell drop handler for type swimlane mode
const onCellDropByType = async (e, targetStatus, type) => {
  if (!draggingTask.value) return
  const task = draggingTask.value
  if (task.type !== type) return
  if (task.status === targetStatus) return

  try {
    await apiMoveTask(task.id, { status: targetStatus.toUpperCase(), projectId: projectStore.currentProjectId })
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
  } finally {
    draggingTask.value = null
  }
}

// Cell drop handler for due date swimlane mode
const onCellDropByDueDate = async (e, targetStatus, period) => {
  if (!draggingTask.value) return
  const task = draggingTask.value
  const taskPeriod = classifyDueDate(task.dueDate)
  if (taskPeriod !== period) return
  if (task.status === targetStatus) return

  try {
    await apiMoveTask(task.id, { status: targetStatus.toUpperCase(), projectId: projectStore.currentProjectId })
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
  } finally {
    draggingTask.value = null
  }
}

// Cell drop handler for progress swimlane mode
const onCellDropByProgress = async (e, targetStatus, rangeKey) => {
  if (!draggingTask.value) return
  const task = draggingTask.value
  const progress = task.progress || 0
  let taskRange = ''
  if (progress === 0) taskRange = 'notStarted'
  else if (progress > 0 && progress < 100) taskRange = 'inProgress'
  else if (progress === 100) taskRange = 'completed'
  if (taskRange !== rangeKey) return
  if (task.status === targetStatus) return

  try {
    await apiMoveTask(task.id, { status: targetStatus.toUpperCase(), projectId: projectStore.currentProjectId })
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
  } finally {
    draggingTask.value = null
  }
}

// Save task
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
        projectId: projectStore.currentProjectId
      }
      await apiUpdateTask(editingTask.value.id, updateData)

      // Update local task
      const idx = tasks.value.findIndex(t => t.id === editingTask.value.id)
      if (idx !== -1) {
        tasks.value[idx] = { ...tasks.value[idx], ...editingTask.value }
      }

      ElMessage.success(t('project.taskUpdated'))
    }
    showTaskDetail.value = false
  } catch (error) {
    console.error('Failed to save task:', error)
    ElMessage.error(t('project.updateTaskFailed'))
  }
}

const onSearchChange = () => {
  // Search is reactive via computed, no extra action needed
}

onMounted(() => {
  loadProjectData()
})
</script>

<style scoped>
.task-board-page {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
  padding-bottom: var(--pm-space-xl);
}

.task-board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--pm-space-sm);
}

.task-board-header .pm-heading-1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--pm-text-primary);
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.task-board-header .pm-heading-1::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, var(--pm-primary), var(--pm-accent));
  border-radius: 2px;
}

.task-board-toolbar {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-md) var(--pm-space-lg);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.search-input {
  width: 220px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: none;
  border: 1px solid var(--pm-border);
  transition: all 0.2s;
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--pm-primary);
  box-shadow: 0 0 0 2px rgba(0, 212, 170, 0.1);
}

.filter-select {
  width: 140px;
}

.filter-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: none;
  border: 1px solid var(--pm-border);
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: linear-gradient(180deg, transparent, var(--pm-border), transparent);
  margin: 0 var(--pm-space-xs);
}

.swimlane-select {
  width: 130px;
  margin-left: auto;
}

.swimlane-select :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: none;
  border: 1px solid var(--pm-border);
  background: var(--pm-bg);
}

.task-board-content {
  flex: 1;
  min-height: 500px;
}

/* Kanban Board */
.kanban-board {
  display: flex;
  gap: var(--pm-space-lg);
  overflow-x: auto;
  padding: var(--pm-space-md) 0 var(--pm-space-lg) 0;
  min-height: 450px;
}

/* Swimlane Board */
.swimlane-board {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.swimlane-header {
  display: flex;
  border-bottom: 2px solid var(--pm-border);
  background: linear-gradient(180deg, var(--pm-bg), var(--pm-background));
}

.swimlane-label-col {
  width: 160px;
  flex-shrink: 0;
  padding: var(--pm-space-md) var(--pm-space-lg);
  font-weight: 600;
  font-size: 13px;
  color: var(--pm-text-secondary);
  background: var(--pm-bg);
  border-right: 1px solid var(--pm-border);
  display: flex;
  align-items: center;
}

.swimlane-timeline-col {
  flex: 1;
  display: flex;
  overflow-x: auto;
}

.swimlane-column-header {
  flex: 1;
  min-width: 200px;
  padding: var(--pm-space-md) var(--pm-space-lg);
  font-size: 13px;
  font-weight: 600;
  color: var(--pm-text-primary);
  border-top: 3px solid;
  border-right: 1px solid var(--pm-border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.5);
}

.swimlane-column-header:last-child {
  border-right: none;
}

.swimlane-row {
  display: flex;
  border-bottom: 1px solid var(--pm-border-light);
  transition: background-color 0.15s;
}

.swimlane-row:hover {
  background: rgba(0, 0, 0, 0.01);
}

.swimlane-row:nth-child(even) {
  background: rgba(0, 0, 0, 0.01);
}

.swimlane-row:nth-child(even):hover {
  background: rgba(0, 0, 0, 0.02);
}

.swimlane-row:last-child {
  border-bottom: none;
}

.swimlane-cell {
  flex: 1;
  min-width: 200px;
  min-height: 100px;
  padding: var(--pm-space-md);
  border-right: 1px solid var(--pm-border-light);
  background: white;
  transition: background-color 0.2s;
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.swimlane-cell:last-child {
  border-right: none;
}

.swimlane-cell:hover {
  background: var(--pm-bg);
}

.assignee-info {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.assignee-avatar {
  background: linear-gradient(135deg, var(--pm-primary), var(--pm-accent));
  color: white;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.assignee-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-primary);
}

.priority-info {
  display: flex;
  align-items: center;
}

.priority-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.priority-badge.priority-p0 {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  color: #991b1b;
  border: 1px solid #fecaca;
}

.priority-badge.priority-p1 {
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  color: #92400e;
  border: 1px solid #fde68a;
}

.priority-badge.priority-p2 {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.priority-badge.priority-p3 {
  background: linear-gradient(135deg, #e2e8f0, #cbd5e1);
  color: #64748b;
  border: 1px solid #cbd5e1;
}

.type-label {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  background: var(--pm-bg);
  color: var(--pm-text-secondary);
  border: 1px solid var(--pm-border);
}

/* Form */
.form-row {
  display: flex;
  gap: var(--pm-space-lg);
}

.form-col {
  flex: 1;
}

.task-detail-content {
  padding: var(--pm-space-lg) 0;
}

.task-detail-content :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--pm-text-secondary);
  font-size: 13px;
}

/* Kanban Column styling improvements in global */
:deep(.pm-kanban-column) {
  border: 1px solid var(--pm-border);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s, transform 0.2s;
}

:deep(.pm-kanban-column:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.pm-kanban-column-header) {
  border-radius: 12px 12px 0 0;
  border-bottom: 1px solid var(--pm-border);
}

:deep(.pm-task-card) {
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}

:deep(.pm-task-card:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}
</style>
