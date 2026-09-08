export type PaymentMethod = 'alipay' | 'wechat'
export type OrderStatus = 'canceled' | 'unpaid' | 'paid' | 'completed'
export type BusinessStatus = 'deleted' | 'closed' | 'open'
export type FoodStatus = 'offline' | 'online'
export type GenderType = 0 | 1 | 2

export interface CategoryItem {
  id: string
  name: string
  image: string
  route: string
}

export interface TasteItem extends CategoryItem {}

export interface MenuItem {
  id: string
  businessId: string
  name: string
  description: string
  price: number
  image: string
  stock: number
  reservedStock: number
  status: FoodStatus
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
  tasteId: number
  tasteName: string
  startPrice: number
  deliveryFee: number
  status: BusinessStatus
  menuSections: MenuSection[]
  orderTypeId?: number
  minOrder?: number
  remark?: string
}

export interface Address {
  id: string
  userId: string
  address: string
  contactName: string
  contactTel: string
  contactGender: GenderType
  isDeleted: boolean
  name: string
  phone: string
  detail: string
  sex: GenderType
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
  stock: number
  reservedStock: number
  status: FoodStatus
  subtotal?: number
}

export interface OrderRecord {
  id: string
  orderNo: string
  userId: string
  userNickname: string
  userPhone: string
  businessId: string
  businessName: string
  businessAddress: string
  merchantName: string
  merchantImage: string
  receiverName: string
  receiverTel: string
  receiverGender: GenderType
  receiverAddress: string
  addressId: string
  addressName: string
  addressPhone: string
  addressDetail: string
  status: OrderStatus
  orderStatus: number
  itemCount: number
  items: CartLine[]
  deliveryFee: number
  subtotal: number
  total: number
  createdAt: string
  paidAt?: string
  completedAt?: string
}

export interface UserProfile {
  id: string
  nickname: string
  name: string
  phone: string
  avatar: string
  gender: GenderType
  role: number
  status: number
  token?: string | null
}

export interface CheckoutDraft {
  businessId: string
  paymentMethod: PaymentMethod
  addressId: string
  createdOrderId: string | null
}
