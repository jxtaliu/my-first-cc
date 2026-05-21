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
      <el-tab-pane :label="$t('project.active')" name="active" />
      <el-tab-pane :label="$t('project.archived')" name="archived" />
    </el-tabs>

    <el-row :gutter="20">
      <el-col v-for="project in projects" :key="project.id" :span="8">
        <el-card class="project-card" @click="goToDetail(project.id)">
          <div class="project-header">
            <span class="project-name">{{ project.name }}</span>
            <el-tag :type="project.type === 'SCRUM' ? 'success' : 'warning'" size="small">
              {{ project.type }}
            </el-tag>
          </div>
          <p class="project-desc">{{ project.description || $t('project.noDescription') }}</p>
          <div class="project-stats">
            <span><el-icon><Document /></el-icon> {{ project.taskCount || 0 }} {{ $t('project.tasks') }}</span>
            <span><el-icon><User /></el-icon> {{ project.memberCount || 0 }} {{ $t('project.members') }}</span>
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
                  <el-dropdown-item command="archive" v-if="!project.archivedAt">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? $t('common.edit') : $t('common.add')" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item :label="$t('project.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('project.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('project.type')" prop="type">
          <el-select v-model="form.type">
            <el-option :label="$t('project.scrum')" value="SCRUM" />
            <el-option :label="$t('project.kanban')" value="KANBAN" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('project.department')" prop="departmentId">
          <el-select v-model="form.departmentId" :placeholder="$t('timesheet.selectProject')">
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
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
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document, User, More } from '@element-plus/icons-vue'
import { getProjects, createProject, updateProject, deleteProject, archiveProject, restoreProject } from '@/api/project'

const router = useRouter()
const { t } = useI18n()
const activeTab = ref('all')
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const projects = ref([])
const formRef = ref()

const form = reactive({
  id: null,
  name: '',
  description: '',
  type: 'SCRUM',
  departmentId: null
})

const rules = {
  name: [{ required: true, message: () => t('project.projectNameRequired') }],
  type: [{ required: true, message: () => t('project.projectTypeRequired') }]
}

const departments = ref([
  { id: 1, name: 'Engineering' },
  { id: 2, name: 'Product' },
  { id: 3, name: 'Design' }
])

const fetchProjects = async () => {
  loading.value = true
  try {
    const params = activeTab.value === 'archived' ? { archived: true } : {}
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
  Object.assign(form, { id: null, name: '', description: '', type: 'SCRUM', departmentId: null })
  dialogVisible.value = true
}

const handleEdit = (project) => {
  isEdit.value = true
  Object.assign(form, { ...project })
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

onMounted(() => {
  fetchProjects()
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.project-name {
  font-size: 18px;
  font-weight: bold;
}

.project-desc {
  color: var(--theme-text-secondary);
  font-size: 14px;
  margin: 8px 0;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
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
</style>
