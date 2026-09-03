<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'

const router = useRouter()
const route = useRoute()
const store = useHungryStore()

// 注册表单字段与后端 UserCreateRequest 对齐。
const form = reactive({
  userId: store.state.user?.id ?? '',
  userName: store.state.user?.name ?? '',
  password: '',
  confirm: '',
  gender: (store.state.user?.gender ?? 'male') as 'male' | 'female',
})

// 只保留一条错误提示，避免表单过度干扰。
const error = ref('')
const loading = ref(false)

// 校验字段后创建后端用户。
async function submit() {
  if (!form.userId.trim()) {
    error.value = '请输入用户编号'
    return
  }

  if (form.userId.length > 20) {
    error.value = '用户编号不能超过 20 位'
    return
  }

  if (!form.userName.trim()) {
    error.value = '请输入用户姓名'
    return
  }

  if (form.userName.length > 20) {
    error.value = '用户姓名不能超过 20 位'
    return
  }

  if (!form.password || form.password.length > 20) {
    error.value = '请输入 1-20 位密码'
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
      userId: form.userId.trim(),
      password: form.password,
      userName: form.userName.trim(),
      userSex: form.gender === 'female' ? 0 : 1,
      userImg: null,
    })
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
    <!-- 注册页头部。 -->
    <SiteHeader title="用户注册" eyebrow="创建后端账号" backable compact @back="router.push('/login')" />

    <!-- 注册说明卡。 -->
    <section class="auth-hero">
      <div class="auth-hero__card">
        <UiIcon name="check" :size="20" />
        <h2 class="auth-hero__title">注册后端账号</h2>
        <p class="auth-hero__text">提交后会调用用户创建接口，并用返回的账户资料进入个人页。</p>
      </div>
    </section>

    <!-- 注册表单。 -->
    <section class="auth-card panel">
      <div class="form-stack">
        <label class="field">
          <span class="field__label">用户编号</span>
          <input v-model="form.userId" class="field__control" type="text" autocomplete="username" placeholder="请输入用户编号" />
        </label>
        <label class="field">
          <span class="field__label">用户姓名</span>
          <input v-model="form.userName" class="field__control" type="text" autocomplete="name" placeholder="请输入用户姓名" />
        </label>
        <label class="field">
          <span class="field__label">密码</span>
          <input v-model="form.password" class="field__control" type="password" placeholder="设置密码" />
        </label>
        <label class="field">
          <span class="field__label">确认密码</span>
          <input v-model="form.confirm" class="field__control" type="password" placeholder="再次输入密码" />
        </label>
        <div class="field">
          <span class="field__label">性别</span>
          <div class="chip-row">
            <button type="button" class="chip" :class="{ 'chip--active': form.gender === 'male' }" @click="form.gender = 'male'">男</button>
            <button type="button" class="chip" :class="{ 'chip--active': form.gender === 'female' }" @click="form.gender = 'female'">女</button>
          </div>
        </div>
      </div>

      <p v-if="error" class="field__hint" style="color: var(--danger); margin-top: 12px">{{ error }}</p>

      <div class="auth-card__footer" style="margin-top: 16px">
        <button type="button" class="primary-button" :disabled="loading" @click="submit">
          {{ loading ? '注册中' : '注册' }}
        </button>
        <button type="button" class="secondary-button" @click="router.push('/login')">返回登录</button>
      </div>
    </section>
  </div>
</template>
