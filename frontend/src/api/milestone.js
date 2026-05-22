import request from './request'

export function getMilestones() {
  return request.get('/v1/milestones')
}

export function getCrossProjectMilestones() {
  return request.get('/v1/milestones/cross-project')
}

export function getMilestonesByProject(projectId) {
  return request.get(`/v1/milestones/project/${projectId}`)
}

export function getMilestone(id) {
  return request.get(`/v1/milestones/${id}`)
}

export function createMilestone(data) {
  return request.post('/v1/milestones', data)
}

export function updateMilestone(id, data) {
  return request.put(`/v1/milestones/${id}`, data)
}

export function deleteMilestone(id) {
  return request.delete(`/v1/milestones/${id}`)
}

export function getDueSoonMilestones(days = 7) {
  return request.get('/v1/milestones/due-soon', { params: { days } })
}
