<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getProducts, checkCollection, addCollection, removeCollection } from '@/api'
import type { Category, Product } from '@/types'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const categories = ref<Category[]>([])
const products = ref<any[]>([])
const selectedCategory = ref<number | null>(null)
const keyword = ref('')
const collectedIds = ref<Set<number>>(new Set())

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

const loadCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = (data as any)?.items || (data as any) || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadProducts = async () => {
  collectedIds.value.clear()
  try {
    const params: any = { pageSize: 100 }
    if (selectedCategory.value) params.categoryId = selectedCategory.value
    if (keyword.value) params.keyword = keyword.value
    const data = await getProducts(params)
    products.value = data?.records || []
    
    await checkAllCollections()
  } catch (error) {
    console.error('Failed to load products:', error)
  }
}

const checkAllCollections = async () => {
  const ids: number[] = []
  for (const p of products.value) {
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

const handleSearch = () => {
  loadProducts()
}

const goToProduct = (id: number) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
  }
  loadCategories()
  loadProducts()
})
</script>

<template>
  <div class="product-list-page">
    <div class="container">
      <div class="search-bar">
        <input v-model="keyword" placeholder="搜索商品..." @keyup.enter="handleSearch" />
        <button @click="handleSearch">搜索</button>
      </div>

      <div class="filter-bar">
        <span class="filter-label">分类:</span>
        <span
          class="filter-item"
          :class="{ active: !selectedCategory }"
          @click="selectedCategory = null; loadProducts()"
        >
          全部
        </span>
        <span
          v-for="category in categories"
          :key="category.id"
          class="filter-item"
          :class="{ active: selectedCategory === category.id }"
          @click="selectedCategory = category.id; loadProducts()"
        >
          {{ category.name }}
        </span>
      </div>

      <div v-if="products.length === 0" class="empty-state">
        <p>暂无商品</p>
      </div>

      <div v-else class="product-grid">
        <div
          v-for="product in products"
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
            <h3 class="product-title">{{ product.name }}</h3>
            <p class="product-price">¥{{ product.price?.toFixed(2) || '0.00' }}</p>
            <button class="product-btn" :disabled="product.stock <= 0">
              {{ product.stock <= 0 ? '已售罄' : '查看详情' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-list-page {
  padding: 32px 0;
  background: #F8F9FA;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 90%;
  margin: 0 auto;
  padding: 0 16px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-bar input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #dee2e6;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}

.search-bar input:focus {
  border-color: #E67E22;
}

.search-bar button {
  padding: 12px 32px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.3s;
}

.search-bar button:hover {
  background: #d35400;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  flex-wrap: wrap;
}

.filter-label {
  font-size: 15px;
  color: #2C3E50;
  font-weight: 500;
  margin-right: 8px;
}

.filter-item {
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  color: #595959;
}

.filter-item:hover {
  color: #E67E22;
}

.filter-item.active {
  background: #E67E22;
  color: #fff;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
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
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
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

.empty-state {
  text-align: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
  color: #7f8c8d;
  font-size: 16px;
}

@media (max-width: 768px) {
  .product-list-page {
    padding: 16px 0;
  }

  .search-bar {
    padding: 16px;
  }

  .filter-bar {
    padding: 12px 16px;
  }

  .filter-item {
    padding: 6px 12px;
    font-size: 13px;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .product-image {
    height: 160px;
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