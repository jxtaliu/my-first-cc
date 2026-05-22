import request from './request'

export function getTemplates() {
  return request.get('/project-templates')
}

export function getTemplate(id) {
  return request.get(`/project-templates/${id}`)
}

export function createTemplate(data) {
  return request.post('/project-templates', data)
}

export function updateTemplate(id, data) {
  return request.put(`/project-templates/${id}`, data)
}

export function deleteTemplate(id) {
  return request.delete(`/project-templates/${id}`)
}
