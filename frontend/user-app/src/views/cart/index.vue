<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(true)

const getProductImage = (item: any): string => {
  if (item.pic) return item.pic
  if (item.productImage) return item.productImage
  if (item.images && Array.isArray(item.images)) {
    if (typeof item.images[0] === 'string') {
      return item.images[0]
    }
    return item.images[0]?.url || '/placeholder.png'
  }
  return '/placeholder.png'
}

const loadCart = async () => {
  try {
    await cartStore.loadCart()
  } catch (error) {
    console.error('Failed to load cart:', error)
  } finally {
    loading.value = false
  }
}

const updateQuantity = async (id: number, quantity: number) => {
  if (quantity < 1) {
    ElMessage.warning('数量不能小于1')
    return
  }
  try {
    await cartStore.updateItem(id, quantity)
    ElMessage.success('更新成功')
  } catch (error: any) {
    const errorMsg = error?.message || '更新失败'
    if (errorMsg.includes('库存不足') || errorMsg.includes('Insufficient stock')) {
      ElMessage.error(errorMsg)
    } else {
      ElMessage.error(errorMsg)
    }
  }
}

const onQtyInputChange = (id: number, event: Event) => {
  const input = event.target as HTMLInputElement
  const value = parseInt(input.value) || 1
  if (value >= 1) {
    updateQuantity(id, value)
  }
}

const onQtyInputBlur = (id: number, event: Event) => {
  const input = event.target as HTMLInputElement
  const value = parseInt(input.value) || 1
  if (value < 1) {
    input.value = '1'
    updateQuantity(id, 1)
  }
}

const removeItem = async (id: number) => {
  try {
    await cartStore.removeItem(id)
    ElMessage.success('已删除')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const goToCheckout = () => {
  if (cartStore.totalCount === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  router.push('/checkout')
}

const goShopping = () => {
  router.push('/')
}

onMounted(() => {
  loadCart()
})
</script>

<template>
  <div class="cart-page">
    <div class="container">
      <div class="page-header">
        <h2 class="page-title">购物车</h2>
        <span class="cart-count">{{ cartStore.totalCount }} 件商品</span>
      </div>

      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="cartStore.items.length === 0" class="empty-cart">
        <div class="empty-icon">🛒</div>
        <p class="empty-text">购物车空空如也</p>
        <button class="go-shopping-btn" @click="goShopping">去购物</button>
      </div>

      <div v-else class="cart-content">
        <div class="cart-main">
          <div class="cart-list">
            <div class="cart-header-row">
              <span class="col-image">商品</span>
              <span class="col-info">商品信息</span>
              <span class="col-price">单价</span>
              <span class="col-quantity">数量</span>
              <span class="col-total">小计</span>
              <span class="col-action">操作</span>
            </div>
            
            <div v-for="item in cartStore.items" :key="item.id" class="cart-item">
              <div class="col-image">
                <div class="item-image">
                  <img :src="getProductImage(item)" :alt="item.productName" />
                </div>
              </div>
              <div class="col-info">
                <h3 class="item-name">{{ item.productName }}</h3>
                <p v-if="item.specName" class="item-spec">{{ item.specName }}</p>
              </div>
              <div class="col-price">
                <span class="price">¥{{ item.price?.toFixed(2) || '0.00' }}</span>
              </div>
              <div class="col-quantity">
                <div class="quantity-control">
                  <button
                    class="qty-btn minus"
                    @click="updateQuantity(item.id, item.quantity - 1)"
                    :disabled="item.quantity <= 1"
                  >
                    <span>-</span>
                  </button>
                  <input
                    type="number"
                    class="qty-input"
                    :value="item.quantity"
                    @change="onQtyInputChange(item.id, $event)"
                    @blur="onQtyInputBlur(item.id, $event)"
                    min="1"
                  />
                  <button class="qty-btn plus" @click="updateQuantity(item.id, item.quantity + 1)">
                    <span>+</span>
                  </button>
                </div>
              </div>
              <div class="col-total">
                <span class="total">¥{{ ((item.price || 0) * item.quantity).toFixed(2) }}</span>
              </div>
              <div class="col-action">
                <button class="remove-btn" @click="removeItem(item.id)">删除</button>
              </div>
            </div>
          </div>
        </div>

        <div class="cart-sidebar">
          <div class="cart-summary">
            <h3 class="summary-title">结算信息</h3>
            <div class="summary-row">
              <span>商品数量:</span>
              <span>{{ cartStore.totalCount }} 件</span>
            </div>
            <div class="summary-row">
              <span>运费:</span>
              <span class="free">免运费</span>
            </div>
            <div class="summary-row total">
              <span>应付金额:</span>
              <span class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
            </div>
            <button class="checkout-btn" @click="goToCheckout">去结算</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  padding: 32px 0;
  background: #F8F9FA;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #E67E22;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0;
}

.cart-count {
  font-size: 14px;
  color: #7f8c8d;
  background: #f0f0f0;
  padding: 6px 16px;
  border-radius: 20px;
}

.loading {
  text-align: center;
  padding: 80px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f0f0f0;
  border-top-color: #E67E22;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-cart {
  text-align: center;
  padding: 100px 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 20px;
  color: #7f8c8d;
  margin: 0 0 28px;
}

.go-shopping-btn {
  padding: 14px 40px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.go-shopping-btn:hover {
  background: #d35400;
}

.cart-content {
  display: flex;
  gap: 24px;
}

.cart-main {
  flex: 1;
}

.cart-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.cart-list {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.cart-header-row {
  display: none;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}

.cart-item:hover {
  background: #fafafa;
}

.cart-item:last-child {
  border-bottom: none;
}

.col-image {
  flex-shrink: 0;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.col-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  color: #2C3E50;
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-spec {
  font-size: 13px;
  color: #95a5a6;
  margin: 0;
}

.col-price {
  width: 80px;
  text-align: center;
}

.price {
  font-size: 15px;
  font-weight: 500;
  color: #e74c3c;
}

.col-quantity {
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

.qty-input {
  width: 50px;
  height: 36px;
  border: none;
  background: #fff;
  text-align: center;
  font-size: 15px;
  color: #2C3E50;
  font-weight: 500;
  outline: none;
}

.qty-input::-webkit-outer-spin-button,
.qty-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.qty-input[type=number] {
  -moz-appearance: textfield;
}

.qty-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 18px;
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

.qty-value {
  width: 48px;
  text-align: center;
  font-size: 15px;
  color: #2C3E50;
  font-weight: 500;
}

.col-total {
  width: 100px;
  text-align: right;
}

.total {
  font-size: 16px;
  font-weight: bold;
  color: #e74c3c;
}

.col-action {
  width: 60px;
  text-align: center;
}

.remove-btn {
  padding: 6px 12px;
  background: #fff;
  color: #95a5a6;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.remove-btn:hover {
  color: #e74c3c;
  border-color: #e74c3c;
}

.cart-summary {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 24px;
}

.summary-title {
  font-size: 16px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #595959;
  margin-bottom: 12px;
}

.summary-row .free {
  color: #27ae60;
}

.summary-row.total {
  font-size: 16px;
  font-weight: 600;
  color: #2C3E50;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e0e0e0;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #e74c3c;
}

.checkout-btn {
  width: 100%;
  padding: 14px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 16px;
}

.checkout-btn:hover {
  background: #d35400;
}

@media (max-width: 768px) {
  .cart-page {
    padding: 16px 0;
  }

  .page-header {
    margin-bottom: 16px;
    padding-bottom: 12px;
  }

  .page-title {
    font-size: 20px;
  }

  .cart-count {
    font-size: 12px;
    padding: 4px 12px;
  }

  .cart-content {
    flex-direction: column;
    gap: 16px;
  }

  .cart-main {
    width: 100%;
  }

  .cart-sidebar {
    width: 100%;
  }

  .cart-item {
    flex-wrap: wrap;
    gap: 12px;
    padding: 16px;
  }

  .item-image {
    width: 80px;
    height: 80px;
  }

  .col-info {
    flex: 1;
    min-width: calc(100% - 96px);
  }

  .col-price {
    width: auto;
    order: 3;
    text-align: left;
    font-size: 14px;
  }

  .col-quantity {
    width: auto;
    order: 4;
  }

  .col-total {
    width: auto;
    order: 5;
    text-align: left;
    font-size: 14px;
  }

  .col-action {
    width: auto;
    order: 6;
    margin-left: auto;
  }

  .quantity-control {
    background: #fff;
    border: 1px solid #e0e0e0;
  }

  .qty-btn {
    width: 32px;
    height: 32px;
  }

  .cart-summary {
    padding: 20px;
  }

  .empty-cart {
    padding: 60px 20px;
  }

  .empty-icon {
    font-size: 64px;
  }

  .empty-text {
    font-size: 16px;
  }

  .go-shopping-btn {
    padding: 12px 32px;
    font-size: 14px;
  }
}
</style>