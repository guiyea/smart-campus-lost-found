/**
 * 积分系统相关 API
 */

import request from '../utils/request'
import type { 
  ApiResponse, 
  PageResult,
  PointRecordVO,
  PointRankVO
} from '../types'

/**
 * 获取当前用户积分明细
 */
export const getPointRecords = (params: {
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<PointRecordVO>>> => {
  return request.get('/api/v1/points', { params })
}

/**
 * 获取当前用户总积分
 */
export const getTotalPoints = (): Promise<ApiResponse<number>> => {
  return request.get('/api/v1/points/total')
}

/**
 * 获取积分排行榜
 */
export const getPointRanking = (params: {
  limit?: number
}): Promise<ApiResponse<PointRankVO[]>> => {
  return request.get('/api/v1/points/ranking', { params })
}