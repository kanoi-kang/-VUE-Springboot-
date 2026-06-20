import request from '@/utils/request'

export const getAvailableCoupons = () => {
  return request.get('/user/coupons/available')
}

export const getMyCoupons = () => {
  return request.get('/user/coupons/my')
}

export const claimCoupon = (couponId: number) => {
  return request.post(`/user/coupons/receive/${couponId}`)
}

export const calculateDiscount = (couponId: number, orderAmount: number) => {
  return request.get('/user/coupons/calculate', { params: { couponId, orderAmount } })
}
