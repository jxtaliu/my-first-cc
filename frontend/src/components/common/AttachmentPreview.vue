<template>
  <div class="attachment-preview">
    <!-- Upload Area -->
    <div
      class="upload-area"
      :class="{ 'drag-over': isDragOver }"
      @dragover.prevent="isDragOver = true"
      @dragleave.prevent="isDragOver = false"
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <input
        ref="fileInput"
        type="file"
        multiple
        :accept="acceptedTypes"
        @change="handleFileSelect"
        style="display: none"
      />
      <div class="upload-content">
        <el-icon class="upload-icon"><Upload /></el-icon>
        <span class="upload-text">{{ $t('attachments.dropzone') }}</span>
        <span class="upload-hint">{{ $t('attachments.supportedFormats') }}</span>
      </div>
    </div>

    <!-- Upload Progress -->
    <div v-if="uploadingFiles.length > 0" class="upload-progress-list">
      <div
        v-for="file in uploadingFiles"
        :key="file.id"
        class="upload-progress-item"
      >
        <div class="upload-file-info">
          <el-icon class="upload-file-icon"><Document /></el-icon>
          <span class="upload-file-name">{{ file.name }}</span>
          <span class="upload-file-size">{{ formatFileSize(file.size) }}</span>
        </div>
        <div class="upload-progress-bar-container">
          <div
            class="upload-progress-bar"
            :class="{ 'upload-success': file.progress === 100 }"
            :style="{ width: file.progress + '%' }"
          ></div>
        </div>
        <span class="upload-progress-percent">{{ file.progress }}%</span>
        <el-button
          type="danger"
          text
          size="small"
          @click="cancelUpload(file)"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- Attachments Header -->
    <div class="attachments-header">
      <h3 class="attachments-title">
        {{ $t('attachments.title') }}
        <span class="attachments-count">({{ attachments.length }})</span>
      </h3>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="attachments-loading">
      <el-skeleton :rows="2" animated />
    </div>

    <!-- Empty State -->
    <el-empty v-else-if="!attachments.length && !uploadingFiles.length" :description="$t('attachments.noAttachments')" />

    <!-- Attachments Grid -->
    <div v-else class="attachments-grid">
      <div
        v-for="attachment in attachments"
        :key="attachment.id"
        class="attachment-card"
        @click="handlePreview(attachment)"
      >
        <!-- Thumbnail/Icon -->
        <div class="attachment-thumbnail">
          <img
            v-if="isImage(attachment.mimeType)"
            :src="attachment.url"
            :alt="attachment.name"
            class="attachment-image"
          />
          <div v-else class="attachment-icon-wrapper" :class="getFileTypeClass(attachment.mimeType)">
            <el-icon class="attachment-icon"><Document /></el-icon>
            <span class="attachment-ext">{{ getFileExtension(attachment.name) }}</span>
          </div>
        </div>

        <!-- Info -->
        <div class="attachment-info">
          <span class="attachment-name" :title="attachment.name">{{ attachment.name }}</span>
          <span class="attachment-meta">
            {{ formatFileSize(attachment.size) }}
            <span v-if="attachment.uploadedAt"> · {{ formatTime(attachment.uploadedAt) }}</span>
          </span>
        </div>

        <!-- Actions -->
        <div class="attachment-actions" @click.stop>
          <el-button
            type="primary"
            text
            size="small"
            @click="downloadFile(attachment)"
            :title="$t('attachments.download')"
          >
            <el-icon><Download /></el-icon>
          </el-button>
          <el-button
            v-if="canDelete(attachment)"
            type="danger"
            text
            size="small"
            @click="handleDelete(attachment)"
            :title="$t('common.delete')"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- Image Preview Modal -->
    <el-dialog
      v-model="imagePreviewVisible"
      :title="previewAttachment?.name"
      width="900px"
      class="image-preview-dialog"
    >
      <div class="image-preview-container">
        <img :src="previewAttachment?.url" :alt="previewAttachment?.name" />
      </div>
      <template #footer>
        <div class="image-preview-footer">
          <span class="preview-meta">
            {{ previewAttachment?.name }} ({{ formatFileSize(previewAttachment?.size || 0) }})
          </span>
          <el-button type="primary" @click="downloadFile(previewAttachment)">
            <el-icon><Download /></el-icon>
            {{ $t('attachments.download') }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- File Info Dialog -->
    <el-dialog
      v-model="fileInfoVisible"
      :title="previewAttachment?.name"
      width="500px"
    >
      <div class="file-info-content" v-if="previewAttachment">
        <div class="file-info-row">
          <span class="file-info-label">{{ $t('attachments.fileName') }}:</span>
          <span class="file-info-value">{{ previewAttachment.name }}</span>
        </div>
        <div class="file-info-row">
          <span class="file-info-label">{{ $t('attachments.fileSize') }}:</span>
          <span class="file-info-value">{{ formatFileSize(previewAttachment.size) }}</span>
        </div>
        <div class="file-info-row">
          <span class="file-info-label">{{ $t('attachments.mimeType') }}:</span>
          <span class="file-info-value">{{ previewAttachment.mimeType }}</span>
        </div>
        <div class="file-info-row">
          <span class="file-info-label">{{ $t('attachments.uploadedBy') }}:</span>
          <span class="file-info-value">{{ previewAttachment.uploadedBy?.name || 'Unknown' }}</span>
        </div>
        <div class="file-info-row">
          <span class="file-info-label">{{ $t('attachments.uploadedAt') }}:</span>
          <span class="file-info-value">{{ formatDateTime(previewAttachment.uploadedAt) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="fileInfoVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="downloadFile(previewAttachment)">
          <el-icon><Download /></el-icon>
          {{ $t('attachments.download') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Document, Download, Delete, Close } from '@element-plus/icons-vue'
import { getTaskAttachments, addTaskAttachment, deleteTaskAttachment } from '@/api/task'

const props = defineProps({
  taskId: {
    type: [Number, String],
    required: true
  },
  accept: {
    type: String,
    default: 'image/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar'
  }
})

const emit = defineEmits(['attachment-added', 'attachment-deleted'])

const { t } = useI18n()

// State
const attachments = ref([])
const loading = ref(false)
const uploadingFiles = ref([])
const isDragOver = ref(false)
const fileInput = ref(null)
const imagePreviewVisible = ref(false)
const fileInfoVisible = ref(false)
const previewAttachment = ref(null)
const currentUser = ref({ id: 1, name: 'Current User', role: 'MEMBER' })

// Computed
const acceptedTypes = computed(() => props.accept)

// Check if file is an image
const isImage = (mimeType) => {
  if (!mimeType) return false
  return mimeType.startsWith('image/')
}

// Get file extension
const getFileExtension = (filename) => {
  if (!filename) return ''
  const parts = filename.split('.')
  return parts.length > 1 ? parts[parts.length - 1].toUpperCase() : ''
}

// Get file type class for styling
const getFileTypeClass = (mimeType) => {
  if (!mimeType) return 'file-type-other'
  if (mimeType.includes('pdf')) return 'file-type-pdf'
  if (mimeType.includes('word') || mimeType.includes('document')) return 'file-type-doc'
  if (mimeType.includes('excel') || mimeType.includes('sheet')) return 'file-type-xls'
  if (mimeType.includes('powerpoint') || mimeType.includes('presentation')) return 'file-type-ppt'
  if (mimeType.includes('zip') || mimeType.includes('rar') || mimeType.includes('archive')) return 'file-type-archive'
  if (mimeType.includes('text')) return 'file-type-text'
  return 'file-type-other'
}

// Format file size
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
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

// Format date time
const formatDateTime = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString()
}

// Check if user can delete an attachment
const canDelete = (attachment) => {
  return attachment.uploadedById === currentUser.value?.id ||
         currentUser.value?.role === 'ADMIN' ||
         currentUser.value?.role === 'PROJECT_ADMIN'
}

// Load attachments from API
const loadAttachments = async () => {
  loading.value = true
  try {
    const res = await getTaskAttachments(props.taskId)
    attachments.value = res.data || res || []
  } catch (error) {
    console.error('Failed to load attachments:', error)
    // Load mock data on error
    loadMockAttachments()
  } finally {
    loading.value = false
  }
}

// Mock attachments for demo
const loadMockAttachments = () => {
  attachments.value = [
    {
      id: 1,
      name: 'screenshot-2024.png',
      url: 'https://via.placeholder.com/150',
      size: 245678,
      mimeType: 'image/png',
      uploadedAt: new Date(Date.now() - 86400000).toISOString(),
      uploadedBy: { id: 1, name: 'Current User' },
      uploadedById: 1
    },
    {
      id: 2,
      name: 'requirements-document.pdf',
      url: '#',
      size: 1234567,
      mimeType: 'application/pdf',
      uploadedAt: new Date(Date.now() - 172800000).toISOString(),
      uploadedBy: { id: 2, name: '张三' },
      uploadedById: 2
    },
    {
      id: 3,
      name: 'budget-analysis.xlsx',
      url: '#',
      size: 567890,
      mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      uploadedAt: new Date(Date.now() - 259200000).toISOString(),
      uploadedBy: { id: 3, name: '李四' },
      uploadedById: 3
    }
  ]
}

// Trigger file input
const triggerFileInput = () => {
  fileInput.value?.click()
}

// Handle file selection
const handleFileSelect = (event) => {
  const files = event.target.files
  if (files.length > 0) {
    uploadFiles(Array.from(files))
  }
  // Reset input
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// Handle drag and drop
const handleDrop = (event) => {
  isDragOver.value = false
  const files = Array.from(event.dataTransfer.files)
  if (files.length > 0) {
    uploadFiles(files)
  }
}

// Upload files
const uploadFiles = (files) => {
  files.forEach((file, index) => {
    const uploadId = Date.now() + index
    const uploadFile = {
      id: uploadId,
      name: file.name,
      size: file.size,
      progress: 0
    }

    uploadingFiles.value.push(uploadFile)
    simulateUpload(uploadFile, file)
  })
}

// Simulate upload progress
const simulateUpload = (uploadFile, file) => {
  const interval = setInterval(() => {
    uploadFile.progress += Math.random() * 15
    if (uploadFile.progress >= 100) {
      uploadFile.progress = 100
      clearInterval(interval)

      // Add to attachments list
      const newAttachment = {
        id: uploadFile.id,
        name: file.name,
        url: URL.createObjectURL(file),
        size: file.size,
        mimeType: file.type || 'application/octet-stream',
        uploadedAt: new Date().toISOString(),
        uploadedBy: currentUser.value,
        uploadedById: currentUser.value.id
      }
      attachments.value.push(newAttachment)
      emit('attachment-added', newAttachment)

      // Remove from uploading list after a delay
      setTimeout(() => {
        uploadingFiles.value = uploadingFiles.value.filter(f => f.id !== uploadFile.id)
      }, 1000)
    }
  }, 200)
}

// Cancel upload
const cancelUpload = (uploadFile) => {
  uploadingFiles.value = uploadingFiles.value.filter(f => f.id !== uploadFile.id)
}

// Handle preview
const handlePreview = (attachment) => {
  previewAttachment.value = attachment
  if (isImage(attachment.mimeType)) {
    imagePreviewVisible.value = true
  } else {
    fileInfoVisible.value = true
  }
}

// Download file
const downloadFile = (attachment) => {
  if (!attachment || !attachment.url) return

  // For demo, show message
  ElMessage.info(t('attachments.downloading'))

  // In production, this would trigger actual download
  // const link = document.createElement('a')
  // link.href = attachment.url
  // link.download = attachment.name
  // link.click()
}

// Delete attachment
const handleDelete = async (attachment) => {
  try {
    await ElMessageBox.confirm(
      t('attachments.confirmDelete'),
      t('common.warning'),
      { type: 'warning' }
    )

    // API call would go here
    // await deleteTaskAttachment(attachment.id)

    attachments.value = attachments.value.filter(a => a.id !== attachment.id)
    emit('attachment-deleted', attachment)
    ElMessage.success(t('attachments.deleteSuccess'))
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete attachment:', error)
    }
  }
}

onMounted(() => {
  loadAttachments()
})
</script>

<style scoped>
.attachment-preview {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-lg);
}

/* Upload Area */
.upload-area {
  border: 2px dashed var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-2xl);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all var(--pm-transition-normal);
  background: var(--pm-background);
}

.upload-area:hover,
.upload-area.drag-over {
  border-color: var(--pm-accent);
  background: rgba(0, 212, 170, 0.05);
}

.upload-area.drag-over {
  transform: scale(1.02);
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--pm-space-sm);
}

.upload-icon {
  font-size: 32px;
  color: var(--pm-text-muted);
}

.upload-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--pm-text-primary);
}

.upload-hint {
  font-size: 12px;
  color: var(--pm-text-muted);
}

/* Upload Progress */
.upload-progress-list {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
}

.upload-progress-item {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  padding: var(--pm-space-md);
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-md);
}

.upload-file-info {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  flex: 1;
  min-width: 0;
}

.upload-file-icon {
  font-size: 20px;
  color: var(--pm-text-muted);
}

.upload-file-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-file-size {
  font-size: 12px;
  color: var(--pm-text-muted);
  flex-shrink: 0;
}

.upload-progress-bar-container {
  width: 100px;
  height: 6px;
  background: var(--pm-border-light);
  border-radius: 3px;
  overflow: hidden;
}

.upload-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--pm-accent), var(--pm-accent-dark));
  border-radius: 3px;
  transition: width 0.2s ease;
}

.upload-progress-bar.upload-success {
  background: var(--pm-status-done);
}

.upload-progress-percent {
  font-size: 12px;
  font-weight: 500;
  color: var(--pm-text-secondary);
  width: 36px;
  text-align: right;
}

/* Attachments Header */
.attachments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.attachments-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--pm-text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.attachments-count {
  font-weight: 400;
  color: var(--pm-text-muted);
}

/* Loading State */
.attachments-loading {
  padding: var(--pm-space-lg);
}

/* Attachments Grid */
.attachments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--pm-space-md);
}

/* Attachment Card */
.attachment-card {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-lg);
  padding: var(--pm-space-md);
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-sm);
  cursor: pointer;
  transition: all var(--pm-transition-normal);
}

.attachment-card:hover {
  border-color: var(--pm-accent);
  box-shadow: var(--pm-shadow-md);
  transform: translateY(-2px);
}

.attachment-thumbnail {
  width: 100%;
  height: 100px;
  border-radius: var(--pm-radius-md);
  overflow: hidden;
  background: var(--pm-background);
  display: flex;
  justify-content: center;
  align-items: center;
}

.attachment-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.attachment-icon-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--pm-space-xs);
  width: 100%;
  height: 100%;
}

.attachment-icon-wrapper.file-type-pdf { background: #FEF2F2; }
.attachment-icon-wrapper.file-type-pdf .attachment-icon { color: #EF4444; }

.attachment-icon-wrapper.file-type-doc { background: #EFF6FF; }
.attachment-icon-wrapper.file-type-doc .attachment-icon { color: #3B82F6; }

.attachment-icon-wrapper.file-type-xls { background: #F0FDF4; }
.attachment-icon-wrapper.file-type-xls .attachment-icon { color: #10B981; }

.attachment-icon-wrapper.file-type-ppt { background: #FEF3C7; }
.attachment-icon-wrapper.file-type-ppt .attachment-icon { color: #F59E0B; }

.attachment-icon-wrapper.file-type-archive { background: #F5F3FF; }
.attachment-icon-wrapper.file-type-archive .attachment-icon { color: #8B5CF6; }

.attachment-icon-wrapper.file-type-text { background: #F9FAFB; }
.attachment-icon-wrapper.file-type-text .attachment-icon { color: #6B7280; }

.attachment-icon-wrapper.file-type-other { background: #F9FAFB; }
.attachment-icon-wrapper.file-type-other .attachment-icon { color: #6B7280; }

.attachment-icon {
  font-size: 32px;
}

.attachment-ext {
  font-size: 10px;
  font-weight: 600;
  color: var(--pm-text-secondary);
}

.attachment-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.attachment-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.attachment-meta {
  font-size: 11px;
  color: var(--pm-text-muted);
}

.attachment-actions {
  display: flex;
  gap: var(--pm-space-xs);
  margin-top: var(--pm-space-xs);
  opacity: 0;
  transition: opacity var(--pm-transition-fast);
}

.attachment-card:hover .attachment-actions {
  opacity: 1;
}

/* Image Preview */
.image-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
  background: var(--pm-background);
}

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.image-preview-container img {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}

.image-preview-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-meta {
  font-size: 13px;
  color: var(--pm-text-secondary);
}

/* File Info */
.file-info-content {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-md);
}

.file-info-row {
  display: flex;
  gap: var(--pm-space-md);
}

.file-info-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-secondary);
  min-width: 100px;
}

.file-info-value {
  font-size: 13px;
  color: var(--pm-text-primary);
  word-break: break-all;
}
</style>
