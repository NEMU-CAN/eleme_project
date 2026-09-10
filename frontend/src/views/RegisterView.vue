<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import type { GenderType } from '@/types'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()

const redirect = route.query.redirect

const form = reactive({
  phone: store.state.user?.phone ?? '',
  nickname: store.state.user?.nickname ?? '',
  password: '',
  confirm: '',
  gender: (store.state.user?.gender ?? 0) as GenderType,
  avatar: '',
})

const error = ref('')
const loading = ref(false)

async function submit() {
  if (!form.phone.trim()) {
    error.value = '请输入手机号'
    return
  }
  if (!form.nickname.trim()) {
    error.value = '请输入昵称'
    return
  }
  if (form.nickname.length > 20) {
    error.value = '昵称不能超过 20 个字符'
    return
  }
  if (!form.password.trim()) {
    error.value = '请输入密码'
    return
  }
  if (form.password !== form.confirm) {
    error.value = '两次输入的密码不一致'
    return
  }
  try {
    loading.value = true
    error.value = ''
    await store.register({
      phone: form.phone.trim(),
      password: form.password,
      nickname: form.nickname.trim(),
      gender: form.gender,
      avatar: form.avatar.trim() || null,
    })
    router.push(typeof redirect === 'string' ? redirect : '/me')
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-shell">
    <SiteHeader title="用户注册" backable @back="router.push('/login')" />

    <div class="auth-brand">
      <span class="auth-brand__logo"><UiIcon name="user" :size="36" /></span>
      <h2 class="auth-brand__title">创建账号</h2>
      <p class="auth-brand__sub">注册后自动登录，同步生成账号令牌</p>
    </div>

    <div class="auth-form">
      <div class="field">
        <span class="field__label">手机号</span>
        <input v-model="form.phone" class="field__control" type="tel" autocomplete="username" placeholder="请输入手机号" />
      </div>
      <div class="field">
        <span class="field__label">昵称</span>
        <input v-model="form.nickname" class="field__control" type="text" autocomplete="nickname" placeholder="请输入昵称" />
      </div>
      <div class="field">
        <span class="field__label">密码</span>
        <input v-model="form.password" class="field__control" type="password" placeholder="设置密码" />
      </div>
      <div class="field">
        <span class="field__label">确认密码</span>
        <input v-model="form.confirm" class="field__control" type="password" placeholder="再次输入密码" />
      </div>
      <div class="field">
        <span class="field__label">头像地址</span>
        <input v-model="form.avatar" class="field__control" type="url" placeholder="可留空使用默认头像" />
      </div>
      <div class="field">
        <span class="field__label">性别</span>
        <div class="seg">
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.gender === 0 }" @click="form.gender = 0">保密</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.gender === 1 }" @click="form.gender = 1">先生</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.gender === 2 }" @click="form.gender = 2">女士</button>
        </div>
      </div>
    </div>

    <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>

    <div class="auth-form__actions">
      <button type="button" class="primary-button" :disabled="loading" @click="submit">
        {{ loading ? '注册中' : '注册' }}
      </button>
      <button type="button" class="secondary-button" @click="router.push('/login')">返回登录</button>
    </div>
  </div>
</template>
