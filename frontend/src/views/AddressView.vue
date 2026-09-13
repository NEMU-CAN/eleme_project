<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { maskPhone } from '@/utils/format'

const router = useRouter()
const store = useHungryStore()
const error = ref('')

const addresses = computed(() => store.state.addresses)
const activeId = computed(() => store.state.addressId)

onMounted(async () => {
  if (!store.isAuthenticated.value) {
    router.replace({ path: '/login', query: { redirect: '/addresses' } })
    return
  }
  try {
    error.value = ''
    await store.loadAddresses()
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
})

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/me')
  }
}

function addAddress() {
  router.push('/addresses/new')
}

function editAddress(id: string) {
  router.push(`/addresses/${id}/edit`)
}

function choose(id: string) {
  store.setAddress(id)
}
</script>

<template>
  <div class="page page--bare ele-address-page">
    <header class="ele-address-header">
      <button @click="goBack"><UiIcon name="chevronLeft" :size="22" /></button>
      <h1>我的收货地址</h1>
      <span />
    </header>
    <main class="ele-address-list-wrap">
      <p class="ele-address-list-title">选择收货地址</p>
      <p v-if="error" class="ele-store-error">{{ error }}</p>
      <div v-if="store.state.loading.addresses" class="ele-empty">正在加载地址…</div>
      <section v-else-if="addresses.length" class="ele-address-list">
        <button v-for="address in addresses" :key="address.id" class="ele-address-item" :class="{ active: address.id === activeId }" @click="choose(address.id)">
          <i class="ele-address-radio"><UiIcon v-if="address.id === activeId" name="check" :size="12" /></i>
          <div>
            <h2>{{ address.detail || address.address }}</h2>
            <p>{{ address.name }} {{ maskPhone(address.phone) }}</p>
            <span v-if="address.id === activeId">当前选择</span>
          </div>
          <span class="ele-address-edit" @click.stop="editAddress(address.id)"><UiIcon name="edit" :size="18" /></span>
        </button>
      </section>
      <div v-else class="ele-address-empty">
        <i><UiIcon name="pin" :size="34" /></i>
        <b>还没有收货地址</b>
        <span>添加地址后，下单配送会更方便</span>
      </div>
    </main>
    <footer class="ele-address-footer"><button @click="addAddress"><UiIcon name="plus" :size="18" /> 新增收货地址</button></footer>
  </div>
</template>

