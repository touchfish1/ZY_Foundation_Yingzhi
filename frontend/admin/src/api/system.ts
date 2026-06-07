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

export interface AuditLog {
  id: number
  userId: number
  username: string
  action: string
  detail: string
  ipAddress: string
  createdAt: string
}

// 获取审计日志列表（分页）
export function listLogs(params?: { page?: number, pageSize?: number, userId?: number, action?: string, startDate?: string, endDate?: string }) {
  const qs = new URLSearchParams()
  if (params?.page) qs.set('page', String(params.page))
  if (params?.pageSize) qs.set('pageSize', String(params.pageSize))
  if (params?.userId) qs.set('userId', String(params.userId))
  if (params?.action) qs.set('action', params.action)
  if (params?.startDate) qs.set('startDate', params.startDate)
  if (params?.endDate) qs.set('endDate', params.endDate)
  const query = qs.toString()
  return request<PageResponse<AuditLog>>(`/api/logs${query ? '?' + query : ''}`)
}

// Operation log types matching backend OperationType enum
export type OperationType = 'CREATE' | 'UPDATE' | 'DELETE' | 'QUERY' | 'LOGIN' | 'EXPORT' | 'IMPORT' | 'OTHER'

// Resource types matching backend ResourceType enum
export type ResourceType = 'CMS_PAGE' | 'PRODUCT_PLAN_GROUP' | 'PRODUCT_PLAN' | 'PRODUCT_PRICE' | 'PRODUCT_FEATURE' | 'ORDER' | 'ASSET_FILE' | 'ADMIN_USER' | 'ADMIN_ROLE' | 'SYSTEM_SETTING' | 'OTHER'

export interface OperationLog {
  id: number
  operatorId: number
  operatorName: string
  operationType: OperationType
  resourceType: ResourceType
  resourceId: string
  detail: string
  ipAddress: string
  success: boolean
  errorMessage: string | null
  createdAt: string
}

// 获取操作日志列表（分页）
export function listOperationLogs(params?: {
  page?: number
  pageSize?: number
  operatorId?: number
  resourceType?: ResourceType
  operationType?: OperationType
  startTime?: string
  endTime?: string
}) {
  const qs = new URLSearchParams()
  if (params?.page) qs.set('page', String(params.page))
  if (params?.pageSize) qs.set('pageSize', String(params.pageSize))
  if (params?.operatorId) qs.set('operatorId', String(params.operatorId))
  if (params?.resourceType) qs.set('resourceType', params.resourceType)
  if (params?.operationType) qs.set('operationType', params.operationType)
  if (params?.startTime) qs.set('startTime', params.startTime)
  if (params?.endTime) qs.set('endTime', params.endTime)
  const query = qs.toString()
  return request<PageResponse<OperationLog>>(`/admin/operation-logs${query ? '?' + query : ''}`)
}

export interface AccessLog {
  id: number
  requestMethod: string
  requestPath: string
  responseStatus: number
  userId: number | null
  username: string | null
  ipAddress: string
  userAgent: string
  durationMs: number
  createdAt: string
}

export function listAccessLogs(params?: {
  page?: number
  pageSize?: number
  method?: string
  path?: string
  status?: number
  userId?: number
  startTime?: string
  endTime?: string
}) {
  const qs = new URLSearchParams()
  if (params?.page) qs.set('page', String(params.page))
  if (params?.pageSize) qs.set('pageSize', String(params.pageSize))
  if (params?.method) qs.set('method', params.method)
  if (params?.path) qs.set('path', params.path)
  if (params?.status) qs.set('status', String(params.status))
  if (params?.userId) qs.set('userId', String(params.userId))
  if (params?.startTime) qs.set('startTime', params.startTime)
  if (params?.endTime) qs.set('endTime', params.endTime)
  const query = qs.toString()
  return request<PageResponse<AccessLog>>(`/admin/access-logs${query ? '?' + query : ''}`)
}

// 用户余额充值
export function rechargeBalance(userId: number, payload: { amount: number, remark?: string }) {
  return request<void>(`/api/balance/${userId}/recharge`, { method: 'POST', body: JSON.stringify(payload) })
}
