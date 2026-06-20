import request from '@/utils/request'

export const getCaptcha = () => {
  return request.get('/captcha', { responseType: 'blob' })
}

export const login = (data: { username: string; password: string; captcha: string }) => {
  return request.post('/login', data)
}
