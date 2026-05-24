import { ref, computed } from 'vue'
import { getTaskStatusesByProject, getSystemTaskStatuses } from '@/api/taskStatus'

export function useKanban() {
  const columns = ref([])
  const taskStatuses = ref([])
  const loading = ref(false)

  // Status mappings
  const statusIdToCode = computed(() => {
    const map = {}
    taskStatuses.value.forEach(s => { map[s.id] = s.code.toLowerCase() })
    return map
  })

  const statusCodeToId = computed(() => {
    const map = {}
    taskStatuses.value.forEach(s => { map[s.code.toLowerCase()] = s.id })
    return map
  })

  async function loadTaskStatuses(projectId) {
    loading.value = true
    try {
      // 直接从 task_status 表获取项目状态
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
      // 直接使用 task_status 表数据生成列
      if (statuses.length > 0) {
        columns.value = statuses.map(s => ({
          id: s.code.toLowerCase(),
          status: s.code.toLowerCase(),
          title: s.nameEn || s.name || s.code,
          ...(s.nameZh && { titleZh: s.nameZh }),
          color: s.color || '#94A3B8',
          statusId: s.id,
          ...(s.sortOrder != null && { sortOrder: s.sortOrder })
        }))
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

  return { columns, taskStatuses, loading, statusIdToCode, statusCodeToId, loadTaskStatuses, normalizeTask }
}
