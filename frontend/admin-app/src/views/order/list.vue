<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, cancelOrder, deleteOrder, shipOrder } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const orders = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)
const shipDialogVisible = ref(false)
const shipOrderId = ref<number | null>(null)
const trackingNo = ref('')

const loadOrders = async () => {
  try {
    loading.value = true
    const res: any = await getOrders({
      page: page.value,
      size: pageSize.value,
      status: statusFilter.value,
      keyword: keyword.value || undefined
    })
    orders.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (error) {
    console.error('Failed to load orders:', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadOrders()
}

const handleStatusFilter = () => {
  page.value = 1
  loadOrders()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadOrders()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  page.value = 1
  loadOrders()
}

const handleView = (id: number) => {
  router.push(`/orders/${id}`)
}

const handleCancelOrder = async (order: any) => {
  if (order.status !== 'pending' && order.status !== 'paid') {
    ElMessage.warning('该订单无法取消')
    return
  }

  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning'
    })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

const handleDeleteOrder = async (order: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？此操作不可恢复。', '警告', {
      type: 'warning'
    })
    await deleteOrder(order.id)
    ElMessage.success('删除成功')
    loadOrders()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    '-1': '已取消',
    '0': '待支付',
    '1': '待发货',
    '2': '已发货',
    '3': '已完成'
  }
  return map[status] || `未知(${status})`
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    '-1': 'status-cancelled',
    '0': 'status-pending',
    '1': 'status-paid',
    '2': 'status-shipped',
    '3': 'status-completed'
  }
  return map[status] || ''
}

const canCancel = (status: number) => {
  return status === 0 || status === 1
}

const canShip = (status: number) => {
  return status === 1
}

const handleShip = (order: any) => {
  shipOrderId.value = order.id
  trackingNo.value = ''
  shipDialogVisible.value = true
}

const confirmShip = async () => {
  if (!trackingNo.value.trim()) {
    ElMessage.warning('请输入物流单号')
    return
  }
  try {
    await shipOrder(shipOrderId.value!, trackingNo.value)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error('发货失败')
  }
}

const formatPrice = (price: number) => {
  return price ? `¥${price.toFixed(2)}` : '-'
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.substring(0, 16).replace('T', ' ')
}

onMounted(() => {
  loadOrders()
})
</script>

<template>
  <div class="order-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">订单管理</h2>
        <span class="total-count">共 {{ total }} 个订单</span>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索订单号/用户..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <select v-model="statusFilter" class="filter-select" @change="handleStatusFilter">
          <option :value="undefined">全部状态</option>
          <option :value="0">待支付</option>
          <option :value="1">待发货</option>
          <option :value="2">已发货</option>
          <option :value="3">已完成</option>
          <option :value="-1">已取消</option>
        </select>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th class="col-id">订单号</th>
            <th class="col-user">用户</th>
            <th class="col-amount">金额</th>
            <th class="col-status">状态</th>
            <th class="col-time">下单时间</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="orders.length === 0 && !loading">
            <td colspan="6" class="empty-cell">
              <div class="empty-state">
                <span class="empty-icon">📋</span>
                <p>暂无订单数据</p>
              </div>
            </td>
          </tr>
          <tr v-for="order in orders" :key="order.id">
            <td class="col-id">
              <span class="order-id">{{ order.id }}</span>
            </td>
            <td class="col-user">
              <div class="user-info">
                <span class="user-name">{{ order.userName || order.user?.username || '-' }}</span>
                <span class="user-id">ID: {{ order.userId || order.user?.id || '-' }}</span>
              </div>
            </td>
            <td class="col-amount">
              <span class="amount">{{ formatPrice(order.totalAmount || order.amount) }}</span>
            </td>
            <td class="col-status">
              <span class="status-badge" :class="getStatusClass(order.status)">
                {{ getStatusText(order.status) }}
              </span>
            </td>
            <td class="col-time">
              <span class="time">{{ formatDate(order.createTime) }}</span>
            </td>
            <td class="col-actions">
              <button class="action-btn view" @click="handleView(order.id)">
                查看
              </button>
              <button
                v-if="canShip(order.status)"
                class="action-btn ship"
                @click="handleShip(order)"
              >
                发货
              </button>
              <button
                v-if="canCancel(order.status)"
                class="action-btn cancel"
                @click="handleCancelOrder(order)"
              >
                取消
              </button>
              <button
                v-if="order.status === -1 || order.status === 3"
                class="action-btn delete"
                @click="handleDeleteOrder(order)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="shipDialogVisible" title="订单发货" width="400px">
      <div class="ship-form">
        <div class="form-item">
          <label>订单ID：</label>
          <span>{{ shipOrderId }}</span>
        </div>
        <div class="form-item">
          <label>物流单号：</label>
          <input v-model="trackingNo" type="text" placeholder="请输入物流单号" />
        </div>
      </div>
      <template #footer>
        <button class="dialog-btn cancel" @click="shipDialogVisible = false">取消</button>
        <button class="dialog-btn confirm" @click="confirmShip">确认发货</button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-list {
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
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.total-count {
  font-size: 14px;
  color: #999;
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

.col-id {
  width: 140px;
}

.col-user {
  width: 160px;
}

.col-amount {
  width: 120px;
}

.col-status {
  width: 100px;
}

.col-time {
  width: 160px;
}

.col-actions {
  width: 180px;
}

.order-id {
  font-family: monospace;
  font-size: 13px;
  color: #667eea;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.user-id {
  font-size: 12px;
  color: #999;
}

.amount {
  font-size: 14px;
  font-weight: 600;
  color: #f5576c;
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

.status-paid {
  background: #e6f7ff;
  color: #1890ff;
}

.status-shipped {
  background: #f0f5ff;
  color: #597ef7;
}

.status-completed {
  background: #f6ffed;
  color: #52c41a;
}

.status-cancelled {
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

.action-btn.view {
  background: #e6f7ff;
  color: #1890ff;
}

.action-btn.view:hover {
  background: #bae7ff;
}

.action-btn.cancel {
  background: #fff2f0;
  color: #f5576c;
}

.action-btn.cancel:hover {
  background: #ffccc7;
}

.action-btn.delete {
  background: #f5f5f5;
  color: #999;
}

.action-btn.delete:hover {
  background: #ffccc7;
  color: #f5576c;
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .search-box {
    flex: 1;
  }

  .search-box input {
    width: 100%;
  }
}

.action-btn.ship {
  background: #f0f5ff;
  color: #597ef7;
}

.action-btn.ship:hover {
  background: #d9e6ff;
}

.ship-form {
  padding: 10px 0;
}

.ship-form .form-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.ship-form .form-item label {
  width: 80px;
  font-size: 14px;
  color: #666;
}

.ship-form .form-item input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.ship-form .form-item input:focus {
  border-color: #597ef7;
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
  background: #597ef7;
  color: #fff;
}

.dialog-btn.confirm:hover {
  background: #4a6ce0;
}
</style>