<template>
  <div class="timesheet-page">
    <div class="page-header">
      <h2>{{ $t('nav.timesheet') }}</h2>
      <div class="header-actions">
        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button value="weekly">{{ $t('timesheet.weekly') }}</el-radio-button>
          <el-radio-button value="monthly">{{ $t('timesheet.monthly') }}</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <el-card class="stats-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">{{ $t('timesheet.totalHours') }}</div>
            <div class="stat-value">{{ stats.totalHours }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">{{ $t('timesheet.approved') }}</div>
            <div class="stat-value approved">{{ stats.approvedHours }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">{{ $t('timesheet.pending') }}</div>
            <div class="stat-value pending">{{ stats.pendingHours }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">{{ $t('timesheet.thisWeek') }}</div>
            <div class="stat-value">{{ stats.weekHours }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <div class="timesheet-content">
      <div class="calendar-header">
        <el-button text @click="navigatePrev">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <span class="current-period">{{ currentPeriodLabel }}</span>
        <el-button text @click="navigateNext">
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button type="primary" @click="goToToday">{{ $t('timesheet.today') }}</el-button>
      </div>

      <div class="calendar-grid" v-if="viewMode === 'weekly'">
        <div class="grid-header">
          <div class="header-cell project-col">{{ $t('timesheet.projectTask') }}</div>
          <div v-for="day in weekDays" :key="day.date" class="header-cell day-col">
            <div class="day-name">{{ day.name }}</div>
            <div class="day-date" :class="{ today: day.isToday }">{{ day.dateNum }}</div>
          </div>
          <div class="header-cell total-col">{{ $t('timesheet.total') }}</div>
        </div>

        <div v-for="row in timesheetRows" :key="row.id" class="grid-row">
          <div class="cell project-col">
            <div class="project-name">{{ row.projectName }}</div>
            <div class="task-name">{{ row.taskName }}</div>
          </div>
          <div v-for="day in weekDays" :key="day.date" class="cell day-col">
            <input
              v-if="row.entries[day.date]"
              type="number"
              min="0"
              max="24"
              step="0.5"
              class="hours-input"
              v-model="row.entries[day.date].hours"
              @change="handleHoursChange(row, day.date)"
            />
            <span v-else class="no-entry">-</span>
          </div>
          <div class="cell total-col">
            <span class="row-total">{{ calculateRowTotal(row) }}</span>
          </div>
        </div>

        <div class="add-row-btn" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon> {{ $t('timesheet.addEntry') }}
        </div>

        <div class="grid-footer">
          <div class="footer-label">{{ $t('timesheet.dailyTotal') }}</div>
          <div v-for="day in weekDays" :key="day.date" class="footer-cell day-col">
            <span :class="{ 'today': day.isToday }">{{ dayTotals[day.date] || 0 }}h</span>
          </div>
          <div class="footer-cell total-col">
            <strong>{{ totalHours }}h</strong>
          </div>
        </div>
      </div>

      <div class="monthly-view" v-else>
        <div class="month-grid">
          <div class="month-header">
            <div v-for="day in ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']" :key="day" class="month-header-cell">
              {{ day }}
            </div>
          </div>
          <div v-for="(week, idx) in monthWeeks" :key="idx" class="month-week">
            <div
              v-for="day in week"
              :key="day.date"
              class="month-day"
              :class="{
                'other-month': !day.currentMonth,
                'today': day.isToday,
                'has-entry': day.hasEntry
              }"
              @click="handleDayClick(day)"
            >
              <span class="day-num">{{ day.dateNum }}</span>
              <span v-if="day.totalHours" class="day-hours">{{ day.totalHours }}h</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showAddDialog" :title="$t('timesheet.addEntry')" width="500px">
      <el-form :model="entryForm" :rules="entryRules" ref="entryFormRef" label-width="120px">
        <el-form-item :label="$t('timesheet.selectProject')" prop="projectId">
          <el-select v-model="entryForm.projectId" :placeholder="$t('timesheet.selectProject')" @change="handleProjectChange">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('timesheet.selectTask')" prop="taskId">
          <el-select v-model="entryForm.taskId" :placeholder="$t('timesheet.selectTask')">
            <el-option v-for="t in tasks" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('timesheet.date')" prop="date">
          <el-date-picker v-model="entryForm.date" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('timesheet.hours')" prop="hours">
          <el-input-number v-model="entryForm.hours" :min="0" :max="24" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('timesheet.description')" prop="description">
          <el-input v-model="entryForm.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleAddEntry">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showApproveDialog" :title="$t('timesheet.approveTimesheet')" width="500px">
      <div class="approve-content">
        <p>{{ $t('timesheet.approveTimesheet') }}: <strong>{{ selectedTimesheet?.userName }}</strong></p>
        <p>{{ selectedTimesheet?.period }}</p>
        <p>{{ $t('timesheet.totalHours') }}: {{ selectedTimesheet?.totalHours }}</p>
        <el-form-item :label="$t('timesheet.description')">
          <el-input v-model="approveComment" type="textarea" rows="3" :placeholder="$t('timesheet.optional')" />
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="showApproveDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="danger" @click="handleReject">{{ $t('timesheet.reject') }}</el-button>
        <el-button type="success" @click="handleApprove">{{ $t('timesheet.approve') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Plus } from '@element-plus/icons-vue'
import { getTimesheets, createTimesheet, updateTimesheet, approveTimesheet, rejectTimesheet } from '@/api/timesheet'
import { getProjects } from '@/api/project'
import { getTasks } from '@/api/task'

const viewMode = ref('weekly')
const { t } = useI18n()
const showAddDialog = ref(false)
const showApproveDialog = ref(false)
const selectedTimesheet = ref(null)
const approveComment = ref('')

const currentDate = ref(new Date())
const timesheetRows = ref([])
const projects = ref([])
const tasks = ref([])

const entryForm = reactive({
  projectId: null,
  taskId: null,
  date: null,
  hours: 8,
  description: ''
})

const entryRules = {
  projectId: [{ required: true, message: () => t('timesheet.selectProject') }],
  taskId: [{ required: true, message: () => t('timesheet.selectTask') }],
  date: [{ required: true, message: () => t('timesheet.date') }],
  hours: [{ required: true, message: () => t('timesheet.hours') }]
}

const entryFormRef = ref()

const weekDays = computed(() => {
  const days = []
  const start = getWeekStart(currentDate.value)
  for (let i = 0; i < 7; i++) {
    const d = new Date(start)
    d.setDate(d.getDate() + i)
    days.push({
      date: d.toISOString().split('T')[0],
      name: d.toLocaleDateString('en-US', { weekday: 'short' }),
      dateNum: d.getDate(),
      isToday: isToday(d)
    })
  }
  return days
})

const currentPeriodLabel = computed(() => {
  if (viewMode.value === 'weekly') {
    const start = getWeekStart(currentDate.value)
    const end = new Date(start)
    end.setDate(end.getDate() + 6)
    return `${formatDate(start)} - ${formatDate(end)}`
  }
  return currentDate.value.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
})

const monthWeeks = computed(() => {
  const weeks = []
  const firstDay = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), 1)
  const lastDay = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth() + 1, 0)

  let currentWeek = []
  const startPadding = firstDay.getDay()

  for (let i = 0; i < startPadding; i++) {
    const d = new Date(firstDay)
    d.setDate(d.getDate() - (startPadding - i))
    currentWeek.push({ dateNum: d.getDate(), date: d.toISOString().split('T')[0], currentMonth: false, isToday: false, hasEntry: false })
  }

  for (let d = 1; d <= lastDay.getDate(); d++) {
    const date = new Date(currentDate.value.getFullYear(), currentDate.value.getMonth(), d)
    currentWeek.push({
      dateNum: d,
      date: date.toISOString().split('T')[0],
      currentMonth: true,
      isToday: isToday(date),
      hasEntry: checkDayHasEntry(date)
    })
    if (currentWeek.length === 7) {
      weeks.push(currentWeek)
      currentWeek = []
    }
  }

  if (currentWeek.length > 0) {
    while (currentWeek.length < 7) {
      const lastDate = new Date(lastDay)
      lastDate.setDate(lastDate.getDate() + currentWeek.length + 1)
      currentWeek.push({ dateNum: lastDate.getDate(), date: lastDate.toISOString().split('T')[0], currentMonth: false, isToday: false, hasEntry: false })
    }
    weeks.push(currentWeek)
  }

  return weeks
})

const dayTotals = computed(() => {
  const totals = {}
  weekDays.value.forEach(day => {
    totals[day.date] = 0
  })
  timesheetRows.value.forEach(row => {
    Object.entries(row.entries).forEach(([date, entry]) => {
      if (totals[date] !== undefined) {
        totals[date] += entry.hours || 0
      }
    })
  })
  return totals
})

const totalHours = computed(() => {
  return Object.values(dayTotals.value).reduce((sum, h) => sum + h, 0)
})

const stats = reactive({
  totalHours: 0,
  approvedHours: 0,
  pendingHours: 0,
  weekHours: 0
})

const getWeekStart = (date) => {
  const d = new Date(date)
  const day = d.getDay()
  d.setDate(d.getDate() - day)
  return d
}

const isToday = (date) => {
  const today = new Date()
  return date.toDateString() === today.toDateString()
}

const formatDate = (date) => {
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

const checkDayHasEntry = (date) => {
  const dateStr = date.toISOString().split('T')[0]
  return timesheetRows.value.some(row => row.entries[dateStr])
}

const fetchTimesheets = async () => {
  try {
    const params = viewMode.value === 'weekly'
      ? { startDate: weekDays.value[0].date, endDate: weekDays.value[6].date }
      : { month: currentDate.value.getMonth() + 1, year: currentDate.value.getFullYear() }
    const res = await getTimesheets(params)
    transformTimesheetData(res.data)
  } catch (e) {
    // Handle error
  }
}

const transformTimesheetData = (data) => {
  timesheetRows.value = data.map(entry => ({
    id: entry.id,
    projectName: entry.projectName,
    taskName: entry.taskName,
    entries: { [entry.date]: { hours: entry.hours, id: entry.id } }
  }))
}

const fetchProjects = async () => {
  try {
    const res = await getProjects({})
    projects.value = res.data || []
  } catch (e) {}
}

const fetchTasks = async (projectId) => {
  if (!projectId) return
  try {
    const res = await getTasks({ projectId })
    tasks.value = res.data || []
  } catch (e) {}
}

const handleProjectChange = (projectId) => {
  entryForm.taskId = null
  fetchTasks(projectId)
}

const handleHoursChange = async (row, date) => {
  const entry = row.entries[date]
  if (entry?.id) {
    try {
      await updateTimesheet(entry.id, { hours: entry.hours })
    } catch (e) {}
  }
}

const handleAddEntry = async () => {
  const valid = await entryFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    const data = {
      projectId: entryForm.projectId,
      taskId: entryForm.taskId,
      date: entryForm.date.toISOString().split('T')[0],
      hours: entryForm.hours,
      description: entryForm.description
    }
    await createTimesheet(data)
    ElMessage.success(t('timesheet.entryAdded'))
    showAddDialog.value = false
    fetchTimesheets()
  } catch (e) {}
}

const handleDayClick = (day) => {
  if (!day.currentMonth) return
  entryForm.date = new Date(day.date)
  showAddDialog.value = true
}

const handleApprove = async () => {
  try {
    await approveTimesheet(selectedTimesheet.value.id)
    ElMessage.success(t('timesheet.approvedSuccess'))
    showApproveDialog.value = false
  } catch (e) {}
}

const handleReject = async () => {
  try {
    await rejectTimesheet(selectedTimesheet.value.id)
    ElMessage.success(t('timesheet.rejectedSuccess'))
    showApproveDialog.value = false
  } catch (e) {}
}

const navigatePrev = () => {
  const d = new Date(currentDate.value)
  if (viewMode.value === 'weekly') {
    d.setDate(d.getDate() - 7)
  } else {
    d.setMonth(d.getMonth() - 1)
  }
  currentDate.value = d
  fetchTimesheets()
}

const navigateNext = () => {
  const d = new Date(currentDate.value)
  if (viewMode.value === 'weekly') {
    d.setDate(d.getDate() + 7)
  } else {
    d.setMonth(d.getMonth() + 1)
  }
  currentDate.value = d
  fetchTimesheets()
}

const goToToday = () => {
  currentDate.value = new Date()
  fetchTimesheets()
}

const calculateRowTotal = (row) => {
  return Object.values(row.entries).reduce((sum, e) => sum + (e.hours || 0), 0)
}

onMounted(() => {
  fetchTimesheets()
  fetchProjects()
})
</script>

<style scoped>
.timesheet-page {
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

.stats-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  color: var(--theme-text-secondary);
  font-size: 13px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
}

.stat-value.approved { color: #67c23a; }
.stat-value.pending { color: #e6a23c; }

.calendar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.current-period {
  font-weight: 600;
  min-width: 200px;
  text-align: center;
}

.calendar-grid {
  background: var(--theme-card-bg);
  border-radius: 8px;
  overflow: hidden;
}

.grid-header {
  display: flex;
  background: var(--theme-border);
  font-weight: 600;
}

.header-cell {
  padding: 12px 8px;
  text-align: center;
}

.project-col { flex: 2; min-width: 200px; }
.day-col { flex: 1; min-width: 80px; }
.total-col { flex: 0 0 80px; }

.day-name {
  font-size: 12px;
  color: var(--theme-text-secondary);
}

.day-date {
  font-size: 18px;
  font-weight: 500;
}

.day-date.today {
  background: var(--theme-primary);
  color: white;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.grid-row {
  display: flex;
  border-bottom: 1px solid var(--theme-border);
}

.cell {
  padding: 12px 8px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.project-col {
  justify-content: flex-start;
  flex-direction: column;
  align-items: flex-start;
}

.project-name {
  font-weight: 500;
}

.task-name {
  font-size: 12px;
  color: var(--theme-text-secondary);
}

.hours-input {
  width: 60px;
  padding: 4px 8px;
  border: 1px solid var(--theme-border);
  border-radius: 4px;
  text-align: center;
  background: var(--theme-bg);
  color: var(--theme-text);
}

.no-entry {
  color: var(--theme-text-secondary);
}

.row-total {
  font-weight: 600;
}

.add-row-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: var(--theme-text-secondary);
  cursor: pointer;
  transition: background 0.2s;
}

.add-row-btn:hover {
  background: var(--theme-border);
}

.grid-footer {
  display: flex;
  background: var(--theme-border);
  font-weight: 600;
}

.footer-label {
  flex: 2;
  padding: 12px 8px;
}

.footer-cell {
  flex: 1;
  padding: 12px 8px;
  text-align: center;
  min-width: 80px;
}

.footer-cell.total-col {
  flex: 0 0 80px;
}

.footer-cell .today {
  color: var(--theme-primary);
}

.monthly-view {
  background: var(--theme-card-bg);
  border-radius: 8px;
  overflow: hidden;
}

.month-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: var(--theme-border);
  font-weight: 600;
}

.month-header-cell {
  padding: 12px;
  text-align: center;
}

.month-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.month-day {
  min-height: 80px;
  padding: 8px;
  border-bottom: 1px solid var(--theme-border);
  border-right: 1px solid var(--theme-border);
  cursor: pointer;
  transition: background 0.2s;
}

.month-day:hover {
  background: var(--theme-border);
}

.month-day.other-month {
  background: var(--theme-bg);
  color: var(--theme-text-secondary);
}

.month-day.today {
  background: rgba(64, 158, 255, 0.1);
}

.month-day.has-entry {
  background: rgba(103, 194, 58, 0.1);
}

.day-num {
  font-weight: 500;
}

.day-hours {
  display: block;
  font-size: 12px;
  color: var(--theme-text-secondary);
  margin-top: 4px;
}

.approve-content p {
  margin: 8px 0;
}
</style>
