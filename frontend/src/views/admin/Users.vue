<template>
  <div class="users-page">
    <div class="page-header">
      <h2>{{ $t('admin.title') }}</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> {{ $t('admin.addUser') }}
      </el-button>
    </div>

    <el-card>
      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="userId" :label="$t('admin.userId')" width="100">
          <template #default="{ row }">
            <el-link type="primary" @click="openDetailDialog(row)">{{ row.userId }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="username" :label="$t('admin.username')" width="140">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="avatar-small">{{ (row.username || '?').charAt(0).toUpperCase() }}</span>
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" :label="$t('admin.email')" min-width="180" />
        <el-table-column prop="realName" :label="$t('admin.realName')" width="120" />
        <el-table-column prop="departmentName" :label="$t('admin.department')" width="120" />
        <el-table-column prop="status" :label="$t('admin.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.actions')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openDialog(row)">{{ $t('common.edit') }}</el-button>
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

    <el-dialog v-model="showDialog" :title="isEdit ? $t('admin.editUser') : $t('admin.addUser')" width="500px">
      <el-form :model="userForm" :rules="userRules" ref="formRef" label-width="100px">
        <el-form-item :label="$t('admin.userId')" prop="userId">
          <el-input v-model="userForm.userId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('admin.username')" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('admin.email')" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item :label="$t('admin.realName')">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        <el-form-item :label="$t('admin.password')" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item :label="$t('admin.department')">
          <el-select v-model="userForm.departmentId" :placeholder="$t('admin.selectDepartment')" style="width: 100%" clearable>
            <el-option v-for="dept in departmentList" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('admin.roles')">
          <el-select v-model="userForm.roleIds" multiple style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" :title="$t('admin.userDetail')" width="500px">
      <div class="user-detail">
        <div class="user-header">
          <div class="avatar">
            {{ (detailUser.username || detailUser.userId || '?').charAt(0).toUpperCase() }}
          </div>
          <div class="user-title">
            <h3>{{ detailUser.realName || detailUser.username || '-' }}</h3>
            <p>{{ detailUser.userId || '-' }}</p>
          </div>
          <div class="status-badge">
            <el-tag :type="detailUser.status === 1 ? 'success' : 'info'" size="large">
              {{ detailUser.status === 1 ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </div>
        </div>
        <el-divider />
        <div class="user-info-grid">
          <div class="info-item">
            <label>{{ $t('admin.username') }}</label>
            <span>{{ detailUser.username || '-' }}</span>
          </div>
          <div class="info-item">
            <label>{{ $t('admin.email') }}</label>
            <span>{{ detailUser.email || '-' }}</span>
          </div>
          <div class="info-item">
            <label>{{ $t('admin.department') }}</label>
            <span>{{ detailUser.departmentName || '-' }}</span>
          </div>
          <div class="info-item">
            <label>{{ $t('admin.roles') }}</label>
            <div class="role-tags">
              <el-tag v-if="detailUser.roleNames && detailUser.roleNames !== '-'" type="info" v-for="role in detailUser.roleNames.split(', ')" :key="role" size="small">
                {{ role }}
              </el-tag>
              <span v-else>-</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showDetailDialog = false">{{ $t('common.ok') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const { t } = useI18n()

const loading = ref(false)
const showDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const detailUser = ref({})
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const users = ref([])
const departmentList = ref([])
const roleList = ref([])
const formRef = ref()

const userForm = reactive({
  id: null,
  userId: '',
  username: '',
  email: '',
  realName: '',
  password: '',
  departmentId: null,
  roleIds: []
})

const userRules = {
  userId: [{ required: true, message: () => t('admin.userIdRequired') }],
  username: [{ required: true, message: () => t('admin.usernameRequired') }],
  email: [
    { required: true, message: () => t('admin.emailRequired') },
    { type: 'email', message: () => t('admin.invalidEmail') }
  ],
  password: [{ required: true, message: () => t('admin.passwordRequired'), trigger: 'blur' }]
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/users')
    const result = await res.json()
    if (result.code === 200) {
      // Fetch department names for each user
      const usersWithDepts = await Promise.all(result.data.map(async (user) => {
        if (user.departmentId) {
          const deptRes = await fetch(`/api/departments/${user.departmentId}`)
          const deptResult = await deptRes.json()
          user.departmentName = deptResult.data?.name || '-'
        } else {
          user.departmentName = '-'
        }
        return user
      }))
      users.value = usersWithDepts
      total.value = users.value.length
    }
  } catch (e) {
    // Handle error
  } finally {
    loading.value = false
  }
}

const fetchDepartments = async () => {
  try {
    const res = await fetch('/api/departments')
    const result = await res.json()
    if (result.code === 200) {
      departmentList.value = result.data
    }
  } catch (e) {
    // Handle error
  }
}

const fetchRoles = async () => {
  try {
    const res = await fetch('/api/roles')
    const result = await res.json()
    if (result.code === 200) {
      roleList.value = result.data
    }
  } catch (e) {
    // Handle error
  }
}

const openDialog = async (user = null) => {
  if (user) {
    isEdit.value = true
    // Fetch user roles
    let roleIds = []
    try {
      const res = await fetch(`/api/users/${user.id}/roles`)
      const result = await res.json()
      if (result.code === 200) {
        roleIds = result.data || []
      }
    } catch (e) {
      // Ignore error
    }
    Object.assign(userForm, {
      id: user.id,
      userId: user.userId,
      username: user.username,
      email: user.email,
      realName: user.realName || '',
      password: '',
      departmentId: user.departmentId,
      roleIds: roleIds
    })
  } else {
    isEdit.value = false
    Object.assign(userForm, {
      id: null,
      userId: '',
      username: '',
      email: '',
      realName: '',
      password: '',
      departmentId: null,
      roleIds: []
    })
  }
  showDialog.value = true
}

const openDetailDialog = async (user) => {
  // Fetch user roles
  let roleNames = '-'
  try {
    const res = await fetch(`/api/users/${user.id}/roles`)
    const result = await res.json()
    if (result.code === 200 && result.data && result.data.length > 0) {
      const roles = roleList.value.filter(r => result.data.includes(r.id))
      roleNames = roles.map(r => r.name).join(', ')
    }
  } catch (e) {
    // Ignore
  }
  detailUser.value = {
    ...user,
    departmentName: user.departmentName || '-',
    roleNames: roleNames
  }
  showDetailDialog.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      // Update user
      const res = await fetch(`/api/users/${userForm.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: userForm.userId,
          username: userForm.username,
          email: userForm.email,
          realName: userForm.realName,
          status: 1
        })
      })
      const result = await res.json()
      if (result.code === 200) {
        // Update department if changed
        if (userForm.departmentId) {
          await fetch(`/api/users/${userForm.id}/department`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ departmentId: userForm.departmentId })
          })
        }
        // Assign roles
        if (userForm.roleIds && userForm.roleIds.length > 0) {
          await fetch(`/api/users/${userForm.id}/roles`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ roleIds: userForm.roleIds })
          })
        }
        ElMessage.success(t('admin.userUpdated'))
      } else {
        ElMessage.error(result.message || t('common.failed'))
        return
      }
    } else {
      // Create user
      const res = await fetch('/api/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: userForm.userId,
          username: userForm.username,
          email: userForm.email,
          realName: userForm.realName,
          password: userForm.password,
          status: 1
        })
      })
      const result = await res.json()
      if (result.code === 200) {
        const newUserId = result.data?.id
        // Assign department if set
        if (userForm.departmentId && newUserId) {
          await fetch(`/api/users/${newUserId}/department`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ departmentId: userForm.departmentId })
          })
        }
        // Assign roles if set
        if (userForm.roleIds && userForm.roleIds.length > 0 && newUserId) {
          await fetch(`/api/users/${newUserId}/roles`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ roleIds: userForm.roleIds })
          })
        }
        ElMessage.success(t('admin.userCreated'))
      } else {
        ElMessage.error(result.message || t('common.failed'))
        return
      }
    }
    showDialog.value = false
    fetchUsers()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
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

const handleSizeChange = () => {
  fetchUsers()
}

const handlePageChange = () => {
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
  fetchDepartments()
  fetchRoles()
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

.user-cell {
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

.user-detail {
  padding: 10px 0;
}

.user-header {
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

.user-title {
  flex: 1;
}

.user-title h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}

.user-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.status-badge {
  margin-left: auto;
}

.user-info-grid {
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

.info-item label {
  color: #909399;
  font-size: 12px;
}

.info-item span {
  color: #303133;
  font-size: 14px;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
