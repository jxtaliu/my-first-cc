<template>
  <div class="gantt-view-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <div>
        <h1 class="pm-heading-1">{{ $t('project.ganttView') }}</h1>
        <p class="pm-text-small">{{ $t('project.ganttViewDesc') }}</p>
      </div>
      <div class="gantt-actions">
        <el-select v-model="swimlaneMode" placeholder="泳道" size="default" style="width: 120px">
          <el-option label="无泳道" value="none" />
          <el-option label="按负责人" value="assignee" />
          <el-option label="按优先级" value="priority" />
        </el-select>
        <el-button-group>
          <el-button :type="timeScale === 'day' ? 'primary' : 'default'" @click="timeScale = 'day'">日</el-button>
          <el-button :type="timeScale === 'week' ? 'primary' : 'default'" @click="timeScale = 'week'">周</el-button>
          <el-button :type="timeScale === 'month' ? 'primary' : 'default'" @click="timeScale = 'month'">月</el-button>
        </el-button-group>
        <el-button type="primary" @click="onToday">
          {{ $t('project.today') }}
        </el-button>
      </div>
    </div>

    <!-- Gantt Chart Container -->
    <div class="gantt-container pm-card" v-loading="loading">
      <div class="gantt-layout">
        <!-- Task List (Left) -->
        <div class="gantt-task-list">
          <div class="gantt-task-header">
            <span>{{ $t('project.taskName') }}</span>
          </div>
          <div class="gantt-task-body">
            <template v-for="(group, groupIndex) in groupedTasks" :key="groupIndex">
              <!-- Swimlane header -->
              <div v-if="swimlaneMode !== 'none'" class="gantt-swimlane-header">
                <span class="swimlane-label">{{ group.label }}</span>
              </div>
              <!-- Task rows -->
              <div
                v-for="task in group.tasks"
                :key="task.id"
                class="gantt-task-row"
                :class="{ 'has-children': task.children?.length }"
              >
                <div
                  class="gantt-task-item"
                  :style="{ paddingLeft: (task.depth * 20) + 'px' }"
                  @click="onTaskClick(task)"
                >
                  <span v-if="task.children?.length" class="gantt-expand-icon" @click.stop="toggleExpand(task.id)">
                    {{ expandedTasks.includes(task.id) ? '−' : '+' }}
                  </span>
                  <span class="gantt-task-type-icon" :class="'type-' + task.type">
                    {{ getTaskTypeIcon(task.type) }}
                  </span>
                  <span class="gantt-task-title">{{ task.title }}</span>
                </div>
              </div>
            </template>
          </div>
        </div>

        <!-- Timeline (Right) -->
        <div class="gantt-timeline" ref="timelineRef" @scroll="onTimelineScroll">
          <!-- Timeline Header -->
          <div class="gantt-timeline-header">
            <div
              v-for="period in timelinePeriods"
              :key="period.key"
              class="gantt-timeline-period"
              :style="{ width: period.width + 'px' }"
            >
              {{ period.label }}
            </div>
          </div>

          <!-- Timeline Body -->
          <div class="gantt-timeline-body" :style="{ width: timelineWidth + 'px' }">
            <!-- Today marker -->
            <div class="gantt-today-marker" :style="{ left: todayPosition + 'px' }"></div>

            <!-- Grid lines -->
            <div class="gantt-grid-lines">
              <div
                v-for="period in timelinePeriods"
                :key="'grid-' + period.key"
                class="gantt-grid-line"
                :style="{ left: period.left + 'px', width: period.width + 'px' }"
              ></div>
            </div>

            <!-- Task bars -->
            <div class="gantt-task-bars">
              <template v-for="(group, groupIndex) in groupedTasks" :key="'group-' + groupIndex">
                <!-- Swimlane background -->
                <div v-if="swimlaneMode !== 'none'" class="gantt-swimlane-row"></div>
                <div
                  v-for="task in group.tasks"
                  :key="'bar-' + task.id"
                  class="gantt-task-bar-row"
                >
                  <div
                    class="gantt-task-bar"
                    :class="['status-' + task.status, 'type-' + task.type]"
                    :style="{
                      left: getTaskBarLeft(task) + 'px',
                      width: getTaskBarWidth(task) + 'px'
                    }"
                    @click="onTaskClick(task)"
                    @mousedown="startDrag($event, task, 'resize')"
                  >
                    <span class="gantt-bar-label">{{ task.title }}</span>
                  </div>
                  <!-- Milestone marker -->
                  <div
                    v-if="task.isMilestone"
                    class="gantt-milestone-marker"
                    :style="{ left: getTaskBarLeft(task) + 'px' }"
                  >
                    🔺
                  </div>
                  <!-- Dependency arrows -->
                  <svg v-if="task.dependencies?.length" class="gantt-dependency-svg">
                    <defs>
                      <marker
                        id="arrowhead"
                        markerWidth="10"
                        markerHeight="7"
                        refX="9"
                        refY="3.5"
                        orient="auto"
                      >
                        <polygon points="0 0, 10 3.5, 0 7" fill="#6B7280" />
                      </marker>
                    </defs>
                    <line
                      v-for="dep in getDependencyLines(task)"
                      :key="dep.id"
                      :x1="dep.x1"
                      :y1="dep.y1"
                      :x2="dep.x2"
                      :y2="dep.y2"
                      stroke="#6B7280"
                      stroke-width="1.5"
                      marker-end="url(#arrowhead)"
                    />
                  </svg>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Task Detail Dialog -->
    <el-dialog
      v-model="showTaskDetail"
      :title="editingTask?.title || $t('project.taskDetail')"
      width="700px"
      class="pm-dialog"
    >
      <div class="task-detail-content" v-if="editingTask">
        <el-form :model="editingTask" label-position="top">
          <el-form-item :label="$t('project.taskTitle')">
            <el-input v-model="editingTask.title" />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="开始日期" class="form-col">
              <el-date-picker
                v-model="editingTask.startDate"
                type="date"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="截止日期" class="form-col">
              <el-date-picker
                v-model="editingTask.dueDate"
                type="date"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          <div class="form-row">
            <el-form-item :label="$t('project.priority')" class="form-col">
              <el-select v-model="editingTask.priority" style="width: 100%">
                <el-option label="P0 紧急" value="P0" />
                <el-option label="P1 高" value="P1" />
                <el-option label="P2 中" value="P2" />
                <el-option label="P3 低" value="P3" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('project.status')" class="form-col">
              <el-select v-model="editingTask.status" style="width: 100%">
                <el-option label="待办" value="todo" />
                <el-option label="进行中" value="in_progress" />
                <el-option label="已完成" value="done" />
              </el-select>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showTaskDetail = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="onSaveTask">{{ $t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { getTasksByProject, updateTask } from '@/api/task'

const { t } = useI18n()
const route = useRoute()

const timeScale = ref('week')
const expandedTasks = ref([])
const timelineRef = ref(null)
const loading = ref(false)
const showTaskDetail = ref(false)
const editingTask = ref(null)
const swimlaneMode = ref('none')

// Drag state
const isDragging = ref(false)
const dragTask = ref(null)
const dragType = ref(null)
const dragStartX = ref(0)
const originalWidth = ref(0)

const ganttTasks = ref([])

// Flatten tasks for display
const flatTasks = computed(() => {
  const result = []
  const flatten = (tasks, depth = 0) => {
    for (const task of tasks) {
      result.push({ ...task, depth })
      if (task.children && expandedTasks.value.includes(task.id)) {
        flatten(task.children, depth + 1)
      }
    }
  }
  flatten(ganttTasks.value)
  return result
})

// Group tasks by swimlane mode
const groupedTasks = computed(() => {
  if (swimlaneMode.value === 'none') {
    return [{ label: '', tasks: flatTasks.value }]
  }

  const groups = {}
  for (const task of flatTasks.value) {
    let key
    if (swimlaneMode.value === 'assignee') {
      key = task.assignee || '未分配'
    } else if (swimlaneMode.value === 'priority') {
      key = task.priority || 'P3'
    }
    if (!groups[key]) {
      groups[key] = { label: key, tasks: [] }
    }
    groups[key].tasks.push(task)
  }

  // Sort by priority or assignee name
  const sortedKeys = Object.keys(groups).sort()
  return sortedKeys.map(key => groups[key])
})

// Calculate timeline properties based on zoom level
const timelineWidth = computed(() => {
  const baseWidth = timeScale.value === 'day' ? 40 : timeScale.value === 'week' ? 100 : 300
  return 12 * baseWidth // 12 periods
})

const dayWidth = computed(() => {
  return timeScale.value === 'day' ? 40 : timeScale.value === 'week' ? 100 / 7 : 300 / 30
})

const timelinePeriods = computed(() => {
  const periods = []
  const now = new Date()
  const startOfPeriod = new Date(now)

  if (timeScale.value === 'day') {
    startOfPeriod.setDate(now.getDate() - now.getDay())
    for (let i = 0; i < 12; i++) {
      const date = new Date(startOfPeriod)
      date.setDate(startOfPeriod.getDate() + i)
      periods.push({
        key: i,
        label: `${date.getMonth() + 1}/${date.getDate()}`,
        left: i * 40,
        width: 40
      })
    }
  } else if (timeScale.value === 'week') {
    startOfPeriod.setDate(now.getDate() - now.getDay())
    for (let i = 0; i < 12; i++) {
      const date = new Date(startOfPeriod)
      date.setDate(startOfPeriod.getDate() + i * 7)
      periods.push({
        key: i,
        label: `${date.getMonth() + 1}/${date.getDate()}`,
        left: i * 100,
        width: 100
      })
    }
  } else {
    startOfPeriod.setDate(1)
    startOfPeriod.setMonth(now.getMonth() - 6)
    for (let i = 0; i < 12; i++) {
      const date = new Date(startOfPeriod)
      date.setMonth(startOfPeriod.getMonth() + i)
      periods.push({
        key: i,
        label: `${date.getFullYear()}/${date.getMonth() + 1}`,
        left: i * 300,
        width: 300
      })
    }
  }
  return periods
})

const todayPosition = computed(() => {
  const now = new Date()
  const startOfWeek = new Date(now)
  startOfWeek.setDate(now.getDate() - now.getDay())
  const daysDiff = Math.floor((now - startOfWeek) / (24 * 60 * 60 * 1000))
  return daysDiff * dayWidth.value + 50 // 50 is offset
})

const getTaskTypeIcon = (type) => {
  const icons = {
    epic: '📦',
    feature: '🧩',
    story: '📋',
    task: '✅',
    bug: '🐛',
    milestone: '🔺'
  }
  return icons[type] || '📋'
}

const getTaskBarLeft = (task) => {
  const now = new Date()
  const startOfWeek = new Date(now)
  startOfWeek.setDate(now.getDate() - now.getDay())

  const start = task.startDate ? new Date(task.startDate) : now
  const daysDiff = Math.floor((start - startOfWeek) / (24 * 60 * 60 * 1000))
  return Math.max(0, daysDiff * dayWidth.value + 50)
}

const getTaskBarWidth = (task) => {
  if (task.isMilestone) return 16

  const start = task.startDate ? new Date(task.startDate) : new Date()
  const end = task.dueDate ? new Date(task.dueDate) : new Date()
  const daysDiff = Math.ceil((end - start) / (24 * 60 * 60 * 1000))
  return Math.max(20, daysDiff * dayWidth.value)
}

const getDependencyLines = (task) => {
  if (!task.dependencies?.length) return []

  const lines = []
  // Find the dependent task's position
  const allTasks = flatTasks.value
  const taskIndex = allTasks.findIndex(t => t.id === task.id)
  const taskTop = taskIndex * 40 + 20 // 40px row height

  for (const depId of task.dependencies) {
    const depTask = allTasks.find(t => t.id === depId)
    if (depTask) {
      const depIndex = allTasks.findIndex(t => t.id === depId)
      const depTop = depIndex * 40 + 20
      const depLeft = getTaskBarLeft(depTask) + getTaskBarWidth(depTask)

      lines.push({
        id: depId,
        x1: depLeft,
        y1: depTop,
        x2: getTaskBarLeft(task),
        y2: taskTop
      })
    }
  }
  return lines
}

const toggleExpand = (taskId) => {
  const idx = expandedTasks.value.indexOf(taskId)
  if (idx >= 0) {
    expandedTasks.value.splice(idx, 1)
  } else {
    expandedTasks.value.push(taskId)
  }
}

const onTaskClick = (task) => {
  editingTask.value = { ...task }
  showTaskDetail.value = true
}

const onSaveTask = async () => {
  if (!editingTask.value) return

  try {
    const updateData = {
      title: editingTask.value.title,
      startDate: editingTask.value.startDate,
      dueDate: editingTask.value.dueDate,
      priority: editingTask.value.priority,
      status: editingTask.value.status
    }
    await updateTask(editingTask.value.id, updateData)

    // Update local task
    const task = flatTasks.value.find(t => t.id === editingTask.value.id)
    if (task) {
      Object.assign(task, editingTask.value)
    }

    ElMessage.success(t('project.taskUpdated'))
    showTaskDetail.value = false
  } catch (error) {
    console.error('Failed to update task:', error)
    ElMessage.error('Failed to update task')
  }
}

const startDrag = (e, task, type) => {
  if (type === 'resize') {
    isDragging.value = true
    dragTask.value = task
    dragType.value = type
    dragStartX.value = e.clientX
    originalWidth.value = getTaskBarWidth(task)

    document.addEventListener('mousemove', onDrag)
    document.addEventListener('mouseup', stopDrag)
  }
}

const onDrag = (e) => {
  if (!isDragging.value || !dragTask.value) return

  const dx = e.clientX - dragStartX.value
  const newWidth = Math.max(20, originalWidth.value + dx)
  dragTask.value._tempWidth = newWidth
}

const stopDrag = async (e) => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)

  if (!isDragging.value || !dragTask.value) return

  isDragging.value = false

  // Calculate new due date based on width change
  const newWidth = dragTask.value._tempWidth || originalWidth.value
  const widthDiff = newWidth - originalWidth.value
  const daysDiff = Math.round(widthDiff / dayWidth.value)

  if (daysDiff !== 0 && dragTask.value.dueDate) {
    const newDueDate = new Date(dragTask.value.dueDate)
    newDueDate.setDate(newDueDate.getDate() + daysDiff)
    dragTask.value.dueDate = newDueDate

    try {
      await updateTask(dragTask.value.id, { dueDate: newDueDate })
      ElMessage.success('Task due date updated')
    } catch (error) {
      console.error('Failed to update due date:', error)
    }
  }

  dragTask.value._tempWidth = null
  dragTask.value = null
  dragType.value = null
}

const onTimelineScroll = (e) => {
  // Sync scroll with task list if needed
}

const onToday = () => {
  // Scroll to today position
  if (timelineRef.value) {
    timelineRef.value.scrollLeft = todayPosition.value - 100
  }
  ElMessage.success(t('project.jumpedToToday'))
}

// Load tasks from API
async function loadTasks() {
  const projectId = route.params.id
  if (!projectId) {
    // Use mock data for demo
    loadMockTasks()
    return
  }

  loading.value = true
  try {
    const res = await getTasksByProject(projectId)
    const tasks = res.data || res || []
    ganttTasks.value = tasks.map(task => ({
      ...task,
      startDate: task.startDate ? new Date(task.startDate) : new Date(),
      dueDate: task.dueDate ? new Date(task.dueDate) : new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      depth: 0,
      children: task.children || []
    }))
  } catch (error) {
    console.error('Failed to load tasks:', error)
    loadMockTasks()
  } finally {
    loading.value = false
  }
}

function loadMockTasks() {
  ganttTasks.value = [
    {
      id: 1,
      title: '用户认证模块',
      type: 'epic',
      status: 'in_progress',
      depth: 0,
      startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
      dueDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000),
      children: [
        {
          id: 11,
          title: '登录功能',
          type: 'feature',
          status: 'done',
          depth: 1,
          startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
          dueDate: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
          children: [
            {
              id: 111,
              title: '登录API开发',
              type: 'task',
              status: 'done',
              depth: 2,
              startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
              dueDate: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000)
            },
            {
              id: 112,
              title: '登录页面开发',
              type: 'task',
              status: 'done',
              depth: 2,
              startDate: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
              dueDate: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000)
            }
          ]
        },
        {
          id: 12,
          title: '注册功能',
          type: 'feature',
          status: 'in_progress',
          depth: 1,
          startDate: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
          dueDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
        },
        {
          id: 13,
          title: 'OAuth集成',
          type: 'feature',
          status: 'todo',
          depth: 1,
          startDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
          dueDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000)
        }
      ]
    },
    {
      id: 2,
      title: '里程碑: Alpha版本',
      type: 'milestone',
      status: 'todo',
      depth: 0,
      isMilestone: true,
      startDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000),
      dueDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000)
    },
    {
      id: 3,
      title: '权限管理模块',
      type: 'epic',
      status: 'todo',
      depth: 0,
      startDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      dueDate: new Date(Date.now() + 21 * 24 * 60 * 60 * 1000)
    }
  ]
}

onMounted(() => {
  loadTasks()
  expandedTasks.value = [1] // Expand first epic by default
})
</script>

<style scoped>
.gantt-view-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.gantt-actions {
  display: flex;
  gap: var(--pm-space-md);
}

.gantt-container {
  flex: 1;
  overflow: hidden;
  margin-top: var(--pm-space-lg);
}

.gantt-layout {
  display: flex;
  height: 100%;
}

.gantt-task-list {
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid var(--pm-border);
  display: flex;
  flex-direction: column;
}

.gantt-task-header {
  height: 48px;
  display: flex;
  align-items: center;
  padding: 0 var(--pm-space-lg);
  background: #FAFBFC;
  border-bottom: 1px solid var(--pm-border);
  font-weight: 600;
  font-size: 13px;
  color: var(--pm-text-secondary);
}

.gantt-task-body {
  flex: 1;
  overflow-y: auto;
}

.gantt-swimlane-header {
  height: 32px;
  display: flex;
  align-items: center;
  padding: 0 var(--pm-space-lg);
  background: var(--pm-border-light);
  font-weight: 600;
  font-size: 12px;
  color: var(--pm-text-secondary);
  border-bottom: 1px solid var(--pm-border);
}

.swimlane-label {
  color: var(--pm-primary);
}

.gantt-task-row {
  height: 40px;
  border-bottom: 1px solid var(--pm-border-light);
}

.gantt-task-item {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 var(--pm-space-lg);
  cursor: pointer;
  transition: background var(--pm-transition-fast);
}

.gantt-task-item:hover {
  background: var(--pm-background);
}

.gantt-expand-icon {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--pm-space-xs);
  font-weight: 600;
  color: var(--pm-text-muted);
  cursor: pointer;
}

.gantt-task-type-icon {
  margin-right: var(--pm-space-sm);
  font-size: 14px;
}

.gantt-task-title {
  font-size: 13px;
  color: var(--pm-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gantt-timeline {
  flex: 1;
  overflow-x: auto;
  position: relative;
}

.gantt-timeline-header {
  height: 48px;
  display: flex;
  background: #FAFBFC;
  border-bottom: 1px solid var(--pm-border);
  position: sticky;
  top: 0;
  z-index: 5;
}

.gantt-timeline-period {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--pm-text-secondary);
  border-right: 1px solid var(--pm-border-light);
}

.gantt-timeline-body {
  position: relative;
  min-height: calc(100% - 48px);
}

.gantt-today-marker {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--pm-status-blocked);
  z-index: 10;
}

.gantt-today-marker::before {
  content: '今天';
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: var(--pm-status-blocked);
  white-space: nowrap;
}

.gantt-grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
}

.gantt-grid-line {
  height: 100%;
  border-right: 1px solid var(--pm-border-light);
}

.gantt-task-bars {
  padding-top: var(--pm-space-md);
}

.gantt-task-bar-row {
  height: 40px;
  position: relative;
  border-bottom: 1px solid var(--pm-border-light);
}

.gantt-swimlane-row {
  height: 32px;
  background: var(--pm-border-light);
  border-bottom: 1px solid var(--pm-border);
}

.gantt-task-bar {
  position: absolute;
  height: 28px;
  top: 6px;
  border-radius: var(--pm-radius-sm);
  display: flex;
  align-items: center;
  padding: 0 var(--pm-space-sm);
  cursor: pointer;
  transition: all var(--pm-transition-fast);
  min-width: 20px;
}

.gantt-task-bar:hover {
  transform: scaleY(1.1);
  box-shadow: var(--pm-shadow-md);
}

.gantt-bar-label {
  font-size: 11px;
  color: white;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gantt-milestone-marker {
  position: absolute;
  top: 12px;
  font-size: 12px;
  z-index: 5;
}

.gantt-dependency-svg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 8;
}

/* Status colors */
.gantt-task-bar.status-todo {
  background: linear-gradient(90deg, var(--pm-status-todo), #B8C4CE);
}

.gantt-task-bar.status-in_progress {
  background: linear-gradient(90deg, var(--pm-status-in-progress), #60A3FA);
}

.gantt-task-bar.status-done {
  background: linear-gradient(90deg, var(--pm-status-done), #34D399);
}

.gantt-task-bar.status-blocked {
  background: linear-gradient(90deg, var(--pm-status-blocked), #F87171);
}

/* Type colors */
.gantt-task-bar.type-epic {
  background: linear-gradient(90deg, var(--pm-type-epic), #A78BFA);
}

.gantt-task-bar.type-feature {
  background: linear-gradient(90deg, var(--pm-type-feature), #60A5FA);
}

.gantt-task-bar.type-milestone {
  background: linear-gradient(90deg, var(--pm-priority-p1), #FBBF24);
  border-radius: 50%;
  width: 16px !important;
  height: 16px !important;
  top: 12px;
}

.task-detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.form-row {
  display: flex;
  gap: var(--pm-space-lg);
}

.form-col {
  flex: 1;
}
</style>
