<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserProfile, updateUserProfile, uploadAvatar } from '@/api'
import type { UserInfo } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref<UserInfo | null>(null)
const editing = ref(false)

const editForm = ref({
  nickname: '',
  phone: '',
  email: ''
})

const errors = ref({
  nickname: '',
  phone: '',
  email: ''
})

const avatarPreview = ref('')
const isUploading = ref(false)

const menuItems = [
  { label: '我的订单', path: '/orders', icon: '📦' },
  { label: '我的收藏', path: '/collections', icon: '⭐' },
  { label: '收货地址', path: '/addresses', icon: '📍' },
  { label: '优惠券', path: '/coupons', icon: '🎫' },
  { label: '用户评价', path: '/review', icon: '💬' },
]

const loadUserInfo = async () => {
  try {
    const response = await getUserProfile()
    const data = response.data || response
    userInfo.value = data
    if (data) {
      editForm.value = {
        nickname: data.nickname || '',
        phone: data.phone || '',
        email: data.email || ''
      }
      avatarPreview.value = data.avatar || ''
    }
  } catch (error) {
    console.error('Failed to load user info:', error)
  }
}

const validateForm = (): boolean => {
  errors.value = { nickname: '', phone: '', email: '' }
  let isValid = true

  if (!editForm.value.nickname.trim()) {
    errors.value.nickname = '请输入昵称'
    isValid = false
  } else if (editForm.value.nickname.length > 20) {
    errors.value.nickname = '昵称不能超过20个字符'
    isValid = false
  }

  if (editForm.value.phone) {
    const phoneRegex = /^1[3-9]\d{9}$/
    if (!phoneRegex.test(editForm.value.phone)) {
      errors.value.phone = '请输入正确的手机号码'
      isValid = false
    }
  }

  if (editForm.value.email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(editForm.value.email)) {
      errors.value.email = '请输入正确的邮箱地址'
      isValid = false
    }
  }

  return isValid
}

const handleSave = async () => {
  if (!userInfo.value) return

  if (!validateForm()) {
    return
  }

  try {
    await updateUserProfile(editForm.value)
    userInfo.value = { ...userInfo.value, ...editForm.value }
    editing.value = false
    window.alert('更新成功')
  } catch (error: any) {
    window.alert(error?.message || '更新失败')
  }
}

const handleCancel = () => {
  editing.value = false
  if (userInfo.value) {
    editForm.value = {
      nickname: userInfo.value.nickname || '',
      phone: userInfo.value.phone || '',
      email: userInfo.value.email || ''
    }
    avatarPreview.value = userInfo.value.avatar || ''
  }
}

const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const maxSize = 2 * 1024 * 1024
  if (file.size > maxSize) {
    window.alert('图片大小不能超过2MB')
    return
  }

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    window.alert('只支持 JPG、PNG、GIF 格式的图片')
    return
  }

  isUploading.value = true

  try {
    const formData = new FormData()
    formData.append('avatar', file)

    const result = await uploadAvatar(formData)
    const response = result.data || result
    if (response.avatar) {
      avatarPreview.value = response.avatar
      if (userInfo.value) {
        userInfo.value.avatar = response.avatar
      }
      window.alert('头像上传成功')
    }
  } catch (error: any) {
    console.error('Avatar upload error:', error)
    window.alert(error?.message || '头像上传失败')
  } finally {
    isUploading.value = false
    target.value = ''
  }
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<template>
  <div class="user-page">
    <div class="container">
      <div class="user-header">
        <div class="avatar-wrapper">
          <div class="avatar" :style="{ backgroundImage: avatarPreview ? `url(${avatarPreview})` : 'none' }">
            <span v-if="!avatarPreview">{{ userInfo?.nickname?.[0] || userInfo?.username?.[0] || '用' }}</span>
          </div>
          <input
            v-if="editing"
            type="file"
            accept="image/jpeg,image/png,image/gif"
            class="avatar-upload"
            id="avatar-upload-input"
            @change="handleAvatarChange"
          />
          <label v-if="editing" for="avatar-upload-input" class="avatar-upload-label" :class="{ 'uploading': isUploading }">
            <span v-if="!isUploading">更换头像</span>
            <span v-else>上传中...</span>
          </label>
        </div>
        <div class="user-info" v-if="userInfo">
          <div class="user-name-row">
            <template v-if="!editing">
              <span class="user-name">{{ userInfo.nickname || userInfo.username }}</span>
              <button class="edit-btn" @click="editing = true">编辑资料</button>
            </template>
            <template v-else>
              <button class="save-btn" @click="handleSave">保存</button>
              <button class="cancel-btn" @click="handleCancel">取消</button>
            </template>
          </div>
          <div class="user-phone">{{ userInfo.phone }}</div>
        </div>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>

      <div v-if="editing" class="edit-section">
        <h3 class="section-title">编辑个人资料</h3>
        <div class="form-group">
          <label>昵称</label>
          <input
            v-model="editForm.nickname"
            type="text"
            placeholder="请输入昵称"
            :class="{ 'error': errors.nickname }"
            maxlength="20"
          />
          <span v-if="errors.nickname" class="error-message">{{ errors.nickname }}</span>
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input
            v-model="editForm.phone"
            type="tel"
            placeholder="请输入手机号（选填）"
            :class="{ 'error': errors.phone }"
            maxlength="11"
          />
          <span v-if="errors.phone" class="error-message">{{ errors.phone }}</span>
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input
            v-model="editForm.email"
            type="email"
            placeholder="请输入邮箱（选填）"
            :class="{ 'error': errors.email }"
          />
          <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
        </div>
      </div>

      <div class="menu-section">
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="menu-item"
          @click="router.push(item.path)"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span class="menu-label">{{ item.label }}</span>
          <span class="menu-arrow">›</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-page {
  padding: 32px 0;
  background: #F8F9FA;
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 90%;
  margin: 0 auto;
  padding: 0 16px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  background: linear-gradient(135deg, #E67E22 0%, #d35400 100%);
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 24px;
  color: #fff;
}

.avatar-wrapper {
  position: relative;
}

.avatar {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.avatar-upload {
  display: none;
}

.avatar-upload-label {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 11px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.3s;
}

.avatar-upload-label:hover {
  background: rgba(0, 0, 0, 0.8);
}

.avatar-upload-label.uploading {
  background: #E67E22;
}

.user-info {
  flex: 1;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
}

.edit-btn,
.save-btn,
.cancel-btn {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.3s;
}

.edit-btn:hover,
.save-btn:hover,
.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.cancel-btn {
  background: transparent;
}

.user-phone {
  font-size: 14px;
  opacity: 0.9;
}

.logout-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.edit-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2C3E50;
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #E67E22;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
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
}

.menu-section {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 18px 24px;
  cursor: pointer;
  transition: background 0.3s;
  border-bottom: 1px solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #F8F9FA;
}

.menu-icon {
  font-size: 20px;
  margin-right: 16px;
}

.menu-label {
  flex: 1;
  font-size: 15px;
  color: #2C3E50;
}

.menu-arrow {
  font-size: 20px;
  color: #b0b0b0;
}

@media (max-width: 768px) {
  .user-page {
    padding: 16px 0;
  }

  .user-header {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }

  .avatar {
    width: 64px;
    height: 64px;
    font-size: 24px;
  }

  .user-info {
    width: 100%;
  }

  .user-name-row {
    justify-content: center;
  }

  .logout-btn {
    margin-top: 12px;
  }

  .menu-item {
    padding: 14px 16px;
  }

  .menu-icon {
    font-size: 18px;
    margin-right: 12px;
  }

  .menu-label {
    font-size: 14px;
  }
}
</style>
