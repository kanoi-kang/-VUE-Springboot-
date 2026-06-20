import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<Record<string, unknown> | null>(null)

  const login = async (username: string, password: string, captcha: string) => {
    const data = await apiLogin({ username, password, captcha })
    token.value = data?.token || ''
    if (token.value) {
      localStorage.setItem('token', token.value)
    }
    return data
  }

  const fetchUserInfo = async () => {
    if (!token.value) return null
    const data = await getUserInfo()
    userInfo.value = data
    return data
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return { token, userInfo, login, fetchUserInfo, logout }
})
