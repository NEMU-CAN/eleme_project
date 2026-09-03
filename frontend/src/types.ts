export type PaymentMethod = 'alipay' | 'wechat'
export type OrderStatus = 'pending' | 'paid'

export interface CategoryItem {
  id: string
  name: string
  image: string
  route: string
}

export interface MenuItem {
  id: string
  name: string
  description: string
  price: number
  image: string
  tag?: string
}

export interface MenuSection {
  id: string
  title: string
  items: MenuItem[]
}

export interface Merchant {
  id: string
  name: string
  image: string
  rating: number
  monthlySales: number
  minOrder: number
  deliveryFee: number
  distanceKm: number
  etaMinutes: number
  cuisine: string
  highlight: string
  tags: string[]
  banner: string
  promotions: string[]
  menuSections: MenuSection[]
}

export interface Address {
  id: string
  name: string
  phone: string
  detail: string
  note?: string
}

export interface CartLine {
  id: string
  name: string
  price: number
  image: string
  quantity: number
}

export interface OrderRecord {
  id: string
  merchantId: string
  merchantName: string
  merchantImage: string
  status: OrderStatus
  paymentMethod: PaymentMethod
  addressId: string
  addressName: string
  addressPhone: string
  addressDetail: string
  items: CartLine[]
  deliveryFee: number
  subtotal: number
  total: number
  createdAt: string
  paidAt?: string
}

export interface UserProfile {
  name: string
  phone: string
  gender: 'male' | 'female'
  avatar: string
}

export interface CheckoutDraft {
  merchantId: string
  paymentMethod: PaymentMethod
  addressId: string
  createdOrderId: string | null
}
