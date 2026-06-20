<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createCoupon, updateCoupon, getCouponDetail } from '@/api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const couponId = computed(() => route.params.id as string)

const form = ref({
  name: '',
  description: '',
  couponType: '1',
  discount: 0,
  minAmount: 0,
  maxDiscount: 0,
  totalCount: 0,
  perLimit: 1,
  startTime: '',
  endTime: ''
})

const loading = ref(false)
const submitting = ref(false)

const loadCoupon = async () => {
  if (!couponId.value) return

  loading.value = true
  try {
    const res: any = await getCouponDetail(Number(couponId.value))
    const data = res?.data
    if (data) {
      form.value = {
        name: data.name || '',
        description: data.description || '',
        couponType: String(data.couponType || 1),
        discount: data.discount || 0,
        minAmount: data.minAmount || 0,
        maxDiscount: data.maxDiscount || 0,
        totalCount: data.totalCount || 0,
        perLimit: data.perLimit || 1,
        startTime: data.startTime ? data.startTime.substring(0, 16) : '',
        endTime: data.endTime ? data.endTime.substring(0, 16) : ''
      }
    }
  } catch (error) {
    console.error('Failed to load coupon:', error)
    ElMessage.error('加载优惠券失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.name) {
    ElMessage.warning('请输入优惠券名称')
    return
  }
  if (form.value.discount <= 0) {
    ElMessage.warning('请输入有效的优惠金额')
    return
  }
  if (form.value.totalCount <= 0) {
    ElMessage.warning('请输入有效的发放数量')
    return
  }
  if (!form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请选择有效期')
    return
  }

  submitting.value = true
  try {
    const couponTypeMap: Record<string, string> = {
      '1': 'FIXED',
      '2': 'PERCENT'
    }
    
    const data = {
      name: form.value.name,
      description: form.value.description,
      couponType: couponTypeMap[form.value.couponType] || form.value.couponType,
      discount: form.value.discount,
      minAmount: form.value.minAmount,
      maxDiscount: form.value.maxDiscount,
      totalCount: form.value.totalCount,
      perLimit: form.value.perLimit,
      startTime: form.value.startTime,
      endTime: form.value.endTime
    }

    if (isEdit.value) {
      await updateCoupon(Number(couponId.value), data)
      ElMessage.success('更新成功')
    } else {
      await createCoupon(data)
      ElMessage.success('创建成功')
    }
    router.push('/coupons')
  } catch (error) {
    console.error('Failed to submit:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push('/admin/coupons')
}

onMounted(() => {
  if (isEdit.value) {
    loadCoupon()
  }
})
</script>

<template>
  <div class="coupon-form">
    <h2>{{ isEdit ? '编辑优惠券' : '新增优惠券' }}</h2>

    <div v-if="loading" class="loading">加载中...</div>

    <form v-else @submit.prevent="handleSubmit" class="form-wrapper">
      <div class="form-group">
        <label>优惠券名称 *</label>
        <input v-model="form.name" type="text" placeholder="请输入优惠券名称" />
      </div>

      <div class="form-group">
        <label>优惠券描述</label>
        <textarea v-model="form.description" placeholder="请输入优惠券描述" rows="3"></textarea>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>优惠类型</label>
          <select v-model="form.couponType">
            <option value="1">满减券</option>
            <option value="2">折扣券</option>
          </select>
        </div>

        <div class="form-group">
          <label>{{ form.couponType === '1' ? '优惠金额 (元)' : '折扣率 (%)' }} *</label>
          <input v-model.number="form.discount" type="number" step="0.01" min="0" placeholder="0.00" />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>最低消费金额 (元)</label>
          <input v-model.number="form.minAmount" type="number" step="0.01" min="0" placeholder="0.00" />
        </div>

        <div class="form-group">
          <label>最高优惠金额 (元)</label>
          <input v-model.number="form.maxDiscount" type="number" step="0.01" min="0" placeholder="0.00" />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>发放数量 *</label>
          <input v-model.number="form.totalCount" type="number" min="1" placeholder="1" />
        </div>

        <div class="form-group">
          <label>每人限领</label>
          <input v-model.number="form.perLimit" type="number" min="1" placeholder="1" />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>开始时间 *</label>
          <input v-model="form.startTime" type="datetime-local" />
        </div>

        <div class="form-group">
          <label>结束时间 *</label>
          <input v-model="form.endTime" type="datetime-local" />
        </div>
      </div>

      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="handleCancel">取消</button>
        <button type="submit" class="btn-submit" :disabled="submitting">
          {{ submitting ? '提交中...' : (isEdit ? '更新' : '创建') }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.coupon-form {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

h2 {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 24px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.form-wrapper {
  max-width: 600px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group textarea,
.form-group select,
.form-group input[type="datetime-local"] {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #409eff;
}

.form-group textarea {
  resize: vertical;
}

.form-group input[type="checkbox"] {
  margin-right: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 30px;
}

.btn-cancel,
.btn-submit {
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-cancel:hover {
  background: #e8e8e8;
}

.btn-submit {
  background: #409eff;
  color: #fff;
}

.btn-submit:hover {
  background: #66b1ff;
}

.btn-submit:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}
</style>