import axios from 'axios'
import request from '@/utils/request'

const captchaInstance = axios.create({
  baseURL: (import.meta as any).env.VITE_API_BASE_URL,
  responseType: 'blob',
  withCredentials: true
})

export const getCaptcha = () => {
  return captchaInstance.get('/user/auth/captcha').then(res => res.data)
}

export const login = (data: { username: string; password: string; captcha: string }) => {
  return request.post('/user/auth/login', data)
}

export const register = (data: { username: string; password: string; captcha: string }) => {
  return request.post('/user/auth/register', data)
}

export const getUserInfo = () => {
  return request.get('/user/me')
}
