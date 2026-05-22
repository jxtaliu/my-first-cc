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
        <div class="kanban-board">
          <div v-for="status in taskStatuses" :key="status.value" class="kanban-column">
            <div class="column-header">
              <span class="column-title">{{ locale === 'zh-CN' ? status.nameZh : status.name }}</span>
              <el-badge :value="getTasksByStatus(status).length" type="info" />
            </div>
            <div
              class="column-content"
              @dragover.prevent
              @drop="handleDrop(status)"
            >
              <div
                v-for="task in getTasksByStatus(status)"
                :key="task.id"
                class="task-card"
                draggable="true"
                @dragstart="handleDragStart(task, $event)"
                @click="openTaskDetail(task)"
              >
                <div class="task-priority" :class="'priority-' + task.priority"></div>
                <div class="task-header">
                  <span class="task-id">{{ task.taskId }}</span>
                  <span class="task-title">{{ task.title }}</span>
                </div>
                <div class="task-meta">
                  <el-tag v-if="task.sprintId" size="small" type="info">
                    {{ getSprintName(task.sprintId) }}
                  </el-tag>
                  <span v-if="task.assigneeId" class="task-assignee">
                    <el-avatar :size="20" src="task.assigneeAvatar" />
                  </span>
                </div>
              </div>
              <div class="add-task-btn" @click="handleAddTask(status)">
                <el-icon><Plus /></el-icon>
                {{ $t('project.addTask') }}
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane name="sprints" v-if="project.sprintMode === 'SCRUM'">
        <template #label>
          <span><el-icon><Timer /></el-icon> {{ $t('project.sprints') }}</span>
        </template>
        <div class="sprints-view">
          <div class="sprints-header">
            <el-button type="primary" @click="sprintDialogVisible = true">
              <el-icon><Plus /></el-icon> {{ $t('project.createSprint') }}
            </el-button>
          </div>
          <el-timeline>
            <el-timeline-item
              v-for="sprint in sprints"
              :key="sprint.id"
              :type="sprint.status === 'ACTIVE' ? 'success' : 'info'"
              :hollow="sprint.status !== 'ACTIVE'"
            >
              <div class="sprint-card">
                <div class="sprint-header">
                  <h4>{{ sprint.name }}</h4>
                  <el-tag :type="sprint.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                    {{ getSprintStatusLabel(sprint.status) }}
                  </el-tag>
                </div>
                <p class="sprint-dates">
                  {{ formatDate(sprint.startDate) }} - {{ formatDate(sprint.endDate) }}
                </p>
                <div class="sprint-stats">
                  <span>{{ sprint.completedTasks || 0 }}/{{ sprint.totalTasks || 0 }} {{ $t('project.tasks') }}</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-tab-pane>

      <el-tab-pane name="members">
        <template #label>
          <span><el-icon><User /></el-icon> {{ $t('project.members') }}</span>
        </template>
        <div class="members-view">
          <div class="members-header">
            <el-button type="primary" @click="showMemberDialog = true">
              <el-icon><Plus /></el-icon> {{ $t('project.addMember') }}
            </el-button>
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

    <el-dialog v-model="sprintDialogVisible" :title="$t('project.createSprint')" width="500px">
      <el-form :model="sprintForm" :rules="sprintRules" ref="sprintFormRef" label-width="100px">
        <el-form-item :label="$t('project.name')" prop="name">
          <el-input v-model="sprintForm.name" />
        </el-form-item>
        <el-form-item :label="$t('project.startDate')" prop="startDate">
          <el-date-picker v-model="sprintForm.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('project.endDate')" prop="endDate">
          <el-date-picker v-model="sprintForm.endDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('project.goal')" prop="goal">
          <el-input v-model="sprintForm.goal" type="textarea" :rows="3" />
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Grid, Timer, User, Plus, Search } from '@element-plus/icons-vue'
import { getProject, getSprints, createSprint } from '@/api/project'
import { getTasksByProject, createTask, updateTask, moveTask } from '@/api/task'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()

const project = ref({})
const tasks = ref([])
const sprints = ref([])
const members = ref([])
const activeTab = ref('board')
const taskDialogVisible = ref(false)
const sprintDialogVisible = ref(false)
const showMemberDialog = ref(false)
const editingTask = ref(null)
const draggedTask = ref(null)
const taskStatuses = ref([])
const taskTypes = ref([])
const priorities = ref([])

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
  startDate: null,
  endDate: null,
  goal: ''
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
  try {
    const res = await getSprints(route.params.id)
    sprints.value = res.data || []
  } catch (e) {
    // Handle error
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
      // Use project.projectId (e.g., "PRJ001") instead of route.params.id (database id)
      await createTask({ ...taskForm, projectId: project.value.projectId, type: 'STORY' })
      ElMessage.success(t('project.taskCreated'))
    }
    taskDialogVisible.value = false
    fetchTasks()
  } catch (e) {
    // Handle error
  }
}

const handleSprintSubmit = async () => {
  const valid = await sprintFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await createSprint(route.params.id, sprintForm)
    ElMessage.success(t('project.sprintCreated'))
    sprintDialogVisible.value = false
    fetchSprints()
  } catch (e) {
    // Handle error
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

.detail-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.kanban-board {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 20px;
}

.kanban-column {
  flex: 0 0 280px;
  background: var(--theme-card-bg);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 280px);
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--theme-border);
}

.column-title {
  font-weight: 600;
}

.column-content {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  min-height: 200px;
}

.task-card {
  background: var(--theme-bg);
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: grab;
  position: relative;
  border: 1px solid var(--theme-border);
  transition: transform 0.2s, box-shadow 0.2s;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.task-priority {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  border-radius: 6px 0 0 6px;
}

.priority-LOW { background: #909399; }
.priority-MEDIUM { background: #409eff; }
.priority-HIGH { background: #e6a23c; }
.priority-URGENT { background: #f56c6c; }

.task-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding-left: 8px;
}

.task-id {
  font-size: 12px;
  color: var(--theme-text-secondary);
  background: var(--theme-border);
  padding: 2px 6px;
  border-radius: 3px;
}

.task-title {
  font-weight: 500;
  flex: 1;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-task-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: var(--theme-text-secondary);
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s;
}

.add-task-btn:hover {
  background: var(--theme-border);
}

.sprints-view,
.members-view {
  padding: 20px;
}

.sprints-header,
.members-header {
  margin-bottom: 20px;
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
</style>
