<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import UiIcon from '@/components/UiIcon.vue'
import LazyImage from '@/components/LazyImage.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { maskPhone } from '@/utils/format'
import type { GenderType } from '@/types'

const router = useRouter()
const store = useHungryStore()
const user = computed(() => store.state.user)
const authenticated = computed(() => store.isAuthenticated.value)
const showEdit = ref(false)
const notice = ref('')
const profileError = ref('')
const profileSaving = ref(false)
const profileForm = reactive({ nickname: '', phone: '', avatar: '', gender: 0 as GenderType })
const genderOptions: Array<{ value: GenderType; label: string }> = [{ value: 0, label: '保密' }, { value: 1, label: '先生' }, { value: 2, label: '女士' }]

const cartItemCount = computed(() => store.state.cartItems.reduce((sum, item) => sum + item.quantity, 0))
const orderCounts = computed(() => ({
  all: store.state.orders.length,
  unpaid: store.state.orders.filter((item) => item.status === 'unpaid').length,
  delivering: store.state.orders.filter((item) => item.status === 'paid').length,
  completed: store.state.orders.filter((item) => item.status === 'completed').length,
}))
const frequentStores = computed(() => {
  const counts = new Map<string, { id: string; name: string; image: string; count: number }>()
  for (const order of store.state.orders) {
    const current = counts.get(order.businessId)
    if (current) current.count += 1
    else counts.set(order.businessId, { id: order.businessId, name: order.merchantName, image: order.merchantImage, count: 1 })
  }
  return [...counts.values()].sort((a, b) => b.count - a.count).slice(0, 3)
})

watch(user, (value) => {
  if (!value) return
  profileForm.nickname = value.nickname || value.name || ''
  profileForm.phone = value.phone || ''
  profileForm.avatar = value.avatar && !value.avatar.startsWith('/eleme/') ? value.avatar : ''
  profileForm.gender = value.gender
}, { immediate: true })

onMounted(() => { if (authenticated.value) void store.loadSessionData().catch(() => undefined) })
function goOrders(filter?: string) { router.push(filter ? `/orders?filter=${filter}` : '/orders') }
function flashNotice(text: string) { notice.value = text; window.setTimeout(() => { notice.value = '' }, 1600) }
async function saveProfile() {
  if (!authenticated.value) return void router.push('/login')
  if (!profileForm.nickname.trim() || !profileForm.phone.trim()) { profileError.value = '请完整填写昵称和手机号'; return }
  try {
    profileSaving.value = true; profileError.value = ''
    await store.updateCurrentUser({ nickname: profileForm.nickname.trim(), phone: profileForm.phone.trim(), avatar: profileForm.avatar.trim() || null, gender: profileForm.gender })
    showEdit.value = false
  } catch (cause) { profileError.value = store.messageFromError(cause) } finally { profileSaving.value = false }
}
async function logout() { await store.logout(); router.push('/') }
</script>

<template>
  <div class="page page--with-nav ele-me-page">
    <header class="ele-me-hero">
      <div class="ele-me-hero__top"><span>我的</span><button @click="showEdit = authenticated ? !showEdit : false"><UiIcon name="edit" :size="20" /></button></div>
      <button class="ele-me-profile" @click="authenticated ? showEdit = !showEdit : router.push('/login')">
        <img v-if="user" :src="user.avatar" :alt="user.name" />
        <span v-else class="ele-me-profile__guest"><UiIcon name="user" :size="34" /></span>
        <div><h1>{{ user?.nickname || '登录 / 注册' }}</h1><p>{{ user ? maskPhone(user.phone) : '登录后查看订单和常点商家' }}</p></div>
        <UiIcon name="chevronRight" :size="20" />
      </button>
      <div class="ele-me-stats">
        <button @click="router.push('/cart')"><b>{{ cartItemCount }}</b><span>购物车</span></button>
        <button @click="router.push('/addresses')"><b>{{ store.state.addresses.length }}</b><span>收货地址</span></button>
        <button @click="goOrders()"><b>{{ orderCounts.all }}</b><span>累计订单</span></button>
      </div>
    </header>

    <p v-if="notice" class="ele-me-toast">{{ notice }}</p>
    <main class="ele-me-content">
      <section class="ele-me-card">
        <div class="ele-me-card__title"><h2>我的订单</h2><button @click="goOrders()">全部订单 <UiIcon name="chevronRight" :size="14" /></button></div>
        <div class="ele-me-orders">
          <button @click="goOrders()"><i><UiIcon name="orders" :size="25" /></i><span>全部</span><em v-if="orderCounts.all">{{ orderCounts.all }}</em></button>
          <button @click="goOrders('unpaid')"><i><UiIcon name="wallet" :size="25" /></i><span>待支付</span><em v-if="orderCounts.unpaid">{{ orderCounts.unpaid }}</em></button>
          <button @click="goOrders('paid')"><i><UiIcon name="scooter" :size="25" /></i><span>配送中</span><em v-if="orderCounts.delivering">{{ orderCounts.delivering }}</em></button>
          <button @click="goOrders('completed')"><i><UiIcon name="bubble" :size="25" /></i><span>已完成</span><em v-if="orderCounts.completed">{{ orderCounts.completed }}</em></button>
        </div>
      </section>

      <section class="ele-me-card ele-me-service-card">
        <div class="ele-me-card__title"><h2>便捷服务</h2></div>
        <div class="ele-me-services">
          <button @click="router.push('/addresses')"><i class="blue"><UiIcon name="pin" :size="24" /></i><span>我的地址</span></button>
          <button @click="router.push('/cart')"><i class="orange"><UiIcon name="cart" :size="24" /></i><span>购物车</span></button>
          <button @click="flashNotice('客服功能敬请期待')"><i class="purple"><UiIcon name="headphone" :size="24" /></i><span>我的客服</span></button>
          <button @click="flashNotice('开票功能敬请期待')"><i class="green"><UiIcon name="invoice" :size="24" /></i><span>开发票</span></button>
        </div>
      </section>

      <section class="ele-me-card">
        <div class="ele-me-card__title"><h2>常点的店</h2><button @click="router.push('/')">去发现 <UiIcon name="chevronRight" :size="14" /></button></div>
        <div v-if="frequentStores.length" class="ele-me-frequent">
          <button v-for="merchant in frequentStores" :key="merchant.id" @click="router.push(`/merchant/${merchant.id}`)"><LazyImage :src="merchant.image" :alt="merchant.name" /><div><b>{{ merchant.name }}</b><span>点过 {{ merchant.count }} 次</span></div><UiIcon name="chevronRight" :size="16" /></button>
        </div>
        <div v-else class="ele-me-empty">完成订单后，这里会出现你的常点商家</div>
      </section>

      <section v-if="showEdit" class="ele-me-card ele-me-editor">
        <div class="ele-me-card__title"><h2>编辑资料</h2><button @click="showEdit = false">取消</button></div>
        <label><span>昵称</span><input v-model="profileForm.nickname" maxlength="20" /></label>
        <label><span>手机号</span><input v-model="profileForm.phone" type="tel" /></label>
        <div class="ele-me-gender"><span>性别</span><button v-for="option in genderOptions" :key="option.value" :class="{ active: profileForm.gender === option.value }" @click="profileForm.gender = option.value">{{ option.label }}</button></div>
        <p v-if="profileError" class="ele-store-error">{{ profileError }}</p>
        <button class="ele-me-save" :disabled="profileSaving" @click="saveProfile">{{ profileSaving ? '保存中…' : '保存资料' }}</button>
      </section>

      <button v-if="authenticated" class="ele-me-logout" @click="logout">退出登录</button>
    </main>
    <BottomNav />
  </div>
</template>
