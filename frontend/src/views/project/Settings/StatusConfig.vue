<template>
  <div class="status-config-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <p class="page-desc">{{ $t('settings.statusConfigDesc') }}</p>
      </div>
      <el-button type="primary" @click="handleAddStatus">
        <el-icon><Plus /></el-icon>
        {{ $t('settings.addStatus') }}
      </el-button>
    </div>

    <!-- Status Table -->
    <el-card class="status-card" shadow="never" v-loading="loading">
      <el-table ref="statusTableRef" :data="statuses" style="width: 100%" row-key="id">
        <!-- Drag Handle -->
        <el-table-column width="50">
          <template #default="{ row }">
            <span :data-status-id="row.id"><el-icon class="drag-handle"><Rank /></el-icon></span>
          </template>
        </el-table-column>

        <!-- Status Name (Chinese + English) -->
        <el-table-column :label="$t('settings.statusName')" min-width="180">
          <template #default="{ row }">
            <div class="status-name-cell">
              <span class="status-color-dot" :style="{ backgroundColor: row.color }"></span>
              <span class="status-name">{{ row.nameZh || '' }} {{ row.nameEn ? `(${row.nameEn})` : '' }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- Status Code -->
        <el-table-column prop="code" :label="$t('settings.statusCode')" width="140">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.code }}</el-tag>
          </template>
        </el-table-column>

        <!-- Color Preview -->
        <el-table-column :label="$t('settings.statusColor')" width="100" align="center">
          <template #default="{ row }">
            <span class="color-preview" :style="{ backgroundColor: row.color }"></span>
          </template>
        </el-table-column>

        <!-- Sort Order -->
        <el-table-column prop="sortOrder" :label="$t('settings.sortOrder')" width="100" align="center" />

        <!-- Actions -->
        <el-table-column :label="$t('admin.actions')" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="handleEditStatus(row)">
              {{ $t('common.edit') }}
            </el-button>
            <el-button
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
        <el-form-item :label="$t('settings.statusCode')" prop="code">
          <el-input
            v-model="statusForm.code"
            :placeholder="$t('settings.enterStatusCode')"
            :disabled="!!editingStatus?.id"
          />
        </el-form-item>
        <el-form-item :label="$t('settings.statusNameEn')" prop="nameEn">
          <el-input v-model="statusForm.nameEn" :placeholder="$t('settings.enterStatusNameEn')" />
        </el-form-item>
        <el-form-item :label="$t('settings.statusNameZh')" prop="nameZh">
          <el-input v-model="statusForm.nameZh" :placeholder="$t('settings.enterStatusNameZh')" />
        </el-form-item>
        <el-form-item :label="$t('settings.statusColor')" prop="color">
          <div class="color-picker-row">
            <el-color-picker v-model="statusForm.color" />
            <span class="color-value">{{ statusForm.color }}</span>
          </div>
          <div class="color-presets">
            <span
              v-for="c in colorPresets"
              :key="c"
              class="color-preset"
              :class="{ active: statusForm.color === c }"
              :style="{ backgroundColor: c }"
              @click="statusForm.color = c"
            ></span>
          </div>
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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Rank } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import { getTaskCountByStatus } from '@/api/task'
import { useTaskStatusStore } from '@/stores/taskStatus'

const props = defineProps({
  projectId: {
    type: [String, Number],
    default: null
  }
})

const route = useRoute()
const { t } = useI18n()

const taskStatusStore = useTaskStatusStore()

// Project ID from props or route
const projectIdSource = computed(() => props.projectId || route.params.id)

// Resolved business ID (cached after first resolution)
const resolvedBusinessId = ref(props.projectId)

// Get actual business ID for API calls (resolves numeric ID to business ID)
const getBusinessIdForApi = async () => {
  const id = projectIdSource.value
  if (!id) return null
  // If it looks like a business ID (contains letters), return as-is
  if (/^[A-Z]/.test(id)) {
    resolvedBusinessId.value = id
    return id
  }
  // Otherwise resolve numeric ID to business ID
  const businessId = await taskStatusStore.getProjectBusinessId(id)
  resolvedBusinessId.value = businessId
  return businessId
}

// Use store's statuses (reactive) - use resolved business ID as key
const statuses = computed(() => {
  return taskStatusStore.getStatusesByProjectId(resolvedBusinessId.value) || []
})

const loading = computed(() => taskStatusStore.loading)

const statusTableRef = ref()
const showStatusDialog = ref(false)
const editingStatus = ref(null)

const statusForm = ref({
  code: '',
  nameEn: '',
  nameZh: '',
  color: '#409eff'
})

const statusRules = {
  code: [{ required: true, message: t('settings.statusCodeRequired') }]
}

const statusFormRef = ref()

const colorPresets = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
  '#8b5cf6', '#06b6d4', '#10b981', '#f59e0b', '#6366f1'
]

// Fetch statuses on mount and when project changes
const fetchStatuses = async () => {
  const businessId = await getBusinessIdForApi()
  if (!businessId) return
  await taskStatusStore.fetchStatuses(businessId)
}

const handleAddStatus = () => {
  editingStatus.value = null
  statusForm.value = {
    code: '',
    nameEn: '',
    nameZh: '',
    color: '#409eff'
  }
  showStatusDialog.value = true
}

const handleEditStatus = (status) => {
  editingStatus.value = status
  statusForm.value = {
    code: status.code,
    nameEn: status.nameEn || '',
    nameZh: status.nameZh || '',
    color: status.color || '#409eff'
  }
  showStatusDialog.value = true
}

const handleSaveStatus = async () => {
  const valid = await statusFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    const businessId = await getBusinessIdForApi()
    if (editingStatus.value?.id) {
      await taskStatusStore.updateStatus(editingStatus.value.id, statusForm.value)
      // Update cache optimistically
      taskStatusStore.updateStatusInCache(businessId, editingStatus.value.id, statusForm.value)
      ElMessage.success(t('settings.statusUpdated'))
    } else {
      await taskStatusStore.createStatus(businessId, statusForm.value)
      ElMessage.success(t('settings.statusCreated'))
    }
    showStatusDialog.value = false
    // Store already updated via actions, no need to refetch
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
      t('settings.confirmDeleteStatus', { name: status.nameZh || status.nameEn || status.code }),
      t('common.warning'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    const businessId = await getBusinessIdForApi()
    await taskStatusStore.deleteStatus(status.id)
    // Remove from cache optimistically
    taskStatusStore.removeStatusFromCache(businessId, status.id)
    ElMessage.success(t('settings.statusDeleted'))
  } catch (e) {
    // Cancelled or error
  }
}

const onDragEnd = async () => {
  const businessId = await getBusinessIdForApi()
  if (!businessId) return

  // Get new order from DOM (Sortable.js has moved DOM elements)
  const table = statusTableRef.value?.$el
  if (!table) return

  const rows = table.querySelectorAll('.el-table__body-wrapper tbody tr')
  const newOrderIds = Array.from(rows).map(row => {
    const span = row.querySelector('[data-status-id]')
    const key = span ? span.getAttribute('data-status-id') : null
    return key ? parseInt(key) : null
  }).filter(id => id !== null)

  if (newOrderIds.length === 0) return

  try {
    // Update local statuses order to match DOM order
    const newOrderStatuses = []
    for (const id of newOrderIds) {
      const status = statuses.value.find(s => s.id === id)
      if (status) newOrderStatuses.push(status)
    }
    // Update sortOrder based on new positions
    newOrderStatuses.forEach((status, index) => {
      status.sortOrder = index + 1
    })

    // Call API to persist the new order
    await taskStatusStore.reorderStatuses(businessId, newOrderIds)

    // Update store cache with new statuses and sort orders
    taskStatusStore.statusesByProject[businessId] = newOrderStatuses

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
  await fetchStatuses()
  await nextTick()
  initSortable()
})

// Re-init sortable when statuses change
watch(() => statuses.value.length, async () => {
  await nextTick()
  initSortable()
})
</script>

<style scoped>
.status-config-page {
  width: 100%;
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
  gap: 10px;
}

.status-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-name {
  font-size: 14px;
}

.color-preview {
  display: inline-block;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  vertical-align: middle;
}

.color-picker-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.color-value {
  font-family: monospace;
  color: var(--pm-text-secondary);
}

.color-presets {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.color-preset {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s, transform 0.2s;
}

.color-preset:hover {
  border-color: var(--pm-text-secondary);
  transform: scale(1.1);
}

.color-preset.active {
  border-color: var(--pm-text-primary);
  box-shadow: 0 0 0 2px var(--pm-bg-color);
}
</style>
