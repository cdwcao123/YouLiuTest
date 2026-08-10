import axios from 'axios'

/**
 * 统一 axios 实例：
 * - baseURL 为 /api，开发环境由 Vite 代理转发到 Java 后端（见 vite.config.ts）
 * - 响应拦截器直接返回 res.data，即后端统一包装的 { code, message, data }
 * - 请求失败时统一打印错误信息，并继续 reject 交给调用方处理
 */
const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.response.use(
  (res) => res.data,
  (error) => {
    const msg = error.response?.data?.message || error.message || '网络错误'
    console.error('API Error:', msg)
    return Promise.reject(error)
  }
)

export default api
