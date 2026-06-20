<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail } from '@/api/admin'

const route = useRoute()
const order = ref<any>(null)

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 19)
}

const getStatusText = (status: number) => {
  switch (status) {
    case -1: return '已取消'
    case 0: return '待付款'
    case 1: return '待发货'
    case 2: return '待收货'
    case 3: return '已完成'
    default: return `未知(${status})`
  }
}

const loadOrder = async () => {
  const id = parseInt(route.params.id as string)
  try {
    const res: any = await getOrderDetail(id)
    order.value = res?.data
  } catch (error) {
    console.error('Failed to load order:', error)
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<template>
  <div class="order-detail">
    <h2>订单详情</h2>

    <div v-if="order" class="detail-content">
      <div class="info-section">
        <h3>订单信息</h3>
        <div>订单号: {{ order.orderNo }}</div>
        <div>状态: {{ getStatusText(order.status) }}</div>
        <div>创建时间: {{ formatDate(order.createTime) }}</div>
      </div>

      <div class="info-section">
        <h3>商品信息</h3>
        <div v-for="item in order.items" :key="item.id" class="order-item">
          <span>{{ item.productName }}</span>
          <span>x{{ item.quantity }}</span>
          <span>¥{{ item.price }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-detail {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

h2 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-section {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.info-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.info-section div {
  padding: 6px 0;
}

.order-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}
</style>
