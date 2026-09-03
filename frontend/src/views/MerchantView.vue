<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import QuantityStepper from '@/components/QuantityStepper.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import type { Merchant } from '@/types'
import { formatCny, formatDistance, formatMinutes } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()

// 路由里的商家 id 作为当前详情页主键，没有时回退到当前选中的商家。
const merchantId = computed(() => String(route.params.merchantId || store.activeMerchant.value?.id || ''))

// 当前详情页正在展示的商家。
const merchant = computed<Merchant>(
  () => store.merchants.find((item) => item.id === merchantId.value) ?? store.merchants[0]!,
)
// 当前商家的购物车汇总，用于页面底部购物车条。
const cartSummary = computed(() => store.checkoutSummary(merchant.value.id))
// 判断是否已满足起送门槛。
const canCheckout = computed(() => store.cartCanCheckout(merchant.value.id))
// 还差多少金额才能起送。
const remaining = computed(() => Math.max(0, merchant.value.minOrder - cartSummary.value.subtotal))

// 进入页面时同步当前商家，保证全局状态和地址栏一致。
watch(
  merchantId,
  (value) => {
    if (value) {
      store.setActiveMerchant(value)
    }
  },
  { immediate: true },
)

// 读取某个商品在购物车里的数量。
function quantityFor(itemId: string) {
  return cartSummary.value.lines.find((line) => line.id === itemId)?.quantity || 0
}

// 给指定商品加一份到购物车。
function addItem(itemId: string) {
  const item = merchant.value.menuSections.flatMap((section) => section.items).find((entry) => entry.id === itemId)
  if (item) {
    store.addToCart(merchant.value.id, item)
  }
}

// 从购物车里减去一份指定商品。
function removeItem(itemId: string) {
  store.removeFromCart(merchant.value.id, itemId)
}

// 生成待结算订单并跳转到确认页。
function checkout() {
  const orderId = store.prepareCheckout(merchant.value.id)
  if (orderId) {
    router.push(`/checkout/${orderId}`)
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
    <SiteHeader :title="merchant.name" eyebrow="商家信息" backable @back="goBack" />

    <!-- 头图：突出商家主视觉和卖点。 -->
    <section class="detail-hero">
      <div class="detail-cover">
        <img class="detail-cover__image" :src="merchant.banner" :alt="merchant.name" />
        <div class="detail-cover__overlay" />
        <div class="detail-cover__content">
          <h2 class="detail-cover__title">{{ merchant.highlight }}</h2>
          <p class="detail-cover__text">{{ merchant.cuisine }} · {{ merchant.tags.join(' / ') }}</p>
        </div>
      </div>
    </section>

    <!-- 基础数据：评分、月售、配送费、距离和时长。 -->
    <section class="detail-stats">
      <div class="detail-stats__row">
        <article class="detail-stat">
          <p class="detail-stat__label">评分</p>
          <p class="detail-stat__value">{{ merchant.rating.toFixed(1) }}</p>
        </article>
        <article class="detail-stat">
          <p class="detail-stat__label">月售</p>
          <p class="detail-stat__value">{{ merchant.monthlySales }} 单</p>
        </article>
      </div>
      <div class="detail-stats__row">
        <article class="detail-stat">
          <p class="detail-stat__label">配送</p>
          <p class="detail-stat__value">{{ formatCny(merchant.deliveryFee) }}</p>
        </article>
        <article class="detail-stat">
          <p class="detail-stat__label">距离 / 时长</p>
          <p class="detail-stat__value">{{ formatDistance(merchant.distanceKm) }} · {{ formatMinutes(merchant.etaMinutes) }}</p>
        </article>
      </div>
    </section>

    <!-- 商家信息卡：活动、起送门槛和说明。 -->
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
          这里的数据是本地 mock，后面接后端只需要替换数据来源。
        </p>
        <div class="chip-row">
          <span v-for="tag in merchant.promotions" :key="tag" class="chip">{{ tag }}</span>
        </div>
      </div>
    </section>

    <!-- 菜单区：每个商品都支持加减数量。 -->
    <section class="menu-board">
      <div v-for="section in merchant.menuSections" :key="section.id" class="menu-section">
        <h3 class="menu-section__heading">{{ section.title }}</h3>
        <article v-for="item in section.items" :key="item.id" class="menu-item panel">
          <img class="menu-item__image" :src="item.image" :alt="item.name" />
          <div>
            <div class="chip-row" style="margin-bottom: 8px">
              <span v-if="item.tag" class="status-pill status-pill--warning">{{ item.tag }}</span>
            </div>
            <h4 class="menu-item__title">{{ item.name }}</h4>
            <p class="menu-item__text">{{ item.description }}</p>
            <div class="menu-item__meta">
              <span class="menu-item__price">{{ formatCny(item.price) }}</span>
              <QuantityStepper
                :quantity="quantityFor(item.id)"
                @add="addItem(item.id)"
                @remove="removeItem(item.id)"
              />
            </div>
          </div>
        </article>
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
      <button type="button" class="primary-button cart-bar__action" :disabled="!cartSummary.count || !canCheckout" @click="checkout">
        {{ canCheckout ? '去结算' : `差 ${formatCny(remaining)} 起送` }}
      </button>
    </div>
  </div>
</template>
