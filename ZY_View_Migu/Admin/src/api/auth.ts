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

export async function login(username: string, password: string) {
  const data = await request<LoginResponse>('/admin/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  })
  setToken(data.accessToken)
  return data
}

export function me() {
  return request<LoginResponse>('/admin/auth/me')
}
