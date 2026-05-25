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
        <el-radio-group v-model="formData.type" :disabled="isEdit" class="type-radio-group">
          <el-radio-button
            v-for="item in typeOptions"
            :key="item.value"
            :value="item.value"
          >
            {{ $t(`requirements.type_${item.value.toLowerCase()}`) }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="$t('requirements.title')" prop="title">
        <el-input v-model="formData.title" :placeholder="$t('requirements.titlePlaceholder')" maxlength="200" show-word-limit />
      </el-form-item>

      <!-- EPIC/FEATURE/STORY: 描述字段 -->
      <el-form-item :label="$t('requirements.description')" v-if="showDescription">
        <el-input
          v-model="formData.description"
          type="textarea"
          :placeholder="$t('requirements.descriptionPlaceholder')"
          :rows="3"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <!-- FEATURE: 关联Epic -->
      <el-form-item :label="$t('requirements.parentEpic')" v-if="formData.type === 'FEATURE'">
        <el-select v-model="formData.parentId" :placeholder="$t('requirements.selectEpic')" clearable filterable class="full-width">
          <el-option
            v-for="epic in parentEpics"
            :key="epic.id"
            :label="epic.title"
            :value="epic.id"
          />
        </el-select>
      </el-form-item>

      <!-- STORY: 关联Feature -->
      <el-form-item :label="$t('requirements.parentFeature')" v-if="formData.type === 'STORY'">
        <el-select v-model="formData.parentId" :placeholder="$t('requirements.selectFeature')" clearable filterable class="full-width">
          <el-option
            v-for="feature in parentFeatures"
            :key="feature.id"
            :label="feature.title"
            :value="feature.id"
          />
        </el-select>
      </el-form-item>

      <!-- TASK: 关联Story -->
      <el-form-item :label="$t('requirements.parentStory')" v-if="formData.type === 'TASK'">
        <el-select v-model="formData.parentId" :placeholder="$t('requirements.selectStory')" clearable filterable class="full-width">
          <el-option
            v-for="story in parentStories"
            :key="story.id"
            :label="story.title"
            :value="story.id"
          />
        </el-select>
      </el-form-item>

      <!-- SUBTASK: 关联Task -->
      <el-form-item :label="$t('requirements.parentTask')" v-if="formData.type === 'SUBTASK'">
        <el-select v-model="formData.parentId" :placeholder="$t('requirements.selectTask')" clearable filterable class="full-width">
          <el-option
            v-for="task in parentTasks"
            :key="task.id"
            :label="task.title"
            :value="task.id"
          />
        </el-select>
      </el-form-item>

      <!-- EPIC/FEATURE/STORY/TASK: 优先级 -->
      <el-form-item :label="$t('requirements.priority')" prop="priority" v-if="showPriority">
        <el-select v-model="formData.priority" :placeholder="$t('requirements.selectPriority')" class="full-width">
          <el-option :label="$t('requirements.p0')" value="P0" />
          <el-option :label="$t('requirements.p1')" value="P1" />
          <el-option :label="$t('requirements.p2')" value="P2" />
          <el-option :label="$t('requirements.p3')" value="P3" />
        </el-select>
      </el-form-item>

      <!-- STORY: 故事点 -->
      <el-form-item :label="$t('requirements.storyPoints')" v-if="formData.type === 'STORY'">
        <el-input-number v-model="formData.storyPoints" :min="0" :max="100" class="full-width" />
      </el-form-item>

      <!-- FEATURE/STORY/TASK/SUBTASK: 预估工时 -->
      <el-form-item :label="$t('requirements.estimateHours')" v-if="showEstimateHours">
        <el-input-number v-model="formData.estimateHours" :min="0" :max="9999" :precision="1" class="full-width" />
      </el-form-item>

      <!-- TASK/SUBTASK: 实际工时 -->
      <el-form-item :label="$t('requirements.actualHours')" v-if="formData.type === 'TASK' || formData.type === 'SUBTASK'">
        <el-input-number v-model="formData.actualHours" :min="0" :max="9999" :precision="1" class="full-width" />
      </el-form-item>

      <!-- EPIC/FEATURE/TASK: 截止日期 -->
      <el-form-item :label="$t('requirements.dueDate')" v-if="showDueDate">
        <el-date-picker
          v-model="formData.dueDate"
          type="date"
          :placeholder="$t('requirements.selectDueDate')"
          class="full-width"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <!-- 所有类型: 负责人 -->
      <el-form-item :label="$t('requirements.assignee')" v-if="showAssignee">
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
import { createRequirement, updateRequirement, getRequirementTree } from '@/api/requirements'
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

// 父级选项
const parentEpics = ref([])
const parentFeatures = ref([])
const parentStories = ref([])
const parentTasks = ref([])

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

// 加载父级选项
const loadParentOptions = async () => {
  if (!props.projectId) return

  try {
    const res = await getRequirementTree(props.projectId)
    const treeData = res.data || []

    // 扁平化树结构
    const flattenTree = (nodes) => {
      const result = []
      const traverse = (items) => {
        items.forEach(item => {
          result.push(item)
          if (item.children?.length) {
            traverse(item.children)
          }
        })
      }
      traverse(nodes)
      return result
    }

    const allItems = flattenTree(treeData)

    // 分类存储
    parentEpics.value = allItems.filter(item => item.type === 'EPIC')
    parentFeatures.value = allItems.filter(item => item.type === 'FEATURE')
    parentStories.value = allItems.filter(item => item.type === 'STORY')
    parentTasks.value = allItems.filter(item => item.type === 'TASK')
  } catch (e) {
    console.error('Failed to load parent options:', e)
  }
}

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

// 根据类型显示/隐藏字段
const showDescription = computed(() => ['EPIC', 'FEATURE', 'STORY'].includes(formData.type))
const showPriority = computed(() => ['EPIC', 'FEATURE', 'STORY', 'TASK'].includes(formData.type))
const showEstimateHours = computed(() => ['FEATURE', 'STORY', 'TASK', 'SUBTASK'].includes(formData.type))
const showDueDate = computed(() => ['EPIC', 'FEATURE', 'TASK'].includes(formData.type))
const showAssignee = computed(() => ['EPIC', 'FEATURE', 'STORY', 'TASK', 'SUBTASK'].includes(formData.type))

const formData = reactive({
  id: null,
  type: 'STORY',
  title: '',
  description: '',
  priority: 'P2',
  storyPoints: null,
  estimateHours: null,
  actualHours: null,
  dueDate: null,
  assigneeId: null,
  parentId: null
})

const formRules = {
  type: [{ required: true, message: t('requirements.typeRequired'), trigger: 'change' }],
  title: [{ required: true, message: t('requirements.titleRequired'), trigger: 'blur' }],
  priority: [{ required: true, message: t('requirements.priorityRequired'), trigger: 'change' }]
}

// 监听弹窗显示
watch(() => props.modelValue, (val) => {
  if (val) {
    loadProjectMembers()
    loadParentOptions()
    // 如果有父项，设置默认类型和parentId
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
      formData.parentId = props.parentItem.id
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
  formData.storyPoints = null
  formData.estimateHours = null
  formData.actualHours = null
  formData.dueDate = null
  formData.assigneeId = null
  formData.parentId = null
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
    // Validate projectId is available
    if (!props.projectId && !props.parentItem?.projectId) {
      ElMessage.error(t('requirements.projectIdRequired'))
      return
    }
    // Use projectId from props, or fall back to parentItem's projectId
    const effectiveProjectId = props.projectId || props.parentItem?.projectId
    const data = {
      projectId: effectiveProjectId,
      type: formData.type,
      title: formData.title,
      description: formData.description || null,
      priority: formData.priority,
      storyPoints: formData.storyPoints || null,
      estimateHours: formData.estimateHours || null,
      actualHours: formData.actualHours || null,
      dueDate: formData.dueDate || null,
      assigneeId: formData.assigneeId || null,
      parentId: formData.parentId || null
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
  formData.storyPoints = item.storyPoints || null
  formData.estimateHours = item.estimateHours || null
  formData.actualHours = item.actualHours || null
  formData.dueDate = item.dueDate || null
  formData.assigneeId = item.assigneeId || null
  formData.parentId = item.parentId || null
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
.type-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.type-radio-group .el-radio-button {
  margin: 0;
}
.type-radio-group .el-radio-button__inner {
  border-radius: 4px;
  border: 1px solid var(--el-border-color);
  padding: 8px 16px;
}
.type-radio-group .el-radio-button:first-child .el-radio-button__inner {
  border-radius: 4px;
}
.type-radio-group .el-radio-button:last-child .el-radio-button__inner {
  border-radius: 4px;
}
.type-radio-group .el-radio-button__original-radio:checked + .el-radio-button__inner {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #fff;
}
</style>
