<template>
  <div class="status-config-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ $t('settings.statusConfig') }}</h1>
        <p class="page-desc">{{ $t('settings.statusConfigDesc') }}</p>
      </div>
      <el-button type="primary" @click="handleAddStatus">
        <el-icon><Plus /></el-icon>
        {{ $t('settings.addStatus') }}
      </el-button>
    </div>

    <!-- Status Categories -->
    <el-tabs v-model="activeCategory" class="status-tabs">
      <el-tab-pane label="Todo" name="TODO">
        <template #label>
          <span class="category-label">
            <span class="category-dot todo"></span>
            {{ $t('settings.todo') }}
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="In Progress" name="IN_PROGRESS">
        <template #label>
          <span class="category-label">
            <span class="category-dot in-progress"></span>
            {{ $t('settings.inProgress') }}
          </span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="Done" name="DONE">
        <template #label>
          <span class="category-label">
            <span class="category-dot done"></span>
            {{ $t('settings.done') }}
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- Status Table -->
    <el-card class="status-card" shadow="never" v-loading="loading">
      <el-table ref="statusTableRef" :data="filteredStatuses" style="width: 100%" row-key="id">
        <el-table-column width="50">
          <template #default>
            <el-icon class="drag-handle"><Rank /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('settings.statusName')" min-width="150">
          <template #default="{ row }">
            <div class="status-name-cell">
              <span class="status-color" :style="{ backgroundColor: row.color }"></span>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" :label="$t('settings.statusCode')" width="120" />
        <el-table-column prop="category" :label="$t('settings.category')" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.category)" size="small">
              {{ getCategoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('project.description')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="taskCount" :label="$t('settings.taskCount')" width="100" align="center" />
        <el-table-column :label="$t('admin.actions')" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="handleEditStatus(row)">
              {{ $t('common.edit') }}
            </el-button>
            <el-button
              v-if="!row.isSystem"
              text
              type="danger"
              size="small"
              @click="handleDeleteStatus(row)"
            >
              {{ $t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Status Dialog -->
    <el-dialog
      v-model="showStatusDialog"
      :title="editingStatus?.id ? $t('settings.editStatus') : $t('settings.addStatus')"
      width="500px"
      class="pm-dialog"
    >
      <el-form :model="statusForm" :rules="statusRules" ref="statusFormRef" label-width="100px">
        <el-form-item :label="$t('settings.statusName')" prop="name">
          <el-input v-model="statusForm.name" :placeholder="$t('settings.enterStatusName')" />
        </el-form-item>
        <el-form-item :label="$t('settings.statusCode')" prop="code">
          <el-input v-model="statusForm.code" :placeholder="$t('settings.enterStatusCode')" :disabled="!!editingStatus?.id" />
        </el-form-item>
        <el-form-item :label="$t('settings.category')" prop="category">
          <el-select v-model="statusForm.category" style="width: 100%">
            <el-option :label="$t('settings.todo')" value="TODO" />
            <el-option :label="$t('settings.inProgress')" value="IN_PROGRESS" />
            <el-option :label="$t('settings.done')" value="DONE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('settings.statusColor')" prop="color">
          <div class="color-picker">
            <el-color-picker v-model="statusForm.color" />
            <span class="color-value">{{ statusForm.color }}</span>
            <div class="color-presets">
              <span
                v-for="c in colorPresets"
                :key="c"
                class="color-preset"
                :style="{ backgroundColor: c }"
                @click="statusForm.color = c"
              ></span>
            </div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('project.description')">
          <el-input v-model="statusForm.description" type="textarea" :rows="2" :placeholder="$t('project.descriptionPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStatusDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSaveStatus">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { getTaskStatusesByProject, createTaskStatus, updateTaskStatus, deleteTaskStatus, reorderTaskStatuses } from '@/api/taskStatus'
import { getTaskCountByStatus } from '@/api/task'

const route = useRoute()
const { t } = useI18n()

const projectId = ref(parseInt(route.params.id) || 1)
const statusTableRef = ref()
const loading = ref(false)
const statuses = ref([])
const activeCategory = ref('TODO')
const showStatusDialog = ref(false)
const editingStatus = ref(null)

const statusForm = ref({
  name: '',
  code: '',
  category: 'TODO',
  color: '#409eff',
  description: ''
})

const statusRules = {
  name: [{ required: true, message: t('settings.statusNameRequired') }],
  code: [{ required: true, message: t('settings.statusCodeRequired') }],
  category: [{ required: true, message: t('settings.categoryRequired') }]
}

const statusFormRef = ref()

const colorPresets = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
  '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b', '#6366f1'
]

const filteredStatuses = computed(() => {
  return statuses.value.filter(s => s.category === activeCategory.value)
})

const fetchStatuses = async () => {
  loading.value = true
  try {
    const res = await getTaskStatusesByProject(projectId.value)
    statuses.value = res.data || getMockStatuses()
  } catch (e) {
    statuses.value = getMockStatuses()
  } finally {
    loading.value = false
  }
}

const getMockStatuses = () => [
  { id: 1, name: '待处理', code: 'TODO', category: 'TODO', color: '#94a3b8', description: '新创建的任务', taskCount: 15, isSystem: true },
  { id: 2, name: '已细化', code: 'REFINED', category: 'TODO', color: '#64748b', description: '需求已细化，可开始', taskCount: 8, isSystem: false },
  { id: 3, name: '开发中', code: 'IN_DEV', category: 'IN_PROGRESS', color: '#3b82f6', description: '正在开发', taskCount: 12, isSystem: true },
  { id: 4, name: '代码评审', code: 'IN_REVIEW', category: 'IN_PROGRESS', color: '#8b5cf6', description: '等待代码评审', taskCount: 5, isSystem: true },
  { id: 5, name: '测试中', code: 'IN_TEST', category: 'IN_PROGRESS', color: '#f59e0b', description: '正在测试', taskCount: 3, isSystem: false },
  { id: 6, name: '已完成', code: 'DONE', category: 'DONE', color: '#10b981', description: '任务已完成', taskCount: 45, isSystem: true },
  { id: 7, name: '已关闭', code: 'CLOSED', category: 'DONE', color: '#6b7280', description: '任务已关闭', taskCount: 20, isSystem: false }
]

const getCategoryTagType = (category) => {
  const types = {
    'TODO': 'info',
    'IN_PROGRESS': 'primary',
    'DONE': 'success'
  }
  return types[category] || 'info'
}

const getCategoryLabel = (category) => {
  const labels = {
    'TODO': t('settings.todo'),
    'IN_PROGRESS': t('settings.inProgress'),
    'DONE': t('settings.done')
  }
  return labels[category] || category
}

const handleAddStatus = () => {
  editingStatus.value = null
  statusForm.value = {
    name: '',
    code: '',
    category: activeCategory.value,
    color: '#409eff',
    description: ''
  }
  showStatusDialog.value = true
}

const handleEditStatus = (status) => {
  editingStatus.value = status
  statusForm.value = {
    name: status.name,
    code: status.code,
    category: status.category,
    color: status.color,
    description: status.description || ''
  }
  showStatusDialog.value = true
}

const handleSaveStatus = async () => {
  const valid = await statusFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (editingStatus.value?.id) {
      await updateTaskStatus(editingStatus.value.id, statusForm.value)
      ElMessage.success(t('settings.statusUpdated'))
    } else {
      await createTaskStatus({ ...statusForm.value, projectId: projectId.value })
      ElMessage.success(t('settings.statusCreated'))
    }
    showStatusDialog.value = false
    fetchStatuses()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

const handleDeleteStatus = async (status) => {
  try {
    // Check if there are tasks using this status
    const res = await getTaskCountByStatus(status.id)
    const count = res.data || res
    if (count > 0) {
      ElMessage.warning(t('project.cannotDeleteStatusWithTasks', { count }))
      return
    }
    await ElMessageBox.confirm(
      t('settings.confirmDeleteStatus', { name: status.name }),
      t('common.warning'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    await deleteTaskStatus(status.id)
    ElMessage.success(t('settings.statusDeleted'))
    fetchStatuses()
  } catch (e) {
    // Cancelled or error
  }
}

const onDragEnd = async () => {
  const statusIds = filteredStatuses.value.map(s => s.id)
  try {
    await reorderTaskStatuses(statusIds)
    ElMessage.success(t('project.statusReordered'))
  } catch (e) {
    ElMessage.error(t('common.failed'))
    fetchStatuses()
  }
}

let sortableInstance = null

const initSortable = () => {
  if (sortableInstance) {
    sortableInstance.destroy()
  }
  const table = statusTableRef.value?.$el
  if (!table) return

  const el = table.querySelector('.el-table__body-wrapper tbody')
  if (!el) return

  sortableInstance = Sortable.create(el, {
    handle: '.drag-handle',
    onEnd: onDragEnd
  })
}

onMounted(async () => {
  fetchStatuses()
  await nextTick()
  initSortable()
})
</script>

<style scoped>
.status-config-page {
  max-width: 1000px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 4px 0;
}

.page-desc {
  margin: 0;
  color: var(--pm-text-secondary);
  font-size: 14px;
}

.status-tabs {
  margin-bottom: 16px;
}

.category-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.category-dot.todo { background-color: #94a3b8; }
.category-dot.in-progress { background-color: #3b82f6; }
.category-dot.done { background-color: #10b981; }

.status-card {
  margin-bottom: 16px;
}

.drag-handle {
  cursor: move;
  color: var(--pm-text-muted);
}

.status-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  flex-shrink: 0;
}

.color-picker {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-value {
  font-family: monospace;
  color: var(--pm-text-secondary);
}

.color-presets {
  display: flex;
  gap: 6px;
}

.color-preset {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.color-preset:hover {
  border-color: var(--pm-text-secondary);
}
</style>
