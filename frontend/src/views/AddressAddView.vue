<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import type { GenderType } from '@/types'

const router = useRouter()
const store = useHungryStore()
const error = ref('')
const saving = ref(false)

const form = reactive({
  address: '',
  contactName: '',
  contactTel: '',
  contactGender: 1 as GenderType,
})

function goBack() {
  router.push('/addresses')
}

async function save() {
  if (!store.isAuthenticated.value) {
    router.push({ path: '/login', query: { redirect: '/addresses/new' } })
    return
  }
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
    const address = await store.createAddress({
      address: form.address.trim(),
      contactName: form.contactName.trim(),
      contactTel: form.contactTel.trim(),
      contactGender: form.contactGender,
    })
    store.setAddress(address.id)
    router.push('/addresses')
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="page page--bare ele-address-page">
    <header class="ele-address-header"><button @click="goBack"><UiIcon name="chevronLeft" :size="22" /></button><h1>新增收货地址</h1><span /></header>
    <main class="ele-address-form-wrap">
      <section class="ele-address-map-card"><i><UiIcon name="pin" :size="25" /></i><div><b>配送到哪里？</b><span>请填写准确地址，方便骑手快速送达</span></div></section>
      <section class="ele-address-form-card">
        <label class="ele-address-field ele-address-field--area"><span>收货地址</span><textarea v-model="form.address" rows="3" maxlength="100" placeholder="例如：天津大学北洋园校区 12 号楼 305室" /></label>
        <label class="ele-address-field"><span>联系人</span><input v-model="form.contactName" type="text" maxlength="20" autocomplete="name" placeholder="请填写收货人姓名" /></label>
        <div class="ele-address-field"><span>性别</span><div class="ele-address-gender"><button :class="{ active: form.contactGender === 1 }" @click="form.contactGender = 1">先生</button><button :class="{ active: form.contactGender === 2 }" @click="form.contactGender = 2">女士</button><button :class="{ active: form.contactGender === 0 }" @click="form.contactGender = 0">保密</button></div></div>
        <label class="ele-address-field"><span>手机号</span><input v-model="form.contactTel" type="tel" maxlength="20" inputmode="tel" autocomplete="tel" placeholder="请填写联系人手机号" /></label>
      </section>
      <p class="ele-address-tip"><UiIcon name="check" :size="13" /> 地址仅用于配送，我们会妥善保护你的隐私</p>
      <p v-if="error" class="ele-store-error">{{ error }}</p>
    </main>
    <footer class="ele-address-footer"><button :disabled="saving" @click="save">{{ saving ? '保存中…' : '保存地址' }}</button></footer>
  </div>
</template>

