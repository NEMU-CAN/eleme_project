<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import BottomNav from '@/components/BottomNav.vue'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { defaultUser } from '@/data/mock'
import { maskPhone } from '@/utils/format'

const router = useRouter()
const store = useHungryStore()

// 如果用户还没登录，就回退到默认演示用户。
const user = computed(() => store.state.user ?? null)
// 页面展示的用户资料。
const displayUser = computed(() => user.value ?? defaultUser)

// 去登录页。
function goLogin() {
  router.push('/login')
}

// 去注册页。
function goRegister() {
  router.push('/register')
}
</script>

<template>
  <div class="page page--with-nav">
    <!-- 个人中心头部。 -->
    <SiteHeader title="我的" eyebrow="账户与订单" />

    <div class="page__content">
      <!-- 顶部个人信息卡。 -->
      <section class="section">
        <div class="hero-panel panel" style="background: linear-gradient(135deg, #0e6de5, #0850b5)">
          <div class="hero-panel__top">
            <div class="hero-panel__location">
              <UiIcon name="user" :size="16" />
              <span>{{ user ? '已登录' : '未登录' }}</span>
            </div>
            <span class="status-pill" style="background: rgba(255, 255, 255, 0.16); color: #fff">
              <UiIcon name="clock" :size="14" />
              演示资料
            </span>
          </div>
          <h2 class="hero-panel__headline">{{ displayUser.name }}</h2>
          <p class="hero-panel__text">{{ maskPhone(displayUser.phone) }} · {{ user?.gender === 'female' ? '女士' : '先生' }}</p>
        </div>
      </section>

      <!-- 订单统计区。 -->
      <section class="section">
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

      <!-- 账户资料展示区。 -->
      <section class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">账户信息</p>
              <h3 class="info-card__title">个人资料</h3>
            </div>
            <span class="status-pill status-pill--success">
              <UiIcon name="check" :size="14" />
              本地可编辑
            </span>
          </div>
          <div class="timeline-list">
            <div class="timeline-line">
              <span>姓名</span>
              <strong>{{ displayUser.name }}</strong>
            </div>
            <div class="timeline-line">
              <span>手机号</span>
              <strong>{{ maskPhone(displayUser.phone) }}</strong>
            </div>
            <div class="timeline-line">
              <span>性别</span>
              <strong>{{ displayUser.gender === 'female' ? '女' : '男' }}</strong>
            </div>
          </div>
        </div>
      </section>

      <!-- 常用入口：登录、注册、订单和商家页。 -->
      <section class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">快捷入口</p>
              <h3 class="info-card__title">前端流程入口</h3>
            </div>
          </div>
          <div class="chip-row">
            <button type="button" class="chip" @click="goLogin">
              <UiIcon name="user" :size="14" />
              去登录
            </button>
            <button type="button" class="chip" @click="goRegister">
              <UiIcon name="check" :size="14" />
              去注册
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

      <!-- 收货地址列表。 -->
      <section class="section">
        <div class="info-card panel">
          <div class="info-card__header">
            <div>
              <p class="eyebrow">收货地址</p>
              <h3 class="info-card__title">默认地址</h3>
            </div>
            <span class="status-pill">
              <UiIcon name="pin" :size="14" />
              {{ store.addresses.length }} 个
            </span>
          </div>
          <div class="timeline-list">
            <div v-for="address in store.addresses" :key="address.id" class="timeline-item panel--soft">
              <p class="timeline-item__name">{{ address.detail }}</p>
              <p class="timeline-item__meta">{{ address.name }} · {{ maskPhone(address.phone) }}</p>
              <p v-if="address.note" class="timeline-item__meta">{{ address.note }}</p>
            </div>
          </div>
        </div>
      </section>
    </div>

    <BottomNav />
  </div>
</template>
