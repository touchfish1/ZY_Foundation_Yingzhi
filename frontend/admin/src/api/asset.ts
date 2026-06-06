import { request, getToken } from './http'

export interface AssetFile {
  id: number
  url: string
  originalName: string
  contentType: string
  sizeBytes: number
}

// 上传文件（使用 FormData multipart 方式，避免 JSON Content-Type）
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

// 获取已上传的文件列表
export function listFiles() {
  console.log('[API] listFiles')
  return request<AssetFile[]>('/admin/assets/files')
}
