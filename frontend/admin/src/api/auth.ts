import { request, setToken } from './http'
import type { LoginResponse } from '@shared/types/auth'
export type { LoginResponse }

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
