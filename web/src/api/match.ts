/**
 * 智能匹配相关 API
 */

import request from '../utils/request'
import type { 
  ApiResponse, 
  MatchVO,
  ConfirmMatchDTO,
  MatchFeedbackDTO
} from '../types'

/**
 * 获取匹配推荐列表
 */
export const getMatchRecommendations = (itemId: number): Promise<ApiResponse<MatchVO[]>> => {
  return request.get(`/api/v1/matches/recommendations/${itemId}`)
}

/**
 * 获取用户的匹配推荐列表
 */
export const getUserMatchRecommendations = (): Promise<ApiResponse<MatchVO[]>> => {
  return request.get('/api/v1/matches/user-recommendations')
}

/**
 * 确认匹配
 */
export const confirmMatch = (data: ConfirmMatchDTO): Promise<ApiResponse<void>> => {
  return request.post('/api/v1/matches/confirm', data)
}

/**
 * 提交匹配反馈
 */
export const submitMatchFeedback = (data: MatchFeedbackDTO): Promise<ApiResponse<void>> => {
  return request.post('/api/v1/matches/feedback', data)
}