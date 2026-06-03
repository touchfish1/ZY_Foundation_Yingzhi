<template>
  <n-layout has-sider class="shell">
    <n-layout-sider bordered collapse-mode="width" :width="236" :native-scrollbar="false">
      <div class="brand">
        <div class="brand-icon">◇</div>
        <div class="brand-text">
          <strong>ZHANGYUAN</strong>
          <span>CMS</span>
        </div>
      </div>
      <n-menu :options="menuOptions" :value="activeKey" @update:value="go" />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="header">
        <div class="header-left">
          <n-breadcrumb>
            <n-breadcrumb-item>内容管理后台</n-breadcrumb-item>
            <n-breadcrumb-item>{{ currentPageLabel }}</n-breadcrumb-item>
          </n-breadcrumb>
        </div>
        <div class="header-right">
          <n-tooltip trigger="hover">
            <template #trigger>
              <n-button quaternary circle @click="logout">
                <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg></n-icon></template>
              </n-button>
            </template>
            退出登录
          </n-tooltip>
        </div>
      </n-layout-header>
      <n-layout-content class="content" :native-scrollbar="false">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NBreadcrumb, NBreadcrumbItem, NButton, NIcon, NLayout, NLayoutContent, NLayoutHeader, NLayoutSider, NMenu, NTooltip } from 'naive-ui'
import { clearToken } from '../api/http'

const route = useRoute()
const router = useRouter()

const menuOptions = [
  { label: '仪表盘', key: '/' },
  { label: '页面管理', key: '/cms/pages' },
  { label: '套餐组', key: '/products/plan-groups' },
  { label: '套餐列表', key: '/products/plans' },
  { label: '订单管理', key: '/orders' },
  { label: '支付记录', key: '/payments/transactions' },
  { label: '媒体资源', key: '/assets' },
  { label: '用户管理', key: '/system/users' },
  { label: '角色管理', key: '/system/roles' },
  { label: '系统设置', key: '/system/settings' }
]

const currentPageLabel = computed(() => {
  const found = menuOptions.find(m => route.path.startsWith(m.key as string))
  return found ? found.label : ''
})

const activeKey = computed(() => {
  if (route.path.startsWith('/cms')) return '/cms/pages'
  if (route.path.startsWith('/products/plan-groups')) return '/products/plan-groups'
  if (route.path.startsWith('/products/plans')) return '/products/plans'
  if (route.path.startsWith('/orders')) return '/orders'
  if (route.path.startsWith('/payments')) return '/payments/transactions'
  if (route.path.startsWith('/assets')) return '/assets'
  if (route.path.startsWith('/system')) return route.path.startsWith('/system/settings') ? '/system/settings' : route.path.startsWith('/system/users') ? '/system/users' : '/system/roles'
  return '/'
})

function go(path: string) {
  router.push(path)
}

function logout() {
  clearToken()
  router.push('/login')
}
</script>

<style scoped>
.shell {
  height: 100vh;
}
.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 22px;
  border-bottom: 1px solid #e5e7eb;
}
.brand-icon {
  font-size: 24px;
  color: #2563eb;
}
.brand-text strong {
  display: block;
  font-size: 15px;
  letter-spacing: 0.06em;
}
.brand-text span {
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.1em;
}
.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: white;
}
.header-left {
  display: flex;
  align-items: center;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.content {
  padding: 24px;
  min-height: calc(100vh - 56px);
}
</style>
