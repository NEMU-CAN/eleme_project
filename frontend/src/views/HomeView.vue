<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import CategoryGrid from '@/components/CategoryGrid.vue'
import MerchantCard from '@/components/MerchantCard.vue'
import UiIcon from '@/components/UiIcon.vue'
import { categories } from '@/data/categories'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const store = useHungryStore()
const searchKeyword = ref('')
const activeCategory = ref('all')
const sortMode = ref<'all' | 'fast' | 'free'>('all')

onMounted(() => {
  void store.loadBusinesses().catch(() => undefined)
  if (store.state.user) void store.loadSessionData().catch(() => undefined)
})

const address = computed(() => store.activeAddress.value?.detail || (store.state.user ? '请选择收货地址' : '天津大学北洋园校区'))
const merchants = computed(() => {
  let list = [...store.merchants]
  if (activeCategory.value !== 'all') list = list.filter((item) => String(item.orderTypeId) === activeCategory.value)
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (keyword) list = list.filter((item) => [item.name, item.address, item.description].some((value) => value?.toLowerCase().includes(keyword)))
  if (sortMode.value === 'fast') list.sort((a, b) => Number(a.id) - Number(b.id))
  if (sortMode.value === 'free') list.sort((a, b) => a.deliveryFee - b.deliveryFee)
  return list
})

function search() {
  router.push({ path: '/businesses', query: searchKeyword.value.trim() ? { keyword: searchKeyword.value.trim() } : {} })
}
</script>

<template>
  <div class="page page--with-nav ele-home">
    <header class="ele-home__header">
      <div class="ele-home__location-row">
        <button type="button" class="ele-home__location" @click="router.push('/me')">
          <UiIcon name="pin" :size="20" :stroke-width="2.6" />
          <strong>{{ address }}</strong>
          <UiIcon name="chevronDown" :size="15" />
        </button>
        <RouterLink to="/me" class="ele-home__round"><UiIcon name="user" :size="20" /></RouterLink>
      </div>
      <form class="ele-search" @submit.prevent="search">
        <UiIcon name="search" :size="19" />
        <input v-model="searchKeyword" placeholder="搜索商家或商品" />
        <button type="submit">搜索</button>
      </form>
    </header>

    <main>
      <div class="ele-home__category-wrap"><CategoryGrid :items="categories" /></div>

      <section class="ele-quick-entry" aria-label="快捷服务">
        <button type="button" @click="router.push('/businesses?orderTypeId=1')"><b>校园美食</b><span>热销餐厅</span></button>
        <button type="button" @click="router.push('/businesses?orderTypeId=5')"><b>甜品饮品</b><span>下午茶必备</span></button>
        <button type="button" @click="router.push('/cart')"><b>我的购物车</b><span>{{ store.state.cartItems.length ? `${store.state.cartItems.length} 件商品` : '快速查看' }}</span></button>
      </section>

      <section class="ele-shop-section">
        <nav class="ele-channel-tabs">
          <button :class="{ active: activeCategory === 'all' }" @click="activeCategory = 'all'">全部</button>
          <button v-for="item in categories.slice(0, 4)" :key="item.id" :class="{ active: activeCategory === item.id }" @click="activeCategory = item.id">{{ item.name }}</button>
        </nav>
        <div class="ele-filter-row">
          <button :class="{ active: sortMode === 'all' }" @click="sortMode = 'all'">综合排序 <UiIcon name="chevronDown" :size="12" /></button>
          <button :class="{ active: sortMode === 'fast' }" @click="sortMode = 'fast'">30分钟</button>
          <button :class="{ active: sortMode === 'free' }" @click="sortMode = 'free'">配送费低</button>
          <button @click="router.push('/businesses')">筛选 <UiIcon name="filter" :size="13" /></button>
        </div>

        <div v-if="store.state.loading.businesses" class="ele-empty">正在加载附近商家…</div>
        <div v-else-if="merchants.length" class="ele-merchant-list">
          <MerchantCard v-for="merchant in merchants" :key="merchant.id" :merchant="merchant" />
        </div>
        <div v-else class="ele-empty">没有找到符合条件的商家</div>
      </section>
    </main>
    <BottomNav />
  </div>
</template>
