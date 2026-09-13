<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import MerchantCard from '@/components/MerchantCard.vue'
import UiIcon from '@/components/UiIcon.vue'
import { categories } from '@/data/categories'
import { useHungryStore } from '@/composables/useHungryStore'

const route = useRoute()
const router = useRouter()
const store = useHungryStore()
const keyword = ref(String(route.query.keyword || ''))
const sortMode = ref<'default' | 'price' | 'delivery'>('default')
const typeId = computed(() => Number(route.query.orderTypeId) || null)

watch(typeId, (value) => void store.loadBusinesses({ tasteId: value ?? undefined }).catch(() => undefined), { immediate: true })
const merchants = computed(() => {
  let list = [...store.merchants]
  const key = keyword.value.trim().toLowerCase()
  if (key) list = list.filter((item) => [item.name, item.address, item.description].some((value) => value?.toLowerCase().includes(key)))
  if (sortMode.value === 'price') list.sort((a, b) => (a.minOrder ?? a.startPrice) - (b.minOrder ?? b.startPrice))
  if (sortMode.value === 'delivery') list.sort((a, b) => a.deliveryFee - b.deliveryFee)
  return list
})

function selectType(id?: string) {
  router.replace({ path: '/businesses', query: id ? { orderTypeId: id } : {} })
}
</script>

<template>
  <div class="page page--with-nav ele-list-page">
    <header class="ele-list-header">
      <div class="ele-list-header__top">
        <button @click="router.push('/')"><UiIcon name="chevronLeft" :size="22" /></button>
        <strong>附近商家</strong>
        <button @click="router.push('/cart')"><UiIcon name="cart" :size="21" /></button>
      </div>
      <label class="ele-search ele-search--list">
        <UiIcon name="search" :size="18" />
        <input v-model="keyword" placeholder="搜索商家、商品" />
        <button v-if="keyword" @click="keyword = ''">清除</button>
      </label>
      <nav class="ele-list-types">
        <button :class="{ active: !typeId }" @click="selectType()">全部</button>
        <button v-for="item in categories" :key="item.id" :class="{ active: typeId === Number(item.id) }" @click="selectType(item.id)">{{ item.name }}</button>
      </nav>
    </header>

    <main class="ele-list-content">
      <div class="ele-filter-row ele-filter-row--sticky">
        <button :class="{ active: sortMode === 'default' }" @click="sortMode = 'default'">综合排序</button>
        <button :class="{ active: sortMode === 'price' }" @click="sortMode = 'price'">起送最低</button>
        <button :class="{ active: sortMode === 'delivery' }" @click="sortMode = 'delivery'">配送费低</button>
        <span>{{ merchants.length }}家</span>
      </div>
      <div v-if="store.state.loading.businesses" class="ele-empty">正在加载商家…</div>
      <div v-else class="ele-merchant-list">
        <MerchantCard v-for="merchant in merchants" :key="merchant.id" :merchant="merchant" />
      </div>
    </main>
    <BottomNav />
  </div>
</template>
