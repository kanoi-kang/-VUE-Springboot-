import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CartItem } from '@/types'
import { getCartItems, addCartItem, updateCartItem, removeCartItem, clearCart } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return items.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
  })

  const loadCart = async () => {
    const data = await getCartItems()
    items.value = data?.items || data || []
  }

  const addItem = async (productId: number, quantity: number) => {
    await addCartItem({ productId, quantity })
    await loadCart()
  }

  const updateItem = async (id: number, quantity: number) => {
    await updateCartItem(id, { quantity })
    await loadCart()
  }

  const removeItem = async (id: number) => {
    await removeCartItem(id)
    await loadCart()
  }

  const clearAll = async () => {
    await clearCart()
    items.value = []
  }

  return { items, totalCount, totalPrice, loadCart, addItem, updateItem, removeItem, clearAll }
})
