import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores'

const routes: RouteRecordRaw[] = [
  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guest: true },
  },
  // 注册页
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { guest: true },
  },
  // 主布局（带导航栏）
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    children: [
      // 首页
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
      },
      // 物品列表
      {
        path: 'items',
        name: 'Items',
        component: () => import('@/views/items/ItemList.vue'),
      },
      // 物品详情
      {
        path: 'item/:id',
        name: 'ItemDetail',
        component: () => import('@/views/items/ItemDetail.vue'),
        props: true,
      },
      // 编辑物品
      {
        path: 'item/:id/edit',
        name: 'ItemEdit',
        component: () => import('@/views/items/ItemCreate.vue'),
        meta: { requiresAuth: true },
        props: true,
      },
      // 发布物品
      {
        path: 'publish',
        name: 'Publish',
        component: () => import('@/views/items/ItemCreate.vue'),
        meta: { requiresAuth: true },
      },
      // 我的发布
      {
        path: 'my/items',
        name: 'MyItems',
        component: () => import('@/views/user/MyItemsView.vue'),
        meta: { requiresAuth: true },
      },
      // 个人中心
      {
        path: 'my/profile',
        name: 'MyProfile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { requiresAuth: true },
      },
      // 消息中心
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/messages/MessageList.vue'),
        meta: { requiresAuth: true },
      },
      // 积分中心
      {
        path: 'points',
        name: 'Points',
        component: () => import('@/views/point/PointView.vue'),
        meta: { requiresAuth: true },
      },
      // 积分排行榜
      {
        path: 'points/ranking',
        name: 'PointsRanking',
        component: () => import('@/views/point/RankingView.vue'),
        meta: { requiresAuth: true },
      },
    ],
  },
  // 兼容旧路由
  {
    path: '/user/profile',
    redirect: '/my/profile',
  },
  {
    path: '/user/items',
    redirect: '/my/items',
  },
  // 管理后台（需要管理员权限）
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      // 数据统计 - 数据概览
      {
        path: '',
        redirect: '/admin/dashboard',
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { title: '数据概览' },
      },
      // 内容管理 - 物品列表
      {
        path: 'items',
        name: 'AdminItems',
        component: () => import('@/views/admin/AdminItems.vue'),
        meta: { title: '物品列表' },
      },
      // 内容管理 - 举报处理
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/AdminReports.vue'),
        meta: { title: '举报处理' },
      },
      // 用户管理 - 用户列表
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue'),
        meta: { title: '用户列表' },
      },
      // 数据导出
      {
        path: 'export',
        name: 'AdminExport',
        component: () => import('@/views/admin/AdminExport.vue'),
        meta: { title: '数据导出' },
      },
    ],
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Navigation guard for authentication
router.beforeEach((to, _from, next) => {
  const authStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)
  const isGuest = to.matched.some(record => record.meta.guest)

  // Check if route requires authentication
  if (requiresAuth && !authStore.isLoggedIn) {
    next({
      name: 'Login',
      query: { redirect: to.fullPath },
    })
    return
  }

  // Check if route requires admin role
  if (requiresAdmin && !authStore.isAdmin) {
    // Redirect to home page if user is not admin
    next({ name: 'Home' })
    return
  }

  // Redirect logged-in users away from guest pages (login/register)
  if (isGuest && authStore.isLoggedIn) {
    const redirect = to.query.redirect as string
    next(redirect || { name: 'Home' })
    return
  }

  next()
})

export default router
