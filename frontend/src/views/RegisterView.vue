<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { defaultUser } from '@/data/mock'

const router = useRouter()
const store = useHungryStore()

// 注册页同样只做前端演示，提交后直接写入本地状态。
const form = reactive({
  phone: store.state.user?.phone ?? '',
  password: '',
  confirm: '',
  gender: (store.state.user?.gender ?? 'male') as 'male' | 'female',
})

// 只保留一条错误提示，避免表单过度干扰。
const error = ref('')

// 校验手机号、密码和确认密码后创建本地演示账号。
function submit() {
  if (!/^1\d{10}$/.test(form.phone)) {
    error.value = '请输入正确的手机号码'
    return
  }

  if (form.password.length < 6) {
    error.value = '密码至少需要 6 位'
    return
  }

  if (form.password !== form.confirm) {
    error.value = '两次输入的密码不一致'
    return
  }

  error.value = ''
  store.updateUser({
    name: defaultUser.name,
    phone: form.phone,
    gender: form.gender,
    avatar: defaultUser.avatar,
  })
  router.push('/login')
}
</script>

<template>
  <div class="auth-shell">
    <!-- 注册页头部。 -->
    <SiteHeader title="用户注册" eyebrow="创建演示账号" backable compact @back="router.push('/login')" />

    <!-- 注册说明卡。 -->
    <section class="auth-hero">
      <div class="auth-hero__card">
        <UiIcon name="check" :size="20" />
        <h2 class="auth-hero__title">注册一个本地演示账号</h2>
        <p class="auth-hero__text">注册后可以直接进入订单和个人页，后续接后端时再把这里换成真实接口。</p>
      </div>
    </section>

    <!-- 注册表单。 -->
    <section class="auth-card panel">
      <div class="form-stack">
        <label class="field">
          <span class="field__label">手机号码</span>
          <input v-model="form.phone" class="field__control" type="tel" inputmode="numeric" placeholder="请输入手机号码" />
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
        <button type="button" class="primary-button" @click="submit">注册</button>
        <button type="button" class="secondary-button" @click="router.push('/login')">返回登录</button>
      </div>
    </section>
  </div>
</template>
