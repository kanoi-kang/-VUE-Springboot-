import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const router = createRouter({
  history: createWebHistory('/mall/'),
  routes: [
    {
      path: '/',
      component: DefaultLayout,
      children: [
        { path: '/', name: 'Home', component: () => import('@/views/home/index.vue') },
        { path: '/products', name: 'ProductList', component: () => import('@/views/product/list.vue') },
        { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/product/detail.vue') },
        { path: '/about', name: 'About', component: () => import('@/views/about/index.vue') },
      ]
    },
    {
      path: '/',
      component: DefaultLayout,
      children: [
        { path: '/cart', name: 'Cart', component: () => import('@/views/cart/index.vue'), meta: { requiresAuth: true } },
        { path: '/checkout', name: 'Checkout', component: () => import('@/views/order/checkout.vue'), meta: { requiresAuth: true } },
        { path: '/pay/:orderId', name: 'Pay', component: () => import('@/views/order/pay.vue'), meta: { requiresAuth: true } },
        { path: '/user', name: 'User', component: () => import('@/views/user/index.vue'), meta: { requiresAuth: true } },
        { path: '/orders', name: 'OrderList', component: () => import('@/views/order/list.vue'), meta: { requiresAuth: true } },
        { path: '/order/:id', name: 'OrderDetail', component: () => import('@/views/order/detail.vue'), meta: { requiresAuth: true } },
        { path: '/refund/:orderId?', name: 'Refund', component: () => import('@/views/order/refund.vue'), meta: { requiresAuth: true } },
        { path: '/collections', name: 'Collections', component: () => import('@/views/user/collections.vue'), meta: { requiresAuth: true } },
        { path: '/coupons', name: 'Coupons', component: () => import('@/views/user/coupons.vue'), meta: { requiresAuth: true } },
        { path: '/coupon-center', name: 'CouponCenter', component: () => import('@/views/user/coupon-center.vue') },
        { path: '/addresses', name: 'Addresses', component: () => import('@/views/user/addresses.vue'), meta: { requiresAuth: true } },
        { path: '/address-edit/:id?', name: 'AddressEdit', component: () => import('@/views/user/address-edit.vue'), meta: { requiresAuth: true } },
        { path: '/review', name: 'Review', component: () => import('@/views/user/review.vue'), meta: { requiresAuth: true } },
      ]
    },
    { path: '/login', name: 'Login', component: () => import('@/views/auth/login.vue') },
    { path: '/register', name: 'Register', component: () => import('@/views/auth/register.vue') },

    { path: '/404', name: 'NotFound', component: () => import('@/views/error/404.vue') },
    { path: '/500', name: 'ServerError', component: () => import('@/views/error/500.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/404' },
  ]
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
