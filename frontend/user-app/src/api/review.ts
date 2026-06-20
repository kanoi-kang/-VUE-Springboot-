import request from '@/utils/request'

export const getProductReviews = (productId: number): Promise<any> => {
  return request.get(`/user/reviews/product/${productId}`)
}

export const getProductReviewStats = (productId: number): Promise<any> => {
  return request.get(`/user/reviews/product/${productId}/stats`)
}

export const getMyReviews = (): Promise<any> => {
  return request.get('/user/reviews/my')
}

export const createReview = (data: { productId: number; rating: number; content: string }): Promise<any> => {
  return request.post('/user/reviews', data)
}
