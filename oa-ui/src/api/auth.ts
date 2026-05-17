import request from '@/utils/request'
import type { SysUser } from '@/types/system'

export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data) as Promise<{ token: string; user: SysUser }>
}

export function logout() {
  return request.post('/auth/logout') as Promise<void>
}

export function getMe() {
  return request.get('/auth/me') as Promise<SysUser>
}
