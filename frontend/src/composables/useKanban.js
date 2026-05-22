import { ref, computed } from 'vue'
import { getTaskStatusesByProject, getSystemTaskStatuses } from '@/api/taskStatus'

export function useKanban() {
  const columns = ref([])
  const taskStatuses = ref([])
  const loading = ref(false)

  // Status mappings
  const statusIdToCode = computed(() => {
    const map = {}
    taskStatuses.value.forEach(s => { map[s.id] = s.code })
    return map
  })

  const statusCodeToId = computed(() => {
    const map = {}
    taskStatuses.value.forEach(s => { map[s.code] = s.id })
    return map
  })

  async function loadTaskStatuses(projectId) {
    loading.value = true
    try {
      let statuses = []
      if (projectId) {
        const projectRes = await getTaskStatusesByProject(projectId)
        statuses = projectRes.data || projectRes
      }
      // Fallback to system statuses if project has no custom statuses
      if (!statuses || !Array.isArray(statuses) || statuses.length === 0) {
        const systemRes = await getSystemTaskStatuses()
        statuses = systemRes.data || systemRes
      }
      taskStatuses.value = Array.isArray(statuses) ? statuses : []
      columns.value = taskStatuses.value.map(s => ({
        id: s.code,
        status: s.code,
        title: s.name,
        color: s.color || '#94A3B8',
        statusId: s.id
      }))
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

  return { columns, taskStatuses, loading, statusIdToCode, statusCodeToId, loadTaskStatuses, normalizeTask }
}
