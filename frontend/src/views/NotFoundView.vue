<script setup lang="ts">
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'

const router = useRouter()
</script>

<template>
  <div class="page page--bare" style="background: var(--bg)">
    <SiteHeader title="页面未找到" backable @back="router.push('/')" />
    <section class="empty-state" style="padding-top: 80px">
      <p class="empty-state__title">这里没有你要找的页面</p>
      <p class="empty-state__text">先回首页看看，或者去逛逛商家。</p>
      <button type="button" class="primary-button" style="margin-top: 16px" @click="router.push('/')">回首页</button>
    </section>
  </div>
</template>
