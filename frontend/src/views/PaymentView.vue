<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderTime } from '@/utils/format'
import type { OrderRecord, PaymentMethod } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useHungryStore()
const error = ref('')
const expanded = ref(false)

const orderIds = computed(() => {
  const batch = String(route.query.batch || '').split(',').filter(Boolean)
  return [...new Set([String(route.params.orderId || ''), ...batch].filter(Boolean))]
})
const orders = computed(() => orderIds.value.map((id) => store.getOrder(id)).filter((item): item is OrderRecord => Boolean(item)))
const order = computed(() => orders.value[0] ?? null)
const totalAmount = computed(() => orders.value.reduce((sum, item) => sum + item.total, 0))
const totalItems = computed(() => orders.value.reduce((sum, item) => sum + item.itemCount, 0))
const selectedMethod = computed<PaymentMethod>({ get: () => store.state.paymentMethod, set: (value) => store.setPaymentMethod(value) })
const isPaying = computed(() => store.state.loading.action)

onMounted(async () => {
  if (!store.isAuthenticated.value) return void router.replace({ path: '/login', query: { redirect: route.fullPath } })
  try {
    error.value = ''
    await Promise.all(orderIds.value.map((id) => store.fetchOrder(id)))
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
})

async function confirmPayment() {
  if (!orders.value.length) return
  try {
    error.value = ''
    for (const item of orders.value) if (item.status === 'unpaid') await store.confirmPayment(item.id, selectedMethod.value)
    router.push('/orders')
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

function back() {
  router.push('/cart')
}
</script>

<template>
  <div class="page page--bare ele-pay-page">
    <header class="ele-pay-header"><button @click="back"><UiIcon name="chevronLeft" :size="22" /></button><h1>确认支付</h1><span /></header>

    <template v-if="orders.length">
      <section class="ele-pay-hero">
        <p>{{ orders.length > 1 ? `${orders.length}笔订单合并支付` : '订单支付金额' }}</p>
        <strong>{{ formatCny(totalAmount) }}</strong>
        <span>请在 15 分钟内完成支付</span>
      </section>

      <main class="ele-pay-content">
        <section class="ele-pay-order-card">
          <button class="ele-pay-order-card__summary" @click="expanded = !expanded">
            <div><b>{{ orders.length > 1 ? `${orders.length}家商户` : order?.merchantName }}</b><span>共 {{ totalItems }} 件商品</span></div>
            <UiIcon :name="expanded ? 'chevronDown' : 'chevronRight'" :size="18" />
          </button>
          <div v-if="expanded" class="ele-pay-details">
            <template v-for="entry in orders" :key="entry.id">
              <div class="ele-pay-details__merchant"><b>{{ entry.merchantName }}</b><small>{{ formatOrderTime(entry.createdAt) }}</small></div>
              <div v-for="item in entry.items" :key="`${entry.id}-${item.id}`" class="ele-pay-details__line"><span>{{ item.name }} × {{ item.quantity }}</span><b>{{ formatCny(item.price * item.quantity) }}</b></div>
              <div class="ele-pay-details__line"><span>配送费</span><b>{{ formatCny(entry.deliveryFee) }}</b></div>
            </template>
          </div>
        </section>

        <section class="ele-pay-methods">
          <h2>支付方式</h2>
          <button :class="{ active: selectedMethod === 'alipay' }" @click="selectedMethod = 'alipay'">
            <img src="/eleme/alipay.png" alt="支付宝" /><div><b>支付宝</b><span>推荐使用支付宝安全支付</span></div><i><UiIcon v-if="selectedMethod === 'alipay'" name="check" :size="13" /></i>
          </button>
          <button :class="{ active: selectedMethod === 'wechat' }" @click="selectedMethod = 'wechat'">
            <img src="/eleme/wechat.png" alt="微信支付" /><div><b>微信支付</b><span>亿万用户的安全选择</span></div><i><UiIcon v-if="selectedMethod === 'wechat'" name="check" :size="13" /></i>
          </button>
        </section>
        <p class="ele-pay-security"><UiIcon name="check" :size="13" /> 支付信息已加密保护</p>
        <p v-if="error" class="ele-store-error">{{ error }}</p>
      </main>

      <footer class="ele-pay-footer"><button :disabled="isPaying" @click="confirmPayment">{{ isPaying ? '支付处理中…' : `确认支付 ${formatCny(totalAmount)}` }}</button></footer>
    </template>

    <section v-else class="ele-empty"><div><b>没有找到待支付订单</b><p>{{ error || '请返回订单列表重新查看' }}</p><button class="primary-button" @click="router.push('/orders')">去订单列表</button></div></section>
  </div>
</template>
