import request from './request'

export function getTimesheets(params) {
  return request.get('/timesheets', { params })
}

export function getTimesheet(id) {
  return request.get(`/timesheets/${id}`)
}

export function createTimesheet(data) {
  return request.post('/timesheets', data)
}

export function updateTimesheet(id, data) {
  return request.put(`/timesheets/${id}`, data)
}

export function deleteTimesheet(id) {
  return request.delete(`/timesheets/${id}`)
}

export function approveTimesheet(id) {
  return request.post(`/timesheets/${id}/approve`)
}

export function rejectTimesheet(id) {
  return request.post(`/timesheets/${id}/reject`)
}

export function getMyTimesheets(params) {
  return request.get('/timesheets/my', { params })
}

export function getTimesheetStats(params) {
  return request.get('/timesheets/stats', { params })
}
