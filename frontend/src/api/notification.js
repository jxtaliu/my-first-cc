import request from './request'

export function getNotifications(userId) {
  return request.get(`/notifications/user/${userId}`)
}

export function getUnreadNotifications(userId) {
  return request.get(`/notifications/user/${userId}/unread`)
}

export function getUnreadCount(userId) {
  return request.get(`/notifications/user/${userId}/unread-count`)
}

export function markAsRead(id) {
  return request.put(`/notifications/${id}/read`)
}

export function markAllAsRead(userId) {
  return request.put(`/notifications/user/${userId}/read-all`)
}

export function deleteNotification(id) {
  return request.delete(`/notifications/${id}`)
}
