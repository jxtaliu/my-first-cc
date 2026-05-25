<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? $t('requirements.editRequirement') : $t('requirements.createRequirement')"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item :label="$t('requirements.type')" prop="type">
        <el-select v-model="formData.type" :placeholder="$t('requirements.selectType')" class="full-width">
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="$t(`requirements.type_${item.value.toLowerCase()}`)"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('requirements.title')" prop="title">
        <el-input v-model="formData.title" :placeholder="$t('requirements.titlePlaceholder')" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item :label="$t('requirements.description')">
        <el-input
          v-model="formData.description"
          type="textarea"
          :placeholder="$t('requirements.descriptionPlaceholder')"
          :rows="3"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item :label="$t('requirements.priority')" prop="priority">
        <el-select v-model="formData.priority" :placeholder="$t('requirements.selectPriority')" class="full-width">
          <el-option :label="$t('requirements.p0')" value="P0" />
          <el-option :label="$t('requirements.p1')" value="P1" />
          <el-option :label="$t('requirements.p2')" value="P2" />
          <el-option :label="$t('requirements.p3')" value="P3" />
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('requirements.estimateHours')">
        <el-input-number v-model="formData.estimateHours" :min="0" :max="9999" :precision="1" class="full-width" />
      </el-form-item>

      <el-form-item :label="$t('requirements.dueDate')">
        <el-date-picker
          v-model="formData.dueDate"
          type="date"
          :placeholder="$t('requirements.selectDueDate')"
          class="full-width"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item :label="$t('requirements.assignee')">
        <el-select
          v-model="formData.assigneeId"
          :placeholder="$t('requirements.selectAssignee')"
          clearable
          filterable
          class="full-width"
        >
          <el-option
            v-for="user in projectMembers"
            :key="user.userId"
            :label="user.userName"
            :value="user.userId"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ $t('common.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createRequirement, updateRequirement } from '@/api/requirements'
import { getProjectMembers } from '@/api/project'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  projectId: {
    type: [String, Number],
    default: null
  },
  parentItem: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { t } = useI18n()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!formData.id)

const formRef = ref()
const submitting = ref(false)
const projectMembers = ref([])

// 可选的类型
const typeOptions = computed(() => {
  const types = ['EPIC', 'FEATURE', 'STORY', 'TASK', 'SUBTASK']
  // 如果有父项，根据父项类型限制可选类型
  if (props.parentItem) {
    const parentType = props.parentItem.type
    if (parentType === 'EPIC') {
      return types.filter(t => ['FEATURE', 'STORY'].includes(t)).map(value => ({ value }))
    } else if (parentType === 'FEATURE') {
      return types.filter(t => ['STORY'].includes(t)).map(value => ({ value }))
    } else if (parentType === 'STORY') {
      return types.filter(t => ['TASK', 'SUBTASK'].includes(t)).map(value => ({ value }))
    } else if (parentType === 'TASK') {
      return types.filter(t => ['SUBTASK'].includes(t)).map(value => ({ value }))
    }
  }
  return types.map(value => ({ value }))
})

const formData = reactive({
  id: null,
  type: 'STORY',
  title: '',
  description: '',
  priority: 'P2',
  estimateHours: null,
  dueDate: null,
  assigneeId: null
})

const formRules = {
  type: [{ required: true, message: t('requirements.typeRequired'), trigger: 'change' }],
  title: [{ required: true, message: t('requirements.titleRequired'), trigger: 'blur' }],
  priority: [{ required: true, message: t('requirements.priorityRequired'), trigger: 'change' }]
}

// 加载项目成员
const loadProjectMembers = async () => {
  if (!props.projectId) return

  try {
    const res = await getProjectMembers(props.projectId)
    projectMembers.value = res.data || []
  } catch (e) {
    console.error('Failed to load project members:', e)
    projectMembers.value = []
  }
}

// 监听弹窗显示
watch(() => props.modelValue, (val) => {
  if (val) {
    loadProjectMembers()
    // 如果有父项，设置默认类型
    if (props.parentItem) {
      const parentType = props.parentItem.type
      if (parentType === 'EPIC') {
        formData.type = 'FEATURE'
      } else if (parentType === 'FEATURE') {
        formData.type = 'STORY'
      } else if (parentType === 'STORY') {
        formData.type = 'TASK'
      } else if (parentType === 'TASK') {
        formData.type = 'SUBTASK'
      }
    }
  }
})

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.type = 'STORY'
  formData.title = ''
  formData.description = ''
  formData.priority = 'P2'
  formData.estimateHours = null
  formData.dueDate = null
  formData.assigneeId = null
  formRef.value?.clearValidate()
}

// 关闭弹窗
const handleClose = () => {
  resetForm()
  dialogVisible.value = false
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    const data = {
      projectId: props.projectId,
      type: formData.type,
      title: formData.title,
      description: formData.description || null,
      priority: formData.priority,
      estimateHours: formData.estimateHours || null,
      dueDate: formData.dueDate || null,
      assigneeId: formData.assigneeId || null
    }

    // 如果有父项，添加 parentId
    if (props.parentItem) {
      data.parentId = props.parentItem.id
    }

    if (isEdit.value) {
      await updateRequirement(formData.id, data)
      ElMessage.success(t('common.success'))
    } else {
      await createRequirement(data)
      ElMessage.success(t('common.success'))
    }

    emit('success')
    handleClose()
  } catch (e) {
    ElMessage.error(t('common.failed'))
  } finally {
    submitting.value = false
  }
}

// 编辑时加载数据
const setEditData = (item) => {
  formData.id = item.id
  formData.type = item.type
  formData.title = item.title
  formData.description = item.description || ''
  formData.priority = item.priority || 'P2'
  formData.estimateHours = item.estimateHours || null
  formData.dueDate = item.dueDate || null
  formData.assigneeId = item.assigneeId || null
}

defineExpose({
  setEditData
})
</script>

<script>
import { useI18n } from 'vue-i18n'
export default {
  name: 'CreateRequirementDialog'
}
</script>

<style scoped>
.full-width {
  width: 100%;
}
</style>
