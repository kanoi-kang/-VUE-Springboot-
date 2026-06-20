import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminLogin } from '@/api/admin'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref<Record<string, unknown> | null>(null)

  const login = async (username: string, password: string, captcha: string) => {
    const res = await adminLogin({ username, password, captcha })
    console.log('Login response:', res)
    if (res.code === 200 && res.data?.token) {
      token.value = res.data.token
      localStorage.setItem('admin_token', res.data.token)
      adminInfo.value = res.data.admin || {}
      console.log('Token saved:', res.data.token)
      return true
    }
    console.log('Login failed, code:', res.code)
    return false
  }

  const logout = () => {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('admin_token')
  }

  return { token, adminInfo, login, logout }
})