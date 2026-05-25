import request from './request'

/**
 * Get requirement tree (Epic and Feature - top level items)
 */
export function getRequirementTree(projectId) {
  return request.get('/v1/tasks/requirements/tree', {
    params: { projectId }
  })
}

/**
 * Get direct children of a requirement
 */
export function getRequirementChildren(id) {
  return request.get(`/v1/tasks/requirements/${id}/children`)
}

/**
 * Get full subtree of a requirement
 */
export function getRequirementSubtree(id) {
  return request.get(`/v1/tasks/requirements/${id}/subtree`)
}

/**
 * Move/ reorder a requirement
 */
export function moveRequirement(data) {
  return request.put('/v1/tasks/requirements/move', data)
}

/**
 * Get bug list for a project
 */
export function getBugs(projectId) {
  return request.get('/v1/tasks/bugs', {
    params: { projectId }
  })
}

/**
 * Create a bug
 */
export function createBug(data) {
  return request.post('/v1/tasks/bugs', data)
}

/**
 * Get bug status options for a project
 */
export function getBugStatuses(projectId) {
  return request.get(`/v1/tasks/bugs/statuses/${projectId}`)
}

/**
 * Get allowed status transitions for a bug
 */
export function getBugStatusTransitions(projectId, fromStatus) {
  return request.get(`/v1/tasks/bugs/transitions/${projectId}/${fromStatus}`)
}

/**
 * Update bug status
 */
export function updateBugStatus(bugId, bugStatusId) {
  return request.put(`/v1/tasks/bugs/${bugId}/status`, null, {
    params: { bugStatusId }
  })
}

/**
 * Create a requirement
 */
export function createRequirement(data) {
  return request.post('/v1/tasks', data)
}

/**
 * Get requirement detail by ID
 */
export function getRequirement(id) {
  return request.get(`/v1/tasks/${id}`)
}

/**
 * Update a requirement
 */
export function updateRequirement(id, data) {
  return request.put(`/v1/tasks/${id}`, data)
}

/**
 * Delete a requirement
 */
export function deleteRequirement(id) {
  return request.delete(`/v1/tasks/${id}`)
}
