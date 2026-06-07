<template>
  <n-layout class="shell" has-sider>
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :native-scrollbar="false"
      :collapsed="collapsed"
      @collapse="collapsed = true"
      @expand="collapsed = false"
      class="sidebar"
    >
      <div class="brand" :class="{ 'brand-collapsed': collapsed }">
        <div class="brand-icon">
          <n-icon size="28" color="#6366f1">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
          </n-icon>
        </div>
        <div v-if="!collapsed" class="brand-text">
          <div class="brand-name">ZHANGYUAN</div>
          <div class="brand-sub">内容管理中心</div>
        </div>
      </div>

      <div class="sidebar-search" v-if="!collapsed">
        <n-input size="small" placeholder="搜索菜单..." clearable v-model:value="menuSearch">
          <template #prefix>
            <n-icon>
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            </n-icon>
          </template>
        </n-input>
      </div>

      <n-menu
        :value="activeKey"
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        @update:value="go"
        :indent="collapsed ? 0 : 18"
      />
    </n-layout-sider>

    <n-layout class="main-area">
      <n-layout-header bordered class="topbar">
        <div class="topbar-left">
          <n-button quaternary circle @click="collapsed = !collapsed" class="collapse-btn">
            <template #icon>
              <n-icon size="20">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
              </n-icon>
            </template>
          </n-button>
          <n-breadcrumb>
            <n-breadcrumb-item>首页</n-breadcrumb-item>
            <n-breadcrumb-item v-if="currentPageLabel">{{ currentPageLabel }}</n-breadcrumb-item>
          </n-breadcrumb>
        </div>

        <div class="topbar-right">
          <n-tooltip trigger="hover">
            <template #trigger>
              <n-button quaternary circle @click="themeStore.toggle()">
                <template #icon>
                  <n-icon size="20">
                    <svg v-if="themeStore.dark" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
                    <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
                  </n-icon>
                </template>
              </n-button>
            </template>
            {{ themeStore.dark ? '日间模式' : '夜间模式' }}
          </n-tooltip>

          <n-dropdown trigger="click" :options="userMenuOptions" @select="handleUserMenu">
            <div class="user-avatar">
              <n-avatar round size="small" color="#6366f1">
                <n-icon size="16">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </n-icon>
              </n-avatar>
              <span class="user-name" v-if="!collapsed">管理员</span>
            </div>
          </n-dropdown>
        </div>
      </n-layout-header>

      <div class="content" style="padding: 24px;">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, ref, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NAvatar, NBreadcrumb, NBreadcrumbItem, NButton, NDropdown, NIcon, NInput,
  NLayout, NLayoutHeader, NLayoutSider, NMenu, NTooltip
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { useThemeStore } from '../stores/theme'
import { usePermissionStore } from '../stores/permission'
import type { MenuItem } from '../api/menu'
import { clearToken } from '../api/http'
import * as Icons from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const permissionStore = usePermissionStore()

const collapsed = ref(false)
const menuSearch = ref('')

const fallbackMenus: MenuItem[] = [
  { id: 1, parentId: null, name: '仪表盘', path: '/', icon: 'Dashboard', menuType: 'page', sortOrder: 1, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
  { id: 2, parentId: null, name: '订单管理', path: '/orders', icon: 'Cart', menuType: 'page', sortOrder: 6, status: 'enabled', permissionCodes: [], children: [
    { id: 21, parentId: 2, name: '订单列表', path: '/orders', icon: null, menuType: 'page', sortOrder: 1, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
    { id: 22, parentId: 2, name: '订阅管理', path: '/orders/subscriptions', icon: null, menuType: 'page', sortOrder: 2, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' }
  ], createdAt: '', updatedAt: '' },
  { id: 3, parentId: null, name: '支付记录', path: '/payments/transactions', icon: 'Wallet', menuType: 'page', sortOrder: 7, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
  { id: 4, parentId: null, name: '页面管理', path: '/cms/pages', icon: 'FileText', menuType: 'page', sortOrder: 3, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
  { id: 5, parentId: null, name: '资产管理', path: '/assets', icon: 'Image', menuType: 'page', sortOrder: 4, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
  { id: 6, parentId: null, name: '产品中心', path: '/products/plan-groups', icon: 'Layers', menuType: 'page', sortOrder: 5, status: 'enabled', permissionCodes: [], children: [
    { id: 61, parentId: 6, name: '套餐分组', path: '/products/plan-groups', icon: null, menuType: 'page', sortOrder: 1, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
    { id: 62, parentId: 6, name: '套餐列表', path: '/products/plans', icon: null, menuType: 'page', sortOrder: 2, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' }
  ], createdAt: '', updatedAt: '' },
  { id: 7, parentId: null, name: '系统管理', path: '/system/users', icon: 'Settings', menuType: 'page', sortOrder: 10, status: 'enabled', permissionCodes: [], children: [
    { id: 71, parentId: 7, name: '用户管理', path: '/system/users', icon: null, menuType: 'page', sortOrder: 1, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
    { id: 72, parentId: 7, name: '角色管理', path: '/system/roles', icon: null, menuType: 'page', sortOrder: 2, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
    { id: 73, parentId: 7, name: '权限管理', path: '/system/permissions', icon: null, menuType: 'page', sortOrder: 3, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
    { id: 74, parentId: 7, name: '菜单管理', path: '/system/menus', icon: null, menuType: 'page', sortOrder: 4, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
    { id: 75, parentId: 7, name: '系统监控', path: '/system/monitor', icon: null, menuType: 'page', sortOrder: 5, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
    { id: 76, parentId: 7, name: '系统设置', path: '/system/settings', icon: null, menuType: 'page', sortOrder: 6, status: 'enabled', permissionCodes: [], children: [], createdAt: '', updatedAt: '' },
    { id: 77, parentId: 7, name: '审计日志', path: '/system/logs', icon: null, menuType: 'page', sortOrder: 7, status: 'enabled', permissionCodes: ['system:log:list'], children: [], createdAt: '', updatedAt: '' },
    { id: 78, parentId: 7, name: '操作日志', path: '/system/operation-logs', icon: null, menuType: 'page', sortOrder: 8, status: 'enabled', permissionCodes: ['system:operation-log'], children: [], createdAt: '', updatedAt: '' },
    { id: 79, parentId: 7, name: '访问日志', path: '/system/access-logs', icon: null, menuType: 'page', sortOrder: 9, status: 'enabled', permissionCodes: ['system:access-log'], children: [], createdAt: '', updatedAt: '' }
  ], createdAt: '', updatedAt: '' }
]

// Icon mapping using @vicons/ionicons5
const iconMap: Record<string, any> = {
  Dashboard: Icons.Grid,
  BookOpen: Icons.BookOutline,
  Image: Icons.Image,
  FileText: Icons.DocumentText,
  Briefcase: Icons.Briefcase,
  Layers: Icons.Layers,
  Pricetags: Icons.Pricetags,
  Cart: Icons.Cart,
  Wallet: Icons.Wallet,
  Settings: Icons.Settings,
  People: Icons.People,
  ShieldCheckmark: Icons.ShieldCheckmark,
  Key: Icons.Key,
  Menu: Icons.Menu,
  Wrench: Icons.Construct,
  PersonOutline: Icons.PersonOutline,
  Code: Icons.Code,
  ColorPalette: Icons.ColorPalette,
  Globe: Icons.Globe,
  Chatbubbles: Icons.Chatbubbles,
  Newspaper: Icons.Newspaper,
  Server: Icons.Server,
  Build: Icons.Build,
  LogOut: Icons.LogOut,
}

function renderIcon(iconName: string | null) {
  if (!iconName || !iconMap[iconName]) return undefined
  return () => h(NIcon, null, { default: () => h(iconMap[iconName]) })
}

const menuOptions = computed<MenuOption[]>(() => {
  const menus = permissionStore.menus.length > 0 ? permissionStore.menus : fallbackMenus
  if (!menuSearch.value) {
    return buildMenuOptions(menus)
  }
  const q = menuSearch.value.toLowerCase()
  function filterTree(items: MenuItem[]): MenuItem[] {
    return items
      .filter(item => item.menuType !== 'button' && item.status === 'enabled')
      .filter(item => {
        if (item.name?.toLowerCase().includes(q)) return true
        if (item.children && item.children.length > 0) {
          const filtered = filterTree(item.children)
          if (filtered.length > 0) return true
        }
        return false
      })
      .map(item => {
        if (item.children && item.children.length > 0) {
          return { ...item, children: filterTree(item.children) }
        }
        return item
      })
  }
  return buildMenuOptions(filterTree(menus))
})

function hasMenuPermission(item: MenuItem): boolean {
  if (!item.permissionCodes || item.permissionCodes.length === 0) return true
  return permissionStore.hasAnyPermission(item.permissionCodes)
}

function buildMenuOptions(items: MenuItem[]): MenuOption[] {
  if (!items || items.length === 0) return []

  return items
    .filter(item => item.menuType !== 'button' && item.status === 'enabled' && hasMenuPermission(item))
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map(item => {
      const option: MenuOption = {
        key: item.path || `menu-${item.id}`,
        label: item.name,
      }

      if (item.icon) {
        option.icon = renderIcon(item.icon)
      }

      if (item.children && item.children.length > 0) {
        const filteredChildren = item.children.filter(
          c => c.menuType !== 'button' && c.status === 'enabled' && hasMenuPermission(c)
        )
        if (filteredChildren.length > 0) {
          if (item.menuType === 'group') {
            option.type = 'group'
          }
          option.children = buildMenuOptions(filteredChildren)
        }
      }

      return option
    })
}

const userMenuOptions = [
  { label: '个人设置', key: 'profile' },
  { type: 'divider' },
  { label: '退出登录', key: 'logout' }
]

function flattenMenuOptions(options: MenuOption[]): MenuOption[] {
  const result: MenuOption[] = []
  for (const option of options) {
    if (!option || !option.key) continue
    result.push(option)
    if (option.children && Array.isArray(option.children)) {
      result.push(...flattenMenuOptions(option.children as MenuOption[]))
    }
  }
  return result
}

const currentPageLabel = computed(() => {
  const flat = flattenMenuOptions(menuOptions.value)
  let bestItem: any = null
  let bestLength = -1
  for (const item of flat) {
    if (typeof item.key === 'string' && item.key.startsWith('/') && route.path.startsWith(item.key)) {
      if (item.key.length > bestLength) {
        bestLength = item.key.length
        bestItem = item
      }
    }
  }
  return bestItem?.label ?? ''
})

const activeKey = computed(() => {
  const flat = flattenMenuOptions(menuOptions.value)
  let bestMatch = '/'
  let bestLength = -1
  for (const item of flat) {
    if (typeof item.key === 'string' && item.key.startsWith('/') && route.path.startsWith(item.key)) {
      if (item.key.length > bestLength) {
        bestLength = item.key.length
        bestMatch = item.key
      }
    }
  }
  return bestMatch
})

function go(path: string) {
  router.push(path)
}

function handleUserMenu(key: string) {
  if (key === 'logout') {
    permissionStore.reset()
    clearToken()
    router.push('/login')
  }
}
</script>

<style scoped>
.shell {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  display: flex;
  flex-direction: column;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  border-bottom: 1px solid var(--n-border-color);
  flex-shrink: 0;
}

.brand-name {
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.06em;
  line-height: 1.2;
}

.brand-sub {
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.08em;
}

.brand-collapsed {
  justify-content: center;
  padding: 0;
  gap: 0;
}

.sidebar-search {
  padding: 12px 14px;
  flex-shrink: 0;
}

.topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.collapse-btn {
  margin-right: 4px;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-avatar:hover {
  background: rgba(99, 102, 241, 0.08);
}

.user-name {
  font-size: 13px;
  font-weight: 600;
}

.main-area {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  overflow: hidden;
}

.content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
