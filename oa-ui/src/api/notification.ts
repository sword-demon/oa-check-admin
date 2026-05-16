import request from '@/utils/request'
import type { PageData } from '@/types'

export interface Notification {
  id: number
  userId: number
  type: string
  title: string
  content: string
  link: string
  isRead: number
  readAt: string
  createdAt: string
}

export function getMyNotifications(params: {
  type?: string
  unreadOnly?: boolean
  page: number
  pageSize: number
}) {
  return request.get<never, PageData<Notification>>('/notification/my', { params })
}

export function getUnreadCount() {
  return request.get<never, number>('/notification/unread-count')
}

export function markNotificationRead(id: number) {
  return request.post<never, void>(`/notification/${id}/read`)
}

export function markAllRead() {
  return request.post<never, void>('/notification/read-all')
}
