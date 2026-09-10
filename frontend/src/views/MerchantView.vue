<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import QuantityStepper from '@/components/QuantityStepper.vue'
import SiteHeader from '@/components/SiteHeader.vue'
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
const merchantStatusText = computed(() => merchant.value ? formatBusinessStatus(merchant.value.status) : '营业信息')
const merchantStatusClass = computed(() => {
  if (!merchant.value) {
    return 'status-pill'
  }

  if (merchant.value.status === 'open') {
    return 'status-pill status-pill--success'
  }

  if (merchant.value.status === 'closed') {
    return 'status-pill status-pill--warning'
  }

  return 'status-pill status-pill--danger'
})
const cartHint = computed(() => {
  if (!cartSummary.value.count) {
    return '先挑选一些商品，再进入结算。'
  }

  if (!store.isAuthenticated.value) {
    return '登录后可以继续结算并同步地址。'
  }

  if (!canCheckout.value) {
    return `还差 ${formatCny(remaining.value)} 才能起送。`
  }

  return `已满足起送，配送费 ${formatCny(merchant.value?.deliveryFee ?? 0)}。`
})
const checkoutLabel = computed(() => {
  if (!store.isAuthenticated.value) {
    return '登录后下单'
  }

  if (!canCheckout.value) {
    return `差 ${formatCny(remaining.value)} 起送`
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

  return `剩余 ${available} 份`
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
      router.push('/me')
    }
  }
}

function goBack() {
  router.push('/businesses')
}
</script>

<template>
  <div class="page">
    <SiteHeader :title="merchant?.name || '商家信息'" eyebrow="商家信息" backable @back="goBack" />

    <template v-if="merchant">
      <section class="detail-hero">
        <div class="detail-cover">
          <img class="detail-cover__image" :src="merchant.image" :alt="merchant.name" />
          <div class="detail-cover__overlay" />
          <div class="detail-cover__content">
            <div class="chip-row detail-cover__chips">
              <span class="status-pill" :class="merchantStatusClass.replace('status-pill ', '')">
                <UiIcon name="check" :size="14" />
                {{ merchantStatusText }}
              </span>
              <span class="status-pill">
                <UiIcon name="filter" :size="14" />
                {{ merchant.tasteName }}
              </span>
            </div>
            <h2 class="detail-cover__title">{{ merchant.description || merchant.name }}</h2>
            <p class="detail-cover__text">{{ merchant.address || '商家暂未填写地址' }}</p>
          </div>
        </div>
      </section>

      <section class="detail-stats">
        <div class="detail-stats__row">
          <article class="detail-stat">
            <p class="detail-stat__label">起送费</p>
            <p class="detail-stat__value">{{ formatCny(merchant.minOrder ?? 0) }}</p>
          </article>
          <article class="detail-stat">
            <p class="detail-stat__label">配送费</p>
            <p class="detail-stat__value">{{ formatCny(merchant.deliveryFee ?? 0) }}</p>
          </article>
        </div>
        <div class="detail-stats__row">
          <article class="detail-stat">
            <p class="detail-stat__label">分类</p>
            <p class="detail-stat__value">{{ merchant.orderTypeId }}</p>
          </article>
          <article class="detail-stat">
            <p class="detail-stat__label">商家编号</p>
            <p class="detail-stat__value">{{ merchant.id }}</p>
          </article>
        </div>
      </section>

      <section class="page__content">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">营业信息</p>
              <h3 class="info-card__title">{{ merchant.name }}</h3>
            </div>
            <span :class="merchantStatusClass">
              <UiIcon name="check" :size="14" />
              {{ merchantStatusText }}
            </span>
          </div>
          <p class="info-card__text">
            满 {{ formatCny(merchant.minOrder ?? 0) }} 起送，配送费 {{ formatCny(merchant.deliveryFee ?? 0) }}。
            {{ merchant.remark || merchant.description || '详情来自后端商家接口。' }}
          </p>
          <p v-if="error" class="field__hint" style="color: var(--danger)">{{ error }}</p>
          <div class="chip-row">
            <span class="chip">分类 {{ merchant.orderTypeId }}</span>
            <span class="chip">{{ merchant.tasteName }}</span>
            <span v-if="merchant.address" class="chip">{{ merchant.address }}</span>
          </div>
        </div>
      </section>

      <section v-if="merchant.menuSections.length" class="menu-board">
        <div v-for="section in merchant.menuSections" :key="section.id" class="menu-section">
          <h3 class="menu-section__heading">{{ section.title }}</h3>
          <article v-for="item in section.items" :key="item.id" class="menu-item panel">
            <img class="menu-item__image" :src="item.image" :alt="item.name" />
            <div>
              <div class="menu-item__badges chip-row">
                <span :class="item.status === 'online' && availableStockFor(item.id) > 0 ? 'status-pill status-pill--success' : 'status-pill status-pill--warning'">
                  {{ stockLabel(item.id) }}
                </span>
              </div>
              <h4 class="menu-item__title">{{ item.name }}</h4>
              <p class="menu-item__text">{{ item.description || '暂无商品介绍' }}</p>
              <div class="menu-item__meta">
                <span class="menu-item__price">{{ formatCny(item.price) }}</span>
                <QuantityStepper
                  :quantity="quantityFor(item.id)"
                  :disabled="store.state.loading.action"
                  :can-add="canAddItem(item.id)"
                  @add="addItem(item.id)"
                  @remove="removeItem(item.id)"
                />
              </div>
            </div>
          </article>
        </div>
      </section>

      <section v-else class="page__content section">
        <div class="empty-state panel">
          <h3 class="empty-state__title">{{ store.state.loading.merchant ? '正在加载菜单' : '暂无菜品' }}</h3>
          <p class="empty-state__text">{{ store.state.loading.merchant ? '正在请求后端菜品接口。' : '后端暂时没有返回这个商家的菜品。' }}</p>
        </div>
      </section>

      <div class="cart-bar">
        <div class="cart-bar__summary">
          <div class="cart-bar__icon">
            <UiIcon name="cart" :size="20" />
            <span v-if="cartSummary.count" class="cart-bar__badge">{{ cartSummary.count }}</span>
          </div>
          <div>
            <p class="cart-bar__title">{{ formatCny(cartSummary.subtotal) }}</p>
            <p class="cart-bar__text">{{ cartHint }}</p>
          </div>
        </div>
        <button
          type="button"
          class="primary-button cart-bar__action"
          :disabled="isBusy || !cartSummary.count || (store.isAuthenticated.value && !canCheckout)"
          @click="checkout"
        >
          {{ isBusy ? '处理中' : checkoutLabel }}
        </button>
      </div>
    </template>

    <section v-else class="page__content section">
      <div class="empty-state panel">
        <h3 class="empty-state__title">{{ store.state.loading.merchant ? '正在加载商家' : '没有找到商家' }}</h3>
        <p class="empty-state__text">{{ error || store.state.error || '请返回商家列表重新选择。' }}</p>
        <button type="button" class="primary-button" style="margin-top: 16px" @click="goBack">
          去商家列表
        </button>
      </div>
    </section>
  </div>
</template>
