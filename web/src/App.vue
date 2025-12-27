<script setup lang="ts">
/**
 * App 组件 - 应用主入口
 * 
 * 功能：
 * - 初始化用户认证状态
 * - 初始化 WebSocket 连接
 * - 处理实时消息通知
 * 
 * _Requirements: 6.5_
 */
import { onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/stores'
import { 
  initWebSocket, 
  disconnectWebSocket, 
  onWebSocketMessage,
  onWebSocketStatusChange
} from '@/utils/websocket'
import { ElNotification } from 'element-plus'
import type { WebSocketMessage } from '@/types'

const userStore = useUserStore()

// 消息回调取消函数
let unsubscribeMessage: (() => void) | null = null
let unsubscribeStatus: (() => void) | null = null

// 处理收到的 WebSocket 消息
const handleWebSocketMessage = (message: WebSocketMessage) => {
  console.log('[App] Received WebSocket message:', message)
  
  // 根据消息类型显示通知
  switch (message.type) {
    case 'message':
      showMessageNotification(message.data)
      break
    case 'match':
      showMatchNotification(message.data)
      break
    case 'system':
      showSystemNotification(message.data)
      break
    default:
      // 默认处理
      if (message.data?.title) {
        showMessageNotification(message.data)
      }
  }
}

// 显示消息通知
const showMessageNotification = (data: any) => {
  ElNotification({
    title: data.title || '新消息',
    message: data.content || '您有一条新消息',
    type: 'info',
    duration: 5000,
    position: 'top-right',
    onClick: () => {
      // 点击通知跳转到消息中心
      window.location.href = '/messages'
    }
  })

  // 尝试显示桌面通知
  showDesktopNotification(data.title || '新消息', data.content || '您有一条新消息')
}

// 显示匹配通知
const showMatchNotification = (data: any) => {
  ElNotification({
    title: '匹配通知',
    message: data.content || '发现可能匹配的物品',
    type: 'success',
    duration: 8000,
    position: 'top-right',
    onClick: () => {
      // 点击通知跳转到物品详情
      if (data.relatedId) {
        window.location.href = `/item/${data.relatedId}`
      } else {
        window.location.href = '/messages'
      }
    }
  })

  // 尝试显示桌面通知
  showDesktopNotification('匹配通知', data.content || '发现可能匹配的物品')
}

// 显示系统通知
const showSystemNotification = (data: any) => {
  ElNotification({
    title: data.title || '系统通知',
    message: data.content || '您有一条系统通知',
    type: 'warning',
    duration: 5000,
    position: 'top-right'
  })
}

// 显示桌面通知
const showDesktopNotification = (title: string, body: string) => {
  // 检查浏览器是否支持通知
  if (!('Notification' in window)) {
    return
  }

  // 检查通知权限
  if (Notification.permission === 'granted') {
    new Notification(title, {
      body,
      icon: '/favicon.ico',
      tag: 'campus-lost-found'
    })
  } else if (Notification.permission !== 'denied') {
    // 请求通知权限
    Notification.requestPermission().then(permission => {
      if (permission === 'granted') {
        new Notification(title, {
          body,
          icon: '/favicon.ico',
          tag: 'campus-lost-found'
        })
      }
    })
  }
}

// 初始化 WebSocket 连接
const initializeWebSocket = () => {
  if (userStore.token) {
    initWebSocket(userStore.token)
    
    // 注册消息回调
    unsubscribeMessage = onWebSocketMessage(handleWebSocketMessage)
    
    // 注册状态变化回调
    unsubscribeStatus = onWebSocketStatusChange((status) => {
      console.log('[App] WebSocket status:', status)
    })
  }
}

// 清理 WebSocket 连接
const cleanupWebSocket = () => {
  if (unsubscribeMessage) {
    unsubscribeMessage()
    unsubscribeMessage = null
  }
  if (unsubscribeStatus) {
    unsubscribeStatus()
    unsubscribeStatus = null
  }
  disconnectWebSocket()
}

// 监听用户登录状态变化
watch(
  () => userStore.token,
  (newToken, oldToken) => {
    if (newToken && !oldToken) {
      // 用户登录，初始化 WebSocket
      initializeWebSocket()
    } else if (!newToken && oldToken) {
      // 用户登出，断开 WebSocket
      cleanupWebSocket()
    }
  }
)

onMounted(async () => {
  // 初始化用户认证状态
  await userStore.initializeAuth()
  
  // 如果用户已登录，初始化 WebSocket
  if (userStore.isLoggedIn) {
    initializeWebSocket()
    
    // 请求桌面通知权限
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission()
    }
  }
})

onUnmounted(() => {
  cleanupWebSocket()
})
</script>

<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style lang="scss">
// Global app styles are imported in main.ts
</style>
