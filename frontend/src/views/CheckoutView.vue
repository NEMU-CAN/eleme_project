<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderTime, maskPhone } from '@/utils/format'
import type { PaymentMethod } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useHungryStore()

// 结算页依据路由里的订单号读取当前订单。
const orderId = computed(() => String(route.params.orderId || ''))
// 当前待结算订单。
const order = computed(() => store.getOrder(orderId.value))
// 结算页再反查一次商家信息，用于展示店铺名称和返回路径。
const merchant = computed(() => store.merchants.find((item) => item.id === order.value?.merchantId))

// 返回商家详情页；如果订单不存在，就回到商家列表。
function goBack() {
  if (order.value) {
    router.push(`/merchant/${order.value.merchantId}`)
    return
  }
  router.push('/businesses')
}

// 支付渠道列表，对应课件里的支付宝和微信支付。
const paymentMethods: Array<{ id: PaymentMethod; title: string; image: string; subtitle: string }> = [
  { id: 'alipay', title: '支付宝', image: '/eleme/alipay.png', subtitle: '即时确认，使用最顺手' },
  { id: 'wechat', title: '微信支付', image: '/eleme/wechat.png', subtitle: '扫码和快捷支付都支持' },
]

// 当前选中的支付方式，直接跟订单草稿同步。
const selectedPayment = computed<PaymentMethod>({
  get() {
    return order.value?.paymentMethod ?? 'alipay'
  },
  set(value) {
    if (order.value) {
      order.value.paymentMethod = value
    }
  },
})

// 选择某个支付方式。
function choosePayment(method: PaymentMethod) {
  selectedPayment.value = method
}

// 进入支付页，完成最终支付动作。
function proceed() {
  if (!order.value) {
    return
  }

  router.push(`/payment/${order.value.id}`)
}
</script>

<template>
  <div class="page page--bare">
    <!-- 结算页头部。 -->
    <SiteHeader title="确认订单" eyebrow="结算前最后一步" backable @back="goBack" />

    <template v-if="order && merchant">
      <!-- 订单基础信息与金额汇总。 -->
      <section class="page__content">
        <div class="order-summary panel">
          <div class="order-summary__head">
            <div>
              <p class="eyebrow">订单信息</p>
              <h3 class="order-summary__title">{{ merchant.name }}</h3>
              <p class="order-summary__text">下单时间 {{ formatOrderTime(order.createdAt) }}</p>
            </div>
            <span class="status-pill status-pill--warning">待支付</span>
          </div>

          <div class="info-card panel--soft" style="padding: 12px">
            <p class="order-summary__text">订单配送至</p>
            <h4 class="info-card__title" style="margin-top: 4px">{{ order.addressDetail }}</h4>
            <p class="info-card__text">{{ order.addressName }} · {{ maskPhone(order.addressPhone) }}</p>
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

      <!-- 支付方式选择区。 -->
      <section class="page__content section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">支付方式</p>
              <h3 class="info-card__title">选择支付渠道</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="wallet" :size="14" />
              本地 mock
            </span>
          </div>
          <div class="timeline-list">
            <button
              v-for="method in paymentMethods"
              :key="method.id"
              type="button"
              class="timeline-item panel--soft"
              :class="{ 'sort-chip--active': selectedPayment === method.id }"
              @click="choosePayment(method.id)"
            >
              <div class="timeline-item__top">
                <div class="merchant-card__metrics" style="align-items: center">
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

      <!-- 去支付按钮。 -->
      <section class="page__content section">
        <button type="button" class="primary-button" style="width: 100%" @click="proceed">去支付</button>
      </section>
    </template>

    <section v-else class="page__content">
      <div class="empty-state panel">
        <h3 class="empty-state__title">没有找到待结算订单</h3>
        <p class="empty-state__text">先去商家页选几样菜，结算页就会出现。</p>
        <button type="button" class="primary-button" style="margin-top: 16px" @click="router.push('/businesses')">
          去商家列表
        </button>
      </div>
    </section>
  </div>
</template>
