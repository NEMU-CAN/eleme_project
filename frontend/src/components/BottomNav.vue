<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const store = useHungryStore()

// 购物车角标：统计所有商品数量。
const cartCount = computed(() => store.state.cartItems.reduce((total, line) => total + line.quantity, 0))

// 底部三栏导航：首页 / 购物车 / 我的。
const items = computed(() => [
  { label: '首页', icon: 'home', to: '/' },
  { label: '购物车', icon: 'cart', to: '/cart', badge: cartCount.value || undefined },
  { label: '我的', icon: 'smile', to: '/me' },
])
</script>

<template>
  <nav class="bottom-nav" aria-label="主导航">
    <RouterLink
      v-for="item in items"
      :key="item.to"
      :to="item.to"
      class="bottom-nav__item"
      v-slot="{ isActive }"
    >
      <span class="bottom-nav__icon-wrap">
        <UiIcon :name="item.icon" :size="24" />
        <span v-if="item.badge" class="bottom-nav__badge">{{ item.badge }}</span>
      </span>
      <span class="bottom-nav__label">{{ item.label }}</span>
    </RouterLink>
  </nav>
</template>
