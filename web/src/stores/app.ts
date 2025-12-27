import { defineStore } from 'pinia'
import * as messageApi from '@/api/message'

/**
 * 应用全局状态管理 Store
 * 
 * 管理应用级别的全局状态，如加载状态、未读消息数等
 * 
 * State:
 * - loading: 全局加载状态
 * - unreadMessageCount: 未读消息数量
 * 
 * Actions:
 * - setLoading: 设置加载状态
 * - fetchUnreadCount: 获取未读消息数量
 * 
 * _Requirements: 1.2, 6.3_
 */
export const useAppStore = defineStore('app', {
  state: () => ({
    /** 全局加载状态 */
    loading: false,
    /** 未读消息数量 */
    unreadMessageCount: 0,
    /** 侧边栏折叠状态 */
    sidebarCollapsed: false,
    /** 当前主题 */
    theme: 'light' as 'light' | 'dark',
  }),

  getters: {
    /** 是否有未读消息 */
    hasUnreadMessages: (state) => state.unreadMessageCount > 0,
  },

  actions: {
    /**
     * 设置全局加载状态
     * @param loading 加载状态
     */
    setLoading(loading: boolean) {
      this.loading = loading
    },

    /**
     * 获取未读消息数量
     * 从服务器获取当前用户的未读消息数量
     * @returns 未读消息数量
     */
    async fetchUnreadCount() {
      try {
        const response = await messageApi.getUnreadCount()
        if (response.data !== null && response.data !== undefined) {
          this.unreadMessageCount = response.data
        }
        return this.unreadMessageCount
      } catch (error) {
        console.error('Failed to fetch unread message count:', error)
        return this.unreadMessageCount
      }
    },

    /**
     * 设置未读消息数量
     * @param count 未读消息数量
     */
    setUnreadCount(count: number) {
      this.unreadMessageCount = count
    },

    /**
     * 增加未读消息数量
     * @param delta 增加的数量，默认为1
     */
    incrementUnreadCount(delta: number = 1) {
      this.unreadMessageCount += delta
    },

    /**
     * 减少未读消息数量
     * @param delta 减少的数量，默认为1
     */
    decrementUnreadCount(delta: number = 1) {
      this.unreadMessageCount = Math.max(0, this.unreadMessageCount - delta)
    },

    /**
     * 清空未读消息数量
     */
    clearUnreadCount() {
      this.unreadMessageCount = 0
    },

    /**
     * 切换侧边栏折叠状态
     */
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    /**
     * 设置侧边栏折叠状态
     * @param collapsed 是否折叠
     */
    setSidebarCollapsed(collapsed: boolean) {
      this.sidebarCollapsed = collapsed
    },

    /**
     * 设置主题
     * @param theme 主题名称
     */
    setTheme(theme: 'light' | 'dark') {
      this.theme = theme
    },

    /**
     * 切换主题
     */
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
    },
  },
})
