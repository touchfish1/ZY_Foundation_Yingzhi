import { request } from './http'
import type { PageResponse } from '../types/common'

export interface PermissionInfo {
  id: number
  code: string
  name: string
  module: string
  createdAt: string
}

export function listPermissions(
  page = 1,
  pageSize = 20,
  modules?: string[],
  keyword?: string
): Promise<PageResponse<PermissionInfo>> {
  const params = new URLSearchParams()
  params.set('page', String(page))
  params.set('pageSize', String(pageSize))
  if (modules && modules.length > 0) {
    modules.forEach(m => params.append('modules', m))
  }
  if (keyword) {
    params.set('keyword', keyword)
  }
  return request<PageResponse<PermissionInfo>>(`/admin/system/permissions?${params}`)
}

export function listPermissionModules() {
  return request<string[]>('/admin/system/permissions/modules')
}

export function createPermission(data: { code: string; name: string; module: string }) {
  return request<PermissionInfo>('/admin/system/permissions', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function updatePermission(id: number, data: { code: string; name: string; module: string }) {
  return request<PermissionInfo>(`/admin/system/permissions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function deletePermission(id: number) {
  return request<void>(`/admin/system/permissions/${id}`, { method: 'DELETE' })
}
