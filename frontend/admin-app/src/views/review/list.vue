<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getReviews, updateReviewShowStatus, deleteReview, deleteReviewsBatch } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const reviews = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const productId = ref<number | undefined>(undefined)
const isShow = ref<number | undefined>(undefined)
const keyword = ref('')
const selectedIds = ref<number[]>([])

const loadReviews = async () => {
  try {
    loading.value = true
    const res: any = await getReviews({
      page: page.value,
      size: pageSize.value,
      productId: productId.value,
      isShow: isShow.value,
      keyword: keyword.value || undefined
    })
    console.log('Reviews API response:', res)
    reviews.value = res?.data?.records || []
    total.value = res?.data?.total || 0
    if (reviews.value.length > 0) {
      console.log('First review item:', reviews.value[0])
    }
  } catch (error) {
    console.error('Failed to load reviews:', error)
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadReviews()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  page.value = 1
  loadReviews()
}

const handleFilterChange = () => {
  page.value = 1
  loadReviews()
}

const handleToggleShow = async (review: any) => {
  const newStatus = review.isShow === 1 ? 0 : 1
  const action = newStatus === 1 ? '显示' : '隐藏'

  try {
    await ElMessageBox.confirm(`确定要${action}该评价吗？`, '提示', {
      type: 'warning'
    })
    await updateReviewShowStatus(review.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadReviews()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleDelete = async (review: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该评价吗？此操作不可恢复。', '警告', {
      type: 'warning'
    })
    await deleteReview(review.id)
    ElMessage.success('删除成功')
    loadReviews()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的评价')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条评价吗？此操作不可恢复。`, '警告', {
      type: 'warning'
    })
    await deleteReviewsBatch(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    loadReviews()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const getShowText = (isShow: number) => {
  return isShow === 1 ? '显示' : '隐藏'
}

const getShowClass = (isShow: number) => {
  return isShow === 1 ? 'status-active' : 'status-disabled'
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.substring(0, 10)
}

const getRatingStars = (rating: number) => {
  return '★'.repeat(rating) + '☆'.repeat(5 - rating)
}

const getPicUrls = (picUrls: string) => {
  if (!picUrls) return []
  try {
    return JSON.parse(picUrls)
  } catch {
    return picUrls.split(',').filter(Boolean)
  }
}

onMounted(() => {
  loadReviews()
})
</script>

<template>
  <div class="review-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">评价管理</h2>
        <span class="total-count">共 {{ total }} 条评价</span>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索评价内容..."
            @keyup.enter="handleFilterChange"
          />
          <button class="search-btn" @click="handleFilterChange">搜索</button>
        </div>
        <select v-model="isShow" class="filter-select" @change="handleFilterChange">
          <option :value="undefined">全部状态</option>
          <option :value="1">显示</option>
          <option :value="0">隐藏</option>
        </select>
        <button
          class="batch-delete-btn"
          @click="handleBatchDelete"
          :disabled="selectedIds.length === 0"
        >
          批量删除 ({{ selectedIds.length }})
        </button>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th class="col-checkbox">
              <input type="checkbox" @change="(e: any) => {
                if (e.target.checked) {
                  selectedIds = reviews.map(r => r.id)
                } else {
                  selectedIds = []
                }
              }" />
            </th>
            <th class="col-id">ID</th>
            <th class="col-product">商品信息</th>
            <th class="col-user">用户</th>
            <th class="col-rating">评分</th>
            <th class="col-content">评价内容</th>
            <th class="col-status">状态</th>
            <th class="col-time">时间</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="reviews.length === 0 && !loading">
            <td colspan="9" class="empty-cell">
              <div class="empty-state">
                <span class="empty-icon">📝</span>
                <p>暂无评价数据</p>
              </div>
            </td>
          </tr>
          <tr v-for="review in reviews" :key="review.id">
            <td class="col-checkbox">
              <input
                type="checkbox"
                :checked="selectedIds.includes(review.id)"
                @change="(e: any) => {
                  if (e.target.checked) {
                    selectedIds.push(review.id)
                  } else {
                    selectedIds = selectedIds.filter(id => id !== review.id)
                  }
                }"
              />
            </td>
            <td class="col-id">
              <span class="id-text">{{ review.id }}</span>
            </td>
            <td class="col-product">
              <span class="product-name">{{ review.productName || `商品ID: ${review.productId}` }}</span>
            </td>
            <td class="col-user">
              <span class="user-name">{{ review.nickname || review.username || '-' }}</span>
            </td>
            <td class="col-rating">
              <span class="rating">{{ getRatingStars(review.rating) }}</span>
            </td>
            <td class="col-content">
              <div class="content-preview">
                <p class="content-text">{{ review.content || '-' }}</p>
                <div class="pic-list" v-if="getPicUrls(review.picUrls).length > 0">
                  <img
                    v-for="(pic, idx) in getPicUrls(review.picUrls)"
                    :key="idx"
                    :src="pic"
                    class="review-pic"
                    @error="(e: any) => e.target.style.display = 'none'"
                  />
                </div>
              </div>
            </td>
            <td class="col-status">
              <span class="status-badge" :class="getShowClass(review.isShow)">
                {{ getShowText(review.isShow) }}
              </span>
            </td>
            <td class="col-time">
              <span class="time">{{ formatDate(review.createTime) }}</span>
            </td>
            <td class="col-actions">
              <button
                class="action-btn"
                :class="review.isShow === 1 ? 'hide' : 'show'"
                @click="handleToggleShow(review)"
              >
                {{ review.isShow === 1 ? '隐藏' : '显示' }}
              </button>
              <button class="action-btn delete" @click="handleDelete(review)">删除</button>
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
  </div>
</template>

<style scoped>
.review-list {
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

.filter-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.batch-delete-btn {
  padding: 8px 16px;
  background: #fff2f0;
  color: #f5576c;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.batch-delete-btn:hover:not(:disabled) {
  background: #ffccc7;
}

.batch-delete-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  vertical-align: top;
}

.data-table tbody tr {
  transition: background 0.2s;
}

.data-table tbody tr:hover {
  background: #fafafa;
}

.col-checkbox {
  width: 40px;
}

.col-id {
  width: 60px;
}

.col-product {
  width: 100px;
}

.col-user {
  width: 100px;
}

.col-rating {
  width: 100px;
}

.col-content {
  max-width: 300px;
}

.col-status {
  width: 80px;
}

.col-time {
  width: 100px;
}

.col-actions {
  width: 150px;
}

.id-text {
  font-family: monospace;
  font-size: 13px;
  color: #999;
}

.product-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.rating {
  font-size: 14px;
  color: #f5a623;
  letter-spacing: 2px;
}

.content-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.content-text {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pic-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.review-pic {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
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

.status-disabled {
  background: #f5f5f5;
  color: #999;
}

.time {
  font-size: 13px;
  color: #666;
}

.action-btn {
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  margin-right: 4px;
}

.action-btn.show {
  background: #f6ffed;
  color: #52c41a;
}

.action-btn.show:hover {
  background: #d9f7be;
}

.action-btn.hide {
  background: #fff7e6;
  color: #faad14;
}

.action-btn.hide:hover {
  background: #ffe58f;
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

  .filter-select {
    flex: 1;
  }
}

.search-box {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  margin-right: 12px;
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
