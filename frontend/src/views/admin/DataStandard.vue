<template>
  <div class="data-standard-page">
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('dataStandardsRoot.title') }}</h1>
        <p class="pm-text-small">{{ $t('dataStandardsRoot.description') }}</p>
      </div>
      <el-button type="primary" @click="openFormDialog()">
        <el-icon><Plus /></el-icon>
        {{ $t('common.add') }}
      </el-button>
    </div>

    <!-- Search and Filter -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        :placeholder="$t('dataStandardsRoot.searchPlaceholder')"
        clearable
        class="search-input"
        @clear="loadData"
        @keyup.enter="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filterType" clearable :placeholder="$t('dataStandardsRoot.filterByType')" @change="loadData">
        <el-option :label="$t('dataStandardsRoot.typeAll')" value="" />
        <el-option :label="$t('dataStandardsRoot.typeEnum')" value="ENUM" />
        <el-option :label="$t('dataStandardsRoot.typeCode')" value="CODE" />
        <el-option :label="$t('dataStandardsRoot.typeString')" value="STRING" />
        <el-option :label="$t('dataStandardsRoot.typeNumber')" value="NUMBER" />
      </el-select>
    </div>

    <!-- Type Tabs -->
    <div class="type-tabs">
      <div
        v-for="tab in typeTabs"
        :key="tab.value"
        class="type-tab"
        :class="{ active: selectedType === tab.value }"
        @click="selectType(tab.value)"
      >
        <span class="tab-label">{{ tab.label }}</span>
        <span class="tab-count">{{ tab.count }}</span>
      </div>
    </div>

    <!-- Data Table -->
    <div class="pm-card">
      <el-table :data="filteredList" v-loading="loading" stripe class="standard-table">
        <el-table-column prop="code" :label="$t('dataStandardsRoot.code')" width="180">
          <template #default="{ row }">
            <span class="code-text">{{ row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('dataStandardsRoot.name')" min-width="150" />
        <el-table-column :label="$t('dataStandardsRoot.type')" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" :label="$t('dataStandardsRoot.owner')" width="120" />
        <el-table-column prop="description" :label="$t('dataStandardsRoot.description')" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('common.actions')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDetail(row)">
              {{ $t('common.view') }}
            </el-button>
            <el-button link type="primary" size="small" @click="openFormDialog(row)">
              {{ $t('common.edit') }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              {{ $t('common.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Form Dialog -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? $t('dataStandardsRoot.editStandard') : $t('dataStandardsRoot.addStandard')"
      width="700px"
      destroy-on-close
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item :label="$t('dataStandardsRoot.code')" prop="code">
          <el-input v-model="form.code" :placeholder="$t('dataStandardsRoot.codePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('dataStandardsRoot.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('dataStandardsRoot.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('dataStandardsRoot.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('dataStandardsRoot.selectType')" :disabled="isEdit" @change="handleTypeChange">
            <el-option :label="$t('dataStandardsRoot.typeEnum')" value="ENUM" />
            <el-option :label="$t('dataStandardsRoot.typeCode')" value="CODE" />
            <el-option :label="$t('dataStandardsRoot.typeString')" value="STRING" />
            <el-option :label="$t('dataStandardsRoot.typeNumber')" value="NUMBER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('dataStandardsRoot.description')">
          <el-input v-model="form.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item :label="$t('dataStandardsRoot.owner')">
          <el-input v-model="form.ownerName" :placeholder="$t('dataStandardsRoot.ownerPlaceholder')" />
        </el-form-item>

        <!-- Enum Items -->
        <template v-if="form.type === 'ENUM'">
          <el-divider>{{ $t('dataStandardsRoot.enumItems') }}</el-divider>
          <div class="enum-items">
            <div v-for="(item, index) in form.enumItems" :key="index" class="enum-item-row">
              <el-input v-model="item.value" :placeholder="$t('dataStandardsRoot.enumValue')" style="flex: 1" />
              <el-input v-model="item.label" :placeholder="$t('dataStandardsRoot.enumLabel')" style="flex: 1" />
              <el-input-number v-model="item.sortOrder" :min="0" controls-position="right" style="width: 100px" />
              <el-button type="danger" circle @click="removeEnumItem(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="dashed" @click="addEnumItem" class="add-enum-btn">
              <el-icon><Plus /></el-icon>
              {{ $t('dataStandardsRoot.addEnumItem') }}
            </el-button>
          </div>
        </template>

        <!-- Code Item -->
        <template v-if="form.type === 'CODE'">
          <el-divider>{{ $t('dataStandardsRoot.codeDefinition') }}</el-divider>
          <el-form-item :label="$t('dataStandardsRoot.codeFormat')">
            <el-input v-model="form.codeItem.format" :placeholder="$t('dataStandardsRoot.codeFormatPlaceholder')" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.codePrefix')">
            <el-input v-model="form.codeItem.prefix" :placeholder="$t('dataStandardsRoot.codePrefixPlaceholder')" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.codeLength')">
            <el-input-number v-model="form.codeItem.length" :min="1" :max="50" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.codeExample')">
            <el-input v-model="form.codeItem.example" :placeholder="$t('dataStandardsRoot.codeExamplePlaceholder')" />
          </el-form-item>
        </template>

        <!-- String Item -->
        <template v-if="form.type === 'STRING'">
          <el-divider>{{ $t('dataStandardsRoot.stringDefinition') }}</el-divider>
          <el-form-item :label="$t('dataStandardsRoot.minLength')">
            <el-input-number v-model="form.stringItem.minLength" :min="0" :max="1000" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.maxLength')">
            <el-input-number v-model="form.stringItem.maxLength" :min="1" :max="1000" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.pattern')">
            <el-input v-model="form.stringItem.pattern" :placeholder="$t('dataStandardsRoot.patternPlaceholder')" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.example')">
            <el-input v-model="form.stringItem.example" />
          </el-form-item>
        </template>

        <!-- Number Item -->
        <template v-if="form.type === 'NUMBER'">
          <el-divider>{{ $t('dataStandardsRoot.numberDefinition') }}</el-divider>
          <el-form-item :label="$t('dataStandardsRoot.minValue')">
            <el-input-number v-model="form.numberItem.minValue" :precision="6" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.maxValue')">
            <el-input-number v-model="form.numberItem.maxValue" :precision="6" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.decimalPlaces')">
            <el-input-number v-model="form.numberItem.decimalPlaces" :min="0" :max="10" />
          </el-form-item>
          <el-form-item :label="$t('dataStandardsRoot.example')">
            <el-input v-model="form.numberItem.example" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDataStandards, getDataStandardDetail, createDataStandard, updateDataStandard, deleteDataStandard } from '@/api/dataStandard'

const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const submitLoading = ref(false)
const formVisible = ref(false)
const isEdit = ref(false)
const selectedType = ref('')
const searchKeyword = ref('')
const filterType = ref('')

const list = ref([])

const typeTabs = computed(() => [
  { label: t('dataStandardsRoot.typeAll'), count: list.value.length, value: '' },
  { label: t('dataStandardsRoot.typeEnum'), count: getTypeCount('ENUM'), value: 'ENUM' },
  { label: t('dataStandardsRoot.typeCode'), count: getTypeCount('CODE'), value: 'CODE' },
  { label: t('dataStandardsRoot.typeString'), count: getTypeCount('STRING'), value: 'STRING' },
  { label: t('dataStandardsRoot.typeNumber'), count: getTypeCount('NUMBER'), value: 'NUMBER' }
])

const formRef = ref()
const form = reactive({
  id: null,
  code: '',
  name: '',
  type: 'ENUM',
  description: '',
  ownerName: '',
  enumItems: [],
  codeItem: { format: '', prefix: '', length: 10, example: '' },
  stringItem: { minLength: 1, maxLength: 255, pattern: '', example: '' },
  numberItem: { minValue: 0, maxValue: 999999, decimalPlaces: 2, example: '' }
})

const formRules = {
  code: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  name: [{ required: true, message: () => t('common.required'), trigger: 'blur' }],
  type: [{ required: true, message: () => t('common.required'), trigger: 'change' }]
}

const filteredList = computed(() => {
  let result = list.value
  if (selectedType.value) {
    result = result.filter(item => item.type === selectedType.value)
  }
  if (filterType.value) {
    result = result.filter(item => item.type === filterType.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(item =>
      item.code?.toLowerCase().includes(kw) ||
      item.name?.toLowerCase().includes(kw)
    )
  }
  return result
})

function getTypeCount(type) {
  return list.value.filter(item => item.type === type).length
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

function selectType(type) {
  selectedType.value = type
  filterType.value = ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await getDataStandards()
    list.value = res.data || []
  } catch (error) {
    console.error('Failed to load data standards:', error)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.code = ''
  form.name = ''
  form.type = 'ENUM'
  form.description = ''
  form.ownerName = ''
  form.enumItems = []
  form.codeItem = { format: '', prefix: '', length: 10, example: '' }
  form.stringItem = { minLength: 1, maxLength: 255, pattern: '', example: '' }
  form.numberItem = { minValue: 0, maxValue: 999999, decimalPlaces: 2, example: '' }
}

async function openFormDialog(row = null) {
  if (row) {
    isEdit.value = true
    try {
      const res = await getDataStandardDetail(row.id)
      const detail = res.data
      form.id = detail.id
      form.code = detail.code
      form.name = detail.name
      form.type = detail.type
      form.description = detail.description || ''
      form.ownerName = detail.ownerName || ''
      // 加载子表数据
      if (detail.type === 'ENUM' && detail.enumItems) {
        form.enumItems = detail.enumItems
      } else {
        form.enumItems = []
      }
      if (detail.type === 'CODE' && detail.codeItem) {
        form.codeItem = detail.codeItem
      } else {
        form.codeItem = { format: '', prefix: '', length: 10, example: '' }
      }
      if (detail.type === 'STRING' && detail.stringItem) {
        form.stringItem = detail.stringItem
      } else {
        form.stringItem = { minLength: 1, maxLength: 255, pattern: '', example: '' }
      }
      if (detail.type === 'NUMBER' && detail.numberItem) {
        form.numberItem = detail.numberItem
      } else {
        form.numberItem = { minValue: 0, maxValue: 999999, decimalPlaces: 2, example: '' }
      }
    } catch (error) {
      console.error('Failed to load detail:', error)
    }
  } else {
    isEdit.value = false
    resetForm()
  }
  formVisible.value = true
}

function handleTypeChange() {
  form.enumItems = []
}

function addEnumItem() {
  form.enumItems.push({ value: '', label: '', sortOrder: form.enumItems.length + 1, description: '' })
}

function removeEnumItem(index) {
  form.enumItems.splice(index, 1)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      code: form.code,
      name: form.name,
      type: form.type,
      description: form.description,
      ownerName: form.ownerName
    }

    if (form.type === 'ENUM' && form.enumItems.length > 0) {
      data.enumItems = form.enumItems
    } else if (form.type === 'CODE') {
      data.codeItem = form.codeItem
    } else if (form.type === 'STRING') {
      data.stringItem = form.stringItem
    } else if (form.type === 'NUMBER') {
      data.numberItem = form.numberItem
    }

    if (isEdit.value) {
      await updateDataStandard(form.id, data)
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await createDataStandard(data)
      ElMessage.success(t('common.addSuccess'))
    }
    formVisible.value = false
    loadData()
  } catch (error) {
    console.error('Failed to save:', error)
    ElMessage.error(error.message || t('common.operationFailed'))
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      t('common.deleteConfirm'),
      t('common.warning'),
      { type: 'warning' }
    )
    await deleteDataStandard(row.id)
    ElMessage.success(t('common.deleteSuccess'))
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete:', error)
    }
  }
}

function viewDetail(row) {
  router.push(`/admin/data-standards/${row.id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.data-standard-page {
  max-width: 1400px;
}

.pm-page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--pm-space-xl);
}

.filter-bar {
  display: flex;
  gap: var(--pm-space-md);
  margin-bottom: var(--pm-space-lg);
}

.search-input {
  width: 300px;
}

.type-tabs {
  display: flex;
  gap: var(--pm-space-sm);
  margin-bottom: var(--pm-space-lg);
  border-bottom: 1px solid var(--pm-border);
  padding-bottom: var(--pm-space-sm);
}

.type-tab {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  padding: var(--pm-space-sm) var(--pm-space-md);
  cursor: pointer;
  border-radius: var(--pm-radius-md);
  transition: all var(--pm-transition-fast);
}

.type-tab:hover {
  background: var(--pm-hover-bg);
}

.type-tab.active {
  background: var(--pm-primary);
  color: white;
}

.tab-count {
  font-size: 12px;
  padding: 2px 6px;
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
}

.type-tab:not(.active) .tab-count {
  background: var(--pm-border);
}

.code-text {
  font-family: monospace;
  color: var(--pm-primary);
}

.enum-items {
  padding: var(--pm-space-md);
  background: var(--pm-bg);
  border-radius: var(--pm-radius-md);
}

.enum-item-row {
  display: flex;
  gap: var(--pm-space-sm);
  margin-bottom: var(--pm-space-sm);
  align-items: center;
}

.add-enum-btn {
  width: 100%;
  margin-top: var(--pm-space-sm);
}
</style>
