export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
export const APP_ENV = import.meta.env.VITE_APP_ENV || 'development'

// Log API configuration in development
if (import.meta.env.DEV) {
  console.log('[API Configuration]', {
    API_BASE_URL,
    APP_ENV,
    'Note': 'Make sure backend is running on the configured URL',
  })
}

