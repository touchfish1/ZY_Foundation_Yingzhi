import { request } from './http'

export interface MenuItem {
  id: number
  parentId: number | null
  name: string
  path: string | null
  icon: string | null
  menuType: 'group' | 'page' | 'button'
  sortOrder: number
  status: string
  permissionCodes: string[]
  children: MenuItem[]
  createdAt: string
  updatedAt: string
}

export function fetchUserMenus() {
  return request<MenuItem[]>('/admin/auth/menus')
}

// CRUD endpoints for menu management
export function fetchAllMenus() {
  return request<MenuItem[]>('/admin/system/menus')
}

export function createMenu(data: {
  parentId?: number | null
  name: string
  path?: string | null
  icon?: string | null
  menuType: string
  sortOrder?: number
  status?: string
  permissionCodes?: string[]
}) {
  return request<MenuItem>('/admin/system/menus', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function updateMenu(id: number, data: {
  parentId?: number | null
  name: string
  path?: string | null
  icon?: string | null
  menuType: string
  sortOrder?: number
  status?: string
  permissionCodes?: string[]
}) {
  return request<MenuItem>(`/admin/system/menus/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export function deleteMenu(id: number) {
  return request<void>(`/admin/system/menus/${id}`, { method: 'DELETE' })
}

export function updateMenuSort(sortList: { id: number; sortOrder: number }[]) {
  return request<void>('/admin/system/menus/sort', {
    method: 'PUT',
    body: JSON.stringify(sortList)
  })
}
