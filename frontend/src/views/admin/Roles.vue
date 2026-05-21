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
        <el-table-column prop="roleId" :label="$t('admin.roleId')" width="120" />
        <el-table-column prop="name" :label="$t('admin.roleName')" width="180">
          <template #default="{ row }">
            <div class="role-cell">
              <span class="avatar-small">{{ (row.name || '?').charAt(0).toUpperCase() }}</span>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('admin.description')" />
        <el-table-column prop="status" :label="$t('admin.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.actions')" width="250">
          <template #default="{ row }">
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

const loadRoles = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/roles')
    const result = await res.json()
    if (result.code === 200) {
      roles.value = result.data
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

const handleSubmit = async () => {
  try {
    const url = isEdit.value ? `/api/roles/${roleForm.value.id}` : '/api/roles'
    const method = isEdit.value ? 'PUT' : 'POST'
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(roleForm.value)
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
</style>
