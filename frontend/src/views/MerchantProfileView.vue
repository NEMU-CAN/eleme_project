<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { categories } from '@/data/categories'
import { getBusinessHours, saveBusinessHours } from '@/utils/businessHours'

const router = useRouter()
const store = useHungryStore()
const error = ref('')
const success = ref('')
const imageFileName = ref('')
const imageData = ref<string | null>(null)
const deactivating = ref(false)
const merchant = computed(() => store.state.activeMerchantId ? store.getMerchant(store.state.activeMerchantId) : null)
const tasteOptions = computed(() => store.tasteCategories.value.length ? store.tasteCategories.value : categories)
const saving = computed(() => store.state.loading.merchant)

const form = reactive({
  name: '',
  address: '',
  description: '',
  tasteId: '',
  startPrice: 0,
  deliveryPrice: 0,
  status: 1 as 0 | 1,
  openTime: '09:00',
  closeTime: '22:00',
})

function fillForm() {
  if (!merchant.value) {
    return
  }
  form.name = merchant.value.name
  form.address = merchant.value.address
  form.description = merchant.value.description
  form.tasteId = String(merchant.value.tasteId)
  form.startPrice = merchant.value.startPrice
  form.deliveryPrice = merchant.value.deliveryFee
  form.status = merchant.value.status === 'open' ? 1 : 0
  const hours = getBusinessHours(merchant.value.id)
  form.openTime = hours.open
  form.closeTime = hours.close
}

function selectImage(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  imageFileName.value = file?.name ?? ''
  imageData.value = null
  if (!file) {
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    imageData.value = typeof reader.result === 'string' ? reader.result : null
  }
  reader.onerror = () => {
    imageFileName.value = ''
    imageData.value = null
    error.value = '门店图片读取失败，请重新选择'
  }
  reader.readAsDataURL(file)
}

async function initialize() {
  try {
    error.value = ''
    if (!merchant.value) {
      await store.loadMyBusinesses()
    }
    if (!merchant.value) {
      throw new Error('没有找到可管理的门店')
    }
    fillForm()
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

async function saveProfile() {
  if (!merchant.value) {
    error.value = '没有找到可管理的门店'
    return
  }
  if (!form.name.trim()) {
    error.value = '请输入门店名称'
    return
  }
  if (!form.address.trim()) {
    error.value = '请输入门店地址'
    return
  }
  if (!form.tasteId) {
    error.value = '请选择口味分类'
    return
  }
  if (!Number.isFinite(Number(form.startPrice)) || Number(form.startPrice) < 0) {
    error.value = '请输入正确的起送价'
    return
  }
  if (!Number.isFinite(Number(form.deliveryPrice)) || Number(form.deliveryPrice) < 0) {
    error.value = '请输入正确的配送费'
    return
  }

  try {
    error.value = ''
    success.value = ''
    await store.updateManagedBusiness(merchant.value.id, {
      name: form.name.trim(),
      address: form.address.trim(),
      description: form.description.trim() || null,
      image: imageData.value,
      tasteId: Number(form.tasteId),
      startPrice: Number(form.startPrice),
      deliveryPrice: Number(form.deliveryPrice),
      status: form.status,
    })
    saveBusinessHours(merchant.value.id, { open: form.openTime, close: form.closeTime })
    imageFileName.value = ''
    imageData.value = null
    fillForm()
    success.value = '店铺资料已保存'
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

async function deactivateShop() {
  if (!merchant.value) {
    return
  }
  const confirmed = window.confirm('注销店铺为永久操作，账号将从商家转为普通用户，确定继续吗？')
  if (!confirmed) {
    return
  }

  try {
    deactivating.value = true
    error.value = ''
    await store.deactivateBusiness(merchant.value.id)
    router.replace('/me')
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    deactivating.value = false
  }
}

onMounted(() => {
  if (!store.isAuthenticated.value) {
    router.replace({ path: '/login', query: { redirect: '/merchant-profile' } })
    return
  }
  if (store.state.user?.role !== 1 && store.state.user?.role !== 2) {
    router.replace('/me')
    return
  }
  void initialize()
})
</script>

<template>
  <div class="merchant-profile-page">
    <SiteHeader title="店铺资料" backable @back="router.push('/merchant-center')" />

    <section v-if="merchant" class="merchant-profile-preview">
      <img :src="imageData || merchant.image" :alt="merchant.name" />
      <div>
        <strong>{{ form.name || merchant.name }}</strong>
        <p>{{ form.address || merchant.address }}</p>
      </div>
      <span :class="form.status === 1 ? 'status-pill status-pill--success' : 'status-pill'">
        {{ form.status === 1 ? '营业中' : '休息中' }}
      </span>
    </section>

    <section class="merchant-profile-form">
      <div class="field">
        <label class="field__label" for="merchant-name">门店名称</label>
        <input id="merchant-name" v-model="form.name" class="field__control" type="text" maxlength="40" placeholder="请输入门店名称" />
      </div>
      <div class="field field--top">
        <label class="field__label" for="merchant-address">门店地址</label>
        <textarea id="merchant-address" v-model="form.address" class="field__control" rows="2" maxlength="100" placeholder="请输入详细地址" />
      </div>
      <div class="field field--top">
        <label class="field__label" for="merchant-description">门店简介</label>
        <textarea id="merchant-description" v-model="form.description" class="field__control" rows="3" maxlength="255" placeholder="介绍一下你的门店" />
      </div>
      <div class="field">
        <span class="field__label">门店图片</span>
        <div class="file-picker">
          <label class="file-picker__button" for="merchant-image">选择图片</label>
          <span class="file-picker__name">{{ imageFileName || '不选择则保留原图' }}</span>
          <input id="merchant-image" class="file-picker__input" type="file" accept="image/*" @change="selectImage" />
        </div>
      </div>
      <div class="field">
        <label class="field__label" for="merchant-taste">口味分类</label>
        <select id="merchant-taste" v-model="form.tasteId" class="field__control open-shop-select">
          <option value="" disabled>请选择口味分类</option>
          <option v-for="taste in tasteOptions" :key="taste.id" :value="taste.id">{{ taste.name }}</option>
        </select>
      </div>
      <div class="field">
        <label class="field__label" for="merchant-start-price">起送价</label>
        <div class="open-shop-price">
          <span>¥</span>
          <input id="merchant-start-price" v-model.number="form.startPrice" class="field__control" type="number" min="0" step="0.01" />
        </div>
      </div>
      <div class="field">
        <label class="field__label" for="merchant-delivery-price">配送费</label>
        <div class="open-shop-price">
          <span>¥</span>
          <input id="merchant-delivery-price" v-model.number="form.deliveryPrice" class="field__control" type="number" min="0" step="0.01" />
        </div>
      </div>
      <div class="field">
        <span class="field__label">营业状态</span>
        <div class="seg">
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.status === 1 }" @click="form.status = 1">营业中</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.status === 0 }" @click="form.status = 0">休息中</button>
        </div>
      </div>
      <div class="field">
        <label class="field__label" for="merchant-open-time">营业时间</label>
        <div class="open-shop-hours">
          <input id="merchant-open-time" v-model="form.openTime" class="field__control" type="time" />
          <span>至</span>
          <input id="merchant-close-time" v-model="form.closeTime" class="field__control" type="time" />
        </div>
      </div>
    </section>

    <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>
    <p v-else-if="success" class="auth-form__hint auth-form__hint--success">{{ success }}</p>

    <div class="form-actions">
      <button type="button" class="primary-button" :disabled="saving || !merchant" @click="saveProfile">
        {{ saving ? '保存中' : '保存资料' }}
      </button>
    </div>

    <button type="button" class="delete-account-button" :disabled="deactivating || !merchant" @click="deactivateShop">
      {{ deactivating ? '正在注销' : '注销店铺' }}
    </button>
  </div>
</template>
