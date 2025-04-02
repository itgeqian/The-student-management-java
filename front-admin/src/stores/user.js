import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout as apiLogout, getInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import router from '@/router'

// 用户信息本地存储key
const USER_INFO_KEY = 'user_info'
const USER_ROLE_KEY = 'user_role'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const name = ref('')
  const avatar = ref('')
  const roles = ref([])
  const userInfo = ref(null)

  // 初始化用户信息
  const initUserInfo = () => {
    const storedInfo = localStorage.getItem(USER_INFO_KEY)
    const storedRole = localStorage.getItem(USER_ROLE_KEY)
    
    if (storedInfo) {
      const info = JSON.parse(storedInfo)
      userInfo.value = info
      name.value = info.name
      avatar.value = info.avatar || ''
    }
    
    if (storedRole) {
      roles.value = [storedRole]
    }
  }

  // 保存用户信息到本地存储
  const saveUserInfo = (info) => {
    console.log('Saving user info:', info)
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
    localStorage.setItem(USER_ROLE_KEY, info.role)
    
    userInfo.value = info
    name.value = info.name
    avatar.value = info.avatar || ''
    roles.value = [info.role]
    console.log('Current roles:', roles.value)
  }

  const userLogin = async (userInfo) => {
    const { username, password } = userInfo
    try {
      const response = await login({
        username: username.trim(),
        password: password
      })
      const { data } = response
      
      // 确保data.token存在
      if (!data.token) {
        throw new Error('登录返回数据缺少token')
      }
      
      // 保存token
      setToken(data.token)
      token.value = data.token
      
      // 确保data.user和data.user.role存在
      if (!data.user || !data.user.role) {
        throw new Error('登录返回数据缺少用户信息或角色')
      }
      
      // 保存用户信息
      const userInfo = {
        ...data.user,
        role: data.user.role.toLowerCase() // 确保角色名称小写
      }
      
      console.log('Login success, user info:', userInfo)
      saveUserInfo(userInfo)

      return Promise.resolve()
    } catch (error) {
      console.error('Login error:', error)
      return Promise.reject(error)
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const { data } = await getInfo()
      saveUserInfo(data)
      return Promise.resolve(data)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  const userLogout = async () => {
    try {
      await apiLogout()
      resetToken()
      clearUserInfo()
      // 重置路由
      location.reload() // 刷新页面以重置路由
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  const resetToken = () => {
    removeToken()
    token.value = ''
  }

  const clearUserInfo = () => {
    localStorage.removeItem(USER_INFO_KEY)
    localStorage.removeItem(USER_ROLE_KEY)
    roles.value = []
    name.value = ''
    userInfo.value = null
    avatar.value = ''
  }

  const logout = () => {
    resetToken()
    clearUserInfo()
  }

  // 初始化store时加载用户信息
  if (token.value) {
    initUserInfo()
  }

  return {
    token,
    name,
    avatar,
    roles,
    userInfo,
    userLogin,
    getUserInfo,
    userLogout,
    resetToken,
    clearUserInfo,
    initUserInfo,
    logout
  }
})
