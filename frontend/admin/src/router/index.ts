import { createRouter, createWebHistory } from 'vue-router'
import { getToken, clearToken } from '../api/http'
import { usePermissionStore } from '../stores/permission'
import AdminLayout from '../layouts/AdminLayout.vue'
import Login from '../pages/Login.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', component: () => import('../pages/Dashboard.vue') },
        { path: 'assets', component: () => import('../pages/assets/Assets.vue') },
        { path: 'cms/pages', component: () => import('../pages/cms/CmsPages.vue') },
        { path: 'cms/pages/:id/edit', component: () => import('../pages/cms/CmsPageEdit.vue') },
        { path: 'cms/pages/:id/versions', component: () => import('../pages/cms/CmsVersions.vue') },
        { path: 'cms/block-definitions', component: () => import('../pages/cms/CmsBlockDefinitions.vue') },
        { path: 'cms/block-definitions/new', component: () => import('../pages/cms/CmsBlockDefinitionEdit.vue') },
        { path: 'cms/block-definitions/:id/edit', component: () => import('../pages/cms/CmsBlockDefinitionEdit.vue') },
        { path: 'products/plan-groups', component: () => import('../pages/products/PlanGroups.vue') },
        { path: 'orders', component: () => import('../pages/orders/Orders.vue') },
        { path: 'orders/:orderNo', component: () => import('../pages/orders/[orderNo].vue'), meta: { title: '订单详情' } },
        { path: 'orders/subscriptions', component: () => import('../pages/orders/Subscriptions.vue'), meta: { title: '订阅管理' } },
        { path: 'products/plans', component: () => import('../pages/products/Plans.vue') },
        { path: 'payments/transactions', component: () => import('../pages/payments/Transactions.vue') },
        { path: 'payments/:paymentNo', component: () => import('../pages/payments/[paymentNo].vue'), meta: { title: '支付详情' } },
        { path: 'system/users', component: () => import('../pages/system/Users.vue') },
        { path: 'system/monitor', component: () => import('../pages/system/Monitor.vue') },
        { path: 'system/reports', component: () => import('../pages/system/Reports.vue'), meta: { title: '数据报表', permissions: ['system:monitor'] } },
        { path: 'system/roles', component: () => import('../pages/system/Roles.vue') },
        { path: 'system/settings', component: () => import('../pages/system/Settings.vue') },
        { path: 'orders/usage', component: () => import('../pages/orders/Usage.vue'), meta: { title: '用量管理', permissions: ['order:usage:list'] } },
        { path: 'system/logs', component: () => import('../pages/system/Logs.vue'), meta: { title: '审计日志', permissions: ['system:log:list'] } },
        { path: 'system/operation-logs', component: () => import('../pages/system/OperationLogs.vue'), meta: { title: '操作日志', permissions: ['system:operation-log'] } },
        { path: 'system/access-logs', component: () => import('../pages/system/AccessLogs.vue'), meta: { title: '访问日志', permissions: ['system:access-log'] } },
        { path: 'system/permissions', component: () => import('../pages/system/Permissions.vue'), meta: { permissions: ['system:permission:list'] } },
        { path: 'system/menus', component: () => import('../pages/system/Menus.vue'), meta: { permissions: ['system:menu:list'] } }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  if (to.path === '/login') return true
  if (!getToken()) return '/login'

  // Lazy-load permission store
  const store = usePermissionStore()
  if (!store.loaded) {
    try {
      await store.fetchUserInfo()
    } catch {
      clearToken()
      return '/login'
    }
  }

  // Check route-level permissions
  const routePerms = to.meta?.permissions as string[] | undefined
  if (routePerms?.length && !store.hasAnyPermission(routePerms)) {
    // Redirect to dashboard if user doesn't have permission
    return '/'
  }

  return true
})
