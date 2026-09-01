<script setup lang="ts">
import { RouterLink } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import type { Merchant } from '@/types'
import { formatCny, formatDistance, formatMinutes } from '@/utils/format'

// 商家卡片用于首页推荐区和商家列表页，承载一整张商户信息卡。
withDefaults(
  defineProps<{
    merchant: Merchant
    compact?: boolean
  }>(),
  {
    compact: false,
  },
)
</script>

<template>
  <RouterLink :to="`/merchant/${merchant.id}`" class="merchant-card panel">
    <div class="merchant-card__media">
      <img class="merchant-card__image" :src="merchant.image" :alt="merchant.name" />
      <span class="merchant-card__rank">{{ merchant.tags[0] || '推荐' }}</span>
    </div>
    <div class="merchant-card__body">
      <div class="merchant-card__top">
        <div>
          <h3 class="merchant-card__title">{{ merchant.name }}</h3>
          <p class="merchant-card__subtitle">{{ merchant.cuisine }}</p>
        </div>
        <span class="merchant-card__tag">
          <UiIcon name="check" :size="14" />
          {{ merchant.tags[0] || '可靠' }}
        </span>
      </div>
      <div class="merchant-card__metrics">
        <span class="merchant-card__metric">
          <UiIcon name="star" :size="14" />
          {{ merchant.rating.toFixed(1) }}
        </span>
        <span class="merchant-card__metric">{{ merchant.monthlySales }} 月售</span>
        <span class="merchant-card__metric">{{ formatDistance(merchant.distanceKm) }}</span>
        <span class="merchant-card__metric">{{ formatMinutes(merchant.etaMinutes) }}</span>
      </div>
      <div class="merchant-card__footer">
        <span>{{ formatCny(merchant.minOrder) }} 起送</span>
        <span>{{ formatCny(merchant.deliveryFee) }} 配送</span>
      </div>
      <p class="merchant-card__promo">{{ merchant.highlight }}</p>
    </div>
  </RouterLink>
</template>
