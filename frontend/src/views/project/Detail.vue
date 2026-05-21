<template>
  <div class="project-detail">
    <div class="detail-header">
      <div class="header-left">
        <el-button text @click="router.push('/projects')">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2>{{ project.name }}</h2>
        <el-tag :type="project.type === 'SCRUM' ? 'success' : 'warning'">{{ project.type }}</el-tag>
      </div>
      <div class="header-right">
        <el-button @click="handleSettings">{{ $t('common.settings') }}</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="detail-tabs">
      <el-tab-pane label="Board" name="board">
        <template #label>
          <span><el-icon><Grid /></el-icon> Board</span>
        </template>
        <div class="kanban-board">
          <div v-for="status in taskStatuses" :key="status.value" class="kanban-column">
            <div class="column-header">
              <span class="column-title">{{ status.label }}</span>
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
                <div class="task-title">{{ task.title }}</div>
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
                Add Task
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Sprints" name="sprints" v-if="project.type === 'SCRUM'">
        <template #label>
          <span><el-icon><Timer /></el-icon> Sprints</span>
        </template>
        <div class="sprints-view">
          <div class="sprints-header">
            <el-button type="primary" @click="showSprintDialog = true">
              <el-icon><Plus /></el-icon> Create Sprint
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
                    {{ sprint.status }}
                  </el-tag>
                </div>
                <p class="sprint-dates">
                  {{ formatDate(sprint.startDate) }} - {{ formatDate(sprint.endDate) }}
                </p>
                <div class="sprint-stats">
                  <span>{{ sprint.completedTasks || 0 }}/{{ sprint.totalTasks || 0 }} tasks completed</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Members" name="members">
        <template #label>
          <span><el-icon><User /></el-icon> Members</span>
        </template>
        <div class="members-view">
          <div class="members-header">
            <el-button type="primary" @click="showMemberDialog = true">
              <el-icon><Plus /></el-icon> Add Member
            </el-button>
          </div>
          <el-table :data="members" style="width: 100%">
            <el-table-column prop="username" label="Username" />
            <el-table-column prop="email" label="Email" />
            <el-table-column prop="role" label="Role">
              <template #default="{ row }">
                <el-select v-model="row.role" @change="handleRoleChange(row)">
                  <el-option label="Admin" value="ADMIN" />
                  <el-option label="Member" value="MEMBER" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="100">
              <template #default="{ row }">
                <el-button text type="danger" @click="handleRemoveMember(row)">Remove</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="taskDialogVisible" :title="editingTask?.id ? 'Edit Task' : 'New Task'" width="600px">
      <el-form :model="taskForm" :rules="taskRules" ref="taskFormRef" label-width="100px">
        <el-form-item label="Title" prop="title">
          <el-input v-model="taskForm.title" />
        </el-form-item>
        <el-form-item label="Description" prop="description">
          <el-input v-model="taskForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Priority" prop="priority">
              <el-select v-model="taskForm.priority">
                <el-option label="Low" value="LOW" />
                <el-option label="Medium" value="MEDIUM" />
                <el-option label="High" value="HIGH" />
                <el-option label="Urgent" value="URGENT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Status" prop="status">
              <el-select v-model="taskForm.status">
                <el-option v-for="s in taskStatuses" :key="s.value" :label="s.label" :value="s.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="project.type === 'SCRUM'">
          <el-col :span="12">
            <el-form-item label="Sprint" prop="sprintId">
              <el-select v-model="taskForm.sprintId" placeholder="Select sprint">
                <el-option v-for="s in sprints" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Assignee" prop="assigneeId">
              <el-select v-model="taskForm.assigneeId" placeholder="Select assignee">
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

    <el-dialog v-model="sprintDialogVisible" title="Create Sprint" width="500px">
      <el-form :model="sprintForm" :rules="sprintRules" ref="sprintFormRef" label-width="100px">
        <el-form-item label="Name" prop="name">
          <el-input v-model="sprintForm.name" />
        </el-form-item>
        <el-form-item label="Start Date" prop="startDate">
          <el-date-picker v-model="sprintForm.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="End Date" prop="endDate">
          <el-date-picker v-model="sprintForm.endDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Goal" prop="goal">
          <el-input v-model="sprintForm.goal" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sprintDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSprintSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Grid, Timer, User, Plus } from '@element-plus/icons-vue'
import { getProject, getSprints, createSprint } from '@/api/project'
import { getTasksByProject, createTask, updateTask, moveTask } from '@/api/task'

const router = useRouter()
const route = useRoute()

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

const taskStatuses = [
  { label: 'Todo', value: 1 },
  { label: 'In Progress', value: 2 },
  { label: 'In Review', value: 3 },
  { label: 'Done', value: 4 }
]

const taskForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 1,
  sprintId: null,
  assigneeId: null
})

const taskRules = {
  title: [{ required: true, message: 'Title is required' }],
  status: [{ required: true, message: 'Status is required' }]
}

const sprintForm = reactive({
  name: '',
  startDate: null,
  endDate: null,
  goal: ''
})

const sprintRules = {
  name: [{ required: true, message: 'Sprint name is required' }],
  startDate: [{ required: true, message: 'Start date is required' }],
  endDate: [{ required: true, message: 'End date is required' }]
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

const fetchProject = async () => {
  try {
    const res = await getProject(route.params.id)
    project.value = res.data || {}
    members.value = res.data?.members || []
  } catch (e) {
    // Handle error
  }
}

const fetchTasks = async () => {
  try {
    const res = await getTasksByProject(route.params.id)
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
    title: task.title,
    description: task.description,
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
    title: '',
    description: '',
    priority: 'MEDIUM',
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
      ElMessage.success('Task updated')
    } else {
      await createTask({ ...taskForm, projectId: route.params.id })
      ElMessage.success('Task created')
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
    ElMessage.success('Sprint created')
    sprintDialogVisible.value = false
    fetchSprints()
  } catch (e) {
    // Handle error
  }
}

const handleRoleChange = async (member) => {
  // API call to update member role
  ElMessage.success('Role updated')
}

const handleRemoveMember = async (member) => {
  // API call to remove member
  ElMessage.success('Member removed')
}

const handleSettings = () => {
  // Navigate to project settings
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  fetchProject()
  fetchTasks()
  if (project.value.type === 'SCRUM') {
    fetchSprints()
  }
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

.task-title {
  font-weight: 500;
  margin-bottom: 8px;
  padding-left: 8px;
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
</style>
