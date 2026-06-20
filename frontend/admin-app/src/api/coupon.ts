import request from '@/utils/request'

export const getCoupons = () => {
  return request.get('/coupons')
}

export const createCoupon = (data: Record<string, unknown>) => {
  return request.post('/coupons', data)
}

export const updateCoupon = (id: number, data: Record<string, unknown>) => {
  return request.put(`/coupons/${id}`, data)
}

export const deleteCoupon = (id: number) => {
  return request.delete(`/coupons/${id}`)
}
