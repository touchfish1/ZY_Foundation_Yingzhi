import { defineStore } from 'pinia'
import { ref } from 'vue'
import { me } from '../api/auth'
import { fetchUserMenus, type MenuItem } from '../api/menu'

export const usePermissionStore = defineStore('permission', () => {
  const permissions = ref<string[]>([])
  const menus = ref<MenuItem[]>([])
  const roles = ref<string[]>([])
  const loaded = ref(false)

  async function fetchUserInfo() {
    try {
      const [userInfo, userMenus] = await Promise.all([
        me(),
        fetchUserMenus()
      ])
      permissions.value = userInfo.permissions ?? []
      roles.value = userInfo.user?.roles ?? []
      menus.value = userMenus ?? []
      loaded.value = true
      console.log('[PermissionStore] Loaded permissions:', permissions.value.length, 'menus:', menus.value.length)
    } catch (e) {
      console.error('[PermissionStore] Failed to fetch user info:', e)
      throw e
    }
  }

  function hasPermission(code: string): boolean {
    if (!code) return true
    return permissions.value.includes(code)
  }

  function hasAnyPermission(codes: string[]): boolean {
    if (!codes.length) return true
    return codes.some(code => permissions.value.includes(code))
  }

  function hasAllPermissions(codes: string[]): boolean {
    if (!codes.length) return true
    return codes.every(code => permissions.value.includes(code))
  }

  function reset() {
    permissions.value = []
    menus.value = []
    roles.value = []
    loaded.value = false
  }

  return {
    permissions, menus, roles, loaded,
    fetchUserInfo, hasPermission, hasAnyPermission, hasAllPermissions, reset
  }
})
