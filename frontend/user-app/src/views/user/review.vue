<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyReviews } from '@/api/review'
import { ElMessage } from 'element-plus'
import type { Review } from '@/types'

const reviews = ref<Review[]>([])
const loading = ref(false)

const loadReviews = async () => {
  try {
    loading.value = true
    const data = await getMyReviews()
    reviews.value = Array.isArray(data) ? data : (data?.records || [])
  } catch (error) {
    console.error('Failed to load reviews:', error)
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

const getRatingStars = (rating: number) => {
  return '★'.repeat(rating) + '☆'.repeat(5 - rating)
}

const formatDate = (date: string) => {
  if (!date) return ''
  return date.substring(0, 10)
}

const getImages = (review: Review): string[] => {
  const pics = review.picUrls || review.images || []
  return typeof pics === 'string' ? pics.split(',') : pics
}

onMounted(() => {
  loadReviews()
})
</script>

<template>
  <div class="review-page">
    <div class="container">
      <h2 class="page-title">我的评价</h2>

      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="reviews.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <p class="empty-text">您还没有发表过评价</p>
        <p class="empty-hint">去商品详情页购买并评价吧~</p>
      </div>

      <div v-else class="reviews-list">
        <div v-for="review in reviews" :key="review.id" class="review-card">
          <div class="review-header">
            <div class="product-info">
              <span class="product-label">{{ review.productName || '商品评价' }}</span>
              <span class="product-id">商品ID: {{ review.productId }}</span>
            </div>
            <div class="review-rating">{{ getRatingStars(review.rating) }}</div>
          </div>
          <div class="review-content">{{ review.content }}</div>
          <div class="review-images" v-if="getImages(review).length > 0">
            <img
              v-for="(pic, idx) in getImages(review)"
              :key="idx"
              :src="pic"
              class="review-pic"
            />
          </div>
          <div class="review-footer">
            <span class="review-date">{{ formatDate(review.createTime || review.createdAt || '') }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.review-page {
  padding: 32px 0;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 24px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #E67E22;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #333;
  margin: 0 0 8px;
}

.empty-hint {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.product-id {
  font-size: 12px;
  color: #999;
}

.review-rating {
  font-size: 16px;
  color: #f5a623;
  letter-spacing: 2px;
}

.review-content {
  font-size: 14px;
  color: #333;
  line-height: 1.7;
  margin-bottom: 14px;
}

.review-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.review-pic {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.review-pic:hover {
  transform: scale(1.05);
}

.review-footer {
  display: flex;
  justify-content: flex-end;
}

.review-date {
  font-size: 12px;
  color: #999;
}

@media (max-width: 576px) {
  .review-page {
    padding: 16px 0;
  }

  .page-title {
    font-size: 18px;
  }

  .review-pic {
    width: 70px;
    height: 70px;
  }
}
</style>
