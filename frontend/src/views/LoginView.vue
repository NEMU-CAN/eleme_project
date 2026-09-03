<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()

// 后端以 userId + password 创建登录会话。
const form = reactive({
  userId: store.state.user?.id ?? '',
  password: '',
})

// 错误和成功状态分别用于表单反馈。
const error = ref('')
const success = ref('')
const loading = ref(false)

// 校验用户编号和密码后，请求后端登录接口。
async function submit() {
  if (!form.userId.trim()) {
    error.value = '请输入用户编号'
    return
  }

  if (form.userId.length > 20) {
    error.value = '用户编号不能超过 20 位'
    return
  }

  if (!form.password || form.password.length > 20) {
    error.value = '请输入 1-20 位密码'
    return
  }

  try {
    loading.value = true
    error.value = ''
    success.value = ''
    await store.login(form.userId.trim(), form.password)
    success.value = '登录成功，正在同步账户数据'
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/me'
    router.push(redirect)
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-shell">
    <!-- 登录页头部。 -->
    <SiteHeader title="用户登录" eyebrow="欢迎回来" backable compact @back="router.push('/me')" />

    <!-- 登录说明卡。 -->
    <section class="auth-hero">
      <div class="auth-hero__card">
        <UiIcon name="user" :size="20" />
        <h2 class="auth-hero__title">登录后继续结算</h2>
        <p class="auth-hero__text">登录会请求后端会话接口，并同步你的购物车、订单和收货地址。</p>
      </div>
    </section>

    <!-- 登录表单。 -->
    <section class="auth-card panel">
      <div class="form-stack">
        <label class="field">
          <span class="field__label">用户编号</span>
          <input v-model="form.userId" class="field__control" type="text" autocomplete="username" placeholder="请输入用户编号" />
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
