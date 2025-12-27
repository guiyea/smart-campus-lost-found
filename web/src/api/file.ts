/**
 * 文件上传相关 API
 */

import request from '../utils/request'
import type { 
  ApiResponse, 
  FileUploadResponse,
  BatchFileUploadResponse,
  RecognitionResult
} from '../types'

/**
 * 上传单个文件
 */
export const uploadFile = (file: File): Promise<ApiResponse<FileUploadResponse>> => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.upload('/api/v1/files/upload', formData)
}

/**
 * 批量上传文件
 */
export const uploadFiles = (files: File[]): Promise<ApiResponse<BatchFileUploadResponse>> => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request.upload('/api/v1/files/upload/batch', formData)
}

/**
 * AI图像识别
 * 识别图片中的物品类别和特征标签
 */
export const recognizeImage = (imageUrl: string): Promise<ApiResponse<RecognitionResult>> => {
  return request.post('/api/v1/ai/recognize', { imageUrl })
}