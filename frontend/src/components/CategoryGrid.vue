<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { CategoryItem } from '@/types'

defineProps<{ items: CategoryItem[] }>()
</script>

<template>
  <section class="category-grid" aria-label="商品分类">
    <RouterLink v-for="item in items" :key="item.id" :to="item.route" class="category-grid__item">
      <span class="category-grid__icon"><img class="category-grid__image" :src="item.image" :alt="item.name" /></span>
      <span class="category-grid__label">{{ item.name }}</span>
    </RouterLink>
  </section>
</template>
