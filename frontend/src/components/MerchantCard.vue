<script setup lang="ts">
import { RouterLink } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import type { Merchant } from '@/types'
import { formatCny } from '@/utils/format'

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
      <span class="merchant-card__rank">商家 {{ merchant.id }}</span>
    </div>
    <div class="merchant-card__body">
      <div class="merchant-card__top">
        <div>
          <h3 class="merchant-card__title">{{ merchant.name }}</h3>
          <p class="merchant-card__subtitle">{{ merchant.address || '地址待商家补充' }}</p>
        </div>
        <span class="merchant-card__tag">
          <UiIcon name="check" :size="14" />
          {{ merchant.remark || '后端同步' }}
        </span>
      </div>
      <div class="merchant-card__metrics">
        <span class="merchant-card__metric">分类 {{ merchant.orderTypeId }}</span>
        <span class="merchant-card__metric">{{ formatCny(merchant.minOrder) }} 起送</span>
        <span class="merchant-card__metric">{{ formatCny(merchant.deliveryFee) }} 配送</span>
      </div>
      <div class="merchant-card__footer">
        <span>{{ merchant.description || '暂无商家介绍' }}</span>
      </div>
      <p class="merchant-card__promo">{{ merchant.remark || '点击进入后将从后端加载菜单' }}</p>
    </div>
  </RouterLink>
</template>
