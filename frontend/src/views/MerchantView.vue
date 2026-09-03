<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import QuantityStepper from '@/components/QuantityStepper.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()
const error = ref('')

// 路由里的商家 id 作为当前详情页主键，没有时回退到当前选中的商家。
const merchantId = computed(() => String(route.params.merchantId || store.activeMerchant.value?.id || ''))

// 当前详情页正在展示的商家。
const merchant = computed(() => store.getMerchant(merchantId.value) ?? null)
// 当前商家的购物车汇总，用于页面底部购物车条。
const cartSummary = computed(() => (merchant.value ? store.checkoutSummary(merchant.value.id) : {
  merchant: null,
  lines: [],
  subtotal: 0,
  fee: 0,
  total: 0,
  count: 0,
}))
// 判断是否已满足起送门槛。
const canCheckout = computed(() => (merchant.value ? store.cartCanCheckout(merchant.value.id) : false))
// 还差多少金额才能起送。
const remaining = computed(() => Math.max(0, (merchant.value?.minOrder ?? 0) - cartSummary.value.subtotal))
const isBusy = computed(() => store.state.loading.merchant || store.state.loading.action)
const checkoutLabel = computed(() => {
  if (!store.state.user) {
    return '登录后下单'
  }

  if (!canCheckout.value) {
    return `差 ${formatCny(remaining.value)} 起送`
  }

  return '去结算'
})

// 进入页面时同步当前商家，保证全局状态和地址栏一致。
watch(
  merchantId,
  async (value) => {
    if (value) {
      error.value = ''
      try {
        await store.ensureMerchantDetail(value)
      } catch (cause) {
        error.value = store.messageFromError(cause)
      }
    }
  },
  { immediate: true },
)

// 读取某个商品在购物车里的数量。
function quantityFor(itemId: string) {
  return cartSummary.value.lines.find((line) => line.id === itemId)?.quantity || 0
}

// 给指定商品加一份到购物车。
async function addItem(itemId: string) {
  if (!store.state.user) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  const item = merchant.value?.menuSections.flatMap((section) => section.items).find((entry) => entry.id === itemId)
  if (item) {
    try {
      error.value = ''
      await store.addToCart(item.businessId, item)
    } catch (cause) {
      error.value = store.messageFromError(cause)
    }
  }
}

// 从购物车里减去一份指定商品。
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

// 生成待结算订单并跳转到确认页。
async function checkout() {
  if (!merchant.value) {
    return
  }

  if (!store.state.user) {
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

// 返回商家列表页。
function goBack() {
  router.push('/businesses')
}
</script>

<template>
  <div class="page">
    <!-- 页面头部：商家名称和返回按钮。 -->
    <SiteHeader :title="merchant?.name || '商家信息'" eyebrow="商家信息" backable @back="goBack" />

    <template v-if="merchant">
      <!-- 头图：突出商家主视觉和卖点。 -->
      <section class="detail-hero">
        <div class="detail-cover">
          <img class="detail-cover__image" :src="merchant.image" :alt="merchant.name" />
          <div class="detail-cover__overlay" />
          <div class="detail-cover__content">
            <h2 class="detail-cover__title">{{ merchant.description || merchant.name }}</h2>
            <p class="detail-cover__text">{{ merchant.address || '商家暂未填写地址' }}</p>
          </div>
        </div>
      </section>

      <!-- 基础数据：后端返回的起送、配送和分类信息。 -->
      <section class="detail-stats">
        <div class="detail-stats__row">
          <article class="detail-stat">
            <p class="detail-stat__label">起送费</p>
            <p class="detail-stat__value">{{ formatCny(merchant.minOrder) }}</p>
          </article>
          <article class="detail-stat">
            <p class="detail-stat__label">配送费</p>
            <p class="detail-stat__value">{{ formatCny(merchant.deliveryFee) }}</p>
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

      <!-- 商家信息卡：后端详情、起送门槛和说明。 -->
      <section class="page__content">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">营业信息</p>
              <h3 class="info-card__title">{{ merchant.name }}</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="check" :size="14" />
              可下单
            </span>
          </div>
          <p class="info-card__text">
            满 {{ formatCny(merchant.minOrder) }} 起送，配送费 {{ formatCny(merchant.deliveryFee) }}。
            {{ merchant.remark || merchant.description || '详情来自后端商家接口。' }}
          </p>
          <p v-if="error" class="field__hint" style="color: var(--danger)">{{ error }}</p>
          <div class="chip-row">
            <span class="chip">分类 {{ merchant.orderTypeId }}</span>
            <span v-if="merchant.address" class="chip">{{ merchant.address }}</span>
          </div>
        </div>
      </section>

      <!-- 菜单区：每个商品都支持加减数量。 -->
      <section v-if="merchant.menuSections.length" class="menu-board">
        <div v-for="section in merchant.menuSections" :key="section.id" class="menu-section">
          <h3 class="menu-section__heading">{{ section.title }}</h3>
          <article v-for="item in section.items" :key="item.id" class="menu-item panel">
            <img class="menu-item__image" :src="item.image" :alt="item.name" />
            <div>
              <div class="chip-row" style="margin-bottom: 8px">
                <span v-if="item.remark" class="status-pill status-pill--warning">{{ item.remark }}</span>
              </div>
              <h4 class="menu-item__title">{{ item.name }}</h4>
              <p class="menu-item__text">{{ item.description || '暂无商品介绍' }}</p>
              <div class="menu-item__meta">
                <span class="menu-item__price">{{ formatCny(item.price) }}</span>
                <QuantityStepper
                  :quantity="quantityFor(item.id)"
                  :disabled="store.state.loading.action"
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

      <!-- 底部购物车条：显示当前小计并提供去结算入口。 -->
      <div class="cart-bar">
        <div class="cart-bar__summary">
          <div class="cart-bar__icon">
            <UiIcon name="cart" :size="20" />
            <span v-if="cartSummary.count" class="cart-bar__badge">{{ cartSummary.count }}</span>
          </div>
          <div>
            <p class="cart-bar__title">{{ formatCny(cartSummary.subtotal) }}</p>
            <p class="cart-bar__text">
              {{ canCheckout ? `已满足起送，配送费 ${formatCny(merchant.deliveryFee)}` : `还差 ${formatCny(remaining)} 起送` }}
            </p>
          </div>
        </div>
        <button
          type="button"
          class="primary-button cart-bar__action"
          :disabled="isBusy || !cartSummary.count || (!!store.state.user && !canCheckout)"
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
