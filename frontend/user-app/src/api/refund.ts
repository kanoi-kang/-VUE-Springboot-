import request from '@/utils/request'

export interface RefundRequest {
  orderId: number
  reason: string
  description?: string
}

export interface Refund {
  id: number
  orderId: number
  orderNo: string
  refundNo: string
  reason: string
  description?: string
  amount: number
  status: number
  statusText: string
  adminRemark?: string
  createTime: string
  processTime?: string
}

export const createRefund = (data: RefundRequest) => {
  return request.post('/user/refunds', data)
}

export const getMyRefunds = (): Promise<Refund[]> => {
  return request.get('/user/refunds')
}

export const getRefundDetail = (refundId: number): Promise<Refund> => {
  return request.get(`/user/refunds/${refundId}`)
}