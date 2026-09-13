<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import QuantityStepper from '@/components/QuantityStepper.vue'
import LazyImage from '@/components/LazyImage.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny } from '@/utils/format'
import type { CartLine, MenuItem } from '@/types'

const router = useRouter()
const store = useHungryStore()
const editing = ref(false)
const error = ref('')

onMounted(async () => {
  if (!store.state.user) return void router.replace({ path: '/login', query: { redirect: '/cart' } })
  try { await Promise.all([store.loadBusinesses(), store.loadCart()]) } catch (cause) { error.value = store.messageFromError(cause) }
})

const groups = computed(() => {
  const map = new Map<string, CartLine[]>()
  for (const line of store.state.cartItems) map.set(line.businessId, [...(map.get(line.businessId) ?? []), line])
  return [...map.entries()].map(([merchantId, lines]) => {
    const merchant = store.getMerchant(merchantId)
    const subtotal = lines.reduce((sum, line) => sum + line.price * line.quantity, 0)
    return { merchantId, merchant, lines, subtotal }
  })
})

async function add(line: CartLine) {
  const item: MenuItem = { id: line.foodId, businessId: line.businessId, name: line.name, description: '', price: line.price, image: line.image, stock: line.stock, reservedStock: line.reservedStock, status: line.status }
  try { await store.addToCart(line.businessId, item) } catch (cause) { error.value = store.messageFromError(cause) }
}
async function remove(line: CartLine) {
  try { await store.removeFromCart(line.businessId, line.foodId) } catch (cause) { error.value = store.messageFromError(cause) }
}
async function clear(merchantId: string) {
  try { await store.clearCart(merchantId) } catch (cause) { error.value = store.messageFromError(cause) }
}
async function checkout(merchantId: string) {
  try { const id = await store.prepareCheckout(merchantId); router.push(`/checkout/${id}`) } catch (cause) { error.value = store.messageFromError(cause) }
}
</script>

<template>
  <div class="page page--with-nav ele-cart-page">
    <header class="ele-cart-header">
      <div><button @click="router.back()"><UiIcon name="chevronLeft" :size="22" /></button><h1>购物车</h1><button @click="editing = !editing">{{ editing ? '完成' : '管理' }}</button></div>
      <p><UiIcon name="pin" :size="14" /> 天津大学北洋园校区</p>
    </header>

    <main class="ele-cart-content">
      <p v-if="error" class="ele-store-error">{{ error }}</p>
      <section v-for="group in groups" :key="group.merchantId" class="ele-cart-shop">
        <div class="ele-cart-shop__head">
          <button @click="router.push(`/merchant/${group.merchantId}`)"><strong>{{ group.merchant?.name || `商家 ${group.merchantId}` }}</strong><UiIcon name="chevronRight" :size="15" /></button>
          <span>{{ 25 + Number(group.merchantId) }}分钟</span>
        </div>
        <div v-for="line in group.lines" :key="line.cartId" class="ele-cart-line">
          <LazyImage :src="line.image" :alt="line.name" />
          <div class="ele-cart-line__body"><h3>{{ line.name }}</h3><p>新鲜现做 · 到店同价</p><strong>{{ formatCny(line.price) }}</strong></div>
          <QuantityStepper :quantity="line.quantity" size="small" :disabled="store.state.loading.action" @add="add(line)" @remove="remove(line)" />
        </div>
        <div class="ele-cart-shop__foot">
          <button v-if="editing" class="ele-cart-delete" @click="clear(group.merchantId)">清空</button>
          <span>共 {{ group.lines.reduce((sum, item) => sum + item.quantity, 0) }} 件</span>
          <b>{{ formatCny(group.subtotal) }}</b>
          <button class="ele-cart-submit" :disabled="!store.cartCanCheckout(group.merchantId)" @click="checkout(group.merchantId)">去结算</button>
        </div>
      </section>
      <div v-if="!groups.length" class="ele-empty ele-cart-empty"><UiIcon name="cart" :size="50" /><b>购物车还是空的</b><button @click="router.push('/')">去逛逛</button></div>
    </main>
    <BottomNav />
  </div>
</template>
