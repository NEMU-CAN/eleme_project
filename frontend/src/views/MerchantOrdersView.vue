<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderTime } from '@/utils/format'
import type { OrderRecord, OrderStatus } from '@/types'

type MerchantOrderFilter = 'all' | 'paid' | 'completed'

const router = useRouter()
const store = useHungryStore()
const filter = ref<MerchantOrderFilter>('all')
const error = ref('')
const expandedIds = ref<string[]>([])
const detailLoadingId = ref('')
const completingId = ref('')

const tabs: Array<{ key: MerchantOrderFilter; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'paid', label: '待处理' },
  { key: 'completed', label: '已完成' },
]

const sortedOrders = computed(() =>
  [...store.state.orders].sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt)),
)
const visibleOrders = computed(() => filter.value === 'all'
  ? sortedOrders.value
  : sortedOrders.value.filter((order) => order.status === filter.value))
const pendingCount = computed(() => store.state.orders.filter((order) => order.status === 'paid').length)

function statusLabel(status: OrderStatus) {
  if (status === 'paid') {
    return '待处理'
  }
  if (status === 'completed') {
    return '已完成'
  }
  if (status === 'canceled') {
    return '已取消'
  }
  return '未支付'
}

function statusClass(status: OrderStatus) {
  if (status === 'completed') {
    return 'status-pill status-pill--success'
  }
  if (status === 'paid' || status === 'unpaid') {
    return 'status-pill status-pill--warning'
  }
  return 'status-pill status-pill--danger'
}

function isExpanded(orderId: string) {
  return expandedIds.value.includes(orderId)
}

async function refreshOrders() {
  try {
    error.value = ''
    await store.loadOrders()
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

async function toggleDetails(order: OrderRecord) {
  if (isExpanded(order.id)) {
    expandedIds.value = expandedIds.value.filter((id) => id !== order.id)
    return
  }

  try {
    detailLoadingId.value = order.id
    error.value = ''
    if (!order.items.length) {
      await store.fetchOrder(order.id)
    }
    expandedIds.value = [...expandedIds.value, order.id]
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    detailLoadingId.value = ''
  }
}

async function completeOrder(order: OrderRecord) {
  const confirmed = window.confirm(`确认订单 ${order.orderNo} 已完成吗？`)
  if (!confirmed) {
    return
  }

  try {
    completingId.value = order.id
    error.value = ''
    await store.completeBusinessOrder(order.id)
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    completingId.value = ''
  }
}

onMounted(() => {
  if (!store.isAuthenticated.value) {
    router.replace({ path: '/login', query: { redirect: '/merchant-orders' } })
    return
  }
  if (store.state.user?.role !== 1 && store.state.user?.role !== 2) {
    router.replace('/me')
    return
  }
  void refreshOrders()
})
</script>

<template>
  <div class="merchant-orders-page">
    <SiteHeader title="订单管理" backable @back="router.push('/merchant-center')" />

    <section class="merchant-orders-summary">
      <div>
        <strong>{{ store.state.orders.length }}</strong>
        <span>全部订单</span>
      </div>
      <div>
        <strong>{{ pendingCount }}</strong>
        <span>待处理</span>
      </div>
      <button type="button" :disabled="store.state.loading.orders" @click="refreshOrders">
        <UiIcon name="clock" :size="16" />
        {{ store.state.loading.orders ? '刷新中' : '刷新订单' }}
      </button>
    </section>

    <div class="filter-bar merchant-orders-filter">
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

    <div v-if="store.state.loading.orders && !store.state.orders.length" class="empty-state">
      <p class="empty-state__text">正在同步订单…</p>
    </div>

    <div v-else-if="visibleOrders.length" class="merchant-orders-list">
      <article v-for="order in visibleOrders" :key="order.id" class="merchant-order-card">
        <div class="merchant-order-card__head">
          <div>
            <strong>{{ order.businessName || '我的门店' }}</strong>
            <p>订单号 {{ order.orderNo }}</p>
          </div>
          <span :class="statusClass(order.status)">{{ statusLabel(order.status) }}</span>
        </div>

        <div class="merchant-order-card__customer">
          <div>
            <span>顾客</span>
            <strong>{{ order.userNickname || '匿名用户' }} · {{ order.userPhone || '暂无电话' }}</strong>
          </div>
          <div>
            <span>配送地址</span>
            <strong>{{ order.receiverName }} · {{ order.receiverTel }}</strong>
            <p>{{ order.receiverAddress }}</p>
          </div>
        </div>

        <div v-if="isExpanded(order.id)" class="merchant-order-card__details">
          <div v-for="item in order.items" :key="item.id" class="row-line">
            <span>{{ item.name }} × {{ item.quantity }}</span>
            <span>{{ formatCny(item.subtotal ?? item.price * item.quantity) }}</span>
          </div>
          <p v-if="!order.items.length">暂无商品明细</p>
        </div>

        <div class="merchant-order-card__meta">
          <span>{{ formatOrderTime(order.createdAt) }} · 共 {{ order.itemCount }} 件</span>
          <strong>{{ formatCny(order.total) }}</strong>
        </div>

        <div class="merchant-order-card__actions">
          <button type="button" class="secondary-button" :disabled="detailLoadingId === order.id" @click="toggleDetails(order)">
            {{ detailLoadingId === order.id ? '加载中' : isExpanded(order.id) ? '收起明细' : '查看商品' }}
          </button>
          <button
            v-if="order.status === 'paid'"
            type="button"
            class="primary-button"
            :disabled="completingId === order.id || store.state.loading.action"
            @click="completeOrder(order)"
          >
            {{ completingId === order.id ? '处理中' : '确认完成' }}
          </button>
        </div>
      </article>
    </div>

    <div v-else class="empty-state">
      <p class="empty-state__title">暂无相关订单</p>
      <p class="empty-state__text">当前筛选条件下没有需要处理的订单。</p>
    </div>
  </div>
</template>
