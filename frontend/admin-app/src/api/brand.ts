import request from '@/utils/request'

export const getBrands = () => {
  return request.get('/brands')
}

export const createBrand = (data: { name: string; logo: string }) => {
  return request.post('/brands', data)
}

export const updateBrand = (id: number, data: { name: string; logo: string }) => {
  return request.put(`/brands/${id}`, data)
}

export const deleteBrand = (id: number) => {
  return request.delete(`/brands/${id}`)
}
