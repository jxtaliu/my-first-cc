<template>
  <div
    class="sprint-lane"
    :class="{ 'is-drag-over': isDragOver }"
  >
    <div class="lane-header">
      <span class="lane-name">{{ lane.name }}</span>
      <span class="lane-task-count">{{ lane.totalCount ?? lane.taskCount }}</span>
    </div>
    <div class="lane-tasks" :class="{ 'drag-over': isDragOver }" @dragover.prevent="onLaneDragOver" @dragleave="onLaneDragLeave" @drop="onLaneDrop">
      <!-- Backlog: Tree view using el-tree (draggable to other lanes) -->
      <div v-if="isBacklog" class="task-tree" @dragover="onLaneDragOver" @drop="onLaneDrop">
        <el-tree
          ref="treeRef"
          :data="lane.tasks"
          :props="treeProps"
          node-key="id"
          :expand-on-click-node="false"
          draggable
          @node-click="handleNodeClick"
          @node-drag-start="handleDragStart"
          @node-drag-end="handleDragEnd"
          @node-drop="handleNodeDrop"
          @node-expand="handleNodeExpand"
          @node-collapse="handleNodeCollapse"
        >
          <template #default="{ data }">
            <div class="tree-node" :class="`type-${data.type?.toLowerCase()}`">
              <span class="node-icon">
                <el-icon v-if="data.type === 'EPIC'"><Folder /></el-icon>
                <el-icon v-else-if="data.type === 'FEATURE'"><FolderOpened /></el-icon>
                <el-icon v-else><Document /></el-icon>
              </span>
              <span class="node-label">{{ data.title }}</span>
            </div>
          </template>
        </el-tree>
      </div>
      <!-- Sprint lanes: Tree view showing Epic→Feature→Story→Task hierarchy (draggable) -->
      <div v-else class="task-tree" @dragover.prevent="onLaneDragOver" @dragleave="onLaneDragLeave" @drop="onLaneDrop">
        <el-tree
          ref="sprintTreeRef"
          :data="sprintLaneTreeData"
          :props="treeProps"
          node-key="id"
          :expand-on-click-node="false"
          draggable
          @node-click="handleNodeClick"
          @node-drag-start="handleDragStart"
          @node-drag-end="handleDragEnd"
          @node-drop="handleNodeDrop"
          @dragend="handleTreeDragEnd"
          @node-expand="handleNodeExpand"
          @node-collapse="handleNodeCollapse"
        >
          <template #default="{ data }">
            <div class="tree-node" :class="`type-${data.type?.toLowerCase()}`">
              <span class="node-icon">
                <el-icon v-if="data.type === 'EPIC'"><Folder /></el-icon>
                <el-icon v-else-if="data.type === 'FEATURE'"><FolderOpened /></el-icon>
                <el-icon v-else><Document /></el-icon>
              </span>
              <span class="node-label">{{ data.title }}</span>
            </div>
          </template>
        </el-tree>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { Folder, FolderOpened, Document } from '@element-plus/icons-vue'

// Use globalThis for truly shared state across all SprintLane instances
const draggingTask = globalThis.__draggingTask__ || (globalThis.__draggingTask__ = ref(null))
const dragTargetSprintId = globalThis.__dragTargetSprintId__ || (globalThis.__dragTargetSprintId__ = ref(null))

defineOptions({
  components: {}
})

const props = defineProps({
  lane: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['drop', 'task-click'])

const isDragOver = ref(false)
const treeRef = ref()
const sprintTreeRef = ref()

// Expanded keys for tree state persistence
const backlogExpandedKeys = ref([])
const sprintExpandedKeys = ref([])

// Storage key based on lane id
const STORAGE_VERSION = 'v2' // Increment to force clear old data
const getStorageKey = (type) => {
  const laneId = props.lane.id ?? 'backlog'
  return `sprint-tree-expanded-${STORAGE_VERSION}-${laneId}-${type}`
}

// Clear old localStorage keys (without version prefix)
const clearOldStorageKeys = () => {
  const keysToRemove = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && key.startsWith('sprint-tree-expanded-') && !key.includes(`-${STORAGE_VERSION}-`)) {
      keysToRemove.push(key)
    }
  }
  keysToRemove.forEach(key => localStorage.removeItem(key))
}

// Clear old keys on init
clearOldStorageKeys()

// Load expanded state from localStorage for specific lane type
const loadExpandedState = (laneType) => {
  try {
    const key = getStorageKey(laneType)
    const stored = localStorage.getItem(key)
    const parsed = stored ? JSON.parse(stored) : []
    if (laneType === 'backlog') {
      backlogExpandedKeys.value = parsed
    } else {
      sprintExpandedKeys.value = parsed
    }
  } catch (e) {
    console.warn('Failed to load tree expanded state:', e)
  }
}

// Save expanded state to localStorage for specific lane type
const saveExpandedState = (laneType) => {
  try {
    const key = getStorageKey(laneType)
    const valueToSave = laneType === 'backlog' ? backlogExpandedKeys.value : sprintExpandedKeys.value
    localStorage.setItem(key, JSON.stringify(valueToSave))
  } catch (e) {
    console.warn('Failed to save tree expanded state:', e)
  }
}

// Handle node expand
const handleNodeExpand = (data, node, vm) => {
  const key = String(data.id)
  const treeInstance = isBacklog.value ? treeRef.value : sprintTreeRef.value
  const laneType = isBacklog.value ? 'backlog' : 'sprint'

  // Add to expanded keys and save
  if (isBacklog.value) {
    if (!backlogExpandedKeys.value.includes(key)) {
      backlogExpandedKeys.value.push(key)
      saveExpandedState(laneType)
    }
  } else {
    if (!sprintExpandedKeys.value.includes(key)) {
      sprintExpandedKeys.value.push(key)
      saveExpandedState(laneType)
    }
  }

  // Ensure el-tree node is expanded (in case event fired without actual expand)
  if (treeInstance) {
    const treeNode = treeInstance.getNode(key)
    if (treeNode && !treeNode.expanded) {
      treeNode.expand()
    }
  }
}

// Handle node collapse
const handleNodeCollapse = (data, node, vm) => {
  const key = String(data.id)
  const laneType = isBacklog.value ? 'backlog' : 'sprint'

  // Update state - el-tree has already handled the visual collapse
  if (isBacklog.value) {
    backlogExpandedKeys.value = backlogExpandedKeys.value.filter(k => k !== key)
  } else {
    sprintExpandedKeys.value = sprintExpandedKeys.value.filter(k => k !== key)
  }
  saveExpandedState(laneType)
}

// Get current expanded keys based on lane type
const getCurrentExpandedKeys = () => {
  return isBacklog.value ? backlogExpandedKeys : sprintExpandedKeys
}

// Restore expanded state after data loads using tree instance methods
const restoreExpandedState = async () => {
  await nextTick()
  const expandedKeys = isBacklog.value ? backlogExpandedKeys.value : sprintExpandedKeys.value
  const treeInstance = isBacklog.value ? treeRef.value : sprintTreeRef.value

  if (!treeInstance || !expandedKeys || expandedKeys.length === 0) {
    return
  }

  // Collect all node IDs in this tree (including nested children)
  const getAllNodeIds = (nodes, ids = new Set()) => {
    for (const node of nodes) {
      ids.add(String(node.id))
      if (node.children && node.children.length > 0) {
        getAllNodeIds(node.children, ids)
      }
    }
    return ids
  }
  const validNodeIds = getAllNodeIds(treeInstance.data)

  // Use getNode + node.expand() to expand saved nodes ONLY if they exist in this tree
  for (const key of expandedKeys) {
    if (!validNodeIds.has(key)) {
      continue
    }
    const node = treeInstance.getNode(key)
    if (node && !node.expanded) {
      node.expand()
    }
  }
}

// Check if this is a backlog lane (no id)
const isBacklog = computed(() => {
  return props.lane.id === null
})

// Watch for tree data changes and restore expanded state
watch(
  () => props.lane.tasks,
  (newTasks, oldTasks) => {
    // Always restore when data changes (task moved to/from)
    if (isBacklog.value && newTasks && newTasks.length > 0) {
      // Backlog
      loadExpandedState('backlog')
      nextTick(() => restoreExpandedState())
    } else if (!isBacklog.value && newTasks && newTasks.length > 0) {
      // Sprint
      loadExpandedState('sprint')
      nextTick(() => restoreExpandedState())
    }
  },
  { immediate: true }
)

// Also watch for sprintLaneTreeData changes
watch(
  () => props.lane.allTasks,
  (newAllTasks, oldAllTasks) => {
    // Always restore when sprint data changes (task moved out)
    if (!isBacklog.value && newAllTasks && newAllTasks.length > 0) {
      loadExpandedState('sprint')
      nextTick(() => restoreExpandedState())
    }
  },
  { immediate: true }
)

// Sprint lane tasks - only show Task and Subtask (not Epic/Feature/Story)
const sprintLaneTasks = computed(() => {
  return props.lane.tasks.filter(t => ['TASK', 'SUBTASK'].includes(t.type))
})

// Sprint lane tree data - build full hierarchy using all tasks
const sprintLaneTreeData = computed(() => {
  // Get tasks that belong to this sprint
  const sprintTaskIds = new Set(props.lane.tasks.map(t => t.id))
  const allTasks = props.lane.allTasks || []

  if (allTasks.length === 0 || sprintTaskIds.size === 0) return []

  // Build task map and children map from ALL tasks
  // Use String keys to ensure consistent type comparison
  const taskMap = {}
  const childrenMap = {}

  allTasks.forEach(task => {
    const taskId = String(task.id)
    taskMap[taskId] = task
    if (task.parentId) {
      const parentIdKey = String(task.parentId)
      if (!childrenMap[parentIdKey]) {
        childrenMap[parentIdKey] = []
      }
      childrenMap[parentIdKey].push(task)
    }
  })

  // Step 1: Find all TASK/SUBTASK in this sprint
  const sprintTaskSet = new Set()
  props.lane.tasks.forEach(task => {
    if (['TASK', 'SUBTASK'].includes(task.type)) {
      sprintTaskSet.add(String(task.id))
    }
  })

  // Step 2: Find all ancestor IDs (EPIC/FEATURE/STORY) that have TASK/SUBTASK descendants
  const ancestorIds = new Set()
  const findAncestors = (taskId) => {
    const task = taskMap[taskId]
    if (!task) return
    if (task.parentId) {
      const parentIdStr = String(task.parentId)
      findAncestors(parentIdStr)
      ancestorIds.add(parentIdStr)
    }
  }
  sprintTaskSet.forEach(taskId => findAncestors(String(taskId)))

  // Step 3: Build tree - only include nodes that are ancestors or TASK/SUBTASK in sprint
  const buildTree = (parentId = null) => {
    // Find children: if parentId is null, find root nodes (parentId = null)
    // Otherwise find children with matching parentId
    let children
    if (parentId === null) {
      children = allTasks.filter(task => !task.parentId)
    } else {
      children = childrenMap[String(parentId)] || []
    }
    return children
      .filter(task => {
        const taskIdStr = String(task.id)
        // Include if it's an ancestor of sprint TASK/SUBTASK
        if (ancestorIds.has(taskIdStr)) return true
        // Include if it's a TASK/SUBTASK in this sprint
        if (sprintTaskSet.has(taskIdStr)) return true
        // Exclude everything else
        return false
      })
      .map(task => ({
        ...task,
        children: buildTree(String(task.id))
      }))
  }

  return buildTree(null)
})

// Tree props for el-tree
const treeProps = {
  children: 'children',
  label: 'title'
}

const handleNodeClick = (data) => {
  emit('task-click', data)
}

const handleDragStart = (node, event) => {
  const taskId = node.data?.id ?? node.id
  const taskType = node.data?.type ?? node.type
  const sourceSprintId = props.lane.id
  draggingTask.value = { id: taskId, type: taskType, setAt: Date.now(), sourceSprintId }
  event.dataTransfer.effectAllowed = 'move'
}

const handleDragEnd = (node, event) => {
  // Don't clear draggingTask here - let onLaneDrop handle the cleanup
}

const handleNodeDrop = (draggingNode, dropNode, position, event) => {
  // Determine actual target: use dragTargetSprintId if set (cross-tree), otherwise use props.lane.id (within-tree)
  const actualTargetSprintId = dragTargetSprintId.value ?? props.lane.id

  if (draggingTask.value) {
    const { id: taskId, type: taskType, sourceSprintId } = draggingTask.value
    // Within-tree drop: both source and target are this lane
    // Cross-tree drop: source is this lane, but target is different (dragTargetSprintId)
    const isWithinTree = (sourceSprintId === props.lane.id) && (actualTargetSprintId === props.lane.id)

    if (isWithinTree) {
      // Within-tree drop - emit drop event
      const targetSprintId = props.lane.id
      emit('drop', { taskId: String(taskId), taskType, targetSprintId, sourceSprintId })
    } else {
      // Cross-tree drop - emit with actual target from dragTargetSprintId
      // handleNodeDrop fires on SOURCE tree component
      emit('drop', { taskId: String(taskId), taskType, targetSprintId: actualTargetSprintId, sourceSprintId })
    }
  }
}

const handleTreeDragEnd = (event) => {
  // Clear drag target when drag ends
  dragTargetSprintId.value = null
}

const getTypeClass = (type) => {
  const map = {
    EPIC: 'epic',
    FEATURE: 'feature',
    STORY: 'story',
    TASK: 'task',
    SUBTASK: 'subtask'
  }
  return map[type] || 'task'
}

const getTypeLabel = (type) => {
  const map = {
    EPIC: 'E',
    FEATURE: 'F',
    STORY: 'S',
    TASK: 'T',
    SUBTASK: 'ST'
  }
  return map[type] || 'T'
}

const onLaneDragOver = (e) => {
  e.preventDefault()
  isDragOver.value = true
  dragTargetSprintId.value = props.lane.id
}

const onLaneDragLeave = (e) => {
  // Only reset if leaving the lane entirely
  if (!e.currentTarget.contains(e.relatedTarget)) {
    isDragOver.value = false
    dragTargetSprintId.value = null
  }
}

const onLaneDrop = (e) => {
  e.preventDefault()
  e.stopPropagation()
  isDragOver.value = false
  if (draggingTask.value) {
    const { id: taskId, type: taskType, sourceSprintId } = draggingTask.value
    const targetSprintId = dragTargetSprintId.value ?? props.lane.id
    emit('drop', { taskId: String(taskId), taskType, targetSprintId, sourceSprintId })
    draggingTask.value = null
    dragTargetSprintId.value = null
  }
}

const onTaskDragStart = (event, taskId) => {
  const task = props.lane.tasks.find(t => String(t.id) === String(taskId))
  event.dataTransfer.setData('taskId', String(taskId))
  if (task) {
    event.dataTransfer.setData('taskType', task.type)
  }
  event.dataTransfer.effectAllowed = 'move'
}
</script>

<style scoped>
.sprint-lane {
  height: calc(100vh - 180px);
  min-height: 400px;
  min-width: 280px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  transition: background-color 0.2s;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  flex-shrink: 0;
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
  flex: 1;
  overflow-y: auto;
  min-height: 100px;
}

.lane-tasks.drag-over {
  background-color: var(--el-color-primary-light-9);
  border: 2px dashed var(--el-color-primary);
  border-radius: 4px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-tree {
  height: 100%;
  overflow: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
}

.tree-node.type-epic {
  color: #8B5CF6;
}

.tree-node.type-feature {
  color: #3B82F6;
}

.tree-node.type-story {
  color: #10B981;
}

.tree-node.type-task {
  color: #F59E0B;
}

.tree-node.type-subtask {
  color: #6B7280;
}

.node-icon {
  display: flex;
  align-items: center;
}

.node-label {
  font-size: 13px;
}

.task-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  cursor: grab;
  flex-wrap: wrap;
}

.task-item:active {
  cursor: grabbing;
}

.task-title {
  font-size: 13px;
}

.task-desc {
  flex: 1;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.task-type {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  padding: 2px 6px;
  background-color: var(--el-fill-color);
  border-radius: 2px;
  flex-shrink: 0;
  margin-left: auto;
}
</style>
