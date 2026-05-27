<template>
  <div
    class="pm-task-card"
    :class="{ 'non-draggable': !isDraggable }"
    :data-status="task.status"
    :data-type="task.type"
    :draggable="isDraggable"
    @dragstart="onDragStart"
    @dragend="onDragEnd"
    @click="onClick"
  >
    <!-- Top Row: Priority + Type Badge + Title -->
    <div class="pm-task-card-top">
      <span v-if="task.priority" class="pm-priority-dot" :class="getPriorityClass(task.priority)" :title="task.priority"></span>
      <span class="pm-type-badge" :class="getTypeClass(task.type)">{{ getTypeLabel(task.type) }}</span>
      <span class="pm-task-card-title">{{ task.title }}</span>
    </div>

    <!-- Middle Row: Assignee + Estimate + Progress inline -->
    <div class="pm-task-card-middle">
      <div class="pm-task-card-assignee" v-if="task.assignee">
        <span class="pm-avatar pm-avatar-xs">{{ getAvatarText(task.assigneeName || task.assignee) }}</span>
        <span class="pm-task-card-assignee-name">{{ task.assigneeName || task.assignee }}</span>
      </div>
      <div class="pm-task-card-estimate" v-if="task.estimateHours">
        <el-icon size="12"><Clock /></el-icon>
        <span>{{ task.estimateHours }}h</span>
      </div>
      <div class="pm-task-card-progress" v-if="showProgress && (task.progress > 0 || isParentType)">
        <el-tooltip :content="subtaskTooltip" :disabled="!isParentType" placement="top">
          <div class="progress-wrapper">
            <el-progress
              :percentage="displayProgress"
              :stroke-width="3"
              :show-text="false"
              :color="getProgressColor(displayProgress)"
            />
            <span class="pm-progress-text">{{ displayProgress }}%</span>
          </div>
        </el-tooltip>
      </div>
    </div>

    <!-- Bottom Row: Comments + Attachments + Duration -->
    <div class="pm-task-card-bottom" v-if="task.commentCount || task.attachmentCount || task.inProgressTime">
      <div class="pm-task-card-info">
        <span v-if="task.commentCount" class="pm-task-card-stat">
          <el-icon size="12"><ChatLineSquare /></el-icon>
          {{ task.commentCount }}
        </span>
        <span v-if="task.attachmentCount" class="pm-task-card-stat">
          <el-icon size="12"><Paperclip /></el-icon>
          {{ task.attachmentCount }}
        </span>
      </div>
      <span v-if="task.inProgressTime" class="pm-task-card-duration">
        <el-icon size="12"><Timer /></el-icon>
        {{ formatDuration(task.inProgressTime) }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Clock, ChatLineSquare, Paperclip, Timer } from '@element-plus/icons-vue'

const props = defineProps({
  task: {
    type: Object,
    required: true,
    default: () => ({
      id: null,
      title: '',
      status: 'todo',
      type: 'task',
      priority: null,
      assignee: null,
      assigneeName: '',
      estimateHours: 0,
      actualHours: 0,
      remainingHours: 0,
      progress: 0,
      commentCount: 0,
      attachmentCount: 0,
      inProgressTime: null,
      startDate: null
    })
  },
  showProgress: {
    type: Boolean,
    default: true
  },
  isDraggable: {
    type: Boolean,
    default: true
  },
  subtaskTooltip: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['click', 'dragstart', 'dragend'])

const parentTypes = ['EPIC', 'FEATURE', 'STORY']

const isParentType = computed(() => {
  return parentTypes.includes(props.task.type)
})

const displayProgress = computed(() => {
  if (isParentType.value && props.task.progress === 0) {
    return 0
  }
  return props.task.progress || 0
})

const getPriorityClass = (priority) => {
  const map = {
    'P0': 'priority-p0',
    'P1': 'priority-p1',
    'P2': 'priority-p2',
    'P3': 'priority-p3'
  }
  return map[priority] || 'priority-p3'
}

const getTypeClass = (type) => {
  const map = {
    'epic': 'type-epic',
    'feature': 'type-feature',
    'story': 'type-story',
    'task': 'type-task',
    'bug': 'type-bug',
    'subtask': 'type-task'
  }
  return map[type?.toLowerCase()] || 'type-task'
}

const getTypeLabel = (type) => {
  const map = {
    'epic': 'E',
    'feature': 'F',
    'story': 'S',
    'task': 'T',
    'bug': 'B',
    'subtask': 'ST'
  }
  return map[type?.toLowerCase()] || type?.charAt(0) || 'T'
}

const getProgressColor = (progress) => {
  if (progress >= 100) return '#10B981'
  if (progress >= 70) return '#10B981'
  if (progress >= 30) return '#F59E0B'
  return '#EF4444'
}

const getAvatarText = (name) => {
  if (!name) return '?'
  const str = String(name)
  return str.charAt(0).toUpperCase()
}

const formatDuration = (days) => {
  if (!days) return ''
  if (days < 1) return `${Math.round(days * 24)}h`
  if (days < 30) return `${Math.round(days)}d`
  return `${Math.round(days / 30)}mo`
}

const onDragStart = (e) => {
  if (!props.isDraggable) {
    e.preventDefault()
    return
  }
  e.dataTransfer.effectAllowed = 'move'
  e.dataTransfer.setData('taskId', props.task.id)
  emit('dragstart', props.task)
}

const onDragEnd = (e) => {
  emit('dragend', props.task)
}

const onClick = () => {
  emit('click', props.task)
}
</script>

<style scoped>
.pm-task-card {
  background: var(--pm-card);
  border: 1px solid var(--pm-border);
  border-radius: var(--pm-radius-sm);
  padding: 10px 12px;
  cursor: grab;
  transition: all var(--pm-transition-fast);
  position: relative;
  border-left: 3px solid var(--pm-status-color, var(--pm-border));
}

.pm-task-card:hover {
  box-shadow: var(--pm-shadow-card-hover);
  transform: translateY(-1px);
}

.pm-task-card.dragging {
  transform: scale(1.02);
  box-shadow: var(--pm-shadow-lg);
  opacity: 0.95;
  cursor: grabbing;
}

.pm-task-card.non-draggable {
  cursor: default;
  opacity: 0.9;
}

/* Status colors for left border */
.pm-task-card[data-status="todo"] { --pm-status-color: var(--pm-status-todo, #94A3B8); }
.pm-task-card[data-status="in_progress"] { --pm-status-color: var(--pm-status-in-progress, #3B82F6); }
.pm-task-card[data-status="development"] { --pm-status-color: var(--pm-status-development, #8B5CF6); }
.pm-task-card[data-status="testing"] { --pm-status-color: var(--pm-status-testing, #F59E0B); }
.pm-task-card[data-status="done"] { --pm-status-color: var(--pm-status-done, #10B981); }
.pm-task-card[data-status="blocked"] { --pm-status-color: var(--pm-status-blocked, #EF4444); }

/* Top Row */
.pm-task-card-top {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.pm-priority-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.pm-priority-dot.priority-p0,
.pm-priority-dot.priority-p1 { background: #EF4444; }
.pm-priority-dot.priority-p2 { background: #F59E0B; }
.pm-priority-dot.priority-p3 { background: #94A3B8; }

.pm-type-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 5px;
  border-radius: 3px;
  flex-shrink: 0;
  color: white;
}

.pm-type-badge.type-epic { background: #8B5CF6; }
.pm-type-badge.type-feature { background: #3B82F6; }
.pm-type-badge.type-story { background: #10B981; }
.pm-type-badge.type-task { background: #64748B; }
.pm-type-badge.type-bug { background: #EF4444; }

.pm-task-card-title {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: var(--pm-text-primary);
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Middle Row */
.pm-task-card-middle {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--pm-text-secondary);
  margin-bottom: 6px;
}

.pm-task-card-assignee {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pm-avatar.pm-avatar-xs {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--pm-primary);
  color: white;
  font-size: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.pm-task-card-assignee-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pm-task-card-estimate {
  display: flex;
  align-items: center;
  gap: 3px;
  color: var(--pm-text-muted);
  font-size: 11px;
}

.pm-task-card-progress {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.pm-task-card-progress .el-progress {
  flex: 1;
}

.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  cursor: inherit;
}

.pm-progress-text {
  font-size: 10px;
  color: var(--pm-text-muted);
  min-width: 28px;
  text-align: right;
}

/* Bottom Row */
.pm-task-card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 6px;
  border-top: 1px solid var(--pm-border-light);
  font-size: 11px;
  color: var(--pm-text-muted);
}

.pm-task-card-info {
  display: flex;
  gap: 10px;
}

.pm-task-card-stat {
  display: flex;
  align-items: center;
  gap: 3px;
}

.pm-task-card-duration {
  display: flex;
  align-items: center;
  gap: 3px;
}
</style>
