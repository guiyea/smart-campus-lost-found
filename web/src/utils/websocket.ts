/**
 * WebSocket 连接管理工具
 * 
 * 功能：
 * - 封装WebSocket连接管理
 * - 连接时携带token
 * - 自动重连机制
 * - 消息接收回调
 * 
 * _Requirements: 6.5_
 */

import type { WebSocketMessage, WebSocketStatus } from '@/types'

// WebSocket 配置
interface WebSocketConfig {
  /** WebSocket 服务器地址 */
  url: string
  /** 重连间隔（毫秒） */
  reconnectInterval?: number
  /** 最大重连次数 */
  maxReconnectAttempts?: number
  /** 心跳间隔（毫秒） */
  heartbeatInterval?: number
}

// 消息回调类型
type MessageCallback = (message: WebSocketMessage) => void
type StatusCallback = (status: WebSocketStatus) => void

class WebSocketManager {
  private ws: WebSocket | null = null
  private config: Required<WebSocketConfig>
  private token: string = ''
  private reconnectAttempts: number = 0
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private heartbeatTimer: ReturnType<typeof setInterval> | null = null
  private messageCallbacks: Set<MessageCallback> = new Set()
  private statusCallbacks: Set<StatusCallback> = new Set()
  private status: WebSocketStatus = 'disconnected'
  private manualClose: boolean = false

  constructor(config: WebSocketConfig) {
    this.config = {
      url: config.url,
      reconnectInterval: config.reconnectInterval || 3000,
      maxReconnectAttempts: config.maxReconnectAttempts || 10,
      heartbeatInterval: config.heartbeatInterval || 30000
    }
  }

  /**
   * 连接 WebSocket
   */
  connect(token: string): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('[WebSocket] Already connected')
      return
    }

    this.token = token
    this.manualClose = false
    this.updateStatus('connecting')

    try {
      // 构建带 token 的 WebSocket URL
      const wsUrl = `${this.config.url}/${token}`
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = this.handleOpen.bind(this)
      this.ws.onmessage = this.handleMessage.bind(this)
      this.ws.onerror = this.handleError.bind(this)
      this.ws.onclose = this.handleClose.bind(this)
    } catch (error) {
      console.error('[WebSocket] Connection error:', error)
      this.updateStatus('error')
      this.scheduleReconnect()
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.manualClose = true
    this.clearTimers()
    
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    
    this.updateStatus('disconnected')
    this.reconnectAttempts = 0
  }

  /**
   * 发送消息
   */
  send(data: any): boolean {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.warn('[WebSocket] Cannot send message: not connected')
      return false
    }

    try {
      const message = typeof data === 'string' ? data : JSON.stringify(data)
      this.ws.send(message)
      return true
    } catch (error) {
      console.error('[WebSocket] Send error:', error)
      return false
    }
  }

  /**
   * 注册消息回调
   */
  onMessage(callback: MessageCallback): () => void {
    this.messageCallbacks.add(callback)
    return () => this.messageCallbacks.delete(callback)
  }

  /**
   * 注册状态变化回调
   */
  onStatusChange(callback: StatusCallback): () => void {
    this.statusCallbacks.add(callback)
    // 立即通知当前状态
    callback(this.status)
    return () => this.statusCallbacks.delete(callback)
  }

  /**
   * 获取当前连接状态
   */
  getStatus(): WebSocketStatus {
    return this.status
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.status === 'connected'
  }

  // 处理连接打开
  private handleOpen(): void {
    console.log('[WebSocket] Connected')
    this.updateStatus('connected')
    this.reconnectAttempts = 0
    this.startHeartbeat()
  }

  // 处理收到消息
  private handleMessage(event: MessageEvent): void {
    try {
      const data = JSON.parse(event.data)
      const messageType = data.type || 'message'
      
      // 心跳响应不触发回调
      if (messageType === 'pong') {
        return
      }
      
      const message: WebSocketMessage = {
        type: messageType as 'message' | 'match' | 'system',
        data: data.data || data,
        timestamp: data.timestamp || Date.now()
      }

      // 通知所有回调
      this.messageCallbacks.forEach(callback => {
        try {
          callback(message)
        } catch (error) {
          console.error('[WebSocket] Message callback error:', error)
        }
      })
    } catch (error) {
      console.error('[WebSocket] Parse message error:', error)
    }
  }

  // 处理错误
  private handleError(event: Event): void {
    console.error('[WebSocket] Error:', event)
    this.updateStatus('error')
  }

  // 处理连接关闭
  private handleClose(event: CloseEvent): void {
    console.log('[WebSocket] Closed:', event.code, event.reason)
    this.clearTimers()
    this.ws = null

    if (!this.manualClose) {
      this.updateStatus('disconnected')
      this.scheduleReconnect()
    }
  }

  // 更新状态
  private updateStatus(status: WebSocketStatus): void {
    if (this.status !== status) {
      this.status = status
      this.statusCallbacks.forEach(callback => {
        try {
          callback(status)
        } catch (error) {
          console.error('[WebSocket] Status callback error:', error)
        }
      })
    }
  }

  // 安排重连
  private scheduleReconnect(): void {
    if (this.manualClose) {
      return
    }

    if (this.reconnectAttempts >= this.config.maxReconnectAttempts) {
      console.log('[WebSocket] Max reconnect attempts reached')
      return
    }

    this.reconnectAttempts++
    console.log(`[WebSocket] Reconnecting in ${this.config.reconnectInterval}ms (attempt ${this.reconnectAttempts}/${this.config.maxReconnectAttempts})`)

    this.reconnectTimer = setTimeout(() => {
      if (this.token && !this.manualClose) {
        this.connect(this.token)
      }
    }, this.config.reconnectInterval)
  }

  // 启动心跳
  private startHeartbeat(): void {
    this.stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: 'ping' })
      }
    }, this.config.heartbeatInterval)
  }

  // 停止心跳
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  // 清除所有定时器
  private clearTimers(): void {
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }
}

// 获取 WebSocket 服务器地址
const getWebSocketUrl = (): string => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = import.meta.env.VITE_WS_HOST || window.location.host
  const basePath = import.meta.env.VITE_WS_PATH || '/ws/messages'
  return `${protocol}//${host}${basePath}`
}

// 创建单例实例
let wsManager: WebSocketManager | null = null

/**
 * 获取 WebSocket 管理器实例
 */
export const getWebSocketManager = (): WebSocketManager => {
  if (!wsManager) {
    wsManager = new WebSocketManager({
      url: getWebSocketUrl(),
      reconnectInterval: 3000,
      maxReconnectAttempts: 10,
      heartbeatInterval: 30000
    })
  }
  return wsManager
}

/**
 * 初始化 WebSocket 连接
 */
export const initWebSocket = (token: string): void => {
  const manager = getWebSocketManager()
  manager.connect(token)
}

/**
 * 断开 WebSocket 连接
 */
export const disconnectWebSocket = (): void => {
  const manager = getWebSocketManager()
  manager.disconnect()
}

/**
 * 注册消息回调
 */
export const onWebSocketMessage = (callback: MessageCallback): () => void => {
  const manager = getWebSocketManager()
  return manager.onMessage(callback)
}

/**
 * 注册状态变化回调
 */
export const onWebSocketStatusChange = (callback: StatusCallback): () => void => {
  const manager = getWebSocketManager()
  return manager.onStatusChange(callback)
}

/**
 * 获取 WebSocket 连接状态
 */
export const getWebSocketStatus = (): WebSocketStatus => {
  const manager = getWebSocketManager()
  return manager.getStatus()
}

/**
 * 检查 WebSocket 是否已连接
 */
export const isWebSocketConnected = (): boolean => {
  const manager = getWebSocketManager()
  return manager.isConnected()
}

export default WebSocketManager
