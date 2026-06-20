<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, cancelOrder, confirmOrder, updateOrderItemQuantity } from '@/api'
import { ElMessage } from 'element-plus'
import type { OrderDetail } from '@/types'

const route = useRoute()
const router = useRouter()

const order = ref<OrderDetail | null>(null)

const isPending = computed(() => {
  return order.value?.status === 0
})

const isDelivering = computed(() => {
  return order.value?.status === 2
})

const canRefund = computed(() => {
  const status = order.value?.status
  return status !== undefined && status !== 0 && status !== -1 && status !== 4
})

const statusClass = computed(() => {
  const status = order.value?.status
  if (status === 0) return 'pending'
  if (status === 1 || status === 2) return 'delivering'
  if (status === 3) return 'completed'
  if (status === -1) return 'cancelled'
  return ''
})

const loadOrder = async () => {
  const id = parseInt(route.params.id as string)
  try {
    const data = await getOrderDetail(id)
    order.value = data
  } catch (error) {
    console.error('Failed to load order:', error)
  }
}

const updateQuantity = async (itemId: number, quantity: number) => {
  if (!order.value) return
  
  if (quantity < 1) {
    ElMessage.warning('数量不能小于1')
    return
  }
  
  try {
    await updateOrderItemQuantity(order.value.id, itemId, quantity)
    ElMessage.success('更新成功')
    loadOrder()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const onQtyInputChange = (itemId: number, event: Event) => {
  const input = event.target as HTMLInputElement
  let value = parseInt(input.value) || 1
  
  const item = order.value?.items.find(i => i.id === itemId)
  const maxStock = item?.stock || 999
  
  value = Math.max(1, Math.min(maxStock, value))
  input.value = value.toString()
  
  if (value >= 1) {
    updateQuantity(itemId, value)
  }
}

const onQtyInputBlur = (itemId: number, event: Event) => {
  const input = event.target as HTMLInputElement
  const item = order.value?.items.find(i => i.id === itemId)
  const maxStock = item?.stock || 999
  
  let value = parseInt(input.value) || 1
  
  if (value < 1) {
    value = 1
  } else if (value > maxStock) {
    value = maxStock
    ElMessage.warning(`库存不足，已调整为最大库存 ${maxStock}`)
  }
  
  input.value = value.toString()
  updateQuantity(itemId, value)
}

const handleCancel = async () => {
  if (!order.value) return
  
  try {
    await cancelOrder(order.value.id, order.value.userId)
    ElMessage.success('订单已取消')
    loadOrder()
  } catch (error) {
    ElMessage.error('取消失败')
  }
}

const handleConfirm = async () => {
  if (!order.value) return
  
  try {
    await confirmOrder(order.value.id, order.value.userId)
    ElMessage.success('已确认收货')
    loadOrder()
  } catch (error) {
    ElMessage.error('确认失败')
  }
}

const goBack = () => {
  router.push('/orders')
}

const goToRefund = () => {
  router.push(`/refund/${order.value?.id}`)
}

onMounted(() => {
  loadOrder()
})
</script>

<template>
  <div class="order-detail-page" v-if="order">
    <div class="container">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2 class="page-title">订单详情</h2>
      
      <div class="order-info">
        <div class="info-row">
          <span class="label">订单编号:</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">创建时间:</span>
          <span class="value">{{ order.createdAt }}</span>
        </div>
        <div class="info-row">
          <span class="label">订单状态:</span>
          <span class="value status" :class="statusClass">{{ order.statusText }}</span>
        </div>
      </div>
      
      <div class="address-section">
        <h3 class="section-title">收货地址</h3>
        <div class="address-content">
          <div class="address-name">{{ order.consignee }} {{ order.phone }}</div>
          <div class="address-detail">{{ order.province }} {{ order.city }} {{ order.district }} {{ order.detailAddress }}</div>
        </div>
      </div>
      
      <div class="items-section">
        <h3 class="section-title">商品清单</h3>
        <div v-for="item in order.items" :key="item.id" class="order-item">
          <div class="item-image">
            <img :src="item.pic || item.productImage" :alt="item.productName" />
          </div>
          <div class="item-info">
            <h4 class="item-name">{{ item.productName }}</h4>
            <p v-if="item.specName" class="item-spec">{{ item.specName }}</p>
            <div class="item-price">¥{{ (item.price || 0).toFixed(2) }}</div>
          </div>
          <div class="item-quantity">
            <div class="quantity-control">
              <button
                class="qty-btn minus"
                @click="updateQuantity(item.id, item.quantity - 1)"
                :disabled="item.quantity <= 1 || !isPending"
              >-</button>
              <input
                type="number"
                class="qty-input"
                :value="item.quantity"
                @change="onQtyInputChange(item.id, $event)"
                @blur="onQtyInputBlur(item.id, $event)"
                min="1"
                :disabled="!isPending"
              />
              <button
                class="qty-btn plus"
                @click="updateQuantity(item.id, item.quantity + 1)"
                :disabled="!isPending"
              >+</button>
            </div>
          </div>
          <div class="item-total">
            <span>¥{{ ((item.price || 0) * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      
      <div class="amount-section">
        <h3 class="section-title">费用明细</h3>
        <div class="amount-row">
          <span>商品金额:</span>
          <span>¥{{ order.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-row">
          <span>运费:</span>
          <span>¥{{ (order.freightAmount || 0).toFixed(2) }}</span>
        </div>
        <div class="amount-row" v-if="order.discountAmount && order.discountAmount > 0">
          <span>优惠:</span>
          <span class="discount">-¥{{ order.discountAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-row total">
          <span>实付金额:</span>
          <span class="total-price">¥{{ order.payAmount.toFixed(2) }}</span>
        </div>
      </div>
      
      <div class="actions-section">
        <button v-if="isPending" class="action-btn primary" @click="router.push(`/pay/${order.id}`)">去支付</button>
        <button v-if="isPending" class="action-btn danger" @click="handleCancel">取消订单</button>
        <button v-if="canRefund" class="action-btn secondary" @click="goToRefund">申请退款</button>
        <button v-if="isDelivering" class="action-btn primary" @click="handleConfirm">确认收货</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.order-detail-page {
  padding: 24px 0;
  background: #F8F9FA;
  min-height: calc(100vh - 180px);
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 16px;
}

.back-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  cursor: pointer;
  color: #595959;
  font-size: 14px;
  margin-bottom: 16px;
  transition: all 0.2s;
}

.back-btn:hover {
  border-color: #E67E22;
  color: #E67E22;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0 0 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #E67E22;
}

.order-info, .address-section, .items-section, .amount-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #7f8c8d;
  font-size: 14px;
}

.info-row .value {
  color: #2C3E50;
  font-size: 14px;
}

.info-row .status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
}

.info-row .status.pending {
  background: #fff3e0;
  color: #e67e22;
}

.info-row .status.delivering {
  background: #e3f2fd;
  color: #1976d2;
}

.info-row .status.completed {
  background: #e8f5e9;
  color: #2e7d32;
}

.info-row .status.cancelled {
  background: #fafafa;
  color: #95a5a6;
}

.address-content {
  padding: 12px 0;
}

.address-name {
  font-size: 15px;
  color: #2C3E50;
  font-weight: 500;
  margin-bottom: 8px;
}

.address-detail {
  font-size: 14px;
  color: #595959;
  line-height: 1.5;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  color: #2C3E50;
  margin: 0 0 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-spec {
  font-size: 13px;
  color: #95a5a6;
  margin: 0 0 8px;
}

.item-price {
  font-size: 14px;
  color: #e74c3c;
  font-weight: 500;
}

.item-quantity {
  width: 120px;
  display: flex;
  justify-content: center;
}

.quantity-control {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 4px;
  overflow: hidden;
}

.qty-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 16px;
  color: #595959;
  transition: all 0.2s;
}

.qty-btn:hover:not(:disabled) {
  background: #fff;
  color: #E67E22;
}

.qty-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.qty-input {
  width: 45px;
  height: 32px;
  border: none;
  background: #fff;
  text-align: center;
  font-size: 14px;
  color: #2C3E50;
  font-weight: 500;
  outline: none;
}

.qty-input:disabled {
  background: #f0f0f0;
  cursor: not-allowed;
}

.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.qty-input[type=number] {
  -moz-appearance: textfield;
}

.item-total {
  width: 80px;
  text-align: right;
  font-size: 15px;
  font-weight: bold;
  color: #e74c3c;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #595959;
  margin-bottom: 10px;
}

.amount-row:last-child {
  margin-bottom: 0;
}

.amount-row .discount {
  color: #27ae60;
}

.amount-row.total {
  font-size: 16px;
  font-weight: 600;
  color: #2C3E50;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e0e0e0;
}

.total-price {
  font-size: 22px;
  font-weight: bold;
  color: #e74c3c;
}

.actions-section {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.action-btn {
  padding: 12px 28px;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.primary {
  background: #E67E22;
  color: #fff;
}

.action-btn.primary:hover {
  background: #d35400;
}

.action-btn.danger {
  background: #fff;
  color: #e74c3c;
  border: 1px solid #e74c3c;
}

.action-btn.danger:hover {
  background: #fef5f5;
}

.action-btn.secondary {
  background: #fff;
  color: #1976d2;
  border: 1px solid #1976d2;
}

.action-btn.secondary:hover {
  background: #f0f7ff;
}

@media (max-width: 600px) {
  .order-item {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .item-info {
    flex: 1;
    min-width: calc(100% - 96px);
  }
  
  .item-quantity {
    margin-left: 96px;
  }
  
  .item-total {
    width: auto;
    margin-left: auto;
  }
  
  .actions-section {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    padding: 14px;
  }
}
</style>