<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCategories, getNewProducts, getHotProducts, checkCollection, addCollection, removeCollection } from '@/api'
import type { Category, Product } from '@/types'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const categories = ref<Category[]>([])
const recommendProducts = ref<any[]>([])
const hotProducts = ref<any[]>([])
const collectedIds = ref<Set<number>>(new Set())

const categoryIcons: Record<string, string> = {
  '新疆鲜果': '🍎',
  '特色美酒': '🍷',
  '文旅文创': '💎',
  '畜牧特产': '🐄',
  'default': '🎁'
}

const getCategoryIcon = (name: string): string => {
  for (const key of Object.keys(categoryIcons)) {
    if (name.includes(key)) {
      return categoryIcons[key]
    }
  }
  return categoryIcons['default']
}

const getProductImage = (product: any): string => {
  if (product.pic) return product.pic
  if (product.pics && Array.isArray(product.pics)) {
    return product.pics[0] || '/placeholder.png'
  }
  if (product.images && Array.isArray(product.images)) {
    if (typeof product.images[0] === 'string') {
      return product.images[0]
    }
    return product.images[0]?.url || '/placeholder.png'
  }
  return '/placeholder.png'
}

const loadData = async () => {
  collectedIds.value.clear()
  try {
    const [catData, recData, hotData] = await Promise.all([
      getCategories(),
      getNewProducts(),
      getHotProducts()
    ])
    categories.value = (catData as Category[]) || []
    recommendProducts.value = (recData as Product[]) || []
    hotProducts.value = (hotData as Product[]) || []
    
    await checkAllCollections()
  } catch (error) {
    console.error('Failed to load data:', error)
  }
}

const checkAllCollections = async () => {
  const allProducts = [...recommendProducts.value, ...hotProducts.value]
  const ids: number[] = []
  for (const p of allProducts) {
    if (p.id) ids.push(p.id)
  }
  if (ids.length === 0) return
  
  try {
    for (const id of ids) {
      try {
        const result = await checkCollection(id)
        const collected = result?.collected || false
        if (collected) {
          collectedIds.value.add(id)
        }
      } catch (e) {
      }
    }
  } catch (error) {
    console.error('Failed to check collections:', error)
  }
}

const handleToggleCollect = async (productId: number, event: Event) => {
  event.stopPropagation()
  
  try {
    if (collectedIds.value.has(productId)) {
      await removeCollection(productId)
      collectedIds.value.delete(productId)
      ElMessage.success('已取消收藏')
    } else {
      await addCollection(productId)
      collectedIds.value.add(productId)
      ElMessage.success('已添加到收藏')
    }
  } catch (error: any) {
    console.error('收藏操作失败:', error)
    const msg = error?.response?.data?.message || error?.message || '操作失败'
    ElMessage.error(msg.includes('401') || msg.includes('登录') ? '请先登录' : msg)
  }
}

const goToProduct = (id: number) => {
  router.push(`/product/${id}`)
}

const goToCategory = (id: number) => {
  router.push(`/products?category=${id}`)
}

const goToProducts = () => {
  router.push('/products')
}

const goToCouponCenter = () => {
  router.push('/coupon-center')
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="home-page">
    <section class="hero-banner">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">大美新疆 · 好物直达</h1>
        <p class="hero-subtitle">正宗新疆特产、文旅周边、特色手工艺品一站式选购</p>
        <div class="hero-buttons">
          <button class="hero-btn primary" @click="goToProducts">立即选购</button>
          <button class="hero-btn secondary" @click="goToCouponCenter">领优惠券</button>
        </div>
      </div>
    </section>

    <section class="category-section">
      <div class="container">
        <h2 class="section-title">商品分类</h2>
        <div v-if="categories.length === 0" class="empty-state">暂无分类</div>
        <div v-else class="category-grid">
          <div
            v-for="category in categories"
            :key="category.id"
            class="category-item"
            @click="goToCategory(category.id)"
          >
            <div class="category-icon">{{ getCategoryIcon(category.name) }}</div>
            <h5 class="category-name">{{ category.name }}</h5>
          </div>
        </div>
      </div>
    </section>

    <section class="product-section">
      <div class="container">
        <h2 class="section-title">热门推荐</h2>
        <div v-if="hotProducts.length === 0" class="empty-state">暂无热门商品</div>
        <div v-else class="product-grid">
          <div
            v-for="product in hotProducts"
            :key="product.id"
            class="product-card"
            :class="{ 'out-of-stock': product.stock <= 0 }"
            @click="goToProduct(product.id)"
          >
            <div class="product-image">
              <img :src="getProductImage(product)" :alt="product.name" />
              <div v-if="product.stock <= 0" class="stock-badge sold-out">已售罄</div>
              <div v-else-if="product.stock <= 10" class="stock-badge low-stock">仅剩{{ product.stock }}件</div>
              <button class="collect-btn" :class="{ collected: collectedIds.has(product.id) }" @click="handleToggleCollect(product.id, $event)">
                <span class="heart-icon">{{ collectedIds.has(product.id) ? '❤' : '🤍' }}</span>
              </button>
            </div>
            <div class="product-body">
              <h5 class="product-title">{{ product.name }}</h5>
              <p class="product-price">¥{{ product.price?.toFixed(2) || '0.00' }}</p>
              <button class="product-btn" :disabled="product.stock <= 0">
                {{ product.stock <= 0 ? '已售罄' : '查看详情' }}
              </button>
            </div>
          </div>
        </div>
        <div class="text-center" style="margin-top: 32px;">
          <button class="btn-outline" @click="goToProducts">查看更多商品</button>
        </div>
      </div>
    </section>

    <section class="product-section bg-light">
      <div class="container">
        <h2 class="section-title">精选好物</h2>
        <div v-if="recommendProducts.length === 0" class="empty-state">暂无推荐商品</div>
        <div v-else class="product-grid">
          <div
            v-for="product in recommendProducts"
            :key="product.id"
            class="product-card"
            :class="{ 'out-of-stock': product.stock <= 0 }"
            @click="goToProduct(product.id)"
          >
            <div class="product-image">
              <img :src="getProductImage(product)" :alt="product.name" />
              <div v-if="product.stock <= 0" class="stock-badge sold-out">已售罄</div>
              <div v-else-if="product.stock <= 10" class="stock-badge low-stock">仅剩{{ product.stock }}件</div>
              <button class="collect-btn" :class="{ collected: collectedIds.has(product.id) }" @click="handleToggleCollect(product.id, $event)">
                <span class="heart-icon">{{ collectedIds.has(product.id) ? '❤' : '🤍' }}</span>
              </button>
            </div>
            <div class="product-body">
              <h5 class="product-title">{{ product.name }}</h5>
              <p class="product-price">¥{{ product.price?.toFixed(2) || '0.00' }}</p>
              <button class="product-btn" :disabled="product.stock <= 0">
                {{ product.stock <= 0 ? '已售罄' : '查看详情' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  background: #F8F9FA;
}

.hero-banner {
  position: relative;
  height: 500px;
  background: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)),
    url('https://picsum.photos/id/1036/1920/1080') center/cover;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-content {
  text-align: center;
  color: white;
  z-index: 1;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 16px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 20px;
  margin-bottom: 32px;
  opacity: 0.95;
}

.hero-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 24px;
}

.hero-btn {
  padding: 14px 40px;
  font-size: 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  font-weight: 500;
}

.hero-btn.primary {
  background-color: #E67E22;
  color: white;
}

.hero-btn.primary:hover {
  background-color: #d35400;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(230, 126, 34, 0.3);
}

.hero-btn.secondary {
  background-color: rgba(255, 255, 255, 0.9);
  color: #E67E22;
  border: 2px solid #E67E22;
}

.hero-btn.secondary:hover {
  background-color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(230, 126, 34, 0.2);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.category-section {
  padding: 60px 0;
  background: #fff;
}

.product-section {
  padding: 60px 0;
}

.product-section.bg-light {
  background: #F8F9FA;
}

.section-title {
  text-align: center;
  font-size: 28px;
  font-weight: bold;
  color: #2C3E50;
  margin-bottom: 40px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.category-item {
  text-align: center;
  padding: 24px 16px;
  border-radius: 10px;
  background: #F8F9FA;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.category-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.category-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.category-name {
  font-size: 16px;
  color: #2C3E50;
  margin: 0;
  font-weight: 500;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f0f0f0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-body {
  padding: 16px;
  text-align: center;
}

.product-title {
  font-size: 15px;
  color: #2C3E50;
  margin: 0 0 8px;
  font-weight: normal;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
  margin: 0 0 12px;
}

.product-btn {
  width: 100%;
  padding: 10px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.product-btn:hover {
  background: #d35400;
}

.product-btn:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.product-image {
  position: relative;
}

.stock-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  z-index: 1;
}

.stock-badge.sold-out {
  background: #e74c3c;
  color: #fff;
}

.stock-badge.low-stock {
  background: #f39c12;
  color: #fff;
}

.collect-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.collect-btn:hover {
  transform: scale(1.1);
  background: #fff;
}

.collect-btn.collected .heart-icon {
  color: #e74c3c;
}

.product-card.out-of-stock {
  opacity: 0.7;
}

.product-card.out-of-stock:hover {
  transform: none;
}

.btn-outline {
  padding: 12px 32px;
  background: transparent;
  color: #E67E22;
  border: 2px solid #E67E22;
  border-radius: 4px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-outline:hover {
  background: #E67E22;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 60px;
  color: #7f8c8d;
  font-size: 16px;
}

.text-center {
  text-align: center;
}

@media (max-width: 992px) {
  .hero-banner {
    height: 400px;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .category-grid,
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .category-icon {
    font-size: 36px;
  }

  .product-image {
    height: 160px;
  }
}

@media (max-width: 576px) {
  .hero-banner {
    height: 320px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-btn {
    padding: 12px 28px;
    font-size: 14px;
  }

  .category-section,
  .product-section {
    padding: 40px 0;
  }

  .section-title {
    font-size: 22px;
    margin-bottom: 24px;
  }

  .category-grid,
  .product-grid {
    gap: 12px;
  }

  .category-item {
    padding: 16px 8px;
  }

  .category-icon {
    font-size: 32px;
  }

  .category-name {
    font-size: 14px;
  }

  .product-image {
    height: 140px;
  }

  .product-body {
    padding: 12px;
  }

  .product-title {
    font-size: 14px;
  }

  .product-price {
    font-size: 16px;
  }

  .product-btn {
    padding: 8px;
    font-size: 13px;
  }
}
</style>