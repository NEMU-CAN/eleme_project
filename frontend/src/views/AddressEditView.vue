<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import type { GenderType } from '@/types'

const route = useRoute()
const router = useRouter()
const store = useHungryStore()
const error = ref('')
const saving = ref(false)

const addressId = computed(() => String(route.params.addressId || ''))
const address = computed(() => store.state.addresses.find((item) => item.id === addressId.value) ?? null)

const form = reactive({
  address: '',
  contactName: '',
  contactTel: '',
  contactGender: 1 as GenderType,
})

onMounted(async () => {
  if (!store.isAuthenticated.value) {
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await store.loadAddresses()
    const target = store.state.addresses.find((item) => item.id === addressId.value)
    if (target) {
      form.address = target.detail || target.address
      form.contactName = target.name || target.contactName
      form.contactTel = target.phone || target.contactTel
      form.contactGender = target.sex ?? target.contactGender ?? 1
    }
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
})

function goBack() {
  router.push('/addresses')
}

async function save() {
  if (!form.address.trim()) {
    error.value = '请输入收货地址'
    return
  }
  if (!form.contactName.trim()) {
    error.value = '请输入收货人姓名'
    return
  }
  if (!form.contactTel.trim()) {
    error.value = '请输入手机号'
    return
  }
  try {
    saving.value = true
    error.value = ''
    await store.updateAddress(addressId.value, {
      address: form.address.trim(),
      contactName: form.contactName.trim(),
      contactTel: form.contactTel.trim(),
      contactGender: form.contactGender,
    })
    router.push('/addresses')
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    saving.value = false
  }
}

async function remove() {
  try {
    error.value = ''
    await store.removeAddress(addressId.value)
    router.push('/addresses')
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}
</script>

<template>
  <div class="form-page">
    <header class="site-header">
      <button type="button" class="site-header__back" @click="goBack">
        <UiIcon name="chevronLeft" :size="20" />
      </button>
      <h1 class="site-header__title" style="text-align: left">编辑地址</h1>
    </header>

    <div class="form-card">
      <div class="field" style="align-items: flex-start">
        <span class="field__label">地址</span>
        <textarea v-model="form.address" class="field__control" rows="2" placeholder="请输入详细地址" />
      </div>
      <div class="field">
        <span class="field__label">收货人</span>
        <input v-model="form.contactName" class="field__control" type="text" placeholder="请输入姓名" />
        <div class="seg">
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.contactGender === 1 }" @click="form.contactGender = 1">先生</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.contactGender === 2 }" @click="form.contactGender = 2">女士</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.contactGender === 0 }" @click="form.contactGender = 0">保密</button>
        </div>
      </div>
      <div class="field">
        <span class="field__label">手机号</span>
        <input v-model="form.contactTel" class="field__control" type="tel" placeholder="请输入手机号" />
      </div>
    </div>

    <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>

    <div class="form-actions">
      <button type="button" class="secondary-button" @click="remove">删除</button>
      <button type="button" class="primary-button primary-button--accent" :disabled="saving" @click="save">
        {{ saving ? '保存中' : '保存' }}
      </button>
    </div>
  </div>
</template>
