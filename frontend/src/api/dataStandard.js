import request from './request'

export function getDataStandards(params) {
  return request.get('/v1/data-standards', { params })
}

export function getDataStandardDetail(id) {
  return request.get(`/v1/data-standards/${id}`)
}

export function createDataStandard(data) {
  return request.post('/v1/data-standards', data)
}

export function updateDataStandard(id, data) {
  return request.put(`/v1/data-standards/${id}`, data)
}

export function deleteDataStandard(id) {
  return request.delete(`/v1/data-standards/${id}`)
}

export function shareDataStandard(id, data) {
  return request.post(`/v1/data-standards/${id}/share`, data)
}
