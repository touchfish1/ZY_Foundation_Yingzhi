import { request } from './http'

export interface AssetFile {
  id: number
  url: string
  originalName: string
  contentType: string
  sizeBytes: number
}

// 上传文件（使用 FormData  multipart 方式）
export function uploadFile(file: File) {
  console.log('[API] uploadFile', { name: file.name, size: file.size, type: file.type })
  const formData = new FormData()
  formData.append('file', file)
  const token = localStorage.getItem('zhangyuan_admin_token')
  return fetch('/admin/assets/files', {
    method: 'POST',
    headers: { 'Authorization': token ? `Bearer ${token}` : '' },
    body: formData
  }).then(async res => {
    const payload = await res.json()
    if (!res.ok || payload?.code !== 0) throw new Error(payload?.message || 'Upload failed')
    console.log('[API] uploadFile success:', payload.data)
    return payload.data as AssetFile
  })
}

// 获取已上传的文件列表
export function listFiles() {
  console.log('[API] listFiles')
  return request<AssetFile[]>('/admin/assets/files')
}
