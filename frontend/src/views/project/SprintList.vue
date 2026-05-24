<template>
  <div class="sprint-list-page">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="router.push(`/projects/${projectId}`)">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2>{{ $t('project.sprintList') }}</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon> {{ $t('project.createSprint') }}
        </el-button>
      </div>
    </div>

    <div class="sprint-list" v-loading="loading">
      <div v-if="sprints.length === 0" class="empty-state">
        <el-empty :description="$t('project.noSprints')" />
      </div>

      <div
        v-for="sprint in sprints"
        :key="sprint.id"
        :class="['sprint-card', 'status-' + sprint.status]"
      >
        <div class="sprint-header">
          <div class="sprint-title">
            <span class="sprint-name">{{ sprint.name }}</span>
            <el-tag :type="getStatusType(sprint.status)" size="small">
              {{ getStatusLabel(sprint.status) }}
            </el-tag>
          </div>
          <div class="sprint-dates">
            {{ formatDate(sprint.startDate) }} ~ {{ formatDate(sprint.endDate) }}
          </div>
        </div>

        <div class="sprint-goal" v-if="sprint.goal">
          {{ $t('project.goal') }}：{{ sprint.goal }}
        </div>

        <div class="sprint-stats">
          <span class="stat-item">
            {{ $t('project.tasks') }}：{{ sprint.totalTasks || 0 }}/{{ sprint.totalTasks || 0 }}
          </span>
          <span class="stat-item" v-if="sprint.capacityHours">
            {{ $t('project.capacity') }}：{{ sprint.capacityHours }}h
          </span>
          <span class="stat-item" v-if="sprint.velocity">
            {{ $t('project.velocity') }}：{{ sprint.velocity }}
          </span>
        </div>

        <div class="sprint-actions">
          <el-button size="small" @click="viewDetail(sprint)">{{ $t('project.viewDetail') }}</el-button>
          <el-button
            v-if="sprint.status === 'PLANNING'"
            size="small"
            type="primary"
            @click="handleStart(sprint)"
          >
            {{ $t('project.startSprint') }}
          </el-button>
          <el-button
            v-if="sprint.status === 'ACTIVE'"
            size="small"
            type="success"
            @click="handleComplete(sprint)"
          >
            {{ $t('project.completeSprint') }}
          </el-button>
          <el-button
            v-if="sprint.status === 'PLANNING'"
            size="small"
            type="danger"
            @click="handleDelete(sprint)"
          >
            {{ $t('common.delete') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Create Sprint Dialog -->
    <el-dialog v-model="showCreateDialog" :title="$t('project.createSprint')" width="500px">
      <el-form :model="sprintForm" :rules="sprintRules" ref="sprintFormRef" label-width="100px">
        <el-form-item :label="$t('project.sprintName')" prop="name">
          <el-input v-model="sprintForm.name" />
        </el-form-item>
        <el-form-item :label="$t('project.goal')" prop="goal">
          <el-input v-model="sprintForm.goal" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item :label="$t('project.startDate')" prop="startDate">
          <el-date-picker v-model="sprintForm.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('project.endDate')" prop="endDate">
          <el-date-picker v-model="sprintForm.endDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('project.capacity')" prop="capacityHours">
          <el-input-number v-model="sprintForm.capacityHours" :min="0" :step="8" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreate">{{ $t('common.create') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const projectId = route.params.id
const loading = ref(false)
const sprints = ref([])
const showCreateDialog = ref(false)

const sprintForm = reactive({
  name: '',
  goal: '',
  startDate: null,
  endDate: null,
  capacityHours: 80
})

const sprintRules = {
  name: [{ required: true, message: () => t('project.sprintNameRequired') }],
  startDate: [{ required: true, message: () => t('project.startDateRequired') }],
  endDate: [{ required: true, message: () => t('project.endDateRequired') }]
}

const sprintFormRef = ref()

const fetchSprints = async () => {
  loading.value = true
  try {
    const res = await request.get(`/v1/projects/${projectId}/sprints`)
    const sprintList = res.data || []

    // Fetch stats for each sprint
    const sprintsWithStats = await Promise.all(
      sprintList.map(async (sprint) => {
        try {
          const statsRes = await request.get(`/v1/projects/${projectId}/sprints/${sprint.id}/stats`)
          return { ...sprint, ...statsRes.data }
        } catch {
          return sprint
        }
      })
    )
    sprints.value = sprintsWithStats
  } catch (e) {
    ElMessage.error(t('project.fetchSprintsFailed'))
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  const valid = await sprintFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await request.post(`/v1/projects/${projectId}/sprints`, sprintForm)
    ElMessage.success(t('project.sprintCreated'))
    showCreateDialog.value = false
    fetchSprints()
    // Reset form
    sprintForm.name = ''
    sprintForm.goal = ''
    sprintForm.startDate = null
    sprintForm.endDate = null
    sprintForm.capacityHours = 80
  } catch (e) {
    ElMessage.error(t('project.createSprintFailed'))
  }
}

const handleStart = async (sprint) => {
  try {
    await request.put(`/v1/projects/${projectId}/sprints/${sprint.id}/start`)
    ElMessage.success(t('project.sprintStarted'))
    fetchSprints()
  } catch (e) {
    ElMessage.error(t('project.startSprintFailed'))
  }
}

const handleComplete = async (sprint) => {
  try {
    await ElMessageBox.confirm(t('project.confirmCompleteSprint'), t('common.confirm'), {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await request.put(`/v1/projects/${projectId}/sprints/${sprint.id}/complete`)
    ElMessage.success(t('project.sprintCompleted'))
    fetchSprints()
  } catch (e) {
    // User cancelled or error
  }
}

const handleDelete = async (sprint) => {
  try {
    await ElMessageBox.confirm(t('project.confirmDeleteSprint'), t('common.confirm'), {
      confirmButtonText: t('common.ok'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await request.delete(`/v1/projects/${projectId}/sprints/${sprint.id}`)
    ElMessage.success(t('project.sprintDeleted'))
    fetchSprints()
  } catch (e) {
    // User cancelled or error
  }
}

const viewDetail = (sprint) => {
  router.push(`/projects/${projectId}/sprints/${sprint.id}`)
}

const getStatusType = (status) => {
  const typeMap = {
    'PLANNING': 'info',
    'ACTIVE': 'success',
    'COMPLETED': '',
    'ARCHIVED': 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusLabel = (status) => {
  const labelMap = {
    'PLANNING': t('project.sprintPlanning'),
    'ACTIVE': t('project.sprintActive'),
    'COMPLETED': t('project.sprintCompleted'),
    'ARCHIVED': t('project.sprintArchived')
  }
  return labelMap[status] || status
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  fetchSprints()
})
</script>

<style scoped>
.sprint-list-page {
  padding: 20px;
}

.page-header {
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

.sprint-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  padding: 60px 0;
}

.sprint-card {
  background: var(--theme-card-bg);
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid var(--theme-border);
}

.sprint-card.status-PLANNING {
  border-left-color: #909399;
}

.sprint-card.status-ACTIVE {
  border-left-color: #67c23a;
}

.sprint-card.status-COMPLETED {
  border-left-color: #409eff;
}

.sprint-header {
  margin-bottom: 12px;
}

.sprint-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.sprint-name {
  font-size: 16px;
  font-weight: 600;
}

.sprint-dates {
  font-size: 13px;
  color: var(--theme-text-secondary);
}

.sprint-goal {
  font-size: 14px;
  color: var(--theme-text-primary);
  margin-bottom: 12px;
  padding: 8px;
  background: var(--theme-border);
  border-radius: 4px;
}

.sprint-stats {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: var(--theme-text-secondary);
  margin-bottom: 12px;
}

.sprint-actions {
  display: flex;
  gap: 8px;
}
</style>
