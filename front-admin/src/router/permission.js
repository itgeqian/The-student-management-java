// 页面权限配置
export const rolePermissions = {
  admin: ['*'], // 管理员可以访问所有页面
  teacher: [
    '/dashboard',
    '/course',
    '/homework',
    '/grade',
    '/profile'
  ],
  student: [
    '/dashboard',
    '/course/selection',
    '/homework/student',
    '/grade/student',
    '/profile'
  ]
}

// 功能权限配置
export const actionPermissions = {
  admin: ['*'], // 管理员可以执行所有操作
  teacher: [
    'course:view',
    'course:edit',
    'homework:view',
    'homework:create',
    'homework:edit',
    'homework:grade',
    'grade:view',
    'grade:edit',
    'grade:publish'
  ],
  student: [
    'course:view',
    'course:select',
    'homework:view',
    'homework:submit',
    'grade:view'
  ]
}

// 检查页面权限
export function hasPagePermission(roles, route) {
  if (!roles || roles.length === 0) return false
  return roles.some(role => {
    const permissions = rolePermissions[role]
    return permissions && (
      permissions.includes('*') ||
      permissions.includes(route) ||
      route.startsWith('/profile')
    )
  })
}

// 检查功能权限
export function hasActionPermission(roles, action) {
  if (!roles || roles.length === 0) return false
  return roles.some(role => {
    const permissions = actionPermissions[role]
    return permissions && (
      permissions.includes('*') ||
      permissions.includes(action)
    )
  })
}

// 指令：v-permission="['admin']"
export const permission = {
  mounted(el, binding) {
    const { value } = binding
    const roles = useUserStore().roles

    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = roles.some(role => value.includes(role))
      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    } else {
      throw new Error('need roles! Like v-permission="[\'admin\',\'teacher\']"')
    }
  }
}

import router from '@/router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import { constantRoutes, asyncRoutes } from './routes'

const whiteList = ['/login'] // 白名单

// 添加动态路由的函数
function generateAsyncRoutes(role) {
  let accessedRoutes = []
  if (role === 'admin') {
    accessedRoutes = asyncRoutes
  } else {
    accessedRoutes = asyncRoutes.filter(route => {
      if (route.meta?.roles) {
        return route.meta.roles.includes(role)
      }
      return true
    })
  }
  return accessedRoutes
}

let isRoutesAdded = false // 标记路由是否已添加

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      const hasUserInfo = userStore.userInfo !== null
      
      if (hasUserInfo) {
        // 确保动态路由只添加一次
        if (!isRoutesAdded) {
          const role = userStore.roles?.[0]
          if (role) {
            const accessedRoutes = generateAsyncRoutes(role)
            // 添加动态路由
            accessedRoutes.forEach(route => {
              router.addRoute(route)
            })
            isRoutesAdded = true
            // 重新导航到目标路由
            next({ ...to, replace: true })
          } else {
            next()
          }
        } else {
          next()
        }
      } else {
        try {
          await userStore.getUserInfo()
          next({ ...to, replace: true })
        } catch (error) {
          await userStore.userLogout()
          next(`/login?redirect=${to.path}`)
        }
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})
