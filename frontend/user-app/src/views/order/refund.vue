<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createRefund, getMyRefunds, type Refund } from '@/api/refund'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const orderId = ref<number>(0)
const reason = ref('')
const description = ref('')
const loading = ref(false)
const listLoading = ref(false)
const refunds = ref<Refund[]>([])

const reasonOptions = [
  { value: '商品损坏', label: '商品损坏' },
  { value: '商品与描述不符', label: '商品与描述不符' },
  { value: '误购/不想买了', label: '误购/不想买了' },
  { value: '收到商品有质量问题', label: '收到商品有质量问题' },
  { value: '物流问题', label: '物流问题' },
  { value: '其他', label: '其他' }
]

onMounted(() => {
  if (route.params.orderId) {
    orderId.value = parseInt(route.params.orderId as string)
  }
  loadMyRefunds()
})

const loadMyRefunds = async () => {
  try {
    listLoading.value = true
    console.log('开始加载退款记录...')
    const data = await getMyRefunds()
    console.log('退款记录返回数据:', data)
    refunds.value = data || []
    console.log('当前退款记录:', refunds.value)
  } catch (error) {
    console.error('加载退款记录失败:', error)
    ElMessage.error('加载退款记录失败')
  } finally {
    listLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!orderId.value) {
    ElMessage.warning('请选择要退款的订单')
    return
  }
  if (!reason.value) {
    ElMessage.warning('请选择退款原因')
    return
  }

  try {
    loading.value = true
    await createRefund({
      orderId: orderId.value,
      reason: reason.value,
      description: description.value
    })
    ElMessage.success('退款申请已提交')
    router.push('/orders')
  } catch (error: any) {
    ElMessage.error(error?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.substring(0, 16).replace('T', ' ')
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待处理',
    1: '已通过',
    2: '已拒绝'
  }
  return map[status] || `未知(${status})`
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'status-pending',
    1: 'status-approved',
    2: 'status-rejected'
  }
  return map[status] || ''
}
</script>

<template>
  <div class="refund-page">
    <div class="container">
      <h2 class="page-title">申请退款</h2>

      <div class="refund-form" v-if="orderId && orderId > 0">
        <div class="form-item">
          <label>订单ID</label>
          <div class="order-id">{{ orderId }}</div>
        </div>

        <div class="form-item">
          <label>退款原因 <span class="required">*</span></label>
          <select v-model="reason" class="reason-select">
            <option value="">请选择退款原因</option>
            <option v-for="opt in reasonOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>

        <div class="form-item">
          <label>详细说明</label>
          <textarea
            v-model="description"
            class="description-textarea"
            placeholder="请详细描述您的问题（选填）"
            rows="4"
          ></textarea>
        </div>

        <div class="form-actions">
          <button class="btn-cancel" @click="router.back()">取消</button>
          <button class="btn-submit" @click="handleSubmit" :disabled="loading">
            {{ loading ? '提交中...' : '提交申请' }}
          </button>
        </div>
      </div>

      <div v-if="listLoading" class="loading-state">
        <p>加载中...</p>
      </div>

      <div class="refund-list" v-else-if="refunds.length > 0">
        <h3 class="section-title">我的退款记录</h3>
        <div class="refund-item" v-for="refund in refunds" :key="refund.id">
          <div class="refund-header">
            <span class="refund-no">{{ refund.refundNo }}</span>
            <span class="refund-status" :class="getStatusClass(refund.status)">
              {{ getStatusText(refund.status) }}
            </span>
          </div>
          <div class="refund-info">
            <div class="info-row">
              <span class="label">订单号：</span>
              <span class="value">{{ refund.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">退款金额：</span>
              <span class="value amount">¥{{ refund.amount?.toFixed(2) }}</span>
            </div>
            <div class="info-row">
              <span class="label">退款原因：</span>
              <span class="value">{{ refund.reason }}</span>
            </div>
            <div class="info-row" v-if="refund.description">
              <span class="label">详细说明：</span>
              <span class="value">{{ refund.description }}</span>
            </div>
            <div class="info-row" v-if="refund.adminRemark">
              <span class="label">处理备注：</span>
              <span class="value">{{ refund.adminRemark }}</span>
            </div>
            <div class="info-row">
              <span class="label">申请时间：</span>
              <span class="value">{{ formatDate(refund.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-else-if="refunds.length === 0 && !listLoading">
        <p>暂无退款申请</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.refund-page {
  padding: 20px 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}

.refund-form {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.required {
  color: #f56c6c;
}

.order-id {
  padding: 10px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
}

.reason-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.description-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn-cancel {
  padding: 10px 24px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-submit {
  padding: 10px 24px;
  background: #E67E22;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-submit:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.refund-list {
  margin-top: 30px;
}

.refund-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.refund-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.refund-no {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.refund-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.status-pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background: #f5f5f5;
  color: #999;
}

.refund-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  font-size: 14px;
}

.info-row .label {
  width: 80px;
  color: #999;
  flex-shrink: 0;
}

.info-row .value {
  color: #333;
}

.info-row .amount {
  color: #f56c6c;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  color: #999;
}
</style>