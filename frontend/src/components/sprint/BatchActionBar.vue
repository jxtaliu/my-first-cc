<template>
  <div class="batch-action-bar">
    <span>{{ $t('project.selectedTasks', { count: selectedCount }) }}</span>
    <el-select v-model="targetSprintId" :placeholder="$t('project.selectSprint')">
      <el-option :label="$t('project.backlog')" :value="null" />
      <el-option
        v-for="sprint in sprints"
        :key="sprint.id"
        :label="sprint.name"
        :value="sprint.id"
      />
    </el-select>
    <el-button @click="emit('batch-assign', targetSprintId)">
      {{ $t('project.assignToSprint') }}
    </el-button>
    <el-button @click="emit('batch-remove')">
      {{ $t('project.removeFromSprint') }}
    </el-button>
    <el-button @click="emit('clear-selection')">
      {{ $t('project.clearSelection') }}
    </el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  selectedCount: {
    type: Number,
    required: true
  },
  sprints: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['batch-assign', 'batch-remove', 'clear-selection'])

const targetSprintId = ref(null)
</script>

<style scoped>
.batch-action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}
</style>
