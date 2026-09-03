<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import CategoryGrid from '@/components/CategoryGrid.vue'
import MerchantCard from '@/components/MerchantCard.vue'
import UiIcon from '@/components/UiIcon.vue'
import { categories } from '@/data/mock'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const store = useHungryStore()

// 首页首屏默认用当前商家，横幅和推荐入口可以直接串到详情页。
const featuredMerchant = computed(() => store.activeMerchant.value ?? store.merchants[0]!)
// 首页只展示前几家商户，作为课程项目里的精选推荐区。
const recommendations = computed(() => store.merchants.slice(0, 4))

// 跳转到商家列表页，承接首页搜索和分类入口。
function goBusinesses() {
  router.push('/businesses')
}
</script>

<template>
  <div class="page page--with-nav">
    <!-- 首屏：定位、个人入口、搜索和主视觉。 -->
    <section class="hero-section">
      <div class="hero-panel">
        <div class="hero-panel__top">
          <div class="hero-panel__location">
            <UiIcon name="pin" :size="16" />
            <span>{{ store.activeAddress.value?.detail || '沈阳市规划大厦' }}</span>
          </div>
          <RouterLink to="/me" class="icon-button">
            <UiIcon name="user" :size="18" />
          </RouterLink>
        </div>
        <h2 class="hero-panel__headline">今天吃点什么</h2>
        <p class="hero-panel__text">
          参考课件结构做成的 Vue 3 + TypeScript 前端雏形，页面、交互和数据状态都已经串起来了。
        </p>
        <div class="hero-panel__actions">
          <button type="button" class="secondary-button" @click="goBusinesses">
            <UiIcon name="search" :size="16" />
            搜索商家
          </button>
          <RouterLink to="/orders" class="ghost-button">
            查看订单
          </RouterLink>
        </div>
      </div>
      <button type="button" class="search-panel" @click="goBusinesses">
        <UiIcon name="search" :size="18" />
        <span class="search-panel__input">搜索饿了么商家、商品名称</span>
      </button>
    </section>

    <div class="page__content">
      <!-- 分类入口：对应课件里的首页九宫格。 -->
      <CategoryGrid :items="categories" />

      <!-- 推荐活动：保留课件里的横幅模块，但用更现代的卡片方式呈现。 -->
      <section class="section">
        <div class="promo-banner">
          <div>
            <p class="eyebrow">推荐套餐</p>
            <h3 class="promo-banner__title">品质套餐</h3>
            <p class="promo-banner__text">把课程里的横幅和会员模块保留下来，但让它们变得更像真实产品页面。</p>
          </div>
          <RouterLink :to="`/merchant/${featuredMerchant.id}`" class="promo-banner__cta">
            立即进入
            <UiIcon name="chevronRight" :size="16" />
          </RouterLink>
        </div>
      </section>

      <!-- 会员模块：展示课程课件中的超级会员入口。 -->
      <section class="section">
        <div class="member-strip">
          <div class="member-strip__left">
            <img src="/eleme/super_member.png" alt="超级会员" width="48" height="48" />
            <div>
              <p class="member-strip__title">超级会员</p>
              <p class="member-strip__text">每月享超值权益，点单更划算</p>
            </div>
          </div>
          <button type="button" class="ghost-button">立即开通</button>
        </div>
      </section>

      <!-- 商家推荐标题区。 -->
      <section class="section">
        <div class="merchant-list__header">
          <div>
            <p class="eyebrow">精选商家</p>
            <h3 class="merchant-list__heading">推荐商家</h3>
          </div>
          <button type="button" class="chip" @click="goBusinesses">
            全部商家
            <UiIcon name="chevronRight" :size="14" />
          </button>
        </div>
      </section>

      <!-- 商家推荐列表：用卡片承载商户信息。 -->
      <section class="merchant-list">
        <MerchantCard v-for="merchant in recommendations" :key="merchant.id" :merchant="merchant" />
      </section>
    </div>

    <BottomNav />
  </div>
</template>
