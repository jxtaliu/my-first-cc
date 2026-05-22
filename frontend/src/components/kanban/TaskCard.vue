<template>
  <div
    class="pm-task-card"
    :data-status="task.status"
    :data-type="task.type"
    draggable="true"
    @dragstart="onDragStart"
    @dragend="onDragEnd"
    @click="onClick"
  >
    <!-- Header: Priority + Title -->
    <div class="pm-task-card-header">
      <span v-if="task.priority" class="pm-priority-badge" :class="getPriorityClass(task.priority)">
        {{ task.priority }}
      </span>
      <span class="pm-task-card-title">{{ task.title }}</span>
    </div>

    <!-- Type Tags -->
    <div class="pm-task-card-tags" v-if="task.type">
      <span class="pm-tag" :class="getTypeClass(task.type)">
        {{ getTypeLabel(task.type) }}
      </span>
    </div>

    <!-- Meta: Assignee + Estimate -->
    <div class="pm-task-card-meta">
      <div class="pm-task-card-assignee" v-if="task.assignee">
        <span class="pm-avatar pm-avatar-sm">
          {{ getAvatarText(task.assignee) }}
        </span>
        <span class="pm-task-card-assignee-name">{{ task.assigneeName || task.assignee }}</span>
      </div>
      <div class="pm-task-card-estimate" v-if="task.estimateHours">
        <span class="pm-text-small">⏱ {{ task.estimateHours }}h</span>
      </div>
    </div>

    <!-- Progress -->
    <div class="pm-task-card-progress" v-if="showProgress">
      <div class="pm-progress">
        <div
          class="pm-progress-bar"
          :class="getProgressClass(task.progress)"
          :style="{ width: (task.progress || 0) + '%' }"
        ></div>
      </div>
      <span class="pm-text-small">{{ task.progress || 0 }}%</span>
    </div>

    <!-- Footer: Comments + Duration -->
    <div class="pm-task-card-footer">
      <div class="pm-task-card-info">
        <span v-if="task.commentCount" class="pm-task-card-comments">
          💬 {{ task.commentCount }}
        </span>
        <span v-if="task.attachmentCount" class="pm-task-card-attachments">
          📎 {{ task.attachmentCount }}
        </span>
      </div>
      <span v-if="task.inProgressTime" class="pm-task-card-duration">
        ⏳ {{ formatDuration(task.inProgressTime) }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

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
  }
})

const emit = defineEmits(['click', 'dragstart', 'dragend'])

const getPriorityClass = (priority) => {
  const map = {
    'P0': 'pm-priority-p0',
    'P1': 'pm-priority-p1',
    'P2': 'pm-priority-p2',
    'P3': 'pm-priority-p3'
  }
  return map[priority] || 'pm-priority-p3'
}

const getTypeClass = (type) => {
  const map = {
    'epic': 'pm-tag-epic',
    'feature': 'pm-tag-feature',
    'story': 'pm-tag-story',
    'task': 'pm-tag-task',
    'bug': 'pm-tag-bug',
    'subtask': 'pm-tag-task'
  }
  return map[type] || 'pm-tag-task'
}

const getTypeLabel = (type) => {
  const map = {
    'epic': 'Epic',
    'feature': 'Feature',
    'story': 'Story',
    'task': 'Task',
    'bug': 'Bug',
    'subtask': 'Sub-task'
  }
  return map[type] || type
}

const getProgressClass = (progress) => {
  if (progress >= 100) return 'success'
  if (progress >= 70) return 'success'
  if (progress >= 30) return 'warning'
  return 'danger'
}

const getAvatarText = (name) => {
  if (!name) return '?'
  const str = String(name)
  return str.charAt(0).toUpperCase()
}

const formatDuration = (days) => {
  if (!days) return ''
  if (days < 1) return `${Math.round(days * 24)}h`
  if (days < 30) return `${Math.round(days)}天`
  return `${Math.round(days / 30)}月`
}

const onDragStart = (e) => {
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
  border-radius: var(--pm-radius-md);
  padding: var(--pm-space-lg);
  cursor: grab;
  transition: all var(--pm-transition-normal);
  position: relative;
  border-left: 4px solid var(--pm-status-color, var(--pm-status-todo));
}

.pm-task-card:hover {
  box-shadow: var(--pm-shadow-card-hover);
  transform: translateY(-2px);
}

.pm-task-card.dragging {
  transform: scale(1.02);
  box-shadow: var(--pm-shadow-lg);
  opacity: 0.95;
  cursor: grabbing;
}

/* Status colors */
.pm-task-card[data-status="todo"] {
  --pm-status-color: var(--pm-status-todo);
}

.pm-task-card[data-status="in_progress"] {
  --pm-status-color: var(--pm-status-in-progress);
}

.pm-task-card[data-status="development"] {
  --pm-status-color: var(--pm-status-development);
}

.pm-task-card[data-status="testing"] {
  --pm-status-color: var(--pm-status-testing);
}

.pm-task-card[data-status="done"] {
  --pm-status-color: var(--pm-status-done);
}

.pm-task-card[data-status="blocked"] {
  --pm-status-color: var(--pm-status-blocked);
}

.pm-task-card-header {
  display: flex;
  align-items: flex-start;
  gap: var(--pm-space-sm);
  margin-bottom: var(--pm-space-sm);
}

.pm-task-card-title {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--pm-text-primary);
  line-height: 1.4;
  word-break: break-word;
}

.pm-task-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--pm-space-xs);
  margin-bottom: var(--pm-space-md);
}

.pm-task-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pm-space-md);
}

.pm-task-card-assignee {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.pm-task-card-assignee-name {
  font-size: 13px;
  color: var(--pm-text-secondary);
}

.pm-task-card-progress {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
  margin-bottom: var(--pm-space-md);
}

.pm-task-card-progress .pm-progress {
  flex: 1;
}

.pm-task-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--pm-space-md);
  border-top: 1px solid var(--pm-border-light);
}

.pm-task-card-info {
  display: flex;
  gap: var(--pm-space-md);
  font-size: 12px;
  color: var(--pm-text-muted);
}

.pm-task-card-duration {
  font-size: 12px;
  color: var(--pm-text-muted);
}
</style>
