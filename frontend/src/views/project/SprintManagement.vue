<template>
  <div class="sprint-management-page pm-page">
    <!-- Page Header -->
    <div class="pm-page-header">
      <h1 class="pm-heading-1">{{ $t('project.sprintManagement') }}</h1>
      <div class="header-right">
        <div class="filters">
          <el-input v-model="searchQuery" :placeholder="$t('project.searchTasks')" />
          <el-select v-model="filterType" clearable>
            <el-option :label="$t('project.task')" value="TASK" />
            <el-option :label="$t('project.story')" value="STORY" />
            <el-option :label="$t('project.bug')" value="BUG" />
            <el-option :label="$t('project.epic')" value="EPIC" />
          </el-select>
          <el-select v-model="filterPriority" clearable>
            <el-option :label="$t('project.p0')" value="P0" />
            <el-option :label="$t('project.p1')" value="P1" />
            <el-option :label="$t('project.p2')" value="P2" />
            <el-option :label="$t('project.p3')" value="P3" />
          </el-select>
        </div>
      </div>
    </div>

    <!-- Lanes Container -->
    <div class="lanes-container" v-loading="loading">
      <SprintLane
        v-for="lane in lanes"
        :key="lane.id ?? 'backlog'"
        :lane="lane"
        :selected-tasks="selectedTasks"
        @task-select="onTaskSelect"
        @task-click="onTaskClick"
        @drop="onDropTask"
      />
    </div>

    <!-- Batch Action Bar -->
    <BatchActionBar
      v-if="selectedTasks.length > 0"
      :selected-count="selectedTasks.length"
      :sprints="sprints"
      @batch-assign="onBatchAssignSprint"
      @batch-remove="onBatchRemove"
      @clear-selection="onClearSelection"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import SprintLane from '@/components/sprint/SprintLane.vue'
import BatchActionBar from '@/components/sprint/BatchActionBar.vue'
import { getSprints, batchAssignTasks, batchRemoveTasks } from '@/api/project'
import { getTasksByProject, moveTask } from '@/api/task'
import { getRequirementTree } from '@/api/requirements'
import { useProjectStore } from '@/stores/project'

const { t } = useI18n()
const route = useRoute()
const projectStore = useProjectStore()

// Refs
const sprints = ref([])
const tasks = ref([])
const backlogTree = ref([])  // Tree structure for backlog
const selectedTasks = ref([])
const loading = ref(false)
const searchQuery = ref('')
const filterType = ref(null)
const filterPriority = ref(null)
let isProcessingDrop = false  // Prevent duplicate drop processing

// Lanes computed - backlog lane first (sprintId=null), then sprint lanes
const lanes = computed(() => {
  const filterTasks = (taskList) => {
    return taskList.filter(t => {
      if (searchQuery.value) {
        const q = searchQuery.value.toLowerCase()
        if (!t.title?.toLowerCase().includes(q) && !t.description?.toLowerCase().includes(q)) {
          return false
        }
      }
      if (filterType.value && t.type !== filterType.value) return false
      if (filterPriority.value && t.priority !== filterPriority.value) return false
      return true
    })
  }

  // Backlog uses the tree structure from getRequirementTree
  // Filter out tasks that are assigned to any sprint
  const sprintTaskIds = new Set(tasks.value.filter(t => t.sprintId).map(t => t.id))
  const filteredBacklogTree = filterBacklogBySprintId(JSON.parse(JSON.stringify(backlogTree.value)), sprintTaskIds)
  const backlogTasks = filterTasks(tasks.value.filter(task => !task.sprintId))
  const backlogLane = {
    id: null,
    name: t('project.backlog'),
    status: 'backlog',
    taskCount: backlogTasks.length,  // Count of unassigned tasks (filtered)
    totalCount: tasks.value.filter(task => !task.sprintId).length,  // Total unassigned tasks
    tasks: filteredBacklogTree,  // Use filtered tree structure for backlog display
    capacityHours: 0
  }

  const sprintLanes = sprints.value.map(sprint => {
    const sprintId = Number(sprint.id)
    const allSprintTasks = tasks.value.filter(task => Number(task.sprintId) === sprintId)
    const sprintTasks = filterTasks(allSprintTasks)
    return {
      id: sprint.id,
      name: sprint.name,
      status: sprint.status,
      taskCount: sprintTasks.length,  // Filtered count (shown in header)
      totalCount: allSprintTasks.length,  // Total tasks in sprint
      tasks: sprintTasks,
      allTasks: tasks.value,  // Pass ALL project tasks to build full hierarchy (Epic→Feature→Story→Task)
      capacityHours: sprint.capacityHours || 0
    }
  })

  return [backlogLane, ...sprintLanes]
})

// Load data using projectStore.currentProjectId
async function loadData() {
  const projectId = projectStore.currentProjectId
  if (!projectId) {
    return
  }

  loading.value = true
  try {
    // Load sprints
    const sprintsRes = await getSprints(projectId)
    sprints.value = sprintsRes.data || sprintsRes || []

    // Load backlog tree using getRequirementTree (returns proper hierarchy)
    const treeRes = await getRequirementTree(projectId)
    const rawTree = treeRes.data || []
    // Filter out invalid leaf nodes (EPIC/FEATURE/STORY that have no TASK/SUBTASK descendants)
    backlogTree.value = filterInvalidLeaves(rawTree)

    // Load flat tasks for sprint lanes
    const tasksRes = await getTasksByProject(projectId)
    tasks.value = tasksRes.data || tasksRes || []
  } catch (error) {
    console.error('Failed to load data:', error)
    ElMessage.error(t('project.loadProjectFailed'))
  } finally {
    loading.value = false
  }
}

// Event handlers
const onCreateSprint = () => {
  ElMessage.info(t('project.createSprint'))
}

const onTaskSelect = (taskId, isSelected) => {
  if (isSelected) {
    if (!selectedTasks.value.includes(taskId)) {
      selectedTasks.value.push(taskId)
    }
  } else {
    selectedTasks.value = selectedTasks.value.filter(id => id !== taskId)
  }
}

const onTaskClick = (task) => {
  ElMessage.info(`Task clicked: ${task.title}`)
}

const onBatchAssignSprint = async (targetSprintId) => {
  try {
    await batchAssignTasks(targetSprintId, selectedTasks.value)
    selectedTasks.value.forEach(id => {
      const task = tasks.value.find(t => t.id === id)
      if (task) task.sprintId = targetSprintId
    })
    selectedTasks.value = []
    ElMessage.success(t('project.tasksAssigned'))
  } catch (e) {
    ElMessage.error(t('project.assignFailed'))
  }
}

const onClearSelection = () => {
  selectedTasks.value = []
}

const onBatchRemove = async () => {
  const firstTask = tasks.value.find(t => selectedTasks.value.includes(t.id))
  if (!firstTask?.sprintId) return

  try {
    await batchRemoveTasks(firstTask.sprintId, selectedTasks.value)
    selectedTasks.value.forEach(id => {
      const task = tasks.value.find(t => t.id === id)
      if (task) task.sprintId = null
    })
    selectedTasks.value = []
    ElMessage.success(t('project.tasksRemoved'))
  } catch (e) {
    ElMessage.error(t('project.removeFailed'))
  }
}

const onDropTask = async ({ taskId, taskType, targetSprintId }) => {
  // Prevent duplicate processing
  if (isProcessingDrop) {
    return
  }
  isProcessingDrop = true

  try {
    const task = tasks.value.find(t => String(t.id) === String(taskId))
    if (!task) {
      isProcessingDrop = false
      return
    }

    // Epic/Feature/Story: move all descendant tasks, but don't change parent sprint
    if (['EPIC', 'FEATURE', 'STORY'].includes(taskType)) {
      // Get source sprint ID from global dragging task (saved at drag start in SprintLane)
      const draggingTaskInfo = globalThis.__draggingTask__?.value
      const sourceSprintId = draggingTaskInfo?.sourceSprintId ?? task.sprintId
      const descendantTaskIds = getDescendantIds(task)

      // Filter to TASK/SUBTASK that are in the source lane
      const descendants = tasks.value.filter(t =>
        descendantTaskIds.includes(t.id) &&
        ['TASK', 'SUBTASK'].includes(t.type) &&
        (sourceSprintId == null ? !t.sprintId : Number(t.sprintId) === Number(sourceSprintId))
      )

      if (descendants.length === 0) {
        ElMessage.warning(t('project.noDescendantsToMove'))
        isProcessingDrop = false
        return
      }

      const targetSprintIdValue = targetSprintId == null ? null : Number(targetSprintId)
      for (const desc of descendants) {
        await moveTask(desc.id, { sprintId: targetSprintIdValue, projectId: desc.projectId })
        desc.sprintId = targetSprintIdValue
      }

      // Reload data if moving to backlog
      if (targetSprintId === null) {
        const projectId = projectStore.currentProjectId
        const [treeRes, tasksRes] = await Promise.all([
          getRequirementTree(projectId),
          getTasksByProject(projectId)
        ])
        backlogTree.value = filterInvalidLeaves(treeRes.data || [])
        tasks.value = tasksRes.data || tasksRes || []
      }

      ElMessage.success(t('project.descendantsMoved', { count: descendants.length }))
      return
    }

    // TASK/SUBTASK: normal move
    const targetSprintIdValue = targetSprintId === null ? null : Number(targetSprintId)
    const currentSprintId = task.sprintId == null ? null : Number(task.sprintId)
    if (currentSprintId === targetSprintIdValue) {
      isProcessingDrop = false
      return
    }

    await moveTask(taskId, { sprintId: targetSprintIdValue, projectId: task.projectId })
    task.sprintId = targetSprintIdValue

    if (targetSprintId === null) {
      const projectId = projectStore.currentProjectId
      const [treeRes, tasksRes] = await Promise.all([
        getRequirementTree(projectId),
        getTasksByProject(projectId)
      ])
      backlogTree.value = filterInvalidLeaves(treeRes.data || [])
      tasks.value = tasksRes.data || tasksRes || []
    } else {
      const idsToRemove = new Set([task.id, ...getDescendantIds(task)])
      filterBacklogTree(idsToRemove)
    }
    ElMessage.success(t('project.taskMoved'))
  } catch (e) {
    ElMessage.error(t('project.taskMoveFailed'))
  } finally {
    isProcessingDrop = false
  }
}

// Get all descendant task IDs for a parent task
function getDescendantIds(parentTask, taskList = null) {
  const list = taskList || tasks.value
  const result = []
  const parentId = Number(parentTask.id)
  const children = list.filter(t => Number(t.parentId) === parentId)
  for (const child of children) {
    result.push(child.id)
    result.push(...getDescendantIds(child, list))
  }
  return result
}

// Filter backlog tree: remove tasks with given IDs and clean up empty ancestors
// Non-TASK/SUBTASK nodes cannot be leaf nodes in backlog
function filterBacklogTree(idsToRemove) {
  // Build parent map to check ancestors
  const parentMap = {}
  tasks.value.forEach(t => {
    if (t.parentId) {
      parentMap[t.id] = t.parentId
    }
  })

  // Check if any ancestor of a node is in idsToRemove
  const hasRemovedAncestor = (nodeId) => {
    let current = parentMap[nodeId]
    while (current) {
      if (idsToRemove.has(current)) return true
      current = parentMap[current]
    }
    return false
  }

  const removeEmptyAncestors = (nodes) => {
    const filtered = nodes
      .filter(node => !idsToRemove.has(node.id) && !hasRemovedAncestor(node.id))
      .map(node => ({
        ...node,
        children: removeEmptyAncestors(node.children || [])
      }))

    return filtered.filter(node => {
      // If has children, keep it
      if (node.children && node.children.length > 0) {
        return true
      }
      // Leaf node: only TASK/SUBTASK can be leaf in backlog
      if (['TASK', 'SUBTASK'].includes(node.type)) {
        return true
      }
      // Non-TASK/SUBTASK leaf - exclude (empty ancestor)
      return false
    })
  }

  backlogTree.value = removeEmptyAncestors(backlogTree.value)
}

// Filter invalid leaf nodes from backlog tree on initial load
// Non-TASK/SUBTASK nodes cannot be leaf nodes in backlog
function filterInvalidLeaves(nodes) {
  return nodes.filter(node => {
    node.children = filterInvalidLeaves(node.children || [])

    // If has children, keep it
    if (node.children.length > 0) {
      return true
    }

    // Leaf node: only TASK/SUBTASK can be leaf in backlog
    if (['TASK', 'SUBTASK'].includes(node.type)) {
      return true
    }

    // Non-TASK/SUBTASK leaf - exclude
    return false
  })
}

// Filter backlog tree to only show tasks with sprintId === null
function filterBacklogBySprintId(nodes, sprintTaskIds) {
  return nodes.filter(node => {
    // Recursively filter children
    node.children = filterBacklogBySprintId(node.children || [], sprintTaskIds)

    // If this node is in a sprint, exclude it and its children
    if (sprintTaskIds.has(node.id)) {
      return false
    }

    // If has children after filtering, keep it
    if (node.children.length > 0) {
      return true
    }

    // Leaf node: only TASK/SUBTASK can be leaf in backlog
    if (['TASK', 'SUBTASK'].includes(node.type)) {
      return true
    }

    // Non-TASK/SUBTASK leaf - exclude
    return false
  })
}

onMounted(() => {
  loadData()
})

// Watch for project changes and reload data
watch(() => projectStore.currentProjectId, (newProjectId) => {
  if (newProjectId) {
    loadData()
  }
})
</script>

<style scoped>
.sprint-management-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.pm-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--pm-space-lg);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--pm-space-md);
}

.filters {
  display: flex;
  align-items: center;
  gap: var(--pm-space-sm);
}

.lanes-container {
  flex: 1;
  overflow-x: auto;
  overflow-y: hidden;
  display: flex;
  gap: var(--pm-space-lg);
  padding: var(--pm-space-md) 0;
  height: calc(100vh - 180px);
  min-height: 400px;
  box-sizing: border-box;
}
</style>
