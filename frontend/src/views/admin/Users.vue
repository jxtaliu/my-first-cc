<template>
  <div class="users-page">
    <div class="page-header">
      <h2>{{ $t('admin.title') }}</h2>
      <el-button type="primary" @click="showUserDialog = true">
        <el-icon><Plus /></el-icon> {{ $t('admin.addUser') }}
      </el-button>
    </div>

    <el-card>
      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="userId" :label="$t('admin.userId')" width="100" />
        <el-table-column prop="username" :label="$t('admin.username')" width="150" />
        <el-table-column prop="email" :label="$t('admin.email')" width="200" />
        <el-table-column prop="department" :label="$t('admin.department')" width="150" />
        <el-table-column prop="role" :label="$t('admin.systemRole')" width="150">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.systemRole)" size="small">
              {{ row.systemRole }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('admin.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.actions')" width="200">
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

    <el-dialog v-model="showUserDialog" :title="isEdit ? $t('admin.editUser') : $t('admin.addUser')" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item :label="$t('admin.userId')" prop="userId">
          <el-input v-model="userForm.userId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('admin.username')" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('admin.email')" prop="email">
          <el-input v-model="userForm.email" type="email" />
        </el-form-item>
        <el-form-item :label="$t('admin.password')" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item :label="$t('admin.department')" prop="department">
          <el-select v-model="userForm.department" style="width: 100%">
            <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('admin.systemRole')" prop="systemRole">
          <el-select v-model="userForm.systemRole" style="width: 100%">
            <el-option :label="$t('admin.superAdmin')" value="SUPER_ADMIN" />
            <el-option :label="$t('admin.deptAdmin')" value="DEPT_ADMIN" />
            <el-option :label="$t('admin.projectAdmin')" value="PROJECT_ADMIN" />
            <el-option :label="$t('admin.member')" value="MEMBER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('admin.roles')">
          <el-select v-model="userForm.roleIds" multiple style="width: 100%">
            <el-option v-for="r in allRoles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('admin.department')">
          <el-tree-select v-model="userForm.departmentId" :data="departmentTree" :props="{ label: 'name', value: 'id' }" clearable />
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
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const { t } = useI18n()
const showUserDialog = ref(false)
const isEdit = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const users = ref([])
const userFormRef = ref()

const userForm = reactive({
  id: null,
  userId: '',
  username: '',
  email: '',
  password: '',
  department: '',
  departmentId: null,
  systemRole: 'MEMBER',
  roleIds: []
})

const userRules = {
  username: [{ required: true, message: () => t('admin.usernameRequired') }],
  email: [
    { required: true, message: () => t('admin.emailRequired') },
    { type: 'email', message: () => t('admin.invalidEmail') }
  ],
  password: [{ required: true, message: () => t('admin.passwordRequired'), trigger: 'blur' }],
  systemRole: [{ required: true, message: () => t('admin.systemRole') }]
}

const departments = ['Engineering', 'Product', 'Design', 'QA', 'DevOps']

const allRoles = ref([
  { id: 1, name: 'Super Admin' },
  { id: 2, name: 'Department Admin' },
  { id: 3, name: 'Project Admin' },
  { id: 4, name: 'Member' }
])

const departmentTree = ref([
  { id: 1, name: 'Engineering', children: [
    { id: 11, name: 'Backend' },
    { id: 12, name: 'Frontend' }
  ] },
  { id: 2, name: 'Product' },
  { id: 3, name: 'Design' },
  { id: 4, name: 'QA' },
  { id: 5, name: 'DevOps' }
])

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
    await ElMessageBox.confirm(t('admin.confirmDelete'), t('common.warning'), { type: 'warning' })
    const res = await fetch(`/api/users/${user.id}`, { method: 'DELETE' })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success(t('admin.userDeleted'))
      fetchUsers()
    } else {
      ElMessage.error(result.message || t('common.failed'))
    }
  } catch (e) {}
}

const handleSubmit = async () => {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return
  const submitData = {
    userId: userForm.userId,
    username: userForm.username,
    email: userForm.email,
    department: userForm.department,
    departmentId: userForm.departmentId,
    systemRole: userForm.systemRole,
    roleIds: userForm.roleIds
  }
  if (!isEdit.value) {
    submitData.password = userForm.password
  }
  // API call would be made here with submitData
  console.log('Submitting user data:', submitData)
  ElMessage.success(isEdit.value ? t('admin.userUpdated') : t('admin.userCreated'))
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
