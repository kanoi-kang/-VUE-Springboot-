<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import {
  DataLine,
  Goods,
  FolderOpened,
  OfficeBuilding,
  ShoppingCartFull,
  Tickets,
  User,
  SwitchButton,
  Fold,
  Expand,
  HomeFilled,
  Setting,
  Comment,
  Money
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

const collapsed = ref(false)

const menuItems = [
  { path: '/dashboards', name: '仪表盘', icon: DataLine },
  { path: '/products', name: '商品管理', icon: Goods },
  { path: '/brands', name: '品牌管理', icon: OfficeBuilding },
  { path: '/orders', name: '订单管理', icon: ShoppingCartFull },
  { path: '/coupons', name: '优惠券管理', icon: Tickets },
  { path: '/refunds', name: '退款管理', icon: Money },
  { path: '/users', name: '用户管理', icon: User },
  { path: '/reviews', name: '评价管理', icon: Comment },
]

const currentPath = computed(() => route.path)

const goTo = (path: string) => {
  router.push(path)
}

const logout = () => {
  adminStore.logout()
  router.push('/login')
}

const isActive = (path: string) => {
  if (path === '/') {
    return currentPath.value === '/'
  }
  return currentPath.value.startsWith(path)
}
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed }">
      <div class="sidebar-header">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <HomeFilled />
          </div>
          <transition name="fade">
            <span v-if="!collapsed" class="logo-text">文旅管理后台</span>
          </transition>
        </div>
      </div>

      <nav class="sidebar-menu">
        <ul>
          <li
            v-for="item in menuItems"
            :key="item.path"
            :class="{ active: isActive(item.path) }"
            @click="goTo(item.path)"
          >
            <div class="menu-icon">
              <component :is="item.icon" />
            </div>
            <transition name="fade">
              <span v-if="!collapsed" class="menu-text">{{ item.name }}</span>
            </transition>
          </li>
        </ul>
      </nav>

      <div class="sidebar-footer">
        <div class="collapse-trigger" @click="collapsed = !collapsed">
          <component :is="collapsed ? Expand : Fold" />
          <transition name="fade">
            <span v-if="!collapsed">收起</span>
          </transition>
        </div>
      </div>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="header-left">
          <h2 class="page-title">
            {{ menuItems.find(m => isActive(m.path))?.name || '首页' }}
          </h2>
        </div>

        <div class="header-right">
          <div class="admin-info">
            <div class="admin-avatar">
              <Setting />
            </div>
            <span class="admin-name">{{ adminStore.adminInfo?.username || '管理员' }}</span>
          </div>
          <button class="logout-btn" @click="logout">
            <SwitchButton />
            <span>退出登录</span>
          </button>
        </div>
      </header>

      <div class="content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-menu {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto;
}

.sidebar-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar-menu li {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 20px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  margin: 4px 12px;
  border-radius: 8px;
}

.sidebar-menu li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 3px 3px 0;
  transition: height 0.3s;
}

.sidebar-menu li:hover {
  background: rgba(255, 255, 255, 0.08);
}

.sidebar-menu li.active {
  background: rgba(102, 126, 234, 0.2);
}

.sidebar-menu li.active::before {
  height: 24px;
}

.menu-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
  opacity: 0.85;
}

.sidebar-menu li.active .menu-icon {
  opacity: 1;
}

.menu-text {
  font-size: 14px;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.collapse-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.3s;
  font-size: 13px;
  opacity: 0.7;
}

.collapse-trigger :deep(svg) {
  width: 14px;
  height: 14px;
}

.collapse-trigger:hover {
  background: rgba(255, 255, 255, 0.1);
  opacity: 1;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  height: 64px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.admin-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}

.admin-name {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: 1px solid #e8e8e8;
  color: #666;
  cursor: pointer;
  font-size: 13px;
  padding: 6px 14px;
  border-radius: 6px;
  transition: all 0.3s;
}

.logout-btn :deep(svg) {
  width: 14px;
  height: 14px;
}

.logout-btn:hover {
  border-color: #f56c6c;
  color: #f56c6c;
  background: #fef0f0;
}

.content-wrapper {
  flex: 1;
  padding: 24px;
  overflow: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
  }

  .sidebar.collapsed {
    width: 0;
    overflow: hidden;
  }
}
</style>