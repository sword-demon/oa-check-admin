import request from '@/utils/request'

export function submitApproval(data: { templateId: number; title: string; formData: string }) {
  return request.post('/approval/submit', data)
}

export function approveTask(taskId: number, data: { result: number; comment: string }) {
  return request.post(`/approval/task/${taskId}/approve`, data)
}

export function getMyTodo() {
  return request.get('/approval/my-todo')
}

export function getMyDone() {
  return request.get('/approval/my-done')
}

export function withdrawInstance(instanceId: number) {
  return request.post(`/approval/${instanceId}/withdraw`)
}

export function getTemplates(params?: { page?: number; size?: number }) {
  return request.get('/approval/template', { params })
}

export function createTemplate(data: Record<string, any>) {
  return request.post('/approval/template', data)
}

export function getMyCc() {
  return request.get('/approval/cc')
}

export function markCcRead(ccId: number) {
  return request.post(`/approval/cc/${ccId}/read`)
}
