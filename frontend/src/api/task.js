import request from './request'

export function getTasks(params) {
  return request.get('/v1/tasks', { params })
}

export function getTask(id) {
  return request.get(`/v1/tasks/${id}`)
}

export function createTask(data) {
  return request.post('/v1/tasks', data)
}

export function updateTask(id, data) {
  return request.put(`/v1/tasks/${id}`, data, { params: { projectId: data.projectId } })
}

export function deleteTask(id) {
  return request.delete(`/v1/tasks/${id}`)
}

export function getTasksBySprint(sprintId) {
  return request.get('/v1/tasks', { params: { sprintId } })
}

export function getTasksByProject(projectId) {
  return request.get('/v1/tasks', { params: { projectId } })
}

export function getTasksByAssignee(assigneeId) {
  return request.get('/v1/tasks', { params: { assigneeId } })
}

export function moveTask(id, data) {
  return request.put(`/v1/tasks/${id}/move`, data, { params: { projectId: data.projectId } })
}

export function assignTask(id, userId) {
  return request.put(`/v1/tasks/${id}/assign`, { userId })
}

export function updateTaskStatus(id, statusCode) {
  return request.put(`/v1/tasks/${id}/status`, { statusCode })
}

export function canTransitionTo(id, targetStatusCode) {
  return request.get(`/v1/tasks/${id}/can-transition/${targetStatusCode}`)
}

export function getTaskComments(taskId) {
  return request.get(`/v1/tasks/${taskId}/comments`)
}

export function addTaskComment(taskId, data) {
  return request.post(`/v1/tasks/${taskId}/comments`, data)
}

export function getTaskAttachments(taskId) {
  return request.get(`/v1/tasks/${taskId}/attachments`)
}

export function addTaskAttachment(taskId, data) {
  return request.post(`/v1/tasks/${taskId}/attachments`, data)
}

export function deleteTaskAttachment(attachmentId) {
  return request.delete(`/v1/tasks/attachments/${attachmentId}`)
}

export function getTaskDependencies(taskId) {
  return request.get(`/v1/tasks/${taskId}/dependencies`)
}

export function getBlockingDependencies(taskId) {
  return request.get(`/v1/tasks/${taskId}/blocking`)
}

export function countBlockingDependencies(taskId) {
  return request.get(`/v1/tasks/${taskId}/blocking-count`)
}

export function addTaskDependency(taskId, data) {
  return request.post(`/v1/tasks/${taskId}/dependencies`, data)
}

export function removeTaskDependency(dependencyId) {
  return request.delete(`/v1/tasks/dependencies/${dependencyId}`)
}

export function getTaskCountByStatus(statusId) {
  return request.get('/v1/tasks/count', { params: { statusId } })
}
