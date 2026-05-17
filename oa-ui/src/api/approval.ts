import request from '@/utils/request'
import type { ApprovalCc, ApprovalInstance, ApprovalTask, DashboardStats, InstanceDiagram, PageData, ProcessTemplate, TaskVO } from '@/types'

export function submitApproval(data: { templateId: number; title: string; formData: string }) {
  return request.post<never, ApprovalInstance>('/approval/submit', data)
}

export function approveTask(taskId: number, data: { result: number; comment: string }) {
  return request.post<never, void>(`/approval/task/${taskId}/approve`, data)
}

export function getMyTodo() {
  return request.get<never, ApprovalTask[]>('/approval/my-todo')
}

export function getMyDone() {
  return request.get<never, ApprovalTask[]>('/approval/my-done')
}

export function withdrawInstance(instanceId: number) {
  return request.post<never, void>(`/approval/${instanceId}/withdraw`)
}

export function getTemplates(params?: { templateName?: string; status?: number; page?: number; pageSize?: number; size?: number }) {
  return request.get<never, PageData<ProcessTemplate>>('/approval/template', { params })
}

export function createTemplate(data: Record<string, unknown>) {
  return request.post<never, ProcessTemplate>('/approval/template', data)
}

export function getMyCc() {
  return request.get<never, ApprovalCc[]>('/approval/cc')
}

export function markCcRead(ccId: number) {
  return request.post<never, void>(`/approval/cc/${ccId}/read`)
}

export function getMyApplications(params: { title?: string; status?: number; page: number; pageSize: number }) {
  return request.get<never, PageData<ApprovalInstance>>('/approval/my-applications', { params })
}

export function getInstanceDetail(instanceId: number) {
  return request.get<never, ApprovalInstance>(`/approval/instance/${instanceId}`)
}

export function getInstanceTasks(instanceId: number) {
  return request.get<never, ApprovalTask[]>(`/approval/instance/${instanceId}/tasks`)
}

export function getInstanceDiagram(instanceId: number) {
  return request.get<never, InstanceDiagram>(`/approval/instance/${instanceId}/diagram`)
}

export function getDashboardStats() {
  return request.get<never, DashboardStats>('/approval/dashboard/stats')
}

export function getMyTodoPaged(params: { title?: string; page: number; pageSize: number }) {
  return request.get<never, PageData<TaskVO>>('/approval/my-todo/paged', { params })
}

export function getMyDonePaged(params: { title?: string; page: number; pageSize: number }) {
  return request.get<never, PageData<TaskVO>>('/approval/my-done/paged', { params })
}

export function transferTask(taskId: number, data: { targetUserId: number; reason: string }) {
  return request.post<never, void>(`/approval/task/${taskId}/transfer`, data)
}
