export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

export interface BackendUser {
  id: string
  name: string
  sex: number
  avatar?: string | null
  delFlag?: number
}

export interface BackendBusiness {
  id: number
  name: string
  address?: string | null
  description?: string | null
  image?: string | null
  orderTypeId: number
  startPrice?: number | string | null
  deliveryPrice?: number | string | null
  remark?: string | null
}

export interface BackendFood {
  id: number
  name: string
  description?: string | null
  image?: string | null
  price: number | string
  businessId: number
  remark?: string | null
}

export interface BackendCartItem {
  id: number
  userId: string
  businessId: number
  foodId: number
  quantity: number
  business?: BackendBusiness | null
  food?: BackendFood | null
}

export interface BackendDeliveryAddress {
  id: number
  contactName: string
  contactSex: number
  contactTel: string
  address: string
  userId: string
}

export interface BackendOrderItem {
  id: number
  orderId: number
  foodId: number
  quantity: number
  food?: BackendFood | null
}

export interface BackendOrder {
  id: number
  userId: string
  businessId: number
  orderDate: string
  orderTotal: number | string
  addressId: number
  orderStatus: number
  business?: BackendBusiness | null
  deliveryAddress?: BackendDeliveryAddress | null
  items?: BackendOrderItem[] | null
}

export interface UserCreatePayload {
  userId: string
  password: string
  userName: string
  userSex: number
  userImg?: string | null
}

export interface LoginPayload {
  userId: string
  password: string
}

export interface CartCreatePayload {
  businessId: number
  foodId: number
  quantity?: number
}

export interface DeliveryAddressPayload {
  contactName: string
  contactSex: number
  contactTel: string
  address: string
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

function buildUrl(path: string, query?: Record<string, string | number | null | undefined>) {
  const origin = typeof window === 'undefined' ? 'http://localhost' : window.location.origin
  const url = new URL(`${API_BASE_URL}${path}`, origin)

  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value))
    }
  })

  if (API_BASE_URL) {
    return url.toString()
  }

  return `${url.pathname}${url.search}`
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  query?: Record<string, string | number | null | undefined>,
): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(buildUrl(path, query), {
    ...options,
    headers,
  })
  const text = await response.text()
  const payload = text ? (JSON.parse(text) as Partial<ApiResult<T>>) : null

  if (!response.ok || payload?.code !== 1) {
    throw new ApiError(payload?.msg || response.statusText || '请求失败', response.status, payload?.code, payload?.data)
  }

  return payload.data as T
}

function encodePath(value: string | number) {
  return encodeURIComponent(String(value))
}

export const elemeApi = {
  getUser(userId: string) {
    return request<BackendUser>(`/api/users/${encodePath(userId)}`)
  },
  createUser(payload: UserCreatePayload) {
    return request<BackendUser>('/api/users', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  createSession(payload: LoginPayload) {
    return request<BackendUser>('/api/sessions', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  listBusinesses(orderTypeId?: number | null) {
    return request<BackendBusiness[]>('/api/businesses', {}, { orderTypeId })
  },
  getBusiness(businessId: string | number) {
    return request<BackendBusiness>(`/api/businesses/${encodePath(businessId)}`)
  },
  listFoods(businessId: string | number) {
    return request<BackendFood[]>(`/api/businesses/${encodePath(businessId)}/foods`)
  },
  listCartItems(userId: string, businessId?: string | number | null) {
    return request<BackendCartItem[]>(`/api/users/${encodePath(userId)}/cart-items`, {}, { businessId })
  },
  upsertCartItem(userId: string, payload: CartCreatePayload) {
    return request<BackendCartItem>(`/api/users/${encodePath(userId)}/cart-items`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  updateCartItem(userId: string, cartId: string | number, quantity: number) {
    return request<BackendCartItem>(`/api/users/${encodePath(userId)}/cart-items/${encodePath(cartId)}`, {
      method: 'PATCH',
      body: JSON.stringify({ quantity }),
    })
  },
  deleteCartItem(userId: string, cartId: string | number) {
    return request<void>(`/api/users/${encodePath(userId)}/cart-items/${encodePath(cartId)}`, {
      method: 'DELETE',
    })
  },
  clearCart(userId: string, businessId?: string | number | null) {
    return request<void>(`/api/users/${encodePath(userId)}/cart-items`, {
      method: 'DELETE',
    }, { businessId })
  },
  listOrders(userId: string, filters?: { businessId?: string | number | null; orderState?: number | null }) {
    return request<BackendOrder[]>(`/api/users/${encodePath(userId)}/orders`, {}, filters)
  },
  createOrder(userId: string, payload: { businessId: number; daId: number }) {
    return request<BackendOrder>(`/api/users/${encodePath(userId)}/orders`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  getOrder(userId: string, orderId: string | number) {
    return request<BackendOrder>(`/api/users/${encodePath(userId)}/orders/${encodePath(orderId)}`)
  },
  payOrder(userId: string, orderId: string | number) {
    return request<BackendOrder>(`/api/users/${encodePath(userId)}/orders/${encodePath(orderId)}/payments`, {
      method: 'POST',
    })
  },
  listDeliveryAddresses(userId: string) {
    return request<BackendDeliveryAddress[]>(`/api/users/${encodePath(userId)}/delivery-addresses`)
  },
  createDeliveryAddress(userId: string, payload: DeliveryAddressPayload) {
    return request<BackendDeliveryAddress>(`/api/users/${encodePath(userId)}/delivery-addresses`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  updateDeliveryAddress(userId: string, addressId: string | number, payload: DeliveryAddressPayload) {
    return request<BackendDeliveryAddress>(`/api/users/${encodePath(userId)}/delivery-addresses/${encodePath(addressId)}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
}
