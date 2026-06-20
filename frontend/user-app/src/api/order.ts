import request from '@/utils/request'

export const getOrders = (params: { page?: number; size?: number; status?: number }): Promise<any> => {
  return request.get('/user/orders', { params })
}

export const getOrderDetail = (id: number): Promise<any> => {
  return request.get(`/user/orders/${id}`)
}

export const createOrder = (data: { addressId: number; couponId?: number; items: { productId: number; quantity: number }[] }): Promise<any> => {
  return request.post('/user/orders', data)
}

export const cancelOrder = (id: number, userId: number): Promise<any> => {
  return request.put(`/user/orders/${id}/cancel`, { userId })
}

export const confirmOrder = (id: number, userId: number): Promise<any> => {
  return request.put(`/user/orders/${id}/confirm`, { userId })
}

export const payOrder = (id: number, data: { paymentMethod: string }): Promise<any> => {
  return request.put(`/user/orders/${id}/pay?payType=${data.paymentMethod}`)
}

export const updateOrderItemQuantity = (orderId: number, itemId: number, quantity: number): Promise<any> => {
  const url = `/user/orders/${orderId}/items/${itemId}/quantity?quantity=${quantity}`
  return request.put(url)
}
