<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ShoppingCart, User, Search, HomeFilled, Menu, Close, UserFilled } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const showSearch = ref(false)
const searchKeyword = ref('')
const mobileMenuOpen = ref(false)
const showMobileNav = ref(false)
const showUserMenu = ref(false)

onMounted(() => {
  if (userStore.token) {
    userStore.fetchUserInfo()
  }
})

const currentPath = computed(() => route.path)

const goTo = (path: string) => {
  router.push(path)
  mobileMenuOpen.value = false
  showMobileNav.value = false
  showUserMenu.value = false
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push(`/products?keyword=${searchKeyword.value}`)
    searchKeyword.value = ''
    showSearch.value = false
  }
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}

const toggleMobileNav = () => {
  showMobileNav.value = !showMobileNav.value
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const isActive = (path: string) => {
  if (path === '/') return currentPath.value === '/'
  return currentPath.value.startsWith(path)
}
</script>

<template>
  <div class="app-wrapper">

    <header class="header">
      <nav class="navbar">
        <div class="container navbar-container">
          <div class="navbar-left">
            <div class="navbar-brand" @click="goTo('/')">
              <ElIcon class="brand-icon"><HomeFilled /></ElIcon>
              <span class="brand-text">新疆文旅商城</span>
            </div>

            <ul class="nav-list">
              <li :class="{ active: isActive('/') }" @click="goTo('/')">
                <span>首页</span>
              </li>
              <li :class="{ active: isActive('/products') }" @click="goTo('/products')">
                <span>全部商品</span>
              </li>
              <li :class="{ active: isActive('/about') }" @click="goTo('/about')">
                <span>关于我们</span>
              </li>
            </ul>
          </div>

          <button class="navbar-toggler" @click="toggleMobileNav">
            <ElIcon v-if="!showMobileNav"><Menu /></ElIcon>
            <ElIcon v-else><Close /></ElIcon>
          </button>

          <div class="navbar-right">
            <button class="action-btn search-btn" @click="showSearch = !showSearch">
              <ElIcon><Search /></ElIcon>
            </button>
            <button class="action-btn cart-btn" @click="goTo('/cart')">
              <ElIcon><ShoppingCart /></ElIcon>
              <span v-if="cartStore.totalCount > 0" class="cart-badge">{{ cartStore.totalCount }}</span>
            </button>
            <button v-if="!userStore.token" class="action-btn login-btn" @click="goTo('/login')">
              <ElIcon><User /></ElIcon>
              <span>登录</span>
            </button>
            <div v-else class="user-menu-wrapper">
              <button class="user-profile-btn" @click="toggleUserMenu">
                <div class="user-avatar">
                  <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar as string" alt="头像" />
                  <span v-else>{{ (userStore.userInfo?.nickname as string || userStore.userInfo?.username as string)?.charAt(0) || '用' }}</span>
                </div>
                <span class="user-name">{{ userStore.userInfo?.nickname as string || userStore.userInfo?.username as string || '用户' }}</span>
              </button>
              <div v-if="showUserMenu" class="user-dropdown-menu">
                <span class="dropdown-item" @click="goTo('/user')">个人中心</span>
                <span class="dropdown-item" @click="goTo('/orders')">我的订单</span>
                <span class="dropdown-item" @click="goTo('/coupons')">我的优惠券</span>
                <span class="dropdown-item" @click="goTo('/collections')">我的收藏</span>
                <div class="dropdown-divider"></div>
                <span class="dropdown-item logout-item" @click="logout">退出登录</span>
              </div>
            </div>
          </div>

          <div class="navbar-menu mobile-only" :class="{ 'mobile-open': showMobileNav }">
            <ul class="nav-list">
              <li :class="{ active: isActive('/') }" @click="goTo('/')">
                <span>首页</span>
              </li>
              <li :class="{ active: isActive('/products') }" @click="goTo('/products')">
                <span>全部商品</span>
              </li>
              <li :class="{ active: isActive('/about') }" @click="goTo('/about')">
                <span>关于我们</span>
              </li>
            </ul>
          </div>
        </div>
      </nav>

      <div class="search-bar" :class="{ 'show': showSearch }">
        <div class="container">
          <div class="search-input-wrapper">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索商品..."
              @keyup.enter="handleSearch"
            />
            <button @click="handleSearch">
              <ElIcon><Search /></ElIcon>
            </button>
          </div>
        </div>
      </div>
    </header>

    <main class="main-content">
      <RouterView />
    </main>

    <footer class="footer">
      <div class="container">
        <p>© 2026 新疆文旅商城 - 传承新疆特色文化</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F8F9FA;
}

.top-bar {
  background: #2C3E50;
  color: #fff;
  padding: 8px 0;
  font-size: 13px;
}

.top-bar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text {
  opacity: 0.9;
}

.top-bar-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fff;
  text-decoration: none;
  transition: opacity 0.3s;
}

.top-link:hover {
  opacity: 0.8;
}

.user-icon {
  font-size: 14px;
}

.divider {
  color: rgba(255, 255, 255, 0.4);
}

.logout-btn {
  color: #e74c3c;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logout-btn:hover {
  opacity: 0.8;
}

.header {
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar {
  background-color: #E67E22 !important;
}

.navbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 0;
  padding-bottom: 0;
  max-width: none;
  padding-left: 20px;
  padding-right: 20px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  padding: 12px 0;
}

.brand-icon {
  font-size: 24px;
}

.brand-text {
  font-size: 18px;
  font-weight: bold;
}

.navbar-toggler {
  display: none;
  background: none;
  border: none;
  color: #fff;
  font-size: 24px;
  padding: 8px;
  cursor: pointer;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.navbar-menu {
  display: none;
}

.navbar-menu.mobile-only {
  display: none;
}

.nav-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 8px;
}

.nav-list li {
  padding: 16px 16px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.85);
  font-size: 15px;
  transition: all 0.3s;
  position: relative;
}

.nav-list li::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 3px;
  background: #fff;
  transition: all 0.3s;
  transform: translateX(-50%);
}

.nav-list li:hover,
.nav-list li.active {
  color: #fff;
}

.nav-list li:hover::after,
.nav-list li.active::after {
  width: 100%;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.search-btn {
  padding: 8px;
}

.cart-btn {
  position: relative;
}

.action-btn :deep(.el-icon) {
  font-size: 16px;
}

.cart-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 16px;
  height: 16px;
  background: #e74c3c;
  color: #fff;
  font-size: 10px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.login-btn {
  background: rgba(255, 255, 255, 0.95);
  color: #E67E22;
  border-color: transparent;
}

.login-btn:hover {
  background: #fff;
}

.user-menu-wrapper {
  position: relative;
}

.user-profile-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  background: transparent;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.user-profile-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 14px;
}

.user-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 160px;
  padding: 8px 0;
  z-index: 100;
}

.dropdown-item {
  display: block;
  padding: 10px 16px;
  color: #333;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s;
  cursor: pointer;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.dropdown-divider {
  height: 1px;
  background: #eee;
  margin: 8px 0;
}

.logout-item {
  color: #e74c3c;
}

.search-bar {
  display: none;
  background: #fff;
  padding: 12px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-bar.show {
  display: block;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  max-width: 500px;
  margin: 0 auto;
  border: 2px solid #E67E22;
  border-radius: 4px;
  overflow: hidden;
}

.search-input-wrapper input {
  flex: 1;
  height: 40px;
  padding: 0 16px;
  border: none;
  outline: none;
  font-size: 14px;
}

.search-input-wrapper button {
  width: 50px;
  height: 40px;
  background: #E67E22;
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-content {
  flex: 1;
}

.footer {
  background: #2C3E50;
  color: #fff;
  padding: 20px 0;
  text-align: center;
}

.footer p {
  margin: 0;
  font-size: 14px;
}

@media (max-width: 992px) {
  .top-bar {
    display: none;
  }

  .navbar-toggler {
    display: block;
  }

  .navbar-left {
    display: none;
  }

  .navbar-right {
    display: none;
  }

  .navbar-menu.mobile-only {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: #E67E22;
    flex-direction: column;
    margin-left: 0;
    padding: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .navbar-menu.mobile-open {
    display: flex;
  }

  .nav-list {
    flex-direction: column;
    width: 100%;
    gap: 0;
  }

  .nav-list li {
    padding: 12px 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .nav-list li::after {
    display: none;
  }

  .navbar-actions {
    margin-top: 16px;
    width: 100%;
    justify-content: center;
  }
}
</style>
