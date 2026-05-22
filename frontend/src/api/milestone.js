import request from './request'

export function getMilestones() {
  return request.get('/milestones')
}

export function getCrossProjectMilestones() {
  return request.get('/milestones/cross-project')
}

export function getMilestonesByProject(projectId) {
  return request.get(`/milestones/project/${projectId}`)
}

export function getMilestone(id) {
  return request.get(`/milestones/${id}`)
}

export function createMilestone(data) {
  return request.post('/milestones', data)
}

export function updateMilestone(id, data) {
  return request.put(`/milestones/${id}`, data)
}

export function deleteMilestone(id) {
  return request.delete(`/milestones/${id}`)
}

export function getDueSoonMilestones(days = 7) {
  return request.get('/milestones/due-soon', { params: { days } })
}
