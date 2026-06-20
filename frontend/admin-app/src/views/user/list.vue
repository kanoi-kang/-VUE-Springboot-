<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUsers, updateUserStatus, deleteUser } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const users = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

const loadUsers = async () => {
  try {
    loading.value = true
    const res: any = await getUsers({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value
    })
    users.value = res?.data?.records || []
    total.value = res?.data?.total || 0
  } catch (error) {
    console.error('Failed to load users:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadUsers()
}

const handleStatusFilter = () => {
  page.value = 1
  loadUsers()
}

const handlePageChange = (newPage: number) => {
  page.value = newPage
  loadUsers()
}

const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  page.value = 1
  loadUsers()
}

const handleViewUser = (id: number) => {
  router.push(`/users/${id}`)
}

const handleToggleStatus = async (user: any) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
      type: 'warning'
    })
    await updateUserStatus(user.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleDeleteUser = async (user: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${user.username || user.nickname}"吗？此操作不可恢复。`, '警告', {
      type: 'warning'
    })
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getStatusText = (status: number) => {
  return status === 1 ? '正常' : '禁用'
}

const getStatusClass = (status: number) => {
  return status === 1 ? 'status-active' : 'status-disabled'
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <div class="user-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户管理</h2>
        <span class="total-count">共 {{ total }} 位用户</span>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索用户名/邮箱..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        <select v-model="statusFilter" class="filter-select" @change="handleStatusFilter">
          <option :value="undefined">全部状态</option>
          <option :value="1">正常</option>
          <option :value="0">禁用</option>
        </select>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table" v-loading="loading">
        <thead>
          <tr>
            <th class="col-id">ID</th>
            <th class="col-user">用户信息</th>
            <th class="col-phone">手机号</th>
            <th class="col-status">状态</th>
            <th class="col-time">注册时间</th>
            <th class="col-actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="users.length === 0 && !loading">
            <td colspan="6" class="empty-cell">
              <div class="empty-state">
                <span class="empty-icon">👤</span>
                <p>暂无用户数据</p>
              </div>
            </td>
          </tr>
          <tr v-for="user in users" :key="user.id">
            <td class="col-id">
              <span class="id-text">{{ user.id }}</span>
            </td>
            <td class="col-user">
              <div class="user-info">
                <div class="user-avatar-wrapper">
                  <img v-if="user.avatar" :src="user.avatar" class="user-avatar-img" alt="头像" @error="(e: any) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'flex' }" />
                  <div v-else class="user-avatar">{{ (user.username || user.nickname || '?').charAt(0).toUpperCase() }}</div>
                </div>
                <div class="user-detail">
                  <span class="user-name">{{ user.username || user.nickname || '-' }}</span>
                  <span class="user-email" v-if="user.email">{{ user.email }}</span>
                </div>
              </div>
            </td>
            <td class="col-phone">
              <span class="phone">{{ user.phone || '-' }}</span>
            </td>
            <td class="col-status">
              <span class="status-badge" :class="getStatusClass(user.status)">
                {{ getStatusText(user.status) }}
              </span>
            </td>
            <td class="col-time">
              <span class="time">{{ formatDate(user.createTime) }}</span>
            </td>
            <td class="col-actions">
              <button class="action-btn view" @click="handleViewUser(user.id)">查看</button>
              <button
                class="action-btn"
                :class="user.status === 1 ? 'disable' : 'enable'"
                @click="handleToggleStatus(user)"
              >
                {{ user.status === 1 ? '禁用' : '启用' }}
              </button>
              <button class="action-btn delete" @click="handleDeleteUser(user)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.user-list {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.total-count {
  font-size: 14px;
  color: #999;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}

.search-box input {
  padding: 8px 12px;
  border: none;
  outline: none;
  font-size: 14px;
  width: 180px;
}

.search-box .search-btn {
  padding: 8px 16px;
  background: #E67E22;
  color: #fff;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-box .search-btn:hover {
  background: #d35400;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 14px 16px;
  background: #fafafa;
  font-weight: 500;
  font-size: 14px;
  color: #666;
  border-bottom: 1px solid #f0f0f0;
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  vertical-align: middle;
}

.data-table tbody tr {
  transition: background 0.2s;
}

.data-table tbody tr:hover {
  background: #fafafa;
}

.col-id {
  width: 80px;
}

.col-user {
  min-width: 200px;
}

.col-phone {
  width: 140px;
}

.col-status {
  width: 80px;
}

.col-time {
  width: 120px;
}

.col-actions {
  width: 220px;
}

.id-text {
  font-family: monospace;
  font-size: 13px;
  color: #999;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-wrapper {
  position: relative;
  width: 40px;
  height: 40px;
}

.user-avatar-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-avatar {
  position: absolute;
  top: 0;
  left: 0;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.user-email {
  font-size: 12px;
  color: #999;
}

.phone {
  font-size: 14px;
  color: #666;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #f6ffed;
  color: #52c41a;
}

.status-disabled {
  background: #f5f5f5;
  color: #999;
}

.time {
  font-size: 13px;
  color: #666;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  margin-right: 6px;
}

.action-btn.view {
  background: #e8f4ff;
  color: #1890ff;
}

.action-btn.view:hover {
  background: #cce8ff;
}

.action-btn.enable {
  background: #f6ffed;
  color: #52c41a;
}

.action-btn.enable:hover {
  background: #d9f7be;
}

.action-btn.disable {
  background: #fff2f0;
  color: #f5576c;
}

.action-btn.disable:hover {
  background: #ffccc7;
}

.action-btn.delete {
  background: #f5f5f5;
  color: #999;
}

.action-btn.delete:hover {
  background: #ffccc7;
  color: #f5576c;
}

.empty-cell {
  padding: 60px 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.5;
}

.empty-state p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .search-box {
    flex: 1;
  }

  .search-box input {
    width: 100%;
  }
}
</style>