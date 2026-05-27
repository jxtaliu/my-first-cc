import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { getSystemTaskStatuses } from '@/api/taskStatus'
import { useTaskStatusStore } from '@/stores/taskStatus'

export function useKanban() {
  const taskStatusStore = useTaskStatusStore()
  const i18n = useI18n()
  const currentLocale = computed(() => i18n.locale?.value || i18n.locale || 'en-US')

  const columns = ref([])
  const taskStatuses = ref([])
  const loading = ref(false)
  const currentProjectId = ref(null)

  const statusIdToCode = computed(() => {
    if (!currentProjectId.value) {
      const map = {}
      taskStatuses.value.forEach(s => { map[s.id] = s.code?.toLowerCase() })
      return map
    }
    return taskStatusStore.getStatusIdToCode(currentProjectId.value)
  })

  const statusCodeToId = computed(() => {
    if (!currentProjectId.value) {
      const map = {}
      taskStatuses.value.forEach(s => { map[s.code?.toLowerCase()] = s.id })
      return map
    }
    return taskStatusStore.getStatusCodeToId(currentProjectId.value)
  })

  function buildColumns(statuses) {
    if (!statuses || statuses.length === 0) {
      columns.value = []
      return
    }

    const isZh = currentLocale.value === 'zh-CN'
    columns.value = statuses.map(s => ({
      id: s.code?.toLowerCase(),
      status: s.code?.toLowerCase(),
      title: isZh ? (s.nameZh || s.nameEn || s.code) : (s.nameEn || s.code),
      color: s.color || '#94A3B8',
      statusId: s.id,
      sortOrder: s.sortOrder
    }))
  }

  watch(
    () => JSON.stringify(taskStatuses.value.map(s => s.id)),
    () => {
      if (taskStatuses.value.length > 0) {
        buildColumns(taskStatuses.value)
      }
    }
  )

  watch(
    () => currentLocale.value,
    () => {
      if (taskStatuses.value.length > 0) {
        buildColumns(taskStatuses.value)
      }
    }
  )

  watch(
    () => {
      if (!currentProjectId.value) return null
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

    let businessId = projectId
    if (!/^[A-Z]/.test(projectId)) {
      businessId = await taskStatusStore.getProjectBusinessId(projectId)
    }

    currentProjectId.value = businessId

    loading.value = true
    try {
      const statuses = await taskStatusStore.fetchStatuses(businessId)
      if (statuses && statuses.length > 0) {
        taskStatuses.value = statuses
        buildColumns(taskStatuses.value)
      } else {
        const systemRes = await getSystemTaskStatuses()
        taskStatuses.value = systemRes.data || []
        buildColumns(taskStatuses.value)
      }
    } finally {
      loading.value = false
    }
  }

  function normalizeTask(apiTask) {
    const normalizedStatus = apiTask.status ? apiTask.status.toLowerCase() : 'todo'

    return {
      id: apiTask.id,
      title: apiTask.title,
      description: apiTask.description,
      type: apiTask.type,
      typeName: apiTask.typeName,
      priority: apiTask.priority || 'P3',
      status: normalizedStatus,
      statusId: apiTask.status,
      assignee: apiTask.assigneeId,
      assigneeId: apiTask.assigneeId,
      assigneeName: apiTask.assigneeName || '',
      projectId: apiTask.projectId,
      sprintId: apiTask.sprintId,
      parentId: apiTask.parentId || apiTask.parent_id,
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
