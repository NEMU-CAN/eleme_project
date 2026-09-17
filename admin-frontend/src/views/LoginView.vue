<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/services/auth'
import { applyTheme, theme } from '@/composables/useTheme'
import { errorMessage } from '@/utils/format'
import AppLogo from '@/components/AppLogo.vue'

const router = useRouter()
const username = ref('admin')
const password = ref('admin')
const loading = ref(false)
const error = ref('')

async function submit() {
  if (loading.value) return
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = '请输入账号和密码'
    return
  }
  loading.value = true
  try {
    await login(username.value.trim(), password.value)
    await router.replace('/dashboard')
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-view">
    <div class="login-theme theme-switch">
      <button type="button" :class="{ active: theme === 'light' }" @click="applyTheme('light')">浅色</button>
      <button type="button" :class="{ active: theme === 'dark' }" @click="applyTheme('dark')">深色</button>
    </div>

    <section class="login-card">
      <AppLogo />
      <p class="login-subtitle">管理员控制台 · 请使用管理员账号登录</p>

      <form @submit.prevent="submit">
        <div class="field">
          <label class="field-label" for="login-username">账号</label>
          <input
            id="login-username"
            v-model="username"
            class="input"
            type="text"
            autocomplete="username"
            placeholder="请输入管理员账号"
          />
        </div>
        <div class="field">
          <label class="field-label" for="login-password">密码</label>
          <input
            id="login-password"
            v-model="password"
            class="input"
            type="password"
            autocomplete="current-password"
            placeholder="请输入密码"
          />
        </div>

        <div v-if="error" class="inline-error" role="alert">{{ error }}</div>

        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </form>

      <p class="login-foot">ELEME ADMIN CONSOLE</p>
    </section>
  </div>
</template>
