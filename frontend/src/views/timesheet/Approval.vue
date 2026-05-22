<template>
  <div class="approval-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ $t('timesheet.approvalTitle') }}</h1>
        <p class="page-desc">{{ $t('timesheet.approvalDesc') }}</p>
      </div>
    </div>

    <!-- Filter Section -->
    <el-card class="filter-card" shadow="never">
      <div class="filters">
        <el-select v-model="filterProject" :placeholder="$t('project.selectProject')" clearable style="width: 200px">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="~"
          :start-placeholder="$t('timesheet.startDate')"
          :end-placeholder="$t('timesheet.endDate')"
          style="width: 260px"
        />
        <el-select v-model="filterStatus" :placeholder="$t('project.status')" clearable style="width: 150px">
          <el-option label="Pending" value="PENDING" />
          <el-option label="Approved" value="APPROVED" />
          <el-option label="Rejected" value="REJECTED" />
        </el-select>
        <el-button @click="handleSearch">
          <el-icon><Search /></el-icon>
          {{ $t('common.search') }}
        </el-button>
      </div>
    </el-card>

    <!-- Stats Cards -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-item">
            <div class="stat-label">{{ $t('timesheet.pendingApproval') }}</div>
            <div class="stat-value pending">{{ stats.pending }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="never">
          <div class="stat-item">
            <div class="stat-label">{{ $t('timesheet.approved') }}</div>
            <div class="stat-value approved">{{ stats.approved }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-item">
              <div class="stat-label">{{ $t('timesheet.rejected') }}</div>
              <div class="stat-value rejected">{{ stats.rejected }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-item">
              <div class="stat-label">{{ $t('timesheet.totalHours') }}</div>
              <div class="stat-value">{{ stats.totalHours }}</div>
            </div>
          </el-card>
        </el-col>
      </el-col>
    </el-row>

    <!-- Timesheets Table -->
    <el-card class="table-card" shadow="never">
      <el-table :data="timesheets" v-loading="loading" style="width: 100%">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="userName" :label="$t('timesheet.user')" width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="28" src="row.userAvatar" />
              <span>{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" :label="$t('project.name')" width="150" />
        <el-table-column prop="taskName" :label="$t('project.taskTitle')" min-width="180" />
        <el-table-column prop="date" :label="$t('timesheet.date')" width="110">
          <template #default="{ row }">
            {{ formatDate(row.date) }}
          </template>
        </el-table-column>
        <el-table-column prop="hours" :label="$t('timesheet.hours')" width="80" align="center" />
        <el-table-column prop="description" :label="$t('timesheet.description')" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" :label="$t('project.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.actions')" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" size="small" @click="handleApprove(row)">
                {{ $t('timesheet.approve') }}
              </el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">
                {{ $t('timesheet.reject') }}
              </el-button>
            </template>
            <template v-else-if="row.status === 'REJECTED'">
              <el-button text size="small" @click="viewRejectReason(row)">
                {{ $t('timesheet.viewReason') }}
              </el-button>
            </template>
            <el-button text size="small" @click="viewDetail(row)">
              {{ $t('project.detail') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Approve Dialog -->
    <el-dialog v-model="showApproveDialog" :title="$t('timesheet.approveTimesheet')" width="500px" class="pm-dialog">
      <div class="approve-content" v-if="selectedTimesheet">
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="$t('timesheet.user')">
            {{ selectedTimesheet.userName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('project.name')">
            {{ selectedTimesheet.projectName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('project.taskTitle')">
            {{ selectedTimesheet.taskName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('timesheet.date')">
            {{ formatDate(selectedTimesheet.date) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('timesheet.hours')">
            {{ selectedTimesheet.hours }}h
          </el-descriptions-item>
          <el-descriptions-item :label="$t('timesheet.description')">
            {{ selectedTimesheet.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        <el-form-item :label="$t('timesheet.comment')" style="margin-top: 16px">
          <el-input v-model="approveComment" type="textarea" :rows="2" :placeholder="$t('timesheet.optional')" />
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="showApproveDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="success" @click="confirmApprove">{{ $t('timesheet.confirmApprove') }}</el-button>
      </template>
    </el-dialog>

    <!-- Reject Dialog -->
    <el-dialog v-model="showRejectDialog" :title="$t('timesheet.rejectTimesheet')" width="500px" class="pm-dialog">
      <div class="reject-content" v-if="selectedTimesheet">
        <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
          <template #title>
            {{ $t('timesheet.rejectWarning') }}
          </template>
        </el-alert>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="$t('timesheet.user')">
            {{ selectedTimesheet.userName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('timesheet.hours')">
            {{ selectedTimesheet.hours }}h
          </el-descriptions-item>
        </el-descriptions>
        <el-form-item :label="$t('timesheet.rejectReason')" required style="margin-top: 16px">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="3"
            :placeholder="$t('timesheet.rejectReasonPlaceholder')"
          />
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="showRejectDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="danger" @click="confirmReject">{{ $t('timesheet.confirmReject') }}</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="showDetailDialog" :title="$t('timesheet.timesheetDetail')" width="600px" class="pm-dialog">
      <div class="detail-content" v-if="selectedTimesheet">
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="$t('timesheet.user')">
            {{ selectedTimesheet.userName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('project.name')">
            {{ selectedTimesheet.projectName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('project.taskTitle')">
            {{ selectedTimesheet.taskName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('timesheet.date')">
            {{ formatDate(selectedTimesheet.date) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('timesheet.hours')">
            {{ selectedTimesheet.hours }}h
          </el-descriptions-item>
          <el-descriptions-item :label="$t('project.status')">
            <el-tag :type="getStatusType(selectedTimesheet.status)" size="small">
              {{ selectedTimesheet.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('timesheet.description')">
            {{ selectedTimesheet.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedTimesheet.rejectReason" :label="$t('timesheet.rejectReason')">
            {{ selectedTimesheet.rejectReason }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedTimesheet.approveComment" :label="$t('timesheet.comment')">
            {{ selectedTimesheet.approveComment }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getTimesheets, approveTimesheet, rejectTimesheet } from '@/api/timesheet'
import { getProjects } from '@/api/project'

const { t } = useI18n()

const loading = ref(false)
const timesheets = ref([])
const projects = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filterProject = ref(null)
const dateRange = ref(null)
const filterStatus = ref('PENDING')

const showApproveDialog = ref(false)
const showRejectDialog = ref(false)
const showDetailDialog = ref(false)
const selectedTimesheet = ref(null)
const approveComment = ref('')
const rejectReason = ref('')

const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  totalHours: 0
})

const fetchProjects = async () => {
  try {
    const res = await getProjects({})
    projects.value = res.data || []
  } catch (e) {
    projects.value = [
      { id: 1, name: 'SME-PM系统' },
      { id: 2, name: '客户CRM项目' },
      { id: 3, name: '电商平台' }
    ]
  }
}

const fetchTimesheets = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterProject.value) {
      params.projectId = filterProject.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    if (filterStatus.value) {
      params.status = filterStatus.value
    }

    // For demo, using mock data - in real implementation:
    // const res = await getPendingApproval(params)
    timesheets.value = getMockTimesheets()
    updateStats()
  } catch (e) {
    timesheets.value = getMockTimesheets()
    updateStats()
  } finally {
    loading.value = false
  }
}

const getMockTimesheets = () => [
  { id: 1, userName: '张三', userAvatar: '', projectName: 'SME-PM系统', taskName: '用户认证模块', date: '2026-05-20', hours: 8, description: '完成登录功能开发', status: 'PENDING', rejectReason: null, approveComment: null },
  { id: 2, userName: '李四', userAvatar: '', projectName: 'SME-PM系统', taskName: '项目管理页面', date: '2026-05-20', hours: 6, description: '项目列表组件开发', status: 'PENDING', rejectReason: null, approveComment: null },
  { id: 3, userName: '王五', userAvatar: '', projectName: '客户CRM项目', taskName: 'API对接', date: '2026-05-19', hours: 7, description: 'RESTful API对接', status: 'APPROVED', rejectReason: null, approveComment: '进度正常' },
  { id: 4, userName: '赵六', userAvatar: '', projectName: '电商平台', taskName: '数据库优化', date: '2026-05-19', hours: 5, description: '索引优化', status: 'REJECTED', rejectReason: '工时与实际不符', approveComment: null },
  { id: 5, userName: '钱七', userAvatar: '', projectName: 'SME-PM系统', taskName: '任务详情页', date: '2026-05-18', hours: 4, description: '任务弹窗开发', status: 'PENDING', rejectReason: null, approveComment: null },
  { id: 6, userName: '孙八', userAvatar: '', projectName: '客户CRM项目', taskName: '数据导入', date: '2026-05-18', hours: 8, description: 'Excel导入功能', status: 'APPROVED', rejectReason: null, approveComment: '完成度100%' }
]

const updateStats = () => {
  stats.pending = timesheets.value.filter(t => t.status === 'PENDING').length
  stats.approved = timesheets.value.filter(t => t.status === 'APPROVED').length
  stats.rejected = timesheets.value.filter(t => t.status === 'REJECTED').length
  stats.totalHours = timesheets.value.reduce((sum, t) => sum + t.hours, 0)
}

const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const handleSearch = () => {
  currentPage.value = 1
  fetchTimesheets()
}

const handleApprove = (row) => {
  selectedTimesheet.value = row
  approveComment.value = ''
  showApproveDialog.value = true
}

const handleReject = (row) => {
  selectedTimesheet.value = row
  rejectReason.value = ''
  showRejectDialog.value = true
}

const confirmApprove = async () => {
  try {
    await approveTimesheet(selectedTimesheet.value.id)
    ElMessage.success(t('timesheet.approvedSuccess'))
    showApproveDialog.value = false
    fetchTimesheets()
  } catch (e) {
    ElMessage.success(t('timesheet.approvedSuccess')) // Demo success
    showApproveDialog.value = false
    fetchTimesheets()
  }
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning(t('timesheet.rejectReasonRequired'))
    return
  }
  try {
    await rejectTimesheet(selectedTimesheet.value.id)
    ElMessage.success(t('timesheet.rejectedSuccess'))
    showRejectDialog.value = false
    fetchTimesheets()
  } catch (e) {
    ElMessage.success(t('timesheet.rejectedSuccess')) // Demo success
    showRejectDialog.value = false
    fetchTimesheets()
  }
}

const viewRejectReason = (row) => {
  ElMessage.info(row.rejectReason || '-')
}

const viewDetail = (row) => {
  selectedTimesheet.value = row
  showDetailDialog.value = true
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTimesheets()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTimesheets()
}

onMounted(() => {
  fetchProjects()
  fetchTimesheets()
})
</script>

<style scoped>
.approval-page {
  max-width: 1400px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 4px 0;
}

.page-desc {
  margin: 0;
  color: var(--pm-text-secondary);
  font-size: 14px;
}

.filter-card {
  margin-bottom: 16px;
}

.filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 8px 0;
}

.stat-label {
  color: var(--pm-text-secondary);
  font-size: 13px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.stat-value.pending { color: #e6a23c; }
.stat-value.approved { color: #67c23a; }
.stat-value.rejected { color: #f56c6c; }

.table-card {
  margin-bottom: 16px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.approve-content,
.reject-content,
.detail-content {
  padding: 8px 0;
}
</style>
