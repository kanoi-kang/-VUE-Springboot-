<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getBrands, getBrandDetail, createBrand, updateBrand, deleteBrand } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const brands = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentBrandId = ref<number | null>(null)

const form = ref({
  name: '',
  logo: '',
  description: '',
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }]
}

const loadBrands = async () => {
  try {
    loading.value = true
    const res: any = await getBrands({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined
    })
    brands.value = res?.data?.records || res?.data || res || []
    total.value = res?.data?.total || 0
  } catch (error) {
    console.error('Failed to load brands:', error)
    ElMessage.error('加载品牌列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadBrands()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  page.value = 1
  loadBrands()
}

const handleSearch = () => {
  page.value = 1
  loadBrands()
}

const handleAdd = () => {
  isEdit.value = false
  currentBrandId.value = null
  form.value = { name: '', logo: '', description: '', sort: 0 }
  dialogVisible.value = true
}

const handleEdit = async (brand: any) => {
  isEdit.value = true
  currentBrandId.value = brand.id

  try {
    const res: any = await getBrandDetail(brand.id)
    if (res?.data) {
      const data = res.data
      form.value = {
        name: data.name || '',
        logo: data.logo || '',
        description: data.description || '',
        sort: data.sort || 0
      }
    }
  } catch (error) {
    console.error('Failed to load brand detail:', error)
    ElMessage.error('加载品牌详情失败')
    return
  }

  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入品牌名称')
    return
  }

  try {
    if (isEdit.value && currentBrandId.value) {
      await updateBrand(currentBrandId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createBrand(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadBrands()
  } catch (error) {
    console.error('Failed to submit:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

const handleDelete = async (brand: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除品牌"${brand.name}"吗？此操作不可恢复。`,
      '警告',
      { type: 'warning' }
    )
    await deleteBrand(brand.id)
    ElMessage.success('删除成功')
    loadBrands()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.substring(0, 10)
}

onMounted(() => {
  loadBrands()
})
</script>

<template>
  <div class="brand-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">品牌管理</h2>
        <span class="total-count">共 {{ total }} 个品牌</span>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索品牌名称..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <button class="add-btn" @click="handleAdd">
          <span>+</span> 添加品牌
        </button>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-info">品牌信息</th>
            <th class="col-sort">排序</th>
            <th class="col-time">创建时间</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="brands.length === 0 && !loading">
            <td colspan="5" class="empty-cell">
              <div class="empty-state">
                <span class="empty-icon">🏢</span>
                <p>暂无品牌数据</p>
              </div>
            </td>
          </tr>
          <tr v-for="brand in brands" :key="brand.id">
            <td class="col-id">
              <span class="id-text">{{ brand.id }}</span>
            </td>
            <td class="col-info">
              <div class="brand-info">
                <img
                  v-if="brand.logo"
                  :src="brand.logo"
                  class="brand-logo"
                  alt=""
                />
                <div v-else class="brand-logo placeholder">
                  <span>{{ (brand.name || '?').charAt(0) }}</span>
                </div>
                <div class="brand-detail">
                  <span class="brand-name">{{ brand.name }}</span>
                  <span class="brand-description" v-if="brand.description">
                    {{ brand.description }}
                  </span>
                </div>
              </div>
            </td>
            <td class="col-sort">
              <span class="sort-text">{{ brand.sort || 0 }}</span>
            </td>
            <td class="col-time">
              <span class="time">{{ formatDate(brand.createTime) }}</span>
            </td>
            <td class="col-actions">
              <button class="action-btn edit" @click="handleEdit(brand)">编辑</button>
              <button class="action-btn delete" @click="handleDelete(brand)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="dialog-overlay" v-if="dialogVisible" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑品牌' : '添加品牌' }}</h3>
          <button class="close-btn" @click="dialogVisible = false">×</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label class="form-label">品牌名称 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              class="form-input"
              placeholder="请输入品牌名称"
            />
          </div>
          <div class="form-item">
            <label class="form-label">品牌Logo</label>
            <input
              v-model="form.logo"
              type="text"
              class="form-input"
              placeholder="请输入Logo URL"
            />
          </div>
          <div class="form-item">
            <label class="form-label">品牌描述</label>
            <textarea
              v-model="form.description"
              class="form-textarea"
              placeholder="请输入品牌描述"
              rows="3"
            ></textarea>
          </div>
          <div class="form-item">
            <label class="form-label">排序</label>
            <input
              v-model.number="form.sort"
              type="number"
              class="form-input"
              placeholder="数值越小越靠前"
            />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="cancel-btn" @click="dialogVisible = false">取消</button>
          <button class="submit-btn" @click="handleSubmit">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.brand-list {
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
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.add-btn span {
  font-size: 18px;
  font-weight: 600;
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

.col-sort {
  width: 80px;
}

.col-time {
  width: 120px;
}

.col-actions {
  width: 150px;
}

.id-text {
  font-family: monospace;
  font-size: 13px;
  color: #999;
}

.sort-text {
  font-size: 13px;
  color: #666;
}

.brand-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f5f5;
}

.brand-logo.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  font-size: 18px;
  font-weight: 600;
}

.brand-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.brand-description {
  font-size: 12px;
  color: #999;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 13px;
  color: #666;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  margin-right: 8px;
}

.action-btn.edit {
  background: #e8f4ff;
  color: #1890ff;
}

.action-btn.edit:hover {
  background: #1890ff;
  color: #fff;
}

.action-btn.delete {
  background: #fff1f0;
  color: #ff4d4f;
}

.action-btn.delete:hover {
  background: #ff4d4f;
  color: #fff;
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

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: #fff;
  border-radius: 12px;
  width: 480px;
  max-width: 90vw;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #666;
}

.dialog-body {
  padding: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.required {
  color: #ff4d4f;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.cancel-btn,
.submit-btn {
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn {
  background: #fff;
  border: 1px solid #d9d9d9;
  color: #666;
}

.cancel-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: #fff;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
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
