<template>
  <div class="dict-page">
    <div class="page-header">
      <h2>{{ $t('admin.dictManagement') }}</h2>
    </div>

    <div class="dict-layout">
      <!-- Left: Type List -->
      <div class="type-panel">
        <div class="panel-header">
          <span>{{ $t('admin.dictTypes') }}</span>
          <el-button type="primary" size="small" @click="openTypeDialog()">
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>
        <el-scrollbar height="calc(100vh - 180px)">
          <div
            v-for="type in typeList"
            :key="type.id"
            class="type-item"
            :class="{ active: selectedTypeId === type.id }"
            @click="selectType(type)"
          >
            <div class="type-info">
              <span class="type-name">{{ type.name }}</span>
              <span class="type-code">{{ type.code }}</span>
            </div>
            <div class="type-actions" @click.stop>
              <el-button text size="small" @click="openTypeDialog(type)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button text size="small" type="danger" @click="handleDeleteType(type)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <div v-if="typeList.length === 0" class="empty-tip">
            {{ $t('admin.noDictTypes') }}
          </div>
        </el-scrollbar>
      </div>

      <!-- Right: Item List -->
      <div class="item-panel">
        <div class="panel-header">
          <span>{{ selectedType ? selectedType.name : $t('admin.selectTypeFirst') }}</span>
          <el-button
            type="primary"
            size="small"
            :disabled="!selectedType"
            @click="openItemDialog()"
          >
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>
        <el-table
          :data="itemList"
          v-loading="loading"
          style="width: 100%"
          :max-height="calcTableHeight()"
        >
          <el-table-column prop="code" :label="$t('admin.dictCode')" width="150" />
          <el-table-column prop="name" :label="$t('admin.dictName')" min-width="150" />
          <el-table-column prop="nameEn" :label="$t('admin.dictNameEn')" width="150" />
          <el-table-column prop="nameZh" :label="$t('admin.dictNameZh')" width="150" />
          <el-table-column prop="sortOrder" :label="$t('admin.sortOrder')" width="100" align="center" />
          <el-table-column :label="$t('admin.actions')" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <el-button text size="small" @click="openItemDialog(row)">{{ $t('common.edit') }}</el-button>
              <el-button text size="small" type="danger" @click="handleDeleteItem(row)">{{ $t('common.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Type Dialog -->
    <el-dialog v-model="showTypeDialog" :title="isEditType ? $t('admin.editDictType') : $t('admin.addDictType')" width="400px">
      <el-form :model="typeForm" :rules="typeRules" ref="typeFormRef" label-width="80px">
        <el-form-item :label="$t('admin.dictTypeCode')" prop="code">
          <el-input v-model="typeForm.code" :disabled="isEditType" />
        </el-form-item>
        <el-form-item :label="$t('admin.dictTypeName')" prop="name">
          <el-input v-model="typeForm.name" />
        </el-form-item>
        <el-form-item :label="$t('admin.description')">
          <el-input v-model="typeForm.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTypeDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleTypeSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <!-- Item Dialog -->
    <el-dialog v-model="showItemDialog" :title="isEditItem ? $t('admin.editDictItem') : $t('admin.addDictItem')" width="500px">
      <el-form :model="itemForm" :rules="itemRules" ref="itemFormRef" label-width="100px">
        <el-form-item :label="$t('admin.dictCode')" prop="code">
          <el-input v-model="itemForm.code" :disabled="isEditItem" />
        </el-form-item>
        <el-form-item :label="$t('admin.dictName')" prop="name">
          <el-input v-model="itemForm.name" />
        </el-form-item>
        <el-form-item :label="$t('admin.dictNameEn')">
          <el-input v-model="itemForm.nameEn" />
        </el-form-item>
        <el-form-item :label="$t('admin.dictNameZh')">
          <el-input v-model="itemForm.nameZh" />
        </el-form-item>
        <el-form-item :label="$t('admin.sortOrder')" prop="sortOrder">
          <el-input-number v-model="itemForm.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item :label="$t('admin.extra')">
          <el-input v-model="itemForm.extra" type="textarea" placeholder='{"color": "#409EFF"}' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showItemDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleItemSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import * as dictApi from '@/api/dict'

const { t } = useI18n()

const loading = ref(false)
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
  name: [{ required: true, message: () => t('admin.dictNameRequired') }]
}

const fetchTypes = async () => {
  try {
    const res = await dictApi.getDictTypes()
    if (res.code === 200) {
      typeList.value = res.data || []
      // Auto-select first type if none selected
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

const calcTableHeight = () => {
  return window.innerHeight - 260
}

onMounted(() => {
  fetchTypes()
})
</script>

<style scoped>
.dict-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.dict-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 140px);
}

.type-panel {
  width: 320px;
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  flex-shrink: 0;
}

.item-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  min-width: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 500;
  font-size: 14px;
}

.type-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.type-item:hover {
  background-color: #f5f7fa;
}

.type-item.active {
  background-color: #ecf5ff;
}

.type-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.type-name {
  font-weight: 500;
  color: #303133;
}

.type-code {
  font-size: 12px;
  color: #909399;
}

.type-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.type-item:hover .type-actions {
  opacity: 1;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

:deep(.el-table .el-table__body-wrapper .el-table__fixed) {
  border-left: 1px solid #EBEEF5;
}
</style>
