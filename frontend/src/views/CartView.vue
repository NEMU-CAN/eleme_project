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
const selectedMerchantIds = ref<string[]>([])

const groups = computed(() => {
  const map = new Map<string, CartLine[]>()
  for (const line of store.state.cartItems) map.set(line.businessId, [...(map.get(line.businessId) ?? []), line])
  return [...map.entries()].map(([merchantId, lines]) => {
    const merchant = store.getMerchant(merchantId)
    const subtotal = lines.reduce((sum, line) => sum + line.price * line.quantity, 0)
    const count = lines.reduce((sum, line) => sum + line.quantity, 0)
    return { merchantId, merchant, lines, subtotal, count }
  })
})

const selectedGroups = computed(() => groups.value.filter((group) => selectedMerchantIds.value.includes(group.merchantId)))
const allSelected = computed(() => groups.value.length > 0 && selectedGroups.value.length === groups.value.length)
const selectedCount = computed(() => selectedGroups.value.reduce((sum, group) => sum + group.count, 0))
const selectedAmount = computed(() => selectedGroups.value.reduce((sum, group) => sum + group.subtotal + (group.merchant?.deliveryFee ?? 0), 0))
const canCheckoutSelected = computed(() => selectedGroups.value.length > 0 && selectedGroups.value.every((group) => store.cartCanCheckout(group.merchantId)))

onMounted(async () => {
  if (!store.state.user) return void router.replace({ path: '/login', query: { redirect: '/cart' } })
  try {
    await Promise.all([store.loadBusinesses(), store.loadCart()])
    selectedMerchantIds.value = groups.value.map((group) => group.merchantId)
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
})

function isSelected(merchantId: string) {
  return selectedMerchantIds.value.includes(merchantId)
}

function toggleMerchant(merchantId: string) {
  selectedMerchantIds.value = isSelected(merchantId)
    ? selectedMerchantIds.value.filter((id) => id !== merchantId)
    : [...selectedMerchantIds.value, merchantId]
}

function toggleAll() {
  selectedMerchantIds.value = allSelected.value ? [] : groups.value.map((group) => group.merchantId)
}

async function add(line: CartLine) {
  const item: MenuItem = { id: line.foodId, businessId: line.businessId, name: line.name, description: '', price: line.price, image: line.image, stock: line.stock, reservedStock: line.reservedStock, status: line.status }
  try { await store.addToCart(line.businessId, item) } catch (cause) { error.value = store.messageFromError(cause) }
}

async function remove(line: CartLine) {
  try { await store.removeFromCart(line.businessId, line.foodId) } catch (cause) { error.value = store.messageFromError(cause) }
}

async function clearSelected() {
  if (!selectedGroups.value.length) return
  try {
    error.value = ''
    for (const group of selectedGroups.value) await store.clearCart(group.merchantId)
    selectedMerchantIds.value = []
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

async function checkoutSelected() {
  if (!canCheckoutSelected.value) {
    error.value = '所选店铺未达到起送金额，请调整后再结算'
    return
  }
  try {
    error.value = ''
    const orderIds: string[] = []
    for (const group of selectedGroups.value) orderIds.push(await store.prepareCheckout(group.merchantId))
    router.push({ path: `/payment/${orderIds[0]}`, query: orderIds.length > 1 ? { batch: orderIds.join(',') } : {} })
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}
</script>

<template>
  <div class="page page--with-nav ele-cart-page ele-cart-page--selectable">
    <header class="ele-cart-header">
      <div><button @click="router.back()"><UiIcon name="chevronLeft" :size="22" /></button><h1>购物车</h1><button @click="editing = !editing">{{ editing ? '完成' : '管理' }}</button></div>
      <p><UiIcon name="pin" :size="14" /> {{ store.activeAddress.value?.detail || '天津大学北洋园校区' }}</p>
    </header>

    <main class="ele-cart-content">
      <p v-if="error" class="ele-store-error ele-cart-notice">{{ error }}</p>
      <section v-for="group in groups" :key="group.merchantId" class="ele-cart-shop" :class="{ 'is-selected': isSelected(group.merchantId) }">
        <div class="ele-cart-shop__head">
          <button class="ele-check" :class="{ active: isSelected(group.merchantId) }" @click="toggleMerchant(group.merchantId)"><UiIcon v-if="isSelected(group.merchantId)" name="check" :size="13" /></button>
          <button class="ele-cart-shop__name" @click="router.push(`/merchant/${group.merchantId}`)"><strong>{{ group.merchant?.name || `商家 ${group.merchantId}` }}</strong><UiIcon name="chevronRight" :size="15" /></button>
          <span>商家配送</span>
        </div>
        <div v-for="line in group.lines" :key="line.cartId" class="ele-cart-line">
          <button class="ele-check ele-check--line" :class="{ active: isSelected(group.merchantId) }" @click="toggleMerchant(group.merchantId)"><UiIcon v-if="isSelected(group.merchantId)" name="check" :size="12" /></button>
          <LazyImage :src="line.image" :alt="line.name" />
          <div class="ele-cart-line__body"><h3>{{ line.name }}</h3><p>新鲜现做 · 到店同价</p><strong>{{ formatCny(line.price) }}</strong></div>
          <QuantityStepper :quantity="line.quantity" size="small" :disabled="store.state.loading.action" @add="add(line)" @remove="remove(line)" />
        </div>
        <div class="ele-cart-shop__foot"><span>配送费 {{ formatCny(group.merchant?.deliveryFee ?? 0) }}</span><b>小计 {{ formatCny(group.subtotal) }}</b></div>
      </section>
      <div v-if="!groups.length" class="ele-empty ele-cart-empty"><UiIcon name="cart" :size="50" /><b>购物车还是空的</b><button @click="router.push('/')">去逛逛</button></div>
    </main>

    <footer v-if="groups.length" class="ele-cart-settle">
      <button class="ele-cart-select-all" @click="toggleAll"><span class="ele-check" :class="{ active: allSelected }"><UiIcon v-if="allSelected" name="check" :size="13" /></span>全选</button>
      <div class="ele-cart-settle__total"><span>合计</span><strong>{{ formatCny(selectedAmount) }}</strong><small>含配送费</small></div>
      <button v-if="editing" class="ele-cart-settle__delete" :disabled="!selectedGroups.length || store.state.loading.action" @click="clearSelected">删除</button>
      <button v-else class="ele-cart-settle__submit" :disabled="!canCheckoutSelected || store.state.loading.action" @click="checkoutSelected">一键结算{{ selectedCount ? `(${selectedCount})` : '' }}</button>
    </footer>
    <BottomNav />
  </div>
</template>
