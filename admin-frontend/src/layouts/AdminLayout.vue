<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppLogo from '@/components/AppLogo.vue'
import { currentUser, logout } from '@/services/auth'
import { applyTheme, theme } from '@/composables/useTheme'

const route = useRoute()
const router = useRouter()
const signingOut = ref(false)
const pageTitle = computed(() => String(route.meta.title || '运营中枢'))
const pageDescription = computed(() => String(route.meta.description || ''))

const navItems = [
  { to: '/dashboard', label: '运营总览' },
  { to: '/users', label: '用户管理' },
  { to: '/businesses', label: '商家管理' },
  { to: '/orders', label: '订单查询' },
]

async function signOut() {
  signingOut.value = true
  try {
    await logout()
  } finally {
    await router.replace('/login')
    signingOut.value = false
  }
}
</script>

<template>
  <div class="admin-shell">
    <aside class="sidebar">
      <AppLogo />
      <nav class="primary-nav" aria-label="管理功能">
        <RouterLink v-for="item in navItems" :key="item.to" :to="item.to">
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <div class="sidebar-foot">
        <span class="system-state"><i /> 服务连接由接口状态决定</span>
        <small>ADMIN CONSOLE</small>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div class="page-heading">
          <h1>{{ pageTitle }}</h1>
          <p>{{ pageDescription }}</p>
        </div>
        <div class="topbar-actions">
          <div class="theme-switch" aria-label="界面主题">
            <button type="button" :class="{ active: theme === 'light' }" @click="applyTheme('light')">浅色</button>
            <button type="button" :class="{ active: theme === 'dark' }" @click="applyTheme('dark')">深色</button>
          </div>
          <div class="admin-identity">
            <span class="avatar">A</span>
            <span><strong>{{ currentUser?.nickname || 'admin' }}</strong><small>系统管理员</small></span>
          </div>
          <button class="text-button" type="button" :disabled="signingOut" @click="signOut">
            {{ signingOut ? '退出中' : '退出' }}
          </button>
        </div>
      </header>
      <div class="workspace-content"><RouterView /></div>
    </main>
  </div>
</template>
