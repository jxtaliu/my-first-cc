import request from './request'

export function getTasks(params) {
  return request.get('/tasks', { params })
}

export function getTask(id) {
  return request.get(`/tasks/${id}`)
}

export function createTask(data) {
  return request.post('/tasks', data)
}

export function updateTask(id, data) {
  return request.put(`/tasks/${id}`, data)
}

export function deleteTask(id) {
  return request.delete(`/tasks/${id}`)
}

export function getTasksBySprint(sprintId) {
  return request.get('/tasks', { params: { sprintId } })
}

export function getTasksByProject(projectId) {
  return request.get('/tasks', { params: { projectId } })
}

export function getTasksByAssignee(assigneeId) {
  return request.get('/tasks', { params: { assigneeId } })
}

export function moveTask(id, data) {
  return request.put(`/tasks/${id}/move`, data)
}

export function assignTask(id, userId) {
  return request.put(`/tasks/${id}/assign`, { userId })
}

export function updateTaskStatus(id, statusId) {
  return request.put(`/tasks/${id}/status`, { statusId })
}

export function canTransitionTo(id, targetStatusId) {
  return request.get(`/tasks/${id}/can-transition/${targetStatusId}`)
}

export function getTaskComments(taskId) {
  return request.get(`/tasks/${taskId}/comments`)
}

export function addTaskComment(taskId, data) {
  return request.post(`/tasks/${taskId}/comments`, data)
}

export function getTaskAttachments(taskId) {
  return request.get(`/tasks/${taskId}/attachments`)
}

export function addTaskAttachment(taskId, data) {
  return request.post(`/tasks/${taskId}/attachments`, data)
}

export function deleteTaskAttachment(attachmentId) {
  return request.delete(`/tasks/attachments/${attachmentId}`)
}

export function getTaskDependencies(taskId) {
  return request.get(`/tasks/${taskId}/dependencies`)
}

export function getBlockingDependencies(taskId) {
  return request.get(`/tasks/${taskId}/blocking`)
}

export function countBlockingDependencies(taskId) {
  return request.get(`/tasks/${taskId}/blocking-count`)
}

export function addTaskDependency(taskId, data) {
  return request.post(`/tasks/${taskId}/dependencies`, data)
}

export function removeTaskDependency(dependencyId) {
  return request.delete(`/tasks/dependencies/${dependencyId}`)
}
