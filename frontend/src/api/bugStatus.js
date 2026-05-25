import request from './request'

export function getBugStatusesByProject(projectId) {
  return request.get(`/v1/bug-statuses/project/${projectId}`)
}

export function getBugStatusTransitions(projectId, fromStatusCode) {
  return request.get(`/v1/bug-statuses/project/${projectId}/transitions/${fromStatusCode}`)
}

export function canTransitionBugStatus(projectId, fromStatus, toStatus) {
  return request.get(`/v1/bug-statuses/project/${projectId}/can-transition`, {
    params: { fromStatus, toStatus }
  })
}

export function initializeBugStatuses(projectId) {
  return request.post(`/v1/bug-statuses/init/${projectId}`)
}
