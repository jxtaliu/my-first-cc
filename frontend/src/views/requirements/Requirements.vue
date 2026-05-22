<template>
  <div class="requirements-container">
    <div class="page-header">
      <h2>{{ $t('nav.requirements') }}</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        {{ $t('common.add') }}
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item :label="$t('requirements.project')">
          <el-select v-model="filterForm.projectId" :placeholder="$t('requirements.selectProject')" clearable>
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('requirements.status')">
          <el-select v-model="filterForm.status" :placeholder="$t('requirements.selectStatus')" clearable>
            <el-option :label="$t('requirements.statusDraft')" value="DRAFT" />
            <el-option :label="$t('requirements.statusPublished')" value="PUBLISHED" />
            <el-option :label="$t('requirements.statusArchived')" value="ARCHIVED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" :label="$t('requirements.title')" min-width="200" />
      <el-table-column prop="projectName" :label="$t('requirements.project')" width="150" />
      <el-table-column prop="priority" :label="$t('requirements.priority')" width="100">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)">{{ row.priority }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('requirements.status')" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creatorName" :label="$t('requirements.creator')" width="100" />
      <el-table-column prop="createdAt" :label="$t('requirements.createTime')" width="180" />
      <el-table-column :label="$t('common.settings')" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
          <el-button link type="danger" @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const projects = ref([])

const filterForm = reactive({
  projectId: null,
  status: null
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const getPriorityType = (priority) => {
  const types = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return types[priority] || ''
}

const getStatusType = (status) => {
  const types = { DRAFT: 'info', PUBLISHED: 'success', ARCHIVED: '' }
  return types[status] || ''
}

const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取需求列表
    // const res = await requirementsApi.list({ ...filterForm, ...pagination })
    // tableData.value = res.data.list
    // pagination.total = res.data.total

    // Demo data
    tableData.value = [
      { id: 1, title: '用户登录功能', projectName: '电商网站重构', priority: 'HIGH', status: 'PUBLISHED', creatorName: 'admin', createdAt: '2026-05-20 10:00:00' },
      { id: 2, title: '商品展示模块', projectName: '电商网站重构', priority: 'MEDIUM', status: 'PUBLISHED', creatorName: 'admin', createdAt: '2026-05-19 14:30:00' },
      { id: 3, title: '购物车功能', projectName: '电商网站重构', priority: 'HIGH', status: 'DRAFT', creatorName: 'admin', createdAt: '2026-05-18 09:00:00' }
    ]
    pagination.total = 3
  } catch (e) {
    ElMessage.error(e.message || 'Fetch failed')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  filterForm.projectId = null
  filterForm.status = null
  handleSearch()
}

const handleAdd = () => {
  ElMessage.info('Add requirement - TODO')
}

const handleEdit = (row) => {
  ElMessage.info(`Edit requirement: ${row.id}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('Delete this requirement?', 'Warning', { type: 'warning' })
    ElMessage.success('Deleted')
    fetchData()
  } catch {
    // cancelled
  }
}

const handleSizeChange = () => {
  fetchData()
}

const handleCurrentChange = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.requirements-container {
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
  font-size: 18px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
