import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'
import type { UserVO, LoginDTO, RegisterDTO } from '@/types'

const createGuestUser = (): UserVO => ({
  id: 0,
  studentId: 'guest',
  name: '游客',
  phone: '',
  avatar: '',
  points: 0,
  role: 0,
  status: 0,
  createdAt: new Date().toISOString(),
})

export const useUserStore = defineStore('user', {
  state: () => {
    const savedUserInfo = localStorage.getItem('user_info')
    const guestMode = localStorage.getItem('guest_mode') === 'true'
    let userInfo: UserVO | null = null
    if (savedUserInfo) {
      try {
        userInfo = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('Failed to parse saved user info:', e)
      }
    }
    if (guestMode && !userInfo) {
      userInfo = createGuestUser()
    }

    return {
      userInfo,
      token: localStorage.getItem('access_token') || '',
      refreshToken: localStorage.getItem('refresh_token') || '',
      loading: false,
      isGuest: guestMode,
    }
  },

  getters: {
    isLoggedIn: (state) => (!!state.token && !!state.userInfo) || state.isGuest,
    isAdmin: (state) => state.userInfo?.role === 1,
    user: (state) => state.userInfo,
    isGuestMode: (state) => state.isGuest,
  },

  actions: {
    async login(loginData: LoginDTO) {
      this.loading = true
      try {
        const response = await authApi.login(loginData)
        if (response.data) {
          const { accessToken, refreshToken, userInfo } = response.data
          this.setToken(accessToken, refreshToken)
          this.setUserInfo(userInfo)
          this.isGuest = false
          localStorage.removeItem('guest_mode')
        }
        return response
      } finally {
        this.loading = false
      }
    },

    async register(registerData: RegisterDTO) {
      this.loading = true
      try {
        const response = await authApi.register(registerData)
        return response
      } finally {
        this.loading = false
      }
    },

    loginAsGuest() {
      const guestUser = createGuestUser()
      this.setUserInfo(guestUser)
      this.token = ''
      this.refreshToken = ''
      this.isGuest = true
      this.loading = false
      localStorage.setItem('guest_mode', 'true')
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
    },

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

    async refreshAccessToken() {
      if (this.isGuest) {
        return null
      }
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
          this.isGuest = false
          localStorage.removeItem('guest_mode')
        }
        return response
      } catch (error) {
        this.clearAuth()
        throw error
      }
    },

    async fetchUserInfo() {
      if (this.isGuest) {
        return null
      }
      if (!this.token) {
        throw new Error('No token available')
      }

      this.loading = true
      try {
        const response = await userApi.getCurrentUser()
        if (response.data) {
          this.setUserInfo(response.data)
          this.isGuest = false
          localStorage.removeItem('guest_mode')
        }
        return response
      } catch (error) {
        console.error('Failed to fetch user info:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    setUserInfo(userInfo: UserVO | null) {
      this.userInfo = userInfo
      if (userInfo) {
        localStorage.setItem('user_info', JSON.stringify(userInfo))
      } else {
        localStorage.removeItem('user_info')
      }
    },

    setToken(token: string, refreshToken?: string) {
      this.token = token
      localStorage.setItem('access_token', token)

      if (refreshToken) {
        this.refreshToken = refreshToken
        localStorage.setItem('refresh_token', refreshToken)
      }
    },

    clearAuth() {
      this.userInfo = null
      this.token = ''
      this.refreshToken = ''
      this.isGuest = false
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('user_info')
      localStorage.removeItem('guest_mode')
    },

    async initializeAuth() {
      if (this.isGuest) {
        if (!this.userInfo) {
          this.setUserInfo(createGuestUser())
        }
        return
      }

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

    updateUserProfile(updates: Partial<UserVO>) {
      if (this.userInfo) {
        this.userInfo = { ...this.userInfo, ...updates }
        localStorage.setItem('user_info', JSON.stringify(this.userInfo))
      }
    },
  },
})
