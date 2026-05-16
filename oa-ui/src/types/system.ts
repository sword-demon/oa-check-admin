export interface SysUser {
  id: number
  username: string
  passwordHash: string | null
  nickname: string
  email: string | null
  phone: string | null
  deptId: number | null
  status: number
  createdAt: string
  updatedAt: string
}

export interface SysRole {
  id: number
  roleName: string
  roleKey: string
  sort: number
  dataScope: number
  status: number
  createdAt: string
  updatedAt: string
}

export enum PermissionType {
  MENU = 1,
  BUTTON = 2,
  API = 3,
}

export interface SysPermission {
  id: number
  parentId: number
  permissionName: string
  permissionType: PermissionType
  path: string | null
  component: string | null
  icon: string | null
  sort: number
  status: number
  children?: SysPermission[]
  createdAt: string
  updatedAt: string
}

export interface SysDept {
  id: number
  parentId: number
  deptName: string
  sort: number
  leaderUserId: number | null
  status: number
  children?: SysDept[]
  createdAt: string
  updatedAt: string
}
