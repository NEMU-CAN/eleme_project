<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderStatus, formatOrderTime, maskPhone } from '@/utils/format'
import type { PaymentMethod } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useHungryStore()
const error = ref('')

const orderId = computed(() => String(route.params.orderId || ''))
const order = computed(() => store.getOrder(orderId.value))
const selectedPayment = computed<PaymentMethod>({
  get() {
    return store.state.paymentMethod
  },
  set(value) {
    store.setPaymentMethod(value)
  },
})
const paymentMethods: Array<{ id: PaymentMethod; title: string; image: string; subtitle: string }> = [
  { id: 'alipay', title: '支付宝', image: '/eleme/alipay.png', subtitle: '即时确认，使用最顺手' },
  { id: 'wechat', title: '微信支付', image: '/eleme/wechat.png', subtitle: '扫码和快捷支付都支持' },
]

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

function goBack() {
  if (order.value) {
    router.push(`/merchant/${order.value.businessId}`)
    return
  }
  router.push('/')
}

function changeAddress() {
  router.push('/addresses')
}

function statusClass(status: string) {
  if (status === 'canceled') {
    return 'status-pill status-pill--danger'
  }
  if (status === 'paid' || status === 'completed') {
    return 'status-pill status-pill--success'
  }
  return 'status-pill status-pill--warning'
}

function proceed() {
  if (!order.value) {
    return
  }
  if (order.value.status !== 'unpaid') {
    router.push('/orders')
    return
  }
  router.push(`/payment/${order.value.id}`)
}
</script>

<template>
  <div class="page page--bare" style="background: var(--bg)">
    <SiteHeader title="确认订单" backable @back="goBack" />

    <template v-if="order">
      <!-- 收货地址 -->
      <section class="card">
        <div style="display: flex; align-items: center; gap: 10px">
          <UiIcon name="pin" :size="18" style="color: var(--primary)" />
          <div style="flex: 1; min-width: 0">
            <p class="card__title">{{ order.addressName || '地址信息待同步' }}</p>
            <p class="card__sub">{{ order.addressDetail || '详细地址待同步' }} · {{ maskPhone(order.addressPhone) }}</p>
          </div>
          <button type="button" style="color: var(--text-3); font-size: 13px" @click="changeAddress">
            更改地址
            <UiIcon name="chevronRight" :size="14" />
          </button>
        </div>
      </section>

      <!-- 商家与菜品 -->
      <section class="card">
        <div style="display: flex; align-items: center; gap: 10px; padding-bottom: 10px; border-bottom: 1px solid var(--line)">
          <img :src="order.merchantImage" :alt="order.merchantName" width="40" height="40" style="border-radius: 8px; object-fit: cover" />
          <div style="flex: 1; min-width: 0">
            <p class="card__title">{{ order.merchantName }}</p>
            <p class="card__sub">{{ formatOrderTime(order.createdAt) }}</p>
          </div>
          <span :class="statusClass(order.status)">
            <UiIcon name="clock" :size="14" />
            {{ formatOrderStatus(order.status) }}
          </span>
        </div>

        <div style="padding: 10px 0">
          <div v-for="item in order.items" :key="item.id" class="row-line">
            <span>{{ item.name }} x {{ item.quantity }}</span>
            <span>{{ formatCny(item.price * item.quantity) }}</span>
          </div>
          <div class="row-line">
            <span>配送费</span>
            <span>{{ formatCny(order.deliveryFee) }}</span>
          </div>
        </div>

        <div class="total-line">
          <span>合计</span>
          <strong>{{ formatCny(order.total) }}</strong>
        </div>
      </section>

      <!-- 支付方式 -->
      <section class="card">
        <p class="card__title">支付方式</p>
        <div style="display: flex; flex-direction: column; gap: 10px; margin-top: 12px">
          <button
            v-for="method in paymentMethods"
            :key="method.id"
            type="button"
            class="pay-method"
            :class="{ 'pay-method--active': selectedPayment === method.id }"
            @click="selectedPayment = method.id"
          >
            <img class="pay-method__icon" :src="method.image" :alt="method.title" />
            <div class="pay-method__info">
              <p class="pay-method__name">{{ method.title }}</p>
              <p class="pay-method__sub">{{ method.subtitle }}</p>
            </div>
            <span v-if="selectedPayment === method.id" class="status-pill status-pill--success">
              <UiIcon name="check" :size="14" />
            </span>
          </button>
        </div>
      </section>

      <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>

      <div class="form-actions">
        <button type="button" class="primary-button primary-button--accent" :disabled="store.state.loading.orders" @click="proceed">
          {{ store.state.loading.orders ? '正在同步订单' : order.status === 'unpaid' ? `去支付 ${formatCny(order.total)}` : '返回订单列表' }}
        </button>
      </div>
    </template>

    <section v-else class="empty-state">
      <p class="empty-state__title">没有找到待结算订单</p>
      <p class="empty-state__text">{{ error || '订单可能已被支付或不存在，请返回订单列表查看。' }}</p>
      <button type="button" class="primary-button" style="margin-top: 16px" @click="router.push('/')">去首页</button>
    </section>
  </div>
</template>
