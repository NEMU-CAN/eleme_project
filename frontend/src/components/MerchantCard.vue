<script setup lang="ts">
import { RouterLink } from 'vue-router'
import LazyImage from '@/components/LazyImage.vue'
import UiIcon from '@/components/UiIcon.vue'
import type { Merchant } from '@/types'
import { formatCny } from '@/utils/format'

defineProps<{ merchant: Merchant; compact?: boolean }>()
</script>

<template>
  <RouterLink :to="`/merchant/${merchant.id}`" class="ele-merchant-card">
    <div class="ele-merchant-card__visual">
      <LazyImage :src="merchant.image" :alt="merchant.name" />
      <span>品牌</span>
    </div>
    <div class="ele-merchant-card__main">
      <div class="ele-merchant-card__headline">
        <h3>{{ merchant.name }}</h3>
        <UiIcon name="chevronRight" :size="15" />
      </div>
      <div class="ele-merchant-card__sales">
        <b>4.{{ 6 + (Number(merchant.id) % 4) }}</b>
        <span>月售 {{ 600 + Number(merchant.id) * 73 }}+</span>
        <em>校园送</em>
      </div>
      <div class="ele-merchant-card__delivery">
        <span>{{ formatCny(merchant.minOrder ?? merchant.startPrice) }}起送</span>
        <i />
        <span>配送约 {{ formatCny(merchant.deliveryFee) }}</span>
        <span class="ele-merchant-card__time">{{ 25 + Number(merchant.id) }}分钟 · {{ (1 + Number(merchant.id) * .4).toFixed(1) }}km</span>
      </div>
      <p class="ele-merchant-card__desc">{{ merchant.description || merchant.address || '附近品质商家' }}</p>
      <div class="ele-merchant-card__labels">
        <span>明厨亮灶</span><span>品质精选</span><span v-if="merchant.remark">{{ merchant.remark }}</span>
      </div>
    </div>
  </RouterLink>
</template>
