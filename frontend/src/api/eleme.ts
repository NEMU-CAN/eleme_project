export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

export interface BackendTaste {
  id: number
  name: string
}

export interface BackendUser {
  id: number
  nickname: string
  phone: string
  avatar?: string | null
  gender: number
  role: number
  status: number
  token?: string | null
}

export interface BackendBusiness {
  id: number
  name: string
  address: string
  description?: string | null
  image?: string | null
  tasteId: number
  startPrice?: number | string | null
  deliveryPrice?: number | string | null
  status: number
}

export interface BackendFood {
  id: number
  name: string
  description?: string | null
  image?: string | null
  price: number | string
  businessId: number
  stock: number
  reservedStock: number
  status: number
}

export interface BackendBusinessVO {
  business: BackendBusiness
  foods: BackendFood[]
}

export interface BackendCart {
  id: number
  userId: number
  businessId: number
  foodId: number
  quantity: number
}

export interface BackendCartItemVO {
  cart: BackendCart
  business: BackendBusiness
  food: BackendFood
}

export interface BackendDeliveryAddress {
  id: number
  userId: number
  address: string
  contactName: string
  contactTel: string
  contactGender: number
  isDeleted?: number | boolean | null
}

export interface BackendOrder {
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
  deliveryPrice: number | string
  totalAmount: number | string
  actualAmount: number | string
  deliveryAddressId: number
  orderStatus: number
}

export interface BackendOrderDetail {
  id: number
  orderId: number
  foodId: number
  quantity: number
  foodName: string
  foodPrice: number | string
  subtotal: number | string
}

export interface BackendOrderDetailVO {
  order: BackendOrder
  deliveryAddress: BackendDeliveryAddress
  details: BackendOrderDetail[]
}

export interface BackendOrderSummaryVO {
  order: BackendOrder
  business: BackendBusiness
  deliveryAddress: BackendDeliveryAddress
  itemCount: number
}

export interface BackendPageResult<T> {
  total: number
  page: number
  pageSize: number
  records: T[]
}

export interface LoginRequest {
  phone: string
  password: string
}

export interface UserCreateRequest {
  phone: string
  password: string
  nickname: string
  gender: number
  avatar?: string | null
}

export interface UserUpdateRequest {
  nickname?: string | null
  phone?: string | null
  avatar?: string | null
  gender?: number | null
}

export interface BusinessSaveRequest {
  name: string
  address: string
  description?: string | null
  image?: string | null
  tasteId: number
  startPrice?: number | string | null
  deliveryPrice?: number | string | null
  status?: number | null
}

export interface BusinessStatusRequest {
  status: number
}

export interface FoodSaveRequest {
  name: string
  description?: string | null
  image?: string | null
  price: number | string
  businessId: number
  stock?: number | null
  status?: number | null
}

export interface FoodStatusRequest {
  status: number
}

export interface CartItemSaveRequest {
  businessId: number
  foodId: number
  quantity?: number
}

export interface CartItemUpdateRequest {
  quantity: number
}

export interface DeliveryAddressSaveRequest {
  address: string
  contactName: string
  contactTel: string
  contactGender?: number | null
}

export interface OrderCreateRequest {
  businessId: number
  deliveryAddressId: number
}

export interface OrderStatusRequest {
  orderStatus: number
}

export interface BusinessListQuery {
  tasteId?: number | null
  status?: number | null
  keyword?: string | null
}

export interface FoodListQuery {
  businessId?: number | null
  status?: number | null
  keyword?: string | null
}

export interface CartListQuery {
  businessId?: number | null
}

export interface OrderListQuery {
  businessId?: number | null
  orderStatus?: number | null
  page?: number | null
  pageSize?: number | null
}

const AUTH_TOKEN_KEY = 'tju-hungry-auth-token-v1'
let authToken = readStoredToken()

function readStoredToken() {
  if (typeof window === 'undefined') {
    return ''
  }

  try {
    return window.localStorage.getItem(AUTH_TOKEN_KEY) ?? ''
  } catch {
    return ''
  }
}

export function getAuthToken() {
  return authToken
}

export function setAuthToken(token?: string | null) {
  authToken = token?.trim() ?? ''

  if (typeof window === 'undefined') {
    return
  }

  try {
    if (authToken) {
      window.localStorage.setItem(AUTH_TOKEN_KEY, authToken)
    } else {
      window.localStorage.removeItem(AUTH_TOKEN_KEY)
    }
  } catch {
    // 本地存储不可用时，退回到内存态即可。
  }
}

export function clearAuthToken() {
  setAuthToken('')
}

export class ApiError extends Error {
  readonly status: number
  readonly code?: number
  readonly details?: unknown

  constructor(message: string, status: number, code?: number, details?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.details = details
  }
}

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')

function buildUrl(path: string, query?: object) {
  const origin = typeof window === 'undefined' ? 'http://localhost' : window.location.origin
  const url = new URL(`${API_BASE_URL}${path}`, origin)
  const queryEntries = (query ?? {}) as Record<string, string | number | null | undefined>

  Object.entries(queryEntries).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })

  if (API_BASE_URL) {
    return url.toString()
  }

  return `${url.pathname}${url.search}`
}

function parseResult<T>(text: string, response: Response): ApiResult<T> | null {
  if (!text.trim()) {
    return null
  }

  try {
    return JSON.parse(text) as ApiResult<T>
  } catch {
    throw new ApiError('服务端返回了无法解析的内容', response.status)
  }
}

async function request<T, Q extends object = object>(
  path: string,
  options: RequestInit = {},
  query?: Q,
): Promise<T> {
  const headers = new Headers(options.headers)
  if (authToken) {
    headers.set('Authorization', `Bearer ${authToken}`)
  }
  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(buildUrl(path, query as object | undefined), {
    ...options,
    headers,
  })
  const text = await response.text()
  const payload = parseResult<T>(text, response)

  if (!response.ok || !payload || payload.code !== 1) {
    throw new ApiError(payload?.msg || response.statusText || '请求失败', response.status, payload?.code, payload?.data)
  }

  return payload.data as T
}

function encodePath(value: string | number) {
  return encodeURIComponent(String(value))
}

export const elemeApi = {
  login(payload: LoginRequest) {
    return request<BackendUser>('/api/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  logout() {
    return request<void>('/api/logout', {
      method: 'DELETE',
    })
  },
  getCurrentUser() {
    return request<BackendUser>('/api/users')
  },
  register(payload: UserCreateRequest) {
    return request<BackendUser>('/api/users', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  updateCurrentUser(payload: UserUpdateRequest) {
    return request<BackendUser>('/api/users', {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  listBusinesses(query: BusinessListQuery = {}) {
    return request<BackendBusiness[]>('/api/businesses', {}, query)
  },
  getBusiness(businessId: string | number) {
    return request<BackendBusinessVO>(`/api/businesses/${encodePath(businessId)}`)
  },
  createBusiness(payload: BusinessSaveRequest) {
    return request<BackendBusinessVO>('/api/businesses', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  updateBusiness(businessId: string | number, payload: BusinessSaveRequest) {
    return request<BackendBusinessVO>(`/api/businesses/${encodePath(businessId)}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  updateBusinessStatus(businessId: string | number, payload: BusinessStatusRequest) {
    return request<void>(`/api/businesses/${encodePath(businessId)}/status`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  listFoods(query: FoodListQuery = {}) {
    return request<BackendFood[]>('/api/foods', {}, query)
  },
  getFood(foodId: string | number) {
    return request<BackendFood>(`/api/foods/${encodePath(foodId)}`)
  },
  createFood(payload: FoodSaveRequest) {
    return request<BackendFood>('/api/foods', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  updateFood(foodId: string | number, payload: FoodSaveRequest) {
    return request<BackendFood>(`/api/foods/${encodePath(foodId)}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  updateFoodStatus(foodId: string | number, payload: FoodStatusRequest) {
    return request<void>(`/api/foods/${encodePath(foodId)}/status`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  listTastes() {
    return request<BackendTaste[]>('/api/tastes')
  },
  listCartItems(query: CartListQuery = {}) {
    return request<BackendCartItemVO[]>('/api/cart/items', {}, query)
  },
  addCartItem(payload: CartItemSaveRequest) {
    return request<BackendCartItemVO>('/api/cart/items', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  updateCartItem(foodId: string | number, payload: CartItemUpdateRequest) {
    return request<BackendCartItemVO>(`/api/cart/items/${encodePath(foodId)}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  removeCartItem(foodId: string | number) {
    return request<void>(`/api/cart/items/${encodePath(foodId)}`, {
      method: 'DELETE',
    })
  },
  clearCart(query: CartListQuery = {}) {
    return request<void>('/api/cart/items', {
      method: 'DELETE',
    }, query)
  },
  createOrder(payload: OrderCreateRequest) {
    return request<BackendOrderDetailVO>('/api/orders', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  listOrders(query: OrderListQuery = {}) {
    const { businessId, orderStatus, page = 1, pageSize = 100 } = query
    return request<BackendPageResult<BackendOrderSummaryVO>>('/api/orders', {}, {
      businessId,
      orderStatus,
      page,
      pageSize,
    })
  },
  getOrder(orderId: string | number) {
    return request<BackendOrderDetailVO>(`/api/orders/${encodePath(orderId)}`)
  },
  updateOrderStatus(orderId: string | number, payload: OrderStatusRequest) {
    return request<void>(`/api/orders/${encodePath(orderId)}/status`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  listAddresses() {
    return request<BackendDeliveryAddress[]>('/api/addresses')
  },
  getAddress(addressId: string | number) {
    return request<BackendDeliveryAddress>(`/api/addresses/${encodePath(addressId)}`)
  },
  createAddress(payload: DeliveryAddressSaveRequest) {
    return request<BackendDeliveryAddress>('/api/addresses', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  updateAddress(addressId: string | number, payload: DeliveryAddressSaveRequest) {
    return request<BackendDeliveryAddress>(`/api/addresses/${encodePath(addressId)}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  removeAddress(addressId: string | number) {
    return request<void>(`/api/addresses/${encodePath(addressId)}`, {
      method: 'DELETE',
    })
  },
}
