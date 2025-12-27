import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'
import type { UserVO, LoginDTO, RegisterDTO } from '@/types'

/**
 * 用户状态管理 Store
 * 
 * 管理用户认证状态、令牌和用户信息
 * 
 * State:
 * - token: 访问令牌
 * - refreshToken: 刷新令牌
 * - userInfo: 用户信息
 * - isLoggedIn: 是否已登录（getter）
 * 
 * Actions:
 * - login: 用户登录
 * - logout: 用户退出
 * - refreshToken: 刷新令牌
 * - fetchUserInfo: 获取用户信息
 * 
 * _Requirements: 1.2, 6.3_
 */
export const useUserStore = defineStore('user', {
  state: () => {
    // 尝试从 localStorage 恢复用户信息
    const savedUserInfo = localStorage.getItem('user_info')
    let userInfo: UserVO | null = null
    if (savedUserInfo) {
      try {
        userInfo = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('Failed to parse saved user info:', e)
      }
    }
    
    return {
      /** 用户信息 */
      userInfo,
      /** 访问令牌 */
      token: localStorage.getItem('access_token') || '',
      /** 刷新令牌 */
      refreshToken: localStorage.getItem('refresh_token') || '',
      /** 加载状态 */
      loading: false,
    }
  },

  getters: {
    /** 是否已登录 */
    isLoggedIn: (state) => !!state.token && !!state.userInfo,
    /** 是否为管理员 */
    isAdmin: (state) => state.userInfo?.role === 1,
    /** 获取用户信息（兼容旧代码） */
    user: (state) => state.userInfo,
  },

  actions: {
    /**
     * 用户登录
     */
    async login(loginData: LoginDTO) {
      this.loading = true
      try {
        const response = await authApi.login(loginData)
        if (response.data) {
          const { accessToken, refreshToken, userInfo } = response.data
          this.setToken(accessToken, refreshToken)
          this.setUserInfo(userInfo)
        }
        return response
      } finally {
        this.loading = false
      }
    },

    /**
     * 用户注册
     */
    async register(registerData: RegisterDTO) {
      this.loading = true
      try {
        const response = await authApi.register(registerData)
        return response
      } finally {
        this.loading = false
      }
    },

    /**
     * 用户退出登录
     */
    async logout() {
      try {
        if (this.token) {
          await authApi.logout()
        }
      } catch (error) {
        console.error('Logout API call failed:', error)
      } finally {
        this.clearAuth()
      }
    },

    /**
     * 刷新访问令牌
     */
    async refreshAccessToken() {
      if (!this.refreshToken) {
        this.clearAuth()
        throw new Error('No refresh token available')
      }

      try {
        const response = await authApi.refreshToken(this.refreshToken)
        if (response.data) {
          const { accessToken, refreshToken, userInfo } = response.data
          this.setToken(accessToken, refreshToken)
          if (userInfo) {
            this.setUserInfo(userInfo)
          }
        }
        return response
      } catch (error) {
        this.clearAuth()
        throw error
      }
    },

    /**
     * 获取当前用户信息
     */
    async fetchUserInfo() {
      if (!this.token) {
        throw new Error('No token available')
      }

      this.loading = true
      try {
        const response = await userApi.getCurrentUser()
        if (response.data) {
          this.setUserInfo(response.data)
        }
        return response
      } catch (error) {
        console.error('Failed to fetch user info:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    /**
     * 设置用户信息
     */
    setUserInfo(userInfo: UserVO) {
      this.userInfo = userInfo
      // 同时保存到 localStorage 以便页面刷新后恢复
      if (userInfo) {
        localStorage.setItem('user_info', JSON.stringify(userInfo))
      }
    },

    /**
     * 设置用户信息（兼容旧代码）
     */
    setUser(user: UserVO) {
      this.userInfo = user
    },

    /**
     * 设置令牌
     */
    setToken(token: string, refreshToken?: string) {
      this.token = token
      localStorage.setItem('access_token', token)

      if (refreshToken) {
        this.refreshToken = refreshToken
        localStorage.setItem('refresh_token', refreshToken)
      }
    },

    /**
     * 清除认证信息
     */
    clearAuth() {
      this.userInfo = null
      this.token = ''
      this.refreshToken = ''
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('user_info')
    },

    /**
     * 初始化用户状态
     */
    async initializeAuth() {
      // 如果已经有 userInfo，不需要重新获取
      if (this.token && this.userInfo) {
        return
      }
      
      if (this.token && !this.userInfo) {
        try {
          await this.fetchUserInfo()
        } catch (error) {
          console.error('Failed to initialize auth:', error)
          this.clearAuth()
        }
      }
    },

    /**
     * 更新用户信息（部分更新）
     */
    updateUserProfile(updates: Partial<UserVO>) {
      if (this.userInfo) {
        this.userInfo = { ...this.userInfo, ...updates }
      }
    },
  },
})
