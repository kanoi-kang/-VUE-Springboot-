<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders } from '@/api'
import { ElMessage } from 'element-plus'
import type { Order } from '@/types'

const router = useRouter()

const orders = ref<Order[]>([])
const loading = ref(true)

const loadOrders = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }
    
    const data = await getOrders({ page: 1, size: 20 })
    orders.value = data?.records || data?.items || []
    
    if (orders.value.length === 0) {
      console.log('订单列表为空，可能是刚创建订单或没有订单')
    }
  } catch (error: any) {
    console.error('Failed to load orders:', error)
    if (error.message.includes('401') || error.message.includes('未授权')) {
      ElMessage.error('请重新登录')
      localStorage.removeItem('token')
      router.push('/login')
    } else {
      ElMessage.error('加载订单失败: ' + error.message)
    }
  } finally {
    loading.value = false
  }
}

const goToDetail = (id: number) => {
  router.push(`/order/${id}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<template>
  <div class="order-list-page">
    <div class="container">
      <h2 class="page-title">我的订单</h2>
      
      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>
      
      <div v-else-if="orders.length === 0" class="empty-state">
        <p>暂无订单</p>
        <button @click="router.push('/products')">去购物</button>
      </div>
      
      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card" @click="goToDetail(order.id)">
          <div class="order-header">
            <span>订单号: {{ order.orderNo }}</span>
            <span :class="['status', `status-${order.status}`]">{{ order.statusText }}</span>
          </div>
          <div class="order-items">
            <div v-for="item in order.items.slice(0, 2)" :key="item.id" class="order-item">
              <img :src="item.productImage || item.pic" :alt="item.productName" />
              <span>{{ item.productName }}</span>
            </div>
            <span v-if="order.items.length > 2" class="more-items">等{{ order.items.length }}件商品</span>
          </div>
          <div class="order-footer">
            <span>共{{ order.items.length }}件商品</span>
            <span class="total">¥{{ order.payAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-list-page {
  padding: 20px 0;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 40px;
}

.empty-state button {
  margin-top: 16px;
  padding: 10px 20px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.status.pending,
.status.status-0 {
  background: #fef3c7;
  color: #d97706;
}

.status.delivering,
.status.status-1,
.status.status-2 {
  background: #dbeafe;
  color: #2563eb;
}

.status.completed,
.status.status-3 {
  background: #f3f4f6;
  color: #6b7280;
}

.status.cancelled,
.status.status--1 {
  background: #fef2f2;
  color: #dc2626;
}

.order-items {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-item img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.order-item span {
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.more-items {
  font-size: 14px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.total {
  font-weight: bold;
  color: #f56c6c;
}
</style>