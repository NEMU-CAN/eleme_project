import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '@/services/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true, title: '管理员登录' },
    },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      children: [
        { path: '', redirect: '/dashboard' },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '运营总览', description: '平台核心数据与最近订单' },
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('@/views/UsersView.vue'),
          meta: { title: '用户管理', description: '查看与维护平台用户资料' },
        },
        {
          path: 'businesses',
          name: 'businesses',
          component: () => import('@/views/BusinessesView.vue'),
          meta: { title: '商家管理', description: '维护商家资料、分类与营业状态' },
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('@/views/OrdersView.vue'),
          meta: { title: '订单查询', description: '只读查看全平台订单与明细' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  if (!to.meta.guest && !isAuthenticated.value) return { name: 'login' }
  if (to.name === 'login' && isAuthenticated.value) return { name: 'dashboard' }
})

export default router
