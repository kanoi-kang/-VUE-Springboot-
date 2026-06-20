<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCoupons, deleteCoupon } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const coupons = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const loadCoupons = async () => {
  try {
    loading.value = true
    const res: any = await getCoupons({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined
    })
    coupons.value = res?.data?.records || res?.data || res || []
    total.value = res?.data?.total || 0
  } catch (error) {
    console.error('Failed to load coupons:', error)
    ElMessage.error('加载优惠券失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadCoupons()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  page.value = 1
  loadCoupons()
}

const handleSearch = () => {
  page.value = 1
  loadCoupons()
}

const handleAdd = () => {
  router.push('/coupons/create')
}

const handleEdit = (id: number) => {
  router.push(`/coupons/edit/${id}`)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该优惠券吗？', '提示', {
      type: 'warning'
    })
    await deleteCoupon(id)
    ElMessage.success('删除成功')
    loadCoupons()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getTypeText = (type: string) => {
  const map: Record<string, string> = {
    FIXED: '满减券',
    PERCENT: '折扣券',
    GIFT: '礼品券',
    fixed: '满减券',
    percent: '折扣券',
    gift: '礼品券'
  }
  return map[type] || type
}

const getTypeClass = (type: string) => {
  const map: Record<string, string> = {
    FIXED: 'type-fixed',
    PERCENT: 'type-percent',
    GIFT: 'type-gift',
    fixed: 'type-fixed',
    percent: 'type-percent',
    gift: 'type-gift'
  }
  return map[type] || ''
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '未激活', 1: '激活', 2: '已过期' }
  return map[status] || '未知'
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'status-inactive',
    1: 'status-active',
    2: 'status-expired'
  }
  return map[status] || ''
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.substring(0, 10)
}

const formatDiscount = (coupon: any) => {
  if (coupon.couponType === 'PERCENT' || coupon.couponType === 'percent') {
    return `${coupon.discount}%`
  } else if (coupon.couponType === 'FIXED' || coupon.couponType === 'fixed') {
    return `¥${coupon.discount}`
  }
  return '-'
}

onMounted(() => {
  loadCoupons()
})
</script>

<template>
  <div class="coupon-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">优惠券管理</h2>
        <span class="total-count">共 {{ total }} 张优惠券</span>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索优惠券名称..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <button class="add-btn" @click="handleAdd">
          <span>+</span> 添加优惠券
        </button>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th class="col-name">优惠券名称</th>
            <th class="col-type">类型</th>
            <th class="col-value">优惠力度</th>
            <th class="col-condition">使用条件</th>
            <th class="col-status">状态</th>
            <th class="col-date">有效期</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="coupons.length === 0 && !loading">
            <td colspan="7" class="empty-cell">
              <div class="empty-state">
                <span class="empty-icon">🎫</span>
                <p>暂无优惠券数据</p>
              </div>
            </td>
          </tr>
          <tr v-for="coupon in coupons" :key="coupon.id">
            <td class="col-name">
              <span class="coupon-name">{{ coupon.name }}</span>
            </td>
            <td class="col-type">
              <span class="type-badge" :class="getTypeClass(coupon.couponType)">
                {{ getTypeText(coupon.couponType) }}
              </span>
            </td>
            <td class="col-value">
              <span class="discount">{{ formatDiscount(coupon) }}</span>
            </td>
            <td class="col-condition">
              <span class="condition">
                {{ coupon.minAmount ? `满¥${coupon.minAmount}` : '无门槛' }}
              </span>
            </td>
            <td class="col-status">
              <span class="status-badge" :class="getStatusClass(coupon.status)">
                {{ getStatusText(coupon.status) }}
              </span>
            </td>
            <td class="col-date">
              <div class="date-range">
                <span>{{ formatDate(coupon.startTime) }}</span>
                <span class="date-separator">至</span>
                <span>{{ formatDate(coupon.endTime) }}</span>
              </div>
            </td>
            <td class="col-actions">
              <button class="action-btn edit" @click="handleEdit(coupon.id)">
                编辑
              </button>
              <button class="action-btn delete" @click="handleDelete(coupon.id)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.coupon-list {
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

.total-count {
  font-size: 14px;
  color: #999;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
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

.col-name {
  min-width: 180px;
}

.col-type {
  width: 100px;
}

.col-value {
  width: 100px;
}

.col-condition {
  width: 120px;
}

.col-status {
  width: 90px;
}

.col-date {
  width: 220px;
}

.col-actions {
  width: 140px;
}

.coupon-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.type-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.type-fixed {
  background: #fff7e6;
  color: #fa8c16;
}

.type-percent {
  background: #e6f7ff;
  color: #1890ff;
}

.type-gift {
  background: #f9f0ff;
  color: #722ed1;
}

.discount {
  font-size: 14px;
  font-weight: 600;
  color: #f5576c;
}

.condition {
  font-size: 13px;
  color: #666;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #f6ffed;
  color: #52c41a;
}

.status-inactive {
  background: #f5f5f5;
  color: #999;
}

.status-expired {
  background: #fff2f0;
  color: #f5576c;
}

.date-range {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #666;
}

.date-separator {
  color: #999;
}

.action-btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.action-btn.edit {
  background: #e6f7ff;
  color: #1890ff;
  margin-right: 8px;
}

.action-btn.edit:hover {
  background: #bae7ff;
}

.action-btn.delete {
  background: #fff2f0;
  color: #f5576c;
}

.action-btn.delete:hover {
  background: #ffccc7;
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