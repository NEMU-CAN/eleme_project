<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderStatus, formatOrderTime } from '@/utils/format'
import type { OrderStatus } from '@/types'

const router = useRouter()
const store = useHungryStore()
const filter = ref<'all' | OrderStatus>('all')
const error = ref('')

onMounted(async () => {
  if (!store.isAuthenticated.value) {
    router.replace({ path: '/login', query: { redirect: '/orders' } })
    return
  }

  try {
    error.value = ''
    await store.loadOrders()
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
})

const sortedOrders = computed(() =>
  [...store.state.orders].sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt)),
)

const unpaidOrders = computed(() => sortedOrders.value.filter((order) => order.status === 'unpaid'))
const paidOrders = computed(() => sortedOrders.value.filter((order) => order.status === 'paid'))
const completedOrders = computed(() => sortedOrders.value.filter((order) => order.status === 'completed'))
const canceledOrders = computed(() => sortedOrders.value.filter((order) => order.status === 'canceled'))

const visibleOrders = computed(() => {
  if (filter.value === 'unpaid') {
    return unpaidOrders.value
  }
  if (filter.value === 'paid') {
    return paidOrders.value
  }
  if (filter.value === 'completed') {
    return completedOrders.value
  }
  if (filter.value === 'canceled') {
    return canceledOrders.value
  }
  return sortedOrders.value
})

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unpaid', label: '待支付' },
  { key: 'paid', label: '已支付' },
  { key: 'completed', label: '已完成' },
  { key: 'canceled', label: '已取消' },
] as const

function statusClass(status: OrderStatus) {
  if (status === 'canceled') {
    return 'status-pill status-pill--danger'
  }

  if (status === 'paid' || status === 'completed') {
    return 'status-pill status-pill--success'
  }

  return 'status-pill status-pill--warning'
}

function goPayment(orderId: string) {
  router.push(`/payment/${orderId}`)
}

function goDetail(orderId: string) {
  router.push(`/checkout/${orderId}`)
}
</script>

<template>
  <div class="page page--with-nav">
    <SiteHeader title="我的订单" eyebrow="按时间排序" />

    <div class="page__content">
      <section class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">订单概览</p>
              <h3 class="info-card__title">后端订单</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="orders" :size="14" />
              {{ store.unpaidOrders.value }} 笔待支付
            </span>
          </div>
          <div class="detail-stats__row">
            <article class="detail-stat">
              <p class="detail-stat__label">待支付</p>
              <p class="detail-stat__value">{{ unpaidOrders.length }}</p>
            </article>
            <article class="detail-stat">
              <p class="detail-stat__label">已支付</p>
              <p class="detail-stat__value">{{ paidOrders.length }}</p>
            </article>
            <article class="detail-stat">
              <p class="detail-stat__label">已完成</p>
              <p class="detail-stat__value">{{ completedOrders.length }}</p>
            </article>
            <article class="detail-stat">
              <p class="detail-stat__label">已取消</p>
              <p class="detail-stat__value">{{ canceledOrders.length }}</p>
            </article>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="tabs tabs--scroll">
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

      <section class="section">
        <p v-if="error" class="field__hint field__hint--danger">{{ error }}</p>

        <div v-if="store.state.loading.orders" class="empty-state panel">
          <h3 class="empty-state__title">正在加载订单</h3>
          <p class="empty-state__text">正在从后端同步订单记录。</p>
        </div>

        <div v-else-if="visibleOrders.length" class="timeline-list">
          <article v-for="order in visibleOrders" :key="order.id" class="timeline-item panel order-card">
            <div class="timeline-item__top">
              <div class="merchant-card__metrics order-card__header">
                <img :src="order.merchantImage" :alt="order.merchantName" width="56" height="56" class="order-card__image" />
                <div>
                  <h3 class="timeline-item__name">{{ order.merchantName }}</h3>
                  <p class="timeline-item__meta">{{ formatOrderTime(order.createdAt) }}</p>
                </div>
              </div>
              <span :class="statusClass(order.status)">{{ formatOrderStatus(order.status) }}</span>
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

            <div class="order-summary__total order-card__total">
              <span>合计</span>
              <strong>{{ formatCny(order.total) }}</strong>
            </div>

            <div class="auth-card__footer order-card__footer">
              <span class="muted">{{ order.addressName || '收货人待同步' }} · {{ order.addressPhone || '电话待同步' }}</span>
              <div class="chip-row">
                <button type="button" class="secondary-button" @click="goDetail(order.id)">
                  查看详情
                </button>
                <button
                  v-if="order.status === 'unpaid'"
                  type="button"
                  class="primary-button"
                  @click="goPayment(order.id)"
                >
                  去支付
                </button>
              </div>
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
