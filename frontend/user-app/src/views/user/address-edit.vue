<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createAddress, updateAddress, getAddress } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const form = ref({
  consignee: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

const isEdit = ref(false)

const loadAddress = async () => {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    try {
      const data = await getAddress(parseInt(id as string))
      form.value = {
        consignee: data.consignee || data.name || '',
        phone: data.phone || '',
        province: data.province || '',
        city: data.city || '',
        district: data.district || '',
        detailAddress: data.detailAddress || data.detail || '',
        isDefault: data.isDefault || 0
      }
    } catch (error) {
      console.error('Failed to load address:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    const submitData = {
      ...form.value,
      isDefault: form.value.isDefault ? 1 : 0
    }
    
    if (isEdit.value) {
      await updateAddress(parseInt(route.params.id as string), submitData)
      ElMessage.success('更新成功')
    } else {
      await createAddress(submitData)
      ElMessage.success('添加成功')
    }
    router.push('/addresses')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadAddress()
})
</script>

<template>
  <div class="address-edit-page">
    <div class="container">
      <h2 class="page-title">{{ isEdit ? '编辑地址' : '添加地址' }}</h2>
      
      <div class="form-group">
        <label>姓名</label>
        <input v-model="form.consignee" type="text" placeholder="请输入姓名" />
      </div>
      
      <div class="form-group">
        <label>手机号</label>
        <input v-model="form.phone" type="tel" placeholder="请输入手机号" />
      </div>
      
      <div class="form-group">
        <label>省</label>
        <input v-model="form.province" type="text" placeholder="请输入省份" />
      </div>
      
      <div class="form-group">
        <label>市</label>
        <input v-model="form.city" type="text" placeholder="请输入城市" />
      </div>
      
      <div class="form-group">
        <label>区</label>
        <input v-model="form.district" type="text" placeholder="请输入区县" />
      </div>
      
      <div class="form-group">
        <label>详细地址</label>
        <textarea v-model="form.detailAddress" placeholder="请输入详细地址"></textarea>
      </div>
      
      <div class="form-group">
        <label>
          <input v-model="form.isDefault" type="checkbox" :true-value="1" :false-value="0" />
          设为默认地址
        </label>
      </div>
      
      <button class="submit-btn" @click="handleSubmit">{{ isEdit ? '保存' : '添加' }}</button>
    </div>
  </div>
</template>

<style scoped>
.address-edit-page {
  padding: 20px 0;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.form-group textarea {
  min-height: 80px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
}
</style>