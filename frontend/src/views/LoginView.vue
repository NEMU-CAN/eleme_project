<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { defaultUser } from '@/data/mock'

const router = useRouter()
const store = useHungryStore()

// 登录表单使用本地演示数据，不依赖后端接口。
const form = reactive({
  phone: store.state.user?.phone ?? defaultUser.phone,
  password: '123456',
})

// 错误和成功状态分别用于表单反馈。
const error = ref('')
const success = ref('')

// 校验手机号和密码后，把用户资料写回本地状态。
function submit() {
  if (!/^1\d{10}$/.test(form.phone)) {
    error.value = '请输入正确的手机号码'
    return
  }

  if (form.password.length < 6) {
    error.value = '密码至少需要 6 位'
    return
  }

  error.value = ''
  store.updateUser({
    ...defaultUser,
    phone: form.phone,
  })
  success.value = '登录成功，正在进入我的页面'
  router.push('/me')
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
        <h2 class="auth-hero__title">登陆后继续结算</h2>
        <p class="auth-hero__text">这里没有后端，只做前端雏形和本地 mock，所以登录动作会直接写入页面状态。</p>
      </div>
    </section>

    <!-- 登录表单。 -->
    <section class="auth-card panel">
      <div class="form-stack">
        <label class="field">
          <span class="field__label">手机号码</span>
          <input v-model="form.phone" class="field__control" type="tel" inputmode="numeric" placeholder="请输入手机号码" />
        </label>
        <label class="field">
          <span class="field__label">密码</span>
          <input v-model="form.password" class="field__control" type="password" placeholder="请输入密码" />
        </label>
      </div>

      <p v-if="error" class="field__hint" style="color: var(--danger); margin-top: 12px">{{ error }}</p>
      <p v-else-if="success" class="field__hint" style="color: var(--success); margin-top: 12px">{{ success }}</p>

      <div class="auth-card__footer" style="margin-top: 16px">
        <button type="button" class="primary-button" @click="submit">登录</button>
        <button type="button" class="secondary-button" @click="router.push('/register')">
          去注册
        </button>
      </div>
    </section>
  </div>
</template>
