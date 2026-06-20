import request from '@/utils/request'

export const getCategories = () => {
  return request.get('/categories')
}

export const getCategoryTree = () => {
  return request.get('/categories/tree')
}

export const createCategory = (data: { name: string; parentId?: number }) => {
  return request.post('/categories', data)
}

export const updateCategory = (id: number, data: { name: string }) => {
  return request.put(`/categories/${id}`, data)
}

export const deleteCategory = (id: number) => {
  return request.delete(`/categories/${id}`)
}