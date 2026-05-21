<template>
  <div class="departments-page">
    <div class="page-header">
        <h2>{{ $t('admin.departments') }}</h2>
        <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon> {{ $t('admin.addDepartment') }}
        </el-button>
    </div>

    <el-card>
        <el-tree :data="departmentTree" :props="{ children: 'children', label: 'name' }" node-key="id" default-expand-all>
            <template #default="{ node, data }">
                <span class="tree-node">
                    <span>{{ data.name }}</span>
                    <span class="actions">
                        <el-button text size="small" @click="handleEdit(data)">{{ $t('common.edit') }}</el-button>
                        <el-button text size="small" type="danger" @click="handleDelete(data)">{{ $t('common.delete') }}</el-button>
                    </span>
                </span>
            </template>
        </el-tree>
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
                <el-tree-select v-model="departmentForm.parentId" :data="departmentTree" :props="{ label: 'name', value: 'id' }" clearable placeholder="-- Root --" />
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const departmentTree = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const departmentForm = ref({})
const formRef = ref()

const loadDepartments = async () => {
    const res = await fetch('/api/departments/tree')
    const result = await res.json()
    if (result.code === 200) {
        departmentTree.value = result.data
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

const handleEdit = (dept) => {
    openDialog(dept)
}

const handleDelete = async (dept) => {
    const res = await fetch(`/api/departments/${dept.id}`, { method: 'DELETE' })
    const result = await res.json()
    if (result.code === 200) {
        ElMessage.success('Deleted')
        loadDepartments()
    } else {
        ElMessage.error(result.message || 'Delete failed')
    }
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
        loadDepartments()
    }
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

.tree-node {
    display: flex;
    justify-content: space-between;
    width: 100%;
    padding-right: 20px;
}
.actions {
    display: none;
}
.el-tree-node__content:hover .actions {
    display: inline;
}
</style>
