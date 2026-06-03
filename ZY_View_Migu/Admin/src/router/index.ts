import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../api/http'
import AdminLayout from '../layouts/AdminLayout.vue'
import Assets from '../pages/assets/Assets.vue'
import CmsPageEdit from '../pages/cms/CmsPageEdit.vue'
import CmsPages from '../pages/cms/CmsPages.vue'
import CmsVersions from '../pages/cms/CmsVersions.vue'
import Dashboard from '../pages/Dashboard.vue'
import Login from '../pages/Login.vue'
import Orders from '../pages/orders/Orders.vue'
import PlanGroups from '../pages/products/PlanGroups.vue'
import Roles from '../pages/system/Roles.vue'
import Settings from '../pages/system/Settings.vue'
import Users from '../pages/system/Users.vue'
import Plans from '../pages/products/Plans.vue'
import Transactions from '../pages/payments/Transactions.vue'

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
        { path: 'products/plans', component: Plans },
        { path: 'payments/transactions', component: Transactions },
        { path: 'system/users', component: Users },
        { path: 'system/roles', component: Roles },
        { path: 'system/settings', component: Settings }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !getToken()) {
    return '/login'
  }
})
