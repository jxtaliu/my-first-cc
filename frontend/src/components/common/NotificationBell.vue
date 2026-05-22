<template>
  <div class="notification-bell" v-if="authStore.user">
    <el-badge
      :value="unreadCount"
      :hidden="unreadCount === 0"
      :max="99"
      class="notification-badge"
    >
      <el-dropdown
        ref="dropdownRef"
        trigger="click"
        @command="handleCommand"
        @visible-change="handleVisibleChange"
      >
        <el-button class="bell-button">
          <el-icon :size="20"><Bell /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu class="notification-dropdown">
            <div class="notification-header">
              <span class="notification-title">{{ $t('notification.title') }}</span>
              <el-button
                v-if="unreadCount > 0"
                type="primary"
                link
                size="small"
                @click.stop="handleMarkAllRead"
              >
                {{ $t('notification.markAllRead') }}
              </el-button>
            </div>

            <div class="notification-list" v-if="notifications.length > 0">
              <el-scrollbar height="400px">
                <div
                  v-for="notification in notifications"
                  :key="notification.id"
                  class="notification-item"
                  :class="{ unread: !notification.isRead }"
                  @click="handleNotificationClick(notification)"
                >
                  <div class="notification-icon" :style="{ background: getNotificationColor(notification.type) }">
                    <el-icon>{{ getNotificationIcon(notification.type) }}</el-icon>
                  </div>
                  <div class="notification-content">
                    <div class="notification-item-title">{{ notification.title }}</div>
                    <div class="notification-item-content">{{ truncateContent(notification.content) }}</div>
                    <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
                  </div>
                  <div class="notification-actions" v-if="!notification.isRead">
                    <el-button
                      type="primary"
                      link
                      size="small"
                      @click.stop="handleMarkAsRead(notification.id)"
                    >
                      {{ $t('notification.markAsRead') }}
                    </el-button>
                  </div>
                </div>
              </el-scrollbar>
            </div>
            <div class="notification-empty" v-else>
              <el-icon :size="40"><Bell /></el-icon>
              <span>{{ $t('notification.noNotifications') }}</span>
            </div>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-badge>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  getUnreadNotifications,
  markAsRead,
  markAllAsRead,
  getUnreadCount
} from '@/api/notification'
import {
  Bell,
  Message,
  User,
  Check,
  Warning,
  Clock,
  Flag,
  Trophy,
  Promotion,
  Timer,
  CircleCheck
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const dropdownRef = ref(null)
const notifications = ref([])
const unreadCount = ref(0)
let refreshInterval = null

const notificationIcons = {
  TASK_ASSIGNED: User,
  TASK_STATUS_CHANGED: Check,
  TASK_COMMENT: Message,
  TASK_MENTION: User,
  MILESTONE_DUE: Clock,
  MILESTONE_ACHIEVED: Trophy,
  SPRINT_START: Promotion,
  SPRINT_END: Timer,
  SPRINT_COMPLETED: CircleCheck,
  TASK_OVERDUE: Warning,
  TASK_DEPENDENCY_BLOCKED: Flag
}

const notificationColors = {
  TASK_ASSIGNED: '#3B82F6',
  TASK_STATUS_CHANGED: '#10B981',
  TASK_COMMENT: '#8B5CF6',
  TASK_MENTION: '#EC4899',
  MILESTONE_DUE: '#F59E0B',
  MILESTONE_ACHIEVED: '#10B981',
  SPRINT_START: '#3B82F6',
  SPRINT_END: '#F59E0B',
  SPRINT_COMPLETED: '#10B981',
  TASK_OVERDUE: '#EF4444',
  TASK_DEPENDENCY_BLOCKED: '#F97316'
}

const getNotificationIcon = (type) => {
  return notificationIcons[type] || Bell
}

const getNotificationColor = (type) => {
  return notificationColors[type] || '#94A3B8'
}

const truncateContent = (content) => {
  if (!content) return ''
  return content.length > 60 ? content.substring(0, 60) + '...' : content
}

const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`
  if (hours < 24) return `${hours}h ago`
  if (days < 7) return `${days}d ago`
  return date.toLocaleDateString()
}

const fetchNotifications = async () => {
  if (!authStore.user?.id) return

  try {
    const [notificationsRes, countRes] = await Promise.all([
      getUnreadNotifications(authStore.user.id),
      getUnreadCount(authStore.user.id)
    ])

    notifications.value = notificationsRes.data || []
    unreadCount.value = countRes.data || 0
  } catch (error) {
    console.error('Failed to fetch notifications:', error)
  }
}

const handleNotificationClick = (notification) => {
  if (!notification.isRead) {
    handleMarkAsRead(notification.id)
  }

  if (notification.relatedTaskId) {
    router.push(`/projects/${notification.relatedProjectId}/tasks/${notification.relatedTaskId}`)
  } else if (notification.relatedProjectId) {
    router.push(`/projects/${notification.relatedProjectId}`)
  }

  dropdownRef.value?.handleClose()
}

const handleMarkAsRead = async (id) => {
  try {
    await markAsRead(id)
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.isRead = true
    }
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch (error) {
    console.error('Failed to mark notification as read:', error)
  }
}

const handleMarkAllRead = async () => {
  if (!authStore.user?.id) return

  try {
    await markAllAsRead(authStore.user.id)
    notifications.value.forEach(n => {
      n.isRead = true
    })
    unreadCount.value = 0
    ElMessage.success('All notifications marked as read')
  } catch (error) {
    console.error('Failed to mark all notifications as read:', error)
  }
}

const handleVisibleChange = (visible) => {
  if (visible) {
    fetchNotifications()
  }
}

const handleCommand = (command) => {
  if (command === 'markAllRead') {
    handleMarkAllRead()
  }
}

onMounted(() => {
  fetchNotifications()
  refreshInterval = setInterval(fetchNotifications, 60000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.notification-bell {
  position: relative;
}

.notification-badge {
  --el-badge-bg-color: #EF4444;
}

.bell-button {
  background: transparent;
  border: none;
  padding: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--theme-text);
  transition: color 0.2s;
}

.bell-button:hover {
  color: var(--theme-accent);
}

.notification-dropdown {
  width: 380px;
  padding: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--theme-border);
  background: var(--theme-card-bg);
}

.notification-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--theme-text);
}

.notification-list {
  max-height: 400px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--theme-border);
}

.notification-item:hover {
  background: var(--theme-hover);
}

.notification-item.unread {
  background: rgba(59, 130, 246, 0.05);
}

.notification-item.unread:hover {
  background: rgba(59, 130, 246, 0.1);
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-item-title {
  font-weight: 500;
  font-size: 13px;
  color: var(--theme-text);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-item-content {
  font-size: 12px;
  color: var(--theme-text-secondary);
  margin-bottom: 4px;
  line-height: 1.4;
}

.notification-time {
  font-size: 11px;
  color: var(--theme-text-secondary);
}

.notification-actions {
  display: flex;
  align-items: center;
}

.notification-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--theme-text-secondary);
  gap: 12px;
}

.notification-empty span {
  font-size: 13px;
}
</style>
