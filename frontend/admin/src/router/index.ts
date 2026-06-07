import { createRouter, createWebHistory } from 'vue-router'
import { getToken, clearToken } from '../api/http'
import { usePermissionStore } from '../stores/permission'
import AdminLayout from '../layouts/AdminLayout.vue'
import Assets from '../pages/assets/Assets.vue'
import CmsPageEdit from '../pages/cms/CmsPageEdit.vue'
import CmsPages from '../pages/cms/CmsPages.vue'
import CmsVersions from '../pages/cms/CmsVersions.vue'
import Dashboard from '../pages/Dashboard.vue'
import Login from '../pages/Login.vue'
import Menus from '../pages/system/Menus.vue'
import Monitor from '../pages/system/Monitor.vue'
import Orders from '../pages/orders/Orders.vue'
import Permissions from '../pages/system/Permissions.vue'
import PlanGroups from '../pages/products/PlanGroups.vue'
import Roles from '../pages/system/Roles.vue'
import Settings from '../pages/system/Settings.vue'
import Users from '../pages/system/Users.vue'
import Plans from '../pages/products/Plans.vue'
import Transactions from '../pages/payments/Transactions.vue'

const Usage = () => import('../pages/orders/Usage.vue')
const Logs = () => import('../pages/system/Logs.vue')
const OperationLogs = () => import('../pages/system/OperationLogs.vue')
const AccessLogs = () => import('../pages/system/AccessLogs.vue')

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', component: Dashboard },
        { path: 'assets', component: Assets },
        { path: 'cms/pages', component: CmsPages },
        { path: 'cms/pages/:id/edit', component: CmsPageEdit },
        { path: 'cms/pages/:id/versions', component: CmsVersions },
        { path: 'products/plan-groups', component: PlanGroups },
        { path: 'orders', component: Orders },
        { path: 'orders/:orderNo', component: () => import('../pages/orders/[orderNo].vue'), meta: { title: '订单详情' } },
        { path: 'orders/subscriptions', component: () => import('../pages/orders/Subscriptions.vue'), meta: { title: '订阅管理' } },
        { path: 'products/plans', component: Plans },
        { path: 'payments/transactions', component: Transactions },
        { path: 'payments/:paymentNo', component: () => import('../pages/payments/[paymentNo].vue'), meta: { title: '支付详情' } },
        { path: 'system/users', component: Users },
        { path: 'system/monitor', component: Monitor },
        { path: 'system/roles', component: Roles },
        { path: 'system/settings', component: Settings },
        { path: 'orders/usage', component: Usage, meta: { title: '用量管理', permissions: ['order:usage:list'] } },
        { path: 'system/logs', component: Logs, meta: { title: '审计日志', permissions: ['system:log:list'] } },
        { path: 'system/operation-logs', component: OperationLogs, meta: { title: '操作日志', permissions: ['system:operation-log'] } },
        { path: 'system/access-logs', component: AccessLogs, meta: { title: '访问日志', permissions: ['system:access-log'] } },
        { path: 'system/permissions', component: Permissions, meta: { permissions: ['system:permission:list'] } },
        { path: 'system/menus', component: Menus, meta: { permissions: ['system:menu:list'] } }
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
