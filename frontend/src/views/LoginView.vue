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
const success = ref('')
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
    success.value = ''
    await store.login(form.phone.trim(), form.password)
    success.value = '登录成功，正在进入系统'
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
    <SiteHeader title="用户登录" eyebrow="欢迎回来" backable compact @back="router.push('/me')" />

    <section class="auth-hero">
      <div class="auth-hero__card">
        <UiIcon name="user" :size="20" />
        <h2 class="auth-hero__title">登录后继续结算</h2>
        <p class="auth-hero__text">登录会同步你的购物车、地址和订单，token 会跟随账号一起校验。</p>
      </div>
    </section>

    <section class="auth-card panel">
      <div class="form-stack">
        <label class="field">
          <span class="field__label">手机号</span>
          <input v-model="form.phone" class="field__control" type="tel" autocomplete="username" placeholder="请输入手机号" />
        </label>
        <label class="field">
          <span class="field__label">密码</span>
          <input v-model="form.password" class="field__control" type="password" autocomplete="current-password" placeholder="请输入密码" />
        </label>
      </div>

      <p v-if="error" class="field__hint" style="color: var(--danger); margin-top: 12px">{{ error }}</p>
      <p v-else-if="success" class="field__hint" style="color: var(--success); margin-top: 12px">{{ success }}</p>

      <div class="auth-card__footer" style="margin-top: 16px">
        <button type="button" class="primary-button" :disabled="loading" @click="submit">
          {{ loading ? '登录中' : '登录' }}
        </button>
        <button type="button" class="secondary-button" @click="router.push('/register')">
          去注册
        </button>
      </div>
    </section>
  </div>
</template>
