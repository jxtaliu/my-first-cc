<template>
  <el-dialog
    v-model="visible"
    :title="$t('project.quickAddTask')"
    width="480px"
    class="pm-dialog quick-add-dialog"
    @close="onClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-position="top"
      @submit.prevent="onSubmit"
    >
      <el-form-item :label="$t('project.taskTitle')" prop="title">
        <el-input
          v-model="formData.title"
          :placeholder="$t('project.taskTitlePlaceholder')"
          autofocus
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <div class="form-row">
        <el-form-item :label="$t('project.taskType')" class="form-col" prop="type">
          <el-select v-model="formData.type" style="width: 100%">
            <el-option label="任务" value="task" />
            <el-option label="缺陷" value="bug" />
            <el-option label="故事" value="story" />
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('project.priority')" class="form-col" prop="priority">
          <el-select v-model="formData.priority" style="width: 100%">
            <el-option label="P0 紧急" value="P0" />
            <el-option label="P1 高" value="P1" />
            <el-option label="P2 中" value="P2" />
            <el-option label="P3 低" value="P3" />
          </el-select>
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="onClose">{{ $t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="loading" @click="onSubmit">
        {{ $t('common.create') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  status: {
    type: String,
    default: 'todo'
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref(null)
const loading = ref(false)

const formData = reactive({
  title: '',
  type: 'task',
  priority: 'P3'
})

const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在 1 到 200 个字符', trigger: 'blur' }
  ]
}

const visible = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const resetForm = () => {
  formData.title = ''
  formData.type = 'task'
  formData.priority = 'P3'
  formRef.value?.clearValidate()
}

const onClose = () => {
  visible.value = false
}

const onSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    emit('submit', {
      title: formData.title,
      type: formData.type,
      priority: formData.priority,
      status: props.status
    })

    visible.value = false
  } catch (error) {
    // Validation failed
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.quick-add-dialog :deep(.el-dialog__body) {
  padding-top: var(--pm-space-lg);
}

.form-row {
  display: flex;
  gap: var(--pm-space-lg);
}

.form-col {
  flex: 1;
}
</style>
