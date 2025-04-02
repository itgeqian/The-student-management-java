import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'
import { constantRoutes, asyncRoutes } from './routes'

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

// 动态添加路由
export function addRoutes(routes) {
  routes.forEach(route => {
    router.addRoute(route)
  })
}

// 重置路由
export function resetRouter() {
  const newRouter = createRouter({
    history: createWebHistory(),
    routes: constantRoutes
  })
  router.matcher = newRouter.matcher
}

export default router
