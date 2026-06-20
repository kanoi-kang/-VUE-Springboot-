import request from '@/utils/request'

export const getBrands = () => {
  return request.get('/goods/brands')
}

export const getBrandById = (id: number) => {
  return request.get(`/goods/brands/${id}`)
}

export const getBrandProducts = (id: number, params: { page?: number; size?: number }) => {
  return request.get('/goods/products', { params: { ...params, brandId: id } })
}