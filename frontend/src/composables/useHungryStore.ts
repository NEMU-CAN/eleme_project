import { computed, reactive } from 'vue'
import {
  clearAuthToken,
  elemeApi,
  setAuthToken,
  type BackendBusiness,
  type BackendCartItemVO,
  type BackendDeliveryAddress,
  type BackendFood,
  type BackendOrder,
  type BackendOrderDetail,
  type BackendTaste,
  type BackendUser,
  ApiError,
  type BusinessListQuery,
  type DeliveryAddressSaveRequest,
  type OrderListQuery,
  type OrderStatusRequest,
  type UserCreateRequest,
  type UserUpdateRequest,
} from '@/api/eleme'
import type {
  Address,
  BusinessStatus,
  CartLine,
  CheckoutDraft,
  FoodStatus,
  GenderType,
  MenuItem,
  MenuSection,
  Merchant,
  OrderRecord,
  OrderStatus,
  PaymentMethod,
  TasteItem,
  UserProfile,
} from '@/types'

const SESSION_KEY = 'tju-hungry-session-v3'
const DEFAULT_PAYMENT_METHOD: PaymentMethod = 'alipay'
const FALLBACK_AVATAR = '/eleme/userImg/userImg.png'
const FALLBACK_MERCHANT_IMAGE = '/eleme/sj01.png'
const FALLBACK_FOOD_IMAGE = '/eleme/sp01.png'

interface LoadingState {
  bootstrap: boolean
  tastes: boolean
  businesses: boolean
  merchant: boolean
  cart: boolean
  orders: boolean
  addresses: boolean
  session: boolean
  action: boolean
}

interface PersistedSession {
  token: string
  user: UserProfile | null
  activeMerchantId: string
  addressId: string
  paymentMethod: PaymentMethod
}

interface HungryState extends PersistedSession {
  bootstrapped: boolean
  tastes: TasteItem[]
  merchants: Merchant[]
  foodsByMerchantId: Record<string, MenuItem[]>
  cartItems: CartLine[]
  orders: OrderRecord[]
  addresses: Address[]
  checkoutDraft: CheckoutDraft | null
  loading: LoadingState
  error: string
}

function toNumber(value: unknown, fallback = 0) {
  const numberValue = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(numberValue) ? numberValue : fallback
}

function normalizeGender(value: unknown): GenderType {
  return value === 1 || value === 2 ? value : 0
}

function normalizeBusinessStatus(value: unknown): BusinessStatus {
  if (value === 1) {
    return 'open'
  }
  if (value === 0) {
    return 'closed'
  }
  return 'deleted'
}

function normalizeFoodStatus(value: unknown): FoodStatus {
  return value === 1 ? 'online' : 'offline'
}

function availableStock(stock: unknown, reservedStock: unknown) {
  return Math.max(0, toNumber(stock, 0) - toNumber(reservedStock, 0))
}

function normalizeOrderStatus(value: unknown): OrderStatus {
  if (value === 1) {
    return 'paid'
  }
  if (value === 2) {
    return 'completed'
  }
  if (value === -1) {
    return 'canceled'
  }
  return 'unpaid'
}

function normalizeStoredUser(value: unknown): UserProfile | null {
  if (!value || typeof value !== 'object') {
    return null
  }

  const user = value as Partial<UserProfile>
  const id = typeof user.id === 'string'
    ? user.id
    : typeof user.id === 'number'
      ? String(user.id)
      : ''
  if (!id) {
    return null
  }

  const nickname = typeof user.nickname === 'string' && user.nickname.trim()
    ? user.nickname.trim()
    : typeof user.name === 'string' && user.name.trim()
      ? user.name.trim()
      : id

  return {
    id,
    nickname,
    name: typeof user.name === 'string' && user.name.trim() ? user.name.trim() : nickname,
    phone: typeof user.phone === 'string' && user.phone.trim() ? user.phone.trim() : id,
    avatar: typeof user.avatar === 'string' && user.avatar ? user.avatar : FALLBACK_AVATAR,
    gender: normalizeGender(user.gender),
    role: toNumber(user.role, 0),
    status: toNumber(user.status, 0),
    token: typeof user.token === 'string' ? user.token : null,
  }
}

function readSession(): PersistedSession {
  const fallback: PersistedSession = {
    token: '',
    user: null,
    activeMerchantId: '',
    addressId: '',
    paymentMethod: DEFAULT_PAYMENT_METHOD,
  }

  if (typeof window === 'undefined') {
    return fallback
  }

  try {
    const raw = window.localStorage.getItem(SESSION_KEY)
    if (!raw) {
      return fallback
    }

    const parsed = JSON.parse(raw) as Partial<PersistedSession>
    const token = typeof parsed.token === 'string' ? parsed.token.trim() : ''

    return {
      token,
      user: token ? normalizeStoredUser(parsed.user) : null,
      activeMerchantId: typeof parsed.activeMerchantId === 'string' ? parsed.activeMerchantId : '',
      addressId: typeof parsed.addressId === 'string' ? parsed.addressId : '',
      paymentMethod: parsed.paymentMethod === 'wechat' ? 'wechat' : DEFAULT_PAYMENT_METHOD,
    }
  } catch {
    return fallback
  }
}

const initialSession = readSession()
setAuthToken(initialSession.token)

const state = reactive<HungryState>({
  bootstrapped: false,
  token: initialSession.token,
  user: initialSession.user,
  activeMerchantId: initialSession.activeMerchantId,
  addressId: initialSession.addressId,
  paymentMethod: initialSession.paymentMethod,
  tastes: [],
  merchants: [],
  foodsByMerchantId: {},
  cartItems: [],
  orders: [],
  addresses: [],
  checkoutDraft: null,
  loading: {
    bootstrap: false,
    tastes: false,
    businesses: false,
    merchant: false,
    cart: false,
    orders: false,
    addresses: false,
    session: false,
    action: false,
  },
  error: '',
})

let bootstrapPromise: Promise<void> | null = null

function replaceArray<T>(target: T[], values: T[]) {
  target.splice(0, target.length, ...values)
}

function persistSession() {
  if (typeof window === 'undefined') {
    return
  }

  const payload: PersistedSession = {
    token: state.token,
    user: state.token && state.user ? { ...state.user } : null,
    activeMerchantId: state.activeMerchantId,
    addressId: state.addressId,
    paymentMethod: state.paymentMethod,
  }

  try {
    window.localStorage.setItem(SESSION_KEY, JSON.stringify(payload))
  } catch {
    // 本地存储不可写时，依然保持内存态可用。
  }
  setAuthToken(state.token)
}

function clearProtectedData(options: { keepUser?: boolean } = {}) {
  state.checkoutDraft = null
  state.activeMerchantId = ''
  state.addressId = ''
  replaceArray(state.addresses, [])
  replaceArray(state.orders, [])
  replaceArray(state.cartItems, [])
  if (!options.keepUser) {
    state.user = null
  }
  persistSession()
}

function resetSession(options: { keepUser?: boolean } = {}) {
  state.token = ''
  clearAuthToken()
  clearProtectedData(options)
}

function normalizeImage(value: unknown, fallback: string) {
  const source = typeof value === 'string' ? value.trim() : ''
  if (!source) {
    return fallback
  }

  if (/^(data:image\/|https?:\/\/|\/)/i.test(source)) {
    return source
  }

  if (source.length > 100 && /^[A-Za-z0-9+/=\s]+$/.test(source)) {
    return `data:image/png;base64,${source.replace(/\s/g, '')}`
  }

  return source
}

function numberedAsset(prefix: 'sj' | 'sp' | 'dcfl', id: string | number, max: number) {
  const value = Number(id)
  const index = Number.isFinite(value) ? ((((Math.trunc(value) - 1) % max) + max) % max) + 1 : 1
  return `/eleme/${prefix}${String(index).padStart(2, '0')}.png`
}

function buildMenuSections(items: MenuItem[]): MenuSection[] {
  if (!items.length) {
    return []
  }

  return [
    {
      id: 'all',
      title: '全部商品',
      items,
    },
  ]
}

function mapUser(user: BackendUser): UserProfile {
  const nickname = user.nickname?.trim() || user.phone
  return {
    id: String(user.id),
    nickname,
    name: nickname,
    phone: user.phone,
    avatar: normalizeImage(user.avatar, FALLBACK_AVATAR),
    gender: normalizeGender(user.gender),
    role: toNumber(user.role, 0),
    status: toNumber(user.status, 0),
    token: typeof user.token === 'string' ? user.token : null,
  }
}

function mapTaste(taste: BackendTaste): TasteItem {
  return {
    id: String(taste.id),
    name: taste.name,
    image: numberedAsset('dcfl', taste.id, 10),
    route: `/businesses?tasteId=${taste.id}`,
  }
}

function getTasteName(tasteId: number) {
  return state.tastes.find((item) => Number(item.id) === tasteId)?.name ?? `口味 ${tasteId}`
}

function mapFood(food: BackendFood): MenuItem {
  const id = String(food.id)
  return {
    id,
    businessId: String(food.businessId),
    name: food.name || `商品 ${id}`,
    description: food.description ?? '',
    price: toNumber(food.price, 0),
    image: normalizeImage(food.image, numberedAsset('sp', id, 12) || FALLBACK_FOOD_IMAGE),
    stock: availableStock(food.stock, food.reservedStock),
    reservedStock: Math.max(0, toNumber(food.reservedStock, 0)),
    status: normalizeFoodStatus(food.status),
  }
}

function mapBusiness(business: BackendBusiness, foods: MenuItem[] = []): Merchant {
  const id = String(business.id)
  const startPrice = toNumber(business.startPrice, 0)
  const deliveryFee = toNumber(business.deliveryPrice, 0)
  const merchant: Merchant = {
    id,
    name: business.name || `商家 ${id}`,
    image: normalizeImage(business.image, numberedAsset('sj', id, 9) || FALLBACK_MERCHANT_IMAGE),
    address: business.address || '',
    description: business.description ?? '',
    tasteId: toNumber(business.tasteId, 0),
    tasteName: getTasteName(toNumber(business.tasteId, 0)),
    startPrice,
    deliveryFee,
    status: normalizeBusinessStatus(business.status),
    menuSections: buildMenuSections(foods),
    orderTypeId: toNumber(business.tasteId, 0),
    minOrder: startPrice,
    remark: business.description ?? '',
  }

  return merchant
}

function mapAddress(address: BackendDeliveryAddress): Address {
  const gender = normalizeGender(address.contactGender)
  return {
    id: String(address.id),
    userId: String(address.userId),
    address: address.address,
    contactName: address.contactName,
    contactTel: address.contactTel,
    contactGender: gender,
    isDeleted: Boolean(address.isDeleted),
    name: address.contactName,
    phone: address.contactTel,
    detail: address.address,
    sex: gender,
  }
}

function mapCartItem(item: BackendCartItemVO): CartLine {
  const foodId = String(item.food?.id ?? item.cart.foodId)
  const businessId = String(item.business?.id ?? item.cart.businessId)
  return {
    id: foodId,
    cartId: String(item.cart.id),
    foodId,
    businessId,
    name: item.food?.name || `商品 ${foodId}`,
    price: toNumber(item.food?.price, 0),
    image: normalizeImage(item.food?.image, numberedAsset('sp', foodId, 12) || FALLBACK_FOOD_IMAGE),
    quantity: Math.max(1, toNumber(item.cart.quantity, 1)),
    stock: availableStock(item.food?.stock, item.food?.reservedStock),
    reservedStock: Math.max(0, toNumber(item.food?.reservedStock, 0)),
    status: normalizeFoodStatus(item.food?.status),
  }
}

function normalizeOrderDate(value: string | undefined) {
  if (!value) {
    return new Date().toISOString()
  }

  return value.includes('T') ? value : value.replace(' ', 'T')
}

function mapOrderDetailLine(
  detail: BackendOrderDetail,
  merchantId: number,
  food?: { image?: string | null; stock?: number; reservedStock?: number; status?: number | FoodStatus; name?: string } | null,
): CartLine {
  const foodId = String(detail.foodId)
  const price = toNumber(detail.foodPrice, 0)
  const quantity = Math.max(1, toNumber(detail.quantity, 1))
  const status = food?.status === 'online' ? 'online' : food?.status === 'offline' ? 'offline' : normalizeFoodStatus(food?.status)
  return {
    id: foodId,
    cartId: String(detail.id),
    foodId,
    businessId: String(merchantId),
    name: detail.foodName || food?.name || `商品 ${foodId}`,
    price,
    image: normalizeImage(food?.image, numberedAsset('sp', foodId, 12) || FALLBACK_FOOD_IMAGE),
    quantity,
    stock: Math.max(0, toNumber(food?.stock, quantity)),
    reservedStock: Math.max(0, toNumber(food?.reservedStock, 0)),
    status,
    subtotal: toNumber(detail.subtotal, price * quantity),
  }
}

function mapOrder(
  order: BackendOrder,
  options: {
    business?: BackendBusiness | null
    deliveryAddress?: BackendDeliveryAddress | null
    details?: BackendOrderDetail[] | null
    itemCount?: number
  } = {},
): OrderRecord {
  const merchantId = toNumber(order.businessId, 0)
  const business = options.business
  const deliveryAddress = options.deliveryAddress
  const details = options.details ?? []
  const items = details.map((detail) => mapOrderDetailLine(detail, merchantId, findFoodById(merchantId, detail.foodId)))
  const subtotal = toNumber(
    order.totalAmount,
    items.reduce((total, item) => total + (item.subtotal ?? item.price * item.quantity), 0),
  )
  const deliveryFee = toNumber(order.deliveryPrice, 0)
  const total = toNumber(order.actualAmount, subtotal + deliveryFee)
  const status = normalizeOrderStatus(order.orderStatus)
  const createdAt = normalizeOrderDate(order.orderDate)
  const merchantImage = normalizeImage(business?.image, numberedAsset('sj', merchantId, 9) || FALLBACK_MERCHANT_IMAGE)
  const detailAddress = deliveryAddress ?? null

  return {
    id: String(order.id),
    orderNo: order.orderNo,
    userId: String(order.userId),
    userNickname: order.userNickname,
    userPhone: order.userPhone,
    businessId: String(order.businessId),
    businessName: order.businessName,
    businessAddress: order.businessAddress,
    merchantName: business?.name || order.businessName,
    merchantImage,
    receiverName: order.receiverName,
    receiverTel: order.receiverTel,
    receiverGender: normalizeGender(order.receiverGender),
    receiverAddress: order.receiverAddress,
    addressId: String(order.deliveryAddressId),
    addressName: detailAddress?.contactName || order.receiverName,
    addressPhone: detailAddress?.contactTel || order.receiverTel,
    addressDetail: detailAddress?.address || order.receiverAddress,
    status,
    orderStatus: toNumber(order.orderStatus, 0),
    itemCount: options.itemCount ?? items.length,
    items,
    deliveryFee,
    subtotal,
    total,
    createdAt,
    paidAt: status === 'paid' || status === 'completed' ? createdAt : undefined,
    completedAt: status === 'completed' ? createdAt : undefined,
  }
}

function upsertMerchant(merchant: Merchant) {
  const current = state.merchants.find((item) => item.id === merchant.id)
  if (current) {
    Object.assign(current, {
      ...merchant,
      menuSections: merchant.menuSections.length ? merchant.menuSections : current.menuSections,
    })
    return current
  }

  state.merchants.push(merchant)
  return merchant
}

function upsertCartLine(line: CartLine) {
  const current = state.cartItems.find((item) => item.cartId === line.cartId || item.id === line.id)
  if (current) {
    Object.assign(current, line)
    return current
  }

  state.cartItems.push(line)
  return line
}

function removeCartLine(line: CartLine) {
  const index = state.cartItems.findIndex((item) => item.cartId === line.cartId || item.id === line.id)
  if (index >= 0) {
    state.cartItems.splice(index, 1)
  }
}

function upsertOrder(order: OrderRecord) {
  const current = state.orders.find((item) => item.id === order.id)
  if (current) {
    Object.assign(current, {
      ...order,
      items: order.items.length ? order.items : current.items,
      itemCount: order.itemCount || current.itemCount,
    })
    return current
  }

  state.orders.unshift(order)
  return order
}

function summaryFor(lines: CartLine[], deliveryFee: number) {
  const subtotal = lines.reduce((total, line) => total + (line.subtotal ?? line.price * line.quantity), 0)
  const total = subtotal + deliveryFee
  const quantity = lines.reduce((count, line) => count + line.quantity, 0)

  return { subtotal, total, quantity }
}

function getMerchant(merchantId: string | number) {
  const id = String(merchantId)
  return state.merchants.find((merchant) => merchant.id === id)
}

function getOrder(orderId: string | number) {
  const id = String(orderId)
  return state.orders.find((order) => order.id === id)
}

function cartLinesForMerchant(merchantId: string | number) {
  const id = String(merchantId)
  return state.cartItems.filter((line) => line.businessId === id)
}

const activeMerchant = computed(() => getMerchant(state.activeMerchantId) ?? state.merchants[0] ?? null)
const activeAddress = computed(() => state.addresses.find((address) => address.id === state.addressId) ?? state.addresses[0] ?? null)
const isAuthenticated = computed(() => Boolean(state.token && state.user))
const unpaidOrders = computed(() => state.orders.filter((order) => order.status === 'unpaid').length)
const unreadOrders = unpaidOrders
const completedOrders = computed(() => state.orders.filter((order) => order.status === 'completed').length)
const cartLines = computed(() => (state.activeMerchantId ? cartLinesForMerchant(state.activeMerchantId) : []))
const cartSummary = computed(() => {
  const merchant = activeMerchant.value
  return summaryFor(cartLines.value, merchant?.deliveryFee ?? 0)
})
const tasteCategories = computed(() => state.tastes)

function messageFromError(error: unknown) {
  if (error instanceof ApiError) {
    const details = Array.isArray(error.details)
      ? error.details
          .map((detail) => {
            if (!detail || typeof detail !== 'object') {
              return ''
            }
            const item = detail as { field?: unknown; message?: unknown; reason?: unknown }
            const field = typeof item.field === 'string' ? item.field : ''
            const message = typeof item.message === 'string'
              ? item.message
              : typeof item.reason === 'string'
                ? item.reason
                : ''
            return field && message ? `${field} ${message}` : message
          })
          .filter(Boolean)
          .join('；')
      : ''

    return details ? `${error.message}：${details}` : error.message
  }

  if (error instanceof Error) {
    return error.message
  }

  return '请求失败，请稍后重试'
}

function setError(error: unknown) {
  state.error = messageFromError(error)
  return state.error
}

function isAuthError(error: unknown) {
  return error instanceof ApiError && error.status === 401
}

async function withLoading<T>(
  key: keyof LoadingState,
  task: () => Promise<T>,
  options: { clearOn401?: boolean } = {},
) {
  state.loading[key] = true
  state.error = ''
  try {
    return await task()
  } catch (error) {
    if (options.clearOn401 && isAuthError(error)) {
      resetSession()
    }
    setError(error)
    throw error
  } finally {
    state.loading[key] = false
  }
}

function requireAuthUser() {
  if (!state.token || !state.user) {
    throw new Error('请先登录后再继续操作')
  }

  return state.user
}

function setSessionUser(user: BackendUser) {
  state.user = mapUser(user)
  state.token = (user.token ?? '').trim()
  setAuthToken(state.token)
  persistSession()
}

async function loadTastes() {
  return withLoading('tastes', async () => {
    const data = await elemeApi.listTastes()
    replaceArray(state.tastes, data.map(mapTaste))
    return state.tastes
  })
}

async function ensureTastesLoaded() {
  if (state.tastes.length) {
    return state.tastes
  }

  return loadTastes()
}

async function loadBusinesses(query: BusinessListQuery = {}) {
  return withLoading('businesses', async () => {
    await ensureTastesLoaded().catch(() => undefined)
    const data = await elemeApi.listBusinesses(query)
    const merchants = data.map((item) => {
      const id = String(item.id)
      const foods = state.foodsByMerchantId[id] ?? []
      return mapBusiness(item, foods)
    })
    replaceArray(state.merchants, merchants)

    if (!state.activeMerchantId && state.merchants[0]) {
      state.activeMerchantId = state.merchants[0].id
      persistSession()
    }

    return state.merchants
  })
}

async function ensureMerchantDetail(merchantId: string | number) {
  return withLoading('merchant', async () => {
    const id = Number(merchantId)
    if (!Number.isInteger(id) || id <= 0) {
      throw new Error('businessId 无效')
    }

    await ensureTastesLoaded().catch(() => undefined)
    const [businessVo, foods] = await Promise.all([
      elemeApi.getBusiness(id),
      elemeApi.listFoods({ businessId: id, status: 1 }),
    ])
    const mappedFoods = foods.map(mapFood)
    state.foodsByMerchantId[String(id)] = mappedFoods
    const merchant = upsertMerchant(mapBusiness(businessVo.business, mappedFoods))
    state.activeMerchantId = merchant.id
    persistSession()

    if (isAuthenticated.value) {
      await loadCart().catch(() => undefined)
    }

    return merchant
  })
}

async function loadCart(query: { businessId?: string | number | null } = {}) {
  requireAuthUser()
  return withLoading('cart', async () => {
    const data = await elemeApi.listCartItems({
      businessId: query.businessId ? toNumber(query.businessId, 0) : undefined,
    })
    const lines = data.map(mapCartItem)
    replaceArray(state.cartItems, lines)

    if (!state.activeMerchantId && lines[0]) {
      state.activeMerchantId = lines[0].businessId
    }

    persistSession()
    return state.cartItems
  }, { clearOn401: true })
}

async function loadOrders(query: OrderListQuery = {}) {
  requireAuthUser()
  return withLoading('orders', async () => {
    const data = await elemeApi.listOrders({
      page: query.page ?? 1,
      pageSize: query.pageSize ?? 100,
      businessId: query.businessId ?? undefined,
      orderStatus: query.orderStatus ?? undefined,
    })

    const orders = data.records.map((record) =>
      mapOrder(record.order, {
        business: record.business,
        deliveryAddress: record.deliveryAddress,
        itemCount: record.itemCount,
      }),
    )
    replaceArray(state.orders, orders)
    return state.orders
  }, { clearOn401: true })
}

async function loadAddresses() {
  requireAuthUser()
  return withLoading('addresses', async () => {
    const data = await elemeApi.listAddresses()
    const addresses = data.map(mapAddress)
    replaceArray(state.addresses, addresses)

    if (!state.addressId || !state.addresses.some((address) => address.id === state.addressId)) {
      state.addressId = state.addresses[0]?.id ?? ''
      persistSession()
    }

    return state.addresses
  }, { clearOn401: true })
}

async function refreshProtectedData() {
  if (!isAuthenticated.value) {
    return
  }

  const results = await Promise.allSettled([loadAddresses(), loadOrders(), loadCart()])
  const failed = results.find((result): result is PromiseRejectedResult => result.status === 'rejected')
  if (failed && !state.error) {
    setError(failed.reason)
  }
}

async function loadSessionData() {
  requireAuthUser()
  return withLoading('session', async () => {
    const user = await elemeApi.getCurrentUser()
    state.user = mapUser(user)
    persistSession()
    await refreshProtectedData()
    return state.user
  }, { clearOn401: true })
}

async function authenticateSession(user: BackendUser) {
  if (!user.token) {
    throw new Error('登录失败，未返回令牌')
  }

  state.token = ''
  clearAuthToken()
  clearProtectedData()
  setSessionUser(user)
  await refreshProtectedData()
  return state.user
}

async function performLogin(phone: string, password: string) {
  const user = await elemeApi.login({ phone, password })
  return authenticateSession(user)
}

async function login(phone: string, password: string) {
  return withLoading('session', async () => performLogin(phone, password))
}

async function register(payload: UserCreateRequest) {
  return withLoading('session', async () => {
    await elemeApi.register(payload)
    return performLogin(payload.phone, payload.password)
  })
}

async function logout() {
  return withLoading('session', async () => {
    try {
      if (state.token) {
        await elemeApi.logout()
      }
    } catch {
      // 退出时以本地失效为准，后端请求失败也继续清理本地会话。
    } finally {
      state.user = null
      state.token = ''
      clearAuthToken()
      clearProtectedData({ keepUser: false })
    }
  })
}

async function updateCurrentUser(payload: UserUpdateRequest) {
  requireAuthUser()
  return withLoading('session', async () => {
    const user = mapUser(await elemeApi.updateCurrentUser(payload))
    state.user = user
    state.token = ''
    clearAuthToken()
    clearProtectedData({ keepUser: true })
    persistSession()
    return user
  }, { clearOn401: true })
}

async function createAddress(payload: DeliveryAddressSaveRequest) {
  requireAuthUser()
  return withLoading('addresses', async () => {
    const address = mapAddress(await elemeApi.createAddress(payload))
    const current = state.addresses.find((item) => item.id === address.id)
    if (current) {
      Object.assign(current, address)
    } else {
      state.addresses.push(address)
    }

    if (!state.addressId) {
      state.addressId = address.id
    }
    persistSession()
    return address
  }, { clearOn401: true })
}

async function updateAddress(addressId: string | number, payload: DeliveryAddressSaveRequest) {
  requireAuthUser()
  return withLoading('addresses', async () => {
    const address = mapAddress(await elemeApi.updateAddress(addressId, payload))
    const current = state.addresses.find((item) => item.id === address.id)
    if (current) {
      Object.assign(current, address)
    } else {
      state.addresses.push(address)
    }

    if (!state.addressId) {
      state.addressId = address.id
    }
    persistSession()
    return address
  }, { clearOn401: true })
}

async function removeAddress(addressId: string | number) {
  requireAuthUser()
  return withLoading('addresses', async () => {
    await elemeApi.removeAddress(addressId)
    const id = String(addressId)
    const index = state.addresses.findIndex((item) => item.id === id)
    if (index >= 0) {
      state.addresses.splice(index, 1)
    }

    if (state.addressId === id) {
      state.addressId = state.addresses[0]?.id ?? ''
    }
    persistSession()
  }, { clearOn401: true })
}

function setActiveMerchant(merchantId: string | number) {
  state.activeMerchantId = String(merchantId)
  persistSession()
}

function setAddress(addressId: string | number) {
  state.addressId = String(addressId)
  persistSession()
}

function setPaymentMethod(method: PaymentMethod) {
  state.paymentMethod = method
  if (state.checkoutDraft) {
    state.checkoutDraft.paymentMethod = method
  }
  persistSession()
}

async function addToCart(merchantId: string | number, item: MenuItem) {
  requireAuthUser()
  return withLoading('action', async () => {
    const line = mapCartItem(await elemeApi.addCartItem({
      businessId: toNumber(merchantId, 0),
      foodId: toNumber(item.id, 0),
      quantity: 1,
    }))
    upsertCartLine(line)
    state.activeMerchantId = String(merchantId)
    persistSession()
    return line
  }, { clearOn401: true })
}

async function removeFromCart(merchantId: string | number, itemId: string | number) {
  requireAuthUser()
  const line = cartLinesForMerchant(merchantId).find((item) => item.foodId === String(itemId))
  if (!line) {
    return null
  }

  return withLoading('action', async () => {
    if (line.quantity > 1) {
      const updated = mapCartItem(await elemeApi.updateCartItem(line.foodId, {
        quantity: line.quantity - 1,
      }))
      upsertCartLine(updated)
      return updated
    }

    await elemeApi.removeCartItem(line.foodId)
    removeCartLine(line)
    return null
  }, { clearOn401: true })
}

async function clearCart(merchantId?: string | number | null) {
  requireAuthUser()
  return withLoading('action', async () => {
    await elemeApi.clearCart({
      businessId: merchantId ? toNumber(merchantId, 0) : undefined,
    })
    if (merchantId) {
      replaceArray(state.cartItems, state.cartItems.filter((line) => line.businessId !== String(merchantId)))
    } else {
      replaceArray(state.cartItems, [])
    }
    persistSession()
  }, { clearOn401: true })
}

async function prepareCheckout(merchantId: string | number) {
  requireAuthUser()
  const merchant = getMerchant(merchantId)
  const lines = cartLinesForMerchant(merchantId)

  if (!merchant || !lines.length) {
    throw new Error('购物车为空，无法创建订单')
  }

  if (!state.addresses.length) {
    await loadAddresses()
  }

  const address = activeAddress.value
  if (!address) {
    throw new Error('请先添加收货地址')
  }

  return withLoading('action', async () => {
    const detail = await elemeApi.createOrder({
      businessId: toNumber(merchantId, 0),
      deliveryAddressId: toNumber(address.id, 0),
    })
    const order = mapOrder(detail.order, {
      business: merchant ? {
        id: Number(merchant.id),
        name: merchant.name,
        address: merchant.address,
        description: merchant.description,
        image: merchant.image,
        tasteId: merchant.tasteId,
        startPrice: merchant.startPrice,
        deliveryPrice: merchant.deliveryFee,
        status: merchant.status === 'open' ? 1 : merchant.status === 'closed' ? 0 : -1,
      } : null,
      deliveryAddress: detail.deliveryAddress,
      details: detail.details,
      itemCount: detail.details.length,
    })
    upsertOrder(order)
    replaceArray(state.cartItems, state.cartItems.filter((line) => line.businessId !== String(merchantId)))
    state.checkoutDraft = {
      businessId: String(merchantId),
      paymentMethod: state.paymentMethod,
      addressId: address.id,
      createdOrderId: order.id,
    }
    persistSession()
    return order.id
  }, { clearOn401: true })
}

async function fetchOrder(orderId: string | number) {
  requireAuthUser()
  return withLoading('orders', async () => {
    const detail = await elemeApi.getOrder(orderId)
    const order = mapOrder(detail.order, {
      deliveryAddress: detail.deliveryAddress,
      details: detail.details,
      itemCount: detail.details.length,
    })
    upsertOrder(order)
    return order
  }, { clearOn401: true })
}

async function confirmPayment(orderId: string | number, method?: PaymentMethod) {
  requireAuthUser()
  if (method) {
    setPaymentMethod(method)
  }

  return withLoading('action', async () => {
    await elemeApi.updateOrderStatus(orderId, {
      orderStatus: 1,
    } as OrderStatusRequest)
    const order = await fetchOrder(orderId)
    order.status = 'paid'
    order.orderStatus = 1
    if (state.checkoutDraft?.createdOrderId === String(orderId)) {
      state.checkoutDraft = null
    }
    persistSession()
    return order
  }, { clearOn401: true })
}

async function cancelOrder(orderId: string | number) {
  requireAuthUser()
  return withLoading('action', async () => {
    await elemeApi.updateOrderStatus(orderId, {
      orderStatus: -1,
    })
    const order = await fetchOrder(orderId)
    order.status = 'canceled'
    order.orderStatus = -1
    persistSession()
    return order
  }, { clearOn401: true })
}

function checkoutSummary(merchantId: string | number) {
  const merchant = getMerchant(merchantId)
  const lines = cartLinesForMerchant(merchantId)
  const fee = merchant?.deliveryFee ?? 0
  const { subtotal, total, quantity } = summaryFor(lines, fee)

  return {
    merchant,
    lines,
    subtotal,
    fee,
    total,
    count: quantity,
  }
}

function cartCanCheckout(merchantId: string | number) {
  const merchant = getMerchant(merchantId)
  if (!merchant || merchant.status !== 'open') {
    return false
  }

  const { subtotal } = summaryFor(cartLinesForMerchant(merchantId), merchant.deliveryFee)
  return subtotal >= (merchant.startPrice ?? merchant.minOrder ?? 0)
}

function findFoodById(businessId: string | number, foodId: string | number) {
  const merchantId = String(businessId)
  const targetFoodId = String(foodId)
  return state.foodsByMerchantId[merchantId]?.find((item) => item.id === targetFoodId)
    ?? state.merchants
      .find((merchant) => merchant.id === merchantId)
      ?.menuSections.flatMap((section) => section.items)
      .find((item) => item.id === targetFoodId)
    ?? null
}

async function initialize() {
  if (state.bootstrapped) {
    return
  }

  if (!bootstrapPromise) {
    bootstrapPromise = (async () => {
      state.loading.bootstrap = true
      state.error = ''
      try {
        await loadTastes().catch(() => undefined)
        await loadBusinesses().catch(() => undefined)

        if (state.token) {
          try {
            const user = await elemeApi.getCurrentUser()
            state.user = mapUser(user)
            persistSession()
            await refreshProtectedData()
          } catch (error) {
            if (isAuthError(error)) {
              resetSession()
            }
            if (!state.error) {
              setError(error)
            }
          }
        }
      } finally {
        state.bootstrapped = true
        state.loading.bootstrap = false
      }
    })().finally(() => {
      bootstrapPromise = null
    })
  }

  return bootstrapPromise
}

export function useHungryStore() {
  return {
    state,
    merchants: state.merchants,
    tastes: state.tastes,
    tasteCategories,
    addresses: state.addresses,
    activeMerchant,
    activeAddress,
    cartLines,
    cartSummary,
    unpaidOrders,
    unreadOrders,
    completedOrders,
    isAuthenticated,
    initialize,
    loadTastes,
    loadBusinesses,
    ensureMerchantDetail,
    loadCart,
    loadOrders,
    loadAddresses,
    loadSessionData,
    login,
    register,
    logout,
    updateCurrentUser,
    createAddress,
    updateAddress,
    removeAddress,
    getMerchant,
    getOrder,
    fetchOrder,
    cartLinesForMerchant,
    setActiveMerchant,
    setAddress,
    setPaymentMethod,
    addToCart,
    removeFromCart,
    clearCart,
    prepareCheckout,
    confirmPayment,
    cancelOrder,
    checkoutSummary,
    cartCanCheckout,
    messageFromError,
    findFoodById,
  }
}
