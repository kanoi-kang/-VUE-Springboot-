<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdminStore } from '@/stores/admin'
import axios from 'axios'

const adminStore = useAdminStore()

const username = ref('')
const password = ref('')
const captcha = ref('')
const captchaUrl = ref('')
const loading = ref(false)

const usernameError = ref('')
const passwordError = ref('')
const captchaError = ref('')
const generalError = ref('')

const clearErrors = () => {
  usernameError.value = ''
  passwordError.value = ''
  captchaError.value = ''
  generalError.value = ''
}

const refreshCaptcha = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/captcha', {
      withCredentials: true,
      responseType: 'blob'
    })
    const url = URL.createObjectURL(res.data)
    captchaUrl.value = url
  } catch (error) {
    console.error('Failed to load captcha:', error)
  }
}

onMounted(() => {
  refreshCaptcha()
  if (localStorage.getItem('admin_token')) {
    window.location.replace('/admin/dashboards')
  }
})

const handleLogin = async () => {
  clearErrors()
  
  if (!username.value.trim()) {
    usernameError.value = '请输入用户名'
    return
  }
  if (!password.value.trim()) {
    passwordError.value = '请输入密码'
    return
  }
  if (!captcha.value.trim()) {
    captchaError.value = '请输入验证码'
    return
  }

  loading.value = true
  try {
    const success = await adminStore.login(username.value, password.value, captcha.value)
    if (success) {
      window.location.replace('/admin/dashboards')
    } else {
      generalError.value = '登录失败'
      refreshCaptcha()
    }
  } catch (error: any) {
    const message = error?.message || error?.response?.data?.message || '登录失败'
    
    if (message.includes('用户名') || message.includes('username')) {
      usernameError.value = '用户名不存在'
    } else if (message.includes('密码') || message.includes('password')) {
      passwordError.value = '密码错误'
    } else if (message.includes('验证码') || message.includes('captcha')) {
      captchaError.value = '验证码错误'
      captcha.value = ''
      refreshCaptcha()
    } else {
      generalError.value = message
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <span>📚</span>
        </div>
        <h1>文旅管理后台</h1>
        <p>Welcome back, please login to continue</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-item">
          <label>用户名</label>
          <div class="input-wrapper" :class="{ 'error': usernameError }">
            <span class="input-icon">👤</span>
            <input
              v-model="username"
              type="text"
              placeholder="请输入用户名"
              autocomplete="username"
              @input="usernameError = ''"
            />
          </div>
          <span v-if="usernameError" class="error-message">{{ usernameError }}</span>
        </div>

        <div class="form-item">
          <label>密码</label>
          <div class="input-wrapper" :class="{ 'error': passwordError }">
            <span class="input-icon">🔒</span>
            <input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              @input="passwordError = ''"
            />
          </div>
          <span v-if="passwordError" class="error-message">{{ passwordError }}</span>
        </div>

        <div class="form-item captcha-item">
          <label>验证码</label>
          <div class="input-wrapper captcha-wrapper" :class="{ 'error': captchaError }">
            <span class="input-icon">🔐</span>
            <input
              v-model="captcha"
              type="text"
              placeholder="请输入验证码"
              class="captcha-input"
              @input="captchaError = ''"
            />
            <img :src="captchaUrl" @click="refreshCaptcha" alt="验证码" class="captcha-img" />
          </div>
          <span v-if="captchaError" class="error-message">{{ captchaError }}</span>
        </div>

        <span v-if="generalError" class="error-message general">{{ generalError }}</span>

        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>登 录</span>
        </button>
      </form>
    </div>

    <div class="login-footer">
      <p>© 2024 文旅管理后台 - All Rights Reserved</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 400px;
  height: 400px;
  right: -100px;
  top: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  left: -80px;
  bottom: -80px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  right: 20%;
  bottom: 10%;
}

.login-card {
  width: 420px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 32px;
}

.login-header h1 {
  font-size: 26px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.login-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.input-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  transition: all 0.3s;
  background: #fafafa;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-wrapper.error {
  border-color: #e74c3c;
  background: #fff5f5;
}

.input-icon {
  padding-left: 14px;
  font-size: 16px;
}

.input-wrapper input {
  flex: 1;
  padding: 14px 14px 14px 10px;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
}

.input-wrapper input::placeholder {
  color: #bfbfbf;
}

.captcha-wrapper {
  flex: 1;
}

.captcha-input {
  width: 120px;
}

.captcha-img {
  width: 100px;
  height: 40px;
  border-radius: 8px;
  margin-right: 8px;
  cursor: pointer;
}

.error-message {
  font-size: 12px;
  color: #e74c3c;
  margin-top: -4px;
}

.error-message.general {
  margin-top: 0;
  text-align: center;
}

.login-btn {
  width: 100%;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.login-footer {
  margin-top: 32px;
  text-align: center;
}

.login-footer p {
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  margin: 0;
}
</style>
