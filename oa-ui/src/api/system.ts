import request from '@/utils/request'

// Role APIs
export function getRoleList(params?: { roleName?: string; status?: number; page?: number; pageSize?: number }) {
  return request.get('/system/role', { params })
}

export function getRole(id: number) {
  return request.get(`/system/role/${id}`)
}

export function createRole(data: Record<string, any>) {
  return request.post('/system/role', data)
}

export function updateRole(id: number, data: Record<string, any>) {
  return request.put(`/system/role/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete(`/system/role/${id}`)
}

export function assignPermissions(roleId: number, permissionIds: number[]) {
  return request.post(`/system/role/${roleId}/permissions`, { permissionIds })
}

export function assignDataScope(roleId: number, dataScope: number, deptIds?: number[]) {
  return request.post(`/system/role/${roleId}/data-scope`, { dataScope, deptIds })
}

// Permission APIs
export function getPermissionTree(status?: number) {
  return request.get('/system/permission/tree', { params: { status } })
}

export function getPermissionList(status?: number) {
  return request.get('/system/permission', { params: { status } })
}

export function createPermission(data: Record<string, any>) {
  return request.post('/system/permission', data)
}

export function updatePermission(id: number, data: Record<string, any>) {
  return request.put(`/system/permission/${id}`, data)
}

export function deletePermission(id: number) {
  return request.delete(`/system/permission/${id}`)
}

// Dept APIs
export function getDeptTree(status?: number) {
  return request.get('/system/dept/tree', { params: { status } })
}

export function getDeptList(parentId?: number) {
  return request.get('/system/dept', { params: { parentId } })
}

export function createDept(data: Record<string, any>) {
  return request.post('/system/dept', data)
}

export function updateDept(id: number, data: Record<string, any>) {
  return request.put(`/system/dept/${id}`, data)
}

export function deleteDept(id: number) {
  return request.delete(`/system/dept/${id}`)
}
