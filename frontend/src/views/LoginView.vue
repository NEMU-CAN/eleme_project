<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()

const redirect = computed(() => (typeof route.query.redirect === 'string' ? route.query.redirect : '/me'))

const form = reactive({
  phone: store.state.user?.phone ?? '',
  password: '',
})

const error = ref('')
const loading = ref(false)

async function submit() {
  if (!form.phone.trim()) {
    error.value = '请输入手机号'
    return
  }
  if (!form.password.trim()) {
    error.value = '请输入密码'
    return
  }
  try {
    loading.value = true
    error.value = ''
    await store.login(form.phone.trim(), form.password)
    router.push(redirect.value)
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-shell">
    <SiteHeader title="用户登录" backable @back="router.push('/me')" />

    <div class="auth-brand">
      <span class="auth-brand__logo"><UiIcon name="cart" :size="36" /></span>
      <h2 class="auth-brand__title">饿了么</h2>
      <p class="auth-brand__sub">登录后同步购物车、地址和订单</p>
    </div>

    <div class="auth-form">
      <div class="field">
        <span class="field__label">手机号</span>
        <input v-model="form.phone" class="field__control" type="tel" autocomplete="username" placeholder="请输入手机号" />
      </div>
      <div class="field">
        <span class="field__label">密码</span>
        <input v-model="form.password" class="field__control" type="password" autocomplete="current-password" placeholder="请输入密码" />
      </div>
    </div>

    <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>

    <div class="auth-form__actions">
      <button type="button" class="primary-button" :disabled="loading" @click="submit">
        {{ loading ? '登录中' : '登录' }}
      </button>
      <button type="button" class="secondary-button" @click="router.push('/register')">去注册</button>
    </div>

    <p class="auth-switch"><a @click="router.push('/register')">还没有账号？立即注册</a></p>
  </div>
</template>
