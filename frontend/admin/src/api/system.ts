import { request } from './http'
import type { PageResponse } from '../types/common'

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
export function listUsers(
  page = 1,
  pageSize = 20,
  keyword?: string
): Promise<PageResponse<UserInfo>> {
  const params = new URLSearchParams()
  params.set('page', String(page))
  params.set('pageSize', String(pageSize))
  if (keyword) {
    params.set('keyword', keyword)
  }
  return request<PageResponse<UserInfo>>(`/admin/system/users?${params}`)
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

// 获取用户已分配的角色 ID 列表
export function getUserRoles(userId: number) {
  return request<number[]>(`/admin/system/users/${userId}/roles`)
}

// 设置用户角色
export function setUserRoles(userId: number, roleIds: number[]) {
  return request<void>(`/admin/system/users/${userId}/roles`, {
    method: 'PUT',
    body: JSON.stringify({ roleIds })
  })
}

// 获取角色列表
export function listRoles(
  page = 1,
  pageSize = 20
): Promise<PageResponse<RoleInfo>> {
  return request<PageResponse<RoleInfo>>(`/admin/system/roles?page=${page}&pageSize=${pageSize}`)
}

// 创建新角色
export function createRole(payload: { code: string; name: string }) {
  console.log('[API] createRole', payload)
  return request<RoleInfo>('/admin/system/roles', { method: 'POST', body: JSON.stringify(payload) })
}

// 更新角色
export function updateRole(id: number, payload: { code?: string; name?: string }) {
  console.log('[API] updateRole', { id, ...payload })
  return request<RoleInfo>(`/admin/system/roles/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
}

// 删除角色
export function deleteRole(id: number) {
  console.log('[API] deleteRole', { id })
  return request<void>(`/admin/system/roles/${id}`, { method: 'DELETE' })
}

// 获取角色的权限 ID 列表
export function getRolePermissions(roleId: number) {
  return request<number[]>(`/admin/system/roles/${roleId}/permissions`)
}

// 设置角色的权限
export function setRolePermissions(roleId: number, permissionIds: number[]) {
  return request<void>(`/admin/system/roles/${roleId}/permissions`, {
    method: 'PUT',
    body: JSON.stringify({ permissionIds })
  })
}

export interface MonitorStats {
  jvm: { maxMemory: number; totalMemory: number; freeMemory: number; usedMemory: number; usagePercent: number }
  system: { osName: string; osArch: string; javaVersion: string; availableProcessors: number }
  uptime: string; startTime: string
  settings: { totalCount: number }
}

export function getMonitorStats() {
  return request<MonitorStats>('/admin/system/monitor/stats')
}
