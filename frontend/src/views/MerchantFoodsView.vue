<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import SiteHeader from '@/components/SiteHeader.vue'
import UiIcon from '@/components/UiIcon.vue'
import { useHungryStore } from '@/composables/useHungryStore'
import { formatCny } from '@/utils/format'
import type { MenuItem } from '@/types'

const router = useRouter()
const store = useHungryStore()
const foods = ref<MenuItem[]>([])
const error = ref('')
const success = ref('')
const showForm = ref(false)
const editingId = ref('')
const imageFileName = ref('')
const imageData = ref<string | null>(null)
const statusUpdatingId = ref('')

const merchant = computed(() => store.state.activeMerchantId ? store.getMerchant(store.state.activeMerchantId) : null)
const saving = computed(() => store.state.loading.action)
const formTitle = computed(() => editingId.value ? '编辑菜品' : '新增菜品')

const form = reactive({
  name: '',
  description: '',
  price: 0,
  stock: 0,
  status: 1 as 0 | 1,
})

function clearForm() {
  editingId.value = ''
  imageFileName.value = ''
  imageData.value = null
  form.name = ''
  form.description = ''
  form.price = 0
  form.stock = 0
  form.status = 1
}

function openCreate() {
  clearForm()
  error.value = ''
  success.value = ''
  showForm.value = true
}

function openEdit(food: MenuItem) {
  editingId.value = food.id
  imageFileName.value = ''
  imageData.value = null
  form.name = food.name
  form.description = food.description
  form.price = food.price
  form.stock = food.stock + food.reservedStock
  form.status = food.status === 'online' ? 1 : 0
  error.value = ''
  success.value = ''
  showForm.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function closeForm() {
  showForm.value = false
  clearForm()
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
    error.value = '菜品图片读取失败，请重新选择'
  }
  reader.readAsDataURL(file)
}

async function loadFoods() {
  try {
    error.value = ''
    if (!merchant.value) {
      await store.loadMyBusinesses()
    }
    if (!merchant.value) {
      throw new Error('没有找到可管理的门店')
    }
    foods.value = await store.loadManagedFoods(merchant.value.id)
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

async function saveFood() {
  if (!merchant.value) {
    error.value = '没有找到可管理的门店'
    return
  }
  if (!form.name.trim()) {
    error.value = '请输入菜品名称'
    return
  }
  if (!Number.isFinite(Number(form.price)) || Number(form.price) < 0) {
    error.value = '请输入正确的菜品价格'
    return
  }
  if (!Number.isInteger(Number(form.stock)) || Number(form.stock) < 0) {
    error.value = '库存必须是大于或等于 0 的整数'
    return
  }
  const editingFood = foods.value.find((food) => food.id === editingId.value)
  if (editingFood && Number(form.stock) < editingFood.reservedStock) {
    error.value = `总库存不能小于已预占库存 ${editingFood.reservedStock}`
    return
  }

  try {
    error.value = ''
    success.value = ''
    const payload = {
      name: form.name.trim(),
      description: form.description.trim() || null,
      image: imageData.value,
      price: form.price,
      businessId: Number(merchant.value.id),
      stock: form.stock,
      status: form.status,
    }
    if (editingId.value) {
      await store.updateManagedFood(editingId.value, payload)
      success.value = '菜品已更新'
    } else {
      await store.createManagedFood(payload)
      success.value = '菜品已新增'
    }
    closeForm()
    await loadFoods()
  } catch (cause) {
    error.value = store.messageFromError(cause)
  }
}

async function toggleStatus(food: MenuItem) {
  const nextStatus: 0 | 1 = food.status === 'online' ? 0 : 1
  const action = nextStatus === 1 ? '上架' : '下架'
  if (!window.confirm(`确定要${action}“${food.name}”吗？`)) {
    return
  }

  try {
    statusUpdatingId.value = food.id
    error.value = ''
    success.value = ''
    await store.updateManagedFoodStatus(food.id, nextStatus)
    success.value = `菜品已${action}`
    await loadFoods()
  } catch (cause) {
    error.value = store.messageFromError(cause)
  } finally {
    statusUpdatingId.value = ''
  }
}

onMounted(() => {
  if (!store.isAuthenticated.value) {
    router.replace({ path: '/login', query: { redirect: '/merchant-foods' } })
    return
  }
  if (store.state.user?.role !== 1 && store.state.user?.role !== 2) {
    router.replace('/me')
    return
  }
  void loadFoods()
})
</script>

<template>
  <div class="merchant-foods-page">
    <SiteHeader title="菜品管理" backable @back="router.push('/merchant-center')" />

    <section class="merchant-foods-toolbar">
      <div>
        <strong>{{ merchant?.name || '我的门店' }}</strong>
        <p>共 {{ foods.length }} 个菜品</p>
      </div>
      <button type="button" class="primary-button" @click="openCreate">
        <UiIcon name="plus" :size="16" />
        新增菜品
      </button>
    </section>

    <section v-if="showForm" class="merchant-food-form">
      <div class="merchant-food-form__heading">
        <h2>{{ formTitle }}</h2>
        <button type="button" aria-label="关闭" @click="closeForm"><UiIcon name="close" :size="18" /></button>
      </div>
      <div class="field">
        <label class="field__label" for="food-name">菜品名称</label>
        <input id="food-name" v-model="form.name" class="field__control" type="text" maxlength="30" placeholder="请输入菜品名称" />
      </div>
      <div class="field field--top">
        <label class="field__label" for="food-description">菜品简介</label>
        <textarea id="food-description" v-model="form.description" class="field__control" rows="2" maxlength="255" placeholder="选填" />
      </div>
      <div class="field">
        <span class="field__label">菜品图片</span>
        <div class="file-picker">
          <label class="file-picker__button" for="food-image">选择图片</label>
          <span class="file-picker__name">{{ imageFileName || (editingId ? '不选择则保留原图' : '未选择图片') }}</span>
          <input id="food-image" class="file-picker__input" type="file" accept="image/*" @change="selectImage" />
        </div>
      </div>
      <div class="field">
        <label class="field__label" for="food-price">价格</label>
        <input id="food-price" v-model.number="form.price" class="field__control" type="number" min="0" step="0.01" />
      </div>
      <div class="field">
        <label class="field__label" for="food-stock">总库存</label>
        <input id="food-stock" v-model.number="form.stock" class="field__control" type="number" min="0" step="1" />
      </div>
      <div class="field">
        <span class="field__label">菜品状态</span>
        <div class="seg">
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.status === 1 }" @click="form.status = 1">上架</button>
          <button type="button" class="seg__item" :class="{ 'seg__item--active': form.status === 0 }" @click="form.status = 0">下架</button>
        </div>
      </div>
      <div class="merchant-food-form__actions">
        <button type="button" class="secondary-button" @click="closeForm">取消</button>
        <button type="button" class="primary-button" :disabled="saving" @click="saveFood">{{ saving ? '保存中' : '保存菜品' }}</button>
      </div>
    </section>

    <p v-if="error" class="auth-form__hint auth-form__hint--danger">{{ error }}</p>
    <p v-else-if="success" class="auth-form__hint auth-form__hint--success">{{ success }}</p>

    <section v-if="store.state.loading.merchant && !foods.length" class="empty-state">
      <p class="empty-state__text">正在加载菜品…</p>
    </section>

    <section v-else-if="foods.length" class="merchant-food-list">
      <article v-for="food in foods" :key="food.id" class="merchant-food-card">
        <img :src="food.image" :alt="food.name" />
        <div class="merchant-food-card__body">
          <div class="merchant-food-card__title">
            <strong>{{ food.name }}</strong>
            <span :class="food.status === 'online' ? 'status-pill status-pill--success' : 'status-pill'">
              {{ food.status === 'online' ? '已上架' : '已下架' }}
            </span>
          </div>
          <p>{{ food.description || '暂无简介' }}</p>
          <strong class="merchant-food-card__price">{{ formatCny(food.price) }}</strong>
          <div class="merchant-food-card__stock">
            <span>总库存 {{ food.stock + food.reservedStock }}</span>
            <span>预占 {{ food.reservedStock }}</span>
            <span>可售 {{ food.stock }}</span>
          </div>
        </div>
        <div class="merchant-food-card__actions">
          <button type="button" class="secondary-button" @click="openEdit(food)">编辑</button>
          <button type="button" class="secondary-button" :disabled="statusUpdatingId === food.id" @click="toggleStatus(food)">
            {{ statusUpdatingId === food.id ? '处理中' : food.status === 'online' ? '下架' : '上架' }}
          </button>
        </div>
      </article>
    </section>

    <section v-else class="empty-state">
      <p class="empty-state__title">暂无菜品</p>
      <p class="empty-state__text">点击“新增菜品”创建门店的第一个菜品。</p>
    </section>
  </div>
</template>
