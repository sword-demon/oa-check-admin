import request from '@/utils/request'

export function getUserList(params: { username?: string; status?: number; page: number; pageSize: number }) {
  return request.get('/system/user', { params })
}

export function getUser(id: number) {
  return request.get(`/system/user/${id}`)
}

export function createUser(data: Record<string, any>) {
  return request.post('/system/user', data)
}

export function updateUser(id: number, data: Record<string, any>) {
  return request.put(`/system/user/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/system/user/${id}`)
}
