import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory('/admin/'),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue') },

    { path: '/404', name: 'NotFound', component: () => import('@/views/error/404.vue') },
    { path: '/500', name: 'ServerError', component: () => import('@/views/error/500.vue') },

    {
      path: '/',
      component: () => import('@/layouts/index.vue'),
      meta: { requiresAuth: true },
      redirect: '/dashboards',  // 默认重定向到仪表盘
      children: [
        { path: 'dashboards', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue') },
        { path: 'products', name: 'ProductList', component: () => import('@/views/product/list.vue') },
        { path: 'products/create', name: 'ProductCreate', component: () => import('@/views/product/form.vue') },
        { path: 'products/edit/:id', name: 'ProductEdit', component: () => import('@/views/product/form.vue') },
        { path: 'brands', name: 'BrandList', component: () => import('@/views/brand/list.vue') },
        { path: 'orders', name: 'OrderList', component: () => import('@/views/order/list.vue') },
        { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/order/detail.vue') },
        { path: 'coupons', name: 'CouponList', component: () => import('@/views/coupon/list.vue') },
        { path: 'coupons/create', name: 'CouponCreate', component: () => import('@/views/coupon/form.vue') },
        { path: 'coupons/edit/:id', name: 'CouponEdit', component: () => import('@/views/coupon/form.vue') },
        { path: 'users', name: 'UserList', component: () => import('@/views/user/list.vue') },
        { path: 'users/:id', name: 'UserDetail', component: () => import('@/views/user/detail.vue') },
        { path: 'reviews', name: 'ReviewList', component: () => import('@/views/review/list.vue') },
        { path: 'refunds', name: 'RefundList', component: () => import('@/views/refund/list.vue') },
      ]
    },

    { path: '/:pathMatch(.*)*', redirect: '/404' },
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router