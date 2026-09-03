import { computed, reactive } from 'vue'
import { ApiError, elemeApi } from '@/api/eleme'
import type {
  BackendBusiness,
  BackendCartItem,
  BackendDeliveryAddress,
  BackendFood,
  BackendOrder,
  BackendUser,
  DeliveryAddressPayload,
  UserCreatePayload,
} from '@/api/eleme'
import type {
  Address,
  CartLine,
  CheckoutDraft,
  MenuItem,
  Merchant,
  OrderRecord,
  PaymentMethod,
  UserProfile,
} from '@/types'

const SESSION_KEY = 'tju-hungry-session-v2'
const DEFAULT_PAYMENT_METHOD: PaymentMethod = 'alipay'
const FALLBACK_AVATAR = '/eleme/userImg/userImg.png'
const FALLBACK_MERCHANT_IMAGE = '/eleme/sj01.png'
const FALLBACK_FOOD_IMAGE = '/eleme/sp01.png'

interface LoadingState {
  businesses: boolean
  merchant: boolean
  cart: boolean
  orders: boolean
  addresses: boolean
  session: boolean
  action: boolean
}

interface PersistedSession {
  user: UserProfile | null
  activeMerchantId: string
  addressId: string
  paymentMethod: PaymentMethod
}

interface HungryState extends PersistedSession {
  merchants: Merchant[]
  foodsByMerchantId: Record<string, MenuItem[]>
  cartItems: CartLine[]
  orders: OrderRecord[]
  addresses: Address[]
  checkoutDraft: CheckoutDraft | null
  loading: LoadingState
  error: string
}

function normalizeStoredUser(value: unknown): UserProfile | null {
  if (!value || typeof value !== 'object') {
    return null
  }

  const user = value as Partial<UserProfile>
  const id = typeof user.id === 'string' && user.id ? user.id : typeof user.phone === 'string' ? user.phone : ''
  if (!id) {
    return null
  }

  const sex: 0 | 1 = user.sex === 0 || user.gender === 'female' ? 0 : 1
  return {
    id,
    name: typeof user.name === 'string' && user.name ? user.name : id,
    phone: typeof user.phone === 'string' && user.phone ? user.phone : id,
    gender: sex === 0 ? 'female' : 'male',
    sex,
    avatar: typeof user.avatar === 'string' && user.avatar ? user.avatar : FALLBACK_AVATAR,
    delFlag: user.delFlag,
  }
}

function readSession(): PersistedSession {
  const fallback: PersistedSession = {
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
    return {
      user: normalizeStoredUser(parsed.user),
      activeMerchantId: typeof parsed.activeMerchantId === 'string' ? parsed.activeMerchantId : '',
      addressId: typeof parsed.addressId === 'string' ? parsed.addressId : '',
      paymentMethod: parsed.paymentMethod === 'wechat' ? 'wechat' : DEFAULT_PAYMENT_METHOD,
    }
  } catch {
    return fallback
  }
}

const initialSession = readSession()

const state = reactive<HungryState>({
  ...initialSession,
  merchants: [],
  foodsByMerchantId: {},
  cartItems: [],
  orders: [],
  addresses: [],
  checkoutDraft: null,
  loading: {
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

let initializePromise: Promise<void> | null = null

function persistSession() {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(
    SESSION_KEY,
    JSON.stringify({
      user: state.user ? { ...state.user } : null,
      activeMerchantId: state.activeMerchantId,
      addressId: state.addressId,
      paymentMethod: state.paymentMethod,
    } satisfies PersistedSession),
  )
}

function replaceArray<T>(target: T[], values: T[]) {
  target.splice(0, target.length, ...values)
}

function toNumber(value: unknown, fallback = 0) {
  const numberValue = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(numberValue) ? numberValue : fallback
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

function numberedAsset(prefix: 'sj' | 'sp', id: string | number, max: number) {
  const value = Number(id)
  const index = Number.isFinite(value) ? ((((Math.trunc(value) - 1) % max) + max) % max) + 1 : 1
  return `/eleme/${prefix}${String(index).padStart(2, '0')}.png`
}

function numericId(value: string | number, field: string) {
  const id = Number(value)
  if (!Number.isInteger(id) || id <= 0) {
    throw new Error(`${field} 无效`)
  }
  return id
}

function buildMenuSections(items: MenuItem[]) {
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
  const sex: 0 | 1 = user.sex === 0 ? 0 : 1
  return {
    id: user.id,
    name: user.name || user.id,
    phone: user.id,
    gender: sex === 0 ? 'female' : 'male',
    sex,
    avatar: normalizeImage(user.avatar, FALLBACK_AVATAR),
    delFlag: user.delFlag,
  }
}

function mapBusiness(business: BackendBusiness, foods: MenuItem[] = []): Merchant {
  const id = String(business.id)
  return {
    id,
    name: business.name || `商家 ${id}`,
    image: normalizeImage(business.image, numberedAsset('sj', id, 9) || FALLBACK_MERCHANT_IMAGE),
    address: business.address ?? '',
    description: business.description ?? '',
    orderTypeId: toNumber(business.orderTypeId, 0),
    minOrder: toNumber(business.startPrice, 0),
    deliveryFee: toNumber(business.deliveryPrice, 0),
    remark: business.remark ?? '',
    menuSections: buildMenuSections(foods),
  }
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
    remark: food.remark ?? '',
  }
}

function mapAddress(address: BackendDeliveryAddress): Address {
  const sex: 0 | 1 = address.contactSex === 0 ? 0 : 1
  return {
    id: String(address.id),
    name: address.contactName,
    phone: address.contactTel,
    detail: address.address,
    sex,
    userId: address.userId,
  }
}

function mapCartItem(item: BackendCartItem): CartLine {
  const foodId = String(item.foodId || item.food?.id || item.id)
  const businessId = String(item.businessId || item.food?.businessId || item.business?.id || '')
  return {
    id: foodId,
    cartId: String(item.id),
    foodId,
    businessId,
    name: item.food?.name || `商品 ${foodId}`,
    price: toNumber(item.food?.price, 0),
    image: normalizeImage(item.food?.image, numberedAsset('sp', foodId, 12) || FALLBACK_FOOD_IMAGE),
    quantity: Math.max(1, toNumber(item.quantity, 1)),
  }
}

function normalizeOrderDate(value: string | undefined) {
  if (!value) {
    return new Date().toISOString()
  }

  return value.includes('T') ? value : value.replace(' ', 'T')
}

function mapOrder(order: BackendOrder): OrderRecord {
  const merchantId = String(order.businessId || order.business?.id || '')
  const knownMerchant = getMerchant(merchantId)
  const deliveryAddress = order.deliveryAddress
  const items = (order.items ?? []).map((item) => {
    const foodId = String(item.foodId || item.food?.id || item.id)
    return {
      id: foodId,
      cartId: String(item.id),
      foodId,
      businessId: String(item.food?.businessId || merchantId),
      name: item.food?.name || `商品 ${foodId}`,
      price: toNumber(item.food?.price, 0),
      image: normalizeImage(item.food?.image, numberedAsset('sp', foodId, 12) || FALLBACK_FOOD_IMAGE),
      quantity: Math.max(1, toNumber(item.quantity, 1)),
    }
  })
  const subtotal = toNumber(
    order.orderTotal,
    items.reduce((total, item) => total + item.price * item.quantity, 0),
  )
  const deliveryFee = toNumber(order.business?.deliveryPrice ?? knownMerchant?.deliveryFee, 0)
  const status = order.orderStatus === 1 ? 'paid' : 'pending'
  const orderId = String(order.id)

  return {
    id: orderId,
    userId: order.userId,
    merchantId,
    merchantName: order.business?.name || knownMerchant?.name || `商家 ${merchantId}`,
    merchantImage: normalizeImage(order.business?.image ?? knownMerchant?.image, numberedAsset('sj', merchantId, 9) || FALLBACK_MERCHANT_IMAGE),
    status,
    paymentMethod: state.checkoutDraft?.createdOrderId === orderId ? state.checkoutDraft.paymentMethod : state.paymentMethod,
    addressId: String(order.addressId || deliveryAddress?.id || ''),
    addressName: deliveryAddress?.contactName || '',
    addressPhone: deliveryAddress?.contactTel || '',
    addressDetail: deliveryAddress?.address || '',
    items,
    deliveryFee,
    subtotal,
    total: subtotal + deliveryFee,
    createdAt: normalizeOrderDate(order.orderDate),
    paidAt: status === 'paid' ? normalizeOrderDate(order.orderDate) : undefined,
  }
}

export function messageFromError(error: unknown) {
  if (error instanceof ApiError) {
    const details = Array.isArray(error.details)
      ? error.details
          .map((detail) => {
            if (!detail || typeof detail !== 'object') {
              return ''
            }
            const item = detail as { field?: unknown; reason?: unknown }
            const field = typeof item.field === 'string' ? item.field : ''
            const reason = typeof item.reason === 'string' ? item.reason : ''
            return field && reason ? `${field} ${reason}` : reason
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

async function withLoading<T>(key: keyof LoadingState, task: () => Promise<T>) {
  state.loading[key] = true
  state.error = ''
  try {
    return await task()
  } catch (error) {
    setError(error)
    throw error
  } finally {
    state.loading[key] = false
  }
}

function requireUser() {
  if (!state.user) {
    throw new Error('请先登录后再继续操作')
  }

  return state.user
}

function upsertMerchant(merchant: Merchant) {
  const current = state.merchants.find((item) => item.id === merchant.id)
  if (current) {
    Object.assign(current, merchant)
    return current
  }

  state.merchants.push(merchant)
  return merchant
}

function upsertCartLine(line: CartLine) {
  const current = state.cartItems.find((item) => item.cartId === line.cartId)
  if (current) {
    Object.assign(current, line)
    return current
  }

  state.cartItems.push(line)
  return line
}

function removeCartLine(line: CartLine) {
  const index = state.cartItems.findIndex((item) => item.cartId === line.cartId)
  if (index >= 0) {
    state.cartItems.splice(index, 1)
  }
}

function upsertOrder(order: OrderRecord) {
  const current = state.orders.find((item) => item.id === order.id)
  if (current) {
    Object.assign(current, order)
    return current
  }

  state.orders.unshift(order)
  return order
}

function summaryFor(lines: CartLine[], deliveryFee: number) {
  const subtotal = lines.reduce((total, line) => total + line.price * line.quantity, 0)
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
const unreadOrders = computed(() => state.orders.filter((order) => order.status === 'pending').length)
const completedOrders = computed(() => state.orders.filter((order) => order.status === 'paid').length)
const cartLines = computed(() => (state.activeMerchantId ? cartLinesForMerchant(state.activeMerchantId) : []))
const cartSummary = computed(() => {
  const merchant = activeMerchant.value
  return summaryFor(cartLines.value, merchant?.deliveryFee ?? 0)
})

async function initialize() {
  if (!initializePromise) {
    initializePromise = (async () => {
      try {
        await loadBusinesses()
        if (state.user) {
          await loadSessionData()
        }
      } catch (error) {
        setError(error)
      }
    })()
  }

  return initializePromise
}

async function loadBusinesses(orderTypeId?: number | null) {
  return withLoading('businesses', async () => {
    const data = await elemeApi.listBusinesses(orderTypeId)
    const merchants = data.map((item) => {
      const id = String(item.id)
      return mapBusiness(item, state.foodsByMerchantId[id] ?? [])
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
    const id = numericId(merchantId, 'businessId')
    const [business, foods] = await Promise.all([
      elemeApi.getBusiness(id),
      elemeApi.listFoods(id),
    ])
    const mappedFoods = foods.map(mapFood)
    state.foodsByMerchantId[String(id)] = mappedFoods
    const merchant = upsertMerchant(mapBusiness(business, mappedFoods))
    state.activeMerchantId = merchant.id
    persistSession()

    if (state.user) {
      await loadCart().catch(setError)
    }

    return merchant
  })
}

async function loadCart() {
  const user = requireUser()
  return withLoading('cart', async () => {
    const data = await elemeApi.listCartItems(user.id)
    replaceArray(state.cartItems, data.map(mapCartItem))
    return state.cartItems
  })
}

async function loadOrders() {
  const user = requireUser()
  return withLoading('orders', async () => {
    const data = await elemeApi.listOrders(user.id)
    replaceArray(state.orders, data.map(mapOrder))
    return state.orders
  })
}

async function loadAddresses() {
  const user = requireUser()
  return withLoading('addresses', async () => {
    const data = await elemeApi.listDeliveryAddresses(user.id)
    const addresses = data.map(mapAddress)
    replaceArray(state.addresses, addresses)

    if (!state.addressId || !state.addresses.some((address) => address.id === state.addressId)) {
      state.addressId = state.addresses[0]?.id ?? ''
      persistSession()
    }

    return state.addresses
  })
}

async function refreshUserResources() {
  const results = await Promise.allSettled([loadAddresses(), loadOrders(), loadCart()])
  const failed = results.find((result): result is PromiseRejectedResult => result.status === 'rejected')
  if (failed && !state.error) {
    setError(failed.reason)
  }
}

async function loadSessionData() {
  const user = requireUser()
  return withLoading('session', async () => {
    state.user = mapUser(await elemeApi.getUser(user.id))
    persistSession()
    await refreshUserResources()
  })
}

async function login(userId: string, password: string) {
  return withLoading('session', async () => {
    const user = mapUser(await elemeApi.createSession({ userId, password }))
    state.user = user
    state.checkoutDraft = null
    persistSession()
    await refreshUserResources()
    return user
  })
}

async function register(payload: UserCreatePayload) {
  return withLoading('session', async () => {
    const user = mapUser(await elemeApi.createUser(payload))
    state.user = user
    state.checkoutDraft = null
    persistSession()
    await refreshUserResources()
    return user
  })
}

function logout() {
  state.user = null
  state.addressId = ''
  state.checkoutDraft = null
  replaceArray(state.addresses, [])
  replaceArray(state.orders, [])
  replaceArray(state.cartItems, [])
  persistSession()
}

async function createAddress(payload: DeliveryAddressPayload) {
  const user = requireUser()
  return withLoading('addresses', async () => {
    const address = mapAddress(await elemeApi.createDeliveryAddress(user.id, payload))
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
  })
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
  const user = requireUser()
  return withLoading('action', async () => {
    const line = mapCartItem(await elemeApi.upsertCartItem(user.id, {
      businessId: numericId(merchantId, 'businessId'),
      foodId: numericId(item.id, 'foodId'),
      quantity: 1,
    }))
    upsertCartLine(line)
    state.activeMerchantId = String(merchantId)
    persistSession()
    return line
  })
}

async function removeFromCart(merchantId: string | number, itemId: string | number) {
  const user = requireUser()
  const line = cartLinesForMerchant(merchantId).find((item) => item.foodId === String(itemId))
  if (!line) {
    return null
  }

  return withLoading('action', async () => {
    if (line.quantity > 1) {
      const updated = mapCartItem(await elemeApi.updateCartItem(user.id, line.cartId, line.quantity - 1))
      upsertCartLine(updated)
      return updated
    }

    await elemeApi.deleteCartItem(user.id, line.cartId)
    removeCartLine(line)
    return null
  })
}

async function clearCart(merchantId: string | number) {
  const user = requireUser()
  return withLoading('action', async () => {
    await elemeApi.clearCart(user.id, merchantId)
    replaceArray(state.cartItems, state.cartItems.filter((line) => line.businessId !== String(merchantId)))
  })
}

async function prepareCheckout(merchantId: string | number) {
  const user = requireUser()
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
    const order = mapOrder(await elemeApi.createOrder(user.id, {
      businessId: numericId(merchantId, 'businessId'),
      daId: numericId(address.id, 'daId'),
    }))
    order.paymentMethod = state.paymentMethod
    upsertOrder(order)
    replaceArray(state.cartItems, state.cartItems.filter((line) => line.businessId !== String(merchantId)))
    state.checkoutDraft = {
      merchantId: String(merchantId),
      paymentMethod: state.paymentMethod,
      addressId: address.id,
      createdOrderId: order.id,
    }
    persistSession()
    return order.id
  })
}

async function fetchOrder(orderId: string | number) {
  const user = requireUser()
  return withLoading('orders', async () => {
    const order = mapOrder(await elemeApi.getOrder(user.id, numericId(orderId, 'orderId')))
    upsertOrder(order)
    return order
  })
}

async function confirmPayment(orderId: string | number, method?: PaymentMethod) {
  const user = requireUser()
  if (method) {
    setPaymentMethod(method)
  }

  return withLoading('action', async () => {
    const order = mapOrder(await elemeApi.payOrder(user.id, numericId(orderId, 'orderId')))
    order.paymentMethod = method ?? state.paymentMethod
    upsertOrder(order)
    if (state.checkoutDraft?.createdOrderId === String(orderId)) {
      state.checkoutDraft = null
    }
    persistSession()
    return order
  })
}

function updateUser(user: UserProfile | null) {
  state.user = user ? { ...user } : null
  persistSession()
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
  if (!merchant) {
    return false
  }

  const { subtotal } = summaryFor(cartLinesForMerchant(merchantId), merchant.deliveryFee)
  return subtotal >= merchant.minOrder
}

export function useHungryStore() {
  return {
    state,
    merchants: state.merchants,
    addresses: state.addresses,
    activeMerchant,
    activeAddress,
    cartLines,
    cartSummary,
    unreadOrders,
    completedOrders,
    initialize,
    loadBusinesses,
    ensureMerchantDetail,
    loadCart,
    loadOrders,
    loadAddresses,
    loadSessionData,
    login,
    register,
    logout,
    createAddress,
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
    updateUser,
    checkoutSummary,
    cartCanCheckout,
    messageFromError,
  }
}
