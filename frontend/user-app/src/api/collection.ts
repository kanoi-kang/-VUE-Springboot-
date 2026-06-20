import request from '@/utils/request'

export const getCollections = (): Promise<any> => {
  return request.get('/user/collections')
}

export const addCollection = (productId: number): Promise<any> => {
  return request.post(`/user/collections/${productId}`)
}

export const removeCollection = (productId: number): Promise<any> => {
  return request.delete(`/user/collections/${productId}`)
}

export const checkCollection = (productId: number): Promise<any> => {
  return request.get(`/user/collections/${productId}/status`)
}
