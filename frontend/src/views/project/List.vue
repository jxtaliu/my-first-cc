<template>
  <div class="project-list">
    <div class="list-header">
      <h2>{{ $t('nav.projects') }}</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        {{ $t('common.add') }}
      </el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane :label="$t('project.all')" name="all" />
      <el-tab-pane v-for="status in statusOptions" :key="status.code" :label="locale === 'zh-CN' ? status.nameZh : status.name" :name="status.code" />
    </el-tabs>

    <el-row :gutter="20">
      <el-col v-for="project in projects" :key="project.id" :span="8">
        <el-card class="project-card" @click="goToDetail(project.id)">
          <div class="project-header">
            <span class="project-name">{{ project.name }}</span>
            <span class="project-id">{{ project.projectId }}</span>
            <el-tag :type="project.sprintMode === 'KANBAN' ? 'warning' : 'success'" size="small">
              {{ project.sprintMode === 'KANBAN' ? $t('project.kanban') : $t('project.scrum') }}
            </el-tag>
          </div>
          <p class="project-desc">{{ project.description || $t('project.noDescription') }}</p>
          <div class="project-info">
            <span class="info-item">
              <el-tag size="small" type="info">{{ getProjectTypeLabel(project.projectType) }}</el-tag>
            </span>
            <span class="info-item">
              <el-tag size="small" :type="getStatusType(project.status)">{{ getStatusLabel(project.status) }}</el-tag>
            </span>
          </div>
          <div class="project-stats">
            <span><el-icon><User /></el-icon> {{ project.memberCount || 0 }} {{ $t('project.members') }}</span>
          </div>
          <div class="task-type-stats">
            <el-tooltip :content="$t('project.epic')" placement="top">
              <span class="type-stat epic">{{ project.epicCount || 0 }}</span>
            </el-tooltip>
            <el-tooltip :content="$t('project.feature')" placement="top">
              <span class="type-stat feature">{{ project.featureCount || 0 }}</span>
            </el-tooltip>
            <el-tooltip :content="$t('project.story')" placement="top">
              <span class="type-stat story">{{ project.storyCount || 0 }}</span>
            </el-tooltip>
            <el-tooltip :content="$t('project.task')" placement="top">
              <span class="type-stat task">{{ project.taskCount || 0 }}</span>
            </el-tooltip>
            <el-tooltip :content="$t('project.subtask')" placement="top">
              <span class="type-stat subtask">{{ project.subtaskCount || 0 }}</span>
            </el-tooltip>
            <el-tooltip :content="$t('project.bug')" placement="top">
              <span class="type-stat bug">{{ project.bugCount || 0 }}</span>
            </el-tooltip>
          </div>
          <div class="project-footer">
            <span class="project-date">{{ $t('project.created') }} {{ formatDate(project.createdAt) }}</span>
            <el-dropdown @click.stop @command="handleCommand($event, project)">
              <el-button text size="small">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">{{ $t('common.edit') }}</el-dropdown-item>
                  <el-dropdown-item command="archive" v-if="project.status !== 'ARCHIVED'">
                    {{ $t('common.archive') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="restore" v-else>
                    {{ $t('common.restore') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>{{ $t('common.delete') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!projects.length && !loading" :description="$t('common.noData')" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('common.edit') : $t('common.add')" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item :label="$t('project.projectId')" prop="projectId">
          <el-input v-model="form.projectId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('project.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('project.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('project.projectType')" prop="projectType">
          <el-select v-model="form.projectType" placeholder="选择项目类型">
            <el-option v-for="item in projectTypeOptions" :key="item.code" :label="locale === 'zh-CN' ? (item.nameZh || item.name) : item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('project.sprintMode')" prop="sprintMode">
          <el-select v-model="form.sprintMode" placeholder="选择敏捷模式">
            <el-option v-for="item in sprintModeOptions" :key="item.code" :label="locale === 'zh-CN' ? (item.nameZh || item.name) : item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('project.status')" prop="status">
          <el-select v-model="form.status" placeholder="选择项目状态">
            <el-option v-for="item in statusOptions" :key="item.code" :label="locale === 'zh-CN' ? (item.nameZh || item.name) : item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('project.owner')" prop="ownerId">
          <el-select v-model="form.ownerId" placeholder="选择负责人" filterable>
            <el-option v-for="user in userOptions" :key="user.id" :label="user.realName || user.username" :value="user.id">
              <span>{{ user.realName || user.username }}</span>
              <span class="user-email">{{ user.email }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document, User, More } from '@element-plus/icons-vue'
import { getProjects, createProject, updateProject, deleteProject, archiveProject, restoreProject } from '@/api/project'
import request from '@/api/request'

const router = useRouter()
const { t, locale } = useI18n()
const activeTab = ref('all')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const projects = ref([])
const formRef = ref()

const form = reactive({
  id: null,
  projectId: '',
  name: '',
  description: '',
  projectType: '',
  status: 'PLANNING',
  sprintMode: 'SCRUM',
  ownerId: null
})

const rules = {
  projectId: [{ required: true, message: () => t('project.projectIdRequired') }],
  name: [{ required: true, message: () => t('project.projectNameRequired') }],
  projectType: [{ required: true, message: () => t('project.selectProjectType') }],
  sprintMode: [{ required: true, message: () => t('project.selectSprintMode') }],
  status: [{ required: true, message: () => t('project.selectStatus') }],
  ownerId: [{ required: true, message: () => t('project.selectOwner') }]
}

// 下拉选项数据
const projectTypeOptions = ref([])
const statusOptions = ref([])
const sprintModeOptions = ref([])
const userOptions = ref([])

const fetchProjectTypeOptions = async () => {
  try {
    const res = await request.get('/dicts/codes/project_type')
    projectTypeOptions.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch project type options', e)
  }
}

const fetchStatusOptions = async () => {
  try {
    const res = await request.get('/dicts/codes/PROJECT_STATUS_PM')
    statusOptions.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch status options', e)
  }
}

const fetchSprintModeOptions = async () => {
  try {
    const res = await request.get('/dicts/codes/SPRINT_MODE_PM')
    sprintModeOptions.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch sprint mode options', e)
  }
}

const fetchUserOptions = async () => {
  try {
    const res = await request.get('/users')
    userOptions.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch user options', e)
  }
}

const fetchProjects = async () => {
  loading.value = true
  try {
    const params = activeTab.value === 'all' ? {} : { status: activeTab.value }
    const res = await getProjects(params)
    projects.value = res.data || []
  } catch (e) {
    // Handle error
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  fetchProjects()
}

const handleCreate = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    projectId: '',
    name: '',
    description: '',
    projectType: projectTypeOptions.value.length > 0 ? projectTypeOptions.value[0].code : '',
    status: 'PLANNING',
    sprintMode: 'SCRUM',
    ownerId: null
  })
  dialogVisible.value = true
}

const handleEdit = (project) => {
  isEdit.value = true
  Object.assign(form, {
    id: project.id,
    projectId: project.projectId,
    name: project.name,
    description: project.description,
    projectType: project.projectType,
    status: project.status,
    sprintMode: project.sprintMode,
    ownerId: project.ownerId
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateProject(form.id, form)
      ElMessage.success(t('project.projectUpdated'))
    } else {
      await createProject(form)
      ElMessage.success(t('project.projectCreated'))
    }
    dialogVisible.value = false
    fetchProjects()
  } catch (e) {
    // Handle error
  } finally {
    submitting.value = false
  }
}

const handleCommand = async (command, project) => {
  switch (command) {
    case 'edit':
      handleEdit(project)
      break
    case 'archive':
      try {
        await archiveProject(project.id)
        ElMessage.success(t('project.projectArchived'))
        fetchProjects()
      } catch (e) {}
      break
    case 'restore':
      try {
        await restoreProject(project.id)
        ElMessage.success(t('project.projectRestored'))
        fetchProjects()
      } catch (e) {}
      break
    case 'delete':
      try {
        await ElMessageBox.confirm(t('project.confirmDelete'), t('common.warning'), { type: 'warning' })
        await deleteProject(project.id)
        ElMessage.success(t('project.projectDeleted'))
        fetchProjects()
      } catch (e) {}
      break
  }
}

const goToDetail = (id) => {
  router.push(`/projects/${id}`)
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

// 根据当前语言返回本地化名称
const getLocalizedName = (item) => {
  if (!item) return ''
  if (locale.value === 'zh-CN') {
    return item.nameZh || item.name
  }
  return item.name
}

const getProjectTypeLabel = (type) => {
  const item = projectTypeOptions.value.find(o => o.code === type)
  return item ? getLocalizedName(item) : type
}

const getStatusLabel = (status) => {
  const item = statusOptions.value.find(o => o.code === status)
  return item ? getLocalizedName(item) : status
}

const getStatusType = (status) => {
  const typeMap = {
    'PLANNING': 'info',
    'STARTING': 'warning',
    'ACTIVE': 'success',
    'COMPLETED': 'primary',
    'PAUSED': 'warning',
    'ARCHIVED': 'info'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  fetchProjects()
  fetchProjectTypeOptions()
  fetchStatusOptions()
  fetchSprintModeOptions()
  fetchUserOptions()
})
</script>

<style scoped>
.project-list {
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h2 {
  margin: 0;
}

.project-card {
  cursor: pointer;
  margin-bottom: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.project-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.project-name {
  font-size: 18px;
  font-weight: bold;
  flex: 1;
}

.project-id {
  font-size: 12px;
  color: var(--theme-text-secondary, #909399);
}

.project-desc {
  color: var(--theme-text-secondary);
  font-size: 14px;
  margin: 8px 0;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.project-info {
  display: flex;
  gap: 8px;
  margin: 8px 0;
}

.info-item {
  display: flex;
  align-items: center;
}

.project-stats {
  display: flex;
  gap: 16px;
  color: var(--theme-text-secondary);
  font-size: 13px;
  margin: 12px 0;
}

.project-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.task-type-stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 8px 0;
  border-top: 1px solid var(--theme-border);
}

.type-stat {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 24px;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-stat.epic {
  background: rgba(139, 92, 246, 0.1);
  color: #8B5CF6;
}

.type-stat.feature {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.type-stat.story {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.type-stat.task {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.type-stat.subtask {
  background: rgba(100, 116, 139, 0.1);
  color: #64748B;
}

.type-stat.bug {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.project-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--theme-border);
}

.project-date {
  color: var(--theme-text-secondary);
  font-size: 12px;
}

.user-email {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}
</style>
