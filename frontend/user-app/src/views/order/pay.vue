<script setup lang="ts">import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getOrderDetail, payOrder } from '@/api';
import { ElMessage } from 'element-plus';
import type { OrderDetail } from '@/types';
const route = useRoute();
const router = useRouter();
const order = ref<OrderDetail | null>(null);
const selectedPayMethod = ref('wechat');
const payMethods = [
 { value: 'wechat', label: '微信支付', icon: '💳' },
 { value: 'alipay', label: '支付宝', icon: '📱' },
 { value: 'bank', label: '银行卡支付', icon: '🏦' }
];
const loading = ref(false);
const loadOrder = async () => {
 const id = parseInt(route.params.orderId as string);
 if (isNaN(id)) {
 ElMessage.error('订单ID无效');
 return;
 }
 try {
 const data = await getOrderDetail(id);
 order.value = data;
 }
 catch (error) {
 console.error('Failed to load order:', error);
 }
};
const handlePay = async () => {
 if (!order.value)
 return;
 loading.value = true;
 try {
 await payOrder(order.value.id, { paymentMethod: selectedPayMethod.value });
 ElMessage.success('支付成功');
 setTimeout(() => {
 router.push(`/order/${order.value?.id}`);
 }, 2000);
 }
 catch (error: any) {
 ElMessage.error('支付失败: ' + error.message);
 }
 finally {
 loading.value = false;
 }
};
const goBack = () => {
 router.push(`/order/${route.params.orderId}`);
};
onMounted(() => {
 loadOrder();
});
</script>

<template>
  <div class="pay-page" v-if="order">
    <div class="container">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h2 class="page-title">确认支付</h2>
      
      <div class="order-summary">
        <h3 class="section-title">订单信息</h3>
        <div class="order-no">订单号: {{ order.orderNo }}</div>
        <div class="order-items">
          <div v-for="item in order.items.slice(0, 2)" :key="item.id" class="item">
            <img :src="item.pic || item.productImage" :alt="item.productName" />
            <span>{{ item.productName }}</span>
          </div>
          <span v-if="order.items.length > 2">等{{ order.items.length }}件商品</span>
        </div>
      </div>
      
      <div class="pay-method-section">
        <h3 class="section-title">选择支付方式</h3>
        <div class="pay-methods">
          <div
            v-for="method in payMethods"
            :key="method.value"
            :class="{ selected: selectedPayMethod === method.value }"
            @click="selectedPayMethod = method.value"
          >
            <span class="icon">{{ method.icon }}</span>
            <span class="label">{{ method.label }}</span>
            <span v-if="selectedPayMethod === method.value" class="check">✓</span>
          </div>
        </div>
      </div>
      
      <div class="amount-section">
        <div class="amount-row">
          <span>订单金额:</span>
          <span>¥{{ order.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-row">
          <span>运费:</span>
          <span>¥{{ (order.freightAmount || 0).toFixed(2) }}</span>
        </div>
        <div class="amount-row total">
          <span>实付金额:</span>
          <span class="total-price">¥{{ order.payAmount.toFixed(2) }}</span>
        </div>
      </div>
      
      <button 
        class="pay-btn" 
        @click="handlePay"
        :disabled="loading"
      >
        <span v-if="loading">支付中...</span>
        <span v-else>立即支付 ¥{{ order.payAmount.toFixed(2) }}</span>
      </button>
      
      <p class="tips">支付安全由系统保障，请放心支付</p>
    </div>
  </div>
</template>

<style scoped>
.pay-page {
  padding: 24px 0;
  background: #F8F9FA;
  min-height: calc(100vh - 180px);
}

.container {
  max-width: 500px;
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

.order-summary, .pay-method-section, .amount-section {
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

.order-no {
  font-size: 14px;
  color: #595959;
  margin-bottom: 12px;
}

.order-items {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.order-items .item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-items .item img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.order-items .item span {
  font-size: 14px;
  color: #2C3E50;
}

.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pay-methods > div {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-methods > div:hover {
  border-color: #E67E22;
}

.pay-methods > div.selected {
  border-color: #E67E22;
  background: #fffaf5;
}

.pay-methods .icon {
  font-size: 24px;
}

.pay-methods .label {
  flex: 1;
  font-size: 16px;
  color: #2C3E50;
}

.pay-methods .check {
  color: #E67E22;
  font-size: 18px;
  font-weight: bold;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #595959;
  margin-bottom: 10px;
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
  font-size: 24px;
  font-weight: bold;
  color: #e74c3c;
}

.pay-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #E67E22 0%, #D35400 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 8px;
}

.pay-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(230, 126, 34, 0.3);
}

.pay-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.tips {
  text-align: center;
  font-size: 12px;
  color: #95a5a6;
  margin-top: 16px;
}
</style>