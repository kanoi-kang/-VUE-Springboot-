<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { getAddresses, createOrder, getMyCoupons, calculateDiscount } from '@/api'
import { ElMessage } from 'element-plus'
import type { Address } from '@/types'

const router = useRouter()
const cartStore = useCartStore()

const addresses = ref<Address[]>([])
const selectedAddressId = ref<number | null>(null)
const coupons = ref<any[]>([])
const selectedCouponId = ref<number | null>(null)
const showCouponPicker = ref(false)
const discountAmount = ref(0)

const selectedAddress = computed(() => {
  return addresses.value.find(a => a.id === selectedAddressId.value)
})

const selectedCoupon = computed(() => {
  if (!selectedCouponId.value) return null
  return coupons.value.find(c => c.couponId === selectedCouponId.value)
})

const totalAmount = computed(() => {
  return cartStore.totalPrice
})

const finalPayAmount = computed(() => {
  return Math.max(0, totalAmount.value - discountAmount.value)
})

const loadAddresses = async () => {
  try {
    const data = await getAddresses()
    addresses.value = data
    const defaultAddress = data.find((a: Address) => a.isDefault)
    if (defaultAddress) {
      selectedAddressId.value = defaultAddress.id
    }
  } catch (error) {
    console.error('Failed to load addresses:', error)
  }
}

const loadCoupons = async () => {
  try {
    const data = await getMyCoupons()
    // 只显示可用的优惠券（status=0）
    coupons.value = (data || []).filter((c: any) => c.status === 0)
  } catch (error) {
    console.error('Failed to load coupons:', error)
  }
}

const handleSelectCoupon = async (couponId: number | null) => {
  if (couponId === null) {
    selectedCouponId.value = null
    discountAmount.value = 0
    return
  }
  
  try {
    const data = await calculateDiscount(couponId, totalAmount.value)
    selectedCouponId.value = couponId
    discountAmount.value = data?.discountAmount || 0
    showCouponPicker.value = false
    ElMessage.success('优惠券已选择')
  } catch (error: any) {
    const errorMsg = error?.response?.data?.message || error?.message || '优惠券使用失败'
    ElMessage.error(errorMsg)
    selectedCouponId.value = null
    discountAmount.value = 0
  }
}

const handleSubmitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.error('请选择收货地址')
    return
  }
  
  try {
    const items = cartStore.items.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))
    
    const data = await createOrder({
      addressId: selectedAddressId.value,
      couponId: selectedCouponId.value,
      items
    })
    
    ElMessage.success('订单创建成功')
    await cartStore.clearAll()
    router.push(`/order/${data.id}`)
  } catch (error: any) {
    const errorMsg = error?.response?.data?.message || error?.message || '订单创建失败'
    if (errorMsg.includes('库存不足') || errorMsg.includes('Insufficient stock')) {
      ElMessage.error(errorMsg)
    } else {
      ElMessage.error(errorMsg)
    }
  }
}

const getProductImage = (item: any) => {
  return item.pic || item.productImage || item.image || ''
}

onMounted(() => {
  loadAddresses()
  loadCoupons()
})
</script>

<template>
  <div class="checkout-page">
    <div class="container">
      <button class="back-btn" @click="router.push('/cart')">← 返回购物车</button>
      <h2 class="page-title">确认订单</h2>
      
      <div class="address-section">
        <div class="section-header">
          <h3 class="section-title">收货地址</h3>
          <button class="add-address-btn" @click="router.push('/address-edit')">+ 添加地址</button>
        </div>
        
        <div v-if="addresses.length === 0" class="empty-address">
          <div class="empty-icon">📦</div>
          <p>暂无收货地址</p>
          <button class="add-btn" @click="router.push('/address-edit')">立即添加</button>
        </div>
        
        <div v-else class="address-list">
          <div
            v-for="address in addresses"
            :key="address.id"
            :class="['address-card', { selected: selectedAddressId === address.id }]"
            @click="selectedAddressId = address.id"
          >
            <div class="address-header">
              <div class="address-name">{{ address.consignee || address.name }}</div>
              <div class="address-phone">{{ address.phone }}</div>
              <div class="address-badges">
                <span v-if="address.isDefault" class="default-tag">默认</span>
                <span v-if="selectedAddressId === address.id" class="check-mark">✓</span>
              </div>
            </div>
            <div class="address-detail">{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress || address.detail }}</div>
          </div>
        </div>
      </div>
      
      <div class="items-section">
        <h3 class="section-title">商品清单</h3>
        <div class="items-list">
          <div v-for="item in cartStore.items" :key="item.id" class="checkout-item">
            <div class="item-image">
              <img :src="getProductImage(item)" :alt="item.productName" />
            </div>
            <div class="item-info">
              <h4 class="item-name">{{ item.productName }}</h4>
              <p v-if="item.specName" class="item-spec">{{ item.specName }}</p>
              <div class="item-price-row">
                <span class="item-price">¥{{ item.price.toFixed(2) }}</span>
                <span class="item-quantity">× {{ item.quantity }}</span>
              </div>
            </div>
            <div class="item-total">
              <span>¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="coupon-section">
        <h3 class="section-title">优惠券</h3>
        <div class="coupon-selector" @click="showCouponPicker = true">
          <div v-if="selectedCoupon" class="selected-coupon">
            <div class="coupon-info">
              <span class="coupon-value">-¥{{ selectedCoupon.discount || 0 }}</span>
              <span class="coupon-name">{{ selectedCoupon.name }}</span>
            </div>
            <span class="change-btn">更换</span>
          </div>
          <div v-else class="no-coupon">
            <span class="placeholder">{{ coupons.length > 0 ? `有${coupons.length}张可用优惠券` : '暂无可用优惠券' }}</span>
            <span class="arrow">></span>
          </div>
        </div>
      </div>
      
      <div class="summary-section">
        <div class="summary-row">
          <span>商品总价:</span>
          <span>¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div v-if="discountAmount > 0" class="summary-row discount">
          <span>优惠券:</span>
          <span>-¥{{ discountAmount.toFixed(2) }}</span>
        </div>
        <div class="summary-row">
          <span>运费:</span>
          <span>¥0.00</span>
        </div>
        <div class="summary-row total">
          <span>实付金额:</span>
          <span class="total-price">¥{{ finalPayAmount.toFixed(2) }}</span>
        </div>
        <button class="submit-btn" @click="handleSubmitOrder">提交订单</button>
      </div>
      
      <div v-if="showCouponPicker" class="coupon-picker-overlay" @click="showCouponPicker = false">
        <div class="coupon-picker" @click.stop>
          <div class="picker-header">
            <h3>选择优惠券</h3>
            <button class="close-btn" @click="showCouponPicker = false">×</button>
          </div>
          <div class="picker-body">
            <div 
              class="coupon-option not-use"
              :class="{ selected: selectedCouponId === null }"
              @click="handleSelectCoupon(null)"
            >
              <span>不使用优惠券</span>
            </div>
            <div 
              v-for="coupon in coupons" 
              :key="coupon.couponId"
              class="coupon-option"
              :class="{ selected: selectedCouponId === coupon.couponId }"
              @click="handleSelectCoupon(coupon.couponId)"
            >
              <div class="option-left">
                <span class="option-value">-¥{{ coupon.discount || 0 }}</span>
                <span class="option-condition">满{{ coupon.minAmount || 0 }}可用</span>
              </div>
              <div class="option-right">
                <div class="option-name">{{ coupon.name }}</div>
                <div v-if="coupon.description" class="option-desc">{{ coupon.description }}</div>
              </div>
              <div v-if="selectedCouponId === coupon.couponId" class="check-mark">✓</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.checkout-page {
  padding: 24px 0;
  background: #F8F9FA;
  min-height: calc(100vh - 180px);
}

.container {
  max-width: 600px;
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0;
}

.add-address-btn {
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #E67E22;
  border-radius: 4px;
  color: #E67E22;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.add-address-btn:hover {
  background: #fffaf5;
}

.address-section, .items-section, .coupon-section, .summary-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.empty-address {
  text-align: center;
  padding: 32px 0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-address p {
  color: #95a5a6;
  margin: 0 0 16px;
}

.empty-address .add-btn {
  padding: 10px 24px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  position: relative;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.address-card:hover {
  border-color: #E67E22;
}

.address-card.selected {
  border-color: #E67E22;
  background: #fffaf5;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.address-name {
  font-size: 15px;
  font-weight: 600;
  color: #2C3E50;
}

.address-phone {
  font-size: 14px;
  color: #595959;
}

.address-badges {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.default-tag {
  padding: 2px 8px;
  background: #fff3e0;
  color: #e67e22;
  font-size: 12px;
  border-radius: 4px;
}

.check-mark {
  width: 20px;
  height: 20px;
  background: #E67E22;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.address-detail {
  font-size: 14px;
  color: #595959;
  line-height: 1.5;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.item-image {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #fff;
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

.item-price-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-price {
  font-size: 14px;
  color: #e74c3c;
  font-weight: 600;
}

.item-quantity {
  font-size: 13px;
  color: #95a5a6;
}

.item-total {
  font-size: 15px;
  font-weight: bold;
  color: #e74c3c;
  flex-shrink: 0;
}

.coupon-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  cursor: pointer;
  transition: all 0.2s;
}

.coupon-selector:hover {
  opacity: 0.8;
}

.selected-coupon {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.coupon-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.coupon-value {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.coupon-name {
  font-size: 14px;
  color: #2C3E50;
}

.change-btn {
  color: #E67E22;
  font-size: 14px;
}

.no-coupon {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.placeholder {
  color: #95a5a6;
  font-size: 14px;
}

.arrow {
  color: #95a5a6;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #595959;
  margin-bottom: 10px;
}

.summary-row.discount {
  color: #e74c3c;
}

.summary-row.total {
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

.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #E67E22 0%, #D35400 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 16px;
  transition: all 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(230, 126, 34, 0.3);
}

.coupon-picker-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
}

.coupon-picker {
  background: #fff;
  width: 100%;
  max-width: 600px;
  border-radius: 16px 16px 0 0;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.picker-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2C3E50;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f0f0f0;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #95a5a6;
}

.picker-body {
  padding: 20px;
  overflow-y: auto;
}

.coupon-option {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.coupon-option:hover {
  border-color: #E67E22;
}

.coupon-option.selected {
  border-color: #E67E22;
  background: #fffaf5;
}

.coupon-option.not-use {
  justify-content: center;
}

.option-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-right: 16px;
  border-right: 2px dashed #e0e0e0;
  min-width: 100px;
}

.option-value {
  font-size: 24px;
  font-weight: bold;
  color: #e74c3c;
}

.option-condition {
  font-size: 12px;
  color: #95a5a6;
  margin-top: 4px;
}

.option-right {
  flex: 1;
  padding-left: 16px;
}

.option-name {
  font-size: 15px;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 12px;
  color: #95a5a6;
}

.coupon-option .check-mark {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
}
</style>