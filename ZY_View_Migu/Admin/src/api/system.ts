import { request } from './http'

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  status: string
  createdAt: string
}

export interface RoleInfo {
  id: number
  code: string
  name: string
  createdAt: string
}

// 获取系统用户列表
export function listUsers() {
  console.log('[API] listUsers')
  return request<UserInfo[]>('/admin/system/users')
}

// 创建新系统用户
export function createUser(payload: { username: string; password: string; nickname?: string; email?: string }) {
  console.log('[API] createUser', { username: payload.username, nickname: payload.nickname, email: payload.email })
  return request<UserInfo>('/admin/system/users', { method: 'POST', body: JSON.stringify(payload) })
}

// 更新用户信息（昵称、邮箱、状态）
export function updateUser(id: number, payload: { nickname?: string; email?: string; status?: string }) {
  console.log('[API] updateUser', { id, ...payload })
  return request<UserInfo>(`/admin/system/users/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
}

// 删除指定用户
export function deleteUser(id: number) {
  console.log('[API] deleteUser', { id })
  return request<void>(`/admin/system/users/${id}`, { method: 'DELETE' })
}

// 获取角色列表
export function listRoles() {
  console.log('[API] listRoles')
  return request<RoleInfo[]>('/admin/system/roles')
}

// 创建新角色
export function createRole(payload: { code: string; name: string }) {
  console.log('[API] createRole', payload)
  return request<RoleInfo>('/admin/system/roles', { method: 'POST', body: JSON.stringify(payload) })
}
