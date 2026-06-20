import request from '@/utils/request'

export const getCartItems = () => {
  return request.get('/user/cart')
}

export const addCartItem = (data: { productId: number; quantity: number }) => {
  return request.post('/user/cart', data)
}

export const updateCartItem = (id: number, data: { quantity: number }) => {
  const url = `/user/cart/${id}?quantity=${data.quantity}`
  return request.put(url)
}

export const removeCartItem = (id: number) => {
  return request.delete(`/user/cart/${id}`)
}

export const clearCart = () => {
  return request.delete('/user/cart')
}
