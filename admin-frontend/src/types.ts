export interface ApiEnvelope<T> {
  code: number
  msg: string
  data: T
}

export interface User {
  id: number
  nickname: string
  phone: string
  avatar: string | null
  gender: number
  role: number
  status: number
  token?: string | null
}

export interface Business {
  id: number
  name: string
  address: string
  description: string | null
  image: string | null
  tasteId: number
  startPrice: number
  deliveryPrice: number
  status: number
}

export interface Taste {
  id: number
  name: string
}

export interface Food {
  id: number
  name: string
  description: string | null
  image: string | null
  price: number
  businessId: number
  stock: number
  reservedStock: number
  status: number
}

export interface BusinessDetail {
  business: Business
  foods: Food[]
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  businessId: number
  userNickname: string
  userPhone: string
  businessName: string
  businessAddress: string
  receiverName: string
  receiverTel: string
  receiverGender: number
  receiverAddress: string
  orderDate: string
  deliveryPrice: number
  totalAmount: number
  actualAmount: number
  deliveryAddressId: number
  orderStatus: number
}

export interface DeliveryAddress {
  id: number
  userId: number
  address: string
  contactName: string
  contactTel: string
  contactGender: number
}

export interface OrderSummary {
  order: Order
  business: Business
  deliveryAddress: DeliveryAddress
  itemCount: number
}

export interface OrderDetailItem {
  id: number
  orderId: number
  foodId: number
  quantity: number
  foodName: string
  foodPrice: number
  subtotal: number
}

export interface OrderDetail {
  order: Order
  deliveryAddress: DeliveryAddress
  details: OrderDetailItem[]
}

export interface PageResult<T> {
  total: number
  page: number
  pageSize: number
  records: T[]
}
