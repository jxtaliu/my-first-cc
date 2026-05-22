<template>
  <div class="my-tasks-page pm-page">
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('nav.myTasks') }}</h1>
    </div>

    <el-card>
      <!-- Filter bar -->
      <div class="filter-bar">
        <el-select v-model="filterStatus" :placeholder="$t('task.status')" clearable>
          <el-option label="All" value="" />
          <el-option label="To Do" value="TODO" />
          <el-option label="In Progress" value="IN_PROGRESS" />
          <el-option label="Done" value="DONE" />
        </el-select>
      </div>

      <el-table :data="filteredTasks" v-loading="loading" empty-text="$t('common.noData')">
        <el-table-column prop="title" :label="$t('task.title')" min-width="200" />
        <el-table-column prop="statusName" :label="$t('task.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.statusName)" size="small">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" :label="$t('task.priority')" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" :label="$t('task.project')" width="150" />
        <el-table-column prop="dueDate" :label="$t('task.dueDate')" width="120">
          <template #default="{ row }">
            {{ formatDate(row.dueDate) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination placeholder for future -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getTasksByAssignee } from '@/api/task'

const { t } = useI18n()
const authStore = useAuthStore()

// State
const tasks = ref([])
const loading = ref(false)
const filterStatus = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// Filtered tasks by status
const filteredTasks = computed(() => {
  if (!filterStatus.value) {
    return tasks.value
  }
  return tasks.value.filter(task => task.status === filterStatus.value)
})

// Status tag type helper
function getStatusType(status) {
  const statusMap = {
    'TODO': 'info',
    'IN_PROGRESS': 'primary',
    'DONE': 'success'
  }
  return statusMap[status] || 'info'
}

// Priority tag type helper
function getPriorityType(priority) {
  const priorityMap = {
    'P0': 'danger',
    'P1': 'warning',
    'P2': 'primary',
    'P3': 'info'
  }
  return priorityMap[priority] || 'info'
}

// Date formatter
function formatDate(date) {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleDateString()
}

// Load tasks assigned to current user
async function loadTasks() {
  if (!authStore.user?.id) {
    ElMessage.warning('User not logged in')
    return
  }

  loading.value = true
  try {
    const res = await getTasksByAssignee(authStore.user.id)
    tasks.value = res.data || res || []
    total.value = tasks.value.length
  } catch (error) {
    console.error('Failed to load tasks:', error)
    ElMessage.error('Failed to load tasks')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.my-tasks-page {
  padding: var(--pm-space-lg);
}

.filter-bar {
  margin-bottom: var(--pm-space-lg);
}

.pagination-wrapper {
  margin-top: var(--pm-space-lg);
  display: flex;
  justify-content: flex-end;
}
</style>
