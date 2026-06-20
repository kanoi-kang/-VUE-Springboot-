<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getProductDetail, createProduct, updateProduct, getCategories, getBrands } from '@/api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const submitting = ref(false)

const categories = ref<any[]>([])
const brands = ref<any[]>([])

const form = ref({
  name: '',
  productSn: '',
  description: '',
  content: '',
  categoryId: '',
  brandId: '',
  price: '',
  originalPrice: '',
  costPrice: '',
  stock: '',
  lowStock: '',
  pic: '',
  picsText: '',
  unit: '',
  weight: '',
  status: 1,
  isPublish: 1,
  isNew: 0,
  isHot: 0
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
}

const loadCategories = async () => {
  try {
    const res: any = await getCategories()
    categories.value = res?.data?.records || res?.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadBrands = async () => {
  try {
    const res: any = await getBrands()
    const records = res?.data?.records || res?.data?.content || []
    brands.value = Array.isArray(records) ? records : []
  } catch (error) {
    console.error('Failed to load brands:', error)
  }
}

const loadProduct = async (id: number) => {
  try {
    loading.value = true
    const res: any = await getProductDetail(id)
    if (res?.data) {
      const data = res.data
      form.value = {
        name: data.name || '',
        productSn: data.productSn || '',
        description: data.description || '',
        content: data.content || '',
        categoryId: data.categoryId || '',
        brandId: data.brandId || '',
        price: data.price || '',
        originalPrice: data.originalPrice || '',
        costPrice: data.costPrice || '',
        stock: data.stock || '',
        lowStock: data.lowStock || '',
        pic: data.pic || '',
        picsText: Array.isArray(data.pics) ? data.pics.join(',') : (data.pics || ''),
        unit: data.unit || '',
        weight: data.weight || '',
        status: data.status ?? 1,
        isPublish: data.isPublish ?? 1,
        isNew: data.isNew ?? 0,
        isHot: data.isHot ?? 0
      }
    }
  } catch (error) {
    console.error('Failed to load product:', error)
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.name) {
    ElMessage.error('请输入商品名称')
    return
  }
  if (!form.value.price) {
    ElMessage.error('请输入商品价格')
    return
  }

  try {
    submitting.value = true
    const data: any = {
      name: form.value.name,
      productSn: form.value.productSn,
      description: form.value.description,
      content: form.value.content,
      categoryId: form.value.categoryId ? Number(form.value.categoryId) : null,
      brandId: form.value.brandId ? Number(form.value.brandId) : null,
      price: form.value.price,
      originalPrice: form.value.originalPrice || null,
      costPrice: form.value.costPrice || null,
      stock: form.value.stock ? Number(form.value.stock) : 0,
      lowStock: form.value.lowStock ? Number(form.value.lowStock) : 0,
      pic: form.value.pic,
      pics: form.value.picsText.split(',').filter(Boolean),
      unit: form.value.unit,
      weight: form.value.weight || null,
      status: form.value.status,
      isPublish: form.value.isPublish,
      isNew: form.value.isNew,
      isHot: form.value.isHot
    }

    if (isEdit.value) {
      await updateProduct(Number(route.params.id), data)
      ElMessage.success('更新成功')
    } else {
      await createProduct(data)
      ElMessage.success('创建成功')
    }
    router.push('/products')
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.back()
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadBrands()])
  if (isEdit.value && route.params.id) {
    await loadProduct(Number(route.params.id))
  }
})
</script>

<template>
  <div class="product-form">
    <div class="page-header">
      <button class="back-btn" @click="handleCancel">
        <span>←</span> 返回
      </button>
      <h2 class="page-title">{{ isEdit ? '编辑商品' : '添加商品' }}</h2>
    </div>

    <div class="form-container" v-loading="loading">
      <div class="form-section">
        <h3 class="section-title">基本信息</h3>
        <div class="form-grid">
          <div class="form-item full-width">
            <label class="form-label">商品名称 <span class="required">*</span></label>
            <input
              v-model="form.name"
              type="text"
              class="form-input"
              placeholder="请输入商品名称"
            />
          </div>

          <div class="form-item">
            <label class="form-label">商品编码</label>
            <input
              v-model="form.productSn"
              type="text"
              class="form-input"
              placeholder="请输入商品编码"
            />
          </div>

          <div class="form-item">
            <label class="form-label">商品单位</label>
            <input
              v-model="form.unit"
              type="text"
              class="form-input"
              placeholder="如: 件、个、箱"
            />
          </div>

          <div class="form-item">
            <label class="form-label">商品分类</label>
            <select v-model="form.categoryId" class="form-select">
              <option value="">请选择分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>

          <div class="form-item">
            <label class="form-label">品牌</label>
            <select v-model="form.brandId" class="form-select">
              <option value="">请选择品牌</option>
              <option v-for="brand in brands" :key="brand.id" :value="brand.id">
                {{ brand.name }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-title">价格与库存</h3>
        <div class="form-grid">
          <div class="form-item">
            <label class="form-label">商品价格 <span class="required">*</span></label>
            <input
              v-model="form.price"
              type="number"
              class="form-input"
              placeholder="0.00"
              step="0.01"
            />
          </div>

          <div class="form-item">
            <label class="form-label">原价</label>
            <input
              v-model="form.originalPrice"
              type="number"
              class="form-input"
              placeholder="0.00"
              step="0.01"
            />
          </div>

          <div class="form-item">
            <label class="form-label">成本价</label>
            <input
              v-model="form.costPrice"
              type="number"
              class="form-input"
              placeholder="0.00"
              step="0.01"
            />
          </div>

          <div class="form-item">
            <label class="form-label">商品重量 (kg)</label>
            <input
              v-model="form.weight"
              type="number"
              class="form-input"
              placeholder="0.00"
              step="0.01"
            />
          </div>

          <div class="form-item">
            <label class="form-label">库存</label>
            <input
              v-model="form.stock"
              type="number"
              class="form-input"
              placeholder="0"
            />
          </div>

          <div class="form-item">
            <label class="form-label">库存预警</label>
            <input
              v-model="form.lowStock"
              type="number"
              class="form-input"
              placeholder="0"
            />
          </div>
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-title">商品图片</h3>
        <div class="form-grid">
          <div class="form-item full-width">
            <label class="form-label">主图</label>
            <input
              v-model="form.pic"
              type="text"
              class="form-input"
              placeholder="请输入主图URL"
            />
            <div v-if="form.pic" class="image-preview">
              <img :src="form.pic" alt="主图" />
            </div>
          </div>

          <div class="form-item full-width">
            <label class="form-label">商品相册 (多张图片用逗号分隔)</label>
            <input
              v-model="form.picsText"
              type="text"
              class="form-input"
              placeholder="请输入图片URL，多张用逗号分隔"
            />
          </div>
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-title">商品详情</h3>
        <div class="form-item full-width">
          <label class="form-label">商品描述</label>
          <textarea
            v-model="form.description"
            class="form-textarea"
            placeholder="请输入商品描述"
            rows="3"
          ></textarea>
        </div>

        <div class="form-item full-width">
          <label class="form-label">商品详情</label>
          <textarea
            v-model="form.content"
            class="form-textarea"
            placeholder="请输入商品详情"
            rows="6"
          ></textarea>
        </div>
      </div>

      <div class="form-section">
        <h3 class="section-title">商品设置</h3>
        <div class="form-grid switch-grid">
          <div class="switch-item">
            <span class="switch-label">上架状态</span>
            <label class="switch">
              <input type="checkbox" v-model="form.status" :true-value="1" :false-value="0" />
              <span class="switch-slider"></span>
            </label>
          </div>

          <div class="switch-item">
            <span class="switch-label">立即发布</span>
            <label class="switch">
              <input type="checkbox" v-model="form.isPublish" :true-value="1" :false-value="0" />
              <span class="switch-slider"></span>
            </label>
          </div>

          <div class="switch-item">
            <span class="switch-label">新品</span>
            <label class="switch">
              <input type="checkbox" v-model="form.isNew" :true-value="1" :false-value="0" />
              <span class="switch-slider"></span>
            </label>
          </div>

          <div class="switch-item">
            <span class="switch-label">热卖</span>
            <label class="switch">
              <input type="checkbox" v-model="form.isHot" :true-value="1" :false-value="0" />
              <span class="switch-slider"></span>
            </label>
          </div>
        </div>
      </div>

      <div class="form-actions">
        <button class="btn btn-cancel" @click="handleCancel">取消</button>
        <button class="btn btn-primary" @click="handleSubmit" :disabled="submitting">
          {{ submitting ? '提交中...' : (isEdit ? '保存修改' : '创建商品') }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-form {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: 1px solid #e8e8e8;
  color: #666;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.form-container {
  max-width: 900px;
}

.form-section {
  margin-bottom: 32px;
}

.form-section:last-of-type {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 20px 0;
  padding-left: 12px;
  border-left: 4px solid #667eea;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item.full-width {
  grid-column: span 2;
}

.form-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.required {
  color: #f5576c;
}

.form-input,
.form-select,
.form-textarea {
  padding: 10px 14px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
  background: #fff;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.image-preview {
  margin-top: 10px;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.switch-grid {
  grid-template-columns: repeat(4, 1fr);
}

.switch-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-label {
  font-size: 14px;
  color: #666;
}

.switch {
  position: relative;
  width: 44px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #ccc;
  border-radius: 24px;
  transition: 0.3s;
}

.switch-slider::before {
  position: absolute;
  content: '';
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background: #fff;
  border-radius: 50%;
  transition: 0.3s;
}

.switch input:checked + .switch-slider {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.switch input:checked + .switch-slider::before {
  transform: translateX(20px);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e8e8e8;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-item.full-width {
    grid-column: span 1;
  }

  .switch-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>