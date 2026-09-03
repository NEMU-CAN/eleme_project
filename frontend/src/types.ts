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
  businessId: string
  name: string
  description: string
  price: number
  image: string
  remark?: string
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
  address: string
  description: string
  orderTypeId: number
  minOrder: number
  deliveryFee: number
  remark?: string
  menuSections: MenuSection[]
}

export interface Address {
  id: string
  name: string
  phone: string
  detail: string
  sex: 0 | 1
  userId: string
}

export interface CartLine {
  id: string
  cartId: string
  foodId: string
  businessId: string
  name: string
  price: number
  image: string
  quantity: number
}

export interface OrderRecord {
  id: string
  userId: string
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
  id: string
  name: string
  phone: string
  gender: 'male' | 'female'
  sex: 0 | 1
  avatar: string
  delFlag?: number
}

export interface CheckoutDraft {
  merchantId: string
  paymentMethod: PaymentMethod
  addressId: string
  createdOrderId: string | null
}
