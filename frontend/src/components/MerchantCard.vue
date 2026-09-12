<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import type { Merchant } from '@/types'
import { formatBusinessStatus, formatCny } from '@/utils/format'

const props = withDefaults(
  defineProps<{
    merchant: Merchant
  }>(),
  {},
)

// 列表页通常不含菜品，只有访问过商家详情后才会有缓存；这里做优雅回退。
const foods = computed(() => props.merchant.menuSections.flatMap((section) => section.items).slice(0, 8))
</script>

<template>
  <RouterLink :to="`/merchant/${merchant.id}`" class="merchant-card">
    <div class="merchant-card__head">
      <img class="merchant-card__image" :src="merchant.image" :alt="merchant.name" />
      <div class="merchant-card__info">
        <h3 class="merchant-card__name">{{ merchant.name }}</h3>
        <p class="merchant-card__meta">
          ¥{{ merchant.minOrder ?? merchant.startPrice }} 起送 · 配送 ¥{{ merchant.deliveryFee }}
        </p>
        <p class="merchant-card__status" :class="{ 'merchant-card__status--closed': merchant.status !== 'open' }">
          {{ formatBusinessStatus(merchant.status) }}
        </p>
      </div>
    </div>

    <div v-if="foods.length" class="merchant-card__foods">
      <div v-for="food in foods" :key="food.id" class="merchant-card__food">
        <img class="merchant-card__food-image" :src="food.image" :alt="food.name" />
        <span class="merchant-card__food-name">{{ food.name }}</span>
        <span class="merchant-card__food-price">¥{{ food.price }}</span>
      </div>
    </div>
    <div v-else class="merchant-card__hint">
      <UiIcon name="chevronRight" :size="12" />
      {{ merchant.description || '点击进入查看菜单' }}
    </div>
  </RouterLink>
</template>
