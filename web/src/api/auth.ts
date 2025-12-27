/**
 * 用户认证相关 API
 */

import request from '../utils/request'
import type { 
  ApiResponse, 
  LoginDTO, 
  RegisterDTO, 
  TokenVO, 
  UserVO 
} from '../types'

/**
 * 用户注册
 */
export const register = (data: RegisterDTO): Promise<ApiResponse<UserVO>> => {
  return request.post('/api/v1/auth/register', data)
}

/**
 * 用户登录
 */
export const login = (data: LoginDTO): Promise<ApiResponse<TokenVO>> => {
  return request.post('/api/v1/auth/login', data)
}

/**
 * 刷新令牌
 */
export const refreshToken = (refreshToken: string): Promise<ApiResponse<TokenVO>> => {
  return request.post('/api/v1/auth/refresh', { refreshToken })
}

/**
 * 退出登录
 */
export const logout = (): Promise<ApiResponse<void>> => {
  return request.post('/api/v1/auth/logout')
}