import request from '@/utils/request'
import type { PageData } from '@/types'

export interface AuditLog {
  id: number
  userId: number
  module: string
  action: string
  targetType: string
  targetId: number
  detail: string
  ip: string
  createdAt: string
}

export function queryAuditLogs(params: {
  module?: string
  action?: string
  targetType?: string
  targetId?: number
  userId?: number
  startTime?: string
  endTime?: string
  page: number
  pageSize: number
}) {
  return request.get<never, PageData<AuditLog>>('/admin/audit-log', { params })
}

export interface AdminInstance {
  id: number
  processTemplateId: number
  instanceTitle: string
  initiatorUserId: number
  status: number
  formData: string
  createdAt: string
  endAt: string
}

export interface AdminMetrics {
  totalInstances: number
  pendingInstances: number
  approvedInstances: number
  rejectedInstances: number
  withdrawnInstances: number
  avgDurationHours: number
  templateMetrics: {
    templateId: number
    templateName: string
    total: number
    pending: number
    approved: number
    rejected: number
  }[]
}

export function getAdminInstances(params: {
  title?: string
  status?: number
  templateId?: number
  initiatorUserId?: number
  startTime?: string
  endTime?: string
  page: number
  pageSize: number
}) {
  return request.get<never, PageData<AdminInstance>>('/admin/approval/instances', { params })
}

export function terminateInstance(instanceId: number) {
  return request.post<never, void>(`/admin/approval/instances/${instanceId}/terminate`)
}

export function reassignTask(taskId: number, targetUserId: number) {
  return request.post<never, void>(`/admin/approval/tasks/${taskId}/reassign?targetUserId=${targetUserId}`)
}

export function getAdminMetrics() {
  return request.get<never, AdminMetrics>('/admin/approval/metrics')
}
