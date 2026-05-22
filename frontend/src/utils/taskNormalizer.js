export function normalizeTask(apiTask, statusIdToCode = {}) {
  if (!apiTask) return null
  return {
    id: apiTask.id,
    title: apiTask.title,
    description: apiTask.description,
    type: apiTask.type,
    priority: apiTask.priority || 'P3',
    status: statusIdToCode[apiTask.statusId] || apiTask.status || 'TODO',
    statusId: apiTask.statusId || apiTask.status,
    assigneeId: apiTask.assigneeId,
    assigneeName: apiTask.assigneeName || '',
    estimateHours: apiTask.estimateHours,
    remainingHours: apiTask.remainingHours,
    actualHours: apiTask.actualHours,
    progress: apiTask.progress || 0,
    dueDate: apiTask.dueDate,
    commentCount: apiTask.commentCount || 0,
    attachmentCount: apiTask.attachmentCount || 0,
    _raw: apiTask
  }
}

export function denormalizeTask(task, statusCodeToId = {}) {
  return {
    title: task.title,
    description: task.description,
    type: task.type,
    priority: task.priority,
    statusId: statusCodeToId[task.status] || task.statusId,
    assigneeId: task.assigneeId,
    estimateHours: task.estimateHours,
    remainingHours: task.remainingHours,
    actualHours: task.actualHours,
    progress: task.progress
  }
}
