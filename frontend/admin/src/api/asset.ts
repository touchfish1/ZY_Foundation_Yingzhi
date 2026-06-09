import { request, getToken } from './http'
import type { PageResponse } from '../types/common'
import type { AssetFile } from '@shared/types/asset'
export type { AssetFile }

export async function uploadFile(file: File): Promise<AssetFile> {
  const formData = new FormData()
  formData.append('file', file)
  const token = getToken()
  const res = await fetch('/admin/assets/files', {
    method: 'POST',
    headers: token ? { 'Authorization': `Bearer ${token}` } : {},
    body: formData
  })
  const payload = await res.json()
  if (!res.ok || payload?.code !== 0) throw new Error(payload?.message || 'Upload failed')
  return payload.data as AssetFile
}

export function listFiles(page = 1, pageSize = 20) {
  return request<PageResponse<AssetFile>>(`/admin/assets/files?page=${page}&pageSize=${pageSize}`)
}
