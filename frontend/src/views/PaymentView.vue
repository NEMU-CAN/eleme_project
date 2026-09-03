<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny, formatOrderTime } from '@/utils/format'
import type { PaymentMethod } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useHungryStore()

// 支付页根据路由参数定位到目标订单。
const orderId = computed(() => String(route.params.orderId || ''))
// 当前支付中的订单。
const order = computed(() => store.getOrder(orderId.value))
// 订单明细展开状态，模拟课件里的明细收起/展开效果。
const expanded = ref(true)
// 当前选中的支付方式。
const selectedMethod = ref<PaymentMethod>('alipay')

// 订单变化时，把支付方式同步到当前订单默认值。
watch(
  order,
  (value) => {
    selectedMethod.value = value?.paymentMethod ?? 'alipay'
  },
  { immediate: true },
)

// 订单商品明细列表，便于模板循环展示。
const detailRows = computed(() => order.value?.items ?? [])

// 确认支付后，把订单状态改成已支付并返回订单页。
function confirmPayment() {
  if (!order.value) {
    return
  }

  store.confirmPayment(order.value.id, selectedMethod.value)
  router.push('/orders')
}

// 返回结算页；没有订单时直接回订单列表。
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
    <!-- 支付页头部。 -->
    <SiteHeader title="在线支付" eyebrow="完成订单收尾" backable @back="back" />

    <template v-if="order">
      <!-- 订单金额与明细展开区。 -->
      <section class="page__content">
        <div class="order-summary panel">
          <div class="order-summary__head">
            <div>
              <p class="eyebrow">订单信息</p>
              <h3 class="order-summary__title">{{ order.merchantName }}</h3>
              <p class="order-summary__text">创建时间 {{ formatOrderTime(order.createdAt) }}</p>
            </div>
            <span class="status-pill status-pill--warning">待支付</span>
          </div>

          <button type="button" class="timeline-item panel--soft" @click="expanded = !expanded">
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

      <!-- 支付方式选择区。 -->
      <section class="page__content section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">支付方式</p>
              <h3 class="info-card__title">请选择支付方式</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="wallet" :size="14" />
              仅前端展示
            </span>
          </div>
          <div class="timeline-list">
            <button
              type="button"
              class="timeline-item panel--soft"
              :class="{ 'sort-chip--active': selectedMethod === 'alipay' }"
              @click="selectedMethod = 'alipay'"
            >
              <div class="timeline-item__top">
                <div class="merchant-card__metrics" style="align-items: center">
                  <img src="/eleme/alipay.png" alt="支付宝" width="112" height="32" />
                  <div>
                    <p class="timeline-item__name">支付宝</p>
                    <p class="timeline-item__meta">即时到账，课件里默认选择</p>
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
              class="timeline-item panel--soft"
              :class="{ 'sort-chip--active': selectedMethod === 'wechat' }"
              @click="selectedMethod = 'wechat'"
            >
              <div class="timeline-item__top">
                <div class="merchant-card__metrics" style="align-items: center">
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

      <!-- 确认支付动作。 -->
      <section class="page__content section">
        <button type="button" class="primary-button" style="width: 100%" @click="confirmPayment">
          确认支付 {{ formatCny(order.total) }}
        </button>
      </section>
    </template>

    <section v-else class="page__content">
      <div class="empty-state panel">
        <h3 class="empty-state__title">没有找到该订单</h3>
        <p class="empty-state__text">订单可能还没创建，先回到商家页面下单吧。</p>
        <button type="button" class="primary-button" style="margin-top: 16px" @click="router.push('/businesses')">
          去商家列表
        </button>
      </div>
    </section>
  </div>
</template>
