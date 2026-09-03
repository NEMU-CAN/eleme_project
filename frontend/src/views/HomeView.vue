<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import CategoryGrid from '@/components/CategoryGrid.vue'
import MerchantCard from '@/components/MerchantCard.vue'
import UiIcon from '@/components/UiIcon.vue'
import { categories } from '@/data/categories'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const store = useHungryStore()

onMounted(() => {
  void store.loadBusinesses().catch(() => undefined)
  if (store.state.user) {
    void store.loadSessionData().catch(() => undefined)
  }
})

// 首页首屏默认用当前商家，横幅和推荐入口会进入真实后端商家详情。
const featuredMerchant = computed(() => store.activeMerchant.value ?? store.merchants[0] ?? null)
// 首页展示后端返回的前几家商户。
const recommendations = computed(() => store.merchants.slice(0, 4))
const heroAddress = computed(() => {
  if (store.activeAddress.value) {
    return store.activeAddress.value.detail
  }

  return store.state.user ? '请选择收货地址' : '登录后同步收货地址'
})

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
            <span>{{ heroAddress }}</span>
          </div>
          <RouterLink to="/me" class="icon-button">
            <UiIcon name="user" :size="18" />
          </RouterLink>
        </div>
        <h2 class="hero-panel__headline">今天吃点什么</h2>
        <p class="hero-panel__text">
          商家、菜单、购物车和订单都已连接后端接口，登录后即可开始真实点餐流程。
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
      <!-- 分类入口：把前端入口映射到后端 orderTypeId。 -->
      <CategoryGrid :items="categories" />

      <!-- 推荐活动：使用后端返回的当前商家信息。 -->
      <section v-if="featuredMerchant" class="section">
        <div class="promo-banner">
          <div>
            <p class="eyebrow">推荐套餐</p>
            <h3 class="promo-banner__title">{{ featuredMerchant.name }}</h3>
            <p class="promo-banner__text">{{ featuredMerchant.description || featuredMerchant.address }}</p>
          </div>
          <RouterLink :to="`/merchant/${featuredMerchant.id}`" class="promo-banner__cta">
            立即进入
            <UiIcon name="chevronRight" :size="16" />
          </RouterLink>
        </div>
      </section>

      <!-- 会员模块：保留静态资源中的会员入口。 -->
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
      <section v-if="store.state.loading.businesses" class="page__content">
        <div class="empty-state panel">
          <h3 class="empty-state__title">正在加载商家</h3>
          <p class="empty-state__text">正在从后端获取最新商家列表。</p>
        </div>
      </section>

      <section v-else-if="recommendations.length" class="merchant-list">
        <MerchantCard v-for="merchant in recommendations" :key="merchant.id" :merchant="merchant" />
      </section>

      <section v-else class="page__content">
        <div class="empty-state panel">
          <h3 class="empty-state__title">暂无商家</h3>
          <p class="empty-state__text">{{ store.state.error || '后端暂时没有返回商家数据。' }}</p>
        </div>
      </section>
    </div>

    <BottomNav />
  </div>
</template>
