/**
 * 消息通知相关 API
 */

import request from '../utils/request'
import type { 
  ApiResponse, 
  PageResult,
  MessageVO,
  SendMessageDTO
} from '../types'

/**
 * 获取消息列表
 */
export const getMessageList = (params: {
  type?: number
  isRead?: boolean
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<MessageVO>>> => {
  return request.get('/api/v1/messages', { params })
}

/**
 * 获取未读消息数量
 */
export const getUnreadCount = (): Promise<ApiResponse<number>> => {
  return request.get('/api/v1/messages/unread-count')
}

/**
 * 标记单条消息已读
 */
export const markMessageAsRead = (id: number): Promise<ApiResponse<void>> => {
  return request.put(`/api/v1/messages/${id}/read`)
}

/**
 * 标记全部消息已读
 */
export const markAllMessagesAsRead = (): Promise<ApiResponse<void>> => {
  return request.put('/api/v1/messages/read-all')
}

/**
 * 发送消息（留言）
 */
export const sendMessage = (data: SendMessageDTO): Promise<ApiResponse<void>> => {
  return request.post('/api/v1/messages', data)
}