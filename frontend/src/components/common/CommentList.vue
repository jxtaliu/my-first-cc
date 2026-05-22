<template>
  <div class="comment-list">
    <!-- Comment Form -->
    <div class="comment-form" v-if="showForm">
      <div class="comment-form-header">
        <el-avatar :size="36" :src="currentUser?.avatar">
          {{ currentUser?.name?.charAt(0) || 'U' }}
        </el-avatar>
        <span class="comment-form-user">{{ currentUser?.name || 'User' }}</span>
      </div>
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        :placeholder="$t('comments.addPlaceholder')"
        @keydown.enter.ctrl="handleSubmitComment"
      />
      <div class="comment-form-actions">
        <span class="comment-form-hint">{{ $t('comments.ctrlEnterHint') }}</span>
        <div class="comment-form-buttons">
          <el-button @click="cancelComment">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="handleSubmitComment" :loading="submitting">
            {{ $t('comments.submit') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Comments Header -->
    <div class="comments-header">
      <h3 class="comments-title">
        {{ $t('comments.title') }}
        <span class="comments-count">({{ comments.length }})</span>
      </h3>
      <el-button v-if="!showForm" type="primary" text @click="startNewComment">
        <el-icon><Plus /></el-icon>
        {{ $t('comments.add') }}
      </el-button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="comments-loading">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- Empty State -->
    <el-empty v-else-if="!comments.length" :description="$t('comments.noComments')" />

    <!-- Comments List -->
    <div v-else class="comments-container">
      <div
        v-for="comment in flatComments"
        :key="comment.id"
        class="comment-item"
        :class="{ 'comment-reply': comment.parentId, 'highlighted': comment.highlighted }"
        :style="{ marginLeft: comment.parentId ? `${Math.min(comment.depth * 24, 96)}px` : '0' }"
      >
        <div class="comment-avatar">
          <el-avatar :size="comment.parentId ? 28 : 36" :src="comment.user?.avatar">
            {{ comment.user?.name?.charAt(0) || 'U' }}
          </el-avatar>
          <div v-if="comment.replies?.length" class="comment-thread-line"></div>
        </div>

        <div class="comment-content">
          <div class="comment-header">
            <span class="comment-author">{{ comment.user?.name || 'Unknown User' }}</span>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            <span v-if="comment.editedAt" class="comment-edited">({{ $t('comments.edited') }})</span>
          </div>

          <div class="comment-body" v-html="formatCommentContent(comment.content)"></div>

          <div class="comment-actions">
            <el-button
              v-if="!comment.parentId"
              type="primary"
              text
              size="small"
              @click="startReply(comment)"
            >
              <el-icon><ChatLineSquare /></el-icon>
              {{ $t('comments.reply') }}
            </el-button>
            <el-button
              v-if="canDelete(comment)"
              type="danger"
              text
              size="small"
              @click="handleDelete(comment)"
            >
              <el-icon><Delete /></el-icon>
              {{ $t('common.delete') }}
            </el-button>
          </div>

          <!-- Reply Form -->
          <div v-if="replyingTo === comment.id" class="reply-form">
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              :placeholder="$t('comments.replyPlaceholder')"
            />
            <div class="reply-form-actions">
              <el-button size="small" @click="cancelReply">{{ $t('common.cancel') }}</el-button>
              <el-button type="primary" size="small" @click="submitReply(comment)" :loading="submitting">
                {{ $t('comments.reply') }}
              </el-button>
            </div>
          </div>

          <!-- Replies -->
          <div v-if="comment.replies?.length" class="comment-replies">
            <div
              v-for="reply in comment.replies"
              :key="reply.id"
              class="comment-item comment-reply"
              :class="{ 'highlighted': reply.highlighted }"
            >
              <div class="comment-avatar">
                <el-avatar :size="28" :src="reply.user?.avatar">
                  {{ reply.user?.name?.charAt(0) || 'U' }}
                </el-avatar>
              </div>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ reply.user?.name || 'Unknown User' }}</span>
                  <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                </div>
                <div class="comment-body" v-html="formatCommentContent(reply.content)"></div>
                <div class="comment-actions">
                  <el-button
                    v-if="canDelete(reply)"
                    type="danger"
                    text
                    size="small"
                    @click="handleDelete(reply)"
                  >
                    <el-icon><Delete /></el-icon>
                    {{ $t('common.delete') }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Load More -->
    <div v-if="hasMore" class="comments-load-more">
      <el-button @click="loadMore" :loading="loadingMore">
        {{ $t('comments.loadMore') }}
      </el-button>
    </div>

    <!-- Image Preview Modal -->
    <el-dialog
      v-model="imagePreviewVisible"
      :title="previewImage?.name"
      width="800px"
      class="image-preview-dialog"
    >
      <div class="image-preview-container">
        <img :src="previewImage?.url" :alt="previewImage?.name" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ChatLineSquare, Delete } from '@element-plus/icons-vue'
import { getTaskComments, addTaskComment } from '@/api/task'

const props = defineProps({
  taskId: {
    type: [Number, String],
    required: true
  },
  showForm: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['comment-added', 'comment-deleted'])

const { t } = useI18n()

// State
const comments = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const submitting = ref(false)
const newComment = ref('')
const replyContent = ref('')
const replyingTo = ref(null)
const hasMore = ref(false)
const page = ref(1)
const pageSize = ref(20)
const currentUser = ref({ id: 1, name: 'Current User', avatar: '' })
const imagePreviewVisible = ref(false)
const previewImage = ref(null)

// Flatten comments for display with nested replies
const flatComments = computed(() => {
  return comments.value.map(comment => ({
    ...comment,
    depth: 0,
    highlighted: false,
    replies: comment.replies || []
  }))
})

// Format comment content with @mentions highlighting
const formatCommentContent = (content) => {
  if (!content) return ''

  // Highlight @mentions
  const mentionRegex = /@(\w+)/g
  let formatted = content.replace(mentionRegex, '<span class="comment-mention">@$1</span>')

  // Highlight code blocks
  formatted = formatted.replace(/`([^`]+)`/g, '<code class="comment-code">$1</code>')

  // Highlight URLs
  formatted = formatted.replace(
    /(https?:\/\/[^\s]+)/g,
    '<a href="$1" target="_blank" class="comment-link">$1</a>'
  )

  return formatted
}

// Format relative time
const formatTime = (dateString) => {
  if (!dateString) return ''

  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return t('notification.justNow')
  if (diffMins < 60) return t('notification.minutesAgo', { minutes: diffMins })
  if (diffHours < 24) return t('notification.hoursAgo', { hours: diffHours })
  if (diffDays < 7) return t('notification.daysAgo', { days: diffDays })

  return date.toLocaleDateString()
}

// Check if user can delete a comment
const canDelete = (comment) => {
  return comment.userId === currentUser.value?.id ||
         currentUser.value?.role === 'ADMIN'
}

// Load comments from API
const loadComments = async () => {
  loading.value = true
  try {
    const res = await getTaskComments(props.taskId)
    comments.value = res.data || res || []
    page.value = 1
    hasMore.value = false
  } catch (error) {
    console.error('Failed to load comments:', error)
    // Load mock data on error
    loadMockComments()
  } finally {
    loading.value = false
  }
}

// Load more comments
const loadMore = async () => {
  if (!hasMore.value || loadingMore.value) return

  loadingMore.value = true
  try {
    page.value++
    const res = await getTaskComments(props.taskId)
    const newComments = res.data || res || []
    comments.value = [...comments.value, ...newComments]
    hasMore.value = newComments.length >= pageSize.value
  } catch (error) {
    console.error('Failed to load more comments:', error)
    page.value--
  } finally {
    loadingMore.value = false
  }
}

// Mock comments for demo
const loadMockComments = () => {
  comments.value = [
    {
      id: 1,
      content: '正在开发这个功能，预计本周完成。@张三 请注意代码审查。',
      createdAt: new Date(Date.now() - 3600000).toISOString(),
      editedAt: null,
      userId: 1,
      user: { id: 1, name: 'Current User', avatar: '' },
      parentId: null,
      replies: [
        {
          id: 2,
          content: '好的，我会尽快审查。',
          createdAt: new Date(Date.now() - 1800000).toISOString(),
          userId: 2,
          user: { id: 2, name: '张三', avatar: '' }
        }
      ]
    },
    {
      id: 3,
      content: '发现了一个bug，需要修复后再继续。`console.error` 需要清理。',
      createdAt: new Date(Date.now() - 86400000).toISOString(),
      editedAt: null,
      userId: 3,
      user: { id: 3, name: '李四', avatar: '' },
      parentId: null,
      replies: []
    }
  ]
}

// Start new comment
const startNewComment = () => {
  newComment.value = ''
  replyingTo.value = null
}

// Cancel new comment
const cancelComment = () => {
  newComment.value = ''
  replyingTo.value = null
}

// Submit new comment
const handleSubmitComment = async () => {
  if (!newComment.value.trim()) return

  submitting.value = true
  try {
    const res = await addTaskComment(props.taskId, {
      content: newComment.value.trim()
    })

    const newCommentObj = {
      id: res.data?.id || Date.now(),
      content: newComment.value.trim(),
      createdAt: new Date().toISOString(),
      user: currentUser.value,
      userId: currentUser.value.id,
      parentId: null,
      replies: []
    }

    comments.value.unshift(newCommentObj)
    newComment.value = ''
    emit('comment-added', newCommentObj)
    ElMessage.success(t('comments.addSuccess'))
  } catch (error) {
    console.error('Failed to add comment:', error)
    ElMessage.error(t('comments.addFailed'))
  } finally {
    submitting.value = false
  }
}

// Start reply to comment
const startReply = (comment) => {
  replyingTo.value = comment.id
  replyContent.value = `@${comment.user?.name} `
}

// Cancel reply
const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

// Submit reply
const submitReply = async (parentComment) => {
  if (!replyContent.value.trim()) return

  submitting.value = true
  try {
    const res = await addTaskComment(props.taskId, {
      content: replyContent.value.trim(),
      parentId: parentComment.id
    })

    const replyObj = {
      id: res.data?.id || Date.now(),
      content: replyContent.value.trim(),
      createdAt: new Date().toISOString(),
      user: currentUser.value,
      userId: currentUser.value.id
    }

    if (!parentComment.replies) {
      parentComment.replies = []
    }
    parentComment.replies.push(replyObj)
    replyContent.value = ''
    replyingTo.value = null
    emit('comment-added', replyObj)
    ElMessage.success(t('comments.replySuccess'))
  } catch (error) {
    console.error('Failed to add reply:', error)
    ElMessage.error(t('comments.addFailed'))
  } finally {
    submitting.value = false
  }
}

// Delete comment
const handleDelete = async (comment) => {
  try {
    await ElMessageBox.confirm(
      t('comments.confirmDelete'),
      t('common.warning'),
      { type: 'warning' }
    )

    // API call would go here
    // await deleteComment(comment.id)

    // Remove from list
    if (comment.parentId) {
      const parent = comments.value.find(c => c.id === comment.parentId)
      if (parent) {
        parent.replies = parent.replies.filter(r => r.id !== comment.id)
      }
    } else {
      comments.value = comments.value.filter(c => c.id !== comment.id)
    }

    emit('comment-deleted', comment)
    ElMessage.success(t('comments.deleteSuccess'))
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete comment:', error)
    }
  }
}

// Watch for task changes
watch(() => props.taskId, () => {
  loadComments()
})

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
}

/* Comment Form */
.comment-form {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
}

.comment-form-header {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.comment-form-user {
  font-weight: 600;
  color: var(--pm-text-primary);
}

.comment-form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comment-form-hint {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.comment-form-buttons {
  display: flex;
  gap: var(--pm-space-sm);
}

/* Comments Header */
.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comments-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.comments-count {
  font-weight: 400;
  color: var(--pm-text-muted);
}

/* Loading State */
.comments-loading {
  padding: var(--pm-space-lg);
}

/* Comments Container */
.comments-container {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
}

/* Comment Item */
.comment-item {
  display: flex;
  gap: var(--pm-space-md);
  transition: all var(--pm-transition-fast);
}

.comment-item.highlighted {
  background: rgba(0, 212, 170, 0.05);
  border-radius: var(--pm-radius-md);
  padding: var(--pm-space-sm);
  margin: 0 calc(-1 * var(--pm-space-sm));
}

.comment-item.comment-reply {
  padding-top: var(--pm-space-sm);
}

.comment-avatar {
  flex-shrink: 0;
  position: relative;
}

.comment-thread-line {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 2px;
  height: 20px;
  background: var(--pm-border);
}

.comment-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xs);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  flex-wrap: wrap;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
  color: var(--pm-text-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--pm-text-muted);
}

.comment-edited {
  font-size: 11px;
  color: var(--pm-text-muted);
  font-style: italic;
}

.comment-body {
  font-size: 14px;
  color: var(--pm-text-secondary);
  line-height: 1.6;
  word-wrap: break-word;
}

.comment-body :deep(.comment-mention) {
  color: var(--pm-accent);
  font-weight: 500;
  background: rgba(0, 212, 170, 0.1);
  padding: 0 4px;
  border-radius: 3px;
}

.comment-body :deep(.comment-code) {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 13px;
  background: var(--pm-background);
  padding: 2px 6px;
  border-radius: var(--pm-radius-xs);
  color: var(--pm-primary);
}

.comment-body :deep(.comment-link) {
  color: var(--pm-accent);
  text-decoration: none;
}

.comment-body :deep(.comment-link:hover) {
  text-decoration: underline;
}

.comment-actions {
  display: flex;
  gap: var(--pm-space-sm);
  margin-top: var(--pm-space-xs);
}

/* Reply Form */
.reply-form {
  margin-top: var(--pm-space-sm);
  padding: var(--pm-space-md);
  background: var(--pm-background);
  border-radius: var(--pm-radius-md);
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.reply-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--pm-space-sm);
}

/* Comment Replies */
.comment-replies {
  margin-top: var(--pm-space-sm);
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
  padding-left: var(--pm-space-lg);
  border-left: 2px solid var(--pm-border-light);
}

/* Load More */
.comments-load-more {
  display: flex;
  justify-content: center;
  padding: var(--pm-space-lg);
}

/* Image Preview */
.image-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--pm-background);
}

.image-preview-container img {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}
</style>
