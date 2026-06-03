const TOKEN_KEY = 'zhangyuan_admin_token'

// 从 localStorage 获取 JWT 令牌
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

// 将 JWT 令牌保存到 localStorage
export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

// 从 localStorage 移除 JWT 令牌（登出）
export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export class ApiError extends Error {
  code: number
  constructor(code: number, message: string) {
    super(message)
    this.code = code
    this.name = 'ApiError'
  }
}

// 通用 HTTP 请求封装：自动携带令牌、统一错误处理、解析统一响应格式
export async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  console.log(`[API] ${options.method || 'GET'} ${url}`, options.body ? { body: JSON.parse(options.body as string) } : '')
  const token = getToken()
  const headers = new Headers(options.headers)
  headers.set('Content-Type', 'application/json')
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(url, { ...options, headers })
  const payload = await response.json().catch(() => null)

  if (!response.ok) {
    console.error(`[API] ${url} failed:`, response.status, payload?.message)
    throw new ApiError(response.status, payload?.message || `请求失败 (${response.status})`)
  }
  if (payload?.code !== 0) {
    console.error(`[API] ${url} biz error:`, payload?.code, payload?.message)
    throw new ApiError(payload?.code || -1, payload?.message || '操作失败')
  }
  console.log(`[API] ${url} success:`, payload?.data)
  return payload.data as T
}
