/**
 * 管理后台相关 API
 */

import request from '../utils/request'
import type { 
  ApiResponse, 
  PageResult,
  StatisticsVO,
  ItemVO,
  ItemAdminSearchDTO,
  UserVO
} from '../types'

/**
 * 用户搜索参数
 */
export interface UserSearchParams {
  /** 学号（支持模糊查询） */
  studentId?: string
  /** 姓名（支持模糊查询） */
  name?: string
  /** 状态：0-正常，1-封禁 */
  status?: number
  /** 页码 */
  pageNum?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 获取数据统计
 */
export const getStatistics = (): Promise<ApiResponse<StatisticsVO>> => {
  return request.get('/api/v1/admin/statistics')
}

/**
 * 获取管理员物品列表
 */
export const getAdminItemList = (params: ItemAdminSearchDTO): Promise<ApiResponse<PageResult<ItemVO>>> => {
  return request.get('/api/v1/admin/items', { params })
}

/**
 * 管理员删除物品
 */
export const adminDeleteItem = (id: number): Promise<ApiResponse<void>> => {
  return request.delete(`/api/v1/admin/items/${id}`)
}

/**
 * 审核物品信息
 * @param id 物品ID
 * @param action 审核操作: 0-通过, 1-删除, 2-警告发布者
 * @param reason 审核原因/备注
 */
export const reviewItem = (id: number, action: number, reason?: string): Promise<ApiResponse<void>> => {
  return request.post(`/api/v1/admin/items/${id}/review`, null, {
    params: { action, reason }
  })
}

/**
 * 恢复已删除的物品
 * @param id 物品ID
 */
export const restoreItem = (id: number): Promise<ApiResponse<void>> => {
  return request.post(`/api/v1/admin/items/${id}/restore`)
}

/**
 * 获取用户列表（管理员）
 */
export const getAdminUserList = (params: UserSearchParams): Promise<ApiResponse<PageResult<UserVO>>> => {
  return request.get('/api/v1/users', { params })
}

/**
 * 封禁用户
 */
export const banUser = (userId: number, reason?: string): Promise<ApiResponse<void>> => {
  return request.post(`/api/v1/admin/users/${userId}/ban`, null, {
    params: { reason }
  })
}

/**
 * 解封用户
 */
export const unbanUser = (userId: number): Promise<ApiResponse<void>> => {
  return request.post(`/api/v1/admin/users/${userId}/unban`)
}

/**
 * 导出数据报表
 */
export const exportDataReport = (params: {
  startDate?: string
  endDate?: string
  type?: 'overview' | 'daily' | 'category'
}): Promise<Blob> => {
  return request.download('/api/v1/admin/export', { 
    params
  })
}