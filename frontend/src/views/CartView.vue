<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import type { CartLine, Merchant } from '@/types'

const router = useRouter()
const store = useHungryStore()
const error = ref('')
const selected = ref<string[]>([])

interface CartGroup {
  businessId: string
  merchant: Merchant | null
  name: string
  image: string
  lines: CartLine[]
  subtotal: number
}

const groups = computed<CartGroup[]>(() => {
  const map = new Map<string, CartGroup>()
  for (const line of store.state.cartItems) {
    const merchant = store.getMerchant(line.businessId)
    let group = map.get(line.businessId)
    if (!group) {
      group = {
        businessId: line.businessId,
        merchant: merchant ?? null,
        name: merchant?.name || `商家 ${line.businessId}`,
        image: merchant?.image || line.image,
        lines: [],
        subtotal: 0,
      }
      map.set(line.businessId, group)
    }
    group.lines.push(line)
    group.subtotal += line.price * line.quantity
  }
  return [...map.values()]
})

const cartCount = computed(() => store.state.cartItems.reduce((total, line) => total + line.quantity, 0))
const allSelected = computed(() => groups.value.length > 0 && selected.value.length === groups.value.length)
const selectedTotal = computed(() =>
  groups.value
    .filter((group) => selected.value.includes(group.businessId))
    .reduce((total, group) => total + group.subtotal, 0),
)

const heroAddress = computed(() => {
  if (store.activeAddress.value) {
    return store.activeAddress.value.detail
  }
  return store.isAuthenticated.value ? '请选择收货地址' : '登录后同步购物车'
})

onMounted(async () => {
  if (!store.isAuthenticated.value) {
    return
  }
  try {
    error.value = ''
    await Promise.all([store.loadCart(), store.loadAddresses()])
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
})

function toggleGroup(businessId: string) {
  if (selected.value.includes(businessId)) {
    selected.value = selected.value.filter((id) => id !== businessId)
  } else {
    selected.value = [...selected.value, businessId]
  }
}

function toggleAll() {
  if (allSelected.value) {
    selected.value = []
  } else {
    selected.value = groups.value.map((group) => group.businessId)
  }
}

function goMerchant(businessId: string) {
  router.push(`/merchant/${businessId}`)
}

async function removeGroup(businessId: string) {
  try {
    error.value = ''
    await store.clearCart(businessId)
    selected.value = selected.value.filter((id) => id !== businessId)
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

async function checkoutSelected() {
  if (!selected.value.length) {
    return
  }
  if (!store.isAuthenticated.value) {
    router.push({ path: '/login', query: { redirect: '/cart' } })
    return
  }
  try {
    error.value = ''
    const orderIds: string[] = []
    for (const businessId of selected.value) {
      orderIds.push(await store.prepareCheckout(businessId))
    }
    if (orderIds.length) {
      router.push(`/checkout/${orderIds[0]}`)
    }
  } catch (cause) {
    const message = store.messageFromError(cause)
    error.value = message
    if (message.includes('收货地址')) {
      router.push('/addresses')
    }
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="page page--with-dock">
    <!-- 顶部地址栏 -->
    <div class="location-bar">
      <UiIcon class="location-bar__icon" name="pin" :size="18" />
      <span class="location-bar__label">{{ heroAddress }}</span>
    </div>

    <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>

    <!-- 空购物车 -->
    <div v-if="store.isAuthenticated && !store.state.loading.cart && !groups.length" class="cart-empty">
      <UiIcon class="cart-empty__face" name="sad" :size="64" />
      <p class="cart-empty__text">这里空空如也，快去买入你喜欢的食物吧</p>
      <button type="button" class="primary-button" @click="router.push('/')">去逛逛</button>
    </div>

    <!-- 未登录 -->
    <div v-else-if="!store.isAuthenticated" class="cart-empty">
      <UiIcon class="cart-empty__face" name="sad" :size="64" />
      <p class="cart-empty__text">登录后查看你的购物车</p>
      <button type="button" class="primary-button" @click="goLogin">去登录</button>
    </div>

    <!-- 购物车按商家分组 -->
    <div v-else class="merchant-list">
      <article v-for="group in groups" :key="group.businessId" class="cart-group">
        <div class="cart-group__head">
          <span class="checkbox" :class="{ 'checkbox--on': selected.includes(group.businessId) }" @click="toggleGroup(group.businessId)">
            <UiIcon v-if="selected.includes(group.businessId)" name="check" :size="14" />
          </span>
          <img class="cart-group__image" :src="group.image" :alt="group.name" />
          <h3 class="cart-group__name" @click="goMerchant(group.businessId)">{{ group.name }}</h3>
          <button type="button" class="cart-group__trash" aria-label="删除" @click="removeGroup(group.businessId)">
            <UiIcon name="trash" :size="20" />
          </button>
        </div>

        <div class="cart-line" @click="goMerchant(group.businessId)">
          <img class="cart-line__image" :src="group.lines[0]?.image" alt="" />
          <div class="cart-line__info">
            <p class="cart-line__name">{{ group.lines.map((line) => `${line.name} x${line.quantity}`).join('、') }}</p>
            <p class="cart-line__price">¥{{ group.subtotal }}</p>
          </div>
          <UiIcon name="chevronRight" :size="16" />
        </div>
      </article>
    </div>

    <!-- 底部一键结算 -->
    <div v-if="groups.length" class="checkout-dock">
      <button type="button" class="checkout-dock__select" @click="toggleAll">
        <span class="checkbox" :class="{ 'checkbox--on': allSelected }">
          <UiIcon v-if="allSelected" name="check" :size="14" />
        </span>
        全选
      </button>
      <div class="checkout-dock__total">
        合计 <strong>¥{{ selectedTotal }}</strong>
      </div>
      <button type="button" class="cart-bar__action" :disabled="!selected.length || store.state.loading.action" @click="checkoutSelected">
        {{ store.state.loading.action ? '结算中' : `一键结算(${selected.length})` }}
      </button>
    </div>

    <BottomNav />
  </div>
</template>
