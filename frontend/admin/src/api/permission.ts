import { request } from './http'

export interface PermissionInfo {
  id: number
  code: string
  name: string
  module: string
  createdAt: string
}

export function listPermissions(module?: string) {
  const params = module ? `?module=${encodeURIComponent(module)}` : ''
  return request<PermissionInfo[]>(`/admin/system/permissions${params}`)
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
