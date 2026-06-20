<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/stores/user';
import Captcha from '@/components/common/Captcha.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const username = ref('');
const password = ref('');
const captcha = ref('');
const loading = ref(false);

const usernameError = ref('');
const passwordError = ref('');
const captchaError = ref('');
const generalError = ref('');

const captchaRef = ref<InstanceType<typeof Captcha> | null>(null);

const clearErrors = () => {
  usernameError.value = '';
  passwordError.value = '';
  captchaError.value = '';
  generalError.value = '';
};

const handleLogin = async () => {
  clearErrors();
  
  if (!username.value.trim()) {
    usernameError.value = '请输入用户名';
    return;
  }
  if (!password.value.trim()) {
    passwordError.value = '请输入密码';
    return;
  }
  if (!captcha.value.trim()) {
    captchaError.value = '请输入验证码';
    return;
  }
  
  loading.value = true;
  try {
    await userStore.login(username.value, password.value, captcha.value);
    await userStore.fetchUserInfo();
    const redirect = route.query.redirect as string || '/';
    router.push(redirect);
  } catch (error: any) {
    const message = error?.message || error?.response?.data?.message || '登录失败';
    
    if (message.includes('用户名') || message.includes('username')) {
      usernameError.value = '用户名不存在';
    } else if (message.includes('密码') || message.includes('password')) {
      passwordError.value = '密码错误';
    } else if (message.includes('验证码') || message.includes('captcha')) {
      captchaError.value = '验证码错误';
      captcha.value = '';
      captchaRef.value?.refreshCaptcha();
    } else {
      generalError.value = message;
    }
  } finally {
    loading.value = false;
  }
};

const goToRegister = () => {
  router.push('/register');
};
</script>

<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1>新疆文旅商城</h1>
        <p>欢迎登录</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            :class="{ 'error': usernameError }"
            @input="usernameError = ''"
          />
          <span v-if="usernameError" class="error-message">{{ usernameError }}</span>
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            :class="{ 'error': passwordError }"
            @input="passwordError = ''"
          />
          <span v-if="passwordError" class="error-message">{{ passwordError }}</span>
        </div>

        <div class="form-group">
          <label>验证码</label>
          <Captcha v-model="captcha" ref="captchaRef" />
          <span v-if="captchaError" class="error-message">{{ captchaError }}</span>
        </div>

        <span v-if="generalError" class="error-message general">{{ generalError }}</span>

        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="loading">登录中...</span>
          <span v-else>登录</span>
        </button>

        <div class="login-links">
          <span class="register-link" @click="goToRegister">还没有账号？立即注册</span>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E67E22 0%, #d35400 100%);
  padding: 20px;
}

.login-container {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 28px;
  color: #E67E22;
  margin-bottom: 8px;
  font-weight: 600;
}

.login-header p {
  color: #7f8c8d;
  font-size: 14px;
}

.login-form {
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

.form-group input.error {
  border-color: #e74c3c;
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
  height: 44px;
  background: #E67E22;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
}

.login-btn:hover:not(:disabled) {
  background: #d35400;
}

.login-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.login-links {
  text-align: center;
  margin-top: 16px;
}

.register-link {
  color: #E67E22;
  cursor: pointer;
  font-size: 14px;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
