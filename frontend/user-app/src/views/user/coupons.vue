<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMyCoupons } from '@/api'
import type { Coupon } from '@/types'
import { formatDate } from '@/utils/format'

const router = useRouter()

const coupons = ref<Coupon[]>([])

const loadCoupons = async () => {
  try {
    const data = await getMyCoupons()
    coupons.value = data
  } catch (error) {
    console.error('Failed to load coupons:', error)
  }
}

const goToCouponCenter = () => {
  router.push('/coupon-center')
}

onMounted(() => {
  loadCoupons()
})
</script>

<template>
  <div class="coupons-page">
    <div class="container">
      <h2 class="page-title">我的优惠券</h2>
      
      <div class="page-actions">
        <button class="center-btn" @click="goToCouponCenter">去领券</button>
      </div>
      
      <div v-if="coupons.length === 0" class="empty-state">
        <p>暂无优惠券</p>
      </div>
      
      <div v-else class="coupons-list">
        <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card" :class="{ used: coupon.status === 1 }">
          <div class="coupon-left">
            <div>¥{{ coupon.discountValue || coupon.amount }}</div>
            <div>满{{ coupon.minAmount }}可用</div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ coupon.name }}</div>
            <div class="coupon-time">有效期至：{{ formatDate(coupon.endTime, 'YYYY-MM-DD') }}</div>
            <div v-if="coupon.status === 1" class="coupon-used-tag">已使用</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.coupons-page {
  padding: 20px 0;
  background: #F8F9FA;
  min-height: calc(100vh - 180px);
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0 0 16px;
}

.page-actions {
  margin-bottom: 20px;
}

.center-btn {
  padding: 10px 24px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.center-btn:hover {
  background: #d35400;
  transform: translateY(-1px);
}

.empty-state {
  text-align: center;
  padding: 40px;
}

.coupons-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.coupon-card {
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  overflow: hidden;
}

.coupon-left {
  width: 120px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #fff;
  border-right: 2px dashed rgba(255, 255, 255, 0.3);
}

.coupon-left div:first-child {
  font-size: 32px;
  font-weight: bold;
}

.coupon-right {
  flex: 1;
  padding: 20px;
  background: #fff;
  position: relative;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 8px;
}

.coupon-time {
  font-size: 13px;
  color: #7f8c8d;
}

.coupon-used-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  background: #ecf0f1;
  color: #7f8c8d;
  font-size: 12px;
  border-radius: 4px;
}

.coupon-card.used {
  opacity: 0.6;
}

.coupon-card.used .coupon-left {
  background: linear-gradient(135deg, #bdc3c7 0%, #95a5a6 100%);
}
</style>
