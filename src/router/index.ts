// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
    },
      {
          path: '/register',
          name: 'register',
          component: () => import('@/views/auth/RegisterView.vue'),
      },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/rooms',
      name: 'rooms',
      component: () => import('@/views/rooms/RoomListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/reservations/create',
      name: 'create-reservation',
      component: () => import('@/views/reservations/CreateView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/my-reservations',
      name: 'my-reservations',
      component: () => import('@/views/reservations/MyListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin/approvals',
      name: 'approval-list',
      component: () => import('@/views/approvals/AllListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/notices',
      name: 'notice-center',
      component: () => import('@/views/notices/NoticeCenterView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/rooms/week-view',
      name: 'week-view',
      component: () => import('@/views/rooms/WeekView.vue'),
      meta: { requiresAuth: true },
    },
    // 其他路由...
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  userStore.initFromStorage()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    // 需要登录但未登录，去登录页
    next('/login')
  } /*else if (to.path === '/login' && userStore.isLoggedIn) {
    // 已登录却访问登录页，重定向到首页
    // 【关键修改】不再区分管理员和用户，都去 /rooms
    next('/rooms')
    // 未来后端准备好后，可以恢复为：
    // next(userStore.isAdmin ? '/admin/dashboard' : '/dashboard')
  } */ else {
    // 其他情况正常放行
    next()
  }
})

export default router
