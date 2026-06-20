<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, checkCollection, addCollection, removeCollection } from '@/api'
import { getProductReviews, getProductReviewStats, createReview } from '@/api/review'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import type { Product } from '@/types'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref<Product | null>(null)
const quantity = ref(1)
const isCollected = ref(false)
const activeTab = ref('detail')
const reviews = ref<any[]>([])
const reviewStats = ref<{ averageRating: number; totalCount: number }>({ averageRating: 0, totalCount: 0 })
const newReviewContent = ref('')
const newReviewRating = ref(5)
const submittingReview = ref(false)
const showReviewModal = ref(false)
const currentImageIndex = ref(0)

const productImages = (): string[] => {
  if (!product.value?.images) return []
  return product.value.images
}

const hasMultipleImages = (): boolean => {
  return productImages().length > 1
}

const prevImage = () => {
  const images = productImages()
  if (images.length <= 1) return
  currentImageIndex.value = currentImageIndex.value === 0 ? images.length - 1 : currentImageIndex.value - 1
}

const nextImage = () => {
  const images = productImages()
  if (images.length <= 1) return
  currentImageIndex.value = (currentImageIndex.value + 1) % images.length
}

const loadProduct = async () => {
  const id = parseInt(route.params.id as string)
  try {
    const data: any = await getProductDetail(id)
    if (data) {
      // 优先用 pic 字段，然后从 pics 或 images 获取
      const images: string[] = []
      if (data.pic) {
        images.push(data.pic)
      }
      if (data.pics && Array.isArray(data.pics)) {
        for (const p of data.pics) {
          if (!images.includes(p)) images.push(p)
        }
      }
      if (data.images && Array.isArray(data.images)) {
        for (const img of data.images) {
          const url = typeof img === 'string' ? img : img.url
          if (url && !images.includes(url)) images.push(url)
        }
      }
      data.images = images
      currentImageIndex.value = 0
      product.value = data as Product
    } else {
      product.value = null
    }

    const result = await checkCollection(id)
    isCollected.value = result?.collected || false
  } catch (error) {
    console.error('Failed to load product:', error)
  }
}

const handleToggleCollect = async () => {
  if (!product.value) return
  
  try {
    if (isCollected.value) {
      await removeCollection(product.value.id)
      isCollected.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addCollection(product.value.id)
      isCollected.value = true
      ElMessage.success('已添加到收藏')
    }
  } catch (error: any) {
    console.error('收藏操作失败:', error)
    const msg = error?.response?.data?.message || error?.message || '操作失败'
    ElMessage.error(msg.includes('401') || msg.includes('登录') ? '请先登录' : msg)
  }
}

const loadReviews = async () => {
  const id = parseInt(route.params.id as string)
  try {
    const [reviewsRes, statsRes] = await Promise.all([
      getProductReviews(id),
      getProductReviewStats(id)
    ])
    reviews.value = Array.isArray(reviewsRes) ? reviewsRes : (reviewsRes?.content || reviewsRes?.records || [])
    reviewStats.value = {
      averageRating: statsRes?.averageRating || 0,
      totalCount: statsRes?.total || 0
    }
  } catch (error) {
    console.error('Failed to load reviews:', error)
  }
}

const onQtyInputChange = () => {
  if (!product.value) return
  quantity.value = Math.max(1, Math.min(product.value.stock, quantity.value || 1))
}

const onQtyInputBlur = () => {
  if (!product.value) return
  if (!quantity.value || quantity.value < 1) {
    quantity.value = 1
  } else if (quantity.value > product.value.stock) {
    quantity.value = product.value.stock
  }
}

const handleAddToCart = async () => {
  if (!product.value) return
  
  if (product.value.stock <= 0) {
    ElMessage.error('该商品已售罄')
    return
  }
  
  if (quantity.value > product.value.stock) {
    ElMessage.warning(`库存不足，最多只能购买${product.value.stock}件`)
    quantity.value = product.value.stock
    return
  }

  try {
    await cartStore.addItem(product.value.id, quantity.value)
    ElMessage.success('已添加到购物车')
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleBuyNow = () => {
  if (!product.value) return
  
  if (product.value.stock <= 0) {
    ElMessage.error('该商品已售罄')
    return
  }
  
  if (quantity.value > product.value.stock) {
    ElMessage.warning(`库存不足，最多只能购买${product.value.stock}件`)
    quantity.value = product.value.stock
    return
  }
  
  handleAddToCart()
  router.push('/checkout')
}

const handleSubmitReview = async () => {
  if (!newReviewContent.value.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }

  if (!product.value) return

  try {
    submittingReview.value = true
    await createReview({
      productId: product.value.id,
      rating: newReviewRating.value,
      content: newReviewContent.value.trim()
    })
    ElMessage.success('评价成功')
    newReviewContent.value = ''
    newReviewRating.value = 5
    showReviewModal.value = false
    loadReviews()
  } catch (error) {
    ElMessage.error('评价失败')
  } finally {
    submittingReview.value = false
  }
}

const getRatingStars = (rating: number) => {
  return '★'.repeat(rating) + '☆'.repeat(5 - rating)
}

const formatDate = (date: string) => {
  if (!date) return ''
  return date.substring(0, 10)
}

const getImages = (review: any): string[] => {
  const pics = review.picUrls || review.images || []
  return typeof pics === 'string' ? pics.split(',') : pics
}

onMounted(async () => {
  await loadProduct()
  loadReviews()
})
</script>

<template>
  <div class="product-detail-page" v-if="product">

    <div class="container">
      <div class="product-main">
        <div class="product-images">
          <div class="main-image">
            <img :src="productImages()[currentImageIndex]" :alt="product.name" />
            <button v-if="hasMultipleImages()" class="image-nav prev" @click="prevImage">‹</button>
            <button v-if="hasMultipleImages()" class="image-nav next" @click="nextImage">›</button>
            <div v-if="hasMultipleImages()" class="image-indicator">
              {{ currentImageIndex + 1 }} / {{ productImages().length }}
            </div>
          </div>
        </div>

        <div class="product-info">
          <h1 class="product-name">
            {{ product.name }}
            <button class="collect-btn" :class="{ collected: isCollected }" @click.stop="handleToggleCollect">
              <span class="heart-icon">{{ isCollected ? '❤' : '🤍' }}</span>
            </button>
          </h1>
          <div class="product-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ product.price.toFixed(2) }}</span>
          </div>
          <div class="product-desc">{{ product.description }}</div>

          <div class="product-stock" :class="{ 'stock-empty': product.stock <= 0 }">
            <span>库存:</span>
            <span v-if="product.stock <= 0" class="stock-warning">已售罄</span>
            <span v-else-if="product.stock <= 10" class="stock-warning">仅剩{{ product.stock }}件</span>
            <span v-else>{{ product.stock }} 件</span>
          </div>

          <div class="quantity-selector" v-if="product.stock > 0">
            <span class="quantity-label">数量:</span>
            <button class="qty-btn" @click="quantity = Math.max(1, quantity - 1)">-</button>
            <input
              type="number"
              class="qty-input"
              v-model.number="quantity"
              @change="onQtyInputChange"
              @blur="onQtyInputBlur"
              min="1"
              :max="product.stock"
            />
            <button class="qty-btn" @click="quantity = Math.min(product.stock, quantity + 1)">+</button>
          </div>

          <div class="product-actions">
            <button class="btn-cart" @click="handleAddToCart" :disabled="product.stock <= 0">
              {{ product.stock <= 0 ? '已售罄' : '加入购物车' }}
            </button>
            <button class="btn-buy" @click="handleBuyNow" :disabled="product.stock <= 0">
              {{ product.stock <= 0 ? '已售罄' : '立即购买' }}
            </button>
          </div>
        </div>
      </div>

      <div class="product-tabs">
        <div class="tabs-header">
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'detail' }"
            @click="activeTab = 'detail'"
          >
            商品详情
          </button>
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'reviews' }"
            @click="activeTab = 'reviews'"
          >
            商品评价 ({{ reviewStats.totalCount }})
          </button>
        </div>

        <div class="tabs-content">
          <div v-if="activeTab === 'detail'" class="tab-panel">
            <div class="product-content" v-html="product.content"></div>
          </div>

          <div v-if="activeTab === 'reviews'" class="tab-panel">
            <div class="reviews-header">
              <div class="reviews-summary">
                <div class="summary-score">
                  <span class="score-value">{{ reviewStats.averageRating.toFixed(1) }}</span>
                  <span class="score-label">分</span>
                </div>
                <div class="summary-stars">{{ getRatingStars(Math.round(reviewStats.averageRating)) }}</div>
                <div class="summary-count">{{ reviewStats.totalCount }} 条评价</div>
              </div>
              <button class="btn-write-review" @click="showReviewModal = true">写评价</button>
            </div>

            <div v-if="reviews.length === 0" class="empty-reviews">
              <p>暂无评价，看看大家的评价吧~</p>
            </div>

            <div v-else class="reviews-list">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="review-user">
                    <div class="user-avatar-wrapper">
                      <img v-if="review.avatar" :src="review.avatar" class="user-avatar-img" alt="头像" />
                      <div v-else class="user-avatar">{{ (review.nickname || review.username || 'U').charAt(0).toUpperCase() }}</div>
                    </div>
                    <span class="user-name">{{ review.nickname || review.username || '匿名用户' }}</span>
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
      </div>
    </div>

    <div v-if="showReviewModal" class="modal-overlay" @click.self="showReviewModal = false">
      <div class="modal-content">
        <h3 class="modal-title">商品评价</h3>
        <div class="rating-select">
          <span class="rating-label">评分:</span>
          <div class="rating-stars">
            <button
              v-for="star in 5"
              :key="star"
              class="star-btn"
              :class="{ active: star <= newReviewRating }"
              @click="newReviewRating = star"
            >
              ★
            </button>
          </div>
          <span class="rating-text">{{ newReviewRating }}星</span>
        </div>
        <textarea
          v-model="newReviewContent"
          class="review-textarea"
          placeholder="分享您的购物体验..."
          rows="4"
        ></textarea>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showReviewModal = false">取消</button>
          <button class="btn-submit" @click="handleSubmitReview" :disabled="submittingReview">
            {{ submittingReview ? '提交中...' : '提交评价' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-detail-page {
  padding: 32px 0;
  background: #F8F9FA;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.product-main {
  display: flex;
  gap: 40px;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.product-images {
  width: 450px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  height: 450px;
  border-radius: 12px;
  overflow: hidden;
  background: #f0f0f0;
  position: relative;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.image-nav:hover {
  background: rgba(0, 0, 0, 0.7);
}

.image-nav.prev {
  left: 10px;
}

.image-nav.next {
  right: 10px;
}

.image-indicator {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 26px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.collect-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 24px;
  padding: 4px;
  transition: transform 0.2s;
}

.collect-btn:hover {
  transform: scale(1.2);
}

.heart-icon {
  transition: all 0.2s;
}

.product-price {
  background: #F8F9FA;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.price-symbol {
  font-size: 18px;
  color: #e74c3c;
}

.price-value {
  font-size: 32px;
  font-weight: bold;
  color: #e74c3c;
}

.product-desc {
  font-size: 15px;
  color: #595959;
  line-height: 1.8;
  margin-bottom: 24px;
}

.product-stock {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 24px;
}

.product-stock.stock-empty {
  color: #e74c3c;
  font-weight: bold;
}

.stock-warning {
  color: #e74c3c;
  font-weight: bold;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
}

.quantity-label {
  font-size: 15px;
  color: #2C3E50;
  font-weight: 500;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #dee2e6;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 18px;
  color: #2C3E50;
  transition: all 0.3s;
}

.qty-btn:hover {
  border-color: #E67E22;
  color: #E67E22;
}

.qty-input {
  width: 60px;
  height: 36px;
  text-align: center;
  font-size: 16px;
  color: #2C3E50;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  outline: none;
}

.qty-input:hover {
  border-color: #E67E22;
}

.qty-input:focus {
  border-color: #E67E22;
  box-shadow: 0 0 0 2px rgba(230, 126, 34, 0.2);
}

.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.qty-input[type=number] {
  -moz-appearance: textfield;
}

.product-actions {
  display: flex;
  gap: 16px;
}

.btn-cart {
  flex: 1;
  padding: 14px 32px;
  background: #fff;
  color: #E67E22;
  border: 2px solid #E67E22;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cart:hover {
  background: #E67E22;
  color: #fff;
}

.btn-cart:disabled,
.btn-buy:disabled {
  background: #95a5a6;
  border-color: #95a5a6;
  color: #fff;
  cursor: not-allowed;
}

.btn-cart:disabled:hover,
.btn-buy:disabled:hover {
  background: #95a5a6;
  border-color: #95a5a6;
  color: #fff;
}

.btn-buy {
  flex: 1;
  padding: 14px 32px;
  background: #E67E22;
  color: #fff;
  border: 2px solid #E67E22;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-buy:hover {
  background: #d35400;
  border-color: #d35400;
}

.product-tabs {
  margin-top: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
}

.tab-btn {
  padding: 16px 32px;
  background: none;
  border: none;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-btn.active {
  color: #E67E22;
  font-weight: 500;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #E67E22;
}

.tab-btn:hover {
  color: #E67E22;
}

.tabs-content {
  padding: 24px;
}

.tab-panel {
  min-height: 200px;
}

.product-content {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}

.product-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.reviews-summary {
  display: flex;
  align-items: center;
  gap: 16px;
}

.summary-score {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: #E67E22;
}

.score-label {
  font-size: 16px;
  color: #666;
}

.summary-stars {
  font-size: 20px;
  color: #f5a623;
  letter-spacing: 2px;
}

.summary-count {
  font-size: 14px;
  color: #999;
}

.btn-write-review {
  padding: 10px 24px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-write-review:hover {
  background: #d35400;
}

.empty-reviews {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.review-rating {
  font-size: 14px;
  color: #f5a623;
  letter-spacing: 1px;
}

.review-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.review-pic {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.review-footer {
  display: flex;
  justify-content: flex-end;
}

.review-date {
  font-size: 12px;
  color: #999;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px;
}

.rating-select {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.rating-label {
  font-size: 14px;
  color: #333;
}

.rating-stars {
  display: flex;
  gap: 4px;
}

.star-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  font-size: 24px;
  color: #ddd;
  cursor: pointer;
  transition: all 0.2s;
}

.star-btn.active {
  color: #f5a623;
}

.star-btn:hover {
  transform: scale(1.1);
}

.rating-text {
  font-size: 14px;
  color: #666;
}

.review-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  margin-bottom: 16px;
}

.review-textarea:focus {
  outline: none;
  border-color: #E67E22;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel,
.btn-submit {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e8e8e8;
}

.btn-submit {
  background: #E67E22;
  color: #fff;
}

.btn-submit:hover:not(:disabled) {
  background: #d35400;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 992px) {
  .product-main {
    flex-direction: column;
    padding: 20px;
  }

  .product-images {
    width: 100%;
  }

  .main-image {
    height: 350px;
  }
}

@media (max-width: 576px) {
  .product-detail-page {
    padding: 16px 0;
  }

  .main-image {
    height: 280px;
  }

  .product-name {
    font-size: 20px;
  }

  .price-value {
    font-size: 26px;
  }

  .product-actions {
    flex-direction: column;
  }

  .btn-cart,
  .btn-buy {
    padding: 12px 24px;
    font-size: 15px;
  }

  .tab-btn {
    padding: 12px 16px;
    font-size: 14px;
  }

  .reviews-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .score-value {
    font-size: 36px;
  }
}
</style>
