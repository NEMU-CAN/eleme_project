<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const store = useHungryStore()

// 底部主导航，固定在页面底部，订单角标直接读取未支付数量。
const items = computed(() => [
  { label: '首页', icon: 'home', to: '/' },
  { label: '商家', icon: 'compass', to: '/businesses' },
  { label: '订单', icon: 'orders', to: '/orders', badge: store.unreadOrders.value || undefined },
  { label: '我的', icon: 'user', to: '/me' },
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
      <span class="bottom-nav__icon-wrap" :class="{ 'bottom-nav__icon-wrap--active': isActive }">
        <UiIcon :name="item.icon" :size="20" />
        <span v-if="item.badge" class="bottom-nav__badge">{{ item.badge }}</span>
      </span>
      <span class="bottom-nav__label" :class="{ 'bottom-nav__label--active': isActive }">
        {{ item.label }}
      </span>
    </RouterLink>
  </nav>
</template>
