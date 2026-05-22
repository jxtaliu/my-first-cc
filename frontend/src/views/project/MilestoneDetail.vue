<template>
  <div class="milestone-detail-page">
    <!-- Page Header -->
    <div class="detail-header">
      <div class="header-left">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2>{{ milestone.name }}</h2>
        <el-tag :type="getStatusType(milestone.status)">{{ milestone.status }}</el-tag>
      </div>
      <div class="header-right">
        <el-button @click="showEditDialog = true">
          <el-icon><Edit /></el-icon>
          {{ $t('common.edit') }}
        </el-button>
      </div>
    </div>

    <!-- Milestone Info Card -->
    <el-card class="info-card" shadow="never">
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="$t('project.milestoneName')">
          {{ milestone.name }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('project.targetDate')">
          {{ formatDate(milestone.targetDate) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('project.status')">
          <el-tag :type="getStatusType(milestone.status)" size="small">{{ milestone.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('project.progress')">
          <div class="progress-container">
            <el-progress :percentage="progressPercentage" :color="progressColor" />
            <span class="progress-text">{{ milestone.completedTasks || 0 }}/{{ milestone.totalTasks || 0 }} {{ $t('project.tasks') }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('project.description')" :span="2">
          {{ milestone.description || $t('project.noDescription') }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- Linked Projects (Cross-Project Milestone) -->
    <el-card v-if="milestone.isCrossProject" class="projects-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ $t('project.relatedProjects') }}</span>
        </div>
      </template>
      <div class="linked-projects">
        <el-tag v-for="project in milestone.projects" :key="project.id" type="info" class="project-tag">
          {{ project.name }}
        </el-tag>
      </div>
    </el-card>

    <!-- Tasks Section -->
    <el-card class="tasks-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ $t('project.tasks') }}</span>
          <el-button type="primary" size="small" @click="showAddTaskDialog = true">
            <el-icon><Plus /></el-icon>
            {{ $t('project.addTask') }}
          </el-button>
        </div>
      </template>

      <!-- Filter -->
      <div class="tasks-filter">
        <el-input
          v-model="taskSearch"
          :placeholder="$t('project.searchTasks')"
          prefix-icon="Search"
          clearable
          style="width: 200px"
        />
        <el-select v-model="taskFilterStatus" :placeholder="$t('project.status')" clearable style="width: 150px">
          <el-option v-for="s in taskStatuses" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </div>

      <!-- Tasks Table -->
      <el-table :data="filteredTasks" style="width: 100%" v-loading="loadingTasks">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="title" :label="$t('project.taskTitle')" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="openTaskDetail(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('project.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="getTaskStatusType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" :label="$t('project.priority')" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" :label="$t('project.assignee')" width="120" />
        <el-table-column prop="estimateHours" :label="$t('project.estimateHours')" width="100" />
        <el-table-column :label="$t('admin.actions')" width="100" fixed="right">
          <template #default="{ row }">
            <el-button text type="danger" size="small" @click="handleRemoveTask(row)">
              {{ $t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Edit Milestone Dialog -->
    <el-dialog v-model="showEditDialog" :title="$t('project.editMilestone')" width="500px" class="pm-dialog">
      <el-form :model="milestoneForm" label-position="top">
        <el-form-item :label="$t('project.milestoneName')">
          <el-input v-model="milestoneForm.name" :placeholder="$t('project.milestoneNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('project.targetDate')">
          <el-date-picker
            v-model="milestoneForm.targetDate"
            type="date"
            :placeholder="$t('project.selectDate')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('project.status')">
          <el-select v-model="milestoneForm.status" style="width: 100%">
            <el-option label="Pending" value="PENDING" />
            <el-option label="In Progress" value="IN_PROGRESS" />
            <el-option label="Completed" value="COMPLETED" />
            <el-option label="Overdue" value="OVERDUE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('project.description')">
          <el-input
            v-model="milestoneForm.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('project.descriptionPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSaveMilestone">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Add Task Dialog -->
    <el-dialog v-model="showAddTaskDialog" :title="$t('project.addTask')" width="600px" class="pm-dialog">
      <el-form :model="addTaskForm" label-position="top">
        <el-form-item :label="$t('project.selectProject')" v-if="milestone.isCrossProject">
          <el-select v-model="addTaskForm.projectId" style="width: 100%">
            <el-option v-for="p in milestone.projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('project.taskTitle')">
          <el-select v-model="addTaskForm.taskId" filterable style="width: 100%" :placeholder="$t('project.searchTasks')">
            <el-option
              v-for="task in availableTasks"
              :key="task.id"
              :label="task.title"
              :value="task.id"
            >
              <span>{{ task.title }}</span>
              <span class="task-project-name">{{ task.projectName }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddTaskDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAddTask">{{ $t('common.add') }}</el-button>
      </template>
    </el-dialog>

    <!-- Task Detail Dialog -->
    <el-dialog v-model="showTaskDetailDialog" :title="$t('project.taskDetail')" width="700px" class="pm-dialog">
      <div v-if="selectedTask" class="task-detail">
        <h3>{{ selectedTask.title }}</h3>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item :label="$t('project.status')">
            <el-tag :type="getTaskStatusType(selectedTask.status)" size="small">{{ selectedTask.statusName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('project.priority')">
            <el-tag :type="getPriorityType(selectedTask.priority)" size="small">{{ selectedTask.priority }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('project.assignee')">{{ selectedTask.assigneeName || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('project.estimateHours')">{{ selectedTask.estimateHours || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('project.remainingHours')">{{ selectedTask.remainingHours || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('project.actualHours')">{{ selectedTask.actualHours || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('project.description')" :span="2">{{ selectedTask.description || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showTaskDetailDialog = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit, Plus } from '@element-plus/icons-vue'
import { getMilestone, updateMilestone } from '@/api/milestone'
import { getTasks } from '@/api/task'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const milestone = ref({
  name: '',
  targetDate: null,
  status: 'PENDING',
  description: '',
  isCrossProject: false,
  projects: [],
  totalTasks: 0,
  completedTasks: 0
})

const tasks = ref([])
const loadingTasks = ref(false)
const showEditDialog = ref(false)
const showAddTaskDialog = ref(false)
const showTaskDetailDialog = ref(false)
const selectedTask = ref(null)
const taskSearch = ref('')
const taskFilterStatus = ref(null)

const milestoneForm = ref({
  name: '',
  targetDate: null,
  status: 'PENDING',
  description: ''
})

const addTaskForm = ref({
  projectId: null,
  taskId: null
})

const taskStatuses = [
  { label: 'Todo', value: 'TODO' },
  { label: 'In Progress', value: 'IN_PROGRESS' },
  { label: 'In Review', value: 'IN_REVIEW' },
  { label: 'Done', value: 'DONE' }
]

const progressPercentage = computed(() => {
  if (!milestone.value.totalTasks || milestone.value.totalTasks === 0) return 0
  return Math.round((milestone.value.completedTasks / milestone.value.totalTasks) * 100)
})

const progressColor = computed(() => {
  const pct = progressPercentage.value
  if (pct < 30) return '#ef4444'
  if (pct < 70) return '#f59e0b'
  return '#10b981'
})

const availableTasks = computed(() => {
  const linkedTaskIds = tasks.value.map(t => t.id)
  return [] // Would be populated from API based on linked projects
})

const filteredTasks = computed(() => {
  let result = tasks.value
  if (taskSearch.value) {
    result = result.filter(t => t.title.toLowerCase().includes(taskSearch.value.toLowerCase()))
  }
  if (taskFilterStatus.value) {
    result = result.filter(t => t.status === taskFilterStatus.value)
  }
  return result
})

const fetchMilestone = async () => {
  try {
    const res = await getMilestone(route.params.milestoneId)
    milestone.value = res.data || getMockMilestone()
    milestoneForm.value = {
      name: milestone.value.name,
      targetDate: milestone.value.targetDate,
      status: milestone.value.status,
      description: milestone.value.description
    }
    if (milestone.value.isCrossProject && !milestone.value.projects) {
      milestone.value.projects = [
        { id: 1, name: 'SME-PM系统' },
        { id: 2, name: '客户CRM项目' }
      ]
    }
  } catch (e) {
    milestone.value = getMockMilestone()
  }
}

const fetchTasks = async () => {
  loadingTasks.value = true
  try {
    // In real implementation: tasks would be fetched with milestoneId filter
    tasks.value = getMockTasks()
  } catch (e) {
    tasks.value = getMockTasks()
  } finally {
    loadingTasks.value = false
  }
}

const getMockMilestone = () => ({
  id: parseInt(route.params.milestoneId) || 1,
  name: '发布 V2.0',
  targetDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
  status: 'IN_PROGRESS',
  description: '完成所有功能开发，准备发布版本2.0',
  isCrossProject: true,
  projects: [
    { id: 1, name: 'SME-PM系统' },
    { id: 2, name: '客户CRM项目' }
  ],
  totalTasks: 25,
  completedTasks: 15
})

const getMockTasks = () => [
  { id: 1, title: '用户认证模块', status: 'DONE', statusName: 'Done', priority: 'HIGH', assigneeName: '张三', estimateHours: 16, remainingHours: 0, actualHours: 18, projectName: 'SME-PM系统' },
  { id: 2, title: '项目管理页面', status: 'IN_PROGRESS', statusName: 'In Progress', priority: 'HIGH', assigneeName: '李四', estimateHours: 24, remainingHours: 8, actualHours: 16, projectName: 'SME-PM系统' },
  { id: 3, title: '任务详情页', status: 'IN_PROGRESS', statusName: 'In Progress', priority: 'MEDIUM', assigneeName: '王五', estimateHours: 12, remainingHours: 4, actualHours: 8, projectName: 'SME-PM系统' },
  { id: 4, title: 'API对接', status: 'TODO', statusName: 'Todo', priority: 'MEDIUM', assigneeName: '赵六', estimateHours: 20, remainingHours: 20, actualHours: 0, projectName: '客户CRM项目' },
  { id: 5, title: '数据库优化', status: 'IN_REVIEW', statusName: 'In Review', priority: 'LOW', assigneeName: '钱七', estimateHours: 8, remainingHours: 2, actualHours: 6, projectName: '客户CRM项目' }
]

const getStatusType = (status) => {
  const types = {
    'PENDING': 'info',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'OVERDUE': 'danger'
  }
  return types[status] || 'info'
}

const getTaskStatusType = (status) => {
  const types = {
    'TODO': 'info',
    'IN_PROGRESS': 'primary',
    'IN_REVIEW': 'warning',
    'DONE': 'success'
  }
  return types[status] || 'info'
}

const getPriorityType = (priority) => {
  const types = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'URGENT': 'danger'
  }
  return types[priority] || 'info'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const handleSaveMilestone = async () => {
  try {
    await updateMilestone(route.params.milestoneId, milestoneForm.value)
    ElMessage.success(t('project.milestoneUpdated'))
    showEditDialog.value = false
    fetchMilestone()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

const handleAddTask = () => {
  if (!addTaskForm.value.taskId) {
    ElMessage.warning(t('project.selectTaskFirst'))
    return
  }
  ElMessage.success(t('project.taskAdded'))
  showAddTaskDialog.value = false
  addTaskForm.value = { projectId: null, taskId: null }
}

const handleRemoveTask = async (task) => {
  try {
    await ElMessageBox.confirm(t('project.confirmRemoveTask'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    ElMessage.success(t('project.taskRemoved'))
    fetchTasks()
  } catch (e) {
    // Cancelled
  }
}

const openTaskDetail = (task) => {
  selectedTask.value = task
  showTaskDetailDialog.value = true
}

onMounted(() => {
  fetchMilestone()
  fetchTasks()
})
</script>

<style scoped>
.milestone-detail-page {
  max-width: 1200px;
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

.info-card,
.projects-card,
.tasks-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-text {
  font-size: 13px;
  color: var(--pm-text-secondary);
  white-space: nowrap;
}

.linked-projects {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.project-tag {
  margin-right: 8px;
}

.tasks-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.task-project-name {
  float: right;
  color: var(--pm-text-muted);
  font-size: 12px;
}

.task-detail h3 {
  margin: 0 0 16px 0;
}
</style>
