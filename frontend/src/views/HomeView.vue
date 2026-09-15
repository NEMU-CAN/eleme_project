<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import MerchantCard from '@/components/MerchantCard.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()

const sortMode = ref<'recommended' | 'minOrder' | 'deliveryFee'>('recommended')
const searchText = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')

// 分类栏：全部 + 后端口味列表。
const categoryItems = computed(() => [
  { id: '', name: '全部', image: '', all: true },
  ...store.tasteCategories.value.map((taste) => ({ id: taste.id, name: taste.name, image: taste.image, all: false })),
])

const activeTasteId = computed(() => {
  const value = Number(route.query.tasteId)
  return Number.isInteger(value) && value > 0 ? value : null
})

const activeStatus = computed(() => {
  const value = Number(route.query.status)
  return value === 0 || value === 1 ? value : null
})

const activeKeyword = computed(() => (typeof route.query.keyword === 'string' ? route.query.keyword.trim() : ''))

const heroAddress = computed(() => {
  if (store.activeAddress.value) {
    return store.activeAddress.value.detail
  }
  return store.isAuthenticated.value ? '请选择收货地址' : '点击这里新增地址'
})

watch(activeKeyword, (value) => {
  searchText.value = value
}, { immediate: true })

watch(
  [activeTasteId, activeStatus, activeKeyword],
  ([tasteId, status, keyword]) => {
    void store.loadBusinesses({ tasteId, status, keyword: keyword || null }).catch(() => undefined)
  },
  { immediate: true },
)

const sortedMerchants = computed(() => {
  const list = [...store.merchants]
  if (sortMode.value === 'minOrder') {
    return list.sort((a, b) => (a.minOrder ?? a.startPrice) - (b.minOrder ?? b.startPrice))
  }
  if (sortMode.value === 'deliveryFee') {
    return list.sort((a, b) => a.deliveryFee - b.deliveryFee)
  }
  return list.sort((a, b) => Number(a.id) - Number(b.id))
})

const sortItems = [
  { key: 'recommended', label: '综合排序' },
  { key: 'minOrder', label: '起送最低' },
  { key: 'deliveryFee', label: '配送费低' },
] as const

const statusItems = [
  { label: '全部', value: null },
  { label: '营业中', value: 1 },
  { label: '已打烊', value: 0 },
] as const

onMounted(() => {
  void store.loadBusinesses().catch(() => undefined)
  if (store.isAuthenticated.value) {
    void store.loadSessionData().catch(() => undefined)
  }
})

function syncQuery(patch: Record<string, string | number | null | undefined>) {
  const next = { ...(route.query as Record<string, string>) }
  Object.entries(patch).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      delete next[key]
      return
    }
    next[key] = String(value)
  })
  router.replace({ query: next })
}

function chooseTaste(id: string) {
  syncQuery({ tasteId: id || null })
}

function chooseStatus(value: number | null) {
  syncQuery({ status: value })
}

function submitSearch() {
  syncQuery({ keyword: searchText.value.trim() || null })
}

function clearSearch() {
  searchText.value = ''
  submitSearch()
}

function goAddress() {
  router.push('/addresses')
}
</script>

<template>
  <div class="page page--with-nav">
    <!-- 定位栏 -->
    <div class="location-bar" role="button" @click="goAddress">
      <UiIcon class="location-bar__icon" name="pin" :size="18" />
      <span class="location-bar__label">{{ heroAddress }}</span>
      <span v-if="!store.activeAddress" class="location-bar__hint">· 添加收货地址</span>
      <UiIcon name="chevronDown" :size="14" />
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <UiIcon class="search-bar__icon" name="search" :size="18" />
      <input
        v-model="searchText"
        class="search-bar__input"
        type="search"
        placeholder="搜索商家或菜品"
        @keyup.enter="submitSearch"
      />
      <button v-if="searchText" type="button" class="search-bar__button" style="background: #ccc" @click="clearSearch">
        清空
      </button>
      <button type="button" class="search-bar__button" @click="submitSearch">搜索</button>
    </div>

    <!-- 店铺分类栏 -->
    <div class="category-bar">
      <button
        v-for="item in categoryItems"
        :key="item.id"
        type="button"
        class="category-chip"
        :class="{ 'category-chip--active': (item.id === '' ? null : Number(item.id)) === activeTasteId }"
        @click="chooseTaste(item.id)"
      >
        <span v-if="item.all" class="category-chip__image" style="display: inline-flex; align-items: center; justify-content: center; color: #999; background: #f2f2f2">
          <UiIcon name="filter" :size="22" />
        </span>
        <img v-else class="category-chip__image" :src="item.image" :alt="item.name" />
        <span class="category-chip__label">{{ item.name }}</span>
      </button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <button
        v-for="item in sortItems"
        :key="item.key"
        type="button"
        class="filter-chip"
        :class="{ 'filter-chip--active': sortMode === item.key }"
        @click="sortMode = item.key"
      >
        {{ item.label }}
        <UiIcon v-if="item.key === 'recommended'" name="chevronDown" :size="13" />
      </button>
      <span class="filter-divider" />
      <button
        v-for="item in statusItems"
        :key="String(item.value)"
        type="button"
        class="filter-chip"
        :class="{ 'filter-chip--active': activeStatus === item.value }"
        @click="chooseStatus(item.value)"
      >
        {{ item.label }}
      </button>
    </div>

    <!-- 商家列表 -->
    <section v-if="store.state.loading.businesses" class="empty-state">
      <p class="empty-state__text">正在加载商家…</p>
    </section>

    <section v-else-if="sortedMerchants.length" class="merchant-list">
      <MerchantCard v-for="merchant in sortedMerchants" :key="merchant.id" :merchant="merchant" />
    </section>

    <section v-else class="empty-state">
      <p class="empty-state__title">暂无商家</p>
      <p class="empty-state__text">{{ store.state.error || '后端暂时没有返回符合条件的商家。' }}</p>
    </section>

    <BottomNav />
  </div>
</template>
