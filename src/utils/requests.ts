// src/utils/requests.ts
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加token - 注意：这里不能直接导入useUserStore，因为这是非组件环境
    // 我们需要在每次请求时从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 直接返回response，让调用方处理
    return response
  },
  (error) => {
    // 处理HTTP错误
    console.error('Request Error:', error)

    if (error.response) {
      // 服务器返回了错误状态码
      const { status, data } = error.response
      switch (status) {
        case 401:
          // token失效，跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          // 使用window.location避免router依赖问题
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          break
        case 403:
          return Promise.reject(new Error('无权限访问'))
        case 500:
          // 处理业务逻辑错误，比如"存在未来预约"
          return Promise.reject(new Error(data?.msg || '服务器内部错误'))
        default:
            return Promise.reject(error)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      return Promise.reject(new Error('网络连接失败，请检查网络'))
    } else {
      // 请求配置出错
      return Promise.reject(new Error('请求配置错误'))
    }

    return Promise.reject(error)
  },
)

// 重新封装请求方法，统一处理响应数据
export const http = {
  async get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await request.get<ApiResponse<T>>(url, config)
    return response.data.data
  },

  async post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await request.post<ApiResponse<T>>(url, data, config)
    return response.data.data
  },

  async put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await request.put<ApiResponse<T>>(url, data, config)
    return response.data.data
  },

  async patch<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await request.patch<ApiResponse<T>>(url, data, config)
    return response.data.data
  },

  async delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await request.delete<ApiResponse<T>>(url, config)
    return response.data.data
  },
}

// 导出原始的request，如果需要访问完整响应信息可以使用
export { request }
