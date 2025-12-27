import { defineStore } from 'pinia'
import type { ItemVO, MessageVO } from '@/types'

// Re-export stores from separate files
export { useUserStore } from './user'
export { useUserStore as useAuthStore } from './user'
export { useAppStore } from './app'

// Items Store
export const useItemsStore = defineStore('items', {
  state: () => ({
    items: [] as ItemVO[],
    currentItem: null as ItemVO | null,
    loading: false,
    total: 0,
    filters: {
      keyword: '',
      category: '',
      status: '',
      startDate: '',
      endDate: '',
    },
    pagination: {
      current: 1,
      size: 10,
    },
  }),

  getters: {
    lostItems: (state) => state.items.filter(item => item.type === 0),
    foundItems: (state) => state.items.filter(item => item.type === 1),
  },

  actions: {
    setItems(items: ItemVO[]) {
      this.items = items
    },

    setCurrentItem(item: ItemVO | null) {
      this.currentItem = item
    },

    setLoading(loading: boolean) {
      this.loading = loading
    },

    setTotal(total: number) {
      this.total = total
    },

    updateFilters(filters: Partial<typeof this.filters>) {
      this.filters = { ...this.filters, ...filters }
    },

    updatePagination(pagination: Partial<typeof this.pagination>) {
      this.pagination = { ...this.pagination, ...pagination }
    },

    clearFilters() {
      this.filters = {
        keyword: '',
        category: '',
        status: '',
        startDate: '',
        endDate: '',
      }
      this.pagination = {
        current: 1,
        size: 10,
      }
    },
  },
})

// Messages Store
export const useMessagesStore = defineStore('messages', {
  state: () => ({
    messages: [] as MessageVO[],
    unreadCount: 0,
    loading: false,
  }),

  getters: {
    unreadMessages: (state) => state.messages.filter(msg => !msg.isRead),
    conversationsByUser: () => {
      // Simplified implementation for now
      return new Map<string, MessageVO[]>()
    },
  },

  actions: {
    setMessages(messages: MessageVO[]) {
      this.messages = messages
      this.updateUnreadCount()
    },

    addMessage(message: MessageVO) {
      this.messages.push(message)
      this.updateUnreadCount()
    },

    markAsRead(messageId: number) {
      const message = this.messages.find(msg => msg.id === messageId)
      if (message) {
        message.isRead = true
        this.updateUnreadCount()
      }
    },

    markAllAsRead() {
      this.messages.forEach(msg => {
        msg.isRead = true
      })
      this.unreadCount = 0
    },

    updateUnreadCount() {
      this.unreadCount = this.messages.filter(msg => !msg.isRead).length
    },

    setLoading(loading: boolean) {
      this.loading = loading
    },
  },
})
