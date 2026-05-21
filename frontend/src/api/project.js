import request from './request'

export function getProjects(params) {
  return request.get('/projects', { params })
}

export function getProject(id) {
  return request.get(`/projects/${id}`)
}

export function createProject(data) {
  return request.post('/projects', data)
}

export function updateProject(id, data) {
  return request.put(`/projects/${id}`, data)
}

export function deleteProject(id) {
  return request.delete(`/projects/${id}`)
}

export function archiveProject(id) {
  return request.post(`/projects/${id}/archive`)
}

export function restoreProject(id) {
  return request.post(`/projects/${id}/restore`)
}

export function getProjectMembers(projectId) {
  return request.get(`/projects/${projectId}/members`)
}

export function addProjectMember(projectId, data) {
  return request.post(`/projects/${projectId}/members`, data)
}

export function removeProjectMember(projectId, userId) {
  return request.delete(`/projects/${projectId}/members/${userId}`)
}

export function getSprints(projectId) {
  return request.get(`/projects/${projectId}/sprints`)
}

export function createSprint(projectId, data) {
  return request.post(`/projects/${projectId}/sprints`, data)
}

export function updateSprint(projectId, sprintId, data) {
  return request.put(`/projects/${projectId}/sprints/${sprintId}`, data)
}

export function deleteSprint(projectId, sprintId) {
  return request.delete(`/projects/${projectId}/sprints/${sprintId}`)
}

export function getProjectStats(projectId) {
  return request.get(`/projects/${projectId}/stats`)
}
