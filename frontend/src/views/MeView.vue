<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { maskPhone } from '@/utils/format'

const router = useRouter()
const store = useHungryStore()
const user = computed(() => store.state.user)
const addressForm = reactive({
  contactName: '',
  contactTel: '',
  address: '',
  contactSex: 1,
})
const addressError = ref('')
const addressSuccess = ref('')

onMounted(async () => {
  if (!user.value) {
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

async function addAddress() {
  if (!user.value) {
    goLogin()
    return
  }

  try {
    addressError.value = ''
    addressSuccess.value = ''
    const address = await store.createAddress({
      contactName: addressForm.contactName,
      contactSex: addressForm.contactSex,
      contactTel: addressForm.contactTel,
      address: addressForm.address,
    })
    store.setAddress(address.id)
    addressForm.contactName = ''
    addressForm.contactTel = ''
    addressForm.address = ''
    addressForm.contactSex = 1
    addressSuccess.value = '收货地址已保存'
  } catch (cause) {
    addressError.value = store.messageFromError(cause)
  }
}

function logout() {
  store.logout()
  router.push('/')
}
</script>

<template>
  <div class="page page--with-nav">
    <SiteHeader title="我的" eyebrow="账户与订单" />

    <div class="page__content">
      <section v-if="user" class="section">
        <div class="hero-panel panel" style="background: linear-gradient(135deg, #0e6de5, #0850b5)">
          <div class="hero-panel__top">
            <div class="hero-panel__location">
              <UiIcon name="user" :size="16" />
              <span>已登录</span>
            </div>
            <span class="status-pill" style="background: rgba(255, 255, 255, 0.16); color: #fff">
              <UiIcon name="clock" :size="14" />
              后端资料
            </span>
          </div>
          <h2 class="hero-panel__headline">{{ user.name }}</h2>
          <p class="hero-panel__text">{{ user.id }} · {{ user.gender === 'female' ? '女士' : '先生' }}</p>
        </div>
      </section>

      <section v-else class="section">
        <div class="empty-state panel">
          <h3 class="empty-state__title">登录后管理账户</h3>
          <p class="empty-state__text">登录后可以同步订单、购物车和收货地址。</p>
          <button type="button" class="primary-button" style="margin-top: 16px" @click="goLogin">去登录</button>
        </div>
      </section>

      <section v-if="user" class="section">
        <div class="detail-stats__row">
          <article class="detail-stat">
            <p class="detail-stat__label">待支付</p>
            <p class="detail-stat__value">{{ store.unreadOrders.value }}</p>
          </article>
          <article class="detail-stat">
            <p class="detail-stat__label">已完成</p>
            <p class="detail-stat__value">{{ store.completedOrders.value }}</p>
          </article>
        </div>
      </section>

      <section v-if="user" class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">账户信息</p>
              <h3 class="info-card__title">个人资料</h3>
            </div>
            <span class="status-pill status-pill--success">
              <UiIcon name="check" :size="14" />
              后端同步
            </span>
          </div>
          <div class="timeline-list">
            <div class="timeline-line">
              <span>用户编号</span>
              <strong>{{ user.id }}</strong>
            </div>
            <div class="timeline-line">
              <span>姓名</span>
              <strong>{{ user.name }}</strong>
            </div>
            <div class="timeline-line">
              <span>性别</span>
              <strong>{{ user.gender === 'female' ? '女' : '男' }}</strong>
            </div>
          </div>
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
            <button v-else type="button" class="chip" @click="logout">
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

      <section class="section">
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

          <div v-if="!user" class="empty-state panel--soft">
            <p class="empty-state__text">登录后可以从后端读取和新增收货地址。</p>
          </div>
          <div v-else-if="store.addresses.length" class="timeline-list">
            <button
              v-for="address in store.addresses"
              :key="address.id"
              type="button"
              class="timeline-item panel--soft"
              :class="{ 'sort-chip--active': address.id === store.state.addressId }"
              @click="store.setAddress(address.id)"
            >
              <p class="timeline-item__name">{{ address.detail }}</p>
              <p class="timeline-item__meta">{{ address.name }} · {{ maskPhone(address.phone) }}</p>
            </button>
          </div>
          <div v-else class="empty-state panel--soft">
            <p class="empty-state__text">还没有收货地址，请填写下面的表单。</p>
          </div>

          <div v-if="user" class="form-stack">
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
            <div class="auth-card__footer">
              <div class="chip-row">
                <button type="button" class="chip" :class="{ 'chip--active': addressForm.contactSex === 1 }" @click="addressForm.contactSex = 1">先生</button>
                <button type="button" class="chip" :class="{ 'chip--active': addressForm.contactSex === 0 }" @click="addressForm.contactSex = 0">女士</button>
              </div>
              <button type="button" class="primary-button" :disabled="store.state.loading.addresses" @click="addAddress">
                {{ store.state.loading.addresses ? '保存中' : '新增地址' }}
              </button>
            </div>
            <p v-if="addressError" class="field__hint" style="color: var(--danger)">{{ addressError }}</p>
            <p v-if="addressSuccess" class="field__hint" style="color: var(--success)">{{ addressSuccess }}</p>
          </div>
        </div>
      </section>
    </div>

    <BottomNav />
  </div>
</template>
