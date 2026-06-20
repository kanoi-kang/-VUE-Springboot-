export interface User {
  id: number
  username: string
  email: string
  phone: string
  avatar: string
  createdAt: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  phone: string
  email: string
  avatar: string
}

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
  content?: string
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

export interface CartItem {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  pic?: string
  specName?: string
}

export interface Address {
  id: number
  name?: string
  consignee?: string
  phone: string
  province: string
  city: string
  district: string
  detail?: string
  detailAddress?: string
  isDefault: boolean | number
  postalCode?: string
  tag?: string
}

export interface Order {
  id: number
  orderNo: string
  status: number
  statusText: string
  totalAmount: number
  payAmount: number
  address: Address
  items: OrderItem[]
  createdAt: string
  userId: number
  consignee: string
  phone: string
  province: string
  city: string
  district: string
  detailAddress: string
  freightAmount?: number
  discountAmount?: number
  couponId?: number
  remark?: string
  trackingNo?: string
}

export interface OrderDetail extends Order {
  shippingNo?: string
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  stock?: number
  specName?: string
  pic?: string
}

export interface Coupon {
  id: number
  name: string
  description?: string
  discountType: string
  discountValue: number
  minAmount: number
  maxDiscount?: number
  amount?: number
  startTime: string
  endTime: string
  status: string
  used?: boolean
  couponId?: number
  perLimit?: number
  totalCount?: number
  remainCount?: number
}

export interface Review {
  id: number
  productId: number
  productName?: string
  rating: number
  content: string
  picUrls?: string | string[]
  images?: string[]
  createTime?: string
  createdAt?: string
  username?: string
  nickname?: string
}

export interface Collection {
  id: number
  productId: number
  productName: string
  productPic: string
  productPrice: number
  createTime: string
}
