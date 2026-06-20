<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAddresses, deleteAddress, setDefaultAddress } from '@/api'
import { ElMessage } from 'element-plus'
import type { Address } from '@/types'

const router = useRouter()

const addresses = ref<Address[]>([])

const loadAddresses = async () => {
  try {
    const data = await getAddresses()
    addresses.value = data
  } catch (error) {
    console.error('Failed to load addresses:', error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await deleteAddress(id)
    addresses.value = addresses.value.filter((a: Address) => a.id !== id)
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleSetDefault = async (id: number) => {
  try {
    await setDefaultAddress(id)
    addresses.value = addresses.value.map((a: Address) => ({
      ...a,
      isDefault: a.id === id
    }))
    ElMessage.success('设置成功')
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const goToEdit = (id?: number) => {
  router.push(id ? `/address-edit/${id}` : '/address-edit')
}

onMounted(() => {
  loadAddresses()
})
</script>

<template>
  <div class="addresses-page">
    <div class="container">
      <h2 class="page-title">收货地址</h2>
      
      <div v-if="addresses.length === 0" class="empty-state">
        <p>暂无收货地址</p>
      </div>
      
      <div v-else class="addresses-list">
        <div v-for="address in addresses" :key="address.id" class="address-card">
          <div>
            <div>{{ address.name }} {{ address.phone }}</div>
            <div>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}</div>
          </div>
          <div v-if="address.isDefault" class="default-tag">默认</div>
          <div class="actions">
            <button @click="goToEdit(address.id)">编辑</button>
            <button v-if="!address.isDefault" @click="handleSetDefault(address.id)">设为默认</button>
            <button @click="handleDelete(address.id)">删除</button>
          </div>
        </div>
      </div>
      
      <button class="add-btn" @click="goToEdit()">添加新地址</button>
    </div>
  </div>
</template>

<style scoped>
.addresses-page {
  padding: 20px 0;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}

.empty-state {
  text-align: center;
  padding: 40px;
}

.addresses-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.address-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.address-card div:first-child {
  flex: 1;
}

.default-tag {
  background: #409eff;
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.actions button {
  padding: 8px 12px;
  margin-left: 8px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
}

.add-btn {
  width: 100%;
  padding: 14px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
}
</style>
