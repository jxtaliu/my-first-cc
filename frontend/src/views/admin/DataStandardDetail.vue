<template>
  <div class="data-standard-detail-page">
    <div class="page-header">
      <el-button text @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        {{ $t('common.back') }}
      </el-button>
      <div class="header-actions">
        <el-button type="primary" @click="openShareDialog">
          <el-icon><Share /></el-icon>
          {{ $t('dataStandardsRoot.share') }}
        </el-button>
        <el-button type="primary" @click="openFormDialog">
          <el-icon><Edit /></el-icon>
          {{ $t('common.edit') }}
        </el-button>
      </div>
    </div>

    <div v-loading="loading">
      <!-- Header Info -->
      <div class="detail-header pm-card">
        <div class="header-main">
          <div class="title-row">
            <h1 class="detail-title">{{ detail.name }}</h1>
            <el-tag :type="getTypeTagType(detail.type)" size="large">
              {{ getTypeLabel(detail.type) }}
            </el-tag>
          </div>
          <p class="detail-code">{{ detail.code }}</p>
        </div>
        <div class="header-meta">
          <div class="meta-item">
            <span class="meta-label">{{ $t('dataStandardsRoot.owner') }}</span>
            <span class="meta-value">{{ detail.ownerName || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ $t('dataStandardsRoot.createdAt') }}</span>
            <span class="meta-value">{{ formatDate(detail.createdAt) }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">{{ $t('dataStandardsRoot.updatedAt') }}</span>
            <span class="meta-value">{{ formatDate(detail.updatedAt) }}</span>
          </div>
        </div>
        <div class="header-description" v-if="detail.description">
          <p>{{ detail.description }}</p>
        </div>
      </div>

      <!-- Type-specific Content -->
      <div class="detail-content">
        <!-- Enum Items -->
        <template v-if="detail.type === 'ENUM' && detail.enumItems">
          <div class="pm-card">
            <h3 class="section-title">{{ $t('dataStandardsRoot.enumItems') }}</h3>
            <el-table :data="detail.enumItems" stripe class="enum-table">
              <el-table-column prop="value" :label="$t('dataStandardsRoot.enumValue')" width="150" />
              <el-table-column prop="label" :label="$t('dataStandardsRoot.enumLabel')" min-width="150" />
              <el-table-column prop="sortOrder" :label="$t('dataStandardsRoot.sortOrder')" width="100" align="center" />
              <el-table-column prop="description" :label="$t('dataStandardsRoot.description')" min-width="200" />
            </el-table>
          </div>
        </template>

        <!-- Code Item -->
        <template v-if="detail.type === 'CODE' && detail.codeItem">
          <div class="pm-card">
            <h3 class="section-title">{{ $t('dataStandardsRoot.codeDefinition') }}</h3>
            <div class="definition-grid">
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.codeFormat') }}</span>
                <span class="def-value code">{{ detail.codeItem.format || '-' }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.codePrefix') }}</span>
                <span class="def-value code">{{ detail.codeItem.prefix || '-' }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.codeLength') }}</span>
                <span class="def-value">{{ detail.codeItem.length || '-' }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.codeExample') }}</span>
                <span class="def-value code">{{ detail.codeItem.example || '-' }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- String Item -->
        <template v-if="detail.type === 'STRING' && detail.stringItem">
          <div class="pm-card">
            <h3 class="section-title">{{ $t('dataStandardsRoot.stringDefinition') }}</h3>
            <div class="definition-grid">
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.minLength') }}</span>
                <span class="def-value">{{ detail.stringItem.minLength }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.maxLength') }}</span>
                <span class="def-value">{{ detail.stringItem.maxLength }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.pattern') }}</span>
                <span class="def-value code">{{ detail.stringItem.pattern || '-' }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.example') }}</span>
                <span class="def-value">{{ detail.stringItem.example || '-' }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- Number Item -->
        <template v-if="detail.type === 'NUMBER' && detail.numberItem">
          <div class="pm-card">
            <h3 class="section-title">{{ $t('dataStandardsRoot.numberDefinition') }}</h3>
            <div class="definition-grid">
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.minValue') }}</span>
                <span class="def-value">{{ detail.numberItem.minValue }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.maxValue') }}</span>
                <span class="def-value">{{ detail.numberItem.maxValue }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.decimalPlaces') }}</span>
                <span class="def-value">{{ detail.numberItem.decimalPlaces }}</span>
              </div>
              <div class="def-item">
                <span class="def-label">{{ $t('dataStandardsRoot.example') }}</span>
                <span class="def-value">{{ detail.numberItem.example || '-' }}</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- Share Dialog -->
    <el-dialog v-model="shareVisible" :title="$t('dataStandardsRoot.share')" width="500px" destroy-on-close>
      <el-form :model="shareForm" label-width="100px">
        <el-form-item :label="$t('dataStandardsRoot.shareMethod')">
          <el-radio-group v-model="shareForm.method">
            <el-radio value="feishu">{{ $t('dataStandardsRoot.feishu') }}</el-radio>
            <el-radio value="email">{{ $t('dataStandardsRoot.email') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="shareForm.method === 'feishu'" :label="$t('dataStandardsRoot.webhookUrl')">
          <el-input v-model="shareForm.webhookUrl" :placeholder="$t('dataStandardsRoot.webhookUrlPlaceholder')" />
        </el-form-item>
        <el-form-item v-if="shareForm.method === 'email'" :label="$t('dataStandardsRoot.recipient')">
          <el-input v-model="shareForm.recipient" :placeholder="$t('dataStandardsRoot.emailPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shareVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleShare" :loading="shareLoading">
          {{ $t('dataStandardsRoot.send') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Form Dialog -->
    <el-dialog
      v-model="formVisible"
      :title="$t('dataStandardsRoot.editStandard')"
      width="700px"
      destroy-on-close
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item :label="$t('dataStandardsRoot.code')" prop="code">
          <el-input v-model="form.code" disabled />
        </el-form-item>
        <el-form-item :label="$t('dataStandardsRoot.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('dataStandardsRoot.type')">
          <el-input :value="getTypeLabel(form.type)" disabled />
        </el-form-item>
        <el-form-item :label="$t('dataStandardsRoot.description')">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item :label="$t('dataStandardsRoot.owner')">
          <el-input v-model="form.ownerName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="submitLoading">
          {{ $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getDataStandardDetail, updateDataStandard, shareDataStandard } from '@/api/dataStandard'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const loading = ref(false)
const shareLoading = ref(false)
const submitLoading = ref(false)
const detail = ref({})
const shareVisible = ref(false)
const formVisible = ref(false)
const formRef = ref()

const shareForm = reactive({
  method: 'feishu',
  webhookUrl: '',
  recipient: ''
})

const form = reactive({
  id: null,
  code: '',
  name: '',
  type: '',
  description: '',
  ownerName: ''
})

const formRules = {
  name: [{ required: true, message: () => t('common.required'), trigger: 'blur' }]
}

function getTypeLabel(type) {
  const map = {
    'ENUM': t('dataStandardsRoot.typeEnum'),
    'CODE': t('dataStandardsRoot.typeCode'),
    'STRING': t('dataStandardsRoot.typeString'),
    'NUMBER': t('dataStandardsRoot.typeNumber')
  }
  return map[type] || type
}

function getTypeTagType(type) {
  const map = {
    'ENUM': '',
    'CODE': 'success',
    'STRING': 'warning',
    'NUMBER': 'info'
  }
  return map[type] || ''
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

function goBack() {
  router.push('/admin/data-standards')
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await getDataStandardDetail(route.params.id)
    detail.value = res.data || {}
  } catch (error) {
    console.error('Failed to load detail:', error)
    ElMessage.error(t('common.loadFailed'))
  } finally {
    loading.value = false
  }
}

function openShareDialog() {
  shareForm.method = 'feishu'
  shareForm.webhookUrl = ''
  shareForm.recipient = ''
  shareVisible.value = true
}

async function handleShare() {
  if (shareForm.method === 'feishu' && !shareForm.webhookUrl) {
    ElMessage.warning(t('dataStandardsRoot.webhookRequired'))
    return
  }
  if (shareForm.method === 'email' && !shareForm.recipient) {
    ElMessage.warning(t('dataStandardsRoot.emailRequired'))
    return
  }

  shareLoading.value = true
  try {
    await shareDataStandard(route.params.id, {
      method: shareForm.method,
      webhookUrl: shareForm.webhookUrl,
      recipient: shareForm.recipient
    })
    ElMessage.success(t('dataStandardsRoot.shareSuccess'))
    shareVisible.value = false
  } catch (error) {
    console.error('Failed to share:', error)
    ElMessage.error(error.message || t('common.operationFailed'))
  } finally {
    shareLoading.value = false
  }
}

function openFormDialog() {
  form.id = detail.value.id
  form.code = detail.value.code
  form.name = detail.value.name
  form.type = detail.value.type
  form.description = detail.value.description || ''
  form.ownerName = detail.value.ownerName || ''
  formVisible.value = true
}

async function handleUpdate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    await updateDataStandard(form.id, {
      name: form.name,
      description: form.description,
      ownerName: form.ownerName
    })
    ElMessage.success(t('common.updateSuccess'))
    formVisible.value = false
    loadDetail()
  } catch (error) {
    console.error('Failed to update:', error)
    ElMessage.error(error.message || t('common.operationFailed'))
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.data-standard-detail-page {
  max-width: 1000px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-xl);
}

.header-actions {
  display: flex;
  gap: var(--pm-space-sm);
}

.detail-header {
  padding: var(--pm-space-xl);
  margin-bottom: var(--pm-space-xl);
}

.title-row {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-sm);
}

.detail-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.detail-code {
  font-family: monospace;
  color: var(--pm-text-secondary);
  margin: 0 0 var(--pm-space-lg) 0;
}

.header-meta {
  display: flex;
  gap: var(--pm-space-2xl);
  padding: var(--pm-space-lg) 0;
  border-top: 1px solid var(--pm-border);
  border-bottom: 1px solid var(--pm-border);
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xs);
}

.meta-label {
  font-size: 12px;
  color: var(--pm-text-secondary);
}

.meta-value {
  font-size: 14px;
  font-weight: 500;
}

.header-description {
  padding-top: var(--pm-space-lg);
  color: var(--pm-text-secondary);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 var(--pm-space-lg) 0;
}

.definition-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--pm-space-lg);
}

.def-item {
  display: flex;
  flex-direction: column;
  gap: var(--pm-space-xs);
}

.def-label {
  font-size: 12px;
  color: var(--pm-text-secondary);
}

.def-value {
  font-size: 14px;
  font-weight: 500;
}

.def-value.code {
  font-family: monospace;
  color: var(--pm-primary);
}

.enum-table {
  margin-top: var(--pm-space-md);
}

.detail-content .pm-card {
  margin-bottom: var(--pm-space-lg);
}
</style>
