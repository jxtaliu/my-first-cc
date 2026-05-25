<template>
  <div class="notification-center-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ $t('notification.title') }}</h1>
        <p class="page-desc">{{ $t('notification.description') }}</p>
      </div>
      <div class="header-actions">
        <el-button @click="handleMarkAllRead" :disabled="!hasUnread">
          <el-icon><Check /></el-icon>
          {{ $t('notification.markAllRead') }}
        </el-button>
      </div>
    </div>

    <!-- Filter Section -->
    <el-card class="filter-card" shadow="never">
      <div class="filters">
        <el-radio-group v-model="filterType" @change="handleFilterChange">
          <el-radio-button value="all">{{ $t('notification.all') }}</el-radio-button>
          <el-radio-button value="TASK">{{ $t('notification.task') }}</el-radio-button>
          <el-radio-button value="TIMESHEET">{{ $t('notification.timesheet') }}</el-radio-button>
          <el-radio-button value="PROJECT">{{ $t('notification.project') }}</el-radio-button>
          <el-radio-button value="SYSTEM">{{ $t('notification.system') }}</el-radio-button>
        </el-radio-group>
        <el-select v-model="filterRead" :placeholder="$t('notification.readStatus')" clearable style="width: 150px">
          <el-option :label="$t('notification.unread')" value="false" />
          <el-option :label="$t('notification.read')" value="true" />
        </el-select>
      </div>
    </el-card>

    <!-- Notifications List -->
    <el-card class="notifications-card" shadow="never" v-loading="loading">
      <div v-if="notifications.length === 0" class="empty-state">
        <el-empty :description="$t('notification.noNotifications')" />
      </div>
      <div v-else class="notification-list">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: !notification.isRead }"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-icon" :class="notification.type.toLowerCase()">
            <el-icon v-if="notification.type === 'TASK'"><Tickets /></el-icon>
            <el-icon v-else-if="notification.type === 'TIMESHEET'"><Clock /></el-icon>
            <el-icon v-else-if="notification.type === 'PROJECT'"><Folder /></el-icon>
            <el-icon v-else><Bell /></el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-message">{{ notification.message }}</div>
            <div class="notification-meta">
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
              <el-tag v-if="notification.type" :type="getTypeTagType(notification.type)" size="small">
                {{ getTypeLabel(notification.type) }}
              </el-tag>
            </div>
          </div>
          <div class="notification-actions">
            <el-button
              v-if="!notification.isRead"
              text
              size="small"
              @click.stop="handleMarkAsRead(notification)"
            >
              {{ $t('notification.markAsRead') }}
            </el-button>
            <el-button
              text
              type="danger"
              size="small"
              @click.stop="handleDelete(notification)"
            >
              {{ $t('common.delete') }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Notification Detail Dialog -->
    <el-dialog v-model="showDetailDialog" :title="$t('notification.notificationDetail')" width="600px" class="pm-dialog">
      <div class="detail-content" v-if="selectedNotification">
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="$t('notification.title')">
            {{ selectedNotification.title }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('notification.type')">
            <el-tag :type="getTypeTagType(selectedNotification.type)" size="small">
              {{ getTypeLabel(selectedNotification.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('notification.time')">
            {{ formatDateTime(selectedNotification.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('notification.status')">
            <el-tag :type="selectedNotification.isRead ? 'success' : 'warning'" size="small">
              {{ selectedNotification.isRead ? $t('notification.read') : $t('notification.unread') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('notification.message')">
            {{ selectedNotification.message }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedNotification.link" :label="$t('notification.relatedLink')">
            <el-link :href="selectedNotification.link" type="primary" :underline="false">
              {{ $t('notification.viewRelated') }}
              <el-icon><Link /></el-icon>
            </el-link>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">{{ $t('common.close') }}</el-button>
        <el-button v-if="selectedNotification && !selectedNotification.isRead" type="primary" @click="handleMarkAsReadFromDialog">
          {{ $t('notification.markAsRead') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Bell, Tickets, Clock, Folder, Link } from '@element-plus/icons-vue'
import { getNotifications, markAsRead, markAllAsRead, deleteNotification } from '@/api/notification'

const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const notifications = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filterType = ref('all')
const filterRead = ref(null)
const showDetailDialog = ref(false)
const selectedNotification = ref(null)

const hasUnread = computed(() => {
  return notifications.value.some(n => !n.isRead)
})

const fetchNotifications = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterType.value !== 'all') {
      params.type = filterType.value
    }
    if (filterRead.value !== null && filterRead.value !== '') {
      params.isRead = filterRead.value === 'true'
    }

    // For demo, using mock data - in real implementation:
    // const res = await getNotifications(params)
    notifications.value = getMockNotifications()
    total.value = notifications.value.length
  } catch (e) {
    notifications.value = getMockNotifications()
    total.value = notifications.value.length
  } finally {
    loading.value = false
  }
}

const getMockNotifications = () => [
  {
    id: 1,
    title: '新任务分配',
    message: '您被分配到任务"完成登录功能开发"，请及时处理。',
    type: 'TASK',
    isRead: false,
    createdAt: new Date(Date.now() - 30 * 60 * 1000),
    link: '/projects/1/task/101',
    taskId: 101,
    projectId: 1
  },
  {
    id: 2,
    title: '工时待审批',
    message: '张三提交了8小时工时，等待您的审批。',
    type: 'TIMESHEET',
    isRead: false,
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000),
    link: '/timesheet/approval',
    timesheetId: 1,
    projectId: 1
  },
  {
    id: 3,
    title: '任务状态变更',
    message: '任务"项目管理页面"已移动到In Review状态。',
    type: 'TASK',
    isRead: true,
    createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000),
    link: '/projects/1/task/102',
    taskId: 102,
    projectId: 1
  },
  {
    id: 4,
    title: '项目里程碑更新',
    message: '项目里程碑"发布 V2.0"已完成60%。',
    type: 'PROJECT',
    isRead: true,
    createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000),
    link: '/projects/milestones',
    projectId: 1
  },
  {
    id: 5,
    title: '冲刺开始提醒',
    message: 'Sprint 16已正式开始，请团队成员更新任务状态。',
    type: 'PROJECT',
    isRead: false,
    createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
    link: '/projects/sprint-board/1',
    sprintId: 16,
    projectId: 1
  },
  {
    id: 6,
    title: '系统更新通知',
    message: 'SME PM系统将于本周日凌晨2:00进行版本更新。',
    type: 'SYSTEM',
    isRead: true,
    createdAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    link: null
  }
]

const getTypeTagType = (type) => {
  const types = {
    'TASK': 'primary',
    'TIMESHEET': 'success',
    'PROJECT': 'warning',
    'SYSTEM': 'info'
  }
  return types[type] || 'info'
}

const getTypeLabel = (type) => {
  const labels = {
    'TASK': t('notification.task'),
    'TIMESHEET': t('notification.timesheet'),
    'PROJECT': t('notification.project'),
    'SYSTEM': t('notification.system')
  }
  return labels[type] || type
}

const formatTime = (date) => {
  if (!date) return ''
  const now = new Date()
  const diff = now - new Date(date)
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return t('notification.justNow')
  if (minutes < 60) return t('notification.minutesAgo', { minutes })
  if (hours < 24) return t('notification.hoursAgo', { hours })
  if (days < 7) return t('notification.daysAgo', { days })
  return new Date(date).toLocaleDateString()
}

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchNotifications()
}

const handleNotificationClick = (notification) => {
  selectedNotification.value = notification
  showDetailDialog.value = true
  if (!notification.isRead) {
    handleMarkAsRead(notification)
  }
}

const handleMarkAsRead = async (notification) => {
  try {
    await markAsRead(notification.id)
    notification.isRead = true
  } catch (e) {
    notification.isRead = true // Demo update
  }
}

const handleMarkAsReadFromDialog = async () => {
  if (selectedNotification.value) {
    await handleMarkAsRead(selectedNotification.value)
  }
}

const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm(t('notification.confirmMarkAllRead'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await markAllAsRead()
    ElMessage.success(t('notification.markAllReadSuccess'))
    fetchNotifications()
  } catch (e) {
    // Cancelled
  }
}

const handleDelete = async (notification) => {
  try {
    await ElMessageBox.confirm(t('notification.confirmDelete'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    await deleteNotification(notification.id)
    ElMessage.success(t('notification.deletedSuccess'))
    fetchNotifications()
  } catch (e) {
    // Cancelled
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchNotifications()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchNotifications()
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.notification-center-page {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 16px;
}

.filters {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.notifications-card {
  margin-bottom: 16px;
}

.empty-state {
  padding: 40px 0;
}

.notification-list {
  display: flex;
  flex-direction: column;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid var(--pm-border);
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: var(--pm-background);
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item.unread {
  background-color: rgba(64, 158, 255, 0.05);
}

.notification-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 18px;
}

.notification-icon.task { background: rgba(64, 158, 255, 0.1); color: #409eff; }
.notification-icon.timesheet { background: rgba(103, 194, 58, 0.1); color: #67c23a; }
.notification-icon.project { background: rgba(230, 162, 60, 0.1); color: #e6a23c; }
.notification-icon.system { background: rgba(144, 147, 153, 0.1); color: #909399; }

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--pm-text-primary);
}

.notification-message {
  font-size: 13px;
  color: var(--pm-text-secondary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-time {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.notification-actions {
  flex-shrink: 0;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.notification-item:hover .notification-actions {
  opacity: 1;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  padding: 8px 0;
}
</style>
