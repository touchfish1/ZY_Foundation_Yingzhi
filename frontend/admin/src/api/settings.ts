import { request } from './http'

export interface Setting {
  key: string
  value: string
  updatedAt: string
}

// 获取系统设置列表
export function listSettings(): Promise<Setting[]> {
  return request<Setting[]>('/admin/system/settings')
}

// 批量更新系统设置
export function batchUpdateSettings(settings: Record<string, string>): Promise<void> {
  return request<void>('/admin/system/settings', { method: 'PUT', body: JSON.stringify({ settings }) })
}

// 更新指定系统设置项的值
export function updateSetting(key: string, value: string): Promise<Setting> {
  return request<Setting>(`/admin/system/settings/${key}`, { method: 'PUT', body: JSON.stringify({ value }) })
}
