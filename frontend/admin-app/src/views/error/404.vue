<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboards')
}

const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-icon">!</div>
      <div class="error-code">404</div>
      <div class="error-title">页面未找到</div>
      <div class="error-desc">抱歉，您访问的页面不存在或已被删除</div>
      <div class="error-actions">
        <button class="btn-home" @click="goHome">返回首页</button>
        <button class="btn-back" @click="goBack">返回上一页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.error-content {
  text-align: center;
}

.error-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #fff;
  border: 4px solid #f56c6c;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: bold;
  color: #f56c6c;
  margin: 0 auto 24px;
}

.error-code {
  font-size: 72px;
  font-weight: bold;
  color: #f56c6c;
  line-height: 1;
  margin-bottom: 16px;
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.error-desc {
  font-size: 14px;
  color: #999;
  margin-bottom: 32px;
}

.error-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn-home,
.btn-back {
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-home {
  background: #409eff;
  color: #fff;
}

.btn-back {
  background: #f5f5f5;
  color: #666;
}

.btn-home:hover {
  background: #66b1ff;
}

.btn-back:hover {
  background: #e8e8e8;
}
</style>
