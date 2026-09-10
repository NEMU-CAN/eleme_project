<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderStatus, formatOrderTime } from '@/utils/format'
import type { OrderStatus } from '@/types'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()
const filter = ref<'all' | OrderStatus>(initialFilter())
const error = ref('')

function initialFilter(): 'all' | OrderStatus {
  const raw = route.query.filter
  if (raw === 'unpaid' || raw === 'paid' || raw === 'completed' || raw === 'canceled') {
    return raw
  }
  return 'all'
}

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'unpaid', label: '待支付' },
  { key: 'paid', label: '配送中' },
  { key: 'completed', label: '已完成' },
  { key: 'canceled', label: '已取消' },
] as const

const sortedOrders = computed(() =>
  [...store.state.orders].sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt)),
)

const visibleOrders = computed(() => {
  if (filter.value === 'all') {
    return sortedOrders.value
  }
  return sortedOrders.value.filter((order) => order.status === filter.value)
})

watch(filter, (value) => {
  router.replace({ query: value === 'all' ? {} : { filter: value } })
})

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

function goBack() {
  router.push('/me')
}
</script>

<template>
  <div class="page page--bare" style="background: var(--bg)">
    <SiteHeader title="我的订单" backable @back="goBack" />

    <!-- 状态筛选 -->
    <div class="filter-bar" style="border-bottom: 1px solid var(--line)">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="filter-chip"
        :class="{ 'filter-chip--active': filter === tab.key }"
        @click="filter = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>

    <div v-if="store.state.loading.orders" class="empty-state">
      <p class="empty-state__text">正在加载订单…</p>
    </div>

    <div v-else-if="visibleOrders.length" style="padding-bottom: 20px">
      <article v-for="order in visibleOrders" :key="order.id" class="order-card">
        <div class="order-card__head">
          <img :src="order.merchantImage" :alt="order.merchantName" class="order-card__image" />
          <div style="flex: 1; min-width: 0">
            <p class="order-card__name">{{ order.merchantName }}</p>
            <p class="order-card__time">{{ formatOrderTime(order.createdAt) }}</p>
          </div>
          <span :class="statusClass(order.status)">{{ formatOrderStatus(order.status) }}</span>
        </div>

        <div class="order-card__lines">
          <div v-for="item in order.items" :key="item.id" class="row-line">
            <span>{{ item.name }} x {{ item.quantity }}</span>
            <span>{{ formatCny(item.price * item.quantity) }}</span>
          </div>
          <div class="row-line">
            <span>配送费</span>
            <span>{{ formatCny(order.deliveryFee) }}</span>
          </div>
        </div>

        <div class="total-line" style="border-top: none; padding-top: 0; margin-top: 0">
          <span>合计</span>
          <strong>{{ formatCny(order.total) }}</strong>
        </div>

        <div class="order-card__footer">
          <span class="order-card__addr">{{ order.addressName || '收货人待同步' }} · {{ order.addressPhone || '电话待同步' }}</span>
          <div style="display: inline-flex; gap: 8px">
            <button type="button" class="secondary-button" style="min-height: 34px; font-size: 13px" @click="goDetail(order.id)">
              查看详情
            </button>
            <button
              v-if="order.status === 'unpaid'"
              type="button"
              class="primary-button primary-button--accent"
              style="min-height: 34px; font-size: 13px"
              @click="goPayment(order.id)"
            >
              去支付
            </button>
          </div>
        </div>
      </article>
    </div>

    <div v-else class="empty-state">
      <p class="empty-state__title">暂无订单</p>
      <p class="empty-state__text">你还没有相关订单，先去首页点一单吧。</p>
    </div>
  </div>
</template>
