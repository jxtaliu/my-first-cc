import { ref, computed, watch } from 'vue'
import { getSystemTaskStatuses } from '@/api/taskStatus'
import { useTaskStatusStore } from '@/stores/taskStatus'

export function useKanban() {
  const taskStatusStore = useTaskStatusStore()

  const columns = ref([])
  const taskStatuses = ref([])
  const loading = ref(false)
  const currentProjectId = ref(null)

  // Status mappings - use store getters if available, otherwise use local data
  const statusIdToCode = computed(() => {
    if (!currentProjectId.value) {
      // For system statuses, build from local taskStatuses
      const map = {}
      taskStatuses.value.forEach(s => { map[s.id] = s.code?.toLowerCase() })
      return map
    }
    return taskStatusStore.getStatusIdToCode(currentProjectId.value)
  })

  const statusCodeToId = computed(() => {
    if (!currentProjectId.value) {
      // For system statuses, build from local taskStatuses
      const map = {}
      taskStatuses.value.forEach(s => { map[s.code?.toLowerCase()] = s.id })
      return map
    }
    return taskStatusStore.getStatusCodeToId(currentProjectId.value)
  })

  // Build columns from statuses
  function buildColumns(statuses) {
    if (!statuses || statuses.length === 0) {
      columns.value = []
      return
    }

    columns.value = statuses.map(s => ({
      id: s.code?.toLowerCase(),
      status: s.code?.toLowerCase(),
      title: s.nameEn || s.code,
      titleZh: s.nameZh || '',
      color: s.color || '#94A3B8',
      statusId: s.id,
      sortOrder: s.sortOrder
    }))
  }

  // Rebuild columns whenever taskStatuses changes
  watch(
    () => JSON.stringify(taskStatuses.value.map(s => s.id)),
    () => {
      if (taskStatuses.value.length > 0) {
        buildColumns(taskStatuses.value)
      }
    }
  )

  // Watch for store changes - rebuild when store's statuses for current project are updated
  // Use a deep watch on the specific project entry to catch reorder changes
  watch(
    () => {
      if (!currentProjectId.value) return null
      // Return JSON string to track both content AND order changes
      const statuses = taskStatusStore.statusesByProject[currentProjectId.value]
      return statuses ? JSON.stringify(statuses.map(s => ({ id: s.id, sortOrder: s.sortOrder }))) : null
    },
    (newVal, oldVal) => {
      if (!newVal || !currentProjectId.value) return
      const storeStatuses = taskStatusStore.statusesByProject[currentProjectId.value]
      if (storeStatuses && storeStatuses.length > 0) {
        taskStatuses.value = storeStatuses
        buildColumns(storeStatuses)
      }
    }
  )

  async function loadTaskStatuses(projectId) {
    if (!projectId) {
      // Load system statuses (no project)
      currentProjectId.value = null
      loading.value = true
      try {
        const systemRes = await getSystemTaskStatuses()
        taskStatuses.value = systemRes.data || []
        buildColumns(taskStatuses.value)
      } finally {
        loading.value = false
      }
      return
    }

    // Resolve numeric ID to business ID if needed
    let businessId = projectId
    if (!/^[A-Z]/.test(projectId)) {
      businessId = await taskStatusStore.getProjectBusinessId(projectId)
    }

    currentProjectId.value = businessId

    loading.value = true
    try {
      // Always fetch fresh data to ensure we have latest (e.g., after reorder)
      const statuses = await taskStatusStore.fetchStatuses(businessId)
      if (statuses && statuses.length > 0) {
        taskStatuses.value = statuses
        buildColumns(taskStatuses.value)
      } else {
        // Fallback to system statuses if project has none
        const systemRes = await getSystemTaskStatuses()
        taskStatuses.value = systemRes.data || []
        buildColumns(taskStatuses.value)
      }
    } finally {
      loading.value = false
    }
  }

  function normalizeTask(apiTask) {
    return {
      id: apiTask.id,
      title: apiTask.title,
      description: apiTask.description,
      type: apiTask.type,
      typeName: apiTask.typeName,
      priority: apiTask.priority || 'P3',
      status: statusIdToCode.value[apiTask.status] || apiTask.status,
      statusId: apiTask.status,
      assignee: apiTask.assigneeId,
      assigneeId: apiTask.assigneeId,
      assigneeName: apiTask.assigneeName || '',
      projectId: apiTask.projectId,
      sprintId: apiTask.sprintId,
      estimateHours: apiTask.estimateHours,
      remainingHours: apiTask.remainingHours,
      actualHours: apiTask.actualHours,
      progress: apiTask.progress || 0,
      dueDate: apiTask.dueDate,
      startDate: apiTask.startDate,
      inProgressSince: apiTask.inProgressSince,
      commentCount: apiTask.commentCount || 0,
      attachmentCount: apiTask.attachmentCount || 0,
      _raw: apiTask
    }
  }

  return {
    columns,
    taskStatuses,
    loading,
    statusIdToCode,
    statusCodeToId,
    loadTaskStatuses,
    normalizeTask
  }
}
