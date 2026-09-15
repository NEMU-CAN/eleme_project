<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const store = useHungryStore()
const user = computed(() => store.state.user)
const merchant = computed(() => store.state.activeMerchantId ? store.getMerchant(store.state.activeMerchantId) : null)
const merchantStatus = computed(() => {
  if (!merchant.value) {
    return '状态待同步'
  }
  return merchant.value.status === 'open' ? '营业中' : '休息中'
})

const overview = [
  { label: '今日订单', value: '--', unit: '单' },
  { label: '待处理', value: '--', unit: '单' },
  { label: '今日营业额', value: '--', unit: '元' },
  { label: '在售商品', value: '--', unit: '件' },
]

const modules = [
  { title: '店铺管理', description: '门店资料、营业状态与配送设置', icon: 'store', tone: 'blue' },
  { title: '菜品管理', description: '新增菜品、库存及上下架管理', icon: 'note', tone: 'orange' },
  { title: '订单管理', description: '查看订单并处理已支付订单', icon: 'orders', tone: 'green' },
  { title: '经营概览', description: '订单趋势、收入和经营数据', icon: 'invoice', tone: 'purple' },
]

async function logout() {
  await store.logout()
  router.push('/login')
}
</script>

<template>
  <div class="merchant-center-page">
    <header class="merchant-center-header">
      <div class="merchant-center-header__top">
        <div>
          <p class="merchant-center-header__eyebrow">饿了么商家中心</p>
          <h1>{{ merchant?.name || '我的门店' }}</h1>
        </div>
        <button type="button" class="merchant-center-header__logout" @click="logout">退出</button>
      </div>
      <div class="merchant-center-profile">
        <img :src="user?.avatar || '/eleme/userImg/userImg.png'" :alt="user?.name || '商家头像'" />
        <div>
          <strong>{{ user?.name || '商家用户' }}</strong>
          <p>{{ merchant?.address || '欢迎回来，请开始管理你的门店' }}</p>
        </div>
        <span class="merchant-center-profile__status">{{ merchantStatus }}</span>
      </div>
    </header>

    <main class="merchant-center-content">
      <section class="merchant-center-section">
        <div class="merchant-center-section__heading">
          <h2>今日概览</h2>
          <span>数据模块预览</span>
        </div>
        <div class="merchant-overview-grid">
          <article v-for="item in overview" :key="item.label" class="merchant-overview-card">
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}<small>{{ item.unit }}</small></strong>
          </article>
        </div>
      </section>

      <section class="merchant-center-section">
        <div class="merchant-center-section__heading">
          <h2>常用功能</h2>
          <span>选择模块开始管理</span>
        </div>
        <div class="merchant-module-grid">
          <article v-for="item in modules" :key="item.title" class="merchant-module-card">
            <span class="merchant-module-card__icon" :class="`merchant-module-card__icon--${item.tone}`">
              <UiIcon :name="item.icon" :size="26" />
            </span>
            <div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </div>
            <UiIcon class="merchant-module-card__arrow" name="chevronRight" :size="18" />
          </article>
        </div>
      </section>

      <section class="merchant-center-section merchant-center-todo">
        <div class="merchant-center-section__heading">
          <h2>待办事项</h2>
          <span>0 项待处理</span>
        </div>
        <div class="merchant-center-todo__empty">
          <UiIcon name="check" :size="30" />
          <p>暂无待办，门店一切正常</p>
        </div>
      </section>
    </main>
  </div>
</template>
