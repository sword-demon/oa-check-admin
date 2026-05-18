import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const SUCCESS_CODE = 200
const HTTP_UNAUTHORIZED_STATUS = 401
const LOGIN_PATH = '/login'
const AUTH_EXPIRED_CODES = new Set([2001, 2004])

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

function isAuthExpiredCode(code: unknown) {
  return AUTH_EXPIRED_CODES.has(Number(code))
}

function redirectToLogin() {
  localStorage.removeItem('token')
  router.push(LOGIN_PATH)
}

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const { code, msg, data } = response.data
    if (code === SUCCESS_CODE) {
      return data
    }
    ElMessage.error(msg || '请求失败')
    if (isAuthExpiredCode(code)) {
      redirectToLogin()
    }
    return Promise.reject(new Error(msg))
  },
  (error) => {
    const responseCode = error.response?.data?.code
    if (error.response?.status === HTTP_UNAUTHORIZED_STATUS || isAuthExpiredCode(responseCode)) {
      redirectToLogin()
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
