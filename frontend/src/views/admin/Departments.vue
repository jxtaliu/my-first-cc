<template>
  <div class="departments-page">
    <div class="page-header">
      <h2>{{ $t('admin.departments') }}</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> {{ $t('admin.addDepartment') }}
      </el-button>
    </div>

    <el-card>
      <el-table :data="departments" v-loading="loading" style="width: 100%">
        <el-table-column prop="departmentId" :label="$t('admin.departmentId')" width="120" />
        <el-table-column prop="name" :label="$t('admin.departmentName')" min-width="200" />
        <el-table-column prop="parentId" :label="$t('admin.parentDepartment')" width="150">
          <template #default="{ row }">
            {{ getParentName(row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" :label="$t('admin.sortOrder')" width="80" />
        <el-table-column prop="status" :label="$t('admin.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('common.active') : $t('common.inactive') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.actions')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" @click="openDialog(row)">{{ $t('common.edit') }}</el-button>
            <el-button text size="small" type="danger" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? $t('admin.editDepartment') : $t('admin.addDepartment')" width="500px">
      <el-form :model="departmentForm" ref="formRef" label-width="100px">
        <el-form-item :label="$t('admin.departmentId')" prop="departmentId">
          <el-input v-model="departmentForm.departmentId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item :label="$t('admin.departmentName')" prop="name">
          <el-input v-model="departmentForm.name" />
        </el-form-item>
        <el-form-item :label="$t('admin.parentDepartment')">
          <el-select v-model="departmentForm.parentId" clearable :placeholder="$t('admin.selectParent')" style="width: 100%">
            <el-option v-for="dept in flatDepartments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('admin.sortOrder')">
          <el-input-number v-model="departmentForm.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('admin.status')">
          <el-switch v-model="departmentForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const { t } = useI18n()

const departments = ref([])
const departmentTree = ref([])
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const departmentForm = ref({})
const formRef = ref()

const flatDepartments = computed(() => {
  const flatten = (nodes, result = []) => {
    for (const node of nodes) {
      result.push(node)
      if (node.children && node.children.length) {
        flatten(node.children, result)
      }
    }
    return result
  }
  return flatten(departmentTree.value)
})

const getParentName = (parentId) => {
  if (!parentId) return '-'
  const parent = flatDepartments.value.find(d => d.id === parentId)
  return parent ? parent.name : '-'
}

const loadDepartments = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/departments/tree')
    const result = await res.json()
    if (result.code === 200) {
      departmentTree.value = result.data
      // Flatten for table display
      const flatten = (nodes, result = []) => {
        for (const node of nodes) {
          result.push(node)
          if (node.children && node.children.length) {
            flatten(node.children, result)
          }
        }
        return result
      }
      departments.value = flatten(result.data)
    }
  } catch (e) {
    // Handle error
  } finally {
    loading.value = false
  }
}

const openDialog = (dept = null) => {
  if (dept) {
    isEdit.value = true
    departmentForm.value = { ...dept }
  } else {
    isEdit.value = false
    departmentForm.value = { status: 1, sortOrder: 0 }
  }
  showDialog.value = true
}

const handleSubmit = async () => {
  const method = isEdit.value ? 'PUT' : 'POST'
  const url = isEdit.value ? `/api/departments/${departmentForm.value.id}` : '/api/departments'
  const res = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(departmentForm.value)
  })
  const result = await res.json()
  if (result.code === 200) {
    showDialog.value = false
    ElMessage.success(isEdit.value ? t('admin.departmentUpdated') : t('admin.departmentCreated'))
    loadDepartments()
  } else {
    ElMessage.error(result.message || 'Operation failed')
  }
}

const handleDelete = async (dept) => {
  try {
    await ElMessageBox.confirm(t('admin.confirmDeleteDepartment'), t('common.warning'), { type: 'warning' })
    const res = await fetch(`/api/departments/${dept.id}`, { method: 'DELETE' })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success(t('admin.departmentDeleted'))
      loadDepartments()
    } else {
      ElMessage.error(result.message || t('admin.canNotDeleteDepartment'))
    }
  } catch (e) {}
}

onMounted(loadDepartments)
</script>

<style scoped>
.departments-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}
</style>
