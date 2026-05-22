<template>
  <div class="departments-page">
    <div class="page-header">
      <h2>{{ $t('admin.departments') }}</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> {{ $t('admin.addDepartment') }}
      </el-button>
    </div>

    <el-card>
      <el-table :data="departments" v-loading="loading" style="width: 100%">
        <el-table-column prop="departmentId" :label="$t('admin.departmentId')" width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="openDetailDialog(row)">{{ row.departmentId }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('admin.departmentName')" min-width="200">
          <template #default="{ row }">
            <div class="dept-cell">
              <span class="avatar-small">{{ (row.name || '?').charAt(0).toUpperCase() }}</span>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="parentId" :label="$t('admin.parentDepartment')" width="150">
          <template #default="{ row }">
            {{ getParentName(row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" :label="$t('admin.sortOrder')" width="80" />
        <el-table-column prop="memberCount" :label="$t('admin.memberCount')" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.memberCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberNames" :label="$t('admin.members')" min-width="200">
          <template #default="{ row }">
            <div class="user-names-cell" v-if="row.memberNames">
              <el-tag v-for="name in row.memberNames.split(', ')" :key="name" size="small" type="info">{{ name }}</el-tag>
            </div>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('admin.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.actions')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openMemberDialog(row)">{{ $t('admin.members') }}</el-button>
            <el-button text size="small" @click="openDialog(row)">{{ $t('common.edit') }}</el-button>
            <el-button text size="small" type="danger" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? $t('admin.editDepartment') : $t('admin.addDepartment')" width="500px">
      <el-form :model="departmentForm" ref="formRef" label-width="100px">
        <el-form-item :label="$t('admin.departmentId')" prop="departmentId">
          <el-input v-model="departmentForm.departmentId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('admin.departmentName')" prop="name">
          <el-input v-model="departmentForm.name" />
        </el-form-item>
        <el-form-item :label="$t('admin.parentDepartment')">
          <el-select v-model="departmentForm.parentId" clearable :placeholder="$t('admin.selectParent')" style="width: 100%">
            <el-option v-for="dept in flatDepartments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('admin.sortOrder')">
          <el-input-number v-model="departmentForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('admin.status')">
          <el-switch v-model="departmentForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showMemberDialog" :title="$t('admin.memberManagement')" width="600px">
      <div class="member-dialog-content">
        <div class="member-header">
          <span class="dept-name">{{ currentDept?.name }}</span>
        </div>
        <el-table :data="departmentUsers" v-loading="memberLoading" size="small">
          <el-table-column prop="username" :label="$t('admin.username')" />
          <el-table-column prop="email" :label="$t('admin.email')" />
          <el-table-column prop="realName" :label="$t('admin.realName')" />
          <el-table-column :label="$t('admin.actions')" width="100">
            <template #default="{ row }">
              <el-button text size="small" type="danger" @click="handleRemoveUser(row)">{{ $t('common.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="add-member-form">
          <el-select v-model="selectedUserId" :placeholder="$t('admin.selectUser')" size="default" clearable style="width: 300px">
            <el-option v-for="user in availableUsers" :key="user.id" :label="user.username" :value="user.id" />
          </el-select>
          <el-button type="primary" size="default" @click="handleAddUser">{{ $t('admin.addMember') }}</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" :title="$t('admin.departmentDetail')" width="500px">
      <div class="dept-detail">
        <div class="dept-header">
          <div class="avatar">
            {{ (detailDept.name || detailDept.departmentId || '?').charAt(0).toUpperCase() }}
          </div>
          <div class="dept-title">
            <h3>{{ detailDept.name || '-' }}</h3>
            <p>{{ detailDept.departmentId || '-' }}</p>
          </div>
          <div class="status-badge">
            <el-tag :type="detailDept.status === 1 ? 'success' : 'info'" size="large">
              {{ detailDept.status === 1 ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </div>
        </div>
        <el-divider />
        <div class="dept-info-grid">
          <div class="info-item">
            <label>{{ $t('admin.departmentId') }}</label>
            <span>{{ detailDept.departmentId || '-' }}</span>
          </div>
          <div class="info-item">
            <label>{{ $t('admin.departmentName') }}</label>
            <span>{{ detailDept.name || '-' }}</span>
          </div>
          <div class="info-item">
            <label>{{ $t('admin.parentDepartment') }}</label>
            <span>{{ getParentName(detailDept.parentId) }}</span>
          </div>
          <div class="info-item">
            <label>{{ $t('admin.sortOrder') }}</label>
            <span>{{ detailDept.sortOrder ?? '-' }}</span>
          </div>
        </div>
        <el-divider />
        <div class="dept-users">
          <h4>{{ $t('admin.deptUsers') }} ({{ deptUsers.length }})</h4>
          <el-table :data="deptUsers" size="small" v-if="deptUsers.length > 0">
            <el-table-column prop="username" :label="$t('admin.username')" />
            <el-table-column prop="email" :label="$t('admin.email')" />
            <el-table-column prop="realName" :label="$t('admin.realName')" />
          </el-table>
          <el-empty v-else :description="$t('admin.noDeptUsers')" :size="60" />
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showDetailDialog = false">{{ $t('common.ok') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const { t } = useI18n()

const departments = ref([])
const departmentTree = ref([])
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const departmentForm = ref({})
const formRef = ref()
const showMemberDialog = ref(false)
const currentDept = ref(null)
const departmentUsers = ref([])
const availableUsers = ref([])
const selectedUserId = ref(null)
const memberLoading = ref(false)
const showDetailDialog = ref(false)
const detailDept = ref({})
const deptUsers = ref([])

const flatDepartments = computed(() => {
  const flatten = (nodes, result = []) => {
    for (const node of nodes) {
      result.push(node)
      if (node.children && node.children.length) {
        flatten(node.children, result)
      }
    }
    return result
  }
  return flatten(departmentTree.value)
})

const getParentName = (parentId) => {
  if (!parentId) return '-'
  const parent = flatDepartments.value.find(d => d.id === parentId)
  return parent ? parent.name : '-'
}

const loadDepartments = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/departments/tree')
    const result = await res.json()
    if (result.code === 200) {
      departmentTree.value = result.data
      // Flatten for table display
      const flatten = (nodes, result = []) => {
        for (const node of nodes) {
          result.push(node)
          if (node.children && node.children.length) {
            flatten(node.children, result)
          }
        }
        return result
      }
      const flatDepts = flatten(result.data)
      // Fetch member names for each department
      const deptsWithMembers = await Promise.all(flatDepts.map(async (dept) => {
        try {
          const userRes = await fetch(`/api/departments/${dept.id}/users`)
          const userResult = await userRes.json()
          const users = userResult.data || []
          dept.memberCount = users.length
          dept.memberNames = users.map(u => u.username).join(', ')
        } catch (e) {
          dept.memberCount = 0
          dept.memberNames = ''
        }
        return dept
      }))
      departments.value = deptsWithMembers
    }
  } catch (e) {
    // Handle error
  } finally {
    loading.value = false
  }
}

const openDialog = (dept = null) => {
  if (dept) {
    isEdit.value = true
    departmentForm.value = { ...dept }
  } else {
    isEdit.value = false
    departmentForm.value = { status: 1, sortOrder: 0 }
  }
  showDialog.value = true
}

const handleSubmit = async () => {
  const method = isEdit.value ? 'PUT' : 'POST'
  const url = isEdit.value ? `/api/departments/${departmentForm.value.id}` : '/api/departments'
  const res = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      departmentId: departmentForm.value.departmentId ?? null,
      name: departmentForm.value.name,
      parentId: departmentForm.value.parentId ?? null,
      sortOrder: departmentForm.value.sortOrder ?? 0,
      status: departmentForm.value.status ?? 1
    })
  })
  const result = await res.json()
  if (result.code === 200) {
    showDialog.value = false
    ElMessage.success(isEdit.value ? t('admin.departmentUpdated') : t('admin.departmentCreated'))
    loadDepartments()
  } else {
    ElMessage.error(result.message || 'Operation failed')
  }
}

const handleDelete = async (dept) => {
  try {
    await ElMessageBox.confirm(t('admin.confirmDeleteDepartment'), t('common.warning'), { type: 'warning' })
    const res = await fetch(`/api/departments/${dept.id}`, { method: 'DELETE' })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success(t('admin.departmentDeleted'))
      loadDepartments()
    } else {
      ElMessage.error(result.message || t('admin.canNotDeleteDepartment'))
    }
  } catch (e) {}
}

const openMemberDialog = async (dept) => {
  currentDept.value = dept
  showMemberDialog.value = true
  selectedUserId.value = null
  await loadDepartmentUsers(dept.id)
  await loadAvailableUsers()
}

const openDetailDialog = async (dept) => {
  detailDept.value = { ...dept }
  showDetailDialog.value = true
  // Fetch users for this department
  try {
    const res = await fetch(`/api/departments/${dept.id}/users`)
    const result = await res.json()
    if (result.code === 200) {
      deptUsers.value = result.data || []
    }
  } catch (e) {
    deptUsers.value = []
  }
}

const loadDepartmentUsers = async (deptId) => {
  memberLoading.value = true
  try {
    const res = await fetch(`/api/departments/${deptId}/users`)
    const result = await res.json()
    if (result.code === 200) {
      departmentUsers.value = result.data || []
    }
  } catch (e) {
    // Handle error
  } finally {
    memberLoading.value = false
  }
}

const loadAvailableUsers = async () => {
  try {
    const res = await fetch('/api/departments/users/available')
    const result = await res.json()
    if (result.code === 200) {
      availableUsers.value = result.data || []
    }
  } catch (e) {
    // Handle error
  }
}

const handleAddUser = async () => {
  if (!selectedUserId.value) return
  try {
    const res = await fetch(`/api/departments/${currentDept.value.id}/users/${selectedUserId.value}`, {
      method: 'POST'
    })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success(t('admin.memberAdded'))
      selectedUserId.value = null
      await loadDepartmentUsers(currentDept.value.id)
      await loadAvailableUsers()
      await loadDepartments()
    } else {
      ElMessage.error(result.message || t('common.failed'))
    }
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

const handleRemoveUser = async (user) => {
  try {
    await ElMessageBox.confirm(t('admin.confirmRemoveMember'), t('common.warning'), { type: 'warning' })
    const res = await fetch(`/api/departments/${currentDept.value.id}/users/${user.id}`, {
      method: 'DELETE'
    })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success(t('admin.memberRemoved'))
      await loadDepartmentUsers(currentDept.value.id)
      await loadAvailableUsers()
      await loadDepartments()
    } else {
      ElMessage.error(result.message || t('common.failed'))
    }
  } catch (e) {}
}

onMounted(loadDepartments)
</script>

<style scoped>
.departments-page {
  padding: 24px;
  background: #F5F7FA;
  min-height: 100vh;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1D2129;
}

.page-header :deep(.el-button--primary) {
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  border: none;
  padding: 10px 20px;
  height: auto;
}

.page-header :deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.dept-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

:deep(.el-card) {
  border-radius: 12px;
  border: 1px solid #EBEEF5;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

:deep(.el-table) {
  --el-table-border-color: #EBEEF5;
  --el-table-header-bg-color: #FAFBFC;
}

:deep(.el-table__header th) {
  background: #FAFBFC;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

:deep(.el-table__row:hover > td) {
  background: #F5F7FA;
}

.member-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.member-header {
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.dept-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.add-member-form {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #EBEEF5;
}

.dept-detail {
  padding: 10px 0;
}

.dept-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dept-header .avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.dept-title {
  flex: 1;
}

.dept-title h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1D2129;
}

.dept-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.status-badge {
  margin-left: auto;
}

.dept-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 10px 0;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  color: #909399;
  font-size: 12px;
}

.info-item span {
  color: #303133;
  font-size: 14px;
}

.dept-users {
  margin-top: 16px;
}

.dept-users h4 {
  margin-bottom: 12px;
  color: #303133;
  font-weight: 600;
}

.user-names-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.user-names-cell :deep(.el-tag) {
  border-radius: 4px;
  font-size: 12px;
}

.no-data {
  color: #909399;
  font-size: 13px;
}

/* Dialog styling */
:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #EBEEF5;
  margin: 0;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 16px;
  color: #1D2129;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid #EBEEF5;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
}

:deep(.el-select) {
  width: 100%;
}

/* Fixed column border */
:deep(.el-table .el-table__body-wrapper .el-table__fixed),
:deep(.el-table .el-table__body-wrapper .el-table__fixed-right) {
  border-left: 1px solid #EBEEF5;
}
</style>
