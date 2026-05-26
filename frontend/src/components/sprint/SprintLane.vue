<template>
  <div
    class="sprint-lane"
    :class="{ 'is-drag-over': isDragOver }"
    @dragover.prevent="onDragOver"
    @dragleave="onDragLeave"
    @drop="onDrop"
  >
    <div class="lane-header">
      <span class="lane-name">{{ lane.name }}</span>
      <span class="lane-task-count">{{ lane.taskCount }}</span>
    </div>
    <div class="lane-tasks">
      <RecycleScroller
        class="scroller"
        :items="lane.tasks"
        :item-size="50"
        key-field="id"
        v-slot="{ item }"
      >
        <div class="task-item" draggable="true" @dragstart="onDragStart($event, item.id)">
          <el-checkbox
            :model-value="selectedTasks.includes(item.id)"
            @change="(checked) => onSelect(item.id, checked)"
          />
          <span class="task-title">{{ item.title }}</span>
          <span class="task-type">{{ item.type }}</span>
        </div>
      </RecycleScroller>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RecycleScroller } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'

defineOptions({
  components: { RecycleScroller }
})

const props = defineProps({
  lane: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['drop', 'select-task', 'select'])

const isDragOver = ref(false)
const selectedTasks = ref([])

const onDragOver = () => {
  isDragOver.value = true
}

const onDragLeave = () => {
  isDragOver.value = false
}

const onDrop = (event) => {
  isDragOver.value = false
  const taskId = event.dataTransfer.getData('taskId')
  emit('drop', { taskId, targetSprintId: props.lane.id })
}

const onDragStart = (event, taskId) => {
  event.dataTransfer.setData('taskId', taskId)
}

const onSelect = (taskId, checked) => {
  const idx = selectedTasks.value.indexOf(taskId)
  if (checked && idx < 0) {
    selectedTasks.value.push(taskId)
  } else if (!checked && idx >= 0) {
    selectedTasks.value.splice(idx, 1)
  }
  emit('select-task', taskId)
}
</script>

<style scoped>
.sprint-lane {
  min-height: 200px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  transition: background-color 0.2s;
}

.sprint-lane.is-drag-over {
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}

.lane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color);
}

.lane-name {
  font-weight: 600;
  font-size: 14px;
}

.lane-task-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.lane-tasks {
  height: 100%;
}

.scroller {
  height: 100%;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: grab;
}

.task-item:active {
  cursor: grabbing;
}

.task-title {
  flex: 1;
  font-size: 13px;
}

.task-type {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  padding: 2px 6px;
  background-color: var(--el-fill-color);
  border-radius: 2px;
}
</style>
