import request from '@/utils/request'
import type { Product } from '@/types'

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export const getProducts = (params: {
  page?: number
  pageSize?: number
  categoryId?: number
  brandId?: number
  keyword?: string
  sortBy?: string
  sortDir?: string
  isPublish?: number
}): Promise<PageResult<Product>> => {
  return request.get('/goods/products', { params: { ...params, isPublish: 1 } })
}

export const getProductDetail = (id: number): Promise<Product> => {
  return request.get(`/goods/products/${id}`)
}

export const searchProducts = (keyword: string): Promise<PageResult<Product>> => {
  return request.get('/goods/products', { params: { keyword, isPublish: 1 } })
}

export const getRecommendProducts = (): Promise<Product[]> => {
  return request.get('/goods/products/best-sellers')
}

export const getNewProducts = (): Promise<Product[]> => {
  return request.get('/goods/products/new')
}

export const getHotProducts = (): Promise<Product[]> => {
  return request.get('/goods/products/hot')
}
