<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCollections, removeCollection } from '@/api'
import { ElMessage } from 'element-plus'
import type { Collection } from '@/types'

const router = useRouter()

const collections = ref<Collection[]>([])

const loadCollections = async () => {
  try {
    const data = await getCollections()
    collections.value = data
  } catch (error) {
    console.error('Failed to load collections:', error)
  }
}

const handleRemove = async (productId: number) => {
  try {
    await removeCollection(productId)
    collections.value = collections.value.filter((c: Collection) => c.productId !== productId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const goToProduct = (id: number) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  loadCollections()
})
</script>

<template>
  <div class="collections-page">
    <div class="container">
      <h2 class="page-title">我的收藏</h2>
      
      <div v-if="collections.length === 0" class="empty-state">
        <p>暂无收藏</p>
      </div>
      
      <div v-else class="collections-list">
        <div v-for="item in collections" :key="item.id" class="collection-item">
          <div class="item-image" @click="goToProduct(item.productId)">
            <img :src="item.productPic" :alt="item.productName" />
          </div>
          <div class="item-info" @click="goToProduct(item.productId)">
            <h3>{{ item.productName }}</h3>
            <div>¥{{ item.productPrice.toFixed(2) }}</div>
          </div>
          <button @click="handleRemove(item.productId)">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.collections-page {
  padding: 20px 0;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px;
}

.collections-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.collection-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.item-info {
  flex: 1;
  cursor: pointer;
}

.item-info h3 {
  font-size: 14px;
}

.collection-item button {
  padding: 8px 16px;
  background: #fff1f0;
  color: #f56c6c;
  border: none;
  border-radius: 4px;
}
</style>
