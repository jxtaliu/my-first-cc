<template>
  <div class="team-board-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('project.teamBoard') }}</h1>
        <p class="pm-text-small">{{ $t('project.teamBoardDesc') }}</p>
      </div>
      <div class="team-board-header-right">
        <el-select v-model="filterProject" :placeholder="$t('project.allProjects')" clearable style="width: 180px">
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
        <el-button type="primary" @click="onCreateTask">
          <el-icon><Plus /></el-icon>
          {{ $t('project.createTask') }}
        </el-button>
      </div>
    </div>

    <!-- Team Overview Stats -->
    <div class="team-stats-row">
      <div class="team-stat-card">
        <div class="team-stat-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="team-stat-info">
          <span class="team-stat-value">{{ teamMembers.length }}</span>
          <span class="team-stat-label">{{ $t('project.teamMembers') }}</span>
        </div>
      </div>
      <div class="team-stat-card">
        <div class="team-stat-icon tasks">
          <el-icon><List /></el-icon>
        </div>
        <div class="team-stat-info">
          <span class="team-stat-value">{{ totalTasks }}</span>
          <span class="team-stat-label">{{ $t('project.totalTasks') }}</span>
        </div>
      </div>
      <div class="team-stat-card">
        <div class="team-stat-icon completed">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="team-stat-info">
          <span class="team-stat-value">{{ completedTasks }}</span>
          <span class="team-stat-label">{{ $t('project.completed') }}</span>
        </div>
      </div>
      <div class="team-stat-card">
        <div class="team-stat-icon hours">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="team-stat-info">
          <span class="team-stat-value">{{ totalHours }}h</span>
          <span class="team-stat-label">{{ $t('project.totalHours') }}</span>
        </div>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="team-board-filters">
      <div class="filter-left">
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

    <!-- Team Swimlanes -->
    <div class="team-board-content" v-loading="loading">
      <Swimlane
        v-for="swimlane in teamSwimlanes"
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

      <!-- Empty State -->
      <div v-if="teamSwimlanes.length === 0 && !loading" class="team-empty-state">
        <el-empty :description="$t('project.noTasks')" />
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
                  v-for="project in projects"
                  :key="project.id"
                  :label="project.name"
                  :value="project.id"
                />
              </el-select>
            </el-form-item>
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
          </div>

          <div class="form-row">
            <el-form-item :label="$t('project.estimateHours')" class="form-col">
              <el-input-number v-model="editingTask.estimateHours" :min="0" :step="0.5" />
            </el-form-item>
            <el-form-item :label="$t('project.remainingHours')" class="form-col">
              <el-input-number v-model="editingTask.remainingHours" :min="0" :step="0.5" />
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
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus, User, List, CircleCheck, Clock } from '@element-plus/icons-vue'
import Swimlane from '@/components/kanban/Swimlane.vue'
import QuickAddDialog from '@/components/kanban/QuickAddDialog.vue'
import { useKanban } from '@/composables/useKanban'
import { useAuthStore } from '@/stores/auth'
import { getTasksByAssignee, moveTask as apiMoveTask, createTask as apiCreateTask, updateTask as apiUpdateTask } from '@/api/task'
import { getProjects, getProjectMembers } from '@/api/project'

const { t } = useI18n()
const authStore = useAuthStore()

// Kanban composable
const { columns: kanbanColumnsFromApi, loadTaskStatuses, normalizeTask, statusCodeToId, statusIdToCode } = useKanban()

// Refs
const projects = ref([])
const allTasks = ref([])
const teamMembers = ref([])
const filterProject = ref(null)
const searchQuery = ref('')
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
  return [
    { id: 'todo', title: '待办', status: 'todo', color: '#94A3B8' },
    { id: 'in_progress', title: '进行中', status: 'in_progress', color: '#3B82F6', wipLimit: 5 },
    { id: 'development', title: '开发完成', status: 'development', color: '#8B5CF6', wipLimit: 3 },
    { id: 'testing', title: '测试中', status: 'testing', color: '#F59E0B', wipLimit: 3 },
    { id: 'done', title: '已完成', status: 'done', color: '#10B981' }
  ]
})

// Filter tasks based on project and search
const filteredTasks = computed(() => {
  let result = allTasks.value
  if (filterProject.value) {
    result = result.filter(task => task.projectId === filterProject.value)
  }
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(task =>
      task.title?.toLowerCase().includes(query) ||
      task.description?.toLowerCase().includes(query)
    )
  }
  return result
})

// Team swimlanes
const teamSwimlanes = computed(() => {
  const groups = {}

  filteredTasks.value.forEach(task => {
    const key = task.assignee || 'unassigned'
    const label = task.assigneeName || task.assignee || 'Unassigned'

    if (!groups[key]) {
      groups[key] = {
        key,
        label,
        tasks: [],
        totalEstimate: 0,
        completedCount: 0
      }
    }
    groups[key].tasks.push(task)
    groups[key].totalEstimate += task.estimateHours || 0
    if (task.status === 'done') {
      groups[key].completedCount++
    }
  })

  return Object.values(groups).sort((a, b) => {
    const aHasAssignee = a.key !== 'unassigned'
    const bHasAssignee = b.key !== 'unassigned'
    if (aHasAssignee && !bHasAssignee) return -1
    if (!aHasAssignee && bHasAssignee) return 1
    return b.totalEstimate - a.totalEstimate
  })
})

// Statistics
const totalTasks = computed(() => filteredTasks.value.length)
const completedTasks = computed(() => filteredTasks.value.filter(t => t.status === 'done').length)
const totalHours = computed(() => filteredTasks.value.reduce((sum, t) => sum + (t.estimateHours || 0), 0))

// Load all tasks and projects
async function loadData() {
  loading.value = true
  try {
    await loadTaskStatuses(null)

    const projectsRes = await getProjects()
    projects.value = projectsRes.data || projectsRes || []

    const allTasksData = []
    const memberMap = {}

    for (const project of projects.value) {
      try {
        const membersRes = await getProjectMembers(project.id)
        const members = membersRes.data || membersRes || []
        members.forEach(member => {
          if (!memberMap[member.userId]) {
            memberMap[member.userId] = {
              userId: member.userId,
              userName: member.userName || member.realName || member.username
            }
          }
        })
      } catch (e) {
        console.warn(`Failed to load members for project ${project.id}`)
      }
    }

    teamMembers.value = Object.values(memberMap)

    const tasksPromises = projects.value.map(project => {
      return getTasksByAssignee(null).catch(() => ({ data: [] }))
    })

    const results = await Promise.all(tasksPromises)
    results.forEach(res => {
      const tasks = res.data || res || []
      allTasksData.push(...tasks)
    })

    allTasks.value = allTasksData.map(task => {
      const normalized = normalizeTask(task)
      const member = memberMap[normalized.assignee]
      if (member) {
        normalized.assigneeName = member.userName
      }
      return normalized
    })
  } catch (error) {
    console.error('Failed to load data:', error)
    ElMessage.error('Failed to load data')
  } finally {
    loading.value = false
  }
}

const onTaskClick = (task) => {
  editingTask.value = { ...task }
  showTaskDetail.value = true
}

const onTaskDrop = async ({ taskId, targetStatus }) => {
  // dataTransfer stores taskId as string, but task.id is number
  const task = allTasks.value.find(t => t.id === Number(taskId))
  if (!task) return

  const oldStatus = task.status
  if (oldStatus === targetStatus) return

  try {
    const newStatusId = statusCodeToId.value[targetStatus]
    await apiMoveTask(Number(taskId), { statusId: newStatusId, sprintId: task.sprintId })
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
  try {
    const createData = {
      title,
      type,
      priority,
      statusId: statusCodeToId.value[status],
      projectId: filterProject.value || projects.value[0]?.id
    }
    const res = await apiCreateTask(createData)
    const newTask = normalizeTask(res.data || res)
    allTasks.value.push(newTask)
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
        remainingHours: editingTask.value.remainingHours
      }
      await apiUpdateTask(editingTask.value.id, updateData)

      const index = allTasks.value.findIndex(t => t.id === editingTask.value.id)
      if (index !== -1) {
        allTasks.value[index] = { ...allTasks.value[index], ...editingTask.value }
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
  loadData()
})
</script>

<style scoped>
.team-board-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.team-board-header-right {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
}

.team-stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--pm-space-lg);
  margin-bottom: var(--pm-space-xl);
}

.team-stat-card {
  display: flex;
  align-items: center;
  gap: var(--pm-space-lg);
  padding: var(--pm-space-xl);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
}

.team-stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--pm-radius-md);
  background: rgba(0, 212, 170, 0.1);
  color: var(--pm-accent);
  font-size: 20px;
}

.team-stat-icon.tasks {
  background: rgba(59, 130, 246, 0.1);
  color: var(--pm-status-in-progress);
}

.team-stat-icon.completed {
  background: rgba(16, 185, 129, 0.1);
  color: var(--pm-status-done);
}

.team-stat-icon.hours {
  background: rgba(139, 92, 246, 0.1);
  color: var(--pm-status-development);
}

.team-stat-info {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xs);
}

.team-stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--pm-text-primary);
}

.team-stat-label {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.team-board-filters {
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

.team-board-content {
  flex: 1;
  overflow-y: auto;
}

.team-empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--pm-space-xxl);
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
