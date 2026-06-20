import request from '@/utils/request'

export const getUsers = (params: { page?: number; size?: number; keyword?: string }) => {
  return request.get('/users', { params })
}

export const getUserDetail = (id: number) => {
  return request.get(`/users/${id}`)
}

export const updateUserStatus = (id: number, status: boolean) => {
  return request.put(`/users/${id}/status`, { status })
}