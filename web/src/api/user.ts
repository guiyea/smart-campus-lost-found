/**
 * 用户信息相关 API
 */

import request from '../utils/request'
import type { 
  ApiResponse, 
  PageResult,
  UserVO, 
  UpdateProfileDTO 
} from '../types'

/**
 * 获取当前用户信息
 */
export const getCurrentUser = (): Promise<ApiResponse<UserVO>> => {
  return request.get('/api/v1/users/me')
}

/**
 * 更新当前用户信息
 */
export const updateProfile = (data: UpdateProfileDTO): Promise<ApiResponse<UserVO>> => {
  return request.put('/api/v1/users/me', data)
}

/**
 * 获取用户列表（管理员）
 */
export const getUserList = (params: {
  studentId?: string
  name?: string
  pageNum?: number
  pageSize?: number
}): Promise<ApiResponse<PageResult<UserVO>>> => {
  return request.get('/api/v1/users', { params })
}