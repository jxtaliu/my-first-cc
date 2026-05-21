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

export function moveTask(id, data) {
  return request.put(`/tasks/${id}/move`, data)
}

export function assignTask(id, userId) {
  return request.put(`/tasks/${id}/assign`, { userId })
}

export function getTaskComments(taskId) {
  return request.get(`/tasks/${taskId}/comments`)
}

export function addTaskComment(taskId, data) {
  return request.post(`/tasks/${taskId}/comments`, data)
}
