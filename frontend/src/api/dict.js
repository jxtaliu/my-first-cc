import request from './request'

// DictType APIs
export function getDictTypes() {
  return request.get('/dicts/types')
}

export function getDictType(id) {
  return request.get(`/dicts/types/${id}`)
}

export function createDictType(data) {
  return request.post('/dicts/types', data)
}

export function updateDictType(id, data) {
  return request.put(`/dicts/types/${id}`, data)
}

export function deleteDictType(id) {
  return request.delete(`/dicts/types/${id}`)
}

// DictCode APIs
export function getDictItems(params) {
  return request.get('/dicts/items', { params })
}

export function getDictItem(id) {
  return request.get(`/dicts/items/${id}`)
}

export function createDictItem(data) {
  return request.post('/dicts/items', data)
}

export function updateDictItem(id, data) {
  return request.put(`/dicts/items/${id}`, data)
}

export function deleteDictItem(id) {
  return request.delete(`/dicts/items/${id}`)
}

// Get dictionary codes by type code (e.g., 'task_status')
export function getDictCodesByType(type) {
  return request.get(`/dicts/codes/${type}`)
}

// Utility
export function refreshDictCache() {
  return request.post('/dicts/refresh')
}
