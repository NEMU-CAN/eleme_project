<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderTime } from '@/utils/format'
import type { OrderStatus } from '@/types'

const router = useRouter()
const store = useHungryStore()
const filter = ref<'all' | OrderStatus>('all')

// 按时间倒序排列，最新订单放在最前面。
const sortedOrders = computed(() =>
  [...store.state.orders].sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt)),
)

// 未支付订单集合，用于顶部概览和筛选。
const pendingOrders = computed(() => sortedOrders.value.filter((order) => order.status === 'pending'))
// 已支付订单集合。
const paidOrders = computed(() => sortedOrders.value.filter((order) => order.status === 'paid'))

// 根据筛选标签决定当前显示哪些订单。
const visibleOrders = computed(() => {
  if (filter.value === 'pending') {
    return pendingOrders.value
  }
  if (filter.value === 'paid') {
    return paidOrders.value
  }
  return sortedOrders.value
})

// 订单页的筛选标签。
const tabs = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '未支付' },
  { key: 'paid', label: '已支付' },
] as const

// 未支付订单直接跳转到支付页。
function goPayment(orderId: string) {
  router.push(`/payment/${orderId}`)
}
</script>

<template>
  <div class="page page--with-nav">
    <!-- 订单页头部。 -->
    <SiteHeader title="我的订单" eyebrow="按时间排序" />

    <div class="page__content">
      <!-- 订单概览区：显示待支付数量和统计。 -->
      <section class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">订单概览</p>
              <h3 class="info-card__title">本地 mock 订单</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="orders" :size="14" />
              {{ store.unreadOrders.value }} 笔待支付
            </span>
          </div>
          <div class="detail-stats__row">
            <article class="detail-stat">
              <p class="detail-stat__label">未支付</p>
              <p class="detail-stat__value">{{ pendingOrders.length }}</p>
            </article>
            <article class="detail-stat">
              <p class="detail-stat__label">已支付</p>
              <p class="detail-stat__value">{{ paidOrders.length }}</p>
            </article>
          </div>
        </div>
      </section>

      <!-- 筛选标签：全部、未支付、已支付。 -->
      <section class="section">
        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            class="tab-button"
            :class="{ 'tab-button--active': filter === tab.key }"
            @click="filter = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>
      </section>

      <!-- 订单列表：每条订单都带有明细和支付入口。 -->
      <section class="section">
        <div v-if="visibleOrders.length" class="timeline-list">
          <article v-for="order in visibleOrders" :key="order.id" class="timeline-item panel">
            <div class="timeline-item__top">
              <div class="merchant-card__metrics" style="align-items: center">
                <img :src="order.merchantImage" :alt="order.merchantName" width="56" height="56" style="border-radius: 16px" />
                <div>
                  <h3 class="timeline-item__name">{{ order.merchantName }}</h3>
                  <p class="timeline-item__meta">{{ formatOrderTime(order.createdAt) }}</p>
                </div>
              </div>
              <span v-if="order.status === 'pending'" class="status-pill status-pill--warning">未支付</span>
              <span v-else class="status-pill status-pill--success">已支付</span>
            </div>

            <div class="timeline-list">
              <div v-for="item in order.items" :key="item.id" class="timeline-line">
                <span>{{ item.name }} x {{ item.quantity }}</span>
                <span>{{ formatCny(item.price * item.quantity) }}</span>
              </div>
              <div class="timeline-line">
                <span>配送费</span>
                <span>{{ formatCny(order.deliveryFee) }}</span>
              </div>
            </div>

            <div class="order-summary__total" style="padding-top: 0; border-top: 0">
              <span>合计</span>
              <strong>{{ formatCny(order.total) }}</strong>
            </div>

            <div class="auth-card__footer">
              <span class="muted">{{ order.addressName }} · {{ order.addressPhone }}</span>
              <button
                v-if="order.status === 'pending'"
                type="button"
                class="primary-button"
                @click="goPayment(order.id)"
              >
                去支付
              </button>
            </div>
          </article>
        </div>

        <div v-else class="empty-state panel">
          <h3 class="empty-state__title">暂无订单</h3>
          <p class="empty-state__text">你还没有创建任何订单，先去首页或者商家页试一下。</p>
        </div>
      </section>
    </div>

    <BottomNav />
  </div>
</template>
