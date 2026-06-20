<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserDetail } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const userId = route.params.id as string

const user = ref<any>(null)
const loading = ref(true)

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 19)
}

const loadUser = async () => {
  loading.value = true
  try {
    const res: any = await getUserDetail(Number(userId))
    user.value = res?.data
  } catch (error) {
    console.error('Failed to load user:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/users')
}

onMounted(() => {
  loadUser()
})
</script>

<template>
  <div class="user-detail">
    <div class="page-header">
      <h2>用户详情</h2>
      <button class="back-btn" @click="goBack">返回列表</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="user" class="detail-content">
      <div class="info-section">
        <h3>基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">用户ID</span>
            <span class="value">{{ user.id }}</span>
          </div>
          <div class="info-item">
            <span class="label">用户名</span>
            <span class="value">{{ user.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">昵称</span>
            <span class="value">{{ user.nickname || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号</span>
            <span class="value">{{ user.phone || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ user.email || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态</span>
            <span class="value">
              <span :class="['status', user.status === 1 ? 'active' : 'inactive']">
                {{ user.status === 1 ? '正常' : '已禁用' }}
              </span>
            </span>
          </div>
          <div class="info-item">
            <span class="label">注册时间</span>
            <span class="value">{{ formatDate(user.createTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">最后登录</span>
            <span class="value">{{ formatDate(user.updateTime) }}</span>
          </div>
        </div>
      </div>

      <div class="info-section" v-if="user.address">
        <h3>收货地址</h3>
        <div class="info-grid">
          <div class="info-item full-width">
            <span class="label">地址</span>
            <span class="value">{{ user.address }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      未找到用户信息
    </div>
  </div>
</template>

<style scoped>
.user-detail {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

.back-btn {
  padding: 8px 16px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.back-btn:hover {
  background: #e8e8e8;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.detail-content {
  max-width: 800px;
}

.info-section {
  margin-bottom: 32px;
}

.info-section h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.label {
  font-size: 13px;
  color: #999;
}

.value {
  font-size: 14px;
  color: #333;
}

.status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status.active {
  background: #f6ffed;
  color: #52c41a;
}

.status.inactive {
  background: #fff1f0;
  color: #f56c6c;
}
</style>
