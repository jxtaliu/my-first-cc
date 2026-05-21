<template>
  <div class="roles-page">
    <div class="page-header">
      <h2>{{ $t('admin.roles') }}</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> {{ $t('admin.addRole') }}
      </el-button>
    </div>

    <el-card>
      <el-table :data="roles" v-loading="loading" style="width: 100%">
        <el-table-column prop="roleId" :label="$t('admin.roleId')" width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="openDetailDialog(row)">{{ row.roleId }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('admin.roleName')" width="180">
          <template #default="{ row }">
            <div class="role-cell">
              <span class="avatar-small">{{ (row.name || '?').charAt(0).toUpperCase() }}</span>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('admin.description')" />
        <el-table-column prop="userCount" :label="$t('admin.userCount')" width="100">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.userCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userNames" :label="$t('admin.users')" min-width="200">
          <template #default="{ row }">
            <div class="user-names-cell" v-if="row.userNames">
              <el-tag v-for="name in row.userNames.split(', ')" :key="name" size="small" type="info">{{ name }}</el-tag>
            </div>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('admin.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.actions')" width="320">
          <template #default="{ row }">
            <el-button text size="small" @click="openUserDialog(row)">{{ $t('admin.manageUsers') }}</el-button>
            <el-button text size="small" @click="openPermissionDialog(row)">{{ $t('admin.assignPermissions') }}</el-button>
            <el-button text size="small" @click="openDialog(row)">{{ $t('common.edit') }}</el-button>
            <el-button text size="small" type="danger" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? $t('admin.editRole') : $t('admin.addRole')" width="500px">
      <el-form :model="roleForm" ref="formRef" label-width="100px">
        <el-form-item :label="$t('admin.roleId')" prop="roleId">
          <el-input v-model="roleForm.roleId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('admin.roleName')" prop="name">
          <el-input v-model="roleForm.name" />
        </el-form-item>
        <el-form-item :label="$t('admin.description')">
          <el-input v-model="roleForm.description" type="textarea" />
        </el-form-item>
        <el-form-item :label="$t('admin.status')">
          <el-switch v-model="roleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showPermissionDialog" :title="$t('admin.assignPermissions')" width="600px">
      <el-checkbox-group v-model="selectedPermissions">
        <div v-for="module in permissionModules" :key="module.module" class="permission-group">
          <h4>{{ module.module }}</h4>
          <el-checkbox v-for="p in module.permissions" :key="p.id" :label="p.id">{{ p.name }}</el-checkbox>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="showPermissionDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSavePermissions">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" :title="$t('admin.roleDetail')" width="600px">
      <div class="role-detail">
        <div class="role-header">
          <div class="avatar">
            {{ (detailRole.name || detailRole.roleId || '?').charAt(0).toUpperCase() }}
          </div>
          <div class="role-title">
            <h3>{{ detailRole.name || '-' }}</h3>
            <p>{{ detailRole.roleId || '-' }}</p>
          </div>
          <div class="status-badge">
            <el-tag :type="detailRole.status === 1 ? 'success' : 'info'" size="large">
              {{ detailRole.status === 1 ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </div>
        </div>
        <el-divider />
        <div class="role-info-grid">
          <div class="info-item">
            <label>{{ $t('admin.roleId') }}</label>
            <span>{{ detailRole.roleId || '-' }}</span>
          </div>
          <div class="info-item">
            <label>{{ $t('admin.roleName') }}</label>
            <span>{{ detailRole.name || '-' }}</span>
          </div>
          <div class="info-item full-width">
            <label>{{ $t('admin.description') }}</label>
            <span>{{ detailRole.description || '-' }}</span>
          </div>
        </div>
        <el-divider />
        <div class="role-users">
          <h4>{{ $t('admin.roleUsers') }} ({{ roleUsers.length }})</h4>
          <el-table :data="roleUsers" size="small" v-if="roleUsers.length > 0">
            <el-table-column prop="username" :label="$t('admin.username')" />
            <el-table-column prop="email" :label="$t('admin.email')" />
            <el-table-column prop="realName" :label="$t('admin.realName')" />
          </el-table>
          <el-empty v-else :description="$t('admin.noRoleUsers')" :size="60" />
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showDetailDialog = false">{{ $t('common.ok') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showUserDialog" :title="$t('admin.manageRoleUsers')" width="600px">
      <div class="member-dialog-content">
        <div class="member-header">
          <span class="role-name">{{ currentRole?.name }}</span>
        </div>
        <el-table :data="roleUsersList" v-loading="userLoading" size="small">
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
          <el-button type="primary" size="default" @click="handleAddUser">{{ $t('admin.addUser') }}</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const { t } = useI18n()

const roles = ref([])
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const roleForm = ref({})
const formRef = ref()
const showPermissionDialog = ref(false)
const selectedPermissions = ref([])
const permissionModules = ref([])
const currentRoleId = ref(null)
const showDetailDialog = ref(false)
const detailRole = ref({})
const roleUsers = ref([])
const showUserDialog = ref(false)
const currentRole = ref(null)
const roleUsersList = ref([])
const availableUsers = ref([])
const selectedUserId = ref(null)
const userLoading = ref(false)

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/roles')
    const result = await res.json()
    if (result.code === 200) {
      // Fetch user count and user names for each role
      const rolesWithCounts = await Promise.all(result.data.map(async (role) => {
        try {
          const userRes = await fetch(`/api/roles/${role.id}/users`)
          const userResult = await userRes.json()
          const users = userResult.data || []
          role.userCount = users.length
          role.userNames = users.map(u => u.username).join(', ')
        } catch (e) {
          role.userCount = 0
          role.userNames = ''
        }
        return role
      }))
      roles.value = rolesWithCounts
    }
  } catch (e) {
    // Handle error
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  // Simulated grouped permissions
  permissionModules.value = [
    {
      module: 'User Management',
      permissions: [
        { id: 'user:read', name: 'View Users' },
        { id: 'user:create', name: 'Create Users' },
        { id: 'user:update', name: 'Update Users' },
        { id: 'user:delete', name: 'Delete Users' }
      ]
    },
    {
      module: 'Project Management',
      permissions: [
        { id: 'project:read', name: 'View Projects' },
        { id: 'project:create', name: 'Create Projects' },
        { id: 'project:update', name: 'Update Projects' },
        { id: 'project:delete', name: 'Delete Projects' }
      ]
    },
    {
      module: 'Task Management',
      permissions: [
        { id: 'task:read', name: 'View Tasks' },
        { id: 'task:create', name: 'Create Tasks' },
        { id: 'task:update', name: 'Update Tasks' },
        { id: 'task:delete', name: 'Delete Tasks' }
      ]
    },
    {
      module: 'Timesheet',
      permissions: [
        { id: 'timesheet:read', name: 'View Timesheets' },
        { id: 'timesheet:approve', name: 'Approve Timesheets' }
      ]
    }
  ]
}

const openDialog = (role = null) => {
  if (role) {
    isEdit.value = true
    roleForm.value = { ...role }
  } else {
    isEdit.value = false
    roleForm.value = { status: 1 }
  }
  showDialog.value = true
}

const openPermissionDialog = async (role) => {
  currentRoleId.value = role.id
  // Simulated - would fetch from API
  selectedPermissions.value = ['user:read', 'project:read', 'task:read']
  showPermissionDialog.value = true
}

const openDetailDialog = async (role) => {
  detailRole.value = { ...role }
  showDetailDialog.value = true
  // Fetch users for this role
  try {
    const res = await fetch(`/api/roles/${role.id}/users`)
    const result = await res.json()
    if (result.code === 200) {
      roleUsers.value = result.data || []
    }
  } catch (e) {
    roleUsers.value = []
  }
}

const openUserDialog = async (role) => {
  currentRole.value = role
  showUserDialog.value = true
  selectedUserId.value = null
  await loadRoleUsers(role.id)
  await loadAvailableUsers()
}

const loadRoleUsers = async (roleId) => {
  userLoading.value = true
  try {
    const res = await fetch(`/api/roles/${roleId}/users`)
    const result = await res.json()
    if (result.code === 200) {
      roleUsersList.value = result.data || []
    }
  } catch (e) {
    roleUsersList.value = []
  } finally {
    userLoading.value = false
  }
}

const loadAvailableUsers = async () => {
  try {
    const allUsersRes = await fetch('/api/users')
    const allUsersResult = await allUsersRes.json()
    const currentUserIds = roleUsersList.value.map(u => u.id)
    if (allUsersResult.code === 200) {
      availableUsers.value = (allUsersResult.data || []).filter(u => !currentUserIds.includes(u.id))
    }
  } catch (e) {
    availableUsers.value = []
  }
}

const handleAddUser = async () => {
  if (!selectedUserId.value) return
  try {
    // Get current user's roles first
    const roleRes = await fetch(`/api/users/${selectedUserId.value}/roles`)
    const roleResult = await roleRes.json()
    if (roleResult.code !== 200) {
      ElMessage.error(roleResult.message || t('common.failed'))
      return
    }
    const currentRoleIds = roleResult.data || []
    // Add the new role ID
    const newRoleIds = [...currentRoleIds, currentRole.value.id]
    const res = await fetch(`/api/users/${selectedUserId.value}/roles`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ roleIds: newRoleIds })
    })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success(t('admin.userAdded'))
      selectedUserId.value = null
      await loadRoleUsers(currentRole.value.id)
      await loadAvailableUsers()
      await loadRoles()
    } else {
      ElMessage.error(result.message || t('common.failed'))
    }
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

const handleRemoveUser = async (user) => {
  try {
    await ElMessageBox.confirm(t('admin.confirmRemoveUser'), t('common.warning'), { type: 'warning' })
    // Get user's current roles, then filter out this role
    const roleRes = await fetch(`/api/users/${user.id}/roles`)
    const roleResult = await roleRes.json()
    if (roleResult.code === 200) {
      const currentRoleIds = roleResult.data || []
      const otherRoleIds = currentRoleIds.filter(id => id !== currentRole.value.id)
      const res = await fetch(`/api/users/${user.id}/roles`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ roleIds: otherRoleIds })
      })
      const result = await res.json()
      if (result.code === 200) {
        ElMessage.success(t('admin.userRemoved'))
        await loadRoleUsers(currentRole.value.id)
        await loadAvailableUsers()
        await loadRoles()
      } else {
        ElMessage.error(result.message || t('common.failed'))
      }
    }
  } catch (e) {}
}

const handleSubmit = async () => {
  try {
    const url = isEdit.value ? `/api/roles/${roleForm.value.id}` : '/api/roles'
    const method = isEdit.value ? 'PUT' : 'POST'
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        roleId: roleForm.value.roleId,
        name: roleForm.value.name,
        description: roleForm.value.description ?? null,
        status: roleForm.value.status ?? 1
      })
    })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success(isEdit.value ? t('admin.roleUpdated') : t('admin.roleCreated'))
      showDialog.value = false
      loadRoles()
    } else {
      ElMessage.error(result.message || t('common.failed'))
    }
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

const handleSavePermissions = async () => {
  ElMessage.success('Permissions updated')
  showPermissionDialog.value = false
}

const handleDelete = async (role) => {
  try {
    await ElMessageBox.confirm(t('admin.confirmDeleteRole'), t('common.warning'), { type: 'warning' })
    const res = await fetch(`/api/roles/${role.id}`, { method: 'DELETE' })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success(t('admin.roleDeleted'))
      loadRoles()
    } else {
      ElMessage.error(result.message || t('admin.canNotDeleteRole'))
    }
  } catch (e) {}
}

onMounted(() => {
  loadRoles()
  loadPermissions()
})
</script>

<style scoped>
.roles-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.role-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar-small {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.permission-group {
  margin-bottom: 15px;
}

.permission-group h4 {
  margin-bottom: 8px;
  text-transform: capitalize;
}

.role-detail {
  padding: 10px 0;
}

.role-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
}

.role-title {
  flex: 1;
}

.role-title h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}

.role-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.status-badge {
  margin-left: auto;
}

.role-info-grid {
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

.role-users {
  margin-top: 16px;
}

.role-users h4 {
  margin-bottom: 12px;
  color: #303133;
}

.member-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.member-header {
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.role-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.add-member-form {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.user-names-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.no-data {
  color: #909399;
}
</style>
