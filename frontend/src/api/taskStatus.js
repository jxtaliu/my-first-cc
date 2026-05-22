import request from './request'

export function getTaskStatusesByProject(projectId) {
  return request.get(`/task-statuses/project/${projectId}`)
}

export function getSystemTaskStatuses() {
  return request.get('/task-statuses/system')
}

export function getTaskStatusByCode(projectId, code) {
  return request.get(`/task-statuses/project/${projectId}/${code}`)
}

export function createTaskStatus(data) {
  return request.post('/task-statuses', data)
}

export function updateTaskStatus(id, data) {
  return request.put(`/task-statuses/${id}`, data)
}

export function deleteTaskStatus(id) {
  return request.delete(`/task-statuses/${id}`)
}
