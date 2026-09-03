<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { CategoryItem } from '@/types'

// 首页分类九宫格，把常见入口直接铺到第一屏。
defineProps<{
  items: CategoryItem[]
}>()
</script>

<template>
  <section class="category-grid panel">
    <RouterLink
      v-for="item in items"
      :key="item.id"
      :to="item.route"
      class="category-grid__item"
    >
      <img class="category-grid__image" :src="item.image" :alt="item.name" />
      <span class="category-grid__label">{{ item.name }}</span>
    </RouterLink>
  </section>
</template>
