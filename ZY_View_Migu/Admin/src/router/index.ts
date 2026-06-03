import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../api/http'
import AdminLayout from '../layouts/AdminLayout.vue'
import CmsPageEdit from '../pages/cms/CmsPageEdit.vue'
import CmsPages from '../pages/cms/CmsPages.vue'
import Dashboard from '../pages/Dashboard.vue'
import Login from '../pages/Login.vue'
import PlanGroups from '../pages/products/PlanGroups.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', component: Dashboard },
        { path: 'cms/pages', component: CmsPages },
        { path: 'cms/pages/:id/edit', component: CmsPageEdit },
        { path: 'products/plan-groups', component: PlanGroups }
      ]
    }
  ]
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !getToken()) {
    return '/login'
  }
})
