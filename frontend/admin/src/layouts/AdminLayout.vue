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
      <div class="brand">
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
        <n-input size="small" placeholder="搜索菜单..." clearable>
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
import { clearToken } from '../api/http'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()

const collapsed = ref(false)

function icon(svgContent: string) {
  return () => h('svg', {
    xmlns: 'http://www.w3.org/2000/svg',
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    'stroke-width': '2',
    style: 'width:20px;height:20px'
  }, svgContent.split('>').map((part, i) => {
    if (!part) return null
    const tagEnd = part.indexOf('<')
    if (tagEnd === -1) return null
    const tag = part.substring(tagEnd + 1, part.indexOf(' ', tagEnd + 1) > 0 ? part.indexOf(' ', tagEnd + 1) : part.indexOf('>', tagEnd + 1))
    if (!tag) return null
    const rest = part.substring(part.indexOf('>', tagEnd + 1) + 1)
    return h(tag, {}, rest)
  }))
}

const menuOptions: MenuOption[] = [
  { label: '仪表盘', key: '/', icon: icon('<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>') },
  { type: 'divider' },
  { label: '内容管理', key: 'cms-group', type: 'group' as const },
  { label: '页面管理', key: '/cms/pages', icon: icon('<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>') },
  { label: '媒体资源', key: '/assets', icon: icon('<rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>') },
  { type: 'divider' },
  { label: '商业管理', key: 'commerce-group', type: 'group' as const },
  { label: '套餐组', key: '/products/plan-groups', icon: icon('<path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/>') },
  { label: '套餐列表', key: '/products/plans', icon: icon('<path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/>') },
  { label: '订单管理', key: '/orders', icon: icon('<circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>') },
  { label: '支付记录', key: '/payments/transactions', icon: icon('<rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/>') },
  { type: 'divider' },
  { label: '系统管理', key: 'sys-group', type: 'group' as const },
  { label: '用户管理', key: '/system/users', icon: icon('<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>') },
  { label: '角色管理', key: '/system/roles', icon: icon('<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>') },
  { label: '系统设置', key: '/system/settings', icon: icon('<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>') }
]

const userMenuOptions = [
  { label: '个人设置', key: 'profile' },
  { type: 'divider' },
  { label: '退出登录', key: 'logout' }
]

const currentPageLabel = computed(() => {
  const list: MenuOption[] = [...menuOptions]
  let i = 0
  while (i < list.length) {
    const item = list[i]
    i++
    if (item && 'key' in item && typeof item.key === 'string' && item.key.startsWith('/') && route.path.startsWith(item.key)) {
      return item.label as string
    }
  }
  return ''
})

const activeKey = computed(() => {
  if (route.path.startsWith('/cms')) return '/cms/pages'
  if (route.path.startsWith('/products/plan-groups')) return '/products/plan-groups'
  if (route.path.startsWith('/products/plans')) return '/products/plans'
  if (route.path.startsWith('/orders')) return '/orders'
  if (route.path.startsWith('/payments')) return '/payments/transactions'
  if (route.path.startsWith('/assets')) return '/assets'
  if (route.path.startsWith('/system')) {
    if (route.path.startsWith('/system/settings')) return '/system/settings'
    if (route.path.startsWith('/system/users')) return '/system/users'
    if (route.path.startsWith('/system/roles')) return '/system/roles'
  }
  return '/'
})

function go(path: string) {
  router.push(path)
}

function handleUserMenu(key: string) {
  if (key === 'logout') {
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
