/**
 * 物品信息相关 API
 */

import request from '../utils/request'
import type { 
  ApiResponse, 
  PageResult,
  ItemDTO,
  ItemVO,
  ItemDetailVO,
  ItemSearchDTO,
  UpdateCategoryDTO
} from '../types'

/**
 * 发布物品信息
 */
export const publishItem = (data: ItemDTO): Promise<ApiResponse<ItemVO>> => {
  return request.post('/api/v1/items', data)
}

/**
 * 更新物品信息
 */
export const updateItem = (id: number, data: ItemDTO): Promise<ApiResponse<ItemVO>> => {
  return request.put(`/api/v1/items/${id}`, data)
}

/**
 * 删除物品信息
 */
export const deleteItem = (id: number): Promise<ApiResponse<void>> => {
  return request.delete(`/api/v1/items/${id}`)
}

/**
 * 获取物品详情
 */
export const getItemDetail = (id: number): Promise<ApiResponse<ItemDetailVO>> => {
  return request.get(`/api/v1/items/${id}`)
}

/**
 * 搜索物品列表
 */
export const searchItems = (params: ItemSearchDTO): Promise<ApiResponse<PageResult<ItemVO>>> => {
  return request.get('/api/v1/items', { params })
}

/**
 * 获取附近物品
 */
export const getNearbyItems = (params: {
  lng: number
  lat: number
  radius?: number
}): Promise<ApiResponse<ItemVO[]>> => {
  return request.get('/api/v1/items/nearby', { params })
}

/**
 * 获取热门物品
 */
export const getHotItems = (): Promise<ApiResponse<ItemVO[]>> => {
  return request.get('/api/v1/items/hot')
}

/**
 * 手动更新物品类别
 */
export const updateItemCategory = (id: number, data: UpdateCategoryDTO): Promise<ApiResponse<void>> => {
  return request.put(`/api/v1/items/${id}/category`, data)
}