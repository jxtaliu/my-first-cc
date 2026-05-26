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
  return request.get(`/v1/projects/${projectId}/sprints`)
}

export function createSprint(projectId, data) {
  return request.post(`/v1/projects/${projectId}/sprints`, data)
}

export function updateSprint(projectId, sprintId, data) {
  return request.put(`/v1/projects/${projectId}/sprints/${sprintId}`, data)
}

export function deleteSprint(projectId, sprintId) {
  return request.delete(`/v1/projects/${projectId}/sprints/${sprintId}`)
}

export function startSprint(projectId, sprintId) {
  return request.put(`/v1/projects/${projectId}/sprints/${sprintId}/start`)
}

export function completeSprint(projectId, sprintId) {
  return request.put(`/v1/projects/${projectId}/sprints/${sprintId}/complete`)
}

export function getSprintTasks(projectId, sprintId) {
  return request.get(`/v1/projects/${projectId}/sprints/${sprintId}/tasks`)
}

export function getProjectStats(projectId) {
  return request.get(`/v1/projects/${projectId}/stats`)
}

export function getProjectRoles(projectId) {
  return request.get(`/v1/project-roles/project/${projectId}`)
}

export function assignProjectRole(projectId, userId, role) {
  return request.post('/v1/project-roles', { projectId, userId, role })
}

export function removeProjectRole(projectId, userId) {
  return request.delete(`/v1/project-roles/project/${projectId}/user/${userId}`)
}

export function batchAssignTasks(sprintId, taskIds) {
  return request.post(`/sprints/${sprintId}/tasks/batch`, { taskIds })
}

export function batchRemoveTasks(sprintId, taskIds) {
  return request.delete(`/sprints/${sprintId}/tasks/batch`, { taskIds })
}
