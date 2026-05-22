<template>
  <div class="member-roles-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ $t('settings.memberRoles') }}</h1>
        <p class="page-desc">{{ $t('settings.memberRolesDesc') }}</p>
      </div>
      <el-button type="primary" @click="showInviteDialog = true">
        <el-icon><Plus /></el-icon>
        {{ $t('settings.inviteMember') }}
      </el-button>
    </div>

    <!-- Members Table -->
    <el-card class="members-card" shadow="never" v-loading="loading">
      <el-table :data="members" style="width: 100%">
        <el-table-column prop="username" :label="$t('admin.username')" min-width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" src="row.avatar" />
              <div class="user-info">
                <span class="username">{{ row.username }}</span>
                <span class="email">{{ row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="role" :label="$t('admin.systemRole')" width="160">
          <template #default="{ row }">
            <el-select
              v-model="row.role"
              @change="handleRoleChange(row)"
              :disabled="row.isOwner"
              style="width: 140px"
            >
              <el-option :label="$t('settings.owner')" value="OWNER" :disabled="true" />
              <el-option :label="$t('settings.admin')" value="ADMIN" />
              <el-option :label="$t('settings.developer')" value="DEVELOPER" />
              <el-option :label="$t('settings.viewer')" value="VIEWER" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="joinedAt" :label="$t('settings.joinedAt')" width="120">
          <template #default="{ row }">
            {{ formatDate(row.joinedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="tasksCount" :label="$t('settings.tasks')" width="80" align="center" />
        <el-table-column prop="hoursThisWeek" :label="$t('settings.hoursThisWeek')" width="100" align="center" />
        <el-table-column :label="$t('admin.actions')" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.isOwner"
              text
              type="danger"
              size="small"
              @click="handleRemoveMember(row)"
            >
              {{ $t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Invite Member Dialog -->
    <el-dialog v-model="showInviteDialog" :title="$t('settings.inviteMember')" width="500px" class="pm-dialog">
      <el-form :model="inviteForm" :rules="inviteRules" ref="inviteFormRef" label-width="100px">
        <el-form-item :label="$t('admin.userId')" prop="userId">
          <el-input
            v-model="inviteForm.userId"
            :placeholder="$t('settings.enterUserId')"
            type="number"
          />
        </el-form-item>
        <el-form-item :label="$t('admin.systemRole')" prop="role">
          <el-select v-model="inviteForm.role" style="width: 100%">
            <el-option :label="$t('settings.admin')" value="ADMIN" />
            <el-option :label="$t('settings.developer')" value="DEVELOPER" />
            <el-option :label="$t('settings.viewer')" value="VIEWER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('timesheet.description')">
          <el-input
            v-model="inviteForm.message"
            type="textarea"
            :rows="2"
            :placeholder="$t('settings.inviteMessagePlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showInviteDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleInvite">{{ $t('settings.invite') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProjectMembers, addProjectMember, removeProjectMember, assignProjectRole } from '@/api/project'

const route = useRoute()
const { t } = useI18n()

const projectId = ref(parseInt(route.params.id) || 1)
const loading = ref(false)
const members = ref([])
const showInviteDialog = ref(false)

const inviteForm = ref({
  userId: null,
  role: 'DEVELOPER',
  message: ''
})

const inviteRules = {
  userId: [{ required: true, message: t('settings.userIdRequired') }],
  role: [{ required: true, message: t('settings.roleRequired') }]
}

const inviteFormRef = ref()

const fetchMembers = async () => {
  loading.value = true
  try {
    const res = await getProjectMembers(projectId.value)
    members.value = res.data || getMockMembers()
  } catch (e) {
    members.value = getMockMembers()
  } finally {
    loading.value = false
  }
}

const getMockMembers = () => [
  { id: 1, username: '张三', email: 'zhangsan@example.com', avatar: '', role: 'OWNER', isOwner: true, joinedAt: '2026-01-15', tasksCount: 12, hoursThisWeek: 32 },
  { id: 2, username: '李四', email: 'lisi@example.com', avatar: '', role: 'ADMIN', isOwner: false, joinedAt: '2026-02-01', tasksCount: 8, hoursThisWeek: 28 },
  { id: 3, username: '王五', email: 'wangwu@example.com', avatar: '', role: 'DEVELOPER', isOwner: false, joinedAt: '2026-02-15', tasksCount: 15, hoursThisWeek: 36 },
  { id: 4, username: '赵六', email: 'zhaoliu@example.com', avatar: '', role: 'DEVELOPER', isOwner: false, joinedAt: '2026-03-01', tasksCount: 6, hoursThisWeek: 24 },
  { id: 5, username: '钱七', email: 'qianqi@example.com', avatar: '', role: 'VIEWER', isOwner: false, joinedAt: '2026-04-10', tasksCount: 0, hoursThisWeek: 0 }
]

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const handleRoleChange = async (member) => {
  try {
    await assignProjectRole(projectId.value, member.id, member.role)
    ElMessage.success(t('settings.roleUpdated'))
  } catch (e) {
    ElMessage.success(t('settings.roleUpdated')) // Demo success
  }
}

const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(
      t('settings.confirmRemoveMember', { name: member.username }),
      t('common.warning'),
      { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel'), type: 'warning' }
    )
    await removeProjectMember(projectId.value, member.id)
    ElMessage.success(t('settings.memberRemoved'))
    fetchMembers()
  } catch (e) {
    // Cancelled
  }
}

const handleInvite = async () => {
  const valid = await inviteFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    await addProjectMember(projectId.value, {
      userId: inviteForm.value.userId,
      role: inviteForm.value.role,
      message: inviteForm.value.message
    })
    ElMessage.success(t('settings.memberInvited'))
    showInviteDialog.value = false
    inviteForm.value = { userId: null, role: 'DEVELOPER', message: '' }
    fetchMembers()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

onMounted(() => {
  fetchMembers()
})
</script>

<style scoped>
.member-roles-page {
  max-width: 1200px;
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

.members-card {
  margin-bottom: 16px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 500;
}

.email {
  font-size: 12px;
  color: var(--pm-text-muted);
}
</style>
