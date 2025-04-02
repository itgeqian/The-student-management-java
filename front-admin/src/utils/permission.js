import router from '@/router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login'] // 白名单

// 检查用户是否有权限
export function hasPermission(roles) {
  const userStore = useUserStore()
  const userRoles = userStore.roles
  
  if (!userRoles || userRoles.length === 0) {
    return false
  }

  // 如果用户是管理员，拥有所有权限
  if (userRoles.includes('admin')) {
    return true
  }

  if (userRoles.includes('ADMIN')) {
    return true
  }

  // 检查用户是否拥有指定角色中的任意一个
  return roles.some(role => (userRoles.includes(role) || userRoles.includes(role.toUpperCase()) || userRoles.includes(role.toLowerCase())))
}

router.beforeEach(async (to, from, next) => {
  NProgress.start()

  const hasToken = getToken()
  const userStore = useUserStore()

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      const hasRoles = userStore.roles && userStore.roles.length > 0
      if (hasRoles) {
        next()
      } else {
        try {
          // 获取用户信息
          const { roles } = await userStore.getUserInfo()
          
          // 根据角色生成可访问的路由表
          const accessRoutes = await userStore.generateRoutes(roles)
          
          // 动态添加可访问路由
          accessRoutes.forEach(route => {
            router.addRoute(route)
          })

          next({ ...to, replace: true })
        } catch (error) {
          // 移除 token 并跳转登录页
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
