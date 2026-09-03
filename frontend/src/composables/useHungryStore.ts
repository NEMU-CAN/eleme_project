import { computed, reactive } from 'vue'
import { addresses, defaultUser, merchants, seededOrders } from '@/data/mock'
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

// 本地持久化的存储键，刷新页面后还能保留购物车、订单和用户状态。
const STORAGE_KEY = 'tju-hungry-state-v1'

// 前端雏形需要长期保存的状态都集中放在这里，便于统一读写。
interface PersistedState {
  activeMerchantId: string
  cartByMerchantId: Record<string, CartLine[]>
  orders: OrderRecord[]
  user: UserProfile | null
  checkoutDraft: CheckoutDraft | null
  addressId: string
}

// 启动时从 localStorage 读取状态；如果没有缓存，就回退到内置 mock 数据。
function createInitialState(): PersistedState {
  const fallbackMerchantId = merchants[0]?.id ?? ''
  const fallbackAddressId = addresses[0]?.id ?? ''

  if (typeof window === 'undefined') {
    return {
      activeMerchantId: fallbackMerchantId,
      cartByMerchantId: {},
      orders: seededOrders.map((order) => ({ ...order, items: order.items.map((item) => ({ ...item })) })),
      user: null,
      checkoutDraft: null,
      addressId: fallbackAddressId,
    }
  }

  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return {
        activeMerchantId: fallbackMerchantId,
        cartByMerchantId: {},
        orders: seededOrders.map((order) => ({ ...order, items: order.items.map((item) => ({ ...item })) })),
        user: null,
        checkoutDraft: null,
        addressId: fallbackAddressId,
      }
    }

    const parsed = JSON.parse(raw) as Partial<PersistedState>
    return {
      activeMerchantId: parsed.activeMerchantId || fallbackMerchantId,
      cartByMerchantId: parsed.cartByMerchantId || {},
      orders: Array.isArray(parsed.orders) && parsed.orders.length ? parsed.orders : seededOrders,
      user: parsed.user ?? null,
      checkoutDraft: parsed.checkoutDraft ?? null,
      addressId: parsed.addressId || fallbackAddressId,
    }
  } catch {
    return {
      activeMerchantId: fallbackMerchantId,
      cartByMerchantId: {},
      orders: seededOrders.map((order) => ({ ...order, items: order.items.map((item) => ({ ...item })) })),
      user: null,
      checkoutDraft: null,
      addressId: fallbackAddressId,
    }
  }
}

// 把当前状态写回 localStorage，保证页面切换后还能继续使用。
function persistState(state: PersistedState) {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

// 深拷贝购物车明细，避免直接引用导致的意外联动。
function cloneCartLines(lines: CartLine[]): CartLine[] {
  return lines.map((line) => ({ ...line }))
}

// 深拷贝订单列表，保存到本地时避免被外部引用改写。
function cloneOrders(orders: OrderRecord[]): OrderRecord[] {
  return orders.map((order) => ({
    ...order,
    items: cloneCartLines(order.items),
  }))
}

// 根据商家 id 找到当前商家，用于详情页、结算页和订单页联动。
function getMerchant(merchantId: string): Merchant | undefined {
  return merchants.find((merchant) => merchant.id === merchantId)
}

// 根据地址 id 找到当前收货地址，供结算和订单页面复用。
function getAddress(addressId: string): Address | undefined {
  return addresses.find((address) => address.id === addressId)
}

// 确保每个商家都有独立购物车数组，减少空值判断。
function findCartLines(cartMap: Record<string, CartLine[]>, merchantId: string): CartLine[] {
  if (!cartMap[merchantId]) {
    cartMap[merchantId] = []
  }

  return cartMap[merchantId]
}

// 计算小计、总价和件数，供商家页、结算页和订单页共用。
function summaryFor(lines: CartLine[], deliveryFee: number) {
  const subtotal = lines.reduce((total, line) => total + line.price * line.quantity, 0)
  const total = subtotal + deliveryFee
  const quantity = lines.reduce((count, line) => count + line.quantity, 0)

  return { subtotal, total, quantity }
}

const state = reactive<PersistedState>(createInitialState())

export function useHungryStore() {
  // 当前选中的商家，页面导航和购物车都围绕它展开。
  const activeMerchant = computed(() => getMerchant(state.activeMerchantId) ?? merchants[0])
  // 当前收货地址，结算页和个人页会直接读取。
  const activeAddress = computed(() => getAddress(state.addressId) ?? addresses[0])
  // 待支付订单数量，用于底部导航和订单概览角标。
  const unreadOrders = computed(() => state.orders.filter((order) => order.status === 'pending').length)
  // 已支付订单数量，用于个人页统计。
  const completedOrders = computed(() => state.orders.filter((order) => order.status === 'paid').length)
  // 当前商家的购物车明细。
  const cartLines = computed(() => findCartLines(state.cartByMerchantId, state.activeMerchantId))
  // 当前商家的金额汇总，首页和详情页都会用到。
  const cartSummary = computed(() => {
    const merchant = activeMerchant.value
    return summaryFor(cartLines.value, merchant?.deliveryFee ?? 0)
  })

  // 把状态统一持久化，避免各个方法各写一遍。
  function save() {
    persistState({
      activeMerchantId: state.activeMerchantId,
      cartByMerchantId: state.cartByMerchantId,
      orders: cloneOrders(state.orders),
      user: state.user ? { ...state.user } : null,
      checkoutDraft: state.checkoutDraft ? { ...state.checkoutDraft } : null,
      addressId: state.addressId,
    })
  }

  // 切换当前浏览的商家。
  function setActiveMerchant(merchantId: string) {
    state.activeMerchantId = merchantId
    save()
  }

  // 切换当前收货地址。
  function setAddress(addressId: string) {
    state.addressId = addressId
    save()
  }

  // 设置结算草稿里的支付方式，给支付页预填值。
  function setPaymentMethod(method: PaymentMethod) {
    if (!state.checkoutDraft) {
      state.checkoutDraft = {
        merchantId: state.activeMerchantId,
        paymentMethod: method,
        addressId: state.addressId,
        createdOrderId: null,
      }
    } else {
      state.checkoutDraft.paymentMethod = method
    }

    save()
  }

  // 向指定商家的购物车里添加一件商品。
  function addToCart(merchantId: string, item: MenuItem) {
    const lines = findCartLines(state.cartByMerchantId, merchantId)
    const current = lines.find((line) => line.id === item.id)

    if (current) {
      current.quantity += 1
    } else {
      lines.push({
        id: item.id,
        name: item.name,
        price: item.price,
        image: item.image,
        quantity: 1,
      })
    }

    state.activeMerchantId = merchantId
    save()
  }

  // 从指定商家的购物车里减少一件商品。
  function removeFromCart(merchantId: string, itemId: string) {
    const lines = findCartLines(state.cartByMerchantId, merchantId)
    const target = lines.find((line) => line.id === itemId)

    if (!target) {
      return
    }

    target.quantity -= 1
    if (target.quantity <= 0) {
      const index = lines.findIndex((line) => line.id === itemId)
      if (index >= 0) {
        lines.splice(index, 1)
      }
    }

    save()
  }

  // 清空某个商家的购物车。
  function clearCart(merchantId: string) {
    state.cartByMerchantId[merchantId] = []
    save()
  }

  // 生成演示用订单号，方便在不同页面间串联流程。
  function buildOrderId() {
    return `order-${Math.random().toString(36).slice(2, 9)}`
  }

  // 将购物车转换为待结算订单，并生成订单详情页跳转目标。
  function prepareCheckout(merchantId: string) {
    const merchant = getMerchant(merchantId)
    const lines = cloneCartLines(findCartLines(state.cartByMerchantId, merchantId))
    const address = activeAddress.value ?? addresses[0]!

    if (!merchant || !lines.length) {
      return null
    }

    const { subtotal, total } = summaryFor(lines, merchant.deliveryFee)
    const orderId = buildOrderId()

    const order: OrderRecord = {
      id: orderId,
      merchantId: merchant.id,
      merchantName: merchant.name,
      merchantImage: merchant.image,
      status: 'pending',
      paymentMethod: state.checkoutDraft?.paymentMethod ?? 'alipay',
      addressId: address.id,
      addressName: address.name,
      addressPhone: address.phone,
      addressDetail: address.detail,
      items: lines,
      deliveryFee: merchant.deliveryFee,
      subtotal,
      total,
      createdAt: new Date().toISOString(),
    }

    state.orders.unshift(order)
    state.checkoutDraft = {
      merchantId: merchant.id,
      paymentMethod: order.paymentMethod,
      addressId: address.id,
      createdOrderId: order.id,
    }
    clearCart(merchantId)
    save()

    return order.id
  }

  // 根据订单号读取订单详情。
  function getOrder(orderId: string) {
    return state.orders.find((order) => order.id === orderId)
  }

  // 支付成功后把订单标记为已支付。
  function confirmPayment(orderId: string, method?: PaymentMethod) {
    const order = getOrder(orderId)
    if (!order) {
      return null
    }

    order.status = 'paid'
    order.paidAt = new Date().toISOString()
    order.paymentMethod = method ?? order.paymentMethod
    if (state.checkoutDraft?.createdOrderId === orderId) {
      state.checkoutDraft = null
    }

    save()
    return order
  }

  // 更新本地用户资料，登录和注册页都会用到。
  function updateUser(user: UserProfile | null) {
    state.user = user ? { ...user } : null
    save()
  }

  // 恢复默认演示用户，方便快速回到初始状态。
  function resetUserToDefault() {
    state.user = { ...defaultUser }
    save()
  }

  // 生成结算页需要的商家、商品和金额汇总。
  function checkoutSummary(merchantId: string) {
    const merchant = getMerchant(merchantId)
    const lines = findCartLines(state.cartByMerchantId, merchantId)
    const fee = merchant?.deliveryFee ?? 0
    const subtotal = lines.reduce((total, line) => total + line.price * line.quantity, 0)
    const total = subtotal + fee
    return {
      merchant,
      lines,
      subtotal,
      fee,
      total,
      count: lines.reduce((totalCount, line) => totalCount + line.quantity, 0),
    }
  }

  // 判断当前购物车是否达到起送门槛。
  function cartCanCheckout(merchantId: string) {
    const merchant = getMerchant(merchantId)
    if (!merchant) {
      return false
    }

    const lines = findCartLines(state.cartByMerchantId, merchantId)
    const { subtotal } = summaryFor(lines, merchant.deliveryFee)
    return subtotal >= merchant.minOrder
  }

  return {
    state,
    merchants,
    addresses,
    activeMerchant,
    activeAddress,
    cartLines,
    cartSummary,
    unreadOrders,
    completedOrders,
    setActiveMerchant,
    setAddress,
    setPaymentMethod,
    addToCart,
    removeFromCart,
    clearCart,
    prepareCheckout,
    confirmPayment,
    updateUser,
    resetUserToDefault,
    getOrder,
    checkoutSummary,
    cartCanCheckout,
  }
}
