<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRefunds, approveRefund, rejectRefund } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Refund {
  id: number
  orderId: number
  orderNo: string
  refundNo: string
  reason: string
  description?: string
  amount: number
  status: number
  statusText: string
  adminRemark?: string
  createTime: string
  processTime?: string
}

const loading = ref(false)
const refunds = ref<Refund[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const statusFilter = ref<number | undefined>(undefined)
const keyword = ref('')
const dialogVisible = ref(false)
const dialogType = ref<'approve' | 'reject'>('approve')
const currentRefund = ref<Refund | null>(null)
const adminRemark = ref('')

const loadRefunds = async () => {
  try {
    loading.value = true
    const res: any = await getRefunds({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value,
      keyword: keyword.value || undefined
    })
    refunds.value = res?.data?.records || res?.data || res || []
    total.value = res?.data?.total || 0
  } catch (error) {
    console.error('Failed to load refunds:', error)
    ElMessage.error('加载退款申请失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  page.value = 1
  loadRefunds()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadRefunds()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  page.value = 1
  loadRefunds()
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

const handleApprove = (refund: Refund) => {
  currentRefund.value = refund
  dialogType.value = 'approve'
  adminRemark.value = ''
  dialogVisible.value = true
}

const handleReject = (refund: Refund) => {
  currentRefund.value = refund
  dialogType.value = 'reject'
  adminRemark.value = ''
  dialogVisible.value = true
}

const confirmAction = async () => {
  if (!currentRefund.value) return

  try {
    if (dialogType.value === 'approve') {
      await approveRefund(currentRefund.value.id, adminRemark.value)
      ElMessage.success('退款申请已通过')
    } else {
      if (!adminRemark.value.trim()) {
        ElMessage.warning('请填写拒绝原因')
        return
      }
      await rejectRefund(currentRefund.value.id, adminRemark.value)
      ElMessage.success('退款申请已拒绝')
    }
    dialogVisible.value = false
    loadRefunds()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.substring(0, 16).replace('T', ' ')
}

const formatPrice = (price: number) => {
  return price ? `¥${price.toFixed(2)}` : '-'
}

onMounted(() => {
  loadRefunds()
})
</script>

<template>
  <div class="refund-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">退款管理</h2>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索订单号/退款号..."
            @keyup.enter="handleFilter"
          />
          <button class="search-btn" @click="handleFilter">搜索</button>
        </div>
        <select v-model="statusFilter" class="filter-select" @change="handleFilter">
          <option :value="undefined">全部状态</option>
          <option :value="0">待处理</option>
          <option :value="1">已通过</option>
          <option :value="2">已拒绝</option>
        </select>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th class="col-id">退款编号</th>
            <th class="col-order">订单号</th>
            <th class="col-amount">退款金额</th>
            <th class="col-reason">退款原因</th>
            <th class="col-status">状态</th>
            <th class="col-time">申请时间</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="refunds.length === 0 && !loading">
            <td colspan="7" class="empty-cell">
              <div class="empty-state">
                <span class="empty-icon">💰</span>
                <p>暂无退款申请</p>
              </div>
            </td>
          </tr>
          <tr v-for="refund in refunds" :key="refund.id">
            <td class="col-id">
              <span class="refund-no">{{ refund.refundNo }}</span>
            </td>
            <td class="col-order">
              <span class="order-no">{{ refund.orderNo }}</span>
            </td>
            <td class="col-amount">
              <span class="amount">{{ formatPrice(refund.amount) }}</span>
            </td>
            <td class="col-reason">
              <div class="reason-content">
                <span class="reason">{{ refund.reason }}</span>
                <span v-if="refund.description" class="description">{{ refund.description }}</span>
              </div>
            </td>
            <td class="col-status">
              <span class="status-badge" :class="getStatusClass(refund.status)">
                {{ getStatusText(refund.status) }}
              </span>
            </td>
            <td class="col-time">
              <span class="time">{{ formatDate(refund.createTime) }}</span>
            </td>
            <td class="col-actions">
              <template v-if="refund.status === 0">
                <button class="action-btn approve" @click="handleApprove(refund)">
                  通过
                </button>
                <button class="action-btn reject" @click="handleReject(refund)">
                  拒绝
                </button>
              </template>
              <span v-else class="no-action">-</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'approve' ? '通过退款申请' : '拒绝退款申请'" width="450px">
      <div class="dialog-content">
        <div class="refund-info">
          <div class="info-row">
            <span class="label">退款编号：</span>
            <span class="value">{{ currentRefund?.refundNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">订单号：</span>
            <span class="value">{{ currentRefund?.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">退款金额：</span>
            <span class="value amount">{{ formatPrice(currentRefund?.amount || 0) }}</span>
          </div>
          <div class="info-row">
            <span class="label">退款原因：</span>
            <span class="value">{{ currentRefund?.reason }}</span>
          </div>
        </div>
        <div class="remark-form" v-if="dialogType === 'reject'">
          <label>拒绝原因 <span class="required">*</span></label>
          <textarea
            v-model="adminRemark"
            placeholder="请填写拒绝原因"
            rows="3"
          ></textarea>
        </div>
        <div class="remark-form" v-else>
          <label>备注（选填）</label>
          <textarea
            v-model="adminRemark"
            placeholder="请填写备注信息"
            rows="3"
          ></textarea>
        </div>
      </div>
      <template #footer>
        <button class="dialog-btn cancel" @click="dialogVisible = false">取消</button>
        <button class="dialog-btn confirm" :class="dialogType" @click="confirmAction">
          {{ dialogType === 'approve' ? '确认通过' : '确认拒绝' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.refund-list {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 14px 16px;
  background: #fafafa;
  font-weight: 500;
  font-size: 14px;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}

.data-table tbody tr {
  transition: background 0.2s;
}

.data-table tbody tr:hover {
  background: #fafafa;
}

.col-id { width: 140px; }
.col-order { width: 120px; }
.col-amount { width: 100px; }
.col-reason { min-width: 150px; }
.col-status { width: 90px; }
.col-time { width: 140px; }
.col-actions { width: 140px; }

.refund-no, .order-no {
  font-size: 13px;
  color: #667eea;
}

.amount {
  font-size: 14px;
  font-weight: 600;
  color: #f5576c;
}

.reason-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reason {
  font-size: 14px;
  color: #333;
}

.description {
  font-size: 12px;
  color: #999;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
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

.time {
  font-size: 13px;
  color: #666;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  margin-right: 6px;
}

.action-btn.approve {
  background: #f6ffed;
  color: #52c41a;
}

.action-btn.approve:hover {
  background: #d9f7be;
}

.action-btn.reject {
  background: #fff2f0;
  color: #f5576c;
}

.action-btn.reject:hover {
  background: #ffccc7;
}

.no-action {
  color: #999;
  font-size: 14px;
}

.empty-cell {
  padding: 60px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.5;
}

.empty-state p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.dialog-content {
  padding: 10px 0;
}

.refund-info {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  width: 80px;
  font-size: 14px;
  color: #999;
  flex-shrink: 0;
}

.info-row .value {
  font-size: 14px;
  color: #333;
}

.info-row .amount {
  color: #f5576c;
  font-weight: 600;
}

.remark-form {
  margin-top: 16px;
}

.remark-form label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.remark-form .required {
  color: #f56c6c;
}

.remark-form textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
}

.dialog-btn {
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.dialog-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.dialog-btn.cancel:hover {
  background: #e5e5e5;
}

.dialog-btn.confirm {
  background: #667eea;
  color: #fff;
}

.dialog-btn.confirm:hover {
  background: #5a71e0;
}

.dialog-btn.confirm.reject {
  background: #f5576c;
}

.dialog-btn.confirm.reject:hover {
  background: #e64a5c;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}

.search-box input {
  padding: 8px 12px;
  border: none;
  outline: none;
  font-size: 14px;
  width: 180px;
}

.search-box .search-btn {
  padding: 8px 16px;
  background: #E67E22;
  color: #fff;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-box .search-btn:hover {
  background: #d35400;
}
</style>