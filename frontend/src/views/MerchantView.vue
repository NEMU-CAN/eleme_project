<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import QuantityStepper from '@/components/QuantityStepper.vue'
import UiIcon from '@/components/UiIcon.vue'
import LazyImage from '@/components/LazyImage.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()
const error = ref('')
const activeTab = ref<'menu' | 'review' | 'store'>('menu')
const activeCategory = ref('推荐')
const merchantId = computed(() => String(route.params.merchantId || store.activeMerchant.value?.id || ''))
const merchant = computed(() => store.getMerchant(merchantId.value) ?? null)
const cartSummary = computed(() => merchant.value ? store.checkoutSummary(merchant.value.id) : { merchant: null, lines: [], subtotal: 0, fee: 0, total: 0, count: 0 })
const canCheckout = computed(() => merchant.value ? store.cartCanCheckout(merchant.value.id) : false)
const remaining = computed(() => Math.max(0, (merchant.value?.minOrder ?? 0) - cartSummary.value.subtotal))
const items = computed(() => merchant.value?.menuSections.flatMap((section) => section.items) ?? [])
const categories = computed(() => ['推荐', '热销', '招牌', '主食', '小吃', '饮品'])

watch(merchantId, async (value) => {
  if (!value) return
  try { error.value = ''; await store.ensureMerchantDetail(value) }
  catch (cause) { error.value = store.messageFromError(cause) }
}, { immediate: true })

function quantityFor(id: string) { return cartSummary.value.lines.find((line) => line.id === id)?.quantity || 0 }
async function addItem(id: string) {
  if (!store.state.user) return void router.push({ path: '/login', query: { redirect: route.fullPath } })
  const item = items.value.find((entry) => entry.id === id)
  if (item) try { await store.addToCart(item.businessId, item) } catch (cause) { error.value = store.messageFromError(cause) }
}
async function removeItem(id: string) {
  if (merchant.value) try { await store.removeFromCart(merchant.value.id, id) } catch (cause) { error.value = store.messageFromError(cause) }
}
async function checkout() {
  if (!merchant.value) return
  if (!store.state.user) return void router.push({ path: '/login', query: { redirect: route.fullPath } })
  try { const orderId = await store.prepareCheckout(merchant.value.id); router.push(`/checkout/${orderId}`) }
  catch (cause) { error.value = store.messageFromError(cause) }
}
</script>

<template>
  <div class="page ele-store-page">
    <header class="ele-store-cover" v-if="merchant">
      <LazyImage :src="merchant.image" :alt="merchant.name" />
      <div class="ele-store-cover__shade" />
      <div class="ele-store-cover__actions">
        <button @click="router.push('/businesses')"><UiIcon name="chevronLeft" :size="22" /></button>
        <button @click="router.push('/cart')"><UiIcon name="cart" :size="21" /></button>
      </div>
    </header>

    <template v-if="merchant">
      <section class="ele-store-summary">
        <div class="ele-store-summary__title"><h1>{{ merchant.name }}</h1><span>收藏</span></div>
        <p>评分 4.8 · 月售 {{ 800 + Number(merchant.id) * 66 }} · 商家配送</p>
        <div class="ele-store-summary__meta"><span>{{ formatCny(merchant.minOrder ?? merchant.startPrice) }}起送</span><span>配送约{{ formatCny(merchant.deliveryFee) }}</span><span>约{{ 25 + Number(merchant.id) }}分钟</span></div>
        <div class="ele-store-summary__notice">公告：{{ merchant.description || '认真做好每一份餐，感谢您的光临。' }}</div>
      </section>

      <nav class="ele-store-tabs">
        <button :class="{ active: activeTab === 'menu' }" @click="activeTab = 'menu'">点餐</button>
        <button :class="{ active: activeTab === 'review' }" @click="activeTab = 'review'">评价 <small>58</small></button>
        <button :class="{ active: activeTab === 'store' }" @click="activeTab = 'store'">商家</button>
      </nav>

      <div v-if="activeTab === 'menu'" class="ele-menu-layout">
        <aside class="ele-menu-categories">
          <button v-for="category in categories" :key="category" :class="{ active: activeCategory === category }" @click="activeCategory = category">{{ category }}</button>
        </aside>
        <main class="ele-menu-products">
          <div class="ele-menu-heading"><h2>{{ activeCategory }}</h2><span>适量点餐，拒绝浪费</span></div>
          <article v-for="item in items" :key="item.id" class="ele-product-row">
            <LazyImage :src="item.image" :alt="item.name" class="ele-product-row__image" />
            <div class="ele-product-row__body">
              <h3>{{ item.name }}</h3>
              <p>{{ item.description || '店内现做，新鲜美味' }}</p>
              <span class="ele-product-row__sales">月售{{ 30 + Number(item.id) * 3 }} · 好评率98%</span>
              <div class="ele-product-row__bottom">
                <strong>{{ formatCny(item.price) }}</strong>
                <QuantityStepper :quantity="quantityFor(item.id)" :disabled="store.state.loading.action" @add="addItem(item.id)" @remove="removeItem(item.id)" />
              </div>
            </div>
          </article>
        </main>
      </div>

      <section v-else-if="activeTab === 'review'" class="ele-store-placeholder"><b>4.8分</b><p>味道很好，包装完整，配送及时</p></section>
      <section v-else class="ele-store-placeholder"><h2>{{ merchant.name }}</h2><p>{{ merchant.address }}</p><p>{{ merchant.description }}</p></section>
      <p v-if="error" class="ele-store-error">{{ error }}</p>

      <div class="ele-cart-bar">
        <button class="ele-cart-bar__icon" @click="router.push('/cart')"><UiIcon name="cart" :size="25" /><i v-if="cartSummary.count">{{ cartSummary.count }}</i></button>
        <div><strong>{{ cartSummary.count ? formatCny(cartSummary.subtotal) : '未选购商品' }}</strong><span>{{ cartSummary.count ? `另需配送费 ${formatCny(merchant.deliveryFee)}` : `满 ${formatCny(merchant.minOrder ?? merchant.startPrice)} 起送` }}</span></div>
        <button class="ele-cart-bar__checkout" :disabled="!cartSummary.count || (!!store.state.user && !canCheckout)" @click="checkout">{{ canCheckout ? '去结算' : `差${formatCny(remaining)}起送` }}</button>
      </div>
    </template>

    <div v-else class="ele-empty">{{ store.state.loading.merchant ? '正在加载店铺…' : '没有找到店铺' }}</div>
  </div>
</template>
