import request from './request'

export function getTemplates() {
  return request.get('/v1/project-templates')
}

export function getTemplate(id) {
  return request.get(`/v1/project-templates/${id}`)
}

export function createTemplate(data) {
  return request.post('/v1/project-templates', data)
}

export function updateTemplate(id, data) {
  return request.put(`/v1/project-templates/${id}`, data)
}

export function deleteTemplate(id) {
  return request.delete(`/v1/project-templates/${id}`)
}
