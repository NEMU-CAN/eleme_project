<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import MerchantCard from '@/components/MerchantCard.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()
const sortMode = ref<'recommended' | 'minOrder' | 'deliveryFee'>('recommended')
const searchText = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')

const activeTasteId = computed(() => {
  const raw = route.query.tasteId ?? route.query.orderTypeId
  const value = Number(raw)
  return Number.isInteger(value) && value > 0 ? value : null
})

const activeStatus = computed(() => {
  const value = Number(route.query.status)
  return value === 0 || value === 1 ? value : null
})

const activeKeyword = computed(() => (typeof route.query.keyword === 'string' ? route.query.keyword.trim() : ''))

watch(activeKeyword, (value) => {
  searchText.value = value
}, { immediate: true })

watch(
  [activeTasteId, activeStatus, activeKeyword],
  ([tasteId, status, keyword]) => {
    void store.loadBusinesses({
      tasteId,
      status,
      keyword: keyword || null,
    }).catch(() => undefined)
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

const sortItems: Array<{ key: typeof sortMode.value; label: string; icon?: string }> = [
  { key: 'recommended', label: '综合排序', icon: 'chevronDown' },
  { key: 'minOrder', label: '起送最低' },
  { key: 'deliveryFee', label: '配送费低' },
]

const statusTabs = [
  { key: 'all', label: '全部', value: null as number | null },
  { key: 'open', label: '营业中', value: 1 as number | null },
  { key: 'closed', label: '已打烊', value: 0 as number | null },
] as const

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

function submitSearch() {
  syncQuery({ keyword: searchText.value.trim() || null })
}

function clearSearch() {
  searchText.value = ''
  submitSearch()
}

function chooseStatus(value: number | null) {
  syncQuery({ status: value })
}
</script>

<template>
  <div class="page page--with-nav">
    <SiteHeader
      title="商家列表"
      :eyebrow="activeTasteId ? `口味 ${activeTasteId}` : '全部商家'"
      backable
      compact
      @back="router.push('/')"
    />

    <section class="page__content section">
      <div class="search-panel" style="cursor: default">
        <UiIcon name="search" :size="18" />
        <input
          v-model="searchText"
          class="search-panel__input"
          type="search"
          placeholder="搜索商家或菜品"
          @keyup.enter="submitSearch"
        />
        <button v-if="searchText" type="button" class="chip" @click="clearSearch">清空</button>
        <button type="button" class="primary-button" style="min-height: 36px; padding-inline: 14px" @click="submitSearch">
          搜索
        </button>
      </div>
    </section>

    <div class="sort-bar">
      <button
        v-for="item in sortItems"
        :key="item.key"
        type="button"
        class="sort-chip"
        :class="{ 'sort-chip--active': sortMode === item.key }"
        @click="sortMode = item.key"
      >
        {{ item.label }}
        <UiIcon v-if="item.icon" :name="item.icon" :size="14" />
      </button>
    </div>

    <section class="section">
      <div class="sort-bar">
        <button
          v-for="tab in statusTabs"
          :key="tab.key"
          type="button"
          class="sort-chip"
          :class="{ 'sort-chip--active': activeStatus === tab.value }"
          @click="chooseStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </section>

    <section v-if="store.state.loading.businesses" class="page__content section">
      <div class="empty-state panel">
        <h3 class="empty-state__title">正在加载商家</h3>
        <p class="empty-state__text">正在请求后端商家接口。</p>
      </div>
    </section>

    <section v-else-if="sortedMerchants.length" class="merchant-list" aria-label="商家列表">
      <MerchantCard
        v-for="merchant in sortedMerchants"
        :key="merchant.id"
        :merchant="merchant"
        class="merchant-list__button"
      />
    </section>

    <section v-else class="page__content section">
      <div class="empty-state panel">
        <h3 class="empty-state__title">暂无商家</h3>
        <p class="empty-state__text">{{ store.state.error || '后端没有返回符合条件的商家。' }}</p>
      </div>
    </section>

    <BottomNav />
  </div>
</template>
