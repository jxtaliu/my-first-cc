<template>
  <div class="dict-page">
    <div class="dict-layout">
      <!-- Left: Type List - Dark elegant sidebar -->
      <div class="type-panel">
        <div class="panel-header">
          <div class="header-content">
            <h3>{{ $t('admin.dictTypes') }}</h3>
            <span class="type-count">{{ typeList.length }}</span>
          </div>
          <div class="header-actions">
            <el-button type="default" size="small" circle @click="handleRefreshCache" :loading="refreshing" class="refresh-btn" :title="$t('admin.refreshCache')">
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-button type="primary" size="small" circle @click="openTypeDialog()" class="add-btn">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
        <el-scrollbar height="calc(100vh - 180px)">
          <div class="type-list">
            <div
              v-for="type in typeList"
              :key="type.id"
              class="type-card"
              :class="{ active: selectedTypeId === type.id }"
              @click="selectType(type)"
            >
              <div class="type-icon">
                <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                  <rect x="2" y="2" width="7" height="7" rx="1.5" :fill="getTypeColor(type.code)" />
                  <rect x="11" y="2" width="7" height="7" rx="1.5" :fill="getTypeColor(type.code)" opacity="0.6" />
                  <rect x="2" y="11" width="7" height="7" rx="1.5" :fill="getTypeColor(type.code)" opacity="0.6" />
                  <rect x="11" y="11" width="7" height="7" rx="1.5" :fill="getTypeColor(type.code)" opacity="0.3" />
                </svg>
              </div>
              <div class="type-content">
                <span class="type-name">{{ type.name }}</span>
                <span class="type-code">{{ type.code }}</span>
              </div>
              <div class="type-actions" @click.stop>
                <el-button text size="small" circle @click="openTypeDialog(type)" class="action-btn">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button text size="small" circle type="danger" @click="handleDeleteType(type)" class="action-btn">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <div v-if="typeList.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
                <circle cx="24" cy="24" r="20" stroke="#DCDFE6" stroke-width="2" stroke-dasharray="4 4" />
                <path d="M24 16V32M16 24H32" stroke="#DCDFE6" stroke-width="2" stroke-linecap="round" />
              </svg>
            </div>
            <p>{{ $t('admin.noDictTypes') }}</p>
          </div>
        </el-scrollbar>
      </div>

      <!-- Right: Item List - Clean content area -->
      <div class="item-panel">
        <div class="panel-header">
          <div class="header-content">
            <h3>{{ selectedType ? selectedType.name : $t('admin.selectTypeFirst') }}</h3>
            <span class="item-count" v-if="selectedType">{{ itemList.length }} {{ $t('admin.items') }}</span>
          </div>
          <el-button
            type="primary"
            size="small"
            :disabled="!selectedType"
            @click="openItemDialog()"
            class="add-btn"
          >
            <el-icon><Plus /></el-icon>
            {{ $t('admin.addItem') }}
          </el-button>
        </div>

        <div class="table-wrapper" v-if="selectedType">
          <el-table
            :data="itemList"
            v-loading="loading"
            stripe
            class="dict-table"
          >
            <el-table-column prop="code" :label="$t('admin.dictCode')" width="140">
              <template #default="{ row }">
                <div class="code-cell">
                  <span class="code-badge">{{ row.code }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="name" :label="$t('admin.dictName')" min-width="120">
              <template #default="{ row }">
                <span class="name-text">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="nameEn" :label="$t('admin.dictNameEn')" width="140">
              <template #default="{ row }">
                <span class="name-en">{{ row.nameEn || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="nameZh" :label="$t('admin.dictNameZh')" width="140">
              <template #default="{ row }">
                <span class="name-zh">{{ row.nameZh || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" :label="$t('admin.sortOrder')" width="100" align="center">
              <template #default="{ row }">
                <span class="sort-order">{{ row.sortOrder }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('admin.actions')" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <div class="action-cell">
                  <el-button text size="small" @click="openItemDialog(row)" class="edit-btn">
                    {{ $t('common.edit') }}
                  </el-button>
                  <el-button text size="small" type="danger" @click="handleDeleteItem(row)" class="delete-btn">
                    {{ $t('common.delete') }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-else class="no-selection">
          <div class="no-selection-icon">
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
              <circle cx="32" cy="32" r="28" stroke="#E4E7ED" stroke-width="2" stroke-dasharray="6 6" />
              <path d="M32 20V44M20 32H44" stroke="#E4E7ED" stroke-width="2" stroke-linecap="round" />
            </svg>
          </div>
          <p>{{ $t('admin.selectDictType') }}</p>
        </div>
      </div>
    </div>

    <!-- Type Dialog -->
    <el-dialog
      v-model="showTypeDialog"
      :title="isEditType ? $t('admin.editDictType') : $t('admin.addDictType')"
      width="460px"
      class="dict-dialog"
    >
      <el-form :model="typeForm" :rules="typeRules" ref="typeFormRef" label-position="top">
        <el-form-item :label="$t('admin.dictTypeCode')" prop="code">
          <el-input v-model="typeForm.code" :disabled="isEditType" :placeholder="$t('admin.dictTypeCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('admin.dictTypeName')" prop="name">
          <el-input v-model="typeForm.name" :placeholder="$t('admin.dictTypeNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('admin.description')">
          <el-input v-model="typeForm.description" type="textarea" :rows="3" :placeholder="$t('admin.dictTypeDescPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTypeDialog = false" class="cancel-btn">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleTypeSubmit" class="save-btn">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Item Dialog -->
    <el-dialog
      v-model="showItemDialog"
      :title="isEditItem ? $t('admin.editDictItem') : $t('admin.addDictItem')"
      width="520px"
      class="dict-dialog"
    >
      <el-form :model="itemForm" :rules="itemRules" ref="itemFormRef" label-position="top">
        <div class="form-row">
          <el-form-item :label="$t('admin.dictCode')" prop="code" class="form-col">
            <el-input v-model="itemForm.code" :disabled="isEditItem" :placeholder="$t('admin.dictCodePlaceholder')" />
          </el-form-item>
          <el-form-item :label="$t('admin.sortOrder')" prop="sortOrder" class="form-col-small">
            <el-input-number v-model="itemForm.sortOrder" :min="0" :max="9999" />
          </el-form-item>
        </div>
        <el-form-item :label="$t('admin.dictName')" prop="name">
          <el-input v-model="itemForm.name" :placeholder="$t('admin.dictNamePlaceholder')" />
        </el-form-item>
        <div class="form-row">
          <el-form-item :label="$t('admin.dictNameEn')" class="form-col">
            <el-input v-model="itemForm.nameEn" :placeholder="$t('admin.dictNameEnPlaceholder')" />
          </el-form-item>
          <el-form-item :label="$t('admin.dictNameZh')" class="form-col">
            <el-input v-model="itemForm.nameZh" :placeholder="$t('admin.dictNameZhPlaceholder')" />
          </el-form-item>
        </div>
        <el-form-item :label="$t('admin.extra')" class="extra-item">
          <div class="json-editor">
            <div class="json-editor-toolbar">
              <span class="json-status" :class="{ valid: jsonValid, invalid: !jsonValid && jsonDirty }">
                <span class="status-dot"></span>
                {{ jsonStatusText }}
              </span>
              <el-button text size="small" @click="formatJson" class="format-btn" :disabled="!jsonValid">
                {{ $t('admin.formatJson') }}
              </el-button>
            </div>
            <el-input
              v-model="itemForm.extra"
              type="textarea"
              :rows="4"
              :placeholder="$t('admin.dictExtraPlaceholder')"
              class="json-textarea"
              @input="validateJson"
              spellcheck="false"
            />
            <div class="json-error" v-if="!jsonValid && jsonDirty">
              {{ jsonError }}
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showItemDialog = false" class="cancel-btn">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleItemSubmit" class="save-btn">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import * as dictApi from '@/api/dict'

const { t } = useI18n()

const loading = ref(false)
const refreshing = ref(false)
const typeList = ref([])
const itemList = ref([])
const selectedTypeId = ref(null)
const selectedType = ref(null)
const showTypeDialog = ref(false)
const showItemDialog = ref(false)
const isEditType = ref(false)
const isEditItem = ref(false)
const typeFormRef = ref()
const itemFormRef = ref()
const jsonValid = ref(true)
const jsonDirty = ref(false)
const jsonError = ref('')

const jsonStatusText = computed(() => {
  if (!itemForm.extra || itemForm.extra.trim() === '') {
    return t('admin.jsonOptional')
  }
  if (jsonValid.value) {
    return t('admin.jsonValid')
  }
  return t('admin.jsonInvalid')
})

const typeForm = reactive({
  id: null,
  code: '',
  name: '',
  description: ''
})

const itemForm = reactive({
  id: null,
  dictTypeId: null,
  code: '',
  name: '',
  nameEn: '',
  nameZh: '',
  sortOrder: 0,
  extra: ''
})

const typeRules = {
  code: [{ required: true, message: () => t('admin.dictTypeCodeRequired') }],
  name: [{ required: true, message: () => t('admin.dictTypeNameRequired') }]
}

const itemRules = {
  code: [{ required: true, message: () => t('admin.dictCodeRequired') }],
  name: [{ required: true, message: () => t('admin.dictNameRequired') }],
  extra: [{
    validator: (rule, value, callback) => {
      if (!value || value.trim() === '') {
        callback()
        return
      }
      try {
        JSON.parse(value)
        callback()
      } catch (e) {
        callback(new Error(t('admin.invalidJson')))
      }
    },
    trigger: 'blur'
  }]
}

const validateJson = () => {
  const value = itemForm.extra
  if (!value || value.trim() === '') {
    jsonValid.value = true
    jsonDirty.value = false
    jsonError.value = ''
    return
  }
  jsonDirty.value = true
  try {
    JSON.parse(value)
    jsonValid.value = true
    jsonError.value = ''
  } catch (e) {
    jsonValid.value = false
    jsonError.value = t('admin.jsonParseError')
  }
}

const formatJson = () => {
  if (!jsonValid.value) return
  try {
    const parsed = JSON.parse(itemForm.extra)
    itemForm.extra = JSON.stringify(parsed, null, 2)
  } catch (e) {}
}

const typeColors = {
  'TASK_STATUS': '#67C23A',
  'TASK_TYPE': '#409EFF',
  'PRIORITY': '#F56C6C',
  'PROJECT_STATUS': '#E6A23C',
  'PROJECT_TYPE': '#909399',
  'default': '#409EFF'
}

const getTypeColor = (code) => {
  return typeColors[code] || typeColors['default']
}

const fetchTypes = async () => {
  try {
    const res = await dictApi.getDictTypes()
    if (res.code === 200) {
      typeList.value = res.data || []
      if (!selectedTypeId.value && typeList.value.length > 0) {
        selectType(typeList.value[0])
      }
    }
  } catch (e) {
    ElMessage.error(t('common.fetchFailed'))
  }
}

const fetchItems = async (typeId) => {
  if (!typeId) {
    itemList.value = []
    return
  }
  loading.value = true
  try {
    const res = await dictApi.getDictItems({ typeId })
    if (res.code === 200) {
      itemList.value = res.data || []
    }
  } catch (e) {
    ElMessage.error(t('common.fetchFailed'))
  } finally {
    loading.value = false
  }
}

const selectType = (type) => {
  selectedTypeId.value = type.id
  selectedType.value = type
  fetchItems(type.id)
}

const openTypeDialog = (type = null) => {
  if (type) {
    isEditType.value = true
    Object.assign(typeForm, {
      id: type.id,
      code: type.code,
      name: type.name,
      description: type.description || ''
    })
  } else {
    isEditType.value = false
    Object.assign(typeForm, {
      id: null,
      code: '',
      name: '',
      description: ''
    })
  }
  showTypeDialog.value = true
}

const openItemDialog = (item = null) => {
  if (item) {
    isEditItem.value = true
    Object.assign(itemForm, {
      id: item.id,
      dictTypeId: item.dictTypeId,
      code: item.code,
      name: item.name,
      nameEn: item.nameEn || '',
      nameZh: item.nameZh || '',
      sortOrder: item.sortOrder || 0,
      extra: item.extra || ''
    })
  } else {
    isEditItem.value = false
    Object.assign(itemForm, {
      id: null,
      dictTypeId: selectedTypeId.value,
      code: '',
      name: '',
      nameEn: '',
      nameZh: '',
      sortOrder: 0,
      extra: ''
    })
  }
  showItemDialog.value = true
}

const handleTypeSubmit = async () => {
  const valid = await typeFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEditType.value) {
      await dictApi.updateDictType(typeForm.id, {
        code: typeForm.code,
        name: typeForm.name,
        description: typeForm.description
      })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await dictApi.createDictType({
        code: typeForm.code,
        name: typeForm.name,
        description: typeForm.description
      })
      ElMessage.success(t('common.createSuccess'))
    }
    showTypeDialog.value = false
    fetchTypes()
  } catch (e) {
    ElMessage.error(e.message || t('common.operationFailed'))
  }
}

const handleDeleteType = async (type) => {
  try {
    await ElMessageBox.confirm(
      t('admin.confirmDeleteType'),
      t('common.warning'),
      { type: 'warning' }
    )
    await dictApi.deleteDictType(type.id)
    ElMessage.success(t('common.deleteSuccess'))
    if (selectedTypeId.value === type.id) {
      selectedTypeId.value = null
      selectedType.value = null
      itemList.value = []
    }
    fetchTypes()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || t('common.deleteFailed'))
    }
  }
}

const handleItemSubmit = async () => {
  const valid = await itemFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEditItem.value) {
      await dictApi.updateDictItem(itemForm.id, {
        code: itemForm.code,
        name: itemForm.name,
        nameEn: itemForm.nameEn,
        nameZh: itemForm.nameZh,
        sortOrder: itemForm.sortOrder,
        extra: itemForm.extra
      })
      ElMessage.success(t('common.updateSuccess'))
    } else {
      await dictApi.createDictItem({
        dictTypeId: selectedTypeId.value,
        code: itemForm.code,
        name: itemForm.name,
        nameEn: itemForm.nameEn,
        nameZh: itemForm.nameZh,
        sortOrder: itemForm.sortOrder,
        extra: itemForm.extra
      })
      ElMessage.success(t('common.createSuccess'))
    }
    showItemDialog.value = false
    fetchItems(selectedTypeId.value)
  } catch (e) {
    ElMessage.error(e.message || t('common.operationFailed'))
  }
}

const handleDeleteItem = async (item) => {
  try {
    await ElMessageBox.confirm(
      t('admin.confirmDeleteItem'),
      t('common.warning'),
      { type: 'warning' }
    )
    await dictApi.deleteDictItem(item.id)
    ElMessage.success(t('common.deleteSuccess'))
    fetchItems(selectedTypeId.value)
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || t('common.deleteFailed'))
    }
  }
}

const handleRefreshCache = async () => {
  refreshing.value = true
  try {
    await dictApi.refreshDictCache()
    ElMessage.success(t('admin.refreshCacheSuccess'))
    fetchTypes()
  } catch (e) {
    ElMessage.error(t('admin.refreshCacheFailed'))
  } finally {
    refreshing.value = false
  }
}

onMounted(() => {
  fetchTypes()
})
</script>

<style scoped>
.dict-page {
  padding: 24px;
  background: #F5F7FA;
  min-height: 100vh;
  box-sizing: border-box;
}

.dict-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 100px);
}

/* Type Panel - Light Sidebar */
.type-panel {
  width: 280px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #EBEEF5;
}

.type-panel .panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #EBEEF5;
}

.type-panel .header-content {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.type-panel h3 {
  margin: 0;
  color: #1D2129;
  font-size: 15px;
  font-weight: 600;
}

.type-panel .type-count {
  font-size: 12px;
  color: #909399;
  background: #F5F7FA;
  padding: 2px 8px;
  border-radius: 10px;
}

.type-panel .add-btn {
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  border: none;
  width: 28px;
  height: 28px;
  padding: 0;
}

.type-panel .add-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.refresh-btn {
  width: 28px;
  height: 28px;
  padding: 0;
  border: 1px solid #DCDFE6;
  background: #fff;
}

.refresh-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.refresh-btn.is-loading {
  border-color: #409EFF;
  color: #409EFF;
}

.type-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.type-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #FAFBFC;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.type-card:hover {
  background: #F5F7FA;
  border-color: #E4E7ED;
  transform: translateX(2px);
}

.type-card.active {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08) 0%, rgba(64, 158, 255, 0.04) 100%);
  border-color: #409EFF;
}

.type-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.type-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.type-name {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.type-code {
  color: #909399;
  font-size: 11px;
  letter-spacing: 0.5px;
}

.type-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.type-card:hover .type-actions {
  opacity: 1;
}

.type-actions .action-btn {
  color: #909399;
  width: 26px;
  height: 26px;
}

.type-actions .action-btn:hover {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #C0C4CC;
  text-align: center;
}

.empty-state p {
  margin: 16px 0 0;
  font-size: 13px;
}

/* Item Panel - Light Content */
.item-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #EBEEF5;
  min-width: 0;
}

.item-panel .panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.item-panel .header-content {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.item-panel h3 {
  margin: 0;
  color: #1D2129;
  font-size: 18px;
  font-weight: 600;
}

.item-panel .item-count {
  font-size: 13px;
  color: #909399;
  background: #F5F7FA;
  padding: 4px 10px;
  border-radius: 12px;
}

.item-panel .add-btn {
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  border: none;
  padding: 10px 20px;
  height: auto;
}

.item-panel .add-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.table-wrapper {
  flex: 1;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #EBEEF5;
}

.dict-table {
  --el-table-border-color: #EBEEF5;
  --el-table-header-bg-color: #FAFBFC;
}

.dict-table :deep(.el-table__header th) {
  background: #FAFBFC;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 0;
}

.dict-table :deep(.el-table__row) {
  transition: background-color 0.2s;
}

.dict-table :deep(.el-table__row:hover > td) {
  background: #F5F7FA;
}

.code-cell {
  display: flex;
  align-items: center;
}

.code-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  font-family: 'SF Mono', 'Fira Code', monospace;
  width: 90px;
  text-align: center;
  display: inline-block;
  box-sizing: border-box;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.name-text {
  color: #303133;
  font-weight: 500;
}

.name-en {
  color: #909399;
  font-style: italic;
}

.name-zh {
  color: #606266;
}

.sort-order {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #F5F7FA;
  border-radius: 6px;
  color: #606266;
  font-size: 13px;
  font-weight: 500;
}

.action-cell {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.edit-btn {
  color: #409EFF;
  font-size: 13px;
}

.delete-btn {
  font-size: 13px;
}

.no-selection {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  text-align: center;
}

.no-selection p {
  margin: 20px 0 0;
  font-size: 14px;
}

/* Dialog Styling */
:deep(.dict-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.dict-dialog .el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #EBEEF5;
  margin: 0;
}

:deep(.dict-dialog .el-dialog__title) {
  font-weight: 600;
  font-size: 16px;
  color: #1D2129;
}

:deep(.dict-dialog .el-dialog__body) {
  padding: 24px;
}

:deep(.dict-dialog .el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid #EBEEF5;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.dict-dialog .el-form-item__label) {
  font-weight: 500;
  color: #303133;
  padding-bottom: 8px;
}

:deep(.dict-dialog .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.dict-dialog .cancel-btn) {
  border-radius: 8px;
  padding: 10px 20px;
}

:deep(.dict-dialog .save-btn) {
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
}

:deep(.dict-dialog .save-btn:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-col {
  flex: 1;
}

.form-col-small {
  width: 120px;
  flex-shrink: 0;
}

/* JSON Editor */
.extra-item :deep(.el-form-item__content) {
  line-height: normal;
}

.json-editor {
  width: 100%;
  border: 1px solid #DCDFE6;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s;
}

.json-editor:focus-within {
  border-color: #409EFF;
}

.json-editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #FAFBFC;
  border-bottom: 1px solid #EBEEF5;
}

.json-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.json-status .status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #909399;
}

.json-status.valid {
  color: #67C23A;
}

.json-status.valid .status-dot {
  background: #67C23A;
}

.json-status.invalid {
  color: #F56C6C;
}

.json-status.invalid .status-dot {
  background: #F56C6C;
}

.format-btn {
  font-size: 12px;
  color: #409EFF;
}

.json-textarea {
  width: 100%;
}

.json-textarea :deep(.el-textarea__inner) {
  border: none;
  border-radius: 0;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  padding: 12px;
  resize: vertical;
  min-height: 100px;
  background: #fff;
}

.json-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.json-error {
  padding: 8px 12px;
  background: #FEF0F0;
  color: #F56C6C;
  font-size: 12px;
  border-top: 1px solid #FDE2E2;
}

/* Fixed column border */
:deep(.el-table .el-table__body-wrapper .el-table__fixed),
:deep(.el-table .el-table__body-wrapper .el-table__fixed-right) {
  border-left: 1px solid #EBEEF5;
}
</style>
