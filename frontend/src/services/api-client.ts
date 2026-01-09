import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { API_BASE_URL } from '@/constants'
import { handleApiError, type ApiException } from '@/utils/error-handler'

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

// Request interceptor
apiClient.interceptors.request.use(
  (config) => {
    // Add API key if needed (future)
    // const apiKey = getApiKey()
    // if (apiKey) {
    //   config.headers['X-API-Key'] = apiKey
    // }

    // Log request in development
    if (import.meta.env.DEV) {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, {
        params: config.params,
        data: config.data,
      })
    }

    return config
  },
  (error) => {
    console.error('[API Request Error]', error)
    return Promise.reject(error)
  }
)

// Response interceptor
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // Log response in development
    if (import.meta.env.DEV) {
      console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, {
        status: response.status,
        data: response.data,
      })
    }

    return response
  },
  (error) => {
    // Transform error to ApiException
    const apiError = handleApiError(error)

    // Enhanced error logging with connection diagnostics
    const errorDetails = {
      code: apiError.code,
      message: apiError.message,
      statusCode: apiError.statusCode,
      details: apiError.details,
      requestId: apiError.requestId,
      url: error.config?.url,
      baseURL: error.config?.baseURL,
      method: error.config?.method,
    }

    console.error('[API Error]', errorDetails)

    // Log connection diagnostics for network errors
    if (apiError.code === 'NETWORK_ERROR' || apiError.code === 'ERR_NETWORK') {
      console.error('[Connection Diagnostics]', {
        baseURL: API_BASE_URL,
        message: 'Cannot connect to backend. Please check:',
        checks: [
          '1. Is the backend server running?',
          '2. Is the backend URL correct?',
          `3. Current API_BASE_URL: ${API_BASE_URL}`,
          '4. Check CORS configuration in backend',
          '5. Check browser console for CORS errors',
        ],
      })
    }

    return Promise.reject(apiError)
  }
)

// API Client methods
export const api = {
  get: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    return apiClient.get<T>(url, config).then((response) => response.data)
  },

  post: <T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> => {
    return apiClient.post<T>(url, data, config).then((response) => response.data)
  },

  put: <T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> => {
    return apiClient.put<T>(url, data, config).then((response) => response.data)
  },

  patch: <T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig
  ): Promise<T> => {
    return apiClient.patch<T>(url, data, config).then((response) => response.data)
  },

  delete: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    return apiClient.delete<T>(url, config).then((response) => response.data)
  },
}

export { apiClient }
export type { ApiException }

