export interface Product {
  id: number
  name: string
  description: string
  price: number
  originalPrice?: number
  images: string[]
  categoryId: number
  brandId: number
  stock: number
  sales: number
  status: string
  createdAt: string
}

export interface Category {
  id: number
  name: string
  parentId: number
  children?: Category[]
}

export interface Brand {
  id: number
  name: string
  logo: string
}

export interface Order {
  id: number
  orderNo: string
  status: string
  statusText: string
  totalAmount: number
  payAmount: number
  createdAt: string
}

export interface OrderDetail extends Order {
  items: OrderItem[]
  address: Address
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
}

export interface Address {
  id: number
  name: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
}

export interface Coupon {
  id: number
  name: string
  discountType: string
  discountValue: number
  minAmount: number
  endTime: string
  status: string
}

export interface User {
  id: number
  username: string
  email: string
  phone: string
  avatar: string
  status: boolean
  createdAt: string
}
