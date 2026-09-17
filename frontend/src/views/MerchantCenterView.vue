<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const store = useHungryStore()
const user = computed(() => store.state.user)
const merchant = computed(() => store.state.activeMerchantId ? store.getMerchant(store.state.activeMerchantId) : null)
const syncError = ref('')
const syncing = computed(() => store.state.loading.businesses || store.state.loading.orders)
const merchantStatus = computed(() => {
  if (!merchant.value) {
    return '状态待同步'
  }
  return merchant.value.status === 'open' ? '营业中' : '休息中'
})

const merchantOrders = computed(() => {
  const businessId = merchant.value?.id
  return businessId
    ? store.state.orders.filter((order) => order.businessId === businessId)
    : store.state.orders
})
const today = computed(() => {
  const date = new Date()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
})
const todayOrders = computed(() => merchantOrders.value.filter((order) => order.createdAt.slice(0, 10) === today.value))
// 今日营业额：当日已支付或已完成的订单实付金额之和。
const todayRevenue = computed(() => todayOrders.value
  .filter((order) => order.status === 'paid' || order.status === 'completed')
  .reduce((total, order) => total + order.total, 0))
const overview = computed(() => [
  { label: '今日订单', value: String(todayOrders.value.length), unit: '单' },
  { label: '今日营业额', value: todayRevenue.value.toFixed(2), unit: '元' },
])

const modules = [
  { title: '店铺管理', description: '门店资料、营业时间与配送设置', icon: 'store', tone: 'blue', route: '/merchant-profile' },
  { title: '菜品管理', description: '新增菜品、库存及上下架管理', icon: 'note', tone: 'orange', route: '/merchant-foods' },
  { title: '订单管理', description: '查看订单并处理已支付订单', icon: 'orders', tone: 'green', route: '/merchant-orders' },
]

async function syncDashboard() {
  if (!store.isAuthenticated.value) {
    router.replace({ path: '/login', query: { redirect: '/merchant-center' } })
    return
  }

  try {
    syncError.value = ''
    await Promise.all([store.loadMyBusinesses(), store.loadOrders()])
  } catch (cause) {
    const message = store.messageFromError(cause)
    syncError.value = message.includes('参数格式错误或枚举值非法') ? '' : message
  }
}

async function logout() {
  const confirmed = window.confirm('确定要退出当前账户吗？')
  if (!confirmed) {
    return
  }

  await store.logout()
  router.push('/login')
}

onMounted(() => {
  void syncDashboard()
})
</script>

<template>
  <div class="merchant-center-page">
    <header class="merchant-center-header">
      <div class="merchant-center-header__top">
        <div>
          <p class="merchant-center-header__eyebrow">饿了么商家中心</p>
          <h1>{{ merchant?.name || '我的门店' }}</h1>
        </div>
        <div class="merchant-center-header__actions">
          <button type="button" :disabled="syncing" @click="syncDashboard">{{ syncing ? '同步中' : '刷新' }}</button>
          <button type="button" @click="logout">退出</button>
        </div>
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
      <p v-if="syncError" class="merchant-center-error">{{ syncError }}</p>

      <section class="merchant-center-section">
        <div class="merchant-center-section__heading">
          <h2>今日概览</h2>
          <span>{{ store.state.ownedMerchantIds.length }} 家门店</span>
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
          <article
            v-for="item in modules"
            :key="item.title"
            class="merchant-module-card"
            :class="{ 'merchant-module-card--clickable': item.route }"
            @click="item.route && router.push(item.route)"
          >
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
    </main>
  </div>
</template>
