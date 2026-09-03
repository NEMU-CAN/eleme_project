<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import MerchantCard from '@/components/MerchantCard.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const store = useHungryStore()
const sortMode = ref<'recommended' | 'distance' | 'sales'>('recommended')

// 根据排序模式动态整理商家列表，便于切换综合、距离和销量。
const sortedMerchants = computed(() => {
  const list = [...store.merchants]

  if (sortMode.value === 'distance') {
    return list.sort((a, b) => a.distanceKm - b.distanceKm)
  }

  if (sortMode.value === 'sales') {
    return list.sort((a, b) => b.monthlySales - a.monthlySales)
  }

  return list.sort((a, b) => {
    const scoreA = a.rating * 1000 + a.monthlySales
    const scoreB = b.rating * 1000 + b.monthlySales
    return scoreB - scoreA
  })
})

// 顶部排序条的可选项，和页面上的按钮一一对应。
const sortItems: Array<{ key: typeof sortMode.value; label: string; icon?: string }> = [
  { key: 'recommended', label: '综合排序', icon: 'chevronDown' },
  { key: 'distance', label: '距离最近' },
  { key: 'sales', label: '销量最高' },
]
</script>

<template>
  <div class="page page--with-nav">
    <!-- 页面头部：标题和返回首页入口。 -->
    <SiteHeader title="商家列表" eyebrow="点餐业务线" backable compact @back="router.push('/')" />

    <!-- 排序与筛选条：和课件里的推荐方式区域对应。 -->
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
      <button type="button" class="sort-chip">
        筛选
        <UiIcon name="filter" :size="14" />
      </button>
    </div>

    <!-- 商家列表：点击卡片进入商家详情。 -->
    <section class="merchant-list" aria-label="商家列表">
      <MerchantCard
        v-for="merchant in sortedMerchants"
        :key="merchant.id"
        :merchant="merchant"
        class="merchant-list__button"
      />
    </section>

    <BottomNav />
  </div>
</template>
