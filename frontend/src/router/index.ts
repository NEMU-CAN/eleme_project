import { createRouter, createWebHistory } from 'vue-router'

// 路由统一串起整条点餐流程：首页 -> 商家 -> 结算 -> 支付 -> 订单/我的。
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior() {
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { title: '首页' },
    },
    {
      path: '/businesses',
      name: 'businesses',
      component: () => import('@/views/BusinessListView.vue'),
      meta: { title: '商家列表' },
    },
    {
      path: '/merchant/:merchantId',
      name: 'merchant',
      component: () => import('@/views/MerchantView.vue'),
      meta: { title: '商家信息' },
    },
    {
      path: '/checkout/:orderId',
      name: 'checkout',
      component: () => import('@/views/CheckoutView.vue'),
      meta: { title: '确认订单' },
    },
    {
      path: '/payment/:orderId',
      name: 'payment',
      component: () => import('@/views/PaymentView.vue'),
      meta: { title: '在线支付' },
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/OrdersView.vue'),
      meta: { title: '我的订单' },
    },
    {
      path: '/me',
      name: 'me',
      component: () => import('@/views/MeView.vue'),
      meta: { title: '我的' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '用户登录' },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { title: '用户注册' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: '页面未找到' },
    },
  ],
})

// 根据当前路由自动更新浏览器标题，方便识别页面功能。
router.afterEach((to) => {
  const suffix = to.meta.title ? ` - ${String(to.meta.title)}` : ''
  document.title = `饿了么前端版${suffix}`
})

export default router
