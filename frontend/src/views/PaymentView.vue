<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderStatus, formatOrderTime } from '@/utils/format'
import type { PaymentMethod } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useHungryStore()
const error = ref('')

const orderId = computed(() => String(route.params.orderId || ''))
const order = computed(() => store.getOrder(orderId.value))
const expanded = ref(true)
const selectedMethod = computed<PaymentMethod>({
  get() {
    return store.state.paymentMethod
  },
  set(value) {
    store.setPaymentMethod(value)
  },
})
const isPaying = computed(() => store.state.loading.action)

onMounted(async () => {
  if (!store.isAuthenticated.value) {
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    error.value = ''
    await store.fetchOrder(orderId.value)
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
})

function statusClass(status: string) {
  if (status === 'canceled') {
    return 'status-pill status-pill--danger'
  }
  if (status === 'paid' || status === 'completed') {
    return 'status-pill status-pill--success'
  }
  return 'status-pill status-pill--warning'
}

async function confirmPayment() {
  if (!order.value) {
    return
  }
  if (order.value.status !== 'unpaid') {
    router.push('/orders')
    return
  }
  try {
    error.value = ''
    await store.confirmPayment(order.value.id, selectedMethod.value)
    router.push('/orders')
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

function back() {
  if (order.value) {
    router.push(`/checkout/${order.value.id}`)
    return
  }
  router.push('/orders')
}
</script>

<template>
  <div class="page page--bare" style="background: var(--bg)">
    <SiteHeader title="在线支付" backable @back="back" />

    <template v-if="order">
      <!-- 订单金额 -->
      <section class="card" style="text-align: center">
        <p class="card__sub">订单金额</p>
        <p style="margin: 8px 0 0; font-size: 32px; font-weight: 700; color: var(--text)">{{ formatCny(order.total) }}</p>
        <p class="card__sub" style="margin-top: 6px">{{ order.merchantName }} · {{ formatOrderTime(order.createdAt) }}</p>
      </section>

      <!-- 明细（可折叠） -->
      <section class="card">
        <button type="button" style="display: flex; align-items: center; justify-content: space-between; width: 100%" @click="expanded = !expanded">
          <span style="font-size: 14px; color: var(--text-2)">{{ expanded ? '收起明细' : '展开明细' }}</span>
          <UiIcon :name="expanded ? 'chevronDown' : 'chevronRight'" :size="18" style="color: var(--text-3)" />
        </button>
        <transition name="fade">
          <div v-if="expanded" style="margin-top: 8px">
            <div v-for="item in order.items" :key="item.id" class="row-line">
              <span>{{ item.name }} x {{ item.quantity }}</span>
              <span>{{ formatCny(item.price * item.quantity) }}</span>
            </div>
            <div class="row-line">
              <span>配送费</span>
              <span>{{ formatCny(order.deliveryFee) }}</span>
            </div>
          </div>
        </transition>
      </section>

      <!-- 支付方式 -->
      <section class="card">
        <p class="card__title">选择支付方式</p>
        <div style="display: flex; flex-direction: column; gap: 10px; margin-top: 12px">
          <button
            type="button"
            class="pay-method"
            :class="{ 'pay-method--active': selectedMethod === 'alipay' }"
            @click="selectedMethod = 'alipay'"
          >
            <img class="pay-method__icon" src="/eleme/alipay.png" alt="支付宝" />
            <div class="pay-method__info">
              <p class="pay-method__name">支付宝</p>
              <p class="pay-method__sub">提交后调用后端支付接口</p>
            </div>
            <span v-if="selectedMethod === 'alipay'" class="status-pill status-pill--success">
              <UiIcon name="check" :size="14" />
            </span>
          </button>
          <button
            type="button"
            class="pay-method"
            :class="{ 'pay-method--active': selectedMethod === 'wechat' }"
            @click="selectedMethod = 'wechat'"
          >
            <img class="pay-method__icon" src="/eleme/wechat.png" alt="微信支付" />
            <div class="pay-method__info">
              <p class="pay-method__name">微信支付</p>
              <p class="pay-method__sub">扫码和快捷支付都可展示</p>
            </div>
            <span v-if="selectedMethod === 'wechat'" class="status-pill status-pill--success">
              <UiIcon name="check" :size="14" />
            </span>
          </button>
        </div>
      </section>

      <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>

      <div class="form-actions">
        <button type="button" class="primary-button primary-button--accent" :disabled="isPaying" @click="confirmPayment">
          {{ isPaying ? '支付处理中' : order.status === 'unpaid' ? `确认支付 ${formatCny(order.total)}` : '返回订单列表' }}
        </button>
      </div>
    </template>

    <section v-else class="empty-state">
      <p class="empty-state__title">没有找到该订单</p>
      <p class="empty-state__text">{{ error || '订单可能已完成支付或不存在，请回订单列表查看。' }}</p>
      <button type="button" class="primary-button" style="margin-top: 16px" @click="router.push('/orders')">去订单列表</button>
    </section>
  </div>
</template>
