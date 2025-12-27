import axios, { type AxiosInstance, AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { API_BASE_URL } from '@/config/env'
import type { ApiResponse } from '@/types/api'

// Create axios instance
const instance: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - Add JWT token
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('access_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// Response interceptor - Handle responses and errors
instance.interceptors.response.use(
  (response) => {
    // Return the full response data (including the Result wrapper)
    return response.data
  },
  (error: AxiosError<ApiResponse>) => {
    // Handle HTTP errors
    if (error.response) {
      const { status, data } = error.response

      // Handle authentication errors
      if (status === 401) {
        // Clear token and redirect to login
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        window.location.href = '/login'
      }

      // Return error with message from backend if available
      const errorMessage = data?.message || 'Request failed'
      return Promise.reject(new Error(errorMessage))
    } else if (error.request) {
      // Network error
      return Promise.reject(new Error('Network error, please check your connection'))
    } else {
      // Other errors
      return Promise.reject(new Error('Request failed'))
    }
  }
)

export default instance
