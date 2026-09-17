<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import type { Merchant } from '@/types'
import { formatCny } from '@/utils/format'
import { formatBusinessHours, getBusinessHours } from '@/utils/businessHours'

const props = withDefaults(
  defineProps<{
    merchant: Merchant
  }>(),
  {},
)

// 菜品默认收起；点击下方按钮展开，重新进入页面时因组件重新挂载而再次收起。
const showFoods = ref(false)

const foods = computed(() => props.merchant.menuSections.flatMap((section) => section.items).slice(0, 8))
const businessHours = computed(() => formatBusinessHours(getBusinessHours(props.merchant.id)))
</script>

<template>
  <div class="merchant-card">
    <RouterLink :to="`/merchant/${merchant.id}`" class="merchant-card__head">
      <img class="merchant-card__image" :src="merchant.image" :alt="merchant.name" />
      <div class="merchant-card__info">
        <h3 class="merchant-card__name">{{ merchant.name }}</h3>
        <p class="merchant-card__meta">
          ¥{{ merchant.minOrder ?? merchant.startPrice }} 起送 · 配送 {{ formatCny(merchant.deliveryFee) }}
        </p>
        <p class="merchant-card__hours">营业时间 {{ businessHours }}</p>
      </div>
    </RouterLink>

    <div v-if="showFoods && foods.length" class="merchant-card__foods">
      <div v-for="food in foods" :key="food.id" class="merchant-card__food">
        <img class="merchant-card__food-image" :src="food.image" :alt="food.name" />
        <span class="merchant-card__food-name">{{ food.name }}</span>
        <span class="merchant-card__food-stock">剩余 {{ food.stock }} 件</span>
        <span class="merchant-card__food-price">¥{{ food.price }}</span>
      </div>
    </div>

    <button type="button" class="merchant-card__toggle" @click="showFoods = !showFoods">
      <UiIcon :name="showFoods ? 'chevronDown' : 'chevronRight'" :size="13" />
      {{ showFoods ? '收起菜品' : foods.length ? '查看菜品' : '进入店铺' }}
    </button>
  </div>
</template>
