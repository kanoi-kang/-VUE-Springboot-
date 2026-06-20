import request from '@/utils/request'
import type { Category } from '@/types'

export const getCategories = (): Promise<Category[]> => {
  return request.get('/goods/categories')
}

export const getCategoryTree = (): Promise<Category[]> => {
  return request.get('/goods/categories/tree')
}

export const getCategoryProducts = (id: number, params: { page?: number; size?: number }) => {
  return request.get(`/goods/products`, { params: { ...params, categoryId: id } })
}
