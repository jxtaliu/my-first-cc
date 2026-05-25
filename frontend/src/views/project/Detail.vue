<template>
  <div class="project-detail">
    <div class="detail-header">
      <div class="header-left">
        <el-button text @click="router.push('/projects')">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2>{{ project.name }}</h2>
        <el-tag :type="project.sprintMode === 'SCRUM' ? 'success' : 'warning'">{{ project.sprintMode === 'SCRUM' ? $t('project.scrum') : $t('project.kanban') }}</el-tag>
      </div>
      <div class="header-right">
        <el-button @click="handleSettings">{{ $t('common.settings') }}</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="detail-tabs">
      <el-tab-pane name="board">
        <template #label>
          <span><el-icon><Grid /></el-icon> {{ $t('project.board') }}</span>
        </template>
        <div class="pm-kanban-board-compact">
          <KanbanColumn
            v-for="column in kanbanColumns"
            :key="column.id"
            :id="column.id"
            :title="column.title"
            :status="column.status"
            :status-color="column.color"
            :tasks="getBoardTasksByStatus(column.status)"
            :wip-limit="column.wipLimit"
            :show-progress="true"
            :allow-add="true"
            @task-click="onBoardTaskClick"
            @task-drop="onBoardTaskDrop"
            @add-task="onBoardAddTask"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane name="sprints" v-if="project.sprintMode === 'SCRUM'">
        <template #label>
          <span><el-icon><Timer /></el-icon> {{ $t('project.sprints') }}</span>
        </template>
        <div class="sprint-board-embedded">
          <!-- Sprint Tabs -->
          <el-tabs v-model="activeSprintTab" class="sprint-tabs" @tab-change="onSprintTabChange">
            <!-- Tab 1: Sprint List -->
            <el-tab-pane name="list">
              <template #label>
                <span><el-icon><List /></el-icon> {{ $t('project.sprintList') }}</span>
              </template>
              <div class="sprint-list-tab">
                <!-- Left-Right Layout in List Tab -->
                <div class="sprint-split-view">
                  <!-- Left: Sprint List Panel -->
                  <div class="sprint-list-panel">
                    <div class="panel-header">
                      <h3>{{ $t('project.sprintList') }}</h3>
                      <el-button type="primary" size="small" @click="openSprintDialog()">
                        <el-icon><Plus /></el-icon>
                      </el-button>
                    </div>
                    <div class="sprint-list-content" v-loading="sprintsLoading">
                      <div v-if="sprints.length === 0" class="empty-state-small">
                        <el-empty :description="$t('project.noSprints')" />
                      </div>
                      <div
                        v-for="sprint in sortedSprints"
                        :key="sprint.id"
                        :class="['sprint-item', { active: currentSprint?.id === sprint.id }]"
                        @click="selectSprint(sprint)"
                      >
                        <div class="sprint-item-header">
                          <span class="sprint-item-name">{{ sprint.name }}</span>
                          <el-tag :type="getSprintTagType(sprint.status)" size="small">{{ getSprintStatusLabel(sprint.status) }}</el-tag>
                        </div>
                        <div class="sprint-item-dates">
                          {{ formatDate(sprint.startDate) }} ~ {{ formatDate(sprint.endDate) }}
                        </div>
                        <div class="sprint-item-actions" @click.stop>
                          <el-button size="small" link type="primary" @click="openSprintDialog(sprint)">{{ $t('common.edit') }}</el-button>
                          <el-button v-if="sprint.status === 'PLANNING'" size="small" link type="success" @click="handleStartSprint(sprint)">{{ $t('project.startSprint') }}</el-button>
                          <el-button v-if="sprint.status === 'ACTIVE'" size="small" link type="warning" @click="handleCompleteSprint(sprint)">{{ $t('project.completeSprint') }}</el-button>
                          <el-button v-if="sprint.status === 'PLANNING'" size="small" link type="danger" @click="handleDeleteSprint(sprint)">{{ $t('common.delete') }}</el-button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Right: Sprint Gantt -->
                  <div class="sprint-gantt-panel">
                    <div class="panel-header">
                      <h3>{{ $t('project.sprintGantt') }}</h3>
                      <div class="gantt-legend">
                        <span class="legend-item"><span class="legend-dot planning"></span>{{ $t('project.sprintPlanning') }}</span>
                        <span class="legend-item"><span class="legend-dot active"></span>{{ $t('project.sprintActive') }}</span>
                        <span class="legend-item"><span class="legend-dot completed"></span>{{ $t('project.sprintCompleted') }}</span>
                      </div>
                    </div>
                    <div class="sprint-gantt-content">
                      <div class="gantt-timeline" v-if="sprints.length > 0">
                        <div class="gantt-header">
                          <div class="gantt-dates-row">
                            <div class="gantt-label"></div>
                            <div class="gantt-dates">
                              <div
                                v-for="date in ganttDates"
                                :key="date"
                                class="gantt-date-cell"
                              >
                                {{ date }}
                              </div>
                            </div>
                          </div>
                        </div>
                        <div class="gantt-body">
                          <div
                            v-for="sprint in sortedSprints"
                            :key="sprint.id"
                            class="gantt-row"
                          >
                            <div class="gantt-row-label" @click="switchToSprintTab(sprint.id)">
                              {{ sprint.name }}
                            </div>
                            <div class="gantt-row-bars">
                              <div
                                class="gantt-bar"
                                :class="['status-' + sprint.status.toLowerCase(), { active: currentSprint?.id === sprint.id }]"
                                :style="getGanttBarStyle(sprint)"
                                @click="switchToSprintTab(sprint.id)"
                              >
                                <span class="gantt-bar-label">{{ sprint.name }}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div v-else class="empty-state-small">
                        <el-empty :description="$t('project.noSprints')" />
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- Sprint Detail Tabs (one per sprint) -->
            <el-tab-pane
              v-for="sprint in sortedSprints"
              :key="sprint.id"
              :name="sprint.id.toString()"
            >
              <template #label>
                <span>{{ sprint.name }}</span>
              </template>
              <div class="sprint-detail-tab">
                <div class="sprint-info-banner" v-if="currentSprint && currentSprint.id === sprint.id">
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
                    <span class="stat-item">{{ $t('project.totalTasks') }}: {{ sprintTasks.length }}</span>
                    <span class="stat-item">{{ $t('project.completed') }}: {{ completedTaskCount }}</span>
                    <span class="stat-item">{{ $t('project.remaining') }}: {{ remainingTaskCount }}</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>

          <!-- Kanban Board (only show when sprint is selected and not on list tab) -->
          <div class="pm-kanban-board-compact" v-if="currentSprint && activeSprintTab !== 'list'">
            <KanbanColumn
              v-for="column in kanbanColumns"
              :key="column.id"
              :id="column.id"
              :title="column.title"
              :status="column.status"
              :status-color="column.color"
              :tasks="getSprintTasksByStatus(column.status)"
              :wip-limit="column.wipLimit"
              :show-progress="true"
              :allow-add="true"
              @task-click="onTaskClick"
              @task-drop="onTaskDrop"
              @add-task="onBoardAddTask"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane name="members">
        <template #label>
          <span><el-icon><User /></el-icon> {{ $t('project.members') }}</span>
        </template>
        <div class="members-view">
          <div class="page-header">
            <div class="page-header-left">
              <h3>{{ $t('project.members') }}</h3>
            </div>
            <div class="page-header-right">
              <el-button type="primary" @click="showMemberDialog = true">
                <el-icon><Plus /></el-icon> {{ $t('project.addMember') }}
              </el-button>
            </div>
          </div>
          <el-table :data="members" style="width: 100%" v-loading="membersLoading">
            <el-table-column :label="$t('project.member')" min-width="200">
              <template #default="{ row }">
                <div class="member-cell">
                  <el-avatar :size="28" class="member-cell-avatar">
                    {{ row.real_name?.charAt(0) || row.username?.charAt(0) || '?' }}
                  </el-avatar>
                  <span class="member-cell-name">{{ row.real_name || row.username }}</span>
                  <span class="member-cell-email">{{ row.email }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="$t('project.role')" width="180">
              <template #default="{ row }">
                <el-select v-model="row.role_id" @change="handleRoleChange(row)" style="width: 100%">
                  <el-option v-for="role in roles" :key="role.roleId" :label="role.name" :value="role.roleId" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="$t('project.joinedAt')" width="120">
              <template #default="{ row }">
                {{ formatDate(row.joined_at) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('project.actions')" width="100" align="center">
              <template #default="{ row }">
                <el-button text type="danger" @click="handleRemoveMember(row)">{{ $t('project.remove') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane name="status-config">
        <template #label>
          <span><el-icon><Grid /></el-icon> {{ $t('project.taskStatusConfig') }}</span>
        </template>
        <div class="status-config-view">
          <StatusConfig :project-id="project.projectId" />
        </div>
      </el-tab-pane>

      <!-- Quick Add Task Dialog -->
      <QuickAddDialog
        v-model="showQuickAdd"
        :status="quickAddStatus"
        @submit="onQuickAddSubmit"
      />
    </el-tabs>

    <el-dialog v-model="taskDialogVisible" :title="editingTask?.id ? $t('project.editTask') : $t('project.newTask')" width="600px">
      <el-form :model="taskForm" :rules="taskRules" ref="taskFormRef" label-width="100px">
        <el-form-item :label="$t('project.taskId')" prop="taskId">
          <el-input v-model="taskForm.taskId" :disabled="!!editingTask?.id" :placeholder="editingTask?.id ? '' : $t('project.autoGenerated')" />
        </el-form-item>
        <el-form-item :label="$t('project.title')" prop="title">
          <el-input v-model="taskForm.title" />
        </el-form-item>
        <el-form-item :label="$t('project.description')" prop="description">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('project.taskType')" prop="type">
              <el-select v-model="taskForm.type" style="width: 100%">
                <el-option v-for="t in taskTypes" :key="t.value" :label="locale === 'zh-CN' ? t.nameZh : t.name" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('project.priority')" prop="priority">
              <el-select v-model="taskForm.priority" style="width: 100%">
                <el-option v-for="p in priorities" :key="p.value" :label="locale === 'zh-CN' ? p.nameZh : p.name" :value="p.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="project.sprintMode === 'SCRUM'">
          <el-col :span="12">
            <el-form-item :label="$t('project.sprint')" prop="sprintId">
              <el-select v-model="taskForm.sprintId" :placeholder="$t('project.selectSprint')" style="width: 100%">
                <el-option v-for="s in sprints" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('project.assignee')" prop="assigneeId">
              <el-select v-model="taskForm.assigneeId" :placeholder="$t('project.selectAssignee')" style="width: 100%">
                <el-option v-for="m in members" :key="m.id" :label="m.username" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="project.sprintMode !== 'SCRUM'">
          <el-col :span="12">
            <el-form-item :label="$t('project.assignee')" prop="assigneeId">
              <el-select v-model="taskForm.assigneeId" :placeholder="$t('project.selectAssignee')" style="width: 100%">
                <el-option v-for="m in members" :key="m.id" :label="m.username" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleTaskSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="sprintDialogVisible" :title="editingSprint ? $t('project.editSprint') : $t('project.createSprint')" width="600px">
      <el-form :model="sprintForm" :rules="sprintRules" ref="sprintFormRef" label-width="100px">
        <el-form-item :label="$t('project.name')" prop="name">
          <el-input v-model="sprintForm.name" :placeholder="$t('project.sprintNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('project.goal')" prop="goal">
          <el-input v-model="sprintForm.goal" type="textarea" :rows="2" :placeholder="$t('project.sprintGoalPlaceholder')" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('project.startDate')" prop="startDate">
              <el-date-picker v-model="sprintForm.startDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('project.endDate')" prop="endDate">
              <el-date-picker v-model="sprintForm.endDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('project.status')" prop="status">
              <el-select v-model="sprintForm.status" style="width: 100%">
                <el-option :label="$t('project.sprintPlanning')" value="PLANNING" />
                <el-option :label="$t('project.sprintActive')" value="ACTIVE" />
                <el-option :label="$t('project.sprintCompleted')" value="COMPLETED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('project.capacityHours')">
              <el-input-number v-model="sprintForm.capacityHours" :min="0" :step="8" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('project.velocity')">
          <el-input-number v-model="sprintForm.velocity" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sprintDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSprintSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Add Member Dialog -->
    <el-dialog v-model="showMemberDialog" :title="$t('project.addMember')" width="700px">
      <div class="member-search">
        <el-input
          v-model="memberSearchQuery"
          :placeholder="$t('project.searchUser')"
          clearable
          @input="filterAvailableUsers"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="member-list">
        <el-scrollbar height="350px">
          <div
            v-for="user in availableUsers"
            :key="user.id"
            class="member-option"
            @click="toggleUserSelection(user)"
          >
            <el-checkbox :model-value="isUserSelected(user.id)" />
            <el-avatar :size="28" class="member-avatar">
              {{ user.realName?.charAt(0) || user.username?.charAt(0) || '?' }}
            </el-avatar>
            <span class="member-name">{{ user.realName || user.username }}</span>
            <span class="member-email">{{ user.email }}</span>
          </div>
        </el-scrollbar>
      </div>
      <div class="selected-count" v-if="selectedUsers.length > 0">
        {{ $t('project.selected') }}: {{ selectedUsers.length }} {{ $t('project.person') }}
      </div>
      <template #footer>
        <el-button @click="showMemberDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAddMember" :disabled="selectedUsers.length === 0">{{ $t('common.add') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Grid, Timer, User, Plus, Search, Calendar } from '@element-plus/icons-vue'
import { getProject, getSprints, createSprint, updateSprint, getSprintTasks } from '@/api/project'
import { getTasksByProject, createTask, updateTask, moveTask } from '@/api/task'
import KanbanColumn from '@/components/kanban/KanbanColumn.vue'
import QuickAddDialog from '@/components/kanban/QuickAddDialog.vue'
import StatusConfig from '@/views/project/Settings/StatusConfig.vue'
import { useKanban } from '@/composables/useKanban'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

const project = ref({})
const tasks = ref([])
const sprints = ref([])
const sprintsLoading = ref(false)
const members = ref([])
const activeTab = ref('board')
const taskDialogVisible = ref(false)
const sprintDialogVisible = ref(false)
const editingSprint = ref(null)
const showMemberDialog = ref(false)
const editingTask = ref(null)
const draggedTask = ref(null)
const taskStatuses = ref([])
const taskTypes = ref([])
const priorities = ref([])

// Sprint board
const currentSprintId = ref(null)
const currentSprint = ref(null)
const activeSprintTab = ref('list')
const sprintTasks = ref([])
const showQuickAdd = ref(false)
const quickAddStatus = ref('todo')

// Kanban composable
const { columns: kanbanColumnsFromApi, loadTaskStatuses, normalizeTask, statusCodeToId } = useKanban()

// Computed: kanban columns
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

const completedTaskCount = computed(() => sprintTasks.value.filter(t => t.status === 'done').length)
const remainingTaskCount = computed(() => sprintTasks.value.length - completedTaskCount.value)

// Member management
const allUsers = ref([])
const availableUsers = ref([])
const selectedUsers = ref([])
const roles = ref([])
const memberSearchQuery = ref('')
const membersLoading = ref(false)
const memberForm = reactive({
  roleId: 'ROLE_DEVELOPER'
})
const memberFormRef = ref()

const taskForm = reactive({
  taskId: '',
  title: '',
  description: '',
  type: 'STORY',
  priority: 'P2',
  status: 1,
  sprintId: null,
  assigneeId: null
})

const taskRules = {
  taskId: [{ required: true, message: () => t('project.taskIdRequired') }],
  title: [{ required: true, message: () => t('project.titleRequired') }],
  type: [{ required: true, message: () => t('project.taskTypeRequired') }],
  priority: [{ required: true, message: () => t('project.priorityRequired') }]
}

const sprintForm = reactive({
  name: '',
  goal: '',
  startDate: new Date(),
  endDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000),
  status: 'PLANNING',
  capacityHours: 40,
  velocity: 0
})

const sprintRules = {
  name: [{ required: true, message: () => t('project.sprintNameRequired') }],
  startDate: [{ required: true, message: () => t('project.startDateRequired') }],
  endDate: [{ required: true, message: () => t('project.endDateRequired') }]
}

const taskFormRef = ref()
const sprintFormRef = ref()

const getTasksByStatus = (status) => {
  return tasks.value.filter(t => t.status === status.value)
}

// Board kanban functions
const getBoardTasksByStatus = (status) => {
  return tasks.value.filter(t => t.status === status)
}

const onBoardTaskClick = (task) => {
  openTaskDetail(task)
}

const onBoardTaskDrop = async ({ taskId, targetStatus }) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (!task) return
  try {
    await moveTask(taskId, { status: targetStatus })
    task.status = targetStatus
    ElMessage.success(t('project.taskStatusUpdated'))
  } catch (e) {
    ElMessage.error(t('project.updateTaskStatusFailed'))
  }
}

const onBoardAddTask = ({ status }) => {
  editingTask.value = null
  Object.assign(taskForm, {
    taskId: '',
    title: '',
    description: '',
    type: taskTypes.value.length > 0 ? taskTypes.value[0].value : 'STORY',
    priority: priorities.value.length > 0 ? priorities.value[0].value : 'P2',
    status: status,
    sprintId: null,
    assigneeId: null
  })
  taskDialogVisible.value = true
}

const getSprintName = (sprintId) => {
  const sprint = sprints.value.find(s => s.id === sprintId)
  return sprint?.name || ''
}

const getSprintStatusLabel = (status) => {
  const statusMap = {
    'PLANNING': locale.value === 'zh-CN' ? '规划中' : 'Planning',
    'ACTIVE': locale.value === 'zh-CN' ? '进行中' : 'Active',
    'COMPLETED': locale.value === 'zh-CN' ? '已完成' : 'Completed'
  }
  return statusMap[status] || status
}

const fetchTaskStatuses = async () => {
  try {
    const res = await request.get('/dicts/codes/task_status')
    // Map dict codes to the format expected by frontend: { value, name, nameZh }
    taskStatuses.value = (res.data || []).map(item => ({
      value: item.sortOrder || 1,
      code: item.code,
      name: item.name,
      nameZh: item.nameZh || item.name
    }))
    // Set default status to first one
    if (taskStatuses.value.length > 0) {
      taskForm.status = taskStatuses.value[0].value
    }
  } catch (e) {
    // Fallback to default statuses
    taskStatuses.value = [
      { value: 1, code: 'TODO', name: 'Todo', nameZh: '待办' },
      { value: 2, code: 'IN_PROGRESS', name: 'In Progress', nameZh: '进行中' },
      { value: 3, code: 'IN_REVIEW', name: 'In Review', nameZh: '审核中' },
      { value: 4, code: 'DONE', name: 'Done', nameZh: '已完成' }
    ]
  }
}

const fetchTaskTypes = async () => {
  try {
    const res = await request.get('/dicts/codes/TASK_TYPE_PM')
    taskTypes.value = (res.data || []).map(item => ({
      value: item.code,
      name: item.name,
      nameZh: item.nameZh || item.name
    }))
    if (taskTypes.value.length > 0 && !taskForm.type) {
      taskForm.type = taskTypes.value[0].value
    }
  } catch (e) {
    taskTypes.value = [
      { value: 'EPIC', name: 'Epic', nameZh: '史诗' },
      { value: 'FEATURE', name: 'Feature', nameZh: '特性' },
      { value: 'STORY', name: 'Story', nameZh: '故事' },
      { value: 'TASK', name: 'Task', nameZh: '任务' },
      { value: 'BUG', name: 'Bug', nameZh: '缺陷' }
    ]
  }
}

const fetchPriorities = async () => {
  try {
    const res = await request.get('/dicts/codes/TASK_PRIORITY')
    priorities.value = (res.data || []).map(item => ({
      value: item.code,
      name: item.name,
      nameZh: item.nameZh || item.name
    }))
    if (priorities.value.length > 0 && !taskForm.priority) {
      taskForm.priority = priorities.value[0].value
    }
  } catch (e) {
    priorities.value = [
      { value: 'P0', name: 'P0 Urgent', nameZh: 'P0 紧急' },
      { value: 'P1', name: 'P1 High', nameZh: 'P1 高' },
      { value: 'P2', name: 'P2 Medium', nameZh: 'P2 中' },
      { value: 'P3', name: 'P3 Low', nameZh: 'P3 低' }
    ]
  }
}

const fetchProject = async () => {
  try {
    const res = await getProject(route.params.id)
    project.value = res.data || {}
    // Load kanban columns for both Kanban and SCRUM modes
    await loadTaskStatuses(route.params.id)
    if (project.value.sprintMode === 'SCRUM') {
      fetchSprints()
    }
  } catch (e) {
    // Handle error
  }
}

const fetchTasks = async () => {
  try {
    // Use project.projectId (e.g., "PRJ001") instead of route.params.id (database id)
    const projectId = project.value.projectId || route.params.id
    const res = await getTasksByProject(projectId)
    tasks.value = res.data || []
  } catch (e) {
    // Handle error
  }
}

const fetchSprints = async () => {
  sprintsLoading.value = true
  try {
    const res = await getSprints(route.params.id)
    sprints.value = res.data || []
    // Load kanban columns from task_status dict
    await loadTaskStatuses(route.params.id)
  } catch (e) {
    // Handle error
  } finally {
    sprintsLoading.value = false
  }
}

const handleDragStart = (task, event) => {
  draggedTask.value = task
  event.dataTransfer.effectAllowed = 'move'
}

const handleDrop = async (newStatus) => {
  if (!draggedTask.value || draggedTask.value.status === newStatus.value) return

  try {
    await moveTask(draggedTask.value.id, { status: newStatus.value })
    draggedTask.value.status = newStatus.value
  } catch (e) {
    // Handle error
  }
}

const openTaskDetail = (task) => {
  editingTask.value = task
  Object.assign(taskForm, {
    taskId: task.taskId,
    title: task.title,
    description: task.description,
    type: task.type,
    priority: task.priority,
    status: task.status,
    sprintId: task.sprintId,
    assigneeId: task.assigneeId
  })
  taskDialogVisible.value = true
}

const handleAddTask = (status) => {
  editingTask.value = null
  Object.assign(taskForm, {
    taskId: '',
    title: '',
    description: '',
    type: taskTypes.value.length > 0 ? taskTypes.value[0].value : 'STORY',
    priority: priorities.value.length > 0 ? priorities.value[0].value : 'P2',
    status: status.value,
    sprintId: null,
    assigneeId: null
  })
  taskDialogVisible.value = true
}

const handleTaskSubmit = async () => {
  const valid = await taskFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (editingTask.value?.id) {
      await updateTask(editingTask.value.id, taskForm)
      ElMessage.success(t('project.taskUpdated'))
    } else {
      await createTask({ ...taskForm, projectId: project.value.projectId })
      ElMessage.success(t('project.taskCreated'))
    }
    taskDialogVisible.value = false
    // Refresh both project tasks and sprint tasks
    fetchTasks()
    loadSprintTasks()
  } catch (e) {
    // Handle error
  }
}

const handleSprintSubmit = async () => {
  const valid = await sprintFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (editingSprint.value) {
      await updateSprint(route.params.id, editingSprint.value.id, sprintForm)
      ElMessage.success(t('project.sprintUpdated'))
    } else {
      await createSprint(route.params.id, sprintForm)
      ElMessage.success(t('project.sprintCreated'))
    }
    sprintDialogVisible.value = false
    editingSprint.value = null
    fetchSprints()
  } catch (e) {
    ElMessage.error(e.message || 'Failed to save sprint')
  }
}

const openSprintDialog = (sprint = null) => {
  editingSprint.value = sprint
  if (sprint) {
    Object.assign(sprintForm, {
      name: sprint.name,
      goal: sprint.goal || '',
      startDate: sprint.startDate ? new Date(sprint.startDate) : null,
      endDate: sprint.endDate ? new Date(sprint.endDate) : null,
      status: sprint.status,
      capacityHours: sprint.capacityHours,
      velocity: sprint.velocity
    })
  } else {
    resetSprintForm()
  }
  sprintDialogVisible.value = true
}

const resetSprintForm = () => {
  Object.assign(sprintForm, {
    name: '',
    goal: '',
    startDate: null,
    endDate: null,
    status: 'PLANNING',
    capacityHours: null,
    velocity: null
  })
}

const handleDeleteSprint = async (sprint) => {
  try {
    await ElMessageBox.confirm(t('project.confirmDeleteSprint'), t('common.warning'), { type: 'warning' })
    await request.delete(`/projects/${route.params.id}/sprints/${sprint.id}`)
    ElMessage.success(t('project.sprintDeleted'))
    if (currentSprintId.value === sprint.id) {
      currentSprintId.value = null
      currentSprint.value = null
      activeSprintTab.value = 'list'
    }
    fetchSprints()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || 'Failed to delete sprint')
    }
  }
}

const handleArchiveSprint = async (sprint) => {
  try {
    await request.post(`/projects/${route.params.id}/sprints/${sprint.id}/archive`)
    ElMessage.success(t('project.sprintArchived'))
    fetchSprints()
  } catch (e) {
    ElMessage.error(e.message || 'Failed to archive sprint')
  }
}

const handleRoleChange = async (member) => {
  try {
    await request.put(`/projects/${project.value.projectId}/members/${member.user_id}`, { roleId: member.role_id })
    ElMessage.success(t('project.roleUpdated'))
  } catch (e) {
    // Revert on error
    fetchMembers()
  }
}

const handleRemoveMember = async (member) => {
  try {
    await request.delete(`/projects/${project.value.projectId}/members/${member.user_id}`)
    ElMessage.success(t('project.memberRemoved'))
    fetchMembers()
  } catch (e) {
    // Handle error
  }
}

const handleAddMember = async () => {
  if (selectedUsers.value.length === 0) return
  try {
    for (const user of selectedUsers.value) {
      await request.post(`/projects/${project.value.projectId}/members`, {
        userId: user.id,
        roleId: 'ROLE_DEVELOPER'
      })
    }
    ElMessage.success(t('project.memberAdded'))
    showMemberDialog.value = false
    selectedUsers.value = []
    memberSearchQuery.value = ''
    fetchMembers()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('project.addMemberFailed'))
  }
}

const fetchMembers = async () => {
  membersLoading.value = true
  try {
    const res = await request.get(`/projects/${project.value.projectId}/members`)
    members.value = res.data || []
  } catch (e) {
    members.value = []
  } finally {
    membersLoading.value = false
  }
}

const fetchUsersAndRoles = async () => {
  try {
    const [usersRes, rolesRes] = await Promise.all([
      request.get('/users'),
      request.get('/roles')
    ])
    allUsers.value = usersRes.data || []
    filterAvailableUsers()
    roles.value = rolesRes.data || []
  } catch (e) {
    allUsers.value = []
    availableUsers.value = []
    roles.value = []
  }
}

// Watch for dialog open to refresh available users
watch(showMemberDialog, (newVal) => {
  if (newVal) {
    filterAvailableUsers()
  }
})

// Reset sprint form when dialog opens
watch(sprintDialogVisible, (newVal) => {
  if (!newVal) {
    editingSprint.value = null
  }
})

const filterAvailableUsers = () => {
  // Filter out users already in the project
  const available = allUsers.value.filter(user => !isUserInProject(user.id))
  if (!memberSearchQuery.value) {
    availableUsers.value = available
  } else {
    const query = memberSearchQuery.value.toLowerCase()
    availableUsers.value = available.filter(user =>
      (user.username?.toLowerCase() || '').includes(query) ||
      (user.email?.toLowerCase() || '').includes(query) ||
      (user.realName?.toLowerCase() || '').includes(query)
    )
  }
}

const isUserInProject = (userId) => {
  return members.value.some(m => m.user_id === userId)
}

const isUserSelected = (userId) => {
  return selectedUsers.value.some(u => u.id === userId)
}

const toggleUserSelection = (user) => {
  if (isUserInProject(user.id)) return
  if (isUserSelected(user.id)) {
    selectedUsers.value = selectedUsers.value.filter(u => u.id !== userId)
  } else {
    selectedUsers.value.push(user)
  }
}

const handleSettings = () => {
  router.push(`/projects/${route.params.id}/settings`)
}

const goToSprints = () => {
  router.push(`/projects/${route.params.id}/sprints`)
}

// Sprint board methods
const sortedSprints = computed(() => {
  const list = sprints.value || []
  return [...list].sort((a, b) => {
    const dateA = a.startDate ? new Date(a.startDate).getTime() : 0
    const dateB = b.startDate ? new Date(b.startDate).getTime() : 0
    return dateA - dateB  // 升序，从小到大
  })
})

const getSprintTagType = (status) => {
  const map = { 'PLANNING': 'info', 'ACTIVE': 'success', 'COMPLETED': '' }
  return map[status] || 'info'
}

const getSprintTasksByStatus = (status) => {
  return sprintTasks.value.filter(task => task.status === status)
}

const onSprintChange = () => {
  currentSprint.value = sprints.value.find(s => s.id === currentSprintId.value)
  loadSprintTasks()
}

const switchToSprintTab = (sprintId) => {
  activeSprintTab.value = sprintId.toString()
  currentSprintId.value = sprintId
  currentSprint.value = sprints.value.find(s => s.id === sprintId)
  loadSprintTasks()
}

const selectSprint = (sprint) => {
  currentSprint.value = sprint
  currentSprintId.value = sprint.id
  loadSprintTasks()
}

// Gantt chart computed
const ganttDates = computed(() => {
  const sprintList = sprints.value || []
  if (sprintList.length === 0) return []
  const dates = []
  const startDates = sprintList.map(s => new Date(s.startDate)).filter(d => !isNaN(d))
  const endDates = sprintList.map(s => new Date(s.endDate)).filter(d => !isNaN(d))
  if (startDates.length === 0 || endDates.length === 0) return []

  const minDate = new Date(Math.min(...startDates))
  const maxDate = new Date(Math.max(...endDates))

  const current = new Date(minDate)
  while (current <= maxDate) {
    dates.push(`${current.getMonth() + 1}/${current.getDate()}`)
    current.setDate(current.getDate() + 7)
  }
  return dates
})

const getGanttBarStyle = (sprint) => {
  const sprintList = sprints.value || sprints
  if (!sprintList || sprintList.length === 0 || !sprint.startDate || !sprint.endDate) return {}

  const startDates = sprintList.map(s => new Date(s.startDate)).filter(d => !isNaN(d))
  const endDates = sprintList.map(s => new Date(s.endDate)).filter(d => !isNaN(d))
  if (startDates.length === 0 || endDates.length === 0) return {}

  const minDate = new Date(Math.min(...startDates))
  const maxDate = new Date(Math.max(...endDates))
  const totalDays = Math.ceil((maxDate - minDate) / (1000 * 60 * 60 * 24))
  const sprintStart = new Date(sprint.startDate)
  const sprintEnd = new Date(sprint.endDate)
  const sprintDays = Math.ceil((sprintEnd - sprintStart) / (1000 * 60 * 60 * 24))

  const offsetDays = Math.ceil((sprintStart - minDate) / (1000 * 60 * 60 * 24))
  const offsetPercent = (offsetDays / totalDays) * 100
  const widthPercent = (sprintDays / totalDays) * 100

  return {
    left: `${Math.max(0, offsetPercent)}%`,
    width: `${Math.min(100 - offsetPercent, widthPercent)}%`
  }
}

const onSprintTabChange = (tabName) => {
  if (tabName === 'list') {
    currentSprint.value = null
    currentSprintId.value = null
    sprintTasks.value = []
  } else {
    const sprintId = parseInt(tabName)
    currentSprint.value = sprints.value.find(s => s.id === sprintId)
    currentSprintId.value = sprintId
    loadSprintTasks()
  }
}

const handleStartSprint = async (sprint) => {
  try {
    await request.post(`/projects/${route.params.id}/sprints/${sprint.id}/start`)
    ElMessage.success(t('project.sprintStarted'))
    await fetchSprints()
  } catch (e) {
    ElMessage.error(e.message || t('project.startSprintFailed'))
  }
}

const handleCompleteSprint = async (sprint) => {
  try {
    await request.post(`/projects/${route.params.id}/sprints/${sprint.id}/complete`)
    ElMessage.success(t('project.sprintCompleted'))
    await fetchSprints()
  } catch (e) {
    ElMessage.error(e.message || 'Failed to complete sprint')
  }
}

const loadSprintTasks = async () => {
  if (!currentSprintId.value) {
    sprintTasks.value = []
    return
  }
  try {
    const res = await getSprintTasks(route.params.id, currentSprintId.value)
    const rawTasks = res.data || res || []
    sprintTasks.value = rawTasks.map(task => normalizeTask(task))
  } catch (e) {
    console.error('Failed to load sprint tasks:', e)
    sprintTasks.value = []
  }
}

const onTaskClick = (task) => {
  editingTask.value = { ...task }
  Object.assign(taskForm, {
    taskId: task.taskId,
    title: task.title,
    description: task.description,
    type: task.type,
    priority: task.priority,
    status: task.status,
    sprintId: task.sprintId,
    assigneeId: task.assigneeId
  })
  taskDialogVisible.value = true
}

const onTaskDrop = async ({ taskId, targetStatus }) => {
  const task = sprintTasks.value.find(t => t.id === taskId)
  if (!task) return

  try {
    const newStatusId = statusCodeToId.value[targetStatus]
    await moveTask(taskId, { statusId: newStatusId, sprintId: currentSprintId.value })
    task.status = targetStatus
    task.statusId = newStatusId
    if (targetStatus === 'done') {
      task.progress = 100
      task.remainingHours = 0
    }
    ElMessage.success(t('project.taskStatusUpdated'))
  } catch (e) {
    console.error('Failed to move task:', e)
    ElMessage.error(t('project.updateTaskStatusFailed'))
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
      projectId: project.value.projectId,
      sprintId: currentSprintId.value
    }
    const res = await createTask(createData)
    const newTask = normalizeTask(res.data || res)
    sprintTasks.value.push(newTask)
    ElMessage.success(t('project.taskCreated'))
    showQuickAdd.value = false
  } catch (e) {
    console.error('Failed to create task:', e)
    ElMessage.error(t('project.createTaskFailed'))
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

onMounted(async () => {
  fetchTaskStatuses()
  fetchTaskTypes()
  fetchPriorities()
  await fetchProject()
  fetchTasks()
  fetchMembers()
  fetchUsersAndRoles()
})
</script>

<style scoped>
.project-detail {
  height: 100%;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  margin: 0;
}

.sprint-list-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

/* Sprint Split View */
.sprint-split-view {
  display: flex;
  gap: 20px;
  height: auto;
  min-height: 600px;
  margin-bottom: 20px;
}

.sprint-list-panel {
  flex: 1;
  background: var(--theme-card-bg);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sprint-gantt-panel {
  flex: 1;
  background: var(--theme-card-bg);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--theme-border);
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.sprint-list-content {
  flex: 1;
  overflow: hidden;
  padding: 12px;
}

.sprint-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 8px;
  background: var(--theme-bg);
  border: 1px solid var(--theme-border);
  transition: all 0.2s;
}

.sprint-item:hover {
  border-color: var(--el-color-primary);
}

.sprint-item.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.sprint-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.sprint-item-name {
  font-weight: 500;
  color: var(--theme-text);
}

.sprint-item-dates {
  font-size: 12px;
  color: var(--theme-text-secondary);
  margin-bottom: 8px;
}

.sprint-item-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Gantt Styles */
.gantt-legend {
  display: flex;
  gap: 16px;
  font-size: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-dot.planning {
  background: #909399;
}

.legend-dot.active {
  background: #67c23a;
}

.legend-dot.completed {
  background: #409eff;
}

.sprint-gantt-content {
  flex: 1;
  overflow: auto;
  padding: 12px;
}

.gantt-timeline {
  min-width: 100%;
}

.gantt-header {
  border-bottom: 1px solid var(--theme-border);
  padding-bottom: 8px;
  margin-bottom: 8px;
}

.gantt-dates-row {
  display: flex;
}

.gantt-label {
  width: 120px;
  flex-shrink: 0;
}

.gantt-dates {
  display: flex;
  flex: 1;
}

.gantt-date-cell {
  flex: 1;
  font-size: 11px;
  color: var(--theme-text-secondary);
  text-align: center;
}

.gantt-row {
  display: flex;
  align-items: center;
  height: 36px;
  margin-bottom: 4px;
}

.gantt-row-label {
  width: 120px;
  flex-shrink: 0;
  font-size: 13px;
  cursor: pointer;
  padding-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gantt-row-label:hover {
  color: var(--el-color-primary);
}

.gantt-row-bars {
  flex: 1;
  position: relative;
  height: 100%;
}

.gantt-bar {
  position: absolute;
  height: 28px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  padding: 0 8px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 40px;
}

.gantt-bar:hover {
  transform: scaleY(1.1);
}

.gantt-bar.status-planning {
  background: #909399;
  color: white;
}

.gantt-bar.status-active {
  background: #67c23a;
  color: white;
}

.gantt-bar.status-completed {
  background: #409eff;
  color: white;
}

.gantt-bar.active {
  box-shadow: 0 0 0 2px var(--el-color-primary);
}

.gantt-bar-label {
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-state-small {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
}

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.sprints-view,
.members-view {
  padding: 20px;
}

.sprints-header,
.members-header {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-lg);
}

.page-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.sprint-card {
  padding: 4px 0 0 0;
}

.sprint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sprint-header h4 {
  margin: 0;
}

.sprint-dates {
  color: var(--theme-text-secondary);
  font-size: 13px;
  margin: 8px 0;
}

.sprint-stats {
  font-size: 13px;
  color: var(--theme-text-secondary);
}

/* Member Management Styles */
.member-search {
  margin-bottom: 16px;
}

.member-list {
  border: 1px solid var(--theme-border);
  border-radius: 8px;
  margin-bottom: 16px;
}

.member-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--theme-border);
}

.member-option:last-child {
  border-bottom: none;
}

.member-option:hover:not(.disabled) {
  background: var(--theme-border);
}

.member-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.member-avatar {
  background: var(--el-color-primary);
  color: white;
  flex-shrink: 0;
  font-size: 12px;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-weight: 500;
  color: var(--theme-text-primary);
  margin-right: 4px;
}

.member-email {
  font-size: 12px;
  color: var(--theme-text-secondary);
}

.selected-count {
  text-align: center;
  color: var(--theme-text-secondary);
  font-size: 13px;
  margin-bottom: 12px;
}

.member-form {
  margin-top: 16px;
}

.member-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-cell-avatar {
  background: var(--el-color-primary);
  color: white;
  flex-shrink: 0;
  font-size: 12px;
}

.member-cell-name {
  font-weight: 500;
  color: var(--theme-text-primary);
  margin-right: 4px;
}

.member-cell-email {
  font-size: 12px;
  color: var(--theme-text-secondary);
}

/* Sprint Board Embedded */
.sprint-board-embedded {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
}

.sprint-board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sprint-selector {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.sprint-label {
  font-size: 14px;
  color: var(--theme-text-secondary);
  font-weight: 500;
}

.sprint-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.sprint-option-name {
  font-weight: 500;
}

.sprint-stats {
  display: flex;
  gap: var(--pm-space-xl);
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.sprint-actions {
  display: flex;
  gap: var(--pm-space-sm);
}

.sprint-info-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--pm-space-md) var(--pm-space-xl);
  border-radius: var(--pm-radius-lg);
  background: var(--theme-card-bg);
  border: 1px solid var(--theme-border);
}

.sprint-info-banner.banner-active {
  border-left: 4px solid var(--pm-status-done);
}

.sprint-info-banner.banner-planning {
  border-left: 4px solid var(--pm-status-todo);
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
  color: var(--theme-text-secondary);
  font-weight: 500;
}

.sprint-goal-text {
  font-size: 14px;
  color: var(--theme-text-primary);
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
  color: var(--theme-text-muted);
}

/* Kanban Board - Fit columns */
.pm-kanban-board-compact {
  display: flex;
  gap: var(--pm-space-md);
  overflow-x: auto;
}

.pm-kanban-board-compact :deep(.pm-kanban-column) {
  flex: 1;
  min-width: 200px;
  max-height: calc(100vh - 350px);
}

.pm-kanban-board-compact :deep(.pm-kanban-column-header) {
  padding: var(--pm-space-md);
}

.pm-kanban-board-compact :deep(.pm-kanban-column-title) {
  font-size: 13px;
}

.pm-kanban-board-compact :deep(.pm-kanban-column-body) {
  padding: var(--pm-space-sm);
  min-height: 80px;
}
</style>
