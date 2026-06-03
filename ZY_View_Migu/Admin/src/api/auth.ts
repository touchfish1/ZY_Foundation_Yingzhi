import { request, setToken } from './http'

export interface LoginResponse {
  accessToken: string
  expiresIn: number
  user: {
    id: number
    username: string
    nickname: string
    roles: string[]
  }
  permissions: string[]
}

// 管理员登录：使用用户名和密码获取 JWT 令牌，并自动保存
export async function login(username: string, password: string) {
  console.log('[API] login', { username })
  const data = await request<LoginResponse>('/admin/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  })
  console.log('[API] login success:', data.user.username)
  setToken(data.accessToken)
  return data
}

// 获取当前登录用户信息
export function me() {
  console.log('[API] me')
  return request<LoginResponse>('/admin/auth/me')
}
