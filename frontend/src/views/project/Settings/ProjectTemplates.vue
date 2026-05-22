<template>
  <div class="project-templates-page pm-page">
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('nav.projectTemplates') }}</h1>
      <el-button type="primary" @click="onCreate">
        {{ $t('common.create') }}
      </el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col v-for="template in templates" :span="8" :key="template.id">
        <el-card class="template-card" shadow="hover">
          <template #header>
            <div class="template-header">
              <span class="template-name">{{ template.name }}</span>
              <el-button text type="primary" size="small" @click="onEdit(template)">
                {{ $t('common.edit') }}
              </el-button>
            </div>
          </template>
          <p class="template-description">{{ template.description || '-' }}</p>
          <div class="template-meta">
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              Sprint: {{ template.sprintDuration }} {{ $t('project.sprintDuration') }}
            </span>
          </div>
        </el-card>
      </el-col>
      <el-col v-if="templates.length === 0 && !loading" :span="24">
        <el-empty :description="$t('common.noData')" />
      </el-col>
    </el-row>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="showDialog"
      :title="dialogTitle"
      width="500px"
      class="pm-dialog"
      @close="onDialogClose"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item :label="$t('project.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('project.name')" />
        </el-form-item>
        <el-form-item :label="$t('project.description')" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('project.description')"
          />
        </el-form-item>
        <el-form-item :label="$t('project.sprintDuration')" prop="sprintDuration">
          <el-input-number v-model="form.sprintDuration" :min="1" :max="12" />
          <span class="form-unit">{{ $t('settings.weeks') }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="onSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import { getTemplates, createTemplate, updateTemplate, deleteTemplate } from '@/api/template'

const { t } = useI18n()

const loading = ref(false)
const templates = ref([])
const showDialog = ref(false)
const editingId = ref(null)
const formRef = ref()

const form = ref({
  name: '',
  description: '',
  sprintDuration: 2
})

const formRules = {
  name: [{ required: true, message: t('project.projectNameRequired'), trigger: 'blur' }],
  sprintDuration: [{ required: true, message: t('common.required'), trigger: 'blur' }]
}

const dialogTitle = computed(() => {
  return editingId.value ? t('common.edit') : t('common.create')
})

const fetchTemplates = async () => {
  loading.value = true
  try {
    const res = await getTemplates()
    templates.value = res.data || []
  } catch (e) {
    // Demo data fallback
    templates.value = getMockTemplates()
  } finally {
    loading.value = false
  }
}

const getMockTemplates = () => [
  { id: 1, name: 'Scrum Template', description: 'Standard Scrum project template with 2-week sprints', sprintDuration: 2 },
  { id: 2, name: 'Kanban Template', description: 'Continuous flow Kanban board for ongoing work', sprintDuration: 0 },
  { id: 3, name: 'Agile Template', description: 'Flexible Agile methodology with monthly cycles', sprintDuration: 4 }
]

const onCreate = () => {
  editingId.value = null
  form.value = { name: '', description: '', sprintDuration: 2 }
  showDialog.value = true
}

const onEdit = (template) => {
  editingId.value = template.id
  form.value = { ...template }
  showDialog.value = true
}

const onDialogClose = () => {
  formRef.value?.resetFields()
}

const onSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (editingId.value) {
      await updateTemplate(editingId.value, form.value)
      ElMessage.success(t('project.projectUpdated'))
    } else {
      await createTemplate(form.value)
      ElMessage.success(t('project.projectCreated'))
    }
    showDialog.value = false
    fetchTemplates()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  }
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.project-templates-page {
  max-width: 1200px;
}

.pm-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.template-card {
  margin-bottom: 16px;
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-name {
  font-weight: 600;
  font-size: 16px;
}

.template-description {
  color: var(--pm-text-secondary);
  font-size: 14px;
  margin: 0 0 16px 0;
  min-height: 40px;
}

.template-meta {
  display: flex;
  gap: 16px;
  color: var(--pm-text-muted);
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-unit {
  margin-left: 8px;
  color: var(--pm-text-secondary);
}
</style>
