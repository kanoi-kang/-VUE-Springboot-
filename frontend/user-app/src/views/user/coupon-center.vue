<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAvailableCoupons, claimCoupon } from '@/api'
import { ElMessage } from 'element-plus'
import type { Coupon } from '@/types'
import { formatDate } from '@/utils/format'

const router = useRouter()
const coupons = ref<Coupon[]>([])
const loading = ref(false)
const claimingIds = ref<Set<number>>(new Set())

const loadCoupons = async () => {
  loading.value = true
  try {
    const data = await getAvailableCoupons()
    coupons.value = data || []
  } catch (error) {
    console.error('Failed to load coupons:', error)
  } finally {
    loading.value = false
  }
}

const handleClaimCoupon = async (couponId: number) => {
  if (claimingIds.value.has(couponId)) return
  
  claimingIds.value.add(couponId)
  try {
    await claimCoupon(couponId)
    ElMessage.success('领取成功')
    await loadCoupons()
  } catch (error: any) {
    const msg = error?.response?.data?.message || error?.message || '领取失败'
    ElMessage.error(msg)
  } finally {
    claimingIds.value.delete(couponId)
  }
}

const goToMyCoupons = () => {
  router.push('/coupons')
}

onMounted(() => {
  loadCoupons()
})
</script>

<template>
  <div class="coupon-center-page">
    <div class="container">
      <div class="page-header">
        <h2 class="page-title">优惠券中心</h2>
        <button class="my-coupons-btn" @click="goToMyCoupons">我的优惠券</button>
      </div>
      
      <div v-if="loading" class="loading-state">加载中...</div>
      
      <div v-else-if="coupons.length === 0" class="empty-state">
        <p>暂无可领取的优惠券</p>
      </div>
      
      <div v-else class="coupons-grid">
        <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card">
          <div class="coupon-left">
            <div class="coupon-value">¥{{ coupon.discountValue }}</div>
            <div class="coupon-condition">满{{ coupon.minAmount }}可用</div>
          </div>
          <div class="coupon-right">
            <h4 class="coupon-name">{{ coupon.name }}</h4>
            <p v-if="coupon.description" class="coupon-desc">{{ coupon.description }}</p>
            <p class="coupon-time">有效期至：{{ formatDate(coupon.endTime, 'YYYY-MM-DD') }}</p>
            <button 
              class="claim-btn" 
              :disabled="claimingIds.has(coupon.id)"
              @click="handleClaimCoupon(coupon.id)"
            >
              {{ claimingIds.has(coupon.id) ? '领取中...' : '立即领取' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.coupon-center-page {
  padding: 20px 0;
  background: #F8F9FA;
  min-height: calc(100vh - 180px);
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0;
}

.my-coupons-btn {
  padding: 8px 20px;
  background: #fff;
  border: 1px solid #E67E22;
  border-radius: 4px;
  color: #E67E22;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.my-coupons-btn:hover {
  background: #E67E22;
  color: #fff;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #95a5a6;
  font-size: 16px;
}

.coupons-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.coupon-left {
  width: 140px;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E67E22 0%, #d35400 100%);
  color: #fff;
  position: relative;
}

.coupon-left::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background-image: radial-gradient(circle at 50% 50%, transparent 50%, #fff 50%);
  background-size: 12px 12px;
  background-position: 0 0, 0 12px;
  background-repeat: repeat-y;
}

.coupon-value {
  font-size: 36px;
  font-weight: bold;
  line-height: 1;
}

.coupon-condition {
  font-size: 14px;
  margin-top: 8px;
  opacity: 0.9;
}

.coupon-right {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0 0 8px;
}

.coupon-desc {
  font-size: 13px;
  color: #7f8c8d;
  margin: 0 0 8px;
}

.coupon-time {
  font-size: 12px;
  color: #95a5a6;
  margin: 0 0 12px;
}

.claim-btn {
  margin-top: auto;
  padding: 10px 24px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.claim-btn:hover:not(:disabled) {
  background: #d35400;
  transform: translateY(-1px);
}

.claim-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .coupons-grid {
    grid-template-columns: 1fr;
  }
  
  .coupon-left {
    width: 120px;
    padding: 20px 12px;
  }
  
  .coupon-value {
    font-size: 28px;
  }
}
</style>
