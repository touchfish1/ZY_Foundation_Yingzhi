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

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  status: string
  createdAt: string
}

export interface RoleInfo {
  id: number
  code: string
  name: string
  createdAt: string
}
