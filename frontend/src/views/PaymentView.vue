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
const detailRows = computed(() => order.value?.items ?? [])

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
  <div class="page page--bare">
    <SiteHeader title="在线支付" eyebrow="完成订单收尾" backable @back="back" />

    <template v-if="order">
      <section class="page__content">
        <div class="order-summary panel">
          <div class="order-summary__head">
            <div>
              <p class="eyebrow">订单信息</p>
              <h3 class="order-summary__title">{{ order.merchantName }}</h3>
              <p class="order-summary__text">创建时间 {{ formatOrderTime(order.createdAt) }}</p>
            </div>
            <span :class="statusClass(order.status)">
              <UiIcon name="clock" :size="14" />
              {{ formatOrderStatus(order.status) }}
            </span>
          </div>

          <button type="button" class="timeline-item panel--soft payment-summary" @click="expanded = !expanded">
            <div class="timeline-item__top">
              <div>
                <p class="timeline-item__name">订单金额 {{ formatCny(order.total) }}</p>
                <p class="timeline-item__meta">{{ expanded ? '收起明细' : '展开明细' }}</p>
              </div>
              <UiIcon :name="expanded ? 'chevronDown' : 'chevronRight'" :size="18" />
            </div>
          </button>

          <transition name="fade">
            <div v-if="expanded" class="timeline-list">
              <div class="timeline-item panel--soft">
                <div class="timeline-item__list">
                  <div v-for="item in detailRows" :key="item.id" class="timeline-line">
                    <span>{{ item.name }} x {{ item.quantity }}</span>
                    <span>{{ formatCny(item.price * item.quantity) }}</span>
                  </div>
                  <div class="timeline-line">
                    <span>配送费</span>
                    <span>{{ formatCny(order.deliveryFee) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </section>

      <section class="page__content section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">支付方式</p>
              <h3 class="info-card__title">请选择支付方式</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="wallet" :size="14" />
              后端支付接口
            </span>
          </div>
          <div class="timeline-list">
            <button
              type="button"
              class="timeline-item panel--soft payment-method"
              :class="{ 'payment-method--active': selectedMethod === 'alipay' }"
              @click="selectedMethod = 'alipay'"
            >
              <div class="timeline-item__top">
                <div class="merchant-card__metrics payment-method__content">
                  <img src="/eleme/alipay.png" alt="支付宝" width="112" height="32" />
                  <div>
                    <p class="timeline-item__name">支付宝</p>
                    <p class="timeline-item__meta">提交后调用后端支付接口</p>
                  </div>
                </div>
                <span v-if="selectedMethod === 'alipay'" class="status-pill status-pill--success">
                  <UiIcon name="check" :size="14" />
                  已选中
                </span>
              </div>
            </button>
            <button
              type="button"
              class="timeline-item panel--soft payment-method"
              :class="{ 'payment-method--active': selectedMethod === 'wechat' }"
              @click="selectedMethod = 'wechat'"
            >
              <div class="timeline-item__top">
                <div class="merchant-card__metrics payment-method__content">
                  <img src="/eleme/wechat.png" alt="微信支付" width="112" height="32" />
                  <div>
                    <p class="timeline-item__name">微信支付</p>
                    <p class="timeline-item__meta">扫码和快捷支付都可展示</p>
                  </div>
                </div>
                <span v-if="selectedMethod === 'wechat'" class="status-pill status-pill--success">
                  <UiIcon name="check" :size="14" />
                  已选中
                </span>
              </div>
            </button>
          </div>
        </div>
      </section>

      <section class="page__content section">
        <p v-if="error" class="field__hint field__hint--danger">{{ error }}</p>
        <button type="button" class="primary-button checkout-action" :disabled="isPaying" @click="confirmPayment">
          {{ isPaying ? '支付处理中' : order.status === 'unpaid' ? `确认支付 ${formatCny(order.total)}` : '返回订单列表' }}
        </button>
      </section>
    </template>

    <section v-else class="page__content">
      <div class="empty-state panel">
        <h3 class="empty-state__title">没有找到该订单</h3>
        <p class="empty-state__text">{{ error || '订单可能已完成支付或不存在，请回订单列表查看。' }}</p>
        <button type="button" class="primary-button" style="margin-top: 16px" @click="router.push('/businesses')">
          去商家列表
        </button>
      </div>
    </section>
  </div>
</template>
