import { getToken } from './http'

export interface Setting {
  key: string
  value: string
  updatedAt: string
}

// 获取系统设置列表
export async function listSettings(): Promise<Setting[]> {
  console.log('[API] listSettings')
  const token = getToken()
  const response = await fetch('/admin/system/settings', {
    headers: { 'Authorization': token ? `Bearer ${token}` : '', 'Content-Type': 'application/json' }
  })
  const payload = await response.json()
  if (!response.ok) throw new Error(payload?.message || `Request failed: ${response.status}`)
  console.log('[API] listSettings success:', payload)
  return payload
}

// 更新指定系统设置项的值
export async function updateSetting(key: string, value: string): Promise<Setting> {
  console.log('[API] updateSetting', { key, value })
  const token = getToken()
  const response = await fetch(`/admin/system/settings/${key}`, {
    method: 'PUT',
    headers: { 'Authorization': token ? `Bearer ${token}` : '', 'Content-Type': 'application/json' },
    body: JSON.stringify({ value })
  })
  const payload = await response.json()
  if (!response.ok) throw new Error(payload?.message || `Request failed: ${response.status}`)
  console.log('[API] updateSetting success:', payload)
  return payload
}
