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
const success = ref('')
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
    success.value = ''
    await store.register({
      phone: form.phone.trim(),
      password: form.password,
      nickname: form.nickname.trim(),
      gender: form.gender,
      avatar: form.avatar.trim() || null,
    })
    success.value = '注册成功，已自动登录'
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
    <SiteHeader title="用户注册" eyebrow="创建新账号" backable compact @back="router.push('/login')" />

    <section class="auth-hero">
      <div class="auth-hero__card">
        <UiIcon name="check" :size="20" />
        <h2 class="auth-hero__title">注册后直接进入系统</h2>
        <p class="auth-hero__text">注册后会自动登录，并同步生成当前账号的令牌。</p>
      </div>
    </section>

    <section class="auth-card panel">
      <div class="form-stack">
        <label class="field">
          <span class="field__label">手机号</span>
          <input v-model="form.phone" class="field__control" type="tel" autocomplete="username" placeholder="请输入手机号" />
        </label>
        <label class="field">
          <span class="field__label">昵称</span>
          <input v-model="form.nickname" class="field__control" type="text" autocomplete="nickname" placeholder="请输入昵称" />
        </label>
        <label class="field">
          <span class="field__label">密码</span>
          <input v-model="form.password" class="field__control" type="password" placeholder="设置密码" />
        </label>
        <label class="field">
          <span class="field__label">确认密码</span>
          <input v-model="form.confirm" class="field__control" type="password" placeholder="再次输入密码" />
        </label>
        <label class="field">
          <span class="field__label">头像地址（可选）</span>
          <input v-model="form.avatar" class="field__control" type="url" placeholder="可粘贴图片地址或留空" />
        </label>
        <div class="field">
          <span class="field__label">性别</span>
          <div class="chip-row">
            <button type="button" class="chip" :class="{ 'chip--active': form.gender === 0 }" @click="form.gender = 0">保密</button>
            <button type="button" class="chip" :class="{ 'chip--active': form.gender === 1 }" @click="form.gender = 1">男</button>
            <button type="button" class="chip" :class="{ 'chip--active': form.gender === 2 }" @click="form.gender = 2">女</button>
          </div>
        </div>
      </div>

      <p v-if="error" class="field__hint" style="color: var(--danger); margin-top: 12px">{{ error }}</p>
      <p v-else-if="success" class="field__hint" style="color: var(--success); margin-top: 12px">{{ success }}</p>

      <div class="auth-card__footer" style="margin-top: 16px">
        <button type="button" class="primary-button" :disabled="loading" @click="submit">
          {{ loading ? '注册中' : '注册' }}
        </button>
        <button type="button" class="secondary-button" @click="router.push('/login')">
          返回登录
        </button>
      </div>
    </section>
  </div>
</template>
