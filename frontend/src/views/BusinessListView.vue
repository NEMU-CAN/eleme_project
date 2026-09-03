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

const activeOrderTypeId = computed(() => {
  const value = Number(route.query.orderTypeId)
  return Number.isInteger(value) && value > 0 ? value : null
})

watch(
  activeOrderTypeId,
  (value) => {
    void store.loadBusinesses(value).catch(() => undefined)
  },
  { immediate: true },
)

// 根据排序模式动态整理商家列表，便于切换综合、距离和销量。
const sortedMerchants = computed(() => {
  const list = [...store.merchants]

  if (sortMode.value === 'minOrder') {
    return list.sort((a, b) => a.minOrder - b.minOrder)
  }

  if (sortMode.value === 'deliveryFee') {
    return list.sort((a, b) => a.deliveryFee - b.deliveryFee)
  }

  return list.sort((a, b) => Number(a.id) - Number(b.id))
})

// 顶部排序条的可选项，和页面上的按钮一一对应。
const sortItems: Array<{ key: typeof sortMode.value; label: string; icon?: string }> = [
  { key: 'recommended', label: '综合排序', icon: 'chevronDown' },
  { key: 'minOrder', label: '起送最低' },
  { key: 'deliveryFee', label: '配送费低' },
]
</script>

<template>
  <div class="page page--with-nav">
    <!-- 页面头部：标题和返回首页入口。 -->
    <SiteHeader
      title="商家列表"
      :eyebrow="activeOrderTypeId ? `分类 ${activeOrderTypeId}` : '全部商家'"
      backable
      compact
      @back="router.push('/')"
    />

    <!-- 排序与筛选条：基于后端返回字段做前端排序。 -->
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
      <button type="button" class="sort-chip" @click="router.push('/businesses')">
        筛选
        <UiIcon name="filter" :size="14" />
      </button>
    </div>

    <!-- 商家列表：点击卡片进入商家详情。 -->
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
