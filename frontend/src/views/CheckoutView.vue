<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatGender, formatOrderStatus, formatOrderTime, maskPhone } from '@/utils/format'
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
  router.push('/businesses')
}

function choosePayment(method: PaymentMethod) {
  selectedPayment.value = method
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
  <div class="page page--bare">
    <SiteHeader title="确认订单" eyebrow="结算前最后一步" backable @back="goBack" />

    <template v-if="order">
      <section class="page__content">
        <div class="order-summary panel">
          <div class="order-summary__head">
            <div>
              <p class="eyebrow">订单信息</p>
              <h3 class="order-summary__title">{{ order.merchantName }}</h3>
              <p class="order-summary__text">下单时间 {{ formatOrderTime(order.createdAt) }}</p>
            </div>
            <span :class="statusClass(order.status)">
              <UiIcon name="clock" :size="14" />
              {{ formatOrderStatus(order.status) }}
            </span>
          </div>

          <div class="info-card panel--soft checkout-address">
            <div class="info-card__header">
              <div>
                <p class="eyebrow">收货信息</p>
                <h4 class="info-card__title">{{ order.addressName || '地址信息待同步' }}</h4>
              </div>
              <span class="status-pill">
                <UiIcon name="pin" :size="14" />
                {{ formatGender(order.receiverGender) }}
              </span>
            </div>
            <p class="info-card__text">
              {{ order.addressDetail || '详细地址待同步' }}
              <br />
              {{ maskPhone(order.addressPhone) }}
            </p>
          </div>

          <div class="order-summary__list">
            <div v-for="item in order.items" :key="item.id" class="order-summary__line">
              <span>{{ item.name }} x {{ item.quantity }}</span>
              <span>{{ formatCny(item.price * item.quantity) }}</span>
            </div>
            <div class="order-summary__line">
              <span>配送费</span>
              <span>{{ formatCny(order.deliveryFee) }}</span>
            </div>
          </div>

          <div class="order-summary__total">
            <span>合计</span>
            <strong>{{ formatCny(order.total) }}</strong>
          </div>
        </div>
      </section>

      <section class="page__content section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">支付方式</p>
              <h3 class="info-card__title">选择支付渠道</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="wallet" :size="14" />
              后端订单
            </span>
          </div>
          <div class="timeline-list">
            <button
              v-for="method in paymentMethods"
              :key="method.id"
              type="button"
              class="timeline-item panel--soft payment-method"
              :class="{ 'payment-method--active': selectedPayment === method.id }"
              @click="choosePayment(method.id)"
            >
              <div class="timeline-item__top">
                <div class="merchant-card__metrics payment-method__content">
                  <img :src="method.image" :alt="method.title" width="112" height="32" />
                  <div>
                    <p class="timeline-item__name">{{ method.title }}</p>
                    <p class="timeline-item__meta">{{ method.subtitle }}</p>
                  </div>
                </div>
                <span v-if="selectedPayment === method.id" class="status-pill status-pill--success">
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
        <button type="button" class="primary-button checkout-action" :disabled="store.state.loading.orders" @click="proceed">
          {{ store.state.loading.orders ? '正在同步订单' : order.status === 'unpaid' ? '去支付' : '返回订单列表' }}
        </button>
      </section>
    </template>

    <section v-else class="page__content">
      <div class="empty-state panel">
        <h3 class="empty-state__title">没有找到待结算订单</h3>
        <p class="empty-state__text">{{ error || '订单可能已被支付或不存在，请返回订单列表查看。' }}</p>
        <button type="button" class="primary-button" style="margin-top: 16px" @click="router.push('/businesses')">
          去商家列表
        </button>
      </div>
    </section>
  </div>
</template>
