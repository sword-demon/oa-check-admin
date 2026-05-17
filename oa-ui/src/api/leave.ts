import request from '@/utils/request'

export interface LeaveRequestQuery {
  title?: string
  leaveType?: number
  status?: number
  page?: number
  pageSize?: number
}

export interface LeaveRequestForm {
  title: string
  leaveType: number
  startTime: string
  endTime: string
  reason?: string
}

export function getLeaveList(params?: LeaveRequestQuery) {
  return request.get('/leave/leave_request', { params })
}

export function getLeaveDetail(id: number) {
  return request.get(`/leave/leave_request/${id}`)
}

export function createLeave(data: LeaveRequestForm) {
  return request.post('/leave/leave_request', data)
}

export function updateLeave(id: number, data: LeaveRequestForm) {
  return request.put(`/leave/leave_request/${id}`, data)
}

export function deleteLeave(id: number) {
  return request.delete(`/leave/leave_request/${id}`)
}

export function submitLeaveForApproval(id: number) {
  return request.post(`/leave/leave_request/${id}/submit`)
}

export function resubmitLeave(id: number, data: LeaveRequestForm) {
  return request.post(`/leave/leave_request/${id}/resubmit`, data)
}
