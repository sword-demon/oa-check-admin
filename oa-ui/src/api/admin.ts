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
