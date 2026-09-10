<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatGender, formatUserRole, maskPhone } from '@/utils/format'
import type { GenderType } from '@/types'

const router = useRouter()
const store = useHungryStore()
const user = computed(() => store.state.user)
const authenticated = computed(() => store.isAuthenticated.value)
const sessionRevoked = computed(() => Boolean(user.value && !authenticated.value))

const profileForm = reactive({
  nickname: '',
  phone: '',
  avatar: '',
  gender: 0 as GenderType,
})
const addressForm = reactive({
  contactName: '',
  contactTel: '',
  address: '',
  contactGender: 1 as GenderType,
})

const profileError = ref('')
const profileSuccess = ref('')
const addressError = ref('')
const addressSuccess = ref('')
const profileSaving = ref(false)

watch(
  user,
  (value) => {
    if (!value) {
      profileForm.nickname = ''
      profileForm.phone = ''
      profileForm.avatar = ''
      profileForm.gender = 0
      return
    }

    profileForm.nickname = value.nickname || value.name || ''
    profileForm.phone = value.phone || ''
    profileForm.avatar = value.avatar && !value.avatar.startsWith('/eleme/') ? value.avatar : ''
    profileForm.gender = value.gender
  },
  { immediate: true },
)

onMounted(async () => {
  if (!authenticated.value) {
    return
  }

  try {
    await Promise.all([store.loadAddresses(), store.loadOrders()])
  } catch (cause) {
    addressError.value = store.messageFromError(cause)
  }
})

function goLogin() {
  router.push('/login')
}

function goRegister() {
  router.push('/register')
}

async function saveProfile() {
  if (!authenticated.value) {
    goLogin()
    return
  }

  if (!profileForm.nickname.trim()) {
    profileError.value = '请输入昵称'
    return
  }

  if (!profileForm.phone.trim()) {
    profileError.value = '请输入手机号'
    return
  }

  try {
    profileSaving.value = true
    profileError.value = ''
    profileSuccess.value = ''
    await store.updateCurrentUser({
      nickname: profileForm.nickname.trim(),
      phone: profileForm.phone.trim(),
      avatar: profileForm.avatar.trim() || null,
      gender: profileForm.gender,
    })
    profileSuccess.value = '资料已更新，本地令牌已失效，请重新登录。'
  } catch (cause) {
    profileError.value = store.messageFromError(cause)
  } finally {
    profileSaving.value = false
  }
}

async function addAddress() {
  if (!authenticated.value) {
    goLogin()
    return
  }

  try {
    addressError.value = ''
    addressSuccess.value = ''
    const address = await store.createAddress({
      contactName: addressForm.contactName.trim(),
      contactGender: addressForm.contactGender,
      contactTel: addressForm.contactTel.trim(),
      address: addressForm.address.trim(),
    })
    store.setAddress(address.id)
    addressForm.contactName = ''
    addressForm.contactTel = ''
    addressForm.address = ''
    addressForm.contactGender = 1
    addressSuccess.value = '收货地址已保存'
  } catch (cause) {
    addressError.value = store.messageFromError(cause)
  }
}

async function logout() {
  await store.logout()
  router.push('/')
}
</script>

<template>
  <div class="page page--with-nav">
    <SiteHeader title="我的" eyebrow="账户与订单" />

    <div class="page__content">
      <section v-if="user" class="section">
        <div class="hero-panel panel profile-hero">
          <div class="hero-panel__top">
            <div class="hero-panel__location">
              <UiIcon name="user" :size="16" />
              <span>{{ authenticated ? '已登录' : '令牌已失效' }}</span>
            </div>
            <span :class="authenticated ? 'status-pill status-pill--success' : 'status-pill status-pill--warning'">
              <UiIcon name="clock" :size="14" />
              {{ authenticated ? '后端同步' : '需要重新登录' }}
            </span>
          </div>

          <div class="profile-hero__body">
            <img class="profile-hero__avatar" :src="user.avatar" :alt="user.name" />
            <div class="profile-hero__copy">
              <h2 class="hero-panel__headline">{{ user.name }}</h2>
              <p class="hero-panel__text">{{ user.phone }} · {{ formatUserRole(user.role) }}</p>
              <p class="hero-panel__text">{{ formatGender(user.gender) }} · {{ maskPhone(user.phone) }}</p>
            </div>
          </div>
        </div>
      </section>

      <section v-else class="section">
        <div class="empty-state panel">
          <h3 class="empty-state__title">登录后管理账户</h3>
          <p class="empty-state__text">登录后可以同步订单、购物车和收货地址。</p>
          <div class="chip-row chip-row--center">
            <button type="button" class="primary-button" @click="goLogin">去登录</button>
            <button type="button" class="secondary-button" @click="goRegister">去注册</button>
          </div>
        </div>
      </section>

      <section v-if="sessionRevoked" class="section">
        <div class="info-card panel session-banner">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">会话状态</p>
              <h3 class="info-card__title">资料已更新</h3>
            </div>
            <span class="status-pill status-pill--warning">
              <UiIcon name="clock" :size="14" />
              令牌已失效
            </span>
          </div>
          <p class="info-card__text">{{ profileSuccess || '当前本地 token 已清空。重新登录后，订单、地址和购物车会再次同步。' }}</p>
          <div class="chip-row">
            <button type="button" class="primary-button" @click="goLogin">去登录</button>
          </div>
        </div>
      </section>

      <section v-if="authenticated" class="section">
        <div class="detail-stats__row">
          <article class="detail-stat">
            <p class="detail-stat__label">待支付</p>
            <p class="detail-stat__value">{{ store.unpaidOrders.value }}</p>
          </article>
          <article class="detail-stat">
            <p class="detail-stat__label">已完成</p>
            <p class="detail-stat__value">{{ store.completedOrders.value }}</p>
          </article>
        </div>
      </section>

      <section v-if="authenticated" class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">账户信息</p>
              <h3 class="info-card__title">编辑资料</h3>
            </div>
            <span class="status-pill status-pill--success">
              <UiIcon name="check" :size="14" />
              后端同步
            </span>
          </div>

          <div class="form-stack profile-form">
            <label class="field">
              <span class="field__label">昵称</span>
              <input v-model="profileForm.nickname" class="field__control" type="text" maxlength="20" placeholder="请输入昵称" />
            </label>
            <label class="field">
              <span class="field__label">手机号</span>
              <input v-model="profileForm.phone" class="field__control" type="tel" placeholder="请输入手机号" />
            </label>
            <label class="field">
              <span class="field__label">头像地址（留空使用默认头像）</span>
              <input v-model="profileForm.avatar" class="field__control" type="url" placeholder="可粘贴图片地址" />
            </label>
            <div class="field">
              <span class="field__label">性别</span>
              <div class="chip-row">
                <button type="button" class="chip" :class="{ 'chip--active': profileForm.gender === 0 }" @click="profileForm.gender = 0">保密</button>
                <button type="button" class="chip" :class="{ 'chip--active': profileForm.gender === 1 }" @click="profileForm.gender = 1">先生</button>
                <button type="button" class="chip" :class="{ 'chip--active': profileForm.gender === 2 }" @click="profileForm.gender = 2">女士</button>
              </div>
            </div>
          </div>

          <div class="auth-card__footer profile-actions">
            <div class="chip-row">
              <button type="button" class="secondary-button" @click="logout">
                <UiIcon name="user" :size="14" />
                退出登录
              </button>
            </div>
            <button type="button" class="primary-button" :disabled="profileSaving" @click="saveProfile">
              {{ profileSaving ? '保存中' : '保存资料' }}
            </button>
          </div>
          <p v-if="profileError" class="field__hint field__hint--danger">{{ profileError }}</p>
          <p v-else-if="profileSuccess" class="field__hint field__hint--success">{{ profileSuccess }}</p>
        </div>
      </section>

      <section class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">快捷入口</p>
              <h3 class="info-card__title">账户操作</h3>
            </div>
          </div>
          <div class="chip-row">
            <button v-if="!user" type="button" class="chip" @click="goLogin">
              <UiIcon name="user" :size="14" />
              去登录
            </button>
            <button v-if="!user" type="button" class="chip" @click="goRegister">
              <UiIcon name="check" :size="14" />
              去注册
            </button>
            <button v-if="authenticated" type="button" class="chip" @click="logout">
              <UiIcon name="user" :size="14" />
              退出登录
            </button>
            <button type="button" class="chip" @click="router.push('/orders')">
              <UiIcon name="orders" :size="14" />
              查看订单
            </button>
            <button type="button" class="chip" @click="router.push('/businesses')">
              <UiIcon name="compass" :size="14" />
              去商家页
            </button>
          </div>
        </div>
      </section>

      <section v-if="authenticated" class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">收货地址</p>
              <h3 class="info-card__title">我的地址</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="pin" :size="14" />
              {{ store.addresses.length }} 个
            </span>
          </div>

          <div v-if="store.addresses.length" class="timeline-list">
            <button
              v-for="address in store.addresses"
              :key="address.id"
              type="button"
              class="timeline-item panel--soft"
              :class="{ 'timeline-item--active': address.id === store.state.addressId }"
              @click="store.setAddress(address.id)"
            >
              <p class="timeline-item__name">{{ address.detail }}</p>
              <p class="timeline-item__meta">{{ address.name }} · {{ maskPhone(address.phone) }}</p>
            </button>
          </div>
          <div v-else class="empty-state panel--soft">
            <p class="empty-state__text">还没有收货地址，请填写下面的表单。</p>
          </div>

          <div class="form-stack profile-form">
            <label class="field">
              <span class="field__label">联系人</span>
              <input v-model="addressForm.contactName" class="field__control" type="text" placeholder="请输入联系人姓名" />
            </label>
            <label class="field">
              <span class="field__label">联系电话</span>
              <input v-model="addressForm.contactTel" class="field__control" type="tel" placeholder="请输入联系电话" />
            </label>
            <label class="field">
              <span class="field__label">收货地址</span>
              <textarea v-model="addressForm.address" class="field__control field__control--textarea" rows="3" placeholder="请输入详细地址" />
            </label>
            <div class="field">
              <span class="field__label">联系人性别</span>
              <div class="chip-row">
                <button type="button" class="chip" :class="{ 'chip--active': addressForm.contactGender === 0 }" @click="addressForm.contactGender = 0">保密</button>
                <button type="button" class="chip" :class="{ 'chip--active': addressForm.contactGender === 1 }" @click="addressForm.contactGender = 1">先生</button>
                <button type="button" class="chip" :class="{ 'chip--active': addressForm.contactGender === 2 }" @click="addressForm.contactGender = 2">女士</button>
              </div>
            </div>
          </div>

          <div class="auth-card__footer profile-actions">
            <p class="field__hint">最新地址会直接写入后端地址表。</p>
            <button type="button" class="primary-button" :disabled="store.state.loading.addresses" @click="addAddress">
              {{ store.state.loading.addresses ? '保存中' : '新增地址' }}
            </button>
          </div>
          <p v-if="addressError" class="field__hint field__hint--danger">{{ addressError }}</p>
          <p v-else-if="addressSuccess" class="field__hint field__hint--success">{{ addressSuccess }}</p>
        </div>
      </section>
    </div>

    <BottomNav />
  </div>
</template>
