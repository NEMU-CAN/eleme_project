<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatBusinessStatus, formatCny } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()
const error = ref('')

const merchantId = computed(() => String(route.params.merchantId || store.activeMerchant.value?.id || ''))
const merchant = computed(() => store.getMerchant(merchantId.value) ?? null)
const menuItems = computed(() => merchant.value?.menuSections.flatMap((section) => section.items) ?? [])
const cartSummary = computed(() => (merchant.value ? store.checkoutSummary(merchant.value.id) : {
  merchant: null,
  lines: [],
  subtotal: 0,
  fee: 0,
  total: 0,
  count: 0,
}))
const canCheckout = computed(() => (merchant.value ? store.cartCanCheckout(merchant.value.id) : false))
const remaining = computed(() => Math.max(0, (merchant.value?.minOrder ?? 0) - cartSummary.value.subtotal))
const isBusy = computed(() => store.state.loading.merchant || store.state.loading.action)

const checkoutLabel = computed(() => {
  if (!store.isAuthenticated.value) {
    return '登录后下单'
  }
  if (!canCheckout.value) {
    return `差 ¥${remaining.value} 起送`
  }
  return '去结算'
})

watch(
  merchantId,
  async (value) => {
    if (!value) {
      return
    }
    error.value = ''
    try {
      await store.ensureMerchantDetail(value)
    } catch (cause) {
      error.value = store.messageFromError(cause)
    }
  },
  { immediate: true },
)

function quantityFor(itemId: string) {
  return cartSummary.value.lines.find((line) => line.id === itemId)?.quantity || 0
}

function availableStockFor(itemId: string) {
  return menuItems.value.find((item) => item.id === itemId)?.stock ?? 0
}

function canAddItem(itemId: string) {
  const item = menuItems.value.find((entry) => entry.id === itemId)
  if (!merchant.value || !item) {
    return false
  }
  if (merchant.value.status !== 'open' || item.status !== 'online') {
    return false
  }
  return quantityFor(item.id) < availableStockFor(item.id)
}

function stockLabel(itemId: string) {
  const item = menuItems.value.find((entry) => entry.id === itemId)
  if (!item) {
    return ''
  }
  if (item.status !== 'online') {
    return '已下架'
  }
  const available = availableStockFor(itemId)
  if (available <= 0) {
    return '已售罄'
  }
  if (available <= 3) {
    return `仅剩 ${available} 份`
  }
  return ''
}

async function addItem(itemId: string) {
  if (!store.isAuthenticated.value) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  const item = menuItems.value.find((entry) => entry.id === itemId)
  if (item && canAddItem(item.id)) {
    try {
      error.value = ''
      await store.addToCart(item.businessId, item)
    } catch (cause) {
      error.value = store.messageFromError(cause)
    }
  }
}

async function removeItem(itemId: string) {
  if (!merchant.value) {
    return
  }
  try {
    error.value = ''
    await store.removeFromCart(merchant.value.id, itemId)
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

async function checkout() {
  if (!merchant.value) {
    return
  }
  if (!store.isAuthenticated.value) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    error.value = ''
    const orderId = await store.prepareCheckout(merchant.value.id)
    router.push(`/checkout/${orderId}`)
  } catch (cause) {
    const message = store.messageFromError(cause)
    error.value = message
    if (message.includes('收货地址')) {
      router.push('/addresses')
    }
  }
}

function goBack() {
  router.push('/')
}
</script>

<template>
  <div class="shop-page">
    <!-- 顶部商家信息（固定） -->
    <header class="shop-header">
      <div class="shop-header__top">
        <button type="button" class="icon-button" @click="goBack">
          <UiIcon name="chevronLeft" :size="20" />
        </button>
        <span class="site-header__title" style="flex: 1; text-align: center">{{ merchant?.name || '商家信息' }}</span>
        <span style="width: 34px" />
      </div>
      <div v-if="merchant" class="shop-header__info">
        <img class="shop-header__image" :src="merchant.image" :alt="merchant.name" />
        <div class="shop-header__copy">
          <h2 class="shop-header__name">{{ merchant.name }}</h2>
          <p class="shop-header__meta">
            ¥{{ merchant.minOrder ?? merchant.startPrice }} 起送 · 配送 ¥{{ merchant.deliveryFee }} ·
            <span style="color: var(--success)">{{ formatBusinessStatus(merchant.status) }}</span>
          </p>
          <p class="shop-header__addr">{{ merchant.address || '商家暂未填写地址' }}</p>
        </div>
      </div>
    </header>

    <!-- 中间菜品（可滑动） -->
    <div class="shop-menu">
      <template v-if="merchant">
        <p class="shop-menu__title">{{ merchant.description || merchant.remark || '菜单' }}</p>
        <p v-if="error" class="auth-form__hint auth-form__hint--danger" style="padding: 0 14px 8px">{{ error }}</p>
        <div v-for="section in merchant.menuSections" :key="section.id">
          <article v-for="item in section.items" :key="item.id" class="food-item">
            <img class="food-item__image" :src="item.image" :alt="item.name" />
            <div class="food-item__body">
              <h4 class="food-item__name">{{ item.name }}</h4>
              <p class="food-item__stock" :class="{ 'food-item__stock--low': availableStockFor(item.id) <= 3 && item.status === 'online' }">
                {{ stockLabel(item.id) || (item.description || '') }}
              </p>
              <div class="food-item__footer">
                <span class="food-item__price">{{ item.price }}</span>
                <div class="stepper">
                  <button
                    v-if="quantityFor(item.id) > 0"
                    type="button"
                    class="stepper__btn stepper__btn--minus"
                    @click="removeItem(item.id)"
                  >
                    <UiIcon name="minus" :size="14" />
                  </button>
                  <span v-if="quantityFor(item.id) > 0" class="stepper__qty">{{ quantityFor(item.id) }}</span>
                  <button
                    type="button"
                    class="stepper__btn stepper__btn--plus"
                    :disabled="isBusy || !canAddItem(item.id)"
                    @click="addItem(item.id)"
                  >
                    <UiIcon name="plus" :size="16" />
                  </button>
                </div>
              </div>
            </div>
          </article>
        </div>

        <section v-if="!merchant.menuSections.length" class="empty-state">
          <p class="empty-state__title">{{ store.state.loading.merchant ? '正在加载菜单' : '暂无菜品' }}</p>
          <p class="empty-state__text">{{ store.state.loading.merchant ? '正在请求后端菜品接口。' : '后端暂时没有返回这个商家的菜品。' }}</p>
        </section>
      </template>

      <section v-else class="empty-state">
        <p class="empty-state__title">{{ store.state.loading.merchant ? '正在加载商家' : '没有找到商家' }}</p>
        <p class="empty-state__text">{{ error || store.state.error || '请返回商家列表重新选择。' }}</p>
      </section>
    </div>

    <!-- 底部购物车结算栏（固定） -->
    <div class="cart-bar">
      <div class="cart-bar__icon" :class="{ 'cart-bar__icon--active': cartSummary.count > 0 }">
        <UiIcon name="cart" :size="22" />
        <span v-if="cartSummary.count" class="cart-bar__badge">{{ cartSummary.count }}</span>
      </div>
      <div class="cart-bar__summary">
        <p class="cart-bar__total">¥{{ cartSummary.subtotal }}</p>
        <p class="cart-bar__hint">
          {{ cartSummary.count ? `配送费 ¥${cartSummary.fee}` : '未选购商品' }}
        </p>
      </div>
      <button
        type="button"
        class="cart-bar__action"
        :disabled="isBusy || !cartSummary.count || (store.isAuthenticated.value && !canCheckout)"
        @click="checkout"
      >
        {{ isBusy ? '处理中' : checkoutLabel }}
      </button>
    </div>
  </div>
</template>
