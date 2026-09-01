<script setup lang="ts">
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'

const router = useRouter()
</script>

<template>
  <div class="page page--bare">
    <!-- 404 页面头部。 -->
    <SiteHeader title="页面未找到" eyebrow="404" />
    <!-- 兜底提示：引导用户回首页或商家列表。 -->
    <section class="page__content">
      <div class="empty-state panel">
        <h3 class="empty-state__title">这里没有你要找的页面</h3>
        <p class="empty-state__text">先回首页或者商家列表，页面雏形都已经准备好了。</p>
        <div class="chip-row" style="justify-content: center; margin-top: 16px">
          <button type="button" class="primary-button" @click="router.push('/')">回首页</button>
          <button type="button" class="secondary-button" @click="router.push('/businesses')">去商家列表</button>
        </div>
      </div>
    </section>
  </div>
</template>
