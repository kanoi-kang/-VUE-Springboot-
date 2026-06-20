import request from '@/utils/request'

export const adminLogin = (data: { username: string; password: string; captcha: string }) => {
  return request.post('/login', data)
}

export const getAdminProfile = () => {
  return request.get('/profile')
}

export const updateAdminPassword = (oldPassword: string, newPassword: string) => {
  return request.put('/password', null, { params: { oldPassword, newPassword } })
}

export const getAdminDashboard = () => {
  return request.get('/dashboard')
}

export const getRevenueStats = (days: number = 7) => {
  return request.get('/dashboard/revenue', { params: { days } })
}

export const getRevenueTrend = (days: number = 7) => {
  return request.get('/dashboard/revenue', { params: { days } })
}

export const getOrderTrend = (days: number = 7) => {
  return request.get('/dashboard/orders-trend', { params: { days } })
}

export const getTopProducts = (limit: number = 10) => {
  return request.get('/dashboard/topProducts', { params: { limit } })
}

export const getCategoryStats = () => {
  return request.get('/dashboard/categoryStats')
}

export const getProducts = (params?: {
  page?: number;
  size?: number;
  keyword?: string;
  categoryId?: number;
  brandId?: number;
  status?: number;
}) => {
  return request.get('/products', { params })
}

export const deleteProduct = (id: number) => {
  return request.delete(`/products/${id}`)
}

export const createProduct = (data: any) => {
  return request.post('/products', data)
}

export const updateProduct = (id: number, data: any) => {
  return request.put(`/products/${id}`, data)
}

export const getProductDetail = (id: number) => {
  return request.get(`/products/${id}`)
}

export const getCategories = () => {
  return request.get('/categories')
}

export const getBrands = (params?: { page?: number; size?: number; keyword?: string }) => {
  return request.get('/brands', { params })
}

export const getBrandDetail = (id: number) => {
  return request.get(`/brands/${id}`)
}

export const createBrand = (data: any) => {
  return request.post('/brands', data)
}

export const updateBrand = (id: number, data: any) => {
  return request.put(`/brands/${id}`, data)
}

export const deleteBrand = (id: number) => {
  return request.delete(`/brands/${id}`)
}

export const getCategoryTree = () => {
  return request.get('/categories/tree')
}

export const getCategoryDetail = (id: number) => {
  return request.get(`/categories/${id}`)
}

export const createCategory = (data: any) => {
  return request.post('/categories', data)
}

export const updateCategory = (id: number, data: any) => {
  return request.put(`/categories/${id}`, data)
}

export const deleteCategory = (id: number) => {
  return request.delete(`/categories/${id}`)
}

export const getOrders = (params?: { page?: number; size?: number; status?: number; keyword?: string }) => {
  return request.get('/orders', { params })
}

export const getOrderDetail = (id: number) => {
  return request.get(`/orders/${id}`)
}

export const updateOrder = (id: number, data: any) => {
  return request.put(`/orders/${id}`, data)
}

export const shipOrder = (id: number, trackingNo: string) => {
  return request.put(`/orders/${id}/ship`, null, { params: { trackingNo } })
}

export const cancelOrder = (id: number) => {
  return request.put(`/orders/${id}/cancel`)
}

export const confirmOrder = (id: number) => {
  return request.put(`/orders/${id}/confirm`)
}

export const deleteOrder = (id: number) => {
  return request.delete(`/orders/${id}`)
}

export const getOrderStats = () => {
  return request.get('/orders/stats')
}

export const getCoupons = (params?: { page?: number; size?: number; keyword?: string }) => {
  return request.get('/coupons', { params })
}

export const createCoupon = (data: any) => {
  return request.post('/coupons', data)
}

export const updateCoupon = (id: number, data: any) => {
  return request.put(`/coupons/${id}`, data)
}

export const deleteCoupon = (id: number) => {
  return request.delete(`/coupons/${id}`)
}

export const getCouponDetail = (id: number) => {
  return request.get(`/coupons/${id}`)
}

export const getUsers = (params?: { page?: number; size?: number; keyword?: string; status?: number }) => {
  return request.get('/users', { params })
}

export const getUserDetail = (id: number) => {
  return request.get(`/users/${id}`)
}

export const updateUser = (id: number, data: any) => {
  return request.put(`/users/${id}`, data)
}

export const updateUserStatus = (id: number, status: number) => {
  return request.put(`/users/${id}/status`, null, { params: { status } })
}

export const deleteUser = (id: number) => {
  return request.delete(`/users/${id}`)
}

export const getUserStats = () => {
  return request.get('/users/stats')
}

export const getReviews = (params?: { page?: number; size?: number; productId?: number; isShow?: number; keyword?: string }) => {
  return request.get('/reviews', { params })
}

export const getReviewDetail = (id: number) => {
  return request.get(`/reviews/${id}`)
}

export const updateReviewShowStatus = (id: number, isShow: number) => {
  return request.put(`/reviews/${id}/show`, null, { params: { isShow } })
}

export const updateReview = (id: number, data: any) => {
  return request.put(`/reviews/${id}`, data)
}

export const deleteReview = (id: number) => {
  return request.delete(`/reviews/${id}`)
}

export const deleteReviewsBatch = (ids: number[]) => {
  return request.delete('/reviews/batch', { data: ids })
}

export const getSpecificationsByProductId = (productId: number) => {
  return request.get(`/specifications/product/${productId}`)
}

export const createSpecification = (data: any) => {
  return request.post('/specifications', data)
}

export const createSpecificationsBatch = (data: any[]) => {
  return request.post('/specifications/batch', data)
}

export const updateSpecification = (id: number, data: any) => {
  return request.put(`/specifications/${id}`, data)
}

export const deleteSpecification = (id: number) => {
  return request.delete(`/specifications/${id}`)
}

export const deleteSpecificationsByProductId = (productId: number) => {
  return request.delete(`/specifications/product/${productId}`)
}

export const updateSpecificationStock = (id: number, quantity: number) => {
  return request.put(`/specifications/${id}/stock`, null, { params: { quantity } })
}

export const updateSpecificationPrice = (id: number, price: number) => {
  return request.put(`/specifications/${id}/price`, null, { params: { price } })
}

export const getRefunds = (params?: { page?: number; size?: number; status?: number; keyword?: string }) => {
  return request.get('/refunds', { params })
}

export const getRefundDetail = (refundId: number) => {
  return request.get(`/refunds/${refundId}`)
}

export const approveRefund = (refundId: number, remark?: string) => {
  return request.put(`/refunds/${refundId}/approve`, null, { params: { remark } })
}

export const rejectRefund = (refundId: number, remark?: string) => {
  return request.put(`/refunds/${refundId}/reject`, null, { params: { remark } })
}
