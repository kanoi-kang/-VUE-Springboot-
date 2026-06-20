<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProducts, deleteProduct, getCategories, getBrands } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const products = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const categoryId = ref<number | undefined>(undefined)
const brandId = ref<number | undefined>(undefined)
const status = ref<number | undefined>(undefined)
const categories = ref<any[]>([])
const brands = ref<any[]>([])

const loadProducts = async () => {
  try {
    loading.value = true
    const res: any = await getProducts({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      brandId: brandId.value,
      status: status.value
    })
    products.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (error) {
    console.error('Failed to load products:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res: any = await getCategories()
    categories.value = res?.data?.records || res?.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadBrandsList = async () => {
  try {
    const res: any = await getBrands()
    brands.value = res?.data?.records || res?.data || []
  } catch (error) {
    console.error('Failed to load brands:', error)
  }
}

const handleSearch = () => {
  page.value = 1
  loadProducts()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadProducts()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  page.value = 1
  loadProducts()
}

const handleEdit = (id: number) => {
  router.push(`/products/edit/${id}`)
}

const handleDelete = async (id: number) => {
  if (!id) {
    ElMessage.error('商品ID无效')
    return
  }
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    })
    await deleteProduct(id)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleAdd = () => {
  router.push('/products/create')
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '下架', 1: '上架' }
  return map[status] || '未知'
}

const getStatusClass = (status: number) => {
  return status === 1 ? 'status-active' : 'status-inactive'
}

onMounted(() => {
  loadProducts()
  loadCategories()
  loadBrandsList()
})
</script>

<template>
  <div class="product-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">商品管理</h2>
        <span class="total-count">共 {{ total }} 件商品</span>
      </div>
      <button class="add-btn" @click="handleAdd">
        <span>+</span> 添加商品
      </button>
    </div>

    <div class="filter-bar">
      <div class="search-box">
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索商品名称..."
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>
      <select v-model="categoryId" class="filter-select" @change="handleSearch">
        <option :value="undefined">全部分类</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
      </select>
      <select v-model="brandId" class="filter-select" @change="handleSearch">
        <option :value="undefined">全部品牌</option>
        <option v-for="brand in brands" :key="brand.id" :value="brand.id">{{ brand.name }}</option>
      </select>
      <select v-model="status" class="filter-select" @change="handleSearch">
        <option :value="undefined">全部状态</option>
        <option :value="1">上架</option>
        <option :value="0">下架</option>
      </select>
    </div>

    <div class="table-container">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-info">商品信息</th>
            <th class="col-price">价格</th>
            <th class="col-stock">库存</th>
            <th class="col-status">状态</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="products.length === 0 && !loading">
            <td colspan="6" class="empty-cell">
              <div class="empty-state">
                <span class="empty-icon">📦</span>
                <p>暂无商品数据</p>
              </div>
            </td>
          </tr>
          <tr v-for="product in products" :key="product.id">
            <td class="col-id">
              <span class="id-text">{{ product.id }}</span>
            </td>
            <td class="col-info">
              <div class="product-info">
                <img
                  v-if="product.pic"
                  :src="product.pic"
                  class="product-image"
                  alt=""
                />
                <div v-else class="product-image placeholder">
                  <span>暂无</span>
                </div>
                <div class="product-detail">
                  <span class="product-name">{{ product.name }}</span>
                  <span class="product-sn">编码: {{ product.productSn || '-' }}</span>
                </div>
              </div>
            </td>
            <td class="col-price">
              <div class="price-info">
                <span class="current-price">¥{{ product.price }}</span>
                <span v-if="product.originalPrice" class="original-price">
                  ¥{{ product.originalPrice }}
                </span>
              </div>
            </td>
            <td class="col-stock">
              <span class="stock-value" :class="{ 'low-stock': product.stock <= (product.lowStock || 10) }">
                {{ product.stock }}
              </span>
              <span v-if="product.lowStock" class="low-stock-threshold">
                最低 {{ product.lowStock }}
              </span>
            </td>
            <td class="col-status">
              <span class="status-badge" :class="getStatusClass(product.status)">
                {{ getStatusText(product.status) }}
              </span>
            </td>
            <td class="col-actions">
              <button class="action-btn edit" @click="handleEdit(product.id)">
                编辑
              </button>
              <button class="action-btn delete" @click="handleDelete(product.id)">
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
  </div>
</template>

<style scoped>
.product-list {
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

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
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
  width: 80px;
}

.col-info {
  min-width: 280px;
}

.col-price {
  width: 120px;
}

.col-stock {
  width: 100px;
}

.col-status {
  width: 80px;
}

.col-actions {
  width: 140px;
}

.id-text {
  font-family: monospace;
  font-size: 13px;
  color: #999;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f5f5;
}

.product-image.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

.product-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-sn {
  font-size: 12px;
  color: #999;
}

.price-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.current-price {
  font-size: 14px;
  font-weight: 600;
  color: #f5576c;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.stock-value {
  font-size: 14px;
  font-weight: 500;
}

.stock-value.low-stock {
  color: #f5576c;
}

.low-stock-threshold {
  display: block;
  font-size: 11px;
  color: #999;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.status-active {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.status-inactive {
  background: #f5f5f5;
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  font-weight: 400;
}

:deep(.el-pagination__total) {
  color: #999;
}
</style>