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
  <div class="page page--bare ele-address-page">
    <header class="ele-address-header"><button @click="goBack"><UiIcon name="chevronLeft" :size="22" /></button><h1>编辑收货地址</h1><span /></header>
    <main class="ele-address-form-wrap">
      <section class="ele-address-map-card"><i><UiIcon name="pin" :size="25" /></i><div><b>修改配送信息</b><span>准确填写楼栋与房间号，方便骑手送达</span></div></section>
      <section class="ele-address-form-card">
        <label class="ele-address-field ele-address-field--area"><span>收货地址</span><textarea v-model="form.address" rows="3" maxlength="100" placeholder="请输入详细地址" /></label>
        <label class="ele-address-field"><span>联系人</span><input v-model="form.contactName" maxlength="20" autocomplete="name" placeholder="请填写收货人姓名" /></label>
        <div class="ele-address-field"><span>性别</span><div class="ele-address-gender"><button :class="{ active: form.contactGender === 1 }" @click="form.contactGender = 1">先生</button><button :class="{ active: form.contactGender === 2 }" @click="form.contactGender = 2">女士</button><button :class="{ active: form.contactGender === 0 }" @click="form.contactGender = 0">保密</button></div></div>
        <label class="ele-address-field"><span>手机号</span><input v-model="form.contactTel" type="tel" maxlength="20" inputmode="tel" autocomplete="tel" placeholder="请填写联系人手机号" /></label>
      </section>
      <p v-if="error" class="ele-store-error">{{ error }}</p>
      <button v-if="address" class="ele-address-remove" @click="remove"><UiIcon name="trash" :size="17" /> 删除该地址</button>
    </main>
    <footer class="ele-address-footer"><button :disabled="saving" @click="save">{{ saving ? '保存中…' : '保存修改' }}</button></footer>
  </div>
</template>

