<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { register } from '@/api/auth';
import Captcha from '@/components/common/Captcha.vue';

const router = useRouter();
const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const captcha = ref('');
const loading = ref(false);

const handleRegister = async () => {
  if (!username.value.trim()) {
    ElMessage.error('请输入用户名');
    return;
  }
  if (!password.value.trim()) {
    ElMessage.error('请输入密码');
    return;
  }
  if (password.value.length < 6) {
    ElMessage.error('密码长度不能少于6位');
    return;
  }
  if (password.value !== confirmPassword.value) {
    ElMessage.error('两次输入的密码不一致');
    return;
  }
  if (!captcha.value.trim()) {
    ElMessage.error('请输入验证码');
    return;
  }
  loading.value = true;
  try {
    await register({ username: username.value, password: password.value, captcha: captcha.value });
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } catch (error) {
    ElMessage.error('注册失败，请检查信息是否正确');
    captcha.value = '';
  } finally {
    loading.value = false;
  }
};

const goToLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h1>新疆文旅商城</h1>
        <p>用户注册</p>
      </div>

      <form class="register-form" @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码（至少6位）"
          />
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入密码"
          />
        </div>

        <div class="form-group">
          <label>验证码</label>
          <Captcha v-model="captcha" />
        </div>

        <button type="submit" class="register-btn" :disabled="loading">
          <span v-if="loading">注册中...</span>
          <span v-else>注册</span>
        </button>

        <div class="register-links">
          <span class="login-link" @click="goToLogin">已有账号？立即登录</span>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E67E22 0%, #d35400 100%);
  padding: 20px;
}

.register-container {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h1 {
  font-size: 28px;
  color: #E67E22;
  margin-bottom: 8px;
  font-weight: 600;
}

.register-header p {
  color: #7f8c8d;
  font-size: 14px;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #2C3E50;
}

.form-group input {
  height: 44px;
  padding: 0 16px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #E67E22;
}

.register-btn {
  height: 44px;
  background: #E67E22;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 8px;
}

.register-btn:hover:not(:disabled) {
  background: #d35400;
}

.register-btn:disabled {
  background: #f5b77c;
  cursor: not-allowed;
}

.register-links {
  text-align: center;
  margin-top: 16px;
}

.login-link {
  color: #E67E22;
  cursor: pointer;
  font-size: 14px;
}

.login-link:hover {
  text-decoration: underline;
}

@media (max-width: 576px) {
  .register-container {
    padding: 32px 24px;
  }

  .register-header h1 {
    font-size: 24px;
  }
}
</style>
