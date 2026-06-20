import request from '@/utils/request'

export const getOrders = (params: {
  page?: number
  size?: number
  status?: string
  keyword?: string
}) => {
  return request.get('/orders', { params })
}

export const getOrderDetail = (id: number) => {
  return request.get(`/orders/${id}`)
}

export const shipOrder = (id: number, data: { trackingNo: string }) => {
  return request.put(`/orders/${id}/ship`, data)
}

export const refundOrder = (id: number) => {
  return request.put(`/orders/${id}/refund`)
}