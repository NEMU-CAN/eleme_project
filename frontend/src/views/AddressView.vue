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
  <div class="address-page">
    <div style="position: sticky; top: 0; z-index: 40; background: var(--bg)">
      <header class="site-header">
        <button type="button" class="site-header__back" @click="goBack">
          <UiIcon name="chevronLeft" :size="20" />
        </button>
        <h1 class="site-header__title" style="text-align: left">我的地址</h1>
      </header>
      <button type="button" class="address-add" @click="addAddress">
        <UiIcon name="plus" :size="16" />
        新增收货地址
      </button>
    </div>

    <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>

    <div v-if="store.state.loading.addresses" class="address-empty">正在加载地址…</div>

    <div v-else-if="addresses.length" class="address-list">
      <button
        v-for="address in addresses"
        :key="address.id"
        type="button"
        class="address-item"
        @click="choose(address.id)"
      >
        <div class="address-item__top">
          <div style="flex: 1; min-width: 0">
            <p class="address-item__name">{{ address.detail || address.address }}</p>
            <p class="address-item__contact">{{ address.name }} · {{ maskPhone(address.phone) }}</p>
          </div>
          <span class="address-item__edit" @click.stop="editAddress(address.id)">编辑</span>
        </div>
        <p v-if="address.id === activeId" class="address-item__active">
          <UiIcon name="check" :size="12" /> 默认地址
        </p>
      </button>
    </div>

    <div v-else class="address-empty">还没有收货地址，点击上方按钮新增。</div>
  </div>
</template>
