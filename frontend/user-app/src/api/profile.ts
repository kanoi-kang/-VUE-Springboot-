import request from '@/utils/request'

export const getUserProfile = () => {
  return request.get('/user/me')
}

export const updateUserProfile = (data: Record<string, unknown>) => {
  return request.put('/user/me', data)
}

export const uploadAvatar = (data: FormData) => {
  return request.post('/user/me/avatar', data)
}
