<template>
  <div class="users-page">
    <div class="page-header">
      <h2>User Management</h2>
      <el-button type="primary" @click="showUserDialog = true">
        <el-icon><Plus /></el-icon> Add User
      </el-button>
    </div>

    <el-card>
      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="username" label="Username" width="150" />
        <el-table-column prop="email" label="Email" width="200" />
        <el-table-column prop="department" label="Department" width="150" />
        <el-table-column prop="role" label="System Role" width="150">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.systemRole)" size="small">
              {{ row.systemRole }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="Status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="200">
          <template #default="{ row }">
            <el-button text size="small" @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-button text size="small" type="danger" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="showUserDialog" :title="isEdit ? 'Edit User' : 'Add User'" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="Username" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="userForm.email" type="email" />
        </el-form-item>
        <el-form-item label="Password" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item label="Department" prop="department">
          <el-select v-model="userForm.department" style="width: 100%">
            <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="System Role" prop="systemRole">
          <el-select v-model="userForm.systemRole" style="width: 100%">
            <el-option label="Super Admin" value="SUPER_ADMIN" />
            <el-option label="Department Admin" value="DEPT_ADMIN" />
            <el-option label="Project Admin" value="PROJECT_ADMIN" />
            <el-option label="Member" value="MEMBER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showUserDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const showUserDialog = ref(false)
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const users = ref([])
const userFormRef = ref()

const userForm = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  department: '',
  systemRole: 'MEMBER'
})

const userRules = {
  username: [{ required: true, message: 'Username is required' }],
  email: [
    { required: true, message: 'Email is required' },
    { type: 'email', message: 'Invalid email format' }
  ],
  password: [{ required: true, message: 'Password is required', trigger: 'blur' }],
  systemRole: [{ required: true, message: 'Role is required' }]
}

const departments = ['Engineering', 'Product', 'Design', 'QA', 'DevOps']

const getRoleTagType = (role) => {
  const types = {
    SUPER_ADMIN: 'danger',
    DEPT_ADMIN: 'warning',
    PROJECT_ADMIN: 'success',
    MEMBER: 'info'
  }
  return types[role] || 'info'
}

const fetchUsers = async () => {
  loading.value = true
  try {
    // Simulated data
    users.value = [
      { id: 1, username: 'admin', email: 'admin@example.com', department: 'Engineering', systemRole: 'SUPER_ADMIN', status: 'ACTIVE' },
      { id: 2, username: 'dept_lead', email: 'dept@example.com', department: 'Product', systemRole: 'DEPT_ADMIN', status: 'ACTIVE' },
      { id: 3, username: 'proj_lead', email: 'proj@example.com', department: 'Engineering', systemRole: 'PROJECT_ADMIN', status: 'ACTIVE' },
      { id: 4, username: 'dev1', email: 'dev1@example.com', department: 'Engineering', systemRole: 'MEMBER', status: 'ACTIVE' }
    ]
    total.value = users.value.length
  } catch (e) {
    // Handle error
  } finally {
    loading.value = false
  }
}

const handleEdit = (user) => {
  isEdit.value = true
  Object.assign(userForm, { ...user, password: '' })
  showUserDialog.value = true
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(`Delete user ${user.username}?`, 'Warning', { type: 'warning' })
    ElMessage.success('User deleted')
    fetchUsers()
  } catch (e) {}
}

const handleSubmit = async () => {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return
  ElMessage.success(isEdit.value ? 'User updated' : 'User created')
  showUserDialog.value = false
  fetchUsers()
}

const handleSizeChange = () => {
  fetchUsers()
}

const handlePageChange = () => {
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.users-page {
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
</style>
