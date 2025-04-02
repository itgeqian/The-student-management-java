import router from './router'
import { useUserStore } from './stores/user'
import { addRoutes } from './router'
import { asyncRoutes } from './router/routes'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const whiteList = ['/login'] // 不重定向白名单

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  const hasToken = userStore.token

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      const hasRoles = userStore.roles && userStore.roles.length > 0
      if (hasRoles) {
        if (to.matched.length === 0) {
          // 如果没有匹配到路由，可能是因为路由还没有完全加载
          // 重新添加路由并重试
          const roles = userStore.roles
          const accessRoutes = filterAsyncRoutes(asyncRoutes, roles)
          addRoutes(accessRoutes)
          next({ ...to, replace: true })
        } else {
          next()
        }
      } else {
        try {
          // 获取用户信息
          const userInfo = await userStore.getUserInfo()
          
          // 根据角色过滤路由
          const roles = userInfo.roles
          const accessRoutes = filterAsyncRoutes(asyncRoutes, roles)
          
          // 动态添加路由
          addRoutes(accessRoutes)
          
          next({ ...to, replace: true })
        } catch (error) {
          // 移除token并跳转登录页面
          await userStore.resetToken()
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

// 根据角色过滤路由
function filterAsyncRoutes(routes, roles) {
  const res = []

  routes.forEach(route => {
    const tmp = { ...route }
    if (hasPermission(roles, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, roles)
      }
      res.push(tmp)
    }
  })

  return res
}

// 判断是否有权限
function hasPermission(roles, route) {
  if (route.meta && route.meta.roles) {
    return roles.some(role => (route.meta.roles.includes(role.toLowerCase()) || route.meta.roles.includes(role.toUpperCase())))
  } else {
    return true
  }
}
