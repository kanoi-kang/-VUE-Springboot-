import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const loading = ref(false)
  const isMobile = ref(false)

  const setLoading = (value: boolean) => {
    loading.value = value
  }

  const setIsMobile = (value: boolean) => {
    isMobile.value = value
  }

  return { loading, isMobile, setLoading, setIsMobile }
})
