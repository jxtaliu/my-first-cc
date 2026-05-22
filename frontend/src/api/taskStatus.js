import request from './request'

export function getTaskStatusesByProject(projectId) {
  return request.get(`/v1/task-statuses/project/${projectId}`)
}

export function getSystemTaskStatuses() {
  return request.get('/v1/task-statuses/system')
}

export function getTaskStatusByCode(projectId, code) {
  return request.get(`/v1/task-statuses/project/${projectId}/${code}`)
}

export function createTaskStatus(data) {
  return request.post('/v1/task-statuses', data)
}

export function updateTaskStatus(id, data) {
  return request.put(`/v1/task-statuses/${id}`, data)
}

export function deleteTaskStatus(id) {
  return request.delete(`/v1/task-statuses/${id}`)
}
