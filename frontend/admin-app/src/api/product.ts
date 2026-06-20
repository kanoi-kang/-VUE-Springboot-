import request from '@/utils/request'

export const getProducts = (params: {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
}) => {
  return request.get('/products', { params })
}

export const getProductDetail = (id: number) => {
  return request.get(`/products/${id}`)
}

export const createProduct = (data: Record<string, unknown>) => {
  return request.post('/products', data)
}

export const updateProduct = (id: number, data: Record<string, unknown>) => {
  return request.put(`/products/${id}`, data)
}

export const deleteProduct = (id: number) => {
  return request.delete(`/products/${id}`)
}

export const publishProduct = (id: number) => {
  return request.put(`/products/${id}/publish`)
}