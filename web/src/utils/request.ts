/**
 * Axios 请求封装
 * 
 * 提供统一的 HTTP 请求接口，包括：
 * - 自动添加认证头
 * - 统一错误处理
 * - 响应拦截处理
 * - 封装常用请求方法
 */

import axios, { 
  type AxiosInstance, 
  type AxiosRequestConfig, 
  type InternalAxiosRequestConfig,
  AxiosError 
} from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '../types'

// 创建 axios 实例
const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 自动添加 Authorization 头
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 localStorage 读取 token
    const token = localStorage.getItem('access_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    console.error('Request interceptor error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理错误响应
instance.interceptors.response.use(
  (response) => {
    // 返回响应数据
    return response.data
  },
  (error: AxiosError<ApiResponse>) => {
    console.error('Response interceptor error:', error)
    
    // 处理 HTTP 错误
    if (error.response) {
      const { status, data } = error.response
      
      // 401 错误 - 未认证，处理登录/注册与普通请求区分
      if (status === 401) {
        const requestUrl = error.config?.url || ''
        const isAuthRequest = requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register')
        const message = data?.message || '登录已过期，请重新登录'
        ElMessage.error(message)

        if (!isAuthRequest) {
          // 清除本地存储的认证信息
          localStorage.removeItem('access_token')
          localStorage.removeItem('refresh_token')
          localStorage.removeItem('user_info')
          // 跳转到登录页
          window.location.href = '/login'
        }
        return Promise.reject(error)
      }
      
      // 403 错误 - 无权限
      if (status === 403) {
        ElMessage.error('无权限访问该资源')
        return Promise.reject(error)
      }
      
      // 404 错误 - 资源不存在
      if (status === 404) {
        ElMessage.error('请求的资源不存在')
        return Promise.reject(error)
      }
      
      // 422 错误 - 参数验证失败
      if (status === 422) {
        const errorMessage = data?.message || '参数验证失败'
        ElMessage.error(errorMessage)
        return Promise.reject(error)
      }
      
      // 429 错误 - 请求频率超限
      if (status === 429) {
        ElMessage.error('请求过于频繁，请稍后再试')
        return Promise.reject(error)
      }
      
      // 500 错误 - 服务器内部错误
      if (status >= 500) {
        ElMessage.error('服务器内部错误，请稍后再试')
        return Promise.reject(error)
      }
      
      // 其他错误
      const errorMessage = data?.message || `请求失败 (${status})`
      ElMessage.error(errorMessage)
      return Promise.reject(error)
    } else if (error.request) {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络设置')
      return Promise.reject(error)
    } else {
      // 其他错误
      ElMessage.error('请求失败，请稍后再试')
      return Promise.reject(error)
    }
  }
)

// 封装请求方法
const request = {
  /**
   * GET 请求
   */
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.get(url, config)
  },

  /**
   * POST 请求
   */
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.post(url, data, config)
  },

  /**
   * PUT 请求
   */
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.put(url, data, config)
  },

  /**
   * DELETE 请求
   */
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.delete(url, config)
  },

  /**
   * PATCH 请求
   */
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.patch(url, data, config)
  },

  /**
   * 上传文件
   */
  upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data',
        ...config?.headers,
      },
    })
  },

  /**
   * 下载文件
   * 注意：由于响应拦截器会返回 response.data，对于 blob 类型响应，
   * 拦截器返回的就是 Blob 对象本身
   */
  async download(url: string, config?: AxiosRequestConfig): Promise<Blob> {
    // 创建一个新的 axios 实例用于下载，避免响应拦截器的干扰
    const downloadInstance = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
      timeout: 60000, // 下载超时时间设置更长
    })
    
    // 添加认证头
    const token = localStorage.getItem('access_token')
    const headers: Record<string, string> = {}
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
    
    const response = await downloadInstance.get(url, {
      ...config,
      responseType: 'blob',
      headers: {
        ...headers,
        ...config?.headers,
      },
    })
    
    // 检查响应是否为错误（某些后端会返回 JSON 错误信息）
    if (response.data instanceof Blob && response.data.type === 'application/json') {
      // 尝试解析错误信息
      const text = await response.data.text()
      try {
        const errorData = JSON.parse(text)
        throw new Error(errorData.message || '下载失败')
      } catch (e) {
        if (e instanceof SyntaxError) {
          // 不是 JSON，返回原始 blob
          return response.data
        }
        throw e
      }
    }
    
    return response.data
  },
}

export default request
